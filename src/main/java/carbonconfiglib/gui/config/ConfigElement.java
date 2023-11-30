package carbonconfiglib.gui.config;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.ListSelectionScreen;
import carbonconfiglib.gui.widgets.CarbonHoverIconButton;
import carbonconfiglib.gui.widgets.CarbonHoverIconButton.IconInfo;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.IOwnable;
import carbonconfiglib.gui.widgets.Icon;
import carbonconfiglib.gui.widgets.screen.IInteractable;
import carbonconfiglib.gui.widgets.screen.IWidget;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ConfigElement extends Element
{
	private static final IChatComponent DELETE = new ChatComponentTranslation("gui.carbonconfig.delete");
	private static final IChatComponent REVERT = new ChatComponentTranslation("gui.carbonconfig.revert");
	private static final IChatComponent DEFAULT = new ChatComponentTranslation("gui.carbonconfig.default");
	private static final IChatComponent SUGGESTIONS = new ChatComponentTranslation("gui.carbonconfig.suggestions");
	private static final IChatComponent RELOAD = new ChatComponentTranslation("gui.carbonconfig.reload").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
	private static final IChatComponent RESTART = new ChatComponentTranslation("gui.carbonconfig.restart").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
	protected List<IInteractable> listeners = new ObjectArrayList<>();
	protected List<Map.Entry<IWidget, AlignOffset>> mappedListeners = new ObjectArrayList<>();
	protected IConfigNode node;
	protected IValueNode value;
	protected IArrayNode array;
	protected ICompoundNode compound;
	protected int compoundIndex = -1;
	
	protected CarbonIconButton setReset;
	protected CarbonIconButton setDefault;
	protected CarbonIconButton suggestion;
	protected CarbonHoverIconButton moveDown;
	protected CarbonHoverIconButton moveUp;
	
	public ConfigElement(IConfigNode node) {
		super(node.getName());
		this.node = node;
		this.value = node.asValue();
	}
	
	public ConfigElement(IConfigNode node, IValueNode value) {
		super(node.getName());
		this.node = node;
		this.value = value;
	}
	
	public ConfigElement(IConfigNode node, IArrayNode array) {
		super(node.getName());
		this.node = node;
		this.array = array;
	}
	
	public ConfigElement(IConfigNode node, IArrayNode array, int index) {
		super(node.getName());
		this.node = node;
		this.array = array;
		this.value = array.asValue(index);
	}
	
	protected <T extends IWidget> T addChild(T element) {
		return addChild(element, GuiAlign.RIGHT, 0);
	}
	
	protected <T extends IWidget> T addChild(T element, int xOffset) {
		return addChild(element, GuiAlign.RIGHT, xOffset);
	}
	
	protected <T extends IWidget> T addChild(T element, GuiAlign align, int xOffset) {
		listeners.add(element);
		mappedListeners.add(new AbstractMap.SimpleEntry<>(element, new AlignOffset(align, -xOffset - 19)));
		if(owner != null && element instanceof IOwnable) {
			((IOwnable)element).setOwner(owner);
		}
		return element;
	}
	
	@Override
	public void init() {
		super.init();
		if(createResetButtons(value)) {
			if(isArray()) {
				setReset = addChild(new CarbonIconButton(0, 0, 18, 18, Icon.DELETE, "", this::onDeleted).setIconOnly(), -31);
				setReset.enabled = isReset();
				moveDown = new CarbonHoverIconButton(0, 0, 15, 8, new IconInfo(0, -3, 16, 16), Icon.MOVE_DOWN, Icon.MOVE_DOWN_HOVERED, this::onMoveDown);
				listeners.add(moveDown);
				moveUp = new CarbonHoverIconButton(0, 0, 15, 8, new IconInfo(0, -3, 16, 16), Icon.MOVE_UP, Icon.MOVE_UP_HOVERED, this::onMoveUp);
				listeners.add(moveUp);
			}
			else {
				setReset = addChild(new CarbonIconButton(0, 0, 18, 18, Icon.REVERT, "", this::onReset).setIconOnly(), -21);
				setDefault = addChild(new CarbonIconButton(0, 0, 18, 18, Icon.SET_DEFAULT, "", this::onDefault).setIconOnly(), -40);
				suggestion = addChild(new CarbonIconButton(0, 0, 18, 18, Icon.SUGGESTIONS, "", this::onSuggestion).setIconOnly(), -59);
				setReset.enabled = isReset();
				setDefault.enabled = !isDefault();
				suggestion.visible = false;
			}
		}
		if(owner != null) {
			for(IInteractable entry : listeners) {
				if(entry instanceof IOwnable) {
					((IOwnable)entry).setOwner(owner);
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		if(setReset != null) {
			setReset.enabled = isReset();
		}
		if(setDefault != null) {
			setDefault.enabled = !isDefault();
		}
	}
	
	@Override
	public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		if(renderName() && !isArray()) {
			renderName(left, top, isChanged(), isCompound() ? 80 : 200, height);
			if(!isCompound()) {
				if(node.requiresReload()) {
					GuiUtils.drawTextureRegion(left-16, top+(height/2)-6, 12, 12, Icon.RELOAD, 16, 16);
					if(mouseX >= left-16 && mouseX <= left-4 && mouseY >= top && mouseY <= top+height && owner.isInsideList(mouseX, mouseY)) {
						owner.addTooltips(RELOAD);
					}
				}
				if(node.requiresRestart()) {
					GuiUtils.drawTextureRegion(left-16, top+(height/2)-6, 12, 12, Icon.RESTART, 16, 16);
					if(mouseX >= left-16 && mouseX <= left-4 && mouseY >= top && mouseY <= top+height && owner.isInsideList(mouseX, mouseY)) {
						owner.addTooltips(RESTART);
					}
				}
			}
		}
		int maxX = Integer.MAX_VALUE;
		if(renderChildren()) {
			if(isArray()) {
				moveUp.xPosition = left + width - 16;
				moveUp.yPosition = top;
				moveUp.visible = canMoveUp();
				moveUp.render(mc, mouseX, mouseY, partialTicks);
				moveDown.xPosition = left + width - 16;
				moveDown.yPosition = top + 10;
				moveDown.visible = canMoveDown();
				moveDown.render(mc, mouseX, mouseY, partialTicks);
				if(moveDown.visible || moveUp.visible) {
					left -= 8;
				}
			}
			for(Map.Entry<IWidget, AlignOffset> entry : mappedListeners) {
				IWidget widget = entry.getKey();
				AlignOffset offset = entry.getValue();
				widget.setX(offset.align.align(left, width, widget.getWidgetWidth()) + offset.offset);
				widget.setY(top);
				widget.render(mc, mouseX, mouseY, partialTicks);
				maxX = Math.min(maxX, widget.getX());
			}
		}
		maxX = getMaxX(maxX);
		if(isArray()) {
			IChatComponent comp = new ChatComponentText(indexOf()+":");
			renderText(comp, maxX-115, top-1, 105, height, GuiAlign.RIGHT, -1);
		}
		if(mouseY >= top && mouseY <= top + height && mouseX >= left && mouseX <= maxX-2 && owner.isInsideList(mouseX, mouseY)) {
			owner.addTooltips(node.getTooltip());
		}
		if(isArray()) {
			if(setReset.isHovered() && owner.isInsideList(mouseX, mouseY)) {
				owner.addTooltips(DELETE);
			}
		}
		else {
			if(setReset.isHovered() && owner.isInsideList(mouseX, mouseY)) {
				owner.addTooltips(REVERT);
			}
			if(setDefault.isHovered() && owner.isInsideList(mouseX, mouseY)) {
				owner.addTooltips(DEFAULT);
			}
			suggestion.visible = hasSuggestions();
			if(suggestion.visible && suggestion.isHovered() & owner.isInsideList(mouseX, mouseY)) {
				owner.addTooltips(SUGGESTIONS);
			}
		}
	}
	
	protected boolean hasSuggestions() {
		if(isCompound()) {
			if(compound.isForcedSuggestion(compoundIndex)) return false;
			List<Suggestion> suggestions = compound.getValidValues(compoundIndex);
			return suggestions != null && suggestions.size() > 0;
		}
		if(node != null && node.isForcingSuggestions()) return false;
		List<Suggestion> suggestions = node == null ? null : node.getValidValues();
		return suggestions != null && suggestions.size() > 0;
	}
	
	public void setCompound(ICompoundNode compound, int index) {
		this.compound = compound;
		this.compoundIndex = index;
		if(compound == null || index < 0) return;
		setName(compound.getName(index));
	}
	
	protected int getMaxX(int prevMaxX) {
		return prevMaxX;
	}
	
	protected boolean isArray() {
		return array != null;
	}
	
	protected boolean isCompound() {
		return compound != null && compoundIndex >= 0;
	}
	
	protected void onMoveDown(CarbonHoverIconButton button) {
		if(!isArray()) return;
		array.moveDown(indexOf());
		owner.updateInformation();
	}
	
	protected void onMoveUp(CarbonHoverIconButton button) {
		if(!isArray()) return;
		array.moveUp(indexOf());
		owner.updateInformation();		
	}
	
	protected boolean canMoveUp() {
		return indexOf() > 0;
	}
	
	protected boolean canMoveDown() {
		return indexOf() < array.size() - 1;
	}
	
	protected boolean renderName() {
		return true;
	}
	
	protected boolean renderChildren() {
		return true;
	}
	
	protected boolean createResetButtons(IValueNode value) {
		return value != null;
	}
	
	protected int indexOf() {
		return array.indexOf(value);
	}
	
	protected boolean isReset() {
		return isArray() || value.isChanged();
	}
	
	public boolean isChanged() {
		return value.isChanged();
	}
	
	public boolean isDefault() {
		return value.isDefault();
	}
	
	public IConfigNode getNode() {
		return node;
	}
	
	protected void onDeleted(CarbonIconButton button) {
		if(!isArray()) return;
		owner.removeEntry(this);
	}
	
	protected void onReset(CarbonIconButton button) {
		value.setPrevious();
		updateValues();
	}
	
	protected void onDefault(CarbonIconButton button) {
		if(value == null) return;
		value.setDefault();
		updateValues();
	}
	
	protected void onSuggestion(CarbonIconButton button) {
		if(value == null) return;
		if(isCompound()) {
			mc.displayGuiScreen(ListSelectionScreen.ofCompoundValue(mc.currentScreen, node, value, compound, compoundIndex, owner.getCustomTexture()));
			return;
		}
		mc.displayGuiScreen(ListSelectionScreen.ofValue(mc.currentScreen, node, value, owner.getCustomTexture()));
	}
	
	@Override
	public void updateValues() {
	}
	
	@Override
	public List<? extends IInteractable> children() {
		return listeners;
	}
	
	public static class AlignOffset {
		GuiAlign align;
		int offset;
		
		public AlignOffset(GuiAlign align, int offset) {
			this.align = align;
			this.offset = offset;
		}
	}
	
	public static enum GuiAlign {
		LEFT,
		CENTER,
		RIGHT;
		
		public int align(int left, int bounds, int width) {
			switch(this) {
				case LEFT: return left;
				case CENTER: return left + (bounds/2) - (width / 2);
				case RIGHT: return left + bounds - width - 42;
				default: return left;
			}
		}
		
		public float align(float width) {
			switch(this) {
				case CENTER: return width * -0.5F;
				case RIGHT: return -width;
				default: return 0F;
			}
		}
		
		public float alignCenter() {
			switch(this) {
				case CENTER: return 0F;
				case RIGHT: return 0.5F;
				default: return -0.5F;
			}
		}
	}
}
