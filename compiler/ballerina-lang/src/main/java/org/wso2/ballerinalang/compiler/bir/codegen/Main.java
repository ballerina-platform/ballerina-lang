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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.compiledPkgCache;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.dlogger;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.externalMapCache;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.generatePackage;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.readFileFully;


//import ballerina/io;
//import ballerina/bir;
//import ballerina/jvm;
//import ballerina/stringutils;
//import ballerina/lang.'int as lint;

public class Main {
    public static List<String> birCacheDirs = new ArrayList<>();
    public static List<String> jarLibraries = new ArrayList<>();

    public static void main(String... args) {
        String pathToEntryBir = args.get(0);
        String mapPath = args.get(1);
        String targetPath = args.get(2);
        boolean dumpBir = stringutils:equalsIgnoreCase(args.get(3), "true");
        boolean useSystemClassLoader = stringutils:equalsIgnoreCase(args.get(4), "true");
        int numCacheDirs = checkpanic lint:
        fromString(args.get(5));

        int i = 0;
        while (i < numCacheDirs) {
            birCacheDirs.add(i, args[6 + i]);
            i = i + 1;
        }
        // main will receive the no of cache directories as 6th arg. Then we read the rest of the args as cache directories
        // based on 6th arg value.
        int argsCount = 6 + numCacheDirs;
        int dependentJarCnt = args.size() - argsCount;
        i = 0;
        while (i < dependentJarCnt) {
            jarLibraries.add(i, args[argsCount + i]);
            i = i + 1;
        }

        var jarFile = generateJarBinary(pathToEntryBir, mapPath, dumpBir, jarLibraries, useSystemClassLoader);
        if (dlogger.getErrorCount() > 0) {
            dlogger.printErrors();
            systemExit(1);
            return;
        }

        writeJarFile(jarFile, targetPath);
    }

    static JarFile generateJarBinary(String pathToEntryBir, String mapPath, boolean dumpBir,
                                     List<String> jarLibraries, boolean useSystemClassLoader) {
        if (!isEmpty(mapPath)) {
            externalMapCache = readMap(mapPath);
        }

        byte[] moduleBytes = readFileFully(pathToEntryBir);
        BIRPackage entryMod = BIRpopulateBIRModuleFromBinary(moduleBytes, false);
        compiledPkgCache[entryMod.org.value + entryMod.name.value] = entryMod;

        if (dumpBir) {
            io:
            println(BIRemitModule(entryMod));
        }

        JarFile jarFile = null;
        InteropValidator interopValidator = new InteropValidator(jarLibraries, useSystemClassLoader);
        generatePackage(createModuleId(entryMod.org.value, entryMod.name.value,
                entryMod.version.value), jarFile, interopValidator, true);
        return jarFile;
    }

    static boolean isEmpty(String s) {
        return s.trim() == "";
    }

    static Map<String, String> readMap(String path) {
        var rbc = io:openReadableFile(path);
        if (rbc instanceof BLangCompilerException) {
            BLangCompilerException openError = (BLangCompilerException) rbc;
            throw openError;
        } else {
            io:
            ReadableCharacterChannel rch = new ReadableCharacterChannel(rbc, "UTF8");

            var result = rch.readJson();
            var didClose = rch.close();
            if (result instanceof BLangCompilerException) {
                BLangCompilerException e = (BLangCompilerException) result;
                throw e;
            } else {
                Map<String, String> externalMap = null;
                Map<String, json> jsonMapResult = <Map<String, json>>result;
                foreach var[ key, val]in jsonMapResult.entries() {
                    externalMap.add(key, (String) val);
                }
                return externalMap;
            }
        }
    }

    public static PackageID createModuleId(String orgName, String moduleName, String moduleVersion) {
        return {org:orgName, name:moduleName, modVersion:moduleVersion, isUnnamed:false, sourceFilename:moduleName };
    }

    static void writeJarFile(JarFile jarFile, String targetPath) {
        writeExecutableJarToFile(jarFile, targetPath);
    }

static void writeExecutableJarToFile(JarFile jarFile, String targetPath)

    public static class JarFile {
        Map<String, String> manifestEntries = null;
        Map<String, byte[]> pkgEntries = null;
    }

        public static class JavaClass {
        String sourceFileName;
        String moduleClass;
        @Nilable
        List<BIRFunction> functions = new ArrayList<>();
        public JavaClass(String sourceFileName, String moduleClass) {
            this.sourceFileName = sourceFileName;
            this.moduleClass = moduleClass;
        }
    } =external;
}