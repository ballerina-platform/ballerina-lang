/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.BmpStringValue;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.Map;


/**
 * Lower the AST to nBallerina Module.
 *
 * @since 0.980.0
 */
public class ModuleGen {

    private static final CompilerContext.Key<ModuleGen> MOD_GEN =
            new CompilerContext.Key<>();


    //private ModGenEnv env;

    //Module modBir = new Module("wso2", "nballerina.bir", "0.1.0");
    Module modFront = new Module("wso2", "nballerina.front", "0.1.0");
    static BMap<BString, Object> tempVal;

    public static ModuleGen getInstance(CompilerContext context) {
        ModuleGen modGen = context.get(MOD_GEN);
        if (modGen == null) {
            modGen = new ModuleGen(context);
        }

        return modGen;
    }

    private ModuleGen(CompilerContext context) {
        context.put(MOD_GEN, this);

    }

    public BLangPackage genMod(BLangPackage astPkg) {
        astPkg.accept(this);
        return astPkg;
    }


    
    public Object visit(BLangPackage astPkg) {
        //astPkg.imports.forEach(impPkg -> impPkg.accept(this));
        //astPkg.constants.forEach(astConst -> astConst.accept(this));
        //astPkg.typeDefinitions.forEach(astTypeDef -> astTypeDef.accept(this));
       // generateClassDefinitions(astPkg.topLevelNodes);
        //astPkg.globalVars.forEach(astGlobalVar -> astGlobalVar.accept(this));
        //astPkg.initFunction.accept(this);
        //astPkg.startFunction.accept(this);
        //astPkg.stopFunction.accept(this);
        astPkg.functions.forEach(astFunc -> astFunc.accept(this));
        //astPkg.annotations.forEach(astAnn -> astAnn.accept(this));
        //astPkg.services.forEach(service -> service.accept(this));
        return null;
    }

    public Object visit(BLangFunction astFunc) {
        return astFunc.body.accept(this);
    }
    
    public Object visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
        return null;
    }

    public Object visit(BLangStatementExpression statementExpression) {
        statementExpression.stmt.accept(this);
        statementExpression.expr.accept(this);
        return null;
    }

    public BMap<BString, Object> visit(BLangReturn astReturnStmt) {
        Object exp = astReturnStmt.expr.accept(this);
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("returnExpr", exp);

        BMap<BString, Object> x = ValueCreator.createReadonlyRecordValue(modFront, "ReturnStmt",
                mapInitialValueEntries);
        ModuleGen.tempVal = x;
        return x;
    }

    public Object visit(BLangIf bLangIf) {
        Object expr = bLangIf.expr.accept(this);
        Object ifTrue = bLangIf.body.accept(this);
        Object ifFalse =bLangIf.elseStmt.accept(this);

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("condition", expr);
        mapInitialValueEntries.put("ifTrue", ifTrue);
        mapInitialValueEntries.put("ifFalse", ifFalse);

        return ValueCreator.createRecordValue(modFront, "IfElseStmt",
                mapInitialValueEntries);
    }

    public Object visit(BLangWhile bLangWhile) {
        Object exp =  bLangWhile.expr.accept(this);
        Object bodyStmts = bLangWhile.body.accept(this);
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("condition", exp);
        mapInitialValueEntries.put("body", bodyStmts);

        return ValueCreator.createRecordValue(modFront, "WhileStmt",
                mapInitialValueEntries);
    }

    public Object visit(BLangBreak bLangBreak){
        return new BmpStringValue("break");
    }

    public Object visit(BLangContinue bLangContinue){
        return new BmpStringValue("continue");
    }

    public Object visit(BLangImportPackage bLangImportPackage) {
        return null;
    }

    public Object visit(BLangNode bLangNode) {
        return null;
    }

    public Object visit(BLangExprFunctionBody astBody) {
        return null;
    }

    public Object visit(BLangBlockFunctionBody astBody) {
        for (BLangStatement astStmt : astBody.stmts) {
            astStmt.accept(this);
        }
        return null;
    }

    public Object visit(BLangExternalFunctionBody astBody) {
        return null;
    }

    public Object visit(BLangLiteral bLangLiteral) {
        Object val = bLangLiteral.getValue();
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("value", val);

        return ValueCreator.createRecordValue(modFront, "SimpleConstExpr", mapInitialValueEntries);
    }
    
}
