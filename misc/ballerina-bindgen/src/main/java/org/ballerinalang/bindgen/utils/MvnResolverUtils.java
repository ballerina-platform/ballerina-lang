package org.ballerinalang.bindgen.utils;

import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.maven.Dependency;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.exceptions.MavenResolverException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.bindgen.command.BindingsGenerator.setClassPaths;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MVN_REPO;
import static org.ballerinalang.bindgen.utils.BindgenConstants.TARGET_DIR;

public class MvnResolverUtils {

    private MvnResolverUtils() {
    }

    public static void mavenResolver(String groupId, String artifactId, String version, Path projectRoot)
            throws BindgenException {
        Path mvnRepository = Paths.get(projectRoot.toString(), TARGET_DIR, MVN_REPO);
        populateBallerinaToml(groupId, artifactId, version, mvnRepository.toString(), false);
        MavenResolver resolver = new MavenResolver(mvnRepository.toString());
        try {
            Dependency dependency = resolver.resolve(groupId, artifactId, version, true);
            dependencyTraversal(dependency, mvnRepository.toString());
        } catch (MavenResolverException e) {
            throw new BindgenException(e.getMessage());
        }
    }

    private static void dependencyTraversal(Dependency dependency, String mvnRepository) {
        if (dependency.getDepedencies() == null) {
            return;
        }
        for (Dependency transitive: dependency.getDepedencies()) {
            populateBallerinaToml(transitive.getGroupId(), transitive.getArtifactId(), transitive.getVersion(),
                    mvnRepository, true);
            dependencyTraversal(transitive, mvnRepository);
        }
    }

    private static void populateBallerinaToml(String groupId, String artifactId, String version, String mvnRepository,
                                              Boolean isTransitive) {
        Path mvnPath = Paths.get(mvnRepository, getPathFromGroupId(groupId), artifactId, version);
        setClassPaths(mvnPath.toString());
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
