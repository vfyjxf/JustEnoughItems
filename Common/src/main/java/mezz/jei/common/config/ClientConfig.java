package mezz.jei.common.config;

import com.google.common.base.Preconditions;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;
import mezz.jei.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class ClientConfig implements IClientConfig {
	@Nullable
	private static IClientConfig instance;

	// appearance
	private final ConfigValue<Boolean> centerSearchBarEnabled;
	private final ConfigValue<Integer> maxRecipeGuiHeight;

	// cheat_mode
	private final ConfigValue<GiveMode> giveMode;
	private final ConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled;
	private final ConfigValue<Boolean> showHiddenItemsEnabled;

	// bookmarks
	private final ConfigValue<Boolean> addBookmarksToFrontEnabled;
	private final ConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures;
	private final ConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled;
	private final ConfigValue<Boolean> dragToRearrangeBookmarksEnabled;

	// history
	private final ConfigValue<Boolean> historyEnabled;
	private final ConfigValue<Integer> maxHistoryRows;
	private final ConfigValue<HistoryViewSide> historyViewSide;

	// advanced
	private final ConfigValue<Boolean> lowMemorySlowSearchEnabled;
	private final ConfigValue<Boolean> catchRenderErrorsEnabled;
	private final ConfigValue<Boolean> lookupFluidContentsEnabled;
	private final ConfigValue<Boolean> lookupBlockTagsEnabled;
	private final ConfigValue<Boolean> showTagRecipesEnabled;
	private final ConfigValue<Boolean> showCreativeTabNamesEnabled;

	// input
	private final ConfigValue<Integer> dragDelayMs;
	private final ConfigValue<Integer> smoothScrollRate;

	// sorting
	private final ConfigValue<List<IngredientSortStage>> ingredientSorterStages;
	private final ConfigValue<List<RecipeSorterStage>> recipeSorterStages;

	// tags
	private final ConfigValue<Boolean> tagContentTooltipEnabled;
	private final ConfigValue<Boolean> hideSingleIngredientTagsEnabled;

	public ClientConfig(IConfigSchemaBuilder schema) {
		instance = this;

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();

		IConfigCategoryBuilder appearance = schema.addCategory("appearance");
		centerSearchBarEnabled = appearance.addBoolean(
			"CenterSearch",
			defaultCenterSearchBar,
			"Move the JEI search bar to the bottom center of the screen."
		);
		maxRecipeGuiHeight = appearance.addInteger(
			"RecipeGuiHeight",
			defaultRecipeGuiHeight,
			minRecipeGuiHeight,
			Integer.MAX_VALUE,
			"Max recipe GUI height."
		);

		IConfigCategoryBuilder cheatMode = schema.addCategory("cheat_mode");
		giveMode = cheatMode.addEnum(
			"GiveMode",
			GiveMode.defaultGiveMode,
			"Choose if JEI should give ingredients directly to the inventory or pick them up with the mouse."
		);
		cheatToHotbarUsingHotkeysEnabled = cheatMode.addBoolean(
			"CheatToHotbarUsingHotkeysEnabled",
			false,
			"Enable cheating items into the hotbar by using Shift + numeric keys."
		);
		showHiddenItemsEnabled = cheatMode.addBoolean(
			"ShowHiddenItems",
			false,
			"Enable showing items that are not in the creative menu."
		);

		IConfigCategoryBuilder bookmarks = schema.addCategory("bookmarks");
		addBookmarksToFrontEnabled = bookmarks.addBoolean(
			"AddBookmarksToFrontEnabled",
			false,
			"Add new bookmarks to the front of the bookmark list instead of the end."
		);
		bookmarkTooltipFeatures = bookmarks.addList(
			"BookmarkTooltipFeatures",
			BookmarkTooltipFeature.DEFAULT_BOOKMARK_TOOLTIP_FEATURES,
			new ListSerializer<>(new EnumSerializer<>(BookmarkTooltipFeature.class)),
			"Extra features for bookmark tooltips."
		);
		holdShiftToShowBookmarkTooltipFeaturesEnabled = bookmarks.addBoolean(
			"HoldShiftToShowBookmarkTooltipFeatures",
			true,
			"Hold Shift to show bookmark tooltip features."
		);
		dragToRearrangeBookmarksEnabled = bookmarks.addBoolean(
			"DragToRearrangeBookmarksEnabled",
			true,
			"Drag bookmarks to rearrange them in the list."
		);

		IConfigCategoryBuilder history = schema.addCategory("history");

		historyEnabled = history.addBoolean(
			"HistoryEnabled",
			false,
			"Enable the history overlay."
		);
		maxHistoryRows = history.addInteger(
			"MaxHistoryRows",
			1,
			0,
			7,
			"Max number of rows in the history overlay."
		);
		historyViewSide = history.addEnum(
				"HistoryViewSide",
				HistoryViewSide.LEFT,
				"Side of the screen to show the history overlay on."
		);

		IConfigCategoryBuilder advanced = schema.addCategory("advanced");
		lowMemorySlowSearchEnabled = advanced.addBoolean(
			"LowMemorySlowSearchEnabled",
			false,
			"Set low-memory mode (makes search very slow but uses less RAM)."
		);
		catchRenderErrorsEnabled = advanced.addBoolean(
			"CatchRenderErrorsEnabled",
			!isDev,
			"Catch render errors from ingredients and attempt to recover from them instead of crashing."
		);
		lookupFluidContentsEnabled = advanced.addBoolean(
			"lookupFluidContentsEnabled",
			false,
			"When looking up recipes with items that contain fluids, also look up recipes for the fluids."
		);
		lookupBlockTagsEnabled = advanced.addBoolean(
			"lookupBlockTagsEnabled",
			true,
			"When searching for item tags, also include tags for the default blocks contained in the items."
		);
		showTagRecipesEnabled = advanced.addBoolean(
			"showTagRecipesEnabled",
			isDev,
			"Show recipes for ingredient tags like item tags and block tags."
		);
		showCreativeTabNamesEnabled = advanced.addBoolean(
			"showCreativeTabNamesEnabled",
			false,
			"Show creative tab names in ingredient tooltips."
		);

		IConfigCategoryBuilder input = schema.addCategory("input");
		dragDelayMs = input.addInteger(
			"dragDelayInMilliseconds",
			150,
			0,
			1000,
			"Number of milliseconds before a long mouse click is considered a drag operation."
		);
		smoothScrollRate = input.addInteger(
			"smoothScrollRate",
			9,
			1,
			50,
			"Scroll rate for scrolling the mouse wheel in smooth-scrolling scroll boxes. Measured in pixels."
		);

		IConfigCategoryBuilder sorting = schema.addCategory("sorting");
		ingredientSorterStages = sorting.addList(
			"IngredientSortStages",
			IngredientSortStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(IngredientSortStage.class)),
			"Sorting order for the ingredient list."
		);
		recipeSorterStages = sorting.addList(
			"RecipeSorterStages",
			RecipeSorterStage.defaultStages,
			new ListSerializer<>(new EnumSerializer<>(RecipeSorterStage.class)),
			"Sorting order for displayed recipes."
		);

		IConfigCategoryBuilder tags = schema.addCategory("tags");
		tagContentTooltipEnabled = tags.addBoolean(
			"TagContentTooltipEnabled",
			true,
			"Show tag content in tooltips."
		);
		hideSingleIngredientTagsEnabled = tags.addBoolean(
			"HideSingleIngredientTagsEnabled",
			true,
			"Hide tags that only have 1 ingredient."
		);
	}

	/**
	 * Only use this for hacky stuff like the debug plugin
	 */
	@Deprecated
	public static IClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	@Override
	public boolean isCenterSearchBarEnabled() {
		return centerSearchBarEnabled.get();
	}

	@Override
	public IJeiConfigValue<Boolean> getCenterSearchBarConfig() {
		return centerSearchBarEnabled;
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled.get();
	}

	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return catchRenderErrorsEnabled.get();
	}

	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return cheatToHotbarUsingHotkeysEnabled.get();
	}

	@Override
	public boolean isAddingBookmarksToFrontEnabled() {
		return addBookmarksToFrontEnabled.get();
	}

	@Override
	public IJeiConfigValue<Boolean> getAddBookmarkToFrontConfig() {
		return addBookmarksToFrontEnabled;
	}

	@Override
	public boolean isLookupFluidContentsEnabled() {
		return lookupFluidContentsEnabled.get();
	}

	@Override
	public boolean isLookupBlockTagsEnabled() {
		return lookupBlockTagsEnabled.get();
	}

	@Override
	public GiveMode getGiveMode() {
		return giveMode.get();
	}

	@Override
	public IJeiConfigValue<GiveMode> getGiveModeConfig() {
		return giveMode;
	}

	@Override
	public boolean isShowHiddenItemsEnabled() {
		return showHiddenItemsEnabled.get();
	}

	@Override
	public List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		return bookmarkTooltipFeatures.get();
	}

	@Override
	public IJeiConfigValue<List<BookmarkTooltipFeature>> getBookmarkTooltipFeaturesConfig() {
		return bookmarkTooltipFeatures;
	}

	@Override
	public boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return holdShiftToShowBookmarkTooltipFeaturesEnabled.get();
	}

	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return dragToRearrangeBookmarksEnabled.get();
	}

	@Override
	public boolean isHistoryEnabled() {
		return historyEnabled.get();
	}

	@Override
	public IJeiConfigValue<Boolean> getHistoryEnabledConfig() {
		return historyEnabled;
	}

	@Override
	public int getMaxHistoryRows() {
		return maxHistoryRows.get();
	}

	@Override
	public IJeiConfigValue<Integer> getMaxHistoryRowsConfig() {
		return maxHistoryRows;
	}

	@Override
	public HistoryViewSide getHistoryViewSide() {
		return historyViewSide.get();
	}

	@Override
	public IJeiConfigValue<HistoryViewSide> getHistoryViewSideConfig() {
		return historyViewSide;
	}

	@Override
	public int getDragDelayMs() {
		return dragDelayMs.get();
	}

	@Override
	public int getSmoothScrollRate() {
		return smoothScrollRate.get();
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return maxRecipeGuiHeight.get();
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return ingredientSorterStages.get();
	}

	@Override
	public Set<RecipeSorterStage> getRecipeSorterStages() {
		return Set.copyOf(recipeSorterStages.getValue());
	}

	@Override
	public void enableRecipeSorterStage(RecipeSorterStage stage) {
		List<RecipeSorterStage> recipeSorterStages = this.recipeSorterStages.get();
		if (!recipeSorterStages.contains(stage)) {
			recipeSorterStages = new ArrayList<>(recipeSorterStages);
			recipeSorterStages.add(stage);
			this.recipeSorterStages.set(recipeSorterStages);
		}
	}

	@Override
	public void disableRecipeSorterStage(RecipeSorterStage stage) {
		List<RecipeSorterStage> recipeSorterStages = this.recipeSorterStages.get();
		if (recipeSorterStages.contains(stage)) {
			recipeSorterStages = new ArrayList<>(recipeSorterStages);
			recipeSorterStages.remove(stage);
			this.recipeSorterStages.set(recipeSorterStages);
		}
	}

	@Override
	public boolean isTagContentTooltipEnabled() {
		return tagContentTooltipEnabled.get();
	}

	@Override
	public boolean isHideSingleIngredientTagsEnabled() {
		return hideSingleIngredientTagsEnabled.get();
	}

	@Override
	public boolean isShowTagRecipesEnabled() {
		return showTagRecipesEnabled.get();
	}

	@Override
	public boolean isShowCreativeTabNamesEnabled() {
		return showCreativeTabNamesEnabled.get();
	}
}
