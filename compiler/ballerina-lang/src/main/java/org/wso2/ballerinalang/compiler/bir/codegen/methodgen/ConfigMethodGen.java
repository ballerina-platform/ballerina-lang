/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURATION_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.POPULATE_CONFIG_DATA_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VARIABLE_KEY;

/**
 * Generates Jvm byte code for the configurable related methods.
 *
 * @since 2.0.0
 */
public class ConfigMethodGen {

    public void generateConfigInit(List<PackageID> imprtMods, BIRNode.BIRPackage pkg, String moduleInitClass,
                                   Map<String, byte[]> jarEntries) {
        String innerClassName = JvmCodeGenUtil.getModuleLevelClassName(pkg.packageID, CONFIGURATION_CLASS_NAME);
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, innerClassName, null, OBJECT, null);

        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, CONFIGURE_INIT, "()V", null, null);
        mv.visitCode();

        mv.visitTypeInsn(NEW, HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, HASH_MAP, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, 0);

        for (PackageID id : imprtMods) {
            generateInvokeConfiguration(mv, id);
        }

        generateInvokeConfiguration(mv, pkg.packageID);
        initializeConfigVariables(mv);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        populateConfigDataMethod(cw, moduleInitClass, pkg);

        cw.visitEnd();
        jarEntries.put(innerClassName + ".class", cw.toByteArray());
    }


    private void generateInvokeConfiguration(MethodVisitor mv, PackageID id) {
        String moduleClass = JvmCodeGenUtil.getModuleLevelClassName(id, CONFIGURATION_CLASS_NAME);
        String initClass = JvmCodeGenUtil.getModuleLevelClassName(id, MODULE_INIT_CLASS_NAME);

        mv.visitMethodInsn(INVOKESTATIC, moduleClass, POPULATE_CONFIG_DATA_METHOD,
                String.format("()[L%s;", VARIABLE_KEY), false);
        mv.visitVarInsn(ASTORE, 1);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARRAYLENGTH);
        Label elseLabel = new Label();
        mv.visitJumpInsn(IFEQ, elseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, initClass, CURRENT_MODULE_VAR_NAME, String.format("L%s;", MODULE));
        mv.visitVarInsn(ALOAD, 1);

        mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), true);
        mv.visitInsn(POP);
        mv.visitLabel(elseLabel);
    }

    private void initializeConfigVariables(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.LAUNCH_UTILS,
                "initConfigurableVariables", "(L" + MAP + ";)V", false);
    }

    private void populateConfigDataMethod(ClassWriter cw, String moduleClass, BIRNode.BIRPackage module) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, POPULATE_CONFIG_DATA_METHOD,
                String.format("()[L%s;", VARIABLE_KEY), null, null);
        mv.visitCode();

        //create configuration data array
        mv.visitIntInsn(BIPUSH, calculateConfigArraySize(module.globalVars));
        mv.visitTypeInsn(ANEWARRAY, VARIABLE_KEY);
        mv.visitVarInsn(ASTORE, 0);
        int varCount = 0;

        for (BIRNode.BIRGlobalVariableDcl globalVar : module.globalVars) {
            long globalVarFlags = globalVar.flags;
            if (Symbols.isFlagOn(globalVarFlags, Flags.CONFIGURABLE)) {
                mv.visitTypeInsn(NEW, VARIABLE_KEY);
                mv.visitInsn(DUP);
                mv.visitFieldInsn(GETSTATIC, moduleClass, CURRENT_MODULE_VAR_NAME,
                        "L" + MODULE + ";");
                mv.visitLdcInsn(globalVar.name.value);
                JvmTypeGen.loadType(mv, globalVar.type);
                mv.visitMethodInsn(INVOKESPECIAL, VARIABLE_KEY, JVM_INIT_METHOD, String.format("(L%s;L%s;L%s;)V",
                        MODULE, STRING_VALUE, TYPE), false);
                mv.visitVarInsn(ASTORE, varCount + 1);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, varCount);
                mv.visitVarInsn(ALOAD, varCount + 1);
                mv.visitInsn(AASTORE);
                varCount++;
            }
        }
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int calculateConfigArraySize(List<BIRNode.BIRGlobalVariableDcl> globalVars) {
        int count = 0;
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            if (Symbols.isFlagOn(globalVar.flags, Flags.CONFIGURABLE)) {
                count++;
            }
        }
        return count;
    }

}
