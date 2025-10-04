package carbonconfiglib.gui.nodes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.utils.ObjectLists;

public class EnumElement extends ValueElement
{
	DropDownState<Suggestion> state = new DropDownState<Suggestion>(T -> Component.literal(T.getName()), ObjectLists.empty()).valueOnly(true).allowEmpty(false).withListener(this::onSelectionChanged);
	DropDownMenu<Suggestion> values = addChild(new DropDownMenu<>(0, 0, 0, 0, state));
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0));
	ParseResult<Boolean> result;
	
	public EnumElement(IValueNode node) {
		super(node);
		state.setValues(node.getSuggestions());
		state.findDefaultSelected(T -> T.getValue().equals(node.getDefault()));
		state.findSelected(T -> T.getValue().equals(node.get()));
		text.getState().setValue(node.get());
		text.getState().setCallback(this::onTextChanged);
	}
	
	private void onTextChanged(String value) {
		text.setTextColor(0xE0E0E0);
		result = null;
		if(!value.isEmpty()) {
			result = node.isValid(value);
			if(!result.getValue()) {
				text.setTextColor(0xFF0000);
				return;
			}
			node.set(value);
			state.findSelected(T -> T.getValue().equals(value));
		}
	}
	
	private void onSelectionChanged(List<Suggestion> values) {
		if(values.isEmpty()) return;
		result = null;
		String value = values.get(0).getValue();
		if(value.isEmpty()) return;
		result = node.isValid(value);
		if(!result.getValue()) {
			text.setTextColor(0xFF0000);
			return;
		}
		node.set(value);
		text.getState().setSilentValue(value);
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		values.visible = value;
		text.visible = !values.visible && value;
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		values.x = left;
		values.y = top;
		values.setWidth(width);
		values.setHeight(height);
		values.render(stack, mouseX, mouseY, partialTicks);
		
		text.x = left;
		text.y = top;
		text.setWidth(width);
		text.setHeight(height);
		text.render(stack, mouseX, mouseY, partialTicks);
	}
	
}
