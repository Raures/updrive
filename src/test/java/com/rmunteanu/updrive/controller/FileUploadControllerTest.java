package com.rmunteanu.updrive.controller;

import com.rmunteanu.updrive.dto.FileDTO;
import com.rmunteanu.updrive.dto.FileMetadataDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

// TODO: Clean up the database after tests.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileUploadControllerTest {

    private static final String REST_API_PATH = "/api/v1";

    private String slotId;

    @Test
    @Order(1)
    void testPostMetadata_whenValidDTO_returnSlotId() {
        FileDTO fileDTO = new FileDTO("test_file.txt", "txt", 1);
        FileDTO[] files = new FileDTO[]{fileDTO};
        FileMetadataDTO fileMetadataDTO = new FileMetadataDTO(5, files);
        slotId =
                given()
                        .body(fileMetadataDTO)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .post(REST_API_PATH + "/upload/metadata")
                        .then()
                        .statusCode(201)
                        .body("$", Matchers.hasKey("slotId"))
                        .extract()
                        .path("slotId");
        Assertions.assertEquals(36, slotId.length());
    }

    @Test
    @Order(2)
    void testPostFile_whenValidFile_returnDownloadURL() throws URISyntaxException {
        File file = Paths.get(ClassLoader.getSystemResource("test_file.txt").toURI()).toFile();
        String url =
                given()
                        .multiPart(file)
                        .when()
                        .post(REST_API_PATH + "/upload/files/" + slotId)
                        .then()
                        .statusCode(201)
                        .body("$", Matchers.hasKey("url"))
                        .extract()
                        .path("url");
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.contains(slotId));
    }

}