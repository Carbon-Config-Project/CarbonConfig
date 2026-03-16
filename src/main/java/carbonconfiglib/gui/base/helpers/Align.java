package carbonconfiglib.gui.base.helpers;

/**
 * Copyright 2026 Speiger, Meduris
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
	
	
	public int alignStart(int startPos, int width, int size) {
		switch(this) {
			case CENTER: return startPos + (width >> 1) - (size >> 1);
			case END: return startPos + width - size;
			default: return startPos;
		}
	}
	
	public float alignStart(float startPos, float width, float size) {
		switch(this) {
			case CENTER: return startPos + (width * 0.5F) - (size * 0.5F);
			case END: return startPos + width - size;
			default: return startPos;
		}
	}
	
	public double alignStart(double startPos, double width, double size) {
		switch(this) {
			case CENTER: return startPos + (width * 0.5D) - (size * 0.5D);
			case END: return startPos + width - size;
			default: return startPos;
		}
	}
}