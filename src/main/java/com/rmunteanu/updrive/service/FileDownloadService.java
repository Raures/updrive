package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
public class FileDownloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadService.class);

    private final FileRepository fileRepository;
    private final Environment environment;

    FileDownloadService(@Autowired FileRepository fileRepository, @Autowired Environment environment) {
        this.fileRepository = fileRepository;
        this.environment = environment;
    }

    public Path downloadFiles(String slotId) {
        FileMetadata fileMetadata = fileRepository.readBySlotId(slotId);
        Path filePath;
        if (fileMetadata == null) {
            LOGGER.error("Slot ID does not exist.");
            throw new RuntimeException("Slot ID does not exist.");
        }
        if (fileMetadata.isExpired()) {
            LOGGER.error("Slot has expired on {}.", fileMetadata.getExpirationDate());
            throw new RuntimeException("Slot has expired on " + fileMetadata.getExpirationDate() + ".");
        }
        String dataFolder = Objects.requireNonNull(environment.getProperty("file.data.directory"));
        Path filesPath = Path.of(dataFolder, slotId);
        if (Files.exists(filesPath)) {
            File filesFolder = filesPath.toFile();
            File[] files = filesFolder.listFiles();
            if (files.length == 0) {
                LOGGER.error("Slot ID exists, but no files were found.");
                throw new RuntimeException("Slot ID exists, but no files were found.");
            } else if (files.length == 1) {
                filePath = files[0].toPath();
            } else {
                LOGGER.error("Slot ID exists but something must've gone wrong during the upload process. Too many files!");
                throw new RuntimeException("Slot ID exists but something must've gone wrong during the upload process. Too many files!");
            }
        } else {
            LOGGER.error("Path does not exist: {}", filesPath);
            throw new RuntimeException("Path does not exist: " + filesPath);
        }
        return filePath;
    }

}
