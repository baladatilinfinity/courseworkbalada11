package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Початковий послідовний сценарій CSV-імпорту (Етап III):
 * читає client/sample-data/container_sites.csv, пропускає заголовок,
 * розбирає перший рядок даних і передає майданчик серверу через HttpWasteClient.
 * <p>
 * Запускається окремо (Tomcat має бути запущений), без JavaFX Task.
 * На Етапі IV буде замінений повним фоновим імпортом через JavaFX Task і ProgressBar.
 */
public class CsvFirstRowImport {

    public static void main(String[] args) throws IOException {
        Path file = Path.of("sample-data", "container_sites.csv");
        if (!Files.exists(file)) {
            System.out.println("Файл не знайдено: " + file.toAbsolutePath());
            System.out.println("Робочий каталог запуску має бути client.");
            return;
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !SiteCsvParser.isHeader(lines.get(0))) {
            System.out.println("Перший рядок не відповідає заголовку: " + SiteCsvParser.HEADER);
            return;
        }
        if (lines.size() < 2) {
            System.out.println("У файлі немає рядків даних.");
            return;
        }

        ContainerSite site;
        try {
            site = SiteCsvParser.parseLine(lines.get(1));
        } catch (IllegalArgumentException e) {
            System.out.println("Рядок 2 відхилено: " + e.getMessage());
            return;
        }
        System.out.println("Рядок 2 розібрано: " + site.getCode() + ", " + site.getAddress()
                + ", " + site.getDistrict() + ", контейнерів " + site.getContainerCount()
                + " × " + site.getContainerVolume() + " м³, інтервал " + site.getPickupIntervalDays() + " дн.");

        try {
            new HttpWasteClient().addSite(site);
            System.out.println("Майданчик " + site.getCode() + " передано серверу і збережено.");
        } catch (RuntimeException e) {
            System.out.println("Сервер відхилив запис: " + e.getMessage());
        }
    }
}
