/*
 * Copyright (c) (2022), WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.OCEDynamicEnvData;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;
import org.wso2.ballerinalang.util.Flags;

/**
 * This class contains static util methods commonly used to desugar class closure access desugaring.
 *
 * @since 2.0.0
 */
public class ClassClosureDesugarUtils {

    public static void updateProceedingClasses(SymbolEnv envArg, OCEDynamicEnvData oceEnvData,
                                               BLangClassDefinition origClassDef) {
        SymbolEnv localEnv = envArg.enclEnv;
        while (localEnv != null) {
            BLangNode node = localEnv.node;
            if (node.getKind() == NodeKind.PACKAGE) {
                break;
            }

            if (node.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDef = (BLangClassDefinition) node;
                if (classDef != origClassDef) {
                    classDef.hasClosureVars = true;
                    OCEDynamicEnvData parentOceData = classDef.oceEnvData;
                    oceEnvData.parent = classDef;
                    parentOceData.closureFuncSymbols.addAll(oceEnvData.closureFuncSymbols);
                    parentOceData.closureBlockSymbols.addAll(oceEnvData.closureBlockSymbols);

                    // new approach
                    parentOceData.closureSymbols.addAll(oceEnvData.closureSymbols);
                    parentOceData.closureVarOriginalSymbols.addAll(oceEnvData.closureVarOriginalSymbols);
                }
            }
            localEnv = localEnv.enclEnv;
        }
    }

    public static void collectObjectCtorClosureSymbols(Location pos, BLangFunction enclosedF, BSymbol resolvedSymbol,
                                                       BLangClassDefinition classDef, SymbolEnv env, BSymbol symbol,
                                                       BLangNode originalNode) {
        classDef.hasClosureVars = true;
        OCEDynamicEnvData oceEnvData = classDef.oceEnvData;
        if (resolvedSymbol.closure && oceEnvData.closureSymbols.contains(resolvedSymbol)) {
            return; // already marked by this class
        }
        oceEnvData.closureSymbols.add(resolvedSymbol);
        oceEnvData.closureVarOriginalSymbols.add(symbol);

        resolvedSymbol.flags |= Flags.OBJECT_CTOR;
        resolvedSymbol.flags |= Flags.ATTACHED;
        resolvedSymbol.closure = true;

        if (originalNode.getKind() == NodeKind.ARROW_EXPR) {
            BLangArrowFunction arrowFunction = (BLangArrowFunction) originalNode;
            if (arrowFunction.parent == null) {
                arrowFunction.parent = classDef;
            }
        }

        if (enclosedF != null) { // for dataflow analyzer only
            enclosedF.closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
        }

        // TODO: refactor this for automation
        if (enclosedF != null && (enclosedF.symbol.params.contains(resolvedSymbol)
                || (enclosedF.symbol.restParam == resolvedSymbol))) {
            oceEnvData.closureFuncSymbols.add(resolvedSymbol);
        } else {
            oceEnvData.closureBlockSymbols.add(resolvedSymbol);
        }
        updateProceedingClasses(env.enclEnv, oceEnvData, classDef);
    }

    /**
     * Update the closure variables with the relevant map access expression.
     * a + b can become a + self[$map$func_4][b]
     * a + b ==> a + self[$map$func_4][b]
     * we are dealing with the simple var ref `b` here
     *
     * @param classDef   OCE class definition
     * @param selfSymbol self symbol of attached method
     * @param varRefExpr closure variable reference to be updated
     */
    public static BLangTypeConversionExpr updateClosureVarsWithMapAccessExpression(BLangClassDefinition classDef,
                                                                                   BVarSymbol selfSymbol,
                                                                                   BLangSimpleVarRef varRefExpr,
                                                                                   BVarSymbol mapSymbol,
                                                                                   BType stringType) {
        // self
        BLangSimpleVarRef.BLangLocalVarRef localSelfVarRef = new BLangSimpleVarRef.BLangLocalVarRef(selfSymbol);
        localSelfVarRef.setBType(classDef.getBType());
        Location pos = varRefExpr.pos;

        // self[mapSymbol]
        BLangIndexBasedAccess.BLangStructFieldAccessExpr accessExprForClassField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(pos, localSelfVarRef,
                        ASTBuilderUtil.createLiteral(pos, stringType,
                                mapSymbol.name), mapSymbol, false, true);
        accessExprForClassField.setBType(mapSymbol.type);
        accessExprForClassField.isLValue = true;

        // self[mapSymbol][varRefExpr]
        BLangIndexBasedAccess.BLangMapAccessExpr closureMapAccessForField =
                new BLangIndexBasedAccess.BLangMapAccessExpr(pos, accessExprForClassField,
                        ASTBuilderUtil.createLiteral(pos, stringType, varRefExpr.symbol.name));
        closureMapAccessForField.setBType(varRefExpr.symbol.type);
        closureMapAccessForField.isLValue = false;

        // <varRefExpr.type> self[mapSymbol][varRefExpr]
        BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        castExpr.expr = closureMapAccessForField;
        castExpr.setBType(varRefExpr.symbol.type);
        castExpr.targetType = varRefExpr.symbol.type;
        return castExpr;
    }

    public static BLangIndexBasedAccess.BLangStructFieldAccessExpr
    getClassMapAccessExpression(Location pos, BVarSymbol mapSymbol, BLangClassDefinition classDef, BType stringType) {
        BVarSymbol selfSymbol = classDef.generatedInitFunction.receiver.symbol;

        // self
        BLangSimpleVarRef.BLangLocalVarRef localSelfVarRef = new BLangSimpleVarRef.BLangLocalVarRef(selfSymbol);
        localSelfVarRef.setBType(classDef.getBType());
//        localSelfVarRef.closureDesugared = true;
        localSelfVarRef.symbol.closure = true;

        // self[mapSymbol]
        BLangIndexBasedAccess.BLangStructFieldAccessExpr accessExprForClassField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(pos, localSelfVarRef,
                        ASTBuilderUtil.createLiteral(pos, stringType,
                                mapSymbol.name), mapSymbol, false, true);
        return accessExprForClassField;
    }
}
