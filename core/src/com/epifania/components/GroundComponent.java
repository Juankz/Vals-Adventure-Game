package com.epifania.components;

import com.badlogic.ashley.core.Component;

public class GroundComponent implements Component {
	
	public static final int RECTANGLE = 0;
	public static final int POLYGON = 1;
	public static final int CIRCLE = 2;
	
	public int shape;
}
