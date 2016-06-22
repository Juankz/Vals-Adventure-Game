package com.epifania.components;

import com.badlogic.ashley.core.Component;

public class CoinComponent implements Component {

	public static final int GOLD = 1;
	public static final int SILVER = 2;
	public static final int BRONCE = 3;
	public int value = GOLD;
}
