/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.split;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmArrayTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmBStringConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmErrorTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmFunctionTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmModuleConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmRefTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmTupleTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmUnionTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.identifiers.JvmBallerinaConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ALL_CONSTANTS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ALL_GLOBAL_VARIABLES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_CONSTANTS_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_VARIABLES_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Class holder keep constants used by class generation and generate class for each constant type.
 *
 * @since 2.0.0
 */
public class JvmConstantsGen {

    private final JvmBallerinaConstantsGen jvmBallerinaConstantsGen;
    private final JvmUnionTypeConstantsGen unionTypeConstantsGen;
    private final JvmErrorTypeConstantsGen errorTypeConstantsGen;
    private final JvmBStringConstantsGen stringConstantsGen;
    private final JvmModuleConstantsGen moduleConstantsGen;
    private final JvmTupleTypeConstantsGen tupleTypeConstantsGen;
    private final JvmArrayTypeConstantsGen arrayTypeConstantsGen;
    private final JvmRefTypeConstantsGen refTypeConstantsGen;
    private final JvmFunctionTypeConstantsGen functionTypeConstantsGen;
    public final BTypeHashComparator bTypeHashComparator;
    public final String allConstantsClassName;
    public final String allGlobalVarsClassName;
    public final String constantsPkgName;
    public final String globalVarsPkgName;

    public JvmConstantsGen(BIRNode.BIRPackage module, Types types, TypeHashVisitor typeHashVisitor,
                           JarEntries jarEntries) {
        this.bTypeHashComparator = new BTypeHashComparator(typeHashVisitor);
        this.stringConstantsGen = new JvmBStringConstantsGen(module.packageID);
        this.moduleConstantsGen = new JvmModuleConstantsGen(module);
        this.functionTypeConstantsGen = new JvmFunctionTypeConstantsGen(module.packageID, module.functions);
        this.jvmBallerinaConstantsGen = new JvmBallerinaConstantsGen(module, this);
        this.unionTypeConstantsGen = new JvmUnionTypeConstantsGen(module.packageID, bTypeHashComparator, jarEntries);
        this.errorTypeConstantsGen = new JvmErrorTypeConstantsGen(module.packageID, bTypeHashComparator, jarEntries);
        this.tupleTypeConstantsGen = new JvmTupleTypeConstantsGen(module.packageID, bTypeHashComparator, jarEntries);
        this.arrayTypeConstantsGen = new JvmArrayTypeConstantsGen(module.packageID, bTypeHashComparator, types,
                jarEntries);
        this.refTypeConstantsGen = new JvmRefTypeConstantsGen(module.packageID, bTypeHashComparator, jarEntries);
        this.allConstantsClassName = getModuleLevelClassName(module.packageID, ALL_CONSTANTS_CLASS_NAME);
        this.constantsPkgName = getModuleLevelClassName(module.packageID, GLOBAL_CONSTANTS_PACKAGE_NAME);
        this.allGlobalVarsClassName = getModuleLevelClassName(module.packageID, ALL_GLOBAL_VARIABLES_CLASS_NAME);
        this.globalVarsPkgName = getModuleLevelClassName(module.packageID, GLOBAL_VARIABLES_PACKAGE_NAME);
    }

    public int getBStringConstantVarIndex(String value) {
        return stringConstantsGen.addBStringConstantVarIndex(value);
    }

    public String getModuleConstantVar(PackageID packageID) {
        return moduleConstantsGen.addModule(packageID);
    }

    public void setJvmCreateTypeGen(JvmCreateTypeGen jvmCreateTypeGen) {
        unionTypeConstantsGen.setJvmUnionTypeGen(jvmCreateTypeGen.getJvmUnionTypeGen());
        errorTypeConstantsGen.setJvmErrorTypeGen(jvmCreateTypeGen.getJvmErrorTypeGen());
        tupleTypeConstantsGen.setJvmTupleTypeGen(jvmCreateTypeGen.getJvmTupleTypeGen());
        arrayTypeConstantsGen.setJvmArrayTypeGen(jvmCreateTypeGen.getJvmArrayTypeGen());
        refTypeConstantsGen.setJvmRefTypeGen(jvmCreateTypeGen.getJvmRefTypeGen());
        functionTypeConstantsGen.setJvmTypeGen(jvmCreateTypeGen.getJvmTypeGen());
    }

    public void generateConstants(JvmPackageGen jvmPackageGen, JarEntries jarEntries) {
        jvmBallerinaConstantsGen.generateConstantInit(jvmPackageGen, jarEntries);
        functionTypeConstantsGen.generateClass(jarEntries);
        moduleConstantsGen.generateConstantInit(jarEntries);
        stringConstantsGen.generateConstantInit(jarEntries);
    }

    public void generateGetBErrorType(MethodVisitor mv, String varName) {
        errorTypeConstantsGen.generateGetBErrorType(mv, varName);
    }

    public void generateGetBUnionType(MethodVisitor mv, String varName) {
        unionTypeConstantsGen.generateGetBUnionType(mv, varName);
    }

    public void generateGetBTupleType(MethodVisitor mv, String varName) {
        tupleTypeConstantsGen.generateGetBTupleType(mv, varName);
    }

    public String getErrorTypeConstantsVar(BType type) {
        return errorTypeConstantsGen.add((BErrorType) type);
    }

    public String getArrayTypeConstantsVar(BType type) {
        return arrayTypeConstantsGen.add((BArrayType) type);
    }

    public String getTupleTypeConstantsVar(BType type, SymbolTable symbolTable) {
        return tupleTypeConstantsGen.add((BTupleType) type, symbolTable);
    }

    public void getRefTypeConstantsVar(BIRNode.BIRTypeDefinition typeDef, JvmPackageGen jvmPackageGen,
                                       JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                       LazyLoadingDataCollector lazyLoadingDataCollector) {
        refTypeConstantsGen.add(typeDef, jvmPackageGen, jvmCastGen, asyncDataCollector,
                lazyLoadingDataCollector);
    }

    public String getUnionTypeConstantsVar(BType type, SymbolTable symbolTable) {
        return unionTypeConstantsGen.add((BUnionType) type, symbolTable);
    }

    public String getModuleConstantClass(String varName) {
        return moduleConstantsGen.getModuleConstantsClass(varName);
    }

    public String getFunctionTypeConstantClass(String varName) {
        return functionTypeConstantsGen.getFunctionTypeConstantClass(varName);
    }

    public String getFunctionTypeVar(String functionName) {
        return functionTypeConstantsGen.getFunctionTypeVar(functionName);
    }

    public void generateGetBArrayType(MethodVisitor mv, String varName) {
        arrayTypeConstantsGen.generateGetBArrayType(mv, varName);
    }
}
