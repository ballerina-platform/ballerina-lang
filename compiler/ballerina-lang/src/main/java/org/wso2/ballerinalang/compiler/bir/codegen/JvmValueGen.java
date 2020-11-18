/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.NameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JFieldFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JMethodFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.OldStyleExternalFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.IdentifierUtils.decodeIdentifier;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.cleanupReadOnlyTypeName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ABSTRACT_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_OPTIONAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_RUNTIME_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_MAPPING_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.COLLECTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_TO_STRING_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_SIMPLE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.POPULATE_INITIAL_VALUES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE_IMPL_CLOSURES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNSUPPORTED_OPERATION_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.enrichWithDefaultableParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.computeLockNameFromString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.desugarOldExternFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.lookupBIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.desugarInteropFuncs;

/**
 * BIR values to JVM byte code generation class.
 *
 * @since 1.2.0
 */
class JvmValueGen {

    static final NameHashComparator NAME_HASH_COMPARATOR = new NameHashComparator();
    private BIRNode.BIRPackage module;
    private JvmPackageGen jvmPackageGen;
    private JvmMethodGen jvmMethodGen;
    private BType booleanType;

    JvmValueGen(BIRNode.BIRPackage module, JvmPackageGen jvmPackageGen, JvmMethodGen jvmMethodGen) {

        this.module = module;
        this.jvmPackageGen = jvmPackageGen;
        this.jvmMethodGen = jvmMethodGen;
        this.booleanType = jvmPackageGen.symbolTable.booleanType;
    }

    private static BIRNode.BIRFunction getFunction(BIRNode.BIRFunction func) {

        if (func == null) {
            throw new BLangCompilerException("Invalid function");
        }

        return func;
    }

