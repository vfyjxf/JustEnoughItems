package mezz.jei.common.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.codecs.EnumCodec;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import mezz.jei.common.ingredients.group.IIngredientGroupSelector;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.common.ingredients.group.IngredientGroupType;
import mezz.jei.common.util.ServerConfigPathUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class IngredientGroupConfig {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final int VERSION = 1;
	private static final Codec<IngredientGroupType> TYPE_CODEC = EnumCodec.create(IngredientGroupType.class);

	private final Map<Identifier, IngredientGroupInfo> ingredientGroups = new LinkedHashMap<>();
	private final MapCodec<IngredientGroupInfo> codec;
	private final Path groupsDir;

	public IngredientGroupConfig(ICodecHelper codecHelper, IIngredientManager ingredientManager, Path groupsDir) {
		this.codec = getGroupCodec(codecHelper, ingredientManager);
		this.groupsDir = groupsDir;
	}

	private static Optional<Path> getPath(Path groupsDir) {
		return Optional.empty();
	}

	private RegistryOps<JsonElement> getRegistryOps(RegistryAccess registryAccess) {
		return registryAccess.createSerializationContext(JsonOps.INSTANCE);
	}

	private static MapCodec<IngredientGroupInfo> getGroupCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
		Codec<IIngredientGroupSelector> selectorCodec = TYPE_CODEC.dispatch(
			IIngredientGroupSelector::getType,
			type -> type.getCodec(codecHelper, ingredientManager)
		);
		return RecordCodecBuilder.mapCodec(instance ->
			instance.group(
				Identifier.CODEC.fieldOf("id").forGetter(IngredientGroupInfo::id),
				selectorCodec.listOf().fieldOf("selectors").forGetter(info ->
					info.selectors()
						.stream()
						.filter(s -> s.getType() != IngredientGroupType.DYNAMIC)
						.toList()
				),
				Codec.BOOL.optionalFieldOf("override", false).forGetter(IngredientGroupInfo::override),
				Codec.BOOL.optionalFieldOf("expanded", false).forGetter(IngredientGroupInfo::expanded)
			).apply(instance, IngredientGroupInfo::new)
		);
	}

	public void add(Identifier id, IIngredientGroupSelector selector) {
		this.ingredientGroups.computeIfAbsent(id, k -> new IngredientGroupInfo(k, new ArrayList<>(), false, false)).add(selector);
	}

	public void load(RegistryAccess registryAccess) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);
		List<IngredientGroupInfo> loaded = loadFromFile(registryOps);
		for (IngredientGroupInfo info : loaded) {
			if (info.override()) {
				ingredientGroups.put(info.id(), info);
			} else {
				IngredientGroupInfo existing = ingredientGroups.get(info.id());
				if (existing != null) {
					info.selectors().forEach(existing::add);
				} else {
					ingredientGroups.put(info.id(), info);
				}
			}
		}
	}

	public boolean save(RegistryAccess registryAccess) {
		return getPath(groupsDir)
			.map(path -> {
				Codec<IngredientGroupInfo> groupCodec = codec.codec();
				RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);

				List<IngredientGroupInfo> toSave = ingredientGroups.values().stream()
					.filter(info -> info.selectors().stream().anyMatch(s -> s.getType() != IngredientGroupType.DYNAMIC))
					.toList();

				try (BufferedWriter out = Files.newBufferedWriter(path)) {
					JsonArrayFileHelper.write(
						out,
						VERSION,
						toSave,
						groupCodec,
						registryOps,
						error -> {
							LOGGER.error("Encountered an error when saving groups config to file {}\n{}", path, error);
						},
						(element, exception) -> {
							LOGGER.error("Encountered an exception when saving groups config to file {}\n{}", path, element, exception);
						}
					);
					LOGGER.debug("Saved groups config to file: {}", path);
					return true;
				} catch (IOException e) {
					LOGGER.error("Failed to save groups config to file {}", path, e);
					return false;
				}
			})
			.orElse(false);
	}

	private List<IngredientGroupInfo> loadFromFile(RegistryOps<JsonElement> registryOps) {
		return getPath(groupsDir)
			.<List<IngredientGroupInfo>>map(path -> {
				if (!Files.exists(path)) {
					return List.of();
				}

				List<IngredientGroupInfo> groups;
				Codec<IngredientGroupInfo> groupCodec = codec.codec();

				try (BufferedReader reader = Files.newBufferedReader(path)) {
					groups = JsonArrayFileHelper.read(
						reader,
						VERSION,
						groupCodec,
						registryOps,
						(element, error) -> {
							LOGGER.error("Encountered an error when loading groups config from file {}\n{}\n{}", path, element, error);
						},
						(element, exception) -> {
							LOGGER.error("Encountered an exception when loading groups config from file {}\n{}", path, element, exception);
						}
					);
					LOGGER.debug("Loaded groups config from file: {}", path);
				} catch (RuntimeException | IOException e) {
					LOGGER.error("Failed to load groups config from file {}", path, e);
					groups = new ArrayList<>();
				}

				return groups;
			})
			.orElseGet(List::of);
	}

	public Map<Identifier, IngredientGroupInfo> getIngredientGroups() {
		return Collections.unmodifiableMap(ingredientGroups);
	}

}
