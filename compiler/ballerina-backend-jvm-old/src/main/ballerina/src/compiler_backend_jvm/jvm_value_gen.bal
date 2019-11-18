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

import ballerina/io;
import ballerina/internal;
import ballerina/jvm;
import ballerina/bir;

public type ObjectGenerator object {

    private bir:Package module;
    private bir:BObjectType? currentObjectType = ();
    private bir:BRecordType? currentRecordType = ();

    public function __init(bir:Package module) {
        self.module = module;
    }

    public function generateValueClasses(bir:TypeDef?[] typeDefs, map<byte[]> jarEntries) {
        foreach var optionalTypeDef in typeDefs {
            bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
            bir:BType bType = typeDef.typeValue;
            if (bType is bir:BObjectType && !bType.isAbstract) {
                self.currentObjectType = bType;
                string className = getTypeValueClassName(self.module, typeDef.name.value);
                byte[] bytes = self.createObjectValueClass(bType, className, typeDef, false);
                jarEntries[className + ".class"] = bytes;
            } else if (bType is bir:BServiceType) {
                self.currentObjectType = bType.oType;
                string className = getTypeValueClassName(self.module, typeDef.name.value);
                byte[] bytes = self.createObjectValueClass(bType.oType, className, typeDef, true);
                jarEntries[className + ".class"] = bytes;
            } else if (bType is bir:BRecordType) {
                self.currentRecordType = bType;
                string className = getTypeValueClassName(self.module, typeDef.name.value);
                byte[] bytes = self.createRecordValueClass(bType, className, typeDef);
                jarEntries[className + ".class"] = bytes;
            }
        }
    }

    // Private methods

    private function createObjectValueClass(bir:BObjectType objectType, string className,
                                            bir:TypeDef typeDef, boolean isService) returns byte[] {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visitSource(typeDef.pos.sourceFileName);
        currentClass = className;
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, (), ABSTRACT_OBJECT_VALUE, [OBJECT_VALUE]);

        bir:BObjectField?[] fields = objectType.fields;
        self.createObjectFields(cw, fields);

        bir:Function?[]? attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            self.createObjectMethods(cw, attachedFuncs, isService, className, typeDef.name.value);
        }

        self.createObjectInit(cw, fields, className);
        self.createCallMethod(cw, attachedFuncs, className, objectType.name.value, isService);
        self.createObjectGetMethod(cw, fields, className);
        self.createObjectSetMethod(cw, fields, className);
        self.createLambdas(cw);
        
        cw.visitEnd();
        var result = cw.toByteArray();
        if !(result is byte[]) {
            logCompileError(result, typeDef, self.module);
            return [];
        } else {
            return result;
        }
    }

    private function createLambdas(jvm:ClassWriter cw) {
        // generate lambdas created during generating methods
        foreach var [name, call] in lambdas.entries() {
            generateLambdaMethod(call, cw, name);
        }
        // clear the lambdas
        lambdas = {};
    }

    private function createObjectFields(jvm:ClassWriter cw, bir:BObjectField?[] fields) {
        foreach var field in fields {
            if (field is bir:BObjectField) {
                jvm:FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.typeValue));
                fv.visitEnd();
                string lockClass = "L" + LOCK_VALUE + ";";
                fv = cw.visitField(ACC_PUBLIC + ACC_FINAL, computeLockNameFromString(field.name.value), lockClass);
                fv.visitEnd();
            }
        }
    }

    private function createObjectMethods(jvm:ClassWriter cw, bir:Function?[] attachedFuncs, boolean isService,
                                        string className, string typeName) {
        foreach var func in attachedFuncs {
            if (func is bir:Function) {
                generateMethod(func, cw, self.module, attachedType = self.currentObjectType, isService = isService, serviceName = typeName);
            }
        }
    }

    private function createObjectInit(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), (), ());
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke super(type);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_OBJECT_VALUE, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), false);
        
        string lockClass = "L" + LOCK_VALUE + ";";
        foreach var field in fields {
            if (field is bir:BObjectField) {
                jvm:Label fLabel = new;
                mv.visitLabel(fLabel);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(NEW, LOCK_VALUE);
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, LOCK_VALUE, "<init>", "()V", false);
                mv.visitFieldInsn(PUTFIELD, className, computeLockNameFromString(field.name.value), lockClass);
            }
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function createCallMethod(jvm:ClassWriter cw, bir:Function?[]? functions, string objClassName,
                                        string objTypeName, boolean isService) {

        bir:Function?[] funcs = getFunctions(functions);

        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call",
                io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT), (), ());
        mv.visitCode();

        int funcNameRegIndex = 2;

        jvm:Label defaultCaseLabel = new jvm:Label();

        // sort the fields before generating switch case
        NodeSorter sorter = new();
        sorter.sortByHash(funcs);

        jvm:Label[] labels = createLabelsforSwitch(mv, funcNameRegIndex, funcs, defaultCaseLabel);
        jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, funcNameRegIndex, funcs, labels,
                defaultCaseLabel);

        // case body
        int i = 0;
        foreach var optionalFunc in funcs {
            bir:Function func = getFunction(optionalFunc);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);

            string methodName = getName(func);
            bir:BType?[] paramTypes = func.typeValue.paramTypes;
            bir:BType? retType = func.typeValue["retType"];

            string methodSig = "";

            // use index access, since retType can be nil.
            methodSig = getMethodDesc(paramTypes, retType);

            // load self
            mv.visitVarInsn(ALOAD, 0);

            // load strand
            mv.visitVarInsn(ALOAD, 1);
            int j = 0;
            foreach var paramType in paramTypes {
                bir:BType pType = getType(paramType);
                // load parameters
                mv.visitVarInsn(ALOAD, 3);

                // load j'th parameter
                mv.visitLdcInsn(j);
                mv.visitInsn(L2I);
                mv.visitInsn(AALOAD);
                addUnboxInsn(mv, pType);
                j += 1;
            }

            mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, getName(func), methodSig, false);
            if (retType is () || retType is bir:BTypeNil) {
                mv.visitInsn(ACONST_NULL);
            } else {
                addBoxInsn(mv, retType);
                if (isService) {
                    mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "handleResourceError", io:sprintf("(L%s;)L%s;",
                                                                                OBJECT, OBJECT) , false);
                }
            }
            mv.visitInsn(ARETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function createObjectGetMethod(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, OBJECT), (), ());
        mv.visitCode();

        int fieldNameRegIndex = 1;
        jvm:Label defaultCaseLabel = new jvm:Label();

        // sort the fields before generating switch case
        NodeSorter sorter = new();
        bir:BObjectField?[] sortedFields = fields.clone();
        sorter.sortByHash(sortedFields);

        jvm:Label[] labels = createLabelsforSwitch(mv, fieldNameRegIndex, sortedFields, defaultCaseLabel);
        jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, sortedFields, labels,
                defaultCaseLabel);

        int i = 0;
        foreach var optionalField in sortedFields {
            bir:BObjectField field = getObjectField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, field.name.value, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);
            mv.visitInsn(ARETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function createObjectSetMethod(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set",
                io:sprintf("(L%s;L%s;)V", STRING_VALUE, OBJECT), (), ());
        mv.visitCode();
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        jvm:Label defaultCaseLabel = new jvm:Label();

        // code gen type checking for inserted value
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "checkFieldUpdate", 
                io:sprintf("(L%s;L%s;)V", STRING_VALUE, OBJECT), false);

        // sort the fields before generating switch case
        NodeSorter sorter = new();
        bir:BObjectField?[] sortedFields = fields.clone();
        sorter.sortByHash(sortedFields);

        jvm:Label[] labels = createLabelsforSwitch(mv, fieldNameRegIndex, sortedFields, defaultCaseLabel);
        jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, sortedFields, labels,
                defaultCaseLabel);

        // case body
        int i = 0;
        foreach var optionalField in sortedFields {
            bir:BObjectField field = getObjectField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, valueRegIndex);
            addUnboxInsn(mv, field.typeValue);
            mv.visitFieldInsn(PUTFIELD, className, field.name.value, getTypeDesc(field.typeValue));
            mv.visitInsn(RETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function getFunction(bir:BAttachedFunction? func) returns bir:BAttachedFunction {
        if (func is bir:BAttachedFunction) {
            return func;
        } else {
            error err = error(io:sprintf("Invalid function: %s", func));
            panic err;
        }
    }

    private function createRecordValueClass(bir:BRecordType recordType, string className, bir:TypeDef typeDef)
            returns byte[] {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visitSource(typeDef.pos.sourceFileName);
        currentClass = <@untainted> className;
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className,
                    io:sprintf("<K:L%s;V:L%s;>L%s<TK;TV;>;L%s<TK;TV;>;", OBJECT, OBJECT, MAP_VALUE_IMPL, MAP_VALUE),
                    MAP_VALUE_IMPL, [MAP_VALUE]);

        bir:Function?[]? attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            self.createRecordMethods(cw, attachedFuncs);
        }

        bir:BRecordField?[] fields = recordType.fields;
        self.createRecordFields(cw, fields);
        self.createRecordGetMethod(cw, fields, className);
        self.createRecordSetMethod(cw, fields, className);
        self.createRecordEntrySetMethod(cw, fields, className);
        self.createRecordContainsKeyMethod(cw, fields, className);
        self.createRecordGetValuesMethod(cw, fields, className);
        self.createGetSizeMethod(cw, fields, className);
        self.createRecordRemoveMethod(cw);
        self.createRecordClearMethod(cw);
        self.createRecordGetKeysMethod(cw, fields, className);

        self.createRecordConstructor(cw, className);
        self.createRecordInitWrapper(cw, className, typeDef);
        self.createLambdas(cw);
        cw.visitEnd();
        var result = cw.toByteArray();
        if !(result is byte[]) {
            logCompileError(result, typeDef, self.module);
            return [];
        } else {
            return result;
        }
    }

    private function createRecordMethods(jvm:ClassWriter cw, bir:Function?[] attachedFuncs) {
        foreach var func in attachedFuncs {
            if (func is bir:Function) {
                generateMethod(func, cw, self.module);
            }
        }
    }

    private function createRecordConstructor(jvm:ClassWriter cw, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", io:sprintf("(L%s;)V", BTYPE), (), ());
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "<init>", io:sprintf("(L%s;)V", BTYPE), false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function createRecordInitWrapper(jvm:ClassWriter cw, string className, bir:TypeDef typeDef) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$init", 
                                              io:sprintf("(L%s;L%s;)V", STRAND, MAP_VALUE), (), ());
        mv.visitCode();
        // load strand
        mv.visitVarInsn(ALOAD, 0);
        // load value
        mv.visitVarInsn(ALOAD, 1);

        // Invoke the init-functions of referenced types. This is done to initialize the 
        // defualt values of the fields coming from the referenced types.
        foreach (bir:BType? typeRef in typeDef.typeRefs) {
            if (typeRef is bir:BRecordType) {
                string refTypeClassName = getTypeValueClassName(typeRef.moduleId, typeRef.name.value);
                mv.visitInsn(DUP2);
                mv.visitMethodInsn(INVOKESTATIC, refTypeClassName, "$init", io:sprintf("(L%s;L%s;)V", STRAND, MAP_VALUE), false);
            }
        }

        // Invoke the init-function of this type.
        string initFuncName;
        string valueClassName;
        bir:Function?[] attachedFuncs = <bir:Function?[]>typeDef.attachedFuncs;

        // Attached functions are empty for type-labeling. In such cases, call the __init() of
        // the original type value;
        if (attachedFuncs.length() != 0) {
            initFuncName = <string> attachedFuncs[0]?.name?.value;
            valueClassName = className;
        } else {
            // record type is the original record-type of this type-label
            bir:BRecordType recordType = <bir:BRecordType> typeDef.typeValue;
            valueClassName = getTypeValueClassName(recordType.moduleId, recordType.name.value);
            initFuncName = cleanupFunctionName(recordType.name.value + "__init_");
        }

        mv.visitMethodInsn(INVOKESTATIC, valueClassName, initFuncName, 
                            io:sprintf("(L%s;L%s;)L%s;", STRAND, MAP_VALUE, OBJECT), false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    private function createRecordFields(jvm:ClassWriter cw, bir:BRecordField?[] fields) {
        foreach var field in fields {
            if (field is bir:BRecordField) {
                jvm:FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.typeValue));
                fv.visitEnd();

                if (self.isOptionalRecordField(field)) {
                    fv = cw.visitField(0, self.getFieldIsPresentFlagName(field.name.value), 
                            getTypeDesc(bir:TYPE_BOOLEAN));
                    fv.visitEnd();
                }
            }
        }
    }

    private function getFieldIsPresentFlagName(string fieldName) returns string {
        return io:sprintf("%s$isPresent", fieldName);
    }

    private function isOptionalRecordField(bir:BRecordField field) returns boolean {
        return (field.flags & BAL_OPTIONAL) == BAL_OPTIONAL;
    }

    private function createRecordGetMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), io:sprintf("(L%s;)TV;", OBJECT), ());
        mv.visitCode();

        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;
        jvm:Label defaultCaseLabel = new jvm:Label();

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        // sort the fields before generating switch case
        NodeSorter sorter = new();
        bir:BRecordField?[] sortedFields = fields.clone();
        sorter.sortByHash(sortedFields);

        jvm:Label[] labels = createLabelsforSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
        jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                defaultCaseLabel);

        int i = 0;
        foreach var optionalField in sortedFields {
            bir:BRecordField field = getRecordField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);

            // if the field is an optional-field, first check the 'isPresent' flag of that field.
            jvm:Label ifPresentLabel = new;
            string fieldName = field.name.value;
            if (self.isOptionalRecordField(field)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, self.getFieldIsPresentFlagName(fieldName),
                                    getTypeDesc(bir:TYPE_BOOLEAN));
                mv.visitJumpInsn(IFNE, ifPresentLabel);
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
            }

            mv.visitLabel(ifPresentLabel);
            // return the value of the field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);
            mv.visitInsn(ARETURN);
            i += 1;
        }

        self.createRecordGetDefaultCase(mv, defaultCaseLabel, strKeyVarIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function createRecordSetMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "putValue",
                io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), "(TK;TV;)TV;", ());

        mv.visitCode();
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        int strKeyVarIndex = 3;
        jvm:Label defaultCaseLabel = new jvm:Label();

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        // sort the fields before generating switch case
        NodeSorter sorter = new();
        bir:BRecordField?[] sortedFields = fields.clone();
        sorter.sortByHash(sortedFields);

        jvm:Label[] labels = createLabelsforSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
        jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                defaultCaseLabel);

        // case body
        int i = 0;
        foreach var optionalField in sortedFields {
            bir:BRecordField field = getRecordField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);

            // load the existing value to return
            string fieldName = field.name.value;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, valueRegIndex);
            addUnboxInsn(mv, field.typeValue);
            mv.visitFieldInsn(PUTFIELD, className, fieldName, getTypeDesc(field.typeValue));

            // if the field is an optional-field, then also set the isPresent flag of that field to true.
            if (self.isOptionalRecordField(field)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ICONST_1);
                mv.visitFieldInsn(PUTFIELD, className, self.getFieldIsPresentFlagName(fieldName), 
                                    getTypeDesc(bir:TYPE_BOOLEAN));
            }

            mv.visitInsn(ARETURN);
            i += 1;
        }

        self.createRecordPutDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, valueRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private function createRecordPutDefaultCase(jvm:MethodVisitor mv, jvm:Label defaultCaseLabel, int nameRegIndex,
                                                int valueRegIndex) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "putValue", 
                            io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), false);
        mv.visitInsn(ARETURN);
    }

    private function createRecordGetDefaultCase(jvm:MethodVisitor mv, jvm:Label defaultCaseLabel, int nameRegIndex) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "get", io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), false);
        mv.visitInsn(ARETURN);
    }

    private function createRecordEntrySetMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "entrySet", 
                            io:sprintf("()L%s;", SET),
                            io:sprintf("()L%s<L%s<TK;TV;>;>;", SET, MAP_ENTRY), 
                            ());
        mv.visitCode();

        int entrySetVarIndex = 1;
        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, entrySetVarIndex);

        foreach var optionalField in fields {
            bir:BRecordField field = getRecordField(optionalField);
            jvm:Label ifNotPresent = new;

            // If its an optional field, generate if-condition to check the presense of the field.
            string fieldName = field.name.value;
            if (self.isOptionalRecordField(field)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, self.getFieldIsPresentFlagName(fieldName), 
                                    getTypeDesc(bir:TYPE_BOOLEAN));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, entrySetVarIndex);
            mv.visitTypeInsn(NEW, MAP_SIMPLE_ENTRY);
            mv.visitInsn(DUP);

            // field name as key
            mv.visitLdcInsn(fieldName);
            // field value as the map-entry value
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);

            mv.visitMethodInsn(INVOKESPECIAL, MAP_SIMPLE_ENTRY, "<init>", io:sprintf("(L%s;L%s;)V", OBJECT, OBJECT),
                                false);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", io:sprintf("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);

            mv.visitLabel(ifNotPresent);
        }

        // Add all from super.enrtySet() to the current entry set.
        mv.visitVarInsn(ALOAD, entrySetVarIndex);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "entrySet", io:sprintf("()L%s;", SET), false);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", io:sprintf("(L%s;)Z", COLLECTION), true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, entrySetVarIndex);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    private function createRecordContainsKeyMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "containsKey", io:sprintf("(L%s;)Z", OBJECT), (), ());
        mv.visitCode();

        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        // sort the fields before generating switch case
        NodeSorter sorter = new();
        bir:BObjectField?[] sortedFields = fields.clone();
        sorter.sortByHash(sortedFields);

        jvm:Label defaultCaseLabel = new jvm:Label();
        jvm:Label[] labels = createLabelsforSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
        jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                defaultCaseLabel);

        int i = 0;
        foreach var optionalField in sortedFields {
            bir:BObjectField field = getObjectField(optionalField);
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);

            string fieldName = field.name.value;
            if (self.isOptionalRecordField(field)) {
                // if the field is optional, then return the value is the 'isPresent' flag.
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, self.getFieldIsPresentFlagName(fieldName), 
                                    getTypeDesc(bir:TYPE_BOOLEAN));
            } else {
                // else always return true.
                mv.visitLdcInsn(true);
            }

            mv.visitInsn(IRETURN);
            i += 1;
        }

        // default case
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "containsKey", io:sprintf("(L%s;)Z", OBJECT), false);
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    function createRecordGetValuesMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor  mv = cw.visitMethod(ACC_PUBLIC, "values", io:sprintf("()L%s;", COLLECTION), 
                            io:sprintf("()L%s<TV;>;", COLLECTION), ());
        mv.visitCode();

        int valuesVarIndex = 1;
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, valuesVarIndex);

        foreach var optionalField in fields {
            bir:BRecordField field = getRecordField(optionalField);
            jvm:Label ifNotPresent = new;

            // If its an optional field, generate if-condition to check the presense of the field.
            string fieldName = field.name.value;
            if (self.isOptionalRecordField(field)) {
                mv.visitVarInsn(ALOAD, 0); // self
                mv.visitFieldInsn(GETFIELD, className, self.getFieldIsPresentFlagName(fieldName),
                                    getTypeDesc(bir:TYPE_BOOLEAN));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, valuesVarIndex);
            mv.visitVarInsn(ALOAD, 0); // self
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", io:sprintf("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
            mv.visitLabel(ifNotPresent);
        }

        mv.visitVarInsn(ALOAD, valuesVarIndex);
        mv.visitVarInsn(ALOAD, 0); // self
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "values", io:sprintf("()L%s;", COLLECTION), false);
        mv.visitMethodInsn(INVOKEINTERFACE, LIST, "addAll", io:sprintf("(L%s;)Z", COLLECTION), true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    function createGetSizeMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "size", "()I", (), ());
        mv.visitCode();
        int sizeVarIndex = 1;

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "size", "()I", false);
        mv.visitVarInsn(ISTORE, sizeVarIndex);

        int requiredFieldsCount = 0;
        foreach var optionalField in fields {
            bir:BObjectField field = getObjectField(optionalField);
            string fieldName = field.name.value;
            if (self.isOptionalRecordField(field)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, self.getFieldIsPresentFlagName(fieldName),
                                    getTypeDesc(bir:TYPE_BOOLEAN));
                jvm:Label l3 = new;
                mv.visitJumpInsn(IFEQ, l3);
                mv.visitIincInsn(sizeVarIndex, 1);
                mv.visitLabel(l3);
            } else {
                requiredFieldsCount += 1;
            }
        }

        mv.visitIincInsn(sizeVarIndex, requiredFieldsCount);
        mv.visitVarInsn(ILOAD, sizeVarIndex);
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    function createRecordRemoveMethod(jvm:ClassWriter cw) {
        // throw an UnsupportedOperationException, since remove is not supported by for records.
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "clear", "()V", (), ());
        mv.visitCode();
        mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, "<init>", "()V", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    function createRecordClearMethod(jvm:ClassWriter cw) {
        // throw an UnsupportedOperationException, since remove is not supported by for records.
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "remove", io:sprintf("(L%s;)L%s;", OBJECT, OBJECT),
                                                io:sprintf("(L%s;)TV;", OBJECT), ());
        mv.visitCode();
        mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, "<init>", "()V", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    function createRecordGetKeysMethod(jvm:ClassWriter cw, bir:BRecordField?[] fields, string className) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getKeys", io:sprintf("()[L%s;",OBJECT), "()[TK;", ());
        mv.visitCode();

        int KeysVarIndex = 1;
        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, KeysVarIndex);

        foreach var optionalField in fields {
            bir:BRecordField field = getRecordField(optionalField);
            jvm:Label ifNotPresent = new;

            // If its an optional field, generate if-condition to check the presense of the field.
            string fieldName = field.name.value;
            if (self.isOptionalRecordField(field)) {
                mv.visitVarInsn(ALOAD, 0); // self
                mv.visitFieldInsn(GETFIELD, className, self.getFieldIsPresentFlagName(fieldName),
                                    getTypeDesc(bir:TYPE_BOOLEAN));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, KeysVarIndex);
            mv.visitLdcInsn(fieldName);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", io:sprintf("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
            mv.visitLabel(ifNotPresent);
        }

        mv.visitVarInsn(ALOAD, KeysVarIndex);
        mv.visitVarInsn(ALOAD, 0); // self
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "keySet", io:sprintf("()L%s;", SET), false);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", io:sprintf("(L%s;)Z", COLLECTION), true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, KeysVarIndex);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "size", "()I", true);
        mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "toArray", io:sprintf("([L%s;)[L%s;", OBJECT, OBJECT), true);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
};

