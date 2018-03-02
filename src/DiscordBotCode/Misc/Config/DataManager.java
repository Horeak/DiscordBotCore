package DiscordBotCode.Misc.Config;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

public interface DataManager<T> extends Supplier<T>, Closeable {
    void save() throws IOException;

    @Override
    default void close() throws IOException {
        save();
    }
}
