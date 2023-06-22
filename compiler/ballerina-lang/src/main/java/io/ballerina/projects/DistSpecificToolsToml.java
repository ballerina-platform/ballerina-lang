package io.ballerina.projects;

import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.ANY_PLATFORM;

public class DistSpecificToolsToml extends BalToolsToml {

    private DistSpecificToolsToml(Path distSpecificToolsTomlPath) {
        super(distSpecificToolsTomlPath);
    }

    public static DistSpecificToolsToml from(Path distSpecificToolsTomlPath, Path globalToolsTomlPath,
                                    Path distPackedToolsTomlPath) {
        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        BalToolsManifest globalToolsManifest = BalToolsManifestBuilder.from(globalToolsToml).build();

        DistSpecificToolsToml distSpecificToolsToml;
        BalToolsManifest distSpecificToolsManifest;
        if (distSpecificToolsTomlPath.toFile().exists()) {
            distSpecificToolsToml = new DistSpecificToolsToml(distSpecificToolsTomlPath);
            distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();

            // if dist specific toml contains any tool which is not in global toml, remove it from dist specific toml.
            // this is because the tool is already globally removed previously in a different distribution version.
            distSpecificToolsManifest.tools().forEach((toolName, tool) -> {
                if (!globalToolsManifest.tools().containsKey(toolName)) {
                    distSpecificToolsManifest.removeTool(toolName);
                }
            });
        } else {
            try {
                Path parentDirectory = distSpecificToolsTomlPath.getParent();
                if (parentDirectory != null && !parentDirectory.toFile().exists()) {
                    Files.createDirectories(parentDirectory);
                }
                Files.createFile(distSpecificToolsTomlPath);

                BalToolsToml distPackedToolsToml = BalToolsToml.from(distPackedToolsTomlPath);
                BalToolsManifest distPackedToolsManifest = BalToolsManifestBuilder.from(distPackedToolsToml).build();

                distSpecificToolsToml = new DistSpecificToolsToml(distSpecificToolsTomlPath);
                distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();

                distPackedToolsManifest.tools().forEach((toolName, tool) -> {
                    // Copy from the toml packed in distribution
                    distSpecificToolsManifest.addTool(toolName, tool.org(), tool.name(), tool.version());

                    // Update the global bal-tools.toml
                    if (!globalToolsManifest.tools().containsKey(toolName)) {
                        globalToolsManifest.addTool(toolName, tool.org(), tool.name(), null);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error while creating bal-tools.toml :" + e);
            }
        }

        updateDistSpecificManifestFromGlobalManifest(distSpecificToolsManifest, globalToolsManifest);

        distSpecificToolsToml.modify(distSpecificToolsManifest);
        return distSpecificToolsToml;
    }

    private static void updateDistSpecificManifestFromGlobalManifest(BalToolsManifest distSpecificToolsManifest,
                                                                     BalToolsManifest globalToolsManifest) {
        globalToolsManifest.tools().forEach((toolName, tool) -> {
            if (!distSpecificToolsManifest.tools().containsKey(toolName)) {
                Path toolCacheDir = ProjectUtils.createAndGetHomeReposPath()
                        .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME).resolve(tool.org()).resolve(tool.name());
                if (toolCacheDir.toFile().isDirectory()) {
                    Optional<SemanticVersion> matchingVersion =
                            checkForLatestLocalToolVersionMatchingCurrDist(toolCacheDir);
                    if (matchingVersion.isPresent()) {
                        distSpecificToolsManifest.addTool(
                                toolName, tool.org(), tool.name(), matchingVersion.get().toString());
                    }
                }
                // if there is no matching version, we do not add anything to dist-specific tool.
                // No match is because the tool is not compatible with the current distribution version or
                // the tool is distribution specific or a specific version of the tool has been deleted
            }
        });
    }

    /**
     * Check for the latest tool version that is compatible with the current distribution version.
     *
     * @param toolCacheDir
     * @return latest compatible locally available tool version
     */
    private static Optional<SemanticVersion> checkForLatestLocalToolVersionMatchingCurrDist(Path toolCacheDir) {
        try {
            return Files.list(toolCacheDir)
                    .filter(DistSpecificToolsToml::isToolDistVersionCompatibleWithLocalDistVersion)
                    .map(path -> path.getFileName().toString())
                    .map(SemanticVersion::from)
                    .max((v1, v2) -> {
                        if (v1.greaterThan(v2)) {
                            return 1;
                        } else if (v2.greaterThan(v1)) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the tool is built with an older or same distribution version as the current distribution version.
     *
     * @param toolCacheVersionDir path to the tool version directory in the central cache
     * @return true if tool distribution version is compatible, false otherwise
     */
    private static boolean isToolDistVersionCompatibleWithLocalDistVersion(Path toolCacheVersionDir) {
        Path balaPath = toolCacheVersionDir.resolve(ANY_PLATFORM);
        if (Files.exists(balaPath)) {
            PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
            SemanticVersion toolDistVersion = SemanticVersion.from(packageJson.getBallerinaVersion());
            SemanticVersion localDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
            return localDistVersion.greaterThanOrEqualTo(toolDistVersion);
        }
        return false;
    }
}
