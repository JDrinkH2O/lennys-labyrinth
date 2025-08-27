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

	@Inject
	private ApiClient apiClient;

	private JLabel titleLabel;
	private JPanel buttonPanel;
	private JLabel statusLabel;

	// Buttons for different states
	private JButton setEventKeyButton;
	private JButton unsetEventKeyButton;
	private JButton changeEventKeyButton;
	private JButton submitAnswerButton;

	public LennysLabyrinthPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title label
		titleLabel = new JLabel("Lenny's Labyrinth");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Button panel - will be dynamically populated
		buttonPanel = new JPanel();
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Status label
		statusLabel = new JLabel();
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Create all buttons
		createButtons();

		// Layout
		add(titleLabel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		// UI state will be initialized in initialize() after injection
	}

	private void createButtons()
	{
		setEventKeyButton = new JButton("Set Event Key");
		setEventKeyButton.addActionListener(this::onSetEventKeyClick);

		unsetEventKeyButton = new JButton("Unset Event Key");
		unsetEventKeyButton.addActionListener(this::onUnsetEventKeyClick);

		changeEventKeyButton = new JButton("Change Event Key");
		changeEventKeyButton.addActionListener(this::onChangeEventKeyClick);

		submitAnswerButton = new JButton("Submit Answer");
		submitAnswerButton.addActionListener(this::onSubmitAnswerClick);
	}

	@Inject
	public void initialize()
	{
		// Set up the bidirectional reference with the service
		gameStateService.setPanel(this);
		
		// Initialize UI state now that injection is complete
		updatePanelForEventKeyState();
	}

	public void onEventKeyChanged()
	{
		SwingUtilities.invokeLater(this::updatePanelForEventKeyState);
	}

	private void updatePanelForEventKeyState()
	{
		buttonPanel.removeAll();

		if (gameStateService.hasEventKey())
		{
			showEventKeySetState();
		}
		else
		{
			showNoEventKeyState();
		}

		buttonPanel.revalidate();
		buttonPanel.repaint();
	}

	private void showNoEventKeyState()
	{
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
		buttonPanel.add(setEventKeyButton);

		statusLabel.setText("<html><center>Set an event key to begin<br/>capturing game state</center></html>");
	}

	private void showEventKeySetState()
	{
		buttonPanel.setLayout(new GridLayout(3, 1, 0, 5));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		buttonPanel.add(unsetEventKeyButton);
		buttonPanel.add(changeEventKeyButton);
		buttonPanel.add(submitAnswerButton);

		String eventKey = gameStateService.getEventKey();
		statusLabel.setText("<html><center>Event: " + eventKey + "<br/>Ready to submit answers</center></html>");
	}

	private void onSetEventKeyClick(ActionEvent e)
	{
		showEventKeyDialog(null, false);
	}

	private void onChangeEventKeyClick(ActionEvent e)
	{
		showEventKeyDialog(gameStateService.getEventKey(), true);
	}

	private void onUnsetEventKeyClick(ActionEvent e)
	{
		gameStateService.clearEventKey();
	}

	private void onSubmitAnswerClick(ActionEvent e)
	{
		gameStateService.captureFromButton();
	}

	private void showEventKeyDialog(String currentEventKey, boolean isChanging)
	{
		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		EventKeyDialog dialog = new EventKeyDialog(
			parentFrame,
			currentEventKey,
			isChanging,
			eventKey -> gameStateService.setEventKey(eventKey),
			apiClient
		);

		dialog.setVisible(true);
	}

	public void updateStatusLabel(String text)
	{
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(text);
		});
	}
}