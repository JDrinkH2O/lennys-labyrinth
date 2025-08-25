package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Lenny's Labyrinth"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ExamplePanel panel;

	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Lenny's Labyrinth started!");

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/util/clue_arrow.png");

		navButton = NavigationButton.builder()
			.tooltip("Lenny's Labyrinth")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Lenny's Labyrinth stopped!");
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// No chat message on login
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		// Only process player animations
		if (!(event.getActor() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getActor();
		
		// Only track local player emotes
		if (player != client.getLocalPlayer())
		{
			return;
		}

		int animationId = player.getAnimation();
		if (isTriggerAnimation(animationId))
		{
			String triggerType = (animationId == AnimationID.DIG) ? "dig" : "emote";
			log.info("{} detected with animation ID: {}", triggerType, animationId);
			panel.captureGameStateFromAnimation(animationId);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		MenuAction action = event.getMenuAction();
		
		// Only capture NPC interactions
		switch (action)
		{
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
				int npcId = event.getId();
				String menuOption = event.getMenuOption();
				
				log.info("NPC interaction detected: {} (ID: {}) - {}", event.getMenuTarget(), npcId, menuOption);
				panel.captureGameStateFromNpcInteraction(npcId, menuOption);
				break;
			default:
				// Ignore all other menu actions (walk, objects, widgets, etc.)
				break;
		}
	}

	private boolean isTriggerAnimation(int animationId)
	{
		// Common emote animation IDs and other trigger animations
		switch (animationId)
		{
			// Basic emotes
			case 855: // Yes
			case 856: // No
			case 857: // Thinking
			case 858: // Bow
			case 859: // Angry
			case 860: // Cry
			case 861: // Laugh
			case 862: // Cheer
			case 863: // Wave
			case 864: // Beckon
			case 865: // Clap
			case 866: // Dance
			case 1128: // Joy
			case 1129: // Yawn
			case 1130: // Spin
			case 1131: // Shrug
			case 2105: // Salute
			case 2106: // Goblin bow
			case 2107: // Goblin salute
			case 2108: // Glass box
			case 2109: // Climb rope
			case 2110: // Lean
			case 2111: // Glass wall
			// Tool actions
			case AnimationID.DIG: // Digging with spade (830)
				return true;
			default:
				return false;
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
