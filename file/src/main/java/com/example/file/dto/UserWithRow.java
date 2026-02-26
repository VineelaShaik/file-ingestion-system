package com.example.file.dto;

import com.example.file.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserWithRow {
    int row;
    User user;
}
