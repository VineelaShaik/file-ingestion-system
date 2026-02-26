package com.example.file.batch;

import com.example.file.dto.ParseRow;
import com.example.file.entity.User;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class CsvReaderConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<ParseRow> reader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<ParseRow> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);

        reader.setLineMapper((line, lineNumber) -> {

            String[] tokens = line.split(",");

            User user = new User();
            user.setFullName(tokens[0].trim());
            user.setEmail(tokens[1].trim());
            user.setPhone(tokens[2].trim());
            user.setCity(tokens[3].trim());

            return new ParseRow(lineNumber, user);
        });
        return reader;
}}
