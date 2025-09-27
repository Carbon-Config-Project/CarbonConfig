package carbonconfiglib.gui.base.screen;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class BaseCarbonScreen extends Screen
{
	public static final int DEFAULT_DELAY = 200;
	protected int centerX;
	protected int centerY;
	protected int tick;
	int lastMouseX = 0;
	int lastMouseY = 0;
	long lastCheck = 0L;
	int lastDrawnToolTipAmount = 0;
	boolean renderTooltip = false;
	
	public BaseCarbonScreen() {
		super(Component.empty());
	}
	
	@Override
	protected void init() {
		super.init();
		centerX = (this.width / 2);
		centerY = (this.height / 2);
		clearWidgets();
	}
	
	@Override
	public void tick() {
		super.tick();
		tick++;
	}
	
	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrix, mouseX, mouseY, partialTicks);
		renderWidgets(matrix, mouseX, mouseY, partialTicks);
		renderForeground(matrix, mouseX, mouseY, partialTicks);
		renderTooltips(matrix, mouseX, mouseY, partialTicks);
	}
	
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void renderWidgets(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		for(Widget widget : this.renderables) {
			widget.render(matrix, mouseX, mouseY, partialTicks);
		}	
	}
	
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void collectTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		
	}
	
	public void renderTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		List<FormattedCharSequence> tooltips = new ObjectArrayList<>();
		if(mouseX != Integer.MAX_VALUE && mouseY != Integer.MAX_VALUE) {
			for(GuiEventListener listener : children()) {
				if(listener instanceof ITooltipProvider) {
					((ITooltipProvider)listener).provideTooltips(T -> tooltips.addAll(font.split(T, Math.max(mouseX, width - mouseX) - 20)));
				}
			}
			collectTooltips(matrix, mouseX, mouseY, partialTicks, T -> tooltips.addAll(font.split(T, Math.max(mouseX, width - mouseX) - 20)));
		}
		if((!renderTooltip && (lastMouseX != mouseX || lastMouseY != mouseY)) || tooltips.isEmpty()) {
			lastCheck = System.currentTimeMillis();
			lastMouseX = mouseX;
			lastMouseY = mouseY;
			lastDrawnToolTipAmount = 0;
			if(tooltips.isEmpty()) renderTooltip = false;
			return;
		}
		else if(System.currentTimeMillis() - lastCheck >= DEFAULT_DELAY) {
			renderTooltip = true;
		}
		else {
			lastDrawnToolTipAmount = 0;
			return;
		}
		lastDrawnToolTipAmount = 0;
		renderTooltip(matrix, tooltips, mouseX, mouseY);
		lastDrawnToolTipAmount = tooltips.size();
	}
}
