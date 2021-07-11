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
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.values.BmpStringValue;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Transform the AST to nBallerina Module.
 *
 * @since 0.980.0
 */
public class ModuleGen {

    private static final CompilerContext.Key<ModuleGen> MOD_GEN =
            new CompilerContext.Key<>();

    //Module modBir = new Module("wso2", "nballerina.bir", "0.1.0");
    Module modFront = new Module("wso2", "nballerina.front", "0.1.0");
    static BMap<BString, Object> tempVal;
    PrintStream console = System.out;

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
        astPkg.imports.forEach(impPkg -> impPkg.accept(this));
        astPkg.constants.forEach(astConst -> astConst.accept(this));
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

        BMap<BString, Object> x = ValueCreator.createRecordValue(modFront, "ReturnStmt",
                mapInitialValueEntries);
        ModuleGen.tempVal = x;
        return x;
    }

    public Object visit(BLangIf bLangIf) {
        Object expr = bLangIf.expr.accept(this);
        ArrayList<Object> ifTrue = new ArrayList<>();
        bLangIf.getBody().getStatements().forEach(stmt -> ifTrue.add(stmt.accept(this)));
        Object ifFalse = bLangIf.elseStmt.accept(this);

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("condition", expr);
        mapInitialValueEntries.put("ifTrue", ifTrue);
        mapInitialValueEntries.put("ifFalse", ifFalse);

        return ValueCreator.createRecordValue(modFront, "IfElseStmt",
                mapInitialValueEntries);
    }

    public Object visit(BLangWhile bLangWhile) {
        Object exp =  bLangWhile.expr.accept(this);
        ArrayList<Object> stmts = new ArrayList<>();
        bLangWhile.body.getStatements().forEach(stmt -> stmts.add(stmt.accept(this)));
        //ValueCreator.createArrayValue();
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("condition", exp);
        mapInitialValueEntries.put("body", stmts);

        return ValueCreator.createRecordValue(modFront, "WhileStmt",
                mapInitialValueEntries);
    }

    public Object visit(BLangBreak bLangBreak) {
        return new BmpStringValue("break");
    }

    public Object visit(BLangContinue bLangContinue) {
        return new BmpStringValue("continue");
    }

    public Object visit(BLangAssignment bLangAssignment) {
        return null;
    }

    public Object visit(BLangConstant bLangConstant) {
        return null;
    }

    public Object visit(BLangImportPackage bLangImportPackage) {

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("org", bLangImportPackage.symbol.pkgID.orgName.getValue());
        mapInitialValueEntries.put("module", bLangImportPackage.symbol.pkgID.name.getValue());
        console.println(bLangImportPackage.symbol.pkgID.orgName.getValue() + "_" +
                bLangImportPackage.symbol.pkgID.name.getValue());
        return ValueCreator.createRecordValue(modFront, "ImportDecl",
                mapInitialValueEntries);
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

    public Object visit(BLangUnaryExpr bLangUnaryExpr) {
        Object operand = bLangUnaryExpr.expr.accept(this);
        BmpStringValue op = new BmpStringValue(bLangUnaryExpr.operator.value());
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("op", op);
        mapInitialValueEntries.put("operand", operand);

        return ValueCreator.createRecordValue(modFront, "UnaryExpr", mapInitialValueEntries);
    }

    public Object visit(BLangBinaryExpr bLangBinaryExpr) {
        Object lhs = bLangBinaryExpr.lhsExpr.accept(this);
        Object rhs = bLangBinaryExpr.rhsExpr.accept(this);
        BmpStringValue op = new BmpStringValue(bLangBinaryExpr.opKind.value());
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("op", op);
        mapInitialValueEntries.put("left", lhs);
        mapInitialValueEntries.put("right", rhs);

        return ValueCreator.createRecordValue(modFront, "BinaryExpr", mapInitialValueEntries);
    }

    
}
