package com.unicity.inventory.mapping;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}