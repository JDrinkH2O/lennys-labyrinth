package com.lennyslabyrinth;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
public class GameStateService
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LennysLabyrinthConfig config;

	@Inject
	private ApiClient apiClient;

	@Inject
	private GameStateCapture gameStateCapture;

	private LennysLabyrinthPanel panel;

	public void setPanel(LennysLabyrinthPanel panel)
	{
		this.panel = panel;
	}

	private boolean isEventKeyValid()
	{
		return !config.eventKey().trim().isEmpty();
	}

	public void captureFromButton()
	{
		clientThread.invokeLater(() -> {
			if (!isEventKeyValid())
			{
				if (config.debug())
				{
					client.addChatMessage(
						ChatMessageType.GAMEMESSAGE,
						"",
						"[LL-debug] Game state capture skipped - Event Key is empty",
						null
					);
				}
				return;
			}
			
			String eventKey = config.eventKey().trim();
			captureGameState("button", "Manual submission via Submit Answer button", null, null, null, eventKey);
		});
	}

	public void captureFromAnimation(int animationId)
	{
		clientThread.invokeLater(() -> {
			if (!isEventKeyValid())
			{
				if (config.debug())
				{
					client.addChatMessage(
						ChatMessageType.GAMEMESSAGE,
						"",
						"[LL-debug] Game state capture skipped - Event Key is empty (Animation ID: " + animationId + ")",
						null
					);
				}
				return;
			}
			
			String triggerType = (animationId == 830) ? "dig" : "emote"; // 830 is AnimationID.DIG
			String eventKey = config.eventKey().trim();
			captureGameState(triggerType, "Animation ID: " + animationId, animationId, null, null, eventKey);
		});
	}

	public void captureFromNpcInteraction(int npcId, String interactionType)
	{
		clientThread.invokeLater(() -> {
			if (!isEventKeyValid())
			{
				if (config.debug())
				{
					client.addChatMessage(
						ChatMessageType.GAMEMESSAGE,
						"",
						"[LL-debug] Game state capture skipped - Event Key is empty (NPC ID: " + npcId + ", Action: " + interactionType + ")",
						null
					);
				}
				return;
			}
			
			String eventKey = config.eventKey().trim();
			captureGameState("npc_interaction", "NPC ID: " + npcId + ", Action: " + interactionType, null, npcId, interactionType, eventKey);
		});
	}

	private void captureGameState(String trigger, String additionalInfo, Integer emoteId, Integer npcId, String interactionType, String eventKey)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			WorldPoint worldLocation = player.getWorldLocation();
			LocalPoint localLocation = player.getLocalLocation();

			ApiClient.LocationData locationData = gameStateCapture.getLocationData(worldLocation, localLocation);
			List<ApiClient.ItemData> inventoryData = gameStateCapture.getInventoryData();
			List<ApiClient.ItemData> wornItemsData = gameStateCapture.getWornItemsData();
			
			ApiClient.GameStateSubmission gameStateSubmission = gameStateCapture.createGameStateSubmission(
				locationData, inventoryData, wornItemsData, emoteId, npcId, interactionType, eventKey);

			// Log submission info
			log.info("=== Lenny's Labyrinth Game State Submission ({}) ===", trigger);
			log.info("Trigger info: {}", additionalInfo);
			log.info("Event Key: {}, Inventory: {} items, Worn: {} items", 
				eventKey, inventoryData.size(), wornItemsData.size());

			// Update UI immediately
			if (panel != null)
			{
				panel.updateStatusLabel(String.format(
					"<html><center>Submitting...<br/>Trigger: %s<br/>%s<br/>Event: %s</center></html>",
					trigger,
					additionalInfo,
					eventKey
				));
			}

			// Submit to API
			CompletableFuture<ApiClient.ApiResponse> future = apiClient.submitGuess(gameStateSubmission);
			future.thenAccept(this::handleApiResponse);

			// Show initial message in chat (debug mode only)
			if (config.debug())
			{
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					"[LL-debug] Submitting guess for '" + eventKey + "'...",
					null
				);
			}
		}
		else
		{
			if (panel != null)
			{
				panel.updateStatusLabel("Player not found");
			}
		}
	}

	private void handleApiResponse(ApiClient.ApiResponse response)
	{
		// Update UI
		if (panel != null)
		{
			String statusText;
			if (response.success)
			{
				statusText = String.format(
					"<html><center><font color='green'>✓ CORRECT!</font><br/>%s</center></html>",
					response.message
				);
			}
			else
			{
				statusText = String.format(
					"<html><center><font color='red'>✗ %s</font><br/>%s</center></html>",
					response.success ? "SUCCESS" : "FAILED",
					response.message
				);
			}
			panel.updateStatusLabel(statusText);
		}

		// Add message to in-game chat on client thread
		clientThread.invokeLater(() -> {
			if (response.success)
			{
				// Always show success messages
				String chatMessage = "[Lenny's Labyrinth] ✓ " + response.message;
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					chatMessage,
					null
				);
			}
			else if (config.debug())
			{
				// Only show failure messages in debug mode
				String chatMessage = "[LL-debug] ✗ " + response.message;
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					chatMessage,
					null
				);
			}
		});
	}
}