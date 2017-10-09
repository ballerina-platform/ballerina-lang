/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.types.ConstrainedType;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType.BStructField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class Types {

    /**
     * @since 0.94
     */
    public enum RecordKind {
        STRUCT("struct"),
        MAP("map"),
        JSON("json");

        public String value;

        RecordKind(String value) {
            this.value = value;
        }
    }

    private static final CompilerContext.Key<Types> TYPES_KEY =
            new CompilerContext.Key<>();

    private Names names;
    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private DiagnosticLog dlog;

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new Types(context);
        }

        return types;
    }

    public Types(CompilerContext context) {
        context.put(TYPES_KEY, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public List<BType> checkTypes(BLangExpression node, List<BType> actualTypes, List<BType> expTypes) {
        List<BType> resTypes = new ArrayList<>();
        for (int i = 0; i < actualTypes.size(); i++) {
            resTypes.add(checkType(node, actualTypes.get(i), expTypes.get(i)));
        }
        return resTypes;
    }

    public BType checkType(BLangExpression node, BType actualType, BType expType) {
        return checkType(node, actualType, expType, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    public BType checkType(BLangExpression expr, BType actualType, BType expType, DiagnosticCode diagCode) {
        expr.type = checkType(expr.pos, actualType, expType, diagCode);
        if (expr.type.tag == TypeTags.ERROR) {
            return expr.type;
        }

        // TODO Set an implicit cast expression if one available
        setImplicitCastExpr(expr, actualType, expType);

        return expr.type;
    }

    public BType checkType(DiagnosticPos pos, BType actualType, BType expType, DiagnosticCode diagCode) {
        // First check whether both references points to the same object.
        if (actualType == expType) {
            return actualType;
        } else if (expType.tag == TypeTags.ERROR) {
            return expType;
        } else if (expType.tag == TypeTags.NONE) {
            return actualType;
        } else if (actualType.tag == TypeTags.ERROR) {
            return actualType;
        } else if (isAssignable(actualType, expType)) {
            return actualType;
        } else if (isImplicitCastPossible(actualType, expType)) {
            return actualType;
        }

        // TODO: Add invokable actualType compatibility check.

        // e.g. incompatible types: expected 'int', found 'string'
        dlog.error(pos, diagCode, expType, actualType);
        return symTable.errType;
    }

    public boolean isValueType(BType type) {
        return type.tag <= TypeTags.TYPE;
    }

    public boolean isAnnotationFieldType(BType type) {
        return this.isValueType(type) ||
                (type.tag == TypeTags.ANNOTATION) ||
                (type.tag == TypeTags.ARRAY && ((BArrayType) type).eType.tag == TypeTags.ANNOTATION) ||
                (type.tag == TypeTags.ARRAY && isValueType(((BArrayType) type).eType));
    }

    public boolean isAssignable(BType actualType, BType expType) {
        // First check whether both references points to the same object.
        if (actualType == expType) {
            return true;
        }

        if (expType.tag == TypeTags.ERROR) {
            return true;
        }

        // If the actual type or the rhs type is NULL, then the expected type cannot be a value type.
        if (actualType.tag == TypeTags.NULL && !isValueType(expType)) {
            return true;
        }

        // If the both type tags are equal, then perform following checks.
        if (actualType.tag == expType.tag && isValueType(actualType)) {
            return true;
        } else if (actualType.tag == expType.tag &&
                !isUserDefinedType(actualType) && !isConstrainedType(actualType) &&
                actualType.tag != TypeTags.ANNOTATION) {
            return true;
        } else if (actualType.tag == expType.tag && actualType.tag == TypeTags.ARRAY) {
            return checkArrayEquivalent(actualType, expType);
        }

        // If both types are structs then check for their equivalency
        if (checkStructEquivalency(actualType, expType)) {
            return true;
        }

        // If both types are constrained types, then check whether they are assignable
        if (isConstrainedTypeAssignable(actualType, expType)) {
            return true;
        }

        // TODO Check connector equivalency
        // TODO Check enums
        return false;
    }

    public boolean isUserDefinedType(BType type) {
        return type.tag == TypeTags.STRUCT || type.tag == TypeTags.CONNECTOR ||
                type.tag == TypeTags.ENUM || type.tag == TypeTags.ARRAY;
    }

    public boolean isConstrainedType(BType type) {
        return type.tag == TypeTags.JSON;
    }

    /**
     * Checks for the availability of an implicit cast from the actual type to the expected type.
     * 1) Lookup the symbol table for an implicit cast
     * 2) Check whether the expected type is ANY
     * 3) If both types are array types, then check implicit cast possibility of their element types.
     *
     * @param actualType actual type of an expression.
     * @param expType    expected type from an expression.
     * @return true if there exist an implicit cast from the actual type to the expected type.
     */
    public boolean isImplicitCastPossible(BType actualType, BType expType) {
        if (isConstrainedType(expType) && ((ConstrainedType) expType).getConstraint() != symTable.noType) {
            return false;
        }

        BSymbol symbol = symResolver.resolveImplicitCastOperator(actualType, expType);
        if (symbol != symTable.notFoundSymbol) {
            return true;
        }

        if (expType.tag == TypeTags.ANY) {
            return true;
        }

        return actualType.tag == TypeTags.ARRAY && expType.tag == TypeTags.ARRAY &&
                isImplicitArrayCastPossible(actualType, expType);
    }

    public boolean isImplicitArrayCastPossible(BType actualType, BType expType) {
        if (expType.tag == TypeTags.ARRAY && actualType.tag == TypeTags.ARRAY) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) expType;
            BArrayType rhsArrayType = (BArrayType) actualType;
            return isImplicitArrayCastPossible(lhrArrayType.getElementType(), rhsArrayType.getElementType());

        } else if (actualType.tag == TypeTags.ARRAY) {
            // Only the right-hand side is an array type
            // Then lhs type should 'any' type
            return expType.tag == TypeTags.ANY;

        } else if (expType.tag == TypeTags.ARRAY) {
            // Only the left-hand side is an array type
            return false;
        }

        // Now both types are not array types and they have to be equal
        if (expType == actualType) {
            // TODO Figure out this.
            return true;
        }

        // In this case, lhs type should be of type 'any' and the rhs type cannot be a value type
        return expType.tag == TypeTags.ANY && !isValueType(actualType);
    }

    public boolean checkArrayEquivalent(BType actualType, BType expType) {
        if (expType.tag == TypeTags.ARRAY && actualType.tag == TypeTags.ARRAY) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) expType;
            BArrayType rhsArrayType = (BArrayType) actualType;
            return checkArrayEquivalent(lhrArrayType.getElementType(), rhsArrayType.getElementType());

        }
        // Now one or both types are not array types and they have to be equal
        if (expType == actualType) {
            return true;
        }
        return false;
    }

    public boolean checkStructEquivalency(BType actualType, BType expType) {
        if (actualType.tag != TypeTags.STRUCT || expType.tag != TypeTags.STRUCT) {
            return false;
        }

        BStructType expStructType = (BStructType) expType;
        BStructType actualStructType = (BStructType) actualType;
        if (expStructType.fields.size() > actualStructType.fields.size()) {
            return false;
        }

        for (int i = 0; i < expStructType.fields.size(); i++) {
            BStructField expStructField = expStructType.fields.get(i);
            BStructField actualStructField = actualStructType.fields.get(i);
            if (expStructField.name.equals(actualStructField.name) &&
                    isAssignable(actualStructField.type, expStructField.type)) {
                continue;
            }
            return false;
        }

        return true;
    }

    public boolean checkStructToJSONCompatibility(BType type) {
        if (type.tag != TypeTags.STRUCT) {
            return false;
        }

        List<BStructField> fields = ((BStructType) type).fields;
        for (int i = 0; i < fields.size(); i++) {
            BType fieldType = fields.get(i).type;
            if (fieldType.tag == TypeTags.STRUCT && checkStructToJSONCompatibility(fieldType)) {
                continue;
            }

            if (fieldType.tag != TypeTags.STRUCT && (isAssignable(fieldType, symTable.jsonType)
                    || isImplicitCastPossible(fieldType, symTable.jsonType))) {
                continue;
            }

            return false;
        }

        return true;
    }

    public void setImplicitCastExpr(BLangExpression expr, BType actualType, BType expType) {
        BSymbol symbol = symResolver.resolveImplicitCastOperator(actualType, expType);
        if (symbol == symTable.notFoundSymbol) {
            return;
        }

        BCastOperatorSymbol castSymbol = (BCastOperatorSymbol) symbol;

        BLangTypeCastExpr implicitCastExpr = (BLangTypeCastExpr) TreeBuilder.createTypeCastNode();
        implicitCastExpr.pos = expr.pos;
        implicitCastExpr.expr = expr.impCastExpr == null ? expr : expr.impCastExpr;
        implicitCastExpr.type = expType;
        implicitCastExpr.types = Lists.of(expType);
        implicitCastExpr.castSymbol = castSymbol;
        expr.impCastExpr = implicitCastExpr;
    }
    

    // private methods

    private boolean isConstrainedTypeAssignable(BType actualType, BType expType) {
        if (!isConstrainedType(actualType) || !isConstrainedType(expType)) {
            return false;
        }

        if (actualType.tag != expType.tag) {
            return false;
        }

        ConstrainedType expConstrainedType = (ConstrainedType) expType;
        ConstrainedType actualConstrainedType = (ConstrainedType) actualType;

        if (((BType) expConstrainedType.getConstraint()).tag == TypeTags.NONE) {
            return true;
        }

        if (expConstrainedType.getConstraint() == actualConstrainedType.getConstraint()) {
            return true;
        }

        return false;
    }
}
