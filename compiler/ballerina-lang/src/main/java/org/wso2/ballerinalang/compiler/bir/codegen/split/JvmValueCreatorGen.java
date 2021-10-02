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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.codegen.split.creators.JvmErrorCreatorGen;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;

/**
 * Ballerina value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmValueCreatorGen {

    private final JvmRecordCreatorGen jvmRecordCreatorGen;
    private final JvmObjectCreatorGen jvmObjectCreatorGen;
    private final JvmErrorCreatorGen jvmErrorCreatorGen;

    public JvmValueCreatorGen(PackageID packageID) {
        this.jvmRecordCreatorGen = new JvmRecordCreatorGen(this, packageID);
        this.jvmObjectCreatorGen = new JvmObjectCreatorGen(this, packageID);
        this.jvmErrorCreatorGen = new JvmErrorCreatorGen(packageID);
    }

    public void generateValueCreatorClasses(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, Map<String, byte[]> jarEntries,
                                     SymbolTable symbolTable) {

        // due to structural type same name can appear twice, need to remove duplicates
        Set<BIRTypeDefinition> recordTypeDefSet = new TreeSet<>(NAME_HASH_COMPARATOR);
        List<BIRTypeDefinition> objectTypeDefList = new ArrayList<>();
        List<BIRTypeDefinition> errorTypeDefList = new ArrayList<>();

        for (BIRTypeDefinition optionalTypeDef : module.typeDefs) {
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.RECORD) {
                recordTypeDefSet.add(optionalTypeDef);
            } else if (bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(bType.tsymbol.flags, Flags.CLASS)) {
                objectTypeDefList.add(optionalTypeDef);
            } else if (bType.tag == TypeTags.ERROR) {
                errorTypeDefList.add(optionalTypeDef);
            }
        }
        ArrayList<BIRTypeDefinition> recordTypeDefList = new ArrayList<>(recordTypeDefSet);
        jvmRecordCreatorGen.generateRecordsClass(jvmPackageGen, module, moduleInitClass, jarEntries,
                recordTypeDefList);
        jvmObjectCreatorGen.generateObjectsClass(jvmPackageGen, module, moduleInitClass, jarEntries,
                objectTypeDefList, symbolTable);
        jvmErrorCreatorGen.generateErrorsClass(jvmPackageGen, module, moduleInitClass, jarEntries, errorTypeDefList,
                symbolTable);
    }

    public void generateStaticInitializer(BIRNode.BIRPackage module, ClassWriter cw,
                                           String typeOwnerClass, String varName, String metaDataVarName) {
        FieldVisitor fv = cw.visitField(Opcodes.ACC_STATIC, metaDataVarName, String.format("L%s;", STRAND_METADATA),
                null, null);
        fv.visitEnd();
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        JvmCodeGenUtil.genStrandMetadataField(mv, typeOwnerClass, module.packageID, metaDataVarName,
                new ScheduleFunctionInfo(varName));
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
