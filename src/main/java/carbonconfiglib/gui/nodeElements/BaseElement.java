package carbonconfiglib.gui.nodeElements;

import java.util.function.ObjIntConsumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import net.minecraft.network.chat.Component;

public class BaseElement extends ListEntry<BaseElement>
{
	protected ObjIntConsumer<Object> selector;
	protected int index = -1;
	
	public BaseElement() {
	}

	@Override
	protected boolean containsSearch(String searchString) { return false; }

	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		font.draw(poseStack, Component.literal("top: "+top), left, top, -1);
	}
	
	
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if(index != -1 && selector != null) {
			selector.accept(this, index);
			return true;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
}
