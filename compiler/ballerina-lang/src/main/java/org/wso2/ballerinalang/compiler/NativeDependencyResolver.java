package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;

import java.nio.file.Path;
import java.util.List;

/**
 * Contains methods to resolve the module jars and native libraries.
 *
 * @since 1.3.0
 */
public interface NativeDependencyResolver {

    Path moduleJar(PackageID packageID, String platform);

    List<Path> nativeDependencies(PackageID packageID);

    List<Path> nativeDependenciesForTests(PackageID packageID);

}
