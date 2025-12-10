package draylar.goml.api;

import draylar.goml.other.LegacyNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public record DataKey<T>(Identifier key, T defaultValue, Function<T, Tag> serializer, Function<Tag, T> deserializer, @Nullable Supplier<T> defaultSupplier) {
    public DataKey(Identifier key, T defaultValue, Function<T, Tag> serializer, Function<Tag, T> deserializer) {
        this(key, defaultValue, serializer, deserializer, () -> defaultValue);
    }

    private static final Map<Identifier, DataKey<?>> REGISTRY = new HashMap<>();

    public DataKey {
        if (REGISTRY.containsKey(key)) {
            throw new RuntimeException("Duplicate key " + key + "! You can't register the same key twice!");
        }

        REGISTRY.put(key, this);
    }

    public static <T, C extends Collection<T>> DataKey<C> ofCollection(Identifier key, Supplier<C> collectionCreator, Function<T, Tag> serializer, Function<Tag, T> deserializer) {
        return new DataKey<>(key, collectionCreator.get(), (list) -> {
            var nbt = new ListTag();

            for (var i : list) {
                if (i != null) {
                    nbt.add(serializer.apply(i));
                }
            }

            return nbt;
        }, (nbt) -> {
            var list = collectionCreator.get();

            if (nbt instanceof CollectionTag nbtList)
            for (var i : nbtList) {
                if (i != null) {
                    list.add(deserializer.apply(i));
                }
            }

            return list;
        }, collectionCreator);
    }

    public static DataKey<String> ofString(Identifier key, String defaultValue) {
        return new DataKey<>(key, defaultValue, StringTag::valueOf, (nbt) -> nbt instanceof StringTag nbtString ? nbtString.value() : defaultValue);
    }

    public static DataKey<Boolean> ofBoolean(Identifier key, boolean defaultValue) {
        return new DataKey<>(key, defaultValue, ByteTag::valueOf, (nbt) -> nbt instanceof NumericTag nbtNumber ? nbtNumber.byteValue() > 0 : defaultValue);
    }

    public static DataKey<Integer> ofInt(Identifier key, int defaultValue) {
        return new DataKey<>(key, defaultValue, IntTag::valueOf, (nbt) -> nbt instanceof NumericTag nbtNumber ? nbtNumber.intValue() : defaultValue);
    }

    public static DataKey<UUID> ofUuid(Identifier key) {
        return new DataKey<>(key, Util.NIL_UUID, LegacyNbtHelper::fromUuid, LegacyNbtHelper::toUuid);
    }

    public static DataKey<Set<UUID>> ofUuidSet(Identifier key) {
        return ofCollection(key, HashSet::new, LegacyNbtHelper::fromUuid, LegacyNbtHelper::toUuid);
    }

    public static DataKey<Double> ofDouble(Identifier key, double defaultValue) {
        return new DataKey<>(key, defaultValue, DoubleTag::valueOf, (nbt) -> nbt instanceof NumericTag nbtNumber ? nbtNumber.doubleValue() : defaultValue);
    }

    public static DataKey<BlockPos> ofPos(Identifier key) {
        return new DataKey<>(key, null, LegacyNbtHelper::fromBlockPos, (nbt) -> nbt instanceof CompoundTag compound ? LegacyNbtHelper.toBlockPos(compound) : null);
    }

    @Nullable
    public static DataKey<?> getKey(Identifier key) {
        return REGISTRY.get(key);
    }

    public static Collection<Identifier> keys() {
        return REGISTRY.keySet();
    }

    public static <T extends Enum<T>> DataKey<T> ofEnum(Identifier key, Class<T> tClass, T defaultValue) {
        return new DataKey<>(key, defaultValue, (i) -> StringTag.valueOf(i.name()), (nbt) -> {
            var value = nbt instanceof StringTag string ? Enum.valueOf(tClass, string.value()) : null;
            return value != null ? value : defaultValue;
        });
    }
}
