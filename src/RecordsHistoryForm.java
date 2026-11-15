import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays completed and pending parking payment transactions in a searchable,
 * filterable table with live record count and revenue totals.
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

    // Shared in-memory list so payments processed during the session appear here automatically
    private static final List<Object[]> sessionRecords = new ArrayList<>();

    // Table model backing tblRecords
    private DefaultTableModel tableModel;

    /**
     * Initializes the RecordsHistoryForm, configures a read-only table,
     * populates filter options, and attaches search/filter listeners.
     */
    public RecordsHistoryForm() {



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

        // Listener 3: Reset search/filter and reload all records when "Refresh" is clicked
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
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
     * Allows CheckoutPaymentForm or SmartParking to log a completed Payment
     * into the history table safely (null-checked to prevent crashes).
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
        String status = payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "PAID";

        sessionRecords.add(new Object[]{id, slot, method, amount, status});
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