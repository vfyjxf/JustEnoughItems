package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

public final class IngredientGroupInfo {
	private final Identifier id;
	private final List<IIngredientGroupSelector> selectors;
	private final boolean override;
	private boolean expanded;

	public IngredientGroupInfo(
			Identifier id,
			List<IIngredientGroupSelector> selectors,
			boolean override,
			boolean expanded
	) {
		this.id = id;
		this.selectors = selectors;
		this.override = override;
		this.expanded = expanded;
	}

	public Component getName() {
		return Component.translatable("jei.group." + id.getNamespace() + "." + id.getPath());
	}

	public void add(IIngredientGroupSelector selector) {
		selectors.add(selector);
	}

	public boolean isGroupMember(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		for (IIngredientGroupSelector selector : selectors) {
			if (selector.test(ingredient, ingredientManager)) {
				return true;
			}
		}
		return false;
	}

	public Identifier id() {return id;}

	public List<IIngredientGroupSelector> selectors() {return selectors;}

	public boolean override() {return override;}

	public boolean expanded() {return expanded;}

	public void setExpanded(boolean expanded) {this.expanded = expanded;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (IngredientGroupInfo) obj;
		return Objects.equals(this.id, that.id) &&
				Objects.equals(this.selectors, that.selectors) &&
				this.override == that.override;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, selectors, override);
	}

	@Override
	public String toString() {
		return "IngredientGroupInfo[" +
				"id=" + id + ", " +
				"selectors=" + selectors + ", " +
				"override=" + override + ']';
	}


}
