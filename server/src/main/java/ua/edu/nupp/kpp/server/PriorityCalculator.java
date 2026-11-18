package ua.edu.nupp.kpp.server;

/**
 * Розрахунок інтегрального показника пріоритетності заявки:
 * I = 0,40·F1 + 0,35·F2 + 0,25·F3, усі фактори в діапазоні 0..100.
 * <ul>
 *   <li>F1 — рівень заповнення контейнерів, % (вводиться вже нормованим);</li>
 *   <li>F2 — санітарний ризик, експертна оцінка 0..100 (вводиться вже нормованою);</li>
 *   <li>F3 — прострочення вивезення: min(100; max(0; d − T) / T · 100),
 *       де d — днів від останнього вивезення, T — нормативний інтервал майданчика.</li>
 * </ul>
 * Значення округлюються до двох знаків після коми.
 */
public final class PriorityCalculator {

    public static final double W1 = 0.40;
    public static final double W2 = 0.35;
    public static final double W3 = 0.25;

    /** Нижня межа високого пріоритету. */
    public static final double HIGH_LEVEL = 70.0;
    /** Нижня межа середнього пріоритету. */
    public static final double MEDIUM_LEVEL = 40.0;

    private PriorityCalculator() {
    }

    /** F1 — рівень заповнення, %. */
    public static double factor1(double fillLevel) {
        check(fillLevel, "Рівень заповнення (F1) має бути в діапазоні 0..100");
        return round2(fillLevel);
    }

    /** F2 — санітарний ризик, бал 0..100. */
    public static double factor2(double sanitaryScore) {
        check(sanitaryScore, "Санітарний ризик (F2) має бути в діапазоні 0..100");
        return round2(sanitaryScore);
    }

    /** F3 — прострочення відносно нормативного інтервалу вивезення. */
    public static double factor3(int daysSincePickup, int pickupIntervalDays) {
        if (daysSincePickup < 0 || pickupIntervalDays <= 0) {
            throw new IllegalArgumentException("Некоректні дані для розрахунку F3");
        }
        double overdueDays = Math.max(0, daysSincePickup - pickupIntervalDays);
        double value = overdueDays / pickupIntervalDays * 100.0;
        return round2(Math.min(100.0, value));
    }

    /** Інтегральний показник пріоритетності. */
    public static double calculate(double f1, double f2, double f3) {
        String message = "Фактор має бути в діапазоні 0..100";
        check(f1, message);
        check(f2, message);
        check(f3, message);
        return round2(W1 * f1 + W2 * f2 + W3 * f3);
    }

    /** Рівень пріоритету для інтерпретації значення показника. */
    public static String level(double priorityIndex) {
        if (priorityIndex >= HIGH_LEVEL) {
            return "ВИСОКИЙ";
        }
        if (priorityIndex >= MEDIUM_LEVEL) {
            return "СЕРЕДНІЙ";
        }
        return "НИЗЬКИЙ";
    }

    private static void check(double value, String message) {
        if (!(value >= 0.0 && value <= 100.0)) {   // так відсікається і NaN
            throw new IllegalArgumentException(message);
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
