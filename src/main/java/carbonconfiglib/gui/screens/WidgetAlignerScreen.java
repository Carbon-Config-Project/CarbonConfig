package carbonconfiglib.gui.screens;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.impl.entries.WidgetAligner;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.ChunkCoordIntPair;
import speiger.src.collections.objects.utils.ObjectLists;

public class WidgetAlignerScreen extends BaseCarbonScreen
{
	public static final DecimalFormat FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	IConfigSerializer<WidgetAligner> serializer;
	BackgroundHolder holder;
	OverlayRenderer renderer;
	ICompoundNode node;
	WidgetAligner cached;
	
	ChunkCoordIntPair pos = null;
	float xScale;
	float yScale;
	
	DropDownState<Align> vertical = new DropDownState<Align>(Align.CENTER, T -> Texts.literal(T.name()), Align.values()).allowEmpty(false).withPrefix(Texts.translatable("gui.carbonconfig.align.vertical")).withListener(T -> updateSlider());
	DropDownState<Align> horizontal = new DropDownState<Align>(Align.CENTER, T -> Texts.literal(T.name()), Align.values()).allowEmpty(false).withPrefix(Texts.translatable("gui.carbonconfig.align.horizontal")).withListener(T -> updateSlider());
	SliderState xOff = new SliderState(0, -10000, 10000, T -> Texts.literal(FORMAT.format(getCurrentValue().xOffset()*100)+"%")).setPrefix(Texts.translatable("gui.carbonconfig.offset_x")).setListener(this::serialize);
	SliderState yOff = new SliderState(0, -10000, 10000, T -> Texts.literal(FORMAT.format(getCurrentValue().yOffset()*100)+"%")).setPrefix(Texts.translatable("gui.carbonconfig.offset_y")).setListener(this::serialize);
	SliderState scale = new SliderState(1000, 10, 5000, T -> Texts.literal(FORMAT.format(T/10F)+"%")).setPrefix(Texts.translatable("gui.carbonconfig.scale")).setListener(this::serialize);
	
	public WidgetAlignerScreen(ICompoundNode node, BackgroundHolder holder, OverlayRenderer renderer, IConfigSerializer<WidgetAligner> serializer) {
		this.node = node;
		this.holder = holder;
		this.renderer = renderer;
		this.serializer = serializer;
		this.node.createTemp();
		initDefault(getCurrentValue());
	}
	
