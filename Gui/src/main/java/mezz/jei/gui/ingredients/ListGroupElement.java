package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public final class ListGroupElement implements IListElement {
	private final IngredientGroupInfo groupInfo;
	private final int createdIndex;
	private final List<IListElement<?>> elements = new ArrayList<>();

	public ListGroupElement(
			IngredientGroupInfo groupInfo
	) {
		this.groupInfo = groupInfo;
		this.createdIndex = ListElementInfo.elementCount++;
	}

	public void addElement(IListElement<?> element) {
		this.elements.add(element);
	}

	@Override
	public ITypedIngredient getTypedIngredient() {
		return elements.getFirst().getTypedIngredient();
	}

	@Override
	public int getSortedIndex() {
		return elements.getFirst().getSortedIndex();
	}

	@Override
	public void setSortedIndex(int sortIndex) {
		elements.getFirst().setSortedIndex(sortIndex);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
		//NOOP
	}

	public IngredientGroupInfo groupInfo() {return groupInfo;}

	public List<? extends IListElement<?>> elements() {return elements;}

	@Override
	public int getCreatedIndex() {
		return createdIndex;
	}
}
