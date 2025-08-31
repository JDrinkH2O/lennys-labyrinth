package com.lennyslabyrinth.dialogs;

import com.lennyslabyrinth.constraints.LocationConstraint;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class LocationConstraintDialog extends JDialog
{
	private JComboBox<String> typeCombo;
	private JTextField exactXField;
	private JTextField exactYField;
	private JTextField minXField;
	private JTextField maxXField;
	private JTextField minYField;
	private JTextField maxYField;
	private JTextField planeField;
	private JTextField toleranceField;
	
	private LocationConstraint constraint;
	private Consumer<LocationConstraint> onSave;
	private boolean cancelled = true;

	public LocationConstraintDialog(JFrame parent, LocationConstraint existing, Consumer<LocationConstraint> onSave)
	{
		super(parent, "Location Constraint", true);
		this.onSave = onSave;
		this.constraint = existing != null ? existing : new LocationConstraint("exact");
		
		initializeUI();
		populateFields();
		pack();
		setLocationRelativeTo(parent);
	}

	private void initializeUI()
	{
		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Type selection
		JPanel typePanel = new JPanel(new FlowLayout());
		typePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		typePanel.add(new JLabel("Type:"));
		typeCombo = new JComboBox<>(new String[]{"exact", "bounds", "tolerance"});
		typeCombo.addActionListener(this::onTypeChanged);
		typePanel.add(typeCombo);

		// Fields panel
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		fieldsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		// Create fields
		exactXField = createNumberField();
		exactYField = createNumberField();
		minXField = createNumberField();
		maxXField = createNumberField();
		minYField = createNumberField();
		maxYField = createNumberField();
		planeField = createNumberField();
		toleranceField = createNumberField();

		// Add fields with labels
		addField(fieldsPanel, gbc, 0, "Exact X:", exactXField);
		addField(fieldsPanel, gbc, 1, "Exact Y:", exactYField);
		addField(fieldsPanel, gbc, 2, "Min X:", minXField);
		addField(fieldsPanel, gbc, 3, "Max X:", maxXField);
		addField(fieldsPanel, gbc, 4, "Min Y:", minYField);
		addField(fieldsPanel, gbc, 5, "Max Y:", maxYField);
		addField(fieldsPanel, gbc, 6, "Plane:", planeField);
		addField(fieldsPanel, gbc, 7, "Tolerance:", toleranceField);

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");
		
		saveButton.addActionListener(this::onSave);
		cancelButton.addActionListener(this::onCancel);
		
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		add(typePanel, BorderLayout.NORTH);
		add(fieldsPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JTextField createNumberField()
	{
		JTextField field = new JTextField(10);
		field.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		field.setForeground(Color.WHITE);
		return field;
	}

	private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field)
	{
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.WEST;
		JLabel labelComp = new JLabel(label);
		labelComp.setForeground(Color.WHITE);
		panel.add(labelComp, gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(field, gbc);
	}

	private void populateFields()
	{
		typeCombo.setSelectedItem(constraint.getType());
		setIntegerField(exactXField, constraint.getExactX());
		setIntegerField(exactYField, constraint.getExactY());
		setIntegerField(minXField, constraint.getMinX());
		setIntegerField(maxXField, constraint.getMaxX());
		setIntegerField(minYField, constraint.getMinY());
		setIntegerField(maxYField, constraint.getMaxY());
		setIntegerField(planeField, constraint.getPlane());
		setIntegerField(toleranceField, constraint.getTolerance());
		updateFieldsVisibility();
	}

	private void setIntegerField(JTextField field, Integer value)
	{
		field.setText(value != null ? value.toString() : "");
	}

	private void onTypeChanged(ActionEvent e)
	{
		updateFieldsVisibility();
	}

	private void updateFieldsVisibility()
	{
		String selectedType = (String) typeCombo.getSelectedItem();
		
		// Reset all fields
		exactXField.setEnabled(false);
		exactYField.setEnabled(false);
		minXField.setEnabled(false);
		maxXField.setEnabled(false);
		minYField.setEnabled(false);
		maxYField.setEnabled(false);
		toleranceField.setEnabled(false);

		switch (selectedType)
		{
			case "exact":
				exactXField.setEnabled(true);
				exactYField.setEnabled(true);
				break;
			case "bounds":
				minXField.setEnabled(true);
				maxXField.setEnabled(true);
				minYField.setEnabled(true);
				maxYField.setEnabled(true);
				break;
			case "tolerance":
				exactXField.setEnabled(true);
				exactYField.setEnabled(true);
				toleranceField.setEnabled(true);
				break;
		}
		
		// Plane is always available
		planeField.setEnabled(true);
	}

	private void onSave(ActionEvent e)
	{
		try
		{
			constraint.setType((String) typeCombo.getSelectedItem());
			constraint.setExactX(parseInteger(exactXField.getText()));
			constraint.setExactY(parseInteger(exactYField.getText()));
			constraint.setMinX(parseInteger(minXField.getText()));
			constraint.setMaxX(parseInteger(maxXField.getText()));
			constraint.setMinY(parseInteger(minYField.getText()));
			constraint.setMaxY(parseInteger(maxYField.getText()));
			constraint.setPlane(parseInteger(planeField.getText()));
			constraint.setTolerance(parseInteger(toleranceField.getText()));

			cancelled = false;
			onSave.accept(constraint);
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
		dispose();
	}

	private Integer parseInteger(String text)
	{
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
}