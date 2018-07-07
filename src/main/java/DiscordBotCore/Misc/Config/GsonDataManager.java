package DiscordBotCore.Misc.Config;

import DiscordBotCore.Main.DiscordBotBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class GsonDataManager<T> implements DataManager<T> {
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Path configPath;
    private final T data;

    public GsonDataManager(Class<T> clazz, String file, Supplier<T> constructor) throws IOException {
        this.configPath = Paths.get(file);
        if (!configPath.toFile().exists()) {
            
            if (configPath.toFile().createNewFile()) {
                FileIOUtils.write(configPath, GSON_PRETTY.toJson(constructor.get()));
            }
        }

        this.data = GSON_PRETTY.fromJson(FileIOUtils.read(configPath), clazz);
    }
    
    @Override
    public T get() {
        return data;
    }

    @Override
    public void save() {
        try {
            FileIOUtils.write(configPath, GSON_PRETTY.toJson(data));
        } catch (IOException e) {
            DiscordBotBase.handleException(e);
        }
    }
}
