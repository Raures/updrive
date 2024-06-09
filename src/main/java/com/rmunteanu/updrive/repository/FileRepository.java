package com.rmunteanu.updrive.repository;

import com.rmunteanu.updrive.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileRepository extends JpaRepository<FileMetadata, Long> {

    @Query(value = "SELECT * FROM file_metadata WHERE slot_id = ?1;", nativeQuery = true)
    FileMetadata readBySlotId(String slotId);

}
