package com.example.file.validation;

import com.example.file.entity.User;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ValidateUser {
    public String validate( int row,User user){
        if(Objects.equals(user.getFullName(), "") ||(user.getFullName().length()<3))
            return ("Invalid name");
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return "Invalid email";
        }
        if (user.getPhone() == null ||
                !user.getPhone().matches("\\d{10}")) {
            return "Invalid phone number";
        }
        if (user.getCity() == null || user.getCity().isBlank()) {
            return "City is missing";
        }
        return null;
    }
}
