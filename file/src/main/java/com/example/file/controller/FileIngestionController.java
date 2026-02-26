package com.example.file.controller;

import com.example.file.dto.FailedRow;

import com.example.file.dto.IngestionResult;
import com.example.file.service.FileIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class FileIngestionController {

    private final JobLauncher jobLauncher;
    private final Job userIngestionJob;
    private final FileIngestionService fileIngestionService;
//    private final IngestionLogRepository ingestionLogRepository;

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<IngestionResult>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files)
            throws Exception {

        List<IngestionResult> results = new ArrayList<>();

        for (MultipartFile file : files) {

            String filePath = fileIngestionService.save(file);

            JobParameters params = new JobParametersBuilder()
                    .addString("filePath", filePath)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution =
                    jobLauncher.run(userIngestionJob, params);

            StepExecution stepExecution =
                    execution.getStepExecutions().iterator().next();

            long successCount = stepExecution.getWriteCount();

            List<FailedRow> failedRows =
                    (List<FailedRow>) stepExecution
                            .getExecutionContext()
                            .get("failedRows");

            if (failedRows == null) {
                failedRows = new ArrayList<>();
            }

            long failedCount = failedRows.size();
            long totalCount = successCount + failedCount;
            failedRows.sort(Comparator.comparingInt(FailedRow::getRow));
            results.add(new IngestionResult(
                    file.getOriginalFilename(),
                    totalCount,
                    successCount,
                    failedCount,
                    failedRows,
                    LocalDateTime.now()
            ));

        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/upload/single")
    public ResponseEntity<IngestionResult> uploadSingle(
            @RequestParam("file") MultipartFile file)
            throws Exception {
    IngestionResult result;
            String filePath = fileIngestionService.save(file);

            JobParameters params = new JobParametersBuilder()
                    .addString("filePath", filePath)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution =
                    jobLauncher.run(userIngestionJob, params);

            StepExecution stepExecution =
                    execution.getStepExecutions().iterator().next();

            long successCount = stepExecution.getWriteCount();

            List<FailedRow> failedRows =
                    (List<FailedRow>) stepExecution
                            .getExecutionContext()
                            .get("failedRows");

            if (failedRows == null) {
                failedRows = new ArrayList<>();
            }

            long failedCount = failedRows.size();
            long totalCount = successCount + failedCount;
            failedRows.sort(Comparator.comparingInt(FailedRow::getRow));
            result= new IngestionResult(
                    file.getOriginalFilename(),
                    totalCount,
                    successCount,
                    failedCount,
                    failedRows,
                    LocalDateTime.now()
            );


        return ResponseEntity.ok(result);
    }
}