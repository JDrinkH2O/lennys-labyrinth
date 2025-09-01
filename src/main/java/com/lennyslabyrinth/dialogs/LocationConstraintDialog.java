package com.lennyslabyrinth.dialogs;

import com.lennyslabyrinth.constraints.LocationConstraint;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LocationConstraintDialog extends JDialog
{
	private JComboBox<String> typeCombo;
	private JPanel dynamicFieldsPanel;
	private Map<String, JTextField> fields;
	private Map<String, JLabel> labels;
	private JLabel coordinatesLabel;
	private JButton useCurrentLocationButton;
	private Timer coordinateUpdateTimer;
	
	private LocationConstraint constraint;
	private Consumer<LocationConstraint> onSave;
	private Client client;
	private boolean cancelled = true;

	public LocationConstraintDialog(JFrame parent, LocationConstraint existing, Consumer<LocationConstraint> onSave, Client client)
	{
		super(parent, "Location Constraint", false); // false = non-modal
		this.onSave = onSave;
		this.client = client;
		this.constraint = existing != null ? existing : new LocationConstraint("exact");
		this.fields = new HashMap<>();
		this.labels = new HashMap<>();
		
		initializeUI();
		populateFields();
		startCoordinateUpdates();
		pack();
		setLocationRelativeTo(parent);
		setAlwaysOnTop(true); // Keep dialog visible above game client
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Top panel with type selection and coordinates
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Type selection
		JPanel typePanel = new JPanel(new FlowLayout());
		typePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JLabel typeLabel = new JLabel("Type:");
		typeLabel.setForeground(Color.WHITE);
		typePanel.add(typeLabel);
		typeCombo = new JComboBox<>(new String[]{"exact", "bounds", "tolerance"});
		typeCombo.addActionListener(this::onTypeChanged);
		typePanel.add(typeCombo);

		// Coordinates display
		JPanel coordinatesPanel = new JPanel(new FlowLayout());
		coordinatesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		coordinatesLabel = new JLabel("Current Location: Loading...");
		coordinatesLabel.setForeground(Color.CYAN);
		coordinatesLabel.setFont(coordinatesLabel.getFont().deriveFont(Font.BOLD));
		coordinatesPanel.add(coordinatesLabel);

		topPanel.add(typePanel, BorderLayout.NORTH);
		topPanel.add(coordinatesPanel, BorderLayout.SOUTH);

		// Dynamic fields panel
		dynamicFieldsPanel = new JPanel(new GridBagLayout());
		dynamicFieldsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Create all possible fields
		createFields();

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");
		
		saveButton.addActionListener(this::onSave);
		cancelButton.addActionListener(this::onCancel);
		
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		add(topPanel, BorderLayout.NORTH);
		add(dynamicFieldsPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void createFields()
	{
		// Create all possible fields
		fields.put("exactX", createNumberField());
		fields.put("exactY", createNumberField());
		fields.put("minX", createNumberField());
		fields.put("maxX", createNumberField());
		fields.put("minY", createNumberField());
		fields.put("maxY", createNumberField());
		fields.put("plane", createNumberField());
		fields.put("tolerance", createNumberField());

		// Create corresponding labels
		labels.put("exactX", createLabel("Exact X:"));
		labels.put("exactY", createLabel("Exact Y:"));
		labels.put("minX", createLabel("Min X:"));
		labels.put("maxX", createLabel("Max X:"));
		labels.put("minY", createLabel("Min Y:"));
		labels.put("maxY", createLabel("Max Y:"));
		labels.put("plane", createLabel("Plane (optional):"));
		labels.put("tolerance", createLabel("Tolerance:"));
	}

	private JTextField createNumberField()
	{
		JTextField field = new JTextField(20); // Doubled from 10 to 20
		field.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		field.setForeground(Color.WHITE);
		return field;
	}

	private JLabel createLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(Color.WHITE);
		return label;
	}

	private void populateFields()
	{
		typeCombo.setSelectedItem(constraint.getType());
		setIntegerField("exactX", constraint.getExactX());
		setIntegerField("exactY", constraint.getExactY());
		setIntegerField("minX", constraint.getMinX());
		setIntegerField("maxX", constraint.getMaxX());
		setIntegerField("minY", constraint.getMinY());
		setIntegerField("maxY", constraint.getMaxY());
		setIntegerField("plane", constraint.getPlane());
		setIntegerField("tolerance", constraint.getTolerance());
		updateFieldsVisibility();
	}

	private void setIntegerField(String fieldName, Integer value)
	{
		JTextField field = fields.get(fieldName);
		if (field != null)
		{
			field.setText(value != null ? value.toString() : "");
		}
	}

	private void onTypeChanged(ActionEvent e)
	{
		updateFieldsVisibility();
		pack(); // Resize dialog to fit new content
	}

	private void updateFieldsVisibility()
	{
		String selectedType = (String) typeCombo.getSelectedItem();
		
		// Clear the panel
		dynamicFieldsPanel.removeAll();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		
		int row = 0;
		
		// Add fields based on selected type
		switch (selectedType)
		{
			case "exact":
				addFieldToPanel(gbc, row++, "exactX");
				addFieldToPanel(gbc, row++, "exactY");
				break;
				
			case "bounds":
				addFieldToPanel(gbc, row++, "minX");
				addFieldToPanel(gbc, row++, "maxX");
				addFieldToPanel(gbc, row++, "minY");
				addFieldToPanel(gbc, row++, "maxY");
				break;
				
			case "tolerance":
				addFieldToPanel(gbc, row++, "exactX");
				addFieldToPanel(gbc, row++, "exactY");
				addFieldToPanel(gbc, row++, "tolerance");
				break;
		}
		
		// Always add plane field (optional for all types)
		addFieldToPanel(gbc, row++, "plane");
		
		// Add "Use Current Location" button for exact and tolerance types
		if ("exact".equals(selectedType) || "tolerance".equals(selectedType))
		{
			useCurrentLocationButton = new JButton("Use Current Location");
			useCurrentLocationButton.addActionListener(this::onUseCurrentLocation);
			
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(10, 5, 5, 5);
			dynamicFieldsPanel.add(useCurrentLocationButton, gbc);
			gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
		}
		
		// Refresh the panel
		dynamicFieldsPanel.revalidate();
		dynamicFieldsPanel.repaint();
	}

	private void addFieldToPanel(GridBagConstraints gbc, int row, String fieldName)
	{
		JLabel label = labels.get(fieldName);
		JTextField field = fields.get(fieldName);
		
		if (label != null && field != null)
		{
			// Add label
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.fill = GridBagConstraints.NONE;
			dynamicFieldsPanel.add(label, gbc);
			
			// Add field
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			dynamicFieldsPanel.add(field, gbc);
			gbc.weightx = 0.0;
		}
	}

	private void onSave(ActionEvent e)
	{
		try
		{
			constraint.setType((String) typeCombo.getSelectedItem());
			constraint.setExactX(parseInteger("exactX"));
			constraint.setExactY(parseInteger("exactY"));
			constraint.setMinX(parseInteger("minX"));
			constraint.setMaxX(parseInteger("maxX"));
			constraint.setMinY(parseInteger("minY"));
			constraint.setMaxY(parseInteger("maxY"));
			constraint.setPlane(parseInteger("plane"));
			constraint.setTolerance(parseInteger("tolerance"));

			cancelled = false;
			onSave.accept(constraint);
			stopCoordinateUpdates();
			dispose();
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onCancel(ActionEvent e)
	{
		cancelled = true;
		stopCoordinateUpdates();
		dispose();
	}

	private void onUseCurrentLocation(ActionEvent e)
	{
		WorldPoint currentLocation = getCurrentPlayerLocation();
		if (currentLocation != null)
		{
			JTextField exactXField = fields.get("exactX");
			JTextField exactYField = fields.get("exactY");
			JTextField planeField = fields.get("plane");
			
			if (exactXField != null) exactXField.setText(String.valueOf(currentLocation.getX()));
			if (exactYField != null) exactYField.setText(String.valueOf(currentLocation.getY()));
			if (planeField != null) planeField.setText(String.valueOf(currentLocation.getPlane()));
		}
	}

	private void startCoordinateUpdates()
	{
		// Update coordinates every 500ms
		coordinateUpdateTimer = new Timer(500, e -> updateCoordinatesDisplay());
		coordinateUpdateTimer.start();
		updateCoordinatesDisplay(); // Initial update
	}

	private void stopCoordinateUpdates()
	{
		if (coordinateUpdateTimer != null)
		{
			coordinateUpdateTimer.stop();
		}
	}

	private void updateCoordinatesDisplay()
	{
		SwingUtilities.invokeLater(() -> {
			WorldPoint location = getCurrentPlayerLocation();
			if (location != null)
			{
				coordinatesLabel.setText(String.format("Current Location: X:%d Y:%d Plane:%d", 
					location.getX(), location.getY(), location.getPlane()));
			}
			else
			{
				coordinatesLabel.setText("Current Location: Not available");
			}
		});
	}

	private WorldPoint getCurrentPlayerLocation()
	{
		if (client == null) return null;
		
		Player player = client.getLocalPlayer();
		if (player == null) return null;
		
		return player.getWorldLocation();
	}

	private Integer parseInteger(String fieldName)
	{
		JTextField field = fields.get(fieldName);
		if (field == null)
		{
			return null;
		}
		
		String text = field.getText();
		if (text == null || text.trim().isEmpty())
		{
			return null;
		}
		return Integer.parseInt(text.trim());
	}

	public boolean wasCancelled()
	{
		return cancelled;
	}

	@Override
	public void dispose()
	{
		stopCoordinateUpdates();
		super.dispose();
	}
}