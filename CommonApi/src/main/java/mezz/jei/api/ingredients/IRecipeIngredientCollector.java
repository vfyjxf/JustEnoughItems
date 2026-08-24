package mezz.jei.api.ingredients;

import mezz.jei.api.recipe.RecipeIngredientRole;

/**
 * A receiver for ingredients collected from a recipe.
 *
 * <p>Pass an implementation of this interface to
 * {@link mezz.jei.api.recipe.IRecipeManager#getRecipeIngredients(mezz.jei.api.recipe.types.IRecipeType, Object, IRecipeIngredientCollector)}
 * to collect the raw ingredients of a recipe without going through JEI's internal {@code ITypedIngredient}
 * or supplier wrapping.</p>
 *
 * <p>JEI drives the recipe category's {@code setRecipe} method and calls
 * {@link #addIngredient(RecipeIngredientRole, int, IIngredientType, Object)} once for every collected
 * ingredient. Each call provides the raw ingredient directly, so mods can feed it straight into their own
 * computation model.</p>
 *
 * @since 30.26.0
 */
public interface IRecipeIngredientCollector {
	/**
	 * Called once for every ingredient collected from a recipe.
	 *
	 * <p>The {@code slotIndex} is the index of the slot that the ingredient belongs to. Ingredients that come
	 * from the same slot share the same {@code slotIndex}, which preserves the grouping between a slot's
	 * candidates. The {@code type} and {@code ingredient} are the raw ingredient, without any wrapping.</p>
	 *
	 * @param role        the role of the ingredient in the recipe (input, output, catalyst, etc.).
	 * @param slotIndex   the index of the slot that the ingredient belongs to.
	 * @param type        the ingredient type of the raw ingredient.
	 * @param ingredient  the raw ingredient.
	 *
	 * @since 30.26.0
	 */
	<T> void addIngredient(RecipeIngredientRole role, int slotIndex, IIngredientType<T> type, T ingredient);
}
