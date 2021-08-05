package me.luxtix.haybale.util;

import me.luxtix.haybale.MinecraftInstance;
import me.luxtix.haybale.features.command.Command;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class WorldUtil implements MinecraftInstance {
  public static void placeBlock(BlockPos pos) {
    for (EnumFacing enumFacing : EnumFacing.values()) {
      if (!mc.world.getBlockState(pos.offset(enumFacing)).getBlock().equals(Blocks.AIR) && !isIntercepted(pos)) {
        Vec3d vec = new Vec3d(pos.getX() + 0.5D + enumFacing.getXOffset() * 0.5D, pos.getY() + 0.5D + enumFacing.getYOffset() * 0.5D, pos.getZ() + 0.5D + enumFacing.getZOffset() * 0.5D);
        float[] old = { mc.player.rotationYaw, mc.player.rotationPitch };
        mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation((float)Math.toDegrees(Math.atan2(vec.z - mc.player.posZ, vec.x - mc.player.posX)) - 90.0F, (float)-Math.toDegrees(Math.atan2(vec.y - mc.player.posY + mc.player.getEyeHeight(), Math.sqrt((vec.x - mc.player.posX) * (vec.x - mc.player.posX) + (vec.z - mc.player.posZ) * (vec.z - mc.player.posZ)))), mc.player.onGround));
        mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING));
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos.offset(enumFacing), enumFacing.getOpposite(), new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(old[0], old[1], mc.player.onGround));
        return;
      } 
    } 
  }
  
  public static void placeBlock(BlockPos pos, int slot) {
    if (slot == -1)
      return; 
    int prev = mc.player.inventory.currentItem;
    mc.player.inventory.currentItem = slot;
    placeBlock(pos);
    mc.player.inventory.currentItem = prev;
  }
  
  public static boolean isIntercepted(BlockPos pos) {
    for (Entity entity : mc.world.loadedEntityList) {
      if ((new AxisAlignedBB(pos)).intersects(entity.getEntityBoundingBox()))
        return true; 
    } 
    return false;
  }
  
  public static BlockPos GetLocalPlayerPosFloored() {
    return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
  }
  
  public static boolean canBreak(BlockPos pos) {
    return (mc.world.getBlockState(pos).getBlock().getBlockHardness(mc.world.getBlockState(pos), (World)mc.world, pos) != -1.0F);
  }
  
  public static void disconnectFromWorld(Command command) {
    mc.world.sendQuittingDisconnectingPacket();
    mc.loadWorld(null);
    mc.displayGuiScreen((GuiScreen)new GuiMainMenu());
  }
}
