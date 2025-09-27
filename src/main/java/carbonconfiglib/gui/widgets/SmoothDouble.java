package carbonconfiglib.gui.widgets;

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