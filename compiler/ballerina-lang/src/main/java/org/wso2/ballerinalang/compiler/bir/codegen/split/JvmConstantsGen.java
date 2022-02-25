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
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmArrayTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmBStringConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmBallerinaConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmModuleConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmTupleTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmUnionTypeConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Map;

/**
 * Class holder keep constants used by class generation and generate class for each constant type.
 *
 * @since 2.0.0
 */
public class JvmConstantsGen {

    private final JvmBallerinaConstantsGen jvmBallerinaConstantsGen;

    private final JvmUnionTypeConstantsGen unionTypeConstantsGen;

    private final JvmBStringConstantsGen stringConstantsGen;

    private final JvmModuleConstantsGen moduleConstantsGen;

    private final JvmTupleTypeConstantsGen tupleTypeConstantsGen;

    private final JvmArrayTypeConstantsGen arrayTypeConstantsGen;

    public final BTypeHashComparator bTypeHashComparator;

    public JvmConstantsGen(BIRNode.BIRPackage module, String moduleInitClass, CompilerContext compilerContext,
                           TypeHashVisitor typeHashVisitor) {
        this.bTypeHashComparator = new BTypeHashComparator(typeHashVisitor);
        this.stringConstantsGen = new JvmBStringConstantsGen(module.packageID);
        this.moduleConstantsGen = new JvmModuleConstantsGen(module);
        this.jvmBallerinaConstantsGen = new JvmBallerinaConstantsGen(module, moduleInitClass, this);
        this.unionTypeConstantsGen = new JvmUnionTypeConstantsGen(module.packageID, bTypeHashComparator);
        this.tupleTypeConstantsGen = new JvmTupleTypeConstantsGen(module.packageID, bTypeHashComparator);
        this.arrayTypeConstantsGen = new JvmArrayTypeConstantsGen(module.packageID,
                bTypeHashComparator, Types.getInstance(compilerContext));
    }

    public String getBStringConstantVar(String value) {
        return stringConstantsGen.addBString(value);
    }

    public String getModuleConstantVar(PackageID packageID) {
        return moduleConstantsGen.addModule(packageID);
    }

    public void setJvmCreateTypeGen(JvmCreateTypeGen jvmCreateTypeGen) {
        unionTypeConstantsGen.setJvmUnionTypeGen(jvmCreateTypeGen.getJvmUnionTypeGen());
        tupleTypeConstantsGen.setJvmTupleTypeGen(jvmCreateTypeGen.getJvmTupleTypeGen());
        arrayTypeConstantsGen.setJvmArrayTypeGen(jvmCreateTypeGen.getJvmArrayTypeGen());
    }

    public void generateConstants(Map<String, byte[]> jarEntries) {
        jvmBallerinaConstantsGen.generateConstantInit(jarEntries);
        unionTypeConstantsGen.generateClass(jarEntries);
        moduleConstantsGen.generateConstantInit(jarEntries);
        stringConstantsGen.generateConstantInit(jarEntries);
        tupleTypeConstantsGen.generateClass(jarEntries);
        arrayTypeConstantsGen.generateClass(jarEntries);
    }

    public void generateGetBUnionType(MethodVisitor mv, String varName) {
        unionTypeConstantsGen.generateGetBUnionType(mv, varName);
    }

    public void generateGetBTupleType(MethodVisitor mv, String varName) {
        tupleTypeConstantsGen.generateGetBTupleType(mv, varName);
    }

    public String getTypeConstantsVar(BType type) {
        switch (type.tag) {
            case TypeTags.ARRAY:
                return arrayTypeConstantsGen.add((BArrayType) type);
            case TypeTags.TUPLE:
                return tupleTypeConstantsGen.add((BTupleType) type);
            default:
                return unionTypeConstantsGen.add((BUnionType) type);
        }
    }

    public String getStringConstantsClass() {
        return stringConstantsGen.getStringConstantsClass();
    }

    public String getModuleConstantClass() {
        return moduleConstantsGen.getModuleConstantClass();
    }

    public String getConstantClass() {
        return jvmBallerinaConstantsGen.getConstantClass();
    }

    public void generateGetBArrayType(MethodVisitor mv, String varName) {
        arrayTypeConstantsGen.generateGetBArrayType(mv, varName);
    }
}
