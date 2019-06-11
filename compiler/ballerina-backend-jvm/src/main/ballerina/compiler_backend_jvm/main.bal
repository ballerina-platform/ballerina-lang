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

bir:BIRContext currentBIRContext = new;

public function main(string... args) {
    string birHome = args[0];
    string progName = args[1];
    string pathToEntryMod = args[2];
    boolean dumpBir = boolean.convert(args[2]);
    writeJarFile(generateJarBinary(progName, birHome, pathToEntryMod, dumpBir), progName, pathToEntryMod);
}

function generateJarBinary(string progName, string birHome, string pathToEntryMod, boolean dumpBir) returns JarFile {
    io:println("Reading bir of ", progName);

    bir:BIRContext birContext = new;
    currentBIRContext = birContext;
    byte[] moduleBytes = bir:decompressSingleFileToBlob(pathToEntryMod, progName);
    bir:Package entryMod = bir:populateBIRModuleFromBinary(moduleBytes);

    if (dumpBir) {
       bir:BirEmitter emitter = new(entryMod);
       emitter.emitPackage();
    }

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    byte[] utilsModuleBytes = bir:decompressSingleFileToBlob(getBaloPathFromModuleId(birHome, "utils", "0.0.0"), "utils");
    byte[] builtinModuleBytes = bir:decompressSingleFileToBlob(getBaloPathFromModuleId(birHome, "builtin", "0.0.0"), "builtin");

    bir:Package utilsMod = bir:populateBIRModuleFromBinary(utilsModuleBytes);
    bir:Package builtinMod = bir:populateBIRModuleFromBinary(builtinModuleBytes);

    generateImportedPackage(utilsMod, jarEntries);
    generateImportedPackage(builtinMod, jarEntries);

    foreach var importModule in entryMod.importModules {
        string pathToLib = getBaloPathFromModuleId(birHome, importModule.modName.value, importModule.modVersion.value);
        moduleBytes = bir:decompressSingleFileToBlob(pathToLib, importModule.modName.value);
        bir:Package module = bir:populateBIRModuleFromBinary(moduleBytes);
        generateImportedPackage(module, jarEntries);
        compiledPkgCache[module.org.value + module.name.value] = module;
    }

    generateEntryPackage(entryMod, progName, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}

function getBaloPathFromModuleId(string birHome, string moduleName, string modVersion) returns string {
    internal:Path path = new(birHome);
    string pathToBalo = path.resolve("lib", "repo", "ballerina", moduleName, modVersion).getPathValue();
    return pathToBalo;
}

function writeJarFile(JarFile jarFile, string fileName, string targetPath) {
    io:println("Writing jar file for ", fileName);
    writeExecutableJarToFile(jarFile, fileName, targetPath);
}

function writeExecutableJarToFile(JarFile jarFile, string fileName, string targetPath) = external;
