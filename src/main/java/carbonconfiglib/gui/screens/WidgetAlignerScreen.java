package carbonconfiglib.gui.screens;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.impl.entries.WidgetAligner;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import speiger.src.collections.objects.utils.ObjectLists;

public class WidgetAlignerScreen extends BaseCarbonScreen
{
	public static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#.##"), T -> T.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
	IConfigSerializer<WidgetAligner> serializer;
	BackgroundHolder holder;
	OverlayRenderer renderer;
	ICompoundNode node;
	WidgetAligner cached;
	
	ChunkPos pos = null;
	float xScale;
	float yScale;
	
	DropDownState<Align> vertical = new DropDownState<Align>(Align.CENTER, T -> Component.literal(T.name()), Align.values()).allowEmpty(false).withPrefix(Component.translatable("gui.carbonconfig.align.vertical")).withListener(T -> updateSlider());
	DropDownState<Align> horizontal = new DropDownState<Align>(Align.CENTER, T -> Component.literal(T.name()), Align.values()).allowEmpty(false).withPrefix(Component.translatable("gui.carbonconfig.align.horizontal")).withListener(T -> updateSlider());
	SliderState xOff = new SliderState(0, -10000, 10000, T -> Component.literal(FORMAT.format(getCurrentValue().xOffset()*100)+"%")).setPrefix(Component.translatable("gui.carbonconfig.offset_x")).setListener(this::serialize);
	SliderState yOff = new SliderState(0, -10000, 10000, T -> Component.literal(FORMAT.format(getCurrentValue().yOffset()*100)+"%")).setPrefix(Component.translatable("gui.carbonconfig.offset_y")).setListener(this::serialize);
	SliderState scale = new SliderState(1000, 10, 5000, T -> Component.literal(FORMAT.format(T/10F)+"%")).setPrefix(Component.translatable("gui.carbonconfig.scale")).setListener(this::serialize);
	
	public WidgetAlignerScreen(ICompoundNode node, BackgroundHolder holder, OverlayRenderer renderer, IConfigSerializer<WidgetAligner> serializer) {
		this.node = node;
		this.holder = holder;
		this.renderer = renderer;
		this.serializer = serializer;
		this.node.createTemp();
		initDefault(getCurrentValue());
	}
	
	private void initDefault(WidgetAligner value) {
		Window window = Minecraft.getInstance().getWindow();
		width = (window.getGuiScaledWidth()-30);
		height = (window.getGuiScaledHeight()-55);
		vertical.setDefaultValues(ObjectLists.singleton(value.screenY()));
		horizontal.setDefaultValues(ObjectLists.singleton(value.screenX()));
		updateAlign();
		xOff.set((long)(value.xOffset()*10000F));
		yOff.set((long)(value.yOffset()*10000F));
		scale.set((long)(value.scale()*1000));
	}
	
	@Override
	protected void init() {
		super.init();
		int realWidth = width - 30;
		int realHeight = height - 55;
		xScale = 1F / realWidth;
		yScale = 1F / realHeight;
		slider(-196, -38, 120, 13, Align.CENTER, Align.END, xOff).withTooltip(Component.translatable("gui.carbonconfig.scroll"));
		slider(-196, -24, 120, 13, Align.CENTER, Align.END, yOff).withTooltip(Component.translatable("gui.carbonconfig.scroll"));
		xOff.getOwner().updateMessage();
		yOff.getOwner().updateMessage();
		dropDown(-75, -24, 150, 13, Align.CENTER, Align.END, vertical);
		dropDown(-75, -38, 150, 13, Align.CENTER, Align.END, horizontal);
		if(serializer.getFormat().hasEntry("scale")) {
			slider(76, -38, 120, 13, Align.CENTER, Align.END, scale).withTooltip(Component.translatable("gui.carbonconfig.scroll"));
			scale.getOwner().updateMessage();
			button(76, -24, 60, 13, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.apply"), T -> onClose());
			button(137, -24, 59, 13, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.cancel"), T -> cancel());
		}
		else {
			button(76, -38, 120, 13, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.apply"), T -> onClose());
			button(76, -24, 120, 13, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.cancel"), T -> cancel());

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
		Align horizontal = this.horizontal.getSelectedElement();
		Align vertical = this.vertical.getSelectedElement();
		long x = xOff.get();
		long y = yOff.get();
		
		xOff.setMinValue(generateMin(horizontal, realWidth)).setMaxValue(generateMax(horizontal, realWidth)).set(updateValue(x, horizontal));
		yOff.setMinValue(generateMin(vertical, realHeight)).setMaxValue(generateMax(vertical, realHeight)).set(updateValue(y, vertical));
	}
	
	private long updateValue(long original, Align align) {
		switch(align) {
			case START: return original < 0 ? -original : original;
			case END: return original > 0 ? -original : original;
			default: return original;
		}
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if(isHoveringObject(mouseX, mouseY)) {
			pos = new ChunkPos((int)mouseX, (int)mouseY);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		pos = null;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if(super.mouseScrolled(pMouseX, pMouseY, pDelta)) return true;
		if(scale.getOwner() != null && isHoveringObject(pMouseX, pMouseY)) {
			return scale.getOwner().mouseScrolled(pMouseX, pMouseY, pDelta);
		}
		return false;
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.x != mouseX || pos.z != mouseY)) {
			long xOffset = (long)((pos.x - mouseX)*xScale*10000F);
			long yOffset = (long)((pos.z - mouseY)*yScale*10000F);
			xOff.setSilent(xOff.get() - xOffset);
			yOff.setSilent(yOff.get() - yOffset);
			pos = new ChunkPos(mouseX, mouseY);
			serialize();
		}
		renderDirtBackground(0);
		GuiUtils.fillDropArea(matrix, 10, 10, width-20, height-20, -3750202, false);
		GuiUtils.fillDropArea(matrix, 15, 15, width-30, height-55, -7631989, true);
		GuiUtils.renderBackground(15, width-15, 15, height-40, 0F, holder.getTexture());
		GuiUtils.pushScissors(15, 15, width-30, height-55);
		matrix.pushPose();
		matrix.translate(15, 15, 0);
		renderer.render(matrix, width-31, height-56, partialTicks, getCurrentValue());
		matrix.popPose();
		GuiUtils.popScissors();
	}
	
	public static interface OverlayRenderer {
		public void render(PoseStack stack, int screenWidth, int screenHeight, float partialTicks, WidgetAligner aligner);
		public double unscaledWidth();
		public double unscaledHeight();
	}
}
