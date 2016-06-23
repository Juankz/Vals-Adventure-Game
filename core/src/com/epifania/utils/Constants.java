/**
 * Use this class for global constants like:
 * 1. World height and width
 * 2. Camera units and UI camera units
 * 3. Gravity (if it is not supposed to change)
 * 4. Path to files or directories
 */
package com.epifania.utils;

public class Constants {

	public static final String ATLAS_PATH = "val.pack";
	public static final float ViewportWidth = 19.20f;
	public static final float ViewportHeight = 10.8f;
	public static final float UIViewportWidth = 1280;
	public static final float UIViewportHeight = 720;
	public static final float PixelsPerUnit = 1/70f;
	public static final float inversePPU = 70f;
	public static final String[] scriptsNames={"scripts/script1.xml","scripts/scriptP.xml","scripts/scriptP.xml"};
	public static final String[] mapsNames={"adventure_maps/level1.tmx","adventure_maps/prototype.tmx","adventure_maps/prototype.tmx"};
	public static final String[] objectsLayersNames={"Objects","Interior Objects"};
	public static final String[] itemsLayersNames={"Items","Build Interior"};
	public static final short[] groupsIndexes={1,2};
	public static final short BOUNDS = 0x0001;
	public static final short PLAYER = 0x0008;
	public static final short[] layerCategoryBits={0x0002,0x0004};
	public static final short[] layerMaskBits={0x0002|BOUNDS|PLAYER,0x0004|BOUNDS|PLAYER};
	public static final short LAYER1MASK = 0x0002|BOUNDS;
	public static final short LAYER2MASK = 0x0004|BOUNDS;
}
