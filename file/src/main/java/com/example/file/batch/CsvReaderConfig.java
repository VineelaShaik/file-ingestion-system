package com.example.file.batch;

import com.example.file.dto.ParseRow;
import com.example.file.dto.UserWithRow;
import com.example.file.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

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
}
    @Bean
    @StepScope
    public FlatFileItemReader<ParseRow> mappedReader(
            @Value("#{jobParameters['filePath']}") String filePath,
            @Value("#{jobParameters['mapping']}") String mappingJson
    ) {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> mapping;

        try {
            mapping = mapper.readValue(mappingJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid mapping JSON", e);
        }

        System.out.println("Mapping received: " + mapping);
        Map<String, Integer> headerIndexMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            String[] headers = headerLine.split(",");

            for (int i = 0; i < headers.length; i++) {
                headerIndexMap.put(headers[i].trim(), i);
            }

            System.out.println("Header Index Map: " + headerIndexMap);

        } catch (Exception e) {
            throw new RuntimeException("Error reading header", e);
        }

        FlatFileItemReader<ParseRow> reader =
                new FlatFileItemReader<>();

        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);

        reader.setLineMapper((line, lineNumber) -> {

            String[] values = line.split(",");

            User user = new User();

            for (Map.Entry<String, String> entry : mapping.entrySet()) {

                String dbField = entry.getKey();
                String fileColumn = entry.getValue();

                Integer index = headerIndexMap.get(fileColumn);

                if (index != null && index < values.length) {

                    String value = values[index];

                    switch (dbField) {
                        case "full_name":
                            user.setFullName(value);
                            break;
                        case "email":
                            user.setEmail(value);
                            break;
                        case "phone":
                            user.setPhone(value);
                            break;
                        case "city":
                            user.setCity(value);
                            break;
                    }
                }
            }

            return new ParseRow(lineNumber, user);
        });

        return reader;
    }

}
