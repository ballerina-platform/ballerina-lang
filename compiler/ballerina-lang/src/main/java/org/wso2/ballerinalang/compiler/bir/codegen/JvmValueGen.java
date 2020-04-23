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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.NameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.OldStyleExternalFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_RUNTIME_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.enrichWithDefaultableParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupTypeName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunction;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getTypeDef;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
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

        List<BIRNode.BIRTypeDefinition> typeDefs = module.typeDefs;
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRNode.BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;
            if (bType instanceof BServiceType) {
                desugarObjectMethods(module, bType, typeDef.attachedFuncs);
            } else if (bType.tag == TypeTags.OBJECT &&
                    !Symbols.isFlagOn(((BObjectType) bType).tsymbol.flags, Flags.ABSTRACT)) {
                desugarObjectMethods(module, bType, typeDef.attachedFuncs);
            } else if (bType.tag == TypeTags.RECORD) {
                desugarObjectMethods(module, bType, typeDef.attachedFuncs);
            }
        }
    }

    private static void desugarObjectMethods(BIRPackage module, BType bType, List<BIRFunction> attachedFuncs) {

        if (attachedFuncs == null) {
            return;
        }
        for (BIRFunction func : attachedFuncs) {
            if (func == null) {
                continue;
            }
            if (isExternFunc(func)) {
                BIRFunctionWrapper extFuncWrapper = lookupBIRFunctionWrapper(module, func, bType);
                if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                    // TODO: Note when this support new interop, update here as well
                    desugarOldExternFuncs(module, (OldStyleExternalFunctionWrapper) extFuncWrapper, func);
                }
            } else {
                addDefaultableBooleanVarsToSignature(func);
            }
            enrichWithDefaultableParamInits(getFunction(func));
        }
    }

    public static List<Label> createLabelsForSwitch(MethodVisitor mv, int nameRegIndex,
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
                hashCodes[i] = getName(node).hashCode();
                i += 1;
            }
        }
        mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels.toArray(new Label[0]));
        return labels;
    }

    public static void createDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex) {

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

    public static String getTypeValueClassName(Object module, String typeName) {

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

    public static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
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
            mv.visitLdcInsn(getName(node));
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

}
