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

import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.NamespaceSymbolName;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.ExpressionVisitor;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LambdaExpression;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.XMLCommentLiteral;
import org.ballerinalang.model.expressions.XMLElementLiteral;
import org.ballerinalang.model.expressions.XMLLiteral;
import org.ballerinalang.model.expressions.XMLPILiteral;
import org.ballerinalang.model.expressions.XMLQNameExpr;
import org.ballerinalang.model.expressions.XMLSequenceLiteral;
import org.ballerinalang.model.expressions.XMLTextLiteral;
import org.ballerinalang.model.expressions.variablerefs.FieldBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.IndexBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.VariableReferenceExpr;
import org.ballerinalang.model.expressions.variablerefs.XMLAttributesRefExpr;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BJSONConstrainedType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BuiltinTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.SemanticErrors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

/**
 * @since 0.92
 */
public class ExpressionSemanticsAnalyzer implements ExpressionVisitor {

    private SemanticAnalyzerContext ctx;

    public ExpressionSemanticsAnalyzer(SemanticAnalyzerContext context) {
        this.ctx = context;
    }

    @Override
    public Expression visit(BasicLiteral basicLiteral) {
        BType bType = BTypes.resolveType(basicLiteral.getTypeName(), ctx.currentScope, basicLiteral.getNodeLocation());
        basicLiteral.setType(bType);
        return basicLiteral;
    }

    @Override
    public Expression visit(NullLiteral nullLiteral) {
        nullLiteral.setType(BTypes.typeNull);
        return nullLiteral;
    }

    @Override
    public Expression visit(SimpleVarRefExpr simpleVarRefExpr) {
        // Resolve package path from the give package name
        if (simpleVarRefExpr.getPkgName() != null && simpleVarRefExpr.getPkgPath() == null) {
            throw BLangExceptionHelper.getSemanticError(simpleVarRefExpr.getNodeLocation(),
                    SemanticErrors.UNDEFINED_PACKAGE_NAME, simpleVarRefExpr.getPkgName(),
                    simpleVarRefExpr.getPkgName() + ":" + simpleVarRefExpr.getVarName());
        }

        // Check whether this symName is declared
        SymbolName symbolName = simpleVarRefExpr.getSymbolName();
        BLangSymbol varDefSymbol = ctx.currentScope.resolve(symbolName);
        if (varDefSymbol == null) {
            BLangExceptionHelper.throwSemanticError(simpleVarRefExpr, SemanticErrors.UNDEFINED_SYMBOL,
                    symbolName);
        }

        // TODO We should be able to remove the following check now. Double check.
        if (!(varDefSymbol instanceof VariableDef)) {
            throw BLangExceptionHelper.getSemanticError(simpleVarRefExpr.getNodeLocation(),
                    SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, symbolName);
        }

        simpleVarRefExpr.setVariableDef((VariableDef) varDefSymbol);
        return simpleVarRefExpr;
    }

    @Override
    public Expression visit(FieldBasedVarRefExpr fieldBasedVarRefExpr) {
        fieldBasedVarRefExpr.accept(ctx.semanticAnalyzer);
        return fieldBasedVarRefExpr;
    }

    @Override
    public Expression visit(IndexBasedVarRefExpr indexBasedVarRefExpr) {
        indexBasedVarRefExpr.accept(ctx.semanticAnalyzer);
        return indexBasedVarRefExpr;
    }

    @Override
    public Expression visit(UnaryExpression unaryExpression) {
        unaryExpression.accept(ctx.semanticAnalyzer);
        return unaryExpression;
    }

    @Override
    public Expression visit(AddExpression addExpr) {
        addExpr.accept(ctx.semanticAnalyzer);
        return addExpr;
    }

    @Override
    public Expression visit(SubtractExpression subtractExpression) {
        subtractExpression.accept(ctx.semanticAnalyzer);
        return subtractExpression;
    }

    @Override
    public Expression visit(MultExpression multExpression) {
        multExpression.accept(ctx.semanticAnalyzer);
        return multExpression;
    }

    @Override
    public Expression visit(DivideExpr divideExpr) {
        divideExpr.accept(ctx.semanticAnalyzer);
        return divideExpr;
    }

