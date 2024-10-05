package com.rmunteanu.updrive.controller;

import com.rmunteanu.updrive.dto.DownloadLinkDTO;
import com.rmunteanu.updrive.dto.FileMetadataDTO;
import com.rmunteanu.updrive.dto.UploadSlotDTO;
import com.rmunteanu.updrive.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    FileUploadController(@Autowired FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/metadata")
    public ResponseEntity<UploadSlotDTO> uploadMetadata(@RequestBody FileMetadataDTO fileMetadataDto) {
        UploadSlotDTO uploadSlotDTO = fileUploadService.uploadFileMetadata(fileMetadataDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadSlotDTO);
    }

    @PostMapping("/files/{slotId}")
    public ResponseEntity<DownloadLinkDTO> uploadFiles(@RequestParam("file") MultipartFile[] files, @PathVariable("slotId") String slotId) {
        DownloadLinkDTO downloadLinkDTO = fileUploadService.uploadFiles(files, slotId);
        return ResponseEntity.status(HttpStatus.CREATED).body(downloadLinkDTO);
    }

}
