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
public class SmoothDouble
{
	protected double value;
	protected double target;
	protected double agility;
	
	public SmoothDouble(double agility) {
		this.agility = agility;
	}
	
	public SmoothDouble(double value, double agility) {
		this.value = value;
		target = value;
		this.agility = agility;
	}
	
	public boolean isDone() { return Math.abs(target - value) <= 0.5D; }
	public void update(double delta) {
		double diff = (target - value) * agility * delta;
		if(target > value) {
			value = Math.min(target, value + diff);
			return;
		}
		value = Math.max(target, value + diff);		
	}
	
	public void setTarget(double value) { target = value; }
	public void addTarget(double value) { target += value; }
	
	public void forceFinish() { value = target; }
	
	public double getValue() { return value; }
	public double getTarget() { return target; }
}