    @Override
    public Expression visit(ModExpression modExpression) {
        modExpression.accept(ctx.semanticAnalyzer);
        return modExpression;
    }

    @Override
    public Expression visit(AndExpression andExpression) {
        andExpression.accept(ctx.semanticAnalyzer);
        return andExpression;
    }

    @Override
    public Expression visit(OrExpression orExpression) {
        orExpression.accept(ctx.semanticAnalyzer);
        return orExpression;
    }

    @Override
    public Expression visit(EqualExpression equalExpression) {
        equalExpression.accept(ctx.semanticAnalyzer);
        return equalExpression;
    }

    @Override
    public Expression visit(NotEqualExpression notEqualExpression) {
        notEqualExpression.accept(ctx.semanticAnalyzer);
        return notEqualExpression;
    }

    @Override
    public Expression visit(GreaterEqualExpression greaterEqualExpression) {
        greaterEqualExpression.accept(ctx.semanticAnalyzer);
        return greaterEqualExpression;
    }

    @Override
    public Expression visit(GreaterThanExpression greaterThanExpression) {
        greaterThanExpression.accept(ctx.semanticAnalyzer);
        return greaterThanExpression;
    }

    @Override
    public Expression visit(LessEqualExpression lessEqualExpression) {
        lessEqualExpression.accept(ctx.semanticAnalyzer);
        return lessEqualExpression;
    }

    @Override
    public Expression visit(LessThanExpression lessThanExpression) {
        lessThanExpression.accept(ctx.semanticAnalyzer);
        return lessThanExpression;
    }

    @Override
    public Expression visit(FunctionInvocationExpr functionInvocationExpr) {
        functionInvocationExpr.accept(ctx.semanticAnalyzer);
        return functionInvocationExpr;
    }

    @Override
    public Expression visit(ActionInvocationExpr actionInvocationExpr) {
        actionInvocationExpr.accept(ctx.semanticAnalyzer);
        return actionInvocationExpr;
    }

    @Override
    public Expression visit(TypeCastExpression typeCastExpression) {
        typeCastExpression.accept(ctx.semanticAnalyzer);
        return typeCastExpression;
    }

    @Override
    public Expression visit(TypeConversionExpr typeConversionExpression) {
        typeConversionExpression.accept(ctx.semanticAnalyzer);
        return typeConversionExpression;
    }

    @Override
    public Expression visit(ArrayInitExpr arrayInitExpr) {
        // array type, json, any
        arrayInitExpr.accept(ctx.semanticAnalyzer);
        return arrayInitExpr;
    }

    @Override
    public Expression visit(RefTypeInitExpr refTypeInitExpr) {
        refTypeInitExpr.accept(ctx.semanticAnalyzer);
        return refTypeInitExpr;

        // Allowed types: json, any, map, struct, message, datatable
        // If the inherited type is message or datatable, then key/value pairs are not allowed.
//        BType initExprType = refTypeInitExpr.getInheritedType();
//        Expression[] argExprs = refTypeInitExpr.getArgExprs();
//        if (BTypes.typeMessage.equals(initExprType) || BTypes.typeDatatable.equals(initExprType)) {
//            if (argExprs.length != 0) {
//                throw BLangExceptionHelper.getSemanticError(refTypeInitExpr,
//                        SemanticErrors.STRUCT_MAP_INIT_NOT_ALLOWED);
//            }
//
//            refTypeInitExpr.setType(initExprType);
//            return refTypeInitExpr;
//
//        }
//
//        RefTypeInitExpr newRefTypeInitExpr;
//        NodeLocation nodeLocation = refTypeInitExpr.getNodeLocation();
//        WhiteSpaceDescriptor wsDescriptor = refTypeInitExpr.getWhiteSpaceDescriptor();
//        if (BTypes.typeAny.equals(initExprType) || BTypes.typeMap.equals(initExprType)) {
//            newRefTypeInitExpr = new MapInitExpr(nodeLocation, wsDescriptor, argExprs);
//            initExprType = BTypes.typeMap;
//
//        } else if (BTypes.typeJSON.equals(initExprType) || initExprType instanceof BJSONConstrainedType) {
//            newRefTypeInitExpr = new JSONInitExpr(nodeLocation, wsDescriptor, argExprs);
//
//        } else if (initExprType instanceof StructDef) {
//            newRefTypeInitExpr = new StructInitExpr(nodeLocation, wsDescriptor, argExprs);
//
//        } else {
//            throw BLangExceptionHelper.getSemanticError(refTypeInitExpr,
//                    SemanticErrors.REF_TYPE_INIT_NOT_ALLOWED_HERE);
//        }
//
//        newRefTypeInitExpr.setType(initExprType);
//        newRefTypeInitExpr.accept(this);
//        return newRefTypeInitExpr;
    }

