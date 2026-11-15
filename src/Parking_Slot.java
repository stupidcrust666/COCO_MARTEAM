
public class Parking_Slot {
    private String slotID;
    private Vehicle_Type allowedType;
    private boolean isOccupied;
    private Vehicle parkedVehicle;
    private boolean isPriorityZone;

    public Parking_Slot(String slotID, Vehicle_Type allowedType) {
        this.slotID = slotID;
        this.allowedType = allowedType;
        this.isOccupied = false;
        this.parkedVehicle = null;
    }

    public Parking_Slot(String slotID, boolean isPriorityZone) {
        this.slotID = slotID;
        this.isPriorityZone = isPriorityZone;
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

    public void occupy() {
        this.isOccupied = true;
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

    public boolean isPriorityZone() {
        return isPriorityZone;
    }
}
