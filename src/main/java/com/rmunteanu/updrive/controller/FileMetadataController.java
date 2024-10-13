package com.rmunteanu.updrive.controller;


import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.dto.UploadSlotDTO;
import com.rmunteanu.updrive.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/metadata")
public class FileMetadataController {

    // TODO: Create a specialized class FileMetadataService and replace this dependency with it
    private final FileUploadService fileUploadService;

    FileMetadataController(@Autowired FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/metadata")
    public ResponseEntity<UploadSlotDTO> uploadFileMetadata(@RequestBody FileMetadataDTO fileMetadataDto) {
        UploadSlotDTO uploadSlotDTO = fileUploadService.uploadFileMetadata(fileMetadataDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadSlotDTO);
    }

}