    @Override
    public Expression visit(ConnectorInitExpr connectorInitExpr) {
        connectorInitExpr.accept(ctx.semanticAnalyzer);
        return connectorInitExpr;
    }

    @Override
    public Expression visit(StructInitExpr structInitExpr) {
        structInitExpr.accept(ctx.semanticAnalyzer);
        return structInitExpr;

//        BType inheritedType = structInitExpr.getInheritedType();
//        structInitExpr.setType(inheritedType);
//        Expression[] argExprs = structInitExpr.getArgExprs();
//        if (argExprs.length == 0) {
//            return;
//        }
//
//        StructDef structDef = (StructDef) inheritedType;
//        for (Expression argExpr : argExprs) {
//            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;
//            Expression keyExpr = keyValueExpr.getKeyExpr();
//            if (!(keyExpr instanceof SimpleVarRefExpr)) {
//                throw BLangExceptionHelper.getSemanticError(keyExpr.getNodeLocation(),
//                        SemanticErrors.INVALID_FIELD_NAME_STRUCT_INIT);
//            }
//
//            SimpleVarRefExpr varRefExpr = (SimpleVarRefExpr) keyExpr;
//            //TODO fix properly package conflict
//            BLangSymbol varDefSymbol = structDef.resolveMembers(new SymbolName(varRefExpr.getSymbolName().getName(),
//                    structDef.getPackagePath()));
//
//            if (varDefSymbol == null) {
//                throw BLangExceptionHelper.getSemanticError(keyExpr.getNodeLocation(),
//                        SemanticErrors.UNKNOWN_FIELD_IN_STRUCT, varRefExpr.getVarName(), structDef.getName());
//            }
//
//            if (!(varDefSymbol instanceof SimpleVariableDef)) {
//                throw BLangExceptionHelper.getSemanticError(varRefExpr.getNodeLocation(),
//                        SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, varDefSymbol.getSymbolName());
//            }
//
//            SimpleVariableDef varDef = (SimpleVariableDef) varDefSymbol;
//            varRefExpr.setVariableDef(varDef);
//
//            BType structFieldType = varDef.getType();
//            Expression valueExpr = keyValueExpr.getValueExpr();
//            if (valueExpr instanceof RefTypeInitExpr) {
//                valueExpr = getNestedInitExpr(valueExpr, structFieldType);
//                keyValueExpr.setValueExpr(valueExpr);
//            }
//
//            valueExpr.accept(this);
//
//            // Check whether the right-hand type can be assigned to the left-hand type.
//            SemanticAnalyzer.AssignabilityResult result = performAssignabilityCheck(structFieldType, valueExpr);
//            if (result.expression != null) {
//                valueExpr = result.expression;
//                keyValueExpr.setValueExpr(valueExpr);
//            } else if (!result.assignable) {
//                BLangExceptionHelper.throwSemanticError(keyExpr, SemanticErrors.INCOMPATIBLE_TYPES,
//                        varDef.getType(), valueExpr.getType());
//            }
//        }
    }

