import javax.swing.*;
import java.util.UUID;

/**
 * Handles the Checkout & Payment GUI screen.
 * Connects the visual Swing components in CheckoutPaymentForm.form
 * to the backend Payment and Ticket objects.
 */
public class CheckoutPaymentForm {
    // Swing UI components automatically bound to CheckoutPaymentForm.form via their field names
    private JPanel mainPanel;
    private JLabel lblTicketId;
    private JLabel lblSlotId;
    private JLabel lblDuration;
    private JComboBox<Payment_Method> cmbPaymentMethod;
    private JCheckBox chkDiscount;
    private JLabel lblTotalAmount;
    private JLabel lblStatus;
    private JButton btnCancel;
    private JButton btnConfirmPayment;

    // Stores the active payment transaction for this checkout session
    private Payment currentPayment;

    // Standard parking rate in PHP per hour
    private static final double DEFAULT_HOURLY_RATE = 50.0;

    /**
     * Default constructor used when running or previewing this form standalone
     * without passing a real Ticket object from the main dashboard.
     */
    public CheckoutPaymentForm() {
        this(null);
    }

    /**
     * Main constructor called by SmartParking when a vehicle checks out.
     *
     * @param ticket The active parking ticket being paid for (or null for demo mode)
     */
    public CheckoutPaymentForm(Ticket ticket) {
        // Populate the dropdown with all constants from the Payment_Method enum (CASH, GCASH, etc.)
        cmbPaymentMethod.setModel(new DefaultComboBoxModel<>(Payment_Method.values()));

        // Generate a short unique payment reference ID (e.g., "PAY-A1B2C3")
        String paymentID = "PAY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Initialize the backend Payment object with default CASH method
        currentPayment = new Payment(paymentID, ticket, DEFAULT_HOURLY_RATE, Payment_Method.CASH);

        // Display the ticket's slot and duration on the labels, then calculate the initial fee
        loadTicketDetails(ticket);
        updateTotalDisplay();

        // Listener 1: Recalculate total whenever the user checks or unchecks the 20% discount box
        chkDiscount.addActionListener(e -> {
            if (chkDiscount.isSelected()) {
                currentPayment.applyDiscount(0.20); // Apply 20% discount
            } else {
                currentPayment.applyDiscount(0.0);  // Remove discount
            }
            updateTotalDisplay();
        });

        // Listener 2: Update the Payment object whenever a different payment method is chosen
        cmbPaymentMethod.addActionListener(e -> {
            Payment_Method selected = (Payment_Method) cmbPaymentMethod.getSelectedItem();
            currentPayment.setPaymentMethod(selected);
        });

        // Listener 3: Finalize the transaction when "Confirm Payment" is clicked
        btnConfirmPayment.addActionListener(e -> handleConfirmPayment());

        // Listener 4: Close only this checkout window without exiting the whole application
        btnCancel.addActionListener(e -> {
            java.awt.Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) {
                window.dispose();
            }
        });
    }

    /**
     * Populates the ticket information labels on the form.
     * Falls back to placeholder values if no Ticket was passed (standalone testing).
     */
    private void loadTicketDetails(Ticket ticket) {
        if (ticket != null) {
            lblTicketId.setText("Active Ticket");
            lblSlotId.setText(ticket.getSlot() != null ? ticket.getSlot().getSlotID() : "N/A");
            lblDuration.setText(String.format("%.2f hrs", ticket.getDuration()));
        } else {
            lblTicketId.setText("DEMO-001");
            lblSlotId.setText("A-01");
            lblDuration.setText("1.00 hr (Minimum)");
        }
    }

    /**
     * Recalculates the total amount from the Payment object and refreshes the UI labels.
     */
    private void updateTotalDisplay() {
        double total = currentPayment.calculateTotal();
        lblTotalAmount.setText(String.format("PHP %.2f", total));
        lblStatus.setText(currentPayment.getPaymentStatus().name());
    }

    /**
     * Processes the payment, updates the status label, and shows a receipt popup.
     */
    private void handleConfirmPayment() {
        // Ensure the latest dropdown selection is saved before processing
        Payment_Method selected = (Payment_Method) cmbPaymentMethod.getSelectedItem();
        currentPayment.setPaymentMethod(selected);

        // Execute payment logic and update the on-screen status (e.g., PENDING -> PAID)
        boolean success = currentPayment.processPayment();
        lblStatus.setText(currentPayment.getPaymentStatus().name());

        if (success) {
            // Disable the button so the user cannot accidentally pay twice
            btnConfirmPayment.setEnabled(false);

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Payment Successful!\n"
                            + "Payment ID: " + currentPayment.getPaymentID() + "\n"
                            + "Method: " + currentPayment.getPaymentMethod() + "\n"
                            + "Total Paid: " + String.format("PHP %.2f", currentPayment.getTotalAmount()),
                    "Receipt",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Payment failed. Please verify payment details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Exposes the root JPanel so other windows (like SmartParking) can display this form.
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Standalone entry point for testing this form independently.
     * Right-click this file and select "Run CheckoutPaymentForm.main()" to preview.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Checkout & Payment");
            frame.setContentPane(new CheckoutPaymentForm().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack(); // Sizes the window neatly around your [20, 20, 20, 20] margins
            frame.setLocationRelativeTo(null); // Centers the window on screen
            frame.setVisible(true);
        });
    }
}