package carbonconfiglib.gui.base.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class LayeredScreen extends Screen
{
	Stack<Screen> layers = new ObjectArrayList<>();
	
	public LayeredScreen() {
		super(Component.empty());
	}
	
	public static void pushGuiLayer(Screen screen) {
		Minecraft mc = Minecraft.getInstance();
		Screen current = mc.screen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).pushLayer(screen);
			return;
		}
		LayeredScreen newScreen = new LayeredScreen();
		mc.screen = null;
		mc.setScreen(newScreen);
		newScreen.pushLayer(current);
		newScreen.pushLayer(screen);
	}
	
	public static void popGuiLayer() {
		Screen current = Minecraft.getInstance().screen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).popLayer();
			return;
		}
		Minecraft.getInstance().setScreen(null);
	}
	
	public static void popAllGuiLayer() {
		Screen current = Minecraft.getInstance().screen;
		if(current instanceof LayeredScreen) {
			((LayeredScreen)current).popAllLayers();
		}
	}
	
	public static Screen topScreen() {
		Screen current = Minecraft.getInstance().screen;
		return current instanceof LayeredScreen ? ((LayeredScreen)current).top() : null;
	}
	
	public void pushLayer(Screen screen) {
		layers.push(screen);
        screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
	}
	
	public void popAllLayers() {
		while(!layers.isEmpty()) {
			popLayer();
		}
		minecraft.setScreen(null);
	}
	
	public void popLayer() {
		if(layers.isEmpty()) {
			minecraft.setScreen(null);
			return;
		}
		layers.pop().removed();
		if(!layers.isEmpty()) return;
		minecraft.setScreen(null);
	}
	
	@Override
	public void tick() {
		if(layers.isEmpty()) {
			minecraft.setScreen(null);
			return;
		}
		layers.top().tick();
	}
	
    public float getGuiFarPlane() {
        return 1000F + 2000F * (1 + layers.size());
    }
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		if(layers.isEmpty()) return;
		float farPlane = getGuiFarPlane();
        Window window = this.minecraft.getWindow();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f matrix4f = Matrix4f.orthographic(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), 0.0F, (float)((double)window.getHeight() / window.getGuiScale()), 1000.0F, farPlane);
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        poseStack.translate(0.0, 0.0, 1000 - farPlane);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
		forEach(T -> {
			stack.pushPose();
			T.render(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
			stack.popPose();
			stack.translate(0F, 0F, 2000F);
		}, false);
		layers.top().render(stack, mouseX, mouseY, partialTicks);
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