    @Override
    public Expression visit(MapInitExpr mapInitExpr) {
        BType inheritedType = mapInitExpr.getInheritedType();
        mapInitExpr.setType(inheritedType);
        Expression[] argExprs = mapInitExpr.getArgExprs();

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;
            Expression keyExpr = keyValueExpr.getKeyExpr();

            // In maps and json, key is always a string literal.
            if (keyExpr instanceof SimpleVarRefExpr) {
                BString key = new BString(((SimpleVarRefExpr) keyExpr).getVarName());
                keyExpr = new BasicLiteral(keyExpr.getNodeLocation(), keyExpr.getWhiteSpaceDescriptor(),
                        new BuiltinTypeName(TypeConstants.STRING_TNAME), key);
                keyValueExpr.setKeyExpr(keyExpr);
            }
            SemanticAnalyzerUtils.visitSingleValueExpr(keyExpr, this);

            Expression valueExpr = keyValueExpr.getValueExpr();
            if (inheritedType instanceof BJSONConstrainedType) {
                String key = ((BasicLiteral) keyExpr).getBValue().stringValue();
                StructDef constraintStructDef = (StructDef) ((BJSONConstrainedType) inheritedType).getConstraint();
                if (constraintStructDef != null) {
                    BLangSymbol varDefSymbol = constraintStructDef
                            .resolveMembers(new SymbolName(key, constraintStructDef.getPackagePath()));
                    if (varDefSymbol == null) {
                        throw BLangExceptionHelper.getSemanticError(keyExpr.getNodeLocation(),
                                SemanticErrors.UNKNOWN_FIELD_IN_JSON_STRUCT, key, constraintStructDef.getName());
                    }
                    VariableDef varDef = (VariableDef) varDefSymbol;
                    BType cJSONFieldType = new BJSONConstrainedType(varDef.getType());
                    if (valueExpr instanceof RefTypeInitExpr) {
                        valueExpr.setInheritedType(cJSONFieldType);
                        valueExpr = valueExpr.accept(this);
                        keyValueExpr.setValueExpr(valueExpr);
                    }
                }
            } else if (valueExpr instanceof RefTypeInitExpr) {
                valueExpr.setInheritedType(inheritedType);
                valueExpr = valueExpr.accept(this);
                keyValueExpr.setValueExpr(valueExpr);
            } else {
                valueExpr.accept(this);
            }

            BType valueExprType = valueExpr.getType();

            // Generate type cast expression if the rhs type is a value type
            if (BTypes.isValueType(valueExprType)) {
                TypeCastExpression newExpr = SemanticAnalyzerUtils.checkWideningPossible(BTypes.typeAny, valueExpr);
                if (newExpr != null) {
                    keyValueExpr.setValueExpr(newExpr);
                } else {
                    BLangExceptionHelper.throwSemanticError(keyValueExpr,
                            SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, valueExprType.getSymbolName(),
                            inheritedType);
                }
            }
        }

