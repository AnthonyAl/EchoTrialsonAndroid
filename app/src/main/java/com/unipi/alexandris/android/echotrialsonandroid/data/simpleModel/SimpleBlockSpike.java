package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public abstract class SimpleBlockSpike extends SimpleBlock {

	protected boolean arisen = false;
	public int texture;
	protected boolean lethal = true;
	
	public SimpleBlockSpike(double x, double y, SimpleID id, int groupId, int texture) {
		super(x, y, id, groupId);
		this.texture = texture;
		setCancelUP(true);
	}

	public void setLethal(boolean lethal) {
		this.lethal = lethal;
	}

	public boolean isLethal() {
		return lethal;
	}

	public boolean isArisen() {
		return arisen;
	}

	public void setArisen(boolean arisen) {
		this.arisen = arisen;
	}
}
