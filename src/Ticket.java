import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

public class Ticket {
    private String ticketID;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Vehicle vehicle;
    private Parking_Slot slot;


    public Ticket(String ticketID, LocalDateTime entryTime, Vehicle vehicle, Parking_Slot slot) {
        this.ticketID = ticketID;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.vehicle = vehicle;
        this.slot = slot;

    }

    public double getDuration(){
        return Duration.between(entryTime, exitTime).toMinutes() / 60.0;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Parking_Slot getSlot() {
        return slot;
    }
}
