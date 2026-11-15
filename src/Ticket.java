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
            LocalDateTime endTime;

            if (exitTime == null) {
                endTime = LocalDateTime.now();
            } else {
                endTime = exitTime;
            }

            return Duration.between(entryTime, endTime).toMinutes() / 60.0;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public Parking_Slot getSlot() {
            return slot;
        }

        public void setExitTime(LocalDateTime exitTime) {
            this.exitTime = exitTime;
        }
    }
