package com.example.file.batch;

import com.example.file.dto.ParseRow;
import com.example.file.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
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
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(false);

        reader.setLineMapper((line, lineNumber) -> {

            FieldSet fieldSet = tokenizer.tokenize(line);

            User user = new User();

            BeanWrapper wrapper = new BeanWrapperImpl(user);

            wrapper.setPropertyValue("fullName", fieldSet.readString(0).trim());
            wrapper.setPropertyValue("email", fieldSet.readString(1).trim());
            wrapper.setPropertyValue("phone", fieldSet.readString(2).trim());
            wrapper.setPropertyValue("city", fieldSet.readString(3).trim());

            return new ParseRow(lineNumber, user);
        });
        return reader;
}
//    @Bean
//    @StepScope
//    public FlatFileItemReader<ParseRow> mappedReader(
//            @Value("#{jobParameters['filePath']}") String filePath,
//            @Value("#{jobParameters['mapping']}") String mappingJson
//    ) {
//        ObjectMapper mapper = new ObjectMapper();
//
//        Map<String, String> mapping;
//
//        try {
//            mapping = mapper.readValue(mappingJson, Map.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Invalid mapping JSON", e);
//        }
//
//        System.out.println("Mapping received: " + mapping);
//        Map<String, Integer> headerIndexMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//
//            String headerLine = br.readLine();
//            String[] headers = headerLine.split(",");
//
//            for (int i = 0; i < headers.length; i++) {
//                headerIndexMap.put(headers[i].trim(), i);
//            }
//
//            System.out.println("Header Index Map: " + headerIndexMap);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error reading header", e);
//        }
//
//        FlatFileItemReader<ParseRow> reader =
//                new FlatFileItemReader<>();
//
//        reader.setResource(new FileSystemResource(filePath));
//        reader.setLinesToSkip(1);
//        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
//        tokenizer.setDelimiter(",");
//        tokenizer.setStrict(false);
//
//        reader.setLineMapper((line, lineNumber) -> {
//
//            FieldSet fieldSet = tokenizer.tokenize(line);
//
//            User user = new User();
//
//            BeanWrapper wrapper = new BeanWrapperImpl(user);
//
//            for (Map.Entry<String, String> entry : mapping.entrySet()) {
//
//                String dbField = entry.getKey();
//                String fileColumn = entry.getValue();
//
//                Integer index = headerIndexMap.get(fileColumn);
//
//                if (index != null) {
//
//                    String value = fieldSet.readString(index);
//
//                    try {
//                        wrapper.setPropertyValue(dbField, value);
//                    } catch (Exception e) {
//                        System.out.println("Field mapping error: " + dbField);
//                    }
//                }
//            }
//
//            return new ParseRow(lineNumber, user);
//        });
//
//        return reader;
//    }
@Bean
@StepScope
public FlatFileItemReader<ParseRow> mappedReader(
        @Value("#{jobParameters['filePath']}") String filePath,
        @Value("#{jobParameters['mapping']}") String mappingJson
) {
    ObjectMapper mapper = new ObjectMapper();

    Map<String, Object> mapping;
    try {
        mapping = mapper.readValue(mappingJson, new TypeReference<Map<String, Object>>() {});
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

    FlatFileItemReader<ParseRow> reader = new FlatFileItemReader<>();
    reader.setResource(new FileSystemResource(filePath));
    reader.setLinesToSkip(1);

    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter(",");
    tokenizer.setStrict(false);

    reader.setLineMapper((line, lineNumber) -> {

        FieldSet fieldSet = tokenizer.tokenize(line);
        User user = new User();
        BeanWrapper wrapper = new BeanWrapperImpl(user);

        for (Map.Entry<String, Object> entry : mapping.entrySet()) {

            String dbField = entry.getKey();
            Object value   = entry.getValue();
            String resolved;

            if (value instanceof String) {
                // single mapping — "fullName": "name_col"
                String fileColumn = (String) value;
                Integer index = headerIndexMap.get(fileColumn);
                if (index == null) continue;
                resolved = fieldSet.readString(index);

            } else if (value instanceof Map) {
                // multi-field formula — "fullName": { sources: [...], formula: "..." }
                @SuppressWarnings("unchecked")
                Map<String, Object> formulaMap = (Map<String, Object>) value;

                String formula = (String) formulaMap.get("formula");

                @SuppressWarnings("unchecked")
                List<String> sources = (List<String>) formulaMap.get("sources");

                // substitute {token} → actual cell value
                for (String src : sources) {
                    Integer index = headerIndexMap.get(src);
                    String cellValue = (index != null) ? fieldSet.readString(index) : "";
                    formula = formula.replace("{" + src + "}", cellValue);
                }

                resolved = evaluateFormula(formula);

            } else {
                continue;
            }

            try {
                wrapper.setPropertyValue(dbField, resolved);
            } catch (Exception e) {
                System.out.println("Field mapping error: " + dbField + " → " + e.getMessage());
            }
        }

        return new ParseRow(lineNumber, user);
    });

    return reader;
}

    private String evaluateFormula(String formula) {
        String trimmed = formula.trim();

        if (isNumericExpression(trimmed)) {
            try {
                return String.valueOf(evalMath(trimmed));
            } catch (Exception e) {
                System.out.println("Math eval failed, falling back to string: " + e.getMessage());
            }
        }

        // string concat — split on +, strip quotes, join
        String[] parts = trimmed.split("\\+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            String p = part.trim();
            if ((p.startsWith("'") && p.endsWith("'")) ||
                    (p.startsWith("\"") && p.endsWith("\""))) {
                p = p.substring(1, p.length() - 1);
            }
            sb.append(p);
        }
        return sb.toString();
    }

    private boolean isNumericExpression(String expr) {
        return expr.matches("[0-9+\\-*/().\\s]+");
    }

    private double evalMath(String expr) {
        expr = expr.trim();
        int depth = 0;
        int lastPlus = -1, lastMinus = -1, lastMul = -1, lastDiv = -1;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if      (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0) {
                if      (c == '+')        lastPlus  = i;
                else if (c == '-' && i>0) lastMinus = i;
                else if (c == '*')        lastMul   = i;
                else if (c == '/')        lastDiv   = i;
            }
        }

        if (lastPlus  >= 0) return evalMath(expr.substring(0, lastPlus))  + evalMath(expr.substring(lastPlus  + 1));
        if (lastMinus >= 0) return evalMath(expr.substring(0, lastMinus)) - evalMath(expr.substring(lastMinus + 1));
        if (lastMul   >= 0) return evalMath(expr.substring(0, lastMul))   * evalMath(expr.substring(lastMul   + 1));
        if (lastDiv   >= 0) return evalMath(expr.substring(0, lastDiv))   / evalMath(expr.substring(lastDiv   + 1));

        if (expr.startsWith("(") && expr.endsWith(")"))
            return evalMath(expr.substring(1, expr.length() - 1));

        return Double.parseDouble(expr.trim());
    }

}
