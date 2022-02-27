package com.github.mikn.end_respawn_anchor.init;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.block.EndRespawnAnchorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EndRespawnAnchor.MODID);
    public static final RegistryObject<Block> END_RESPAWN_ANCHOR = BLOCKS.register("end_respawn_anchor", () -> new EndRespawnAnchorBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel((p_152639_) -> EndRespawnAnchorBlock.getScaledChargeLevel(p_152639_, 15))));
}
