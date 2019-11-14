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
import ballerina/stringutils;
import ballerina/lang.'int as lint;

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
string[] jarLibraries = [];

public function main(string... args) {
    string pathToEntryBir = <@untainted> args[0];
    string mapPath = <@untainted> args[1];
    string targetPath = args[2];
    boolean dumpBir = stringutils:equalsIgnoreCase(args[3], "true");
    boolean useSystemClassLoader = stringutils:equalsIgnoreCase(args[4], "true");
    int numCacheDirs =  <int>lint:fromString(args[5]);

    int i = 0;
    while (i < numCacheDirs) {
        birCacheDirs[i] = <@untainted> args[6 + i];
        i = i + 1;
    }
    // main will receive the no of cache directories as 6th arg. Then we read the rest of the args as cache directories
    // based on 6th arg value.
    int argsCount = 6 + numCacheDirs;
    int dependentJarCnt = args.length() - argsCount;
    i = 0;
    while (i < dependentJarCnt) {
        jarLibraries[i] = <@untainted> args[argsCount + i];
        i = i + 1;
    }

    var jarFile = generateJarBinary(pathToEntryBir, mapPath, dumpBir, jarLibraries, useSystemClassLoader);
    if (dlogger.getErrorCount() > 0) {
        dlogger.printErrors();
        jvm:systemExit(1);
        return;
    }

    writeJarFile(jarFile, targetPath);
}

function generateJarBinary(string pathToEntryBir, string mapPath, boolean dumpBir,
                           string[] jarLibraries, boolean useSystemClassLoader) returns JarFile {
    if (mapPath != "") {
        externalMapCache = readMap(mapPath);
    }

    byte[] moduleBytes = readFileFully(pathToEntryBir);
    bir:Package entryMod = bir:populateBIRModuleFromBinary(moduleBytes, false);
    compiledPkgCache[entryMod.org.value + entryMod.name.value] = entryMod;

    if (dumpBir) {
        io:println(bir:emitModule(entryMod));
    }

    JarFile jarFile = {};
    jvm:InteropValidator interopValidator = new(jarLibraries, useSystemClassLoader);
    generatePackage(createModuleId(entryMod.org.value, entryMod.name.value,
                                        entryMod.versionValue.value), <@untainted> jarFile, interopValidator, true);
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
