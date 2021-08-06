package me.luxtix.haybale.mixin.mixins.accessors;

import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({CPacketUseEntity.class})
public interface AccessorCPacketUseEntity {
    @Accessor("entityId")
    void setEntityId(int paramInt);

    @Accessor("action")
    void setAction(CPacketUseEntity.Action paramAction);
}