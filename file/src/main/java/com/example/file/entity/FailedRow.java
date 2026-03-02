package com.example.file.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class FailedRow implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int rowNum;
    private String description;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "ingestion_result_id")
    private IngestionResult ingestionResult;
    public FailedRow(int rowNumber,String description){
        this.rowNum=rowNumber;
        this.description=description;
    }
}