package com.restoration;

import com.restoration.db.BedrockDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;

public class BedrockRestorerMod implements ModInitializer {
    public static final String MOD_ID = "bedrock_restorer";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            File bedrockDbDir = FabricLoader.getInstance().getGameDir().resolve("bedrock_world/db").toFile();
            if (bedrockDbDir.exists()) {
                try {
                    BedrockDatabase.open(bedrockDbDir);
                    System.out.println("[BedrockRestorer] LevelDB успешно подключена из " + bedrockDbDir.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("[BedrockRestorer] Ошибка подключения к LevelDB: " + e.getMessage());
                }
            } else {
                System.err.println("[BedrockRestorer] ВНИМАНИЕ: Папка мира Bedrock не найдена по пути: " + bedrockDbDir.getAbsolutePath());
                System.err.println("[BedrockRestorer] Создайте папку 'bedrock_world/db' в папке игры и скопируйте туда базу данных исходного мира.");
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                BedrockDatabase.close();
                System.out.println("[BedrockRestorer] База LevelDB успешно закрыта.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
