package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.Translator;
import mezz.jei.core.util.WeakList;
import net.minecraft.client.renderer.Rect2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class IngredientManager implements IIngredientManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RegisteredIngredients registeredIngredients;
	private final WeakList<IIngredientListener> listeners = new WeakList<>();

	public IngredientManager(RegisteredIngredients registeredIngredients) {
		this.registeredIngredients = registeredIngredients;
	}

	@Override
	@Unmodifiable
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getAllIngredients();
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		return getIngredientTypeChecked(ingredient)
			.map(this::getIngredientHelper)
			.orElseThrow(() -> new IllegalArgumentException("Unknown ingredient class: " + ingredient.getClass()));
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientHelper();
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		return getIngredientTypeChecked(ingredient)
			.map(this::getIngredientRenderer)
			.orElseThrow(() -> new IllegalArgumentException("Unknown ingredient class: " + ingredient.getClass()));
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");

		return this.registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientRenderer();
	}

	@Override
	public Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
		return this.registeredIngredients.getIngredientTypes();
	}

	@Override
	public Optional<IIngredientType<?>> getIngredientTypeForUid(String ingredientTypeUid) {
		ErrorUtil.checkNotNull(ingredientTypeUid, "ingredientTypeUid");

		return this.registeredIngredients.getIngredientTypes()
			.stream()
			.filter(t -> ingredientTypeUid.equals(t.getUid()))
			.findFirst();
	}

	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);

		LOGGER.info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();
		Collection<V> validIngredients = ingredients.stream()
			.filter(i -> {
				if (!ingredientHelper.isValidIngredient(i)) {
					String errorInfo = ingredientHelper.getErrorInfo(i);
					LOGGER.error("Attempted to add an invalid Ingredient: {}", errorInfo);
					return false;
				}
				if (!ingredientHelper.isIngredientOnServer(i)) {
					String errorInfo = ingredientHelper.getErrorInfo(i);
					LOGGER.error("Attempted to add an Ingredient that is not on the server: {}", errorInfo);
					return false;
				}
				return true;
			})
			.toList();

		ingredientInfo.addIngredients(validIngredients);

		if (!this.listeners.isEmpty()) {
			List<ITypedIngredient<V>> typedIngredients = validIngredients.stream()
				.map(i -> TypedIngredient.createUnvalidated(ingredientType, i))
				.toList();

			this.listeners.forEach(listener -> listener.onIngredientsAdded(ingredientHelper, typedIngredients));
		}
	}

	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return this.registeredIngredients.getIngredientType(ingredient);
	}

	@Override
	public <B, I> Optional<IIngredientTypeWithSubtypes<B, I>> getIngredientTypeWithSubtypesFromBase(B baseIngredient) {
		ErrorUtil.checkNotNull(baseIngredient, "baseIngredient");
		return this.registeredIngredients.getIngredientTypeWithSubtypesFromBase(baseIngredient);
	}

	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");

		return this.registeredIngredients.getIngredientType(ingredientClass);
	}

	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		IngredientInfo<V> ingredientInfo = this.registeredIngredients.getIngredientInfo(ingredientType);

		LOGGER.info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		ingredientInfo.removeIngredients(ingredients);

		if (!this.listeners.isEmpty()) {
			List<ITypedIngredient<V>> typedIngredients = ingredients.stream()
				.flatMap(i -> TypedIngredient.createAndFilterInvalid(this, ingredientType, i, false).stream())
				.toList();

			IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

			this.listeners.forEach(listener -> listener.onIngredientsRemoved(ingredientHelper, typedIngredients));
		}
	}

	@Override
	public void registerIngredientListener(IIngredientListener listener) {
		ErrorUtil.checkNotNull(listener, "listener");
		this.listeners.add(listener);
	}

	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient) {
		return TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, false);
	}

	@Override
	public <V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> typedIngredient) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		IIngredientType<V> type = typedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = getIngredientHelper(type);
		return TypedIngredient.normalize(typedIngredient, ingredientHelper);
	}

	@Override
	public <V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> ingredientType, V ingredient, Rect2i area, boolean normalize) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(area, "area");
		return TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, normalize)
			.map(typedIngredient -> {
				ImmutableRect2i slotArea = new ImmutableRect2i(area);
				return new ClickableIngredient<>(typedIngredient, slotArea);
			});
	}

	@Override
	@Deprecated
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid) {
		return registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientByUid(ingredientUuid);
	}

	@Override
	public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> ingredientType, String ingredientUuid) {
		return registeredIngredients
			.getIngredientInfo(ingredientType)
			.getIngredientByUid(ingredientUuid)
			.flatMap(i -> TypedIngredient.createAndFilterInvalid(this, ingredientType, i, true));
	}

	@Override
	public Collection<String> getIngredientAliases(ITypedIngredient<?> ingredient) {
		return getIngredientAliasesInternal(ingredient);
	}

	private <T> Collection<String> getIngredientAliasesInternal(ITypedIngredient<T> typedIngredient) {
		return registeredIngredients
			.getIngredientInfo(typedIngredient.getType())
			.getIngredientAliases(typedIngredient.getIngredient())
			.stream()
			.map(Translator::translateToLocal)
			.sorted(String::compareToIgnoreCase)
			.toList();
	}
}
