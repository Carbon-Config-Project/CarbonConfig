package carbonconfiglib.gui.api;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public interface IRequestReceiver
{
	public void receiveConfigData(UUID requestId, FriendlyByteBuf buf);
	
	public static class Impl {
		
		private static final List<IRequestReceiver> RECEIVERS = new ObjectArrayList<>();
		
		public static void register(IRequestReceiver receiver) {
			RECEIVERS.add(receiver);
		}
		
		public static void unregister(IRequestReceiver receiver) {
			RECEIVERS.remove(receiver);
		}
		
		public static void receiveData(UUID requestId, FriendlyByteBuf buf) {
			RECEIVERS.forEach(T -> T.receiveConfigData(requestId, buf));
		}
	}
}
