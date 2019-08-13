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
import ballerina/system;
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

string[] birCacheDirs = [];

public function main(string... args) {
    string pathToEntryBir = <@untainted> args[0];
    string mapPath = <@untainted> args[1];
    string targetPath = args[2];
    boolean dumpBir = internal:equalsIgnoreCase(args[3], "true");

    var numCacheDirs = args.length() - 4;
    int i = 0;
    while (i < numCacheDirs) {
        birCacheDirs[i] = args[4 + i];
        i = i + 1;
    }

    var jarFile = generateJarBinary(pathToEntryBir, mapPath, dumpBir);
    if (jarFile is error) {
        jvm:systemExit(1);
    } else {
        writeJarFile(jarFile, targetPath);
    }
}

function generateJarBinary(string pathToEntryBir, string mapPath, boolean dumpBir) returns JarFile | error {
    if (mapPath != "") {
        externalMapCache = readMap(mapPath);
    }

    byte[] moduleBytes = readFileFully(pathToEntryBir);
    bir:Package entryMod = bir:populateBIRModuleFromBinary(moduleBytes, false);

    compiledPkgCache[entryMod.org.value + entryMod.name.value] = entryMod;

    if (dumpBir) {
       bir:BirEmitter emitter = new(entryMod);
       emitter.emitPackage();
    }

    JarFile jarFile = {};
    check generatePackage(createModuleId(entryMod.org.value, entryMod.name.value,
                                        entryMod.versionValue.value), <@untainted> jarFile, true);
    return jarFile;
}

function readMap(string path) returns map<string> {
    var rbc = io:openReadableFile(path);
    if (rbc is error) {
        error openError = <error>rbc;
        panic openError;
    } else {
        io:ReadableCharacterChannel rch = new(rbc, "UTF8");

        var result = <@untainted> rch.readJson();
        var didClose = rch.close();
        if (result is error) {
            error e = <error>result;
            panic e;
        } else {
            map<string> externalMap = {};
            map<json> jsonMapResult = <map<json>> result;
            foreach var [key, val] in jsonMapResult.entries() {
                externalMap[key] = <string> val;
            }
            return externalMap;
        }
    }
}

public function createModuleId(string orgName, string moduleName, string moduleVersion) returns bir:ModuleID {
    return { org: orgName, name: moduleName, modVersion: moduleVersion, isUnnamed: false, sourceFilename: moduleName };
}

function writeJarFile(JarFile jarFile, string targetPath) {
    writeExecutableJarToFile(jarFile, targetPath);
}

function writeExecutableJarToFile(JarFile jarFile, string targetPath) = external;
