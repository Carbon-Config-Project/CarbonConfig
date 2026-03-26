package carbonconfiglib.gui.base.screen;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.base.interaction.IInteractable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class LayeredScreen extends GuiScreen implements IInteractable, ITickableScreen
{
	Stack<GuiScreen> layers = new ObjectArrayList<>();
	
	public LayeredScreen() {
	}
	
	public static void pushGuiLayer(GuiScreen screen) {
		Minecraft mc = Minecraft.getMinecraft();
		GuiScreen current = mc.currentScreen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).pushLayer(screen);
			return;
		}
		LayeredScreen newScreen = new LayeredScreen();
		mc.currentScreen = null;
		mc.displayGuiScreen(newScreen);
		newScreen.pushLayer(current);
		newScreen.pushLayer(screen);
	}
	
	public static void popGuiLayer() {
		GuiScreen current = Minecraft.getMinecraft().currentScreen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).popLayer();
			return;
		}
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	public static void popAllGuiLayer() {
		GuiScreen current = Minecraft.getMinecraft().currentScreen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).popAllLayers();
		}
	}
	
	public static GuiScreen topScreen() {
		GuiScreen current = Minecraft.getMinecraft().currentScreen;
		return current instanceof LayeredScreen ? ((LayeredScreen)current).top() : current;
	}
	
	public void pushLayer(GuiScreen screen) {
		layers.push(screen);
		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        screen.setWorldAndResolution(mc, res.getScaledWidth(), res.getScaledHeight());
	}
	
	public void popAllLayers() {
		while(!layers.isEmpty()) {
			popLayer();
		}
		mc.displayGuiScreen(null);
	}
	
	public void popLayer() {
		if(layers.isEmpty()) {
			mc.displayGuiScreen(null);
			return;
		}
		layers.pop().onGuiClosed();
		if(!layers.isEmpty()) return;
		mc.displayGuiScreen(null);
	}
	
	@Override
	public void tick() {
		if(layers.isEmpty()) {
			mc.displayGuiScreen(null);
			return;
		}
		GuiScreen screen = top();
		if(screen instanceof ITickableScreen) {
			((ITickableScreen)screen).tick();
		}
	}
	
    public float getGuiFarPlane() {
        return 1000F + 2000F * (1 + layers.size());
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(layers.isEmpty()) return;
		float farPlane = getGuiFarPlane();
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, farPlane);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, 1000 - farPlane);
        
		forEach(T -> {
			GL11.glPushMatrix();
			T.drawScreen(Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
			GL11.glPopMatrix();
			GL11.glTranslatef(0.0F, 0.0F, 2000.0F);
		}, false);
		layers.top().drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void forEach(Consumer<GuiScreen> layer, boolean top) {
		for(int i = layers.size()-1;i>=(top ? 0 : 1);i--) {
			layer.accept(layers.peek(i));
		}
	}
	
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height){
		super.setWorldAndResolution(mc, width, height);
		forEach(T -> T.setWorldAndResolution(mc, width, height), true);
	}
	
	@Override
	public void handleInput() {
		if(layers.isEmpty()) return;
		layers.top().handleInput();
	}
		
	public GuiScreen top() {
		return layers.isEmpty() ? null : layers.top();
	}
}
