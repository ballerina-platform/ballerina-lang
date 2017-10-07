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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;

public class LatticeGenerator {

    public static void main(String[] args) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.TRUE.toString());

        // TODO: make SymbolTable public to run this.
//        SymbolTable symbolTable = new SymbolTable(context);
        SymbolTable symbolTable = null;

        List<OperatorEdge> operatorLattice = symbolTable.rootScope.entries.values().stream()
              .filter(scopeEntry -> scopeEntry.symbol instanceof BOperatorSymbol
                                    && !(scopeEntry.symbol instanceof BConversionOperatorSymbol)
                                    && !(scopeEntry.symbol instanceof BCastOperatorSymbol))
              .map(scopeEntry -> {
                  BOperatorSymbol bOperatorSymbol = (BOperatorSymbol) scopeEntry.symbol;
                  OperatorEdge operatorEdge = new OperatorEdge();
                  operatorEdge.setOperator(bOperatorSymbol.getName().value);
                  if (bOperatorSymbol.type instanceof BInvokableType) {
                      BInvokableType type = (BInvokableType) bOperatorSymbol.type;
                      List<BType> paramTypes = type.getParameterTypes();
                      List<BType> returnTypes = type.getReturnTypes();
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
                  }
                  return operatorEdge;
              }).collect(Collectors.toList());

        List<SimpleTypeEdge> conversionLattice = symbolTable.rootScope.entries.values().stream()
            .filter(scopeEntry -> scopeEntry.symbol instanceof BConversionOperatorSymbol)
            .map(scopeEntry -> {
                SimpleTypeEdge simpleTypeEdge = new SimpleTypeEdge();
                BConversionOperatorSymbol bConversionSymbol = (BConversionOperatorSymbol) scopeEntry.symbol;
                simpleTypeEdge.setSafe(bConversionSymbol.safe);
                if (bConversionSymbol.type instanceof BInvokableType) {
                    BInvokableType type = (BInvokableType) bConversionSymbol.type;
                    List<BType> paramTypes = type.getParameterTypes();
                    if (paramTypes.size() == 2) {
                        simpleTypeEdge.setSource(paramTypes.get(0).getKind().typeName());
                        simpleTypeEdge.setTarget(paramTypes.get(1).getKind().typeName());
                    }
                }
                return simpleTypeEdge;
            }).collect(Collectors.toList());


        List<SimpleTypeEdge> castLattice = symbolTable.rootScope.entries.values().stream()
              .filter(scopeEntry -> scopeEntry.symbol instanceof BCastOperatorSymbol)
              .map(scopeEntry -> {
                  SimpleTypeEdge simpleTypeEdge = new SimpleTypeEdge();
                  BCastOperatorSymbol bConversionSymbol = (BCastOperatorSymbol) scopeEntry.symbol;
                  simpleTypeEdge.setSafe(bConversionSymbol.safe);
                  if (bConversionSymbol.type instanceof BInvokableType) {
                      BInvokableType type = (BInvokableType) bConversionSymbol.type;
                      List<BType> paramTypes = type.getParameterTypes();
                      if (paramTypes.size() == 2) {
                          simpleTypeEdge.setSource(paramTypes.get(0).getKind().typeName());
                          simpleTypeEdge.setTarget(paramTypes.get(1).getKind().typeName());
                      }
                  }
                  return simpleTypeEdge;
              }).collect(Collectors.toList());

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        System.out.println(operatorLattice.size() + " " + gson.toJson(operatorLattice));
        System.out.println(castLattice.size() + " " + gson.toJson(castLattice));
        System.out.println(conversionLattice.size() + " " + gson.toJson(conversionLattice));
    }
}
