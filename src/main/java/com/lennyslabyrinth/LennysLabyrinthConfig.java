package com.lennyslabyrinth;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lennyslabyrinth")
public interface LennysLabyrinthConfig extends Config
{
	@ConfigItem(
		keyName = "debug",
		name = "Debug Mode",
		description = "Show all animation IDs in chat when player performs animations"
	)
	default boolean debug()
	{
		return false;
	}

	@ConfigItem(
		keyName = "eventKey",
		name = "Event Key",
		description = "The event key for Lenny's Labyrinth submissions"
	)
	default String eventKey()
	{
		return "";
	}
}