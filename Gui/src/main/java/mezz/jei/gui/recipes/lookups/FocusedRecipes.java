package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class FocusedRecipes<T> implements IFocusedRecipes<T> {
	private final IRecipeManager recipeManager;
	private final IRecipeCategory<T> recipeCategory;
	private final IFocusGroup focuses;

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private @Nullable List<T> recipes;

	public static <T> IFocusedRecipes<T> create(IFocusGroup focuses, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		return new FocusedRecipes<>(focuses, recipeManager, recipeCategory);
	}

	private FocusedRecipes(IFocusGroup focuses, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		this.focuses = focuses;
		this.recipeManager = recipeManager;
		this.recipeCategory = recipeCategory;
		this.recipes = null;
	}

	@Override
	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	@Unmodifiable
	public List<T> getRecipes() {
		if (recipes == null) {
			recipes = recipeManager.createRecipeLookup(recipeCategory.getRecipeType())
				.limitFocus(focuses.getAllFocuses())
				.get()
				.toList();
		}
		return recipes;
	}
}
