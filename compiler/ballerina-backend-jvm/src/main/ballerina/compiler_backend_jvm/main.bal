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
    //do nothing
}

function generateJarBinary(boolean dumpBir, bir:BIRContext birContext, bir:ModuleID entryModId, string progName)
            returns JarFile {
    currentBIRContext = birContext;
    bir:Package entryMod = birContext.lookupBIRModule(entryModId);

    if (dumpBir) {
       bir:BirEmitter emitter = new(entryMod);
       emitter.emitPackage();
    }

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    foreach var importModule in entryMod.importModules {
        bir:Package module = lookupModule(importModule, birContext);
        generateImportedPackage(module, jarEntries);
    }

    generateEntryPackage(entryMod, progName, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}
