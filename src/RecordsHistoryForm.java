import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays completed and pending parking payment transactions in a searchable,
 * filterable table with live record count, revenue totals, and CSV file persistence.
 */
public class RecordsHistoryForm {
    // Swing UI components automatically bound to RecordsHistoryForm.form via their field names
    private JPanel mainPanel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JButton btnRefresh;
    private JButton btnClear;
    private JTable tblRecords;
    private JLabel lblTotalRecords;
    private JLabel lblTotalRevenue;
    private JButton btnClose;

    // Shared in-memory list backed by a local CSV file so records persist across runs
    private static final List<Object[]> sessionRecords = new ArrayList<>();
    private static final String RECORDS_FILE = "parking_records.csv";
    private static boolean isLoadedFromFile = false;

    // Table model backing tblRecords
    private DefaultTableModel tableModel;

    // Load previously saved records from disk as soon as the class is referenced
    static {
        loadRecordsFromFile();
    }

    /**
     * Initializes the RecordsHistoryForm, configures a read-only table,
     * populates filter options, and attaches search/filter listeners.
     */
    public RecordsHistoryForm() {
        // Ensure records are loaded from file if not yet initialized
        if (!isLoadedFromFile) {
            loadRecordsFromFile();
        }

        // Set up table columns and lock cells so users cannot accidentally edit history rows
        String[] columns = {"Payment ID", "Slot", "Plate Number", "Method", "Amount (PHP)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevents accidental cell editing during live testing
            }
        };
        tblRecords.setModel(tableModel);
        tblRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRecords.getTableHeader().setReorderingAllowed(false);
        configureTableColumnWidths();

        // Populate status filter dropdown
        cmbStatusFilter.setModel(new DefaultComboBoxModel<>(new String[]{"ALL", "PAID", "PENDING"}));

        // Load initial rows into the table
        refreshTable();

