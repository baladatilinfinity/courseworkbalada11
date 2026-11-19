package ua.edu.nupp.kpp.client;

/**
 * Клієнтська модель транспортного засобу (JSON із /vehicles).
 */
public class Vehicle {

    private Long id;
    private String plateNumber;
    private String model;
    private double capacityM3;
    private String driverName;

    public Vehicle() {
    }

    public Vehicle(Long id,
                   String plateNumber,
                   String model,
                   double capacityM3,
                   String driverName) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.model = model;
        this.capacityM3 = capacityM3;
        this.driverName = driverName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getCapacityM3() {
        return capacityM3;
    }

    public void setCapacityM3(double capacityM3) {
        this.capacityM3 = capacityM3;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
