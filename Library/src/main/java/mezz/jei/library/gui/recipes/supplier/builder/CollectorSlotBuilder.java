package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.TilingDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.IRecipeIngredientCollector;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Minimal version of {@link IngredientSlotBuilder} that forwards ingredients directly
 * to an {@link IRecipeIngredientCollector} as raw {@code (IIngredientType, ingredient)} pairs,
 * without wrapping them into {@link ITypedIngredient} or {@link SlotIngredient} first.
 *
 * <p>Only {@link SlotDisplay} and {@link Ingredient} additions go through JEI's internal
 * resolution (which inherently produces typed ingredients); their results are unwrapped
 * again before being passed to the collector.</p>
 */
public class CollectorSlotBuilder implements IRecipeSlotBuilder {
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;
	private final IRecipeIngredientCollector collector;
	private final RecipeIngredientRole role;
	private final int slotIndex;

	public CollectorSlotBuilder(IIngredientManagerInternal ingredientManager, ContextMap contextMap, IRecipeIngredientCollector collector, RecipeIngredientRole role, int slotIndex) {
		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
		this.collector = collector;
		this.role = role;
		this.slotIndex = slotIndex;
	}

	@Override
	public ContextMap getContextMap() {
		return contextMap;
	}

	@Override
	public CollectorSlotBuilder add(SlotDisplay slotDisplay) {
		ingredientManager.resolveSlotDisplay(contextMap, role, slotDisplay)
			.forEach(this::emitUnwrapped);
		return this;
	}

	@Override
	public <I> CollectorSlotBuilder add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		ingredientManager.resolveSlotDisplay(ingredientType, contextMap, role, slotDisplay)
			.forEach(this::emitUnwrapped);
		return this;
	}

	@Override
	public CollectorSlotBuilder add(ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			emit(VanillaTypes.ITEM_STACK, itemStack);
		}
		return this;
	}

	@Override
	public CollectorSlotBuilder add(ItemLike itemLike) {
		return add(new ItemStack(itemLike));
	}

	@Override
	public CollectorSlotBuilder add(ItemStackTemplate itemStackTemplate) {
		return add(new ItemStack(itemStackTemplate.typeHolder(), itemStackTemplate.count(), itemStackTemplate.components()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public CollectorSlotBuilder add(Fluid fluid) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), fluidHelper.bucketVolume(), DataComponentPatch.EMPTY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CollectorSlotBuilder add(Fluid fluid, long amount) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CollectorSlotBuilder add(Fluid fluid, long amount, DataComponentPatch component) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		return addFluidInternal(fluidHelper, fluid.builtInRegistryHolder(), amount, component);
	}

	private <T> CollectorSlotBuilder addFluidInternal(IPlatformFluidHelperInternal<T> fluidHelper, Holder<Fluid> fluidHolder, long amount, DataComponentPatch component) {
		T fluidStack = fluidHelper.create(fluidHolder, amount, component);
		IIngredientTypeWithSubtypes<Fluid, T> fluidIngredientType = fluidHelper.getFluidIngredientType();
		emit(fluidIngredientType, fluidStack);
		return this;
	}

	@Override
	public CollectorSlotBuilder add(Ingredient ingredient) {
		return add(ingredient.display());
	}

	@Override
	public <I> CollectorSlotBuilder add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		return add(ingredientType, ingredient.display());
	}

	@Override
	public <I> CollectorSlotBuilder add(ITypedIngredient<I> typedIngredient) {
		emit(typedIngredient.getType(), typedIngredient.getIngredient());
		return this;
	}

	@Override
	public <I> CollectorSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
		if (ingredient != null) {
			emit(ingredientType, ingredient);
		}
		return this;
	}

	@Override
	public <I> CollectorSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		for (I ingredient : ingredients) {
			add(ingredientType, ingredient);
		}
		return this;
	}

	@Override
	public CollectorSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		for (Object ingredient : ingredients) {
			if (ingredient != null) {
				ingredientManager.getIngredientTypeChecked(ingredient)
					.ifPresent(ingredientType -> emit(ingredientType, ingredient));
			}
		}
		return this;
	}

	@Override
	public CollectorSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		for (ITypedIngredient<?> typedIngredient : ingredients) {
			add(typedIngredient);
		}
		return this;
	}

	@Override
	public CollectorSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		for (Optional<ITypedIngredient<?>> typedIngredient : ingredients) {
			typedIngredient.ifPresent(this::add);
		}
		return this;
	}

	@Override
	public CollectorSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		for (ItemStack itemStack : itemStacks) {
			add(itemStack);
		}
		return this;
	}

	private void emitUnwrapped(SlotIngredient<?> slotIngredient) {
		ITypedIngredient<?> typedIngredient = slotIngredient.typedIngredient();
		emit(typedIngredient.getType(), typedIngredient.getIngredient());
	}

	private <T> void emit(IIngredientType<T> ingredientType, Object ingredient) {
		this.collector.addIngredient(role, slotIndex, ingredientType, ingredientType.getIngredientClass().cast(ingredient));
	}

	@Override
	public CollectorSlotBuilder setStandardSlotBackground() {
		return this;
	}

	@Override
	public CollectorSlotBuilder setOutputSlotBackground() {
		return this;
	}

	@Override
	public CollectorSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public CollectorSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public CollectorSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		return this;
	}

	@Override
	public CollectorSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height, TilingDirection tilingDirection) {
		return this;
	}

	@Override
	public <T> CollectorSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		return this;
	}

	@Override
	public CollectorSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
		return this;
	}

	@Override
	public CollectorSlotBuilder setSlotName(String slotName) {
		return this;
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}

	@Override
	public CollectorSlotBuilder setPosition(int xPos, int yPos) {
		return this;
	}

	@Override
	public CollectorSlotBuilder setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return this;
	}
}
