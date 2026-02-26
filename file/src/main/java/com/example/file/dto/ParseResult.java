package com.example.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class ParseResult {
    List<ParseRow> parsedRows;
    List<FailedRow> structureFailedRow;
}
