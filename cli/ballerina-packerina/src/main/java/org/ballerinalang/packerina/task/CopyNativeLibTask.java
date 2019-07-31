/*
 * Copyright (c) 2019, WSO2 Inc. All Rights Reserved.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

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

        // Create target/tmp folder
        try {
            Files.createDirectory(tmpDir);
        } catch (IOException e) {
            throw new BallerinaException("unable to copy the native libary :" +
            e.getMessage());
        }

        // Iterate through
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        for (BLangPackage module : moduleBirMap) {
            Path baloAbsolutePath = buildContext.getBaloFromTarget(module.packageID);
            copyLibsFromBalo(baloAbsolutePath.toString(), tmpDir.toString());

        }

    }

    private static void copyLibsFromBalo(String jarFileName, String destFile) throws NullPointerException {
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
