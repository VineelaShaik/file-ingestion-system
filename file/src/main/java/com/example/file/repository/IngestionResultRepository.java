package com.example.file.repository;

import com.example.file.entity.IngestionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionResultRepository extends JpaRepository<IngestionResult,Long> {
}
