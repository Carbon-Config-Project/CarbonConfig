package carbonconfiglib.gui.screen;

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
public class SmoothFloat
{
	protected float value;
	protected float target;
	protected float agility;
	
	public SmoothFloat(float agility) {
		this.agility = agility;
	}
	
	public SmoothFloat(float value, float agility) {
		this.value = value;
		target = value;
		this.agility = agility;
	}
	
	public boolean isDone() { return Math.abs(target - value) <= 0.5F; }
	public void update(float delta) { value += (target - value) * agility * delta; }
	
	public void setTarget(float value) { target = value; }
	public void addTarget(float value) { target += value; }
	
	public void forceFinish() { value = target; }
	
	public float getValue() { return value; }
	public float getTarget() { return target; }
}