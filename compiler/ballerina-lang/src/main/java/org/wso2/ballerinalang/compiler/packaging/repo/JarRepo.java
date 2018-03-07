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
        resolver = new PathResolver(pathWithinJar(jarLocation));
    }


    public JarRepo(Path explodedPath) {
        resolver = new PathResolver(explodedPath);
    }

    private static Path pathWithinJar(URI pathToJar) {
        try {
            URI pathInJar = new URI("jar:" + pathToJar.getScheme(),
                                    pathToJar.getUserInfo(), pathToJar.getHost(), pathToJar.getPort(),
                                    pathToJar.getPath() + "!/META-INF/",
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
        return new Patten(Patten.path("ballerina",
                                      pkg.getName().value.replace('.', '/')),
                          Patten.BAL_SANS_TEST_AND_RES);
    }

    @Override
    public Resolver<Path> getResolverInstance() {
        return resolver;
    }
}
