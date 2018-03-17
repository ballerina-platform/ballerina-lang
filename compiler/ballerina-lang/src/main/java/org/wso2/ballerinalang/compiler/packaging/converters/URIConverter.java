package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.util.HomeRepoUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.MissingResourceException;
import java.util.stream.Stream;

/**
 * Provide functions need to covert a patten to steam of by paths, by downloading them as url .
 */
public class URIConverter implements Converter<URI> {

    public static final String BALLERINA_PULL_BALX = "/ballerina.pull.balx";
    private final URI base;
    private final EmbeddedExecutor executor;
    private final URI pullBalxLocation;

    public URIConverter(URI base) {
        this.base = base;
        executor = EmbeddedExecutorProvider.getInstance().getExecutor();
        URI uri = null;
        URL url = executor.getClass().getResource(BALLERINA_PULL_BALX);
        if (url != null) {
            try {
                uri = url.toURI();
            } catch (URISyntaxException ignore) {
            }
        }

        if (uri != null) {
            this.pullBalxLocation = uri;
        } else {
            throw new MissingResourceException("Missing balx artifact for pulling resources",
                    executor.getClass().toString(),
                    BALLERINA_PULL_BALX);
        }
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
    public Stream<URI> expand(URI u) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<URI> expandBal(URI u) {
        throw new UnsupportedOperationException();

    }

    public Stream<Path> finalize(URI u, PackageID packageID) {
        String orgName = packageID.getOrgName().getValue();
        String pkgName = packageID.getName().getValue();
        String version = packageID.getPackageVersion().getValue();
        Path destDirPath = HomeRepoUtils.createAndGetHomeReposPath().resolve("repo").resolve(orgName).resolve(pkgName)
                .resolve(version);
        createDirectory(destDirPath);
        try {
            Path destDir = destDirPath.resolve(pkgName + ".zip");
            String fullPkgPath = orgName + "/" + pkgName + ":" + version;
            executor.execute(pullBalxLocation,
                    u.toString(),
                    destDir.toString(),
                    fullPkgPath);
            return Stream.of(destDir);
        } catch (Exception ignore) {
        }
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}
