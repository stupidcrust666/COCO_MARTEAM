import java.time.LocalDateTime;

public class Ticket {
    private String ticketID;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Vehicle vehicle;
    private Parking_Slot slot;

    public double getDuration(){

        return;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Parking_Slot getSlot() {
        return slot;
    }
}
