package com.github.mikn.end_respawn_anchor.capabilities;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerDataCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        // "era" is an aggrevation for End Respawn Anchor
        public static final ResourceLocation IDENTIFIER = new ResourceLocation(EndRespawnAnchor.MODID, "eracap");
        private final IPlayerDataCapability backend = new PlayerDataCapability();
        private final LazyOptional<IPlayerDataCapability> optionalData = LazyOptional.of(() -> backend);

        @Override
        public CompoundTag serializeNBT() {
            return this.backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.backend.deserializeNBT(nbt);
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return PlayerDataCapability.INSTANCE.orEmpty(cap, optionalData);
        }

        void invalidate() {
            this.optionalData.invalidate();
        }
    }
