package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;

import java.util.ArrayList;
import java.util.List;

public final class ListGroupElement implements IListElement {
	private final IngredientGroupInfo groupInfo;
	private final int createdIndex;
	private final List<IListElement> members;

	public ListGroupElement(IngredientGroupInfo groupInfo) {
		this(groupInfo, new ArrayList<>());
	}

	public ListGroupElement(IngredientGroupInfo groupInfo, List<IListElement> members) {
		this.groupInfo = groupInfo;
		this.createdIndex = ListElementInfo.elementCount++;
		this.members = new ArrayList<>(members);
	}

	public void addMember(IListElement member) {
		this.members.add(member);
	}

	public IngredientGroupInfo getGroupInfo() {
		return groupInfo;
	}

	public List<IListElement> getMembers() {
		return members;
	}

	@Override
	public ITypedIngredient<?> getTypedIngredient() {
		return members.getFirst().getTypedIngredient();
	}

	@Override
	public int getSortedIndex() {
		if (members.isEmpty()) {
			return createdIndex;
		}
		return members.stream()
					  .mapToInt(IListElement::getSortedIndex)
					  .min()
					  .orElse(createdIndex);
	}

	@Override
	public void setSortedIndex(int sortIndex) {
		// Group sort index derived from members, no-op
	}

	@Override
	public int getCreatedIndex() {
		return createdIndex;
	}

	@Override
	public boolean isVisible() {
		return members.stream().anyMatch(IListElement::isVisible);
	}

	@Override
	public void setVisible(boolean visible) {
	}
}
