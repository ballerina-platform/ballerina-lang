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

import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_ARRAY_ELEMENT;

/**
 * BIR array types to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmArrayTypeGen {

    private final JvmTypeGen jvmTypeGen;

    public JvmArrayTypeGen(JvmTypeGen jvmTypeGen) {
        this.jvmTypeGen = jvmTypeGen;
    }

    public void populateArray(MethodVisitor mv, BArrayType bType) {
        mv.visitTypeInsn(CHECKCAST, ARRAY_TYPE_IMPL);
        jvmTypeGen.loadType(mv, bType.eType);
        loadDimension(mv, bType.eType, 1);
        jvmTypeGen.loadReadonlyFlag(mv, bType.eType);
        mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_TYPE_IMPL, "setElementType", SET_ARRAY_ELEMENT, false);
    }

    private void loadDimension(MethodVisitor mv, BType eType, int dimension) {
        switch (eType.tag) {
            case TypeTags.ARRAY:
                loadDimension(mv, ((BArrayType) eType).eType, dimension + 1);
                break;
            case TypeTags.TYPEREFDESC:
                loadDimension(mv, ((BTypeReferenceType) eType).referredType, dimension);
                break;
            default:
                mv.visitLdcInsn(dimension);
        }
    }

    /**
     * Create a runtime type instance for array.
     * @param mv        method visitor
     * @param arrayType array type
     * @param types     types instance to check filler value
     */
    public void createArrayType(MethodVisitor mv, BArrayType arrayType, Types types) {
        // Create an new array type
        mv.visitTypeInsn(NEW, ARRAY_TYPE_IMPL);
        mv.visitInsn(DUP);

        if (TypeTags.isSimpleBasicType(arrayType.eType.tag)) {
            // Load the element type
            jvmTypeGen.loadType(mv, arrayType.eType);
            int arraySize = arrayType.size;
            mv.visitLdcInsn((long) arraySize);
            mv.visitInsn(L2I);

            jvmTypeGen.loadReadonlyFlag(mv, arrayType);
            mv.visitLdcInsn(jvmTypeGen.typeFlag(arrayType.eType));
            mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE_IMPL, JVM_INIT_METHOD, INIT_ARRAY_TYPE_IMPL, false);
            return;
        }

        mv.visitLdcInsn(jvmTypeGen.typeFlag(arrayType.eType));

        int arraySize = arrayType.size;
        mv.visitLdcInsn((long) arraySize);
        mv.visitInsn(L2I);

        jvmTypeGen.loadReadonlyFlag(mv, arrayType);
        mv.visitInsn(types.hasFillerValue(arrayType.eType) ? ICONST_1 : ICONST_0);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE_IMPL, JVM_INIT_METHOD,  "(IIZZ)V", false);
    }

}
