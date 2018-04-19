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
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.TypeFlag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.clauses.GroupByNode;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
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
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;
import org.wso2.ballerinalang.util.TypeFlags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private EndpointSPIAnalyzer endpointSPIAnalyzer;
    private BLangDiagnosticLog dlog;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private BType resType;

    private Map<BLangBlockStmt, SymbolEnv> blockStmtEnvMap = new HashMap<>();

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
        this.endpointSPIAnalyzer = EndpointSPIAnalyzer.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
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
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        // Visit all the imported packages
        pkgNode.imports.forEach(importNode -> analyzeDef(importNode, pkgEnv));

        pkgNode.topLevelNodes.stream().filter(pkgLevelNode -> pkgLevelNode.getKind() != NodeKind.FUNCTION)
                .forEach(topLevelNode -> analyzeDef((BLangNode) topLevelNode, pkgEnv));

        analyzeFunctions(pkgNode.functions, pkgEnv);

        analyzeDef(pkgNode.initFunction, pkgEnv);
        analyzeDef(pkgNode.startFunction, pkgEnv);
        analyzeDef(pkgNode.stopFunction, pkgEnv);

        pkgNode.completedPhases.add(CompilerPhase.TYPE_CHECK);
    }

    private void analyzeFunctions(List<BLangFunction> functions, SymbolEnv pkgEnv) {
        //reversing the order here to process lambdas first - needed for analysing closures
        Collections.reverse(functions);
        functions.forEach(func -> {
            SymbolEnv symbolEnv;
            if (func.enclBlockStmt != null) {
                symbolEnv = blockStmtEnvMap.get(func.enclBlockStmt);
            } else {
                symbolEnv = pkgEnv;
            }
            analyzeDef(func, symbolEnv);
        });
        Collections.reverse(functions);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
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
        typeChecker.checkExpr(xmlnsNode.namespaceURI, env, symTable.stringType);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        //set function param flag to final
        funcNode.symbol.params.forEach(param -> param.flags |= Flags.FUNCTION_FINAL);

        funcNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.FUNCTION);
            this.analyzeDef(annotationAttachment, funcEnv);
        });

        funcNode.docAttachments.forEach(doc -> analyzeDef(doc, funcEnv));
        funcNode.requiredParams.forEach(p -> this.analyzeDef(p, funcEnv));
        funcNode.defaultableParams.forEach(p -> this.analyzeDef(p, funcEnv));
        if (funcNode.restParam != null) {
            this.analyzeDef(funcNode.restParam, funcEnv);
        }

        // Check for native functions
        if (Symbols.isNative(funcNode.symbol) || funcNode.interfaceFunction) {
            return;
        }

        funcNode.endpoints.forEach(e -> {
            symbolEnter.defineNode(e, funcEnv);
            analyzeDef(e, funcEnv);
        });
        analyzeStmt(funcNode.body, funcEnv);

        this.processWorkers(funcNode, funcEnv);
    }

    private void processWorkers(BLangInvokableNode invNode, SymbolEnv invEnv) {
        if (invNode.workers.size() > 0) {
            invEnv.scope.entries.putAll(invNode.body.scope.entries);
            invNode.workers.forEach(e -> this.symbolEnter.defineNode(e, invEnv));
            invNode.workers.forEach(e -> analyzeNode(e, invEnv));
        }
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeDef(field, structEnv));

        analyzeDef(structNode.initFunction, structEnv);
        structNode.docAttachments.forEach(doc -> analyzeDef(doc, structEnv));
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        BSymbol typeDefSymbol = typeDefinition.symbol;
        SymbolEnv typeDefEnv = SymbolEnv.createPkgLevelSymbolEnv(typeDefinition, typeDefSymbol.scope, env);

        typeDefinition.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.TYPE);
            annotationAttachment.accept(this);
        });

        typeDefinition.docAttachments.forEach(doc -> analyzeDef(doc, typeDefEnv));
    }


    public void visit(BLangObject objectNode) {
        BSymbol objectSymbol = objectNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createObjectEnv(objectNode, objectSymbol.scope, env);
        objectNode.fields.forEach(field -> analyzeDef(field, objectEnv));

        objectNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.OBJECT);
            annotationAttachment.accept(this);
        });
        objectNode.docAttachments.forEach(doc -> analyzeDef(doc, objectEnv));

        analyzeDef(objectNode.initFunction, objectEnv);

        validateConstructorAndCheckDefaultable(objectNode);

        //Visit temporary init statements in the init function
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(objectNode.initFunction,
                objectNode.initFunction.symbol.scope, objectEnv);
        objectNode.initFunction.initFunctionStmts.values().forEach(s -> analyzeNode(s, funcEnv));

        objectNode.functions.forEach(f -> analyzeDef(f, objectEnv));
    }

    @Override
    public void visit(BLangRecord record) {
        BSymbol structSymbol = record.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(record, structSymbol.scope, env);
        record.fields.forEach(field -> analyzeDef(field, structEnv));

        record.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.TYPE);
            annotationAttachment.accept(this);
        });

        analyzeDef(record.initFunction, structEnv);

        validateDefaultable(record);

        record.docAttachments.forEach(doc -> analyzeDef(doc, structEnv));
    }

    @Override
    public void visit(BLangEnum enumNode) {
        BSymbol enumSymbol = enumNode.symbol;
        SymbolEnv enumEnv = SymbolEnv.createPkgLevelSymbolEnv(enumNode, enumSymbol.scope, env);

        enumNode.docAttachments.forEach(doc -> analyzeDef(doc, enumEnv));
    }

    @Override
    public void visit(BLangDocumentation docNode) {
        Set<BLangIdentifier> visitedAttributes = new HashSet<>();
        for (BLangDocumentationAttribute attribute : docNode.attributes) {
            if (attribute.docTag == DocTag.ENDPOINT) {
                if (!this.env.enclObject.getFunctions().stream().anyMatch(bLangFunction -> "getClient".equals
                        (bLangFunction.getName().toString()))) {
                    this.dlog.warning(attribute.pos, DiagnosticCode.INVALID_USE_OF_ENDPOINT_DOCUMENTATION_ATTRIBUTE,
                            attribute.docTag.getValue());
                }
                continue;
            }
            if (attribute.docTag == DocTag.RETURN) {
                attribute.type = this.env.enclInvokable.returnTypeNode.type;
                // return params can't have names, hence can't validate
                continue;
            }
            if (!visitedAttributes.add(attribute.documentationField)) {
                this.dlog.warning(attribute.pos, DiagnosticCode.DUPLICATE_DOCUMENTED_ATTRIBUTE,
                        attribute.documentationField);
                continue;
            }
            Name attributeName = names.fromIdNode(attribute.documentationField);
            BSymbol attributeSymbol = this.env.scope.lookup(attributeName).symbol;
            if (attributeSymbol == null) {
                this.dlog.warning(attribute.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE,
                        attribute.documentationField, attribute.docTag.getValue());
                continue;
            }
            int ownerSymTag = env.scope.owner.tag;
            if ((ownerSymTag & SymTag.ANNOTATION) == SymTag.ANNOTATION) {
                if (attributeSymbol.tag != SymTag.ANNOTATION_ATTRIBUTE
                        || ((BAnnotationAttributeSymbol) attributeSymbol).docTag != attribute.docTag) {
                    this.dlog.warning(attribute.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE,
                            attribute.documentationField, attribute.docTag.getValue());
                    continue;
                }
            } else {
                if (!(attributeSymbol.tag == SymTag.VARIABLE || attributeSymbol.tag == SymTag.ENDPOINT) || (
                        (BVarSymbol) attributeSymbol).docTag != attribute.docTag) {
                    this.dlog.warning(attribute.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE, attribute
                            .documentationField, attribute.docTag.getValue());
                    continue;
                }
            }
            attribute.type = attributeSymbol.type;
        }
    }

    public void visit(BLangAnnotation annotationNode) {
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationNode.symbol.scope, env);
        annotationNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ANNOTATION);
            annotationAttachment.accept(this);
        });
        annotationNode.docAttachments.forEach(doc -> analyzeDef(doc, annotationEnv));
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
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_NOT_ALLOWED,
                        annotationSymbol, msg);
            }
        }
        // Validate Annotation Attachment data struct against Annotation Definition struct.
        validateAnnotationAttachmentExpr(annAttachmentNode, annotationSymbol);
    }

    private void validateAnnotationAttachmentExpr(BLangAnnotationAttachment annAttachmentNode, BAnnotationSymbol
            annotationSymbol) {
        if (annotationSymbol.attachedType == null) {
            if (annAttachmentNode.expr != null) {
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_ATTACHMENT_NO_VALUE,
                        annotationSymbol.name);
            }
            return;
        }
        if (annAttachmentNode.expr != null) {
            this.typeChecker.checkExpr(annAttachmentNode.expr, env, annotationSymbol.attachedType.type);
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

        varNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.TYPE);
            annotationAttachment.accept(this);
        });

        BType lhsType = varNode.symbol.type;
        varNode.type = lhsType;

        // Here we validate annotation attachments for package level variables.
        // Here we validate document attachments for package level variables.
        varNode.docAttachments.forEach(doc -> {
            doc.accept(this);
        });

        // Analyze the init expression
        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr == null) {
            if (varNode.symbol.owner.tag == SymTag.PACKAGE && !types.defaultValueExists(varNode.pos, varNode.type)) {
                dlog.error(varNode.pos, DiagnosticCode.UNINITIALIZED_VARIABLE, varNode.name);
            }
            return;
        }

        // Here we create a new symbol environment to catch self references by keep the current
        // variable symbol in the symbol environment
        // e.g. int a = x + a;
        SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);

        // Return if this not a safe assignment
        if (!varNode.safeAssignment) {
            typeChecker.checkExpr(rhsExpr, varInitEnv, lhsType);
            return;
        }

        handleSafeAssignment(varNode.pos, lhsType, rhsExpr, varInitEnv);
    }


    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockStmtEnvMap.put(blockNode, blockEnv);
        blockNode.stmts.forEach(stmt -> analyzeStmt(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        analyzeDef(varDefNode.var, env);

        // Check whether variable is initialized, if the type don't support default values.
        // eg: struct types.
        if (varDefNode.var.expr == null && !types.defaultValueExists(varDefNode.pos, varDefNode.var.type)) {
            dlog.error(varDefNode.pos, DiagnosticCode.UNINITIALIZED_VARIABLE, varDefNode.var.name);
        }
    }

    public void visit(BLangPostIncrement postIncrement) {
        BLangExpression varRef = postIncrement.varRef;
        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                varRef.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            if (postIncrement.opKind == OperatorKind.ADD) {
                dlog.error(varRef.pos, DiagnosticCode.OPERATOR_NOT_ALLOWED_VARIABLE,
                        OperatorKind.INCREMENT, varRef);
            } else {
                dlog.error(varRef.pos, DiagnosticCode.OPERATOR_NOT_ALLOWED_VARIABLE,
                        OperatorKind.DECREMENT, varRef);
            }
            return;
        } else {
            this.typeChecker.checkExpr(varRef, env);
        }
        this.typeChecker.checkExpr(postIncrement.increment, env);
        if (varRef.type == symTable.intType || varRef.type == symTable.floatType) {
            BSymbol opSymbol = this.symResolver.resolveBinaryOperator(postIncrement.opKind, varRef.type,
                    postIncrement.increment.type);
            postIncrement.modifiedExpr = getBinaryExpr(varRef,
                    postIncrement.increment,
                    postIncrement.opKind,
                    opSymbol);
        } else {
            if (postIncrement.opKind == OperatorKind.ADD) {
                dlog.error(varRef.pos, DiagnosticCode.OPERATOR_NOT_SUPPORTED,
                        OperatorKind.INCREMENT, varRef.type);
            } else {
                dlog.error(varRef.pos, DiagnosticCode.OPERATOR_NOT_SUPPORTED,
                        OperatorKind.DECREMENT, varRef.type);
            }
        }
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {
        List<BType> expTypes = new ArrayList<>();
        BLangExpression varRef = compoundAssignment.varRef;
        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                varRef.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
            expTypes.add(symTable.errType);
        } else {
            this.typeChecker.checkExpr(varRef, env);
            expTypes.add(varRef.type);
        }
        this.typeChecker.checkExpr(compoundAssignment.expr, env);
        if (expTypes.get(0) != symTable.errType && compoundAssignment.expr.type != symTable.errType) {
            BSymbol opSymbol = this.symResolver.resolveBinaryOperator(compoundAssignment.opKind, expTypes.get(0),
                    compoundAssignment.expr.type);
            if (opSymbol == symTable.notFoundSymbol) {
                dlog.error(compoundAssignment.pos, DiagnosticCode.BINARY_OP_INCOMPATIBLE_TYPES,
                        compoundAssignment.opKind, expTypes.get(0), compoundAssignment.expr.type);
            } else {
                compoundAssignment.modifiedExpr = getBinaryExpr(varRef,
                        compoundAssignment.expr,
                        compoundAssignment.opKind,
                        opSymbol);
                this.types.checkTypes(compoundAssignment.modifiedExpr,
                        Lists.of(compoundAssignment.modifiedExpr.type), expTypes);
            }
        }
    }

    public void visit(BLangAssignment assignNode) {
        if (assignNode.isDeclaredWithVar()) {
            handleAssignNodeWithVar(assignNode.pos, assignNode.varRef, assignNode.safeAssignment, assignNode.expr);
            return;
        }

        // Check each LHS expression.
        BType expType = getTypeOfVarReferenceInAssignment(assignNode.varRef);
        if (!assignNode.safeAssignment) {
            typeChecker.checkExpr(assignNode.expr, this.env, expType);
            return;
        }

        // Assume that there is only one variable reference in LHS
        // Continue the validate if this is a safe assignment operator
        handleSafeAssignment(assignNode.pos, assignNode.varRef.type, assignNode.expr, this.env);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDeStmt) {
        if (tupleDeStmt.isDeclaredWithVar()) {
            handleAssignNodeWithVarDeStructure(tupleDeStmt);
            return;
        }

        // Check each LHS expression.
        List<BType> expTypes = new ArrayList<>();
        for (BLangExpression expr : tupleDeStmt.varRefs) {
            expTypes.add(getTypeOfVarReferenceInAssignment(expr));
        }

        expType = new BTupleType(expTypes);
        typeChecker.checkExpr(tupleDeStmt.expr, this.env, expType);
    }

    public void visit(BLangBind bindNode) {
        BType expType;
        // Check each LHS expression.
        BLangExpression varRef = bindNode.varRef;
        ((BLangVariableReference) varRef).lhsVar = true;
        expType = typeChecker.checkExpr(varRef, env);
        checkConstantAssignment(varRef);
        typeChecker.checkExpr(bindNode.expr, this.env, expType);
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
        if (!Names.IGNORE.equals(varName) && env.enclInvokable != env.enclPkg.initFunction) {
            if ((simpleVarRef.symbol.flags & Flags.FINAL) == Flags.FINAL) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FINAL, varRef);
            } else if ((simpleVarRef.symbol.flags & Flags.FUNCTION_FINAL) == Flags.FUNCTION_FINAL) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FUNCTION_ARGUMENT, varRef);
            }
        }
    }

    private void checkReadonlyAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.errType) {
            return;
        }

        BLangVariableReference varRefExpr = (BLangVariableReference) varRef;
        if (varRefExpr.symbol != null) {
            if (env.enclPkg.symbol.pkgID != varRefExpr.symbol.pkgID && varRefExpr.lhsVar
                    && (varRefExpr.symbol.flags & Flags.READONLY) == Flags.READONLY) {
                dlog.error(varRefExpr.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_READONLY, varRefExpr);
            }
        }
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        BType bType = typeChecker.checkExpr(exprStmtNode.expr, stmtEnv, symTable.noType);
        if (bType != symTable.nilType && bType != symTable.errType) {
            dlog.error(exprStmtNode.pos, DiagnosticCode.ASSIGNMENT_REQUIRED);
        }
    }

    public void visit(BLangIf ifNode) {
        typeChecker.checkExpr(ifNode.expr, env, symTable.booleanType);
        analyzeStmt(ifNode.body, env);

        if (ifNode.elseStmt != null) {
            analyzeStmt(ifNode.elseStmt, env);
        }
    }

    public void visit(BLangMatch matchNode) {
        List<BType> exprTypes;
        BType exprType = typeChecker.checkExpr(matchNode.expr, env, symTable.noType);
        if (exprType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) exprType;
            exprTypes = new ArrayList<>(unionType.memberTypes);
        } else {
            exprTypes = Lists.of(exprType);
        }

        //  visit patterns
        matchNode.patternClauses.forEach(patternClause -> patternClause.accept(this));
        matchNode.exprTypes = exprTypes;
    }

    public void visit(BLangMatchStmtPatternClause patternClause) {
        // If the variable is not equal to '_', then define the variable in the block scope
        if (!patternClause.variable.name.value.endsWith(Names.IGNORE.value)) {
            SymbolEnv blockEnv = SymbolEnv.createBlockEnv((BLangBlockStmt) patternClause.body, env);
            symbolEnter.defineNode(patternClause.variable, blockEnv);
            analyzeStmt(patternClause.body, blockEnv);
            return;
        }

        symbolEnter.defineNode(patternClause.variable, this.env);
        analyzeStmt(patternClause.body, this.env);
    }

    public void visit(BLangForeach foreach) {
        typeChecker.checkExpr(foreach.collection, env);
        foreach.varTypes = types.checkForeachTypes(foreach.collection, foreach.varRefs.size());
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        handleForeachVariables(foreach, foreach.varTypes, blockEnv);
        analyzeStmt(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        typeChecker.checkExpr(whileNode.expr, env, symTable.booleanType);
        analyzeStmt(whileNode.body, env);
    }

    @Override
    public void visit(BLangLock lockNode) {
        analyzeStmt(lockNode.body, env);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        connectorNode.docAttachments.forEach(doc -> analyzeDef(doc, connectorEnv));

        connectorNode.params.forEach(param -> this.analyzeDef(param, connectorEnv));
        connectorNode.varDefs.forEach(varDef -> this.analyzeDef(varDef, connectorEnv));
        connectorNode.endpoints.forEach(e -> analyzeDef(e, connectorEnv));
        this.analyzeDef(connectorNode.initFunction, connectorEnv);
        connectorNode.actions.forEach(action -> this.analyzeDef(action, connectorEnv));
        this.analyzeDef(connectorNode.initAction, connectorEnv);
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;

        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        actionNode.docAttachments.forEach(doc -> analyzeDef(doc, actionEnv));

        if (Symbols.isNative(actionSymbol)) {
            return;
        }

        actionNode.requiredParams.forEach(p -> this.analyzeDef(p, actionEnv));
        actionNode.defaultableParams.forEach(p -> this.analyzeDef(p, actionEnv));
        if (actionNode.restParam != null) {
            this.analyzeDef(actionNode.restParam, actionEnv);
        }

        actionNode.endpoints.forEach(e -> analyzeDef(e, actionEnv));
        analyzeStmt(actionNode.body, actionEnv);
        this.processWorkers(actionNode, actionEnv);
    }

    public void visit(BLangService serviceNode) {
        BServiceSymbol serviceSymbol = (BServiceSymbol) serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceSymbol.scope, env);
        handleServiceTypeStruct(serviceNode);
        handleServiceEndpointBinds(serviceNode, serviceSymbol);
        handleAnonymousEndpointBind(serviceNode);
        serviceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.SERVICE);
            this.analyzeDef(a, serviceEnv);
        });
        serviceNode.docAttachments.forEach(doc -> analyzeDef(doc, serviceEnv));
        serviceNode.vars.forEach(v -> this.analyzeDef(v, serviceEnv));
        serviceNode.endpoints.forEach(e -> {
            symbolEnter.defineNode(e, serviceEnv);
            analyzeDef(e, serviceEnv);
        });
        this.analyzeDef(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(r -> this.analyzeDef(r, serviceEnv));
    }

    private void handleServiceTypeStruct(BLangService serviceNode) {
        if (serviceNode.serviceTypeStruct == null) {
            return;
        }
        final BType serviceStructType = symResolver.resolveTypeNode(serviceNode.serviceTypeStruct, env);
        serviceNode.endpointType = endpointSPIAnalyzer.getEndpointTypeFromServiceType(
                serviceNode.serviceTypeStruct.pos, serviceStructType);
        if (serviceNode.endpointType != null) {
            serviceNode.endpointClientType = endpointSPIAnalyzer.getClientType(
                    (BStructSymbol) serviceNode.endpointType.tsymbol);
        }
    }

    private void handleServiceEndpointBinds(BLangService serviceNode, BServiceSymbol serviceSymbol) {
        for (BLangSimpleVarRef ep : serviceNode.boundEndpoints) {
            typeChecker.checkExpr(ep, env);
            if (ep.symbol == null || (ep.symbol.tag & SymTag.ENDPOINT) != SymTag.ENDPOINT) {
                dlog.error(ep.pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, ep.variableName);
                continue;
            }
            final BEndpointVarSymbol epSym = (BEndpointVarSymbol) ep.symbol;
            if ((epSym.tag & SymTag.ENDPOINT) == SymTag.ENDPOINT) {
                if (epSym.registrable) {
                    serviceSymbol.boundEndpoints.add(epSym);
                    if (serviceNode.endpointType == null) {
                        serviceNode.endpointType = (BStructType) epSym.type;
                        serviceNode.endpointClientType = endpointSPIAnalyzer.getClientType(
                                (BStructSymbol) serviceNode.endpointType.tsymbol);
                    }
                    // TODO : Validate serviceType endpoint type with bind endpoint types.
                } else {
                    dlog.error(ep.pos, DiagnosticCode.ENDPOINT_NOT_SUPPORT_REGISTRATION, epSym);
                }
            } else {
                dlog.error(ep.pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, epSym);
            }
        }
        if (serviceNode.endpointType == null) {
            dlog.error(serviceNode.pos, DiagnosticCode.SERVICE_INVALID_ENDPOINT_TYPE, serviceNode.name);
        }
    }

    private void handleAnonymousEndpointBind(BLangService serviceNode) {
        if (serviceNode.anonymousEndpointBind == null) {
            return;
        }
        if (serviceNode.endpointType == null) {
            dlog.error(serviceNode.pos, DiagnosticCode.SERVICE_SERVICE_TYPE_REQUIRED_ANONYMOUS, serviceNode.name);
            return;
        }
        this.typeChecker.checkExpr(serviceNode.anonymousEndpointBind, env,
                endpointSPIAnalyzer.getEndpointConfigType((BStructSymbol) serviceNode.endpointType.tsymbol));
    }

    private void validateConstructorAndCheckDefaultable(BLangObject objectNode) {
        boolean defaultableStatus = true;
        for (BLangVariable field : objectNode.fields) {
            if (field.expr != null || types.defaultValueExists(field.pos, field.symbol.type)) {
                continue;
            }
            defaultableStatus = false;
            if (objectNode.initFunction.symbol.params.stream().filter(p -> p.field ? p.originalName.equals(field
                    .symbol.name) : p.name.equals(field.symbol.name)).collect(Collectors.toList()).size() == 0) {
                dlog.error(objectNode.pos, DiagnosticCode.OBJECT_UN_INITIALIZABLE_FIELD, field);
            }
        }

        if (objectNode.initFunction.symbol.params.size() > 0) {
            defaultableStatus = false;
        }
        if (defaultableStatus) {
            objectNode.symbol.type.flags = TypeFlags.asMask(EnumSet.of(TypeFlag.DEFAULTABLE_CHECKED,
                    TypeFlag.DEFAULTABLE));
        } else {
            objectNode.symbol.type.flags = TypeFlags.asMask(EnumSet.of(TypeFlag.DEFAULTABLE_CHECKED));
        }
    }

    private void validateDefaultable(BLangRecord recordNode) {
        boolean defaultableStatus = true;
        for (BLangVariable field : recordNode.fields) {
            if (field.expr != null || types.defaultValueExists(field.pos, field.symbol.type)) {
                continue;
            }
            defaultableStatus = false;
            break;
        }
        if (defaultableStatus) {
            recordNode.symbol.type.flags = TypeFlags.asMask(EnumSet.of(TypeFlag.DEFAULTABLE_CHECKED,
                    TypeFlag.DEFAULTABLE));
        } else {
            recordNode.symbol.type.flags = TypeFlags.asMask(EnumSet.of(TypeFlag.DEFAULTABLE_CHECKED));
        }
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        resourceNode.annAttachments.forEach(a -> {
            a.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.RESOURCE);
            this.analyzeDef(a, resourceEnv);
        });
        defineResourceEndpoint(resourceNode, resourceEnv);
        resourceNode.docAttachments.forEach(doc -> analyzeDef(doc, resourceEnv));
        resourceNode.requiredParams.forEach(p -> analyzeDef(p, resourceEnv));
        resourceNode.endpoints.forEach(e -> {
            symbolEnter.defineNode(e, resourceEnv);
            analyzeDef(e, resourceEnv);
        });
        analyzeStmt(resourceNode.body, resourceEnv);
        this.processWorkers(resourceNode, resourceEnv);
    }

    private void defineResourceEndpoint(BLangResource resourceNode, SymbolEnv resourceEnv) {
        if (!resourceNode.getParameters().isEmpty()) {
            final BLangVariable variable = resourceNode.getParameters().get(0);
            if (variable.type == symTable.endpointType) {
                String actualVarName = variable.name.value.substring(1);
                variable.name = new BLangIdentifier();
                variable.name.value = actualVarName;
                if (resourceEnv.enclService.endpointType != null) {
                    variable.type = resourceEnv.enclService.endpointType;
                    final BEndpointVarSymbol bEndpointVarSymbol = symbolEnter.defineEndpointVarSymbol(variable.pos,
                            EnumSet.noneOf(Flag.class), variable.type, names.fromString(actualVarName), resourceEnv);
                    variable.symbol = bEndpointVarSymbol;
                    if (variable.type.tsymbol.kind == SymbolKind.OBJECT
                            || variable.type.tsymbol.kind == SymbolKind.RECORD) {
                        endpointSPIAnalyzer.populateEndpointSymbol((BStructSymbol) variable.type.tsymbol,
                                bEndpointVarSymbol);
                    }
                } else {
                    variable.type = symTable.errType;
                    variable.symbol = symbolEnter.defineVarSymbol(variable.pos, EnumSet.noneOf(Flag.class),
                            variable.type, names.fromString(actualVarName), resourceEnv);
                }
                // Replace old symbol with new one.
                resourceNode.symbol.params.remove(0);
                resourceNode.symbol.params.add(0, variable.symbol);
                ((BInvokableType) resourceNode.symbol.type).paramTypes.remove(0);
                ((BInvokableType) resourceNode.symbol.type).paramTypes.add(0, variable.type);
            }
        }
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
        if (!this.types.checkStructEquivalency(bLangCatch.param.type, symTable.errStructType)) {
            dlog.error(bLangCatch.param.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errStructType,
                    bLangCatch.param.type);
        }
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeStmt(transactionNode.transactionBody, env);
        if (transactionNode.onRetryBody != null) {
            analyzeStmt(transactionNode.onRetryBody, env);
        }
        if (transactionNode.retryCount != null) {
            typeChecker.checkExpr(transactionNode.retryCount, env, symTable.intType);
            checkRetryStmtValidity(transactionNode.retryCount);
        }

        if (transactionNode.onCommitFunction != null) {
            typeChecker.checkExpr(transactionNode.onCommitFunction, env, symTable.noType);
            checkTransactionHandlerValidity(transactionNode.onCommitFunction);
        }

        if (transactionNode.onAbortFunction != null) {
            typeChecker.checkExpr(transactionNode.onAbortFunction, env, symTable.noType);
            checkTransactionHandlerValidity(transactionNode.onAbortFunction);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangDone doneNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangRetry retryNode) {
        /* ignore */
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
                    timeoutVarEnv, symTable.intType);
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

    @Override
    public void visit(BLangEndpoint endpointNode) {
        endpointNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachmentPoint =
                    new BLangAnnotationAttachmentPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ENDPOINT);
            this.analyzeDef(annotationAttachment, env);
        });
        if (endpointNode.configurationExpr == null) {
            return;
        }
        BType configType = symTable.errType;
        if (endpointNode.symbol != null && endpointNode.symbol.type.tag == TypeTags.STRUCT) {
            if (endpointNode.configurationExpr.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
                // Init expression.
                configType = endpointSPIAnalyzer.getEndpointConfigType(
                        (BStructSymbol) endpointNode.symbol.type.tsymbol);
            } else {
                // assign Expression.
                configType = endpointNode.symbol.type;
            }
        }
        this.typeChecker.checkExpr(endpointNode.configurationExpr, env, configType);
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
        this.typeChecker.checkExpr(workerSendNode.expr, this.env);
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
        this.typeChecker.checkExpr(workerReceiveNode.expr, this.env);
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
        }
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (this.env.enclInvokable.getKind() == NodeKind.RESOURCE) {
            return;
        }

        this.typeChecker.checkExpr(returnNode.expr, this.env,
                this.env.enclInvokable.returnTypeNode.type);
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

    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        this.typeChecker.checkExpr(throwNode.expr, env);
        if (!types.checkStructEquivalency(throwNode.expr.type, symTable.errStructType)) {
            dlog.error(throwNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errStructType,
                    throwNode.expr.type);
        }
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        transformerNode.docAttachments.forEach(doc -> analyzeDef(doc, transformerEnv));

        validateTransformerMappingType(transformerNode.source);
        validateTransformerMappingType(transformerNode.retParams.get(0));

        analyzeStmt(transformerNode.body, transformerEnv);

        // TODO: update this accordingly once the unsafe conversion are supported
        int returnCount = transformerNode.retParams.size();
        if (returnCount == 0) {
            dlog.error(transformerNode.pos, DiagnosticCode.TRANSFORMER_MUST_HAVE_OUTPUT);
        }

        this.processWorkers(transformerNode, transformerEnv);
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


    //Streaming related methods.

    public void visit(BLangForever foreverStatement) {
        for (StreamingQueryStatementNode streamingQueryStatement : foreverStatement.getStreamingQueryStatements()) {
            analyzeStmt((BLangStatement) streamingQueryStatement, env);
        }

        //Validate output attribute names with stream/struct
        for (StreamingQueryStatementNode streamingQueryStatement : foreverStatement.getStreamingQueryStatements()) {
            checkOutputAttributes((BLangStatement) streamingQueryStatement);
            validateOutputAttributeTypes((BLangStatement) streamingQueryStatement);
        }
    }

    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
            JoinStreamingInput joinStreamingInput = streamingQueryStatement.getJoiningInput();
            if (joinStreamingInput != null) {
                ((BLangJoinStreamingInput) joinStreamingInput).accept(this);
            }
        }

        SelectClauseNode selectClauseNode = streamingQueryStatement.getSelectClause();
        if (selectClauseNode != null) {
            ((BLangSelectClause) selectClauseNode).accept(this);
        }


        OrderByNode orderByNode = streamingQueryStatement.getOrderbyClause();
        if (orderByNode != null) {
            ((BLangOrderBy) orderByNode).accept(this);
        }

        StreamActionNode streamActionNode = streamingQueryStatement.getStreamingAction();
        if (streamActionNode != null) {
            ((BLangStreamAction) streamActionNode).accept(this);
        }

        BLangPatternClause patternClause = (BLangPatternClause) streamingQueryStatement.getPatternClause();
        if (patternClause != null) {
            patternClause.accept(this);
        }
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        BLangPatternStreamingInput patternStreamingInput = (BLangPatternStreamingInput) patternClause
                .getPatternStreamingNode();
        patternStreamingInput.accept(this);
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs = patternStreamingInput
                .getPatternStreamingEdgeInputs();
        for (PatternStreamingEdgeInputNode inputNode : patternStreamingEdgeInputs) {
            BLangPatternStreamingEdgeInput streamingInput = (BLangPatternStreamingEdgeInput) inputNode;
            streamingInput.accept(this);
        }

        BLangPatternStreamingInput nestedPatternStreamingInput = (BLangPatternStreamingInput) patternStreamingInput
                .getPatternStreamingInput();
        if (nestedPatternStreamingInput != null) {
            nestedPatternStreamingInput.accept(this);
        }
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        BLangVariableReference streamRef = (BLangVariableReference) patternStreamingEdgeInput.getStreamReference();
        typeChecker.checkExpr(streamRef, env);

        BLangWhere where = (BLangWhere) patternStreamingEdgeInput.getWhereClause();
        if (where != null) {
            where.accept(this);
        }
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        BLangExpression streamRef = (BLangExpression) streamingInput.getStreamReference();
        typeChecker.checkExpr(streamRef, env);

        WhereNode beforeWhereNode = streamingInput.getBeforeStreamingCondition();
        if (beforeWhereNode != null) {
            ((BLangWhere) beforeWhereNode).accept(this);
        }

        WindowClauseNode windowClauseNode = streamingInput.getWindowClause();
        if (windowClauseNode != null) {
            ((BLangWindow) windowClauseNode).accept(this);
        }

        WhereNode afterWhereNode = streamingInput.getAfterStreamingCondition();
        if (afterWhereNode != null) {
            ((BLangWhere) afterWhereNode).accept(this);
        }
    }

    @Override
    public void visit(BLangWindow windowClause) {
        ExpressionNode expressionNode = windowClause.getFunctionInvocation();
        ((BLangExpression) expressionNode).accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        VariableReferenceNode variableReferenceNode = invocationExpr.getExpression();
        if (variableReferenceNode != null) {
            ((BLangVariableReference) variableReferenceNode).accept(this);
        }
    }

    @Override
    public void visit(BLangWhere whereClause) {
        ExpressionNode expressionNode = whereClause.getExpression();
        ((BLangExpression) expressionNode).accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        ExpressionNode leftExpression = binaryExpr.getLeftExpression();
        ((BLangExpression) leftExpression).accept(this);

        ExpressionNode rightExpression = binaryExpr.getRightExpression();
        ((BLangExpression) rightExpression).accept(this);
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        GroupByNode groupByNode = selectClause.getGroupBy();
        if (groupByNode != null) {
            ((BLangGroupBy) groupByNode).accept(this);
        }

        HavingNode havingNode = selectClause.getHaving();
        if (havingNode != null) {
            ((BLangHaving) havingNode).accept(this);
        }

        List<? extends SelectExpressionNode> selectExpressionsList = selectClause.getSelectExpressions();
        if (selectExpressionsList != null) {
            for (SelectExpressionNode selectExpressionNode : selectExpressionsList) {
                ((BLangSelectExpression) selectExpressionNode).accept(this);
            }
        }
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> variableExpressionList = groupBy.getVariables();
        for (ExpressionNode expressionNode : variableExpressionList) {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangHaving having) {
        ExpressionNode expressionNode = having.getExpression();
        if (expressionNode != null) {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends OrderByVariableNode> orderByVariableList = orderBy.getVariables();
        for (OrderByVariableNode orderByVariableNode : orderByVariableList) {
            ((BLangOrderByVariable) orderByVariableNode).accept(this);
        }
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        BLangExpression expression = (BLangExpression) orderByVariable.getVariableReference();
        expression.accept(this);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        ExpressionNode expressionNode = selectExpression.getExpression();
        ((BLangExpression) expressionNode).accept(this);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction function = (BLangLambdaFunction) streamAction.getInvokableBody();
        typeChecker.checkExpr(function, env);
        validateStreamingActionFunctionParameters(streamAction);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        StreamingInput streamingInput = joinStreamingInput.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
        }

        ExpressionNode expressionNode = joinStreamingInput.getOnExpression();
        if (expressionNode != null) {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        ExpressionNode expressionNode = setAssignmentClause.getExpressionNode();
        ((BLangExpression) expressionNode).accept(this);

        ExpressionNode variableReference = setAssignmentClause.getVariableReference();
        ((BLangExpression) variableReference).accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        VariableReferenceNode variableReferenceNode = fieldAccessExpr.getExpression();
        ((BLangVariableReference) variableReferenceNode).accept(this);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // ignore
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // ignore
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        //ignore
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        //ignore
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        /* ignore */
    }

    // Private methods

    private void handleForeachVariables(BLangForeach foreachStmt, List<BType> varTypes, SymbolEnv env) {
        for (int i = 0; i < foreachStmt.varRefs.size(); i++) {
            BLangExpression varRef = foreachStmt.varRefs.get(i);
            // foreach variables supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                continue;
            }
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            simpleVarRef.lhsVar = true;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                simpleVarRef.type = this.symTable.noType;
                typeChecker.checkExpr(simpleVarRef, env);
                continue;
            }
            // Check variable symbol for existence.
            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), varTypes.get(i), varName, env);
                typeChecker.checkExpr(simpleVarRef, env);
            } else {
                dlog.error(simpleVarRef.pos, DiagnosticCode.REDECLARED_SYMBOL, varName);
            }
        }
    }

    private void handleAssignNodeWithVar(DiagnosticPos pos,
                                         BLangExpression varRefExpr,
                                         boolean safeAssignment,
                                         BLangExpression rhsExpr) {
        // The lhs supports only simpleVarRef expression only.
        if (varRefExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRefExpr);
            varRefExpr.type = this.symTable.errType;
            return;
        }
        validateVariableDefinition(rhsExpr);
        BType rhsType = typeChecker.checkExpr(rhsExpr, this.env, expType);
        if (safeAssignment) {
            rhsType = handleSafeAssignmentWithVarDeclaration(varRefExpr.pos, rhsType);
        }

        // Check variable symbol if exists.
        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRefExpr;
        simpleVarRef.lhsVar = true;
        Name varName = names.fromIdNode(simpleVarRef.variableName);
        if (varName != Names.IGNORE) {
            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol != symTable.notFoundSymbol) {
                dlog.error(simpleVarRef.pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
                return;
            }
        }
        // Define the new variable
        BVarSymbol varSymbol = this.symbolEnter.defineVarSymbol(simpleVarRef.pos,
                Collections.emptySet(), rhsType, varName, env);
        simpleVarRef.symbol = varSymbol;
        simpleVarRef.type = varSymbol.type;
    }

    private void handleAssignNodeWithVarDeStructure(BLangTupleDestructure tupleDeNode) {
        int ignoredCount = 0;

        List<Name> newVariables = new ArrayList<Name>();
        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        for (int i = 0; i < tupleDeNode.varRefs.size(); i++) {
            BLangExpression varRef = tupleDeNode.varRefs.get(i);
            // If the assignment is declared with "var", then lhs supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }
            // Check variable symbol if exists.
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            ((BLangVariableReference) varRef).lhsVar = true;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                ignoredCount++;
                simpleVarRef.type = this.symTable.noType;
                expTypes.add(symTable.noType);
                continue;
            }

            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                newVariables.add(varName);
                expTypes.add(symTable.noType);
            } else {
                dlog.error(varRef.pos, DiagnosticCode.REDECLARED_SYMBOL, symbol.name);
                expTypes.add(symbol.type);
            }
        }

        if (ignoredCount == tupleDeNode.varRefs.size()) {
            dlog.error(tupleDeNode.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
        }

        List<BType> rhsTypes;
        BType expType = new BTupleType(expTypes);
        BType rhsType = typeChecker.checkExpr(tupleDeNode.expr, this.env, expType);
        if (rhsType != symTable.errType && rhsType.tag == TypeTags.TUPLE) {
            BTupleType tupleType = (BTupleType) rhsType;
            rhsTypes = tupleType.tupleTypes;
        } else {
            dlog.error(tupleDeNode.pos, DiagnosticCode.INCOMPATIBLE_TYPES_EXP_TUPLE, rhsType);
            rhsTypes = typeChecker.getListWithErrorTypes(tupleDeNode.varRefs.size());
        }

        // visit all lhs expressions
        for (int i = 0; i < tupleDeNode.varRefs.size(); i++) {
            BLangExpression varRef = tupleDeNode.varRefs.get(i);
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                continue;
            }
            BType actualType = rhsTypes.get(i);
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (newVariables.contains(varName)) {
                // define new variables
                this.symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), actualType, varName, env);
            }
            typeChecker.checkExpr(simpleVarRef, env);
        }
    }

    private void checkRetryStmtValidity(BLangExpression retryCountExpr) {
        boolean error = true;
        NodeKind retryKind = retryCountExpr.getKind();
        if (retryKind == NodeKind.LITERAL) {
            if (retryCountExpr.type.tag == TypeTags.INT) {
                int retryCount = Integer.parseInt(((BLangLiteral) retryCountExpr).getValue().toString());
                if (retryCount >= 0) {
                    error = false;
                }
            }
        } else if (retryKind == NodeKind.SIMPLE_VARIABLE_REF) {
            if (((BLangSimpleVarRef) retryCountExpr).symbol.flags == Flags.FINAL) {
                if (((BLangSimpleVarRef) retryCountExpr).symbol.type.tag == TypeTags.INT) {
                    error = false;
                }
            }
        }
        if (error) {
            this.dlog.error(retryCountExpr.pos, DiagnosticCode.INVALID_RETRY_COUNT);
        }
    }

    private void checkTransactionHandlerValidity(BLangExpression transactionHanlder) {
        if (transactionHanlder != null) {
            if (transactionHanlder.type.tag == TypeTags.INVOKABLE) {
                BInvokableType handlerType = (BInvokableType) transactionHanlder.type;
                int parameterCount = handlerType.paramTypes.size();
                if (parameterCount != 1) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_ARGS);
                }
                if (handlerType.paramTypes.get(0).tag != TypeTags.STRING) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_ARGS);
                }
            } else {
                dlog.error(transactionHanlder.pos, DiagnosticCode.LAMBDA_REQUIRED_FOR_TRANSACTION_HANDLER);
            }
        }
    }

    private void validateTransformerMappingType(BLangVariable param) {
        BType type = param.type;
        if (types.isValueType(type) || (type instanceof BBuiltInRefType) || type.tag == TypeTags.STRUCT) {
            return;
        }

        dlog.error(param.pos, DiagnosticCode.TRANSFORMER_UNSUPPORTED_TYPES, type);
    }

    private BLangExpression getBinaryExpr(BLangExpression lExpr,
                                          BLangExpression rExpr,
                                          OperatorKind opKind,
                                          BSymbol opSymbol) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.lhsExpr = lExpr;
        binaryExpressionNode.rhsExpr = rExpr;
        binaryExpressionNode.pos = rExpr.pos;
        binaryExpressionNode.opKind = opKind;
        if (opSymbol != symTable.notFoundSymbol) {
            binaryExpressionNode.type = opSymbol.type.getReturnType();
            binaryExpressionNode.opSymbol = (BOperatorSymbol) opSymbol;
        } else {
            binaryExpressionNode.type = symTable.errType;
        }
        return binaryExpressionNode;
    }

    private void validateVariableDefinition(BLangExpression expr) {
        // following cases are invalid.
        // var a = [ x, y, ... ];
        // var a = { x : y };
        // var a = new ;
        final NodeKind kind = expr.getKind();
        if (kind == NodeKind.RECORD_LITERAL_EXPR || kind == NodeKind.ARRAY_LITERAL_EXPR
                || (kind == NodeKind.Type_INIT_EXPR && ((BLangTypeInit) expr).userDefinedType == null)) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_ANY_VAR_DEF);
        }
    }

    private void handleSafeAssignment(DiagnosticPos lhsPos, BType lhsType, BLangExpression rhsExpr, SymbolEnv env) {
        // Collect all the lhs types
        Set<BType> lhsTypes = lhsType.tag == TypeTags.UNION ?
                ((BUnionType) lhsType).memberTypes :
                new LinkedHashSet<BType>() {{
                    add(lhsType);
                }};

        // If there is at least one lhs type which assignable to the error type, then report an error.
        for (BType type : lhsTypes) {
            if (types.isAssignable(symTable.errStructType, type)) {
                dlog.error(lhsPos, DiagnosticCode.SAFE_ASSIGN_STMT_INVALID_USAGE);
                typeChecker.checkExpr(rhsExpr, env, symTable.errType);
                return;
            }
        }

        // Create a new union type with the error type and continue the type checking process.
        lhsTypes.add(symTable.errStructType);
        BUnionType lhsUnionType = new BUnionType(null, lhsTypes, lhsTypes.contains(symTable.nilType));
        typeChecker.checkExpr(rhsExpr, env, lhsUnionType);

        if (rhsExpr.type.tag == TypeTags.UNION) {
            BUnionType rhsUnionType = (BUnionType) rhsExpr.type;
            for (BType type : rhsUnionType.memberTypes) {
                if (types.isAssignable(symTable.errStructType, type)) {
                    return;
                }
            }
        } else if (rhsExpr.type.tag != TypeTags.ERROR) {
            dlog.error(rhsExpr.pos, DiagnosticCode.SAFE_ASSIGN_STMT_INVALID_USAGE);
        }
    }

    private BType handleSafeAssignmentWithVarDeclaration(DiagnosticPos pos, BType rhsType) {
        if (rhsType.tag != TypeTags.UNION && types.isAssignable(symTable.errStructType, rhsType)) {
            dlog.error(pos, DiagnosticCode.SAFE_ASSIGN_STMT_INVALID_USAGE);
            return symTable.errType;
        } else if (rhsType.tag != TypeTags.UNION) {
            return rhsType;
        }

        // Collect all the rhs types from the union type
        boolean isErrorFound = false;
        BUnionType unionType = (BUnionType) rhsType;
        Set<BType> rhsTypeSet = new LinkedHashSet(unionType.memberTypes);
        for (BType type : unionType.memberTypes) {
            if (types.isAssignable(type, symTable.errStructType)) {
                rhsTypeSet.remove(type);
                isErrorFound = true;
            }
        }

        if (rhsTypeSet.isEmpty() || !isErrorFound) {
            dlog.error(pos, DiagnosticCode.SAFE_ASSIGN_STMT_INVALID_USAGE);
            return symTable.noType;
        } else if (rhsTypeSet.size() == 1) {
            return rhsTypeSet.toArray(new BType[0])[0];
        }

        return new BUnionType(null, rhsTypeSet, rhsTypeSet.contains(symTable.nilType));
    }

    private BType getTypeOfVarReferenceInAssignment(BLangExpression expr) {
        // In assignment, lhs supports only simpleVarRef, indexBasedAccess, filedBasedAccess expressions.
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                expr.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                expr.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                expr.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, expr);
            return symTable.errType;
        }

        BLangVariableReference varRefExpr = (BLangVariableReference) expr;
        varRefExpr.lhsVar = true;
        typeChecker.checkExpr(varRefExpr, env);

        // Check whether we've got an enumerator access expression here.
        if (varRefExpr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                ((BLangFieldBasedAccess) varRefExpr).expr.type.tag == TypeTags.ENUM) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRefExpr);
            return symTable.errType;
        }

        //Check whether this is an readonly field.
        checkReadonlyAssignment(varRefExpr);

        checkConstantAssignment(varRefExpr);
        return varRefExpr.type;
    }

    private void checkOutputAttributes(BLangStatement streamingQueryStatement) {

        List<? extends SelectExpressionNode> selectExpressions =
                ((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().getSelectExpressions();

        List<String> variableList = new ArrayList<>();
        if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
            for (SelectExpressionNode expressionNode : selectExpressions) {
                String variableName;
                if (expressionNode.getIdentifier() != null) {
                    variableName = expressionNode.getIdentifier();
                } else {
                    if (expressionNode.getExpression() instanceof BLangFieldBasedAccess) {
                        variableName = ((BLangFieldBasedAccess) expressionNode.getExpression()).field.value;
                    } else {
                        variableName = ((BLangSimpleVarRef) (expressionNode).getExpression()).variableName.value;
                    }
                }
                variableList.add(variableName);
            }
        } else {
            List<BStructType.BStructField> fields = ((BStructType) ((BStreamType) ((BLangSimpleVarRef)
                    (((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput()).
                            getStreamReference()).type).constraint).fields;

            for (BStructType.BStructField structField : fields) {
                variableList.add(structField.name.value);
            }
        }

        BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

        if (streamActionArgumentType.tag == TypeTags.ARRAY) {
            BType structType = (((BArrayType) streamActionArgumentType).eType);

            if (structType.tag == TypeTags.STRUCT) {
                List<BStructType.BStructField> structFieldList = ((BStructType) structType).fields;
                List<String> structFieldNameList = new ArrayList<>();
                for (BStructType.BStructField structField : structFieldList) {
                    structFieldNameList.add(structField.name.value);
                }

                if (!variableList.equals(structFieldNameList)) {
                    dlog.error(((BLangStreamAction) ((BLangStreamingQueryStatement) streamingQueryStatement).
                            getStreamingAction()).pos, DiagnosticCode.INCOMPATIBLE_STREAM_ACTION_ARGUMENT, structType);
                }
            }
        }
    }

    private void validateOutputAttributeTypes(BLangStatement streamingQueryStatement) {
        StreamingInput streamingInput = ((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput();
        JoinStreamingInput joinStreamingInput = ((BLangStreamingQueryStatement) streamingQueryStatement).
                getJoiningInput();

        if (streamingInput != null) {
            Map<String, List<BStructType.BStructField>> inputStreamSpecificFieldMap =
                    createInputStreamSpecificFieldMap(streamingInput, joinStreamingInput);
            BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                    streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

            if (streamActionArgumentType.tag == TypeTags.ARRAY) {
                BType structType = (((BArrayType) streamActionArgumentType).eType);

                if (structType.tag == TypeTags.STRUCT) {
                    List<BStructType.BStructField> outputStreamFieldList = ((BStructType) structType).fields;
                    List<? extends SelectExpressionNode> selectExpressions = ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getSelectClause().getSelectExpressions();

                    if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
                        for (int i = 0; i < selectExpressions.size(); i++) {
                            SelectExpressionNode expressionNode = selectExpressions.get(i);
                            BStructType.BStructField structField = null;
                            if (expressionNode.getExpression() instanceof BLangFieldBasedAccess) {
                                String attributeName =
                                        ((BLangFieldBasedAccess) expressionNode.getExpression()).field.value;
                                String streamIdentifier = ((BLangSimpleVarRef) ((BLangFieldBasedAccess) expressionNode.
                                        getExpression()).expr).variableName.value;

                                List<BStructType.BStructField> streamFieldList = inputStreamSpecificFieldMap.
                                        get(streamIdentifier);
                                if (streamFieldList == null) {
                                    dlog.error(((BLangSelectClause)
                                                    ((BLangStreamingQueryStatement) streamingQueryStatement).
                                                            getSelectClause()).pos,
                                            DiagnosticCode.UNDEFINED_STREAM_REFERENCE, streamIdentifier);
                                } else {
                                    structField = getStructField(streamFieldList, attributeName);
                                    validateAttributeWithOutputStruct(structField, attributeName,
                                            streamingQueryStatement, outputStreamFieldList.get(i));
                                }
                            } else if (expressionNode.getExpression() instanceof BLangSimpleVarRef) {
                                String attributeName = ((BLangSimpleVarRef) expressionNode.getExpression()).
                                        variableName.getValue();

                                for (List<BStructType.BStructField> streamFieldList :
                                        inputStreamSpecificFieldMap.values()) {
                                    structField = getStructField(streamFieldList, attributeName);
                                    if (structField != null) {
                                        break;
                                    }
                                }
                                validateAttributeWithOutputStruct(structField, attributeName, streamingQueryStatement,
                                        outputStreamFieldList.get(i));
                            }
                        }
                    } else {
                        List<BStructType.BStructField> inputStreamFields = ((BStructType) ((BStreamType)
                                ((BLangSimpleVarRef) (((BLangStreamingQueryStatement) streamingQueryStatement).
                                        getStreamingInput()).getStreamReference()).type).constraint).fields;

                        for (int i = 0; i < inputStreamFields.size(); i++) {
                            BStructType.BStructField inputStructField = inputStreamFields.get(i);
                            BStructType.BStructField outputStructField = outputStreamFieldList.get(i);
                            this.types.checkType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                                            streamingQueryStatement).getStreamingAction()).pos,
                                    outputStructField.getType(), inputStructField.getType(),
                                    DiagnosticCode.INCOMPATIBLE_TYPES);
                        }
                    }
                }
            }
        }
    }

    private List<BStructType.BStructField> getFieldListFromStreamInput(StreamingInput streamingInput) {

        return ((BStructType) ((BStreamType) ((BLangSimpleVarRef)
                streamingInput.getStreamReference()).type).constraint).fields;
    }

    private String getStreamIdentifier(StreamingInput streamingInput) {
        String streamIdentifier = streamingInput.getAlias();
        if (streamIdentifier == null) {
            streamIdentifier = ((BLangSimpleVarRef) streamingInput.getStreamReference()).variableName.value;
        }

        return streamIdentifier;
    }

    private BStructType.BStructField getStructField(List<BStructType.BStructField> fieldList, String fieldName) {
        for (BStructType.BStructField structField : fieldList) {
            String structFieldName = structField.name.getValue();
            if (structFieldName.equalsIgnoreCase(fieldName)) {
                return structField;
            }
        }

        return null;
    }

    private void validateAttributeWithOutputStruct(BStructType.BStructField structField, String attributeName,
                                                   BLangStatement streamingQueryStatement,
                                                   BStructType.BStructField outputStructField) {

        if (structField == null) {
            dlog.error(((BLangSelectClause) ((BLangStreamingQueryStatement)
                            streamingQueryStatement).
                            getSelectClause()).pos, DiagnosticCode.UNDEFINED_STREAM_ATTRIBUTE,
                    attributeName);
        } else {
            this.types.checkType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getStreamingAction()).pos,
                    outputStructField.getType(), structField.getType(),
                    DiagnosticCode.INCOMPATIBLE_TYPES);
        }

    }

    private Map<String, List<BStructType.BStructField>> createInputStreamSpecificFieldMap
            (StreamingInput streamingInput, JoinStreamingInput joinStreamingInput) {

        Map<String, List<BStructType.BStructField>> inputStreamSpecificFieldMap = new HashMap<>();
        String firstStreamIdentifier = getStreamIdentifier(streamingInput);
        List<BStructType.BStructField> firstInputStreamFieldList = getFieldListFromStreamInput(streamingInput);
        inputStreamSpecificFieldMap.put(firstStreamIdentifier, firstInputStreamFieldList);

        if (joinStreamingInput != null) {
            List<BStructType.BStructField> secondInputStreamFieldList =
                    getFieldListFromStreamInput(joinStreamingInput.getStreamingInput());
            String secondStreamIdentifier = getStreamIdentifier(joinStreamingInput.getStreamingInput());
            inputStreamSpecificFieldMap.put(secondStreamIdentifier, secondInputStreamFieldList);
        }

        return inputStreamSpecificFieldMap;
    }

    private void validateStreamingActionFunctionParameters(BLangStreamAction streamAction) {
        List<BLangVariable> functionParameters = ((BLangFunction) streamAction.getInvokableBody().
                getFunctionNode()).requiredParams;
        if (functionParameters == null || functionParameters.size() != 1) {
            dlog.error((streamAction).pos,
                    DiagnosticCode.INVALID_STREAM_ACTION_ARGUMENT_COUNT,
                    functionParameters == null ? 0 : functionParameters.size());
        } else if (!(functionParameters.get(0).type.tag == TypeTags.ARRAY &&
                ((BArrayType) functionParameters.get(0).type).eType.tag == TypeTags.STRUCT)) {
            dlog.error((streamAction).pos,
                    DiagnosticCode.INVALID_STREAM_ACTION_ARGUMENT_TYPE);
        }
    }
}
