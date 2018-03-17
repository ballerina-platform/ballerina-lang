package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.compiler.BLangCompilerException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide functions to convert a patten to a stream of zip paths.
 */
public class ZipConverter extends PathConverter {

    public ZipConverter(Path archivePath) {
        super(resolveIntoArchive(archivePath));
    }

    @Override
    public Path combine(Path path, String pathPart) {
        return resolveIntoArchive(path.resolve(pathPart));
    }

    private static Path resolveIntoArchive(Path newPath) {
        String pathPart = newPath.toString();
        if ((pathPart.endsWith(".zip") || pathPart.endsWith(".jar")) && Files.isRegularFile(newPath)) {
            return pathWithinZip(newPath.toUri());
        } else {
            return newPath;
        }
    }

    private static Path pathWithinZip(URI pathToZip) {
        try {
            URI pathInZip = new URI("jar:" + pathToZip.getScheme(),
                    pathToZip.getUserInfo(), pathToZip.getHost(), pathToZip.getPort(),
                    pathToZip.getPath() + "!/",
                    pathToZip.getQuery(), pathToZip.getFragment());
            initFS(pathInZip);
            return Paths.get(pathInZip);
        } catch (URISyntaxException ignore) {
        }
        return Paths.get(pathToZip);
    }

    private static void initFS(URI uri) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            FileSystems.newFileSystem(uri, env);
        } catch (FileSystemAlreadyExistsException ignore) {
        } catch (IOException e) {
            throw new BLangCompilerException("Error loading balo " + uri.getPath(), e);
        }
    }
}
