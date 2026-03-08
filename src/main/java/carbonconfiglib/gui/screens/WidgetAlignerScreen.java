package carbonconfiglib.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.impl.entries.WidgetAligner;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.world.level.ChunkPos;

public class WidgetAlignerScreen extends BaseCarbonScreen
{
	IConfigSerializer<WidgetAligner> serializer;
	OverlayRenderer renderer;
	ICompoundNode node;
	WidgetAligner cached;
	SliderState xOff = new SliderState(0, -10000, 10000);
	SliderState yOff = new SliderState(0, -10000, 10000);
	
	ChunkPos pos = null;
	float xScale;
	float yScale;
	
	public WidgetAlignerScreen(ICompoundNode node, OverlayRenderer renderer, IConfigSerializer<WidgetAligner> serializer) {
		this.node = node;
		this.renderer = renderer;
		this.serializer = serializer;
	}
	
	@Override
	protected void init() {
		super.init();
		xScale = 1F / (width-50F);
		yScale = 1F / (height-50F);
	}
	
	private long unscale(float value, float width) {
		return (long)(value * (width-50));
	}
	
	private void serialize() {
		WidgetAligner aligner = new WidgetAligner(Align.CENTER, Align.CENTER, xOff.get() * xScale, yOff.get() * yScale, 1F);
		node.set(serializer.getFormat().serialize(serializer.serialize(aligner), false));
		cached = aligner;
	}
	
	protected WidgetAligner getCurrentValue() {
		if(cached != null) return cached;
		ParseResult<WidgetAligner> aligner = serializer.deserialize(serializer.getFormat().parse(node.get()));
		return (cached = aligner.isValid() ? aligner.getValue() : serializer.getExample());
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(isHoveringObject(mouseX, mouseY)) {
			pos = new ChunkPos((int)mouseX, (int)mouseY);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		pos = null;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	protected boolean isHoveringObject(double mouseX, double mouseY) {
		WidgetAligner current = getCurrentValue();
		double screenWidth = width-50;
		double screenHeight = height-50;
		double width = renderer.unscaledWidth();
		double height = renderer.unscaledHeight();
		double x = 25 + current.applyX(screenWidth, width);
		double y = 25 + current.applyY(screenHeight, height);
		return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.x != mouseX || pos.z != mouseY)) {
			long xOffset = unscale((pos.x - mouseX)*xScale, width);
			long yOffset = unscale((pos.z - mouseY)*yScale, height);
			xOff.setSilent(xOff.get() - xOffset);
			yOff.setSilent(yOff.get() - yOffset);
			pos = new ChunkPos(mouseX, mouseY);
			serialize();
		}
		renderDirtBackground(0);
		matrix.pushPose();
		matrix.translate(25, 25, 0);
		renderer.render(matrix, width-50, height-50, partialTicks, getCurrentValue(), isHoveringObject(mouseX, mouseY) ? 0xFF00FF00 : -1);
		matrix.popPose();
	}
	
	public static interface OverlayRenderer {
		public void render(PoseStack stack, int screenWidth, int screenHeight, float partialTicks, WidgetAligner aligner, int color);
		public double unscaledWidth();
		public double unscaledHeight();
	}
}
