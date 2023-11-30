package carbonconfiglib.gui.widgets.screen;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiScreen;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class CarbonScreen extends GuiScreen implements IInteractableContainer
{
	private IInteractable focused;
	private boolean isDragging;
	List<IInteractable> interactables = new ObjectArrayList<>();
	List<IRenderable> renderable = new ObjectArrayList<>();
	double lastX = -1;
	double lastY = -1;
	
	@Override
	public void initGui() {
		super.initGui();
		interactables.clear();
		renderable.clear();
	}
	
	public <T extends IWidget> T addWidget(T widget) {
		interactables.add(widget);
		renderable.add(widget);
		return widget;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		double weel = Mouse.getDWheel() / 120D;
		if(((int)weel) != 0 && mouseScroll(mouseX, mouseY, weel));
		
		for(IRenderable render : renderable) {
			render.render(mc, mouseX, mouseY, partialTicks);
		}
	}
	
	public void tick() {
		
	}
	
	public final boolean isDragging() {
		return this.isDragging;
	}
	
	public final void setDragging(boolean value) {
		this.isDragging = value;
	}
	
	public IInteractable getFocused() {
		return this.focused;
	}
	
	public void setFocused(IInteractable interact) {
		this.focused = interact;
	}

	@Override
	public List<? extends IInteractable> children() {
		return interactables;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(this.charTyped(typedChar, keyCode)) return;
		if (keyCode == 1) {
			onClose();
		}
	}
	
	protected void onClose() {
		this.mc.displayGuiScreen((GuiScreen)null);
		if(this.mc.currentScreen == null){
			this.mc.setIngameFocus();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		lastX = mouseX;
		lastY = mouseY;
		if(mouseClick(mouseX, mouseY, mouseButton)) return;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if(mouseRelease(mouseX, mouseY, state)) return;
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		double diffX = mouseX - lastX;
		double diffY = mouseY - lastY;
		lastX = mouseX;
		lastY = mouseY;
		if(mouseDrag(mouseX, mouseY, clickedMouseButton, diffX, diffY)) return;
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	
}
