package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.configuration.UploadConfiguration;
import com.rmunteanu.updrive.controller.exception.NoFileFoundRuntimeException;
import com.rmunteanu.updrive.controller.exception.SlotExpiredRuntimeException;
import com.rmunteanu.updrive.controller.exception.SlotNotFoundRuntimeException;
import com.rmunteanu.updrive.controller.exception.TooManyFilesRuntimeException;
import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileDownloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadService.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final UploadConfiguration uploadConfiguration;

    FileDownloadService(@Autowired FileMetadataRepository fileMetadataRepository, @Autowired UploadConfiguration uploadConfiguration) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadConfiguration = uploadConfiguration;
    }

    public Path downloadFiles(String slotId) {
        FileMetadata fileMetadata = fileMetadataRepository.readBySlotId(slotId);
        Path filePath;
        if (fileMetadata == null) {
            LOGGER.error("Slot ID {} does not exist.", slotId);
            throw new SlotNotFoundRuntimeException("Slot ID %s does not exist.", slotId);
        }
        if (fileMetadata.isExpired()) {
            LOGGER.error("Slot has expired on {}.", fileMetadata.getExpirationDate());
            throw new SlotExpiredRuntimeException("Slot has expired on %s.", fileMetadata.getExpirationDate());
        }
        String dataFolder = uploadConfiguration.getDataDirectory();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading contents of folder: {}", dataFolder);
        }
        Path filesPath = Path.of(dataFolder, slotId);
        if (Files.exists(filesPath)) {
            File filesFolder = filesPath.toFile();
            File[] files = filesFolder.listFiles();
            if (files == null || files.length == 0) {
                LOGGER.error("Slot ID exists, but no files were found.");
                throw new NoFileFoundRuntimeException("Slot ID exists, but no files were found.");
            } else if (files.length == 1) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Returning file for slot ID {}.", slotId);
                }
                filePath = files[0].toPath();
            } else {
                LOGGER.error("Should read exactly one file for a successful file upload. Slot ID {} returned {} files.", slotId, files.length);
                throw new TooManyFilesRuntimeException("Slot ID exists but something must've gone wrong during the upload process. Too many files!");
            }
        } else {
            LOGGER.error("Path does not exist: {}", filesPath);
            throw new NoFileFoundRuntimeException("Path does not exist: %s", filesPath);
        }
        return filePath;
    }

}
