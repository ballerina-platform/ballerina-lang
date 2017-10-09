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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransform;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private Names names;
    private SymbolResolver symResolver;
    private TypeChecker typeChecker;
    private Types types;
    private DiagnosticLog dlog;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private BType resType;

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }


    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TYPE_CHECK)) {
            return;
        }
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);

        // Visit all the imported packages
        pkgNode.imports.forEach(importNode -> analyzeDef(importNode, pkgEnv));

        // Then visit each top-level element sorted using the compilation unit
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeDef((BLangNode) topLevelNode, pkgEnv));

        analyzeDef(pkgNode.initFunction, pkgEnv);

        pkgNode.completedPhases.add(CompilerPhase.TYPE_CHECK);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        analyzeDef(pkgEnv.node, pkgEnv);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.type = symTable.stringType;

        // Namespace node already having the symbol means we are inside an init-function,
        // and the symbol has already been declared by the original statement.
        if (xmlnsNode.symbol != null) {
            return;
        }

        symbolEnter.defineNode(xmlnsNode, env);
        typeChecker.checkExpr(xmlnsNode.namespaceURI, env, Lists.of(symTable.stringType));
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        funcNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.FUNCTION, null);
            this.analyzeDef(annotationAttachment, funcEnv);
        });

        // Check for native functions
        if (Symbols.isNative(funcNode.symbol)) {
            return;
        }

        analyzeStmt(funcNode.body, funcEnv);

        if (funcNode.workers.size() > 0) {
            // Process workers
            funcEnv.scope.entries.putAll(funcNode.body.scope.entries);
            funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
            funcNode.workers.forEach(e -> analyzeNode(e, funcEnv));
        }
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeDef(field, structEnv));

        structNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.STRUCT, null);
            annotationAttachment.accept(this);
        });
    }

    public void visit(BLangAnnotation annotationNode) {
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationNode.symbol.scope, env);
        annotationNode.attributes.forEach(attribute -> {
            analyzeNode(attribute, annotationEnv);
        });

        annotationNode.attachmentPoints.forEach(point -> {
            if (point.pkgAlias != null) {
                BSymbol pkgSymbol = symResolver.resolvePkgSymbol(annotationNode.pos,
                        annotationEnv, names.fromIdNode(point.pkgAlias));
                if (pkgSymbol == symTable.notFoundSymbol) {
                    return;
                }
                point.pkgPath = pkgSymbol.pkgID.name.getValue();
            }
        });

        annotationNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ANNOTATION, null);
            annotationAttachment.accept(this);
        });
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        if (annotationAttribute.expr != null) {
            // Default value exists case, default value should be of simpleLiteral
            BType actualType = this.typeChecker.checkExpr(annotationAttribute.expr, env,
                    Lists.of(annotationAttribute.symbol.type),
                    DiagnosticCode.INVALID_OPERATION_INCOMPATIBLE_TYPES).get(0);

            if (!(this.types.isValueType(annotationAttribute.symbol.type) && this.types.isValueType(actualType))) {
                this.dlog.error(annotationAttribute.pos, DiagnosticCode.INVALID_DEFAULT_VALUE);
            }
        } else {
            if (!this.types.isAnnotationFieldType(annotationAttribute.symbol.type)) {
                this.dlog.error(annotationAttribute.pos, DiagnosticCode.INVALID_ATTRIBUTE_TYPE,
                        annotationAttribute.symbol.type);
            }
        }
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        BSymbol symbol = this.symResolver.resolveAnnotation(annAttachmentNode.pos, env,
                names.fromString(annAttachmentNode.pkgAlias.getValue()),
                names.fromString(annAttachmentNode.getAnnotationName().getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                    annAttachmentNode.getAnnotationName().getValue());
            return;
        }
        // Validate Attachment Point against the Annotation Definition.
        BAnnotationSymbol annotationSymbol = (BAnnotationSymbol) symbol;
        annAttachmentNode.annotationSymbol = annotationSymbol;
        if (annotationSymbol.getAttachmentPoints() != null && annotationSymbol.getAttachmentPoints().size() > 0) {
            BLangAnnotationAttachmentPoint[] attachmentPointsArrray =
                    new BLangAnnotationAttachmentPoint[annotationSymbol.getAttachmentPoints().size()];
            Optional<BLangAnnotationAttachmentPoint> matchingAttachmentPoint = Arrays
                    .stream(annotationSymbol.getAttachmentPoints().toArray(attachmentPointsArrray))
                    .filter(attachmentPoint -> attachmentPoint.equals(annAttachmentNode.attachmentPoint))
                    .findAny();
            if (!matchingAttachmentPoint.isPresent()) {
                String msg = annAttachmentNode.attachmentPoint.getAttachmentPoint().getValue();
                if (annAttachmentNode.attachmentPoint.getPkgPath() != null) {
                    msg = annAttachmentNode.attachmentPoint.getAttachmentPoint().getValue() +
                            "<" + annAttachmentNode.attachmentPoint.getPkgPath() + ">";
                }
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_NOT_ALLOWED,
                        annotationSymbol, msg);
            }
        }
        // Validate Annotation Attachment Attributes against Annotation Definition.
        validateAttributes(annAttachmentNode, annotationSymbol);
        // Populate default values for Annotation Attachment from Annotation Definition.
        populateDefaultValues(annAttachmentNode, annotationSymbol);
    }

    private void validateAttributes(BLangAnnotationAttachment annAttachmentNode, BAnnotationSymbol annotationSymbol) {
        annAttachmentNode.attributes.forEach(annotAttachmentAttribute -> {
            BAnnotationAttributeSymbol attributeSymbol = (BAnnotationAttributeSymbol)
                    annotationSymbol.scope.lookup(new Name(annotAttachmentAttribute.getName())).symbol;
            // Resolve Attribute against the Annotation Definition
            if (attributeSymbol == null) {
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.NO_SUCH_ATTRIBUTE,
                        annotAttachmentAttribute.getName(), annotationSymbol.name);
                return;
            }

            if (annotAttachmentAttribute.value.value != null
                    && annotAttachmentAttribute.value.value instanceof BLangExpression) {
                BType resolvedType = this.typeChecker.checkExpr((BLangExpression) annotAttachmentAttribute.value.value,
                        env, Lists.of(attributeSymbol.type), DiagnosticCode.INCOMPATIBLE_TYPES).get(0);
                if (resolvedType == symTable.errType) {
                    return;
                }
                if (annotAttachmentAttribute.value.value instanceof BLangSimpleVarRef &&
                        ((BLangSimpleVarRef) annotAttachmentAttribute.value.value).symbol.flags != Flags.CONST) {
                    this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ATTRIBUTE_VAL_CANNOT_REFER_NON_CONST);
                }
                return;
            } else {
                if (attributeSymbol.type.tag == TypeTags.ARRAY) {
                    // Attachment Attribute is Non Array Type as opposed to
                    // Annotation Definition Attribute is of Array Type
                    if (annotAttachmentAttribute.value.value != null) {
                        if (annotAttachmentAttribute.value.value instanceof BLangExpression) {
                            this.typeChecker.checkExpr((BLangExpression) annotAttachmentAttribute.value.value,
                                    env, Lists.of(attributeSymbol.type), DiagnosticCode.INCOMPATIBLE_TYPES);
                        } else {
                            BLangAnnotationAttachment childAttachment =
                                    (BLangAnnotationAttachment) annotAttachmentAttribute.value.value;
                            BSymbol symbol = this.symResolver.resolveAnnotation(childAttachment.pos, env,
                                    names.fromString(childAttachment.pkgAlias.getValue()),
                                    names.fromString(childAttachment.getAnnotationName().getValue()));
                            if (symbol == this.symTable.notFoundSymbol) {
                                this.dlog.error(childAttachment.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                                        childAttachment.getAnnotationName().getValue());
                                return;
                            }
                            childAttachment.type = symbol.type;
                            this.types.checkType(childAttachment.pos, childAttachment.type,
                                    attributeSymbol.type, DiagnosticCode.INCOMPATIBLE_TYPES);
                        }
                    }
                    annotAttachmentAttribute.value.arrayValues.forEach(value -> {
                        if (value.value instanceof BLangAnnotationAttachment) {
                            BLangAnnotationAttachment childAttachment =
                                    (BLangAnnotationAttachment) value.value;
                            if (childAttachment != null) {
                                BSymbol symbol = this.symResolver.resolveAnnotation(childAttachment.pos, env,
                                        names.fromString(childAttachment.pkgAlias.getValue()),
                                        names.fromString(childAttachment.getAnnotationName().getValue()));
                                if (symbol == this.symTable.notFoundSymbol) {
                                    this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                                            childAttachment.getAnnotationName().getValue());
                                    return;
                                }
                                childAttachment.type = symbol.type;
                                childAttachment.annotationSymbol = (BAnnotationSymbol) symbol;
                                this.types.checkType(childAttachment.pos, childAttachment.type,
                                        ((BArrayType) attributeSymbol.type).eType, DiagnosticCode.INCOMPATIBLE_TYPES);
                                validateAttributes(childAttachment, (BAnnotationSymbol) symbol);
                            }
                        } else {
                            this.typeChecker.checkExpr((BLangExpression) value.value,
                                    env, Lists.of(((BArrayType) attributeSymbol.type).eType),
                                    DiagnosticCode.INCOMPATIBLE_TYPES);
                        }
                    });
                } else {
                    // Attachment Attribute is Array Type as opposed to
                    // Annotation Definition Attribute is Non Array Type
                    if (annotAttachmentAttribute.value.value == null) {
                        this.dlog.error(annAttachmentNode.pos, DiagnosticCode.INCOMPATIBLE_TYPES_ARRAY_FOUND,
                                attributeSymbol.type);
                    }

                    BLangAnnotationAttachment childAttachment =
                            (BLangAnnotationAttachment) annotAttachmentAttribute.value.value;
                    if (childAttachment != null) {
                        BSymbol symbol = this.symResolver.resolveAnnotation(childAttachment.pos, env,
                                names.fromString(childAttachment.pkgAlias.getValue()),
                                names.fromString(childAttachment.getAnnotationName().getValue()));
                        if (symbol == this.symTable.notFoundSymbol) {
                            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                                    childAttachment.getAnnotationName().getValue());
                            return;
                        }
                        childAttachment.type = symbol.type;
                        childAttachment.annotationSymbol = (BAnnotationSymbol) symbol;
                        this.types.checkType(childAttachment.pos, childAttachment.type,
                                attributeSymbol.type, DiagnosticCode.INCOMPATIBLE_TYPES);
                        validateAttributes(childAttachment, (BAnnotationSymbol) symbol);
                    }
                }
            }
        });
    }

    private void populateDefaultValues(BLangAnnotationAttachment annAttachmentNode,
                                       BAnnotationSymbol annotationSymbol) {
        for (BAnnotationAttributeSymbol defAttribute : annotationSymbol.attributes) {
            BLangAnnotAttachmentAttribute[] attributeArrray =
                    new BLangAnnotAttachmentAttribute[annAttachmentNode.getAttributes().size()];
            // Traverse through Annotation Attachment attributes and find whether current
            // Annotation Definition attribute is present
            Optional<BLangAnnotAttachmentAttribute> matchingAttribute = Arrays
                    .stream(annAttachmentNode.getAttributes().toArray(attributeArrray))
                    .filter(attribute -> attribute.name.equals(defAttribute.name.getValue()))
                    .findAny();
            // If no matching attribute is present populate with default value
            if (!matchingAttribute.isPresent()) {
                if (defAttribute.expr != null) {
                    BLangAnnotAttachmentAttributeValue value = new BLangAnnotAttachmentAttributeValue();
                    value.value = defAttribute.expr;
                    annAttachmentNode.addAttribute(defAttribute.name.getValue(), value);
                }
                continue;
            }

            // Annotation Definition attribute is basic literal and it is included in current
            // Annotation Attachment attribute, so continue to next Annotation Definition attribute
            if (matchingAttribute.get().value.value != null &&
                    !(matchingAttribute.get().value.value instanceof BLangAnnotationAttachment)) {
                continue;
            }

            // Annotation Definition attribute is an Array of Annotation Attachments and it is included in current
            // Annotation Attachment attribute,
            // Recursively populate default values for this Array of Annotation Attachments
            if (matchingAttribute.get().value.arrayValues.size() > 0) {
                for (BLangAnnotAttachmentAttributeValue attr : matchingAttribute.get().value.arrayValues) {
                    // Default values are not populated for BLangLiteral arrays
                    if (attr.value != null &&
                            !(attr.value instanceof BLangAnnotationAttachment)) {
                        continue;
                    }
                    BLangAnnotationAttachment attachment =
                            (BLangAnnotationAttachment) attr.value;
                    if (attachment != null) {
                        BSymbol symbol = this.symResolver.resolveAnnotation(attachment.pos, env,
                                names.fromString(attachment.pkgAlias.getValue()),
                                names.fromString(attachment.getAnnotationName().getValue()));
                        attachment.annotationSymbol = (BAnnotationSymbol) symbol;
                        if (symbol == this.symTable.notFoundSymbol) {
                            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                                    attachment.getAnnotationName().getValue());
                            return;
                        }
                        populateDefaultValues(attachment, (BAnnotationSymbol) symbol);
                    }
                }
            } else {
                // Annotation Definition attribute it self is Annotation Attachment and it is included in current
                // Annotation Attachment attribute,
                // Recursively populate default values for this Annotation Attachment
                BLangAnnotationAttachment attachment =
                        (BLangAnnotationAttachment) matchingAttribute.get().value.value;
                if (attachment != null) {
                    BSymbol symbol = this.symResolver.resolveAnnotation(attachment.pos, env,
                            names.fromString(attachment.pkgAlias.getValue()),
                            names.fromString(attachment.getAnnotationName().getValue()));
                    attachment.annotationSymbol = (BAnnotationSymbol) symbol;
                    if (symbol == this.symTable.notFoundSymbol) {
                        this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                                attachment.getAnnotationName().getValue());
                        return;
                    }
                    populateDefaultValues(attachment, (BAnnotationSymbol) symbol);
                }
            }
        }
    }

    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // This is a variable declared in a function, an action or a resource
            // If the variable is parameter then the variable symbol is already defined
            if (varNode.symbol == null) {
                symbolEnter.defineNode(varNode, env);
            }
        }

        // Analyze the init expression
        if (varNode.expr != null) {
            // Here we create a new symbol environment to catch self references by keep the current
            // variable symbol in the symbol environment
            // e.g. int a = x + a;
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            typeChecker.checkExpr(varNode.expr, varInitEnv, Lists.of(varNode.symbol.type));
            if (varNode.symbol.flags == Flags.CONST) {
                varNode.annAttachments.forEach(a -> {
                    a.attachmentPoint =
                            new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONST,
                                    null);
                    this.analyzeDef(a, varInitEnv);
                });
            }
        }
        varNode.type = varNode.symbol.type;
    }


    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(stmt -> analyzeStmt(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangAssignment assignNode) {
        if (assignNode.isDeclaredWithVar()) {
            handleAssignNodeWithVar(assignNode);
            return;
        }
        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression varRef = assignNode.varRefs.get(i);
            // In assignment, lhs supports only simpleVarRef, indexBasedAccess, filedBasedAccess only.
            if (varRef.getKind() == NodeKind.INVOCATION) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }
            ((BLangVariableReference) varRef).lhsVar = true;
            expTypes.add(typeChecker.checkExpr(varRef, env).get(0));
            checkConstantAssignment(varRef);
        }
        typeChecker.checkExpr(assignNode.expr, this.env, expTypes);
    }

    private void checkConstantAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.errType) {
            return;
        }

        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
        if (simpleVarRef.pkgSymbol != null && simpleVarRef.pkgSymbol.tag == SymTag.XMLNS) {
            dlog.error(varRef.pos, DiagnosticCode.XML_QNAME_UPDATE_NOT_ALLOWED);
            return;
        }

        Name varName = names.fromIdNode(simpleVarRef.variableName);
        if (!Names.IGNORE.equals(varName) && simpleVarRef.symbol.flags == Flags.CONST
                && env.enclInvokable != env.enclPkg.initFunction) {
            dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_CONSTANT, varRef);
        }
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        typeChecker.checkExpr(exprStmtNode.expr, stmtEnv, new ArrayList<>());
    }

    public void visit(BLangIf ifNode) {
        typeChecker.checkExpr(ifNode.expr, env, Lists.of(symTable.booleanType));
        analyzeStmt(ifNode.body, env);

        if (ifNode.elseStmt != null) {
            analyzeStmt(ifNode.elseStmt, env);
        }
    }

    public void visit(BLangWhile whileNode) {
        typeChecker.checkExpr(whileNode.expr, env, Lists.of(symTable.booleanType));
        analyzeStmt(whileNode.body, env);
    }

    public void visit(BLangTransform transformNode) {
        analyzeStmt(transformNode.body, env);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        connectorNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONNECTOR, null);
            this.analyzeDef(a, connectorEnv);
        });
        connectorNode.params.forEach(param -> this.analyzeDef(param, connectorEnv));
        connectorNode.varDefs.forEach(varDef -> this.analyzeDef(varDef, connectorEnv));

        this.analyzeDef(connectorNode.initFunction, connectorEnv);
        connectorNode.actions.forEach(action -> this.analyzeDef(action, connectorEnv));
        this.analyzeDef(connectorNode.initAction, connectorEnv);
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;

        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        actionNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ACTION, null);
            this.analyzeDef(a, actionEnv);
        });

        if (Symbols.isNative(actionSymbol)) {
            return;
        }

        actionNode.params.forEach(p -> this.analyzeDef(p, actionEnv));
        analyzeStmt(actionNode.body, actionEnv);
        // Process workers
        actionNode.workers.forEach(e -> this.symbolEnter.defineNode(e, actionEnv));
        actionNode.workers.forEach(e -> analyzeNode(e, actionEnv));
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        BSymbol protocolPkg = symResolver.resolvePkgSymbol(serviceNode.pos,
                serviceEnv, names.fromIdNode(serviceNode.protocolPkgIdentifier));
        //TODO validate protocol package existance
        ((BTypeSymbol) serviceSymbol).protocolPkgId = protocolPkg.pkgID;
        serviceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.SERVICE,
                            protocolPkg.pkgID.name.getValue());
            this.analyzeDef(a, serviceEnv);
        });
        serviceNode.vars.forEach(v -> this.analyzeDef(v, serviceEnv));
        this.analyzeDef(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(r -> this.analyzeDef(r, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        resourceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.RESOURCE, null);
            this.analyzeDef(a, resourceEnv);
        });

        resourceNode.params.forEach(p -> this.analyzeDef(p, resourceEnv));
        resourceNode.workers.forEach(w -> this.analyzeDef(w, resourceEnv));
        analyzeStmt(resourceNode.body, resourceEnv);
    }

    public void visit(BLangTryCatchFinally tryCatchFinally) {
        analyzeStmt(tryCatchFinally.tryBody, env);
        tryCatchFinally.catchBlocks.forEach(c -> analyzeNode(c, env));
        if (tryCatchFinally.finallyBody != null) {
            analyzeStmt(tryCatchFinally.finallyBody, env);
        }
    }

    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode(bLangCatch.param, catchBlockEnv);
        this.types.checkType(bLangCatch.param.pos, bLangCatch.param.type, symTable.errStructType,
                DiagnosticCode.INCOMPATIBLE_TYPES);
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeStmt(transactionNode.transactionBody, env);
        if (transactionNode.failedBody != null) {
            analyzeStmt(transactionNode.failedBody, env);
        }
        if (transactionNode.committedBody != null) {
            analyzeStmt(transactionNode.committedBody, env);
        }
        if (transactionNode.abortedBody != null) {
            analyzeStmt(transactionNode.abortedBody, env);
        }
        if (transactionNode.retryCount != null) {
            typeChecker.checkExpr(transactionNode.retryCount, env, Lists.of(symTable.intType));
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    private boolean isJoinResultType(BLangVariable var) {
        BLangType type = var.typeNode;
        if (type instanceof BuiltInReferenceTypeNode) {
            return ((BuiltInReferenceTypeNode) type).getTypeKind() == TypeKind.MAP;
        }
        return false;
    }

    private BLangVariableDef createVarDef(BLangVariable var) {
        BLangVariableDef varDefNode = new BLangVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv forkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.env);
        forkJoin.workers.forEach(e -> this.symbolEnter.defineNode(e, forkJoinEnv));
        forkJoin.workers.forEach(e -> this.analyzeDef(e, forkJoinEnv));
        if (!this.isJoinResultType(forkJoin.joinResultVar)) {
            this.dlog.error(forkJoin.joinResultVar.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(this.createVarDef(forkJoin.joinResultVar));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.env);
        this.analyzeNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier 
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.analyzeNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            if (!this.isJoinResultType(forkJoin.timeoutVariable)) {
                this.dlog.error(forkJoin.timeoutVariable.pos, DiagnosticCode.INVALID_WORKER_TIMEOUT_RESULT_TYPE);
            }
            /* create code black and environment for timeout section */
            BLangBlockStmt timeoutVarBlock = this.generateCodeBlock(this.createVarDef(forkJoin.timeoutVariable));
            SymbolEnv timeoutVarEnv = SymbolEnv.createBlockEnv(timeoutVarBlock, this.env);
            this.typeChecker.checkExpr(forkJoin.timeoutExpression,
                    timeoutVarEnv, Arrays.asList(symTable.intType));
            this.analyzeNode(timeoutVarBlock, timeoutVarEnv);
            /* create an environment for the timeout body, making the enclosing environment the earlier 
             * timeout var's environment */
            SymbolEnv timeoutBodyEnv = SymbolEnv.createBlockEnv(forkJoin.timeoutBody, timeoutVarEnv);
            this.analyzeNode(forkJoin.timeoutBody, timeoutBodyEnv);
        }
        
        this.validateJoinWorkerList(forkJoin, forkJoinEnv);
    }
    
    private void validateJoinWorkerList(BLangForkJoin forkJoin, SymbolEnv forkJoinEnv) {
        forkJoin.joinedWorkers.forEach(e -> {
            if (!this.workerExists(forkJoinEnv, e.value)) {
                this.dlog.error(forkJoin.pos, DiagnosticCode.UNDEFINED_WORKER, e.value);
            }
        });
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        this.analyzeNode(workerNode.body, workerEnv);
    }

    private boolean isInTopLevelWorkerEnv() {
        return this.env.enclEnv.node.getKind() == NodeKind.WORKER;
    }

    private boolean workerExists(SymbolEnv env, String workerName) {
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.WORKER);
        return (symbol != this.symTable.notFoundSymbol);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.env = this.env;
        workerSendNode.exprs.forEach(e -> this.typeChecker.checkExpr(e, this.env));
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
        }
        if (!workerSendNode.isForkJoinSend) {
            String workerName = workerSendNode.workerIdentifier.getValue();
            if (!this.workerExists(this.env, workerName)) {
                this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            }
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs.forEach(e -> this.typeChecker.checkExpr(e, this.env));
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
        }
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }

    private boolean checkReturnValueCounts(BLangReturn returnNode) {
        boolean success = false;
        int expRetCount = this.env.enclInvokable.getReturnParameters().size();
        int actualRetCount = returnNode.exprs.size();
        if (expRetCount > 1 && actualRetCount <= 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.MULTI_VALUE_RETURN_EXPECTED);
        } else if (expRetCount == 1 && actualRetCount > 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.SINGLE_VALUE_RETURN_EXPECTED);
        } else if (expRetCount == 0 && actualRetCount >= 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.RETURN_VALUE_NOT_EXPECTED);
        } else if (expRetCount > actualRetCount) {
            this.dlog.error(returnNode.pos, DiagnosticCode.NOT_ENOUGH_RETURN_VALUES);
        } else if (expRetCount < actualRetCount) {
            this.dlog.error(returnNode.pos, DiagnosticCode.TOO_MANY_RETURN_VALUES);
        } else {
            success = true;
        }
        return success;
    }

    private boolean isInvocationExpr(BLangExpression expr) {
        return expr.getKind() == NodeKind.INVOCATION;
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.exprs.size() == 1 && this.isInvocationExpr(returnNode.exprs.get(0))) {
            /* a single return expression can be expanded to match a multi-value return */
            this.typeChecker.checkExpr(returnNode.exprs.get(0), this.env,
                    this.env.enclInvokable.getReturnParameters().stream()
                            .map(e -> e.getTypeNode().type)
                            .collect(Collectors.toList()));
        } else {
            if (returnNode.exprs.size() == 0 && this.env.enclInvokable.getReturnParameters().size() > 0
                    && !this.env.enclInvokable.getReturnParameters().get(0).name.value.isEmpty()) {
                // Return stmt has no expressions, but function/action has returns and they are named returns.
                // Rewrite tree at desuger phase.
                returnNode.namedReturnVariables = this.env.enclInvokable.getReturnParameters();
                return;
            }
            if (this.checkReturnValueCounts(returnNode)) {
                for (int i = 0; i < returnNode.exprs.size(); i++) {
                    this.typeChecker.checkExpr(returnNode.exprs.get(i), this.env,
                            Arrays.asList(this.env.enclInvokable.getReturnParameters().get(i).getTypeNode().type));
                }
            }
        }
    }

    BType analyzeDef(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env);
    }

    BType analyzeStmt(BLangStatement stmtNode, SymbolEnv env) {
        return analyzeNode(stmtNode, env);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env, symTable.noType, null);
    }

    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        this.typeChecker.checkExpr(throwNode.expr, env, Lists.of(symTable.errStructType));
    }

    BType analyzeNode(BLangNode node, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = diagCode;
        node.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;

        return resType;
    }

    // Private methods

    private void handleAssignNodeWithVar(BLangAssignment assignNode) {
        int ignoredCount = 0;
        int createdSymbolCount = 0;

        List<Name> newVariables = new ArrayList<Name>();

        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression varRef = assignNode.varRefs.get(i);
            // If the assignment is declared with "var", then lhs supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }
            ((BLangVariableReference) varRef).lhsVar = true;
            // Check variable symbol if exists.
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                ignoredCount++;
                simpleVarRef.type = this.symTable.noType;
                expTypes.add(symTable.noType);
                typeChecker.checkExpr(simpleVarRef, env);
                continue;
            }
            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                createdSymbolCount++;
                newVariables.add(varName);
                expTypes.add(symTable.noType);
            } else {
                expTypes.add(symbol.type);
            }
        }

        if (ignoredCount == assignNode.varRefs.size() || createdSymbolCount == 0) {
            dlog.error(assignNode.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
        }
        // Check RHS expressions with expected type list.
        final List<BType> rhsTypes = typeChecker.checkExpr(assignNode.expr, this.env, expTypes);

        // visit all lhs expressions
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BType actualType = rhsTypes.get(i);
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) assignNode.varRefs.get(i);
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (newVariables.contains(varName)) {
                // define new variables
                this.symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), actualType, varName, env);
            }
            typeChecker.checkExpr(simpleVarRef, env);
        }
    }

    @Override
    public void visit(BLangComment commentNode) {
        // do nothing
    }
}
