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
string[] birCacheDirs = [];

public function main(string... args) {
    string pathToEntryBir = untaint args[0];
    string mapPath = untaint args[1];
    string targetPath = args[2];
    boolean dumpBir = boolean.convert(args[3]);

    var numCacheDirs = args.length() - 4;
    int i = 0;
    while (i < numCacheDirs) {
        birCacheDirs[i] = args[4 + i];
        i = i + 1;
    }

    writeJarFile(generateJarBinary(pathToEntryBir, mapPath, dumpBir), targetPath);
}

function generateJarBinary(string pathToEntryBir, string mapPath, boolean dumpBir) returns JarFile {
    externalMapCache = {
        "ballerina/bir/getBIRModuleBinary" : "org/ballerinalang/nativeimpl/bir/GetBIRModuleBinary" ,
        "ballerina/bir/decompressSingleFileToBlob" : "org/ballerinalang/nativeimpl/bir/DecompressBIR",
        "ballerina/jvm/ClassWriter.visitEnd" : "org/ballerinalang/nativeimpl/jvm/classwriter/VisitEnd" ,
        "ballerina/jvm/ClassWriter.visitSource" : "org/ballerinalang/nativeimpl/jvm/classwriter/VisitSource" ,
        "ballerina/jvm/ClassWriter.init" : "org/ballerinalang/nativeimpl/jvm/classwriter/Init" ,
        "ballerina/jvm/ClassWriter.visit" : "org/ballerinalang/nativeimpl/jvm/classwriter/Visit" ,
        "ballerina/jvm/ClassWriter.toByteArray" : "org/ballerinalang/nativeimpl/jvm/classwriter/ToByteArray" ,
        "ballerina/jvm/ClassWriter.visitMethod" : "org/ballerinalang/nativeimpl/jvm/classwriter/VisitMethod" ,
        "ballerina/jvm/ClassWriter.visitField" : "org/ballerinalang/nativeimpl/jvm/classwriter/VisitField" ,
        "ballerina/jvm/FieldVisitor.visitEnd" : "org/ballerinalang/nativeimpl/jvm/fieldvisitor/VisitEnd" ,
        "ballerina/jvm/MethodVisitor.visitInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitInsn" ,
        "ballerina/jvm/MethodVisitor.visitLookupSwitchInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitLookupSwitchInsn" ,
        "ballerina/jvm/MethodVisitor.visitFieldInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitFieldInsn" ,
        "ballerina/jvm/MethodVisitor.visitCode" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitCode" ,
        "ballerina/jvm/MethodVisitor.visitInvokeDynamicInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitInvokeDynamicInsn" ,
        "ballerina/jvm/MethodVisitor.visitEnd" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitEnd" ,
        "ballerina/jvm/MethodVisitor.visitLineNumber" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitLineNumber" ,
        "ballerina/jvm/MethodVisitor.visitLabel" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitLabel" ,
        "ballerina/jvm/MethodVisitor.visitJumpInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitJumpInsn" ,
        "ballerina/jvm/MethodVisitor.visitMethodInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitMethodInsn" ,
        "ballerina/jvm/MethodVisitor.visitLdcInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitLdcInsn" ,
        "ballerina/jvm/MethodVisitor.visitIntInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitIntInsn" ,
        "ballerina/jvm/MethodVisitor.visitVarInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitVarInsn" ,
        "ballerina/jvm/MethodVisitor.visitMaxs" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitMaxs" ,
        "ballerina/jvm/MethodVisitor.visitTryCatchBlock" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitTryCatchBlock" ,
        "ballerina/jvm/MethodVisitor.visitTypeInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitTypeInsn" ,
        "ballerina/jvm/MethodVisitor.visitMultiANewArrayInsn" : "org/ballerinalang/nativeimpl/jvm/methodvisitor/VisitMultiANewArrayInsn" ,
        "ballerina/jvm/Label.init" : "org/ballerinalang/nativeimpl/jvm/label/Init" ,
        "ballerina/jvm/lookupExternClassName" : "org/ballerinalang/nativeimpl/jvm/LookupExternClassName"
    };
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
    generatePackage(createModuleId(entryMod.org.value, entryMod.name.value, entryMod.versionValue.value), jarFile, true);

    return jarFile;
}

function readMap(string path) returns map<string> {
    io:ReadableByteChannel rbc = io:openReadableFile(path);
    io:ReadableCharacterChannel rch = new(rbc, "UTF8");
    var result = untaint rch.readJson();
    var didClose = rch.close();
    if (result is error) {
        panic result;
    } else {
        var externalMap = map<string>.convert(result);
        if (externalMap is error){
            panic externalMap;
        } else {
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

function createBIRContext(string sourceDir, string pathToCompilerBackend, string libDir) returns bir:BIRContext = external;
