package carbonconfiglib.networking.buffer;

import java.io.IOException;
import java.util.UUID;

import carbonconfiglib.api.buffer.IReadBuffer;
import net.minecraft.network.PacketBuffer;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ReadBuffer implements IReadBuffer
{
	PacketBuffer buf;
	
	public ReadBuffer(PacketBuffer buf) {
		this.buf = buf;
	}

	@Override
	public boolean readBoolean() {
		return buf.readBoolean();
	}
	
	@Override
	public byte readByte() {
		return buf.readByte();
	}
	
	@Override
	public short readShort() {
		return buf.readShort();
	}
	
	@Override
	public int readMedium() {
		return buf.readMedium();
	}
	
	@Override
	public int readInt() {
		return buf.readInt();
	}
	
	@Override
	public int readVarInt() {
		return buf.readVarIntFromBuffer();
	}
	
	@Override
	public float readFloat() {
		return buf.readFloat();
	}
	
	@Override
	public double readDouble() {
		return buf.readDouble();
	}
	
	@Override
	public long readLong() {
		return buf.readLong();
	}
	
	@Override
	public char readChar() {
		return buf.readChar();
	}
	
	@Override
	public <T extends Enum<T>> T readEnum(Class<T> clz) {
		return clz.getEnumConstants()[buf.readVarIntFromBuffer()];
	}
	
	@Override
	public byte[] readBytes() {
		byte[] data = new byte[buf.readVarIntFromBuffer()];
		buf.readBytes(data);
        return data;
	}
	
	@Override
	public String readString() {
		try { return buf.readStringFromBuffer(32767); }
		catch(IOException e) { e.printStackTrace(); }
		throw new RuntimeException();
	}
	
	@Override
	public UUID readUUID() {
		return new UUID(buf.readLong(), buf.readLong());
	}
}
