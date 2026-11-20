import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
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
    private JTable tblRecords;
    private JLabel lblTotalRecords;
    private JLabel lblTotalRevenue;
    private JButton btnClose;
    private JButton button1;

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
        String[] columns = {"Payment ID", "Slot", "Method", "Amount (PHP)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevents accidental cell editing during live testing
            }
        };
        tblRecords.setModel(tableModel);
        tblRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRecords.getTableHeader().setReorderingAllowed(false);

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

        // Listener 4: Close only this history window without exiting the whole application
        btnClose.addActionListener(e -> {
            java.awt.Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) {
                window.dispose();
            }
        });
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
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    String slot = parts[1].trim();
                    String method = parts[2].trim();
                    double amount = Double.parseDouble(parts[3].trim());
                    String status = parts[4].trim();
                    sessionRecords.add(new Object[]{id, slot, method, amount, status});
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
            writer.write("PaymentID,Slot,Method,Amount,Status");
            writer.newLine();
            for (Object[] row : sessionRecords) {
                writer.write(
                        row[0] + "," +
                                row[1] + "," +
                                row[2] + "," +
                                String.format("%.2f", ((Number) row[3]).doubleValue()) + "," +
                                row[4]
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
        String method = payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "CASH";
        double amount = payment.getTotalAmount();
        String status = payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "PENDING";

        sessionRecords.add(new Object[]{id, slot, method, amount, status});
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
                    row[2] = updatedPayment.getPaymentMethod() != null
                            ? updatedPayment.getPaymentMethod().name()
                            : row[2];
                    row[3] = updatedPayment.getTotalAmount();
                    row[4] = updatedPayment.getPaymentStatus() != null
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
            String method = String.valueOf(row[2]);
            double amount = (row[3] instanceof Number) ? ((Number) row[3]).doubleValue() : 0.0;
            String status = String.valueOf(row[4]);

            // Check status filter
            boolean matchesStatus = selectedStatus.equals("ALL") || status.equalsIgnoreCase(selectedStatus);

            // Check search text against Payment ID, Slot, or Payment Method
            boolean matchesSearch = query.isEmpty()
                    || paymentId.toLowerCase().contains(query)
                    || slot.toLowerCase().contains(query)
                    || method.toLowerCase().contains(query);

            if (matchesStatus && matchesSearch) {
                tableModel.addRow(new Object[]{
                        paymentId,
                        slot,
                        method,
                        String.format("%.2f", amount),
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
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}