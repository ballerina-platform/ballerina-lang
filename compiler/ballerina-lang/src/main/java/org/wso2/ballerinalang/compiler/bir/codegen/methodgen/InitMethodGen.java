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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JavaClass;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.isBuiltInPackage;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CREATOR;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * Generates Jvm byte code for the init methods.
 *
 * @since 2.0.0
 */
public class InitMethodGen {

    private final SymbolTable symbolTable;
    private final BUnionType errorOrNilType;
    private int nextId = -1;
    private int nextVarId = -1;

    public InitMethodGen(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errorOrNilType = BUnionType.create(null, symbolTable.errorType, symbolTable.nilType);
    }

    /**
     * Generate a lambda function to invoke ballerina main.
     *
     * @param cw        class visitor
     * @param pkg       bir package
     * @param initClass module init class
     * @param depMods   dependent module list
     */
    public void generateLambdaForPackageInits(ClassWriter cw, BIRNode.BIRPackage pkg, String initClass,
                                              List<PackageID> depMods, JvmCastGen jvmCastGen) {
        //need to generate lambda for package Init as well, if exist
        if (!MethodGenUtils.hasInitFunction(pkg)) {
            return;
        }
        generateLambdaForModuleFunction(cw, MODULE_INIT, initClass, jvmCastGen);

        // generate another lambda for start function as well
        generateLambdaForModuleFunction(cw, MODULE_START, initClass, jvmCastGen);

        MethodVisitor mv = visitFunction(cw, MethodGenUtils
                .calculateLambdaStopFuncName(pkg.packageID));

        invokeStopFunction(initClass, mv);

        for (PackageID id : depMods) {
            String jvmClass = JvmCodeGenUtil.getPackageName(id) + MODULE_INIT_CLASS_NAME;
            generateLambdaForDepModStopFunc(cw, id, jvmClass);
        }
    }

    private void generateLambdaForModuleFunction(ClassWriter cw, String funcName, String initClass,
                                                 JvmCastGen jvmCastGen) {
        MethodVisitor mv = visitFunction(cw, String.format("$lambda$%s$", funcName));
        mv.visitCode();

        //load strand as first arg
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);

