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
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.BmpStringValue;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;


/**
 * Transform the AST to nBallerina Module.
 *
 * @since 0.980.0
 */
public class ModuleGen {

    private static final CompilerContext.Key<ModuleGen> MOD_GEN =
            new CompilerContext.Key<>();

    static final Module MODFRONT = new Module("wso2", "nballerina.front", "0.1.0");
    static final Module MODBIR = new Module("wso2", "nballerina.bir", "0.1.0");
    static final Module MODTYPES = new Module("wso2", "nballerina.types", "0.1.0");
    static final Module MODERROR = new Module("wso2", "nballerina.err", "0.1.0");
    private static UnionType stmtTyp;
    private static UnionType exprTyp;
    private static UnionType defTyp;
    private static UnionType typeDescTyp;
    private static UnionType conTypeDescTyp;
    private static ArrayType stmtArrTyp;
    private static ArrayType typDescArrTyp;
    private static BasicBlock curBlock;
    private static BasicBlock nextBlock;
    private static BasicBlock endBlock;
    private static BasicBlock bb;
    private static JNModule jnmod;

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
        stmtArrTyp = TypeCreator.createArrayType(stmtTyp);
        typDescArrTyp = TypeCreator.createArrayType(typeDescTyp);
    }

    public JNModule genMod(BLangPackage astPkg) {
        jnmod = new JNModule();
        jnmod.moduleId.organization = "wso2";
        jnmod.moduleId.names.add("baltest");

        for (BLangFunction func : astPkg.functions) {
            boolean acc = func.getFlags().contains(Flag.PUBLIC);
            String name = func.getName().toString();
            TypeKind ret = func.returnTypeNode.getBType().getKind();
            Position position = new Position(func.getPosition().lineRange().startLine().line() + 1,
                    func.getPosition().textRange().startOffset() + 9);
            FunctionDefn funcDefn = new FunctionDefn(acc, name, ret, position);
            func.getParameters().forEach(param -> {
                funcDefn.signature.paramTypes.add(param.getBType().getKind());
            });
            jnmod.functionDefns.put(name, funcDefn);
        }
        for (BLangFunction func : astPkg.functions) {
            FunctionCode fcode = new FunctionCode();
            BasicBlock startBlock = fcode.createBasicBlock();
            func.getParameters().forEach(param -> {
                fcode.createRegister(param.getBType().getKind(), param.getName().toString());
            });
            curBlock = startBlock;
            ((BLangBlockFunctionBody) func.body).getStatements().forEach(stmt -> codeGenStmt(stmt, fcode));
            endBlock = curBlock;
            jnmod.code.add(fcode);
        }
        return jnmod;
        //return astPkg.accept(this);
    }

    void codeGenStmt(BLangStatement stmt, FunctionCode code) {
        if (stmt instanceof BLangReturn) {
            BLangReturn retStmt = (BLangReturn) stmt;
            Operand operand = codeGenExpr(retStmt.expr, code);
            nextBlock.insns.add(new RetInsn(operand));
            curBlock = null;
        } else if (stmt instanceof BLangSimpleVariableDef) {

            curBlock = null;
        } else if (stmt instanceof BLangExpressionStmt) {
            codeGenExpr(((BLangExpressionStmt) stmt).getExpression(), code);
        }
    }

    Operand codeGenExpr(BLangExpression expr, FunctionCode code) {
        if (expr instanceof BLangLiteral) {
            nextBlock = curBlock;
            Operand op = new Operand(false);
            op.value = ((BLangLiteral) expr).getValue();
            return op;
        } else if (expr instanceof BLangSimpleVarRef) {
            nextBlock = curBlock;
            String name = ((BLangSimpleVarRef) expr).variableName.getValue();
            Operand op = new Operand(true);
            if (!code.registers.containsKey(name)) {
                throw new BallerinaException("variable '" + name + "' not found");
            }
            op.register = code.registers.get(name);
            return op;
        } else if (expr instanceof BLangUnaryExpr) {
            BLangUnaryExpr unexpr = (BLangUnaryExpr) expr;
            Operand operand = codeGenExpr(unexpr.expr, code);
            Operand result = new Operand(true);
            OperatorKind op = unexpr.operator;
            switch (op) {
                case NOT:
                    Register reg1 = code.createRegister(TypeKind.BOOLEAN, null);
                    curBlock.insns.add(new BoolNotInsn(operand.register, reg1));
                    result.register = reg1;
                    return result;
                case SUB:
                    Register reg2 = code.createRegister(TypeKind.INT, null);
                    IntArithmeticBinaryInsn ins = new IntArithmeticBinaryInsn();
                    ins.op = "-";
                    ins.result = reg2;
                    Operand zeroOp = new Operand(false);
                    zeroOp.value = 0;
                    ins.operands[0] = zeroOp;
                    ins.operands[1] = operand;
                    curBlock.insns.add(ins);
                    result.register = reg2;
                    return result;
            }
        } else if (expr instanceof BLangBinaryExpr) {
            BLangBinaryExpr bexpr = (BLangBinaryExpr) expr;
            Operand lhs = codeGenExpr(bexpr.lhsExpr, code);
            Operand rhs = codeGenExpr(bexpr.rhsExpr, code);
            Operand result = new Operand(true);
            OperatorKind op = bexpr.opKind;
            switch (op) {
                case ADD: case SUB: case MUL: case DIV: case MOD:
                    Register reg = code.createRegister(TypeKind.INT, null);
                    IntArithmeticBinaryInsn ins = new IntArithmeticBinaryInsn();
                    ins.op = op.toString();
                    ins.result = reg;
                    ins.operands[0] = lhs;
                    ins.operands[1] = rhs;
                    ins.position = new Position(bexpr.getPosition().lineRange().startLine().line() + 1,
                            bexpr.getPosition().textRange().startOffset());
                    curBlock.insns.add(ins);
                    result.register = reg;
                    return result;
            }
        } else if (expr instanceof BLangInvocation) {
            BLangInvocation funcCall = (BLangInvocation) expr;
            return codegenFunctionCall(funcCall, code);
        }
        return null;
    }

    Operand codegenFunctionCall(BLangInvocation funcCall, FunctionCode code) {
        FunctionRef ref;
        Operand result = new Operand(true);
        if (funcCall.pkgAlias.getValue().equals("")) {
            FunctionDefn def = jnmod.functionDefns.get(funcCall.getName().getValue());
            ref  = new FunctionRef(def.symbol, def.signature);
        } else {
            FunctionSignature signature = new FunctionSignature();
            signature.returnType = funcCall.getBType().getKind();
            for (BLangExpression arg: funcCall.requiredArgs) {
                signature.paramTypes.add(arg.getBType().getKind());
            }
            ExternalSymbol symbol = new ExternalSymbol(jnmod.moduleId, funcCall.getName().getValue());
            ref = new FunctionRef(symbol, signature);
        }

        ArrayList<Operand> args = new ArrayList<>();
        funcCall.getArgumentExpressions().forEach(arg -> args.add(codeGenExpr((BLangExpression) arg, code)));
        Register reg = code.createRegister(ref.signature.returnType , null);
        CallInsn callInsn = new CallInsn(reg, ref, args);
        curBlock.insns.add(callInsn);
        result.register = reg;
        return result;
    }

    static long convertSimpleSemType(TypeKind typeKind) {
        switch (typeKind) {
            case INT:
                return 4L;
            case BOOLEAN:
                return 2L;
            case NIL:
                return 1L;
            default:
                throw new BallerinaException("Semtype not implemented for type");
        }

    }

    private static UnionType getStmtTyp() {
        RecordType varDeclStmt = TypeCreator.createRecordType("VarDeclStmt", MODFRONT, 1,
                true, 0);
        RecordType assignStmt = TypeCreator.createRecordType("AssignStmt", MODFRONT, 1,
                true, 6);
        RecordType funcCallExpr = TypeCreator.createRecordType("FunctionCallExpr", MODFRONT, 1,
                true, 6);
        RecordType returnStmt = TypeCreator.createRecordType("ReturnStmt", MODFRONT, 1,
                true, 6);
        RecordType ifElseStmt = TypeCreator.createRecordType("IfElseStmt", MODFRONT, 1,
                true, 0);
        RecordType whileStmt = TypeCreator.createRecordType("WhileStmt", MODFRONT, 1,
                true, 0);
        LinkedHashSet breakSet = new LinkedHashSet();
        breakSet.add(new BmpStringValue("break"));
        FiniteType breakStmt = TypeCreator.createFiniteType("BreakStmt", breakSet, 6);
        LinkedHashSet continueSet = new LinkedHashSet();
        continueSet.add(new BmpStringValue("continue"));
        FiniteType continueStmt = TypeCreator.createFiniteType("ContinueStmt", continueSet, 6);
        return TypeCreator.createUnionType(varDeclStmt, assignStmt, funcCallExpr, returnStmt, ifElseStmt, whileStmt,
                breakStmt, continueStmt);
    }

    private static UnionType getDefTyp() {
        RecordType typeDef = TypeCreator.createRecordType("TypeDef", MODFRONT, 1,
                true, 0);
        RecordType funcDef = TypeCreator.createRecordType("FunctionDef", MODFRONT, 1,
                true, 0);
        return TypeCreator.createUnionType(funcDef, typeDef);
    }

    private static UnionType getExprTyp() {
        RecordType simpleConstExpr = TypeCreator.createRecordType("SimpleConstExpr", MODFRONT, 1,
                true, 6);
        RecordType binaryExpr = TypeCreator.createRecordType("BinaryExpr", MODFRONT, 1,
                true, 6);
        RecordType unaryExpr = TypeCreator.createRecordType("UnaryExpr", MODFRONT, 1,
                true, 6);
        RecordType functionCallExpr = TypeCreator.createRecordType("FunctionCallExpr", MODFRONT, 1,
                true, 6);
        RecordType varRefExpr = TypeCreator.createRecordType("VarRefExpr", MODFRONT, 1,
                true, 6);
        return TypeCreator.createUnionType(simpleConstExpr, binaryExpr, unaryExpr, functionCallExpr, varRefExpr);
    }

    private static UnionType getTypeDescTyp() {
        LinkedHashSet typeSet = new LinkedHashSet();
        typeSet.addAll(Arrays.asList(new BmpStringValue("any"), new BmpStringValue("boolean"),
                new BmpStringValue("decimal"), new BmpStringValue("error"), new BmpStringValue("float"),
                new BmpStringValue("handle"), new BmpStringValue("int"), new BmpStringValue("json"),
                new BmpStringValue("never"), new BmpStringValue("readonly"), new BmpStringValue("string"),
                new BmpStringValue("typedesc"), new BmpStringValue("xml"), new BmpStringValue("()")));
        FiniteType builtInTypeDesc = TypeCreator.createFiniteType("BuiltInTypeDesc", typeSet, 6);
        FiniteType builtinIntSubtypeDesc = TypeCreator.createFiniteType("BuiltinIntSubtypeDesc");
        UnionType leafTypeDesc = TypeCreator.createUnionType(builtInTypeDesc, builtinIntSubtypeDesc);

        RecordType binaryTypeDesc = TypeCreator.createRecordType("BinaryTypeDesc", MODFRONT, 1,
                true, 6);
        UnionType constructorTypeDesc = conTypeDescTyp;
        RecordType typeDescRef = TypeCreator.createRecordType("TypeDescRef", MODFRONT, 1,
                true, 6);
        RecordType singletonTypeDesc = TypeCreator.createRecordType("SingletonTypeDesc", MODFRONT, 1,
                true, 6);

        return TypeCreator.createUnionType(leafTypeDesc, binaryTypeDesc, constructorTypeDesc,
                typeDescRef, singletonTypeDesc);
    }

    private static UnionType getConstructorTypDescTyp() {
        RecordType listTypeDesc = TypeCreator.createRecordType("ListTypeDesc", MODFRONT, 1,
                true, 0);
        RecordType mappingTypeDesc = TypeCreator.createRecordType("MappingTypeDesc", MODFRONT, 1,
                true, 0);
        RecordType functionTypeDesc = TypeCreator.createRecordType("FunctionTypeDesc", MODFRONT, 1,
                true, 0);
        RecordType errorTypeDesc = TypeCreator.createRecordType("ErrorTypeDesc", MODFRONT, 1,
                true, 0);
        return TypeCreator.createUnionType(listTypeDesc, mappingTypeDesc, functionTypeDesc, errorTypeDesc);
    }

    public Object visit(BLangPackage astPkg) {
        RecordType impTyp = TypeCreator.createRecordType("ImportDecl", MODFRONT, 1, false, 6);
        ArrayType impArrTyp = TypeCreator.createArrayType(impTyp);
        BArray imps = ValueCreator.createArrayValue(impArrTyp);

        ArrayType defArrTyp = TypeCreator.createArrayType(defTyp);
        BArray defs = ValueCreator.createArrayValue(defArrTyp);

        astPkg.imports.forEach(impPkg -> imps.append(impPkg.accept(this)));
        astPkg.functions.forEach(astFunc -> defs.append(astFunc.accept(this)));
        astPkg.typeDefinitions.forEach(astTypDef -> defs.append(astTypDef.accept(this)));

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        if (imps.size() > 0) {
            mapInitialValueEntries.put("importDecl", imps.get(0));
        }
        mapInitialValueEntries.put("defs", defs);
        return ValueCreator.createRecordValue(MODFRONT, "ModulePart", mapInitialValueEntries);
    }

    public Object visit(BLangFunction astFunc) {
        String name = astFunc.getName().toString();
        ArrayType paramArrTyp = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING);
        BArray paramNames = ValueCreator.createArrayValue(paramArrTyp);
        astFunc.getParameters().forEach(param -> paramNames.append(new BmpStringValue(param.getName().toString())));

        BArray body = (BArray) astFunc.body.accept(this);
        boolean accessor = astFunc.getFlags().contains(Flag.PUBLIC);
        int lineNumber = astFunc.pos.lineRange().startLine().line() + 1;
        int indexInLine = astFunc.pos.textRange().startOffset() + 9;

        BArray args = ValueCreator.createArrayValue(typDescArrTyp);
        BArray rets = ValueCreator.createArrayValue(typDescArrTyp);
        astFunc.getParameters().forEach(param -> {
            String arg = param.getBType().getKind().typeName();
            if (arg.equals(TypeKind.NIL.typeName())) {
                arg = "()";
            }
            args.append(new BmpStringValue(arg)); });
        String ret = astFunc.getReturnTypeNode().getBType().getKind().typeName();
        if (ret.equals(TypeKind.NIL.typeName())) {
            ret = "()";
        }
        rets.append(new BmpStringValue(ret));

        Map<String, Object> funcTypeDefMap = new HashMap<>();
        funcTypeDefMap.put("args", args);
        funcTypeDefMap.put("ret", rets.get(0));
        BMap<BString, Object> td = ValueCreator.createRecordValue(MODFRONT, "FunctionTypeDesc",
                funcTypeDefMap);

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        if (accessor) {
            indexInLine += 7;
            mapInitialValueEntries.put("vis", new BmpStringValue("public"));
        }
        mapInitialValueEntries.put("name", new BmpStringValue(name));
        mapInitialValueEntries.put("paramNames", paramNames);
        mapInitialValueEntries.put("body", body);
        mapInitialValueEntries.put("typeDesc", td);
        mapInitialValueEntries.put("tempLineNumber", lineNumber);
        mapInitialValueEntries.put("tempIndexInLine", indexInLine);
        return ValueCreator.createRecordValue(MODFRONT, "FunctionDef", mapInitialValueEntries);
    }
    
    public Object visit(BLangExpressionStmt exprStmtNode) {
        return exprStmtNode.expr.accept(this);
    }

    public Object visit(BLangTypeDefinition typeDef) {
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("name", new BmpStringValue(typeDef.name.getValue()));

        return null;
    }
    public Object visit(BLangStatementExpression statementExpression) {
        return statementExpression.stmt.accept(this);
    }

    public BMap<BString, Object> visit(BLangReturn astReturnStmt) {
        Object exp = astReturnStmt.expr.accept(this);
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("returnExpr", exp);

        return ValueCreator.createRecordValue(MODFRONT, "ReturnStmt", mapInitialValueEntries);
    }

    public Object visit(BLangIf bLangIf) {
        Object expr = bLangIf.expr.accept(this);
        BArray ifTrue = ValueCreator.createArrayValue(stmtArrTyp);
        BArray ifFalse = ValueCreator.createArrayValue(stmtArrTyp);
        bLangIf.getBody().getStatements().forEach(stmt -> ifTrue.append(stmt.accept(this)));
        ((BLangBlockStmt) bLangIf.elseStmt).getStatements().forEach(stmt -> {
            ifFalse.append(stmt.accept(this)); });
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("condition", expr);
        mapInitialValueEntries.put("ifTrue", ifTrue);
        mapInitialValueEntries.put("ifFalse", ifFalse);

        return ValueCreator.createRecordValue(MODFRONT, "IfElseStmt",
                mapInitialValueEntries);
    }

    public Object visit(BLangWhile bLangWhile) {
        Object exp =  bLangWhile.expr.accept(this);
        BArray stmts = ValueCreator.createArrayValue(stmtArrTyp);
        bLangWhile.body.getStatements().forEach(stmt -> stmts.append(stmt.accept(this)));
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("condition", exp);
        mapInitialValueEntries.put("body", stmts);

        return ValueCreator.createRecordValue(MODFRONT, "WhileStmt",
                mapInitialValueEntries);
    }

    public Object visit(BLangBreak bLangBreak) {
        return new BmpStringValue("break");
    }

    public Object visit(BLangContinue bLangContinue) {
        return new BmpStringValue("continue");
    }

    public Object visit(BLangInvocation bLangInvocation) {
        ArrayType arrTyp = TypeCreator.createArrayType(exprTyp);
        BArray exprs = ValueCreator.createArrayValue(arrTyp);
        bLangInvocation.argExprs.forEach(expr -> exprs.append(expr.accept(this)));

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        String prefix = bLangInvocation.pkgAlias.getValue();
        if (!prefix.equals("")) {
            mapInitialValueEntries.put("prefix", new BmpStringValue(prefix));
        }
        mapInitialValueEntries.put("funcName", new BmpStringValue(bLangInvocation.name.getValue()));
        mapInitialValueEntries.put("args", exprs);

        return ValueCreator.createRecordValue(MODFRONT, "FunctionCallExpr",
                mapInitialValueEntries);
    }

    public Object visit(BLangSimpleVariableDef varDec) {
        String varName = varDec.var.symbol.name.value;
        Object initExpr = varDec.var.expr.accept(this);
        BArray tds = ValueCreator.createArrayValue(typDescArrTyp);
        tds.append(new BmpStringValue(varDec.var.symbol.type.getKind().typeName()));

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("varName", new BmpStringValue(varName));
        mapInitialValueEntries.put("initExpr", initExpr);
        mapInitialValueEntries.put("td", tds.get(0));
        return ValueCreator.createRecordValue(MODFRONT, "VarDeclStmt", mapInitialValueEntries);
    }

    public Object visit(BLangSimpleVarRef varRef) {
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("varName", new BmpStringValue(varRef.variableName.getValue()));
        return ValueCreator.createRecordValue(MODFRONT, "VarRefExpr", mapInitialValueEntries);
    }

    public Object visit(BLangAssignment bLangAssignment) {
        return null;
    }

    public Object visit(BLangImportPackage bLangImportPackage) {

        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("org", bLangImportPackage.symbol.pkgID.orgName.getValue());
        mapInitialValueEntries.put("module", bLangImportPackage.symbol.pkgID.name.getValue());

        return ValueCreator.createRecordValue(MODFRONT, "ImportDecl",
                mapInitialValueEntries);
    }

    public Object visit(BLangNode bLangNode) {
        console.println("generic visitor" + bLangNode.toString());
        return null;
    }

    public Object visit(BLangBlockFunctionBody astBody) {
        BArray stmts = ValueCreator.createArrayValue(stmtArrTyp);
        astBody.getStatements().forEach(stmt -> {
            Object x = stmt.accept(this);
            stmts.append(x);
        });
        return stmts;
    }

    public Object visit(BLangLiteral bLangLiteral) {
        Object val = bLangLiteral.getValue();
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("value", val);

        return ValueCreator.createRecordValue(MODFRONT, "SimpleConstExpr", mapInitialValueEntries);
    }

    public Object visit(BLangUnaryExpr bLangUnaryExpr) {
        Object operand = bLangUnaryExpr.expr.accept(this);
        BmpStringValue op = new BmpStringValue(bLangUnaryExpr.operator.value());
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("op", op);
        mapInitialValueEntries.put("operand", operand);

        return ValueCreator.createRecordValue(MODFRONT, "UnaryExpr", mapInitialValueEntries);
    }

    public Object visit(BLangBinaryExpr bLangBinaryExpr) {
        Object lhs = bLangBinaryExpr.lhsExpr.accept(this);
        Object rhs = bLangBinaryExpr.rhsExpr.accept(this);
        BmpStringValue op = new BmpStringValue(bLangBinaryExpr.opKind.value());
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("op", op);
        mapInitialValueEntries.put("left", lhs);
        mapInitialValueEntries.put("right", rhs);
        return ValueCreator.createRecordValue(MODFRONT, "BinaryExpr", mapInitialValueEntries);
    }
}
