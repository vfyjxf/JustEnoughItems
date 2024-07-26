package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class SuspiciousStewSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final SuspiciousStewSubtypeInterpreter INSTANCE = new SuspiciousStewSubtypeInterpreter();

	private SuspiciousStewSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		SuspiciousStewEffects suspiciousStewEffects = itemStack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
		if (suspiciousStewEffects == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		List<SuspiciousStewEffects.Entry> effects = suspiciousStewEffects.effects();
		List<String> strings = new ArrayList<>();
		for (SuspiciousStewEffects.Entry e : effects) {
			String effect = e.effect().getRegisteredName();
			int duration = e.duration();
			strings.add(effect + "." + duration);
		}

		StringJoiner joiner = new StringJoiner(",", "[", "]");
		strings.sort(null);
		for (String s : strings) {
			joiner.add(s);
		}
		return joiner.toString();
	}
}