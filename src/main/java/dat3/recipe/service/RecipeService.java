package dat3.recipe.service;

import com.nimbusds.jose.proc.SecurityContext;
import dat3.recipe.dto.RecipeDto;
import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.repository.RecipeRepository;
import dat3.security.entity.UserWithRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import dat3.security.repository.UserWithRolesRepository;

import java.security.Principal;
import java.util.List;

@Service
public class RecipeService {

    private RecipeRepository recipeRepository;
    private CategoryRepository categoryRepository;
    private UserWithRolesRepository userWithRolesRepository;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, UserWithRolesRepository userWithRolesRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.userWithRolesRepository = userWithRolesRepository;
    }

    public List<RecipeDto> getAllRecipes(String category) {
        List<Recipe> recipes = category == null ? recipeRepository.findAll() : recipeRepository.findByCategoryName(category);
        List<RecipeDto> recipeResponses = recipes.stream()
                .map((r) -> new RecipeDto(r, false))
                .toList();
        return recipeResponses;
    }

    public RecipeDto getRecipeById(int idInt) {
        Recipe recipe = recipeRepository.findById(idInt)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        return new RecipeDto(recipe, false);
    }

    public RecipeDto addRecipe(RecipeDto request, Principal principal) {
        if (request.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot provide the id for a new recipe");
        }
        Category category = categoryRepository.findByName(request.getCategory()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));
        Recipe newRecipe = new Recipe();

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String owner = authentication.getName();

        updateRecipe(newRecipe, request, category, principal.getName());
        recipeRepository.save(newRecipe);
        return new RecipeDto(newRecipe, false);
    }

    private void updateRecipe(Recipe original, RecipeDto r, Category category, String owner) {
        original.setName(r.getName());
        original.setInstructions(r.getInstructions());
        original.setIngredients(r.getIngredients());
        original.setThumb(r.getThumb());
        original.setYouTube(r.getYouTube());
        original.setSource(r.getSource());
        original.setCategory(category);
        if (owner != null) {
            original.setOwner(owner);
        }
    }

    public RecipeDto editRecipe(RecipeDto request, int id, Principal principal) {
        if (request.getId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change the id of an existing recipe");
        }
        Category category = categoryRepository.findByName(request.getCategory()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));

        Recipe recipeToEdit = recipeRepository.findById(id)
                .orElseThrow(()
                        -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        // Check if user is admin or owner of recipe
        if (!isOwnerOrAdmin(principal, recipeToEdit)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin or the owner of the recipe. You cannot edit it.");
        }

        updateRecipe(recipeToEdit, request, category, null);
        recipeRepository.save(recipeToEdit);
        return new RecipeDto(recipeToEdit, false);
    }

    public ResponseEntity deleteRecipe(int id, Principal principal) {

        UserWithRoles user = userWithRolesRepository.findById(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        // Check if user is admin or owner of recipe
        if (!isOwnerOrAdmin(principal, recipe)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin or the owner of the recipe. You cannot delete it.");
        }

        recipeRepository.delete(recipe);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private boolean isOwnerOrAdmin(Principal principal, Recipe recipe) {
        UserWithRoles user = userWithRolesRepository.findById(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if user is admin
        boolean isAdmin = (user.getRoles()
                .stream()
                .anyMatch(role -> role.getRoleName()
                        .equals("ADMIN")));

        // Check if user is owner of recipe
        boolean isOwner = recipe.getOwner()
                .equals(user.getUsername());

        return isAdmin || isOwner;
    }


}

