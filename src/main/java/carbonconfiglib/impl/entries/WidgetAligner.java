package carbonconfiglib.impl.entries;

import java.util.Objects;

import com.mojang.blaze3d.platform.GlStateManager;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.gui.api.types.EntrySettingTypes.CompoundOverride;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.utils.ParsedCollections.ParsedMap;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WidgetAligner
{
	final Align screenX;
	final Align screenY;
	final float xOffset;
	final float yOffset;
	final float scale;
	
	public WidgetAligner(Align screenX, Align screenY, float xOffset, float yOffset, float scale) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.scale = scale;
	}

	public WidgetAligner(Align screenX, Align screenY, float xOff, float yOff) {
		this(screenX, screenY, xOff, yOff, 1F);
	}
	
	public WidgetAligner() {
		this(Align.CENTER, Align.CENTER, 0F, 0F, 1F);
	}
	
	public Align horizontalAlignment() {
		return screenX;
	}
	
	public Align verticalAlignment() {
		return screenY;
	}
	
	public float scale() {
		return scale;
	}
	
	public float xOffset() {
		return xOffset;
	}
	
	public float yOffset() {
		return yOffset;
	}
	
	@Override
	public String toString() {
		return "WidgetAligner[alignX="+screenX+", alignY="+screenY+", xOffset="+xOffset+", yOffset="+yOffset+", scale="+scale+"]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof WidgetAligner) {
			WidgetAligner align = (WidgetAligner)obj;
			return align.screenX == screenX && align.screenY == screenY && Float.compare(align.xOffset, xOffset) == 0 && Float.compare(align.yOffset, yOffset) == 0 && Float.compare(align.scale, scale) == 0;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(screenX, screenY, xOffset, yOffset, scale);
	}
	
	public double applyX(double screenWidth, double width) {
		return screenX.alignStart(0F, screenWidth, width*scale)+xOffset*screenWidth;
	}
	
	public double applyY(double screenHeight, double height) {
		return screenY.alignStart(0F, screenHeight, height*scale)+yOffset*screenHeight;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void applyToPose(double screenWidth, double screenHeight, double width, double height) {
		GlStateManager.translated(screenX.alignStart(0F, screenWidth, width*scale)+xOffset*screenWidth, screenY.alignStart(0F, screenHeight, height*scale)+yOffset*screenHeight, 0F);
		GlStateManager.scalef(scale, scale, 1F);
	}
	
	private ParsedMap serialize() {
		ParsedMap map = new ParsedMap();
		map.put("horizontal-align", screenX);
		map.put("vertical-align", screenY);
		map.put("xOffset", xOffset);
		map.put("yOffset", yOffset);
		map.put("scale", scale);
		return map;
	}
	
	public static IConfigSerializer<WidgetAligner> create(Class<?> clz, boolean includeScale) {
		CompoundBuilder builder = new CompoundBuilder().addSetting(new CompoundOverride(clz))
				.enums("horizontal-align", Align.class)
				.enums("vertical-align", Align.class)
				.simple("xOffset", EntryDataType.FLOAT)
				.simple("yOffset", EntryDataType.FLOAT);
		if(includeScale) builder.simple("scale", EntryDataType.FLOAT);
		return IConfigSerializer.simpleSync(builder.build(), new WidgetAligner(), WidgetAligner::parse, WidgetAligner::serialize, WidgetAligner::read, WidgetAligner::write);
	}
	
	//Helper functions
	private static WidgetAligner parse(ParsedMap map) {
		return new WidgetAligner(map.getOrThrow("horizontal-align", Align.class), map.getOrThrow("vertical-align", Align.class), map.getOrThrow("xOffset", Float.class), map.getOrThrow("yOffset", Float.class), map.getOrDefault("scale", Float.class, 1F));
	}
	
	private static void write(IWriteBuffer buffer, WidgetAligner aligner) {
		buffer.writeEnum(aligner.screenX);
		buffer.writeEnum(aligner.screenY);
		buffer.writeFloat(aligner.xOffset);
		buffer.writeFloat(aligner.yOffset);
		buffer.writeFloat(aligner.scale);
	}
	
	private static WidgetAligner read(IReadBuffer buffer) {
		return new WidgetAligner(buffer.readEnum(Align.class), buffer.readEnum(Align.class), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
	}
}
