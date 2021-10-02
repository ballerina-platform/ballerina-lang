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
package org.wso2.ballerinalang.compiler.bir.codegen.split.types;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_UNION_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_ORIGINAL_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_IMPL;

/**
 * BIR union types to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmUnionTypeGen {

    public final String unionTypesClass;
    public final ClassWriter unionTypesCw;
    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final  JvmConstantsGen jvmConstantsGen;

    public JvmUnionTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen,
                           PackageID packageID) {
        this.unionTypesClass = getModuleLevelClassName(packageID, MODULE_UNION_TYPES_CLASS_NAME);
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.unionTypesCw = new BallerinaClassWriter(COMPUTE_FRAMES);
        this.unionTypesCw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, unionTypesClass, null, OBJECT, null);
    }

    public void visitEnd(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module, Map<String, byte[]> jarEntries) {
        unionTypesCw.visitEnd();
        jarEntries.put(unionTypesClass + ".class", jvmPackageGen.getBytes(unionTypesCw, module));
    }


    /**
     * Create a runtime type instance for union used in type definitions.
     *
     * @param mv        method visitor
     * @param unionType union type
     */
    public void createUnionType(MethodVisitor mv, BUnionType unionType) {
        mv.visitTypeInsn(NEW, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);

        boolean nameLoaded = jvmTypeGen.loadUnionName(mv, unionType);
        if (nameLoaded) {
            BTypeSymbol tsymbol = unionType.tsymbol;
            if (tsymbol == null) {
                mv.visitInsn(ACONST_NULL);
            } else {
                String varName = jvmConstantsGen.getModuleConstantVar(unionType.tsymbol.pkgID);
                mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName,
                        String.format("L%s;", MODULE));
            }
        }

        mv.visitLdcInsn(jvmTypeGen.typeFlag(unionType));

        jvmTypeGen.loadCyclicFlag(mv, unionType);

        mv.visitLdcInsn(unionType.flags);
        // initialize the union type without the members array
        if (nameLoaded) {
            mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD,
                    String.format("(L%s;L%s;IZJ)V", STRING_VALUE, MODULE), false);
        } else {
            mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD, "(IZJ)V", false);
        }
    }

    public void populateUnion(ClassWriter cw, MethodVisitor mv, BUnionType bType, String className, String name) {
        mv.visitTypeInsn(CHECKCAST, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);

        // populate member fields
        addUnionMembers(cw, mv, bType, className, name);
        jvmCreateTypeGen.addImmutableType(mv, bType);
    }

    /**
     * Add member type to unions in a type definition.
     *
     * @param mv        method visitor
     * @param unionType unionType
     */
    private void addUnionMembers(ClassWriter cw, MethodVisitor mv, BUnionType unionType, String className,
                                 String name) {
        jvmTypeGen.createUnionMembersArray(cw, mv, unionType.getMemberTypes(), className, name);
        mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_MEMBERS_METHOD,
                String.format("([L%s;)V", TYPE), false);

        jvmTypeGen.createUnionMembersArray(cw, mv, unionType.getOriginalMemberTypes(), className, "original" + name);
        mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_ORIGINAL_MEMBERS_METHOD,
                String.format("([L%s;)V", TYPE), false);
    }
}
