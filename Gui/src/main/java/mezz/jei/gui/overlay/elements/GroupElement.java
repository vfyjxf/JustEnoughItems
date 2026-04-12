package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.ingredients.ListGroupElement;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"rawtypes"})
public class GroupElement implements IElement {

	private final ListGroupElement element;
	private final List<? extends IElement<?>> elements;
	private final Runnable onExpandedChange;
	public GroupElement(ListGroupElement element,Runnable onExpandedChange) {
		this.element = element;
		this.elements = element.elements().stream()
							   .map(e -> new IngredientElement<>(e.getTypedIngredient()))
							   .toList();
		this.onExpandedChange = onExpandedChange;
	}

	@Override
	public ITypedIngredient getTypedIngredient() {
		return elements.getFirst().getTypedIngredient();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return elements.getFirst().getBookmark();
	}

	@Override
	public @Nullable IDrawable createRenderOverlay() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer ingredientRenderer, IIngredientHelper ingredientHelper) {
		if (elements.size() <= 1) {
			elements.getFirst().getTooltip(tooltip, tooltipHelper, ingredientRenderer, ingredientHelper);
			tooltip.add(Component.empty());
			tooltip.add(element.groupInfo().getName().copy().withStyle(ChatFormatting.GRAY));
			String modName = tooltipHelper.getModIdHelper().getFormattedModNameForModId(element.groupInfo().id().getNamespace());
			MutableComponent addedBy = Component.translatable("jei.group.added_by", modName);
			tooltip.add(addedBy.withStyle(ChatFormatting.GRAY));
			return;
		}
		tooltip.add(element.groupInfo().getName());
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();
		IJeiKeyMapping groupAction = keyMappings.getGroupAction();
		if (!element.groupInfo().expanded()) {
			tooltip.addKeyUsageComponent("jei.group.expand", groupAction);
			tooltip.add(new GroupElementTooltipComponent(elements));
		} else {
			tooltip.addKeyUsageComponent("jei.group.collapse", groupAction);
		}
		String modName = tooltipHelper.getModIdHelper().getFormattedModNameForModId(element.groupInfo().id().getNamespace());
		MutableComponent addedBy = Component.translatable("jei.group.added_by", modName);
		tooltip.add(addedBy.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getGroupAction())) {
			if (input.isSimulate()) {
				return true;
			}
			element.groupInfo().setExpanded(!element.groupInfo().expanded());
			onExpandedChange.run();
			return true;
		}
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List list) {
	}

	private record GroupEntryElement<T>(IElement<T> element) implements IElement<T> {

		@Override
		public ITypedIngredient<T> getTypedIngredient() {
			return element.getTypedIngredient();
		}

		@Override
		public Optional<IBookmark> getBookmark() {
			return element.getBookmark();
		}

		@Override
		public @Nullable IDrawable createRenderOverlay() {
			return null;
		}

		@Override
		public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {

		}

		@Override
		public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
			element.getTooltip(tooltip, tooltipHelper, ingredientRenderer, ingredientHelper);
		}

		@Override
		public boolean isVisible() {
			return false;
		}
	}
}
