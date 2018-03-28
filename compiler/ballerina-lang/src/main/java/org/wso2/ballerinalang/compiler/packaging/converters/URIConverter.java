package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSourceEntry;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.util.HomeRepoUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            this.pullBalxLocation = uri;
        } else {
            throw new MissingResourceException("Missing internal modules when building package",
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

    public Stream<PackageSourceEntry> finalize(URI u, PackageID packageID) {
        String orgName = packageID.getOrgName().getValue();
        String pkgName = packageID.getName().getValue();
        Path destDirPath = HomeRepoUtils.createAndGetHomeReposPath().resolve(Paths.get("repo", orgName, pkgName));
        createDirectory(destDirPath);
        try {
            String fullPkgPath = orgName + "/" + pkgName;
            executor.execute(pullBalxLocation,
                             u.toString(),
                             destDirPath.toString(),
                             fullPkgPath,
                             File.separator);
            // TODO Simplify using ZipRepo
            Patten pattern = new Patten(Patten.WILDCARD_DIR,
                                        Patten.path(pkgName + ".zip"),
                                        Patten.path("src"), Patten.WILDCARD_SOURCE);
            return pattern.convertToSources(new ZipConverter(destDirPath), packageID);
        } catch (Exception ignore) {
        }
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}
