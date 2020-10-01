/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Write a bir to a provided path.
 */
public class BirWriter {

    /**
     * Write the compiled module to a bir file.
     *
     * @param module      module to create the BIR for
     * @param birFilePath The path to the bir file.
     */
    public static void write(Module module, Path birFilePath) {
        write(module, birFilePath, false);
    }

    /**
     * Write the compiled module to a bir file.
     *
     * @param module      module to create the BIR for
     * @param birFilePath The path to the bir file.
     * @param forceOverwrite whether the file should be overwritten
     */
    public static void write(Module module, Path birFilePath, boolean forceOverwrite) {
        BLangPackage bLangPackage = module.getCompilation().bLangPackage();
        try {
            //TODO: write our own utility for writePackage
            byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(bLangPackage.symbol.birPackageFile);
            if (forceOverwrite) {
                Files.write(birFilePath, pkgBirBinaryContent);
            } else {
                Files.write(birFilePath, pkgBirBinaryContent, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            String msg = "error writing the compiled module(BIR) of '" +
                    bLangPackage.packageID + "' to '" + birFilePath + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }
}
