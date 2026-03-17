package carbonconfiglib.examples;

import com.mojang.blaze3d.vertex.PoseStack;

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
		public void render(PoseStack stack, int screenWidth, int screenHeight, float partialTicks, WidgetAligner aligner) {
			stack.pushPose();
			aligner.applyToPose(stack, screenWidth, screenHeight, 50, 50);
			GuiUtils.drawFrame(stack, 0F, 0F, 50, 50, -1, 1F);
			
			stack.popPose();
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
