package com.adfarms.dto;

import com.adfarms.entity.BranchEntity;
import com.adfarms.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmployeeForm {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Long branchId;

}
