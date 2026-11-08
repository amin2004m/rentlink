package rentlink.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "{auth.email.required}")
    @Email(message = "{auth.email.invalid}")
    String email;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 8, message = "{auth.password.tooShort}")
    String password;

    @NotBlank(message = "{user.fullName.required}")
    String fullName;
}
