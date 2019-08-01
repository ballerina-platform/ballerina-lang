/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *  Copy native libraries to target/tmp.
 */
public class CopyNativeLibTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {

        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        Path tmpDir = targetDir.resolve(ProjectDirConstants.TARGET_TMP_DIRECTORY);
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        // Create target/tmp folder
        try {
            Files.createDirectory(tmpDir);
        } catch (IOException e) {
            throw new BallerinaException("unable to copy the native libary :" +
                    e.getMessage());
        }
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Iterate through the imports and copy dependencies.
        for (BLangPackage pkg : moduleBirMap) {
            for (BPackageSymbol importz : pkg.symbol.imports) {
                Path importJar = findImportBaloPath(buildContext, importz, sourceRootPath);
                if (importJar != null && Files.exists(importJar)) {
                    copyLibsFromBalo(importJar.toString(), tmpDir.toString());
                }
            }
        }
        // Iterate through module  balo
        for (BLangPackage module : moduleBirMap) {
            Path baloAbsolutePath = buildContext.getBaloFromTarget(module.packageID);
            copyLibsFromBalo(baloAbsolutePath.toString(), tmpDir.toString());
        }
    }

    private static Path findImportBaloPath(BuildContext buildContext, BPackageSymbol importz, Path project) {
        // Get the jar paths
        PackageID id = importz.pkgID;

        // Skip ballerina and ballerinax
        if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
            return null;
        }
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
            // If so fetch from project balo cache
            return buildContext.getBaloFromTarget(id);
        } else {
            // If not fetch from home balo cache.
            return buildContext.getBaloFromHomeCache(id);
        }
        // return the path
    }

    private  void copyLibsFromBalo(String jarFileName, String destFile) throws NullPointerException {
        try (JarFile jar = new JarFile(jarFileName)) {

            java.util.Enumeration enumEntries = jar.entries();

            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().contains(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME)) {
                    File f = new File(destFile + File.separator +
                            file.getName().split(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME)[1]);
                    if (file.isDirectory()) { // if its a directory, ignore
                        continue;
                    }
                    // get the input stream
                    try (InputStream is = jar.getInputStream(file); FileOutputStream fos = new FileOutputStream(f)) {
                        while (is.available() > 0) {  // write contents of 'is' to 'fos'
                            fos.write(is.read());
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new BLangCompilerException("Unable to copy native jar :" + e.getMessage());
        }
    }

}

