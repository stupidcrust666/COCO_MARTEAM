import java.util.ArrayList;

public class Parking_Lot {
    private int amountOfVehicles;
    private int capacity;
    private ArrayList<Parking_Slot> parkingSlots;

    // priorityCount = how many of the slots are in the priority zone
    public Parking_Lot(int capacity, int priorityCount) {
        this.capacity = capacity;
        this.amountOfVehicles = 0;
        this.parkingSlots = new ArrayList<>();
        for (int i = 1; i <= capacity; i++) {
            parkingSlots.add(new Parking_Slot(String.format("S-%02d", i), i <= priorityCount));
        }
    }

    public int getAmountOfVehicles() { return amountOfVehicles; }
    public int getCapacity() { return capacity; }

    public int getAvailableSlots() { return capacity - amountOfVehicles; }
    public int checkAvailability() { return getAvailableSlots(); }

    // Finds a free slot, occupies it, and returns it (null if none available)
    public Parking_Slot reserveSlot(boolean needsPriority) {
        Parking_Slot slot = null;
        if (needsPriority) {
            slot = findFree(true);
        }
        if (slot == null) {
            slot = findFree(false);   // regular zone
        }
        if (slot != null) {
            slot.occupy();
            amountOfVehicles++;
        }
        return slot;
    }

    public void releaseSlot(Parking_Slot slot) {
        if (slot != null && slot.isOccupied()) {
            slot.releaseSlot();
            amountOfVehicles--;
        }
    }

    private Parking_Slot findFree(boolean priorityZone) {
        for (Parking_Slot s : parkingSlots) {
            if (!s.isOccupied() && s.isPriorityZone() == priorityZone) {
                return s;
            }
        }
        return null;
    }

    public void displayAvailableSlots() {
        System.out.println("Available: " + getAvailableSlots() + "/" + capacity);
        for (Parking_Slot s : parkingSlots) {
            System.out.println("  " + s);
        }
    }
}