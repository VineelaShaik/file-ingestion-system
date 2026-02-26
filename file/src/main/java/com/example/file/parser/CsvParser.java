package com.example.file.parser;


import com.example.file.dto.FailedRow;
import com.example.file.dto.ParseResult;
import com.example.file.dto.ParseRow;
import com.example.file.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser {
    public ParseResult parse(MultipartFile file) throws IOException{
        int row=0;
        List<ParseRow> parsed= new ArrayList<>();
        List<FailedRow> failedRowCount = new ArrayList<>();
        try(BufferedReader reader= new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;
            boolean header=true;
            while((line = reader.readLine())!=null){
                if(header){
                    header=false;
                    continue;
                }
                if(line.trim().isEmpty())continue;
                String[] data= line.split(",");
                row++;
                if(data.length!=4) {
                    failedRowCount.add(new FailedRow(row,"Column count is less"));
                    continue;
                }
                User user= new User();
                user.setFullName(data[0].trim());
                user.setEmail(data[1].trim());
                user.setPhone(data[2].trim());
                user.setCity(data[3].trim());
                parsed.add(new ParseRow(row,user));
            }
        }
        return new ParseResult(parsed, failedRowCount);
    }
}
