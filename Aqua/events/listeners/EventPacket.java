package events.listeners;

import events.Event;
import events.listeners.EventPacket;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class EventPacket
extends Event {
    public static Packet packet;
    private boolean cancel;
    private Action eventAction;
    private INetHandler netHandler;
    private EnumPacketDirection packetDirection;

    public EventPacket(Action action, Packet packet, INetHandler netHandler, EnumPacketDirection direction) {
        this.eventAction = action;
        EventPacket.packet = packet;
        this.netHandler = netHandler;
        this.packetDirection = direction;
    }

    public INetHandler getNetHandler() {
        return this.netHandler;
    }

    public void setNetHandler(INetHandler netHandler) {
        this.netHandler = netHandler;
    }

    public EnumPacketDirection getDirection() {
        return this.packetDirection;
    }

    public static Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        EventPacket.packet = packet;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Action getEventAction() {
        return this.eventAction;
    }

    public boolean isSend() {
        return this.getEventAction() == Action.SEND;
    }

    public boolean isReceive() {
        return this.getEventAction() == Action.RECEIVE;
    }
}
