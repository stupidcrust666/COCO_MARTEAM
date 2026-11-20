import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Handles parking fee calculation, 20% PWD/Senior Citizen discount application,
 * payment confirmation, and updating the transaction status in RecordsHistoryForm.
 */
public class CheckoutPaymentForm {
    // Swing UI components bound to CheckoutPaymentForm.form
    private JPanel mainPanel;
    private JLabel lblTicketId;
    private JLabel lblPlateNumber;
    private JLabel lblPlate;
    private JLabel lblSlotId;
    private JLabel lblSlot;
    private JLabel lblEntryTime;
    private JLabel lblExitTime;
    private JLabel lblDuration;
    private JLabel lblSubtotal;
    private JCheckBox chkDiscount;
    private JLabel lblDiscount;
    private JLabel lblTotalAmount;
    private JLabel lblTotal;
    private JComboBox<Payment_Method> cmbPaymentMethod;
    private JLabel lblStatus;
    private JButton btnConfirmPayment;
    private JButton btnCancel;

    private final Ticket ticket;
    private Payment currentPayment;
    private final String ticketId;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final Runnable onPaymentSuccess;
    private double baseFee;

    private static final double DISCOUNT_RATE = 0.20;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Overloaded constructor for backward compatibility when only a Ticket is passed.
     */
    public CheckoutPaymentForm(Ticket ticket) {
        this(ticket, ticket != null ? ticket.getPayment() : null, "TKT-SESSION", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    /**
     * Overloaded constructor when Ticket and PENDING Payment are passed.
     */
    public CheckoutPaymentForm(Ticket ticket, Payment existingPayment) {
        this(ticket, existingPayment, "TKT-SESSION", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    /**
     * Overloaded constructor without completion callback.
     */
    public CheckoutPaymentForm(
            Ticket ticket,
            Payment existingPayment,
            String ticketId,
            LocalDateTime entryTime,
            LocalDateTime exitTime
    ) {
        this(ticket, existingPayment, ticketId, entryTime, exitTime, null);
    }

    /**
     * Initializes the checkout screen with the completed Ticket, PENDING Payment,
     * exact display metadata, and a callback invoked only when payment succeeds.
     */
    public CheckoutPaymentForm(
            Ticket ticket,
            Payment existingPayment,
            String ticketId,
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            Runnable onPaymentSuccess
    ) {
        this.ticket = ticket;
        this.currentPayment = existingPayment;
        this.ticketId = ticketId != null ? ticketId : "TKT-SESSION";
        this.entryTime = entryTime != null ? entryTime : LocalDateTime.now();
        this.exitTime = exitTime != null ? exitTime : LocalDateTime.now();
        this.onPaymentSuccess = onPaymentSuccess;

        if (cmbPaymentMethod != null) {
            cmbPaymentMethod.setModel(new DefaultComboBoxModel<>(Payment_Method.values()));
        }
        populateTicketDetails();
        recalculateTotal();

        // Recalculate total dynamically when discount checkbox is toggled
        if (chkDiscount != null) {
            chkDiscount.addActionListener(e -> recalculateTotal());
        }

        // Process payment, release slot via callback, and update RecordsHistoryForm from PENDING to PAID
        if (btnConfirmPayment != null) {
            btnConfirmPayment.addActionListener(e -> handleConfirmPayment());
        }

        // Cancel checkout: clear provisional exitTime and keep the slot OCCUPIED
        if (btnCancel != null) {
            btnCancel.addActionListener(e -> {
                if (this.ticket != null) {
                    this.ticket.setExitTime(null);
                }
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                if (window != null) {
                    window.dispose();
                }
            });
        }
    }

    /**
     * Null-safe helper to update JLabel text without throwing NullPointerException
     * if a UI designer field name differs.
     */
    private void setLabelText(JLabel label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    /**
     * Populates ticket metadata and computes the base fee using Ticket.getDuration().
     */
    private void populateTicketDetails() {
        if (ticket == null) return;

        String plateText = ticket.getVehicle() != null ? ticket.getVehicle().getPlateNum() : "-";
        String slotText = ticket.getSlot() != null ? ticket.getSlot().getSlotID() : "-";

        setLabelText(lblTicketId, ticketId);
        setLabelText(lblPlateNumber, plateText);
        setLabelText(lblPlate, plateText);
        setLabelText(lblSlotId, slotText);
        setLabelText(lblSlot, slotText);

        setLabelText(lblEntryTime, entryTime.format(FORMATTER));
        setLabelText(lblExitTime, exitTime.format(FORMATTER));

        long hours = (long) Math.max(1.0, Math.ceil(ticket.getDuration()));
        setLabelText(lblDuration, hours + " hr(s)");

        double hourlyRate = (ticket.getVehicle() != null && ticket.getVehicle().getVehicleType() != null)
                ? ticket.getVehicle().getVehicleType().getHourlyRate()
                : Vehicle_Type.CAR.getHourlyRate();


        if (currentPayment == null) {
            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            currentPayment = new Payment(paymentId, ticket, hourlyRate, Payment_Method.CASH);
        }

        // Ensure Payment recalculates with the final exitTime stamped on Ticket
        currentPayment.applyDiscount(0.0);
        baseFee = currentPayment.getTotalAmount();

        setLabelText(lblStatus, currentPayment.getPaymentStatus().name());
    }

    /**
     * Uses Payment.applyDiscount(...) to update subtotal, discount, and final total labels.
     */
    private void recalculateTotal() {
        if (currentPayment == null) return;

        double rate = (chkDiscount != null && chkDiscount.isSelected()) ? DISCOUNT_RATE : 0.0;
        currentPayment.applyDiscount(rate);
        double finalTotal = currentPayment.getTotalAmount();
        double discount = Math.max(0.0, baseFee - finalTotal);

        setLabelText(lblSubtotal, String.format("PHP %.2f", baseFee));
        setLabelText(lblDiscount, String.format("- PHP %.2f", discount));
        setLabelText(lblTotalAmount, String.format("PHP %.2f", finalTotal));
        setLabelText(lblTotal, String.format("PHP %.2f", finalTotal));
    }

    /**
     * Finalizes the Payment object, marks it PAID, releases the parking slot,
     * updates the existing PENDING row in RecordsHistoryForm, and displays a receipt.
     */
    private void handleConfirmPayment() {
        if (currentPayment == null) return;

        Payment_Method method = (cmbPaymentMethod != null && cmbPaymentMethod.getSelectedItem() != null)
                ? (Payment_Method) cmbPaymentMethod.getSelectedItem()
                : Payment_Method.CASH;

        currentPayment.setPaymentMethod(method);
        recalculateTotal();

        boolean success = currentPayment.processPayment();
        if (success) {
            // Disable the button so the user cannot accidentally pay twice
            if (btnConfirmPayment != null) {
                btnConfirmPayment.setEnabled(false);
            }

            setLabelText(lblStatus, currentPayment.getPaymentStatus().name());

            // Flip the existing PENDING record to PAID in RecordsHistoryForm
            RecordsHistoryForm.updatePaymentRecord(currentPayment);

            // Release the slot on the dashboard now that payment is confirmed
            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Payment Confirmed!\n"
                            + "Payment ID: " + currentPayment.getPaymentID() + "\n"
                            + "Method: " + method + "\n"
                            + "Amount Paid: PHP " + String.format("%.2f", currentPayment.getTotalAmount()),
                    "Receipt",
                    JOptionPane.INFORMATION_MESSAGE
            );

            Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) {
                window.dispose();
            }
        }
    }

    /**
     * Exposes the root JPanel so SmartParking can launch this screen.
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
}