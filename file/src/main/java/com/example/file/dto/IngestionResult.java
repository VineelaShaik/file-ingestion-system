package com.example.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@AllArgsConstructor
@Getter
@Setter
public class IngestionResult {
    String fileName;
    long totalCount;
    long successCount;
    long FailedCount;
    List<FailedRow> Failed;
    LocalDateTime processedAt;



}
