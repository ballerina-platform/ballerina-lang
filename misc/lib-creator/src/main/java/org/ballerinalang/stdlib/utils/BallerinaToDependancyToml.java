package org.ballerinalang.stdlib.utils;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BallerinaToDependancyToml {

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please specify a path");
        }
        String path = args[0];
        Path dir = Paths.get(System.getProperty("user.dir")).resolve(path);
        PathMatcher pathMatcher = FileSystems.getDefault()
                .getPathMatcher("glob:**/Ballerina.toml");
        Files.walk(Paths.get("..")).filter(pathMatcher::matches).forEach(BallerinaToDependancyToml::migrate);
    }

    public static void migrate(Path ballerinaTomlPath){
        String tomlString = null;
        TomlWriter tomlWriter = new TomlWriter();
        try {
            tomlString = Files.readString(ballerinaTomlPath);
            Toml ballerinaToml = new Toml().read(tomlString);
            if(ballerinaToml.contains("dependency")) {
                List<Toml> dependency = ballerinaToml.getList("dependency");
                Map<String, Object> dependencyToml = new HashMap<String, Object>();
                dependencyToml.put("dependency", dependency);
                Path dependencyTomlPath = ballerinaTomlPath.getParent().resolve("Dependencies.toml");
                Files.writeString(dependencyTomlPath, tomlWriter.write(dependencyToml));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
