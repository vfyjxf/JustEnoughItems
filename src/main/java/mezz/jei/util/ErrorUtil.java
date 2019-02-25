package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.Ingredients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ErrorUtil {
	private static Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static IModIdHelper modIdHelper;

	private ErrorUtil() {
	}

	public static void setModIdHelper(IModIdHelper modIdHelper) {
		ErrorUtil.modIdHelper = modIdHelper;
	}

	public static <T> String getInfoFromRecipe(T recipe, IRecipeCategory<T> recipeCategory) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		String recipeName = getNameForRecipe(recipe);
		recipeInfoBuilder.append(recipeName);

		Ingredients ingredients = new Ingredients();

		try {
			recipeCategory.setIngredients(recipe, ingredients);
		} catch (RuntimeException | LinkageError ignored) {
			recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		recipeInfoBuilder.append("\nOutputs:");
		Set<IIngredientType> outputTypes = ingredients.getOutputIngredients().keySet();
		for (IIngredientType<?> outputType : outputTypes) {
			List<String> ingredientOutputInfo = getIngredientOutputInfo(outputType, ingredients);
			recipeInfoBuilder.append('\n').append(outputType.getIngredientClass().getName()).append(": ").append(ingredientOutputInfo);
		}

		recipeInfoBuilder.append("\nInputs:");
		Set<IIngredientType> inputTypes = ingredients.getInputIngredients().keySet();
		for (IIngredientType<?> inputType : inputTypes) {
			List<String> ingredientInputInfo = getIngredientInputInfo(inputType, ingredients);
			recipeInfoBuilder.append('\n').append(inputType.getIngredientClass().getName()).append(": ").append(ingredientInputInfo);
		}

		return recipeInfoBuilder.toString();
	}

	private static <T> List<String> getIngredientOutputInfo(IIngredientType<T> ingredientType, IIngredients ingredients) {
		List<List<T>> outputs = ingredients.getOutputs(ingredientType);
		return getIngredientInfo(ingredientType, outputs);
	}

	private static <T> List<String> getIngredientInputInfo(IIngredientType<T> ingredientType, IIngredients ingredients) {
		List<List<T>> inputs = ingredients.getInputs(ingredientType);
		return getIngredientInfo(ingredientType, inputs);
	}

	public static String getNameForRecipe(Object recipe) {
		ResourceLocation registryName = null;
		if (recipe instanceof IRecipe) {
			registryName = ((IRecipe) recipe).getId();
		} else if (recipe instanceof IForgeRegistryEntry) {
			IForgeRegistryEntry registryEntry = (IForgeRegistryEntry) recipe;
			registryName = registryEntry.getRegistryName();
		}
		if (registryName != null) {
			String modId = registryName.getNamespace();
			if (modIdHelper != null) {
				String modName = modIdHelper.getModNameForModId(modId);
				return modName + " " + registryName + " " + recipe.getClass();
			}
			return registryName + " " + recipe.getClass();
		}
		try {
			return recipe.toString();
		} catch (RuntimeException e) {
			LOGGER.error("Failed recipe.toString", e);
			return recipe.getClass().toString();
		}
	}

	public static <T> String getIngredientInfo(T ingredient) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredient);
		return ingredientHelper.getErrorInfo(ingredient);
	}

	public static <T> List<String> getIngredientInfo(IIngredientType<T> ingredientType, List<? extends List<T>> ingredients) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredientType);
		List<String> allInfos = new ArrayList<>(ingredients.size());

		for (List<T> inputList : ingredients) {
			List<String> infos = new ArrayList<>(inputList.size());
			for (T input : inputList) {
				String errorInfo = ingredientHelper.getErrorInfo(input);
				infos.add(errorInfo);
			}
			allInfos.add(infos.toString());
		}

		return allInfos;
	}

	@SuppressWarnings("ConstantConditions")
	public static String getItemStackInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return "null";
		}
		Item item = itemStack.getItem();
		final String itemName;
		ResourceLocation registryName = item.getRegistryName();
		if (registryName != null) {
			itemName = registryName.toString();
		} else if (item instanceof ItemBlock) {
			final String blockName;
			Block block = ((ItemBlock) item).getBlock();
			if (block == null) {
				blockName = "null";
			} else {
				ResourceLocation blockRegistryName = block.getRegistryName();
				if (blockRegistryName != null) {
					blockName = blockRegistryName.toString();
				} else {
					blockName = block.getClass().getName();
				}
			}
			itemName = "ItemBlock(" + blockName + ")";
		} else {
			itemName = item.getClass().getName();
		}

		NBTTagCompound nbt = itemStack.getTag();
		if (nbt != null) {
			return itemStack + " " + itemName + " nbt:" + nbt;
		}
		return itemStack + " " + itemName;
	}

	public static void checkNotEmpty(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			throw new NullPointerException("ItemStack must not be null.");
		} else if (itemStack.isEmpty()) {
			String info = getItemStackInfo(itemStack);
			throw new IllegalArgumentException("ItemStack value must not be empty. " + info);
		}
	}

	public static void checkNotEmpty(@Nullable ItemStack itemStack, String name) {
		if (itemStack == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (itemStack.isEmpty()) {
			String info = getItemStackInfo(itemStack);
			throw new IllegalArgumentException("ItemStack " + name + " must not be empty. " + info);
		}
	}

	public static <T> void checkNotEmpty(@Nullable T[] values, String name) {
		if (values == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (values.length <= 0) {
			throw new IllegalArgumentException(name + " must not be empty.");
		}
		for (T value : values) {
			if (value == null) {
				throw new NullPointerException(name + " must not contain null values.");
			}
		}
	}

	public static void checkNotEmpty(@Nullable Collection values, String name) {
		if (values == null) {
			throw new NullPointerException(name + " must not be null.");
		} else if (values.isEmpty()) {
			throw new IllegalArgumentException(name + " must not be empty.");
		} else if (!(values instanceof NonNullList)) {
			for (Object value : values) {
				if (value == null) {
					throw new NullPointerException(name + " must not contain null values.");
				}
			}
		}
	}

	public static <T> void checkNotNull(@Nullable T object, String name) {
		if (object == null) {
			throw new NullPointerException(name + " must not be null.");
		}
	}

	public static <T> void checkIsValidIngredient(@Nullable T ingredient, String name) {
		checkNotNull(ingredient, name);
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		if (!ingredientHelper.isValidIngredient(ingredient)) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Invalid ingredient found. Parameter Name: " + name + " Ingredient Info: " + ingredientInfo);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void assertMainThread() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && !minecraft.isCallingFromMinecraftThread()) {
			Thread currentThread = Thread.currentThread();
			throw new IllegalStateException(
				"A JEI API method is being called by another mod from the wrong thread:\n" +
					currentThread + "\n" +
					"It must be called on the main thread by using Minecraft.addScheduledTask."
			);
		}
	}

	public static <T> ReportedException createRenderIngredientException(Throwable throwable, final T ingredient) {
		final IIngredientHelper<T> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(ingredient);
		CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
		CrashReportCategory crashreportcategory = crashreport.makeCategory("Ingredient being rendered");
		if (modIdHelper != null) {
			crashreportcategory.addDetail("Ingredient Mod", () -> {
				String modId = ingredientHelper.getModId(ingredient);
				return modIdHelper.getModNameForModId(modId);
			});
		}
		crashreportcategory.addDetail("Ingredient Info", () -> ingredientHelper.getErrorInfo(ingredient));
		throw new ReportedException(crashreport);
	}
}
