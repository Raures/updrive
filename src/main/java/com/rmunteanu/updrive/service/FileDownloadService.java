package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileRepository fileRepository;
    private final Environment environment;

    public Path downloadFiles(String slotId) {
        FileMetadata fileMetadata = fileRepository.readBySlotId(slotId);
        Path filePath;
        if (fileMetadata == null) {
            throw new RuntimeException("Slot ID does not exist.");
        }
        String dataFolder = Objects.requireNonNull(environment.getProperty("file.data.directory"));
        Path filesPath = Path.of(dataFolder, slotId);
        if (Files.exists(filesPath)) {
            File filesFolder = filesPath.toFile();
            File[] files = filesFolder.listFiles();
            if (files.length == 0) {
                throw new RuntimeException("Slot ID exists, but no files were found.");
            } else if (files.length == 1) {
                filePath = files[0].toPath();
            } else {
                throw new RuntimeException("Slot ID exists but something must've gone wrong during the upload process. Too many files!");
            }
        } else {
            throw new RuntimeException("Path does not exist: " + filesPath);
        }
        return filePath;
    }

}
