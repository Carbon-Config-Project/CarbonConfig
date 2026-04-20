package carbonconfiglib.examples;

import org.joml.Matrix3x2fStack;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.gui.api.types.CompoundType;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.screens.WidgetAlignerScreen.OverlayRenderer;
import carbonconfiglib.impl.entries.WidgetAligner;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TestWidget
{
	public static void initTest() {
		Config config = new Config("widgetTest");
		IConfigSerializer<WidgetAligner> instance = WidgetAligner.create(HelperObject.class, true);
		CompoundType.registerWidgetAligner(HelperObject.class, new Renderer(), instance);
		config.add("testing").addParsed("widget", new WidgetAligner(Align.CENTER, Align.CENTER, 0, 0, 1F), instance);
		instance = WidgetAligner.create(SecondHelper.class, false);
		CompoundType.registerWidgetAligner(SecondHelper.class, new Renderer(), instance);
		config.add("testing").addParsed("unscaled", new WidgetAligner(Align.CENTER, Align.CENTER, 0, 0, 1F), instance);
		
		CarbonConfig.CONFIGS.createConfig(config, ConfigSettings.withConfigType(ConfigType.CLIENT)).register();
	}
	
	public static class HelperObject {}
	public static class SecondHelper {}
	
	public static class Renderer implements OverlayRenderer {
		@Override
		public void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, float partialTicks, WidgetAligner aligner) {
			Matrix3x2fStack stack = graphics.pose();
			stack.pushMatrix();
			aligner.applyToPose(graphics, screenWidth, screenHeight, 50, 50);
			GuiUtils.drawFrame(graphics, 0, 0, 50, 50, -1, 1);
			stack.popMatrix();
		}

		@Override
		public double unscaledWidth() {
			return 50;
		}

		@Override
		public double unscaledHeight() {
			return 50;
		}
		
	}
}
