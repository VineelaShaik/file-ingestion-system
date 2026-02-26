package com.example.file.batch;

import com.example.file.dto.FailedRow;
import com.example.file.dto.ParseRow;
import org.springframework.batch.core.SkipListener;
import com.example.file.dto.UserWithRow;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserSkipListener implements SkipListener<ParseRow, UserWithRow> {

    private static final String FAILED_ROWS_KEY = "failedRows";

    @Override
    public void onSkipInRead(Throwable t) {

        if (t instanceof FlatFileParseException ex) {

            StepExecution stepExecution =
                    StepSynchronizationManager.getContext().getStepExecution();

            List<FailedRow> failedRows = getOrCreate(stepExecution);

            failedRows.add(
                    new FailedRow(
                            ex.getLineNumber(),
                            "Structural error: " + ex.getInput()
                    )
            );

            stepExecution.getExecutionContext()
                    .put(FAILED_ROWS_KEY, failedRows);
        }
    }

    @Override
    public void onSkipInProcess(ParseRow item, Throwable t) {

        StepExecution stepExecution =
                StepSynchronizationManager.getContext().getStepExecution();

        List<FailedRow> failedRows = getOrCreate(stepExecution);

        failedRows.add(
                new FailedRow(
                        item.getRow(),
                        t.getMessage()
                )
        );

        stepExecution.getExecutionContext()
                .put(FAILED_ROWS_KEY, failedRows);
    }

    @Override
    public void onSkipInWrite(UserWithRow item, Throwable t) {

        StepExecution stepExecution =
                StepSynchronizationManager.getContext().getStepExecution();

        List<FailedRow> failedRows = getOrCreate(stepExecution);

        failedRows.add(
                new FailedRow(
                        item.getRow(),
                        "Write error: " + t.getMessage()
                )
        );

        stepExecution.getExecutionContext()
                .put(FAILED_ROWS_KEY, failedRows);
    }

    private List<FailedRow> getOrCreate(StepExecution stepExecution) {

        List<FailedRow> failedRows =
                (List<FailedRow>) stepExecution
                        .getExecutionContext()
                        .get(FAILED_ROWS_KEY);

        if (failedRows == null) {
            failedRows = new ArrayList<>();
        }

        return failedRows;
    }
}