package com.amazon.dto;

import lombok.*;

import javax.validation.constraints.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class UserDto {

    @Getter
    @Setter
    private Long id;

    @Setter
    @Getter
    @NotBlank(message = "Email is required field.")
    @Email(message = "A user with this email already exists. Please try with different email.")
    private String username;

    @Getter
    @Setter(AccessLevel.NONE)
    @NotBlank(message = "Password is required field")
    @Pattern(regexp = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{4,}",
            message = "Password should be at least 4 characters long and needs to contain 1 capital letter, " +
                    "1 small letter and 1 special character or number.")
    private String password;

    @Getter
    @Setter(AccessLevel.NONE)
    @NotNull(message = "Password should match")
    private String confirmPassword;

    @Setter
    @Getter
    @NotBlank(message = "First Name is required field.")
    @Size(min = 2, max = 50, message = "First Name must be between 2 and 50 characters long.")
    private String firstname;

    @Setter
    @Getter
    @NotBlank(message = "Last Name is required field.")
    @Size(min = 2, max = 50, message = "Last Name must be between 2 and 50 characters long.")
    private String lastname;

    @Setter
    @Getter
    @NotBlank(message = "Phone number is required field")
    @Pattern(regexp = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]\\d{3}[\\s.-]\\d{4}$",
            message = "Phone Number is required field and may be in any valid phone number format " +
                    "example: +1 (957) 463-7174")

    private String phone;

    @Setter
    @Getter
    @NotNull(message = "Please select a Role.")
    private RoleDto role;

    @Setter
    @Getter
    @NotNull(message = "Please select a company")
    private CompanyDto company;

    private boolean isOnlyAdmin;

    public void setPassword(String password) {
        this.password = password;
        checkConfirmPassword();
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        checkConfirmPassword();
    }

    private void checkConfirmPassword() {
        if (password != null && !password.equals(confirmPassword)) {
            this.confirmPassword = null;
        }
    }

    public void setOnlyAdmin(boolean onlyAdmin) {
        isOnlyAdmin = onlyAdmin;
    }

    public boolean getIsOnlyAdmin() {
        return isOnlyAdmin;
    }
}