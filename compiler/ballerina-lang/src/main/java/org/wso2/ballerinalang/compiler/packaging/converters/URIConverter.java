package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Provide functions need to covert a patten to steam of by paths, by downloading them as url .
 */
public class URIConverter implements Converter<URI> {

    private static CacheRepo binaryRepo = new CacheRepo(RepoUtils.createAndGetHomeReposPath(),
                                                        ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME);
    private final URI base;
    private boolean isBuild = true;
    private PrintStream outStream = System.err;

    public URIConverter(URI base) {
        this.base = base;
    }

    public URIConverter(URI base, boolean isBuild) {
        this.base = base;
        this.isBuild = isBuild;
    }

    /**
     * Create the dir path provided.
     *
     * @param dirPath destination dir path
     */
    public void createDirectory(Path dirPath) {
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException("Error occured when creating the directory path " + dirPath);
            }
        }
    }

    @Override
    public URI start() {
        return base;
    }

    @Override
    public URI combine(URI s, String p) {
        return s.resolve(p + '/');
    }

    @Override
    public Stream<URI> latest(URI u, PackageID packageID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<URI> expandBalWithTest(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<URI> expandBal(URI u) {
        throw new UnsupportedOperationException();

    }

    public Stream<CompilerInput> finalize(URI u, PackageID packageID) {
        String orgName = packageID.getOrgName().getValue();
        String pkgName = packageID.getName().getValue();
        Path destDirPath = RepoUtils.createAndGetHomeReposPath().resolve(Paths.get(ProjectDirConstants.CACHES_DIR_NAME,
                                                                                   ProjectDirConstants
                                                                                           .BALLERINA_CENTRAL_DIR_NAME,
                                                                                   orgName, pkgName));
        createDirectory(destDirPath);
        try {
            String fullPkgPath = orgName + "/" + pkgName;
            Proxy proxy = RepoUtils.readSettings().getProxy();

            String supportedVersionRange = "?supported-version-range=" + ProgramFileConstants.MIN_SUPPORTED_VERSION +
                    "," + ProgramFileConstants.MAX_SUPPORTED_VERSION;
            EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();
            executor.execute("packaging_pull/packaging_pull.balx", true, u.toString(), destDirPath.toString(),
                             fullPkgPath, File.separator, proxy.getHost(), proxy.getPort(), proxy.getUserName(),
                             proxy.getPassword(), RepoUtils.getTerminalWidth(), supportedVersionRange,
                             String.valueOf(isBuild));

            Patten patten = binaryRepo.calculate(packageID);
            return patten.convertToSources(binaryRepo.getConverterInstance(), packageID);
        } catch (Exception e) {
            outStream.println(isBuild ? "    " : "" + "Error occurred when pulling the remote artifact");
        }
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}
