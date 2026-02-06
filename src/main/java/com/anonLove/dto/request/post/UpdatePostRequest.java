package com.anonLove.dto.request.post;

import com.anonLove.domain.post.TargetGender;
import com.anonLove.domain.post.VisibilityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePostRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @JsonProperty("visibility_type")
    private VisibilityType visibilityType;

    @JsonProperty("target_gender")
    private TargetGender targetGender;
}
