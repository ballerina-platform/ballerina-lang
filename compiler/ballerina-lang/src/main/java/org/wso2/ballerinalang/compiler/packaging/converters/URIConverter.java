package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorProvider;

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

    public static final String BALLERINA_PULL_BALX = "/pull.bal.balx";
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
        try {
            Path tmp = Files.createTempDirectory("bal-download-tmp-");
            String pkgName =  packageID.getName().getValue();
            String destDir = tmp.resolve(pkgName + ".zip").toString();
            String fullPkgPath = packageID.getOrgName() + "/" + packageID.getName() + ":" + packageID.getPackageVersion();
            executor.execute(pullBalxLocation,
                             u.toString(),
                             destDir,
                             fullPkgPath);
            return Files.find(tmp, 1, (s, b) -> {
                Path fileName = s.getFileName();
                return fileName != null && fileName.endsWith(".zip");
            });
        } catch (Exception ignore) {
        }
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}
