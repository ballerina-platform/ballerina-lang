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

import io.ballerina.identifier.Utils;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collection;
import java.util.Set;

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
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURATION_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURE_INIT_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAUNCH_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.POPULATE_CONFIG_DATA_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VARIABLE_KEY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_MODULE_CONFIG_DATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CURRENT_MODULE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JBOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_CONFIG;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INTI_VARIABLE_KEY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.POPULATE_CONFIG_DATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm byte code for configurable related methods.
 *
 * @since 2.0.0
 */
public class ConfigMethodGen {
    String innerClassName;

    public void generateConfigMapper(Set<PackageID> imprtMods, BIRNode.BIRPackage pkg, String moduleInitClass,
                                     JvmConstantsGen jvmConstantsGen, TypeHashVisitor typeHashVisitor,
                                     JarEntries jarEntries, SymbolTable symbolTable) {
        innerClassName = getModuleLevelClassName(pkg.packageID, CONFIGURATION_CLASS_NAME);
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC | ACC_SUPER, innerClassName, null, OBJECT, null);
        generateStaticFields(cw, innerClassName);
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, JVM_INIT_METHOD, innerClassName);
        mv.visitEnd();

        generateConfigInit(cw, imprtMods, pkg.packageID, innerClassName);
        populateConfigDataMethod(cw, moduleInitClass, pkg, new JvmTypeGen(jvmConstantsGen, pkg.packageID,
                typeHashVisitor, symbolTable));
        cw.visitEnd();
        jarEntries.put(innerClassName + CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    private void generateStaticFields(ClassWriter cw, String innerClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, CONFIGURE_INIT_ATTEMPTED, GET_JBOOLEAN_TYPE,
                null, null);
        fv.visitEnd();
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, innerClassName, CONFIGURE_INIT_ATTEMPTED, GET_JBOOLEAN_TYPE);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, JVM_STATIC_INIT_METHOD, innerClassName);
        mv.visitEnd();
    }

    private void generateConfigInit(ClassWriter cw, Set<PackageID> imprtMods, PackageID packageID,
                                    String innerClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, CONFIGURE_INIT, INIT_CONFIG, null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, innerClassName, CONFIGURE_INIT_ATTEMPTED, GET_JBOOLEAN_TYPE);
        Label labelIf = new Label();
        mv.visitJumpInsn(IFEQ, labelIf);
        mv.visitInsn(RETURN);
        mv.visitLabel(labelIf);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, innerClassName, CONFIGURE_INIT_ATTEMPTED, GET_JBOOLEAN_TYPE);
        for (PackageID id : imprtMods) {
            generateInvokeConfigureInit(mv, id);
        }
        generateInvokeConfiguration(mv, packageID);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CONFIGURE_INIT, innerClassName);
        mv.visitEnd();
    }

    private void generateInvokeConfiguration(MethodVisitor mv, PackageID id) {
        String moduleClass = getModuleLevelClassName(id, CONFIGURATION_CLASS_NAME);
        String initClass = getModuleLevelClassName(id, MODULE_INIT_CLASS_NAME);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESTATIC, moduleClass, POPULATE_CONFIG_DATA_METHOD, POPULATE_CONFIG_DATA, false);
        mv.visitVarInsn(ASTORE, 5);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitInsn(ARRAYLENGTH);
        Label elseLabel = new Label();
        mv.visitJumpInsn(IFEQ, elseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, initClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "addModuleConfigData", ADD_MODULE_CONFIG_DATA, false);
        mv.visitLabel(elseLabel);
    }

    private void generateInvokeConfigureInit(MethodVisitor mv, PackageID id) {
        String configClass = getModuleLevelClassName(id, CONFIGURATION_CLASS_NAME);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESTATIC, configClass, CONFIGURE_INIT, INIT_CONFIG, false);
    }

    private void populateConfigDataMethod(ClassWriter cw, String moduleClass,
                                          BIRNode.BIRPackage module, JvmTypeGen jvmTypeGen) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, POPULATE_CONFIG_DATA_METHOD,
                POPULATE_CONFIG_DATA, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, moduleClass, CURRENT_MODULE_INIT_METHOD, CURRENT_MODULE_INIT, false);

        //create configuration data array
        mv.visitLdcInsn(calculateConfigArraySize(module.globalVars));
        mv.visitTypeInsn(ANEWARRAY, VARIABLE_KEY);
        mv.visitVarInsn(ASTORE, 1);
        int varCount = 0;

        for (BIRNode.BIRGlobalVariableDcl globalVar : module.globalVars) {
            long globalVarFlags = globalVar.flags;
            if (Symbols.isFlagOn(globalVarFlags, Flags.CONFIGURABLE)) {
                mv.visitTypeInsn(NEW, VARIABLE_KEY);
                mv.visitInsn(DUP);
                mv.visitFieldInsn(GETSTATIC, moduleClass, CURRENT_MODULE_VAR_NAME,
                        "L" + MODULE + ";");
                mv.visitLdcInsn(globalVar.name.value);
                jvmTypeGen.loadType(mv, globalVar.type);
                mv.visitLdcInsn(getOneBasedLocationString(module, globalVar.pos));
                if (Symbols.isFlagOn(globalVarFlags, Flags.REQUIRED)) {
                    mv.visitInsn(ICONST_1);
                } else {
                    mv.visitInsn(ICONST_0);
                }
                mv.visitMethodInsn(INVOKESPECIAL, VARIABLE_KEY, JVM_INIT_METHOD, INTI_VARIABLE_KEY, false);
                mv.visitVarInsn(ASTORE, varCount + 2);

                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(varCount);
                mv.visitVarInsn(ALOAD, varCount + 2);
                mv.visitInsn(AASTORE);
                varCount++;
            }
        }
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, POPULATE_CONFIG_DATA_METHOD, innerClassName);
        mv.visitEnd();
    }

    private String getOneBasedLocationString(BIRNode.BIRPackage module, Location location) {
        LineRange lineRange = location.lineRange();
        String oneBasedLineTrace = lineRange.fileName() + ":" + (lineRange.startLine().line() + 1);
        if (module.packageID.equals(JvmConstants.DEFAULT)) {
            return oneBasedLineTrace;
        }
        return Utils.decodeIdentifier(module.packageID.toString()) + "(" + oneBasedLineTrace + ")";
    }

    private int calculateConfigArraySize(Collection<BIRNode.BIRGlobalVariableDcl> globalVars) {
        int count = 0;
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            if (Symbols.isFlagOn(globalVar.flags, Flags.CONFIGURABLE)) {
                count++;
            }
        }
        return count;
    }
}
