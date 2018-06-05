/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Write Ballerina.lock to target after every build.
 *
 * @since 0.973.1
 */
public class LockFileWriter {
    private static final CompilerContext.Key<LockFileWriter> LOCK_FILE_WRITER_KEY = new CompilerContext.Key<>();
    private static List<StringBuilder> builderList = new ArrayList<>();
    private final SourceDirectory sourceDirectory;

    /**
     * Constructor of LockFileWriter.
     *
     * @param context compiler context
     */
    private LockFileWriter(CompilerContext context) {
        context.put(LOCK_FILE_WRITER_KEY, this);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
    }

    /**
     * Get an instance of the LockFileWriter.
     *
     * @param context compiler context
     * @return instance of the LockFileWriter
     */
    public static LockFileWriter getInstance(CompilerContext context) {
        LockFileWriter lockFileWriter = context.get(LOCK_FILE_WRITER_KEY);
        if (lockFileWriter == null) {
            lockFileWriter = new LockFileWriter(context);
        }
        return lockFileWriter;
    }

    /**
     * Generate list of dependencies of package.
     *
     * @param packageNode package node
     * @param depth       depth
     * @return list of dependencies of the package node
     */
    private static List<StringBuilder> generatePkgDependencies(BPackageSymbol packageNode, int depth) {
        List<StringBuilder> result = new LinkedList<>();
        if (depth > 0) {
            result.add(new StringBuilder().append("[\"").append(packageNode.pkgID.orgName).append("/")
                                          .append(packageNode.pkgID.name).append("\"]"));
        }
        List<BPackageSymbol> importPackages = getImportPackages(packageNode);
        if (importPackages.size() > 0) {
            for (BPackageSymbol importPackage : importPackages) {
                List<StringBuilder> subtree = generatePkgDependencies(importPackage, 1);
                generateDependancyStr(result, subtree, importPackage.pkgID.version.getValue());
            }
        }
        return result;
    }

    /**
     * Get import packages of package.
     *
     * @param packageNode packafe node
     * @return import package list
     */
    private static List<BPackageSymbol> getImportPackages(BPackageSymbol packageNode) {
        return packageNode.imports.stream()
                                  .filter(pkg -> !pkg.pkgID.orgName.value
                                          .equals(LockFileConstants.BALLERINA))
                                  .collect(Collectors.toList());
    }

    /**
     * Add dependency of package as a string.
     *
     * @param result     list of dependency strings
     * @param subtree    list of dependency strings of the subtree
     * @param pkgVersion package version
     */
    private static void generateDependancyStr(List<StringBuilder> result, List<StringBuilder> subtree,
                                              String pkgVersion) {
        Iterator<StringBuilder> iterator = subtree.iterator();
        if (iterator.hasNext()) {
            result.add(iterator.next().insert(0, "    "));
            result.add(new StringBuilder().append(LockFileConstants.VERSION + "= \"").append(pkgVersion).append("\"\n")
                                          .insert(0, "    "));
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
    }

    /**
     * Get ballerina version.
     *
     * @return ballerina version
     */
    private static String getBallerinaVersion() {
        try (InputStream inputStream = LockFileWriter.class
                .getResourceAsStream(LockFileConstants.LAUNCHER_PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(LockFileConstants.BALLERINA_VERSION_PROPERTY);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }

    /**
     * Generate the project description.
     *
     * @param manifest manfiest object
     * @return string with the project description
     */
    private StringBuilder generateProjectDesc(Manifest manifest) {
        return new StringBuilder()
                .append("[" + LockFileConstants.PROJECT_LOCK + "]").append("\n")
                .append(LockFileConstants.ORG_NAME).append(" = ").append(manifest.getName()).append("\n")
                .append(LockFileConstants.VERSION).append(" = ").append(manifest.getVersion()).append("\n")
                .append(LockFileConstants.LOCKFILE_VERSION).append(" = 1").append("\n")
                .append(LockFileConstants.BALLERINA_VERSION).append(" = ").append(getBallerinaVersion()).append("\n");
    }

    /**
     * Get dependencies of package.
     *
     * @param packageNode package node
     */
    void generatePkgDependencies(BLangPackage packageNode) {
        builderList.add(new StringBuilder().append("[" + LockFileConstants.DEPENDENCIES + ".")
                                           .append(packageNode.packageID.name).append("] \n"));
        builderList.addAll(generatePkgDependencies(packageNode.symbol, 0));
    }

    /**
     * Write Ballerina.lock file.
     *
     * @param manifest manifest object
     */
    void writeLockFile(Manifest manifest) {
        Path ballerinaLockFilePath = this.sourceDirectory.getPath().resolve(ProjectDirConstants.TARGET_DIR_NAME)
                                                         .resolve(Paths.get(LockFileConstants
                                                                                    .BALLERINA_LOCK_FILE_NAME));
        builderList.add(0, generateProjectDesc(manifest));

        StringBuilder sb = new StringBuilder(builderList.size() * 10);
        for (StringBuilder line : builderList) {
            sb.append(line);
            sb.append("\n");
        }
        try {
            Files.write(ballerinaLockFilePath, sb.toString().getBytes());
        } catch (IOException ignore) {
        }
    }
}
