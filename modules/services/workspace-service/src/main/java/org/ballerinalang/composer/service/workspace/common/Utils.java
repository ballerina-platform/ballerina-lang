package org.ballerinalang.composer.service.workspace.common;

import org.ballerinalang.composer.service.workspace.langserver.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.utils.BallerinaProgramContentProvider;
import java.util.Map;

/**
 *  Class with common utility functions used by workspace services
 */
public class Utils {
    /**
     *
     * @param directoryCount - packagePath
     * @param filePath - file path to parent directory of the .bal file
     * @return parent dir
     */
    public static java.nio.file.Path getProgramDirectory(int directoryCount, java.nio.file.Path filePath) {
        // find program directory
        java.nio.file.Path parentDir = filePath.getParent();
        for (int i = 0; i < directoryCount; ++i) {
            parentDir = parentDir.getParent();
        }
        return parentDir;
    }

    /**
     * Get all the ballerina packages associated with the runtime
     * @return - packages set
     */
    public static Map<String, ModelPackage> getAllPackages() {
        BallerinaProgramContentProvider programContentProvider = BallerinaProgramContentProvider.getInstance();
        Map<String, ModelPackage> packages = programContentProvider.getAllPackages();
        return packages;
    }
}
