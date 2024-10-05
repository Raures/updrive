package com.rmunteanu.updrive.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
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

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public int getAvailabilityTime() {
        return availabilityTime;
    }

    public void setAvailabilityTime(int availabilityTime) {
        this.availabilityTime = availabilityTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

}
