package me.luxtix.haybale.features.modules.combat;

import com.google.common.eventbus.Subscribe;
import java.lang.reflect.Field;
import me.luxtix.haybale.Phobos;
import me.luxtix.haybale.features.command.Command;
import me.luxtix.haybale.features.modules.Module;
import me.luxtix.haybale.features.modules.movement.ReverseStep;
import me.luxtix.haybale.features.setting.Setting;
import me.luxtix.haybale.util.MappingUtil;
import me.luxtix.haybale.util.BlockUtil;
import me.luxtix.haybale.util.InventoryUtil;
import me.luxtix.haybale.util.ItemUtil;
import me.luxtix.haybale.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;

public class InstantSelfFill extends Module {
  private final Setting<Float> height = register(new Setting("TPHeight", Float.valueOf(5.0F), Float.valueOf(-20.0F), Float.valueOf(20.0F)));
  
  private final Setting<Float> extraboost = register(new Setting("Extra", Float.valueOf(0.0F), Float.valueOf(-10.0F), Float.valueOf(10.0F)));
  
  public final Setting<Page> page = register(new Setting("Block", Page.EChest));
  
  private final Setting<Boolean> packetJump = register(new Setting("PacketJump", Boolean.valueOf(false), v -> !((Boolean)this.timerfill.getValue()).booleanValue()));
  
  private final Setting<Boolean> timerfill = register(new Setting("TimerJump", Boolean.valueOf(true), v -> !((Boolean)this.packetJump.getValue()).booleanValue()));
  
  private final Setting<Boolean> autoCenter = register(new Setting("Center", Boolean.valueOf(false)));
  
  private final Setting<Boolean> rotate = register(new Setting("Rotate", Boolean.FALSE));
  
  private final Setting<Boolean> sneaking = register(new Setting("SneakPacket", Boolean.valueOf(false)));
  
  private final Setting<Boolean> offground = register(new Setting("Offground", Boolean.valueOf(false)));
  
  public BlockPos startPos = null;
  
  private BlockPos playerPos;
  
  private int hudAmount = 0;
  
  private int blockSlot;
  
  public InstantSelfFill() {
    super("TurkFill", "b", Module.Category.COMBAT, true, false, false);
  }
  
  public void onEnable() {
    if (((Boolean)this.timerfill.getValue()).booleanValue()) {
      setTimer(50.0F);
      Phobos.moduleManager.getModuleByName("ReverseStep").isEnabled();
      ReverseStep.getInstance().disable();
      this.playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
      if (mc.world.getBlockState(this.playerPos).getBlock().equals(Blocks.OBSIDIAN)) {
        disable();
        return;
      } 
      mc.player.jump();
    } 
    if (((Boolean)this.packetJump.getValue()).booleanValue()) {
      if (fullNullCheck()) {
        disable();
        return;
      }
      Phobos.moduleManager.getModuleByName("ReverseStep").isEnabled();
      ReverseStep.getInstance().disable();
      if (this.page.getValue() == Page.EChest || this.page.getValue() == Page.Obsdidian)
        this.startPos = new BlockPos(mc.player.getPositionVector()); 
    } 
  }
  
  @Subscribe
  public void onUpdate() {
    int amount = 0;
    if (((Boolean)this.timerfill.getValue()).booleanValue()) {
      if (nullCheck())
        return; 
      if (mc.player.posY > this.playerPos.getY() + 1.04D) {
        WorldUtil.placeBlock(this.playerPos, InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN));
        mc.player.jump();
        disable();
      } 
    } 
    if (((Boolean)this.autoCenter.getValue()).booleanValue())
      Phobos.positionManager.setPositionPacket(this.startPos.getX() + 0.5D, this.startPos.getY(), this.startPos.getZ() + 0.5D, true, true, true);
    if (((Boolean)this.packetJump.getValue()).booleanValue()) {
      if (fullNullCheck())
        return; 
      int startSlot = mc.player.inventory.currentItem;
      if (this.page.getValue() == Page.EChest) {
        int enderSlot = ItemUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
        ItemUtil.switchToHotbarSlot(enderSlot, false);
        if (enderSlot == -1) {
          Command.sendMessage("<" + getDisplayName() + "> out of echests.");
          disable();
          return;
        } 
      } 
      if (this.page.getValue() == Page.Obsdidian) {
        int obbySlot = ItemUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN));
        ItemUtil.switchToHotbarSlot(obbySlot, false);
        if (obbySlot == -1) {
          Command.sendMessage("<" + getDisplayName() + "> out of obsidian.");
          disable();
          return;
        } 
      } 
      if (this.page.getValue() == Page.EChest || this.page.getValue() == Page.Obsdidian) {
        mc.getConnection().sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.4199999D + ((Float)this.extraboost.getValue()).floatValue(), mc.player.posZ, !((Boolean)this.offground.getValue()).booleanValue()));
        mc.getConnection().sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999D + ((Float)this.extraboost.getValue()).floatValue(), mc.player.posZ, !((Boolean)this.offground.getValue()).booleanValue()));
        mc.getConnection().sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.0013359D + ((Float)this.extraboost.getValue()).floatValue(), mc.player.posZ, !((Boolean)this.offground.getValue()).booleanValue()));
        mc.getConnection().sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.1661092D + ((Float)this.extraboost.getValue()).floatValue(), mc.player.posZ, !((Boolean)this.offground.getValue()).booleanValue()));
        BlockUtil.placeBlock(this.startPos, EnumHand.MAIN_HAND, ((Boolean)this.rotate.getValue()).booleanValue(), true, false);
        mc.getConnection().sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + ((Float)this.height.getValue()).floatValue(), mc.player.posZ, !((Boolean)this.offground.getValue()).booleanValue()));
        if (((Boolean)this.sneaking.getValue()).booleanValue()) {
          mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING));
          mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        } 
        if (startSlot != -1)
          ItemUtil.switchToHotbarSlot(startSlot, false); 
        disable();
      } 
    } 
    if (!this.settings.isEmpty())
      for (Setting setting : this.settings) {
        if (!(setting.getValue() instanceof Boolean) || !((Boolean)setting.getValue()).booleanValue() || setting.getName().equalsIgnoreCase("Enabled") || setting.getName().equalsIgnoreCase("drawn"))
          continue; 
        amount++;
      }  
    this.hudAmount = amount;
  }
  
  public void onDisable() {
    Phobos.moduleManager.getModuleByName("ReverseStep").isDisabled();
    ReverseStep.getInstance().enable();
    if (((Boolean)this.timerfill.getValue()).booleanValue())
      setTimer(1.0F); 
  }
  
  private void setTimer(float value) {
    try {
      Field timer = Minecraft.class.getDeclaredField(MappingUtil.timer);
      timer.setAccessible(true);
      Field tickLength = Timer.class.getDeclaredField(MappingUtil.tickLength);
      tickLength.setAccessible(true);
      tickLength.setFloat(timer.get(mc), 50.0F / value);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public String getDisplayInfo() {
    if (this.hudAmount == 0)
      return ""; 
    return this.hudAmount + "";
  }
  
  public enum Page {
    EChest, Obsdidian, Soulsand;
  }
}
