package com.lennyslabyrinth;

import com.lennyslabyrinth.constraints.*;
import com.lennyslabyrinth.dialogs.LocationConstraintDialog;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

@Singleton
public class AnswerBuilderPanel extends PluginPanel
{
	@Inject
	private GameStateService gameStateService;

	private AnswerBuilder answerBuilder;
	private JLabel titleLabel;
	private JTextArea rewardTextArea;
	private JPanel constraintsPanel;
	private JPanel buttonPanel;
	private JLabel statusLabel;
	private JScrollPane constraintsScrollPane;

	// Buttons
	private JButton addLocationButton;
	private JButton addInventoryButton;
	private JButton addEquipmentButton;
	private JButton addActionButton;
	private JButton addEventKeyButton;
	private JButton testAnswerButton;
	private JButton clearAnswerButton;

	public AnswerBuilderPanel()
	{
		this.answerBuilder = new AnswerBuilder();
		
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		titleLabel = new JLabel("Answer Builder");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeFont());
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Main content panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Reward text area
		JPanel rewardPanel = new JPanel(new BorderLayout());
		rewardPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		rewardPanel.setBorder(BorderFactory.createTitledBorder("Reward Text"));

		rewardTextArea = new JTextArea(2, 20);
		rewardTextArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rewardTextArea.setForeground(Color.WHITE);
		rewardTextArea.setLineWrap(true);
		rewardTextArea.setWrapStyleWord(true);
		JScrollPane rewardScrollPane = new JScrollPane(rewardTextArea);
		rewardPanel.add(rewardScrollPane, BorderLayout.CENTER);

		// Constraints panel
		constraintsPanel = new JPanel();
		constraintsPanel.setLayout(new BoxLayout(constraintsPanel, BoxLayout.Y_AXIS));
		constraintsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		constraintsScrollPane = new JScrollPane(constraintsPanel);
		constraintsScrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		constraintsScrollPane.setBorder(BorderFactory.createTitledBorder("Constraints"));
		constraintsScrollPane.setPreferredSize(new Dimension(300, 200));

		// Button panel
		buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		createButtons();

		// Status label
		statusLabel = new JLabel("Ready to build answer");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Layout
		mainPanel.add(rewardPanel, BorderLayout.NORTH);
		mainPanel.add(constraintsScrollPane, BorderLayout.CENTER);

		add(titleLabel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);
		add(statusLabel, BorderLayout.SOUTH);

		updateConstraintsDisplay();
	}

	private void createButtons()
	{
		addLocationButton = new JButton("+ Location");
		addLocationButton.addActionListener(this::onAddLocationConstraint);

		addInventoryButton = new JButton("+ Inventory");
		addInventoryButton.addActionListener(this::onAddInventoryConstraint);

		addEquipmentButton = new JButton("+ Equipment");
		addEquipmentButton.addActionListener(this::onAddEquipmentConstraint);

		addActionButton = new JButton("+ Action");
		addActionButton.addActionListener(this::onAddActionConstraint);

		addEventKeyButton = new JButton("+ Event Key");
		addEventKeyButton.addActionListener(this::onAddEventKeyConstraint);

		testAnswerButton = new JButton("Test Answer");
		testAnswerButton.addActionListener(this::onTestAnswer);

		clearAnswerButton = new JButton("Clear All");
		clearAnswerButton.addActionListener(this::onClearAnswer);

		buttonPanel.add(addLocationButton);
		buttonPanel.add(addInventoryButton);
		buttonPanel.add(addEquipmentButton);
		buttonPanel.add(addActionButton);
		buttonPanel.add(addEventKeyButton);
		buttonPanel.add(new JSeparator());
		buttonPanel.add(testAnswerButton);
		buttonPanel.add(clearAnswerButton);
	}

	@Inject
	public void initialize()
	{
		updateConstraintsDisplay();
	}

	private void onAddLocationConstraint(ActionEvent e)
	{
		// Find the parent frame
		Window parentWindow = SwingUtilities.getWindowAncestor(this);
		JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

		LocationConstraintDialog dialog = new LocationConstraintDialog(
			parentFrame, 
			null, 
			constraint -> {
				answerBuilder.addConstraint(constraint);
				updateConstraintsDisplay();
				statusLabel.setText("Added location constraint");
			}
		);
		
		dialog.setVisible(true);
	}

	private void onAddInventoryConstraint(ActionEvent e)
	{
		InventoryConstraint constraint = new InventoryConstraint("contains");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added inventory constraint");
	}

	private void onAddEquipmentConstraint(ActionEvent e)
	{
		EquipmentConstraint constraint = new EquipmentConstraint("contains");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added equipment constraint");
	}

	private void onAddActionConstraint(ActionEvent e)
	{
		ActionConstraint constraint = new ActionConstraint("emote");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added action constraint");
	}

	private void onAddEventKeyConstraint(ActionEvent e)
	{
		EventKeyConstraint constraint = new EventKeyConstraint("exact");
		// TODO: Show dialog to configure constraint
		answerBuilder.addConstraint(constraint);
		updateConstraintsDisplay();
		statusLabel.setText("Added event key constraint");
	}

	private void onTestAnswer(ActionEvent e)
	{
		// Test by submitting current game state to API
		// The API will validate against the answer being built
		if (gameStateService != null)
		{
			gameStateService.captureFromButton();
		}
	}

	private void onClearAnswer(ActionEvent e)
	{
		answerBuilder.clear();
		rewardTextArea.setText("");
		updateConstraintsDisplay();
		statusLabel.setText("Answer cleared");
	}

	private void updateConstraintsDisplay()
	{
		constraintsPanel.removeAll();
		
		if (answerBuilder.getConstraintCount() == 0)
		{
			JLabel emptyLabel = new JLabel("No constraints added");
			emptyLabel.setForeground(Color.GRAY);
			constraintsPanel.add(emptyLabel);
		}
		else
		{
			for (int i = 0; i < answerBuilder.getConstraintCount(); i++)
			{
				Constraint constraint = answerBuilder.getConstraint(i);
				JPanel constraintPanel = createConstraintPanel(constraint, i);
				constraintsPanel.add(constraintPanel);
				constraintsPanel.add(Box.createVerticalStrut(5));
			}
		}

		constraintsPanel.revalidate();
		constraintsPanel.repaint();
	}

	private JPanel createConstraintPanel(Constraint constraint, int index)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel typeLabel = new JLabel(constraint.getConstraintType().toUpperCase());
		typeLabel.setForeground(Color.CYAN);
		typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD));

		JLabel descLabel = new JLabel(constraint.description());
		descLabel.setForeground(Color.WHITE);

		JButton removeButton = new JButton("X");
		removeButton.setPreferredSize(new Dimension(25, 25));
		removeButton.addActionListener(e -> {
			answerBuilder.removeConstraint(index);
			updateConstraintsDisplay();
			statusLabel.setText("Constraint removed");
		});

		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		infoPanel.add(typeLabel, BorderLayout.NORTH);
		infoPanel.add(descLabel, BorderLayout.CENTER);

		panel.add(infoPanel, BorderLayout.CENTER);
		panel.add(removeButton, BorderLayout.EAST);

		return panel;
	}

	public AnswerBuilder getAnswerBuilder()
	{
		return answerBuilder;
	}

	public void updateStatusLabel(String text)
	{
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(text);
		});
	}
}