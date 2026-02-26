package com.example.file.batch;

import com.example.file.Exception.DuplicateUserException;
import com.example.file.dto.UserWithRow;
import com.example.file.repository.UserRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserWriter implements ItemWriter<UserWithRow> {

    private final UserRepository userRepository;

    @Override
    public void write(@Nonnull Chunk<? extends UserWithRow> chunk) {

        for (UserWithRow item : chunk.getItems()) {
            try {
                userRepository.save(item.getUser());
            } catch (DataIntegrityViolationException ex) {

                throw new DuplicateUserException(
                        item.getRow(),
                        "Duplicate email in database"
                );
            }
        }
    }
}


