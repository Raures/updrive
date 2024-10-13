package com.rmunteanu.updrive.controller;


import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.dto.UploadSlotDTO;
import com.rmunteanu.updrive.service.FileMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/metadata")
public class FileMetadataController {

    private final FileMetadataService fileMetadataService;

    FileMetadataController(@Autowired FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    @PostMapping
    public ResponseEntity<UploadSlotDTO> uploadFileMetadata(@RequestBody FileMetadataDTO fileMetadataDto) {
        UploadSlotDTO uploadSlotDTO = fileMetadataService.uploadFileMetadata(fileMetadataDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadSlotDTO);
    }

}
