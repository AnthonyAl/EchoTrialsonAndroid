package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public abstract class SimpleTrigger extends SimplePhysicsObject {

	protected int delay = 0;
	protected int speed = 1;
	protected int WR1 = 0;
	protected int WR2 = 0;
	protected int HR1 = 0;
	protected int HR2 = 0;

	public SimpleTrigger(int x, int y, SimpleID id, int groupId) {
		super(x, y, 48, id, groupId);
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getWR1() {
		return WR1;
	}

	public void setWR1(int WR1) {
		this.WR1 = WR1;
	}

	public int getWR2() {
		return WR2;
	}

	public void setWR2(int WR2) {
		this.WR2 = WR2;
	}

	public int getHR1() {
		return HR1;
	}

	public void setHR1(int HR1) {
		this.HR1 = HR1;
	}

	public int getHR2() {
		return HR2;
	}

	public void setHR2(int HR2) {
		this.HR2 = HR2;
	}
}
