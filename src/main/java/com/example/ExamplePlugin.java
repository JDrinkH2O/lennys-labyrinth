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
		
		// Only track local player animations
		if (player != client.getLocalPlayer())
		{
			return;
		}

		int animationId = player.getAnimation();
		
		// Debug mode: show all animation IDs in chat
		if (config.debug())
		{
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"[DEBUG] Animation ID: " + animationId,
				null
			);
		}
		
		// Check for trigger animations
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
		// Emote animation IDs and other trigger animations
		switch (animationId)
		{
			// Basic emotes (confirmed)
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
			case 1128: // Joy (Jump for Joy)
			case 1129: // Yawn
			case 1130: // Spin
			case 1131: // Shrug
			case 2105: // Salute
			case 2127: // Goblin bow
			case 2128: // Goblin salute
			case 2108: // Glass box
			case 2109: // Climb rope
			case 2110: // Lean
			case 2111: // Glass wall
			case 1374: // Blow Kiss
			case 3544: // Zombie Walk
			case 6111: // Rabbit Hop
			
			// Additional emotes (educated guesses - may need testing)
			case 1132: // Jig (estimated)
			case 2104: // Headbang (estimated)
			case 2112: // Panic (estimated)
			case 2113: // Raspberry (estimated)
			case 3920: // Premier Shield (estimated)
			case 1200: // Sit down (estimated)
			case 1133: // Flex (estimated)
			case 3545: // Zombie Dance (estimated)
			case 1201: // Sit up (estimated)
			case 1202: // Push up (estimated)
			case 1203: // Star jump (estimated)
			case 1204: // Jog (estimated)
			case 7535: // Air Guitar (estimated)
			case 8118: // Uri transform (estimated)
			case 8524: // Explore (estimated)
			case 9990: // Fortis Salute (estimated - newest)
			case 4275: // Idea (estimated)
			case 4276: // Stamp (estimated)
			case 4277: // Flap (estimated)
			case 4278: // Slap Head (estimated)
			case 3867: // Scared (estimated)
			case 3546: // Zombie Hand (estimated)
			case 7929: // Hypermobile Drinker (estimated)
			case 7930: // Smooth dance (estimated)
			case 7931: // Crazy dance (estimated)
			case 7932: // Party (estimated)
			case 7933: // Trick (estimated)
			
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