function injectDefaultParamInitsToAttachedFuncs(bir:Package module) {
    bir:TypeDef?[] typeDefs = module.typeDefs;
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BObjectType && !bType.isAbstract) {
            desugarObjectMethods(module, bType, typeDef.attachedFuncs);
        } else if (bType is bir:BServiceType) {
            desugarObjectMethods(module, bType, typeDef.attachedFuncs);
        } else if (bType is bir:BRecordType) {
            desugarObjectMethods(module, bType, typeDef.attachedFuncs);
        }
    }
}

function desugarObjectMethods(bir:Package module, bir:BType bType, bir:Function?[]? attachedFuncs) {
    if (attachedFuncs is bir:Function?[]) {
        foreach var func in attachedFuncs {
            if (func is bir:Function) {
                if isExternFunc(func) {
                    var extFuncWrapper = lookupBIRFunctionWrapper(module, func, attachedType = bType);
                    if extFuncWrapper is OldStyleExternalFunctionWrapper {
                        // Note when this support new interop, update here as well TODO
                        desugarOldExternFuncs(module, extFuncWrapper, func);
                    }
                } else {
                    addDefaultableBooleanVarsToSignature(func);
                }
                enrichWithDefaultableParamInits(getFunction(<@untainted> func));
            }
        }
    }
}

