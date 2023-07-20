package carbonconfiglib.networking.buffer;

import java.util.UUID;

import carbonconfiglib.api.buffer.IReadBuffer;
import net.minecraft.network.FriendlyByteBuf;

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
	FriendlyByteBuf buf;
	
	public ReadBuffer(FriendlyByteBuf buf) {
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
		return buf.readVarInt();
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
		return buf.readEnum(clz);
	}
	
	@Override
	public byte[] readBytes() {
		return buf.readByteArray();
	}
	
	@Override
	public String readString() {
		return buf.readUtf(32767);
	}
	
	@Override
	public UUID readUUID() {
		return buf.readUUID();
	}
}
