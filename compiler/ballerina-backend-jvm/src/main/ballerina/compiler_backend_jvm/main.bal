// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;
import ballerina/internal;

public type JarFile record {|
    map<string> manifestEntries;
    map<byte[]> jarEntries;
|};

public type JavaClass record {|
    string sourceFileName;
    string moduleClass;
    bir:Function?[] functions = [];
|};

internal:Path birHome = new("");
bir:BIRContext currentBIRContext = new;

public function main(string... args) {
    birHome = new(untaint args[0]);
    string progName = args[1];
    string pathToEntryMod = args[2];
    string targetPath = args[3];
    boolean dumpBir = boolean.convert(args[4]);

    currentBIRContext = createBIRContext(pathToEntryMod, birHome.getPathValue());
    writeJarFile(generateJarBinary(progName, pathToEntryMod, dumpBir), progName, targetPath);
}

function generateJarBinary(string progName, string pathToEntryMod, boolean dumpBir) returns JarFile {
    io:println("Reading bir of ", progName);

    byte[] moduleBytes = bir:decompressSingleFileToBlob(pathToEntryMod, progName);
    bir:Package entryMod = bir:populateBIRModuleFromBinary(moduleBytes);

    if (dumpBir) {
       bir:BirEmitter emitter = new(entryMod);
       emitter.emitPackage();
    }

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    generateBuiltInPackages(currentBIRContext, jarEntries);

    foreach var importModule in entryMod.importModules {
        bir:Package module = lookupModule(importModule, currentBIRContext);
        generateImportedPackage(module, jarEntries);
        compiledPkgCache[module.org.value + module.name.value] = module;
    }

    generateEntryPackage(entryMod, progName, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}

public function getBaloPathFromModuleId(string moduleName, string modVersion) returns string {
    string moduleVersion = modVersion == "" ? "0.0.0" : modVersion;
    return birHome.resolve("lib", "repo", "ballerina", moduleName, moduleVersion).getPathValue();
}

function writeJarFile(JarFile jarFile, string fileName, string targetPath) {
    io:println("Writing jar file for ", fileName);
    writeExecutableJarToFile(jarFile, fileName, targetPath);
}

function writeExecutableJarToFile(JarFile jarFile, string fileName, string targetPath) = external;

function createBIRContext(string sourceDir, string balHome) returns bir:BIRContext = external;
