package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JarRepo implements Repo<Path> {
    private final PathResolver resolver;

    public JarRepo(URI jarLocation) {
        boolean isJar = jarLocation.getPath().endsWith(".jar");
        if (isJar) {
            this.resolver = new PathResolver(pathWithinJar(jarLocation));
        } else {
            this.resolver = new PathResolver(Paths.get(jarLocation));
        }
    }

    private static Path pathWithinJar(URI pathToJar) {
        try {
            URI pathInJar = new URI("jar:" + pathToJar.getScheme(),
                                    pathToJar.getUserInfo(), pathToJar.getHost(), pathToJar.getPort(),
                                    pathToJar.getPath() + "!/",
                                    pathToJar.getQuery(), pathToJar.getFragment());
            initFS(pathInJar);
            return Paths.get(pathInJar);
        } catch (URISyntaxException | IOException e) {
            //TODO
            return null;
        }
    }

    private static void initFS(URI uri) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            FileSystems.newFileSystem(uri, env);
        } catch (FileSystemAlreadyExistsException ignore) {
            System.err.println("jar already registered.");
        }
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path("META-INF", "ballerina",
                                      pkg.getName().value.replace('.', '/')),
                          Patten.WILDCARD_BAL);
    }

    @Override
    public Resolver<Path> getResolverInstance() {
        return resolver;
    }
}
