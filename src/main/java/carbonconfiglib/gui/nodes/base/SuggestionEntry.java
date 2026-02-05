package carbonconfiglib.gui.nodes.base;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.network.chat.Component;

public class SuggestionEntry extends ListEntry<SuggestionEntry> {
	Component text;
	Suggestion suggestion;
	
	public SuggestionEntry(Suggestion suggestion) {
		this.suggestion = suggestion;
		this.text = Component.literal(suggestion.getName());
	}

	@Override
	protected boolean containsSearch(String searchString) {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		ISuggestionRenderer renderer = ISuggestionRenderer.Registry.getRendererForType(suggestion.getType());
		if(renderer != null) {
			renderer.renderSuggestion(poseStack, suggestion.getValue(), left, (int)Align.CENTER.alignStart(top, height, 16));
			left += 20;
			width -= 20;
		}
		GuiUtils.drawScrollingShadowText(poseStack, font, text, left, top, width, height, GuiAlign.CENTER, -1, Objects.hashCode(suggestion));
	}
}