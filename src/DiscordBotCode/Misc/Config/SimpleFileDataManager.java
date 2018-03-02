package DiscordBotCode.Misc.Config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleFileDataManager implements DataManager<List<String>> {
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r\\n?|\\r?\\n");
    private final List<String> data = new ArrayList<>();
    private final Path path;

    public SimpleFileDataManager(String file) throws IOException {
        this.path = Paths.get(file);
        if (!this.path.toFile().exists()) {
            System.err.println("Could not find config file at " + this.path.toFile().getAbsolutePath() + ", creating a new one...");
            if (this.path.toFile().createNewFile()) {
                System.err.println("Generated new config file at " + this.path.toFile().getAbsolutePath() + ".");
                FileIOUtils.write(this.path, this.data.stream().collect(Collectors.joining()));
                System.err.println("Please, fill the file with valid properties.");
            } else {
                System.err.println(("Could not create config file at " + file));
            }
        }

        Collections.addAll(data, NEWLINE_PATTERN.split(FileIOUtils.read(this.path)));
        data.removeIf(s -> s.startsWith("//"));
    }

    @Override
    public List<String> get() {
        return data;
    }

    @Override
    public void save() throws IOException {
        FileIOUtils.write(path, this.data.stream().collect(Collectors.joining("\n")));
    }
}
