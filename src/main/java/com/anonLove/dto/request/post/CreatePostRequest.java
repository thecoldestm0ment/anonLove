package com.anonLove.dto.request.post;

import com.anonLove.domain.post.TargetGender;
import com.anonLove.domain.post.VisibilityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Category is required")
    @JsonProperty("category_id")
    private Integer categoryId;

    @JsonProperty("visibility_type")
    private VisibilityType visibilityType;

    @JsonProperty("target_gender")
    private TargetGender targetGender;
}
