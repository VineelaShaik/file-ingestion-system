package com.example.file.batch;

import com.example.file.Exception.ValidationException;
import com.example.file.dto.ParseRow;
import com.example.file.dto.UserWithRow;
import com.example.file.validation.ValidateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserProcessor implements ItemProcessor<ParseRow, UserWithRow> {

    private final ValidateUser validateUser;

    @Override
    public UserWithRow process(ParseRow parseRow) {

        String error = validateUser.validate(
                parseRow.getRow(),
                parseRow.getUser()
        );

        if (error != null) {
            throw new ValidationException(
                    parseRow.getRow(),
                    error
            );
        }

        return new UserWithRow(parseRow.getRow(), parseRow.getUser());
    }
}

