package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Converter;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathConverter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculate path pattens within meta-inf dir of jars (or exploded jars).
 * Used to load system org bal files.
 */
public class JarRepo implements Repo<Path> {
    private final PathConverter converter;

    public JarRepo(URI jarLocation) {
        Path root;
        boolean isJar = jarLocation.getPath().endsWith(".jar");
        if (isJar) {
            root = pathWithinJar(jarLocation);
        } else {
            root = Paths.get(jarLocation);
        }
        this.converter = new PathConverter(root);
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
        }
    }

    @Override
    public Patten calculate(PackageID pkg) {
        return new Patten(Patten.path("META-INF"),
                          Patten.WILDCARD_DIR,
                          Patten.path(pkg.getName().value.replace('.', '/')), //TODO: remove replacement
                          Patten.WILDCARD_SOURCE);
    }

    @Override
    public Converter<Path> getConverterInstance() {
        return converter;
    }

    @Override
    public String toString() {
        return "{t:'JarRepo', c:'" + converter + "'}";
    }


}
