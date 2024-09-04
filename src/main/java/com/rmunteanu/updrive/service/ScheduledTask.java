package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ScheduledTask {

    private final FileRepository fileRepository;
    private final Environment environment;

    @Scheduled(fixedRate = 5000)
    public void deleteExpiredFiles() {
        List<FileMetadata> expiredFileMetadata = fileRepository.readByExpirationDate();
        if (!expiredFileMetadata.isEmpty()) {
            expiredFileMetadata.forEach(fm -> fm.setExpired(true));
            fileRepository.saveAll(expiredFileMetadata);
        }
        String dataFolder = Objects.requireNonNull(environment.getProperty("file.data.directory"));
        try {
            for (FileMetadata fileMetadata : expiredFileMetadata) {
                FileUtils.deleteDirectory(Path.of(dataFolder, fileMetadata.getSlotId()).toFile());
            }
        } catch (IOException e) {
            System.out.println("Failed to delete expired files." + e.getMessage());
        }
    }
}