function createLabelsforSwitch(jvm:MethodVisitor mv, int nameRegIndex, NamedNode?[] nodes,
        jvm:Label defaultCaseLabel) returns jvm:Label[] {
    mv.visitVarInsn(ALOAD, nameRegIndex);
    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

    // Create labels for the cases
    int i = 0;
    jvm:Label[] labels = [];
    int[] hashCodes = [];
    foreach var node in nodes {
        if (node is NamedNode) {
            labels[i] = new jvm:Label();
            hashCodes[i] = internal:hashCode(getName(node));
            i += 1;
        }
    }
    mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels);
    return labels;
}

function createDefaultCase(jvm:MethodVisitor mv, jvm:Label defaultCaseLabel, int nameRegIndex) {
    mv.visitLabel(defaultCaseLabel);
    mv.visitTypeInsn(NEW, BLANG_RUNTIME_EXCEPTION);
    mv.visitInsn(DUP);

    // Create error message
    mv.visitTypeInsn(NEW, STRING_BUILDER);
    mv.visitInsn(DUP);
    mv.visitLdcInsn("No such field or method: ");
    mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER, "<init>", io:sprintf("(L%s;)V", STRING_VALUE), false);
    mv.visitVarInsn(ALOAD, nameRegIndex);
    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "append", io:sprintf("(L%s;)L%s;", STRING_VALUE, STRING_BUILDER), false);
    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "toString", io:sprintf("()L%s;", STRING_VALUE), false);

    mv.visitMethodInsn(INVOKESPECIAL, BLANG_RUNTIME_EXCEPTION, "<init>",
            io:sprintf("(L%s;)V", STRING_VALUE), false);
    mv.visitInsn(ATHROW);
}

