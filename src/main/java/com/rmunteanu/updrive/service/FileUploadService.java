package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.dto.DownloadLinkDTO;
import com.rmunteanu.updrive.dto.FileDTO;
import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.dto.UploadSlotDTO;
import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileRepository fileRepository;
    private final Environment environment;

    private void validate(FileMetadataDTO fileMetadataDTO) {
        if (fileMetadataDTO.files().length < 1) {
            throw new RuntimeException("No files provided.");
        }
        for (FileDTO fileDTO : fileMetadataDTO.files()) {
            String[] fileNameSplit = fileDTO.name().split("\\.");
            if (fileNameSplit.length < 2) {
                throw new RuntimeException("File name is invalid, either the name or the extension is missing.");
            }
            if (fileDTO.sizeMB() > 10) {
                throw new RuntimeException("File size can not be greater than 10 MB");
            }
        }
    }

    private FileMetadata transform(FileMetadataDTO fileMetadataDTO) {
        FileMetadata fileMetadata = new FileMetadata();
        if (fileMetadataDTO.availabilityTime() == null || fileMetadataDTO.availabilityTime() == 0) {
            int defaultAvailabilityTime = Integer.parseInt(Objects.requireNonNull(environment.getProperty("file.available.time")));
            System.out.println("No availability time provided, using default: " + defaultAvailabilityTime + " hours.");
            fileMetadata.setAvailabilityTime(defaultAvailabilityTime);
        } else if (fileMetadataDTO.availabilityTime() > 6000) {
            throw new RuntimeException("Availability time can not be greater than 6000!");
        } else if (fileMetadataDTO.availabilityTime() <= 0) {
            throw new RuntimeException("Availability time can not be less or equal to 0!");
        } else {
            fileMetadata.setAvailabilityTime(fileMetadataDTO.availabilityTime());
        }
        fileMetadata.setActive(true);
        return fileMetadata;
    }

    private UploadSlotDTO store(FileMetadata fileMetadata) {
        fileRepository.save(fileMetadata);
        return new UploadSlotDTO(fileMetadata.getSlotId());
    }

    public UploadSlotDTO uploadFileMetadata(FileMetadataDTO fileMetadataDTO) {
        validate(fileMetadataDTO);
        FileMetadata fileMetadata = transform(fileMetadataDTO);
        String slotId = UUID.randomUUID().toString();
        fileMetadata.setSlotId(slotId);
        return store(fileMetadata);
    }

    private DownloadLinkDTO createDownloadLink(String uploadName, String slotId) {
        String url = "http://localhost:8080/api/v1/download/" + slotId;
        return new DownloadLinkDTO(uploadName, url);
    }

    private String getArchiveNameTemplate() {
        return "updrive_%s_%s.zip";
    }

    public DownloadLinkDTO uploadFiles(MultipartFile[] files, String slotId) {
        FileMetadata fileMetadata = fileRepository.readBySlotId(slotId);
        String uploadName;
        int numFiles = files.length;
        if (fileMetadata != null) {
            if (!fileMetadata.isActive()) {
                throw new RuntimeException("The slot is no longer available. Please create a new upload request.");
            }
            if (numFiles > 0 && files[0].getSize() > 0) {
                String dataFolder = Objects.requireNonNull(environment.getProperty("file.data.directory"));
                Path slotFolder = Path.of(dataFolder, slotId);
                try {
                    // Create UUID folder in "data" folder
                    Path folder = Files.createDirectory(slotFolder);
                    if (numFiles == 1) {
                        uploadName = handleSingleFileUpload(files, folder);
                    } else {
                        uploadName = handleManyFilesUpload(files, folder);
                    }
                    fileMetadata.setActive(false);
                    computeExpirationDate(fileMetadata);
                    fileRepository.save(fileMetadata);
                } catch (Exception e) {
                    try {
                        boolean deleted = Files.deleteIfExists(slotFolder.toAbsolutePath());
                        System.out.println("Deleted path: " + deleted);
                    } catch (IOException f) {
                        // Do nothing
                    }
                    throw new RuntimeException("Failed to create a new directory.", e);
                }
            } else {
                throw new RuntimeException("No files were provided.");
            }
        } else {
            throw new RuntimeException("Slot ID does not exist.");
        }
        return createDownloadLink(uploadName, slotId);
    }

    private String handleSingleFileUpload(MultipartFile[] files, Path folder) throws IOException {
        // Save file in the UUID folder
        MultipartFile file = files[0];
        Files.copy(file.getInputStream(), Path.of(folder.toString(), file.getOriginalFilename()));
        return file.getOriginalFilename();
    }

    private String handleManyFilesUpload(MultipartFile[] files, Path folder) throws IOException {
        String uploadName;
        // Save all files in the UUID folder
        for (MultipartFile file : files) {
            Files.copy(file.getInputStream(), Path.of(folder.toString(), file.getOriginalFilename()));
        }
        // Create archive in UUID folder and place the files inside
        String firstFileName = files[0].getOriginalFilename().split("\\.")[0];
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("y_M_d_H_m"));
        File zipArchive = new File(folder.toString(), getArchiveNameTemplate().formatted(firstFileName, currentDate));
        uploadName = zipArchive.getName();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipArchive))) {
            File[] savedFiles = folder.toFile().listFiles();
            assert savedFiles != null;
            for (File file : savedFiles) {
                if (zipArchive.getName().equals(file.getName())) {
                    continue;
                }
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fileInputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                    zipOutputStream.closeEntry();
                }
            }
            for (File file : savedFiles) {
                if (zipArchive.getName().equals(file.getName())) {
                    continue;
                }
                Files.delete(file.toPath());
            }
        }
        return uploadName;
    }

    private void computeExpirationDate(FileMetadata fileMetadata) {
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(fileMetadata.getAvailabilityTime());
        fileMetadata.setExpirationDate(expirationDate);
    }
}