        return mapInitExpr;
    }

    @Override
    public Expression visit(JSONInitExpr jsonInitExpr) {
        BType inheritedType = jsonInitExpr.getInheritedType();
        jsonInitExpr.setType(inheritedType);
        Expression[] argExprs = jsonInitExpr.getArgExprs();

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;
            Expression keyExpr = keyValueExpr.getKeyExpr();

            // In maps and json, key is always a string literal.
            if (keyExpr instanceof SimpleVarRefExpr) {
                BString key = new BString(((SimpleVarRefExpr) keyExpr).getVarName());
                keyExpr = new BasicLiteral(keyExpr.getNodeLocation(), keyExpr.getWhiteSpaceDescriptor(),
                        new BuiltinTypeName(TypeConstants.STRING_TNAME), key);
                keyValueExpr.setKeyExpr(keyExpr);
            }
            SemanticAnalyzerUtils.visitSingleValueExpr(keyExpr, this);

            Expression valueExpr = keyValueExpr.getValueExpr();
            if (inheritedType instanceof BJSONConstrainedType) {
                String key = ((BasicLiteral) keyExpr).getBValue().stringValue();
                StructDef constraintStructDef = (StructDef) ((BJSONConstrainedType) inheritedType).getConstraint();
                if (constraintStructDef != null) {
                    BLangSymbol varDefSymbol = constraintStructDef
                            .resolveMembers(new SymbolName(key, constraintStructDef.getPackagePath()));
                    if (varDefSymbol == null) {
                        throw BLangExceptionHelper.getSemanticError(keyExpr.getNodeLocation(),
                                SemanticErrors.UNKNOWN_FIELD_IN_JSON_STRUCT, key, constraintStructDef.getName());
                    }
                    VariableDef varDef = (VariableDef) varDefSymbol;
                    BType cJSONFieldType = new BJSONConstrainedType(varDef.getType());
                    if (valueExpr instanceof RefTypeInitExpr) {
                        valueExpr.setInheritedType(cJSONFieldType);
                        valueExpr = valueExpr.accept(this);
                        keyValueExpr.setValueExpr(valueExpr);
                    }
                }
            } else if (valueExpr instanceof RefTypeInitExpr) {
                valueExpr.setInheritedType(inheritedType);
                valueExpr = valueExpr.accept(this);
                keyValueExpr.setValueExpr(valueExpr);
            } else {
                valueExpr.accept(this);
            }

            BType valueExprType = valueExpr.getType();

            // Check the type compatibility of the value.
            if (BTypes.isValueType(valueExprType)) {
                TypeCastExpression typeCastExpr =
                        SemanticAnalyzerUtils.checkWideningPossible(BTypes.typeJSON, valueExpr);
                if (typeCastExpr != null) {
                    keyValueExpr.setValueExpr(typeCastExpr);
                } else {
                    BLangExceptionHelper.throwSemanticError(keyValueExpr,
                            SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, valueExprType.getSymbolName(),
                            inheritedType.getSymbolName());
                }
                continue;
            }

            if (valueExprType != BTypes.typeNull
                    && SemanticAnalyzerUtils.isAssignableTo(BTypes.typeJSON, valueExprType)) {
                continue;
            }

            TypeCastExpression typeCastExpr = SemanticAnalyzerUtils.checkWideningPossible(BTypes.typeJSON, valueExpr);
            if (typeCastExpr == null) {
                BLangExceptionHelper.throwSemanticError(jsonInitExpr, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT,
                        valueExpr.getType(), BTypes.typeJSON);
            }
            keyValueExpr.setValueExpr(typeCastExpr);
        }

        return jsonInitExpr;
    }

    @Override
    public Expression visit(JSONArrayInitExpr jsonArrayInitExpr) {
        jsonArrayInitExpr.accept(ctx.semanticAnalyzer);
        return jsonArrayInitExpr;
    }

    @Override
    public Expression visit(XMLLiteral xmlLiteral) {
        xmlLiteral.accept(ctx.semanticAnalyzer);
        return xmlLiteral;
    }

    @Override
    public Expression visit(XMLElementLiteral xmlElement) {
        Expression startTagName = xmlElement.getStartTagName();
        Map<String, Expression> namespaces;
        XMLElementLiteral parent = xmlElement.getParent();
        if (parent == null) {
            namespaces =
                    SemanticAnalyzerUtils.getNamespaceInScope(xmlElement.getNodeLocation(), ctx.currentScope, this);
        } else {
            namespaces = parent.getNamespaces();
            xmlElement.setDefaultNamespaceUri(parent.getDefaultNamespaceUri());
        }
        xmlElement.setNamespaces(namespaces);

        // add the inline declared namespaces to the namespace map
        List<KeyValueExpr> attributes = xmlElement.getAttributes();
        Iterator<KeyValueExpr> attrItr = attributes.iterator();
        while (attrItr.hasNext()) {
            KeyValueExpr attribute = attrItr.next();
            Expression attrNameExpr = attribute.getKeyExpr();
            if (!(attrNameExpr instanceof XMLQNameExpr)) {
                continue;
            }

            Expression attrValueExpr = attribute.getValueExpr();
            XMLQNameExpr xmlQNameRefExpr = (XMLQNameExpr) attrNameExpr;
            if (xmlQNameRefExpr.getPrefix().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attrValueExpr.accept(this);

                if (attrValueExpr instanceof BasicLiteral
                        && ((BasicLiteral) attrValueExpr).getBValue().stringValue().isEmpty()) {
                    BLangExceptionHelper.throwSemanticError(attribute, SemanticErrors.INVALID_NAMESPACE_DECLARATION,
                            xmlQNameRefExpr.getLocalname());
                }

                namespaces.put(xmlQNameRefExpr.getLocalname(), attrValueExpr);
                attrItr.remove();
                continue;
            }

            // if the default namesapce is declared inline, then override default namepsace defined at the
            // parent scope level
            if (xmlQNameRefExpr.getLocalname().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attrValueExpr.accept(this);
                xmlElement.setDefaultNamespaceUri(attrValueExpr);
                attrItr.remove();
            }
        }

        if (xmlElement.getDefaultNamespaceUri() == null) {
            BasicLiteral defaultnsUriLiteral = new BasicLiteral(xmlElement.getNodeLocation(), null,
                    new BuiltinTypeName(TypeConstants.STRING_TNAME), new BString(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
            defaultnsUriLiteral.setType(BTypes.typeString);
            defaultnsUriLiteral.accept(this);
            xmlElement.setDefaultNamespaceUri(defaultnsUriLiteral);
        }

        SemanticAnalyzerUtils.validateXMLLiteralAttributes(attributes, namespaces, this);

        // Validate start tag
        if (startTagName instanceof XMLQNameExpr) {
            SemanticAnalyzerUtils.validateXMLQname((XMLQNameExpr) startTagName, namespaces, this);
        } else {
            startTagName.accept(this);
        }

        if (startTagName.getType() != BTypes.typeString) {
            // Implicit cast from right to left
            startTagName = SemanticAnalyzerUtils.createImplicitStringConversionExpr(startTagName,
                    startTagName.getType(), this);
            xmlElement.setStartTagName(startTagName);
        }

        // Validate the ending tag of the XML element
        SemanticAnalyzerUtils.validateXMLLiteralEndTag(xmlElement, this);

        // Visit children
        XMLSequenceLiteral children = xmlElement.getContent();
        if (children != null) {
            children.accept(this);
        }
        return xmlElement;
    }

    @Override
    public Expression visit(XMLCommentLiteral xmlComment) {
        Expression contentExpr = xmlComment.getContent();
        if (contentExpr != null) {
            contentExpr.accept(this);
        }

        if (contentExpr.getType() != BTypes.typeString) {
            contentExpr =
                    SemanticAnalyzerUtils.createImplicitStringConversionExpr(contentExpr, contentExpr.getType(), this);
            xmlComment.setContent(contentExpr);
        }
        return xmlComment;
    }

    @Override
    public Expression visit(XMLTextLiteral xmlText) {
        Expression contentExpr = xmlText.getContent();
        if (contentExpr != null) {
            contentExpr.accept(this);
        }
        return xmlText;
    }

    @Override
    public Expression visit(XMLPILiteral xmlPI) {
        Expression target = xmlPI.getTarget();
        target.accept(this);

        if (target.getType() != BTypes.typeString) {
            target = SemanticAnalyzerUtils.createImplicitStringConversionExpr(target, target.getType(), this);
            xmlPI.setTarget(target);
        }

        Expression data = xmlPI.getData();
        if (data != null) {
            data.accept(this);
        }

        if (data.getType() != BTypes.typeString) {
            data = SemanticAnalyzerUtils.createImplicitStringConversionExpr(data, data.getType(), this);
            xmlPI.setData(data);
        }
        return xmlPI;
    }

    @Override
    public Expression visit(XMLAttributesRefExpr xmlAttributesRefExpr) {
        VariableReferenceExpr varRefExpr = xmlAttributesRefExpr.getVarRefExpr();
        varRefExpr.accept(this);

        if (varRefExpr.getType() != BTypes.typeXML) {
            BLangExceptionHelper.throwSemanticError(xmlAttributesRefExpr, SemanticErrors.INCOMPATIBLE_TYPES,
                    BTypes.typeXML, varRefExpr.getType());
        }

        Expression indexExpr = xmlAttributesRefExpr.getIndexExpr();
        if (indexExpr == null) {
            if (xmlAttributesRefExpr.isLHSExpr()) {
                BLangExceptionHelper.throwSemanticError(xmlAttributesRefExpr,
                        SemanticErrors.XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED);
            }
            xmlAttributesRefExpr.setType(BTypes.typeXMLAttributes);
            return xmlAttributesRefExpr;
        }

        xmlAttributesRefExpr.setType(BTypes.typeString);
        indexExpr.accept(this);
        if (indexExpr instanceof XMLQNameExpr) {
            ((XMLQNameExpr) indexExpr).setUsedInXML(true);
            return xmlAttributesRefExpr;
        }

        if (indexExpr.getType() != BTypes.typeString) {
            BLangExceptionHelper.throwSemanticError(indexExpr, SemanticErrors.NON_STRING_MAP_INDEX,
                    indexExpr.getType());
        }

        Map<String, Expression> namespaces = SemanticAnalyzerUtils
                .getNamespaceInScope(xmlAttributesRefExpr.getNodeLocation(), ctx.currentScope, this);
        xmlAttributesRefExpr.setNamespaces(namespaces);
        return xmlAttributesRefExpr;
    }

    @Override
    public Expression visit(XMLQNameExpr xmlQNameRefExpr) {
        if (xmlQNameRefExpr.isLHSExpr()) {
            BLangExceptionHelper.throwSemanticError(xmlQNameRefExpr, SemanticErrors.XML_QNAME_UPDATE_NOT_ALLOWED);
        }

        xmlQNameRefExpr.setType(BTypes.typeString);
        String prefix = xmlQNameRefExpr.getPrefix();
        if (prefix.isEmpty()) {
            return xmlQNameRefExpr;
        }

        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            BLangExceptionHelper.throwSemanticError(xmlQNameRefExpr, SemanticErrors.INVALID_NAMESPACE_PREFIX, prefix);
        }

        NamespaceSymbolName nsSymbolName = new NamespaceSymbolName(prefix);
        BLangSymbol symbol = ctx.currentScope.resolve(nsSymbolName);

        if (symbol == null) {
            BLangExceptionHelper.throwSemanticError(xmlQNameRefExpr, SemanticErrors.UNDEFINED_NAMESPACE, prefix);
        }

        String namepsaceUri = ((NamespaceDeclaration) symbol).getNamespaceUri();
        BasicLiteral namespaceUriLiteral = new BasicLiteral(xmlQNameRefExpr.getNodeLocation(), null,
                new BuiltinTypeName(TypeConstants.STRING_TNAME), new BString(namepsaceUri));
        namespaceUriLiteral.accept(this);
        xmlQNameRefExpr.setNamepsaceUri(namespaceUriLiteral);
        return xmlQNameRefExpr;
    }

    @Override
    public Expression visit(XMLSequenceLiteral xmlSequence) {
        Expression[] items = xmlSequence.getItems();
        List<Expression> newItems = new ArrayList<Expression>();

        // Consecutive non-xml type items are converted to string, and combined together using binary add expressions.
        Expression addExpr = null;
        for (int i = 0; i < items.length; i++) {
            Expression currentItem = items[i];
            currentItem.accept(this);

            if (xmlSequence.hasParent() && currentItem.getType() == BTypes.typeXML) {
                if (addExpr != null) {
                    newItems.add(addExpr);
                    addExpr = null;
                }
                newItems.add(currentItem);
                continue;
            }

            if (currentItem.getType() != BTypes.typeString) {
                Expression castExpr = SemanticAnalyzerUtils.getImplicitConversionExpr(currentItem,
                        currentItem.getType(), BTypes.typeString, this);

                if (castExpr == null) {
                    if (xmlSequence.hasParent()) {
                        BLangExceptionHelper.throwSemanticError(currentItem,
                                SemanticErrors.INCOMPATIBLE_TYPES_IN_XML_TEMPLATE, currentItem.getType());
                    }
                    BLangExceptionHelper.throwSemanticError(currentItem, SemanticErrors.INCOMPATIBLE_TYPES,
                            BTypes.typeString, currentItem.getType());
                }

                currentItem = castExpr;
            }

            if (addExpr == null) {
                addExpr = currentItem;
                continue;
            }

            if (addExpr.getType() == BTypes.typeString) {
                addExpr = new AddExpression(currentItem.getNodeLocation(), currentItem.getWhiteSpaceDescriptor(),
                        addExpr, currentItem);
            } else {
                newItems.add(addExpr);
                addExpr = currentItem;
            }
            addExpr.setType(BTypes.typeString);
        }

        if (addExpr != null) {
            newItems.add(addExpr);
        }

        // Replace the existing items with the new reduced items
        items = newItems.toArray(new Expression[newItems.size()]);
        xmlSequence.setItems(items);

        // Create and set XML concatenation expression using all the items in the sequence
        xmlSequence.setConcatExpr(SemanticAnalyzerUtils.getXMLConcatExpression(items));
        return xmlSequence;
    }

    @Override
    public Expression visit(LambdaExpression lambdaExpr) {
        lambdaExpr.accept(ctx.semanticAnalyzer);
        return lambdaExpr;
    }
}
