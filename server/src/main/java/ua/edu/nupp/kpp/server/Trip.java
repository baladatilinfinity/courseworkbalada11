package ua.edu.nupp.kpp.server;

/**
 * Виїзд транспортного засобу за заявкою (таблиця trip).
 * <p>
 * collectedVolume має тип Double, бо до виконання виїзду обсяг невідомий (NULL у БД).
 * Дата зберігається рядком у форматі yyyy-MM-dd.
 */
public class Trip {

    private Long id;
    private long requestId;
    private long vehicleId;
    private String tripDate;
    private Double collectedVolume;
    private String note;

    public Trip() {
    }

    public Trip(Long id,
                long requestId,
                long vehicleId,
                String tripDate,
                Double collectedVolume,
                String note) {
        this.id = id;
        this.requestId = requestId;
        this.vehicleId = vehicleId;
        this.tripDate = tripDate;
        this.collectedVolume = collectedVolume;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public Double getCollectedVolume() {
        return collectedVolume;
    }

    public void setCollectedVolume(Double collectedVolume) {
        this.collectedVolume = collectedVolume;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
