package com.adfarms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmployeeDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Long branchId;

}
