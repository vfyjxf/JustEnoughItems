package mezz.jei.gui.ingredients;

import com.google.common.collect.Iterables;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class ListGroupElementInfo implements IListElementInfo {

	private @Nullable ListGroupElement element;
	private final List<IListElementInfo<?>> elements = new ArrayList<>();
	private final IModIdHelper modIdHelper;
	private final IngredientGroupInfo groupInfo;

	public ListGroupElementInfo(IngredientGroupInfo groupInfo, IModIdHelper modIdHelper) {
		this.groupInfo = groupInfo;
		this.modIdHelper = modIdHelper;
	}

	public IngredientGroupInfo getGroupInfo() {
		return groupInfo;
	}

	public void addElement(IListElementInfo<?> element) {
		this.elements.add(element);
	}

	public List<IListElementInfo<?>> getElements() {
		return elements;
	}

	@Override
	public List<String> getNames() {
		return elements.stream()
					   .map(IListElementInfo::getNames)
					   .flatMap(List::stream)
					   .toList();
	}

	@Override
	public String getModNameForSorting() {
		return modIdHelper.getModNameForModId(groupInfo.id().getNamespace());
	}

	@Override
	public List<String> getModNames() {
		return elements.stream()
					   .map(IListElementInfo::getModNames)
					   .flatMap(List::stream)
					   .toList();
	}

	@Override
	public List<String> getModIds() {
		return List.of(groupInfo.id().getNamespace());
	}

	@Override
	public @Unmodifiable Set<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		return Set.of();
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		return elements.stream()
					   .map(info -> info.getTagStrings(ingredientManager))
					   .flatMap(Collection::stream)
					   .toList();
	}

	@Override
	public Stream<Identifier> getTagIds(IIngredientManager ingredientManager) {
		return elements.stream()
					   .flatMap(info -> info.getTagIds(ingredientManager));
	}

	@Override
	public Iterable<Integer> getColors(IIngredientManager ingredientManager) {
		return Iterables.concat(
				elements.stream()
						.map(info -> info.getColors(ingredientManager))
						.toList()
		);
	}

	@Override
	public @Unmodifiable Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager) {
		return elements.stream()
					   .map(info -> info.getCreativeTabsStrings(ingredientManager))
					   .flatMap(Collection::stream)
					   .toList();
	}

	@Override
	public Identifier getIdentifier() {
		return groupInfo.id();
	}

	@Override
	public IListElement getElement() {
		if (element == null) {
			int createdIndex = ListElementInfo.elementCount++;
			element = new ListGroupElement(
					groupInfo,
					elements.stream()
							.map(IListElementInfo::getElement)
							.toList(),
					createdIndex
			);
		}
		return element;
	}

	@Override
	public ITypedIngredient getTypedIngredient() {
		return getElement().getTypedIngredient();
	}

	@Override
	public int getCreatedIndex() {
		return getElement().getCreatedIndex();
	}
}
