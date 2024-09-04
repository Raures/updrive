package com.rmunteanu.updrive.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Column(name = "slot_id", unique = true)
    String slotId;

    @Column(name = "available_hours")
    int availabilityTime;

    @Column(name = "active")
    boolean active;

    @Column(name = "expired")
    boolean expired;

    @Column(name = "expiration_date", nullable = false)
    @CreationTimestamp
    LocalDateTime expirationDate;

    @Column(name = "creation_date", nullable = false)
    @CreationTimestamp
    LocalDateTime creationDate;

}
