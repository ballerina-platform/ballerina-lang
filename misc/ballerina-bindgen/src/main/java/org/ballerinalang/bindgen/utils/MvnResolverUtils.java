package org.ballerinalang.bindgen.utils;

import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.maven.Dependency;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Platform;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.bindgen.command.BindingsGenerator.getOutputPath;
import static org.ballerinalang.bindgen.command.BindingsGenerator.setClassPaths;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_TOML;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MVN_REPO;
import static org.ballerinalang.bindgen.utils.BindgenConstants.TARGET_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;

public class MvnResolverUtils {

    private MvnResolverUtils() {
    }

    public static void mavenResolver(String groupId, String artifactId, String version, Path projectRoot)
            throws BindgenException {
        Path mvnRepository;
        if (projectRoot == null) {
            if (getOutputPath() != null) {
                mvnRepository = Paths.get(getOutputPath(), TARGET_DIR, MVN_REPO);
            } else {
                mvnRepository = Paths.get(System.getProperty(USER_DIR), TARGET_DIR, MVN_REPO);
            }
        } else {
            mvnRepository = Paths.get(projectRoot.toString(), TARGET_DIR, MVN_REPO);
        }
        handleDependency(groupId, artifactId, version, mvnRepository.toString(), projectRoot, false);
        MavenResolver resolver = new MavenResolver(mvnRepository.toString());
        try {
            Dependency dependency = resolver.resolve(groupId, artifactId, version, true);
            dependencyTraversal(dependency, mvnRepository.toString(), projectRoot);
        } catch (MavenResolverException e) {
            throw new BindgenException(e.getMessage());
        }
    }

    private static void dependencyTraversal(Dependency dependency, String mvnRepository, Path projectRoot) throws BindgenException {
        if (dependency.getDepedencies() == null) {
            return;
        }
        for (Dependency transitive: dependency.getDepedencies()) {
            handleDependency(transitive.getGroupId(), transitive.getArtifactId(), transitive.getVersion(),
                    mvnRepository, projectRoot, true);
            dependencyTraversal(transitive, mvnRepository, projectRoot);
        }
    }

    private static void handleDependency(String groupId, String artifactId, String version, String mvnRepository,
                                         Path projectRoot, Boolean isTransitive) throws BindgenException {
        Path mvnPath = Paths.get(mvnRepository, getPathFromGroupId(groupId), artifactId, version);
        setClassPaths(mvnPath.toString());
        if (projectRoot != null) {
            File tomlFile = new File(Paths.get(projectRoot.toString(), BALLERINA_TOML).toString());
            if(tomlFile.exists() && !tomlFile.isDirectory()) {
                populateBallerinaToml(groupId, artifactId, version, tomlFile, projectRoot, isTransitive);
            }
        }
    }

    private static void populateBallerinaToml(String groupId, String artifactId, String version, File tomlFile,
                                              Path projectRoot, boolean isTransitive) throws BindgenException {
        Manifest manifest = TomlParserUtils.getManifest(projectRoot);
        try (FileWriter fileWriter = new FileWriter(tomlFile, true)) {
            Platform platform = manifest.getPlatform();
            if (platform.target == null && platform.libraries == null) {
                fileWriter.write("\n\n[platform]\n");
                fileWriter.write("target = \"java8\"\n");
            } else {
                for (Library library:platform.getLibraries()) {
                    if (library.groupId != null && library.artifactId != null && library.version != null &&
                            library.groupId.equals(groupId) && library.artifactId.equals(artifactId)
                                && library.version.equals(version)) {
                        return;
                    }
                }
            }
            fileWriter.write("\n");
            if (isTransitive) {
                fileWriter.write("# Transitive Dependency\n");
            }
            fileWriter.write("[[platform.libraries]]\n");
            fileWriter.write("groupId = \"" + groupId + "\"\n");
            fileWriter.write("artifactId = \"" + artifactId + "\"\n");
            fileWriter.write("version = \"" + version + "\"\n");
        } catch (IOException io) {
            throw new BindgenException("Error while updating the Ballerina.toml file.");
        }
    }

    private static String getPathFromGroupId(String groupId) {
        String[] paths = groupId.split("\\.");
        if (paths.length == 0) {
            return groupId;
        }
        File combined = new File(paths[0]);
        for (int i = 1; i < paths.length ; i++) {
            combined = new File(combined, paths[i]);
        }
        return combined.getPath();
    }
}
