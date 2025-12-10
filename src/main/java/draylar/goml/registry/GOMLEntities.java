package draylar.goml.registry;

import draylar.goml.GetOffMyLawn;
import draylar.goml.block.entity.ClaimAnchorBlockEntity;
import draylar.goml.block.entity.ClaimAugmentBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GOMLEntities {

    public static final BlockEntityType<ClaimAnchorBlockEntity> CLAIM_ANCHOR = register(
            "claim_anchor",
            FabricBlockEntityTypeBuilder.create(
                    ClaimAnchorBlockEntity::new,
                    GOMLBlocks.ANCHORS.toArray(new Block[0])).build(null));

    public static final BlockEntityType<ClaimAugmentBlockEntity> CLAIM_AUGMENT = register(
            "claim_augment",
            FabricBlockEntityTypeBuilder.create(
                    ClaimAugmentBlockEntity::new,
                    GOMLBlocks.AUGMENTS.toArray(new Block[0])).build(null));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> entity) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, GetOffMyLawn.id(name), entity);
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> entity) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, GetOffMyLawn.id(name), entity);
    }

    public static void init() {
        PolymerBlockUtils.registerBlockEntity(CLAIM_ANCHOR, CLAIM_AUGMENT);
    }

    private GOMLEntities() {
        // NO-OP
    }
}
