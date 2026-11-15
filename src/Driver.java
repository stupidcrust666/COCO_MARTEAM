public class Driver extends Person implements Discountable, Prioritizable {
    private static final double DISCOUNT_RATE = 0.20;

    private String licenseNum;
    private Vehicle vehicle;
    private boolean isPWD;
    private boolean isSenior;
    private Parking_Slot assignedSlot;

    public Driver(String name, int age, String contactNum, String licenseNum, Vehicle vehicle, boolean isPWD, boolean isSenior, Parking_Slot assignedSlot) {
        super(name, age, contactNum);
        this.licenseNum = licenseNum;
        this.vehicle = vehicle;
        this.isPWD = isPWD;
        this.isSenior = isSenior;
        this.assignedSlot = assignedSlot;
    }

    public String getLicenseNum() { return licenseNum; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public void setAssignedSlot(Parking_Slot slot) {this.assignedSlot = slot; }

    @Override
    public String getRole() { return "Driver"; }

    // Discountable
    @Override
    public boolean isDiscountEligible() { return isPWD || isSenior; }

    @Override
    public double getDiscountRate() { return isDiscountEligible() ? DISCOUNT_RATE : 0.0; }

    @Override
    public double calculateDiscount(double amount) { return amount * getDiscountRate(); }

    // ---- Prioritizable ----
    @Override
    public boolean isPriorityEligible() { return isPWD || isSenior; }

    @Override
    public Parking_Slot getSlot() { return assignedSlot; }
}
