package com.lennyslabyrinth;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@Singleton
public class LennysLabyrinthPanel extends PluginPanel
{
	@Inject
	private GameStateService gameStateService;

	private final JButton locationButton;
	private final JLabel statusLabel;

	public LennysLabyrinthPanel()
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

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonPanel.add(locationButton);

		add(titleLabel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
	}

	@Inject
	public void initialize()
	{
		// Set up the bidirectional reference with the service
		gameStateService.setPanel(this);
	}

	private void onLocationButtonClick(ActionEvent e)
	{
		gameStateService.captureFromButton();
	}

	public void updateStatusLabel(String text)
	{
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(text);
		});
	}
}