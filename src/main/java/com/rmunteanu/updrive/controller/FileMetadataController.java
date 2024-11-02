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
    public ResponseEntity<UploadSlotDTO> uploadFileMetadata(@RequestBody FileMetadataDTO fileMetadataDTO) {
        UploadSlotDTO uploadSlotDTO = fileMetadataService.uploadFileMetadata(fileMetadataDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadSlotDTO);
    }

    @PutMapping("/update/{slotId}")
    public ResponseEntity<String> updateFileMetadata(@PathVariable("slotId") String slotId, @RequestBody FileMetadataDTO fileMetadataDTO) {
        fileMetadataService.updateFileMetadata(slotId, fileMetadataDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Updated file metadata.");
    }

}
