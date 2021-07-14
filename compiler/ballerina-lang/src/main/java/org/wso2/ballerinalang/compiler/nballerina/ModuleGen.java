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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
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

    //private static Module modBir = new Module("wso2", "nballerina.bir", "0.1.0");
    private static final Module modFront = new Module("wso2", "nballerina.front", "0.1.0");
    //private static final Module modErr = new Module("wso2", "nballerina.err", "0.1.0");
    static BMap<BString, Object> tempVal;
    private static UnionType stmtTyp;
    private static UnionType exprTyp;
    private static UnionType defTyp;
    private static UnionType typeDescTyp;
    private static UnionType conTypeDescTyp;
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
        stmtTyp = getStmtTyp();
        exprTyp = getExprTyp();
        defTyp = getDefTyp();
        conTypeDescTyp = getConstructorTypDescTyp();
        typeDescTyp = getTypeDescTyp();
    }

    public Object genMod(BLangPackage astPkg) {
        return astPkg.accept(this);
    }

    private static UnionType getStmtTyp() {
        RecordType varDeclStmt = TypeCreator.createRecordType("VarDeclStmt", modFront, 0,
                false, 0);
        RecordType assignStmt = TypeCreator.createRecordType("AssignStmt", modFront, 0,
                false, 0);
        RecordType funcCallExpr = TypeCreator.createRecordType("FunctionCallExpr", modFront, 0,
                false, 0);
        RecordType returnStmt = TypeCreator.createRecordType("ReturnStmt", modFront, 0,
                false, 0);
        RecordType ifElseStmt = TypeCreator.createRecordType("IfElseStmt", modFront, 0,
                false, 0);
        RecordType whileStmt = TypeCreator.createRecordType("WhileStmt", modFront, 0,
                false, 0);
        FiniteType breakStmt = TypeCreator.createFiniteType("BreakStmt");
        FiniteType continueStmt = TypeCreator.createFiniteType("ContinueStmt");
        return TypeCreator.createUnionType(whileStmt, returnStmt, breakStmt, continueStmt);
    }

    private static UnionType getDefTyp() {
        RecordType typeDef = TypeCreator.createRecordType("TypeDef", modFront, 0,
                false, 0);
        RecordType funcDef = TypeCreator.createRecordType("FunctionDef", modFront, 0,
                false, 0);
        return TypeCreator.createUnionType(funcDef);
    }

    private static UnionType getExprTyp() {
        RecordType simpleConstExpr = TypeCreator.createRecordType("SimpleConstExpr", modFront, 0,
                false, 0);
        RecordType binaryExpr = TypeCreator.createRecordType("BinaryExpr", modFront, 0,
                false, 0);
        RecordType unaryExpr = TypeCreator.createRecordType("UnaryExpr", modFront, 0,
                false, 0);
        RecordType functionCallExpr = TypeCreator.createRecordType("FunctionCallExpr", modFront, 0,
                false, 0);
        RecordType varRefExpr = TypeCreator.createRecordType("VarRefExpr", modFront, 0,
                false, 0);
        return TypeCreator.createUnionType(simpleConstExpr, binaryExpr, unaryExpr, functionCallExpr, varRefExpr);
    }

    private static UnionType getTypeDescTyp() {

        FiniteType builtInTypeDesc = TypeCreator.createFiniteType("BuiltInTypeDesc");
        FiniteType builtinIntSubtypeDesc = TypeCreator.createFiniteType("BuiltinIntSubtypeDesc");
        UnionType leafTypeDesc = TypeCreator.createUnionType(builtInTypeDesc, builtinIntSubtypeDesc);

        RecordType binaryTypeDesc = TypeCreator.createRecordType("BinaryTypeDesc", modFront, 0,
                false, 0);
        UnionType constructorTypeDesc = conTypeDescTyp;
        RecordType typeDescRef = TypeCreator.createRecordType("TypeDescRef", modFront, 0,
                false, 0);
        RecordType singletonTypeDesc = TypeCreator.createRecordType("SingletonTypeDesc", modFront, 0,
                false, 0);

        return TypeCreator.createUnionType(leafTypeDesc, binaryTypeDesc, constructorTypeDesc,
                typeDescRef, singletonTypeDesc);
    }

    private static UnionType getConstructorTypDescTyp() {
        RecordType listTypeDesc = TypeCreator.createRecordType("ListTypeDesc", modFront, 0,
                false, 0);
        RecordType mappingTypeDesc = TypeCreator.createRecordType("MappingTypeDesc", modFront, 0,
                false, 0);
        RecordType functionTypeDesc = TypeCreator.createRecordType("FunctionTypeDesc", modFront, 0,
                false, 0);
        RecordType errorTypeDesc = TypeCreator.createRecordType("ErrorTypeDesc", modFront, 0,
                false, 0);
        return TypeCreator.createUnionType(listTypeDesc, mappingTypeDesc, functionTypeDesc, errorTypeDesc);
    }

    public Object visit(BLangPackage astPkg) {
        RecordType impTyp = TypeCreator.createRecordType("ImportDecl", modFront, 0, false, 0);
        ArrayType impArrTyp = TypeCreator.createArrayType(impTyp);
        BArray imps = ValueCreator.createArrayValue(impArrTyp);

        ArrayType defArrTyp = TypeCreator.createArrayType(defTyp);
        BArray defs = ValueCreator.createArrayValue(defArrTyp);

        astPkg.imports.forEach(impPkg -> imps.append(impPkg.accept(this)));
        astPkg.functions.forEach(astFunc -> defs.append(astFunc.accept(this)));

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("importDecl", imps.get(0));
        mapInitialValueEntries.put("defs", defs);
        return ValueCreator.createRecordValue(modFront, "ModulePart", mapInitialValueEntries);
    }

    public Object visit(BLangFunction astFunc) {
        String name = astFunc.getName().toString();
        ArrayType paramArrTyp = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING);
        BArray paramNames = ValueCreator.createArrayValue(paramArrTyp);
        astFunc.getParameters().forEach(param -> paramNames.append(new BmpStringValue(param.getName().toString())));

        BArray body = (BArray) astFunc.body.accept(this);

        //HashMap<String, Object> mapPosValues = new HashMap<>();
        //mapPosValues.put("lineNumber", 1);
        //mapPosValues.put("indexInLine", 1);
        //BMap<BString, Object> pos = ValueCreator.createReadonlyRecordValue(modErr, "Position", mapPosValues);
        ArrayType typDescArrTyp = TypeCreator.createArrayType(typeDescTyp);
        BArray args = ValueCreator.createArrayValue(typDescArrTyp);
        args.append((Object) new BmpStringValue("boolean"));

        Map<String, Object> funcTypeDefMap = new HashMap<>();
        funcTypeDefMap.put("args", args);
        funcTypeDefMap.put("ret", args.get(0));
        BMap<BString, Object> td = ValueCreator.createRecordValue(modFront, "FunctionTypeDesc",
                funcTypeDefMap);

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("name", new BmpStringValue(name));
        mapInitialValueEntries.put("paramNames", paramNames);
        mapInitialValueEntries.put("body", body);
        mapInitialValueEntries.put("typeDesc", td);
        //mapInitialValueEntries.put("pos", pos);
        return ValueCreator.createRecordValue(modFront, "FunctionDef", mapInitialValueEntries);
    }
    
    public Object visit(BLangExpressionStmt exprStmtNode) {
        return exprStmtNode.expr.accept(this);
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
        ArrayType arrTyp = TypeCreator.createArrayType(stmtTyp);
        BArray ifTrue = ValueCreator.createArrayValue(arrTyp);
        bLangIf.getBody().getStatements().forEach(stmt -> ifTrue.append(stmt.accept(this)));
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
        ArrayType arrTyp = TypeCreator.createArrayType(stmtTyp);
        BArray stmts = ValueCreator.createArrayValue(arrTyp);
        bLangWhile.body.getStatements().forEach(stmt -> stmts.append(stmt.accept(this)));
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

    public Object visit(BLangInvocation bLangInvocation) {
        ArrayType arrTyp = TypeCreator.createArrayType(exprTyp);
        BArray exprs = ValueCreator.createArrayValue(arrTyp);
        bLangInvocation.argExprs.forEach(expr -> exprs.append(expr.accept(this)));

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("funcName", new BmpStringValue(bLangInvocation.name.getValue()));
        mapInitialValueEntries.put("args", exprs);

        return ValueCreator.createRecordValue(modFront, "FunctionCallExpr",
                mapInitialValueEntries);
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
        console.println("generic visitor");
        return null;
    }

    public Object visit(BLangExprFunctionBody astBody) {
        return null;
    }

    public Object visit(BLangBlockFunctionBody astBody) {
        ArrayType arrTyp = TypeCreator.createArrayType(stmtTyp);
        BArray stmts = ValueCreator.createArrayValue(arrTyp);
        astBody.getStatements().forEach(stmt -> stmts.append(stmt.accept(this)));
        return stmts;
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
        console.println("binary expression");
        return ValueCreator.createRecordValue(modFront, "BinaryExpr", mapInitialValueEntries);
    }
}
