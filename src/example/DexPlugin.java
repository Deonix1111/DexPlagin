package example;
import example.DexWebhook;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;
import mindustry.game.EventType;

//дает Events.on
import java.util.HashMap;
//соxранения url
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;



public class DexPlugin extends Plugin{
    
    private static final String CONFIG_FILE = "config/dexwebhook.properties";
    
    //слушает события 
    public DexPlugin(){
        Events.on(EventType.PlayerChatEvent.class, event -> {
            DexWebhook.sendMessage(event.message, event.player.name);
        });
        Events.on(EventType.PlayerJoin.class, event -> {
            DexWebhook.sendMessage(event.player.name, "игрок зашел");
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            DexWebhook.sendMessage(event.player.name, "игрок вышел");
        });
        
        
        
        
    }
    
    //вызывается при инициализации игры
    @Override
    public void init(){
        readConfigFile(); // Загружаем конфигурацию при загрузке плагина
        //Log.info("setdexwebhook для того чтобы вставить свою url");
        
    }
    
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("setdexwebhook", "<url>", "сюда url ссылку на вебхук", (args) -> {
            if (args.length == 0) {
                Call.sendMessage("[red]Usage: /setdexwebhook <url>");
                return;
            }

            DexWebhook.webhookUrl = args[0];
            Call.sendMessage("[green]DexWebhook установлин");
            Log.info("[green]DexWebhook установлин");
            saveConfigFile(); // Сохраняем конфигурацию после изменения
        });
        handler.register("dexhelp", "", "Показывает доступные команды DexPlugin", (args) -> {
            Call.sendMessage("[orange]Доступные команды DexPlugin:\n" +
                           "[cyan]/setdexwebhook <url>[white] - Устанавливает URL вебхука Dex.\n" +
                           "[cyan]/dexhelp[white] - Показывает это сообщение.");
            Log.info("Основаная команда setdexwebhook (url вебхука) если есть баги то можешь написать мне в discord https://discord.gg/uWnKXuaTxX ник волк "); // Исправлено Log.info
        });
    }
    
    
    
    private void readConfigFile() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        File configDir = configFile.getParentFile();
        if (configDir != null && !configDir.exists()) {
            try {
                if (configDir.mkdirs()) {
                    Log.info("Директория конфигурации создана: " + configDir.getAbsolutePath()); // Directory created
                } else {
                    Log.err("Не удалось создать директорию конфигурации: " + configDir.getAbsolutePath()); // Directory creation failed
                // Consider a fallback strategy here, like using the current directory
                    return; // Stop further execution as directory creation failed
                }
            } catch (SecurityException e) {
                Log.err("Ошибка безопасности при создании директории конфигурации: " + e.getMessage()); // Security exception
                return; // Stop execution due to security issue
            }
        }
        
        try (FileInputStream in = new FileInputStream(configFile)) {
            props.load(in);
            DexWebhook.webhookUrl = props.getProperty("webhookUrl", "<default_webhook>");
            Log.info("URL вебхука загружен из конфигурации: " ); // Webhook URL loaded
        } catch (FileNotFoundException e) {
        // File does not exist, which is normal on first run
            Log.info("Файл конфигурации не найден. Создается с URL вебхука по умолчанию."); // Config file not found
            saveConfigFile();
        } catch (IOException e) {
            Log.err("Не удалось загрузить файл конфигурации: " + e.getMessage()); // Failed to load config file
        // Consider whether to proceed with default webhook URL if loading fails
        }
    }    
    
    
    
    private void saveConfigFile() {
        Properties props = new Properties();
        props.setProperty("webhookUrl", DexWebhook.webhookUrl);
        
        File configFile = new File(CONFIG_FILE);
        File configDir = configFile.getParentFile();
        
        if (configDir != null && !configDir.exists()) {
            try {
                if (configDir.mkdirs()) {
                    Log.info("Директория конфигурации создана: " + configDir.getAbsolutePath());
                } else {
                    Log.err("Не удалось создать директорию конфигурации: " + configDir.getAbsolutePath());
                    return; // Прекращаем выполнение, если не удалось создать директорию
                }
            } catch (SecurityException e) {
                Log.err("Ошибка безопасности при создании директории конфигурации: " + e.getMessage());
                return; // Прекращаем выполнение из-за проблем с безопасностью
            }
        }

        try (FileOutputStream out = new FileOutputStream(configFile)) {
            props.store(out, "DexWebhook Configuration");
            Log.info("URL вебхука сохранен в конфигурацию: " + DexWebhook.webhookUrl);
        } catch (IOException e) {
            Log.err("Не удалось сохранить файл конфигурации: " + e.getMessage());
        }
    }
}
