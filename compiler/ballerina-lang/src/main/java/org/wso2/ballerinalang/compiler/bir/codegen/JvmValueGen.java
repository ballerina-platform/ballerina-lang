/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
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
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ABSTRACT_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_OPTIONAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_RUNTIME_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.COLLECTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_SIMPLE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNSUPPORTED_OPERATION_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.enrichWithDefaultableParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.IS_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.I_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addBoxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addUnboxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupFunctionName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupTypeName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateLambdaMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunction;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunctions;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getMethodDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getObjectField;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getRecordField;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getTypeDef;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nameOfBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.computeLockNameFromString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.currentClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.OldStyleExternalFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.desugarOldExternFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.lookupBIRFunctionWrapper;

/**
 * BIR values to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmValueGen {

    public static final NameHashComparator NAME_HASH_COMPARATOR = new NameHashComparator();

    static void injectDefaultParamInitsToAttachedFuncs(BIRPackage module) {
        @Nilable List<BIRNode.BIRTypeDefinition> typeDefs = module.typeDefs;
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRNode.BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;
            if (bType.tag == TypeTags.OBJECT &&
                    !Symbols.isFlagOn(((BObjectType) bType).tsymbol.flags, Flags.ABSTRACT)) {
                desugarObjectMethods(module, bType, typeDef.attachedFuncs);
            } else if (bType instanceof BServiceType) {
                desugarObjectMethods(module, bType, typeDef.attachedFuncs);
            } else if (bType.tag == TypeTags.RECORD) {
                desugarObjectMethods(module, bType, typeDef.attachedFuncs);
            }
        }
    }

    private static void desugarObjectMethods(BIRPackage module, BType bType, @Nilable List<BIRFunction> attachedFuncs) {
        if (attachedFuncs != null) {
            for (BIRFunction func : attachedFuncs) {
                if (func != null) {
                    if (isExternFunc(func)) {
                        JvmPackageGen.BIRFunctionWrapper extFuncWrapper = lookupBIRFunctionWrapper(module, func, bType);
                        if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                            // Note when this support new interop, update here as well TODO
                            desugarOldExternFuncs(module, (OldStyleExternalFunctionWrapper) extFuncWrapper, func);
                        }
                    } else {
                        addDefaultableBooleanVarsToSignature(func);
                    }
                    enrichWithDefaultableParamInits(getFunction(func));
                }
            }
        }
    }

    static List<Label> createLabelsForSwitch(MethodVisitor mv, int nameRegIndex, @Nilable List<? extends NamedNode> nodes,
                                             Label defaultCaseLabel) {
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

        // Create labels for the cases
        int i = 0;
        List<Label> labels = new ArrayList<>();
        int[] hashCodes = new int[nodes.size()];
        for (NamedNode node : nodes) {
            if (node != null) {
                labels.add(i, new Label());
                hashCodes[i] = getName(node).hashCode();
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
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER, "<init>", String.format("(L%s;)V", STRING_VALUE), false);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "append",
                String.format("(L%s;)L%s;", STRING_VALUE, STRING_BUILDER), false);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "toString", String.format("()L%s;", STRING_VALUE), false);

        mv.visitMethodInsn(INVOKESPECIAL, BLANG_RUNTIME_EXCEPTION, "<init>",
                String.format("(L%s;)V", STRING_VALUE), false);
        mv.visitInsn(ATHROW);
    }

    static String getTypeValueClassName(Object module, String typeName) {
        String packageName;
        if (module instanceof BIRPackage) {
            BIRPackage birPackage = (BIRPackage) module;
            packageName = getPackageName(birPackage.org.value, birPackage.name.value);
        } else if (module instanceof PackageID) {
            PackageID packageID = (PackageID) module;
            packageName = getPackageName(packageID.orgName, packageID.name);
        } else {
            throw new ClassCastException("module should be PackageID or BIRPackage but is : "
                    + (module == null ? "null" : module.getClass()));
        }

        return packageName + "$value$" + cleanupTypeName(typeName);
    }

    static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
                                                 @Nilable List<? extends NamedNode> nodes,
                                                 List<Label> labels, Label defaultCaseLabel) {
        List<Label> targetLabels = new ArrayList<>();
        int i = 0;
        for (NamedNode node : nodes) {
            if (node != null) {
                mv.visitLabel(labels.get(i));
                mv.visitVarInsn(ALOAD, nameRegIndex);
                mv.visitLdcInsn(getName(node));
                mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                        String.format("(L%s;)Z", OBJECT), false);
                Label targetLabel = new Label();
                mv.visitJumpInsn(IFNE, targetLabel);
                mv.visitJumpInsn(GOTO, defaultCaseLabel);
                targetLabels.add(i, targetLabel);
                i += 1;
            }
        }

        return targetLabels;
    }

    private static String getName(Object node) {
        if (node instanceof NamedNode) {
            return ((NamedNode) node).getName().value;
        } else {
            throw new BLangCompilerException(String.format("Invalid node: %s", node));
        }
    }

    static class ObjectGenerator {

        private BIRPackage module;
        private @Nilable
        BObjectType currentObjectType = null;
        private @Nilable
        BRecordType currentRecordType = null;

        private void createLambdas(ClassWriter cw) {
            for (Map.Entry<String, BIRInstruction> entry : JvmPackageGen.lambdas.entrySet()) {
                generateLambdaMethod(entry.getValue(), cw, entry.getKey());
            }

            JvmPackageGen.lambdas = new HashMap<>();
        }

        private void createObjectFields(ClassWriter cw, @Nilable List<BField> fields) {
            for (BField field : fields) {
                if (field != null) {
                    if (IS_BSTRING) {
                        FieldVisitor fvb = cw.visitField(0, nameOfBStringFunc(field.name.value),
                                getTypeDesc(field.type, true), null, null);
                        fvb.visitEnd();
                    } else {
                        FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.type, false), null,
                                null);
                        fv.visitEnd();
                    }
                    String lockClass = "L" + LOCK_VALUE + ";";
                    FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL, computeLockNameFromString(field.name.value),
                            lockClass, null, null);
                    fv.visitEnd();
                }
            }
        }

        private void createObjectMethods(ClassWriter cw, @Nilable List<BIRFunction> attachedFuncs, boolean isService,
                                         String className, String typeName) {
            for (BIRFunction func : attachedFuncs) {
                if (func != null) {
                    generateMethod(func, cw, this.module, this.currentObjectType, isService, typeName);
                }
            }
        }

        private void createObjectInit(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", String.format("(L%s;)V", OBJECT_TYPE), null, null);
            mv.visitCode();

            // load super
            mv.visitVarInsn(ALOAD, 0);
            // load type
            mv.visitVarInsn(ALOAD, 1);
            // invoke super(type);
            mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_OBJECT_VALUE, "<init>", String.format("(L%s;)V", OBJECT_TYPE),
                    false);

            String lockClass = "L" + LOCK_VALUE + ";";
            for (BField field : fields) {
                if (field != null) {
                    Label fLabel = new Label();
                    mv.visitLabel(fLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitTypeInsn(NEW, LOCK_VALUE);
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, LOCK_VALUE, "<init>", "()V", false);
                    mv.visitFieldInsn(PUTFIELD, className, computeLockNameFromString(field.name.value), lockClass);
                }
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(5, 5);
            mv.visitEnd();
        }

        private void createCallMethod(ClassWriter cw, @Nilable List<BIRFunction> functions, String objClassName,
                                      String objTypeName, boolean isService) {

            @Nilable List<BIRFunction> funcs = getFunctions(functions);


            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call",
                    String.format("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT), null, null);
            mv.visitCode();

            int funcNameRegIndex = 2;

            Label defaultCaseLabel = new Label();

            // sort the fields before generating switch case
            funcs.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, funcNameRegIndex, funcs, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, funcNameRegIndex, funcs, labels,
                    defaultCaseLabel);

            // case body
            int i = 0;
            for (BIRFunction optionalFunc : funcs) {
                BIRFunction func = getFunction(optionalFunc);
                Label targetLabel = targetLabels.get(i);
                mv.visitLabel(targetLabel);

                @Nilable List<BType> paramTypes = func.type.paramTypes;
                @Nilable BType retType = func.type.retType;

                String methodSig = "";

                // use index access, since retType can be nil.
                boolean useBString = IS_BSTRING;
                methodSig = getMethodDesc(paramTypes, retType, null, useBString, false);

                // load self
                mv.visitVarInsn(ALOAD, 0);

                // load strand
                mv.visitVarInsn(ALOAD, 1);
                int j = 0;
                for (BType paramType : paramTypes) {
                    BType pType = getType(paramType);
                    // load parameters
                    mv.visitVarInsn(ALOAD, 3);

                    // load j'th parameter
                    mv.visitLdcInsn((long)j);
                    mv.visitInsn(L2I);
                    mv.visitInsn(AALOAD);
                    addUnboxInsn(mv, pType, useBString);
                    j += 1;
                }

                mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, func.name.value, methodSig, false);
                if (retType == null || retType.tag == TypeTags.NIL) {
                    mv.visitInsn(ACONST_NULL);
                } else {
                    addBoxInsn(mv, retType);
                    if (isService) {
                        mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "handleResourceError", String.format("(L%s;)L%s;",
                                OBJECT, OBJECT), false);
                    }
                }
                mv.visitInsn(ARETURN);
                i += 1;
            }

            createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex);
            mv.visitMaxs(funcs.size() + 10, funcs.size() + 10);
            mv.visitEnd();
        }

        private void createObjectGetMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                    String.format("(L%s;)L%s;", STRING_VALUE, OBJECT), null, null);
            mv.visitCode();

            int fieldNameRegIndex = 1;
            Label defaultCaseLabel = new Label();

            // sort the fields before generating switch case
            @Nilable List<BField> sortedFields = new ArrayList<>(fields);
            sortedFields.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, sortedFields, labels,
                    defaultCaseLabel);

            int i = 0;
            for (BField optionalField : sortedFields) {
                BField field = getObjectField(optionalField);
                Label targetLabel = targetLabels.get(i);
                mv.visitLabel(targetLabel);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, field.name.value, getTypeDesc(field.type, false));
                addBoxInsn(mv, field.type);
                mv.visitInsn(ARETURN);
                i += 1;
            }

            createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createObjectSetMethod(ClassWriter cw, @Nilable List<BField> fields, String className,
                                           boolean useBString) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set",
                    String.format("(L%s;L%s;)V", useBString ? I_STRING_VALUE : STRING_VALUE, OBJECT), null, null);
            mv.visitCode();
            int fieldNameRegIndex = 1;
            int valueRegIndex = 2;
            Label defaultCaseLabel = new Label();

            // code gen type checking for inserted value
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, fieldNameRegIndex);
            if (useBString) {
                mv.visitMethodInsn(INVOKEINTERFACE, I_STRING_VALUE, "getValue", String.format("()L%s;", STRING_VALUE),
                        true);
                mv.visitInsn(DUP);
                fieldNameRegIndex = 3;
                mv.visitVarInsn(ASTORE, fieldNameRegIndex);
            }
            mv.visitVarInsn(ALOAD, valueRegIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "checkFieldUpdate",
                    String.format("(L%s;L%s;)V", STRING_VALUE, OBJECT), false);

            // sort the fields before generating switch case
            @Nilable List<BField> sortedFields = new ArrayList<>(fields);
            sortedFields.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, sortedFields, labels,
                    defaultCaseLabel);

            // case body
            int i = 0;
            for (BField optionalField : sortedFields) {
                BField field = getObjectField(optionalField);
                Label targetLabel = targetLabels.get(i);
                mv.visitLabel(targetLabel);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, valueRegIndex);
                addUnboxInsn(mv, field.type, useBString);
                String filedName = field.name.value;
                if (useBString) {
                    filedName = nameOfBStringFunc(filedName);
                }
                mv.visitFieldInsn(PUTFIELD, className, filedName, getTypeDesc(field.type, useBString));
                mv.visitInsn(RETURN);
                i += 1;
            }

            createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private static BIRFunction getFunction(@Nilable BIRFunction func) {
            if (func != null) {
                return func;
            } else {
                throw new BLangCompilerException(String.format("Invalid function: %s", func));
            }
        }

        private byte[] createRecordValueClass(BRecordType recordType, String className,
                                              BIRNode.BIRTypeDefinition typeDef) {
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            cw.visitSource(typeDef.pos.getSource().cUnitName, null);
            currentClass = className;
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className,
                    String.format("<K:L%s;V:L%s;>L%s<TK;TV;>;L%s<TK;TV;>;", OBJECT, OBJECT, MAP_VALUE_IMPL, MAP_VALUE),
                    MAP_VALUE_IMPL, new String[]{MAP_VALUE});

            @Nilable List<BIRFunction> attachedFuncs = typeDef.attachedFuncs;
            if (attachedFuncs != null) {
                this.createRecordMethods(cw, attachedFuncs);
            }

            @Nilable List<BField> fields = recordType.fields;
            this.createRecordFields(cw, fields);
            this.createRecordGetMethod(cw, fields, className);
            this.createRecordSetMethod(cw, fields, className);
            this.createRecordEntrySetMethod(cw, fields, className);
            this.createRecordContainsKeyMethod(cw, fields, className);
            this.createRecordGetValuesMethod(cw, fields, className);
            this.createGetSizeMethod(cw, fields, className);
            this.createRecordRemoveMethod(cw);
            this.createRecordClearMethod(cw);
            this.createRecordGetKeysMethod(cw, fields, className);

            this.createRecordConstructor(cw, className);
            this.createRecordInitWrapper(cw, className, typeDef);
            this.createLambdas(cw);
            cw.visitEnd();
            byte[] result = cw.toByteArray();
            if (result == null) {
//                logCompileError(result, typeDef, this.module);
                return null;
            } else {
                return result;
            }
        }

        private void createRecordMethods(ClassWriter cw, @Nilable List<BIRFunction> attachedFuncs) {
            for (BIRFunction func : attachedFuncs) {
                if (func != null) {
                    generateMethod(func, cw, this.module, null, false, "");
                }
            }
        }

        private void createRecordConstructor(ClassWriter cw, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", String.format("(L%s;)V", BTYPE), null, null);
            mv.visitCode();

            // load super
            mv.visitVarInsn(ALOAD, 0);
            // load type
            mv.visitVarInsn(ALOAD, 1);
            // invoke `super(type)`;
            mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "<init>", String.format("(L%s;)V", BTYPE), false);

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createRecordInitWrapper(ClassWriter cw, String className, BIRNode.BIRTypeDefinition typeDef) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$init",
                    String.format("(L%s;L%s;)V", STRAND, MAP_VALUE), null, null);
            mv.visitCode();
            // load strand
            mv.visitVarInsn(ALOAD, 0);
            // load value
            mv.visitVarInsn(ALOAD, 1);

            // Invoke the init-functions of referenced types. This is done to initialize the
            // defualt values of the fields coming from the referenced types.
            for (BType typeRef : typeDef.referencedTypes) {
                if (typeRef.tag == TypeTags.RECORD) {
                    String refTypeClassName = getTypeValueClassName(typeRef.tsymbol.pkgID, toNameString(typeRef));
                    mv.visitInsn(DUP2);
                    mv.visitMethodInsn(INVOKESTATIC, refTypeClassName, "$init",
                            String.format("(L%s;L%s;)V", STRAND, MAP_VALUE), false);
                }
            }

            // Invoke the init-function of this type.
            String initFuncName;
            String valueClassName;
            @Nilable List<BIRFunction> attachedFuncs = typeDef.attachedFuncs;

            // Attached functions are empty for type-labeling. In such cases, call the __init() of
            // the original type value;
            if (attachedFuncs.size() != 0) {
                initFuncName = attachedFuncs.get(0).name.value; /*?.name ?.value;*/
                valueClassName = className;
            } else {
                // record type is the original record-type of this type-label
                BRecordType recordType = (BRecordType) typeDef.type;
                valueClassName = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
                initFuncName = cleanupFunctionName(recordType.name + "__init_");
            }

            mv.visitMethodInsn(INVOKESTATIC, valueClassName, initFuncName,
                    String.format("(L%s;L%s;)L%s;", STRAND, MAP_VALUE, OBJECT), false);
            mv.visitInsn(POP);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createRecordFields(ClassWriter cw, @Nilable List<BField> fields) {
            for (BField field : fields) {
                if (field != null) {
                    FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.type, false), null, null);
                    fv.visitEnd();

                    if (this.isOptionalRecordField(field)) {
                        fv = cw.visitField(0, this.getFieldIsPresentFlagName(field.name.value),
                                getTypeDesc(symbolTable.booleanType, false), null, null);
                        fv.visitEnd();
                    }
                }
            }
        }

        private String getFieldIsPresentFlagName(String fieldName) {
            return String.format("%s$isPresent", fieldName);
        }

        private boolean isOptionalRecordField(BField field) {
            return (field.symbol.flags & BAL_OPTIONAL) == BAL_OPTIONAL;
        }

        private void createRecordGetMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                    String.format("(L%s;)L%s;", OBJECT, OBJECT), String.format("(L%s;)TV;", OBJECT), null);
            mv.visitCode();

            int fieldNameRegIndex = 1;
            int strKeyVarIndex = 2;
            Label defaultCaseLabel = new Label();

            // cast key to java.lang.String
            mv.visitVarInsn(ALOAD, fieldNameRegIndex);
            mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
            mv.visitVarInsn(ASTORE, strKeyVarIndex);

            // sort the fields before generating switch case
            @Nilable List<BField> sortedFields = new ArrayList<>(fields);
            sortedFields.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                    defaultCaseLabel);

            int i = 0;
            for (BField optionalField : sortedFields) {
                BField field = getRecordField(optionalField);
                Label targetLabel = targetLabels.get(i);
                mv.visitLabel(targetLabel);

                // if the field is an optional-field, first check the 'isPresent' flag of that field.
                Label ifPresentLabel = new Label();
                String fieldName = field.name.value;
                if (this.isOptionalRecordField(field)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
                    mv.visitJumpInsn(IFNE, ifPresentLabel);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitInsn(ARETURN);
                }

                mv.visitLabel(ifPresentLabel);
                // return the value of the field
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.type, false));
                addBoxInsn(mv, field.type);
                mv.visitInsn(ARETURN);
                i += 1;
            }

            this.createRecordGetDefaultCase(mv, defaultCaseLabel, strKeyVarIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createRecordSetMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "putValue",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), "(TK;TV;)TV;", null);

            mv.visitCode();
            int fieldNameRegIndex = 1;
            int valueRegIndex = 2;
            int strKeyVarIndex = 3;
            Label defaultCaseLabel = new Label();

            // cast key to java.lang.String
            mv.visitVarInsn(ALOAD, fieldNameRegIndex);
            mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
            mv.visitVarInsn(ASTORE, strKeyVarIndex);

            // sort the fields before generating switch case
            @Nilable List<BField> sortedFields = new ArrayList<>(fields);
            sortedFields.sort(NAME_HASH_COMPARATOR);

            List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                    defaultCaseLabel);

            // case body
            int i = 0;
            for (BField optionalField : sortedFields) {
                BField field = getRecordField(optionalField);
                Label targetLabel = targetLabels.get(i);
                mv.visitLabel(targetLabel);

                // load the existing value to return
                String fieldName = field.name.value;
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.type, false));
                addBoxInsn(mv, field.type);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, valueRegIndex);
                addUnboxInsn(mv, field.type, false);
                mv.visitFieldInsn(PUTFIELD, className, fieldName, getTypeDesc(field.type, false));

                // if the field is an optional-field, then also set the isPresent flag of that field to true.
                if (this.isOptionalRecordField(field)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(ICONST_1);
                    mv.visitFieldInsn(PUTFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
                }

                mv.visitInsn(ARETURN);
                i += 1;
            }

            this.createRecordPutDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, valueRegIndex);
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

        private void createRecordEntrySetMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "entrySet",
                    String.format("()L%s;", SET),
                    String.format("()L%s<L%s<TK;TV;>;>;", SET, MAP_ENTRY),
                    null);
            mv.visitCode();

            int entrySetVarIndex = 1;
            mv.visitTypeInsn(NEW, LINKED_HASH_SET);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, entrySetVarIndex);

            for (BField optionalField : fields) {
                BField field = getRecordField(optionalField);
                Label ifNotPresent = new Label();

                // If its an optional field, generate if-condition to check the presense of the field.
                String fieldName = field.name.value;
                if (this.isOptionalRecordField(field)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
                    mv.visitJumpInsn(IFEQ, ifNotPresent);
                }

                mv.visitVarInsn(ALOAD, entrySetVarIndex);
                mv.visitTypeInsn(NEW, MAP_SIMPLE_ENTRY);
                mv.visitInsn(DUP);

                // field name as key
                mv.visitLdcInsn(fieldName);
                // field value as the map-entry value
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.type, false));
                addBoxInsn(mv, field.type);

                mv.visitMethodInsn(INVOKESPECIAL, MAP_SIMPLE_ENTRY, "<init>", String.format("(L%s;L%s;)V", OBJECT,
                        OBJECT), false);
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

        private void createRecordContainsKeyMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "containsKey", String.format("(L%s;)Z", OBJECT), null, null);
            mv.visitCode();

            int fieldNameRegIndex = 1;
            int strKeyVarIndex = 2;

            // cast key to java.lang.String
            mv.visitVarInsn(ALOAD, fieldNameRegIndex);
            mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
            mv.visitVarInsn(ASTORE, strKeyVarIndex);

            // sort the fields before generating switch case
            @Nilable List<BField> sortedFields = new ArrayList<>(fields);
            sortedFields.sort(NAME_HASH_COMPARATOR);

            Label defaultCaseLabel = new Label();
            List<Label> labels = createLabelsForSwitch(mv, strKeyVarIndex, sortedFields, defaultCaseLabel);
            List<Label> targetLabels = createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields, labels,
                    defaultCaseLabel);

            int i = 0;
            for (BField optionalField : sortedFields) {
                BField field = getObjectField(optionalField);
                Label targetLabel = targetLabels.get(i);
                mv.visitLabel(targetLabel);

                String fieldName = field.name.value;
                if (this.isOptionalRecordField(field)) {
                    // if the field is optional, then return the value is the 'isPresent' flag.
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
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

        void createRecordGetValuesMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "values", String.format("()L%s;", COLLECTION),
                    String.format("()L%s<TV;>;", COLLECTION), null);
            mv.visitCode();

            int valuesVarIndex = 1;
            mv.visitTypeInsn(NEW, ARRAY_LIST);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, valuesVarIndex);

            for (BField optionalField : fields) {
                BField field = getRecordField(optionalField);
                Label ifNotPresent = new Label();

                // If its an optional field, generate if-condition to check the presense of the field.
                String fieldName = field.name.value;
                if (this.isOptionalRecordField(field)) {
                    mv.visitVarInsn(ALOAD, 0); // this
                    mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
                    mv.visitJumpInsn(IFEQ, ifNotPresent);
                }

                mv.visitVarInsn(ALOAD, valuesVarIndex);
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(field.type, false));
                addBoxInsn(mv, field.type);
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

        void createGetSizeMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "size", "()I", null, null);
            mv.visitCode();
            int sizeVarIndex = 1;

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "size", "()I", false);
            mv.visitVarInsn(ISTORE, sizeVarIndex);

            int requiredFieldsCount = 0;
            for (BField optionalField : fields) {
                BField field = getObjectField(optionalField);
                String fieldName = field.name.value;
                if (this.isOptionalRecordField(field)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
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
            mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, "<init>", "()V", false);
            mv.visitInsn(ATHROW);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void createRecordClearMethod(ClassWriter cw) {
            // throw an UnsupportedOperationException, since remove is not supported by for records.
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "remove", String.format("(L%s;)L%s;", OBJECT, OBJECT),
                    String.format("(L%s;)TV;", OBJECT), null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, "<init>", "()V", false);
            mv.visitInsn(ATHROW);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        void createRecordGetKeysMethod(ClassWriter cw, @Nilable List<BField> fields, String className) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getKeys", String.format("()[L%s;", OBJECT), "()[TK;", null);
            mv.visitCode();

            int KeysVarIndex = 1;
            mv.visitTypeInsn(NEW, LINKED_HASH_SET);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, KeysVarIndex);

            for (BField optionalField : fields) {
                BField field = getRecordField(optionalField);
                Label ifNotPresent = new Label();

                // If its an optional field, generate if-condition to check the presense of the field.
                String fieldName = field.name.value;
                if (this.isOptionalRecordField(field)) {
                    mv.visitVarInsn(ALOAD, 0); // this
                    mv.visitFieldInsn(GETFIELD, className, this.getFieldIsPresentFlagName(fieldName),
                            getTypeDesc(symbolTable.booleanType, false));
                    mv.visitJumpInsn(IFEQ, ifNotPresent);
                }

                mv.visitVarInsn(ALOAD, KeysVarIndex);
                mv.visitLdcInsn(fieldName);
                mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", String.format("(L%s;)Z", OBJECT), true);
                mv.visitInsn(POP);
                mv.visitLabel(ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, KeysVarIndex);
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "keySet", String.format("()L%s;", SET), false);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", String.format("(L%s;)Z", COLLECTION), true);
            mv.visitInsn(POP);

            mv.visitVarInsn(ALOAD, KeysVarIndex);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "size", "()I", true);
            mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "toArray", String.format("([L%s;)[L%s;", OBJECT, OBJECT), true);

            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        ObjectGenerator(BIRPackage module) {
            this.module = module;
        }

        void generateValueClasses(@Nilable List<BIRNode.BIRTypeDefinition> typeDefs, Map<String, byte[]> jarEntries) {

            for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
                BIRNode.BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
                BType bType = typeDef.type;
                if (bType.tag == TypeTags.OBJECT &&
                        !Symbols.isFlagOn(((BObjectType) bType).tsymbol.flags, Flags.ABSTRACT)) {
                    BObjectType objectType = (BObjectType) bType;
                    this.currentObjectType = objectType;
                    String className = getTypeValueClassName(this.module, typeDef.name.value);
                    byte[] bytes = this.createObjectValueClass(objectType, className, typeDef, false);
                    jarEntries.put(className + ".class", bytes);
                } else if (bType instanceof BServiceType) {
                    BServiceType serviceType = (BServiceType) bType;
                    this.currentObjectType = serviceType;
                    String className = getTypeValueClassName(this.module, typeDef.name.value);
                    byte[] bytes = this.createObjectValueClass(serviceType, className, typeDef, true);
                    jarEntries.put(className + ".class", bytes);
                } else if (bType.tag == TypeTags.RECORD) {
                    BRecordType recordType = (BRecordType) bType;
                    this.currentRecordType = recordType;
                    String className = getTypeValueClassName(this.module, typeDef.name.value);
                    byte[] bytes = this.createRecordValueClass(recordType, className, typeDef);
                    jarEntries.put(className + ".class", bytes);
                }
            }
        }

        // Private methods
        private byte[] createObjectValueClass(BObjectType objectType, String className,
                                              BIRNode.BIRTypeDefinition typeDef, boolean isService) {
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            cw.visitSource(typeDef.pos.getSource().cUnitName, null);
            currentClass = className;
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, ABSTRACT_OBJECT_VALUE, new String[]{OBJECT_VALUE});

            @Nilable List<BField> fields = objectType.fields;
            this.createObjectFields(cw, fields);

            @Nilable List<BIRFunction> attachedFuncs = typeDef.attachedFuncs;
            if (attachedFuncs != null) {
                this.createObjectMethods(cw, attachedFuncs, isService, className, typeDef.name.value);
            }

            this.createObjectInit(cw, fields, className);
            this.createCallMethod(cw, attachedFuncs, className, toNameString(objectType), isService);
            this.createObjectGetMethod(cw, fields, className);
            if (IS_BSTRING) {
                this.createObjectSetMethod(cw, fields, className, true);
            } else {
                this.createObjectSetMethod(cw, fields, className, false);
            }
            this.createLambdas(cw);

            cw.visitEnd();
            byte[] result = cw.toByteArray();
            if (result == null) {
//                logCompileError(result, typeDef, this.module);
                // TODO log error
                return new byte[0];
            } else {
                return result;
            }
        }
    }

    // --------------------- Sorting ---------------------------

    static class NodeSorter {

//        void sortByHash(@Nilable List<NamedNode> arr) {
//            quickSort(arr, 0, arr.size() - 1);
//        }

        private static void quickSort(@Nilable List<NamedNode> arr, int low, int high) {
            if (low < high) {
                // pi is partitioning index, arr[pi] is now at right place
                int pi = partition(arr, low, high);

                // Recursively sort elements before partition and after partition
                quickSort(arr, low, pi - 1);
                quickSort(arr, pi + 1, high);
            }
        }

        private static int partition(@Nilable List<NamedNode> arr, int begin, int end) {
            int pivot = getHash(arr.get(end));
            int i = begin - 1;

            int j = begin;
            while (j < end) {
                if (getHash(arr.get(j)) <= pivot) {
                    i += 1;
                    swap(arr, i, j);
                }
                j += 1;
            }
            swap(arr, i + 1, end);
            return i + 1;
        }

        private static int getHash(Object node) {
            return getName(node).hashCode();
        }

        private static void swap(@Nilable List<NamedNode> arr, int i, int j) {
            @Nilable NamedNode swapTemp = arr.get(i);
            arr.set(i, arr.get(j));
            arr.set(j, swapTemp);
        }
    }
}
