package dev.utils.network;

import dev.utils.Utils;
import net.minecraft.network.Packet;

public class PacketUtils implements Utils {

    public static void sendPacket(Packet<?> packet, boolean silent) {
        if (mc.player != null) {
            mc.getConnection().getNetworkManager().sendPacket(packet);
        }
    }

    public static void sendPacketNoEvent(Packet packet) {
        sendPacket(packet, true);
    }

    public static void sendPacket(Packet packet) {
        sendPacket(packet, false);
    }

}
