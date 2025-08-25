package com.example;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
public class ExamplePanel extends PluginPanel
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private final JButton locationButton;
	private final JLabel statusLabel;

	public ExamplePanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Lenny's Labyrinth");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		locationButton = new JButton("Submit Answer");
		locationButton.addActionListener(this::onLocationButtonClick);

		statusLabel = new JLabel("Click the button to submit a guess based on current gamestate.");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		add(titleLabel, BorderLayout.NORTH);
		add(locationButton, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
	}

	private void onLocationButtonClick(ActionEvent e)
	{
		clientThread.invokeLater(() -> {
			captureGameState("button", "Manual submission via Submit Answer button", null, null, null);
		});
	}

	private List<Map<String, Object>> getInventoryData()
	{
		List<Map<String, Object>> inventoryItems = new ArrayList<>();
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

		if (inventory == null)
		{
			return inventoryItems;
		}

		Item[] items = inventory.getItems();
		for (int i = 0; i < items.length; i++)
		{
			Item item = items[i];
			if (item != null && item.getId() != -1)
			{
				Map<String, Object> itemData = new HashMap<>();
				itemData.put("slot", i);
				itemData.put("id", item.getId());
				itemData.put("quantity", item.getQuantity());
				inventoryItems.add(itemData);
			}
		}

		return inventoryItems;
	}

	private List<Map<String, Object>> getWornItemsData()
	{
		List<Map<String, Object>> wornItems = new ArrayList<>();
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment == null)
		{
			return wornItems;
		}

		Item[] items = equipment.getItems();
		for (int i = 0; i < items.length; i++)
		{
			Item item = items[i];
			if (item != null && item.getId() != -1)
			{
				Map<String, Object> itemData = new HashMap<>();
				itemData.put("slot", i);
				itemData.put("id", item.getId());
				itemData.put("quantity", item.getQuantity());
				wornItems.add(itemData);
			}
		}

		return wornItems;
	}

	private Map<String, Object> getLocationData(WorldPoint worldLocation, LocalPoint localLocation)
	{
		Map<String, Object> locationData = new HashMap<>();
		
		Map<String, Object> worldCoords = new HashMap<>();
		worldCoords.put("x", worldLocation.getX());
		worldCoords.put("y", worldLocation.getY());
		worldCoords.put("plane", worldLocation.getPlane());
		
		Map<String, Object> localCoords = new HashMap<>();
		localCoords.put("sceneX", localLocation.getSceneX());
		localCoords.put("sceneY", localLocation.getSceneY());
		
		locationData.put("world", worldCoords);
		locationData.put("local", localCoords);
		
		return locationData;
	}

	private Map<String, Object> createGameStateJson(Map<String, Object> locationData, 
		List<Map<String, Object>> inventoryData, List<Map<String, Object>> wornItemsData, 
		Integer emoteId, Integer npcId, String interactionType)
	{
		Map<String, Object> gameState = new HashMap<>();
		gameState.put("location", locationData);
		gameState.put("inventory", inventoryData);
		gameState.put("worn_items", wornItemsData);
		gameState.put("emote_id", emoteId);
		gameState.put("npc_id", npcId);
		gameState.put("interaction_type", interactionType);
		return gameState;
	}

	public void captureGameStateFromAnimation(int animationId)
	{
		clientThread.invokeLater(() -> {
			String triggerType = (animationId == 830) ? "dig" : "emote"; // 830 is AnimationID.DIG
			captureGameState(triggerType, "Animation ID: " + animationId, animationId, null, null);
		});
	}

	public void captureGameStateFromNpcInteraction(int npcId, String interactionType)
	{
		clientThread.invokeLater(() -> {
			captureGameState("npc_interaction", "NPC ID: " + npcId + ", Action: " + interactionType, null, npcId, interactionType);
		});
	}

	private void captureGameState(String trigger, String additionalInfo, Integer emoteId, Integer npcId, String interactionType)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			WorldPoint worldLocation = player.getWorldLocation();
			LocalPoint localLocation = player.getLocalLocation();

			Map<String, Object> locationData = getLocationData(worldLocation, localLocation);
			List<Map<String, Object>> inventoryData = getInventoryData();
			List<Map<String, Object>> wornItemsData = getWornItemsData();
			
			Map<String, Object> gameStateJson = createGameStateJson(locationData, inventoryData, wornItemsData, emoteId, npcId, interactionType);

			// Log JSON data to console with trigger information
			log.info("=== Lenny's Labyrinth Game State JSON ({}) ===", trigger);
			log.info("Trigger info: {}", additionalInfo);
			log.info("{}", gameStateJson);

			// Update side panel with trigger information
			SwingUtilities.invokeLater(() -> {
				String detailedText = String.format(
					"<html><center>Trigger: %s<br/>%s<br/>Inventory: %d items<br/>Worn: %d items<br/>JSON: %s</center></html>",
					trigger,
					additionalInfo,
					inventoryData.size(),
					wornItemsData.size(),
					gameStateJson.toString()
				);
				statusLabel.setText(detailedText);
			});

			// Show trigger-specific message in chat
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"Game state captured (" + trigger + ")",
				null
			);
		}
		else
		{
			SwingUtilities.invokeLater(() -> {
				statusLabel.setText("Player not found");
			});
		}
	}
}