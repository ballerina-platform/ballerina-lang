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
    map<string> manifestEntries = {};
    map<byte[]> pkgEntries = {};
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

    byte[] moduleBytes = bir:decompressSingleFileToBlob(pathToEntryMod, progName);
    bir:Package entryMod = bir:populateBIRModuleFromBinary(moduleBytes);

    compiledPkgCache[entryMod.org.value + entryMod.name.value] = entryMod;

    if (dumpBir) {
       bir:BirEmitter emitter = new(entryMod);
       emitter.emitPackage();
    }

    JarFile jarFile = {};
    generatePackage(createModuleId(entryMod.org.value, entryMod.name.value, entryMod.versionValue.value), jarFile, true);

    return jarFile;
}

public function createModuleId(string orgName, string moduleName, string moduleVersion) returns bir:ModuleID {
    return { org: orgName, name: moduleName, modVersion: moduleVersion, isUnnamed: false, sourceFilename: moduleName };
}

function writeJarFile(JarFile jarFile, string fileName, string targetPath) {
    writeExecutableJarToFile(jarFile, fileName, targetPath);
}

function writeExecutableJarToFile(JarFile jarFile, string fileName, string targetPath) = external;

function createBIRContext(string sourceDir, string balHome) returns bir:BIRContext = external;
