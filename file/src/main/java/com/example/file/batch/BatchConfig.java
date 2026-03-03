package com.example.file.batch;

import com.example.file.Exception.DuplicateUserException;
import com.example.file.Exception.ValidationException;
import com.example.file.dto.ParseRow;
import com.example.file.dto.UserWithRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job userIngestionJob(Step userIngestionStep) {
        return new JobBuilder("UserIngestionJob",jobRepository)
                .start(userIngestionStep)
                .build();
    }

    @Bean
    public Step userIngestionStep(FlatFileItemReader<ParseRow> reader,
                                  UserProcessor processor,
                                  UserWriter writer,
                                  UserSkipListener skipListener) {
        return new StepBuilder("userIngestionStep", jobRepository)
                .<ParseRow, UserWithRow>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .skip(DuplicateUserException.class)
                .skipLimit(1000)
                .listener(skipListener)
                .build();
    }
    @Bean
    public Job mappedIngestionJob(Step mappedIngestionStep){
        return new JobBuilder("MappedIngestionJob",jobRepository)
                .start(mappedIngestionStep)
                .build();
    }
    @Bean
    public Step mappedIngestionStep(FlatFileItemReader<ParseRow> mappedReader,
                           UserProcessor processor,
                           UserWriter userWriter,
                           UserSkipListener skipListener,
                           PlatformTransactionManager transactionManager
                           ){
        return new StepBuilder("mappedIngestionStep",jobRepository)
                .<ParseRow,UserWithRow>chunk(10, transactionManager)
                .reader(mappedReader)
                .processor(processor)
                .writer(userWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .skip(DuplicateUserException.class)
                .skipLimit(1000)
                .listener(skipListener)
                .build();

    }

}
