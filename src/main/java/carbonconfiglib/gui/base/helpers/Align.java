package carbonconfiglib.gui.base.helpers;

public enum Align {
	START,
	CENTER,
	END;
	
	public float align(float width) {
		switch(this) {
			case CENTER: return width * -0.5F;
			case END: return -width;
			default: return 0F;
		}
	}
	
	public float alignCenter() {
		switch(this) {
			case CENTER: return 0F;
			case END: return 0.5F;
			default: return -0.5F;
		}
	}
	
	public float align(float startPos, float width, float size) {
		switch(this) {
			case CENTER: return startPos + (width * 0.5F) - (size * 0.5F);
			case END: return startPos + width;
			default: return startPos - size;
		}
	}
	
	public float alignStart(float startPos, float width, float size) {
		switch(this) {
			case CENTER: return startPos + (width * 0.5F) - (size * 0.5F);
			case END: return startPos + width - size;
			default: return startPos;
		}
	}
}