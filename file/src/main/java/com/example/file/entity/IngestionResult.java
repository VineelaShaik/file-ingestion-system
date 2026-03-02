package com.example.file.entity;

import com.example.file.entity.FailedRow;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IngestionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private long totalCount;
    private long successCount;
    private long failedCount;
    @OneToMany(mappedBy = "ingestionResult", cascade = CascadeType.ALL)
    private List<FailedRow> failedRows;
    private LocalDateTime processedAt;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name ="ingestion_log_id")
    private IngestionLog ingestionLog;
    @PrePersist
    public void onCreate() {
        this.processedAt = LocalDateTime.now();
    }
    public IngestionResult(String fileName,
                           long totalCount,
                           long successCount,
                           long failedCount,
                           List<FailedRow> failedRows) {
        this.fileName = fileName;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.failedRows = failedRows;
    }
}
