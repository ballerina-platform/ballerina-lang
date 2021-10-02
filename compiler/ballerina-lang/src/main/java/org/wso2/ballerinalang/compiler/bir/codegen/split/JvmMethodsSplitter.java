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

import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.Map;

/**
 * Split initialization of types and other type related methods.
 *
 * @since 2.0.0
 */
public class JvmMethodsSplitter {

    private final JvmPackageGen jvmPackageGen;
    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmValueCreatorGen jvmValueCreatorGen;
    private final JvmAnnotationsGen jvmAnnotationsGen;
    private final BIRNode.BIRPackage module;
    private final String moduleInitClass;

    public JvmMethodsSplitter(JvmPackageGen jvmPackageGen, JvmConstantsGen jvmConstantsGen,
                              BIRNode.BIRPackage module, String moduleInitClass) {
        this.module = module;
        this.jvmPackageGen = jvmPackageGen;
        this.moduleInitClass = moduleInitClass;
        JvmTypeGen jvmTypeGen = new JvmTypeGen(jvmConstantsGen, module.packageID);
        this.jvmCreateTypeGen = new JvmCreateTypeGen(jvmTypeGen, jvmConstantsGen, module.packageID);
        this.jvmAnnotationsGen = new JvmAnnotationsGen(module, jvmPackageGen, jvmTypeGen);
        this.jvmValueCreatorGen = new JvmValueCreatorGen(module.packageID);
        jvmConstantsGen.setJvmCreateTypeGen(jvmCreateTypeGen);
    }

    public void generateMethods(Map<String, byte[]> jarEntries) {
        jvmCreateTypeGen.generateTypeClass(jvmPackageGen, module, jarEntries, moduleInitClass,
                jvmPackageGen.symbolTable);
        jvmValueCreatorGen.generateValueCreatorClasses(jvmPackageGen, module, moduleInitClass, jarEntries,
                jvmPackageGen.symbolTable);
        jvmCreateTypeGen.generateAnonTypeClass(jvmPackageGen, module, moduleInitClass, jarEntries);
        jvmAnnotationsGen.generateAnnotationsClass(jarEntries);
    }
}
