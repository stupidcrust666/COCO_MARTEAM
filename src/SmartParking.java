import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main dashboard for the Smart Parking Management System.
 * Manages slot allocation by Vehicle_Type, vehicle check-in/check-out,
 * and launches CheckoutPaymentForm and RecordsHistoryForm.
 */
public class SmartParking {
    // Swing UI components automatically bound to SmartParking.form via their field names
    private JPanel mainPanel;
    private JTextField txtPlateNumber;
    private JComboBox<Vehicle_Type> cmbVehicleType;
    private JButton btnCheckIn;
    private JTable tblParkingSlots;
    private JLabel lblSlotSummary;
    private JButton btnHistory;
    private JButton btnCheckOut;
    private JButton btnLogout;

    // In-memory parking slots and active tickets keyed by slotID
    private final List<Parking_Slot> parkingSlots = new ArrayList<>();
    private final Map<String, Ticket> activeTickets = new HashMap<>();

    // Table model backing tblParkingSlots
    private DefaultTableModel tableModel;

    // Formatter for displaying readable entry timestamps in the table
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Initializes the dashboard, sets up 10 typed parking slots,
     * configures the read-only slot table, and attaches all button listeners.
     */
    public SmartParking() {
        // Populate vehicle type dropdown directly from the Vehicle_Type enum
        cmbVehicleType.setModel(new DefaultComboBoxModel<>(Vehicle_Type.values()));

        // Initialize 10 parking slots grouped by Vehicle_Type (4 Cars, 4 Motorcycles, 2 Vans)
        initializeSlots();

        // Set up read-only table columns so users cannot manually edit slot cells
        String[] columns = {"Slot ID", "Allowed Type", "Status", "Plate Number", "Entry Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevents accidental editing during live testing
            }
        };
        tblParkingSlots.setModel(tableModel);
        tblParkingSlots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblParkingSlots.getTableHeader().setReorderingAllowed(false);

        // Render initial slot state and availability counter
        refreshSlotTable();

        // Listener 1: Park / Check In vehicle into an available slot of matching Vehicle_Type
        btnCheckIn.addActionListener(e -> handleCheckIn());
        txtPlateNumber.addActionListener(e -> handleCheckIn()); // Allow pressing Enter to park

        // Listener 2: Check Out selected occupied slot and launch CheckoutPaymentForm
        btnCheckOut.addActionListener(e -> handleCheckOut());

        // Listener 3: Open RecordsHistoryForm in a secondary window
        btnHistory.addActionListener(e -> {
            JFrame historyFrame = new JFrame("Parking & Payment History");
            historyFrame.setContentPane(new RecordsHistoryForm().getMainPanel());
            historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            historyFrame.pack();
            historyFrame.setLocationRelativeTo(mainPanel);
            historyFrame.setVisible(true);
        });

