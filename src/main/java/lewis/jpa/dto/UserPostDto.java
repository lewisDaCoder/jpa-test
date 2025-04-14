package lewis.jpa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPostDto {
    // User fields
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    
    // Post fields
    private String postTitle;
    private String postContent;
} 