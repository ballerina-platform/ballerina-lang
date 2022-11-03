/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_FRAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.YIELD_LOCATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.YIELD_STATUS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGen.FUNCTION_INVOCATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGen.STATE;

/**
 * Generates Jvm byte code for the generateFrame classes.
 *
 * @since 2.0.0
 */
public class FrameClassGen {

    public void generateFrameClasses(BIRNode.BIRPackage pkg, Map<String, byte[]> pkgEntries) {
        pkg.functions.forEach(
                func -> generateFrameClassForFunction(pkg.packageID, func, pkgEntries, null));

        for (BIRNode.BIRTypeDefinition typeDef : pkg.typeDefs) {
            List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;
            if (attachedFuncs == null || attachedFuncs.size() == 0) {
                continue;
            }

            BType attachedType;
            if (typeDef.type.tag == TypeTags.RECORD) {
                // Only attach function of records is the record init. That should be
                // generated as a static function.
                attachedType = null;
            } else {
                attachedType = typeDef.type;
            }
            attachedFuncs.forEach(func -> generateFrameClassForFunction(
                    pkg.packageID, func, pkgEntries, attachedType));
        }
    }

    private void generateFrameClassForFunction(PackageID packageID, BIRNode.BIRFunction func,
                                               Map<String, byte[]> pkgEntries,
                                               BType attachedType) {
        String frameClassName = MethodGenUtils.getFrameClassName(JvmCodeGenUtil.getPackageName(packageID),
                                                                 func.name.value, attachedType);
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        if (func.pos != null && func.pos.lineRange().fileName() != null) {
            cw.visitSource(func.pos.lineRange().fileName(), null);
        }
        cw.visit(V1_8, Opcodes.ACC_PUBLIC + ACC_SUPER, frameClassName, null, OBJECT,
                new String[]{FUNCTION_FRAME});
        JvmCodeGenUtil.generateDefaultConstructor(cw, OBJECT);

        int k = 0;
        List<BIRNode.BIRVariableDcl> localVars = func.localVars;
        while (k < localVars.size()) {
            BIRNode.BIRVariableDcl localVar = localVars.get(k);
            if (localVar.onlyUsedInSingleBB) {
                k = k + 1;
                continue;
            }
            BType bType = localVar.type;
            String fieldName = localVar.jvmVarName;
            String typeSig = JvmCodeGenUtil.getFieldTypeSignature(bType);
            cw.visitField(Opcodes.ACC_PUBLIC, fieldName, typeSig, null, null).visitEnd();
            k = k + 1;
        }

        FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC, STATE, "I", null, null);
        fv.visitEnd();
        fv = cw.visitField(Opcodes.ACC_PUBLIC, FUNCTION_INVOCATION, "I", null, null);
        fv.visitEnd();
        fv = cw.visitField(Opcodes.ACC_PUBLIC, YIELD_LOCATION, GET_STRING, null, null);
        fv.visitEnd();
        fv = cw.visitField(Opcodes.ACC_PUBLIC, YIELD_STATUS, GET_STRING, null, null);
        fv.visitEnd();

        generateGetStringFieldMethod(cw, frameClassName, "getYieldLocation", YIELD_LOCATION);
        generateGetStringFieldMethod(cw, frameClassName, "getYieldStatus", YIELD_STATUS);

        cw.visitEnd();

        // panic if there are errors in the frame class. These cannot be logged, since
        // frame classes are internal implementation details.
        pkgEntries.put(frameClassName + ".class", cw.toByteArray());
    }

    private void generateGetStringFieldMethod(ClassWriter cw, String frameClassName, String methodName,
                                              String fieldName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, GET_JSTRING, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, frameClassName, fieldName, GET_STRING);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

}
