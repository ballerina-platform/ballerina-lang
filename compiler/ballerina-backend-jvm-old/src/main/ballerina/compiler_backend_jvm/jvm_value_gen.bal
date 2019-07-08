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
                string className = self.getTypeValueClassName(typeDef.name.value);
                byte[] bytes = self.createObjectValueClass(bType, className, typeDef);
                jarEntries[className + ".class"] = bytes;
            } else if (bType is bir:BServiceType) {
                self.currentObjectType = bType.oType;
                string className = self.getTypeValueClassName(typeDef.name.value);
                byte[] bytes = self.createObjectValueClass(bType.oType, className, typeDef);
                jarEntries[className + ".class"] = bytes;
            } else if (bType is bir:BRecordType) {
                self.currentRecordType = bType;
                string className = self.getTypeValueClassName(typeDef.name.value);
                byte[] bytes = self.createRecordValueClass(bType, className, typeDef);
                jarEntries[className + ".class"] = bytes;
            }
        }
    }

    // Private methods

    private function getTypeValueClassName(string typeName) returns string {
        return getPackageName(self.module.org.value, self.module.name.value) + cleanupTypeName(typeName);
    }

    private function createObjectValueClass(bir:BObjectType objectType, string className, bir:TypeDef typeDef) 
            returns byte[] {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visitSource(typeDef.pos.sourceFileName);
        currentClass = className;
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, (), ABSTRACT_OBJECT_VALUE, [OBJECT_VALUE]);

        bir:BObjectField?[] fields = objectType.fields;
        self.createObjectFields(cw, fields);

        bir:Function?[]? attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            self.createObjectMethods(cw, attachedFuncs);
        }

        self.createObjectInit(cw);
        self.createCallMethod(cw, attachedFuncs, className, objectType.name.value);
        self.createGetMethod(cw, fields, className);
        self.createSetMethod(cw, fields, className);
        self.createLambdas(cw);
        
        cw.visitEnd();
        return cw.toByteArray();
    }

    private function createLambdas(jvm:ClassWriter cw) {
        // generate lambdas created during generating methods
        foreach var (name, call) in lambdas {
            generateLambdaMethod(call[0], cw, call[1], name);
        }
        // clear the lambdas
        lambdas = {};
    }

    private function createObjectFields(jvm:ClassWriter cw, bir:BObjectField?[] fields) {
        foreach var field in fields {
            if (field is bir:BObjectField) {
                jvm:FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.typeValue));
                fv.visitEnd();
            }
        }
    }

    private function createObjectMethods(jvm:ClassWriter cw, bir:Function?[] attachedFuncs) {
        foreach var func in attachedFuncs {
            if (func is bir:Function) {
                addDefaultableBooleanVarsToSignature(func);
                generateMethod(func, cw, self.module, attachedType = self.currentObjectType);
            }
        }
    }

    private function createObjectInit(jvm:ClassWriter cw) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), (), ());
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke super(type);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_OBJECT_VALUE, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    private function createCallMethod(jvm:ClassWriter cw, bir:Function?[]? functions, string objClassName,
                                        string objTypeName) {

        bir:Function?[] funcs = getFunctions(functions);

        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call",
                io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT), (), ());
        mv.visitCode();

        int funcNameRegIndex = 2;

        // Uncomment to get some debug information at runtime
        // mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // mv.visitLdcInsn(objClassName + " - ");
        // mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

        // mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // mv.visitVarInsn(ALOAD, funcNameRegIndex);
        // mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

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

            if (isExternFunc(func)) {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "blockedOnExtern", "Z");
                jvm:Label blockedOnExternLabel = new;
                mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

                mv.visitVarInsn(ALOAD, 1);
                mv.visitInsn(ICONST_0);
                mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "blockedOnExtern", "Z");

                if (!(retType is () || retType is bir:BTypeNil)) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "returnValue", "Ljava/lang/Object;");
                    mv.visitInsn(ARETURN);
                } else {
                    mv.visitInsn(ACONST_NULL);
                    mv.visitInsn(ARETURN);
                }

                mv.visitLabel(blockedOnExternLabel);

                string lookupKey = getPackageName(self.module.org.value, self.module.name.value) + objTypeName + "." +
                                                    methodName;
                methodSig = lookupJavaMethodDescription(lookupKey);
                string className = lookupFullQualifiedClassName(lookupKey);

                // load strand
                mv.visitVarInsn(ALOAD, 1);
                // load self
                mv.visitVarInsn(ALOAD, 0);

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
                    
                    // assuming that boolean args are added with named/expr args support
                    j += 2;
                }

                mv.visitMethodInsn(INVOKESTATIC, className, getName(func), methodSig, false);
                if (retType is () || retType is bir:BTypeNil) {
                    mv.visitInsn(ACONST_NULL);
                } else {
                    addBoxInsn(mv, retType);
                }
                mv.visitInsn(ARETURN);
            } else {
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
                }
                mv.visitInsn(ARETURN);
            }
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex);
        mv.visitMaxs(funcs.length() + 10, funcs.length() + 10);
        mv.visitEnd();
    }

    private function createGetMethod(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
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
        mv.visitMaxs(sortedFields.length() + 10, sortedFields.length() + 10);
        mv.visitEnd();
    }

    private function createSetMethod(jvm:ClassWriter cw, bir:BObjectField?[] fields, string className) {
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
        mv.visitMaxs(sortedFields.length() + 10, sortedFields.length() + 10);
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
        currentClass = untaint className;
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, (), MAP_VALUE_IMPL, [MAP_VALUE]);

        bir:Function?[]? attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            self.createRecordMethods(cw, attachedFuncs);
        }

        self.createRecordConstructor(cw, className);
        self.createLambdas(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private function createRecordMethods(jvm:ClassWriter cw, bir:Function?[] attachedFuncs) {
        foreach var func in attachedFuncs {
            if (func is bir:Function) {
                generateMethod(func, cw, self.module, attachedType = self.currentRecordType);
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

        // invoke super(type);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
        mv.visitVarInsn(ALOAD, 0);

        mv.visitTypeInsn(NEW, "org/ballerinalang/jvm/Strand");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "org/ballerinalang/jvm/Scheduler");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_4);
        //TODO remove this and load the strand from ALOAD
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, "<init>", "(IZ)V", false);

        mv.visitMethodInsn(INVOKESPECIAL, "org/ballerinalang/jvm/Strand", "<init>",
                            "(Lorg/ballerinalang/jvm/Scheduler;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "__init_", "(Lorg/ballerinalang/jvm/Strand;)V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }
};

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
            hashCodes[i] = getName(node).hashCode();
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
        return getName(node).hashCode();
    }

    private function swap(NamedNode?[] arr, int i, int j) {
        NamedNode? swapTemp = arr[i];
        arr[i] = arr[j];
        arr[j] = swapTemp;
    }
};
