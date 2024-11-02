package com.rmunteanu.updrive.repository;

import com.rmunteanu.updrive.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    @Query(value = "SELECT * FROM file_metadata WHERE slot_id = ?1;", nativeQuery = true)
    FileMetadata readBySlotId(String slotId);

    @Query(value = "SELECT * FROM file_metadata WHERE expiration_date < now() AND active = false AND expired = false;", nativeQuery = true)
    List<FileMetadata> readByExpirationDate();

    @Query(value = "SELECT EXISTS(SELECT * FROM file_metadata WHERE slot_id = ?1);", nativeQuery = true)
    boolean existsBySlotId(String slotId);

}
