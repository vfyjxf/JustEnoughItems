package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IRecipeIngredientCollector;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import net.minecraft.util.context.ContextMap;

/**
 * A minimal {@link IRecipeLayoutBuilder} that drives {@code setRecipe} and streams every ingredient
 * directly to an {@link IRecipeIngredientCollector} as raw {@code (IIngredientType, ingredient)} pairs,
 * preserving the slot index of each ingredient, without JEI's internal ingredient wrapping.
 */
public class IngredientCollectorBuilder implements IRecipeLayoutBuilder {
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;
	private final IRecipeIngredientCollector collector;
	private int nextSlotIndex = 0;

	public IngredientCollectorBuilder(IIngredientManagerInternal ingredientManager, ContextMap contextMap, IRecipeIngredientCollector collector) {
		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
		this.collector = collector;
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		return addSlot(role);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		return new CollectorSlotBuilder(ingredientManager, contextMap, collector, role, nextSlotIndex++);
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		return addSlot(role);
	}

	@Override
	public void moveRecipeTransferButton(int posX, int posY) {
	}

	@Override
	public void setShapeless() {
	}

	@Override
	public void setShapeless(int posX, int posY) {
	}

	@Override
	public void createFocusLink(IIngredientAcceptor<?>... slots) {
	}
}
