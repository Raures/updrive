package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.configuration.UploadConfiguration;
import com.rmunteanu.updrive.controller.exception.*;
import com.rmunteanu.updrive.dto.*;
import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

    private final FileRepository fileRepository;
    private final UploadConfiguration uploadConfiguration;

    FileUploadService(@Autowired FileRepository fileRepository, @Autowired UploadConfiguration uploadConfiguration) {
        this.fileRepository = fileRepository;
        this.uploadConfiguration = uploadConfiguration;
    }

    private void validate(FileMetadataDTO fileMetadataDTO) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Validating file metadata: {}.", fileMetadataDTO);
        }
        if (fileMetadataDTO.files().length < 1) {
            LOGGER.error("No files provided.");
            throw new NoFileProvidedRuntimeException("No files were provided.");
        }
        for (FileDTO fileDTO : fileMetadataDTO.files()) {
            String[] fileNameSplit = fileDTO.name().split("\\.");
            if (fileNameSplit.length < 2) {
                LOGGER.error("File name is invalid, either the name or the extension is missing.");
                throw new InvalidMetadataRuntimeException("File name is invalid, either the name or the extension is missing: %s.", fileDTO.name());
            }
            if (fileDTO.sizeMB() > uploadConfiguration.getSizeMax()) {
                LOGGER.error("File size can not be greater than {} MB.", uploadConfiguration.getSizeMax());
                throw new InvalidMetadataRuntimeException("File size can not be greater than %d MB, your file %s exceeds this limit.", uploadConfiguration.getSizeMax(), fileDTO.name());
            }
        }
    }

    private FileMetadata transform(FileMetadataDTO fileMetadataDTO) {
        FileMetadata fileMetadata = new FileMetadata();
        if (fileMetadataDTO.availabilityTime() == null || fileMetadataDTO.availabilityTime() == 0) {
            int defaultAvailabilityTime = uploadConfiguration.getAvailableTime();
            LOGGER.info("No availability time provided, using default: {} hours", defaultAvailabilityTime);
            fileMetadata.setAvailabilityTime(defaultAvailabilityTime);
        } else if (fileMetadataDTO.availabilityTime() > 6000) {
            LOGGER.error("Desired availability time exceeds 6000!");
            throw new InvalidMetadataRuntimeException("Availability time can not be greater than 6000!");
        } else if (fileMetadataDTO.availabilityTime() <= 0) {
            LOGGER.error("Desired availability time is less than 0!");
            throw new InvalidMetadataRuntimeException("Availability time can not be less or equal to 0!");
        } else {
            fileMetadata.setAvailabilityTime(fileMetadataDTO.availabilityTime());
        }
        fileMetadata.setActive(true);
        return fileMetadata;
    }

    private UploadSlotDTO store(FileMetadata fileMetadata) {
        fileRepository.save(fileMetadata);
        LinkDTO[] links = new LinkDTO[1];
        links[0] = new LinkDTO("http://localhost:8080/api/v1/upload/files/" + fileMetadata.getSlotId(), "upload-files", HttpMethod.POST.name());
        return new UploadSlotDTO(fileMetadata.getSlotId(), links);
    }

    public UploadSlotDTO uploadFileMetadata(FileMetadataDTO fileMetadataDTO) {
        validate(fileMetadataDTO);
        FileMetadata fileMetadata = transform(fileMetadataDTO);
        String slotId = UUID.randomUUID().toString();
        fileMetadata.setSlotId(slotId);
        return store(fileMetadata);
    }

    private DownloadLinkDTO createDownloadLink(String uploadName, String slotId) {
        LinkDTO[] links = new LinkDTO[1];
        links[0] = new LinkDTO("http://localhost:8080/api/v1/download/" + slotId, "download-files", HttpMethod.GET.name());
        return new DownloadLinkDTO(uploadName, links);
    }

    public DownloadLinkDTO uploadFiles(MultipartFile[] files, String slotId) {
        FileMetadata fileMetadata = fileRepository.readBySlotId(slotId);
        String uploadName;
        int numFiles = files.length;
        if (fileMetadata != null) {
            if (!fileMetadata.isActive()) {
                LOGGER.error("The slot is no longer available. Please create a new upload request.");
                throw new SlotConsumedException("The slot ID %s is no longer available. Please create a new upload request.", slotId);
            }
            if (numFiles > 0 && files[0].getSize() > 0) {
                String dataFolder = uploadConfiguration.getDataDirectory();
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
                        LOGGER.error("Deleted path: {}.", deleted);
                    } catch (IOException f) {
                        LOGGER.error("Failed to delete path.", f);
                    }
                    LOGGER.error("Failed to create a new directory.", e);
                    throw new InternalErrorRuntimeException("The server encountered an error. Please try again.");
                }
            } else {
                LOGGER.error("No files were provided.");
                throw new NoFileProvidedRuntimeException("No files were provided.");
            }
        } else {
            LOGGER.error("Slot ID does not exist.");
            throw new SlotNotFoundRuntimeException("Slot ID does not exist.");
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
        File zipArchive = new File(folder.toString(), "updrive_%s_%s.zip".formatted(firstFileName, currentDate));
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
