package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.configuration.UploadConfiguration;
import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileMetadataRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class ScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTask.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final UploadConfiguration uploadConfiguration;

    ScheduledTask(@Autowired FileMetadataRepository fileMetadataRepository, @Autowired UploadConfiguration uploadConfiguration) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadConfiguration = uploadConfiguration;
    }

    @Scheduled(fixedRate = 30000)
    public void deleteExpiredFiles() {
        List<FileMetadata> expiredFileMetadata = fileMetadataRepository.readByExpirationDate();
        if (!expiredFileMetadata.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Identified {} expired files to be deleted.", expiredFileMetadata.size());
                LOGGER.debug("Saving expired status to the database.");
            }
            expiredFileMetadata.forEach(fm -> fm.setExpired(true));
            fileMetadataRepository.saveAll(expiredFileMetadata);
            String dataFolder = uploadConfiguration.getDataDirectory();
            try {
                for (FileMetadata fileMetadata : expiredFileMetadata) {
                    FileUtils.deleteDirectory(Path.of(dataFolder, fileMetadata.getSlotId()).toFile());
                }
            } catch (IOException e) {
                LOGGER.error("An error occurred while deleting expired files.", e);
            }
        }
    }
}
