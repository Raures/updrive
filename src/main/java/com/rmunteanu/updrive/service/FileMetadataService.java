package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.configuration.UploadConfiguration;
import com.rmunteanu.updrive.controller.exception.InvalidMetadataRuntimeException;
import com.rmunteanu.updrive.controller.exception.NoFileProvidedRuntimeException;
import com.rmunteanu.updrive.dto.FileDTO;
import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.dto.LinkDTO;
import com.rmunteanu.updrive.dto.UploadSlotDTO;
import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FileMetadataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileMetadataService.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final UploadConfiguration uploadConfiguration;

    FileMetadataService(@Autowired FileMetadataRepository fileMetadataRepository, @Autowired UploadConfiguration uploadConfiguration) {
        this.fileMetadataRepository = fileMetadataRepository;
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
        fileMetadataRepository.save(fileMetadata);
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

}
