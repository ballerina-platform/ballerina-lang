package org.ballerinalang.langserver.hover.util;

import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds last build of the project for the hover operations.
 */
public class HoverCacheHolder {
    private static volatile Map<String, List<BLangPackage>> cachedBuild = new HashMap<>();

    private HoverCacheHolder() {
    }

    /**
     * Set cache.
     *
     * @param lsDocument {@link LSDocument}
     * @param build      build package
     */
    public static void put(LSDocument lsDocument, List<BLangPackage> build) {
        cachedBuild.put(lsDocument.getProjectRoot(), build);
    }

    /**
     * Get cache.
     *
     * @param lsDocument {@link LSDocument}
     * @return list of {@link BLangPackage}
     */
    public static List<BLangPackage> get(LSDocument lsDocument) {
        return cachedBuild.get(lsDocument.getProjectRoot());
    }
}
