package io.ballerina.projects.internal.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code CompilerPluginJson} Model for `Compiler-plugin.toml` file.
 *
 * @since 2.0.0
 */
public class CompilerPluginToml {
    private Plugin plugin;

    private static final String DEPENDENCY = "Dependency";
    @SerializedName(DEPENDENCY) private List<Dependency> dependencies;

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getCompilerPluginDependencies() {
        List<String> compilerPluginDependencies = new ArrayList<>();
        for (Dependency dependency : this.dependencies) {
            compilerPluginDependencies.add(dependency.getPath());
        }
        return compilerPluginDependencies;
    }

    public static class Plugin {
        private static final String SERIALIZED_CLASS = "class";
        @SerializedName(SERIALIZED_CLASS) private String className;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

    public static class Dependency {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
