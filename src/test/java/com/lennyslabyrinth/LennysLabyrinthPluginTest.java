package com.lennyslabyrinth;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LennysLabyrinthPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LennysLabyrinthPlugin.class);
		RuneLite.main(args);
	}
}