        mv.visitMethodInsn(INVOKESTATIC, initClass, funcName, String.format("(L%s;)L%s;", STRAND_CLASS, OBJECT), false);
        jvmCastGen.addBoxInsn(mv, errorOrNilType);
        MethodGenUtils.visitReturn(mv);
    }

    private void generateLambdaForDepModStopFunc(ClassWriter cw, PackageID pkgID, String initClass) {
        String lambdaName = MethodGenUtils.calculateLambdaStopFuncName(pkgID);
        MethodVisitor mv = visitFunction(cw, lambdaName);
        invokeStopFunction(initClass, mv);
    }

    private MethodVisitor visitFunction(ClassWriter cw, String funcName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, funcName, String.format(
                "([L%s;)L%s;", OBJECT, OBJECT), null, null);
        mv.visitCode();
        return mv;
    }

    private void invokeStopFunction(String initClass, MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        String stopFuncName = MethodGenUtils.encodeModuleSpecialFuncName(MethodGenUtils.STOP_FUNCTION_SUFFIX);
        mv.visitMethodInsn(INVOKESTATIC, initClass, stopFuncName, String.format("(L%s;)L%s;", STRAND_CLASS, OBJECT),
                           false);
        MethodGenUtils.visitReturn(mv);
    }

    public void generateModuleInitializer(ClassWriter cw, BIRNode.BIRPackage module, String typeOwnerClass,
                                          String moduleTypeClass) {
        // Using object return type since this is similar to a ballerina function without a return.
        // A ballerina function with no returns is equivalent to a function with nil-return.
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, CURRENT_MODULE_INIT,
                                          String.format("()L%s;", OBJECT), null, null);
        mv.visitCode();

        mv.visitMethodInsn(INVOKESTATIC, moduleTypeClass, CREATE_TYPES_METHOD, "()V", false);
        mv.visitTypeInsn(NEW, typeOwnerClass);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, typeOwnerClass, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(module.packageID.orgName.getValue()));
        mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(module.packageID.name.getValue()));
        mv.visitLdcInsn(getMajorVersion(module.packageID.version.getValue()));
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, String.format("%s", VALUE_CREATOR), "addValueCreator",
                           String.format("(L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE,
                                         VALUE_CREATOR),
                           false);

        // Add a nil-return
        mv.visitInsn(ACONST_NULL);
        MethodGenUtils.visitReturn(mv);
    }

    public void enrichPkgWithInitializers(Map<String, JavaClass> jvmClassMap, String typeOwnerClass,
                                          BIRNode.BIRPackage pkg, List<PackageID> moduleImports) {
        JavaClass javaClass = jvmClassMap.get(typeOwnerClass);
        BIRNode.BIRFunction initFunc = generateDefaultFunction(moduleImports, pkg, MODULE_INIT,
                                                               MethodGenUtils.INIT_FUNCTION_SUFFIX);
        javaClass.functions.add(initFunc);
        pkg.functions.add(initFunc);

        BIRNode.BIRFunction startFunc = generateDefaultFunction(moduleImports, pkg, MODULE_START,
                                                                MethodGenUtils.START_FUNCTION_SUFFIX);
        javaClass.functions.add(startFunc);
        pkg.functions.add(startFunc);

    }

    private BIRNode.BIRFunction generateDefaultFunction(List<PackageID> imprtMods, BIRNode.BIRPackage pkg,
                                                        String funcName, String initName) {
        nextId = -1;
        nextVarId = -1;

        BIRNode.BIRVariableDcl retVar = new BIRNode.BIRVariableDcl(null, errorOrNilType, new Name("%ret"),
                                                                   VarScope.FUNCTION, VarKind.RETURN, "");
        BIROperand retVarRef = new BIROperand(retVar);

        BInvokableType funcType = new BInvokableType(Collections.emptyList(), null, errorOrNilType, null);
        BIRNode.BIRFunction modInitFunc = new BIRNode.BIRFunction(null, new Name(funcName), 0, funcType, null, 0,
                                                                  VIRTUAL);
        modInitFunc.localVars.add(retVar);
        addAndGetNextBasicBlock(modInitFunc);

        BIRNode.BIRVariableDcl boolVal = addAndGetNextVar(modInitFunc, symbolTable.booleanType);
        BIROperand boolRef = new BIROperand(boolVal);

        for (PackageID id : imprtMods) {
            String initFuncName = MethodGenUtils.encodeModuleSpecialFuncName(initName);
            addCheckedInvocation(modInitFunc, id, initFuncName, retVarRef, boolRef);
        }

        String currentInitFuncName = MethodGenUtils.encodeModuleSpecialFuncName(initName);
        BIRNode.BIRBasicBlock lastBB = addCheckedInvocation(modInitFunc, pkg.packageID, currentInitFuncName, retVarRef,
                                                            boolRef);

        lastBB.terminator = new BIRTerminator.Return(null);

        return modInitFunc;
    }

    private BIRNode.BIRVariableDcl addAndGetNextVar(BIRNode.BIRFunction func, BType typeVal) {
        BIRNode.BIRVariableDcl nextLocalVar = new BIRNode.BIRVariableDcl(typeVal, getNextVarId(), VarScope.FUNCTION,
                                                                         VarKind.LOCAL);
        func.localVars.add(nextLocalVar);
        return nextLocalVar;
    }

    private Name getNextVarId() {
        String varIdPrefix = "%";
        nextVarId += 1;
        return new Name(varIdPrefix + nextVarId);
    }

    private BIRNode.BIRBasicBlock addCheckedInvocation(BIRNode.BIRFunction func, PackageID modId, String initFuncName,
                                                       BIROperand retVar, BIROperand boolRef) {
        BIRNode.BIRBasicBlock lastBB = func.basicBlocks.get(func.basicBlocks.size() - 1);
        BIRNode.BIRBasicBlock nextBB = addAndGetNextBasicBlock(func);
        // TODO remove once lang.annotation is fixed
        if (isBuiltInPackage(modId)) {
            lastBB.terminator = new BIRTerminator.Call(null, InstructionKind.CALL, false, modId,
                                                       new Name(initFuncName), Collections.emptyList(), null, nextBB,
                                                       Collections.emptyList(),
                                                       Collections.emptySet());
            return nextBB;
        }
        lastBB.terminator = new BIRTerminator.Call(null, InstructionKind.CALL, false, modId, new Name(initFuncName),
                                                   Collections.emptyList(), retVar, nextBB, Collections.emptyList(),
                                                   Collections.emptySet());

        BIRNonTerminator.TypeTest typeTest = new BIRNonTerminator.TypeTest(null, symbolTable.errorType, boolRef,
                                                                           retVar);
        nextBB.instructions.add(typeTest);

        BIRNode.BIRBasicBlock trueBB = addAndGetNextBasicBlock(func);
        BIRNode.BIRBasicBlock retBB = addAndGetNextBasicBlock(func);
        retBB.terminator = new BIRTerminator.Return(null);
        trueBB.terminator = new BIRTerminator.GOTO(null, retBB);

        BIRNode.BIRBasicBlock falseBB = addAndGetNextBasicBlock(func);
        nextBB.terminator = new BIRTerminator.Branch(null, boolRef, trueBB, falseBB);
        return falseBB;
    }

    private BIRNode.BIRBasicBlock addAndGetNextBasicBlock(BIRNode.BIRFunction func) {
        BIRNode.BIRBasicBlock nextbb = new BIRNode.BIRBasicBlock(getNextBBId());
        func.basicBlocks.add(nextbb);
        return nextbb;
    }

    private Name getNextBBId() {
        String bbIdPrefix = "genBB";
        nextId += 1;
        return new Name(bbIdPrefix + nextId);
    }

    public void resetIds() {
        nextId = -1;
        nextVarId = -1;
    }

    public int incrementAndGetNextId() {
        return nextId++;
    }

}
