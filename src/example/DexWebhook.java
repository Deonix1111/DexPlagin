package example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DexWebhook {
    
    private static final String LOG_FILE = "config/logs/discord_webhook.log"; 
    //private static final String LOG_FILE = "discord_webhook.log"; // Имя файла лога
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static String webhookUrl = "<default_webhook>";
    
    public static void sendMessage(String message, String name) {
        new Thread(() -> {
            try {
                // Используем static переменную webhookUrl
                URL url = new URL(webhookUrl);
                
                
                
                
                String jsonBrut = "{\"embeds\": [{"
                        + "\"title\": \"" + name + "\","
                        + "\"description\": \"" + message + "\","
                        + "\"color\": 8379590"
                        + "}]}";

                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBrut.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    logToFile("Сообщение от пользователя " + name + " отправлено в Discord успешно!");
                } else {
                    String errorBody = "";
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            errorBody += line + "\n";
                        }
                    }
                    logToFile("Ошибка отправки сообщения в Discord от пользователя " + name + ". Код состояния: " + responseCode + ". Тело ошибки: " + errorBody);
                }
            } catch (Exception e) {
                logToFile("Произошла ошибка при отправке сообщения от пользователя " + name + ": " + e.getMessage());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logToFile("StackTrace: " + sw.toString());  // Записываем полный stack trace для отладки
            }
        }).start();
    }

    private static void logToFile(String message) {
        //System.out.println("Текущая рабочая директория: " + System.getProperty("user.dir"));
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);
        String logEntry = timestamp + " - " + message + "\n";

        try {
            Path logPath = Paths.get(LOG_FILE);

            // Создаем директории, если их нет
            Path parentDir = logPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir); // Создаем все необходимые директории в пути
            }

            Files.write(logPath, logEntry.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог-файл: " + e.getMessage());
            e.printStackTrace();
        }
    }
}