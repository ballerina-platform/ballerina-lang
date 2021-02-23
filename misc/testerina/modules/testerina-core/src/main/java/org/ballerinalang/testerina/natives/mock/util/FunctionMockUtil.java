package org.ballerinalang.testerina.natives.mock.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.test.runtime.Main.getTestExecutionDependencies;


/**
 * Class that contains utility functions related to function mocking.
 */
public class FunctionMockUtil {

    public static URLClassLoader resolveClassLoader(String moduleName) {
        List<String> testExecutionDependencies = getTestExecutionDependencies();

        // Add them to a URL list
        List<URL> urlList = new ArrayList<>();

        for (String jarFilePath : testExecutionDependencies) {
            try {
                urlList.add(Paths.get(jarFilePath).toUri().toURL());
            } catch (MalformedURLException e) {
                // This path cannot get executed
                throw new RuntimeException("Failed to create classloader with all jar files", e);
            }
        }

        // Pass the list to the URL ClassLoader
        URLClassLoader classLoader = AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>)
                        () -> new URLClassLoader(urlList.toArray(new URL[0]), ClassLoader.getSystemClassLoader()));

        return classLoader;

    }
}
