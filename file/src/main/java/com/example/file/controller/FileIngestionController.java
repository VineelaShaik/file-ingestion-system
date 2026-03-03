package com.example.file.controller;

import com.example.file.entity.FailedRow;

import com.example.file.entity.IngestionLog;
import com.example.file.entity.IngestionResult;
import com.example.file.repository.IngestionLogRepository;
import com.example.file.repository.IngestionResultRepository;
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

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class FileIngestionController {

    private final JobLauncher jobLauncher;
    private final Job userIngestionJob;
    private final Job mappedIngestionJob;
    private final FileIngestionService fileIngestionService;
    private final IngestionResultRepository ingestionResultRepository;
    private final IngestionLogRepository ingestionLogRepository;

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<IngestionResult>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files)
            throws Exception {

        List<IngestionResult> results = new ArrayList<>();
        IngestionResult result;

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
            failedRows.sort(Comparator.comparingInt(FailedRow::getRowNum));
            result= new IngestionResult(
                    file.getOriginalFilename(),
                    totalCount,
                    successCount,
                    failedCount,
                    failedRows
            );
            for (FailedRow row : failedRows) {
                row.setIngestionResult(result);
            }
            results.add(result);

        }

        IngestionLog log=new IngestionLog(results);
        for (IngestionResult res : results) {
            res.setIngestionLog(log);
        }
        ingestionLogRepository.save(log);


        return ResponseEntity.ok(results);
    }

    @PostMapping("/upload/single")
    public ResponseEntity<List<IngestionResult>> uploadSingle(
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
            failedRows.sort(Comparator.comparingInt(FailedRow::getRowNum));
            result= new IngestionResult(
                    file.getOriginalFilename(),
                    totalCount,
                    successCount,
                    failedCount,
                    failedRows
            );
        for (FailedRow row : failedRows) {
            row.setIngestionResult(result);
        }
        ArrayList<IngestionResult> results = new ArrayList<>();
        results.add(result);
        IngestionLog log=new IngestionLog(results);
        for (IngestionResult res : results) {
            res.setIngestionLog(log);
        }
            ingestionLogRepository.save(log);


        return ResponseEntity.ok(results);
    }
    @PostMapping("/upload/mapped")
    public ResponseEntity<List<IngestionResult>> uploadMapped(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mapping") String mappingJson
    ) throws Exception {
        IngestionResult result;

        String filePath = fileIngestionService.save(file);

        JobParameters params = new JobParametersBuilder()
                .addString("filePath", filePath)
                .addString("mapping", mappingJson)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution= jobLauncher.run(mappedIngestionJob, params);

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
        failedRows.sort(Comparator.comparingInt(FailedRow::getRowNum));
        result= new IngestionResult(
                file.getOriginalFilename(),
                totalCount,
                successCount,
                failedCount,
                failedRows
        );
        for (FailedRow row : failedRows) {
            row.setIngestionResult(result);
        }
        ArrayList<IngestionResult> results = new ArrayList<>();
        results.add(result);
        IngestionLog log=new IngestionLog(results);
        for (IngestionResult res : results) {
            res.setIngestionLog(log);
        }
        ingestionLogRepository.save(log);


        return ResponseEntity.ok(results);
    }
}