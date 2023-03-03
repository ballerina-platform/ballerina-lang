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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_MODULES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_CONSTANT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * Generates Jvm class for the used ballerina module constants for given module.
 *
 * @since 2.0.0
 */
public class JvmModuleConstantsGen {

    private final HashMap<PackageID, String> moduleVarMap;
    private final String moduleConstantClass;
    private final AtomicInteger constantIndex = new AtomicInteger();

    public JvmModuleConstantsGen(BIRNode.BIRPackage module) {
        this.moduleVarMap = new HashMap<>();
        this.moduleConstantClass = getModuleLevelClassName(module.packageID, MODULE_CONSTANT_CLASS_NAME);
    }

    public String addModule(PackageID packageID) {
        return moduleVarMap.computeIfAbsent(packageID,
                                            s -> JvmConstants.MODULE_VAR_PREFIX + constantIndex.getAndIncrement());
    }

    public void generateConstantInit(Map<String, byte[]> jarEntries) {

        if (moduleVarMap.isEmpty()) {
            return;
        }
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, moduleConstantClass);

        moduleVarMap.values().forEach(moduleVar -> visitModuleField(cw, moduleVar));
        // Create multiple module constant init methods based on module count.
        generateModuleInits(cw);
        // Create static initializer which will call previously generated module init methods.
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(moduleConstantClass + ".class", cw.toByteArray());
    }

    private void visitModuleField(ClassWriter cw, String varName) {

        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName, GET_MODULE, null, null);
        fv.visitEnd();
    }

    private void generateModuleInits(ClassWriter cw) {
        MethodVisitor mv = null;
        int moduleCount = 0;
        int methodCount = 0;
        for (Map.Entry<PackageID, String> entry : moduleVarMap.entrySet()) {
            if (moduleCount % MAX_MODULES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, MODULE_INIT_METHOD_PREFIX + methodCount++, "()V", null, null);
            }
            PackageID packageID = entry.getKey();
            String varName = entry.getValue();
            mv.visitTypeInsn(NEW, MODULE);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(Utils.decodeIdentifier(packageID.orgName.value));
            mv.visitLdcInsn(Utils.decodeIdentifier(packageID.name.value));
            mv.visitLdcInsn(getMajorVersion(packageID.version.value));
            if (packageID.isTestPkg) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }
            mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                    INIT_MODULE, false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, moduleConstantClass, varName, GET_MODULE);
            moduleCount++;
            if (moduleCount % MAX_MODULES_PER_METHOD == 0) {
                if (moduleCount != moduleVarMap.size()) {
                    mv.visitMethodInsn(INVOKESTATIC, moduleConstantClass,
                                       MODULE_INIT_METHOD_PREFIX + methodCount, "()V", false);
                    mv.visitInsn(RETURN);
                }
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        // Visit the previously started module init method if not ended.
        if (moduleCount % MAX_MODULES_PER_METHOD != 0) {
            genMethodReturn(mv);
        }
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        mv.visitMethodInsn(INVOKESTATIC, moduleConstantClass, MODULE_INIT_METHOD_PREFIX + 0, "()V", false);
        genMethodReturn(mv);
    }

    public String getModuleConstantClass() {
        return moduleConstantClass;
    }
}