function getTypeValueClassName(bir:Package|bir:ModuleID module, string typeName) returns string {
    string packageName;
    if (module is bir:Package) {
        packageName = getPackageName(module.org.value, module.name.value);
    } else {
        packageName = getPackageName(module.org, module.name);
    }

    return packageName + "$value$" + cleanupTypeName(typeName);
}

function createLabelsForEqualCheck(jvm:MethodVisitor mv, int nameRegIndex, NamedNode?[] nodes,
        jvm:Label[] labels, jvm:Label defaultCaseLabel) returns jvm:Label[] {
    jvm:Label[] targetLabels = [];
    int i = 0;
    foreach var node in nodes {
        if (node is NamedNode) {
            mv.visitLabel(labels[i]);
            mv.visitVarInsn(ALOAD, nameRegIndex);
            mv.visitLdcInsn(getName(node));
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    io:sprintf("(L%s;)Z", OBJECT), false);
            jvm:Label targetLabel = new jvm:Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels[i] = targetLabel;
            i += 1;
        }
    }

    return targetLabels;
}

function getName(any node) returns string {
    if (node is NamedNode) {
        return node.name.value;
    } else {
        error err = error(io:sprintf("Invalid node: %s", node));
        panic err;
    }
}

// --------------------- Sorting ---------------------------

type NamedNode record {
    bir:Name name = {};
};

type NodeSorter object {

    function sortByHash(NamedNode?[] arr) {
        self.quickSort(arr, 0, arr.length() - 1);
    }

    private function quickSort(NamedNode?[] arr, int low, int high) { 
        if (low < high) { 
            // pi is partitioning index, arr[pi] is now at right place
            int pi = self.partition(arr, low, high); 

            // Recursively sort elements before partition and after partition 
            self.quickSort(arr, low, pi - 1); 
            self.quickSort(arr, pi + 1, high); 
        } 
    }

    private function partition(NamedNode?[] arr, int begin, int end) returns int {
        int pivot = self.getHash(arr[end]);
        int i = begin - 1;

        int j = begin;
        while (j < end) {
            if (self.getHash(arr[j]) <= pivot) {
                i += 1;
                self.swap(arr, i, j);
            }
            j += 1;
        }
        self.swap(arr, i+1, end);
        return i + 1;
    }

    private function getHash(any node) returns int {
        return internal:hashCode(getName(node));
    }

    private function swap(NamedNode?[] arr, int i, int j) {
        NamedNode? swapTemp = arr[i];
        arr[i] = arr[j];
        arr[j] = swapTemp;
    }
};
