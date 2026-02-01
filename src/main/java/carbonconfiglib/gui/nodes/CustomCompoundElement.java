package carbonconfiglib.gui.nodes;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CustomCompoundElement extends NodeElement
{
	ICompoundNode node;
	CarbonButton button;
	Function<ICompoundNode, Screen> creator;
	
	public CustomCompoundElement(ICompoundNode node, Function<ICompoundNode, Screen> creator) {
		super(node);
		this.node = node;
		this.creator = creator;
		button = addChild(new CarbonButton(0, 0, 0, 0, node.getName(), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	public void setEditable(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	
	@Override
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, shouldRenderIndex() ? Component.literal(index(node)+": ") : node.getName(), left, top, width, height, GuiAlign.LEFT, -1, node.hashCode());
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		button.x = left;
		button.y = top;
		button.setWidth(width);
		button.setHeight(height);
		button.render(stack, mouseX, mouseY, partialTicks);
	}
	
	protected void onClick(Button button) {
		if(creator == null) {
			CarbonConfig.LOGGER.info("Custom Compound function isn't implemented");
			return;
		}
		Screen screen = creator.apply(node);
		if(screen == null) {
			CarbonConfig.LOGGER.info("Custom Compound function created a null Object");
			return;
		}
		BaseCarbonScreen.pushExternalScreen(screen);
	}
}
