package me.luxtix.haybale.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.client.network.NetworkPlayerInfo;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import me.luxtix.haybale.features.command.Command;
import me.luxtix.haybale.util.PlayerUtil;
import me.luxtix.haybale.util.Util;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.IOUtils;

public class PlayerUtil2
implements Util {
    private static final JsonParser PARSER = new JsonParser();

    public static String getNameFromUUID(UUID uuid) {
        try {
            lookUpName process = new lookUpName(uuid);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(PlayerUtil2.mc.player.posX), Math.floor(PlayerUtil2.mc.player.posY), Math.floor(PlayerUtil2.mc.player.posZ));
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (PlayerUtil2.mc.player != null && PlayerUtil2.mc.player.isPotionActive(Potion.getPotionById(1))) {
            int amplifier = PlayerUtil2.mc.player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }

    public static boolean isMoving(EntityLivingBase entity) {
        return entity.moveForward != 0.0f || entity.moveStrafing != 0.0f;
    }

    public static void setSpeed(EntityLivingBase entity, double speed) {
        double[] dir = PlayerUtil2.forward(speed);
        entity.motionX = dir[0];
        entity.motionZ = dir[1];
    }

    public static double[] forward(double speed) {
        float forward = PlayerUtil2.mc.player.movementInput.moveForward;
        float side = PlayerUtil2.mc.player.movementInput.moveStrafe;
        float yaw = PlayerUtil2.mc.player.prevRotationYaw + (PlayerUtil2.mc.player.rotationYaw - PlayerUtil2.mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double)forward * speed * cos + (double)side * speed * sin;
        double posZ = (double)forward * speed * sin - (double)side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static String getNameFromUUID(String uuid) {
        try {
            lookUpName process = new lookUpName(uuid);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static UUID getUUIDFromName(String name) {
        try {
            lookUpUUID process = new lookUpUUID(name);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getUUID();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String requestIDs(String data) {
        try {
            String query = "https://api.mojang.com/profiles/minecraft";
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            String res = PlayerUtil2.convertStreamToString(in);
            ((InputStream)in).close();
            conn.disconnect();
            return res;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "/";
    }

    public static List<String> getHistoryOfNames(UUID id) {
        try {
            JsonArray array = PlayerUtil2.getResources(new URL("https://api.mojang.com/user/profiles/" + PlayerUtil2.getIdNoHyphens(id) + "/names"), "GET").getAsJsonArray();
            ArrayList<String> temp = Lists.newArrayList();
            Iterator iterator = array.iterator();
            while (iterator.hasNext()) {
                JsonElement e = (JsonElement)iterator.next();
                JsonObject node = e.getAsJsonObject();
                String name = node.get("name").getAsString();
                long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0L;
                temp.add(name + "Ã‚Â§8" + new Date(changedAt).toString());
            }
            Collections.sort(temp);
            return temp;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    public static int findObiInHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = PlayerUtil2.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) continue;
            Block block = ((ItemBlock)((Object)stack.getItem())).getBlock();
            if (block instanceof BlockEnderChest) {
                return i;
            }
            if (!(block instanceof BlockObsidian)) continue;
            return i;
        }
        return -1;
    }

    public static String getIdNoHyphens(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }

    private static JsonElement getResources(URL url, String request) throws Exception {
        return PlayerUtil2.getResources(url, request, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static JsonElement getResources(URL url, String request, JsonElement element) throws Exception {
        HttpsURLConnection connection = null;
        try {
            JsonElement data;
            connection = (HttpsURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(request);
            connection.setRequestProperty("Content-Type", "application/json");
            if (element != null) {
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(AdvancementManager.GSON.toJson(element));
                output.close();
            }
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }
            scanner.close();
            String json = builder.toString();
            JsonElement jsonElement = data = PARSER.parse(json);
            return jsonElement;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class lookUpName
    implements Runnable {
        private final String uuid;
        private final UUID uuidID;
        private volatile String name;

        public lookUpName(String input) {
            this.uuid = input;
            this.uuidID = UUID.fromString(input);
        }

        public lookUpName(UUID input) {
            this.uuidID = input;
            this.uuid = input.toString();
        }

        @Override
        public void run() {
            this.name = this.lookUpName();
        }

        public String lookUpName() {
            EntityPlayer player = null;
            if (Util.mc.world != null) {
                player = Util.mc.world.getPlayerEntityByUUID(this.uuidID);
            }
            if (player == null) {
                String url = "https://api.mojang.com/user/profiles/" + this.uuid.replace("-", "") + "/names";
                try {
                    String nameJson = IOUtils.toString(new URL(url));
                    if (nameJson.contains(",")) {
                        List<String> names = Arrays.asList(nameJson.split(","));
                        Collections.reverse(names);
                        return names.get(1).replace("{\"name\":\"", "").replace("\"", "");
                    }
                    return nameJson.replace("[{\"name\":\"", "").replace("\"}]", "");
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                    return null;
                }
            }
            return player.getName();
        }

        public String getName() {
            return this.name;
        }
    }

    public static class lookUpUUID
    implements Runnable {
        private final String name;
        private volatile UUID uuid;

        public lookUpUUID(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            NetworkPlayerInfo profile;
            try {
                ArrayList infoMap = new ArrayList(Objects.requireNonNull(Util.mc.getConnection()).getPlayerInfoMap());
                profile = (NetworkPlayerInfo) infoMap.stream().filter(networkPlayerInfo -> new getGameProfile().getName().equalsIgnoreCase(this.name)).findFirst().orElse(null);
                assert (profile != null);
                this.uuid = profile.getGameProfile().getId();
            }
            catch (Exception e) {
                profile = null;
            }
            if (profile == null) {
                Command.sendMessage("Player isn't online. Looking up UUID..");
                String s = PlayerUtil.requestIDs("[\"" + this.name + "\"]");
                if (s == null || s.isEmpty()) {
                    Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");
                } else {
                    JsonElement element = new JsonParser().parse(s);
                    if (element.getAsJsonArray().size() == 0) {
                        Command.sendMessage("Couldn't find player ID. (1)");
                    } else {
                        try {
                            String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                            this.uuid = UUIDTypeAdapter.fromString((String)id);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Command.sendMessage("Couldn't find player ID. (2)");
                        }
                    }
                }
            }
        }

        public UUID getUUID() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }

        private class getGameProfile {
            public String getName() {
                return null;
            }
        }
    }
}
