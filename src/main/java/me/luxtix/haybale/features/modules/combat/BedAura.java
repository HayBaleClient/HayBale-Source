package me.luxtix.haybale.features.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import me.luxtix.haybale.Phobos;
import me.luxtix.haybale.event.events.PacketEvent;
import me.luxtix.haybale.event.events.UpdateWalkingPlayerEvent;
import me.luxtix.haybale.features.modules.Module;
import me.luxtix.haybale.features.modules.client.ClickGui;
import me.luxtix.haybale.features.modules.client.ServerModule;
import me.luxtix.haybale.features.setting.Setting;
import me.luxtix.haybale.util.BlockUtil;
import me.luxtix.haybale.util.DamageUtil;
import me.luxtix.haybale.util.EntityUtil;
import me.luxtix.haybale.util.InventoryUtil;
import me.luxtix.haybale.util.MathUtil;
import me.luxtix.haybale.util.RotationUtil;
import me.luxtix.haybale.util.Timer;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BedAura extends Module {
    private final Setting<Boolean> server = register(new Setting("Server", Boolean.valueOf(false)));

    private final Setting<Boolean> place = register(new Setting("Place", Boolean.valueOf(false)));

    private final Setting<Integer> placeDelay = register(new Setting("Placedelay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500), v -> ((Boolean)this.place.getValue()).booleanValue()));

    private final Setting<Float> placeRange = register(new Setting("PlaceRange", Float.valueOf(6.0F), Float.valueOf(1.0F), Float.valueOf(10.0F), v -> ((Boolean)this.place.getValue()).booleanValue()));

    private final Setting<Boolean> extraPacket = register(new Setting("InsanePacket", Boolean.valueOf(false), v -> ((Boolean)this.place.getValue()).booleanValue()));

    private final Setting<Boolean> packet = register(new Setting("Packet", Boolean.valueOf(false), v -> ((Boolean)this.place.getValue()).booleanValue()));

    private final Setting<Boolean> explode = register(new Setting("Break", Boolean.valueOf(true)));

    private final Setting<BreakLogic> breakMode = register(new Setting("BreakMode", BreakLogic.ALL, v -> ((Boolean)this.explode.getValue()).booleanValue()));

    private final Setting<Integer> breakDelay = register(new Setting("Breakdelay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500), v -> ((Boolean)this.explode.getValue()).booleanValue()));

    private final Setting<Float> breakRange = register(new Setting("BreakRange", Float.valueOf(6.0F), Float.valueOf(1.0F), Float.valueOf(10.0F), v -> ((Boolean)this.explode.getValue()).booleanValue()));

    private final Setting<Float> minDamage = register(new Setting("MinDamage", Float.valueOf(5.0F), Float.valueOf(1.0F), Float.valueOf(36.0F), v -> ((Boolean)this.explode.getValue()).booleanValue()));

    private final Setting<Float> range = register(new Setting("Range", Float.valueOf(10.0F), Float.valueOf(1.0F), Float.valueOf(12.0F), v -> ((Boolean)this.explode.getValue()).booleanValue()));

    private final Setting<Boolean> suicide = register(new Setting("Suicide", Boolean.valueOf(false), v -> ((Boolean)this.explode.getValue()).booleanValue()));

    private final Setting<Boolean> removeTiles = register(new Setting("RemoveTiles", Boolean.valueOf(false)));

    private final Setting<Boolean> rotate = register(new Setting("Rotate", Boolean.valueOf(false)));

    private final Setting<Boolean> oneDot15 = register(new Setting("1.15", Boolean.valueOf(false)));

    private final Setting<Logic> logic = register(new Setting("Logic", Logic.BREAKPLACE, v -> (((Boolean)this.place.getValue()).booleanValue() && ((Boolean)this.explode.getValue()).booleanValue())));

    private final Setting<Boolean> craft = register(new Setting("Craft", Boolean.valueOf(false)));

    private final Setting<Boolean> placeCraftingTable = register(new Setting("PlaceTable", Boolean.valueOf(false), v -> ((Boolean)this.craft.getValue()).booleanValue()));

    private final Setting<Boolean> openCraftingTable = register(new Setting("OpenTable", Boolean.valueOf(false), v -> ((Boolean)this.craft.getValue()).booleanValue()));

    private final Setting<Boolean> craftTable = register(new Setting("CraftTable", Boolean.valueOf(false), v -> ((Boolean)this.craft.getValue()).booleanValue()));

    private final Setting<Float> tableRange = register(new Setting("TableRange", Float.valueOf(6.0F), Float.valueOf(1.0F), Float.valueOf(10.0F), v -> ((Boolean)this.craft.getValue()).booleanValue()));

    private final Setting<Integer> craftDelay = register(new Setting("CraftDelay", Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(10), v -> ((Boolean)this.craft.getValue()).booleanValue()));

    private final Setting<Integer> tableSlot = register(new Setting("TableSlot", Integer.valueOf(8), Integer.valueOf(0), Integer.valueOf(8), v -> ((Boolean)this.craft.getValue()).booleanValue()));

    private final Setting<Boolean> sslot = register(new Setting("S-Slot", Boolean.valueOf(false)));

    private final Timer breakTimer = new Timer();

    private final Timer placeTimer = new Timer();

    private final Timer craftTimer = new Timer();

    private EntityPlayer target = null;

    private boolean sendRotationPacket = false;

    private final AtomicDouble yaw = new AtomicDouble(-1.0D);

    private final AtomicDouble pitch = new AtomicDouble(-1.0D);

    private final AtomicBoolean shouldRotate = new AtomicBoolean(false);

    private boolean one;

    private boolean two;

    private boolean three;

    private boolean four;

    private boolean five;

    private boolean six;

    private boolean seven;

    private boolean eight;

    private boolean nine;

    private boolean ten;

    private BlockPos maxPos = null;

    private boolean shouldCraft;

    private int craftStage = 0;

    private int lastCraftStage = -1;

    private int lastHotbarSlot = -1;

    private int bedSlot = -1;

    private BlockPos finalPos;

    private EnumFacing finalFacing;

    public BedAura() {
        super("BedAura", "AutoPlace and Break for beds", Module.Category.COMBAT, true, false, false);
    }

    public void onEnable() {
        if (!fullNullCheck() && shouldServer()) {
            mc.player.connection.sendPacket((Packet)new CPacketChatMessage("@Serverprefix" + (String)(ClickGui.getInstance()).prefix.getValue()));
            mc.player.connection.sendPacket((Packet)new CPacketChatMessage("@Server" + (String)(ClickGui.getInstance()).prefix.getValue() + "module BedBomb set Enabled true"));
        }
    }

    public void onDisable() {
        if (!fullNullCheck() && shouldServer()) {
            mc.player.connection.sendPacket((Packet)new CPacketChatMessage("@Serverprefix" + (String)(ClickGui.getInstance()).prefix.getValue()));
            mc.player.connection.sendPacket((Packet)new CPacketChatMessage("@Server" + (String)(ClickGui.getInstance()).prefix.getValue() + "module BedBomb set Enabled false"));
            if (((Boolean)this.sslot.getValue()).booleanValue())
                mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event) {
        if (this.shouldRotate.get() && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            packet.yaw = (float)this.yaw.get();
            packet.pitch = (float)this.pitch.get();
            this.shouldRotate.set(false);
        }
    }

    private boolean shouldServer() {
        return (ServerModule.getInstance().isConnected() && ((Boolean)this.server.getValue()).booleanValue());
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (fullNullCheck() || (mc.player.dimension != -1 && mc.player.dimension != 1) || shouldServer())
            return;
        if (event.getStage() == 0) {
            doBedBomb();
            if (this.shouldCraft && mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiCrafting) {
                int woolSlot = InventoryUtil.findInventoryWool(false);
                int woodSlot = InventoryUtil.findInventoryBlock(BlockPlanks.class, true);
                if (woolSlot == -1 || woodSlot == -1) {
                    mc.displayGuiScreen(null);
                    mc.currentScreen = null;
                    this.shouldCraft = false;
                    return;
                }
                if (this.craftStage > 1 && !this.one) {
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 1, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    this.one = true;
                } else if (this.craftStage > 1 + ((Integer)this.craftDelay.getValue()).intValue() && !this.two) {
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 2, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    this.two = true;
                } else if (this.craftStage > 1 + ((Integer)this.craftDelay.getValue()).intValue() * 2 && !this.three) {
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 3, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    this.three = true;
                } else if (this.craftStage > 1 + ((Integer)this.craftDelay.getValue()).intValue() * 3 && !this.four) {
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 4, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    this.four = true;
                } else if (this.craftStage > 1 + ((Integer)this.craftDelay.getValue()).intValue() * 4 && !this.five) {
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 5, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    this.five = true;
                } else if (this.craftStage > 1 + ((Integer)this.craftDelay.getValue()).intValue() * 5 && !this.six) {
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 6, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    recheckBedSlots(woolSlot, woodSlot);
                    mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, 0, 0, ClickType.QUICK_MOVE, (EntityPlayer)mc.player);
                    this.six = true;
                    this.one = false;
                    this.two = false;
                    this.three = false;
                    this.four = false;
                    this.five = false;
                    this.six = false;
                    this.craftStage = -2;
                    this.shouldCraft = false;
                }
                this.craftStage++;
            }
        } else if (event.getStage() == 1 && this.finalPos != null) {
            Vec3d hitVec = (new Vec3d((Vec3i)this.finalPos.down())).add(0.5D, 0.5D, 0.5D).add((new Vec3d(this.finalFacing.getOpposite().getDirectionVec())).scale(0.5D));
            mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING));
            InventoryUtil.switchToHotbarSlot(this.bedSlot, false);
            BlockUtil.rightClickBlock(this.finalPos.down(), hitVec, (this.bedSlot == -2) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, EnumFacing.UP, ((Boolean)this.packet.getValue()).booleanValue());
            mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            this.placeTimer.reset();
            this.finalPos = null;
        }
    }

    public void recheckBedSlots(int woolSlot, int woodSlot) {
        int i;
        for (i = 1; i <= 3; i++) {
            if (mc.player.openContainer.getInventory().get(i) == ItemStack.EMPTY) {
                mc.playerController.windowClick(1, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                mc.playerController.windowClick(1, i, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                mc.playerController.windowClick(1, woolSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
            }
        }
        for (i = 4; i <= 6; i++) {
            if (mc.player.openContainer.getInventory().get(i) == ItemStack.EMPTY) {
                mc.playerController.windowClick(1, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                mc.playerController.windowClick(1, i, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
                mc.playerController.windowClick(1, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
            }
        }
    }

    public void incrementCraftStage() {
        if (this.craftTimer.passedMs(((Integer)this.craftDelay.getValue()).intValue())) {
            this.craftStage++;
            if (this.craftStage > 9)
                this.craftStage = 0;
            this.craftTimer.reset();
        }
    }

    private void doBedBomb() {
        switch ((Logic)this.logic.getValue()) {
            case BREAKPLACE:
                mapBeds();
                breakBeds();
                placeBeds();
                break;
            case PLACEBREAK:
                mapBeds();
                placeBeds();
                breakBeds();
                break;
        }
    }

    private void breakBeds() {
        if (((Boolean)this.explode.getValue()).booleanValue() && this.breakTimer.passedMs(((Integer)this.breakDelay.getValue()).intValue()))
            if (this.breakMode.getValue() == BreakLogic.CALC) {
                if (this.maxPos != null) {
                    Vec3d hitVec = (new Vec3d((Vec3i)this.maxPos)).add(0.5D, 0.5D, 0.5D);
                    float[] rotations = RotationUtil.getLegitRotations(hitVec);
                    this.yaw.set(rotations[0]);
                    if (((Boolean)this.rotate.getValue()).booleanValue()) {
                        this.shouldRotate.set(true);
                        this.pitch.set(rotations[1]);
                    }
                    RayTraceResult result;
                    EnumFacing facing = ((result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(this.maxPos.getX() + 0.5D, this.maxPos.getY() - 0.5D, this.maxPos.getZ() + 0.5D))) == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
                    BlockUtil.rightClickBlock(this.maxPos, hitVec, EnumHand.MAIN_HAND, facing, true);
                    this.breakTimer.reset();
                }
            } else {
                for (TileEntity entityBed : mc.world.loadedTileEntityList) {
                    if (!(entityBed instanceof TileEntityBed) || mc.player.getDistanceSq(entityBed.getPos()) > MathUtil.square(((Float)this.breakRange.getValue()).floatValue()))
                        continue;
                    Vec3d hitVec = (new Vec3d((Vec3i)entityBed.getPos())).add(0.5D, 0.5D, 0.5D);
                    float[] rotations = RotationUtil.getLegitRotations(hitVec);
                    this.yaw.set(rotations[0]);
                    if (((Boolean)this.rotate.getValue()).booleanValue()) {
                        this.shouldRotate.set(true);
                        this.pitch.set(rotations[1]);
                    }
                    RayTraceResult result;
                    EnumFacing facing = ((result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityBed.getPos().getX() + 0.5D, entityBed.getPos().getY() - 0.5D, entityBed.getPos().getZ() + 0.5D))) == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
                    BlockUtil.rightClickBlock(entityBed.getPos(), hitVec, EnumHand.MAIN_HAND, facing, true);
                    this.breakTimer.reset();
                }
            }
    }

    private void mapBeds() {
        this.maxPos = null;
        float maxDamage = 0.5F;
        if (((Boolean)this.removeTiles.getValue()).booleanValue()) {
            ArrayList<BedData> removedBlocks = new ArrayList<>();
            for (TileEntity tile : mc.world.loadedTileEntityList) {
                if (!(tile instanceof TileEntityBed))
                    continue;
                TileEntityBed bed = (TileEntityBed)tile;
                BedData data = new BedData(tile.getPos(), mc.world.getBlockState(tile.getPos()), bed, bed.isHeadPiece());
                removedBlocks.add(data);
            }
            for (BedData data : removedBlocks)
                mc.world.setBlockToAir(data.getPos());
            for (BedData data : removedBlocks) {
                float selfDamage;
                BlockPos pos;
                if (!data.isHeadPiece() || mc.player.getDistanceSq(pos = data.getPos()) > MathUtil.square(((Float)this.breakRange.getValue()).floatValue()) || ((selfDamage = DamageUtil.calculateDamage(pos, (Entity)mc.player)) + 1.0D >= EntityUtil.getHealth((Entity)mc.player) && DamageUtil.canTakeDamage(((Boolean)this.suicide.getValue()).booleanValue())))
                    continue;
                for (EntityPlayer player : mc.world.playerEntities) {
                    float damage;
                    if (player.getDistanceSq(pos) >= MathUtil.square(((Float)this.range.getValue()).floatValue()) || !EntityUtil.isValid((Entity)player, (((Float)this.range.getValue()).floatValue() + ((Float)this.breakRange.getValue()).floatValue())) || ((damage = DamageUtil.calculateDamage(pos, (Entity)player)) <= selfDamage && (damage <= ((Float)this.minDamage.getValue()).floatValue() || DamageUtil.canTakeDamage(((Boolean)this.suicide.getValue()).booleanValue())) && damage <= EntityUtil.getHealth((Entity)player)) || damage <= maxDamage)
                        continue;
                    maxDamage = damage;
                    this.maxPos = pos;
                }
            }
            for (BedData data : removedBlocks)
                mc.world.setBlockState(data.getPos(), data.getState());
        } else {
            for (TileEntity tile : mc.world.loadedTileEntityList) {
                float selfDamage;
                BlockPos pos;
                TileEntityBed bed;
                if (!(tile instanceof TileEntityBed) || !(bed = (TileEntityBed)tile).isHeadPiece() || mc.player.getDistanceSq(pos = bed.getPos()) > MathUtil.square(((Float)this.breakRange.getValue()).floatValue()) || ((selfDamage = DamageUtil.calculateDamage(pos, (Entity)mc.player)) + 1.0D >= EntityUtil.getHealth((Entity)mc.player) && DamageUtil.canTakeDamage(((Boolean)this.suicide.getValue()).booleanValue())))
                    continue;
                for (EntityPlayer player : mc.world.playerEntities) {
                    float damage;
                    if (player.getDistanceSq(pos) >= MathUtil.square(((Float)this.range.getValue()).floatValue()) || !EntityUtil.isValid((Entity)player, (((Float)this.range.getValue()).floatValue() + ((Float)this.breakRange.getValue()).floatValue())) || ((damage = DamageUtil.calculateDamage(pos, (Entity)player)) <= selfDamage && (damage <= ((Float)this.minDamage.getValue()).floatValue() || DamageUtil.canTakeDamage(((Boolean)this.suicide.getValue()).booleanValue())) && damage <= EntityUtil.getHealth((Entity)player)) || damage <= maxDamage)
                        continue;
                    maxDamage = damage;
                    this.maxPos = pos;
                }
            }
        }
    }

    private void placeBeds() {
        if (((Boolean)this.place.getValue()).booleanValue() && this.placeTimer.passedMs(((Integer)this.placeDelay.getValue()).intValue()) && this.maxPos == null) {
            this.bedSlot = findBedSlot();
            if (this.bedSlot == -1)
                if (mc.player.getHeldItemOffhand().getItem() == Items.BED) {
                    this.bedSlot = -2;
                } else {
                    if (((Boolean)this.craft.getValue()).booleanValue() && !this.shouldCraft && EntityUtil.getClosestEnemy(((Float)this.placeRange.getValue()).floatValue()) != null)
                        doBedCraft();
                    return;
                }
            this.lastHotbarSlot = mc.player.inventory.currentItem;
            this.target = EntityUtil.getClosestEnemy(((Float)this.placeRange.getValue()).floatValue());
            if (this.target != null) {
                BlockPos targetPos = new BlockPos(this.target.getPositionVector());
                placeBed(targetPos, true);
                if (((Boolean)this.craft.getValue()).booleanValue())
                    doBedCraft();
            }
        }
    }

    private void placeBed(BlockPos pos, boolean firstCheck) {
        if (mc.world.getBlockState(pos).getBlock() == Blocks.BED)
            return;
        float damage = DamageUtil.calculateDamage(pos, (Entity)mc.player);
        if (damage > EntityUtil.getHealth((Entity)mc.player) + 0.5D) {
            if (firstCheck && ((Boolean)this.oneDot15.getValue()).booleanValue())
                placeBed(pos.up(), false);
            return;
        }
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            if (firstCheck && ((Boolean)this.oneDot15.getValue()).booleanValue())
                placeBed(pos.up(), false);
            return;
        }
        ArrayList<BlockPos> positions = new ArrayList<>();
        HashMap<BlockPos, EnumFacing> facings = new HashMap<>();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos position;
            if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && mc.player.getDistanceSq(position = pos.offset(facing)) <= MathUtil.square(((Float)this.placeRange.getValue()).floatValue()) && mc.world.getBlockState(position).getMaterial().isReplaceable() && !mc.world.getBlockState(position.down()).getMaterial().isReplaceable()) {
                positions.add(position);
                facings.put(position, facing.getOpposite());
            }
        }
        if (positions.isEmpty()) {
            if (firstCheck && ((Boolean)this.oneDot15.getValue()).booleanValue())
                placeBed(pos.up(), false);
            return;
        }
        positions.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
        this.finalPos = positions.get(0);
        this.finalFacing = facings.get(this.finalPos);
        float[] rotation = RotationUtil.simpleFacing(this.finalFacing);
        if (!this.sendRotationPacket && ((Boolean)this.extraPacket.getValue()).booleanValue()) {
            RotationUtil.faceYawAndPitch(rotation[0], rotation[1]);
            this.sendRotationPacket = true;
        }
        this.yaw.set(rotation[0]);
        this.pitch.set(rotation[1]);
        this.shouldRotate.set(true);
        Phobos.rotationManager.setPlayerRotations(rotation[0], rotation[1]);
    }

    public String getDisplayInfo() {
        if (this.target != null)
            return this.target.getName();
        return null;
    }

    public void doBedCraft() {
        int woolSlot = InventoryUtil.findInventoryWool(false);
        int woodSlot = InventoryUtil.findInventoryBlock(BlockPlanks.class, true);
        if (woolSlot == -1 || woodSlot == -1) {
            if (mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiCrafting) {
                mc.displayGuiScreen(null);
                mc.currentScreen = null;
            }
            return;
        }
        List<?> targets;
        if (((Boolean)this.placeCraftingTable.getValue()).booleanValue() && BlockUtil.getBlockSphere(((Float)this.tableRange.getValue()).floatValue() - 1.0F, BlockWorkbench.class).size() == 0 && !(targets = (List)BlockUtil.getSphere(EntityUtil.getPlayerPos((EntityPlayer)mc.player), ((Float)this.tableRange.getValue()).floatValue(), ((Float)this.tableRange.getValue()).intValue(), false, true, 0).stream().filter(pos -> (BlockUtil.isPositionPlaceable(pos, false) == 3)).sorted(Comparator.comparingInt(pos -> -safety(pos))).collect(Collectors.toList())).isEmpty()) {
            BlockPos target = (BlockPos)targets.get(0);
            int tableSlot = InventoryUtil.findHotbarBlock(BlockWorkbench.class);
            if (tableSlot != -1) {
                mc.player.inventory.currentItem = tableSlot;
                BlockUtil.placeBlock(target, EnumHand.MAIN_HAND, ((Boolean)this.rotate.getValue()).booleanValue(), true, false);
            } else {
                if (((Boolean)this.craftTable.getValue()).booleanValue())
                    craftTable();
                if ((tableSlot = InventoryUtil.findHotbarBlock(BlockWorkbench.class)) != -1) {
                    mc.player.inventory.currentItem = tableSlot;
                    BlockUtil.placeBlock(target, EnumHand.MAIN_HAND, ((Boolean)this.rotate.getValue()).booleanValue(), true, false);
                }
            }
        }
        if (((Boolean)this.openCraftingTable.getValue()).booleanValue()) {
            List<BlockPos> tables = BlockUtil.getBlockSphere(((Float)this.tableRange.getValue()).floatValue(), BlockWorkbench.class);
            tables.sort(Comparator.comparingDouble(pos -> mc.player.getDistanceSq(pos)));
            if (!tables.isEmpty() && !(mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiCrafting)) {
                BlockPos target = tables.get(0);
                mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                if (mc.player.getDistanceSq(target) > MathUtil.square(((Float)this.breakRange.getValue()).floatValue()))
                    return;
                Vec3d hitVec = new Vec3d((Vec3i)target);
                float[] rotations = RotationUtil.getLegitRotations(hitVec);
                this.yaw.set(rotations[0]);
                if (((Boolean)this.rotate.getValue()).booleanValue()) {
                    this.shouldRotate.set(true);
                    this.pitch.set(rotations[1]);
                }
                RayTraceResult result;
                EnumFacing facing = ((result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(target.getX() + 0.5D, target.getY() - 0.5D, target.getZ() + 0.5D))) == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
                BlockUtil.rightClickBlock(target, hitVec, EnumHand.MAIN_HAND, facing, true);
                this.breakTimer.reset();
                if (mc.player.isSneaking())
                    mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            this.shouldCraft = mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiCrafting;
            this.craftStage = 0;
            this.craftTimer.reset();
        }
    }

    public void craftTable() {
        int woodSlot = InventoryUtil.findInventoryBlock(BlockPlanks.class, true);
        if (woodSlot != -1) {
            mc.playerController.windowClick(0, woodSlot, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
            mc.playerController.windowClick(0, 1, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
            mc.playerController.windowClick(0, 2, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
            mc.playerController.windowClick(0, 3, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
            mc.playerController.windowClick(0, 4, 1, ClickType.PICKUP, (EntityPlayer)mc.player);
            mc.playerController.windowClick(0, 0, 0, ClickType.QUICK_MOVE, (EntityPlayer)mc.player);
            int table = InventoryUtil.findInventoryBlock(BlockWorkbench.class, true);
            if (table != -1) {
                mc.playerController.windowClick(0, table, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                mc.playerController.windowClick(0, ((Integer)this.tableSlot.getValue()).intValue(), 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                mc.playerController.windowClick(0, table, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
            }
        }
    }

    public void onToggle() {
        this.lastHotbarSlot = -1;
        this.bedSlot = -1;
        this.sendRotationPacket = false;
        this.target = null;
        this.yaw.set(-1.0D);
        this.pitch.set(-1.0D);
        this.shouldRotate.set(false);
        this.shouldCraft = false;
    }

    private int findBedSlot() {
        for (int i = 0; i < 9; ) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || stack.getItem() != Items.BED) {
                i++;
                continue;
            }
            return i;
        }
        return -1;
    }

    private int safety(BlockPos pos) {
        int safety = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            if (!mc.world.getBlockState(pos.offset(facing)).getMaterial().isReplaceable())
                safety++;
        }
        return safety;
    }

    public enum BreakLogic {
        ALL, CALC;
    }

    public enum Logic {
        BREAKPLACE, PLACEBREAK;
    }

    public static class BedData {
        private final BlockPos pos;

        private final IBlockState state;

        private final boolean isHeadPiece;

        private final TileEntityBed entity;

        public BedData(BlockPos pos, IBlockState state, TileEntityBed bed, boolean isHeadPiece) {
            this.pos = pos;
            this.state = state;
            this.entity = bed;
            this.isHeadPiece = isHeadPiece;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public IBlockState getState() {
            return this.state;
        }

        public boolean isHeadPiece() {
            return this.isHeadPiece;
        }

        public TileEntityBed getEntity() {
            return this.entity;
        }
    }
}