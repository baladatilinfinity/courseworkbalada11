package ua.edu.nupp.kpp.client;

/**
 * Клієнтська модель контейнерного майданчика (JSON із /sites).
 * Лише зберігає дані для відображення в JavaFX; правил і SQL не містить.
 */
public class ContainerSite {

    private Long id;
    private String code;
    private String address;
    private String district;
    private int containerCount;
    private double containerVolume;
    private int pickupIntervalDays;

    public ContainerSite() {
    }

    public ContainerSite(Long id,
                         String code,
                         String address,
                         String district,
                         int containerCount,
                         double containerVolume,
                         int pickupIntervalDays) {
        this.id = id;
        this.code = code;
        this.address = address;
        this.district = district;
        this.containerCount = containerCount;
        this.containerVolume = containerVolume;
        this.pickupIntervalDays = pickupIntervalDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getContainerCount() {
        return containerCount;
    }

    public void setContainerCount(int containerCount) {
        this.containerCount = containerCount;
    }

    public double getContainerVolume() {
        return containerVolume;
    }

    public void setContainerVolume(double containerVolume) {
        this.containerVolume = containerVolume;
    }

    public int getPickupIntervalDays() {
        return pickupIntervalDays;
    }

    public void setPickupIntervalDays(int pickupIntervalDays) {
        this.pickupIntervalDays = pickupIntervalDays;
    }
}
