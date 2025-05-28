/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.split;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.creators.JvmErrorCreatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.creators.JvmFunctionCallsCreatorsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.creators.JvmObjectCreatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.creators.JvmRecordCreatorGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.NAME_HASH_COMPARATOR;

/**
 * Ballerina value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmValueCreatorGen {

    private final JvmRecordCreatorGen jvmRecordCreatorGen;
    private final JvmObjectCreatorGen jvmObjectCreatorGen;
    private final JvmErrorCreatorGen jvmErrorCreatorGen;
    private final JvmFunctionCallsCreatorsGen jvmFunctionCallsCreatorsGen;

    public JvmValueCreatorGen(PackageID packageID, JvmTypeGen jvmTypeGen) {
        this.jvmRecordCreatorGen = new JvmRecordCreatorGen(packageID, jvmTypeGen);
        this.jvmObjectCreatorGen = new JvmObjectCreatorGen(packageID);
        this.jvmErrorCreatorGen = new JvmErrorCreatorGen(packageID, jvmTypeGen);
        this.jvmFunctionCallsCreatorsGen = new JvmFunctionCallsCreatorsGen(packageID);
    }

    public void generateValueCreatorClasses(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                            JarEntries jarEntries,
                                            SymbolTable symbolTable, JvmCastGen jvmCastGen,
                                            List<BIRNode.BIRFunction> sortedFunctions) {

        // due to structural type same name can appear twice, need to remove duplicates
        Set<BIRTypeDefinition> recordTypeDefSet = new TreeSet<>(NAME_HASH_COMPARATOR);
        List<BIRTypeDefinition> objectTypeDefList = new ArrayList<>();
        List<BIRTypeDefinition> errorTypeDefList = new ArrayList<>();

        for (BIRTypeDefinition optionalTypeDef : module.typeDefs) {
            BType bType = JvmCodeGenUtil.getImpliedType(optionalTypeDef.type);
            if (bType.tag == TypeTags.RECORD) {
                recordTypeDefSet.add(optionalTypeDef);
            } else if (bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(bType.tsymbol.flags, Flags.CLASS)) {
                objectTypeDefList.add(optionalTypeDef);
            } else if (bType.tag == TypeTags.ERROR) {
                errorTypeDefList.add(optionalTypeDef);
            }
        }
        ArrayList<BIRTypeDefinition> recordTypeDefList = new ArrayList<>(recordTypeDefSet);

        jvmRecordCreatorGen.generateRecordsClass(jvmPackageGen, module, jarEntries, recordTypeDefList);
        jvmObjectCreatorGen.generateObjectsClass(jvmPackageGen, module, jarEntries, objectTypeDefList, symbolTable);
        jvmErrorCreatorGen.generateErrorsClass(jvmPackageGen, module, jarEntries, errorTypeDefList, symbolTable);
        jvmFunctionCallsCreatorsGen.generateFunctionCallsClass(jvmPackageGen, module, jarEntries, jvmCastGen,
                sortedFunctions);
    }
}