        // Listener 4: Logout and return cleanly to LoginForm
        btnLogout.addActionListener(e -> handleLogout());
    }

    /**
     * Creates 10 default slots across CAR, MOTORCYCLE, and VAN zones.
     */
    private void initializeSlots() {
        parkingSlots.add(new Parking_Slot("C-01", Vehicle_Type.CAR));
        parkingSlots.add(new Parking_Slot("C-02", Vehicle_Type.CAR));
        parkingSlots.add(new Parking_Slot("C-03", Vehicle_Type.CAR));
        parkingSlots.add(new Parking_Slot("C-04", Vehicle_Type.CAR));

        parkingSlots.add(new Parking_Slot("M-01", Vehicle_Type.MOTORCYCLE));
        parkingSlots.add(new Parking_Slot("M-02", Vehicle_Type.MOTORCYCLE));
        parkingSlots.add(new Parking_Slot("M-03", Vehicle_Type.MOTORCYCLE));
        parkingSlots.add(new Parking_Slot("M-04", Vehicle_Type.MOTORCYCLE));

        parkingSlots.add(new Parking_Slot("V-01", Vehicle_Type.VAN));
        parkingSlots.add(new Parking_Slot("V-02", Vehicle_Type.VAN));
    }

    /**
     * Validates plate input, checks for duplicate active plates, assigns the first
     * free slot matching the chosen Vehicle_Type, and generates a Ticket.
     */
    private void handleCheckIn() {
        String rawPlate = txtPlateNumber.getText() != null ? txtPlateNumber.getText().trim().toUpperCase() : "";

        // Guard 1: Empty plate number
        if (rawPlate.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Please enter a vehicle plate number.",
                    "Missing Plate Number",
                    JOptionPane.WARNING_MESSAGE
            );
            txtPlateNumber.requestFocus();
            return;
        }

        // Guard 2: Basic length/character check so invalid inputs don't slip through
        if (rawPlate.length() < 3 || !rawPlate.matches("[A-Z0-9 -]+")) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Enter a valid plate number (letters, numbers, spaces, or hyphens only).",
                    "Invalid Plate Format",
                    JOptionPane.WARNING_MESSAGE
            );
            txtPlateNumber.requestFocus();
            return;
        }

        // Guard 3: Prevent checking in a plate number that is already parked
        for (Parking_Slot slot : parkingSlots) {
            if (slot.isOccupied() && slot.getParkedVehicle() != null
                    && rawPlate.equalsIgnoreCase(slot.getParkedVehicle().getPlateNum())) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "Vehicle " + rawPlate + " is already parked in slot " + slot.getSlotID() + ".",
                        "Duplicate Vehicle",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }

        Vehicle_Type selectedType = (Vehicle_Type) cmbVehicleType.getSelectedItem();
        if (selectedType == null) {
            selectedType = Vehicle_Type.CAR;
        }

        // Find the first unoccupied slot matching the selected Vehicle_Type
        Parking_Slot targetSlot = null;
        for (Parking_Slot slot : parkingSlots) {
            if (!slot.isOccupied() && slot.getAllowedType() == selectedType) {
                targetSlot = slot;
                break;
            }
        }

        // Guard 4: All slots for that vehicle type are full
        if (targetSlot == null) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No available slots left for " + selectedType + ".",
                    "Zone Full",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Create and populate Vehicle using its public fields
        Vehicle vehicle = new Vehicle();
        vehicle.plateNum = rawPlate;
        vehicle.vehicleType = selectedType;
        vehicle.brand = "Standard";
        vehicle.model = selectedType.name();
        vehicle.color = "N/A";

        // Assign vehicle to slot and issue an active Ticket
        if (targetSlot.assignVehicle(vehicle)) {
            String ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            Ticket ticket = new Ticket(ticketId, LocalDateTime.now(), vehicle, targetSlot);
            activeTickets.put(targetSlot.getSlotID(), ticket);

            txtPlateNumber.setText("");
            txtPlateNumber.requestFocus();
            refreshSlotTable();

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Vehicle " + rawPlate + " parked in slot " + targetSlot.getSlotID() + ".",
                    "Check-In Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Validates row selection, stamps exitTime on the Ticket, releases the slot,
     * and opens CheckoutPaymentForm for payment processing.
     */
    private void handleCheckOut() {
        int selectedRow = tblParkingSlots.getSelectedRow();

        // Guard 1: No row selected
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Please select an occupied parking slot row first.",
                    "No Slot Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String slotId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        Parking_Slot selectedSlot = null;
        for (Parking_Slot slot : parkingSlots) {
            if (slot.getSlotID().equals(slotId)) {
                selectedSlot = slot;
                break;
            }
        }

        // Guard 2: Selected slot is currently available (not occupied)
        if (selectedSlot == null || !selectedSlot.isOccupied()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Slot " + slotId + " is currently empty. Select an OCCUPIED slot to check out.",
                    "Slot Empty",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Retrieve active ticket, stamp exit time, and release the slot
        Ticket ticket = activeTickets.remove(slotId);
        if (ticket == null) {
            // Fallback ticket if state ever desynced, preventing NullPointerException
            ticket = new Ticket("TKT-FALLBACK", LocalDateTime.now(), selectedSlot.getParkedVehicle(), selectedSlot);
        }
        ticket.setExitTime(LocalDateTime.now());
        selectedSlot.releaseSlot();
        refreshSlotTable();

        // Open CheckoutPaymentForm with the completed Ticket
        JFrame checkoutFrame = new JFrame("Checkout & Payment - Slot " + slotId);
        checkoutFrame.setContentPane(new CheckoutPaymentForm(ticket).getMainPanel());
        checkoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        checkoutFrame.pack();
        checkoutFrame.setLocationRelativeTo(mainPanel);
        checkoutFrame.setVisible(true);
    }

    /**
     * Confirms logout and switches back to the LoginForm window.
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                mainPanel,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            java.awt.Window currentWindow = SwingUtilities.getWindowAncestor(mainPanel);
            if (currentWindow != null) {
                currentWindow.dispose();
            }

            JFrame loginFrame = new JFrame("Smart Parking Login");
            loginFrame.setContentPane(new LoginForm().getMainPanel());
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.pack();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        }
    }

    /**
     * Rebuilds the slot table rows and updates the "Available Slots: X / 10" counter.
     */
    private void refreshSlotTable() {
        tableModel.setRowCount(0);
        int availableCount = 0;

        for (Parking_Slot slot : parkingSlots) {
            if (!slot.isOccupied()) {
                availableCount++;
                tableModel.addRow(new Object[]{
                        slot.getSlotID(),
                        slot.getAllowedType().name(),
                        "AVAILABLE",
                        "-",
                        "-"
                });
            } else {
                Vehicle v = slot.getParkedVehicle();
                String plate = (v != null && v.getPlateNum() != null) ? v.getPlateNum() : "UNKNOWN";
                String timeText = LocalDateTime.now().format(TIME_FORMATTER);
                tableModel.addRow(new Object[]{
                        slot.getSlotID(),
                        slot.getAllowedType().name(),
                        "OCCUPIED",
                        plate,
                        timeText
                });
            }
        }

        lblSlotSummary.setText("Available Slots: " + availableCount + " / " + parkingSlots.size());
    }

    /**
     * Exposes the root JPanel so LoginForm can launch the dashboard.
     *
     * @return The root JPanel of SmartParking
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Standalone entry point for testing SmartParking independently.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Smart Parking Management System");
            frame.setContentPane(new SmartParking().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}