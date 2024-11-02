package com.rmunteanu.updrive.service;

import com.rmunteanu.updrive.configuration.UploadConfiguration;
import com.rmunteanu.updrive.controller.exception.InvalidMetadataRuntimeException;
import com.rmunteanu.updrive.controller.exception.NoFileProvidedRuntimeException;
import com.rmunteanu.updrive.controller.exception.SlotNotFoundRuntimeException;
import com.rmunteanu.updrive.dto.FileDTO;
import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.dto.LinkDTO;
import com.rmunteanu.updrive.dto.UploadSlotDTO;
import com.rmunteanu.updrive.entity.FileMetadata;
import com.rmunteanu.updrive.repository.FileMetadataRepository;
import com.rmunteanu.updrive.service.converters.Converter;
import com.rmunteanu.updrive.service.converters.FileMetadataConverter;
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
    private final Converter<FileMetadataDTO, FileMetadata> converter;

    FileMetadataService(@Autowired FileMetadataRepository fileMetadataRepository,
                        @Autowired UploadConfiguration uploadConfiguration,
                        @Autowired FileMetadataConverter converter) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadConfiguration = uploadConfiguration;
        this.converter = converter;
    }

    private void validate(FileMetadataDTO fileMetadataDTO) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Validating file metadata: {}.", fileMetadataDTO);
        }
        if (fileMetadataDTO.availabilityTime() != null && (fileMetadataDTO.availabilityTime() <= 0 || fileMetadataDTO.availabilityTime() > 6000)) {
            LOGGER.error("Invalid availability time: {}", fileMetadataDTO.availabilityTime());
            throw new InvalidMetadataRuntimeException("Availability time must be between 0 and 6000, your value is: %d", fileMetadataDTO.availabilityTime());
        }
    }

    private void validateFiles(FileMetadataDTO fileMetadataDTO) {
        if (fileMetadataDTO.files() == null || fileMetadataDTO.files().length < 1) {
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

    private UploadSlotDTO store(FileMetadata fileMetadata) {
        fileMetadataRepository.save(fileMetadata);
        LinkDTO[] links = new LinkDTO[1];
        links[0] = new LinkDTO("http://localhost:8080/api/v1/upload/files/" + fileMetadata.getSlotId(), "upload-files", HttpMethod.POST.name());
        return new UploadSlotDTO(fileMetadata.getSlotId(), links);
    }

    public UploadSlotDTO uploadFileMetadata(FileMetadataDTO fileMetadataDTO) {
        validate(fileMetadataDTO);
        validateFiles(fileMetadataDTO);
        FileMetadata fileMetadata = converter.fromDTO(fileMetadataDTO);
        fileMetadata.setActive(true);
        String slotId = UUID.randomUUID().toString();
        fileMetadata.setSlotId(slotId);
        return store(fileMetadata);
    }

    public void updateFileMetadata(String slotId, FileMetadataDTO fileMetadataDTO) {
        validate(fileMetadataDTO);
        boolean slotExists = fileMetadataRepository.existsBySlotId(slotId);
        if (!slotExists) {
            throw new SlotNotFoundRuntimeException("Slot ID does not exist.");
        }
        FileMetadata fileMetadata = converter.fromDTO(fileMetadataDTO);
        fileMetadataRepository.save(fileMetadata);
    }

}
