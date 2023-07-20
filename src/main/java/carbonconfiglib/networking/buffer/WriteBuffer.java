package carbonconfiglib.networking.buffer;

import java.util.UUID;

import carbonconfiglib.api.buffer.IWriteBuffer;
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
public class WriteBuffer implements IWriteBuffer
{
	FriendlyByteBuf buf;
	
	public WriteBuffer(FriendlyByteBuf buf) {
		this.buf = buf;
	}

	@Override
	public void writeBoolean(boolean value) {
		buf.writeBoolean(value);
	}
	
	@Override
	public void writeByte(byte value) {
		buf.writeByte(value);
	}
	
	@Override
	public void writeShort(short value) {
		buf.writeShort(value);
	}
	
	@Override
	public void writeMedium(int value) {
		buf.writeMedium(value);
	}
	
	@Override
	public void writeInt(int value) {
		buf.writeInt(value);
	}
	
	@Override
	public void writeVarInt(int value) {
		buf.writeVarInt(value);
	}
	
	@Override
	public void writeFloat(float value) {
		buf.writeFloat(value);
	}
	
	@Override
	public void writeDouble(double value) {
		buf.writeDouble(value);
	}
	
	@Override
	public void writeLong(long value) {
		buf.writeLong(value);
	}
	
	@Override
	public void writeChar(char value) {
		buf.writeChar(value);
	}
	
	@Override
	public void writeEnum(Enum<?> value) {
		buf.writeEnum(value);
	}
	
	@Override
	public void writeString(String value) {
		buf.writeUtf(value, 32767);
	}
	
	@Override
	public void writeBytes(byte[] value) {
		buf.writeByteArray(value);
	}
	
	@Override
	public void writeUUID(UUID value) {
		buf.writeUUID(value);
	}
	
}
