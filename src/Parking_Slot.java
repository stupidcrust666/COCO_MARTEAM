
public class Parking_Slot {
    private String slotID;
    private Vehicle_Type allowedType;
    private boolean isOccupied;
    private Vehicle parkedVehicle;

    public Parking_Slot(String slotID, Vehicle_Type allowedType) {
        this.slotID = slotID;
        this.allowedType = allowedType;
        this.isOccupied = false;
        this.parkedVehicle = null;
    }

    public boolean assignVehicle(Vehicle vehicle) {
        if (isOccupied || vehicle == null) {
            return false;
        }
        this.parkedVehicle = vehicle;
        this.isOccupied = true;
        return true;
    }

    public void releaseSlot() {
        this.parkedVehicle = null;
        this.isOccupied = false;
    }

    public String getSlotID() {
        return slotID;
    }

    public Vehicle_Type getAllowedType() {
        return allowedType;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
}
