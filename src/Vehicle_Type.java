public enum Vehicle_Type {
    CAR(40.0),
    MOTORCYCLE(20.0),
    VAN(60.0);

    private final double hourlyRate;

    Vehicle_Type(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }
}