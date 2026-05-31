package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

import android.graphics.Bitmap;
import java.util.ArrayList;

@SuppressWarnings(value = "unused")
public class SimpleForeground extends SimplePhysicsObject {

	private Bitmap foreground;
	private ArrayList<char[][]> maps;
	private ArrayList<Bitmap> images;

	public SimpleForeground(int x, int y, int groupId) {
		super(x, y, 0, SimpleID.Foreground, groupId);
	}

	public Bitmap getForeground() {
		return foreground;
	}

	public void setForeground(Bitmap foreground) {
		this.foreground = foreground;
	}

	public ArrayList<char[][]> getMaps() {
		return maps;
	}

	public void setMaps(ArrayList<char[][]> maps) {
		this.maps = maps;
	}

	public ArrayList<Bitmap> getImages() {
		return images;
	}

	public void setImages(ArrayList<Bitmap> images) {
		this.images = images;
	}
}