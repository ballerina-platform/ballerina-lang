/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.composer.service.workspace.lattice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.workspace.rest.typelattice.OperatorEdge;
import org.ballerinalang.composer.service.workspace.rest.typelattice.SimpleTypeEdge;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

public class LatticeGenerator {

    public static void main(String[] args) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(COMPILER_PHASE, CompilerPhase.DEFINE.toString());

        Names names = Names.getInstance(context);

        // TODO: make SymbolTable public to run this.
//        SymbolTable symbolTable = new SymbolTable(context);
        SymbolTable symbolTable = null;


        // create operator lattice
        Map<String, List<OperatorEdge>> operatorLattice = new HashMap<>();

        OperatorKind[] operators = OperatorKind.values();
        for (int i = 0; i < operators.length; i++) {
            Scope.ScopeEntry entry =
                    symbolTable.rootScope.entries.get(names.fromString(operators[i].value()));
            List<OperatorEdge> operatorEdges = new ArrayList<>();

            if (entry == null) {
                operatorLattice.put(operators[i].value(), operatorEdges);
                continue;
            }

            while (entry != NOT_FOUND_ENTRY) {
                BInvokableType opType = (BInvokableType) entry.symbol.type;
                OperatorEdge operatorEdge = new OperatorEdge();
                List<BType> paramTypes = opType.getParameterTypes();
                List<BType> returnTypes = opType.getReturnTypes();
                if (paramTypes.size() == 2) {
                    operatorEdge.setType("binary");
                    operatorEdge.setLhType(paramTypes.get(0).getKind().typeName());
                    operatorEdge.setRhType(paramTypes.get(1).getKind().typeName());

                } else if (paramTypes.size() == 1) {
                    operatorEdge.setType("unary");
                    operatorEdge.setRhType(paramTypes.get(0).getKind().typeName());
                }
                if (returnTypes.size() == 1) {
                    operatorEdge.setRetType(returnTypes.get(0).getKind().typeName());
                }
                operatorEdges.add(operatorEdge);
                entry = entry.next;
            }
            operatorLattice.put(operators[i].value(), operatorEdges);
        }


        // Create conversion lattice
        Scope.ScopeEntry conversionEntry =
                symbolTable.rootScope.entries.get(Names.CONVERSION_OP);
        List<SimpleTypeEdge> conversionLattice = new ArrayList<>();

        if (conversionEntry != null) {
            while (conversionEntry != NOT_FOUND_ENTRY) {
                SimpleTypeEdge simpleTypeEdge = new SimpleTypeEdge();
                BConversionOperatorSymbol bConversionSymbol = (BConversionOperatorSymbol) conversionEntry.symbol;
                simpleTypeEdge.setSafe(bConversionSymbol.safe);
                if (bConversionSymbol.type instanceof BInvokableType) {
                    BInvokableType type = (BInvokableType) bConversionSymbol.type;
                    List<BType> paramTypes = type.getParameterTypes();
                    if (paramTypes.size() == 2) {
                        simpleTypeEdge.setSource(paramTypes.get(0).getKind().typeName());
                        simpleTypeEdge.setTarget(paramTypes.get(1).getKind().typeName());
                    }
                }
                conversionLattice.add(simpleTypeEdge);
                conversionEntry = conversionEntry.next;
            }
        }

        // Create cast lattice
        Scope.ScopeEntry castEntry =
                symbolTable.rootScope.entries.get(Names.CAST_OP);
        List<SimpleTypeEdge> castLattice = new ArrayList<>();

        if (castEntry != null) {
            while (castEntry != NOT_FOUND_ENTRY) {
                SimpleTypeEdge simpleTypeEdge = new SimpleTypeEdge();
                BCastOperatorSymbol bCastOperatorSymbol = (BCastOperatorSymbol) castEntry.symbol;
                simpleTypeEdge.setSafe(bCastOperatorSymbol.safe);
                if (bCastOperatorSymbol.type instanceof BInvokableType) {
                    BInvokableType type = (BInvokableType) bCastOperatorSymbol.type;
                    List<BType> paramTypes = type.getParameterTypes();
                    if (paramTypes.size() == 2) {
                        simpleTypeEdge.setSource(paramTypes.get(0).getKind().typeName());
                        simpleTypeEdge.setTarget(paramTypes.get(1).getKind().typeName());
                    }
                }
                castLattice.add(simpleTypeEdge);
                castEntry = castEntry.next;
            }
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        System.out.println(operatorLattice.size() + " " + gson.toJson(operatorLattice));
        System.out.println(castLattice.size() + " " + gson.toJson(castLattice));
        System.out.println(conversionLattice.size() + " " + gson.toJson(conversionLattice));
    }
}
