package com.lunartyx.invasionmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TntEntity.class)
public interface TntEntityAccessor {
    @Accessor("causingEntity")
    void invasionmod$setCausingEntity(@Nullable LivingEntity entity);
}
