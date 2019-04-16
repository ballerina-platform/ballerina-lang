// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type ObjectGenerator object {

    public function generateValueClasses(bir:TypeDef?[] typeDefs, map<byte[]> jarEntries) {
        foreach var optionalTypeDef in typeDefs {
            bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
            bir:BType bType = typeDef.typeValue;
            if (bType is bir:BObjectType) {
                string className = typeDef.name.value;
                byte[] bytes = self.createClass(bType, className);
                jarEntries[className + ".class"] = bytes;
            }
        }
    }

    // TODO: add args to the signature
    // TODO: invoke user's init function
    # Create an value instance of a given object type  
    public function createInstance(jvm:MethodVisitor mv, bir:BObjectType objectType) {
        string objectValClassName = objectType.name.value;
        mv.visitTypeInsn(NEW, objectValClassName);
        mv.visitInsn(DUP);
        loadType(mv, objectType);
        mv.visitMethodInsn(INVOKESPECIAL, objectValClassName, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
    }

    // Private methods

    function createClass(bir:BObjectType objectType, string className) returns byte[] {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, (), ABSTRACT_OBJECT_VALUE, ());

        bir:BObjectField?[] fields = objectType.fields;
        self.createFields(cw, fields);

        self.createInit(cw);
        self.createCallMethod(cw);
        self.createGetMethod(cw, fields, className);
        self.createSetMethod(cw, fields, className);
        cw.visitEnd();
        return cw.toByteArray();
    }

    function createFields(jvm:ClassWriter cw, bir:BObjectField?[] fields) {
        foreach var field in fields {
            if (field is bir:BObjectField) {
                jvm:FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.typeValue));
                fv.visitEnd();
            }
        }
    }

    function createInit(jvm:ClassWriter cw) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", io:sprintf("(L%s;)V", BTYPE), (), ());
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke super(type);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_OBJECT_VALUE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    function createCallMethod(jvm:ClassWriter cw) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call",
                io:sprintf("(L%s;[L%s;)L%s;", STRING_VALUE, OBJECT, OBJECT),
                (), ());
        mv.visitCode();
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    function createGetMethod(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, OBJECT), (), ());
        mv.visitCode();

        int fieldNameRegIndex = 2;
        jvm:Label defaultCaseLabel = new jvm:Label();
        jvm:Label[] labels = self.createLabelsforSwitch(mv, fieldNameRegIndex, fields, defaultCaseLabel);
        jvm:Label[] targetLabels = self.createLabelsForEqualCheck(mv, fieldNameRegIndex, fields, labels, 
                defaultCaseLabel);

        int i = 0;
        foreach var optionalField in fields {
            bir:BObjectField field = getObjectField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, field.name.value, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);
            mv.visitInsn(ARETURN);
            i += 1;
        }

        self.createDefaultCase(mv, defaultCaseLabel);
        mv.visitMaxs(fields.length() + 10, fields.length() + 10);
        mv.visitEnd();
    }

    function createSetMethod(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set",
                io:sprintf("(L%s;L%s;)V", STRING_VALUE, OBJECT),
                (), ());
        mv.visitCode();

        int fieldNameRegIndex = 3;
        jvm:Label defaultCaseLabel = new jvm:Label();
        jvm:Label[] labels = self.createLabelsforSwitch(mv, fieldNameRegIndex, fields, defaultCaseLabel);
        jvm:Label[] targetLabels = self.createLabelsForEqualCheck(mv, fieldNameRegIndex, fields, labels, 
                defaultCaseLabel);

        // case body
        int i = 0;
        foreach var optionalField in fields {
            bir:BObjectField field = getObjectField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            addUnboxInsn(mv, field.typeValue);
            mv.visitFieldInsn(PUTFIELD, className, field.name.value, getTypeDesc(field.typeValue));
            mv.visitInsn(RETURN);
            i += 1;
        }

        self.createDefaultCase(mv, defaultCaseLabel);
        mv.visitMaxs(fields.length() + 10, fields.length() + 10);
        mv.visitEnd();
    }

    function createLabelsforSwitch(jvm:MethodVisitor mv, int fieldNameRegIndex, bir:BObjectField?[] fields, 
            jvm:Label defaultCaseLabel) returns jvm:Label[] {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

        // Create labels for the cases
        int i = 0;
        jvm:Label[] labels = [];
        int[] hashCodes = [];
        foreach var field in fields {
            labels[i] = new jvm:Label();
            hashCodes[i] = getObjectField(field).name.value.hashCode();
            i += 1;
        }
        mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels);
        return labels;
    }

    function createDefaultCase(jvm:MethodVisitor mv, jvm:Label defaultCaseLabel) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("error!");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>",
                io:sprintf("(L%s;)V", STRING_VALUE), false);
        mv.visitInsn(ATHROW);
    }

    function createLabelsForEqualCheck(jvm:MethodVisitor mv, int fieldNameRegIndex, bir:BObjectField?[] fields,
            jvm:Label[] labels, jvm:Label defaultCaseLabel) returns jvm:Label[] {
        jvm:Label[] targetLabels = [];
        int i = 0;
        foreach var field in fields {
            mv.visitLabel(labels[i]);
            mv.visitVarInsn(ALOAD, fieldNameRegIndex);
            mv.visitLdcInsn(getObjectField(field).name.value);
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    io:sprintf("(L%s;)Z", OBJECT), false);
            jvm:Label targetLabel = new jvm:Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels[i] = targetLabel;
            i += 1;
        }

        return targetLabels;
    }
};
