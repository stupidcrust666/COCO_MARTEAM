import java.time.LocalDateTime;

public class Parking_Attendant extends Person {
    private static int ticketCounter = 1;
    private static int paymentCounter = 1;

    private String employeeID;
    private Parking_Lot lot;     // the lot this attendant manages

    public Parking_Attendant(String name, int age, String contactNum, String employeeID, Parking_Lot lot) {
        super(name, age, contactNum);
        this.employeeID = employeeID;
        this.lot = lot;
    }

    public String getEmployeeID() { return employeeID; }

    @Override
    public String getRole() { return "Parking Attendant"; }

    // Priority drivers get priority slots first. Returns null if the lot is full.
    public Parking_Slot assignParkingSlot(Driver driver) {
        Parking_Slot slot = lot.reserveSlot(driver.isPriorityEligible());
        driver.setAssignedSlot(slot);
        return slot;
    }

    // Returns null if there is no free slot
    public Ticket issueTicket(Driver driver) {
        Parking_Slot slot = assignParkingSlot(driver);
        if (slot == null) {
            return null;
        }
        String id = String.format("T-%04d", ticketCounter++);
        return new Ticket(id, LocalDateTime.now(), driver.getVehicle(), slot);
    }

    public boolean verifyPayment(Ticket ticket) {
        return ticket.getPayment() != null && ticket.getPayment().verifyPayment();
    }

    public void processVehicleExit(Ticket ticket, Driver driver, Payment_Method method) {
        ticket.setExitTime(LocalDateTime.now());

        String id = String.format("P-%04d", paymentCounter++);
        double rate = ticket.getVehicle().getVehicleType().getHourlyRate();

        Payment payment = new Payment(id, ticket, rate, method);
        payment.applyDiscount(driver.getDiscountRate());
        ticket.setPayment(payment);
        payment.processPayment();

        if (verifyPayment(ticket)) {
            lot.releaseSlot(ticket.getSlot());
            driver.setAssignedSlot(null);
        }
    }

    public int checkParkingAvailability() {
        return lot.checkAvailability();
    }
}