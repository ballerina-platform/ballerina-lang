package org.ballerinalang.packerina;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.fs.LocalFSPackageRepository;
import org.ballerinalang.spi.UserRepositoryProvider;

import java.nio.file.Path;

import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;

/**
 * Load bal files form $HOME/.ballerina.
 * <p>
 * ie: import foo.bar; will load from ~/.ballerina/artifacts/src/foo/bar/*.bal
 */
@JavaSPIService("org.ballerinalang.spi.UserRepositoryProvider")
public class HomeDirRepositoryProvider implements UserRepositoryProvider {
    @Override
    public PackageRepository loadRepository() {
        Path repoPath = UserRepositoryUtils.getUserRepositoryPath();
        Path srcDirPath = repoPath.resolve(USER_REPO_ARTIFACTS_DIRNAME).resolve(USER_REPO_SRC_DIRNAME);
        return new LocalFSPackageRepository(srcDirPath.toAbsolutePath().toString());
    }
}