    static void injectDefaultParamInitsToAttachedFuncs(BIRNode.BIRPackage module, JvmMethodGen jvmMethodGen,
                                                       JvmPackageGen jvmPackageGen) {

        List<BIRNode.BIRTypeDefinition> typeDefs = module.typeDefs;
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;
            if (bType instanceof BServiceType || (bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(
                    bType.tsymbol.flags, Flags.CLASS)) || bType.tag == TypeTags.RECORD) {
                desugarObjectMethods(module, bType, optionalTypeDef.attachedFuncs, jvmMethodGen, jvmPackageGen);
            }
        }
    }

    private static void desugarObjectMethods(BIRNode.BIRPackage module, BType bType,
                                             List<BIRNode.BIRFunction> attachedFuncs, JvmMethodGen jvmMethodGen,
                                             JvmPackageGen jvmPackageGen) {

        if (attachedFuncs == null) {
            return;
        }
        for (BIRNode.BIRFunction birFunc : attachedFuncs) {
            if (birFunc == null) {
                continue;
            }
            if (JvmCodeGenUtil.isExternFunc(birFunc)) {
                BIRFunctionWrapper extFuncWrapper = lookupBIRFunctionWrapper(module, birFunc, bType, jvmPackageGen);
                if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                    desugarOldExternFuncs((OldStyleExternalFunctionWrapper) extFuncWrapper, birFunc, jvmMethodGen);
                } else if (extFuncWrapper instanceof JMethodFunctionWrapper) {
                    desugarInteropFuncs((JMethodFunctionWrapper) extFuncWrapper, birFunc, jvmMethodGen);
                    enrichWithDefaultableParamInits(birFunc, jvmMethodGen);
                } else if (!(extFuncWrapper instanceof JFieldFunctionWrapper)) {
                    enrichWithDefaultableParamInits(birFunc, jvmMethodGen);
                }
            } else {
                addDefaultableBooleanVarsToSignature(birFunc, jvmPackageGen.symbolTable.booleanType);
            }
            enrichWithDefaultableParamInits(birFunc, jvmMethodGen);
        }
    }

    static List<Label> createLabelsForSwitch(MethodVisitor mv, int nameRegIndex,
                                             List<? extends NamedNode> nodes, Label defaultCaseLabel) {

        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

        // Create labels for the cases
        int i = 0;
        List<Label> labels = new ArrayList<>();
        int[] hashCodes = new int[nodes.size()];
        for (NamedNode node : nodes) {
            if (node != null) {
                labels.add(i, new Label());
                hashCodes[i] = decodeIdentifier(getName(node)).hashCode();
                i += 1;
            }
        }
        mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels.toArray(new Label[0]));
        return labels;
    }

    static void createDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex) {

        mv.visitLabel(defaultCaseLabel);
        mv.visitTypeInsn(NEW, BLANG_RUNTIME_EXCEPTION);
        mv.visitInsn(DUP);

        // Create error message
        mv.visitTypeInsn(NEW, STRING_BUILDER);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("No such field or method: ");
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER, JVM_INIT_METHOD, String.format("(L%s;)V", STRING_VALUE),
                false);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "append",
                String.format("(L%s;)L%s;", STRING_VALUE, STRING_BUILDER), false);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, JVM_TO_STRING_METHOD,
                String.format("()L%s;", STRING_VALUE), false);
        mv.visitMethodInsn(INVOKESPECIAL, BLANG_RUNTIME_EXCEPTION, JVM_INIT_METHOD,
                String.format("(L%s;)V", STRING_VALUE), false);
        mv.visitInsn(ATHROW);
    }

    static String getTypeDescClassName(Object module, String typeName) {

        String packageName = calculateJavaPkgName(module);
        return packageName + TYPEDESC_CLASS_PREFIX + cleanupReadOnlyTypeName(typeName);
    }

    static String getTypeValueClassName(Object module, String typeName) {

        String packageName = calculateJavaPkgName(module);
        return packageName + VALUE_CLASS_PREFIX + cleanupReadOnlyTypeName(typeName);
    }

    private static String calculateJavaPkgName(Object module) {

        String packageName;
        if (module instanceof BIRNode.BIRPackage) {
            BIRNode.BIRPackage birPackage = (BIRNode.BIRPackage) module;
            packageName = JvmCodeGenUtil.getPackageName(birPackage);
        } else if (module instanceof PackageID) {
            PackageID packageID = (PackageID) module;
            packageName = JvmCodeGenUtil.getPackageName(packageID);
        } else {
            throw new ClassCastException("module should be PackageID or BIRPackage but is : "
                    + (module == null ? "null" : module.getClass()));
        }
        return packageName;
    }

    static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
                                                 List<? extends NamedNode> nodes,
                                                 List<Label> labels, Label defaultCaseLabel) {

        List<Label> targetLabels = new ArrayList<>();
        int i = 0;
        for (NamedNode node : nodes) {
            if (node == null) {
                continue;
            }
            mv.visitLabel(labels.get(i));
            mv.visitVarInsn(ALOAD, nameRegIndex);
            mv.visitLdcInsn(decodeIdentifier(getName(node)));
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    String.format("(L%s;)Z", OBJECT), false);
            Label targetLabel = new Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels.add(i, targetLabel);
            i += 1;
        }

        return targetLabels;
    }

    private static String getName(Object node) {

        if (node instanceof NamedNode) {
            return ((NamedNode) node).getName().value;
        }

        throw new BLangCompilerException(String.format("Invalid node: %s", node));
    }

    private void createLambdas(ClassWriter cw, AsyncDataCollector asyncDataCollector) {

        for (Map.Entry<String, BIRInstruction> entry : asyncDataCollector.getLambdas().entrySet()) {
            jvmMethodGen.generateLambdaMethod(entry.getValue(), cw, entry.getKey());
        }
    }

    private void createObjectFields(ClassWriter cw, Map<String, BField> fields) {

        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }
            FieldVisitor fvb = cw.visitField(0, field.name.value, getTypeDesc(field.type), null, null);
            fvb.visitEnd();
            String lockClass = "L" + LOCK_VALUE + ";";
            FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL, computeLockNameFromString(field.name.value),
                                            lockClass, null, null);
            fv.visitEnd();
        }
    }

    private void createObjectMethods(ClassWriter cw, List<BIRNode.BIRFunction> attachedFuncs,
                                     String moduleClassName, BObjectType currentObjectType,
                                     AsyncDataCollector asyncDataCollector) {

        for (BIRNode.BIRFunction func : attachedFuncs) {
            if (func == null) {
                continue;
            }
            jvmMethodGen.generateMethod(func, cw, module, currentObjectType, moduleClassName,
                                        asyncDataCollector);
        }
    }

    private void createObjectInit(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, String.format("(L%s;)V", OBJECT_TYPE_IMPL), null,
                                          null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke super(type);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_OBJECT_VALUE, JVM_INIT_METHOD, String.format("(L%s;)V",
                                                                                                OBJECT_TYPE_IMPL),
                false);

        String lockClass = "L" + LOCK_VALUE + ";";
        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }

            Label fLabel = new Label();
            mv.visitLabel(fLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, LOCK_VALUE);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, LOCK_VALUE, JVM_INIT_METHOD, "()V", false);
            mv.visitFieldInsn(PUTFIELD, className, computeLockNameFromString(field.name.value), lockClass);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    private void createCallMethod(ClassWriter cw, List<BIRNode.BIRFunction> functions, String objClassName) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call", String.format(
                "(L%s;L%s;[L%s;)L%s;", STRAND_CLASS, STRING_VALUE, OBJECT, OBJECT), null, null);
        mv.visitCode();

        int funcNameRegIndex = 2;

        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        functions.sort(NAME_HASH_COMPARATOR);

        List<Label> labels = createLabelsForSwitch(mv, funcNameRegIndex, functions, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, funcNameRegIndex, functions, labels,
                                                             defaultCaseLabel);

        // case body
        int i = 0;
        for (BIRNode.BIRFunction optionalFunc : functions) {
            BIRNode.BIRFunction func = getFunction(optionalFunc);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            List<BType> paramTypes = func.type.paramTypes;
            BType retType = func.type.retType;

            String methodSig;

            // use index access, since retType can be nil.
            methodSig = JvmCodeGenUtil.getMethodDesc(paramTypes, retType);

            // load self
            mv.visitVarInsn(ALOAD, 0);

            // load strand
            mv.visitVarInsn(ALOAD, 1);
            int j = 0;
            for (BType paramType : paramTypes) {
                // load parameters
                mv.visitVarInsn(ALOAD, 3);

                // load j'th parameter
                mv.visitLdcInsn((long) j);
                mv.visitInsn(L2I);
                mv.visitInsn(AALOAD);
                JvmCastGen.addUnboxInsn(mv, paramType);
                j += 1;
            }

            mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, JvmCodeGenUtil.cleanupFunctionName(func.name.value),
                               methodSig, false);
            if (retType == null || retType.tag == TypeTags.NIL || retType.tag == TypeTags.NEVER) {
                mv.visitInsn(ACONST_NULL);
            } else {
                JvmCastGen.addBoxInsn(mv, retType);
                if (io.ballerina.runtime.util.Flags.isFlagOn(func.flags, io.ballerina.runtime.util.Flags.RESOURCE)) {
                    mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, "handleResourceError",
                                       String.format("(L%s;)L%s;", OBJECT, OBJECT), false);
                }
            }
            mv.visitInsn(ARETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex);
        mv.visitMaxs(functions.size() + 10, functions.size() + 10);
        mv.visitEnd();
    }

    private void createObjectGetMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        String signature = String.format("(L%s;)L%s;", B_STRING_VALUE, OBJECT);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", signature, null, null);
        mv.visitCode();

        int fieldNameRegIndex = 1;
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD,
                           String.format("()L%s;", STRING_VALUE), true);
        fieldNameRegIndex = 2;
        mv.visitVarInsn(ASTORE, fieldNameRegIndex);
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(NAME_HASH_COMPARATOR);

        List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, sortedFields, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, sortedFields, labels,
                defaultCaseLabel);

        int i = 0;
        for (BField optionalField : sortedFields) {
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, optionalField.name.value, getTypeDesc(optionalField.type));
            JvmCastGen.addBoxInsn(mv, optionalField.type);
            mv.visitInsn(ARETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createObjectSetMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        createObjectSetMethod(cw, fields, className, "set", "checkFieldUpdate");
    }

    private void createObjectSetOnInitializationMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        createObjectSetMethod(cw, fields, className, "setOnInitialization", "checkFieldUpdateOnInitialization");
    }

    private void createObjectSetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                       String setFuncName, String checkFieldUpdateFuncName) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, setFuncName,
                                          String.format("(L%s;L%s;)V", B_STRING_VALUE, OBJECT), null, null);
        mv.visitCode();
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        Label defaultCaseLabel = new Label();

        // code gen type checking for inserted value
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, String.format("()L%s;", STRING_VALUE),
                           true);
        mv.visitInsn(DUP);
        fieldNameRegIndex = 3;
        mv.visitVarInsn(ASTORE, fieldNameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, checkFieldUpdateFuncName,
                           String.format("(L%s;L%s;)V", STRING_VALUE, OBJECT), false);

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(NAME_HASH_COMPARATOR);

        List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, sortedFields, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, sortedFields, labels,
                                                             defaultCaseLabel);

        // case body
        int i = 0;
        for (BField optionalField : sortedFields) {
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, valueRegIndex);
            JvmCastGen.addUnboxInsn(mv, optionalField.type);
            String filedName = optionalField.name.value;
            mv.visitFieldInsn(PUTFIELD, className, filedName, getTypeDesc(optionalField.type));
            mv.visitInsn(RETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private byte[] createRecordTypeDescClass(BRecordType recordType, String className,
                                             BIRNode.BIRTypeDefinition typeDef) {

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        if (typeDef.pos != null && typeDef.pos.src != null) {
            cw.visitSource(typeDef.pos.getSource().cUnitName, null);
        } else {
            cw.visitSource(className, null);
        }
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, TYPEDESC_VALUE_IMPL, new String[]{TYPEDESC_VALUE});

        this.createTypeDescConstructor(cw);
        this.createInstantiateMethod(cw, recordType, typeDef);

        cw.visitEnd();

        return jvmPackageGen.getBytes(cw, typeDef);
    }

    private void createInstantiateMethod(ClassWriter cw, BRecordType recordType,
                                         BIRNode.BIRTypeDefinition typeDef) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "instantiate",
                                          String.format("(L%s;[L%s;)L%s;", STRAND_CLASS, B_INITIAL_VALUE_ENTRY, OBJECT),
                                          null, null);
        mv.visitCode();

        String className = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", TYPEDESC_VALUE), false);

        BAttachedFunction initializer = ((BRecordTypeSymbol) recordType.tsymbol).initializerFunc;
        StringBuilder closureParamSignature = calcClosureMapSignature(initializer.type.paramTypes.size());

        // Invoke the init-function of this type.
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(SWAP);


        // Invoke the init-functions of referenced types. This is done to initialize the
        // defualt values of the fields coming from the referenced types.
        for (BType typeRef : typeDef.referencedTypes) {
            if (typeRef.tag == TypeTags.RECORD) {
                String refTypeClassName = getTypeValueClassName(typeRef.tsymbol.pkgID, toNameString(typeRef));
                mv.visitInsn(DUP2);
                mv.visitMethodInsn(INVOKESTATIC, refTypeClassName, JvmConstants.RECORD_INIT_WRAPPER_NAME,
                                   String.format("(L%s;L%s;)V", STRAND_CLASS, MAP_VALUE), false);
            }
        }


        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, TYPEDESC_VALUE_IMPL, TYPEDESC_VALUE_IMPL_CLOSURES,
                          String.format("[L%s;", MAP_VALUE));

        for (int i = 0; i < initializer.type.paramTypes.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            mv.visitInsn(AALOAD);
            mv.visitInsn(SWAP);

            mv.visitInsn(ICONST_1);
            mv.visitInsn(SWAP);
        }
        mv.visitInsn(POP);


        // Invoke the init-function of this type.
        String initFuncName;
        String valueClassName;
        List<BIRFunction> attachedFuncs = typeDef.attachedFuncs;

        // Attached functions are empty for type-labeling. In such cases, call the init() of
        // the original type value;
        if (attachedFuncs.size() != 0) {
            initFuncName = attachedFuncs.get(0).name.value;
            valueClassName = className;
        } else {
            // record type is the original record-type of this type-label
            valueClassName = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
            initFuncName = JvmCodeGenUtil.cleanupFunctionName(recordType.name + "__init_");
        }

        mv.visitMethodInsn(INVOKESTATIC, valueClassName, initFuncName,
                           String.format("(L%s;L%s;%s)L%s;", STRAND_CLASS, MAP_VALUE, closureParamSignature, OBJECT),
                           false);

        mv.visitInsn(POP);

        mv.visitInsn(DUP);
        mv.visitTypeInsn(CHECKCAST, valueClassName);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, String.format("[L%s;", B_MAPPING_INITIAL_VALUE_ENTRY));
        mv.visitMethodInsn(INVOKEVIRTUAL, valueClassName, POPULATE_INITIAL_VALUES_METHOD,
                           String.format("([L%s;)V", B_MAPPING_INITIAL_VALUE_ENTRY), false);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private StringBuilder calcClosureMapSignature(int size) {
        StringBuilder closureParamSignature = new StringBuilder();
        for (int i = 0; i < size; i++) {
            closureParamSignature.append('L');
            closureParamSignature.append(MAP_VALUE);
            closureParamSignature.append(";Z");
        }
        return closureParamSignature;
    }

    private byte[] createRecordValueClass(BRecordType recordType, String className,
                                          BIRNode.BIRTypeDefinition typeDef) {

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        if (typeDef.pos != null && typeDef.pos.src != null) {
            cw.visitSource(typeDef.pos.getSource().cUnitName, null);
        } else {
            cw.visitSource(className, null);
        }
        AsyncDataCollector asyncDataCollector = new AsyncDataCollector(className);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className,
                String.format("<K:L%s;V:L%s;>L%s<TK;TV;>;L%s<TK;TV;>;", OBJECT, OBJECT, MAP_VALUE_IMPL, MAP_VALUE),
                MAP_VALUE_IMPL, new String[]{MAP_VALUE});

        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs != null) {
            this.createRecordMethods(cw, attachedFuncs, className, asyncDataCollector);
        }

        Map<String, BField> fields = recordType.fields;
        this.createRecordFields(cw, fields);
        this.createRecordGetMethod(cw, fields, className);
        this.createRecordSetMethod(cw, fields, className);
        this.createRecordEntrySetMethod(cw, fields, className);
        this.createRecordContainsKeyMethod(cw, fields, className);
        this.createRecordGetValuesMethod(cw, fields, className);
        this.createGetSizeMethod(cw, fields, className);
        this.createRecordRemoveMethod(cw);
        this.createRecordClearMethod(cw, fields, className);
        this.createRecordGetKeysMethod(cw, fields, className);
        this.createRecordPopulateInitialValuesMethod(cw);

        this.createRecordConstructor(cw, TYPEDESC_VALUE);
        this.createRecordConstructor(cw, TYPE);
        this.createRecordInitWrapper(cw, className, typeDef);
        this.createLambdas(cw, asyncDataCollector);
        JvmCodeGenUtil.visitStrandMetadataField(cw, asyncDataCollector);
        this.generateStaticInitializer(cw, className, module, asyncDataCollector);
        cw.visitEnd();

        return jvmPackageGen.getBytes(cw, typeDef);
    }

    private void createRecordMethods(ClassWriter cw, List<BIRNode.BIRFunction> attachedFuncs, String moduleClassName,
                                     AsyncDataCollector asyncDataCollector) {

        for (BIRNode.BIRFunction func : attachedFuncs) {
            if (func == null) {
                continue;
            }
            jvmMethodGen.generateMethod(func, cw, this.module, null, moduleClassName, asyncDataCollector);
        }
    }

    private void createTypeDescConstructor(ClassWriter cw) {

        String descriptor = String.format("(L%s;[L%s;)V", TYPE, MAP_VALUE);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, descriptor, null, null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);

        mv.visitVarInsn(ALOAD, 2);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_VALUE_IMPL, JVM_INIT_METHOD, descriptor, false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordConstructor(ClassWriter cw, String argumentClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, String.format("(L%s;)V", argumentClass),
                null, null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, JVM_INIT_METHOD, String.format("(L%s;)V", argumentClass),
                false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateStaticInitializer(ClassWriter cw, String moduleClass, BIRNode.BIRPackage module,
                                                  AsyncDataCollector asyncDataCollector) {

        if (asyncDataCollector.getStrandMetadata().isEmpty()) {
            return;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        JvmCodeGenUtil.generateStrandMetadata(mv, moduleClass, module, asyncDataCollector);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    // TODO: remove this method, logic moved to createInstantiateMethod, see #23012
    private void createRecordInitWrapper(ClassWriter cw, String className, BIRNode.BIRTypeDefinition typeDef) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, JvmConstants.RECORD_INIT_WRAPPER_NAME,
                                          String.format("(L%s;L%s;)V", STRAND_CLASS, MAP_VALUE), null, null);
        mv.visitCode();
        // load strand
        mv.visitVarInsn(ALOAD, 0);
        // load value
        mv.visitVarInsn(ALOAD, 1);

        // Invoke the init-functions of referenced types. This is done to initialize the
        // defualt values of the fields coming from the referenced types.
        for (BType typeRef : typeDef.referencedTypes) {
            if (typeRef.tag != TypeTags.RECORD) {
                continue;
            }

            String refTypeClassName = getTypeValueClassName(typeRef.tsymbol.pkgID,
                                                            toNameString(typeRef));
            mv.visitInsn(DUP2);
            mv.visitMethodInsn(INVOKESTATIC, refTypeClassName, JvmConstants.RECORD_INIT_WRAPPER_NAME,
                               String.format("(L%s;L%s;)V", STRAND_CLASS, MAP_VALUE), false);
        }

        // Invoke the init-function of this type.
        String initFuncName;
        String valueClassName;
        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;

        // Attached functions are empty for type-labeling. In such cases, call the init() of the original type value
        if (!attachedFuncs.isEmpty()) {
            initFuncName = attachedFuncs.get(0).name.value;
            valueClassName = className;
        } else {
            // record type is the original record-type of this type-label
            BRecordType recordType = (BRecordType) typeDef.type;
            valueClassName = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
            initFuncName = JvmCodeGenUtil.cleanupFunctionName(recordType.name + "__init_");
        }

        mv.visitMethodInsn(INVOKESTATIC, valueClassName, initFuncName,
                           String.format("(L%s;L%s;)L%s;", STRAND_CLASS, MAP_VALUE, OBJECT), false);
        mv.visitInsn(POP);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordFields(ClassWriter cw, Map<String, BField> fields) {
        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }
            String fieldName = field.name.value;
            FieldVisitor fv = cw.visitField(0, fieldName, getTypeDesc(field.type), null, null);
            fv.visitEnd();

            if (this.isOptionalRecordField(field)) {
                fv = cw.visitField(0, this.getFieldIsPresentFlagName(fieldName), getTypeDesc(booleanType),
                        null, null);
                fv.visitEnd();
            }
        }
    }

    private String getFieldIsPresentFlagName(String fieldName) {

        return String.format("%s$isPresent", fieldName);
    }

    private boolean isOptionalRecordField(BField field) {

        return (field.symbol.flags & BAL_OPTIONAL) == BAL_OPTIONAL;
    }

    private void createRecordGetMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                                              String.format("(L%s;)L%s;", OBJECT, OBJECT),
                                              String.format("(L%s;)TV;", OBJECT), null);
        mv.visitCode();

        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;
        Label defaultCaseLabel = new Label();

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
            mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
            mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, String.format("()L%s;", STRING_VALUE),
                               true);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                                                                 defaultCaseLabel);

        int i = 0;
        for (BField optionalField : sortedFields) {
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            // if the field is an optional-field, first check the 'isPresent' flag of that field.
            Label ifPresentLabel = new Label();
            String fieldName = optionalField.name.value;
            if (this.isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
                mv.visitJumpInsn(IFNE, ifPresentLabel);
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
            }

            mv.visitLabel(ifPresentLabel);
            // return the value of the field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            JvmCastGen.addBoxInsn(mv, optionalField.type);
            mv.visitInsn(ARETURN);
            i += 1;
        }

        this.createRecordGetDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordSetMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "putValue",
                                              String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), "(TK;TV;)TV;",
                                              null);

        mv.visitCode();
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        int strKeyVarIndex = 3;
        Label defaultCaseLabel = new Label();

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, String.format("()L%s;", STRING_VALUE),
                           true);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                                                                 defaultCaseLabel);

        // case body
        int i = 0;
        for (BField optionalField : sortedFields) {
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            // load the existing value to return
            String fieldName = optionalField.name.value;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            JvmCastGen.addBoxInsn(mv, optionalField.type);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, valueRegIndex);
            JvmCastGen.addUnboxInsn(mv, optionalField.type);
            mv.visitFieldInsn(PUTFIELD, className, fieldName, getTypeDesc(optionalField.type));

            // if the field is an optional-field, then also set the isPresent flag of that field to true.
            if (this.isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ICONST_1);
                mv.visitFieldInsn(PUTFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
            }

            mv.visitInsn(ARETURN);
            i += 1;
        }

        this.createRecordPutDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, valueRegIndex);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordPutDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex,
                                            int valueRegIndex) {

        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "putValue",
                String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), false);
        mv.visitInsn(ARETURN);
    }

    private void createRecordGetDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex) {

        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "get", String.format("(L%s;)L%s;", OBJECT, OBJECT),
                false);
        mv.visitInsn(ARETURN);
    }

    private void createRecordEntrySetMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "entrySet",
                                          String.format("()L%s;", SET),
                                          String.format("()L%s<L%s<TK;TV;>;>;", SET, MAP_ENTRY),
                                          null);
        mv.visitCode();

        int entrySetVarIndex = 1;
        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, entrySetVarIndex);

        for (BField optionalField : fields.values()) {
            Label ifNotPresent = new Label();

            // If its an optional field, generate if-condition to check the presense of the field.
            String fieldName = optionalField.name.value;
            if (this.isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className,
                        this.getFieldIsPresentFlagName(decodeIdentifier(fieldName)),
                        getTypeDesc(booleanType));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, entrySetVarIndex);
            mv.visitTypeInsn(NEW, MAP_SIMPLE_ENTRY);
            mv.visitInsn(DUP);

            // field name as key
            mv.visitLdcInsn(decodeIdentifier(fieldName));
            mv.visitMethodInsn(INVOKESTATIC, JvmConstants.STRING_UTILS, "fromString",
                               String.format("(L%s;)L%s;", STRING_VALUE, B_STRING_VALUE), false);

            // field value as the map-entry value
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            JvmCastGen.addBoxInsn(mv, optionalField.type);

            mv.visitMethodInsn(INVOKESPECIAL, MAP_SIMPLE_ENTRY, JVM_INIT_METHOD,
                    String.format("(L%s;L%s;)V", OBJECT, OBJECT), false);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);

            mv.visitLabel(ifNotPresent);
        }

        // Add all from super.enrtySet() to the current entry set.
        mv.visitVarInsn(ALOAD, entrySetVarIndex);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "entrySet", String.format("()L%s;", SET), false);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", String.format("(L%s;)Z", COLLECTION), true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, entrySetVarIndex);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    private void createRecordContainsKeyMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "containsKey", String.format("(L%s;)Z", OBJECT), null, null);
        mv.visitCode();

        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, String.format("()L%s;", STRING_VALUE),
                           true);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(NAME_HASH_COMPARATOR);

        Label defaultCaseLabel = new Label();
        List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                defaultCaseLabel);

        int i = 0;
        for (BField optionalField : sortedFields) {
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            String fieldName = optionalField.name.value;
            if (this.isOptionalRecordField(optionalField)) {
                // if the field is optional, then return the value is the 'isPresent' flag.
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
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
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "containsKey", String.format("(L%s;)Z", OBJECT), false);
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordGetValuesMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "values", String.format("()L%s;", COLLECTION),
                                          String.format("()L%s<TV;>;", COLLECTION), null);
        mv.visitCode();

        int valuesVarIndex = 1;
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, valuesVarIndex);

        for (BField optionalField : fields.values()) {
            Label ifNotPresent = new Label();

            // If its an optional field, generate if-condition to check the presense of the field.
            String fieldName = optionalField.name.value;
            if (this.isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                                  getTypeDesc(booleanType));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, valuesVarIndex);
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            JvmCastGen.addBoxInsn(mv, optionalField.type);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
            mv.visitLabel(ifNotPresent);
        }

        mv.visitVarInsn(ALOAD, valuesVarIndex);
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "values", String.format("()L%s;", COLLECTION), false);
        mv.visitMethodInsn(INVOKEINTERFACE, LIST, "addAll", String.format("(L%s;)Z", COLLECTION), true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createGetSizeMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "size", "()I", null, null);
        mv.visitCode();
        int sizeVarIndex = 1;

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "size", "()I", false);
        mv.visitVarInsn(ISTORE, sizeVarIndex);

        int requiredFieldsCount = 0;
        for (BField optionalField : fields.values()) {
            String fieldName = optionalField.name.value;
            if (this.isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                                  getTypeDesc(booleanType));
                Label l3 = new Label();
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

    private void createRecordRemoveMethod(ClassWriter cw) {
        // throw an UnsupportedOperationException, since remove is not supported by for records.
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "clear", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordClearMethod(ClassWriter cw, Map<String, BField> fields, String className) {
        // throw an UnsupportedOperationException, since remove is not supported by for records.
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "remove", String.format("(L%s;)L%s;", OBJECT, OBJECT),
                                          String.format("(L%s;)TV;", OBJECT), null);
        mv.visitCode();

        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, String.format("()L%s;", STRING_VALUE),
                           true);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "validateFreezeStatus", "()V", false);

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(NAME_HASH_COMPARATOR);

        Label defaultCaseLabel = new Label();
        List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                defaultCaseLabel);

        int i = 0;
        for (BField optionalField : sortedFields) {
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            //Setting isPresent as zero
            if (this.isOptionalRecordField(optionalField)) {
                String fieldName = optionalField.name.value;
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ICONST_0);
                mv.visitFieldInsn(PUTFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));

                // load the existing value to return
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
                JvmCastGen.addBoxInsn(mv, optionalField.type);

                // Set default value for reference types
                if (checkIfValueIsJReferenceType(optionalField.type)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitFieldInsn(PUTFIELD, className, fieldName, getTypeDesc(optionalField.type));
                }

                mv.visitInsn(ARETURN);
            } else {
                mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, JVM_INIT_METHOD, "()V", false);
                mv.visitInsn(ATHROW);
            }
            i += 1;
        }

        // default case
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "remove",
                String.format("(L%s;)L%s;", OBJECT, OBJECT), false);
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private boolean checkIfValueIsJReferenceType(BType bType) {

        switch (bType.getKind()) {
            case INT:
            case BOOLEAN:
            case FLOAT:
            case BYTE:
                return false;
            default:
                return true;
        }
    }

    private void createRecordGetKeysMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getKeys", String.format("()[L%s;", OBJECT), "()[TK;", null);
        mv.visitCode();

        int keysVarIndex = 1;
        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, keysVarIndex);

        for (BField optionalField : fields.values()) {
            Label ifNotPresent = new Label();

            // If its an optional field, generate if-condition to check the presense of the field.
            String fieldName = decodeIdentifier(optionalField.name.value);
            if (this.isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                                  getTypeDesc(booleanType));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, keysVarIndex);
            mv.visitLdcInsn(fieldName);
                mv.visitMethodInsn(INVOKESTATIC, JvmConstants.STRING_UTILS, "fromString",
                                   String.format("(L%s;)L%s;", STRING_VALUE, B_STRING_VALUE), false);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
            mv.visitLabel(ifNotPresent);
        }

        mv.visitVarInsn(ALOAD, keysVarIndex);
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "keySet", String.format("()L%s;", SET), false);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", String.format("(L%s;)Z", COLLECTION), true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, keysVarIndex);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "size", "()I", true);
            mv.visitTypeInsn(ANEWARRAY, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "toArray", String.format("([L%s;)[L%s;", OBJECT, OBJECT), true);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordPopulateInitialValuesMethod(ClassWriter cw) {

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, POPULATE_INITIAL_VALUES_METHOD,
                                          String.format("([L%s;)V", B_MAPPING_INITIAL_VALUE_ENTRY), null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, POPULATE_INITIAL_VALUES_METHOD,
                           String.format("([L%s;)V", B_MAPPING_INITIAL_VALUE_ENTRY), false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateValueClasses(Map<String, byte[]> jarEntries) {

        module.typeDefs.parallelStream().forEach(optionalTypeDef -> {
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(bType.tsymbol.flags, Flags.CLASS)) {
                BObjectType objectType = (BObjectType) bType;
                String className = getTypeValueClassName(this.module, optionalTypeDef.name.value);
                byte[] bytes = this.createObjectValueClass(objectType, className, optionalTypeDef);
                jarEntries.put(className + ".class", bytes);
            } else if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                String className = getTypeValueClassName(this.module, optionalTypeDef.name.value);
                byte[] bytes = this.createRecordValueClass(recordType, className, optionalTypeDef);
                jarEntries.put(className + ".class", bytes);

                String typedescClass = getTypeDescClassName(this.module, optionalTypeDef.name.value);
                bytes = this.createRecordTypeDescClass(recordType, typedescClass, optionalTypeDef);
                jarEntries.put(typedescClass + ".class", bytes);
            }
        });
    }

    private byte[] createObjectValueClass(BObjectType objectType, String className, BIRNode.BIRTypeDefinition typeDef) {

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visitSource(typeDef.pos.getSource().cUnitName, null);

        AsyncDataCollector asyncDataCollector = new AsyncDataCollector(className);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, ABSTRACT_OBJECT_VALUE, new String[]{B_OBJECT});

        Map<String, BField> fields = objectType.fields;
        this.createObjectFields(cw, fields);

        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs != null) {
            this.createObjectMethods(cw, attachedFuncs, className, objectType,
                                     asyncDataCollector);
        }

        this.createObjectInit(cw, fields, className);
        this.createCallMethod(cw, attachedFuncs, className);
        this.createObjectGetMethod(cw, fields, className);
        this.createObjectSetMethod(cw, fields, className);
        this.createObjectSetOnInitializationMethod(cw, fields, className);
        this.createLambdas(cw, asyncDataCollector);
        JvmCodeGenUtil.visitStrandMetadataField(cw, asyncDataCollector);
        this.generateStaticInitializer(cw, className, module, asyncDataCollector);

        cw.visitEnd();
        return jvmPackageGen.getBytes(cw, typeDef);
    }
}
