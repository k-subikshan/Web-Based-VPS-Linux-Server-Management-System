package serverviwe.view.layer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class vpsloginform {
    @NotBlank(message = "Host/IP is required")
    private String host;

    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    private int port = 22;

    @NotBlank(message = "Username is required")
    private String username = "root";

    @NotBlank(message = "Password is required")
    private String password;

    private String path = "/";
}
