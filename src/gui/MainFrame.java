package gui;

import dispatch.DispatchManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private final DispatchManager manager;
    private final Controller controller;

    private JTextArea output_area;
    private JComboBox<String> unitTypeBox;
    private JTextField unitId;
    private JTextField unitSpeed;
    private JTextField unitTraffic;
    private JTextField unitName;

    private JComboBox<String> incidentTypeBox;
    private JTextField incidentId;
    private JTextField incidentDescription;
    private JTextField incidentDistance;
    private JSlider incidentSeverity;
    private JLabel severityLabel;
    private JLabel extra1Label;
    private JLabel extra2Label;
    private JTextField extra1Field;
    private JTextField extra2Field;
    private JCheckBox flagBox;

    private JTextField dispatchIdField;
    private String selectedStatus = "OPEN";

    private StatusPanel statusPanel;

    private JTextField prefixField;

    public MainFrame(DispatchManager manager){
        super("Emergency Response Management system");
        this.manager = manager;
        this.controller = new Controller(manager);


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        setJMenuBar(buildMenuBar());
        add(buildForm(), BorderLayout.WEST);
        add(buildOperation(), BorderLayout.NORTH);
        add(builtOutput(), BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadItem.addActionListener(event -> loadFromMenu());
        saveItem.addActionListener(event -> saveFromMenu());
        exitItem.addActionListener(event -> System.exit(0));

        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private String readPrefix() {
        String prefix = prefixField.getText().trim();
        if (prefix.isEmpty()) {
            throw new IllegalArgumentException("Enter a file prefix first.");
        }
        return prefix;
    }

    private void saveFromMenu() {
        try {
            appendOutput(controller.save(readPrefix()) + "\n");
        } catch (IllegalArgumentException e) {
            appendOutput("Input error: " + e.getMessage() + "\n");
        } catch (Exception e) {
            appendOutput("Error: " + e.getMessage() + "\n");
        }
    }

    private void loadFromMenu() {
        try {
            String message = controller.load(readPrefix());
            showText(message + "\n" + controller.listAllUnits() + "\n" + controller.listAllIncidents());
            refreshView();
        } catch (IllegalArgumentException e) {
            appendOutput("Input error: " + e.getMessage() + "\n");
        } catch (Exception e) {
            appendOutput("Error: " + e.getMessage() + "\n");
        }
    }

    private JPanel buildForm() {
        JPanel form_unit_incident = new JPanel(new GridLayout(2,1,5,5));
        form_unit_incident.add(buildUnitForm());
        form_unit_incident.add(buildIncidentForm());
        return  form_unit_incident;
    }

    private JPanel buildUnitForm(){
        JPanel unit_form = new JPanel(new GridLayout(6,2,5,5));
        unit_form.setBorder(BorderFactory.createTitledBorder("Add Response Unit"));

        unitTypeBox = new JComboBox<>(new String[]{
            "AMBULANCE", "FIRE_ENGINE", "REPAIR_VAN", "SEARCH_DRONE", "HAZMAT_UNIT"
        });
        unitId = new JTextField(10);
        unitName = new JTextField(10);
        unitSpeed = new JTextField(10);
        unitTraffic = new JTextField("1.2",10);

        unit_form.add(new JLabel("Type: "));
        unit_form.add(unitTypeBox);
        unit_form.add(new JLabel("ID:"));
        unit_form.add(unitId);
        unit_form.add(new JLabel("Name:"));
        unit_form.add(unitName);
        unit_form.add(new JLabel("Max speed:"));
        unit_form.add(unitSpeed);
        unit_form.add(new JLabel("Traffic factor:"));
        unit_form.add(unitTraffic);

        JButton addButton = new JButton("Add Unit");
        unit_form.add(new JLabel(""));
        unit_form.add(addButton);
        unitTypeBox.addActionListener(new UnitTypeListener());
        addButton.addActionListener(new AddUnitListener());
        return unit_form;
    }

    private JPanel buildIncidentForm(){
        JPanel incident_form = new JPanel(new GridLayout(9, 2, 5, 5));
        incident_form.setBorder(BorderFactory.createTitledBorder("Report new Incident"));

        incidentTypeBox = new JComboBox<>(new String[] {
                "MEDICAL", "FIRE", "INFRASTRUCTURE", "SEARCH", "HAZMAT"
        });
        incidentId = new JTextField(10);
        incidentDescription = new JTextField(10);
        incidentDistance = new JTextField(10);

        incidentSeverity = new JSlider(1, 5, 3);
        incidentSeverity.setMajorTickSpacing(1);
        incidentSeverity.setPaintTicks(true);
        incidentSeverity.setPaintLabels(true);
        incidentSeverity.setSnapToTicks(true);
        severityLabel = new JLabel("Severity: 3");

        extra1Label = new JLabel();
        extra2Label = new JLabel();
        extra1Field = new JTextField(10);
        extra2Field = new JTextField(10);
        flagBox = new JCheckBox();

        incident_form.add(new JLabel("Type:"));
        incident_form.add(incidentTypeBox);
        incident_form.add(new JLabel("ID:"));
        incident_form.add(incidentId);
        incident_form.add(new JLabel("Description:"));
        incident_form.add(incidentDescription);
        incident_form.add(new JLabel("Distance from base:"));
        incident_form.add(incidentDistance);
        incident_form.add(severityLabel);
        incident_form.add(incidentSeverity);
        incident_form.add(extra1Label);
        incident_form.add(extra1Field);
        incident_form.add(extra2Label);
        incident_form.add(extra2Field);
        incident_form.add(new JLabel(""));
        incident_form.add(flagBox);

        JButton reportButton = new JButton("Report Incident");
        incident_form.add(new JLabel(""));
        incident_form.add(reportButton);

        incidentTypeBox.addActionListener(new IncidentTypeListener());
        incidentSeverity.addChangeListener(new SeverityListener());
        reportButton.addActionListener(new ReportIncidentListener());

        updateIncidentFields();
        return incident_form;
    }

    private JPanel buildOperation(){
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Operations"));
        panel.add(buildViewRow());
        panel.add(buildStatusRow());
        panel.add(buildDispatchRow());
        panel.add(buildFileRow());
        return panel;
    }

    private JPanel buildDispatchRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Incident ID:"));
        dispatchIdField = new JTextField(8);
        row.add(dispatchIdField);

        makeButton(row, "Preview Dispatch", event -> runIncidentAction(controller::previewDispatch, false));
        makeButton(row, "Dispatch Incident", event -> runIncidentAction(controller::dispatch, true));
        makeButton(row, "Resolve Incident", event -> runIncidentAction(controller::resolve, true));
        return row;
    }

    private JPanel buildFileRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("File prefix for Save / Load:"));
        prefixField = new JTextField("gui_state", 12);
        row.add(prefixField);
        return row;
    }

    private interface IncidentAction {
        String run(String incidentId) throws Exception;
    }

    private void runIncidentAction(IncidentAction action, boolean changesState) {
        try {
            String id = dispatchIdField.getText().trim();
            if (id.isEmpty()) {
                throw new IllegalArgumentException("Enter an incident ID first.");
            }
            String result = action.run(id);
            appendOutput(result + "\n");
            if (changesState) {
                appendOutput(controller.stateSummary() + "\n");   // show updated statuses and availability
                refreshView();
            }
        } catch (IllegalArgumentException e) {
            appendOutput("Input error: " + e.getMessage() + "\n");
        } catch (Exception e) {
            appendOutput("Error: " + e.getMessage() + "\n");   // e.g. no suitable unit, not ASSIGNED
        }
    }

    private JButton makeButton(JPanel panel, String label, ActionListener listener) {
        JButton button = new JButton(label);
        button.addActionListener(listener);
        panel.add(button);
        return button;
    }

    private void showText(String text) {
        output_area.setText(text);
        output_area.setCaretPosition(0);
    }

    private JPanel buildViewRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        makeButton(row, "All Units",       event -> output_area.setText(controller.listAllUnits()));
        makeButton(row, "Available Units", event -> output_area.setText(controller.listAvailableUnits()));
        makeButton(row, "All Incidents",   event -> output_area.setText(controller.listAllIncidents()));
        makeButton(row, "System Report", event -> showText(controller.report()));
        makeButton(row, "Clear Output",    event -> output_area.setText(""));
        return row;
    }

    private JPanel buildStatusRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Incidents by status:"));

        ActionListener radioListener = event -> selectedStatus = event.getActionCommand();

        ButtonGroup group = new ButtonGroup();
        for (String status : new String[] {"OPEN", "ASSIGNED", "RESOLVED"}) {
            JRadioButton radio = new JRadioButton(status);
            radio.setActionCommand(status);
            radio.addActionListener(radioListener);
            group.add(radio);
            row.add(radio);
            if (status.equals("OPEN")) {
                radio.setSelected(true);
            }
        }

        makeButton(row, "Show", event -> showText(controller.listIncidentsByStatus(selectedStatus)));
        return row;
    }



    private  JScrollPane builtOutput(){
        output_area = new JTextArea(20,60);
        output_area.setEditable(false);
        output_area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(output_area);
        scroll.setBorder(BorderFactory.createTitledBorder("Output"));
        return scroll;
    }

    private StatusPanel buildStatus() {
        statusPanel = new StatusPanel(manager);
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        return statusPanel;
    }

    private void refreshView() {
        statusPanel.repaint();
    }

    public void appendOutput(String text){
        output_area.append(text);
        output_area.setCaretPosition(output_area.getDocument().getLength());
    }

    private double parseNumber(String text, String fieldName){
        try {
            return Double.parseDouble(text.trim());
        }
        catch (NumberFormatException e){
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    private class UnitTypeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean isDrone = "SEARCH_DRONE".equals(unitTypeBox.getSelectedItem());
            unitTraffic.setEnabled(!isDrone);
        }
    }

    private class AddUnitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                String type = (String) unitTypeBox.getSelectedItem();
                String id = unitId.getText().trim();
                String name = unitName.getText().trim();

                if (id.isEmpty()) {
                    throw new IllegalArgumentException("Unit ID cannot be empty.");
                }
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Unit name cannot be empty.");
                }
                double speed = parseNumber(unitSpeed.getText(), "Speed");
                double traffic = 1.0;
                if (unitTraffic.isEnabled()) {
                    traffic = parseNumber(unitTraffic.getText(), "Traffic factor");
                }

                String message = controller.addUnit(type, id, name, speed, traffic);
                appendOutput(message + "\n");

                unitId.setText("");
                unitName.setText("");
                unitSpeed.setText("");
                refreshView();
            } catch (IllegalArgumentException e) {
                appendOutput("Input error: " + e.getMessage() + "\n");
            } catch (Exception e) {
                appendOutput("Error: " + e.getMessage() + "\n");
            }
        }
    }

    private void setExtras(String label1, String label2, String flagText) {
        extra1Label.setText(label1 + ":");
        extra2Label.setText(label2 == null ? "(not used)" : label2 + ":");
        extra2Field.setEnabled(label2 != null);
        flagBox.setText(flagText == null ? "(not used)" : flagText);
        flagBox.setEnabled(flagText != null);
        if (flagText == null) {
            flagBox.setSelected(false);
        }
    }

    private void updateIncidentFields() {
        String type = (String) incidentTypeBox.getSelectedItem();
        if (type.equals("MEDICAL")) {
            setExtras("Patient count", "Critical patients", null);
        } else if (type.equals("FIRE")) {
            setExtras("Affected area", null, "Hazardous material present");
        } else if (type.equals("INFRASTRUCTURE")) {
            setExtras("Affected users", null, "Critical service affected");
        } else if (type.equals("SEARCH")) {
            setExtras("Missing persons", "Search area", null);
        } else {   // HAZMAT
            setExtras("Contaminant spread", "Population at risk", null);
        }
    }

    private class IncidentTypeListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event){
            updateIncidentFields();
        }
    }

    private class SeverityListener implements ChangeListener{
        @Override
        public void stateChanged(ChangeEvent event){
            severityLabel.setText("Severity: " + incidentSeverity.getValue());
        }
    }

    private class ReportIncidentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent event){
            try {
                String type = (String) incidentTypeBox.getSelectedItem();
                String id = incidentId.getText().trim();
                String description = incidentDescription.getText().trim();

                if (id.isEmpty()){
                    throw new IllegalArgumentException("Incident ID cannot be empty.");
                }
                if (description.isEmpty()) {
                    throw new IllegalArgumentException("Description cannot be empty.");
                }
                double distance = parseNumber(incidentDistance.getText(), "Distance from base");
                if (distance < 0) {
                    throw new IllegalArgumentException("Distance from base cannot be negative.");
                }
                int severity = incidentSeverity.getValue();

                double extra1 = parseNumber(extra1Field.getText(), extra1Label.getText().replace(":",""));
                double extra2 = 0;

                if (extra2Field.isEnabled()){
                    extra2 = parseNumber(extra2Field.getText(), extra2Label.getText().replace(":",""));
                }

                String message = controller.addIncident(type, id, description, distance,
                        severity, extra1, extra2, flagBox.isSelected());
                appendOutput(message + "\n");

                incidentId.setText("");
                incidentDescription.setText("");
                incidentDistance.setText("");
                extra1Field.setText("");
                extra2Field.setText("");
                refreshView();

            }
            catch (IllegalArgumentException e) {
                appendOutput("Input error: " + e.getMessage() + "\n");
            }
            catch (Exception e) {
                appendOutput("Error: " + e.getMessage() + "\n");
            }
        }
    }
}
