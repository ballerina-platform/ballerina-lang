package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ReleaseVersion;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.packaging.converters.SortablePath;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * Balo cache repository.
 */
public class HomeBaloCache {

    private Path repositoryLocation;
    private List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
    private Map<ModuleId, Manifest> dependencyManifests;

    public HomeBaloCache(Map<ModuleId, Manifest> dependencyManifests) {
        this.repositoryLocation = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
        this.supportedPlatforms.add("any");
        this.dependencyManifests = dependencyManifests;
    }

    public ModuleId resolveVersion(ModuleId moduleId, ReleaseVersion releaseVersion) {
        return null;
    }

    public boolean isModuleExists(ModuleId moduleId) {
        return false;
    }

    public Module getModule(ModuleId moduleId) {
        try {
            // if path to balo is not given in the manifest file
            String orgName = moduleId.orgName;
            String moduleName = moduleId.moduleName;
            String versionStr = moduleId.version;

            // if the module doesn't exists at all stop looking for it.
            if (this.repositoryLocation.resolve(orgName).resolve(moduleName).toFile().exists()) {
                return null;
            }

            for (String platform : supportedPlatforms) {
                Path baloFilePath;
                // check if version is empty. If so get the latest balo file directly.
                if (versionStr.isEmpty()) {
                    Optional<Path> latestVersionPath = getLatestBaloFile(
                            this.repositoryLocation.resolve(orgName).resolve(moduleName));

                    if (latestVersionPath.isPresent()) {
                        Path latestVersionDirectoryName = latestVersionPath.get().getFileName();
                        if (null != latestVersionDirectoryName) {
                            versionStr = latestVersionDirectoryName.toString();
                            baloFilePath = findBaloPath(orgName, moduleName, platform, versionStr);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    // Get the existing balo file.
                    baloFilePath = findBaloPath(orgName, moduleName, platform, versionStr);
                }

                // return Module only if balo file exists.
                Path baloFileName = baloFilePath.getFileName();
                if (baloFilePath.toFile().exists() && null != baloFileName) {
                    moduleId.version = versionStr;

                    // update dependency manifests map for imports of this moduleID.
                    this.dependencyManifests
                            .put(moduleId, RepoUtils.getManifestFromBalo(baloFilePath.toAbsolutePath()));
                    return new Module(moduleId,
                            Paths.get(ProjectDirConstants.SOURCE_DIR_NAME, moduleName, String.valueOf(baloFileName)));
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get the latest balo for a given module name and platform.
     * Iterates through the module folder(<org_name>/<module-name>/*) and find the latest version.
     * Return the path to balo file with the found version.
     *
     * @param moduleFolder Folder where versions of a module is located(<org_name>/<module-name>).
     * @return The path to the balo file.
     * @throws IOException Error when getting the list of version of the module folder.
     */
    private Optional<Path> getLatestBaloFile(Path moduleFolder) throws IOException {
        Optional<Path> path;
        try (Stream<Path> fileStream = Files.list(moduleFolder)) {
            path = fileStream.map(SortablePath::new).filter(SortablePath::valid).sorted(Comparator.reverseOrder())
                    .limit(1).map(SortablePath::getPath).findFirst();
        }
        return path;
    }

    /**
     * Find balo file from given latest balo path.
     * @param orgName
     * @param pkgName
     * @param platform
     * @param versionStr
     * @return
     * @throws IOException
     */
    private Path findBaloPath(String orgName, String pkgName, String platform, String versionStr) throws IOException {
        Path baloFilePath = this.repositoryLocation.resolve(orgName).resolve(pkgName).resolve(versionStr);
        // try to find a compatible balo file
        if (baloFilePath.toFile().exists()) {
            Stream<Path> list = Files.list(baloFilePath);
            PathMatcher pathMatcher = baloFilePath.getFileSystem()
                    .getPathMatcher("glob:**/" + pkgName + "-*-" + platform + "-" + versionStr + ".balo");
            for (Path file : (Iterable<Path>) list::iterator) {
                if (pathMatcher.matches(file)) {
                    return file;
                }
            }
        }
        // if a similar file is not found assume the default balo name
        String baloFileName = pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + versionStr + ".balo";
        return baloFilePath.resolve(baloFileName);
    }
}
