package carbonconfiglib.gui.nodes;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.suggestion.ISuggestionRenderer;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.gui.base.helpers.Align;
import net.minecraft.network.chat.Component;

public class RegistryElement extends SelectionElement
{
	ISuggestionRenderer renderer;
	int[] bounds = new int[2];
	Component last;
	public RegistryElement(IValueNode node, ISuggestionRenderer renderer) {
		super(node);
		this.renderer = renderer;
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		bounds[0] = left;
		bounds[1] = top;
		if(renderer != null) {
			last = renderer.renderSuggestion(stack, node.get(), left, (int)Align.CENTER.alignStart(top, height, 16));
			left += 20;
		}
		super.renderRightPart(stack, left, top, width, height, mouseX, mouseY, selected, partialTicks);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		if(last != null && mouseX >= bounds[0] && mouseX < bounds[0] + 20 && mouseY >= bounds[1] && mouseY < bounds[1] + 20) {
			tooltips.accept(last);
		}
		super.provideTooltips(mouseX, mouseY, tooltips);
	}
	
	
	public static DataType createForType(Class<?> clz, String defaultValue) {
		ISuggestionRenderer renderer = ISuggestionRenderer.Registry.getRendererForType(clz);
		return new DataType(defaultValue, T -> new RegistryElement(T, renderer));
	}
}
