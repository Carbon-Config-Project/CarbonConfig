package carbonconfiglib.impl.entries;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.gui.api.types.EntrySettingTypes.CompoundOverride;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.utils.ParsedCollections.ParsedMap;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundBuilder;

public class WidgetAligner
{
	private final Align screenX;
	private final Align screenY;
	private final float xOff;
	private final float yOff;
	private final float scale;
	
	public WidgetAligner() {
		this(Align.CENTER, Align.CENTER, 0F, 0F, 1F);
	}
	
	private WidgetAligner(ParsedMap map) {
		this(map.getOrThrow("horizontal-align", Align.class), map.getOrThrow("vertical-align", Align.class), map.getOrThrow("xOffset", Float.class), map.getOrThrow("yOffset", Float.class), map.getOrThrow("scale", Float.class));
	}
	
	public WidgetAligner(Align screenX, Align screenY, float xOff, float yOff, float scale) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.xOff = xOff;
		this.yOff = yOff;
		this.scale = scale;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(screenX, screenY, xOff, yOff, scale);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof WidgetAligner) {
			WidgetAligner aligner = (WidgetAligner)obj;
			return aligner.screenX == screenX && aligner.screenY == screenY && Float.compare(aligner.xOff, xOff) == 0 && Float.compare(aligner.yOff, yOff) == 0 && Float.compare(aligner.scale, scale) == 0;
		}
		return false;
	}
	
	public Align horizontalAlignment() {
		return screenX;
	}
	
	public Align verticalAlignment() {
		return screenY;
	}
	
	public float xOffset() {
		return xOff;
	}
	
	public float yOffset() {
		return yOff;
	}
	
	public float scale() {
		return scale;
	}
	
	public double applyX(double screenWidth, double width) {
		return screenX.alignStart(0F, screenWidth, width*scale)+xOff*screenWidth;
	}
	
	public double applyY(double screenHeight, double height) {
		return screenY.alignStart(0F, screenHeight, height*scale)+yOff*screenHeight;
	}
	
	public void applyToPose(PoseStack stack, double screenWidth, double screenHeight, double width, double height) {
		stack.translate(screenX.alignStart(0F, screenWidth, width*scale)+xOff*screenWidth, screenY.alignStart(0F, screenHeight, height*scale)+yOff*screenHeight, 0F);
		stack.scale(scale, scale, 1F);
	}
	
	private ParsedMap serialize() {
		ParsedMap map = new ParsedMap();
		map.put("horizontal-align", screenX);
		map.put("vertical-align", screenY);
		map.put("xOffset", xOff);
		map.put("yOffset", yOff);
		map.put("scale", scale);
		return map;
	}
	
	private static void write(IWriteBuffer buffer, WidgetAligner aligner) {
		buffer.writeEnum(aligner.screenX);
		buffer.writeEnum(aligner.screenY);
		buffer.writeFloat(aligner.xOff);
		buffer.writeFloat(aligner.yOff);
		buffer.writeFloat(aligner.scale);
	}
	
	private static WidgetAligner read(IReadBuffer buffer) {
		return new WidgetAligner(buffer.readEnum(Align.class), buffer.readEnum(Align.class), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
	}
	
	public static IConfigSerializer<WidgetAligner> create(Class<?> clz) {
		CompoundBuilder builder = new CompoundBuilder().addSetting(new CompoundOverride(clz))
				.enums("horizontal-align", Align.class)
				.enums("vertical-align", Align.class)
				.simple("xOffset", EntryDataType.FLOAT)
				.simple("yOffset", EntryDataType.FLOAT)
				.simple("scale", EntryDataType.FLOAT);
		return IConfigSerializer.simpleSync(builder.build(), new WidgetAligner(), WidgetAligner::new, WidgetAligner::serialize, WidgetAligner::read, WidgetAligner::write);
	}
}
