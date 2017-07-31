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
package org.ballerinalang.util.semantics;

import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.CallableUnit;
import org.ballerinalang.model.CallableUnitSymbolName;
import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.NamespaceSymbolName;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.expressions.AbstractExpression;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.XMLElementLiteral;
import org.ballerinalang.model.expressions.XMLQNameExpr;
import org.ballerinalang.model.expressions.XMLTextLiteral;
import org.ballerinalang.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BJSONConstrainedType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BuiltinTypeName;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.SemanticErrors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;

/**
 * This class contains a set of utility methods used by the semantic analyzer.
 *
 * @since 0.92
 */
public class SemanticAnalyzerUtils {

    public static AssignabilityResult performAssignabilityCheck(BType lhsType, Expression rhsExpr) {
        AssignabilityResult assignabilityResult = new AssignabilityResult();
        BType rhsType = rhsExpr.getType();
        if (lhsType.equals(rhsType)) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        if (rhsType.equals(BTypes.typeNull) && !BTypes.isValueType(lhsType)) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        if ((rhsType instanceof BJSONConstrainedType) && (lhsType.equals(BTypes.typeJSON))) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        // Now check whether an implicit cast is available;
        TypeCastExpression implicitCastExpr = checkWideningPossible(lhsType, rhsExpr);
        if (implicitCastExpr != null) {
            assignabilityResult.assignable = true;
            assignabilityResult.expression = implicitCastExpr;
            return assignabilityResult;
        }

        // Now check whether left-hand side type is 'any', then an implicit cast is possible;
        if (isImplicitCastPossible(lhsType, rhsType)) {
            implicitCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    null, rhsExpr, lhsType);
            implicitCastExpr.setOpcode(InstructionCodes.NOP);

            assignabilityResult.assignable = true;
            assignabilityResult.expression = implicitCastExpr;
            return assignabilityResult;
        }

        if (lhsType.equals(BTypes.typeFloat) && rhsType.equals(BTypes.typeInt) && rhsExpr instanceof BasicLiteral) {
            BasicLiteral newExpr = new BasicLiteral(rhsExpr.getNodeLocation(), rhsExpr.getWhiteSpaceDescriptor(),
                    new BuiltinTypeName(TypeConstants.FLOAT_TNAME), new BFloat(((BasicLiteral) rhsExpr)
                    .getBValue().intValue()));
//            visitSingleValueExpr(newExpr); //TODO uncomment once method is moved here
            assignabilityResult.assignable = true;
            assignabilityResult.expression = newExpr;
            return assignabilityResult;
        }

        // SemanticAnalyzer 3913-3931

