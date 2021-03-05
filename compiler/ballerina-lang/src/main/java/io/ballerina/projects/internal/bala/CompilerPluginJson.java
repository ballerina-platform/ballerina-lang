package io.ballerina.projects.internal.bala;

import java.util.List;

/**
 * {@code CompilerPluginJson} Model for compiler plugin JSON file.
 *
 * @since 2.0.0
 */
public class CompilerPluginJson {
    private String plugin_class;
    private List<String> dependency_paths;

    public CompilerPluginJson(String pluginClass, List<String> dependencyPaths) {
        this.plugin_class = pluginClass;
        this.dependency_paths = dependencyPaths;
    }

    public String pluginClass() {
        return plugin_class;
    }

    public List<String> dependencyPaths() {
        return dependency_paths;
    }
}
