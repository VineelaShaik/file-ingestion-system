//package com.example.file.entity;
//import com.example.file.dto.IngestionResult;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@Table(name = "ingestion_log")
//public class IngestionLog {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////    @ElementCollection
////    private List<IngestionResult> ingestionResults;
////    private LocalDateTime processedAt;
////    @PrePersist
////    public void onCreate() {
////        this.processedAt = LocalDateTime.now();
////    }
////    public IngestionLog(List<IngestionResult> ingestionResults){
////        this.ingestionResults=ingestionResults;
////    }
//
//
//}
