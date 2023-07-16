package net.minecraft.network.handshake.client;

import java.io.IOException;

import net.minecraft.network.AbstractPacket;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;

public class C00Handshake extends AbstractPacket<INetHandlerHandshakeServer> {
	private int protocolVersion;
	public String ip;
	private int port;
	private EnumConnectionState requestedState;

	public C00Handshake() {
	}

	public C00Handshake(int version, String ip, int port, EnumConnectionState requestedState) {
		this.protocolVersion = version;
		this.ip = ip;
		this.port = port;
		this.requestedState = requestedState;
	}

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) throws IOException {
		this.protocolVersion = buf.readVarIntFromBuffer();
		this.ip = buf.readStringFromBuffer(255);
		this.port = buf.readUnsignedShort();
		this.requestedState = EnumConnectionState.getById(buf.readVarIntFromBuffer());
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeVarIntToBuffer(this.protocolVersion);
		buf.writeString(this.ip);
		buf.writeShort(this.port);
		buf.writeVarIntToBuffer(this.requestedState.getId());
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(INetHandlerHandshakeServer handler) {
		handler.processHandshake(this);
	}

	public EnumConnectionState getRequestedState() {
		return this.requestedState;
	}

	public int getProtocolVersion() {
		return this.protocolVersion;
	}

	 public String getIp()
  {
    return this.ip;
  }

	 public void setIp(String ip)
	  {
	    this.ip = ip;
	  }
}
