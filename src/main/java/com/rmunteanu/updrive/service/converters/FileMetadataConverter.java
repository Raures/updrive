package com.rmunteanu.updrive.service.converters;

import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.entity.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataConverter implements Converter<FileMetadataDTO, FileMetadata> {

    @Override
    public FileMetadata fromDTO(FileMetadataDTO dto) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setAvailabilityTime(dto.availabilityTime());
        return fileMetadata;
    }

    @Override
    public FileMetadataDTO toDTO(FileMetadata entity) {
        return new FileMetadataDTO(entity.getAvailabilityTime(), null);
    }
}
