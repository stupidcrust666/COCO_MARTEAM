import java.time.LocalDateTime;

public class Payment {
    private String paymentID;
    private Ticket ticket;
    private double hourlyRate;
    private double discountAmount;
    private double totalAmount;
    private Payment_Method paymentMethod;
    private Payment_Status paymentStatus;
    private LocalDateTime paymentTime;

    public Payment(String paymentID, Ticket ticket, double hourlyRate, Payment_Method paymentMethod) {
        this.paymentID = paymentID;
        this.ticket = ticket;
        this.hourlyRate = hourlyRate;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = Payment_Status.PENDING;
        this.discountAmount = 0.0;
        this.totalAmount = calculateTotal();
    }

    public double calculateTotal() {
        if (ticket == null) {
            return 0.0;
        }
        double hours = Math.max(1.0, Math.ceil(ticket.getDuration())); // Minimum 1-hour charge
        double rawAmount = hours * hourlyRate;
        this.totalAmount = Math.max(0.0, rawAmount - discountAmount);
        return this.totalAmount;
    }

    public void applyDiscount(double discountRate) {
        double hours = Math.max(1.0, Math.ceil(ticket.getDuration()));
        double rawAmount = hours * hourlyRate;
        this.discountAmount = rawAmount * discountRate;
        calculateTotal();
    }

    public boolean processPayment() {
        if (totalAmount >= 0 && paymentMethod != null) {
            this.paymentStatus = Payment_Status.PAID;
            this.paymentTime = LocalDateTime.now();
//            if (ticket != null && ticket.getSlot() != null) {
//                ticket.getSlot().releaseSlot();
//            }
            return true;
        }
        this.paymentStatus = Payment_Status.FAILED;
        return false;
    }

    public boolean verifyPayment() { return paymentStatus == Payment_Status.PAID; }

    public String getPaymentID() {
        return paymentID;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Payment_Method getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Payment_Method paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Payment_Status getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Payment_Status paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
}