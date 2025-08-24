package com.example;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@Singleton
public class ExamplePanel extends PluginPanel
{
	@Inject
	private Client client;

	private final JButton locationButton;
	private final JLabel statusLabel;

	public ExamplePanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Location Tracker");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		locationButton = new JButton("Get My Location");
		locationButton.addActionListener(this::onLocationButtonClick);

		statusLabel = new JLabel("Click button to get location");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		add(titleLabel, BorderLayout.NORTH);
		add(locationButton, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
	}

	private void onLocationButtonClick(ActionEvent e)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			WorldPoint worldLocation = player.getWorldLocation();
			LocalPoint localLocation = player.getLocalLocation();

			String locationText = String.format(
				"World: (%d, %d, %d) | Local: (%d, %d)",
				worldLocation.getX(),
				worldLocation.getY(),
				worldLocation.getPlane(),
				localLocation.getSceneX(),
				localLocation.getSceneY()
			);

			statusLabel.setText("<html><center>" + locationText + "</center></html>");

			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"Your location: " + locationText,
				null
			);
		}
		else
		{
			statusLabel.setText("Player not found");
		}
	}
}