package org.wso2.ballerinalang.util;

import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Toml parser util methods.
 *
 * @since 0.982.0
 */
public class TomlParserUtils {
    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    public static Settings readSettings() {
        Path settingsFilePath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.SETTINGS_FILE_NAME);
        try {
            return SettingsProcessor.parseTomlContentFromFile(settingsFilePath.toString());
        } catch (IOException e) {
            return new Settings();
        }
    }

    /**
     * Read Ballerina.toml (Manifest) to populate the configurations.
     *
     * @param projectDirPath Project Directory Path
     * @return {@link Manifest} manifest object
     */
    public static Manifest getManifest(Path projectDirPath) {
        Path manifestFilePath = projectDirPath.resolve((ProjectDirConstants.MANIFEST_FILE_NAME));
        try {
            return ManifestProcessor.parseTomlContentFromFile(manifestFilePath.toString());
        } catch (IOException e) {
            return new Manifest();
        }
    }
}
