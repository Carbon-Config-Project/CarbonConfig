package carbonconfiglib.gui.base.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager;

import carbonconfiglib.gui.api.Texts;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class LayeredScreen extends Screen
{
	Stack<Screen> layers = new ObjectArrayList<>();
	
	public LayeredScreen() {
		super(Texts.empty());
	}
	
	public static void pushGuiLayer(Screen screen) {
		Minecraft mc = Minecraft.getInstance();
		Screen current = mc.currentScreen;
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
		Screen current = Minecraft.getInstance().currentScreen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).popLayer();
			return;
		}
		Minecraft.getInstance().displayGuiScreen(null);
	}
	
	public static void popAllGuiLayer() {
		Screen current = Minecraft.getInstance().currentScreen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).popAllLayers();
		}
	}
	
	public static Screen topScreen() {
		Screen current = Minecraft.getInstance().currentScreen;
		return current instanceof LayeredScreen ? ((LayeredScreen)current).top() : null;
	}
	
	public void pushLayer(Screen screen) {
		layers.push(screen);
        screen.init(minecraft, minecraft.mainWindow.getScaledWidth(), minecraft.mainWindow.getScaledHeight());
	}
	
	public void popAllLayers() {
		while(!layers.isEmpty()) {
			popLayer();
		}
		minecraft.displayGuiScreen(null);
	}
	
	public void popLayer() {
		if(layers.isEmpty()) {
			minecraft.displayGuiScreen(null);
			return;
		}
		layers.pop().removed();
		if(!layers.isEmpty()) return;
		minecraft.displayGuiScreen(null);
	}
	
	@Override
	public void tick() {
		if(layers.isEmpty()) {
			minecraft.displayGuiScreen(null);
			return;
		}
		layers.top().tick();
	}
	
    public float getGuiFarPlane() {
        return 1000F + 2000F * (1 + layers.size());
    }
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if(layers.isEmpty()) return;
		float farPlane = getGuiFarPlane();
        MainWindow mainwindow = minecraft.mainWindow;
        GlStateManager.clear(256, Minecraft.IS_RUNNING_ON_MAC);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double)mainwindow.getFramebufferWidth() / mainwindow.getGuiScaleFactor(), (double)mainwindow.getFramebufferHeight() / mainwindow.getGuiScaleFactor(), 0.0D, 1000.0D, farPlane);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translatef(0.0F, 0.0F, 1000 - farPlane);
		forEach(T -> {
			T.render(Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
	        GlStateManager.translatef(0.0F, 0.0F, 2000.0F);
		}, false);
		layers.top().render(mouseX, mouseY, partialTicks);
	}
	
	private void forEach(Consumer<Screen> layer, boolean top) {
		for(int i = layers.size()-1;i>=(top ? 0 : 1);i--) {
			layer.accept(layers.peek(i));
		}
	}
	
	@Override
	public void resize(Minecraft mc, int width, int height) {
		super.resize(mc, width, height);
		forEach(T -> T.resize(mc, width, height), true);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return !layers.isEmpty() && layers.top().mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return !layers.isEmpty() && layers.top().mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double diffX, double diffY) {
		return !layers.isEmpty() && layers.top().mouseDragged(mouseX, mouseY, button, diffX, diffY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		return !layers.isEmpty() && layers.top().mouseScrolled(mouseX, mouseY, scroll);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return !layers.isEmpty() && layers.top().keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return !layers.isEmpty() && layers.top().keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char letter, int scancode) {
		return !layers.isEmpty() && layers.top().charTyped(letter, scancode);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if(layers.isEmpty()) return;
		layers.top().mouseMoved(mouseX, mouseY);
	}
	
	public Screen top() {
		return layers.isEmpty() ? null : layers.top();
	}
}
