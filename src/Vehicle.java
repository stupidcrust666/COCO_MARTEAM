public class Vehicle {
    public String plateNum;
    public Vehicle_Type vehicleType;
    public String brand;
    public String model;
    public String color;

    public String getPlateNum() {
        return plateNum;
    }
    public Vehicle_Type getVehicleType() {
        return vehicleType;
    }
    public String getVehicleDetails(){
        return brand + " " + model  + " " + color;
    }
}