        return assignabilityResult;
    }

    public static boolean isImplicitCastPossible(BType lhsType, BType rhsType) {
        if (lhsType.equals(BTypes.typeAny)) {
            return true;
        }

        // 2) Check whether both types are array types
        if (lhsType.getTag() == TypeTags.ARRAY_TAG || rhsType.getTag() == TypeTags.ARRAY_TAG) {
            return isImplicitArrayCastPossible(lhsType, rhsType);
        }

        return false;
    }

    public static boolean isImplicitArrayCastPossible(BType lhsType, BType rhsType) {
        if (lhsType.getTag() == TypeTags.ARRAY_TAG && rhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) lhsType;
            BArrayType rhsArrayType = (BArrayType) rhsType;
            return isImplicitArrayCastPossible(lhrArrayType.getElementType(), rhsArrayType.getElementType());

        } else if (rhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Only the right-hand side is an array type
            // Then lhs type should 'any' type
            return lhsType.equals(BTypes.typeAny);

        } else if (lhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Only the left-hand side is an array type
            return false;
        }

        // Now both types are not array types
        if (lhsType.equals(rhsType)) {
            return true;
        }

        // In this case, lhs type should be of type 'any' and the rhs type cannot be a value type
        return lhsType.getTag() == BTypes.typeAny.getTag() && !BTypes.isValueType(rhsType);
    }

    public static TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr) {
        TypeCastExpression typeCastExpr = null;
        BType rhsType = rhsExpr.getType();

        TypeEdge typeEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rhsType, lhsType, null);
        if (typeEdge != null) {
            typeCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    rhsExpr.getWhiteSpaceDescriptor(), rhsExpr, lhsType);
            typeCastExpr.setOpcode(typeEdge.getOpcode());
        }
        return typeCastExpr;
    }

    public static boolean isAssignableTo(BType lhsType, BType rhsType) {
        if (lhsType == BTypes.typeAny) {
            return true;
        }

        if (rhsType == BTypes.typeNull && !BTypes.isValueType(lhsType)) {
            return true;
        }

        if (lhsType == BTypes.typeJSON && rhsType.getTag() == TypeTags.C_JSON_TAG) {
            return true;
        }

        return lhsType == rhsType || lhsType.equals(rhsType);
    }

    public static void visitSingleValueExpr(Expression expr, ExpressionSemanticsAnalyzer expressionAnalyzer) {
        expr.accept(expressionAnalyzer);
        if (expr.isMultiReturnExpr()) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) expr;
            String nameWithPkgName = (funcIExpr.getPackageName() != null)
                    ? funcIExpr.getPackageName() + ":" + funcIExpr.getName() : funcIExpr.getName();
            BLangExceptionHelper.throwSemanticError(expr, SemanticErrors.MULTIPLE_VALUE_IN_SINGLE_VALUE_CONTEXT,
                    nameWithPkgName);
        }
    }


    /**
     * Get the XML namespaces that are visible to to the current scope.
     *
     * @param location Source location of the ballerina file
     * @param expressionAnalyzer Semantic analyzer for expression visits
     * @return XML namespaces that are visible to the current scope, as a map
     */
    public static Map<String, Expression> getNamespaceInScope(NodeLocation location, SymbolScope scope,
            ExpressionSemanticsAnalyzer expressionAnalyzer) {
        Map<String, Expression> namespaces = new HashMap<String, Expression>();
        while (true) {
            for (Entry<SymbolName, BLangSymbol> symbols : scope.getSymbolMap().entrySet()) {
                SymbolName symbolName = symbols.getKey();
                if (!(symbolName instanceof NamespaceSymbolName)) {
                    continue;
                }

                NamespaceDeclaration namespaceDecl = (NamespaceDeclaration) symbols.getValue();
                if (!namespaces.containsKey(namespaceDecl.getPrefix())
                        && !namespaces.containsValue(namespaceDecl.getNamespaceUri())) {

                    BasicLiteral namespaceUriLiteral = new BasicLiteral(location, null,
                            new BuiltinTypeName(TypeConstants.STRING_TNAME),
                            new BString(namespaceDecl.getNamespaceUri()));
                    namespaceUriLiteral.accept(expressionAnalyzer);
                    namespaces.put(namespaceDecl.getPrefix(), namespaceUriLiteral);
                }
            }

            if (scope instanceof BLangPackage) {
                break;
            }
            scope = scope.getEnclosingScope();
        }

        return namespaces;
    }

    /**
     * Create and return an XML concatenation expression using using the provided expressions.
     * Expressions can only be either XML type or string type. All the string type expressions
     * will be converted to XML text literals ({@link XMLTextLiteral}).
     *
     * @param items Expressions to create concatenating expression.
     * @return XML concatenating expression
     */
    public static Expression getXMLConcatExpression(Expression[] items) {
        if (items.length == 0) {
            return null;
        }

        Expression concatExpr = null;
        for (int i = 0; i < items.length; i++) {
            Expression currentItem = items[i];
            if (currentItem.getType() == BTypes.typeString) {
                currentItem = new XMLTextLiteral(currentItem.getNodeLocation(), currentItem.getWhiteSpaceDescriptor(),
                        currentItem);
                items[0] = currentItem;
            }

            if (concatExpr == null) {
                concatExpr = currentItem;
                continue;
            }

            concatExpr = new AddExpression(currentItem.getNodeLocation(), currentItem.getWhiteSpaceDescriptor(),
                    concatExpr, currentItem);
            concatExpr.setType(BTypes.typeXML);
        }

        return concatExpr;
    }

    public static void validateXMLQname(XMLQNameExpr qname, Map<String, Expression> namespaces,
            ExpressionSemanticsAnalyzer expressionAnalyzer) {
        qname.setType(BTypes.typeString);
        String prefix = qname.getPrefix();

        if (prefix.isEmpty()) {
            BasicLiteral emptyNsUriLiteral = new BasicLiteral(qname.getNodeLocation(), null,
                    new BuiltinTypeName(TypeConstants.STRING_TNAME), new BString(XMLConstants.NULL_NS_URI));
            emptyNsUriLiteral.accept(expressionAnalyzer);
            qname.setNamepsaceUri(emptyNsUriLiteral);
            return;
        }

        if (namespaces.containsKey(qname.getPrefix())) {
            Expression namespaceUri = namespaces.get(qname.getPrefix());
            qname.setNamepsaceUri(namespaceUri);
        } else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            BLangExceptionHelper.throwSemanticError(qname, SemanticErrors.INVALID_NAMESPACE_PREFIX, prefix);
        } else {
            BLangExceptionHelper.throwSemanticError(qname, SemanticErrors.UNDEFINED_NAMESPACE, qname.getPrefix());
        }
    }

    public static void validateXMLLiteralAttributes(List<KeyValueExpr> attributes, Map<String, Expression> namespaces,
            ExpressionSemanticsAnalyzer expressionAnalyzer) {
        // Validate attributes
        for (KeyValueExpr attribute : attributes) {
            Expression attrNameExpr = attribute.getKeyExpr();

            if (attrNameExpr instanceof XMLQNameExpr) {
                XMLQNameExpr attrQNameRefExpr = (XMLQNameExpr) attrNameExpr;
                attrQNameRefExpr.isUsedInXML();
                validateXMLQname(attrQNameRefExpr, namespaces, expressionAnalyzer);
            } else {
                attrNameExpr.accept(expressionAnalyzer);
                if (attrNameExpr.getType() != BTypes.typeString) {
                    attrNameExpr = createImplicitStringConversionExpr(attrNameExpr, attrNameExpr.getType(),
                            expressionAnalyzer);
                    attribute.setKeyExpr(attrNameExpr);
                }
            }

            Expression attrValueExpr = attribute.getValueExpr();
            attrValueExpr.accept(expressionAnalyzer);
            if (attrValueExpr.getType() != BTypes.typeString) {
                attrValueExpr =
                        createImplicitStringConversionExpr(attrValueExpr, attrValueExpr.getType(), expressionAnalyzer);
                attribute.setValueExpr(attrValueExpr);
            }
        }                                           
    }

    public static void validateXMLLiteralEndTag(XMLElementLiteral xmlElementLiteral,
            ExpressionSemanticsAnalyzer expressionAnalyzer) {
        Expression startTagName = xmlElementLiteral.getStartTagName();
        Expression endTagName = xmlElementLiteral.getEndTagName();

        // Compare start and end tags
        if (endTagName != null) {
            if (startTagName instanceof XMLQNameExpr && endTagName instanceof XMLQNameExpr) {
                XMLQNameExpr startName = (XMLQNameExpr) startTagName;
                XMLQNameExpr endName = (XMLQNameExpr) endTagName;
                if (!startName.getPrefix().equals(endName.getPrefix())
                        || !startName.getLocalname().equals(endName.getLocalname())) {
                    BLangExceptionHelper.throwSemanticError(endTagName, SemanticErrors.XML_TAGS_MISMATCH);
                }
            }

            if (((startTagName instanceof XMLQNameExpr) && !(endTagName instanceof XMLQNameExpr))
                    || (!(startTagName instanceof XMLQNameExpr) && (endTagName instanceof XMLQNameExpr))) {
                BLangExceptionHelper.throwSemanticError(endTagName, SemanticErrors.XML_TAGS_MISMATCH);
            }

            if (endTagName instanceof XMLQNameExpr) {
                validateXMLQname((XMLQNameExpr) endTagName, xmlElementLiteral.getNamespaces(), expressionAnalyzer);
            } else {
                endTagName.accept(expressionAnalyzer);
            }

            if (endTagName.getType() != BTypes.typeString) {
                endTagName = createImplicitStringConversionExpr(endTagName, endTagName.getType(), expressionAnalyzer);
                xmlElementLiteral.setEndTagName(endTagName);
            }
        }
    }

    public static Expression getImplicitConversionExpr(Expression sExpr, BType sType, BType tType,
            ExpressionSemanticsAnalyzer expressionAnalyzer) {
        TypeEdge newEdge;
        newEdge = TypeLattice.getTransformLattice().getEdgeFromTypes(sType, tType, null);
        if (newEdge != null) {
            TypeConversionExpr newExpr =
                    new TypeConversionExpr(sExpr.getNodeLocation(), sExpr.getWhiteSpaceDescriptor(), sExpr, tType);
            newExpr.setOpcode(newEdge.getOpcode());
            newExpr.accept(expressionAnalyzer);
            return newExpr;
        }

        return null;
    }

    public static Expression createImplicitStringConversionExpr(Expression sExpr, BType sType,
            ExpressionSemanticsAnalyzer expressionAnalyzer) {
        Expression conversionExpr = getImplicitConversionExpr(sExpr, sType, BTypes.typeString, expressionAnalyzer);
        if (conversionExpr == null) {
            BLangExceptionHelper.throwSemanticError(sExpr, SemanticErrors.INCOMPATIBLE_TYPES, BTypes.typeString, sType);
        }
        return conversionExpr;
    }

    /**
     * Helper method to match the callable unit with invocation (check whether parameters map, do cast if applicable).
     *
     * @param callableIExpr  invocation expression
     * @param symbolName     callable symbol name
     * @param callableSymbol matching symbol
     * @param currentScope   current symbol scope
     * @return callableSymbol  matching symbol
     */
    public static BLangSymbol matchAndUpdateArguments(AbstractExpression callableIExpr,
                                                      CallableUnitSymbolName symbolName,
                                                      BLangSymbol callableSymbol,
                                                      SymbolScope currentScope) {
        if (callableSymbol == null) {
            return null;
        }

        Expression[] argExprs = ((CallableUnitInvocationExpr) callableIExpr).getArgExprs();
        Expression[] updatedArgExprs = new Expression[argExprs.length];

        CallableUnitSymbolName funcSymName = (CallableUnitSymbolName) callableSymbol.getSymbolName();
        if (!funcSymName.isNameAndParamCountMatch(symbolName)) {
            return null;
        }

        boolean implicitCastPossible = true;

        if (callableSymbol instanceof NativeUnitProxy) {
            NativeUnit nativeUnit = ((NativeUnitProxy) callableSymbol).load();
            for (int i = 0; i < argExprs.length; i++) {
                Expression argExpr = argExprs[i];
                updatedArgExprs[i] = argExpr;
                SimpleTypeName simpleTypeName = nativeUnit.getArgumentTypeNames()[i];
                BType lhsType = BTypes.resolveType(simpleTypeName, currentScope, callableIExpr.getNodeLocation());

                AssignabilityResult result = performAssignabilityCheck(lhsType, argExpr);
                if (result.expression != null) {
                    updatedArgExprs[i] = result.expression;
                } else if (!result.assignable) {
                    // TODO do we need to throw an error here?
                    implicitCastPossible = false;
                    break;
                }
            }
        } else {
            for (int i = 0; i < argExprs.length; i++) {
                Expression argExpr = argExprs[i];
                updatedArgExprs[i] = argExpr;
                BType lhsType = ((CallableUnit) callableSymbol).getParameterDefs()[i].getType();

                AssignabilityResult result = performAssignabilityCheck(lhsType, argExpr);
                if (result.expression != null) {
                    updatedArgExprs[i] = result.expression;
                } else if (!result.assignable) {
                    // TODO do we need to throw an error here?
                    implicitCastPossible = false;
                    break;
                }
            }
        }

        if (!implicitCastPossible) {
            return null;
        }

        for (int i = 0; i < updatedArgExprs.length; i++) {
            ((CallableUnitInvocationExpr) callableIExpr).getArgExprs()[i] = updatedArgExprs[i];
        }
        return callableSymbol;
    }

    public static void assignVariableRefTypes(Expression[] expr, BType[] returnTypes) {
        for (int i = 0; i < expr.length; i++) {
            if (expr[i] instanceof SimpleVarRefExpr && ((SimpleVarRefExpr) expr[i]).getVarName().equals("_")) {
                continue;
            }
            ((SimpleVarRefExpr) expr[i]).getVariableDef().setType(returnTypes[i]);
        }
    }
}
