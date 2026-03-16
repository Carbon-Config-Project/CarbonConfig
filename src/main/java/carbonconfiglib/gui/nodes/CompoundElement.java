package carbonconfiglib.gui.nodes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.CompoundArrayNamer;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.gui.nodes.base.NodeElement;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

/**
 * Copyright 2026 Speiger, Meduris
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
public class CompoundElement extends NodeElement implements IFolderNode
{
	ICompoundNode node;
	CarbonButton button;
	IFolderController listener;
	
	public CompoundElement(ICompoundNode node) {
		super(node);
		this.node = node;
		button = addChild(new CarbonButton(0, 0, 0, 0, Component.literal("▶"), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	public void setEditable(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	
	@Override
	public void setCallbacks(IFolderController listener) {
		this.listener = listener;
	}
	
	@Override
	public Component getName() {
		if(shouldRenderIndex()) {
			Component comp = getOverride();
			if(comp != null) return comp;
		}
		return super.getName();
	}
	
	@Override
	public String getNodeName() {
		return node.getNodeName();
	}

	@Override
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		Component comp = shouldRenderIndex() ? getOverride() : null;
		GuiUtils.drawScrollingShadowText(stack, font, comp != null ? comp : node.getName(), left, top, width-23, height, Align.START, -1, sinceFullyVisible);
		boolean active = context.isElementActive(this);
		button.setMessage(Component.literal(active ? "◀" : "▶"));
		button.setSelected(active);
		button.x = left + width - 22;
		button.y = top;
		button.setWidth(20);
		button.setHeight(height);
		button.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@SuppressWarnings("unchecked")
	private <T> Component getOverride() {
		CompoundArrayNamer<T> result = node.getSetting(CompoundArrayNamer.class);
		if(result == null) return null;
		IConfigSerializer<T> serial = result.serializer().get();
		if(serial == null) return null;
		ParseResult<T> data = serial.deserialize(serial.getFormat().parse(node.get())); 
		if(!data.isValid()) return null;
		return result.provider().apply(data.getValue());
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, Component.translatable("gui.carbonconfig.elements", node.getValues().size()), left+(desiredWidth>>1), top, (desiredWidth>>1)-2, height, Align.END, -1, sinceFullyVisible);
	}
	
	protected void onClick(Button button) {
		if(listener == null) return;
		listener.pushNode(this, layer, false);
	}
	
	@Override
	public List<BaseElement> getChildNodes() {
		List<BaseElement> result = new ObjectArrayList<>();
		for(INode entry : node.getValues()) {
			BaseElement element = createNode(entry);
			if(element == null) continue;
			result.add(element);
		}
		return result;
	}
}
