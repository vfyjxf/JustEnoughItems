package mezz.jei.common.config;

import mezz.jei.api.runtime.config.IJeiConfigValue;

import java.util.List;
import java.util.Set;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	IJeiConfigValue<Boolean> getCenterSearchBarConfig();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCatchRenderErrorsEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	boolean isAddingBookmarksToFrontEnabled();

	IJeiConfigValue<Boolean> getAddBookmarkToFrontConfig();

	boolean isLookupFluidContentsEnabled();

	boolean isLookupBlockTagsEnabled();

	GiveMode getGiveMode();

	IJeiConfigValue<GiveMode> getGiveModeConfig();

	boolean isShowHiddenItemsEnabled();

	List<BookmarkTooltipFeature> getBookmarkTooltipFeatures();

	IJeiConfigValue<List<BookmarkTooltipFeature>> getBookmarkTooltipFeaturesConfig();

	boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled();

	boolean isDragToRearrangeBookmarksEnabled();

	boolean isHistoryEnabled();

	IJeiConfigValue<Boolean> getHistoryEnabledConfig();

	int getMaxHistoryRows();

	IJeiConfigValue<Integer> getMaxHistoryRowsConfig();

	HistoryViewSide getHistoryViewSide();

	IJeiConfigValue<HistoryViewSide> getHistoryViewSideConfig();

	int getDragDelayMs();

	int getSmoothScrollRate();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	Set<RecipeSorterStage> getRecipeSorterStages();

	void enableRecipeSorterStage(RecipeSorterStage stage);

	void disableRecipeSorterStage(RecipeSorterStage stage);

	boolean isTagContentTooltipEnabled();

	boolean isHideSingleIngredientTagsEnabled();

	boolean isShowTagRecipesEnabled();

	boolean isShowCreativeTabNamesEnabled();
}
