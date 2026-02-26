//package com.example.file.entity;
//
//import com.example.file.dto.FailedRow;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//public class IngestionResult {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String fileName;
//    private long totalCount;
//    private long successCount;
//    private long FailedCount;
//    private List<FailedRow> Failed;
//    private LocalDateTime processedAt;
//}