        // Listener 1: Filter rows live as the user types in the search box
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshTable(); }

            @Override
            public void removeUpdate(DocumentEvent e) { refreshTable(); }

            @Override
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        // Listener 2: Filter rows whenever the status dropdown changes
        cmbStatusFilter.addActionListener(e -> refreshTable());

        // Listener 3: Reset search/filter and reload records from file when "Refresh" is clicked
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            loadRecordsFromFile();
            refreshTable();
        });

        // Listener 4: Confirm and clear all saved history records (useful before live demos)
        if (btnClear != null) {
            btnClear.addActionListener(e -> handleClearHistory());
        }

        // Listener 5: Close only this history window without exiting the whole application
        btnClose.addActionListener(e -> {
            java.awt.Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) {
                window.dispose();
            }
        });
    }

    /**
     * Sets explicit column widths, row height, and preferred viewport size
     * so headers and cell values never truncate with "..." when packed.
     */
    private void configureTableColumnWidths() {
        tblRecords.setRowHeight(24);
        tblRecords.setPreferredScrollableViewportSize(new Dimension(620, 300));

        int[] widths = {115, 60, 110, 110, 110, 85};
        for (int i = 0; i < widths.length && i < tblRecords.getColumnCount(); i++) {
            tblRecords.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /**
     * Prompts for confirmation and clears all records from memory and parking_records.csv.
     */
    private void handleClearHistory() {
        int confirm = JOptionPane.showConfirmDialog(
                mainPanel,
                "Are you sure you want to clear all parking and payment history records?",
                "Confirm Clear History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            sessionRecords.clear();
            saveRecordsToFile();
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            refreshTable();

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "All history records have been cleared.",
                    "History Cleared",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Reads saved payment records from parking_records.csv into sessionRecords.
     */
    public static void loadRecordsFromFile() {
        File file = new File(RECORDS_FILE);
        sessionRecords.clear();
        isLoadedFromFile = true;

        if (!file.exists()) {
            return; // No saved records yet on first run
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("PaymentID,")) {
                    continue; // Skip blank lines and CSV header
                }
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String id = parts[0].trim();
                    String slot = parts[1].trim();
                    String plate = parts[2].trim();
                    String method = parts[3].trim();
                    double amount = Double.parseDouble(parts[4].trim());
                    String status = parts[5].trim();
                    sessionRecords.add(new Object[]{id, slot, plate, method, amount, status});
                } else if (parts.length == 5) {
                    // Backward compatibility for older 5-column CSV rows without plate number
                    String id = parts[0].trim();
                    String slot = parts[1].trim();
                    String method = parts[2].trim();
                    double amount = Double.parseDouble(parts[3].trim());
                    String status = parts[4].trim();
                    sessionRecords.add(new Object[]{id, slot, "N/A", method, amount, status});
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading records file: " + e.getMessage());
        }
    }

    /**
     * Writes all current sessionRecords to parking_records.csv so data persists on disk.
     */
    public static void saveRecordsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORDS_FILE))) {
            writer.write("PaymentID,Slot,PlateNumber,Method,Amount,Status");
            writer.newLine();
            for (Object[] row : sessionRecords) {
                writer.write(
                        row[0] + "," +
                                row[1] + "," +
                                row[2] + "," +
                                row[3] + "," +
                                String.format("%.2f", ((Number) row[4]).doubleValue()) + "," +
                                row[5]
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving records file: " + e.getMessage());
        }
    }

    /**
     * Allows CheckoutPaymentForm or SmartParking to log a pending or completed Payment
     * into the history table and save it to disk immediately.
     *
     * @param payment The Payment transaction to record
     * @param slotId  The parking slot ID associated with the ticket
     */
    public static void addPaymentRecord(Payment payment, String slotId) {
        if (payment == null) return;

        String id = payment.getPaymentID() != null ? payment.getPaymentID() : "N/A";
        String slot = (slotId != null && !slotId.trim().isEmpty()) ? slotId : "N/A";
        String plate = "N/A";
        if (payment.getTicket() != null && payment.getTicket().getVehicle() != null
                && payment.getTicket().getVehicle().getPlateNum() != null) {
            plate = payment.getTicket().getVehicle().getPlateNum();
        }
        String method = payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "CASH";
        double amount = payment.getTotalAmount();
        String status = payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "PENDING";

        sessionRecords.add(new Object[]{id, slot, plate, method, amount, status});
        saveRecordsToFile();
    }

    /**
     * Updates an existing PENDING payment record to PAID once checkout is confirmed,
     * saves the updated list to disk, or appends a new record if not found.
     *
     * @param updatedPayment The confirmed Payment transaction
     */
    public static void updatePaymentRecord(Payment updatedPayment) {
        if (updatedPayment == null) return;

        String targetId = updatedPayment.getPaymentID();
        if (targetId != null) {
            for (Object[] row : sessionRecords) {
                if (targetId.equals(String.valueOf(row[0]))) {
                    row[3] = updatedPayment.getPaymentMethod() != null
                            ? updatedPayment.getPaymentMethod().name()
                            : row[3];
                    row[4] = updatedPayment.getTotalAmount();
                    row[5] = updatedPayment.getPaymentStatus() != null
                            ? updatedPayment.getPaymentStatus().name()
                            : "PAID";
                    saveRecordsToFile();
                    return;
                }
            }
        }

        // Fallback: add as a new record if it was not logged during check-in
        String slotId = (updatedPayment.getTicket() != null && updatedPayment.getTicket().getSlot() != null)
                ? updatedPayment.getTicket().getSlot().getSlotID()
                : "N/A";
        addPaymentRecord(updatedPayment, slotId);
    }

    /**
     * Filters sessionRecords by search keyword and status, updates the JTable,
     * and recalculates the total record count and revenue footer labels.
     */
    private void refreshTable() {
        tableModel.setRowCount(0);

        String query = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        Object selectedObj = cmbStatusFilter.getSelectedItem();
        String selectedStatus = selectedObj != null ? selectedObj.toString() : "ALL";

        int visibleCount = 0;
        double totalRevenue = 0.0;

        for (Object[] row : sessionRecords) {
            String paymentId = String.valueOf(row[0]);
            String slot = String.valueOf(row[1]);
            String plate = String.valueOf(row[2]);
            String method = String.valueOf(row[3]);
            double amount = (row[4] instanceof Number) ? ((Number) row[4]).doubleValue() : 0.0;
            String status = String.valueOf(row[5]);
            String amountStr = String.format("%.2f", amount);

            // Check status filter
            boolean matchesStatus = selectedStatus.equals("ALL") || status.equalsIgnoreCase(selectedStatus);

            // Check search text against Payment ID, Slot, Plate Number, Method, Amount, or Status
            boolean matchesSearch = query.isEmpty()
                    || paymentId.toLowerCase().contains(query)
                    || slot.toLowerCase().contains(query)
                    || plate.toLowerCase().contains(query)
                    || method.toLowerCase().contains(query)
                    || amountStr.contains(query)
                    || status.toLowerCase().contains(query);

            if (matchesStatus && matchesSearch) {
                tableModel.addRow(new Object[]{
                        paymentId,
                        slot,
                        plate,
                        method,
                        amountStr,
                        status
                });
                visibleCount++;

                // Only count PAID transactions toward collected revenue
                if (status.equalsIgnoreCase("PAID")) {
                    totalRevenue += amount;
                }
            }
        }

        lblTotalRecords.setText("Total Records: " + visibleCount);
        lblTotalRevenue.setText(String.format("Total Revenue: PHP %.2f", totalRevenue));
    }

    /**
     * Exposes the root JPanel so SmartParking can open this screen.
     *
     * @return The root JPanel of RecordsHistoryForm
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Standalone entry point for testing RecordsHistoryForm independently.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Parking & Payment History");
            frame.setContentPane(new RecordsHistoryForm().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(680, 440));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}