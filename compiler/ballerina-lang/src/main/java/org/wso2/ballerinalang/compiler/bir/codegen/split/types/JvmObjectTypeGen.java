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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.model.DoubleCheckLabelsRecord;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLIENT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.POPULATE_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REMOTE_METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RESOURCE_METHOD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RESOURCE_METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_IMMUTABLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_TYPEID_SET_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.METHOD_TYPE_IMPL_ARRAY_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.METHOD_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.METHOD_TYPE_IMPL_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.POPULATE_ATTACHED_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RESOURCE_METHOD_TYPE_ARRAY_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RESOURCE_METHOD_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_IMMUTABLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_METHODS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_RESOURCE_METHOD_TYPE_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.endDoubleCheckGetEnd;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.genDoubleCheckGetStart;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.toNameString;

/**
 * BIR object type to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmObjectTypeGen {

    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;
    public int methodCount = 0;

    public JvmObjectTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void createObjectType(ClassWriter cw, MethodVisitor mv, String objectTypeClass, BObjectType objectType,
                                 String fieldName, boolean isAnnotatedType, BIRVarToJVMIndexMap indexMap,
                                 SymbolTable symbolTable) {
        // Create type field
        cw.visitField(ACC_STATIC | ACC_PUBLIC | ACC_FINAL, TYPE_VAR_FIELD, GET_OBJECT_TYPE_IMPL, null, null).visitEnd();
        // Create the object type
        String objectClassName = Symbols.isService(objectType.tsymbol) ? SERVICE_TYPE_IMPL :
                Symbols.isClient(objectType.tsymbol) ? CLIENT_TYPE_IMPL : OBJECT_TYPE_IMPL;
        mv.visitTypeInsn(NEW, objectClassName);
        mv.visitInsn(DUP);
        // Load type name
        BTypeSymbol typeSymbol = objectType.tsymbol;
        mv.visitLdcInsn(decodeIdentifier(typeSymbol.name.getValue()));
        // Load module
        String moduleVar = jvmConstantsGen.getModuleConstantVar(objectType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        // Load flags
        mv.visitLdcInsn(typeSymbol.flags);
        // initialize the object
        mv.visitMethodInsn(INVOKESPECIAL, objectClassName, JVM_INIT_METHOD, INIT_OBJECT, false);
        mv.visitFieldInsn(PUTSTATIC, objectTypeClass, TYPE_VAR_FIELD, GET_OBJECT_TYPE_IMPL);
        genGetTypeMethod(cw, objectType, objectTypeClass, fieldName, moduleVar, isAnnotatedType, symbolTable, indexMap);
    }

    private void genGetTypeMethod(ClassWriter cw, BObjectType objectType, String objectTypeClass, String fieldName,
                                  String moduleVar, boolean isAnnotatedType, SymbolTable symbolTable,
                                  BIRVarToJVMIndexMap indexMap) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, GET_TYPE_METHOD, GET_OBJECT_TYPE_METHOD, null, null);
        mv.visitCode();
        DoubleCheckLabelsRecord checkLabelsRecord = genDoubleCheckGetStart(mv, objectTypeClass, GET_OBJECT_TYPE_IMPL
                                                                          );
        populateObject(cw, mv, objectTypeClass, fieldName, moduleVar, objectType, symbolTable, indexMap);
        endDoubleCheckGetEnd(mv, objectTypeClass, GET_OBJECT_TYPE_IMPL, checkLabelsRecord, isAnnotatedType);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public void populateObject(ClassWriter cw, MethodVisitor mv, String objectTypeClass, String fieldName,
                               String moduleVar, BObjectType bType, SymbolTable symbolTable,
                               BIRVarToJVMIndexMap indexMap) {
        Optional<BIntersectionType> immutableType = jvmCreateTypeGen.getImmutableType(bType, symbolTable);
        BTypeIdSet objTypeIdSet = bType.typeIdSet;
        mv.visitFieldInsn(GETSTATIC, objectTypeClass, TYPE_VAR_FIELD, GET_OBJECT_TYPE_IMPL);
        mv.visitInsn(DUP);
        if (immutableType.isPresent()) {
            mv.visitInsn(DUP);
        }
        if (!objTypeIdSet.isEmpty()) {
            mv.visitInsn(DUP);
        }
        addObjectFields(cw, mv, objectTypeClass, bType);
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bType.tsymbol;
        addObjectInitFunction(mv, objectTypeClass, objectTypeSymbol.generatedInitializerFunc, bType, indexMap,
                "$init$",  moduleVar, symbolTable, true);
        addObjectInitFunction(mv, objectTypeClass, objectTypeSymbol.initializerFunc, bType, indexMap, "init",
                moduleVar, symbolTable, false);
        addResourceMethods(cw, mv, objectTypeClass, fieldName, moduleVar, objectTypeSymbol.attachedFuncs, bType,
                symbolTable);
        addObjectAttachedFunctions(cw, mv, objectTypeClass, fieldName, moduleVar, objectTypeSymbol.attachedFuncs, bType,
                symbolTable);
        if (immutableType.isPresent()) {
            jvmTypeGen.loadType(mv, immutableType.get());
            mv.visitMethodInsn(INVOKEINTERFACE, TYPE, SET_IMMUTABLE_TYPE_METHOD, SET_IMMUTABLE_TYPE, true);
        }
        if (!objTypeIdSet.isEmpty()) {
            jvmCreateTypeGen.loadTypeIdSet(mv, objTypeIdSet);
            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, SET_TYPEID_SET_METHOD, SET_TYPE_ID_SET, false);
        }
    }


    private void addObjectAttachedFunctions(ClassWriter cw, MethodVisitor mv, String objectTypeClass, String fieldName,
                                            String moduleVar, List<BAttachedFunction> attachedFunctions,
                                            BObjectType objType, SymbolTable symbolTable) {
        // Create the attached function array
        mv.visitLdcInsn((long) attachedFunctions.size() - resourceFunctionCount(attachedFunctions));
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, METHOD_TYPE_IMPL);
        String methodName = POPULATE_METHOD_PREFIX + fieldName + "$attachedFunctions";
        int methodCount = splitObjectAttachedFunctions(cw, objectTypeClass, methodName, moduleVar, attachedFunctions,
                objType, symbolTable);
        if (methodCount > 0) {
            mv.visitVarInsn(ASTORE, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, objectTypeClass, methodName + 0, POPULATE_ATTACHED_FUNCTION, false);
            mv.visitVarInsn(ALOAD, 0);
        }
        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setMethods", SET_METHODS, false);
    }

    private int splitObjectAttachedFunctions(ClassWriter cw, String objectTypeClass, String methodName,
                                             String moduleVar, List<BAttachedFunction> attachedFunctions,
                                             BObjectType objType, SymbolTable symbolTable) {
        int fTypeCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        List<BAttachedFunction> nonResourceFunctions = new ArrayList<>();
        for (BAttachedFunction attachedFunc : attachedFunctions) {
            if (attachedFunc != null && !(attachedFunc instanceof BResourceFunction)) {
                nonResourceFunctions.add(attachedFunc);
            }
        }
        indexMap.addIfNotExists("$array", symbolTable.anyType);
        for (BAttachedFunction attachedFunc : nonResourceFunctions) {
            if (fTypeCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName + methodCount++,
                        METHOD_TYPE_IMPL_ARRAY_PARAM, null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
            }
            // create and load attached function
            createObjectMemberFunction(mv, objectTypeClass, moduleVar, attachedFunc);
            int attachedFunctionVarIndex = indexMap.addIfNotExists(toNameString(objType) + attachedFunc.funcName.value,
                    symbolTable.anyType);
            mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) fTypeCount);
            mv.visitInsn(L2I);
            // Add the member to the array
            mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
            mv.visitInsn(AASTORE);
            fTypeCount++;
            if (fTypeCount % MAX_TYPES_PER_METHOD == 0) {
                if (fTypeCount != nonResourceFunctions.size()) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, objectTypeClass, methodName + methodCount,
                            METHOD_TYPE_IMPL_ARRAY_PARAM, false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(fTypeCount + 10, fTypeCount + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && fTypeCount % MAX_TYPES_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(fTypeCount + 10, fTypeCount + 10);
            mv.visitEnd();
        }
        return methodCount;
    }

    private void addObjectInitFunction(MethodVisitor mv, String objectTypeClass, BAttachedFunction initFunction,
                                       BObjectType objType, BIRVarToJVMIndexMap indexMap, String funcName,
                                       String moduleVar, SymbolTable symbolTable, boolean isGeneratedInit) {
        if (initFunction == null || !initFunction.funcName.value.contains(funcName)) {
            return;
        }
        mv.visitInsn(DUP);
        createObjectMemberFunction(mv, objectTypeClass, moduleVar, initFunction);
        int attachedFunctionVarIndex = indexMap.addIfNotExists(objType.name + initFunction.funcName.value,
                symbolTable.anyType);
        mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);
        mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
        mv.visitInsn(DUP);
        mv.visitInsn(POP);
        if (isGeneratedInit) {
            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setGeneratedInitMethod", METHOD_TYPE_IMPL_PARAM,
                    false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setInitMethod", SET_INIT_METHOD, false);
        }
    }

    private void addResourceMethods(ClassWriter cw, MethodVisitor mv, String objectTypeClass, String fieldName,
                                    String moduleVar, List<BAttachedFunction> attachedFunctions, BObjectType objType,
                                    SymbolTable symbolTable) {
        if (!Symbols.isService(objType.tsymbol) && !Symbols.isClient(objType.tsymbol)) {
            return;
        }
        String objectClassName = Symbols.isService(objType.tsymbol) ? SERVICE_TYPE_IMPL : CLIENT_TYPE_IMPL;
        mv.visitInsn(DUP);
        mv.visitTypeInsn(CHECKCAST, objectClassName);
        // Create the resource function array
        mv.visitLdcInsn(resourceFunctionCount(attachedFunctions));
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, RESOURCE_METHOD_TYPE);
        String methodName = POPULATE_METHOD_PREFIX + fieldName + "$resourceFunctions";
        int methodCount = splitResourceMethods(cw, objectTypeClass, methodName, moduleVar, attachedFunctions, objType,
                symbolTable);
        if (methodCount > 0) {
            mv.visitVarInsn(ASTORE, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, objectTypeClass, methodName + 0, SET_RESOURCE_METHOD_TYPE_ARRAY, false);
            mv.visitVarInsn(ALOAD, 0);
        }
        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, objectClassName, "setResourceMethods", RESOURCE_METHOD_TYPE_ARRAY_PARAM,
                false);
    }

    private int splitResourceMethods(ClassWriter cw, String objectTypeClass, String methodName, String moduleVar,
                                     List<BAttachedFunction> attachedFunctions, BObjectType objType,
                                     SymbolTable symbolTable) {
        int resourcesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        List<BAttachedFunction> resourceFunctions = new ArrayList<>();
        for (BAttachedFunction attachedFunc : attachedFunctions) {
            if (attachedFunc instanceof BResourceFunction) {
                resourceFunctions.add(attachedFunc);
            }
        }
        indexMap.addIfNotExists("$array", symbolTable.anyType);
        for (BAttachedFunction attachedFunc : resourceFunctions) {
            if (resourcesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName + methodCount++,
                        RESOURCE_METHOD_TYPE_ARRAY_PARAM, null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
            }
            BResourceFunction resourceFunction = (BResourceFunction) attachedFunc;
            createResourceFunction(mv, objectTypeClass, moduleVar, resourceFunction);
            String varRefName = toNameString(objType) + resourceFunction.funcName.value + "$r$func";
            int rFuncVarIndex = indexMap.addIfNotExists(varRefName, symbolTable.anyType);
            mv.visitVarInsn(ASTORE, rFuncVarIndex);
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) resourcesCount);
            mv.visitInsn(L2I);
            // Add the member to the array
            mv.visitVarInsn(ALOAD, rFuncVarIndex);
            mv.visitInsn(AASTORE);
            resourcesCount++;
            if (resourcesCount % MAX_TYPES_PER_METHOD == 0) {
                if (resourcesCount != resourceFunctions.size()) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, objectTypeClass, methodName + methodCount,
                            RESOURCE_METHOD_TYPE_ARRAY_PARAM, false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(resourcesCount + 10, resourcesCount + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && resourcesCount % MAX_TYPES_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(resourcesCount + 10, resourcesCount + 10);
            mv.visitEnd();
        }
        return methodCount;
    }

    private static long resourceFunctionCount(List<BAttachedFunction> attachedFunctions) {
        int i = 0;
        for (BAttachedFunction attachedFunction : attachedFunctions) {
            if (attachedFunction instanceof BResourceFunction) {
                i++;
            }
        }
        return i;
    }

    private void createObjectMemberFunction(MethodVisitor mv, String objectTypeClass, String moduleVar,
                                            BAttachedFunction attachedFunc) {
        if (Symbols.isRemote(attachedFunc.symbol)) {
            createRemoteFunction(mv, objectTypeClass, moduleVar, attachedFunc);
            return;
        }
        mv.visitTypeInsn(NEW, METHOD_TYPE_IMPL);
        mv.visitInsn(DUP);
        // Load function name
        mv.visitLdcInsn(decodeIdentifier(attachedFunc.funcName.value));
        // Load module
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        // Load the parent object type
        mv.visitFieldInsn(GETSTATIC, objectTypeClass, TYPE_VAR_FIELD, GET_OBJECT_TYPE_IMPL);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
        // Load the field type
        jvmTypeGen.loadType(mv, attachedFunc.type);
        // Load flags
        mv.visitLdcInsn(attachedFunc.symbol.flags);
        mv.visitMethodInsn(INVOKESPECIAL, METHOD_TYPE_IMPL, JVM_INIT_METHOD, METHOD_TYPE_IMPL_INIT, false);
    }

    private void createRemoteFunction(MethodVisitor mv, String objectTypeClass, String moduleVar,
                                      BAttachedFunction attachedFunc) {
        mv.visitTypeInsn(NEW, REMOTE_METHOD_TYPE_IMPL);
        mv.visitInsn(DUP);
        // Load function name
        mv.visitLdcInsn(decodeIdentifier(attachedFunc.funcName.value));
        // Load module
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        // Load the parent object type
        mv.visitFieldInsn(GETSTATIC, objectTypeClass, TYPE_VAR_FIELD, GET_OBJECT_TYPE_IMPL);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
        // Load the field type
        jvmTypeGen.loadType(mv, attachedFunc.type);
        // Load flags
        mv.visitLdcInsn(attachedFunc.symbol.flags);
        mv.visitMethodInsn(INVOKESPECIAL, REMOTE_METHOD_TYPE_IMPL, JVM_INIT_METHOD, METHOD_TYPE_IMPL_INIT, false);

    }

    private void createResourceFunction(MethodVisitor mv, String objectTypeClass, String moduleVar,
                                        BResourceFunction resourceFunction) {
        mv.visitTypeInsn(NEW, RESOURCE_METHOD_TYPE_IMPL);
        mv.visitInsn(DUP);
        // Load function name
        mv.visitLdcInsn(resourceFunction.funcName.value);
        // Load module
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        // Load the parent object type
        mv.visitFieldInsn(GETSTATIC, objectTypeClass, TYPE_VAR_FIELD, GET_OBJECT_TYPE_IMPL);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
        // Load the invokable type
        jvmTypeGen.loadType(mv, resourceFunction.type);
        // Load the resource path type
        List<BResourcePathSegmentSymbol> pathSegmentSymbols = resourceFunction.pathSegmentSymbols;
        int pathSegmentSize = pathSegmentSymbols.size();
        mv.visitLdcInsn((long) pathSegmentSize);
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, TYPE);
        for (int i = 0; i < pathSegmentSize; i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            // load resource path name
            jvmTypeGen.loadType(mv, pathSegmentSymbols.get(i).type);
            mv.visitInsn(AASTORE);
        }
        // Load flags
        mv.visitLdcInsn(resourceFunction.symbol.flags);
        // Load accessor
        mv.visitLdcInsn(decodeIdentifier(resourceFunction.accessor.value));
        // Load resource path
        mv.visitLdcInsn((long) pathSegmentSize);
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
        for (int i = 0; i < pathSegmentSize; i++) {
            Name path = pathSegmentSymbols.get(i).name;
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            // load resource path name
            mv.visitLdcInsn(path.value);
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKESPECIAL, RESOURCE_METHOD_TYPE_IMPL, JVM_INIT_METHOD, RESOURCE_METHOD_TYPE_IMPL_INIT,
                false);
    }

    private void addObjectFields(ClassWriter cw, MethodVisitor mv, String objectTypesClass,
                                 BObjectType bType) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        if (!bType.fields.isEmpty()) {
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESTATIC, objectTypesClass, "addFields", SET_LINKED_HASH_MAP, false);
            jvmCreateTypeGen.splitAddFields(cw, bType, objectTypesClass);
        }
        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setFields", SET_MAP, false);
    }
}
