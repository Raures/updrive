package com.rmunteanu.updrive.controller;

import com.rmunteanu.updrive.service.FileDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Controller
@RequestMapping("api/v1/download")
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    FileDownloadController(@Autowired FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<byte[]> downloadFiles(@PathVariable("slotId") String slotId) throws IOException {
        Path filePath = fileDownloadService.downloadFiles(slotId);
        byte[] fileBytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName().toString() + "\"");
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(fileBytes.length);
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(fileBytes);
    }

}
