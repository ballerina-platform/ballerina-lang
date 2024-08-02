/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STRAND_METADATA_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * Generates Jvm class for strand metadata for given module.
 *
 * @since 2201.10.0
 */
public class JvmStrandMetadataConstantsGen {

    private final String strandMetadataConstantsClass;
    private final ClassWriter cw;
    private final PackageID packageID;

    public JvmStrandMetadataConstantsGen(PackageID packageID) {
        this.strandMetadataConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                MODULE_STRAND_METADATA_CLASS_NAME);
        this.packageID = packageID;
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, strandMetadataConstantsClass);
    }

    public void generateClass(JarEntries jarEntries, Map<String, ScheduleFunctionInfo> strandMetadata) {
        visitStrandMetadataFields(cw, strandMetadata);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        generateStrandMetadata(mv, strandMetadata);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(strandMetadataConstantsClass + CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    private void generateStrandMetadata(MethodVisitor mv, Map<String, ScheduleFunctionInfo> strandMetadata) {
        strandMetadata.forEach((varName, metaData) -> genStrandMetadataField(mv, varName, metaData));
    }

    private void genStrandMetadataField(MethodVisitor mv, String varName, ScheduleFunctionInfo metaData) {
        mv.visitTypeInsn(Opcodes.NEW, STRAND_METADATA);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(Utils.decodeIdentifier(this.packageID.orgName.value));
        mv.visitLdcInsn(Utils.decodeIdentifier(this.packageID.name.value));
        mv.visitLdcInsn(getMajorVersion(this.packageID.version.value));
        if (metaData.typeName == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mv.visitLdcInsn(metaData.typeName);
        }
        mv.visitLdcInsn(metaData.parentFunctionName);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, STRAND_METADATA, JVM_INIT_METHOD, INIT_STRAND_METADATA, false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, strandMetadataConstantsClass, varName, GET_STRAND_METADATA);
    }

    private void visitStrandMetadataFields(ClassWriter cw, Map<String, ScheduleFunctionInfo> strandMetaDataMap) {
        strandMetaDataMap.keySet().forEach(varName -> visitStrandMetadataField(cw, varName));
    }

    private void visitStrandMetadataField(ClassWriter cw, String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, varName, GET_STRAND_METADATA, null, null);
        fv.visitEnd();
    }

    public String getStrandMetadataConstantsClass() {
        return strandMetadataConstantsClass;
    }
}
