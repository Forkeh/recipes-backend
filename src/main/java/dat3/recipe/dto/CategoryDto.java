package dat3.recipe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dat3.recipe.entity.Category;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {
    @NotNull
    private String name;

    public CategoryDto(Category c) {
        this.name = c.getName();
    }
}
