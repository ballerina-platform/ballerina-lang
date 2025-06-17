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

import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;

import java.util.List;

/**
 * Split initialization of types and other type related methods.
 *
 * @since 2.0.0
 */
public class JvmMethodsSplitter {

    private final JvmPackageGen jvmPackageGen;
    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmValueCreatorGen jvmValueCreatorGen;
    private final BIRNode.BIRPackage module;

    public JvmMethodsSplitter(JvmPackageGen jvmPackageGen, JvmConstantsGen jvmConstantsGen, BIRNode.BIRPackage module,
                              TypeHashVisitor typeHashVisitor, JvmTypeGen jvmTypeGen) {
        this.module = module;
        this.jvmPackageGen = jvmPackageGen;
        this.jvmCreateTypeGen = new JvmCreateTypeGen(jvmTypeGen, jvmConstantsGen, module.packageID, typeHashVisitor);
        this.jvmValueCreatorGen = new JvmValueCreatorGen(module.packageID, jvmTypeGen);
        jvmConstantsGen.setJvmCreateTypeGen(jvmCreateTypeGen);
    }

    public void generateMethods(JarEntries jarEntries, JvmCastGen jvmCastGen, List<BIRNode.BIRFunction> sortedFunctions) {
        jvmCreateTypeGen.generateRefTypeConstants(module.typeDefs);
        jvmCreateTypeGen.createTypes(module, jarEntries, jvmPackageGen.symbolTable);
        jvmValueCreatorGen.generateValueCreatorClasses(jvmPackageGen, module, jarEntries,
                jvmPackageGen.symbolTable, jvmCastGen, sortedFunctions);
        jvmCreateTypeGen.generateAnonTypeClass(jvmPackageGen, module, jarEntries);
        jvmCreateTypeGen.generateFunctionTypeClass(jvmPackageGen, module, jarEntries, sortedFunctions);
    }
}
