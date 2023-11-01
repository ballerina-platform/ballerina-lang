/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Detect the unused BIRFunctions, BIRTypeDefs and BIRConstants
 *
 */
public class DeadBIRNodeAnalyzer extends BIRVisitor {

    private static final CompilerContext.Key<DeadBIRNodeAnalyzer> DEAD_BIR_NODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final HashSet<String> USED_FUNCTION_NAMES =
            new HashSet<>(Arrays.asList("main", ".<init>", ".<start>", ".<stop>"));
    public PackageCache pkgCache;
    public TypeDefAnalyzer typeDefAnalyzer;
    public BIRNode.BIRFunction currentParentFunction;
    public InvocationData currentInvocationData;
    public final HashMap<PackageID, InvocationData> pkgWiseInvocationData = new HashMap<>();

    private DeadBIRNodeAnalyzer(CompilerContext context) {
        context.put(DEAD_BIR_NODE_ANALYZER_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
        this.typeDefAnalyzer = TypeDefAnalyzer.getInstance(context);
    }

    public static DeadBIRNodeAnalyzer getInstance(CompilerContext context) {
        DeadBIRNodeAnalyzer deadBIRNodeAnalyzer = context.get(DEAD_BIR_NODE_ANALYZER_KEY);
        if (deadBIRNodeAnalyzer == null) {
            deadBIRNodeAnalyzer = new DeadBIRNodeAnalyzer(context);
        }
        return deadBIRNodeAnalyzer;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        currentInvocationData = pkgNode.symbol.invocationData;
        pkgWiseInvocationData.putIfAbsent(pkgNode.packageID, currentInvocationData);
        currentInvocationData.registerNodes(pkgNode.symbol.bir);
        typeDefAnalyzer.analyze(pkgNode.symbol.bir);

        visit(pkgNode.symbol.bir);

        // TODO Make this happen only once after all the modules are analyzed
        pkgWiseInvocationData.values().forEach(InvocationData::updateInvocationData);
        return pkgNode;
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition typeDef) {
        typeDef.attachedFuncs.forEach(func -> func.accept(this));
        // TODO find a way to move this code to someplace else. It is used to track function invocations inside typeDefs
        typeDef.attachedFuncs.forEach(func -> {
            //TODO use a constant for finding the init function of the type def
            if (func.name.toString().equals(".<init>")) {
                func.childNodes.forEach(typeDef::addChildNode);
            }
        });
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        currentParentFunction = birFunction;
        if (USED_FUNCTION_NAMES.contains(birFunction.originalName.value)) {
            birFunction.markSelfAndChildrenAsUsed();
        }
        birFunction.basicBlocks.forEach(bb -> bb.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        if (birBasicBlock.terminator.getKind() == InstructionKind.CALL) {
            BIRTerminator.Call terminatorCall = (BIRTerminator.Call) birBasicBlock.terminator;
            currentParentFunction.addChildNode(lookupBirFunction(terminatorCall));
        }

        birBasicBlock.instructions.forEach(instruction -> {
            if (instruction.getKind() == InstructionKind.NEW_TYPEDESC ||
                    instruction.getKind() == InstructionKind.NEW_INSTANCE ||
                    instruction.getKind() == InstructionKind.TYPE_CAST) {
                instruction.accept(this);
            }
        });
    }

    @Override
    public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        if (newTypeDesc.type.tsymbol == null) {
            return;
        }
        currentParentFunction.addChildNode(lookupBIRTypeDef(newTypeDesc));
    }

    @Override
    public void visit(BIRNonTerminator.NewInstance newInstance) {
        currentParentFunction.addChildNode(lookupBIRTypeDef(newInstance));
    }

    @Override
    public void visit(BIRNonTerminator.TypeCast typeCast) {
        currentParentFunction.addChildNode(lookupBIRTypeDef(typeCast));
    }

    public BIRNode.BIRFunction lookupBirFunction(BIRTerminator.Call terminatorCall) {
        InvocationData invocationData = pkgCache.getInvocationData(terminatorCall.calleePkg);
        return invocationData.functionPool.get(terminatorCall.originalName.value);
    }

    public BIRNode.BIRTypeDefinition lookupBIRTypeDef(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        InvocationData invocationData = pkgCache.getInvocationData(newTypeDesc.type.tsymbol.pkgID);
        return invocationData.typeDefPool.get(newTypeDesc.type.tsymbol.originalName.toString());
    }

    public BIRNode.BIRTypeDefinition lookupBIRTypeDef(BIRNonTerminator.NewInstance newInstance) {
        if (newInstance.expectedType.tsymbol == null) {
            return null;
        }
        InvocationData invocationData = pkgCache.getInvocationData(newInstance.expectedType.tsymbol.pkgID);
        return invocationData.typeDefPool.get(newInstance.expectedType.tsymbol.originalName.toString());
    }

    public BIRNode.BIRTypeDefinition lookupBIRTypeDef(BIRNonTerminator.TypeCast typeCast) {
        if (typeCast.type.tsymbol == null) {
            return null;
        }
        InvocationData invocationData = pkgCache.getInvocationData(typeCast.type.tsymbol.pkgID);
        return invocationData.typeDefPool.get(typeCast.type.tsymbol.originalName.value);
    }
    public static class InvocationData {

        // TODO Change the nodePool structure to HashMap<BType, BIRNode>
        public final HashMap<String, BIRNode.BIRFunction> functionPool = new HashMap<>();
        public final HashSet<BIRNode.BIRFunction> usedFunctions = new HashSet<>();
        public final HashSet<BIRNode.BIRFunction> deadFunctions = new HashSet<>();
        public final HashMap<String, BIRNode.BIRTypeDefinition> typeDefPool = new HashMap<>();
        public final HashSet<BIRNode.BIRTypeDefinition> usedTypeDefs = new HashSet<>();
        public final HashSet<BIRNode.BIRTypeDefinition> deadTypeDefs = new HashSet<>();

        // TODO Remove the "remove from hashset" part later. It wont be needed
        public void updateInvocationData() {
            functionPool.values().forEach(birFunction -> {
                if (birFunction.usedState == UsedState.UNUSED) {
                    deadFunctions.add(birFunction);
                    usedFunctions.remove(birFunction);
                } else {
                    usedFunctions.add(birFunction);
                    deadFunctions.remove(birFunction);
                }
            });

            typeDefPool.values().forEach(birTypeDef -> {
                if (birTypeDef.usedState == UsedState.UNUSED) {
                    deadTypeDefs.add(birTypeDef);
                    usedTypeDefs.remove(birTypeDef);
                } else {
                    usedTypeDefs.add(birTypeDef);
                    deadTypeDefs.remove(birTypeDef);
                }
            });
        }

        // TODO Find a better way register the BIR Nodes
        public void registerNodes(BIRNode.BIRPackage birPackage) {
            birPackage.functions.forEach(this::initializeFunction);
            // TODO check why we omitted the FINITE typeDefs
            // TODO migrating the typeDef UsedState initialization to TypeDefAnalyzer. Clean up here if its permanent
            birPackage.typeDefs.forEach(typeDef -> {
//                if (typeDef.type.getKind() != TypeKind.FINITE) {
//                    typeDef.usedState = UsedState.UNUSED;
//                }
                typeDefPool.putIfAbsent(typeDef.internalName.value, typeDef);
                typeDef.attachedFuncs.forEach(this::initializeFunction);
            });
        }

        public void initializeFunction(BIRNode.BIRFunction birFunction) {
            birFunction.usedState = UsedState.UNUSED;
            functionPool.putIfAbsent(birFunction.originalName.value, birFunction);
        }
    }
}