	private void initDefault(WidgetAligner value) {
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		width = (res.getScaledWidth()-30);
		height = (res.getScaledHeight()-55);
		vertical.setDefaultValues(ObjectLists.singleton(value.verticalAlignment()));
		horizontal.setDefaultValues(ObjectLists.singleton(value.horizontalAlignment()));
		updateAlign();
		xOff.set((long)(value.xOffset()*10000F));
		yOff.set((long)(value.yOffset()*10000F));
		scale.set((long)(value.scale()*1000));
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int realWidth = width - 30;
		int realHeight = height - 55;
		xScale = 1F / realWidth;
		yScale = 1F / realHeight;
		slider(-196, -38, 120, 13, Align.CENTER, Align.END, xOff).withTooltip(Texts.translatable("gui.carbonconfig.scroll"));
		slider(-196, -24, 120, 13, Align.CENTER, Align.END, yOff).withTooltip(Texts.translatable("gui.carbonconfig.scroll"));
		xOff.getOwner().updateMessage();
		yOff.getOwner().updateMessage();
		dropDown(-75, -24, 150, 13, Align.CENTER, Align.END, vertical);
		dropDown(-75, -38, 150, 13, Align.CENTER, Align.END, horizontal);
		if(serializer.getFormat().hasEntry("scale")) {
			slider(76, -38, 120, 13, Align.CENTER, Align.END, scale).withTooltip(Texts.translatable("gui.carbonconfig.scroll"));
			scale.getOwner().updateMessage();
			button(76, -24, 60, 13, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.apply"), T -> onClose());
			button(137, -24, 59, 13, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.cancel"), T -> cancel());
		}
		else {
			button(76, -38, 120, 13, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.apply"), T -> onClose());
			button(76, -24, 120, 13, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.cancel"), T -> cancel());

		}
		updateAlign();
	}
	
	private void cancel() {
		node.setPrevious();
		onClose();
	}
	
	@Override
	public void onClose() {
		node.deleteTempIfNeeded();
		super.onClose();
	}
	
	private void updateSlider() {
		updateAlign();
		serialize();
	}
	
	private void updateAlign() {
		int realWidth = 10000;
		int realHeight = 10000;
		Align vertical = this.vertical.getSelectedElement();
		Align horizontal = this.horizontal.getSelectedElement();
		xOff.setMinValue(generateMin(horizontal, realWidth)).setMaxValue(generateMax(horizontal, realWidth));
		yOff.setMinValue(generateMin(vertical, realHeight)).setMaxValue(generateMax(vertical, realHeight));
	}
	
	private int generateMin(Align align, int value) {
		return align == Align.START ? 0 : (align == Align.CENTER ? -(value >> 1) : -value);
	}
	
	private int generateMax(Align align, int value) {
		return align == Align.START ? value : (align == Align.CENTER ? value >> 1 : 0);
	}
	
	private void serialize() {
		WidgetAligner aligner = new WidgetAligner(horizontal.getSelectedElement(), vertical.getSelectedElement(), round(xOff.get() * 0.0001F), round(yOff.get() * 0.0001F), scale.get() / 1000F);
		node.set(serializer.getFormat().serialize(serializer.serialize(aligner), false));
		cached = aligner;
	}
	
	private float round(float input) {
		return Math.round(input*10000)*0.0001F;
	}
	
	protected WidgetAligner getCurrentValue() {
		if(cached != null) return cached;
		ParseResult<WidgetAligner> aligner = serializer.deserialize(serializer.getFormat().parse(node.get()));
		return (cached = aligner.isValid() ? aligner.getValue() : serializer.getExample());
	}
	
	protected boolean isHoveringObject(double mouseX, double mouseY) {
		WidgetAligner current = getCurrentValue();
		double screenWidth = width-50;
		double screenHeight = height-50;
		float scale = current.scale();
		double width = renderer.unscaledWidth();
		double height = renderer.unscaledHeight();
		double x = 25 + current.applyX(screenWidth, width);
		double y = 25 + current.applyY(screenHeight, height);
		return mouseX >= x && mouseY >= y && mouseX <= x + width * scale && mouseY <= y + height * scale;
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(super.mouseClick(mouseX, mouseY, button)) {
			return true;
		}
		if(isHoveringObject(mouseX, mouseY)) {
			pos = new ChunkCoordIntPair((int)mouseX, (int)mouseY);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseRelease(double mouseX, double mouseY, int button) {
		pos = null;
		return super.mouseRelease(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScroll(double pMouseX, double pMouseY, double pDelta) {
		if(super.mouseScroll(pMouseX, pMouseY, pDelta)) return true;
		if(scale.getOwner() != null && isHoveringObject(pMouseX, pMouseY)) {
			return scale.getOwner().mouseScroll(pMouseX, pMouseY, pDelta);
		}
		return false;
	}
	
	@Override
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.chunkXPos != mouseX || pos.chunkZPos != mouseY)) {
			long xOffset = (long)((pos.chunkXPos - mouseX)*xScale*10000F);
			long yOffset = (long)((pos.chunkZPos - mouseY)*yScale*10000F);
			xOff.setSilent(xOff.get() - xOffset);
			yOff.setSilent(yOff.get() - yOffset);
			pos = new ChunkCoordIntPair(mouseX, mouseY);
			serialize();
		}
		drawDefaultBackground();
		GuiUtils.fillDropArea(10, 10, width-20, height-20, -3750202, false);
		GuiUtils.fillDropArea(15, 15, width-30, height-55, -7631989, true);
		GuiUtils.renderBackground(15, width-15, 15, height-40, 0F, holder.getTexture());
		GuiUtils.pushScissors(15, 15, width-30, height-55);
		GlStateManager.pushMatrix();
		GlStateManager.translate(15, 15, 0);
		renderer.render(width-31, height-56, partialTicks, getCurrentValue());
		GlStateManager.popMatrix();
		GuiUtils.popScissors();
	}
	
	public static interface OverlayRenderer {
		public void render(int screenWidth, int screenHeight, float partialTicks, WidgetAligner aligner);
		public double unscaledWidth();
		public double unscaledHeight();
	}
}
