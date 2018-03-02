package DiscordBotCode.Misc.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class GsonDataManager<T> implements DataManager<T> {
    public static final Gson GSON_PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create(), GSON_UNPRETTY = new GsonBuilder().serializeNulls().create();
    private final Path configPath;
    private final T data;

    public GsonDataManager(Class<T> clazz, String file, Supplier<T> constructor) throws IOException {
        this.configPath = Paths.get(file);
        if (!configPath.toFile().exists()) {
            System.err.println("Could not find config file at " + configPath.toFile().getAbsolutePath() + ", creating a new one...");
            if (configPath.toFile().createNewFile()) {
                System.err.println("Generated new config file at " + configPath.toFile().getAbsolutePath() + ".");
                FileIOUtils.write(configPath, GSON_PRETTY.toJson(constructor.get()));
                System.err.println("Please, fill the file with valid properties.");
            } else {
                System.err.println("Could not create config file at " + file);
            }
//            System.exit(0);  //Why was this a thing!?
        }

        this.data = GSON_PRETTY.fromJson(FileIOUtils.read(configPath), clazz);
    }

    public static Gson gson(boolean pretty) {
        return pretty ? GSON_PRETTY : GSON_UNPRETTY;
    }

    @Override
    public T get() {
        return data;
    }

    @Override
    public void save() throws IOException {
        FileIOUtils.write(configPath, GSON_PRETTY.toJson(data));
    }
}
