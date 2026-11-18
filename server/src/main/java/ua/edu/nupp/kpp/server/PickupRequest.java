package ua.edu.nupp.kpp.server;

/**
 * Заявка на вивезення відходів (таблиця pickup_request) — об'єкт життєвого циклу.
 * <p>
 * fillLevel, sanitaryScore і daysSincePickup вводить диспетчер;
 * state, f1, f2, f3 і priorityIndex задає сервер (PickupRequestService).
 * Дата зберігається рядком у форматі yyyy-MM-dd.
 */
public class PickupRequest {

    private Long id;
    private long siteId;
    private String createdDate;
    private double fillLevel;
    private double sanitaryScore;
    private int daysSincePickup;
    private RequestState state;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;

    public PickupRequest() {
    }

    public PickupRequest(Long id,
                         long siteId,
                         String createdDate,
                         double fillLevel,
                         double sanitaryScore,
                         int daysSincePickup,
                         RequestState state,
                         double f1,
                         double f2,
                         double f3,
                         double priorityIndex) {
        this.id = id;
        this.siteId = siteId;
        this.createdDate = createdDate;
        this.fillLevel = fillLevel;
        this.sanitaryScore = sanitaryScore;
        this.daysSincePickup = daysSincePickup;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.priorityIndex = priorityIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public double getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(double fillLevel) {
        this.fillLevel = fillLevel;
    }

    public double getSanitaryScore() {
        return sanitaryScore;
    }

    public void setSanitaryScore(double sanitaryScore) {
        this.sanitaryScore = sanitaryScore;
    }

    public int getDaysSincePickup() {
        return daysSincePickup;
    }

    public void setDaysSincePickup(int daysSincePickup) {
        this.daysSincePickup = daysSincePickup;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getPriorityIndex() {
        return priorityIndex;
    }

    public void setPriorityIndex(double priorityIndex) {
        this.priorityIndex = priorityIndex;
    }
}
