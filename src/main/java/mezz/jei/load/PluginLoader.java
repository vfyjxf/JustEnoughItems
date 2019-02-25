package mezz.jei.load;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.ingredients.SubtypeManager;
import mezz.jei.load.registration.AdvancedRegistration;
import mezz.jei.load.registration.GuiHandlerRegistration;
import mezz.jei.load.registration.RecipeCatalystRegistration;
import mezz.jei.load.registration.RecipeCategoryRegistration;
import mezz.jei.load.registration.RecipeRegistration;
import mezz.jei.load.registration.RecipeTransferRegistration;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.recipes.RecipeManager;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;
import mezz.jei.util.StackHelper;

public class PluginLoader {
	private final LoggedTimer timer;
	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;
	private final AdvancedRegistration advancedRegistration;
	private final IngredientManager ingredientManager;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IHideModeConfig hideModeConfig;
	private final BookmarkConfig bookmarkConfig;
	private final JeiHelpers jeiHelpers;
	private final RecipeTransferRegistration recipeTransferRegistration;
	private final GuiHandlerRegistration guiHandlerRegistration;
	private final ImmutableList<IRecipeCategory> recipeCategories;
	private final ImmutableListMultimap<ResourceLocation, Object> recipeCatalysts;
	private final ImmutableListMultimap<ResourceLocation, Object> recipes;
	private final ImmutableList<IRecipeManagerPlugin> recipeManagerPlugins;
	@Nullable
	private RecipeManager recipeManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private BookmarkList bookmarkList;

	public PluginLoader(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		Textures textures,
		IHideModeConfig hideModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		BookmarkConfig bookmarkConfig,
		IModIdHelper modIdHelper,
		boolean debugMode)
	{
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.hideModeConfig = hideModeConfig;
		this.bookmarkConfig = bookmarkConfig;
		this.timer = new LoggedTimer();
		this.modIdHelper = modIdHelper;
		this.blacklist = new IngredientBlacklistInternal();

		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		PluginCaller.callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistration));
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);

		ModIngredientRegistration modIngredientManager = new ModIngredientRegistration();
		PluginCaller.callOnPlugins("Registering ingredients", plugins, p -> p.registerIngredients(modIngredientManager, subtypeManager));
		ingredientManager = modIngredientManager.createIngredientManager(modIdHelper, blacklist, debugMode);
		Internal.setIngredientManager(ingredientManager);

		StackHelper stackHelper = new StackHelper(subtypeManager);
		GuiHelper guiHelper = new GuiHelper(ingredientManager, textures);
		jeiHelpers = new JeiHelpers(guiHelper, stackHelper, modIdHelper);
		Internal.setHelpers(jeiHelpers);

		VanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory(ingredientManager);
		IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper();
		recipeTransferRegistration = new RecipeTransferRegistration(jeiHelpers.getStackHelper(), handlerHelper);

		RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration();
		PluginCaller.callOnPlugins("Registering categories", plugins, p -> p.registerCategories(recipeCategoryRegistration, jeiHelpers));
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory();
		ErrorUtil.checkNotNull(craftingCategory, "vanilla crafting category");
		PluginCaller.callOnPlugins("Registering vanilla category extensions", plugins, p -> p.registerVanillaCategoryExtensions(craftingCategory));
		ImmutableMap<ResourceLocation, IRecipeCategory> recipeCategoriesByUid = recipeCategoryRegistration.getRecipeCategoriesByUid();
		recipeCategories = recipeCategoryRegistration.getRecipeCategories();

		RecipeRegistration recipeRegistration = new RecipeRegistration(recipeCategoriesByUid);
		PluginCaller.callOnPlugins("Registering recipes", plugins, p -> p.registerRecipes(recipeRegistration, jeiHelpers, ingredientManager, vanillaRecipeFactory));
		PluginCaller.callOnPlugins("Registering recipes transfer handlers", plugins, p -> p.registerRecipeTransferHandlers(recipeTransferRegistration, jeiHelpers, handlerHelper));
		recipes = recipeRegistration.getRecipes();

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration();
		PluginCaller.callOnPlugins("Registering recipe catalysts", plugins, p -> p.registerRecipeCatalysts(recipeCatalystRegistration));
		recipeCatalysts = recipeCatalystRegistration.getRecipeCatalysts();

		guiHandlerRegistration = new GuiHandlerRegistration();
		PluginCaller.callOnPlugins("Registering gui handlers", plugins, p -> p.registerGuiHandlers(guiHandlerRegistration));

		advancedRegistration = new AdvancedRegistration();
		PluginCaller.callOnPlugins("Registering advanced plugins", plugins, p -> p.registerAdvanced(advancedRegistration, jeiHelpers));
		recipeManagerPlugins = advancedRegistration.getRecipeManagerPlugins();
	}

	public GuiHandlerRegistration getGuiHandlerRegistration() {
		return guiHandlerRegistration;
	}

	public JeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	public RecipeTransferRegistration getRecipeTransferRegistration() {
		return recipeTransferRegistration;
	}

	public RecipeManager getRecipeManager() {
		if (recipeManager == null) {
			timer.start("Building recipe registry");
			recipeManager = new RecipeManager(recipeCategories, recipes, recipeCatalysts, ingredientManager, recipeManagerPlugins);
			timer.stop();
		}
		return recipeManager;
	}

	public IngredientFilter getIngredientFilter() {
		if (ingredientFilter == null) {
			timer.start("Building ingredient list");
			NonNullList<IIngredientListElement<?>> ingredientList = IngredientListElementFactory.createBaseList(ingredientManager);
			timer.stop();
			timer.start("Building ingredient filter");
			ingredientFilter = new IngredientFilter(blacklist, ingredientFilterConfig, hideModeConfig, ingredientManager, modIdHelper);
			ingredientFilter.addIngredients(ingredientList, ingredientManager, modIdHelper);
			Internal.setIngredientFilter(ingredientFilter);
			timer.stop();
		}
		return ingredientFilter;
	}

	public IngredientManager getIngredientManager() {
		return ingredientManager;
	}

	public BookmarkList getBookmarkList() {
		if (bookmarkList == null) {
			timer.start("Building bookmarks");
			bookmarkList = new BookmarkList(ingredientManager, bookmarkConfig);
			bookmarkConfig.loadBookmarks(ingredientManager, bookmarkList);
			timer.stop();
		}
		return bookmarkList;
	}
}
