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
package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.tools.diagnostics.Location;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.BlockFunctionBodyNode;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.BLangCompilerConstants;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeParamAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.OCEDynamicEnvironmentData;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangWildCardBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStringAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTableAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTupleAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangListConstructorSpreadOpExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangTupleLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAssertion;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomCharOrEscape;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCapturingGroups;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSet;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSetRange;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharacterClass;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReDisjunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagsOnOff;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReSequence;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangTypeLoad;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangValueExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorCauseMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorFieldMatchPatterns;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMessageMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangFieldMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangUnLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.util.BLangCompilerConstants.RETRY_MANAGER_OBJECT_SHOULD_RETRY_FUNC;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createBlockStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createErrorVariableDef;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createExpressionStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createIdentifier;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createLiteral;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createStatementExpression;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createVariable;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createVariableRef;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;
import static org.wso2.ballerinalang.compiler.util.Names.GENERATED_INIT_SUFFIX;
import static org.wso2.ballerinalang.compiler.util.Names.GEN_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.util.Names.IGNORE;
import static org.wso2.ballerinalang.compiler.util.Names.IS_TRANSACTIONAL;

/**
 * @since 0.94
 */
public class Desugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<Desugar> DESUGAR_KEY =
            new CompilerContext.Key<>();
    private static final String ERROR_MESSAGE_FUNCTION_NAME = "message";
    private static final String ERROR_CAUSE_FUNCTION_NAME = "cause";
    private static final String ERROR_DETAIL_FUNCTION_NAME = "detail";
    private static final String TO_STRING_FUNCTION_NAME = "toString";
    private static final String LENGTH_FUNCTION_NAME = "length";
    private static final String ERROR_REASON_NULL_REFERENCE_ERROR = "NullReferenceException";
    private static final String CLONE_WITH_TYPE = "cloneWithType";
    private static final String PUSH_LANGLIB_METHOD = "push";
    private static final String DESUGARED_VARARG_KEY = "$vararg$";
    private static final String GENERATED_ERROR_VAR = "$error$";
    private static final String HAS_KEY = "hasKey";
    private static final String CREATE_RECORD_VALUE = "createRecordFromMap";

    public static final String XML_INTERNAL_SELECT_DESCENDANTS = "selectDescendants";
    public static final String XML_INTERNAL_CHILDREN = "children";
    public static final String XML_INTERNAL_GET_FILTERED_CHILDREN_FLAT = "getFilteredChildrenFlat";
    public static final String XML_INTERNAL_GET_ELEMENT_NAME_NIL_LIFTING = "getElementNameNilLifting";
    public static final String XML_INTERNAL_GET_ATTRIBUTE = "getAttribute";
    public static final String XML_INTERNAL_GET_ELEMENTS = "getElements";
    public static final String XML_GET_CONTENT_OF_TEXT = "getContent";

    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private ClosureDesugar closureDesugar;
    private ParameterDesugar parameterDesugar;
    private QueryDesugar queryDesugar;
    private TransactionDesugar transactionDesugar;
    private ObservabilityDesugar observabilityDesugar;
    private Code2CloudDesugar code2CloudDesugar;
    private AnnotationDesugar annotationDesugar;
    private Types types;
    private Names names;
    private ServiceDesugar serviceDesugar;
    private BLangNode result;
    private NodeCloner nodeCloner;
    private SemanticAnalyzer semanticAnalyzer;
    private BLangAnonymousModelHelper anonModelHelper;
    private Unifier unifier;
    private MockDesugar mockDesugar;
    private ClassClosureDesugar classClosureDesugar;
    private LargeMethodSplitter largeMethodSplitter;

    public Stack<BLangLockStmt> enclLocks = new Stack<>();
    private BLangOnFailClause onFailClause;
    private boolean shouldReturnErrors;
    private int transactionBlockCount;
    private BLangLiteral trxBlockId;
    private List<BLangOnFailClause> enclosingOnFailClause = new ArrayList<>();
    private Map<BLangOnFailClause, BLangSimpleVarRef> enclosingShouldPanic = new HashMap<>();
    private List<BLangSimpleVarRef> enclosingShouldContinue = new ArrayList<>();
    private BLangSimpleVarRef shouldRetryRef;

    private SymbolEnv env;
    private int lambdaFunctionCount = 0;
    private int recordCount = 0;
    private int errorCount = 0;
    private int annonVarCount = 0;
    private int indexExprCount = 0;
    private int letCount = 0;
    private int varargCount = 0;
    private int funcParamCount = 1;
    private boolean isVisitingQuery;

    // Safe navigation related variables
    private Stack<BLangMatchStatement> matchStmtStack = new Stack<>();
    Stack<BLangExpression> accessExprStack = new Stack<>();
    private BLangMatchClause successClause;
    private BLangAssignment safeNavigationAssignment;
    static boolean isJvmTarget = false;

    private Map<BSymbol, Set<BVarSymbol>> globalVariablesDependsOn;
    private List<BLangStatement> matchStmtsForPattern = new ArrayList<>();
    private Map<String, BLangSimpleVarRef> declaredVarDef = new HashMap<>();
    private List<BLangXMLNS> inlineXMLNamespaces;
    private Map<Name, BLangStatement> stmtsToBePropagatedToQuery = new HashMap<>();
    // Reuse the strand annotation in isolated workers and start action
    private BLangAnnotationAttachment strandAnnotAttachement;

    public static Desugar getInstance(CompilerContext context) {
        Desugar desugar = context.get(DESUGAR_KEY);
        if (desugar == null) {
            desugar = new Desugar(context);
        }

        return desugar;
    }

    private Desugar(CompilerContext context) {
        // This is a temporary flag to differentiate desugaring to BVM vs BIR
        // TODO: remove this once bootstrapping is added.
        isJvmTarget = true;

        context.put(DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.closureDesugar = ClosureDesugar.getInstance(context);
        this.parameterDesugar = ParameterDesugar.getInstance(context);
        this.queryDesugar = QueryDesugar.getInstance(context);
        this.transactionDesugar = TransactionDesugar.getInstance(context);
        this.observabilityDesugar = ObservabilityDesugar.getInstance(context);
        this.code2CloudDesugar = Code2CloudDesugar.getInstance(context);
        this.annotationDesugar = AnnotationDesugar.getInstance(context);
        this.largeMethodSplitter = LargeMethodSplitter.getInstance(context);
        this.types = Types.getInstance(context);
        this.names = Names.getInstance(context);
        this.names = Names.getInstance(context);
        this.serviceDesugar = ServiceDesugar.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
        this.semanticAnalyzer = SemanticAnalyzer.getInstance(context);
        this.anonModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.mockDesugar = MockDesugar.getInstance(context);
        this.classClosureDesugar = ClassClosureDesugar.getInstance(context);
        this.unifier = new Unifier();
    }

    public BLangPackage perform(BLangPackage pkgNode) {
        SymbolEnv env = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        this.globalVariablesDependsOn = env.enclPkg.globalVariableDependencies;
        return rewrite(pkgNode, env);
    }

    private void addAttachedFunctionsToPackageLevel(BLangPackage pkgNode, SymbolEnv env) {
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            BSymbol typeSymbol = typeDef.symbol.tag == SymTag.TYPE_DEF ? typeDef.symbol.type.tsymbol : typeDef.symbol;
            if (typeSymbol.tag == SymTag.OBJECT) {
                BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDef.typeNode;

                objectTypeNode.functions.forEach(f -> {
                    if (!pkgNode.objAttachedFunctions.contains(f.symbol)) {
                        pkgNode.functions.add(f);
                        pkgNode.topLevelNodes.add(f);
                    }
                });
            } else if (typeSymbol.tag == SymTag.RECORD) {
                BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
                recordTypeNode.initFunction = rewrite(
                        TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names, symTable),
                        env);
                pkgNode.functions.add(recordTypeNode.initFunction);
                pkgNode.topLevelNodes.add(recordTypeNode.initFunction);
            }
        }
        int toplevelNodeCount = pkgNode.topLevelNodes.size();
        for (int i = 0; i < toplevelNodeCount; i++) {
            TopLevelNode topLevelNode = pkgNode.topLevelNodes.get(i);
            if (topLevelNode.getKind() != NodeKind.CLASS_DEFN) {
                continue;
            }
            addClassMemberFunctionsToTopLevel(pkgNode, env, (BLangClassDefinition) topLevelNode);
        }
    }

    private void addClassMemberFunctionsToTopLevel(BLangPackage pkgNode, SymbolEnv env,
                                                   BLangClassDefinition classDefinition) {
        for (BLangFunction function : classDefinition.functions) {
            if (!pkgNode.objAttachedFunctions.contains(function.symbol)) {
                pkgNode.functions.add(function);
                pkgNode.topLevelNodes.add(function);
            }
        }

        BLangFunction tempGeneratedInitFunction = createGeneratedInitializerFunction(classDefinition, env);
        tempGeneratedInitFunction.clonedEnv = SymbolEnv.createFunctionEnv(tempGeneratedInitFunction,
                tempGeneratedInitFunction.symbol.scope, env);
        SemanticAnalyzer.AnalyzerData data = new SemanticAnalyzer.AnalyzerData(env);
        this.semanticAnalyzer.analyzeNode(tempGeneratedInitFunction, data);
        classDefinition.generatedInitFunction = tempGeneratedInitFunction;

        // Add generated init function to the attached function list
        pkgNode.functions.add(classDefinition.generatedInitFunction);
        pkgNode.topLevelNodes.add(classDefinition.generatedInitFunction);

        // Add init function to the attached function list
        if (classDefinition.initFunction != null) {
            pkgNode.functions.add(classDefinition.initFunction);
            pkgNode.topLevelNodes.add(classDefinition.initFunction);
        }
    }

    /**
     * This method synthesizes an initializer method for objects which is responsible for initializing the default
     * values given to fields. When a user creates a new instance of the object, first, this synthesized initializer is
     * invoked on the newly created object instance. Then, if there is a user-defined init method (i.e., the init()
     * method), an method call expression for this init() method is added in the return statement of the synthesized
     * initializer. When desugaring, the following method adds params and return type for the synthesized initializer by
     * looking at the params and return type of the user-defined init() method. Therefore, when desugaring object type
     * nodes, one should always take care to call this method **after** desugaring the init() method (if there is
     * supposed to be one).
     *
     * @param classDefinition The class definition node for which the initializer is created
     * @param env            The env for the type node
     * @return The generated initializer method
     */
    private BLangFunction createGeneratedInitializerFunction(BLangClassDefinition classDefinition, SymbolEnv env) {
        BLangFunction generatedInitFunc = createInitFunctionForClassDefn(classDefinition, env);
        if (classDefinition.initFunction == null) {
            return rewrite(generatedInitFunc, env);
        }

        return wireUpGeneratedInitFunction(generatedInitFunc,
                    (BObjectTypeSymbol) classDefinition.symbol, classDefinition.initFunction);
    }

    private BLangFunction wireUpGeneratedInitFunction(BLangFunction generatedInitFunc,
                                                      BObjectTypeSymbol objectTypeSymbol, BLangFunction initFunction) {
        addReturnIfNotPresent(generatedInitFunc);
        BAttachedFunction initializerFunc = objectTypeSymbol.initializerFunc;
        BAttachedFunction generatedInitializerFunc =
                objectTypeSymbol.generatedInitializerFunc;
        addRequiredParamsToGeneratedInitFunction(initFunction, generatedInitFunc,
                                                 generatedInitializerFunc);
        addRestParamsToGeneratedInitFunction(initFunction, generatedInitFunc, generatedInitializerFunc);

        generatedInitFunc.returnTypeNode = initFunction.returnTypeNode;
        generatedInitializerFunc.symbol.retType = generatedInitFunc.returnTypeNode.getBType();

        ((BInvokableType) generatedInitFunc.symbol.type).paramTypes = initializerFunc.type.paramTypes;
        ((BInvokableType) generatedInitFunc.symbol.type).retType = initializerFunc.type.retType;
        ((BInvokableType) generatedInitFunc.symbol.type).restType = initializerFunc.type.restType;
        generatedInitFunc.symbol.type.tsymbol = initializerFunc.type.tsymbol;

        generatedInitializerFunc.type = initializerFunc.type;
        generatedInitFunc.desugared = false;
        return generatedInitFunc;
    }

    private void addRequiredParamsToGeneratedInitFunction(BLangFunction initFunction, BLangFunction generatedInitFunc,
                                                          BAttachedFunction generatedInitializerFunc) {
        if (initFunction.requiredParams.isEmpty()) {
            return;
        }
        for (BLangSimpleVariable requiredParameter : initFunction.requiredParams) {
            BLangSimpleVariable var =
                    ASTBuilderUtil.createVariable(initFunction.pos,
                                                  requiredParameter.name.getValue(), requiredParameter.getBType(),
                                                  createRequiredParamExpr(requiredParameter.expr),
                                                  new BVarSymbol(Flags.asMask(requiredParameter.flagSet),
                                                                 names.fromString(requiredParameter.name.getValue()),
                                                                 requiredParameter.symbol.pkgID,
                                                                 requiredParameter.getBType(),
                                                                 requiredParameter.symbol.owner, initFunction.pos,
                                                                 VIRTUAL));
            generatedInitFunc.requiredParams.add(var);
            generatedInitializerFunc.symbol.params.add(var.symbol);
        }
    }

    private BLangExpression createRequiredParamExpr(BLangExpression expr) {
        if (expr == null) {
            return null;
        }
        if (expr.getKind() == NodeKind.LAMBDA) {
            BLangFunction func = ((BLangLambdaFunction) expr).function;
            return createLambdaFunction(func.pos, func.name.value, func.requiredParams, func.returnTypeNode, func.body);
        }
        // Since the expression of the requiredParam of both init functions refer to same object,
        // expression should be cloned.
        expr.cloneAttempt++;
        BLangExpression expression = this.nodeCloner.cloneNode(expr);
        if (expression.getKind() == NodeKind.ARROW_EXPR) {
            BLangIdentifier func = (BLangIdentifier) ((BLangArrowFunction) expression).functionName;
            ((BLangArrowFunction) expression).functionName = ASTBuilderUtil.createIdentifier(func.pos,
                    "$" + func.getValue() + "$");
        }
        return expression;
    }

    private void addRestParamsToGeneratedInitFunction(BLangFunction initFunction, BLangFunction generatedInitFunc,
                                                      BAttachedFunction generatedInitializerFunc) {
        if (initFunction.restParam == null) {
            return;
        }
        BLangSimpleVariable restParam = initFunction.restParam;
        generatedInitFunc.restParam =
                ASTBuilderUtil.createVariable(initFunction.pos,
                                              restParam.name.getValue(), restParam.getBType(), null,
                                              new BVarSymbol(0, names.fromString(restParam.name.getValue()),
                                                             restParam.symbol.pkgID, restParam.getBType(),
                                                             restParam.symbol.owner, restParam.pos, VIRTUAL));
        generatedInitializerFunc.symbol.restParam = generatedInitFunc.restParam.symbol;
    }

    /**
     * Create package init functions.
     *
     * @param pkgNode package node
     * @param env     symbol environment of package
     */
    private void createPackageInitFunctions(BLangPackage pkgNode, SymbolEnv env) {
        String alias = "";
        pkgNode.initFunction = ASTBuilderUtil.createInitFunctionWithErrorOrNilReturn(pkgNode.pos, alias,
                                                                                     Names.INIT_FUNCTION_SUFFIX,
                                                                                     symTable);
        // Add package level namespace declarations to the init function
        BLangBlockFunctionBody initFnBody = (BLangBlockFunctionBody) pkgNode.initFunction.body;
        for (BLangXMLNS xmlns : pkgNode.xmlnsList) {
            initFnBody.addStatement(createNamespaceDeclrStatement(xmlns));
        }
        pkgNode.startFunction = ASTBuilderUtil.createInitFunctionWithErrorOrNilReturn(pkgNode.pos, alias,
                                                                                      Names.START_FUNCTION_SUFFIX,
                                                                                      symTable);
        pkgNode.stopFunction = ASTBuilderUtil.createInitFunctionWithNilReturn(pkgNode.pos, alias,
                                                                              Names.STOP_FUNCTION_SUFFIX);
        // Create invokable symbol for init function
        createInvokableSymbol(pkgNode.initFunction, env);
        // Create invokable symbol for start function
        createInvokableSymbol(pkgNode.startFunction, env);
        // Create invokable symbol for stop function
        createInvokableSymbol(pkgNode.stopFunction, env);
    }

    private void addUserDefinedModuleInitInvocationAndReturn(BLangPackage pkgNode) {
        Optional<BLangFunction> userDefInitOptional = pkgNode.functions.stream()
                .filter(bLangFunction -> !bLangFunction.attachedFunction &&
                            bLangFunction.name.value.equals(Names.USER_DEFINED_INIT_SUFFIX.value))
                .findFirst();

        BLangBlockFunctionBody initFnBody = (BLangBlockFunctionBody) pkgNode.initFunction.body;
        if (!userDefInitOptional.isPresent()) {
            // Assumption: compiler generated module init function body is always a block function body.
            addNilReturnStatement(initFnBody);
            return;
        }

        BLangFunction userDefInit = userDefInitOptional.get();

        BLangInvocation userDefInitInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        userDefInitInvocation.pos = pkgNode.initFunction.pos;
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.setLiteral(false);
        name.setValue(userDefInit.name.value);
        userDefInitInvocation.name = name;
        userDefInitInvocation.symbol = userDefInit.symbol;

        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setLiteral(false);
        pkgAlias.setValue(pkgNode.packageID.name.value);
        userDefInitInvocation.pkgAlias = pkgAlias;

        userDefInitInvocation.setBType(userDefInit.returnTypeNode.getBType());
        userDefInitInvocation.requiredArgs = Collections.emptyList();

        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pkgNode.initFunction.pos;
        returnStmt.expr = userDefInitInvocation;
        initFnBody.stmts.add(returnStmt);
    }

    /**
     * Create invokable symbol for function.
     *
     * @param bLangFunction function node
     * @param env           Symbol environment
     */
    protected void createInvokableSymbol(BLangFunction bLangFunction, SymbolEnv env) {
        BType returnType = bLangFunction.returnTypeNode.getBType() == null ?
                symResolver.resolveTypeNode(bLangFunction.returnTypeNode, env) :
                bLangFunction.returnTypeNode.getBType();
        BInvokableType invokableType = new BInvokableType(new ArrayList<>(), getRestType(bLangFunction),
                                                          returnType, null);
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(bLangFunction.flagSet),
                                                                       new Name(bLangFunction.name.value),
                                                                       new Name(bLangFunction.name.originalValue),
                                                                       env.enclPkg.packageID, invokableType,
                                                                       env.enclPkg.symbol, true, bLangFunction.pos,
                                                                       VIRTUAL);
        functionSymbol.retType = returnType;
        functionSymbol.originalName = new Name(bLangFunction.name.originalValue);
        // Add parameters
        for (BLangVariable param : bLangFunction.requiredParams) {
            functionSymbol.params.add(param.symbol);
        }

        functionSymbol.scope = new Scope(functionSymbol);
        bLangFunction.symbol = functionSymbol;
    }

    /**
     * Add nil return statement.
     *
     * @param bLangBlockStmt block statement node
     */
    private void addNilReturnStatement(BlockNode bLangBlockStmt) {
        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(((BLangNode) bLangBlockStmt).pos, symTable.nilType);
        bLangBlockStmt.addStatement(returnStmt);
    }

    /**
     * Create namespace declaration statement for XMNLNS.
     *
     * @param xmlns XMLNS node
     * @return XMLNS statement
     */
    private BLangXMLNSStatement createNamespaceDeclrStatement(BLangXMLNS xmlns) {
        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = xmlns.pos;
        return xmlnsStmt;
    }
    // visitors

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DESUGAR)) {
            result = pkgNode;
            return;
        }
        observabilityDesugar.addObserveInternalModuleImport(pkgNode);

        code2CloudDesugar.addCode2CloudModuleImport(pkgNode);

        createPackageInitFunctions(pkgNode, env);
        // Adding object functions to package level.
        addAttachedFunctionsToPackageLevel(pkgNode, env);

        if (!pkgNode.testablePkgs.isEmpty() && pkgNode.getTestablePkg().getMockFunctionNamesMap() != null) {
            mockDesugar.generateMockFunctions(pkgNode);
        }

        // create closures for default values
        parameterDesugar.visit(pkgNode);

        // Initialize the annotation map
        annotationDesugar.initializeAnnotationMap(pkgNode);

        pkgNode.constants.stream()
                .filter(constant -> constant.expr.getKind() == NodeKind.LITERAL ||
                        constant.expr.getKind() == NodeKind.NUMERIC_LITERAL)
                .forEach(constant -> pkgNode.typeDefinitions.add(constant.associatedTypeDefinition));

        BLangBlockStmt serviceAttachments = serviceDesugar.rewriteServiceVariables(pkgNode.services, env);
        BLangBlockFunctionBody initFnBody = (BLangBlockFunctionBody) pkgNode.initFunction.body;

        rewriteConstants(pkgNode, initFnBody);

        pkgNode.constants = removeDuplicateConstants(pkgNode);

        pkgNode.globalVars = desugarGlobalVariables(pkgNode, initFnBody);

        pkgNode.services.forEach(service -> serviceDesugar.engageCustomServiceDesugar(service, env));

        desugarAnnotations(pkgNode);

        // Add invocation for user specified module init function (`init()`) if present and return.
        addUserDefinedModuleInitInvocationAndReturn(pkgNode);

        //Sort type definitions with precedence
        pkgNode.typeDefinitions.sort(Comparator.comparing(t -> t.precedence));

        pkgNode.typeDefinitions = rewrite(pkgNode.typeDefinitions, env);
        pkgNode.xmlnsList = rewrite(pkgNode.xmlnsList, env);
        pkgNode.constants = rewrite(pkgNode.constants, env);
        pkgNode.globalVars = rewrite(pkgNode.globalVars, env);
        pkgNode.classDefinitions = rewrite(pkgNode.classDefinitions, env);

        serviceDesugar.rewriteListeners(pkgNode.globalVars, env, pkgNode.startFunction, pkgNode.stopFunction);
        ASTBuilderUtil.appendStatements(serviceAttachments, (BLangBlockFunctionBody) pkgNode.initFunction.body);

        addNilReturnStatement((BLangBlockFunctionBody) pkgNode.startFunction.body);
        addNilReturnStatement((BLangBlockFunctionBody) pkgNode.stopFunction.body);

        if (isJvmTarget) {
            largeMethodSplitter.splitLargeFunctions(pkgNode, env);
        }

        pkgNode.initFunction = rewrite(pkgNode.initFunction, env);
        pkgNode.startFunction = rewrite(pkgNode.startFunction, env);
        pkgNode.stopFunction = rewrite(pkgNode.stopFunction, env);

        for (int i = 0; i < pkgNode.functions.size(); i++) {
            BLangFunction function = pkgNode.functions.get(i);
            if (!function.flagSet.contains(Flag.LAMBDA) || function.name.value.contains(MockDesugar.MOCK_FUNCTION)) {
                pkgNode.functions.set(i, rewrite(pkgNode.functions.get(i), env));
            }
        }

        // Invoke closure desugar.
        closureDesugar.visit(pkgNode);

        for (BLangTestablePackage testablePkg : pkgNode.getTestablePkgs()) {
            rewrite(testablePkg, this.symTable.pkgEnvMap.get(testablePkg.symbol));
        }
        pkgNode.completedPhases.add(CompilerPhase.DESUGAR);
        result = pkgNode;
    }

    private void desugarAnnotations(BLangPackage pkgNode) {
        List<BLangTypeDefinition> prevTypeDefinitions = new ArrayList<>(pkgNode.typeDefinitions);

        annotationDesugar.rewritePackageAnnotations(pkgNode, env);

        addInitFunctionForRecordTypeNodeInTypeDef(pkgNode, prevTypeDefinitions);
    }

    private void addInitFunctionForRecordTypeNodeInTypeDef(BLangPackage pkgNode,
                                                         List<BLangTypeDefinition> prevTypeDefinitions) {
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            if (prevTypeDefinitions.contains(typeDef)) {
                continue;
            }

            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            BSymbol symbol = typeDef.symbol;
            BSymbol typeSymbol = symbol.tag == SymTag.TYPE_DEF ? symbol.type.tsymbol : symbol;
            if (typeSymbol.tag != SymTag.RECORD) {
                continue;
            }
            BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
            recordTypeNode.initFunction = rewrite(
                    TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names, symTable),
                    env);
            pkgNode.functions.add(recordTypeNode.initFunction);
            pkgNode.topLevelNodes.add(recordTypeNode.initFunction);
        }
    }

    private void rewriteConstants(BLangPackage pkgNode, BLangBlockFunctionBody initFnBody) {
        for (BLangConstant constant : pkgNode.constants) {
            BType constType = Types.getReferredType(constant.symbol.type);
            if (constType.tag != TypeTags.INTERSECTION) {
                continue;
            }
            for (BType memberType : ((BIntersectionType) constType).getConstituentTypes()) {
                if (memberType.tag != TypeTags.RECORD) {
                    continue;
                }
                BLangSimpleVarRef constVarRef = ASTBuilderUtil.createVariableRef(constant.pos, constant.symbol);
                constant.expr = rewrite(constant.expr,
                        SymbolEnv.createTypeEnv(constant.associatedTypeDefinition.typeNode,
                                pkgNode.initFunction.symbol.scope, env));
                BLangAssignment constInit = ASTBuilderUtil.createAssignmentStmt(constant.pos, constVarRef,
                        constant.expr);
                initFnBody.stmts.add(constInit);
            }
        }
    }

    private List<BLangConstant> removeDuplicateConstants(BLangPackage pkgNode) {
        HashSet<String> elements = new HashSet<>();
        for (int i = 0; i < pkgNode.constants.size(); i++) {
            String next = pkgNode.constants.get(i).symbol.name.value;
            if (elements.contains(next)) {
                pkgNode.constants.remove(i);
                i -= 1;
            } else {
                elements.add(next);
            }
        }
        return pkgNode.constants;
    }

    private BLangStatementExpression createIfElseFromConfigurable(BLangSimpleVariable configurableVar,
                                                                  SymbolEnv initFunctionEnv) {

        /*
         * If else will be generated as follows:
         *
         * if (hasValue(key)) {
         *    result = getValue(key);
         * } else {
         *    result = defaultValue;
         * }
         *
         * key = orgName + "." + moduleName + "." + version + "." + configVarName
         */

        List<BLangExpression> args = getConfigurableLangLibInvocationParam(configurableVar);

        // Check if value is configured
        BLangInvocation hasValueInvocation = createLangLibInvocationNode("hasConfigurableValue",
                args, symTable.booleanType, configurableVar.pos);
        // Get value if configured else get default value provided
        BLangInvocation getValueInvocation = createLangLibInvocationNode("getConfigurableValue",
                args, symTable.anydataType, configurableVar.pos);

        BLangBlockStmt thenBody = ASTBuilderUtil.createBlockStmt(configurableVar.pos);
        BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(configurableVar.pos);

        // Create then assignment
        BLangSimpleVarRef thenResultVarRef =
                ASTBuilderUtil.createVariableRef(configurableVar.pos, configurableVar.symbol);
        BLangAssignment thenAssignment =
                ASTBuilderUtil.createAssignmentStmt(configurableVar.pos, thenResultVarRef, getValueInvocation);
        thenBody.addStatement(thenAssignment);

        // Create else assignment
        BLangSimpleVarRef elseResultVarRef =
                ASTBuilderUtil.createVariableRef(configurableVar.pos, configurableVar.symbol);
        BLangAssignment elseAssignment =
                ASTBuilderUtil.createAssignmentStmt(configurableVar.pos, elseResultVarRef, configurableVar.expr);
        elseBody.addStatement(elseAssignment);

        BLangIf ifElse = ASTBuilderUtil.createIfElseStmt(configurableVar.pos, hasValueInvocation, thenBody, elseBody);

        // Then make it an expression-statement, since we need it to be an expression
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(configurableVar.pos, configurableVar.symbol);
        BLangStatementExpression stmtExpr = createStatementExpression(ifElse, resultVarRef);
        stmtExpr.setBType(configurableVar.getBType());

        return rewrite(stmtExpr, initFunctionEnv);
    }

    private List<BLangExpression> getConfigurableLangLibInvocationParam(BLangSimpleVariable configurableVar) {
        // Prepare parameters
        String orgName = env.enclPkg.packageID.orgName.getValue();
        BLangLiteral orgLiteral = ASTBuilderUtil.createLiteral(configurableVar.pos, symTable.stringType, orgName);
        String moduleName = env.enclPkg.packageID.name.getValue();
        BLangLiteral moduleNameLiteral =
                ASTBuilderUtil.createLiteral(configurableVar.pos, symTable.stringType, moduleName);
        String versionNumber = getMajorVersion(env.enclPkg.packageID.version.getValue());
        BLangLiteral versionLiteral =
                ASTBuilderUtil.createLiteral(configurableVar.pos, symTable.stringType, versionNumber);
        String configVarName = configurableVar.name.getValue();
        BLangLiteral configNameLiteral =
                ASTBuilderUtil.createLiteral(configurableVar.pos, symTable.stringType, configVarName);
        BType type = configurableVar.getBType();
        BType typedescType = new BTypedescType(type, symTable.typeDesc.tsymbol);

        BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
        typedescExpr.resolvedType = type;
        typedescExpr.setBType(typedescType);

        return new ArrayList<>(Arrays.asList(orgLiteral, moduleNameLiteral, versionLiteral, configNameLiteral,
                typedescExpr));
    }

    private List<BLangVariable> desugarGlobalVariables(BLangPackage pkgNode, BLangBlockFunctionBody initFnBody) {
        List<BLangVariable> desugaredGlobalVarList = new ArrayList<>();
        SymbolEnv initFunctionEnv =
                SymbolEnv.createFunctionEnv(pkgNode.initFunction, pkgNode.initFunction.symbol.scope, env);

        for (BLangVariable globalVar : pkgNode.globalVars) {
            this.env.enclPkg.topLevelNodes.remove(globalVar);
            // This will convert complex variables to simple variables.
            switch (globalVar.getKind()) {
                case TUPLE_VARIABLE:
                    BLangNode blockStatementNode = rewrite(globalVar, initFunctionEnv);
                    List<BLangStatement> statements = ((BLangBlockStmt) blockStatementNode).stmts;

                    int statementSize = statements.size();
                    for (int i = 0; i < statementSize; i++) {
                        addToGlobalVariableList(statements.get(i), initFnBody, globalVar, desugaredGlobalVarList);
                    }
                    break;
                case RECORD_VARIABLE:
                case ERROR_VARIABLE:
                    blockStatementNode = rewrite(globalVar, initFunctionEnv);
                    for (BLangStatement statement : ((BLangBlockStmt) blockStatementNode).stmts) {
                        addToGlobalVariableList(statement, initFnBody, globalVar, desugaredGlobalVarList);
                    }
                    break;
                default:
                    long globalVarFlags = globalVar.symbol.flags;
                    BLangSimpleVariable simpleGlobalVar = (BLangSimpleVariable) globalVar;
                    if (Symbols.isFlagOn(globalVarFlags, Flags.CONFIGURABLE)) {
                        if (Symbols.isFlagOn(globalVarFlags, Flags.REQUIRED)) {
                            // If it is required configuration get directly
                            List<BLangExpression> args = getConfigurableLangLibInvocationParam(simpleGlobalVar);
                            BLangInvocation getValueInvocation = createLangLibInvocationNode("getConfigurableValue",
                                    args, symTable.anydataType, simpleGlobalVar.pos);
                            simpleGlobalVar.expr = getValueInvocation;
                        } else {
                            // If it is optional configuration create if else
                            simpleGlobalVar.expr = createIfElseFromConfigurable(simpleGlobalVar, initFunctionEnv);
                        }
                    }

                    // Module init should fail if listener is a error value.
                    if (Symbols.isFlagOn(globalVarFlags, Flags.LISTENER)
                            && types.containsErrorType(globalVar.expr.getBType())) {
                        globalVar.expr = ASTBuilderUtil.createCheckExpr(globalVar.expr.pos, globalVar.expr,
                                                                        globalVar.getBType());
                    }

                    addToInitFunction(simpleGlobalVar, initFnBody);
                    desugaredGlobalVarList.add(simpleGlobalVar);
                    break;
            }
        }

        this.env.enclPkg.topLevelNodes.addAll(desugaredGlobalVarList);
        return desugaredGlobalVarList;
    }

    private void addToGlobalVariableList(BLangStatement bLangStatement, BLangBlockFunctionBody initFnBody,
                                         BLangVariable globalVar, List<BLangVariable> desugaredGlobalVarList) {
        if (bLangStatement.getKind() == NodeKind.ASSIGNMENT || bLangStatement.getKind() == NodeKind.BLOCK) {
            initFnBody.stmts.add(bLangStatement);
            return;
        }
        if (bLangStatement.getKind() == NodeKind.VARIABLE_DEF) {
            BLangSimpleVariable simpleVar = ((BLangSimpleVariableDef) bLangStatement).var;
            if ((simpleVar.symbol.owner.tag & SymTag.PACKAGE) != SymTag.PACKAGE) {
                initFnBody.stmts.add(bLangStatement);
                return;
            }
            simpleVar.annAttachments = globalVar.getAnnotationAttachments();
            addToInitFunction(simpleVar, initFnBody);
            desugaredGlobalVarList.add(simpleVar);
        }
    }

    private void addToInitFunction(BLangSimpleVariable globalVar, BLangBlockFunctionBody initFnBody) {
        if (globalVar.expr == null) {
            return;
        }
        BLangAssignment assignment = createAssignmentStmt(globalVar);
        initFnBody.stmts.add(assignment);
        globalVar.expr = null;
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        rewrite(pkgEnv.node, pkgEnv);
        result = importPkgNode;
    }

    @Override
    public void visit(BLangTypeDefinition typeDef) {
        typeDef.typeNode = rewrite(typeDef.typeNode, env);

        typeDef.annAttachments.forEach(attachment ->  rewrite(attachment, env));
        result = typeDef;
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        finiteTypeNode.valueSpace.forEach(param -> rewrite(param, env));
        result = finiteTypeNode;
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        objectTypeNode.fields.forEach(field -> {
            rewrite(field, env);
        });
        // Merge the fields defined within the object and the fields that
        // get inherited via the type references.
        objectTypeNode.fields.addAll(objectTypeNode.includedFields);
        result = objectTypeNode;
    }

    @Override
    public void visit(BLangClassDefinition classDef) {
        BLangClassDefinition classDefinition = classDef;
        if (classDef.flagSet.contains(Flag.OBJECT_CTOR) &&
                !classDef.definitionCompleted && classDef.cloneRef != null) {
            BLangClassDefinition originalClass = classDefinition.oceEnvData.originalClass;
            classDefinition = (BLangClassDefinition) originalClass.cloneRef;
        }
        classDefinition.annAttachments.forEach(attachment -> rewrite(attachment, env));

        // Merge the fields defined within the object and the fields that
        // get inherited via the type references.
        classDefinition.fields.addAll(classDefinition.referencedFields);

        for (BLangSimpleVariable bLangSimpleVariable : classDefinition.fields) {
            bLangSimpleVariable.typeNode = rewrite(bLangSimpleVariable.typeNode, env);
        }

        BLangFunction generatedInitFunction = classDefinition.generatedInitFunction;
        if (classDef.flagSet.contains(Flag.OBJECT_CTOR)) {
            generatedInitFunction.flagSet.add(Flag.OBJECT_CTOR);
            generatedInitFunction.parent = classDef; // improve debug
            classClosureDesugar.desugar(classDef);
        }

        // Add object level variables default values to the init function.
        Map<BSymbol, BLangStatement> initFuncStmts = generatedInitFunction.initFunctionStmts;
        for (BLangSimpleVariable field : classDefinition.fields) {
            // skip if the field is already have an value set by the constructor.
            // we are creating an assignment to closures here need change them to self refs should we rewrite this
            // or skip and create after populating closure maps
            if (field.symbol.isDefaultable && field.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) field.expr;
                if (varRef.symbol.closure) {
                    // convert to local variable
                    visit((BLangSimpleVarRef) field.expr);
                    //  not adding them to init but will be handled in closure desugar
                    continue;
                }
            }

            if (!initFuncStmts.containsKey(field.symbol) && field.expr != null) {
                initFuncStmts.put(field.symbol,
                        createStructFieldUpdate(generatedInitFunction, field,
                                generatedInitFunction.receiver.symbol));
            }
        }

        // Adding init statements to the init function.
        BLangStatement[] initStmts = initFuncStmts.values().toArray(new BLangStatement[0]);
        BLangBlockFunctionBody generatedInitFnBody =
                (BLangBlockFunctionBody) generatedInitFunction.body;
        int i = generatedInitFnBody.stmts.size() - 1;
        int j = 0;
        int length = i + initStmts.length;
        for (; i < length; i++, j++) {
            generatedInitFnBody.stmts.add(i, initStmts[j]);
        }

        if (classDefinition.initFunction != null) {
            ((BLangReturn) generatedInitFnBody.stmts.get(generatedInitFnBody.stmts.size() - 1)).expr =
                    createUserDefinedInitInvocation(classDefinition.pos,
                            (BObjectTypeSymbol) classDefinition.symbol, generatedInitFunction);
        }

        // Rewrite the object methods to ensure that any anonymous types defined in method params, return type etc.
        // gets defined before its first use.
        for (BLangFunction fn : classDefinition.functions) {
            rewrite(fn, this.env);
        }
        rewrite(generatedInitFunction, this.env);
        rewrite(classDefinition.initFunction, this.env);

        result = classDefinition;
    }

    private BLangExpression createUserDefinedInitInvocation(Location location,
                                                            BObjectTypeSymbol objectTypeSymbol,
                                                            BLangFunction generatedInitFunction) {
        ArrayList<BLangExpression> paramRefs = new ArrayList<>(generatedInitFunction.requiredParams.size());
        for (BLangSimpleVariable var : generatedInitFunction.requiredParams) {
            paramRefs.add(ASTBuilderUtil.createVariableRef(location, var.symbol));
        }

        BLangInvocation invocation = ASTBuilderUtil.createInvocationExprMethod(null,
                objectTypeSymbol.initializerFunc.symbol,
                paramRefs, Collections.emptyList(), symResolver);
        if (generatedInitFunction.restParam != null) {
            BLangSimpleVarRef restVarRef = ASTBuilderUtil.createVariableRef(location,
                    generatedInitFunction.restParam.symbol);
            invocation.restArgs.add(createRestArgsExpression(restVarRef));
        }
        invocation.exprSymbol =
                objectTypeSymbol.generatedInitializerFunc.symbol.receiverSymbol;

        return invocation;
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        recordTypeNode.fields.addAll(recordTypeNode.includedFields);

        for (BLangSimpleVariable bLangSimpleVariable : recordTypeNode.fields) {
            bLangSimpleVariable.typeNode = rewrite(bLangSimpleVariable.typeNode, env);
        }

        recordTypeNode.restFieldType = rewrite(recordTypeNode.restFieldType, env);

        // Will be null only for locally defined anonymous types
        if (recordTypeNode.initFunction == null) {
            recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env,
                                                                                               names, symTable);
            env.enclPkg.addFunction(recordTypeNode.initFunction);
            env.enclPkg.topLevelNodes.add(recordTypeNode.initFunction);
        }

        // Add struct level variables to the init function.
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            // Only add a field if it is required. Checking if it's required is enough since non-defaultable
            // required fields will have been caught in the type checking phase.
            if (!recordTypeNode.initFunction.initFunctionStmts.containsKey(field.symbol) &&
                    !Symbols.isOptional(field.symbol) && field.expr != null) {
                recordTypeNode.initFunction.initFunctionStmts
                        .put(field.symbol, createStructFieldUpdate(recordTypeNode.initFunction, field,
                                                                   recordTypeNode.initFunction.receiver.symbol));
            }
        }

        //Adding init statements to the init function.
        BLangStatement[] initStmts = recordTypeNode.initFunction.initFunctionStmts
                .values().toArray(new BLangStatement[0]);
        BLangBlockFunctionBody initFnBody = (BLangBlockFunctionBody) recordTypeNode.initFunction.body;
        for (int i = 0; i < recordTypeNode.initFunction.initFunctionStmts.size(); i++) {
            initFnBody.stmts.add(i, initStmts[i]);
        }

        // TODO:
        // Add invocations for the initializers of each of the type referenced records. Here, the initializers of the
        // referenced types are invoked on the current record type.

        if (recordTypeNode.isAnonymous && recordTypeNode.isLocal) {
            BLangUserDefinedType userDefinedType = desugarLocalAnonRecordTypeNode(recordTypeNode);
            TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordTypeNode.getBType(),
                    recordTypeNode.getBType().tsymbol, recordTypeNode, env);
            recordTypeNode.desugared = true;
            result = userDefinedType;
            return;
        }

        result = recordTypeNode;
    }

    private BLangUserDefinedType desugarLocalAnonRecordTypeNode(BLangRecordTypeNode recordTypeNode) {
        return ASTBuilderUtil.createUserDefineTypeNode(recordTypeNode.symbol.name.value, recordTypeNode.getBType(),
                                                       recordTypeNode.pos);
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        arrayType.elemtype = rewrite(arrayType.elemtype, env);
        result = arrayType;
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        constrainedType.constraint = rewrite(constrainedType.constraint, env);
        result = constrainedType;
    }

    @Override
    public void visit(BLangStreamType streamType) {
        streamType.constraint = rewrite(streamType.constraint, env);
        streamType.error = rewrite(streamType.error, env);
        result = streamType;
    }

    @Override
    public void visit(BLangTableTypeNode tableTypeNode) {
        tableTypeNode.constraint = rewrite(tableTypeNode.constraint, env);
        tableTypeNode.tableKeyTypeConstraint = rewrite(tableTypeNode.tableKeyTypeConstraint, env);
        result = tableTypeNode;
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint keyTypeConstraint) {
        keyTypeConstraint.keyType = rewrite(keyTypeConstraint.keyType, env);
        result = keyTypeConstraint;
    }

    @Override
    public void visit(BLangValueType valueType) {
        result = valueType;
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        result = userDefinedType;
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        List<BLangType> rewrittenMembers = new ArrayList<>();
        unionTypeNode.memberTypeNodes.forEach(typeNode -> rewrittenMembers.add(rewrite(typeNode, env)));
        unionTypeNode.memberTypeNodes = rewrittenMembers;
        result = unionTypeNode;
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        List<BLangType> rewrittenConstituents = new ArrayList<>();

        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            rewrittenConstituents.add(rewrite(constituentTypeNode, env));
        }

        intersectionTypeNode.constituentTypeNodes = rewrittenConstituents;
        result = intersectionTypeNode;
    }

    @Override
    public void visit(BLangErrorType errorType) {
        errorType.detailType = rewrite(errorType.detailType, env);

        boolean hasTypeParam = errorType.getDetailsTypeNode() != null;
        // Error without type param is either a user-defined-type or a default error, they don't need a type-def.
        // We need to create type-defs for local anonymous types with type param.
        if (errorType.isLocal && errorType.isAnonymous && hasTypeParam) {
            BLangUserDefinedType userDefinedType = desugarLocalAnonRecordTypeNode(errorType);
            TypeDefBuilderHelper.createTypeDefinitionForTSymbol(errorType.getBType(), errorType.getBType().tsymbol,
                    errorType, env);
            errorType.desugared = true;
            result = userDefinedType;
            return;
        }
        result = errorType;
    }

    private BLangUserDefinedType desugarLocalAnonRecordTypeNode(BLangErrorType errorTypeNode) {
        return ASTBuilderUtil.createUserDefineTypeNode(errorTypeNode.getBType().tsymbol.name.value,
                                                       errorTypeNode.getBType(),
                                                       errorTypeNode.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        SymbolEnv typeDefEnv =
                            SymbolEnv.createTypeEnv(functionTypeNode, functionTypeNode.getBType().tsymbol.scope, env);
        for (BLangSimpleVariable param : functionTypeNode.params) {
            rewrite(param, typeDefEnv);
        }
        if (functionTypeNode.restParam != null) {
            rewrite(functionTypeNode.restParam, typeDefEnv);
        }
        functionTypeNode.returnTypeNode = rewrite(functionTypeNode.returnTypeNode, typeDefEnv);
        result = functionTypeNode;
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode refTypeNode) {
        result = refTypeNode;
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        List<BLangType> rewrittenMembers = new ArrayList<>();
        tupleTypeNode.memberTypeNodes.forEach(member -> rewrittenMembers.add(rewrite(member, env)));
        tupleTypeNode.memberTypeNodes = rewrittenMembers;
        tupleTypeNode.restParamType = rewrite(tupleTypeNode.restParamType, env);
        result = tupleTypeNode;
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        body.stmts = rewriteStmt(body.stmts, bodyEnv);
        result = body;
    }

    @Override
    public void visit(BLangExprFunctionBody exprBody) {
        BLangBlockFunctionBody body = ASTBuilderUtil.createBlockFunctionBody(exprBody.pos, new ArrayList<>());
        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(exprBody.pos, body);
        returnStmt.expr = rewriteExpr(exprBody.expr);
        result = body;
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        for (BLangAnnotationAttachment attachment : body.annAttachments) {
            rewrite(attachment, env);
        }
        result = body;
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        Map<Name, BLangStatement> prevXmlnsDecls = this.stmtsToBePropagatedToQuery;
        if (!funcNode.flagSet.contains(Flag.QUERY_LAMBDA)) {
            this.stmtsToBePropagatedToQuery = new HashMap<>();
        }
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        if (!funcNode.interfaceFunction) {
            addReturnIfNotPresent(funcNode);
        }

        // Duplicate the invokable symbol and the invokable type.
        funcNode.originalFuncSymbol = funcNode.symbol;
        funcNode.symbol = ASTBuilderUtil.duplicateInvokableSymbol(funcNode.symbol);
        funcNode.requiredParams = rewrite(funcNode.requiredParams, funcEnv);
        funcNode.restParam = rewrite(funcNode.restParam, funcEnv);

        if (funcNode.returnTypeNode != null && funcNode.returnTypeNode.getKind() != null) {
            funcNode.returnTypeNode = rewrite(funcNode.returnTypeNode, funcEnv);
        }

        funcNode.body = rewrite(funcNode.body, funcEnv);
        funcNode.annAttachments.forEach(attachment -> rewrite(attachment, env));
        if (funcNode.returnTypeNode != null) {
            funcNode.returnTypeAnnAttachments.forEach(attachment -> rewrite(attachment, env));
        }
        this.stmtsToBePropagatedToQuery = prevXmlnsDecls;
        result = funcNode;
    }

    @Override
    public void visit(BLangInferredTypedescDefaultNode inferTypedescExpr) {
        BType constraintType = ((BTypedescType) inferTypedescExpr.getBType()).constraint;
        result = ASTBuilderUtil.createTypedescExpr(inferTypedescExpr.pos, inferTypedescExpr.getBType(), constraintType);
    }

    public void visit(BLangAnnotation annotationNode) {
        annotationNode.annAttachments.forEach(attachment ->  rewrite(attachment, env));
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (annAttachmentNode.expr == null && annAttachmentNode.annotationSymbol.attachedType != null) {
            BType attachedType =
                    Types.getReferredType(annAttachmentNode.annotationSymbol.attachedType);
            if (attachedType.tag != TypeTags.FINITE) {
                annAttachmentNode.expr = ASTBuilderUtil.createEmptyRecordLiteral(annAttachmentNode.pos,
                        attachedType.tag == TypeTags.ARRAY ? ((BArrayType) attachedType).eType : attachedType);
            }
        }
        if (annAttachmentNode.expr != null) {
            annAttachmentNode.expr = rewrite(annAttachmentNode.expr, env);
            for (AttachPoint point : annAttachmentNode.annotationSymbol.points) {
                if (!point.source) {
                    annAttachmentNode.expr = visitCloneReadonly(annAttachmentNode.expr,
                                                                annAttachmentNode.expr.getBType());
                    break;
                }
            }
        }
        result = annAttachmentNode;
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (varNode.typeNode != null && varNode.typeNode.getKind() != null) {
            varNode.typeNode = rewrite(varNode.typeNode, env);
        }

        BLangExpression bLangExpression;
        if (Symbols.isFlagOn(varNode.symbol.flags, Flags.DEFAULTABLE_PARAM)) {
            bLangExpression = varNode.expr;
        } else {
            bLangExpression = rewriteExpr(varNode.expr);
            if (bLangExpression != null) {
                bLangExpression = addConversionExprIfRequired(bLangExpression, varNode.getBType());
            }
        }

        varNode.expr = bLangExpression;

        varNode.annAttachments.forEach(attachment -> rewrite(attachment, env));

        result = varNode;
    }

    @Override
    public void visit(BLangLetExpression letExpression) {
        SymbolEnv prevEnv = this.env;
        // Set the enclInvokable of let expression since, when in module level the enclInvokable will be initFunction
        // and the initFunction is created in desugar phase.
        letExpression.env.enclInvokable = this.env.enclInvokable;
        this.env = letExpression.env;
        BLangExpression expr = letExpression.expr;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(letExpression.pos);
        blockStmt.scope = letExpression.env.scope;
        blockStmt.isLetExpr = true;

        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            BLangNode node  = rewrite((BLangNode) letVariable.definitionNode, env);
            if (node.getKind() == NodeKind.BLOCK) {
                blockStmt.stmts.addAll(((BLangBlockStmt) node).stmts);
            } else {
                blockStmt.addStatement((BLangSimpleVariableDef) node);
            }
        }
        BLangSimpleVariableDef tempVarDef = createVarDef(String.format("$let_var_%d_$", letCount++),
                                                         expr.getBType(), expr, expr.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(expr.pos, tempVarDef.var.symbol);
        blockStmt.addStatement(tempVarDef);
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, tempVarRef);
        stmtExpr.setBType(expr.getBType());
        result = rewrite(stmtExpr, env);
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangTupleVariable varNode) {
        varNode.typeNode = rewrite(varNode.typeNode, env);
        //  case 1:
        //  [string, int] (a, b) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0];
        //  int b = x[1];
        //
        //  case 2:
        //  [[string, float], int] [[a, b], c] = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0][0];
        //  float b = x[0][1];
        //  int c = x[1];

        // Create tuple destruct block stmt
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(varNode.pos);

        // Create a simple var for the array 'any[] x = (tuple)' based on the dimension for x

        String name = anonModelHelper.getNextTupleVarKey(env.enclPkg.packageID);
        final BLangSimpleVariable tuple =
                ASTBuilderUtil.createVariable(varNode.pos, name, symTable.arrayAllType, null,
                                              new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID,
                                                             symTable.arrayAllType, this.env.scope.owner, varNode.pos,
                                                             VIRTUAL));
        tuple.expr = varNode.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(varNode.pos, blockStmt);
        variableDef.var = tuple;

        // Create the variable definition statements using the root block stmt created
        createVarDefStmts(varNode, blockStmt, tuple.symbol, null);
        createRestFieldVarDefStmts(varNode, blockStmt, tuple.symbol, varNode.expr);

        // Finally rewrite the populated block statement
        result = rewrite(blockStmt, env);
    }

    @Override
    public void visit(BLangRecordVariable varNode) {
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(varNode.pos);
        String name = anonModelHelper.getNextRecordVarKey(env.enclPkg.packageID);
        final BLangSimpleVariable mapVariable =
                ASTBuilderUtil.createVariable(varNode.pos, name, symTable.mapAllType, null,
                                              new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID,
                                                             symTable.mapAllType, this.env.scope.owner, varNode.pos,
                                                             VIRTUAL));
        mapVariable.expr = varNode.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(varNode.pos, blockStmt);
        variableDef.var = mapVariable;

        createVarDefStmts(varNode, blockStmt, mapVariable.symbol, null);

        result = rewrite(blockStmt, env);
    }

    @Override
    public void visit(BLangErrorVariable varNode) {
        // Create error destruct block stmt.
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(varNode.pos);

        BType errorType = varNode.getBType() == null ? symTable.errorType : varNode.getBType();
        // Create a simple var for the error 'error x = ($error$)'.
        String name = anonModelHelper.getNextErrorVarKey(env.enclPkg.packageID);
        BVarSymbol errorVarSymbol = new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID,
                                                   errorType, this.env.scope.owner, varNode.pos, VIRTUAL);
        final BLangSimpleVariable error = ASTBuilderUtil.createVariable(varNode.pos, name, errorType, null,
                errorVarSymbol);
        error.expr = varNode.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(varNode.pos, blockStmt);
        variableDef.var = error;

        // Create the variable definition statements using the root block stmt created.
        createVarDefStmts(varNode, blockStmt, error.symbol, null);

        // Finally rewrite the populated block statement.
        result = rewrite(blockStmt, env);
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt block) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(block, env);
        block.stmts = rewriteStmt(block.stmts, blockEnv);
        result = block;
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangTupleVariableDef varDefNode) {
        result = rewrite(varDefNode.var, env);
    }

    private void createRestFieldVarDefStmts(BLangTupleVariable parentTupleVariable, BLangBlockStmt blockStmt,
                                            BVarSymbol tupleVarSymbol, BLangExpression tupleExpr) {
        final BLangSimpleVariable arrayVar = (BLangSimpleVariable) parentTupleVariable.restVariable;
        Location pos = blockStmt.pos;
        if (arrayVar != null) {
            // T[] t = [];
            BLangArrayLiteral arrayExpr = createArrayLiteralExprNode();
            arrayExpr.setBType(arrayVar.getBType());
            arrayVar.expr = arrayExpr;
            BLangSimpleVariableDef arrayVarDef = ASTBuilderUtil.createVariableDefStmt(arrayVar.pos, blockStmt);
            arrayVarDef.var = arrayVar;
            BLangSimpleVarRef arrayVarRef = ASTBuilderUtil.createVariableRef(pos, arrayVar.symbol);

            // [int, float, int, string...] v = [1, 2.0, 3, "B"];
            // var [a, ...rest] = v;
            // rest is [float, int, string...].
            // Do the following first.
            // rest[0] = v[1];
            // rest[1] = v[2];
            BType restType = parentTupleVariable.restVariable.getBType();
            BLangExpression countExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType,
                    (long) parentTupleVariable.memberVariables.size());
            boolean isIndexBasedAccessExpr = tupleExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR;
            if (restType.tag == TypeTags.TUPLE) {
                BTupleType restTupleType = (BTupleType) restType;
                for (int index = 0; index < restTupleType.tupleTypes.size(); index++) {
                    BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType,
                            (long) index);
                    BLangIndexBasedAccess tupleValueExpr = ASTBuilderUtil.createIndexAccessExpr(arrayVarRef, indexExpr);
                    tupleValueExpr.setBType(restTupleType.tupleTypes.get(index));
                    if (isIndexBasedAccessExpr) {
                        createAssignmentStmt(tupleValueExpr, blockStmt, countExpr, tupleVarSymbol,
                                (BLangIndexBasedAccess) tupleExpr);
                    } else {
                        createAssignmentStmt(tupleValueExpr, blockStmt, countExpr, tupleVarSymbol, null);
                    }
                    // Increment the count for each visited member in the tupleLiteral.
                    countExpr = ASTBuilderUtil.createBinaryExpr(pos, countExpr, createLiteral(pos, symTable.intType,
                            (long) 1), symTable.intType, OperatorKind.ADD, null);
                }

                if (restTupleType.restType == null) {
                    return;
                }
            }

            // The remaining values should be mapped with the restType.
            // count => start with index of the first member out of the remaining values.
            // foreach var $foreach$i in count...tupleLiteral.length() {
            //     t[t.length()] = <T> tupleLiteral[$foreach$i];
            BLangInvocation lengthInvocation = createLengthInvocation(pos, tupleExpr);
            BLangInvocation intRangeInvocation = replaceWithIntRange(pos, countExpr,
                    getModifiedIntRangeEndExpr(lengthInvocation));

            BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
            foreach.pos = pos;
            foreach.collection = intRangeInvocation;
            types.setForeachTypedBindingPatternType(foreach);

            final BLangSimpleVariable foreachVariable = ASTBuilderUtil.createVariable(pos,
                    "$foreach$i", foreach.varType);
            foreachVariable.symbol = new BVarSymbol(0, names.fromIdNode(foreachVariable.name),
                                                    this.env.scope.owner.pkgID, foreachVariable.getBType(),
                                                    this.env.scope.owner, pos, VIRTUAL);
            BLangSimpleVarRef foreachVarRef = ASTBuilderUtil.createVariableRef(pos, foreachVariable.symbol);
            foreach.variableDefinitionNode = ASTBuilderUtil.createVariableDef(pos, foreachVariable);
            foreach.isDeclaredWithVar = true;
            BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);

            // t[t.length()] = <T> tupleLiteral[$foreach$i];
            BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(arrayVarRef,
                    createLengthInvocation(pos, arrayVarRef));
            if (restType.tag == TypeTags.TUPLE) {
                indexAccessExpr.setBType(((BTupleType) restType).restType);
            } else {
                indexAccessExpr.setBType(((BArrayType) restType).eType);
            }
            if (isIndexBasedAccessExpr) {
                createAssignmentStmt(indexAccessExpr, foreachBody, foreachVarRef, tupleVarSymbol,
                        (BLangIndexBasedAccess) tupleExpr);
            } else {
                createAssignmentStmt(indexAccessExpr, foreachBody, foreachVarRef, tupleVarSymbol, null);
            }
            foreach.body = foreachBody;
            blockStmt.addStatement(foreach);
        }
    }

    @Override
    public void visit(BLangRecordVariableDef varDefNode) {
        result = rewrite(varDefNode.var, env);
    }

    @Override
    public void visit(BLangErrorVariableDef varDefNode) {
        result = rewrite(varDefNode.errorVariable, env);
    }

    /**
     * This method iterate through each member of the tupleVar and create the relevant var def statements. This method
     * does the check for node kind of each member and call the related var def creation method.
     *
     * Example:
     * ((string, float) int)) ((a, b), c)) = (tuple)
     *
     * (a, b) is again a tuple, so it is a recursive var def creation.
     *
     * c is a simple var, so a simple var def will be created.
     *
     */
    private void createVarDefStmts(BLangTupleVariable parentTupleVariable, BLangBlockStmt parentBlockStmt,
                                   BVarSymbol tupleVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {

        final List<BLangVariable> memberVars = parentTupleVariable.memberVariables;
        for (int index = 0; index < memberVars.size(); index++) {
            BLangVariable variable = memberVars.get(index);
            BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(variable.pos, symTable.intType, (long) index);

            if (NodeKind.VARIABLE == variable.getKind()) { //if this is simple var, then create a simple var def stmt
                createSimpleVarDefStmt((BLangSimpleVariable) variable, parentBlockStmt, indexExpr, tupleVarSymbol,
                        parentIndexAccessExpr);
                continue;
            }

            if (variable.getKind() == NodeKind.TUPLE_VARIABLE) { // Else recursively create the var def statements.
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVariable.pos,
                        new BArrayType(symTable.anyType), tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangTupleVariable) variable, parentBlockStmt, tupleVarSymbol, arrayAccessExpr);
                createRestFieldVarDefStmts(tupleVariable, parentBlockStmt, tupleVarSymbol, arrayAccessExpr);
                continue;
            }

            if (variable.getKind() == NodeKind.RECORD_VARIABLE) {
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentTupleVariable.pos, symTable.mapType, tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangRecordVariable) variable, parentBlockStmt, tupleVarSymbol, arrayAccessExpr);
                continue;
            }

            if (variable.getKind() == NodeKind.ERROR_VARIABLE) {

                BType accessedElemType = symTable.errorType;
                BType tupleVarType = Types.getReferredType(tupleVarSymbol.type);
                if (tupleVarType.tag == TypeTags.ARRAY) {
                    BArrayType arrayType = (BArrayType) tupleVarType;
                    accessedElemType = arrayType.eType;
                }
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentTupleVariable.pos, accessedElemType, tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangErrorVariable) variable, parentBlockStmt, tupleVarSymbol, arrayAccessExpr);
            }
        }
    }

    /**
     * Overloaded method to handle record variables.
     * This method iterate through each member of the recordVar and create the relevant var def statements. This method
     * does the check for node kind of each member and call the related var def creation method.
     *
     * Example:
     * type Foo record {
     *     string name;
     *     (int, string) age;
     *     Address address;
     * };
     *
     * Foo {name: a, age: (b, c), address: d} = {record literal}
     *
     *  a is a simple var, so a simple var def will be created.
     *
     * (b, c) is a tuple, so it is a recursive var def creation.
     *
     * d is a record, so it is a recursive var def creation.
     *
     */
    private void createVarDefStmts(BLangRecordVariable parentRecordVariable, BLangBlockStmt parentBlockStmt,
                                   BVarSymbol recordVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {

        List<BLangRecordVariableKeyValue> variableList = parentRecordVariable.variableList;
        for (BLangRecordVariableKeyValue recordFieldKeyValue : variableList) {
            BLangVariable variable = recordFieldKeyValue.valueBindingPattern;
            BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(variable.pos, symTable.stringType,
                    recordFieldKeyValue.key.value);

            if (recordFieldKeyValue.valueBindingPattern.getKind() == NodeKind.VARIABLE) {
                createSimpleVarDefStmt((BLangSimpleVariable) recordFieldKeyValue.valueBindingPattern, parentBlockStmt,
                        indexExpr, recordVarSymbol, parentIndexAccessExpr);
                continue;
            }

            if (recordFieldKeyValue.valueBindingPattern.getKind() == NodeKind.TUPLE_VARIABLE) {
                BLangTupleVariable tupleVariable = (BLangTupleVariable) recordFieldKeyValue.valueBindingPattern;
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVariable.pos,
                        new BArrayType(symTable.anyType), recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangTupleVariable) recordFieldKeyValue.valueBindingPattern,
                        parentBlockStmt, recordVarSymbol, arrayAccessExpr);
                continue;
            }

            if (recordFieldKeyValue.valueBindingPattern.getKind() == NodeKind.RECORD_VARIABLE) {
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentRecordVariable.pos, symTable.mapType, recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangRecordVariable) recordFieldKeyValue.valueBindingPattern, parentBlockStmt,
                        recordVarSymbol, arrayAccessExpr);
                continue;
            }

            if (variable.getKind() == NodeKind.ERROR_VARIABLE) {
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentRecordVariable.pos, variable.getBType(), recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangErrorVariable) variable, parentBlockStmt, recordVarSymbol, arrayAccessExpr);
            }
        }

        if (parentRecordVariable.restParam != null) {
            // The restParam is desugared to a filter iterable operation that filters out the fields provided in the
            // record variable
            // map<any> restParam = $map$_0.filter($lambdaArg$_0);

            Location pos = parentBlockStmt.pos;
            BType restParamType = parentRecordVariable.restParam.getBType();
            BLangSimpleVarRef variableReference;

            if (parentIndexAccessExpr != null) {
                BLangSimpleVariable mapVariable =
                        ASTBuilderUtil.createVariable(pos, "$map$_1",
                                                      parentIndexAccessExpr.getBType(), null,
                                                      new BVarSymbol(0, names.fromString("$map$_1"),
                                                                     this.env.scope.owner.pkgID,
                                                                     parentIndexAccessExpr.getBType(),
                                                                     this.env.scope.owner, pos, VIRTUAL));
                mapVariable.expr = parentIndexAccessExpr;
                BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(pos, parentBlockStmt);
                variableDef.var = mapVariable;

                variableReference = ASTBuilderUtil.createVariableRef(pos, mapVariable.symbol);
            } else {
                variableReference = ASTBuilderUtil.createVariableRef(pos,
                        ((BLangSimpleVariableDef) parentBlockStmt.stmts.get(0)).var.symbol);
            }

            List<String> keysToRemove = parentRecordVariable.variableList.stream()
                    .map(var -> var.getKey().getValue())
                    .collect(Collectors.toList());

            BLangSimpleVariable filteredDetail = generateRestFilter(variableReference, pos,
                    keysToRemove, restParamType, parentBlockStmt);

            BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(pos, filteredDetail.symbol);

            // Create rest param variable definition
            BLangSimpleVariable restParam = (BLangSimpleVariable) parentRecordVariable.restParam;
            BLangSimpleVariableDef restParamVarDef = ASTBuilderUtil.createVariableDefStmt(pos,
                    parentBlockStmt);
            restParamVarDef.var = restParam;
            restParamVarDef.var.setBType(restParamType);
            restParam.expr = varRef;
        }
    }

    /**
     * This method will create the relevant var def statements for reason and details of the error variable.
     * The var def statements are created by creating the reason() and detail() builtin methods.
     */
    private void createVarDefStmts(BLangErrorVariable parentErrorVariable, BLangBlockStmt parentBlockStmt,
                                   BVarSymbol errorVariableSymbol, BLangIndexBasedAccess parentIndexBasedAccess) {

        BVarSymbol convertedErrorVarSymbol;
        if (parentIndexBasedAccess != null) {
            BType prevType = parentIndexBasedAccess.getBType();
            parentIndexBasedAccess.setBType(symTable.anyType);
            BLangSimpleVariableDef errorVarDef = createVarDef(GENERATED_ERROR_VAR + UNDERSCORE + errorCount++,
                    symTable.errorType,
                    addConversionExprIfRequired(parentIndexBasedAccess, symTable.errorType),
                    parentErrorVariable.pos);
            parentIndexBasedAccess.setBType(prevType);
            parentBlockStmt.addStatement(errorVarDef);
            convertedErrorVarSymbol = errorVarDef.var.symbol;
        } else {
            convertedErrorVarSymbol = errorVariableSymbol;
        }

        if (parentErrorVariable.message != null) {
            parentErrorVariable.message.expr =
                    generateErrorMessageBuiltinFunction(parentErrorVariable.message.pos,
                                                        parentErrorVariable.message.getBType(), convertedErrorVarSymbol,
                                                        null);

            if (names.fromIdNode(parentErrorVariable.message.name) == Names.IGNORE) {
                parentErrorVariable.message = null;
            } else {
                BLangSimpleVariableDef messageVariableDef =
                        ASTBuilderUtil.createVariableDefStmt(parentErrorVariable.message.pos, parentBlockStmt);
                messageVariableDef.var = parentErrorVariable.message;
            }
        }

        if (parentErrorVariable.cause != null) {
            parentErrorVariable.cause.expr =
                    generateErrorCauseLanglibFunction(parentErrorVariable.cause.pos,
                                                      parentErrorVariable.cause.getBType(), convertedErrorVarSymbol,
                                                      null);
            BLangVariable errorCause = parentErrorVariable.cause;

            if (errorCause.getKind() == NodeKind.ERROR_VARIABLE) {
                BLangErrorVariableDef errorVarDef = createErrorVariableDef(errorCause.pos,
                        (BLangErrorVariable) errorCause);
                BLangNode blockStatementNode = rewrite(errorVarDef, env);
                List<BLangStatement> statements = ((BLangBlockStmt) blockStatementNode).stmts;

                for (BLangStatement statement : statements) {
                    parentBlockStmt.addStatement(statement);
                }
            } else {
                BLangSimpleVariableDef causeVariableDef =
                        ASTBuilderUtil.createVariableDefStmt(parentErrorVariable.cause.pos, parentBlockStmt);
                causeVariableDef.var = (BLangSimpleVariable) parentErrorVariable.cause;
            }
        }

        if ((parentErrorVariable.detail == null || parentErrorVariable.detail.isEmpty())
            && parentErrorVariable.restDetail == null) {
            return;
        }

        parentErrorVariable.detailExpr = generateErrorDetailBuiltinFunction(
                parentErrorVariable.pos,
                convertedErrorVarSymbol, null);

        BLangSimpleVariableDef detailTempVarDef = createVarDef("$error$detail",
                                                               parentErrorVariable.detailExpr.getBType(),
                                                               parentErrorVariable.detailExpr, parentErrorVariable.pos);
        detailTempVarDef.setBType(parentErrorVariable.detailExpr.getBType());
        parentBlockStmt.addStatement(detailTempVarDef);

        this.env.scope.define(names.fromIdNode(detailTempVarDef.var.name), detailTempVarDef.var.symbol);

        for (BLangErrorVariable.BLangErrorDetailEntry detailEntry : parentErrorVariable.detail) {
            BLangExpression detailEntryVar = createErrorDetailVar(detailEntry, detailTempVarDef.var.symbol);

            // create the bound variable, and final rewrite will define them in sym table.
            createAndAddBoundVariableDef(parentBlockStmt, detailEntry, detailEntryVar);

        }
        if (parentErrorVariable.restDetail != null && !parentErrorVariable.restDetail.name.value.equals(IGNORE.value)) {
            Location pos = parentErrorVariable.restDetail.pos;
            BLangSimpleVarRef detailVarRef = ASTBuilderUtil.createVariableRef(
                    pos, detailTempVarDef.var.symbol);
            List<String> keysToRemove = parentErrorVariable.detail.stream()
                    .map(detail -> detail.key.getValue())
                    .collect(Collectors.toList());

            BLangSimpleVariable filteredDetail = generateRestFilter(detailVarRef, parentErrorVariable.pos, keysToRemove,
                                                                    parentErrorVariable.restDetail.getBType(),
                                                                    parentBlockStmt);

            BLangSimpleVariableDef variableDefStmt = ASTBuilderUtil.createVariableDefStmt(pos, parentBlockStmt);
            variableDefStmt.var = ASTBuilderUtil.createVariable(pos,
                    parentErrorVariable.restDetail.name.value,
                                                                filteredDetail.getBType(),
                    ASTBuilderUtil.createVariableRef(pos, filteredDetail.symbol),
                    parentErrorVariable.restDetail.symbol);
        }
    }

    private BLangSimpleVariableDef forceCastIfApplicable(BVarSymbol errorVarySymbol, Location pos,
                                                         BType targetType) {
        BVarSymbol errorVarSym = new BVarSymbol(Flags.PUBLIC, names.fromString("$cast$temp$"),
                                                this.env.enclPkg.packageID, targetType, this.env.scope.owner, pos,
                                                VIRTUAL);
        BLangSimpleVarRef variableRef = ASTBuilderUtil.createVariableRef(pos, errorVarySymbol);

        BLangExpression expr;
        if (targetType.tag == TypeTags.RECORD) {
            expr = variableRef;
        } else {
            expr = addConversionExprIfRequired(variableRef, targetType);
        }
        BLangSimpleVariable errorVar = ASTBuilderUtil.createVariable(pos, errorVarSym.name.value, targetType, expr,
                errorVarSym);
        return ASTBuilderUtil.createVariableDef(pos, errorVar);
    }

    private BType getRestFilterConstraintType(BType targetType) {
        BType constraintType;
        if (targetType.tag == TypeTags.RECORD) {
            BRecordType recordType = (BRecordType) targetType;
            Map<String, BField> remainingFields = recordType.fields.entrySet()
                    .stream().filter(entry -> entry.getValue().type.tag != TypeTags.NEVER)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            constraintType = symbolEnter.getRestMatchPatternConstraintType(recordType, remainingFields,
                    recordType.restFieldType);
        } else {
            constraintType = ((BMapType) targetType).constraint;
        }
        return constraintType;
    }

    private BLangSimpleVariable generateRestFilter(BLangSimpleVarRef mapVarRef, Location pos,
                                                   List<String> keysToRemove, BType targetType,
                                                   BLangBlockStmt parentBlockStmt) {
        //      restVar = (<map<T>mapVarRef)
        //                       .entries()
        //                       .filter([key, val] => isKeyTakenLambdaInvoke())
        //                       .map([key, val] => val);

        BLangExpression typeCastExpr = addConversionExprIfRequired(mapVarRef, targetType);

        int restNum = annonVarCount++;
        String name = "$map$ref$" + UNDERSCORE + restNum;
        BLangSimpleVariable mapVariable = defVariable(pos, targetType, parentBlockStmt, typeCastExpr, name);

        BLangInvocation entriesInvocation = generateMapEntriesInvocation(
                ASTBuilderUtil.createVariableRef(pos, mapVariable.symbol), typeCastExpr.getBType());
        String entriesVarName = "$map$ref$entries$" + UNDERSCORE + restNum;
        BType constraintType = getRestFilterConstraintType(targetType);
        BType entriesType = new BMapType(TypeTags.MAP,
                new BTupleType(Arrays.asList(symTable.stringType, constraintType)), null);
        BLangSimpleVariable entriesInvocationVar = defVariable(pos, entriesType, parentBlockStmt,
                addConversionExprIfRequired(entriesInvocation, entriesType),
                entriesVarName);

        BLangLambdaFunction filter = createFuncToFilterOutRestParam(keysToRemove, pos);

        BLangInvocation filterInvocation = generateMapFilterInvocation(pos, entriesInvocationVar, filter);
        String filteredEntriesName = "$filtered$detail$entries" + restNum;
        BLangSimpleVariable filteredVar = defVariable(pos, entriesType, parentBlockStmt, filterInvocation,
                filteredEntriesName);

        String filteredVarName = "$detail$filtered" + restNum;
        BLangLambdaFunction backToMapLambda = generateEntriesToMapLambda(pos, constraintType);
        BLangInvocation mapInvocation = generateMapMapInvocation(pos, filteredVar, backToMapLambda);
        BLangSimpleVariable filtered = defVariable(pos, targetType, parentBlockStmt,
                mapInvocation,
                filteredVarName);

        BLangSimpleVariable converted = filtered;
        if (targetType.tag == TypeTags.RECORD) {
            String filteredRecVarName = "$filteredRecord";
            BLangInvocation recordConversion = generateCreateRecordValueInvocation(pos, targetType, filtered.symbol);
            converted = defVariable(pos, targetType, parentBlockStmt, recordConversion, filteredRecVarName);
        }

        String filteredRestVarName = "$restVar$" + UNDERSCORE + restNum;

        return defVariable(pos, targetType, parentBlockStmt,
                addConversionExprIfRequired(createVariableRef(pos, converted.symbol), targetType),
                filteredRestVarName);
    }

    private BLangInvocation generateMapEntriesInvocation(BLangExpression expr, BType type) {
        BLangInvocation invocationNode = createInvocationNode("entries", new ArrayList<>(), type);

        invocationNode.expr = expr;
        invocationNode.symbol = symResolver.lookupLangLibMethod(type, names.fromString("entries"), env);
        invocationNode.requiredArgs = Lists.of(expr);
        invocationNode.setBType(invocationNode.symbol.type.getReturnType());
        invocationNode.langLibInvocation = true;
        return invocationNode;
    }

    private BLangInvocation generateMapMapInvocation(Location pos, BLangSimpleVariable filteredVar,
                                                     BLangLambdaFunction backToMapLambda) {
        BLangInvocation invocationNode = createInvocationNode("map", new ArrayList<>(), filteredVar.getBType());

        invocationNode.expr = ASTBuilderUtil.createVariableRef(pos, filteredVar.symbol);
        invocationNode.symbol = symResolver.lookupLangLibMethod(filteredVar.getBType(), names.fromString("map"), env);
        invocationNode.requiredArgs = Lists.of(ASTBuilderUtil.createVariableRef(pos, filteredVar.symbol));
        invocationNode.setBType(invocationNode.symbol.type.getReturnType());
        invocationNode.requiredArgs.add(backToMapLambda);
        return invocationNode;
    }

    private BLangLambdaFunction generateEntriesToMapLambda(Location pos, BType constraint) {
        // var.map([key, val] => val)

        String anonfuncName = "$anonGetValFunc$" + UNDERSCORE + lambdaFunctionCount++;
        BLangFunction function = ASTBuilderUtil.createFunction(pos, anonfuncName);

        BVarSymbol keyValSymbol = new BVarSymbol(0, names.fromString("$lambdaArg$_0"), this.env.scope.owner.pkgID,
                                                 getStringAnyTupleType(), this.env.scope.owner, pos, VIRTUAL);

        BLangSimpleVariable inputParameter = ASTBuilderUtil.createVariable(pos, null, getStringAnyTupleType(),
                                                                           null, keyValSymbol);
        function.requiredParams.add(inputParameter);

        BLangUserDefinedType constraintTypeNode = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        constraintTypeNode.setBType(constraint);
        function.returnTypeNode = constraintTypeNode;

        BLangBlockFunctionBody functionBlock = ASTBuilderUtil.createBlockFunctionBody(pos, new ArrayList<>());
        function.body = functionBlock;

        BLangIndexBasedAccess indexBasesAccessExpr =
                ASTBuilderUtil.createIndexBasesAccessExpr(pos, constraint, keyValSymbol,
                                                          ASTBuilderUtil
                                                                  .createLiteral(pos, symTable.intType, (long) 1));
        BLangSimpleVariableDef tupSecondElem = createVarDef("$val", indexBasesAccessExpr.getBType(),
                                                            indexBasesAccessExpr, pos);
        functionBlock.addStatement(tupSecondElem);

        // Create return stmt.
        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(pos, functionBlock);
        returnStmt.expr = ASTBuilderUtil.createVariableRef(pos, tupSecondElem.var.symbol);

        // Create function symbol before visiting desugar phase for the function
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                                                                       new Name(function.name.value),
                                                                       new Name(function.name.originalValue),
                                                                       env.enclPkg.packageID, function.getBType(),
                                                                       env.enclEnv.enclVarSym, true, function.pos,
                                                                       VIRTUAL);
        functionSymbol.originalName = new Name(function.name.originalValue);
        functionSymbol.retType = function.returnTypeNode.getBType();
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = env.scope;
        functionSymbol.type = new BInvokableType(Collections.singletonList(getStringAnyTupleType()), constraint, null);
        function.symbol = functionSymbol;
        rewrite(function, env);
        env.enclPkg.addFunction(function);

        // Create and return a lambda function
        return createLambdaFunction(function, functionSymbol, env);
    }

    private BLangInvocation generateMapFilterInvocation(Location pos,
                                                        BLangSimpleVariable entriesInvocationVar,
                                                        BLangLambdaFunction filter) {
        BLangInvocation invocationNode = createInvocationNode("filter", new ArrayList<>(),
                                                              entriesInvocationVar.getBType());

        invocationNode.expr = ASTBuilderUtil.createVariableRef(pos, entriesInvocationVar.symbol);
        invocationNode.symbol = symResolver.lookupLangLibMethod(entriesInvocationVar.getBType(),
                names.fromString("filter"), env);
        invocationNode.requiredArgs = Lists.of(ASTBuilderUtil.createVariableRef(pos, entriesInvocationVar.symbol));
        invocationNode.setBType(invocationNode.symbol.type.getReturnType());
        invocationNode.requiredArgs.add(filter);

        return invocationNode;
    }

    private BLangSimpleVariable defVariable(Location pos, BType varType, BLangBlockStmt parentBlockStmt,
                                            BLangExpression expression, String name) {
        Name varName = names.fromString(name);
        BLangSimpleVariable detailMap = ASTBuilderUtil.createVariable(pos, name, varType, expression,
                                                                      new BVarSymbol(Flags.PUBLIC, varName,
                                                                                     env.enclPkg.packageID, varType,
                                                                                     env.scope.owner, pos, VIRTUAL));
        BLangSimpleVariableDef constructedMap = ASTBuilderUtil.createVariableDef(pos, detailMap);
        constructedMap.setBType(varType);
        parentBlockStmt.addStatement(constructedMap);
        env.scope.define(varName, detailMap.symbol);
        return detailMap;
    }

    private void createAndAddBoundVariableDef(BLangBlockStmt parentBlockStmt,
                                              BLangErrorVariable.BLangErrorDetailEntry detailEntry,
                                              BLangExpression detailEntryVar) {
        BLangVariable valueBindingPattern = detailEntry.valueBindingPattern;
        NodeKind valueBindingPatternKind = valueBindingPattern.getKind();

        if (valueBindingPatternKind == NodeKind.VARIABLE) {
            BLangSimpleVariableDef errorDetailVar = createVarDef(((BLangSimpleVariable) valueBindingPattern).name.value,
                                                                 valueBindingPattern.getBType(), detailEntryVar,
                                                                 valueBindingPattern.pos);
            errorDetailVar.var.symbol = valueBindingPattern.symbol;
            parentBlockStmt.addStatement(errorDetailVar);
            return;
        }

        valueBindingPattern.expr = detailEntryVar;
        BLangNode blockStatementNode = rewrite(valueBindingPattern, env);
        List<BLangStatement> statements = ((BLangBlockStmt) blockStatementNode).stmts;

        for (BLangStatement statement : statements) {
            parentBlockStmt.addStatement(statement);
        }
    }

    private BLangExpression createErrorDetailVar(BLangErrorVariable.BLangErrorDetailEntry detailEntry,
                                                 BVarSymbol tempDetailVarSymbol) {
        BLangExpression detailEntryVar = createIndexBasedAccessExpr(
                detailEntry.valueBindingPattern.getBType(),
                detailEntry.valueBindingPattern.pos,
                createStringLiteral(detailEntry.key.pos, detailEntry.key.value),
                tempDetailVarSymbol, null);
        if (detailEntryVar.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            BLangIndexBasedAccess bLangIndexBasedAccess = (BLangIndexBasedAccess) detailEntryVar;
            bLangIndexBasedAccess.originalType = symTable.cloneableType;
        }
        return detailEntryVar;
    }

    private BLangExpression constructStringTemplateConcatExpression(List<BLangExpression> exprs) {
        BLangExpression concatExpr = null;
        BLangExpression currentExpr;
        for (BLangExpression expr : exprs) {
            currentExpr = expr;
            if (expr.getBType().tag != TypeTags.STRING && expr.getBType().tag != TypeTags.XML) {
                currentExpr = getToStringInvocationOnExpr(expr);
            }

            if (concatExpr == null) {
                concatExpr = currentExpr;
                continue;
            }

            BType binaryExprType =
                    TypeTags.isXMLTypeTag(concatExpr.getBType().tag)
                            || TypeTags.isXMLTypeTag(currentExpr.getBType().tag) ?
                            symTable.xmlType : symTable.stringType;
            concatExpr =
                    ASTBuilderUtil.createBinaryExpr(concatExpr.pos, concatExpr, currentExpr,
                            binaryExprType, OperatorKind.ADD, null);
        }
        return concatExpr;
    }

    private BLangInvocation getToStringInvocationOnExpr(BLangExpression expression) {
        BInvokableSymbol symbol = (BInvokableSymbol) symTable.langValueModuleSymbol.scope
                .lookup(names.fromString(TO_STRING_FUNCTION_NAME)).symbol;

        List<BLangExpression> requiredArgs = new ArrayList<BLangExpression>() {{
            add(addConversionExprIfRequired(expression, symbol.params.get(0).type));
        }};
        return ASTBuilderUtil.createInvocationExprMethod(expression.pos, symbol, requiredArgs, new ArrayList<>(),
                                                         symResolver);
    }

    // TODO: Move the logic on binding patterns to a seperate class
    private BLangInvocation generateErrorDetailBuiltinFunction(Location pos, BVarSymbol errorVarySymbol,
                                                               BLangIndexBasedAccess parentIndexBasedAccess) {
        BLangExpression onExpr =
                parentIndexBasedAccess != null
                        ? parentIndexBasedAccess : ASTBuilderUtil.createVariableRef(pos, errorVarySymbol);

        return createLangLibInvocationNode(ERROR_DETAIL_FUNCTION_NAME, onExpr, new ArrayList<>(), null, pos);
    }

    private BLangInvocation generateErrorMessageBuiltinFunction(Location pos, BType reasonType,
                                                                BVarSymbol errorVarSymbol,
                                                                BLangIndexBasedAccess parentIndexBasedAccess) {
        BLangExpression onExpr;
        if (parentIndexBasedAccess != null) {
            onExpr = parentIndexBasedAccess;
        } else {
            onExpr = ASTBuilderUtil.createVariableRef(pos, errorVarSymbol);
        }
        return createLangLibInvocationNode(ERROR_MESSAGE_FUNCTION_NAME, onExpr, new ArrayList<>(), reasonType, pos);
    }

    private BLangInvocation generateErrorCauseLanglibFunction(Location pos, BType causeType,
                                                              BVarSymbol errorVarSymbol,
                                                              BLangIndexBasedAccess parentIndexBasedAccess) {
        BLangExpression onExpr;
        if (parentIndexBasedAccess != null) {
            onExpr = parentIndexBasedAccess;
        } else {
            onExpr = ASTBuilderUtil.createVariableRef(pos, errorVarSymbol);
        }
        return createLangLibInvocationNode(ERROR_CAUSE_FUNCTION_NAME, onExpr, new ArrayList<>(), causeType, pos);
    }

    private BLangInvocation generateCreateRecordValueInvocation(Location pos,
                                                                BType targetType,
                                                                BVarSymbol source) {
        BType typedescType = new BTypedescType(targetType, symTable.typeDesc.tsymbol);
        BLangInvocation invocationNode = createInvocationNode(CREATE_RECORD_VALUE, new ArrayList<>(), typedescType);

        BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
        typedescExpr.resolvedType = targetType;
        typedescExpr.setBType(typedescType);

        invocationNode.expr = typedescExpr;
        invocationNode.symbol = symResolver.lookupLangLibMethod(typedescType,
                names.fromString(CREATE_RECORD_VALUE), env);
        invocationNode.requiredArgs = Lists.of(ASTBuilderUtil.createVariableRef(pos, source), typedescExpr);
        invocationNode.setBType(BUnionType.create(null, targetType, symTable.errorType));
        return invocationNode;
    }

    private BLangInvocation generateCloneWithTypeInvocation(Location pos,
                                                            BType targetType,
                                                            BVarSymbol source) {
        BType typedescType = new BTypedescType(targetType, symTable.typeDesc.tsymbol);
        BLangInvocation invocationNode = createInvocationNode(CLONE_WITH_TYPE, new ArrayList<>(), typedescType);

        BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
        typedescExpr.resolvedType = targetType;
        typedescExpr.setBType(typedescType);

        invocationNode.expr = typedescExpr;
        invocationNode.symbol = symResolver.lookupLangLibMethod(typedescType, names.fromString(CLONE_WITH_TYPE), env);
        invocationNode.requiredArgs = Lists.of(ASTBuilderUtil.createVariableRef(pos, source), typedescExpr);
        invocationNode.setBType(BUnionType.create(null, targetType, symTable.errorType));
        return invocationNode;
    }

    private BLangLambdaFunction createFuncToFilterOutRestParam(List<String> toRemoveList, Location pos) {

        // Creates following anonymous function
        //
        // function ((string, any) $lambdaArg$_0) returns boolean {
        //     Following if block is generated for all parameters given in the record variable
        //     if ($lambdaArg$_0[0] == "name") {
        //         return false;
        //     }
        //     if ($lambdaArg$_0[0] == "age") {
        //         return false;
        //     }
        //      return true;
        // }

        String anonfuncName = "$anonRestParamFilterFunc$" + UNDERSCORE + lambdaFunctionCount++;
        BLangFunction function = ASTBuilderUtil.createFunction(pos, anonfuncName);

        BVarSymbol keyValSymbol = new BVarSymbol(0, names.fromString("$lambdaArg$_0"), this.env.scope.owner.pkgID,
                                                 getStringAnyTupleType(), this.env.scope.owner, pos, VIRTUAL);
        BLangBlockFunctionBody functionBlock = createAnonymousFunctionBlock(pos, function, keyValSymbol);

        BLangIndexBasedAccess indexBasesAccessExpr =
                ASTBuilderUtil.createIndexBasesAccessExpr(pos, symTable.anyType, keyValSymbol, ASTBuilderUtil
                        .createLiteral(pos, symTable.intType, (long) 0));
        BLangSimpleVariableDef tupFirstElem = createVarDef("$key", indexBasesAccessExpr.getBType(),
                                                           indexBasesAccessExpr, pos);
        functionBlock.addStatement(tupFirstElem);

        // Create the if statements
        for (String toRemoveItem : toRemoveList) {
            createIfStmt(pos, tupFirstElem.var.symbol, functionBlock, toRemoveItem);
        }

        // Create the final return true statement
        BInvokableSymbol functionSymbol = createReturnTrueStatement(pos, function, functionBlock);

        // Create and return a lambda function
        return createLambdaFunction(function, functionSymbol, env);
    }

    private BLangLambdaFunction createFuncToFilterOutRestParam(BLangRecordVariable recordVariable,
                                                               Location location) {
        List<String> fieldNamesToRemove = recordVariable.variableList.stream()
                .map(var -> var.getKey().getValue())
                .collect(Collectors.toList());
        return createFuncToFilterOutRestParam(fieldNamesToRemove, location);
    }

    private void createIfStmt(Location location,
                              BVarSymbol inputParamSymbol,
                              BLangBlockFunctionBody blockStmt,
                              String key) {
        BLangSimpleVarRef firstElemRef = ASTBuilderUtil.createVariableRef(location, inputParamSymbol);
        BLangExpression converted = addConversionExprIfRequired(firstElemRef, symTable.stringType);

        BLangIf ifStmt = ASTBuilderUtil.createIfStmt(location, blockStmt);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(location, new ArrayList<>());
        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(location, ifBlock);
        returnStmt.expr = ASTBuilderUtil.createLiteral(location, symTable.booleanType, false);
        ifStmt.body = ifBlock;

        BLangGroupExpr groupExpr = new BLangGroupExpr();
        groupExpr.setBType(symTable.booleanType);

        BLangBinaryExpr binaryExpr = ASTBuilderUtil.createBinaryExpr(location, converted,
                ASTBuilderUtil.createLiteral(location, symTable.stringType, key),
                symTable.booleanType, OperatorKind.EQUAL, null);

        binaryExpr.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                binaryExpr.opKind, binaryExpr.lhsExpr.getBType(), binaryExpr.rhsExpr.getBType());

        groupExpr.expression = binaryExpr;
        ifStmt.expr = groupExpr;
    }

    BLangLambdaFunction createLambdaFunction(BLangFunction function, BInvokableSymbol functionSymbol, SymbolEnv env) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaFunction.function = function;
        lambdaFunction.setBType(functionSymbol.type);
        lambdaFunction.capturedClosureEnv = env;
        return lambdaFunction;
    }

    private BInvokableSymbol createReturnTrueStatement(Location pos, BLangFunction function,
                                                       BLangBlockFunctionBody functionBlock) {
        BLangReturn trueReturnStmt = ASTBuilderUtil.createReturnStmt(pos, functionBlock);
        trueReturnStmt.expr = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);

        // Create function symbol before visiting desugar phase for the function
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                                                                       new Name(function.name.value),
                                                                       new Name(function.name.originalValue),
                                                                       env.enclPkg.packageID, function.getBType(),
                                                                       env.enclEnv.enclVarSym, true, function.pos,
                                                                       VIRTUAL);
        functionSymbol.originalName = new Name(function.name.originalValue);
        functionSymbol.retType = function.returnTypeNode.getBType();
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = env.scope;
        functionSymbol.type = new BInvokableType(Collections.singletonList(getStringAnyTupleType()),
                                                 getRestType(functionSymbol), symTable.booleanType, null);
        function.symbol = functionSymbol;
        rewrite(function, env);
        env.enclPkg.addFunction(function);
        return functionSymbol;
    }

    private BLangBlockFunctionBody createAnonymousFunctionBlock(Location pos, BLangFunction function,
                                                                BVarSymbol keyValSymbol) {
        BLangSimpleVariable inputParameter = ASTBuilderUtil.createVariable(pos, null, getStringAnyTupleType(),
                                                                           null, keyValSymbol);
        function.requiredParams.add(inputParameter);
        BLangValueType booleanTypeKind = new BLangValueType();
        booleanTypeKind.typeKind = TypeKind.BOOLEAN;
        booleanTypeKind.setBType(symTable.booleanType);
        function.returnTypeNode = booleanTypeKind;

        BLangBlockFunctionBody functionBlock = ASTBuilderUtil.createBlockFunctionBody(pos, new ArrayList<>());
        function.body = functionBlock;
        return functionBlock;
    }

    private BTupleType getStringAnyTupleType() {
        ArrayList<BType> typeList = new ArrayList<BType>() {{
            add(symTable.stringType);
            add(symTable.anyType);
        }};
        return new BTupleType(typeList);
    }

    /**
     * This method creates a simple variable def and assigns and array expression based on the given indexExpr.
     *
     *  case 1: when there is no parent array access expression, but with the indexExpr : 1
     *  string s = x[1];
     *
     *  case 2: when there is a parent array expression : x[2] and indexExpr : 3
     *  string s = x[2][3];
     *
     *  case 3: when there is no parent array access expression, but with the indexExpr : name
     *  string s = x[name];
     *
     *  case 4: when there is a parent map expression : x[name] and indexExpr : fName
     *  string s = x[name][fName]; // record variable inside record variable
     *
     *  case 5: when there is a parent map expression : x[name] and indexExpr : 1
     *  string s = x[name][1]; // tuple variable inside record variable
     */
    private void createSimpleVarDefStmt(BLangSimpleVariable simpleVariable, BLangBlockStmt parentBlockStmt,
                                        BLangLiteral indexExpr, BVarSymbol tupleVarSymbol,
                                        BLangIndexBasedAccess parentArrayAccessExpr) {

        Name varName = names.fromIdNode(simpleVariable.name);
        if (varName == Names.IGNORE) {
            return;
        }

        final BLangSimpleVariableDef simpleVariableDef = ASTBuilderUtil.createVariableDefStmt(simpleVariable.pos,
                parentBlockStmt);
        simpleVariableDef.var = simpleVariable;

        simpleVariable.expr = createIndexBasedAccessExpr(simpleVariable.getBType(), simpleVariable.pos,
                                                         indexExpr, tupleVarSymbol, parentArrayAccessExpr);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        boolean fieldAccessLVExpr = assignNode.varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR;
        assignNode.varRef = rewriteExpr(assignNode.varRef);
        assignNode.expr = rewriteExpr(assignNode.expr);
        BType castingType = assignNode.varRef.getBType();
        if (fieldAccessLVExpr) {
            castingType = types.addNilForNillableAccessType(castingType);
        }
        assignNode.expr = addConversionExprIfRequired(rewriteExpr(assignNode.expr), castingType);
        result = assignNode;
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructure) {
        //  case 1:
        //  a is string, b is float
        //  (a, b) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0];
        //  int b = x[1];
        //
        //  case 2:
        //  a is string, b is float, c is int
        //  ((a, b), c)) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0][0];
        //  float b = x[0][1];
        //  int c = x[1];


        //create tuple destruct block stmt
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(tupleDestructure.pos);

        //create a array of any-type based on the dimension
        BType runTimeType = new BArrayType(symTable.anyType);

        //create a simple var for the array 'any[] x = (tuple)' based on the dimension for x
        String name = "tuple";
        final BLangSimpleVariable tuple =
                ASTBuilderUtil.createVariable(tupleDestructure.pos, name, runTimeType, null,
                                              new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID,
                                                             runTimeType, this.env.scope.owner, tupleDestructure.pos,
                                                             VIRTUAL));
        tuple.expr = tupleDestructure.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(tupleDestructure.pos,
                                                                                        blockStmt);
        variableDef.var = tuple;

        //create the variable definition statements using the root block stmt created
        createVarRefAssignmentStmts(tupleDestructure.varRef, blockStmt, tuple.symbol, null);
        createRestFieldAssignmentStmt(tupleDestructure.varRef, blockStmt, tuple.symbol, tupleDestructure.expr);

        //finally rewrite the populated block statement
        result = rewrite(blockStmt, env);
    }

    private void createRestFieldAssignmentStmt(BLangTupleVarRef tupleVarRef, BLangBlockStmt blockStmt,
                                               BVarSymbol tupleVarSymbol, BLangExpression tupleExpr) {
        if (tupleVarRef.restParam == null) {
            return;
        }
        Location pos = blockStmt.pos;
        // T[] t = [];
        BLangSimpleVarRef restParam = (BLangSimpleVarRef) tupleVarRef.restParam;
        BType restParamType = restParam.getBType();
        BLangArrayLiteral arrayExpr = createArrayLiteralExprNode();
        arrayExpr.setBType(restParamType);

        BLangAssignment restParamAssignment = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        restParamAssignment.varRef = restParam;
        restParamAssignment.varRef.setBType(restParamType);
        restParamAssignment.expr = arrayExpr;

        BLangExpression countExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType,
                (long) tupleVarRef.expressions.size());
        boolean isIndexBasedAccessExpr = tupleExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR;
        if (restParamType.tag == TypeTags.TUPLE) {
            BTupleType restTupleType = (BTupleType) restParamType;
            BLangIndexBasedAccess modifiedTupleExpr = ASTBuilderUtil.createIndexAccessExpr(tupleExpr, countExpr);
            modifiedTupleExpr.setBType(tupleExpr.getBType());
            tupleExpr = modifiedTupleExpr;
            isIndexBasedAccessExpr = true;
            for (int index = 0; index < restTupleType.tupleTypes.size(); index++) {
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType,
                        (long) index);
                BLangIndexBasedAccess tupleValueExpr = ASTBuilderUtil.createIndexAccessExpr(restParam, indexExpr);
                tupleValueExpr.setBType(restTupleType.tupleTypes.get(index));
                createAssignmentStmt(tupleValueExpr, blockStmt, indexExpr, tupleVarSymbol,
                        (BLangIndexBasedAccess) tupleExpr);
                // Increment the count for each visited member in the tupleLiteral.
                countExpr = ASTBuilderUtil.createBinaryExpr(pos, indexExpr, createLiteral(pos, symTable.intType,
                        (long) 1), symTable.intType, OperatorKind.ADD, null);
            }

            if (restTupleType.restType == null) {
                return;
            }
        }
        // foreach var $foreach$i in count...tupleLiteral.length() {
        //     t[t.length()] = <T> tupleLiteral[$foreach$i];
        // }
        BLangInvocation lengthInvocation = createLengthInvocation(pos, tupleExpr);
        BLangInvocation intRangeInvocation = replaceWithIntRange(pos, countExpr,
                getModifiedIntRangeEndExpr(lengthInvocation));

        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = pos;
        foreach.collection = intRangeInvocation;
        types.setForeachTypedBindingPatternType(foreach);

        final BLangSimpleVariable foreachVariable = ASTBuilderUtil.createVariable(pos,
                "$foreach$i", foreach.varType);
        foreachVariable.symbol = new BVarSymbol(0, names.fromIdNode(foreachVariable.name),
                this.env.scope.owner.pkgID, foreachVariable.getBType(),
                this.env.scope.owner, pos, VIRTUAL);
        BLangSimpleVarRef foreachVarRef = ASTBuilderUtil.createVariableRef(pos, foreachVariable.symbol);
        foreach.variableDefinitionNode = ASTBuilderUtil.createVariableDef(pos, foreachVariable);
        foreach.isDeclaredWithVar = true;
        BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);

        // t[t.length()] = <T> tupleLiteral[$foreach$i];
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(restParam,
                createLengthInvocation(pos, restParam));
        if (restParamType.tag == TypeTags.TUPLE) {
            indexAccessExpr.setBType(((BTupleType) restParamType).restType);
        } else {
            indexAccessExpr.setBType(((BArrayType) restParamType).eType);
        }
        if (isIndexBasedAccessExpr) {
            createAssignmentStmt(indexAccessExpr, foreachBody, foreachVarRef, tupleVarSymbol,
                    (BLangIndexBasedAccess) tupleExpr);
        } else {
            createAssignmentStmt(indexAccessExpr, foreachBody, foreachVarRef, tupleVarSymbol, null);
        }
        foreach.body = foreachBody;
        blockStmt.addStatement(foreach);
    }

    private BLangInvocation createLengthInvocation(Location pos, BLangExpression collection) {
        BInvokableSymbol lengthInvokableSymbol = (BInvokableSymbol) symResolver
                .lookupLangLibMethod(collection.getBType(), names.fromString(LENGTH_FUNCTION_NAME), env);
        BLangInvocation lengthInvocation = ASTBuilderUtil.createInvocationExprForMethod(pos, lengthInvokableSymbol,
                Lists.of(collection), symResolver);
        lengthInvocation.argExprs = lengthInvocation.requiredArgs;
        lengthInvocation.setBType(lengthInvokableSymbol.type.getReturnType());
        return lengthInvocation;
    }

    /**
     * This method iterate through each member of the tupleVarRef and create the relevant var ref assignment statements.
     * This method does the check for node kind of each member and call the related var ref creation method.
     *
     * Example:
     * ((a, b), c)) = (tuple)
     *
     * (a, b) is again a tuple, so it is a recursive var ref creation.
     *
     * c is a simple var, so a simple var def will be created.
     *
     */
    private void createVarRefAssignmentStmts(BLangTupleVarRef parentTupleVariable, BLangBlockStmt parentBlockStmt,
                                             BVarSymbol tupleVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {

        final List<BLangExpression> expressions = parentTupleVariable.expressions;
        for (int index = 0; index < expressions.size(); index++) {
            BLangExpression expression = expressions.get(index);
            if (NodeKind.SIMPLE_VARIABLE_REF == expression.getKind() ||
                    NodeKind.FIELD_BASED_ACCESS_EXPR == expression.getKind() ||
                    NodeKind.INDEX_BASED_ACCESS_EXPR == expression.getKind() ||
                    NodeKind.XML_ATTRIBUTE_ACCESS_EXPR == expression.getKind()) {
                //if this is simple var, then create a simple var def stmt
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(expression.pos, symTable.intType, (long) index);
                createAssignmentStmt(expression, parentBlockStmt, indexExpr, tupleVarSymbol, parentIndexAccessExpr);
                continue;
            }

            if (expression.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
                //else recursively create the var def statements for tuple var ref
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) expression;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(tupleVarRef.pos, symTable.intType, (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVarRef.pos,
                        new BArrayType(symTable.anyType), tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts((BLangTupleVarRef) expression, parentBlockStmt, tupleVarSymbol,
                        arrayAccessExpr);
                createRestFieldAssignmentStmt((BLangTupleVarRef) expression, parentBlockStmt, tupleVarSymbol,
                        arrayAccessExpr);
                continue;
            }

            if (expression.getKind() == NodeKind.RECORD_VARIABLE_REF) {
                //else recursively create the var def statements for record var ref
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) expression;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(recordVarRef.pos, symTable.intType,
                                                                      (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentTupleVariable.pos, symTable.mapType, tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts((BLangRecordVarRef) expression, parentBlockStmt, tupleVarSymbol,
                                            arrayAccessExpr);

                BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(
                        (BRecordType) recordVarRef.getBType(), env.enclPkg.packageID, symTable, recordVarRef.pos);
                recordTypeNode.initFunction = TypeDefBuilderHelper
                        .createInitFunctionForRecordType(recordTypeNode, env, names, symTable);
                TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordVarRef.getBType(),
                        recordVarRef.getBType().tsymbol, recordTypeNode, env);

                continue;
            }

            if (expression.getKind() == NodeKind.ERROR_VARIABLE_REF) {
                // Else recursively create the var def statements for record var ref.
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) expression;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(errorVarRef.pos, symTable.intType,
                        (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentTupleVariable.pos, expression.getBType(), tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts((BLangErrorVarRef) expression, parentBlockStmt, tupleVarSymbol,
                        arrayAccessExpr);
            }
        }
    }

    /**
     * This method creates a assignment statement and assigns and array expression based on the given indexExpr.
     *
     */
    private void createAssignmentStmt(BLangExpression accessibleExpression, BLangBlockStmt parentBlockStmt,
                                      BLangExpression indexExpr, BVarSymbol tupleVarSymbol,
                                      BLangIndexBasedAccess parentArrayAccessExpr) {

        if (accessibleExpression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            Name varName = names.fromIdNode(((BLangSimpleVarRef) accessibleExpression).variableName);
            if (varName == Names.IGNORE) {
                return;
            }
        }

        BLangExpression assignmentExpr =
                createIndexBasedAccessExpr(accessibleExpression.getBType(), accessibleExpression.pos, indexExpr,
                                           tupleVarSymbol, parentArrayAccessExpr);

        assignmentExpr = addConversionExprIfRequired(assignmentExpr, accessibleExpression.getBType());

        final BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(parentBlockStmt.pos,
                parentBlockStmt);
        assignmentStmt.varRef = accessibleExpression;
        assignmentStmt.expr = assignmentExpr;
    }

    private BLangExpression createIndexBasedAccessExpr(BType varType,
                                                       Location varLocation,
                                                       BLangExpression indexExpr,
                                                       BVarSymbol tupleVarSymbol,
                                                       BLangIndexBasedAccess parentExpr) {

        BLangIndexBasedAccess arrayAccess = ASTBuilderUtil.createIndexBasesAccessExpr(varLocation,
                symTable.anyType, tupleVarSymbol, indexExpr);
        arrayAccess.originalType = varType;

        if (parentExpr != null) {
            arrayAccess.expr = parentExpr;
        }

        final BLangExpression assignmentExpr;
        if (types.isValueType(varType)) {
            BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
            castExpr.expr = arrayAccess;
            castExpr.setBType(varType);
            assignmentExpr = castExpr;
        } else {
            assignmentExpr = arrayAccess;
        }
        return assignmentExpr;
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {

        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(recordDestructure.pos);

        BType runTimeType = new BMapType(TypeTags.MAP, symTable.anyType, null);

        String name = "$map$_0";
        final BLangSimpleVariable mapVariable =
                ASTBuilderUtil.createVariable(recordDestructure.pos, name, runTimeType, null,
                                              new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID,
                                                             runTimeType, this.env.scope.owner, recordDestructure.pos,
                                                             VIRTUAL));
        mapVariable.expr = recordDestructure.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.
                createVariableDefStmt(recordDestructure.pos, blockStmt);
        variableDef.var = mapVariable;

        //create the variable definition statements using the root block stmt created
        createVarRefAssignmentStmts(recordDestructure.varRef, blockStmt, mapVariable.symbol, null);

        //finally rewrite the populated block statement
        result = rewrite(blockStmt, env);
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure) {
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(errorDestructure.pos);
        final BLangSimpleVariable errorVar =
                ASTBuilderUtil.createVariable(errorDestructure.pos, GENERATED_ERROR_VAR, symTable.errorType, null,
                        new BVarSymbol(0, names.fromString(GENERATED_ERROR_VAR), this.env.scope.owner.pkgID,
                                symTable.errorType, this.env.scope.owner,
                                errorDestructure.pos, VIRTUAL));
        errorVar.expr = errorDestructure.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(errorDestructure.pos,
                                                                                        blockStmt);
        variableDef.var = errorVar;
        createVarRefAssignmentStmts(errorDestructure.varRef, blockStmt, errorVar.symbol, null);
        result = rewrite(blockStmt, env);
    }

    private void createVarRefAssignmentStmts(BLangRecordVarRef parentRecordVarRef, BLangBlockStmt parentBlockStmt,
                                             BVarSymbol recordVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {
        final List<BLangRecordVarRefKeyValue> variableRefList = parentRecordVarRef.recordRefFields;
        for (BLangRecordVarRefKeyValue varRefKeyValue : variableRefList) {
            BLangExpression expression = varRefKeyValue.variableReference;
            BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(expression.pos, symTable.stringType,
                    varRefKeyValue.variableName.getValue());

            if (NodeKind.SIMPLE_VARIABLE_REF == expression.getKind() ||
                    NodeKind.FIELD_BASED_ACCESS_EXPR == expression.getKind() ||
                    NodeKind.INDEX_BASED_ACCESS_EXPR == expression.getKind() ||
                    NodeKind.XML_ATTRIBUTE_ACCESS_EXPR == expression.getKind()) {
                createAssignmentStmt(expression, parentBlockStmt, indexExpr, recordVarSymbol, parentIndexAccessExpr);
                continue;
            }

            if (NodeKind.RECORD_VARIABLE_REF == expression.getKind()) {
                BLangRecordVarRef recordVariable = (BLangRecordVarRef) expression;
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentRecordVarRef.pos, symTable.mapType, recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts(recordVariable, parentBlockStmt, recordVarSymbol, arrayAccessExpr);
                continue;
            }

            if (NodeKind.TUPLE_VARIABLE_REF == expression.getKind()) {
                BLangTupleVarRef tupleVariable = (BLangTupleVarRef) expression;
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVariable.pos,
                        symTable.tupleType, recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts(tupleVariable, parentBlockStmt, recordVarSymbol, arrayAccessExpr);
                continue;
            }

            if (NodeKind.ERROR_VARIABLE_REF == expression.getKind()) {
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(expression.pos,
                        symTable.errorType, recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts((BLangErrorVarRef) expression, parentBlockStmt, recordVarSymbol,
                        arrayAccessExpr);
            }
        }

        if (parentRecordVarRef.restParam != null) {
            // The restParam is desugared to a filter iterable operation that filters out the fields provided in the
            // record variable
            // map<any> restParam = $map$_0.filter($lambdaArg$_0);

            Location pos = parentBlockStmt.pos;
            BLangSimpleVarRef restParamRef = (BLangSimpleVarRef) parentRecordVarRef.restParam;

            BType restParamType = restParamRef.getBType();
            BLangSimpleVarRef variableReference;

            if (parentIndexAccessExpr != null) {
                BLangSimpleVariable mapVariable =
                        ASTBuilderUtil.createVariable(pos, "$map$_1", restParamType, null,
                                                      new BVarSymbol(0, names.fromString("$map$_1"),
                                                                     this.env.scope.owner.pkgID, restParamType,
                                                                     this.env.scope.owner, pos, VIRTUAL));
                mapVariable.expr = parentIndexAccessExpr;
                BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(pos, parentBlockStmt);
                variableDef.var = mapVariable;

                variableReference = ASTBuilderUtil.createVariableRef(pos, mapVariable.symbol);
            } else {
                variableReference = ASTBuilderUtil.createVariableRef(pos,
                        ((BLangSimpleVariableDef) parentBlockStmt.stmts.get(0)).var.symbol);
            }

            BLangSimpleVarRef restParam = (BLangSimpleVarRef) parentRecordVarRef.restParam;

            List<String> keysToRemove = parentRecordVarRef.recordRefFields.stream()
                    .map(field -> field.variableName.value)
                    .collect(Collectors.toList());

            BLangSimpleVariable filteredDetail = generateRestFilter(variableReference, pos,
                    keysToRemove, restParamType, parentBlockStmt);

            BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(pos, filteredDetail.symbol);

            // Create rest param variable definition
            BLangAssignment restParamAssignment = ASTBuilderUtil.createAssignmentStmt(pos, parentBlockStmt);
            restParamAssignment.varRef = restParam;
            restParamAssignment.varRef.setBType(restParamType);
            restParamAssignment.expr = varRef;
        }
    }

    private void createVarRefAssignmentStmts(BLangErrorVarRef parentErrorVarRef, BLangBlockStmt parentBlockStmt,
                                             BVarSymbol errorVarySymbol, BLangIndexBasedAccess parentIndexAccessExpr) {
        if (parentErrorVarRef.message != null &&
                names.fromIdNode(((BLangSimpleVarRef) parentErrorVarRef.message).variableName) != Names.IGNORE) {
            BLangAssignment message = ASTBuilderUtil.createAssignmentStmt(parentBlockStmt.pos, parentBlockStmt);
            message.expr = generateErrorMessageBuiltinFunction(parentErrorVarRef.message.pos,
                    symTable.stringType, errorVarySymbol, parentIndexAccessExpr);
            message.expr = addConversionExprIfRequired(message.expr, parentErrorVarRef.message.getBType());
            message.varRef = parentErrorVarRef.message;
        }

        if (parentErrorVarRef.cause != null && (parentErrorVarRef.cause.getKind() != NodeKind.SIMPLE_VARIABLE_REF ||
                names.fromIdNode(((BLangSimpleVarRef) parentErrorVarRef.cause).variableName) != Names.IGNORE)) {
            BLangAssignment cause = ASTBuilderUtil.createAssignmentStmt(parentBlockStmt.pos, parentBlockStmt);
            cause.expr = generateErrorCauseLanglibFunction(parentErrorVarRef.cause.pos,
                    symTable.errorType, errorVarySymbol, parentIndexAccessExpr);
            cause.expr = addConversionExprIfRequired(cause.expr, parentErrorVarRef.cause.getBType());
            cause.varRef = parentErrorVarRef.cause;
        }

        // When no detail nor rest detail are to be destructured, we don't need to generate the detail invocation.
        if (parentErrorVarRef.detail.isEmpty() && isIgnoredErrorRefRestVar(parentErrorVarRef)) {
            return;
        }

        BLangInvocation errorDetailBuiltinFunction = generateErrorDetailBuiltinFunction(parentErrorVarRef.pos,
                errorVarySymbol,
                parentIndexAccessExpr);

        BLangSimpleVariableDef detailTempVarDef = createVarDef("$error$detail$" + UNDERSCORE + errorCount++,
                                                               symTable.detailType, errorDetailBuiltinFunction,
                                                               parentErrorVarRef.pos);
        detailTempVarDef.setBType(symTable.detailType);
        parentBlockStmt.addStatement(detailTempVarDef);
        this.env.scope.define(names.fromIdNode(detailTempVarDef.var.name), detailTempVarDef.var.symbol);

        List<String> extractedKeys = new ArrayList<>();
        for (BLangNamedArgsExpression detail : parentErrorVarRef.detail) {
            extractedKeys.add(detail.name.value);
            BLangVariableReference ref = (BLangVariableReference) detail.expr;

            // create a index based access
            BLangExpression detailEntryVar =
                    createIndexBasedAccessExpr(ref.getBType(), ref.pos,
                                               createStringLiteral(detail.name.pos, detail.name.value),
                                               detailTempVarDef.var.symbol, null);
            if (detailEntryVar.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
                BLangIndexBasedAccess bLangIndexBasedAccess = (BLangIndexBasedAccess) detailEntryVar;
                bLangIndexBasedAccess.originalType = symTable.cloneableType;
            }

            BLangAssignment detailAssignment = ASTBuilderUtil.createAssignmentStmt(ref.pos, parentBlockStmt);
            detailAssignment.varRef = ref;
            detailAssignment.expr = detailEntryVar;
        }

        if (!isIgnoredErrorRefRestVar(parentErrorVarRef)) {
            BLangSimpleVarRef detailVarRef = ASTBuilderUtil.createVariableRef(parentErrorVarRef.restVar.pos,
                    detailTempVarDef.var.symbol);

            BLangSimpleVariable filteredDetail =
                    generateRestFilter(detailVarRef, parentErrorVarRef.restVar.pos, extractedKeys,
                                       parentErrorVarRef.restVar.getBType(), parentBlockStmt);
            BLangAssignment restAssignment = ASTBuilderUtil.createAssignmentStmt(parentErrorVarRef.restVar.pos,
                    parentBlockStmt);
            restAssignment.varRef = parentErrorVarRef.restVar;
            restAssignment.expr = ASTBuilderUtil.createVariableRef(parentErrorVarRef.restVar.pos,
                    filteredDetail.symbol);
        }

        BErrorType errorType = (BErrorType) Types.getReferredType(parentErrorVarRef.getBType());
        if (Types.getReferredType(errorType.detailType).getKind() == TypeKind.RECORD) {
            // Create empty record init attached func
            BRecordTypeSymbol tsymbol = (BRecordTypeSymbol) Types.getReferredType(errorType.detailType).tsymbol;
            tsymbol.initializerFunc = createRecordInitFunc();
            tsymbol.scope.define(tsymbol.initializerFunc.funcName, tsymbol.initializerFunc.symbol);
        }
    }

    private boolean isIgnoredErrorRefRestVar(BLangErrorVarRef parentErrorVarRef) {
        if (parentErrorVarRef.restVar == null) {
            return true;
        }
        if (parentErrorVarRef.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return (((BLangSimpleVarRef) parentErrorVarRef.restVar).variableName.value.equals(IGNORE.value));
        }
        return false;
    }

    @Override
    public void visit(BLangRetry retryNode) {
        if (retryNode.onFailClause != null) {
            // wrap user defined on fail within a do statement
            BLangOnFailClause onFailClause = retryNode.onFailClause;
            retryNode.onFailClause = null;
            retryNode.retryBody.failureBreakMode = BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
            BLangDo doStmt = wrapStatementWithinDo(retryNode.pos, retryNode, onFailClause);
            result = rewrite(doStmt, env);
        } else {
            Location pos = retryNode.retryBody.pos;
            BLangSimpleVarRef prevShouldRetryRef = this.shouldRetryRef;
            BLangBlockStmt retryBlockStmt = ASTBuilderUtil.createBlockStmt(retryNode.pos);
            retryBlockStmt.parent = env.enclInvokable;
            retryBlockStmt.scope = new Scope(env.scope.owner);

            if (retryNode.commonStmtForRetries != null) {
                BLangSimpleVariableDef prevAttemptDef = (BLangSimpleVariableDef) retryNode.commonStmtForRetries;
                retryBlockStmt.scope.define(prevAttemptDef.var.symbol.name, prevAttemptDef.var.symbol);
                retryBlockStmt.stmts.add(retryNode.commonStmtForRetries);
            }

            // <RetryManagerType> $retryManager$ = new();
            BLangSimpleVariableDef retryManagerVarDef = createRetryManagerDef(retryNode.retrySpec, retryNode.pos);
            retryBlockStmt.stmts.add(retryManagerVarDef);
            BLangSimpleVarRef retryManagerVarRef = ASTBuilderUtil.createVariableRef(pos, retryManagerVarDef.var.symbol);
            BVarSymbol retryMangerRefVarSymbol = new BVarSymbol(0, names.fromString("$retryManagerRef$"),
                    env.scope.owner.pkgID, retryManagerVarDef.var.symbol.type, this.env.scope.owner, pos,
                    VIRTUAL);
            retryMangerRefVarSymbol.closure = true;
            BLangSimpleVariable retryMangerRefVar = ASTBuilderUtil.createVariable(pos, "$retryManagerRef$",
                    retryManagerVarDef.var.symbol.type, retryManagerVarRef, retryMangerRefVarSymbol);
            retryBlockStmt.scope.define(retryMangerRefVarSymbol.name, retryMangerRefVarSymbol);
            BLangSimpleVariableDef retryMangerRefDef = ASTBuilderUtil.createVariableDef(pos, retryMangerRefVar);
            BLangSimpleVarRef retryManagerRef = ASTBuilderUtil.createVariableRef(pos, retryMangerRefVarSymbol);
            retryBlockStmt.stmts.add(retryMangerRefDef);

            // error? $retryResult$ = ();
            BLangLiteral nillLiteral =  ASTBuilderUtil.createLiteral(pos, symTable.nilType, null);
            BVarSymbol retryResultVarSymbol = new BVarSymbol(0, names.fromString("$retryResult$"),
                    env.scope.owner.pkgID, symTable.errorOrNilType, this.env.scope.owner, pos, VIRTUAL);
            retryResultVarSymbol.closure = true;
            BLangSimpleVariable retryResultVariable = ASTBuilderUtil.createVariable(pos, "$retryResult$",
                    symTable.errorOrNilType, nillLiteral, retryResultVarSymbol);
            retryBlockStmt.scope.define(retryResultVarSymbol.name, retryResultVarSymbol);
            BLangSimpleVariableDef retryResultDef = ASTBuilderUtil.createVariableDef(pos, retryResultVariable);
            BLangSimpleVarRef retryResultRef = ASTBuilderUtil.createVariableRef(pos, retryResultVarSymbol);
            retryBlockStmt.stmts.add(retryResultDef);

            // boolean $shouldRetry$ = false;
            BLangLiteral falseLiteral =  ASTBuilderUtil.createLiteral(pos, symTable.booleanType, false);
            BVarSymbol shouldRetryVarSymbol = new BVarSymbol(0, names.fromString("$shouldRetry$"),
                    env.scope.owner.pkgID, symTable.booleanType, this.env.scope.owner, pos, VIRTUAL);
            shouldRetryVarSymbol.closure = true;
            BLangSimpleVariable shouldRetryVariable = ASTBuilderUtil.createVariable(pos, "$shouldRetry$",
                    symTable.booleanType, falseLiteral, shouldRetryVarSymbol);
            retryBlockStmt.scope.define(shouldRetryVarSymbol.name, shouldRetryVarSymbol);
            BLangSimpleVariableDef shouldRetryDef = ASTBuilderUtil.createVariableDef(pos, shouldRetryVariable);
            this.shouldRetryRef = ASTBuilderUtil.createVariableRef(pos, shouldRetryVarSymbol);
            retryBlockStmt.stmts.add(shouldRetryDef);

            //while ((retryRes == ()) || (retryRes is error && shouldRetryRes)) {
            // }
            BLangWhile whileLoop = createRetryWhileLoop(pos, retryNode.retryBody, retryManagerRef,
                    retryResultRef, shouldRetryRef);
            retryBlockStmt.stmts.add(whileLoop);

            if (!enclosingShouldContinue.isEmpty() && enclosingShouldContinue.size() > 1) {
                BLangSimpleVarRef nestedLoopShouldContinue =
                        enclosingShouldContinue.get(enclosingShouldContinue.size() - 2);
                BLangBlockStmt shouldContinueBlock = createBlockStmt(pos);
                BLangContinue loopContinueStmt = (BLangContinue) TreeBuilder.createContinueNode();
                loopContinueStmt.pos = pos;
                shouldContinueBlock.stmts.add(loopContinueStmt);

                BLangIf shouldContinue = ASTBuilderUtil.createIfElseStmt(pos, nestedLoopShouldContinue,
                        shouldContinueBlock, null);
                retryBlockStmt.stmts.add(shouldContinue);
            }

            //at this point:
            // RetryManagerType> $retryManager$ = new();
            // error? $retryResult$ = ();
            // boolean $shouldRetry$ = false;
            // while($retryResult$ == () || ($retryResult$ is error && $shouldRetry$)) {
            //      boolean $returnErrorResult$ = false;
            //      boolean $continueLoop$ = false;
            //      $shouldRetry$ = false;
            //
            //      do {
            //          <"Content in retry block goes here">
            //      } on fail var $caughtError$ {
            //           $retryResult$ = $caughtError$;
            //           $shouldRetry$ = $retryManager$.shouldRetry();
            //           if (!$shouldRetry$) {
            //                  fail $retryResult$;
            //           }
            //           $continueLoop$ = true;
            //           continue;
            //      }
            //      ### when no enclosing on fail clause to jump to ###
            //      } on fail var $caughtError$ {
            //           $retryResult$ = $caughtError$;
            //           $shouldRetry$ = $retryManager$.shouldRetry();
            //           if (!$shouldRetry$) {
            //                 $returnErrorResult$ = true;
            //           }
            //           $continueLoop$ = true;
            //      }
            //
            //      if($returnErrorResult$) {
            //           return $retryResult$;
            //      }
            //
            //      if($continueLoop$) {
            //          continue;
            //      } else {
            //          break;
            //      }
            // }
            result = rewrite(retryBlockStmt, env);
            enclosingShouldContinue.remove(enclosingShouldContinue.size() - 1);
            this.shouldRetryRef = prevShouldRetryRef;
        }
    }

    protected BLangWhile createRetryWhileLoop(Location pos,
                                              BLangBlockStmt retryBody,
                                              BLangSimpleVarRef retryManagerRef,
                                              BLangSimpleVarRef retryResultRef,
                                              BLangSimpleVarRef shouldRetryRef) {
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.pos = pos;
        BLangBlockStmt whileBody = createBlockStmt(pos);
        whileBody.scope = new Scope(env.scope.owner);

        BLangLiteral falseLiteral =  createLiteral(pos, symTable.booleanType, false);

        // boolean $returnErrorResult$ = false;
        BVarSymbol returnResultSymbol = new BVarSymbol(0, names.fromString("$returnErrorResult$"),
                env.scope.owner.pkgID, symTable.booleanType, this.env.scope.owner, pos, VIRTUAL);
        returnResultSymbol.closure = true;
        BLangSimpleVariable returnResultVariable = createVariable(pos, "$returnErrorResult$",
                symTable.booleanType, falseLiteral, returnResultSymbol);
        whileBody.scope.define(returnResultSymbol.name, returnResultSymbol);
        BLangSimpleVariableDef returnResultDef = ASTBuilderUtil.createVariableDef(pos, returnResultVariable);
        BLangSimpleVarRef returnResultRef = createVariableRef(pos, returnResultSymbol);
        whileBody.stmts.add(returnResultDef);

        // boolean continueLoop = false;
        BVarSymbol continueLoopVarSymbol = new BVarSymbol(0, names.fromString("$continueLoop$"),
                env.scope.owner.pkgID, symTable.booleanType, this.env.scope.owner, pos, VIRTUAL);
        continueLoopVarSymbol.closure = true;
        BLangSimpleVariable continueLoopVariable = createVariable(pos, "$continueLoop$",
                symTable.booleanType, falseLiteral, continueLoopVarSymbol);
        whileBody.scope.define(continueLoopVarSymbol.name, continueLoopVarSymbol);
        BLangSimpleVariableDef continueLoopDef = ASTBuilderUtil.createVariableDef(pos, continueLoopVariable);
        BLangSimpleVarRef continueLoopRef = createVariableRef(pos, continueLoopVarSymbol);
        whileBody.stmts.add(continueLoopDef);

        // on fail error $caughtError$ {
        //    $retryResult$ = $caughtError$;
        //    $shouldRetry$ = $retryManager$.shouldRetry();
        //    if (!$shouldRetry$) {
        //           fail $retryResult$;
        //    }
        //    $continueLoop$ = true;
        //    continue;
        // }
        BLangOnFailClause internalOnFail = createRetryInternalOnFail(pos, retryResultRef,
                retryManagerRef, shouldRetryRef, continueLoopRef, returnResultRef);
        enclosingShouldContinue.add(continueLoopRef);

        BLangDo retryDo = wrapStatementWithinDo(pos, retryBody, internalOnFail);

        BLangTypeTestExpr isErrorCheck = createTypeCheckExpr(pos, retryResultRef,
                getErrorTypeNode());
        BLangBinaryExpr shouldRetryCheck = ASTBuilderUtil.createBinaryExpr(pos, isErrorCheck, shouldRetryRef,
                symTable.booleanType, OperatorKind.AND, null);
        BLangGroupExpr rhsCheck =  new BLangGroupExpr();
        rhsCheck.setBType(symTable.booleanType);
        rhsCheck.expression = shouldRetryCheck;

        BLangLiteral nillLiteral = createLiteral(pos, symTable.nilType, null);
        BLangBinaryExpr equalToNullCheck = ASTBuilderUtil.createBinaryExpr(pos, retryResultRef, nillLiteral,
                symTable.booleanType, OperatorKind.EQUAL, null);
        BLangGroupExpr lhsCheck =  new BLangGroupExpr();
        lhsCheck.setBType(symTable.booleanType);
        lhsCheck.expression = equalToNullCheck;

        // while($retryResult$ == () ||($retryResult$ is error && $shouldRetry$))
        whileNode.expr = ASTBuilderUtil.createBinaryExpr(pos, lhsCheck, rhsCheck,
                symTable.booleanType, OperatorKind.OR, null);

        //$shouldRetry$ = false;
        BLangAssignment shouldRetryFalse = ASTBuilderUtil.createAssignmentStmt(pos, shouldRetryRef,
                createLiteral(pos, symTable.booleanType, false));
        whileBody.stmts.add(shouldRetryFalse);

        whileBody.stmts.add(retryDo);

        BLangBlockStmt returnBlock = createBlockStmt(pos);
        BLangReturn errorReturn = ASTBuilderUtil.createReturnStmt(pos, rewrite(retryResultRef, env));
        errorReturn.desugared = true;
        returnBlock.stmts.add(errorReturn);

        // if($returnErrorResult$) {
        //      return $retryResult$;
        // }
        BLangIf exitIf = ASTBuilderUtil.createIfElseStmt(pos, returnResultRef, returnBlock, null);
        whileBody.stmts.add(exitIf);

        //if(shouldContinue) {
        //     continue;
        // } else {
        //     break;
        // }
        BLangBlockStmt shouldContinueBlock = createBlockStmt(pos);
        BLangContinue loopContinueStmt = (BLangContinue) TreeBuilder.createContinueNode();
        loopContinueStmt.pos = pos;
        shouldContinueBlock.stmts.add(loopContinueStmt);

        BLangBlockStmt elseBlock = createBlockStmt(pos);
        BLangBreak breakStmt = (BLangBreak) TreeBuilder.createBreakNode();
        breakStmt.pos = pos;
        elseBlock.stmts.add(breakStmt);

        BLangIf shouldContinue = ASTBuilderUtil.createIfElseStmt(pos, continueLoopRef,
                shouldContinueBlock, elseBlock);
        whileBody.stmts.add(shouldContinue);
        whileNode.body = whileBody;

        //at this point:
        // while($retryResult$ == () || ($retryResult$ is error && $shouldRetry$)) {
        //      $shouldRetry$ = false;
        //      $returnErrorResult$ = false;
        //      $continueLoop$ = false;
        //
        //      do {
        //          <"Content in retry block goes here">
        //      } on fail var $caughtError$ {
        //           $retryResult$ = $caughtError$;
        //           $shouldRetry$ = $retryManager$.shouldRetry();
        //           if (!$shouldRetry$) {
        //                  fail $retryResult$;
        //           }
        //           $continueLoop$ = true;
        //           continue;
        //      }
        //      ### when no enclosing on fail clause to jump to ###
        //      } on fail var $caughtError$ {
        //           $retryResult$ = $caughtError$;
        //           $shouldRetry$ = $retryManager$.shouldRetry();
        //           if (!$shouldRetry$) {
        //                 $returnErrorResult$ = true;
        //           }
        //           $continueLoop$ = true;
        //      }
        //
        //
        //      if($returnErrorResult$) {
        //           return $retryResult$;
        //      }
        //
        //      if($continueLoop$) {
        //          continue;
        //      } else {
        //          break;
        //      }
        // }
        return whileNode;
    }

    protected BLangSimpleVariableDef createRetryManagerDef(BLangRetrySpec retrySpec, Location pos) {
        BSymbol retryManagerTypeSymbol = symTable.langErrorModuleSymbol.scope
                .lookup(names.fromString("DefaultRetryManager")).symbol;
        BType retryManagerType = retryManagerTypeSymbol.type;
        if (retrySpec.retryManagerType != null) {
            retryManagerType = retrySpec.retryManagerType.getBType();
        }

        //<RetryManagerType> $retryManager$ = new;
        BVarSymbol retryMangerSymbol = new BVarSymbol(0, names.fromString("$retryManager$"),
                                                      env.scope.owner.pkgID, retryManagerType, this.env.scope.owner,
                                                      pos, VIRTUAL);
        BLangTypeInit managerInit = ASTBuilderUtil.createEmptyTypeInit(pos, retryManagerType);
        ((BLangInvocation) managerInit.initInvocation).requiredArgs = retrySpec.argExprs;
        BLangSimpleVariable retryManagerVariable = ASTBuilderUtil.createVariable(pos, "$retryManager$",
                retryManagerType, managerInit, retryMangerSymbol);
        return ASTBuilderUtil.createVariableDef(pos, retryManagerVariable);
    }

    BLangInvocation createRetryManagerShouldRetryInvocation(Location location,
                                                            BLangSimpleVarRef managerVarRef,
                                                            BLangExpression trapResultRef) {
        BInvokableSymbol shouldRetryFuncSymbol = getShouldRetryFunc((BVarSymbol) managerVarRef.symbol).symbol;
        BLangInvocation shouldRetryInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        shouldRetryInvocation.pos = location;
        shouldRetryInvocation.expr = managerVarRef;
        shouldRetryInvocation.requiredArgs = Lists.of(trapResultRef);
        shouldRetryInvocation.argExprs = shouldRetryInvocation.requiredArgs;
        shouldRetryInvocation.symbol = shouldRetryFuncSymbol;
        shouldRetryInvocation.setBType(shouldRetryFuncSymbol.retType);
        shouldRetryInvocation.langLibInvocation = false;
        return shouldRetryInvocation;
    }

    private BAttachedFunction getShouldRetryFunc(BVarSymbol retryManagerSymbol) {
        BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) retryManagerSymbol.type.tsymbol;
        for (BAttachedFunction bAttachedFunction : typeSymbol.attachedFuncs) {
            if (bAttachedFunction.funcName.value.equals(RETRY_MANAGER_OBJECT_SHOULD_RETRY_FUNC)) {
                return bAttachedFunction;
            }
        }
        return null;
    }

    protected BLangTypeTestExpr createTypeCheckExpr(Location pos, BLangExpression expr, BLangType type) {
        BLangTypeTestExpr testExpr = ASTBuilderUtil.createTypeTestExpr(pos, expr, type);
        testExpr.setBType(symTable.booleanType);
        return testExpr;
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        //transactions:Info? prevAttempt = ();
        BLangSimpleVariableDef prevAttemptVarDef = transactionDesugar.createPrevAttemptInfoVarDef(env,
                retryTransaction.pos);
        retryTransaction.transaction.prevAttemptInfo = ASTBuilderUtil.createVariableRef(retryTransaction.pos,
                prevAttemptVarDef.var.symbol);

        // the positions are removed to prevent unnecessary observability calls from desugared code.
        retryTransaction.pos = null;
        retryTransaction.retrySpec.pos = null;
        retryTransaction.transaction.pos = null;
        retryTransaction.transaction.transactionBody.pos = null;

        BLangBlockStmt retryBody = ASTBuilderUtil.createBlockStmt(retryTransaction.pos);
        retryBody.stmts.add(retryTransaction.transaction);

        BLangRetry retry = (BLangRetry) TreeBuilder.createRetryNode();
        retry.commonStmtForRetries = prevAttemptVarDef;
        retry.retryBody = retryBody;
        retry.retrySpec = retryTransaction.retrySpec;
        result = rewrite(retry, env);
    }

    protected BLangNode createExpressionStatement(Location location,
                                                  BLangStatementExpression retryTransactionStmtExpr,
                                                  boolean retryReturns,
                                                  SymbolEnv env) {

        if (retryReturns) {
            BLangReturn bLangReturn = ASTBuilderUtil.createReturnStmt(location, rewrite(retryTransactionStmtExpr, env));
            return rewrite(bLangReturn, env);
        } else {
            BLangExpressionStmt transactionExprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
            transactionExprStmt.pos = location;
            transactionExprStmt.expr = retryTransactionStmtExpr;
            transactionExprStmt.setBType(symTable.nilType);
            return rewrite(transactionExprStmt, env);
        }
    }

    protected void createErrorReturn(Location pos, BlockNode blockStmt, BLangSimpleVarRef resultRef) {
        BLangIf returnError = ASTBuilderUtil.createIfStmt(pos, blockStmt);
        returnError.expr = createTypeCheckExpr(pos, resultRef, getErrorTypeNode());
        returnError.body = ASTBuilderUtil.createBlockStmt(pos);
        BLangFail failExpressionNode = (BLangFail) TreeBuilder.createFailNode();
        failExpressionNode.expr = addConversionExprIfRequired(resultRef, symTable.errorType);
        returnError.body.stmts.add(failExpressionNode);
    }

    @Override
    public void visit(BLangContinue nextNode) {
        result = nextNode;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        result = breakNode;
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // If the return node do not have an expression, we add `done` statement instead of a return statement. This is
        // to distinguish between returning nil value specifically and not returning any value.
        if (returnNode.expr != null) {
            returnNode.expr = rewriteExpr(returnNode.expr);
        }
        result = returnNode;
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr = rewriteExpr(panicNode.expr);
        result = panicNode;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl = rewrite(xmlnsStmtNode.xmlnsDecl, env);
        // need to propagate the local namespace to be used at the query clauses
        this.stmtsToBePropagatedToQuery.put(xmlnsStmtNode.xmlnsDecl.symbol.name, xmlnsStmtNode);
        result = xmlnsStmtNode;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        BLangXMLNS generatedXMLNSNode;
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        BSymbol ownerSymbol = xmlnsNode.symbol.owner;

        // Local namespace declaration in a function/resource/action/worker
        if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            generatedXMLNSNode = new BLangLocalXMLNS();
        } else {
            generatedXMLNSNode = new BLangPackageXMLNS();
        }

        generatedXMLNSNode.namespaceURI = xmlnsNode.namespaceURI;
        generatedXMLNSNode.prefix = xmlnsNode.prefix;
        generatedXMLNSNode.symbol = xmlnsNode.symbol;
        result = generatedXMLNSNode;
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {

        BLangValueExpression varRef = compoundAssignment.varRef;
        if (compoundAssignment.varRef.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR) {
            result = ASTBuilderUtil.createAssignmentStmt(compoundAssignment.pos, rewriteExpr(varRef),
                    rewriteExpr(compoundAssignment.modifiedExpr));
            return;
        }
        // If compound Assignment is an index based expression such as a[f(1, foo)][3][2] += y,
        // should return a block statement which is equivalent to
        // var $temp3$ = a[f(1, foo)];
        // var $temp2$ = 3;
        // var $temp1$ = 2;
        // a[$temp3$][$temp2$][$temp1$] = a[$temp3$][$temp2$][$temp1$] + y;
        List<BLangStatement> statements = new ArrayList<>();
        List<BLangSimpleVarRef> varRefs = new ArrayList<>();
        List<BType> types = new ArrayList<>();

        // Extract the index Expressions from compound assignment and create variable definitions. ex:
        // var $temp3$ = a[f(1, foo)];
        // var $temp2$ = 3;
        // var $temp1$ = 2;
        do {
            BLangSimpleVariableDef tempIndexVarDef =
                    createVarDef("$temp" + ++indexExprCount + "$",
                                 ((BLangIndexBasedAccess) varRef).indexExpr.getBType(),
                                 ((BLangIndexBasedAccess) varRef).indexExpr, compoundAssignment.pos);
            BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(tempIndexVarDef.pos,
                    tempIndexVarDef.var.symbol);
            statements.add(0, tempIndexVarDef);
            varRefs.add(0, tempVarRef);
            types.add(0, varRef.getBType());

            varRef = (BLangValueExpression) ((BLangIndexBasedAccess) varRef).expr;
        } while (varRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR);

        // Create the index access expression. ex: c[$temp3$][$temp2$][$temp1$]
        BLangExpression var = varRef;
        for (int ref = 0; ref < varRefs.size(); ref++) {
            BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(var, varRefs.get(ref));
            indexAccessExpr.originalType = types.get(ref);
            var = indexAccessExpr;
            var.setBType(indexAccessExpr.originalType);
        }
        var.setBType(compoundAssignment.varRef.getBType());

        // Create the right hand side binary expression of the assignment. ex: c[$temp3$][$temp2$][$temp1$] + y
        BLangExpression rhsExpression = ASTBuilderUtil.createBinaryExpr(compoundAssignment.pos, var,
                                                                        compoundAssignment.expr,
                                                                        compoundAssignment.getBType(),
                                                                        compoundAssignment.opKind, null);
        rhsExpression.setBType(compoundAssignment.modifiedExpr.getBType());

        // Create assignment statement. ex: a[$temp3$][$temp2$][$temp1$] = a[$temp3$][$temp2$][$temp1$] + y;
        BLangAssignment assignStmt = ASTBuilderUtil.createAssignmentStmt(compoundAssignment.pos, var,
                rhsExpression);

        statements.add(assignStmt);
        // Create block statement. ex: var $temp3$ = a[f(1, foo)];var $temp2$ = 3;var $temp1$ = 2;
        // a[$temp3$][$temp2$][$temp1$] = a[$temp3$][$temp2$][$temp1$] + y;
        BLangBlockStmt bLangBlockStmt = ASTBuilderUtil.createBlockStmt(compoundAssignment.pos, statements);
        result = rewrite(bLangBlockStmt, env);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewriteExpr(exprStmtNode.expr);
        result = exprStmtNode;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewriteExpr(ifNode.expr);
        ifNode.body = rewrite(ifNode.body, env);
        ifNode.elseStmt = rewrite(ifNode.elseStmt, env);

        result = ifNode;
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        BLangOnFailClause currentOnFailClause = this.onFailClause;
        BLangBlockStmt matchBlockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
        matchBlockStmt.pos = matchStatement.pos;
        matchBlockStmt.failureBreakMode = matchStatement.onFailClause != null ?
                BLangBlockStmt.FailureBreakMode.BREAK_TO_OUTER_BLOCK : BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
        if (matchStatement.onFailClause != null) {
            rewrite(matchStatement.onFailClause, env);
        }

        String matchExprVarName = GEN_VAR_PREFIX.value + "t_match_var";

        BLangExpression matchExpr = matchStatement.expr;
        BLangSimpleVariable matchExprVar =
                ASTBuilderUtil.createVariable(matchExpr.pos, matchExprVarName, matchExpr.getBType(), matchExpr,
                                              new BVarSymbol(0, names.fromString(matchExprVarName),
                                                             this.env.scope.owner.pkgID, matchExpr.getBType(),
                                                             this.env.scope.owner, matchExpr.pos, VIRTUAL));

        BLangSimpleVariableDef matchExprVarDef = ASTBuilderUtil.createVariableDef(matchBlockStmt.pos, matchExprVar);
        matchBlockStmt.stmts.add(matchExprVarDef);
        matchBlockStmt.stmts.add(convertMatchClausesToIfElseStmt(matchStatement.matchClauses, matchExprVar));
        rewrite(matchBlockStmt, this.env);

        result = matchBlockStmt;
        this.onFailClause = currentOnFailClause;
    }

    private BLangStatement convertMatchClausesToIfElseStmt(List<BLangMatchClause> matchClauses,
                                                           BLangSimpleVariable matchExprVar) {
        BLangDo outerScopeBlock = rewriteMatchClauseScope(matchClauses.get(0).pos, matchClauses.get(0));
        BLangDo innerScopeBlock = outerScopeBlock;
        BLangIf parentIfNode = convertMatchClauseToIfStmt(matchClauses.get(0), matchExprVar);
        BLangIf currentIfNode = parentIfNode;
        for (int i = 1; i < matchClauses.size(); i++) {
            BLangDo currentScopeBlock = rewriteMatchClauseScope(matchClauses.get(i).pos, matchClauses.get(i));
            innerScopeBlock.body.stmts.add(0, currentScopeBlock);
            innerScopeBlock = currentScopeBlock;
            currentIfNode.elseStmt = convertMatchClauseToIfStmt(matchClauses.get(i), matchExprVar);
            currentIfNode = (BLangIf) currentIfNode.elseStmt;
        }
        innerScopeBlock.body.stmts.add(parentIfNode);
        return outerScopeBlock;
    }

    private BLangDo rewriteMatchClauseScope(Location pos, BLangMatchClause matchClause) {
        BLangDo doStmt = (BLangDo) TreeBuilder.createDoNode();
        BLangBlockStmt scopeBlock = ASTBuilderUtil.createBlockStmt(pos);
        scopeBlock.scope = new Scope(matchClause.blockStmt.scope.owner);
        scopeBlock.scope.entries = matchClause.blockStmt.scope.entries;
        matchClause.blockStmt.scope = new Scope(scopeBlock.scope.owner);
        doStmt.body = scopeBlock;
        return doStmt;
    }

    private BLangIf convertMatchClauseToIfStmt(BLangMatchClause matchClause, BLangSimpleVariable matchExprVar) {
        BLangExpression ifCondition = createConditionFromMatchPatterns(matchClause, matchExprVar, matchClause.pos);
        if (matchClause.matchGuard != null) {
            ifCondition = ASTBuilderUtil.createBinaryExpr(matchClause.pos, ifCondition, matchClause.matchGuard.expr,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        }
        return ASTBuilderUtil.createIfElseStmt(matchClause.pos, ifCondition, matchClause.blockStmt, null);
    }

    private BLangExpression createConditionFromMatchPatterns(BLangMatchClause matchClause,
                                                             BLangSimpleVariable matchExprVar, Location pos) {
        BLangSimpleVariableDef resultVarDef = createVarDef("$result$", symTable.booleanType, null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlock = ASTBuilderUtil.createBlockStmt(pos);
        mainBlock.addStatement(resultVarDef);
        defineVars(mainBlock, new ArrayList<>(matchClause.declaredVars.values()));
        // $result$ = true
        BLangBlockStmt successBody = createSuccessOrFailureBody(true, resultVarRef, pos);

        List<BLangMatchPattern> matchPatterns = matchClause.matchPatterns;
        BLangIf parentIfElse = createIfElseStmtFromMatchPattern(matchPatterns.get(0), matchExprVar, successBody, pos);
        BLangIf currentIfElse = parentIfElse;

        for (int i = 1; i < matchPatterns.size(); i++) {
            successBody = createSuccessOrFailureBody(true, resultVarRef, pos);
            currentIfElse.elseStmt = createIfElseStmtFromMatchPattern(matchPatterns.get(i), matchExprVar, successBody,
                    matchPatterns.get(i).pos);
            currentIfElse = (BLangIf) currentIfElse.elseStmt;
        }

        currentIfElse.elseStmt = createSuccessOrFailureBody(false, resultVarRef, pos);
        mainBlock.addStatement(parentIfElse);
        BLangStatementExpression stmtExpr = createStatementExpression(mainBlock, resultVarRef);

        return rewriteExpr(stmtExpr);
    }

    private void defineVars(BLangBlockStmt blockStmt, List<BVarSymbol> vars) {
        for (BVarSymbol var : vars) {
            BLangSimpleVariable simpleVariable = ASTBuilderUtil.createVariable(var.pos, var.name.value, var.type,
                    null, var);
            BLangSimpleVariableDef simpleVariableDef = ASTBuilderUtil.createVariableDef(var.pos, simpleVariable);
            BLangSimpleVarRef simpleVarRef = ASTBuilderUtil.createVariableRef(var.pos, var);
            declaredVarDef.put(var.name.value, simpleVarRef);
            blockStmt.addStatement(simpleVariableDef);
        }
    }

    private BLangBlockStmt createSuccessOrFailureBody(boolean status, BLangSimpleVarRef varRef, Location pos) {
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos);
        BLangAssignment bLangAssignment =
                ASTBuilderUtil.createAssignmentStmt(pos, varRef, getBooleanLiteral(status));
        blockStmt.addStatement(bLangAssignment);
        return blockStmt;
    }

    private BLangIf createIfElseStmtFromMatchPattern(BLangMatchPattern matchPattern,
                                                     BLangSimpleVariable matchExprVar,
                                                     BLangBlockStmt successBody,
                                                     Location pos) {
        BLangSimpleVarRef matchExprVarRef = ASTBuilderUtil.createVariableRef(matchExprVar.pos, matchExprVar.symbol);
        BLangExpression condition = createConditionForMatchPattern(matchPattern, matchExprVarRef);
        successBody.getStatements().addAll(matchStmtsForPattern);
        matchStmtsForPattern.clear();
        return ASTBuilderUtil.createIfElseStmt(pos, condition, successBody, null);
    }

    private BLangExpression createConditionForMatchPattern(BLangMatchPattern matchPattern,
                                                           BLangSimpleVarRef matchExprVarRef) {
        NodeKind patternKind = matchPattern.getKind();
        switch (patternKind) {
            case WILDCARD_MATCH_PATTERN:
                return createConditionForWildCardMatchPattern((BLangWildCardMatchPattern) matchPattern,
                        matchExprVarRef);
            case CONST_MATCH_PATTERN:
                return createConditionForConstMatchPattern((BLangConstPattern) matchPattern, matchExprVarRef);
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                return createConditionForVarBindingPatternMatchPattern(
                        (BLangVarBindingPatternMatchPattern) matchPattern, matchExprVarRef);
            case LIST_MATCH_PATTERN:
                return createConditionForListMatchPattern((BLangListMatchPattern) matchPattern, matchExprVarRef);
            case MAPPING_MATCH_PATTERN:
                return createConditionForMappingMatchPattern((BLangMappingMatchPattern) matchPattern, matchExprVarRef);
            case ERROR_MATCH_PATTERN:
                return createConditionForErrorMatchPattern((BLangErrorMatchPattern) matchPattern, matchExprVarRef);
            default:
                // If some patterns are not implemented, those should be detected before this phase
                // TODO : Remove this after all patterns are implemented
                return null;
        }
    }

    private BLangExpression createConditionForWildCardMatchPattern(BLangWildCardMatchPattern wildCardMatchPattern,
                                                                   BLangSimpleVarRef matchExprVarRef) {
        BLangExpression lhsCheck = createLiteral(wildCardMatchPattern.pos, symTable.booleanType,
                types.isAssignable(matchExprVarRef.getBType(), wildCardMatchPattern.getBType()));

        BLangValueType anyType = (BLangValueType) TreeBuilder.createValueTypeNode();
        anyType.setBType(symTable.anyType);
        anyType.typeKind = TypeKind.ANY;
        BLangExpression rhsCheck = createTypeCheckExpr(wildCardMatchPattern.pos, matchExprVarRef, anyType);

        return ASTBuilderUtil.createBinaryExpr(wildCardMatchPattern.pos, lhsCheck, rhsCheck,
                symTable.booleanType, OperatorKind.OR, null);
    }

    private BLangExpression createConditionForConstMatchPattern(BLangConstPattern constPattern,
                                                                BLangSimpleVarRef matchExprVarRef) {
        return createBinaryExpression(constPattern.pos, matchExprVarRef, constPattern.expr);
    }

    private BLangExpression createConditionForWildCardBindingPattern(BLangWildCardBindingPattern wildCardBindingPattern,
                                                                     BLangSimpleVarRef matchExprVarRef) {
        return ASTBuilderUtil.createLiteral(wildCardBindingPattern.pos, symTable.booleanType,
                types.isAssignable(matchExprVarRef.getBType(), wildCardBindingPattern.getBType()));
    }

    private BLangExpression createConditionForCaptureBindingPattern(BLangCaptureBindingPattern captureBindingPattern,
                                                                    BLangSimpleVarRef matchExprVarRef) {
        Location pos = captureBindingPattern.pos;
        BLangSimpleVarRef captureBindingPatternVarRef =
                declaredVarDef.get(captureBindingPattern.getIdentifier().getValue());
        matchStmtsForPattern.add(ASTBuilderUtil.createAssignmentStmt(pos,
                captureBindingPatternVarRef, matchExprVarRef));
        return ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
    }

    private BLangExpression createConditionForListBindingPattern(BLangListBindingPattern listBindingPattern,
                                                                 BLangSimpleVarRef matchExprVarRef) {
        Location pos = listBindingPattern.pos;
        BType bindingPatternType = listBindingPattern.getBType();

        BLangSimpleVariableDef resultVarDef = createVarDef("$listBindingPatternResult$", symTable.booleanType, null,
                pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (bindingPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression typeCheckCondition = createIsLikeExpression(listBindingPattern.pos, matchExprVarRef,
                bindingPatternType);

        BLangExpression typeConvertedExpr = addConversionExprIfRequired(matchExprVarRef, bindingPatternType);
        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", bindingPatternType,
                typeConvertedExpr, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos,
                tempCastVarDef.var.symbol);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifBlock.addStatement(tempCastVarDef);
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos, typeCheckCondition, ifBlock, null);
        mainBlockStmt.addStatement(ifStmt);

        List<BLangBindingPattern> bindingPatterns = listBindingPattern.bindingPatterns;
        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);

        for (int i = 0; i < bindingPatterns.size(); i++) {
            BLangExpression memberPatternCondition = createConditionForListMemberPattern(i, bindingPatterns.get(i),
                                                                                         tempCastVarDef, ifBlock,
                                                                                         bindingPatterns.get(i)
                                                                                                 .getBType(), pos);
            if (memberPatternCondition.getKind() == NodeKind.LITERAL) {
                if ((Boolean) ((BLangLiteral) memberPatternCondition).value) {
                    continue;
                }
            }
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, memberPatternCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        }

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (listBindingPattern.restBindingPattern != null) {
            BLangRestBindingPattern restBindingPattern = listBindingPattern.restBindingPattern;
            BLangSimpleVarRef restBindingPatternVarRef = declaredVarDef.get(restBindingPattern.variableName.value);
            matchStmtsForPattern.add(ASTBuilderUtil.createAssignmentStmt(pos, restBindingPatternVarRef,
                    createLangLibInvocationNode("slice", tempCastVarRef,
                            new ArrayList<>(Arrays.asList(new BLangLiteral((long) bindingPatterns.size(),
                                    symTable.intType))), null, pos)));
        }

        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        ifBlock.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BLangExpression createConditionForListMemberPattern(int index, BLangBindingPattern bindingPattern,
                                                                BLangSimpleVariableDef tempCastVarDef,
                                                                BLangBlockStmt blockStmt, BType type,
                                                                Location pos) {
        BLangExpression indexExpr = createIndexBasedAccessExpr(type, pos, new BLangLiteral((long) index,
                symTable.intType), tempCastVarDef.var.symbol, null);

        BLangSimpleVariableDef tempVarDef = createVarDef("$memberVarTemp$" + index + "_$", type, indexExpr,
                bindingPattern.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(pos, tempVarDef.var.symbol);
        blockStmt.addStatement(tempVarDef);

        return createVarCheckCondition(bindingPattern, tempVarRef);
    }

    private BLangExpression createVarCheckCondition(BLangBindingPattern bindingPattern, BLangSimpleVarRef varRef) {
        NodeKind bindingPatternKind = bindingPattern.getKind();
        switch (bindingPatternKind) {
            case WILDCARD_BINDING_PATTERN:
                return createConditionForWildCardBindingPattern((BLangWildCardBindingPattern) bindingPattern, varRef);
            case CAPTURE_BINDING_PATTERN:
                return createConditionForCaptureBindingPattern((BLangCaptureBindingPattern) bindingPattern, varRef);
            case LIST_BINDING_PATTERN:
                return createVarCheckConditionForListBindingPattern((BLangListBindingPattern) bindingPattern, varRef);
            case MAPPING_BINDING_PATTERN:
                return createVarCheckConditionForMappingBindingPattern((BLangMappingBindingPattern) bindingPattern,
                        varRef);
            case ERROR_BINDING_PATTERN:
                return createConditionForErrorBindingPattern((BLangErrorBindingPattern) bindingPattern, varRef);
            default:
                // If some patterns are not implemented, those should be detected before this phase
                // TODO : Remove this after all patterns are implemented
                return null;
        }
    }

    private BLangExpression createVarCheckConditionForListBindingPattern(BLangListBindingPattern listBindingPattern,
                                                                         BLangSimpleVarRef varRef) {
        Location pos = listBindingPattern.pos;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos);

        BLangSimpleVariableDef resultVarDef = createVarDef("$listPatternVarResult$", symTable.booleanType, null,
                pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        blockStmt.addStatement(resultVarDef);

        if (listBindingPattern.getBType() == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, blockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        blockStmt.addStatement(failureResult);

        List<BType> memberTupleTypes = ((BTupleType) listBindingPattern.getBType()).getTupleTypes();
        List<BLangBindingPattern> bindingPatterns = listBindingPattern.bindingPatterns;

        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", listBindingPattern.getBType(), varRef, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos,
                tempCastVarDef.var.symbol);
        blockStmt.addStatement(tempCastVarDef);
        BLangExpression condition = createConditionForListMemberPattern(0, bindingPatterns.get(0),
                tempCastVarDef, blockStmt, memberTupleTypes.get(0), pos);

        for (int i = 1; i < bindingPatterns.size(); i++) {
            BLangExpression memberPatternCondition = createConditionForListMemberPattern(i,
                    bindingPatterns.get(i), tempCastVarDef, blockStmt, memberTupleTypes.get(i), pos);

            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, memberPatternCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType,
                                    symTable.booleanType));
        }

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (listBindingPattern.restBindingPattern != null) {
            BLangRestBindingPattern restBindingPattern = listBindingPattern.restBindingPattern;
            BLangSimpleVarRef restBindingPatternVarRef =
                    declaredVarDef.get(restBindingPattern.variableName.value);
            matchStmtsForPattern.add(ASTBuilderUtil.createAssignmentStmt(pos, restBindingPatternVarRef,
                    createLangLibInvocationNode("slice", tempCastVarRef,
                            new ArrayList<>(Arrays.asList(new BLangLiteral((long) bindingPatterns.size(),
                                    symTable.intType))), null, pos)));
        }

        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        blockStmt.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(blockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BLangExpression createConditionForVarBindingPatternMatchPattern(BLangVarBindingPatternMatchPattern
                                                                                    varBindingPatternMatchPattern,
                                                                            BLangSimpleVarRef matchExprVarRef) {
        BLangBindingPattern bindingPattern = varBindingPatternMatchPattern.getBindingPattern();
        Location pos = bindingPattern.pos;

        switch (bindingPattern.getKind()) {
            case WILDCARD_BINDING_PATTERN:
                return createConditionForWildCardBindingPattern((BLangWildCardBindingPattern) bindingPattern,
                        matchExprVarRef);
            case CAPTURE_BINDING_PATTERN:
                return createConditionForCaptureBindingPattern((BLangCaptureBindingPattern) bindingPattern,
                        matchExprVarRef, pos);
            case LIST_BINDING_PATTERN:
                return createConditionForListBindingPattern((BLangListBindingPattern) bindingPattern, matchExprVarRef);
            case MAPPING_BINDING_PATTERN:
                return createConditionForMappingBindingPattern((BLangMappingBindingPattern) bindingPattern,
                        matchExprVarRef);
            case ERROR_BINDING_PATTERN:
                return createConditionForErrorBindingPattern((BLangErrorBindingPattern) bindingPattern,
                        matchExprVarRef);
            default:
                // If some patterns are not implemented, those should be detected before this phase
                // TODO : Remove this after all patterns are implemented
                return null;
        }
    }

    private BLangExpression createConditionForCaptureBindingPattern(BLangCaptureBindingPattern captureBindingPattern,
                                                                    BLangSimpleVarRef matchExprVarRef,
                                                                    Location pos) {
        BLangSimpleVarRef captureBindingPatternVarRef =
                declaredVarDef.get(captureBindingPattern.getIdentifier().getValue());
        matchStmtsForPattern.add(ASTBuilderUtil.createAssignmentStmt(pos,
                captureBindingPatternVarRef, matchExprVarRef));
        return ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
    }

    private BLangExpression createConditionForMappingBindingPattern(BLangMappingBindingPattern mappingBindingPattern,
                                                                    BLangSimpleVarRef matchExprVarRef) {

        BType bindingPatternType = mappingBindingPattern.getBType();
        Location pos = mappingBindingPattern.pos;

        BLangSimpleVariableDef resultVarDef = createVarDef("$mappingBindingPatternResult$", symTable.booleanType,
                null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (bindingPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression typeCheckCondition = createIsLikeExpression(pos, matchExprVarRef, bindingPatternType);
        BLangExpression typeConvertedExpr = addConversionExprIfRequired(matchExprVarRef, bindingPatternType);
        BLangSimpleVariableDef tempCastVarDef =
                createVarDef("$castTemp$", bindingPatternType, typeConvertedExpr, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifBlock.addStatement(tempCastVarDef);
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos, typeCheckCondition, ifBlock, null);
        mainBlockStmt.addStatement(ifStmt);

        BLangExpression condition = createConditionForFieldBindingPatterns(mappingBindingPattern.fieldBindingPatterns,
                tempCastVarDef, ifBlock, pos);

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (mappingBindingPattern.restBindingPattern != null) {
            BLangRestBindingPattern restBindingPattern = mappingBindingPattern.restBindingPattern;
            Location restPatternPos = restBindingPattern.pos;
            List<String> keysToRemove = getKeysToRemove(mappingBindingPattern);
            BLangSimpleVarRef restMatchPatternVarRef =
                    declaredVarDef.get(restBindingPattern.getIdentifier().getValue());
            createRestPattern(restPatternPos, keysToRemove, tempCastVarRef, restBindingPattern.getBType(),
                    tempBlockStmt, restMatchPatternVarRef);
        }
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        ifBlock.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);

        addAsRecordTypeDefinition(bindingPatternType, pos);
        return statementExpression;
    }

    private BLangExpression createConditionForErrorBindingPattern(BLangErrorBindingPattern errorBindingPattern,
                                                                  BLangSimpleVarRef matchExprVarRef) {
        BType bindingPatternType = errorBindingPattern.getBType();
        Location pos = errorBindingPattern.pos;

        BLangSimpleVariableDef resultVarDef = createVarDef("errorBindingPatternResult$", symTable.booleanType, null,
                pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (bindingPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression typeCheckCondition = createIsLikeExpression(pos, matchExprVarRef, bindingPatternType);
        BLangExpression typeConvertedExpr = addConversionExprIfRequired(matchExprVarRef, bindingPatternType);
        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", bindingPatternType, typeConvertedExpr, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifBlock.addStatement(tempCastVarDef);
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos, typeCheckCondition, ifBlock, null);
        mainBlockStmt.addStatement(ifStmt);

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        BLangExpression condition = createConditionForErrorArgListBindingPattern(errorBindingPattern, ifBlock,
                tempBlockStmt, tempCastVarRef, pos);

        tempBlockStmt.addStatement(successResult);
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        ifBlock.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BLangExpression createConditionForErrorArgListBindingPattern(BLangErrorBindingPattern errorBindingPattern,
                                                                         BLangBlockStmt ifBlock,
                                                                         BLangBlockStmt restPatternBlock,
                                                                         BLangSimpleVarRef varRef,
                                                                         Location pos) {
        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
        if (errorBindingPattern.errorMessageBindingPattern != null) {
            Location messagePos = errorBindingPattern.errorMessageBindingPattern.pos;
            BLangInvocation messageInvocation = createLangLibInvocationNode(ERROR_MESSAGE_FUNCTION_NAME, varRef,
                    new ArrayList<>(), null, messagePos);
            BLangSimpleVariableDef messageVarDef = createVarDef("$errorMessage$", messageInvocation.getBType(),
                                                                messageInvocation, messagePos);
            ifBlock.addStatement(messageVarDef);
            BLangSimpleVarRef messageVarRef = ASTBuilderUtil.createVariableRef(messagePos, messageVarDef.var.symbol);
            condition = createConditionForErrorMessageBindingPattern(errorBindingPattern.errorMessageBindingPattern,
                    messageVarRef);
        }

        if (errorBindingPattern.errorCauseBindingPattern != null) {
            Location errorCausePos = errorBindingPattern.errorCauseBindingPattern.pos;
            BLangInvocation causeInvocation = createLangLibInvocationNode(ERROR_CAUSE_FUNCTION_NAME, varRef,
                    new ArrayList<>(), null, errorCausePos);
            BLangSimpleVariableDef causeVarDef = createVarDef("$errorCause$", causeInvocation.getBType(),
                                                              causeInvocation, errorCausePos);
            ifBlock.addStatement(causeVarDef);
            BLangSimpleVarRef causeVarRef = ASTBuilderUtil.createVariableRef(errorCausePos, causeVarDef.var.symbol);
            BLangExpression errorCauseCondition =
                    createConditionForErrorCauseBindingPattern(errorBindingPattern.errorCauseBindingPattern,
                            causeVarRef);
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, errorCauseCondition, symTable.booleanType,
                    OperatorKind.AND, (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.AND,
                            symTable.booleanType, symTable.booleanType));
        }

        if (errorBindingPattern.errorFieldBindingPatterns != null) {
            Location errorFieldPos = errorBindingPattern.errorFieldBindingPatterns.pos;
            BLangInvocation errorDetailInvocation = createLangLibInvocationNode(ERROR_DETAIL_FUNCTION_NAME,
                    varRef, new ArrayList<>(), null, errorFieldPos);
            BLangSimpleVariableDef errorDetailVarDef = createVarDef("$errorDetail$", errorDetailInvocation.getBType(),
                                                                    errorDetailInvocation, errorFieldPos);
            ifBlock.addStatement(errorDetailVarDef);
            BLangSimpleVarRef errorDetailVarRef = ASTBuilderUtil.createVariableRef(errorFieldPos,
                    errorDetailVarDef.var.symbol);

            BLangExpression errorDetailCondition =
                    createConditionForErrorFieldBindingPatterns(errorBindingPattern.errorFieldBindingPatterns,
                            errorDetailVarRef);

            if (condition != null) {
                condition = ASTBuilderUtil.createBinaryExpr(pos, condition, errorDetailCondition, symTable.booleanType,
                        OperatorKind.AND, (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.AND,
                                symTable.booleanType, symTable.booleanType));
            } else {
                condition = errorDetailCondition;
            }

            if (errorBindingPattern.errorFieldBindingPatterns.restBindingPattern != null) {
                BLangRestBindingPattern restBindingPattern =
                        errorBindingPattern.errorFieldBindingPatterns.restBindingPattern;
                Location restPatternPos = restBindingPattern.pos;
                List<String> keysToRemove = getKeysToRemove(errorBindingPattern.errorFieldBindingPatterns);
                BMapType entriesType = new BMapType(TypeTags.MAP, new BTupleType(Arrays.asList(symTable.stringType,
                        symTable.anydataType)), null);
                BLangInvocation entriesInvocation = generateMapEntriesInvocation(errorDetailVarRef, entriesType);
                BLangSimpleVariableDef entriesVarDef = createVarDef("$entries$", entriesType, entriesInvocation,
                        restPatternPos);
                restPatternBlock.addStatement(entriesVarDef);
                BLangLambdaFunction filteringFunction = createFuncToFilterOutRestParam(keysToRemove, restPatternPos);
                BLangInvocation filterInvocation = generateMapFilterInvocation(pos, entriesVarDef.var,
                        filteringFunction);
                BLangSimpleVariableDef filtersVarDef = createVarDef("$filteredVarDef$", entriesType, filterInvocation,
                        restPatternPos);
                restPatternBlock.addStatement(filtersVarDef);
                BType errFieldRestType = errorBindingPattern.errorFieldBindingPatterns.restBindingPattern.getBType();
                BLangLambdaFunction backToMapLambda =
                        generateEntriesToMapLambda(restPatternPos, ((BMapType) errFieldRestType).constraint);
                BLangInvocation mapInvocation = generateMapMapInvocation(restPatternPos, filtersVarDef.var,
                        backToMapLambda);
                BLangSimpleVarRef restMatchPatternVarRef =
                        declaredVarDef.get(restBindingPattern.getIdentifier().getValue());
                restPatternBlock.addStatement(ASTBuilderUtil.createAssignmentStmt(pos, restMatchPatternVarRef,
                        mapInvocation));
            }
        }

        return condition;
    }

    private BLangExpression createConditionForListMatchPattern(BLangListMatchPattern listMatchPattern,
                                                               BLangSimpleVarRef matchExprVarRef) {
        Location pos = listMatchPattern.pos;
        BType matchPatternType = listMatchPattern.getBType();

        BLangSimpleVariableDef resultVarDef = createVarDef("$listPatternResult$", symTable.booleanType, null,
                pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (matchPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression typeCheckCondition = createIsLikeExpression(listMatchPattern.pos, matchExprVarRef,
                matchPatternType);

        BLangExpression typeConvertedExpr = addConversionExprIfRequired(matchExprVarRef, matchPatternType);
        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", matchPatternType,
                typeConvertedExpr, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos,
                tempCastVarDef.var.symbol);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifBlock.addStatement(tempCastVarDef);
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos, typeCheckCondition, ifBlock, null);
        mainBlockStmt.addStatement(ifStmt);

        List<BLangMatchPattern> matchPatterns = listMatchPattern.matchPatterns;
        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);

        for (int i = 0; i < matchPatterns.size(); i++) {
            BLangExpression memberPatternCondition = createConditionForListMemberPattern(i, matchPatterns.get(i),
                                                                                         tempCastVarDef, ifBlock,
                                                                                         matchPatterns.get(i)
                                                                                                 .getBType(), pos);
            if (memberPatternCondition.getKind() == NodeKind.LITERAL) {
                if ((Boolean) ((BLangLiteral) memberPatternCondition).value) {
                    continue;
                }
            }
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, memberPatternCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType,
                                    symTable.booleanType));
        }

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (listMatchPattern.restMatchPattern != null) {
            BLangRestMatchPattern restMatchPattern = listMatchPattern.restMatchPattern;
            BLangSimpleVarRef restMatchPatternVarRef = declaredVarDef.get(restMatchPattern.getIdentifier().getValue());
            matchStmtsForPattern.add(ASTBuilderUtil.createAssignmentStmt(pos, restMatchPatternVarRef,
                    createLangLibInvocationNode("slice", tempCastVarRef,
                            new ArrayList<>(Arrays.asList(new BLangLiteral((long) matchPatterns.size(),
                                    symTable.intType))), null, pos)));
        }

        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        ifBlock.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BLangExpression createConditionForListMemberPattern(int index, BLangMatchPattern listMemberMatchPattern,
                                                                BLangSimpleVariableDef tempCastVarDef,
                                                                BLangBlockStmt blockStmt, BType type,
                                                                Location pos) {
        BLangExpression indexExpr = createIndexBasedAccessExpr(type, pos, new BLangLiteral((long) index,
                symTable.intType), tempCastVarDef.var.symbol, null);

        BLangSimpleVariableDef tempVarDef = createVarDef("$memberVarTemp$" + index + "_$", type, indexExpr,
                listMemberMatchPattern.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(pos, tempVarDef.var.symbol);
        blockStmt.addStatement(tempVarDef);

        return createVarCheckCondition(listMemberMatchPattern, tempVarRef);
    }

    private BLangExpression createConditionForMappingMatchPattern(BLangMappingMatchPattern mappingMatchPattern,
                                                                  BLangSimpleVarRef matchExprVarRef) {
        BType matchPatternType = mappingMatchPattern.getBType();
        Location pos = mappingMatchPattern.pos;

        BLangSimpleVariableDef resultVarDef = createVarDef("$mappingPatternResult$", symTable.booleanType, null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (matchPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression typeCheckCondition = createIsLikeExpression(mappingMatchPattern.pos, matchExprVarRef,
                matchPatternType);
        BLangExpression typeConvertedExpr = addConversionExprIfRequired(matchExprVarRef, matchPatternType);
        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", matchPatternType, typeConvertedExpr, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifBlock.addStatement(tempCastVarDef);
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos, typeCheckCondition, ifBlock, null);
        mainBlockStmt.addStatement(ifStmt);

        BLangExpression condition = createConditionForFieldMatchPatterns(mappingMatchPattern.fieldMatchPatterns,
                tempCastVarDef, ifBlock, pos);

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (mappingMatchPattern.restMatchPattern != null) {
            BLangRestMatchPattern restMatchPattern = mappingMatchPattern.restMatchPattern;
            Location restPatternPos = restMatchPattern.pos;
            List<String> keysToRemove = getKeysToRemove(mappingMatchPattern);
            BLangSimpleVarRef restMatchPatternVarRef = declaredVarDef.get(restMatchPattern.getIdentifier().getValue());
            createRestPattern(restPatternPos, keysToRemove, tempCastVarRef,
                    restMatchPattern.getBType(), tempBlockStmt, restMatchPatternVarRef);
        }
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        ifBlock.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        addAsRecordTypeDefinition(matchPatternType, pos);
        return statementExpression;
    }

    private void createRestPattern(Location pos, List<String> keysToRemove, BLangSimpleVarRef matchExprVarRef,
                                   BType targetType, BLangBlockStmt blockStmt,
                                   BLangSimpleVarRef restMatchPatternVarRef) {
        BType constraintType = getRestFilterConstraintType(targetType);
        BMapType entriesType = new BMapType(TypeTags.MAP, new BTupleType(Arrays.asList(symTable.stringType,
                constraintType)), null);
        BLangInvocation entriesInvocation = generateMapEntriesInvocation(matchExprVarRef, entriesType);
        BLangSimpleVariableDef entriesVarDef = createVarDef("$entries$", entriesType, entriesInvocation, pos);
        blockStmt.addStatement(entriesVarDef);
        BLangLambdaFunction filteringFunction = createFuncToFilterOutRestParam(keysToRemove, pos);
        BLangInvocation filterInvocation = generateMapFilterInvocation(pos, entriesVarDef.var, filteringFunction);
        BLangSimpleVariableDef filtersVarDef = createVarDef("$filteredVarDef$", entriesType,
                filterInvocation, pos);
        blockStmt.addStatement(filtersVarDef);

        BLangLambdaFunction backToMapLambda = generateEntriesToMapLambda(pos, constraintType);
        BLangInvocation mapInvocation = generateMapMapInvocation(pos, filtersVarDef.var,
                backToMapLambda);
        BLangSimpleVariableDef mappedVarDef = createVarDef("$mappedVarDef$", entriesType,
                mapInvocation, pos);
        blockStmt.addStatement(mappedVarDef);

        BLangInvocation recordConversion = generateCreateRecordValueInvocation(pos, targetType,
                mappedVarDef.var.symbol);
        BLangSimpleVariableDef recordVarDef = createVarDef("$recordVarDef$", entriesType,
                recordConversion, pos);
        blockStmt.addStatement(recordVarDef);

        blockStmt.addStatement(ASTBuilderUtil.createAssignmentStmt(pos, restMatchPatternVarRef,
                addConversionExprIfRequired(createVariableRef(pos, recordVarDef.var.symbol),
                        targetType)));
    }

    private List<String> getKeysToRemove(BLangMappingMatchPattern mappingMatchPattern) {
        List<String> keysToRemove = new ArrayList<>();
        for (BLangFieldMatchPattern fieldMatchPattern : mappingMatchPattern.fieldMatchPatterns) {
            keysToRemove.add(fieldMatchPattern.fieldName.value);
        }
        return keysToRemove;
    }

    private List<String> getKeysToRemove(BLangMappingBindingPattern mappingBindingPattern) {
        List<String> keysToRemove = new ArrayList<>();
        List<BLangFieldBindingPattern> fieldBindingPatterns = mappingBindingPattern.fieldBindingPatterns;
        for (int i = 0; i < fieldBindingPatterns.size(); i++) {
            keysToRemove.add(fieldBindingPatterns.get(i).fieldName.value);
        }
        return keysToRemove;
    }

    private BLangExpression createConditionForFieldMatchPatterns(List<BLangFieldMatchPattern> fieldMatchPatterns,
                                                                 BLangSimpleVariableDef varDef,
                                                                 BLangBlockStmt blockStmt,
                                                                 Location pos) {
        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
        for (int i = 0; i < fieldMatchPatterns.size(); i++) {
            BLangExpression fieldMatchPatternCondition =
                    createConditionForFieldMatchPattern(i, fieldMatchPatterns.get(i), varDef, blockStmt);
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, fieldMatchPatternCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        }
        return condition;
    }

    private BLangExpression createConditionForFieldMatchPattern(int index, BLangFieldMatchPattern fieldMatchPattern,
                                                                BLangSimpleVariableDef tempCastVarDef,
                                                                BLangBlockStmt blockStmt) {
        String fieldName = fieldMatchPattern.fieldName.value;
        BLangMatchPattern matchPattern = fieldMatchPattern.matchPattern;
        BLangFieldBasedAccess fieldBasedAccessExpr = getFieldAccessExpression(fieldMatchPattern.pos, fieldName,
                                                                              matchPattern.getBType(),
                                                                              tempCastVarDef.var.symbol);
        BLangSimpleVariableDef tempVarDef = createVarDef("$memberVarTemp$" + index + "_$", matchPattern.getBType(),
                                                         fieldBasedAccessExpr, matchPattern.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(matchPattern.pos, tempVarDef.var.symbol);
        blockStmt.addStatement(tempVarDef);
        return createVarCheckCondition(matchPattern, tempVarRef);
    }

    private BLangExpression createConditionForFieldBindingPatterns(List<BLangFieldBindingPattern> fieldBindingPatterns,
                                                                   BLangSimpleVariableDef varDef,
                                                                   BLangBlockStmt blockStmt,
                                                                   Location pos) {
        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
        if (fieldBindingPatterns.isEmpty()) {
            return condition;
        }

        condition = createConditionForFieldBindingPattern(0, fieldBindingPatterns.get(0), varDef, blockStmt);
        for (int i = 1; i < fieldBindingPatterns.size(); i++) {
            BLangExpression fieldMatchPatternCondition =
                    createConditionForFieldBindingPattern(i, fieldBindingPatterns.get(i), varDef, blockStmt);
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, fieldMatchPatternCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        }
        return condition;
    }

    private BLangExpression createConditionForFieldBindingPattern(int index,
                                                                  BLangFieldBindingPattern fieldBindingPattern,
                                                                  BLangSimpleVariableDef tempCastVarDef,
                                                                  BLangBlockStmt blockStmt) {
        String fieldName = fieldBindingPattern.fieldName.value;
        BLangBindingPattern bindingPattern = fieldBindingPattern.bindingPattern;
        BLangFieldBasedAccess fieldBasedAccessExpr = getFieldAccessExpression(fieldBindingPattern.pos, fieldName,
                                                                              bindingPattern.getBType(),
                                                                              tempCastVarDef.var.symbol);
        BLangSimpleVariableDef tempVarDef = createVarDef("$memberVarTemp$" + index + "_$", bindingPattern.getBType(),
                                                         fieldBasedAccessExpr, bindingPattern.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(bindingPattern.pos, tempVarDef.var.symbol);
        blockStmt.addStatement(tempVarDef);
        return createVarCheckCondition(bindingPattern, tempVarRef);
    }

    private BLangExpression createVarCheckCondition(BLangMatchPattern matchPattern, BLangSimpleVarRef varRef) {

        NodeKind patternKind = matchPattern.getKind();
        switch (patternKind) {
            case WILDCARD_MATCH_PATTERN:
                return createConditionForWildCardMatchPattern((BLangWildCardMatchPattern) matchPattern, varRef);
            case CONST_MATCH_PATTERN:
                return createConditionForConstMatchPattern((BLangConstPattern) matchPattern, varRef);
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                return createVarCheckCondition(((BLangVarBindingPatternMatchPattern) matchPattern).getBindingPattern(),
                        varRef);
            case LIST_MATCH_PATTERN:
                return createVarCheckConditionForListMatchPattern((BLangListMatchPattern) matchPattern, varRef);
            case MAPPING_MATCH_PATTERN:
                return createVarCheckConditionForMappingMatchPattern((BLangMappingMatchPattern) matchPattern, varRef);
            case ERROR_MATCH_PATTERN:
                return createConditionForErrorMatchPattern((BLangErrorMatchPattern) matchPattern, varRef);
            default:
                // If some patterns are not implemented, those should be detected before this phase
                // TODO : Remove this after all patterns are implemented
                return null;
        }
    }

    private BLangExpression createVarCheckConditionForMappingBindingPattern(BLangMappingBindingPattern
                                                                                    mappingBindingPattern,
                                                                            BLangSimpleVarRef varRef) {
        BType bindingPatternType = mappingBindingPattern.getBType();
        Location pos = mappingBindingPattern.pos;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos);

        BLangSimpleVariableDef resultVarDef = createVarDef("$mappingBindingPatternVarResult$", symTable.booleanType,
                null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        blockStmt.addStatement(resultVarDef);

        if (bindingPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, blockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        blockStmt.addStatement(failureResult);

        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", bindingPatternType, varRef,
                pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);
        blockStmt.addStatement(tempCastVarDef);

        BLangExpression condition =
                createConditionForFieldBindingPatterns(mappingBindingPattern.fieldBindingPatterns, tempCastVarDef,
                        blockStmt, pos);

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        createRestBindingPatternCondition(mappingBindingPattern, tempBlockStmt, tempCastVarRef);
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        blockStmt.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(blockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        addAsRecordTypeDefinition(bindingPatternType, pos);
        return statementExpression;
    }

    private void createRestBindingPatternCondition(BLangMappingBindingPattern mappingBindingPattern,
                                                   BLangBlockStmt blockStmt, BLangSimpleVarRef varRef) {
        BLangRestBindingPattern restBindingPattern = mappingBindingPattern.restBindingPattern;
        if (restBindingPattern == null) {
            return;
        }

        Location restPatternPos = restBindingPattern.pos;
        List<String> keysToRemove = getKeysToRemove(mappingBindingPattern);
        BMapType entriesType = new BMapType(TypeTags.MAP, new BTupleType(Arrays.asList(symTable.stringType,
                ((BRecordType) restBindingPattern.getBType()).restFieldType)), null);
        BLangInvocation entriesInvocation = generateMapEntriesInvocation(varRef, entriesType);
        BLangSimpleVariableDef entriesVarDef = createVarDef("$entries$", entriesType, entriesInvocation,
                restPatternPos);
        blockStmt.addStatement(entriesVarDef);
        BLangLambdaFunction filteringFunction = createFuncToFilterOutRestParam(keysToRemove, restPatternPos);
        BLangInvocation filterInvocation = generateMapFilterInvocation(restPatternPos, entriesVarDef.var,
                filteringFunction);
        BLangSimpleVariableDef filtersVarDef = createVarDef("$filteredVarDef$", entriesType, filterInvocation,
                restPatternPos);
        blockStmt.addStatement(filtersVarDef);
        BLangLambdaFunction backToMapLambda = generateEntriesToMapLambda(restPatternPos,
                ((BRecordType) restBindingPattern.getBType()).restFieldType);
        BLangInvocation mapInvocation = generateMapMapInvocation(restPatternPos, filtersVarDef.var,
                backToMapLambda);
        BLangSimpleVarRef restMatchPatternVarRef =
                declaredVarDef.get(restBindingPattern.getIdentifier().getValue());
        blockStmt.addStatement(ASTBuilderUtil.createAssignmentStmt(restPatternPos, restMatchPatternVarRef,
                mapInvocation));
    }

    private BLangExpression createVarCheckConditionForListMatchPattern(BLangListMatchPattern listMatchPattern,
                                                                       BLangSimpleVarRef varRef) {
        Location pos = listMatchPattern.pos;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos);

        BLangSimpleVariableDef resultVarDef = createVarDef("$listPatternVarResult$", symTable.booleanType, null,
                pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        blockStmt.addStatement(resultVarDef);

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        blockStmt.addStatement(failureResult);

        List<BType> memberTupleTypes = ((BTupleType) listMatchPattern.getBType()).getTupleTypes();
        List<BLangMatchPattern> matchPatterns = listMatchPattern.matchPatterns;

        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", listMatchPattern.getBType(), varRef, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);
        blockStmt.addStatement(tempCastVarDef);
        BLangExpression condition = createConditionForListMemberPattern(0, matchPatterns.get(0),
                tempCastVarDef, blockStmt, memberTupleTypes.get(0), pos);

        for (int i = 1; i < matchPatterns.size(); i++) {
            BLangExpression memberPatternCondition = createConditionForListMemberPattern(i,
                    matchPatterns.get(i), tempCastVarDef, blockStmt, memberTupleTypes.get(i), pos);

            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, memberPatternCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType,
                                    symTable.booleanType));
        }

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (listMatchPattern.restMatchPattern != null) {
            BLangRestMatchPattern restMatchPattern = listMatchPattern.restMatchPattern;
            BLangSimpleVarRef restMatchPatternVarRef =
                    declaredVarDef.get(restMatchPattern.getIdentifier().getValue());
            matchStmtsForPattern.add(ASTBuilderUtil.createAssignmentStmt(pos, restMatchPatternVarRef,
                    createLangLibInvocationNode("slice", tempCastVarRef,
                            new ArrayList<>(Arrays.asList(new BLangLiteral((long) matchPatterns.size(),
                                    symTable.intType))), null, pos)));
        }
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        blockStmt.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(blockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BLangExpression createVarCheckConditionForMappingMatchPattern(BLangMappingMatchPattern mappingMatchPattern,
                                                                          BLangSimpleVarRef varRef) {
        BRecordType recordType = (BRecordType) mappingMatchPattern.getBType();
        Location pos = mappingMatchPattern.pos;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos);

        BLangSimpleVariableDef resultVarDef = createVarDef("$mappingPatternVarResult$", symTable.booleanType,
                null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        blockStmt.addStatement(resultVarDef);

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        blockStmt.addStatement(failureResult);

        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", mappingMatchPattern.getBType(), varRef,
                                                             pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);
        blockStmt.addStatement(tempCastVarDef);

        BLangExpression condition =
                createConditionForFieldMatchPatterns(mappingMatchPattern.fieldMatchPatterns, tempCastVarDef,
                        blockStmt, pos);

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        tempBlockStmt.addStatement(successResult);
        if (mappingMatchPattern.restMatchPattern != null) {
            BLangRestMatchPattern restMatchPattern = mappingMatchPattern.restMatchPattern;
            Location restPatternPos = restMatchPattern.pos;
            List<String> keysToRemove = getKeysToRemove(mappingMatchPattern);
            BMapType entriesType = new BMapType(TypeTags.MAP, new BTupleType(Arrays.asList(symTable.stringType,
                    ((BRecordType) restMatchPattern.getBType()).restFieldType)), null);
            BLangInvocation entriesInvocation = generateMapEntriesInvocation(tempCastVarRef, entriesType);
            BLangSimpleVariableDef entriesVarDef = createVarDef("$entries$", entriesType, entriesInvocation,
                    restPatternPos);
            tempBlockStmt.addStatement(entriesVarDef);
            BLangLambdaFunction filteringFunction = createFuncToFilterOutRestParam(keysToRemove, restPatternPos);
            BLangInvocation filterInvocation = generateMapFilterInvocation(pos, entriesVarDef.var, filteringFunction);
            BLangSimpleVariableDef filtersVarDef = createVarDef("$filteredVarDef$", entriesType, filterInvocation,
                    restPatternPos);
            tempBlockStmt.addStatement(filtersVarDef);
            BLangLambdaFunction backToMapLambda = generateEntriesToMapLambda(restPatternPos,
                    ((BRecordType) restMatchPattern.getBType()).restFieldType);
            BLangInvocation mapInvocation = generateMapMapInvocation(restPatternPos, filtersVarDef.var,
                    backToMapLambda);
            BLangSimpleVarRef restMatchPatternVarRef =
                    declaredVarDef.get(restMatchPattern.getIdentifier().getValue());
            tempBlockStmt.addStatement(ASTBuilderUtil.createAssignmentStmt(pos, restMatchPatternVarRef, mapInvocation));
        }
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        blockStmt.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(blockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        addAsRecordTypeDefinition(recordType, pos);
        return statementExpression;
    }

    private void addAsRecordTypeDefinition(BType type, Location pos) {
        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                addAsRecordTypeDefinition(memberType, pos);
            }
            return;
        }
        if (type.tag != TypeTags.RECORD) {
            return;
        }
        BRecordType recordType = (BRecordType) type;
        if (isRecordTypeDefExist(recordType.tsymbol, env)) {
            return;
        }
        BLangRecordTypeNode recordTypeNode = new BLangRecordTypeNode();
        recordTypeNode.pos = pos;
        recordTypeNode.setBType(recordType);
        List<BLangSimpleVariable> typeDefFields = new ArrayList<>();
        for (BField field : recordType.fields.values()) {
            typeDefFields.add(ASTBuilderUtil.createVariable(field.pos, field.name.value, field.type, null,
                    field.symbol));
        }
        recordTypeNode.fields = typeDefFields;
        recordTypeNode.symbol = recordType.tsymbol;
        recordTypeNode.isAnonymous = true;
        recordTypeNode.isLocal = true;
        recordTypeNode.getBType().tsymbol.scope = new Scope(recordTypeNode.getBType().tsymbol);
        recordTypeNode.initFunction =
                rewrite(TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names, symTable),
                        env);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordType, recordType.tsymbol, recordTypeNode, env);
    }

    private boolean isRecordTypeDefExist(BTypeSymbol recordTypeSymbol, SymbolEnv env) {
        for (BLangTypeDefinition typeDef : env.enclPkg.getTypeDefinitions()) {
            if (typeDef.symbol == recordTypeSymbol) {
                return true;
            }
        }
        return false;
    }

    private BLangExpression createConditionForErrorMatchPattern(BLangErrorMatchPattern errorMatchPattern,
                                                                BLangSimpleVarRef matchExprVarRef) {
        BType matchPatternType = errorMatchPattern.getBType();
        Location pos = errorMatchPattern.pos;

        BLangSimpleVariableDef resultVarDef = createVarDef("errorPatternResult$", symTable.booleanType, null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (matchPatternType == symTable.noType) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression typeCheckCondition = createIsLikeExpression(pos, matchExprVarRef, matchPatternType);
        BLangExpression typeConvertedExpr = addConversionExprIfRequired(matchExprVarRef, matchPatternType);
        BLangSimpleVariableDef tempCastVarDef = createVarDef("$castTemp$", matchPatternType, typeConvertedExpr, pos);
        BLangSimpleVarRef tempCastVarRef = ASTBuilderUtil.createVariableRef(pos, tempCastVarDef.var.symbol);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifBlock.addStatement(tempCastVarDef);
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos, typeCheckCondition, ifBlock, null);
        mainBlockStmt.addStatement(ifStmt);

        BLangBlockStmt tempBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        BLangExpression condition = createConditionForErrorArgListMatchPattern(errorMatchPattern, ifBlock,
                tempBlockStmt, tempCastVarRef, pos);

        tempBlockStmt.addStatement(successResult);
        BLangIf ifStmtForMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBlockStmt, null);
        ifBlock.addStatement(ifStmtForMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BLangExpression createConditionForErrorArgListMatchPattern(BLangErrorMatchPattern errorMatchPattern,
                                                                       BLangBlockStmt ifBlock,
                                                                       BLangBlockStmt restPatternBlock,
                                                                       BLangSimpleVarRef varRef,
                                                                       Location pos) {
        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
        if (errorMatchPattern.errorMessageMatchPattern != null) {
            Location messagePos = errorMatchPattern.errorMessageMatchPattern.pos;
            BLangInvocation messageInvocation = createLangLibInvocationNode(ERROR_MESSAGE_FUNCTION_NAME, varRef,
                    new ArrayList<>(), null, messagePos);
            BLangSimpleVariableDef messageVarDef = createVarDef("$errorMessage$", messageInvocation.getBType(),
                                                                messageInvocation, messagePos);
            ifBlock.addStatement(messageVarDef);
            BLangSimpleVarRef messageVarRef = ASTBuilderUtil.createVariableRef(messagePos, messageVarDef.var.symbol);
            condition = createConditionForErrorMessageMatchPattern(errorMatchPattern.errorMessageMatchPattern,
                    messageVarRef);
        }

        if (errorMatchPattern.errorCauseMatchPattern != null) {
            Location errorCausePos = errorMatchPattern.errorCauseMatchPattern.pos;
            BLangInvocation causeInvocation = createLangLibInvocationNode(ERROR_CAUSE_FUNCTION_NAME, varRef,
                    new ArrayList<>(), null, errorCausePos);
            BLangSimpleVariableDef causeVarDef = createVarDef("$errorCause$", causeInvocation.getBType(),
                                                              causeInvocation, errorCausePos);
            ifBlock.addStatement(causeVarDef);
            BLangSimpleVarRef causeVarRef = ASTBuilderUtil.createVariableRef(errorCausePos, causeVarDef.var.symbol);
            BLangExpression errorCauseCondition =
                    createConditionForErrorCauseMatchPattern(errorMatchPattern.errorCauseMatchPattern, causeVarRef);
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, errorCauseCondition, symTable.booleanType,
                    OperatorKind.AND, (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.AND,
                            symTable.booleanType, symTable.booleanType));
        }

        if (errorMatchPattern.errorFieldMatchPatterns != null) {
            Location errorFieldPos = errorMatchPattern.errorFieldMatchPatterns.pos;
            BLangInvocation errorDetailInvocation = createLangLibInvocationNode(ERROR_DETAIL_FUNCTION_NAME,
                    varRef, new ArrayList<>(), null, errorFieldPos);
            BLangSimpleVariableDef errorDetailVarDef = createVarDef("$errorDetail$", errorDetailInvocation.getBType(),
                                                                    errorDetailInvocation, errorFieldPos);
            ifBlock.addStatement(errorDetailVarDef);
            BLangSimpleVarRef errorDetailVarRef = ASTBuilderUtil.createVariableRef(errorFieldPos,
                    errorDetailVarDef.var.symbol);

            BLangExpression errorDetailCondition =
                    createConditionForErrorFieldMatchPatterns(errorMatchPattern.errorFieldMatchPatterns,
                            errorDetailVarRef);
            condition = ASTBuilderUtil.createBinaryExpr(pos, condition, errorDetailCondition, symTable.booleanType,
                    OperatorKind.AND, (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.AND,
                            symTable.booleanType, symTable.booleanType));

            if (errorMatchPattern.errorFieldMatchPatterns.restMatchPattern != null) {
                BLangRestMatchPattern restMatchPattern = errorMatchPattern.errorFieldMatchPatterns.restMatchPattern;
                Location restPatternPos = restMatchPattern.pos;
                List<String> keysToRemove = getKeysToRemove(errorMatchPattern.errorFieldMatchPatterns);
                BMapType entriesType = new BMapType(TypeTags.MAP, new BTupleType(Arrays.asList(symTable.stringType,
                        symTable.anydataType)), null);
                BLangInvocation entriesInvocation = generateMapEntriesInvocation(errorDetailVarRef, entriesType);
                BLangSimpleVariableDef entriesVarDef = createVarDef("$entries$", entriesType, entriesInvocation,
                        restPatternPos);
                restPatternBlock.addStatement(entriesVarDef);
                BLangLambdaFunction filteringFunction = createFuncToFilterOutRestParam(keysToRemove, restPatternPos);
                BLangInvocation filterInvocation = generateMapFilterInvocation(pos, entriesVarDef.var,
                        filteringFunction);
                BLangSimpleVariableDef filtersVarDef = createVarDef("$filteredVarDef$", entriesType, filterInvocation,
                        restPatternPos);
                restPatternBlock.addStatement(filtersVarDef);
                BLangLambdaFunction backToMapLambda = generateEntriesToMapLambda(restPatternPos,
                        ((BMapType) errorMatchPattern.errorFieldMatchPatterns.restMatchPattern.getBType()).constraint);
                BLangInvocation mapInvocation = generateMapMapInvocation(restPatternPos, filtersVarDef.var,
                        backToMapLambda);
                BLangSimpleVarRef restMatchPatternVarRef =
                        declaredVarDef.get(restMatchPattern.getIdentifier().getValue());
                restPatternBlock.addStatement(ASTBuilderUtil.createAssignmentStmt(restPatternPos,
                        restMatchPatternVarRef, mapInvocation));
            }
        }

        return condition;
    }

    private List<String> getKeysToRemove(BLangErrorFieldMatchPatterns errorFieldMatchPattern) {
        List<String> keysToRemove = new ArrayList<>();
        for (BLangNamedArgMatchPattern namedArgMatchPattern : errorFieldMatchPattern.namedArgMatchPatterns) {
            keysToRemove.add(namedArgMatchPattern.argName.value);
        }
        return keysToRemove;
    }

    private List<String> getKeysToRemove(BLangErrorFieldBindingPatterns errorFieldBindingPattern) {
        List<String> keysToRemove = new ArrayList<>();
        for (BLangNamedArgBindingPattern namedArgBindingPattern : errorFieldBindingPattern.namedArgBindingPatterns) {
            keysToRemove.add(namedArgBindingPattern.argName.value);
        }
        return keysToRemove;
    }

    private BLangExpression createConditionForErrorFieldMatchPatterns(
            BLangErrorFieldMatchPatterns errorFieldMatchPatterns, BLangSimpleVarRef matchExprVarRef) {
        Location pos = errorFieldMatchPatterns.pos;
        BLangSimpleVariableDef resultVarDef = createVarDef("errorFieldResult$", symTable.booleanType, null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression condition = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);
        for (int i = 0; i < errorFieldMatchPatterns.namedArgMatchPatterns.size(); i++) {
            BLangNamedArgMatchPattern namedArgMatchPattern = errorFieldMatchPatterns.namedArgMatchPatterns.get(i);
            String argName = namedArgMatchPattern.argName.value;
            BLangMatchPattern matchPattern = namedArgMatchPattern.matchPattern;
            Location matchPatternPos = matchPattern.pos;

            BLangFieldBasedAccess fieldBasedAccessExpr = getFieldAccessExpression(matchPatternPos, argName,
                    symTable.anydataOrReadonly, (BVarSymbol) matchExprVarRef.symbol);
            BLangSimpleVariableDef tempVarDef = createVarDef("$errorFieldVarTemp$" + i + "_$",
                    symTable.anydataOrReadonly, fieldBasedAccessExpr, matchPatternPos);
            mainBlockStmt.addStatement(tempVarDef);
            BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(matchPatternPos, tempVarDef.var.symbol);

            BLangExpression varCheckCondition = createConditionForNamedArgMatchPattern(matchPattern, tempVarRef);
            BLangExpression typeCheckCondition = createIsLikeExpression(matchPatternPos, tempVarRef,
                                                                        matchPattern.getBType());
            varCheckCondition = ASTBuilderUtil.createBinaryExpr(pos, typeCheckCondition,
                   varCheckCondition, symTable.booleanType, OperatorKind.AND, null);

            if (i == 0) {
                condition = varCheckCondition;
                continue;
            }
            condition = ASTBuilderUtil.createBinaryExpr(matchPatternPos, condition, varCheckCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        }

        BType matchingType = createMatchingRecordType(errorFieldMatchPatterns);
        BLangExpression isLikeExpr = createIsLikeExpression(errorFieldMatchPatterns.pos, matchExprVarRef, matchingType);
        condition = ASTBuilderUtil.createBinaryExpr(errorFieldMatchPatterns.pos, condition, isLikeExpr,
                symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                        .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        BLangBlockStmt tempBLock = ASTBuilderUtil.createBlockStmt(pos);
        tempBLock.addStatement(successResult);
        BLangIf ifStmtForFieldMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBLock, null);
        mainBlockStmt.addStatement(ifStmtForFieldMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BType createMatchingRecordType(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        BRecordType detailRecordType = createAnonRecordType(errorFieldMatchPatterns.pos);
        List<BLangSimpleVariable> typeDefFields = new ArrayList<>();
        for (BLangNamedArgMatchPattern bindingPattern : errorFieldMatchPatterns.namedArgMatchPatterns) {
            Name fieldName = names.fromIdNode(bindingPattern.argName);
            BVarSymbol declaredVarSym = bindingPattern.declaredVars.get(fieldName.value);
            if (declaredVarSym == null) {
                // constant match pattern expr, not needed for detail record type
                continue;
            }
            BType fieldType = declaredVarSym.type;
            BVarSymbol fieldSym = new BVarSymbol(Flags.PUBLIC, fieldName, detailRecordType.tsymbol.pkgID, fieldType,
                    detailRecordType.tsymbol, bindingPattern.pos, VIRTUAL);
            detailRecordType.fields.put(fieldName.value, new BField(fieldName, bindingPattern.pos, fieldSym));
            detailRecordType.tsymbol.scope.define(fieldName, fieldSym);
            typeDefFields.add(ASTBuilderUtil.createVariable(null, fieldName.value, fieldType, null, fieldSym));
        }
        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(typeDefFields, detailRecordType,
                errorFieldMatchPatterns.pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper
                .createInitFunctionForRecordType(recordTypeNode, env, names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(detailRecordType, detailRecordType.tsymbol,
                recordTypeNode, env);
        return detailRecordType;
    }

    private BLangExpression createConditionForErrorFieldBindingPatterns(
            BLangErrorFieldBindingPatterns errorFieldBindingPatterns, BLangSimpleVarRef matchExprVarRef) {
        Location pos = errorFieldBindingPatterns.pos;
        BLangSimpleVariableDef resultVarDef = createVarDef("errorFieldResult$", symTable.booleanType, null, pos);
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVarDef.var.symbol);
        BLangBlockStmt mainBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        mainBlockStmt.addStatement(resultVarDef);

        if (errorFieldBindingPatterns.namedArgBindingPatterns.isEmpty()) {
            return createConditionForUnmatchedPattern(resultVarRef, mainBlockStmt);
        }

        BLangAssignment failureResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(false));
        BLangAssignment successResult =
                ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, getBooleanLiteral(true));
        mainBlockStmt.addStatement(failureResult);

        BLangExpression condition = createLiteral(pos, symTable.booleanType, true);
        for (int i = 0; i < errorFieldBindingPatterns.namedArgBindingPatterns.size(); i++) {
            BLangNamedArgBindingPattern namedArgBindingPattern =
                    errorFieldBindingPatterns.namedArgBindingPatterns.get(i);
            String argName = namedArgBindingPattern.argName.value;
            BLangBindingPattern bindingPattern = namedArgBindingPattern.bindingPattern;
            Location matchPatternPos = bindingPattern.pos;

            BLangFieldBasedAccess fieldBasedAccessExpr = getFieldAccessExpression(matchPatternPos, argName,
                    symTable.anydataOrReadonly, (BVarSymbol) matchExprVarRef.symbol);
            BLangSimpleVariableDef tempVarDef = createVarDef("$errorFieldVarTemp$" + i + "_$",
                    symTable.anydataOrReadonly, fieldBasedAccessExpr, matchPatternPos);
            mainBlockStmt.addStatement(tempVarDef);
            BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(matchPatternPos, tempVarDef.var.symbol);

            BLangExpression varCheckCondition = createConditionForNamedArgBindingPattern(bindingPattern, tempVarRef);
            BLangExpression typeCheckCondition = createIsLikeExpression(matchPatternPos, tempVarRef,
                    bindingPattern.getBType());
            varCheckCondition = ASTBuilderUtil.createBinaryExpr(pos, typeCheckCondition,
                    varCheckCondition, symTable.booleanType, OperatorKind.AND, null);
            if (i == 0) {
                condition = varCheckCondition;
                continue;
            }
            condition = ASTBuilderUtil.createBinaryExpr(matchPatternPos, condition, varCheckCondition,
                    symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));
        }

        BType matchingType = createMatchingRecordType(errorFieldBindingPatterns);
        BLangExpression isLikeExpr = createIsLikeExpression(errorFieldBindingPatterns.pos, matchExprVarRef,
                matchingType);
        condition = ASTBuilderUtil.createBinaryExpr(errorFieldBindingPatterns.pos, condition, isLikeExpr,
                symTable.booleanType, OperatorKind.AND, (BOperatorSymbol) symResolver
                        .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType, symTable.booleanType));

        BLangBlockStmt tempBLock = ASTBuilderUtil.createBlockStmt(pos);
        tempBLock.addStatement(successResult);
        BLangIf ifStmtForFieldMatchPatterns = ASTBuilderUtil.createIfElseStmt(pos, condition, tempBLock, null);
        mainBlockStmt.addStatement(ifStmtForFieldMatchPatterns);

        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(mainBlockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    private BType createMatchingRecordType(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
        BRecordType detailRecordType = createAnonRecordType(errorFieldBindingPatterns.pos);
        List<BLangSimpleVariable> typeDefFields = new ArrayList<>();
        for (BLangNamedArgBindingPattern bindingPattern : errorFieldBindingPatterns.namedArgBindingPatterns) {
            Name fieldName = names.fromIdNode(bindingPattern.argName);
            BVarSymbol declaredVarSym = bindingPattern.declaredVars.get(fieldName.value);
            if (declaredVarSym == null) {
                // constant match pattern expr, not needed for detail record type
                continue;
            }
            BType fieldType = declaredVarSym.type;
            BVarSymbol fieldSym = new BVarSymbol(Flags.PUBLIC, fieldName, detailRecordType.tsymbol.pkgID, fieldType,
                    detailRecordType.tsymbol, bindingPattern.pos, VIRTUAL);
            detailRecordType.fields.put(fieldName.value, new BField(fieldName, bindingPattern.pos, fieldSym));
            detailRecordType.tsymbol.scope.define(fieldName, fieldSym);
            typeDefFields.add(ASTBuilderUtil.createVariable(null, fieldName.value, fieldType, null, fieldSym));
        }
        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(typeDefFields, detailRecordType,
                errorFieldBindingPatterns.pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper
                .createInitFunctionForRecordType(recordTypeNode, env, names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(detailRecordType, detailRecordType.tsymbol,
                recordTypeNode, env);
        return detailRecordType;
    }

    private BLangExpression createConditionForNamedArgMatchPattern(BLangMatchPattern matchPattern,
                                                                   BLangSimpleVarRef matchExprVarRef) {
        return createVarCheckCondition(matchPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForNamedArgBindingPattern(BLangBindingPattern bindingPattern,
                                                                     BLangSimpleVarRef matchExprVarRef) {
        return createVarCheckCondition(bindingPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForErrorCauseMatchPattern(BLangErrorCauseMatchPattern errorCausePattern,
                                                                     BLangSimpleVarRef matchExprVarRef) {
        if (errorCausePattern.simpleMatchPattern != null) {
            return createConditionForSimpleMatchPattern(errorCausePattern.simpleMatchPattern, matchExprVarRef);
        }
        return createConditionForErrorMatchPattern(errorCausePattern.errorMatchPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForErrorCauseBindingPattern(BLangErrorCauseBindingPattern errorCausePattern,
                                                                       BLangSimpleVarRef matchExprVarRef) {
        if (errorCausePattern.simpleBindingPattern != null) {
            return createConditionForSimpleBindingPattern(errorCausePattern.simpleBindingPattern, matchExprVarRef);
        }
        return createConditionForErrorBindingPattern(errorCausePattern.errorBindingPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForErrorMessageMatchPattern(BLangErrorMessageMatchPattern errorMsgPattern,
                                                                       BLangSimpleVarRef matchExprVarRef) {
        return createConditionForSimpleMatchPattern(errorMsgPattern.simpleMatchPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForErrorMessageBindingPattern(
            BLangErrorMessageBindingPattern errorMsgPattern,
            BLangSimpleVarRef matchExprVarRef) {
        return createConditionForSimpleBindingPattern(errorMsgPattern.simpleBindingPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForSimpleMatchPattern(BLangSimpleMatchPattern simpleMatchPattern,
                                                                 BLangSimpleVarRef matchExprVarRef) {
        if (simpleMatchPattern.wildCardMatchPattern != null) {
            return createVarCheckCondition(simpleMatchPattern.wildCardMatchPattern, matchExprVarRef);
        }

        if (simpleMatchPattern.constPattern != null) {
            return createVarCheckCondition(simpleMatchPattern.constPattern, matchExprVarRef);
        }

        return createVarCheckCondition(simpleMatchPattern.varVariableName, matchExprVarRef);
    }

    private BLangExpression createConditionForSimpleBindingPattern(BLangSimpleBindingPattern simpleBindingPattern,
                                                                   BLangSimpleVarRef matchExprVarRef) {
        if (simpleBindingPattern.wildCardBindingPattern != null) {
            return createVarCheckCondition(simpleBindingPattern.wildCardBindingPattern, matchExprVarRef);
        }
        return createVarCheckCondition(simpleBindingPattern.captureBindingPattern, matchExprVarRef);
    }

    private BLangExpression createConditionForUnmatchedPattern(BLangSimpleVarRef resultVarRef,
                                                                  BLangBlockStmt blockStmt) {
        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(blockStmt,
                resultVarRef);
        statementExpression.setBType(symTable.booleanType);
        return statementExpression;
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (foreach.onFailClause != null) {
            BLangOnFailClause onFailClause = foreach.onFailClause;
            foreach.onFailClause = null;
            foreach.body.failureBreakMode = BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
            BLangDo doStmt = wrapStatementWithinDo(foreach.pos, foreach, onFailClause);
            result = rewrite(doStmt, env);
        } else {
            // We need to create a new variable for the expression as well. This is needed because integer ranges can be
            // added as the expression so we cannot get the symbol in such cases.
            BVarSymbol dataSymbol = new BVarSymbol(0, names.fromString("$data$"),
                                                   this.env.scope.owner.pkgID, foreach.collection.getBType(),
                                                   this.env.scope.owner, foreach.pos, VIRTUAL);
            BLangSimpleVariable dataVariable = ASTBuilderUtil.createVariable(foreach.pos, "$data$",
                                                                             foreach.collection.getBType(),
                                                                             foreach.collection, dataSymbol);
            BLangSimpleVariableDef dataVarDef = ASTBuilderUtil.createVariableDef(foreach.pos, dataVariable);

            BLangBlockStmt blockNode = desugarForeachStmt(dataVariable.symbol, foreach.collection.getBType(),
                    foreach, dataVarDef);

            // Rewrite the block.
            rewrite(blockNode, this.env);
            result = blockNode;
        }
    }

    BLangBlockStmt desugarForeachStmt(BVarSymbol collectionSymbol, BType collectionType, BLangForeach foreach,
                                      BLangSimpleVariableDef dataVarDef) {
        // Get the symbol of the variable (collection).
        switch (collectionType.tag) {
            case TypeTags.STRING:
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
            case TypeTags.XML:
            case TypeTags.XML_TEXT:
            case TypeTags.MAP:
            case TypeTags.TABLE:
            case TypeTags.STREAM:
            case TypeTags.RECORD:
                BInvokableSymbol iteratorSymbol = getLangLibIteratorInvokableSymbol(collectionSymbol);
                return desugarForeachWithIteratorDef(foreach, dataVarDef, collectionSymbol,
                        iteratorSymbol, true);
            case TypeTags.OBJECT: //We know for sure, the object is an iterable from TypeChecker phase.
                iteratorSymbol = getIterableObjectIteratorInvokableSymbol(collectionSymbol);
                return desugarForeachWithIteratorDef(foreach, dataVarDef, collectionSymbol,
                        iteratorSymbol, false);
            case TypeTags.TYPEREFDESC:
                return desugarForeachStmt(collectionSymbol, Types.getReferredType(foreach.collection.getBType()),
                        foreach, dataVarDef);
            default:
                BLangBlockStmt blockNode = ASTBuilderUtil.createBlockStmt(foreach.pos);
                blockNode.stmts.add(0, dataVarDef);
                return blockNode;
        }
    }

    @Override
    public void visit(BLangDo doNode) {
        BLangOnFailClause currentOnFailClause = this.onFailClause;
        analyzeOnFailClause(doNode.onFailClause, doNode.body);
        result = rewrite(doNode.body, this.env);
        swapAndResetEnclosingOnFail(currentOnFailClause);
    }

    private void swapAndResetEnclosingOnFail(BLangOnFailClause onFailClause) {
        this.enclosingOnFailClause.remove(onFailClause);
        this.onFailClause = onFailClause;
    }

    public void resetSkipFailStmtRewrite() {
        this.isVisitingQuery = false;
    }

    private void analyzeOnFailClause(BLangOnFailClause onFailClause, BLangBlockStmt blockStmt) {
        if (onFailClause != null) {
            this.enclosingOnFailClause.add(this.onFailClause);
            this.onFailClause = onFailClause;
            if (onFailClause.bodyContainsFail) {
                if (onFailClause.isInternal) {
                    blockStmt.failureBreakMode = BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
                } else {
                    blockStmt.failureBreakMode = BLangBlockStmt.FailureBreakMode.BREAK_WITHIN_BLOCK;
                }
            } else {
                rewrite(onFailClause, env);
            }
        }
    }

    private BLangFail createOnFailInvocation(BLangOnFailClause onFailClause, BLangFail fail) {
        BLangBlockStmt onFailBody = ASTBuilderUtil.createBlockStmt(onFailClause.pos);
        onFailBody.stmts.addAll(onFailClause.body.stmts);
        env.scope.entries.putAll(onFailClause.body.scope.entries);
        onFailBody.failureBreakMode = onFailClause.body.failureBreakMode;
        VariableDefinitionNode onFailVarDefNode = onFailClause.variableDefinitionNode;

        if (onFailVarDefNode != null) {
            BVarSymbol onFailErrorVariableSymbol =
                    ((BLangSimpleVariableDef) onFailVarDefNode).var.symbol;
            BLangSimpleVariable errorVar = ASTBuilderUtil.createVariable(onFailErrorVariableSymbol.pos,
                    onFailErrorVariableSymbol.name.value, onFailErrorVariableSymbol.type, rewrite(fail.expr, env),
                    onFailErrorVariableSymbol);
            BLangSimpleVariableDef errorVarDef = ASTBuilderUtil.createVariableDef(onFailClause.pos, errorVar);
            env.scope.define(onFailErrorVariableSymbol.name, onFailErrorVariableSymbol);
            onFailBody.stmts.add(0, errorVarDef);
        }

        if (onFailClause.isInternal && fail.exprStmt != null) {
            if (fail.exprStmt instanceof BLangPanic) {
                setPanicErrorToTrue(onFailBody, onFailClause);
            } else {
                onFailBody.stmts.add((BLangStatement) fail.exprStmt);
            }
        }

        BLangLiteral nilLiteral = ASTBuilderUtil.createLiteral(fail.pos, symTable.nilType, Names.NIL_VALUE);
        BLangStatementExpression statementExpression =
                createStatementExpression(onFailBody, nilLiteral);
        statementExpression.setBType(symTable.nilType);

        fail.exprStmt = rewrite(statementExpression, env);
        return fail;
    }

    private BLangBlockStmt rewriteNestedOnFail(BLangOnFailClause onFailClause, BLangFail fail) {
        BLangOnFailClause currentOnFail = this.onFailClause;

        BLangBlockStmt onFailBody = ASTBuilderUtil.createBlockStmt(onFailClause.pos);
        onFailBody.stmts.addAll(onFailClause.body.stmts);
        onFailBody.scope = onFailClause.body.scope;
        onFailBody.mapSymbol = onFailClause.body.mapSymbol;
        onFailBody.failureBreakMode = onFailClause.body.failureBreakMode;
        VariableDefinitionNode onFailVarDefNode = onFailClause.variableDefinitionNode;

        if (onFailVarDefNode != null) {
            BVarSymbol onFailErrorVariableSymbol =
                    ((BLangSimpleVariableDef) onFailVarDefNode).var.symbol;
            BLangSimpleVariable errorVar = ASTBuilderUtil.createVariable(onFailErrorVariableSymbol.pos,
                    onFailErrorVariableSymbol.name.value, onFailErrorVariableSymbol.type, rewrite(fail.expr, env),
                    onFailErrorVariableSymbol);
            BLangSimpleVariableDef errorVarDef = ASTBuilderUtil.createVariableDef(onFailClause.pos, errorVar);
            onFailBody.scope.define(onFailErrorVariableSymbol.name, onFailErrorVariableSymbol);
            onFailBody.stmts.add(0, errorVarDef);
        }

        int currentOnFailIndex = this.enclosingOnFailClause.indexOf(this.onFailClause);
        int enclosingOnFailIndex = currentOnFailIndex <= 0 ? this.enclosingOnFailClause.size() - 1
                : (currentOnFailIndex - 1);
        this.onFailClause = this.enclosingOnFailClause.get(enclosingOnFailIndex);
        onFailBody = rewrite(onFailBody, env);
        BLangFail failToEndBlock = new BLangFail();
        if (onFailClause.isInternal && fail.exprStmt != null) {
            if (fail.exprStmt instanceof BLangPanic) {
                setPanicErrorToTrue(onFailBody, onFailClause);
            } else {
                onFailBody.stmts.add((BLangStatement) fail.exprStmt);
            }
        }
        if (onFailClause.bodyContainsFail && !onFailClause.isInternal) {
            onFailBody.stmts.add(failToEndBlock);
        }
        this.onFailClause = currentOnFail;
        return onFailBody;
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        this.onFailClause = onFailClause;
        result = this.onFailClause;
    }

    private void setPanicErrorToTrue(BLangBlockStmt onfailBlock, BLangOnFailClause onFailClause) {
        BLangSimpleVarRef shouldPanic = enclosingShouldPanic.get(onFailClause);
        BLangAssignment assignment = ASTBuilderUtil.createAssignmentStmt(onFailClause.pos, shouldPanic,
                ASTBuilderUtil.createLiteral(onFailClause.pos, symTable.booleanType, true));
        onfailBlock.stmts.add(0, rewrite(assignment, env));
    }

    private BLangBlockStmt desugarForeachWithIteratorDef(BLangForeach foreach,
                                                         BLangSimpleVariableDef dataVariableDefinition,
                                                         BVarSymbol collectionSymbol,
                                                         BInvokableSymbol iteratorInvokableSymbol,
                                                         boolean isIteratorFuncFromLangLib) {
        BLangSimpleVariableDef iteratorVarDef = getIteratorVariableDefinition(foreach.pos, collectionSymbol,
                iteratorInvokableSymbol, isIteratorFuncFromLangLib);
        BLangBlockStmt blockNode = desugarForeachToWhile(foreach, iteratorVarDef);
        blockNode.stmts.add(0, dataVariableDefinition);
        return blockNode;
    }

    public BInvokableSymbol getIterableObjectIteratorInvokableSymbol(BVarSymbol collectionSymbol) {
        BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) Types.getReferredType(collectionSymbol.type).tsymbol;
        // We know for sure at this point, the object symbol should have the `iterator` method
        BAttachedFunction iteratorFunc = null;
        for (BAttachedFunction func : typeSymbol.attachedFuncs) {
            if (func.funcName.value.equals(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC)) {
                iteratorFunc = func;
                break;
            }
        }
        BAttachedFunction function = iteratorFunc;
        return function.symbol;
    }

    BInvokableSymbol getLangLibIteratorInvokableSymbol(BVarSymbol collectionSymbol) {
        return (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionSymbol.type,
                names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), env);
    }

    private BLangBlockStmt desugarForeachToWhile(BLangForeach foreach, BLangSimpleVariableDef varDef) {

        // We desugar the foreach statement to a while loop here.
        //
        // int[] data = [1, 2, 3];
        //
        // // Before desugaring.
        // foreach int i in data {
        //     io:println(i);
        // }
        //
        // ---------- After desugaring -------------
        //
        // int[] $data$ = data;
        //
        // abstract object {public function next() returns record {|int value;|}? $iterator$ = $data$.iterator();
        //
        // while {record {|int value;|}? $result$ = $iterator$.next(), $result} is record {|int value;|} {
        //     int i = $result$.value;
        //     ....
        //     [foreach node body]
        //     ....
        // }

        // Note - any $iterator$ = $data$.iterator();
        // -------------------------------------------------------------------

        // Note - $data$.iterator();

        BVarSymbol iteratorSymbol = varDef.var.symbol;

        // Create a new symbol for the $result$.
        BVarSymbol resultSymbol = new BVarSymbol(0, names.fromString("$result$"), this.env.scope.owner.pkgID,
                                                 foreach.nillableResultType, this.env.scope.owner, foreach.pos,
                                                 VIRTUAL);

        // Note - map<T>? $result$ = $iterator$.next();
        BLangSimpleVariableDef resultVariableDefinition = getIteratorNextVariableDefinition(foreach.pos,
                foreach.nillableResultType, iteratorSymbol, resultSymbol);

        // Note - {map<T>? $result$ = $iterator$.next(), $result}
        BLangSimpleVarRef resultReferenceInWhile = ASTBuilderUtil.createVariableRef(foreach.pos, resultSymbol);
        BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(
                resultVariableDefinition, resultReferenceInWhile);
        statementExpression.setBType(foreach.nillableResultType);

        // Note - $result != ()
        BLangType userDefineType = getUserDefineTypeNode(foreach.resultType);
        BLangTypeTestExpr typeTestExpr = ASTBuilderUtil
                .createTypeTestExpr(foreach.pos, statementExpression, userDefineType);
        // create while loop: while ($result$ != ())
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.pos = foreach.pos;
        whileNode.expr = typeTestExpr;
        whileNode.body = foreach.body;

        VariableDefinitionNode variableDefinitionNode = foreach.variableDefinitionNode;

        // Generate $result$.value
        // However, we are within the while loop. hence the $result$ can never be nil nor error.
        // Therefore cast $result$ to non-nilable type. i.e `int item = <>$result$.value;`
        BLangFieldBasedAccess valueAccessExpr = getValueAccessExpression(foreach.pos, foreach.varType, resultSymbol);

        BLangExpression expr = valueAccessExpr.expr;
        // Since `$result$` is always a record (with a single `value` field), add a cast to `map<any|error>`
        // to avoid casting to an anonymous type - https://github.com/ballerina-platform/ballerina-lang/issues/28262.
        valueAccessExpr.expr = addConversionExprIfRequired(expr, symTable.mapAllType);
        variableDefinitionNode.getVariable()
                .setInitialExpression(addConversionExprIfRequired(valueAccessExpr, foreach.varType));
        whileNode.body.stmts.add(0, (BLangStatement) variableDefinitionNode);

        // Create a new block statement node.
        BLangBlockStmt blockNode = ASTBuilderUtil.createBlockStmt(foreach.pos);

        // Add iterator variable to the block.
        blockNode.addStatement(varDef);

        // Add the while node to the block.
        blockNode.addStatement(whileNode);
        return blockNode;
    }

    private BLangType getUserDefineTypeNode(BType type) {
        BLangUserDefinedType recordType =
                new BLangUserDefinedType(ASTBuilderUtil.createIdentifier(null, ""),
                                         ASTBuilderUtil.createIdentifier(null, ""));
        recordType.setBType(type);
        return recordType;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (whileNode.onFailClause != null) {
            BLangOnFailClause onFailClause = whileNode.onFailClause;
            whileNode.onFailClause = null;
            whileNode.body.failureBreakMode = BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
            BLangDo doStmt = wrapStatementWithinDo(whileNode.pos, whileNode, onFailClause);
            result = rewrite(doStmt, env);
        } else {
            whileNode.expr = rewriteExpr(whileNode.expr);
            whileNode.body = rewrite(whileNode.body, env);
            result = whileNode;
        }
    }

    private BLangDo wrapStatementWithinDo(Location location, BLangStatement statement,
                                          BLangOnFailClause onFailClause) {
        BLangDo bLDo = (BLangDo) TreeBuilder.createDoNode();
        BLangBlockStmt doBlock = ASTBuilderUtil.createBlockStmt(location);
        doBlock.scope = new Scope(env.scope.owner);
        bLDo.body = doBlock;
        bLDo.pos = location;
        bLDo.onFailClause = onFailClause;
        bLDo.body.failureBreakMode = BLangBlockStmt.FailureBreakMode.BREAK_TO_OUTER_BLOCK;
        doBlock.stmts.add(statement);
        return bLDo;
    }

    @Override
    public void visit(BLangLock lockNode) {
        // Lock nodes will get desugared to below code
        // before desugar -
        //
        // lock {
        //      a = a + 7;
        // }
        //
        // after desugar -
        //
        // lock ;
        // var res = trap a = a + 7;
        // unlock ;
        // if (res is error) {
        //      panic res;
        // }
        BLangOnFailClause currentOnFailClause = this.onFailClause;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(lockNode.pos);
        if (lockNode.onFailClause != null) {
            blockStmt.failureBreakMode = BLangBlockStmt.FailureBreakMode.BREAK_TO_OUTER_BLOCK;
            rewrite(lockNode.onFailClause, env);
        }
        BLangLockStmt lockStmt = new BLangLockStmt(lockNode.pos);
        blockStmt.addStatement(lockStmt);

        enclLocks.push(lockStmt);

        BLangLiteral nilLiteral = ASTBuilderUtil.createLiteral(lockNode.pos, symTable.nilType, Names.NIL_VALUE);
        BType nillableError = BUnionType.create(null, symTable.errorType, symTable.nilType);
        BLangStatementExpression statementExpression = createStatementExpression(lockNode.body, nilLiteral);
        statementExpression.setBType(symTable.nilType);

        BLangTrapExpr trapExpr = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        trapExpr.setBType(nillableError);
        trapExpr.expr = statementExpression;
        BVarSymbol nillableErrorVarSymbol = new BVarSymbol(0, names.fromString("$errorResult"),
                                                           this.env.scope.owner.pkgID, nillableError,
                                                           this.env.scope.owner, lockNode.pos, VIRTUAL);
        BLangSimpleVariable simpleVariable = ASTBuilderUtil.createVariable(lockNode.pos, "$errorResult",
                                                                           nillableError, trapExpr,
                                                                           nillableErrorVarSymbol);
        BLangSimpleVariableDef simpleVariableDef = ASTBuilderUtil.createVariableDef(lockNode.pos, simpleVariable);
        blockStmt.addStatement(simpleVariableDef);

        BLangUnLockStmt unLockStmt = new BLangUnLockStmt(lockNode.pos);
        unLockStmt.relatedLock = lockStmt; // Used to find the related lock to unlock.
        blockStmt.addStatement(unLockStmt);
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(lockNode.pos, nillableErrorVarSymbol);

        BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(lockNode.pos);
        BLangPanic panicNode = (BLangPanic) TreeBuilder.createPanicNode();
        panicNode.pos = lockNode.pos;
        panicNode.expr = addConversionExprIfRequired(varRef, symTable.errorType);
        ifBody.addStatement(panicNode);

        BLangTypeTestExpr isErrorTest =
                ASTBuilderUtil.createTypeTestExpr(lockNode.pos, varRef, getErrorTypeNode());
        isErrorTest.setBType(symTable.booleanType);

        BLangIf ifelse = ASTBuilderUtil.createIfElseStmt(lockNode.pos, isErrorTest, ifBody, null);
        blockStmt.addStatement(ifelse);
        result = rewrite(blockStmt, env);
        enclLocks.pop();
        this.onFailClause = currentOnFailClause;
    }

    @Override
    public void visit(BLangLockStmt lockStmt) {
        result = lockStmt;
    }

    @Override
    public void visit(BLangUnLockStmt unLockStmt) {
        result = unLockStmt;
    }


    private BLangOnFailClause createTrxInternalOnFail(Location pos, BLangSimpleVarRef shouldPanicRef,
                                                      BLangSimpleVarRef shouldRetryRef) {
        BLangOnFailClause trxOnFailClause = (BLangOnFailClause) TreeBuilder.createOnFailClauseNode();
        trxOnFailClause.pos = pos;
        trxOnFailClause.body = ASTBuilderUtil.createBlockStmt(pos);
        trxOnFailClause.body.scope = new Scope(env.scope.owner);
        trxOnFailClause.isInternal = true;

        // on fail error $trxError$ {
        // }
        BVarSymbol trxOnFailErrorSym = new BVarSymbol(0, names.fromString("$trxError$"),
                env.scope.owner.pkgID, symTable.errorType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable trxOnFailError = ASTBuilderUtil.createVariable(pos,
                "$trxError$", symTable.errorType, null, trxOnFailErrorSym);
        trxOnFailClause.variableDefinitionNode = ASTBuilderUtil.createVariableDef(pos,
                trxOnFailError);
        trxOnFailClause.body.scope.define(trxOnFailErrorSym.name, trxOnFailErrorSym);

        // if (($trxError$ is error) && !($trxError$ is TransactionError) && transctional) {
        //     $shouldCleanUp$ = true;
        //     check panic rollback $trxError$;
        // }
        transactionDesugar.createRollbackIfFailed(pos, trxOnFailClause.body, trxOnFailErrorSym,
                trxBlockId, shouldRetryRef);

        BLangGroupExpr shouldNotPanic = new BLangGroupExpr();
        shouldNotPanic.setBType(symTable.booleanType);
        shouldNotPanic.expression = createNotBinaryExpression(pos, shouldPanicRef);

        BLangSimpleVarRef caughtError =  ASTBuilderUtil.createVariableRef(pos, trxOnFailErrorSym);

        BLangBlockStmt failBlock = ASTBuilderUtil.createBlockStmt(pos);

        BLangPanic panicNode = (BLangPanic) TreeBuilder.createPanicNode();
        panicNode.pos = pos;
        panicNode.expr = caughtError;

        // if(!$shouldPanic$) {
        //      fail $trxError$;
        // } else {
        //      panic $trxError$;
        // }
        BLangIf exitIf = ASTBuilderUtil.createIfElseStmt(pos, shouldNotPanic, failBlock, panicNode);
        trxOnFailClause.body.stmts.add(exitIf);

        BLangFail failStmt = (BLangFail) TreeBuilder.createFailNode();
        failStmt.pos = pos;
        failStmt.expr = caughtError;
        failBlock.stmts.add(failStmt);
        trxOnFailClause.bodyContainsFail = true;

        // at this point;
        // on fail error $trxError$ {
        //     if (($trxError$ is error) && !($trxError$ is TransactionError) && transactional) {
        //          $shouldCleanUp$ = true;
        //          check panic rollback $trxError$;
        //      }
        // }
        // if(!$shouldPanic$) {
        //      fail $trxError$;
        // } else {
        //      panic $trxError$;
        // }
        return trxOnFailClause;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (transactionNode.onFailClause != null) {
            //rewrite user defined on fail within a do statement
            BLangOnFailClause onFailClause = transactionNode.onFailClause;
            transactionNode.onFailClause = null;
            transactionNode.transactionBody.failureBreakMode = BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
            BLangDo doStmt = wrapStatementWithinDo(transactionNode.pos, transactionNode, onFailClause);
            // at this point;
            // do {
            //      transction {
            //        <Transaction Body>
            //      }
            // } on fail var e {
            //     (User Defined On Fail Clause)
            // }
            result = rewrite(doStmt, env);
        } else {
            BLangLiteral currentTrxBlockId = this.trxBlockId;
            String uniqueId = String.valueOf(++transactionBlockCount);
            this.trxBlockId = ASTBuilderUtil.createLiteral(transactionNode.pos, symTable.stringType, uniqueId);
            boolean currShouldReturnErrors = this.shouldReturnErrors;
            this.shouldReturnErrors = true;

            BLangOnFailClause currOnFailClause = this.onFailClause;

            // boolean $shouldPanic$ = false;
            BLangLiteral falseLiteral = ASTBuilderUtil.createLiteral(transactionNode.pos, symTable.booleanType, false);
            BVarSymbol shouldPanicVarSymbol = new BVarSymbol(0, names.fromString("$shouldPanic$"),
                    env.scope.owner.pkgID, symTable.booleanType, this.env.scope.owner, transactionNode.pos, VIRTUAL);
            shouldPanicVarSymbol.closure = true;
            BLangSimpleVariable shouldPanicVariable = ASTBuilderUtil.createVariable(transactionNode.pos,
                    "$shouldPanic$", symTable.booleanType, falseLiteral, shouldPanicVarSymbol);

            BLangSimpleVariableDef shouldPanicDef = ASTBuilderUtil.createVariableDef(transactionNode.pos,
                    shouldPanicVariable);
            BLangSimpleVarRef shouldPanicRef = ASTBuilderUtil.createVariableRef(transactionNode.pos,
                    shouldPanicVarSymbol);

            // on fail error $trxError$ {
            //     if (($trxError$ is error) && !($trxError$ is TransactionError) && transactional) {
            //          $shouldCleanUp$ = true;
            //          check panic rollback $trxError$;
            //      }
            // }
            // if(!$shouldPanic$) {
            //      fail $trxError$;
            // } else {
            //      panic $trxError$;
            // }
            BLangOnFailClause trxInternalOnFail = createTrxInternalOnFail(transactionNode.pos, shouldPanicRef,
                    this.shouldRetryRef);
            enclosingShouldPanic.put(trxInternalOnFail, shouldPanicRef);

            boolean userDefinedOnFailAvbl = this.onFailClause != null;
            analyzeOnFailClause(trxInternalOnFail, transactionNode.transactionBody);

            BLangBlockStmt transactionStmtBlock =
                    transactionDesugar.rewrite(transactionNode, trxBlockId, env, uniqueId);

            transactionStmtBlock.stmts.add(0, shouldPanicDef);
            transactionStmtBlock.scope.define(shouldPanicVarSymbol.name, shouldPanicVarSymbol);
            transactionStmtBlock.failureBreakMode = userDefinedOnFailAvbl ?
                    BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE :
                    BLangBlockStmt.FailureBreakMode.BREAK_TO_OUTER_BLOCK;

            // at this point;
            //
            // boolean $shouldPanic$ = false;
            // do {
            //      boolean $shouldCleanUp$ = false;
            //      transactions:Info? prevAttempt = ();
            //      string transactionId = "";
            //      error? $trapResult = trap {
            //                                  transactionId = startTransaction(1, prevAttempt)
            //                                  prevAttempt = info();
            //
            //                                  <Transaction Body>
            //                                 }
            //      if($trapResult$ is error) {
            //          $shouldPanic$ = true;
            //          error $trxError$ = <error> $trapResult$;
            //      }
            //      if ($shouldCleanUp$) {
            //          cleanupTransactionContext(1);
            //      }
            // } on fail error $trxError$ {
            //     if (($trxError$ is error) && !($trxError$ is TransactionError) && transactional) {
            //          $shouldCleanUp$ = true;
            //          check panic rollback $trxError$;
            //      }
            // }
            // if(!$shouldPanic$) {
            //      fail $trxError$;
            // } else {
            //      panic $trxError$;
            // }
            result = rewrite(transactionStmtBlock, this.env);

            this.shouldReturnErrors = currShouldReturnErrors;
            this.trxBlockId = currentTrxBlockId;
            swapAndResetEnclosingOnFail(currOnFailClause);
        }
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        BLangBlockStmt rollbackStmtExpr = transactionDesugar.desugar(rollbackNode, trxBlockId, this.shouldRetryRef);
        result = rewrite(rollbackStmtExpr, env);
    }

    private BLangOnFailClause createRetryInternalOnFail(Location pos,
                                                        BLangSimpleVarRef retryResultRef,
                                                        BLangSimpleVarRef retryManagerRef,
                                                        BLangSimpleVarRef shouldRetryRef,
                                                        BLangSimpleVarRef continueLoopRef,
                                                        BLangSimpleVarRef returnResult) {
        BLangOnFailClause internalOnFail = (BLangOnFailClause) TreeBuilder.createOnFailClauseNode();
        internalOnFail.pos = pos;
        internalOnFail.body = ASTBuilderUtil.createBlockStmt(pos);
        internalOnFail.body.scope = new Scope(env.scope.owner);

        BVarSymbol caughtErrorSym = new BVarSymbol(0, names.fromString("$caughtError$"),
                env.scope.owner.pkgID, symTable.errorType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable caughtError = ASTBuilderUtil.createVariable(pos,
                "$caughtError$", symTable.errorType, null, caughtErrorSym);
        internalOnFail.variableDefinitionNode = ASTBuilderUtil.createVariableDef(pos,
                caughtError);
        env.scope.define(caughtErrorSym.name, caughtErrorSym);
        BLangSimpleVarRef caughtErrorRef = ASTBuilderUtil.createVariableRef(pos, caughtErrorSym);

        // $retryResult$ = $caughtError$;
        BLangAssignment errorAssignment = ASTBuilderUtil.createAssignmentStmt(pos, retryResultRef, caughtErrorRef);
        internalOnFail.body.stmts.add(errorAssignment);

        //$continueLoop$ = true;
        BLangAssignment continueLoopTrue = ASTBuilderUtil.createAssignmentStmt(pos, continueLoopRef,
                ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true));
        internalOnFail.body.stmts.add(continueLoopTrue);

        // $shouldRetry$ = $retryManager$.shouldRetry();
        BLangInvocation shouldRetryInvocation = createRetryManagerShouldRetryInvocation(pos,
                retryManagerRef, caughtErrorRef);
        BLangAssignment shouldRetryAssignment = ASTBuilderUtil.createAssignmentStmt(pos, shouldRetryRef,
                shouldRetryInvocation);
        internalOnFail.body.stmts.add(shouldRetryAssignment);

        BLangGroupExpr shouldNotRetryCheck = new BLangGroupExpr();
        shouldNotRetryCheck.setBType(symTable.booleanType);
        shouldNotRetryCheck.expression = createNotBinaryExpression(pos, shouldRetryRef);

        BLangGroupExpr exitCheck = new BLangGroupExpr();
        exitCheck.setBType(symTable.booleanType);
        exitCheck.expression = shouldNotRetryCheck;

        BLangBlockStmt exitLogicBlock = ASTBuilderUtil.createBlockStmt(pos);
        BLangIf exitIf = ASTBuilderUtil.createIfElseStmt(pos, exitCheck, exitLogicBlock, null);

        if (this.onFailClause != null) {
            //adding fail statement to jump to enclosing on fail clause
            // fail $retryResult$;
            BLangFail failStmt = (BLangFail) TreeBuilder.createFailNode();
            failStmt.pos = pos;
            failStmt.expr = retryResultRef;

            exitLogicBlock.stmts.add(failStmt);
            internalOnFail.bodyContainsFail = true;
            internalOnFail.body.stmts.add(exitIf);

            //continue;
            BLangContinue loopContinueStmt = (BLangContinue) TreeBuilder.createContinueNode();
            loopContinueStmt.pos = pos;
            internalOnFail.body.stmts.add(loopContinueStmt);

            // if (!$shouldRetry$) {
            //      fail $retryResult$;
            // }
            // continue;
        } else {
            BLangAssignment returnErrorTrue = ASTBuilderUtil.createAssignmentStmt(pos, returnResult,
                    ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true));
            exitLogicBlock.stmts.add(returnErrorTrue);
            internalOnFail.body.stmts.add(exitIf);
            // if (!$shouldRetry$) {
            //      $returnErrorResult$ = true;
            // }
        }
        return internalOnFail;
    }

    BLangUnaryExpr createNotBinaryExpression(Location pos, BLangExpression expression) {
        List<BType> paramTypes = new ArrayList<>();
        paramTypes.add(symTable.booleanType);
        BInvokableType type = new BInvokableType(paramTypes, symTable.booleanType,
                null);
        BOperatorSymbol notOperatorSymbol = new BOperatorSymbol(
                names.fromString(OperatorKind.NOT.value()), symTable.rootPkgSymbol.pkgID, type, symTable.rootPkgSymbol,
                symTable.builtinPos, VIRTUAL);
        return ASTBuilderUtil.createUnaryExpr(pos, expression, symTable.booleanType,
                OperatorKind.NOT, notOperatorSymbol);
    }

    BLangLambdaFunction createLambdaFunction(Location pos, String functionNamePrefix,
                                             List<BLangSimpleVariable> lambdaFunctionVariable,
                                             TypeNode returnType, BLangFunctionBody lambdaBody) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        BLangFunction func =
                ASTBuilderUtil.createFunction(pos, functionNamePrefix + UNDERSCORE + lambdaFunctionCount++);
        lambdaFunction.function = func;
        func.requiredParams.addAll(lambdaFunctionVariable);
        func.setReturnTypeNode(returnType);
        func.desugaredReturnType = true;
        defineFunction(func, env.enclPkg);
        lambdaFunctionVariable = func.requiredParams;

        func.body = lambdaBody;
        func.desugared = false;
        lambdaFunction.pos = pos;
        List<BType> paramTypes = new ArrayList<>();
        lambdaFunctionVariable.forEach(variable -> paramTypes.add(variable.symbol.type));
        lambdaFunction.setBType(new BInvokableType(paramTypes, func.symbol.type.getReturnType(),
                                                   null));
        return lambdaFunction;
    }

    protected BLangLambdaFunction createLambdaFunction(Location pos, String functionNamePrefix,
                                                       List<BLangSimpleVariable> lambdaFunctionVariable,
                                                       TypeNode returnType, List<BLangStatement> fnBodyStmts,
                                                       SymbolEnv env, Scope bodyScope) {
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        body.scope = bodyScope;
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        body.stmts = rewriteStmt(fnBodyStmts, bodyEnv);
        return createLambdaFunction(pos, functionNamePrefix, lambdaFunctionVariable, returnType, body);
    }

    private void defineFunction(BLangFunction funcNode, BLangPackage targetPkg) {
        final BPackageSymbol packageSymbol = targetPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
         result = forkJoin;
    }

    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        if (Types.getReferredType(literalExpr.getBType()).tag == TypeTags.ARRAY) {
            // this is blob literal as byte array
            result = rewriteBlobLiteral(literalExpr);
            return;
        }
        result = literalExpr;
    }

    private BLangNode rewriteBlobLiteral(BLangLiteral literalExpr) {
        byte[] values = types.convertToByteArray((String) literalExpr.value);
        BLangArrayLiteral arrayLiteralNode = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        arrayLiteralNode.setBType(literalExpr.getBType());
        arrayLiteralNode.pos = literalExpr.pos;
        arrayLiteralNode.exprs = new ArrayList<>();
        for (byte b : values) {
            arrayLiteralNode.exprs.add(createByteLiteral(literalExpr.pos, b));
        }
        return arrayLiteralNode;
    }

    @Override
    public void visit(BLangListConstructorSpreadOpExpr listConstructorSpreadOpExpr) {
        listConstructorSpreadOpExpr.expr = rewriteExpr(listConstructorSpreadOpExpr.expr);
        result = listConstructorSpreadOpExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructor) {
        listConstructor.exprs = rewriteExprs(listConstructor.exprs);
        BLangExpression expr;
        BType listConstructorType = Types.getReferredType(listConstructor.getBType());
        if (listConstructorType.tag == TypeTags.TUPLE) {
            expr = new BLangTupleLiteral(listConstructor.pos, listConstructor.exprs, listConstructor.getBType());
            result = rewriteExpr(expr);
        } else if (listConstructorType.tag == TypeTags.JSON) {
            expr = new BLangJSONArrayLiteral(listConstructor.exprs, new BArrayType(listConstructor.getBType()));
            result = rewriteExpr(expr);
        } else if (getElementType(listConstructorType).tag == TypeTags.JSON) {
            expr = new BLangJSONArrayLiteral(listConstructor.exprs, listConstructor.getBType());
            result = rewriteExpr(expr);
        } else if (listConstructorType.tag == TypeTags.TYPEDESC) {
            final BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
            typedescExpr.resolvedType = listConstructor.typedescType;
            typedescExpr.setBType(symTable.typeDesc);
            result = rewriteExpr(typedescExpr);
        } else {
            expr = new BLangArrayLiteral(listConstructor.pos, listConstructor.exprs, listConstructor.getBType());
            result = rewriteExpr(expr);
        }
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        rewriteExprs(tableConstructorExpr.recordLiteralList);
        result = tableConstructorExpr;
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs = rewriteExprs(arrayLiteral.exprs);
        BType arrayLiteralType = Types.getReferredType(arrayLiteral.getBType());
        if (arrayLiteralType.tag == TypeTags.JSON) {
            result = new BLangJSONArrayLiteral(arrayLiteral.exprs, new BArrayType(arrayLiteral.getBType()));
            return;
        } else if (getElementType(arrayLiteralType).tag == TypeTags.JSON) {
            result = new BLangJSONArrayLiteral(arrayLiteral.exprs, arrayLiteral.getBType());
            return;
        }
        result = arrayLiteral;
    }

    @Override
    public void visit(BLangTupleLiteral tupleLiteral) {
        if (tupleLiteral.isTypedescExpr) {
            final BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
            typedescExpr.resolvedType = tupleLiteral.typedescType;
            typedescExpr.setBType(symTable.typeDesc);
            result = rewriteExpr(typedescExpr);
            return;
        }
        List<BLangExpression> exprs = tupleLiteral.exprs;
        BTupleType tupleType = (BTupleType) Types.getReferredType(tupleLiteral.getBType());
        List<BType> tupleMemberTypes = tupleType.tupleTypes;
        int tupleMemberTypeSize = tupleMemberTypes.size();
        int tupleExprSize = exprs.size();

        boolean isInRestType = false;
        int i = 0;
        for (BLangExpression expr: exprs) {
            if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                BType spreadOpType = ((BLangListConstructorSpreadOpExpr) expr).expr.getBType();
                spreadOpType = Types.getReferredType(spreadOpType);
                if (spreadOpType.tag == TypeTags.ARRAY) {
                    BArrayType spreadOpBArray = (BArrayType) spreadOpType;
                    if (spreadOpBArray.size >= 0) {
                        i += spreadOpBArray.size;
                        continue;
                    }
                } else {
                    BTupleType spreadOpTuple = (BTupleType) spreadOpType;
                    if (types.isFixedLengthTuple(spreadOpTuple)) {
                        i += spreadOpTuple.tupleTypes.size();
                        continue;
                    }
                }
                isInRestType = true;
                continue;
            }

            BType expType = expr.impConversionExpr == null ? expr.getBType() : expr.impConversionExpr.getBType();

            BType targetType = tupleType.restType;
            if (!isInRestType && i < tupleMemberTypeSize) {
                targetType = tupleMemberTypes.get(i);
            }

            types.setImplicitCastExpr(expr, expType, targetType);
            i++;
        }

        tupleLiteral.exprs = rewriteExprs(tupleLiteral.exprs);
        result = tupleLiteral;
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        result = rewriteExpr(groupExpr.expression);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        List<RecordLiteralNode.RecordField> fields =  recordLiteral.fields;
        fields.sort((v1, v2) -> Boolean.compare(isComputedKey(v1), isComputedKey(v2)));
        result = rewriteExpr(rewriteMappingConstructor(recordLiteral));
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        BLangSimpleVarRef genVarRefExpr = varRefExpr;

        // XML qualified name reference. e.g: ns0:foo
        if (varRefExpr.pkgSymbol != null && varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
            BLangXMLQName qnameExpr = new BLangXMLQName(varRefExpr.variableName);
            qnameExpr.nsSymbol = (BXMLNSSymbol) varRefExpr.pkgSymbol;
            qnameExpr.localname = varRefExpr.variableName;
            qnameExpr.prefix = varRefExpr.pkgAlias;
            qnameExpr.namespaceURI = qnameExpr.nsSymbol.namespaceURI;
            qnameExpr.isUsedInXML = false;
            qnameExpr.pos = varRefExpr.pos;
            qnameExpr.setBType(symTable.stringType);
            result = qnameExpr;
            return;
        }

        if (varRefExpr.symbol == null) {
            result = varRefExpr;
            return;
        }

        // Restore the original symbol
        if ((varRefExpr.symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) {
            BVarSymbol varSymbol = (BVarSymbol) varRefExpr.symbol;
            if (varSymbol.originalSymbol != null) {
                varRefExpr.symbol = varSymbol.originalSymbol;
            }
        }

        BType type = varRefExpr.getBType();

        BSymbol ownerSymbol = varRefExpr.symbol.owner;
        if ((varRefExpr.symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION &&
                Types.getReferredType(varRefExpr.symbol.type).tag == TypeTags.INVOKABLE) {
            genVarRefExpr = new BLangFunctionVarRef((BVarSymbol) varRefExpr.symbol);
        } else if ((varRefExpr.symbol.tag & SymTag.TYPE) == SymTag.TYPE &&
                !((varRefExpr.symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT)) {
            genVarRefExpr = new BLangTypeLoad(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE ||
                (ownerSymbol.tag & SymTag.FUNCTION_TYPE) == SymTag.FUNCTION_TYPE ||
                (ownerSymbol.tag & SymTag.LET) == SymTag.LET) {
            // Local variable in a function/resource/action/worker/let
            genVarRefExpr = new BLangLocalVarRef((BVarSymbol) varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.STRUCT) == SymTag.STRUCT) {
            genVarRefExpr = new BLangFieldVarRef((BVarSymbol) varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) {

            // TODO: The following condition can be removed once constant propagation for mapping constructor
            // TODO: is resolved #issue-21127
            if ((varRefExpr.symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
                BConstantSymbol constSymbol = (BConstantSymbol) varRefExpr.symbol;
                BType referredType = Types.getReferredType(constSymbol.literalType);
                if (referredType.tag <= TypeTags.BOOLEAN || referredType.tag == TypeTags.NIL) {
                    BLangLiteral literal = ASTBuilderUtil.createLiteral(varRefExpr.pos, constSymbol.literalType,
                            constSymbol.value.value);
                    result = rewriteExpr(addConversionExprIfRequired(literal, varRefExpr.getBType()));
                    return;
                }
            }

            // Package variable | service variable.
            // We consider both of them as package level variables.
            genVarRefExpr = new BLangPackageVarRef((BVarSymbol) varRefExpr.symbol);

            if (!enclLocks.isEmpty()) {
                BVarSymbol symbol = (BVarSymbol) varRefExpr.symbol;
                BLangLockStmt lockStmt = enclLocks.peek();
                lockStmt.addLockVariable(symbol);
                lockStmt.addLockVariable(this.globalVariablesDependsOn.getOrDefault(symbol, new HashSet<>()));
            }
        }

        genVarRefExpr.setBType(type);
        genVarRefExpr.pos = varRefExpr.pos;

        if ((varRefExpr.isLValue)
                || genVarRefExpr.symbol.name.equals(IGNORE)) { //TODO temp fix to get this running in bvm
            genVarRefExpr.isLValue = varRefExpr.isLValue;
            genVarRefExpr.setBType(varRefExpr.symbol.type);
            result = genVarRefExpr;
            return;
        }

        // If the the variable is not used in lhs, then add a conversion if required.
        // This is done to make the types compatible for narrowed types.
        genVarRefExpr.isLValue = varRefExpr.isLValue;
        BType targetType = genVarRefExpr.getBType();
        genVarRefExpr.setBType(genVarRefExpr.symbol.type);
        BLangExpression expression = addConversionExprIfRequired(genVarRefExpr, targetType);
        result = expression.impConversionExpr != null ? expression.impConversionExpr : expression;
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        rewriteFieldBasedAccess(nsPrefixedFieldBasedAccess);
    }

    private void rewriteFieldBasedAccess(BLangFieldBasedAccess fieldAccessExpr) {
        if (safeNavigate(fieldAccessExpr)) {
            result = rewriteExpr(rewriteSafeNavigationExpr(fieldAccessExpr));
            return;
        }

        BLangAccessExpression targetVarRef = fieldAccessExpr;

        // First get the type and then visit the expr. Order matters, since the desugar
        // can change the type of the expression, if it is type narrowed.
        BType varRefType = types.getTypeWithEffectiveIntersectionTypes(fieldAccessExpr.expr.getBType());
        fieldAccessExpr.expr = rewriteExpr(fieldAccessExpr.expr);
        if (!types.isSameType(fieldAccessExpr.expr.getBType(), varRefType)) {
            fieldAccessExpr.expr = addConversionExprIfRequired(fieldAccessExpr.expr, varRefType);
        }

        BLangLiteral stringLit = createStringLiteral(fieldAccessExpr.field.pos,
                StringEscapeUtils.unescapeJava(fieldAccessExpr.field.value));
        BType refType = Types.getReferredType(varRefType);
        int varRefTypeTag = refType.tag;
        if (varRefTypeTag == TypeTags.OBJECT ||
                (varRefTypeTag == TypeTags.UNION &&
                        Types.getReferredType(
                                ((BUnionType) refType).getMemberTypes().iterator().next()).tag == TypeTags.OBJECT)) {
            if (fieldAccessExpr.symbol != null && fieldAccessExpr.symbol.type.tag == TypeTags.INVOKABLE &&
                    ((fieldAccessExpr.symbol.flags & Flags.ATTACHED) == Flags.ATTACHED)) {
                result = rewriteObjectMemberAccessAsField(fieldAccessExpr);
                return;
            } else {
                boolean isStoreOnCreation = fieldAccessExpr.isStoreOnCreation;

                if (!isStoreOnCreation && varRefTypeTag == TypeTags.OBJECT && env.enclInvokable != null) {
                    BInvokableSymbol originalFuncSymbol = ((BLangFunction) env.enclInvokable).originalFuncSymbol;
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) refType.tsymbol;
                    BAttachedFunction initializerFunc = objectTypeSymbol.initializerFunc;
                    BAttachedFunction generatedInitializerFunc = objectTypeSymbol.generatedInitializerFunc;

                    if ((generatedInitializerFunc != null && originalFuncSymbol == generatedInitializerFunc.symbol) ||
                            (initializerFunc != null && originalFuncSymbol == initializerFunc.symbol)) {
                        isStoreOnCreation = true;
                    }
                }

                targetVarRef = new BLangStructFieldAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit,
                        (BVarSymbol) fieldAccessExpr.symbol, false,
                        isStoreOnCreation);
                // Only supporting object field lock at the moment
            }
        } else if (varRefTypeTag == TypeTags.RECORD ||
                (varRefTypeTag == TypeTags.UNION &&
                        ((BUnionType) refType).getMemberTypes().iterator().next().tag == TypeTags.RECORD)) {
            if (fieldAccessExpr.symbol != null && fieldAccessExpr.symbol.type.tag == TypeTags.INVOKABLE
                    && ((fieldAccessExpr.symbol.flags & Flags.ATTACHED) == Flags.ATTACHED)) {
                targetVarRef = new BLangStructFunctionVarRef(fieldAccessExpr.expr, (BVarSymbol) fieldAccessExpr.symbol);
            } else {
                targetVarRef = new BLangStructFieldAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit,
                        (BVarSymbol) fieldAccessExpr.symbol, false, fieldAccessExpr.isStoreOnCreation);
            }
        } else if (types.isLax(refType)) {
            if (!(refType.tag == TypeTags.XML || refType.tag == TypeTags.XML_ELEMENT)) {
                if (refType.tag == TypeTags.MAP && TypeTags.isXMLTypeTag(((BMapType) refType).constraint.tag)) {
                    result = rewriteExpr(rewriteLaxMapAccess(fieldAccessExpr));
                    return;
                }
                // Handle unions of lax types such as json|map<json>,
                // by casting to json and creating a BLangJSONAccessExpr.
                fieldAccessExpr.expr = addConversionExprIfRequired(fieldAccessExpr.expr, symTable.jsonType);
                targetVarRef = new BLangJSONAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit);
            } else {
                BLangInvocation xmlAccessInvocation = rewriteXMLAttributeOrElemNameAccess(fieldAccessExpr);
                xmlAccessInvocation.setBType(fieldAccessExpr.getBType());
                result = xmlAccessInvocation;
                return;
            }
        } else if (varRefTypeTag == TypeTags.MAP) {
            // TODO: 7/1/19 remove once foreach field access usage is removed.
            targetVarRef = new BLangMapAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit,
                    fieldAccessExpr.isStoreOnCreation);
        } else if (TypeTags.isXMLTypeTag(varRefTypeTag)) {
            targetVarRef = new BLangXMLAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit,
                    fieldAccessExpr.fieldKind);
        }

        targetVarRef.isLValue = fieldAccessExpr.isLValue;
        targetVarRef.setBType(fieldAccessExpr.getBType());
        targetVarRef.optionalFieldAccess = fieldAccessExpr.optionalFieldAccess;
        result = targetVarRef;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        rewriteFieldBasedAccess(fieldAccessExpr);
    }

    private BLangNode rewriteObjectMemberAccessAsField(BLangFieldBasedAccess fieldAccessExpr) {
        Location pos = fieldAccessExpr.pos;
        BInvokableSymbol originalMemberFuncSymbol = (BInvokableSymbol) fieldAccessExpr.symbol;
        // Can we cache this?
        BLangFunction func = (BLangFunction) TreeBuilder.createFunctionNode();
        String funcName = "$anon$method$delegate$" + originalMemberFuncSymbol.name.value + "$" + lambdaFunctionCount++;
        BInvokableSymbol funcSymbol = new BInvokableSymbol(SymTag.INVOKABLE, (Flags.ANONYMOUS | Flags.LAMBDA),
                                                           Names.fromString(funcName), env.enclPkg.packageID,
                                                           originalMemberFuncSymbol.type, env.scope.owner, pos,
                                                           VIRTUAL);
        funcSymbol.retType = originalMemberFuncSymbol.retType;
        funcSymbol.bodyExist = true;
        funcSymbol.params = new ArrayList<>();
        funcSymbol.scope = new Scope(funcSymbol);
        func.pos = pos;
        func.name = createIdentifier(pos, funcName);
        func.flagSet.add(Flag.LAMBDA);
        func.flagSet.add(Flag.ANONYMOUS);
        func.body = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        func.symbol = funcSymbol;
        func.setBType(funcSymbol.type);
        func.closureVarSymbols = new LinkedHashSet<>();
        // When we are supporting non var ref exprs we need to create a def, assign, get the ref and use it here.
        BLangExpression receiver = fieldAccessExpr.expr;
        // This is used to keep the tempary var def, when the receiver is a expression we need to have a
        // vardef in encl invocable and we can cosider that receiver is taken as a closure var.
        BLangSimpleVariableDef intermediateObjDef = null;
        if (receiver.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BSymbol receiverSymbol = ((BLangVariableReference) receiver).symbol;
            receiverSymbol.closure = true;
            func.closureVarSymbols.add(new ClosureVarSymbol(receiverSymbol, pos));
        } else {
            BLangSimpleVariableDef varDef = createVarDef("$$temp$obj$" + annonVarCount++, receiver.getBType(),
                                                         receiver, pos);
            intermediateObjDef = varDef;
            varDef.var.symbol.closure = true;
            env.scope.define(varDef.var.symbol.name, varDef.var.symbol);
            BLangSimpleVarRef variableRef = createVariableRef(pos, varDef.var.symbol);
            func.closureVarSymbols.add(new ClosureVarSymbol(varDef.var.symbol, pos));
            receiver = variableRef;
        }

        // todo: handle taint table; issue: https://github.com/ballerina-platform/ballerina-lang/issues/25962

        ArrayList<BLangExpression> requiredArgs = new ArrayList<>();
        for (BVarSymbol param : originalMemberFuncSymbol.params) {
            BLangSimpleVariable fParam = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            fParam.symbol = new BVarSymbol(0, param.name, env.enclPkg.packageID, param.type,  funcSymbol, pos,
                    VIRTUAL);
            fParam.pos = pos;
            fParam.name = createIdentifier(pos, param.name.value);
            fParam.setBType(param.type);
            func.requiredParams.add(fParam);
            funcSymbol.params.add(fParam.symbol);
            funcSymbol.scope.define(fParam.symbol.name, fParam.symbol);

            BLangSimpleVarRef paramRef = createVariableRef(pos, fParam.symbol);
            requiredArgs.add(paramRef);
        }

        ArrayList<BLangExpression> restArgs = new ArrayList<>();
        if (originalMemberFuncSymbol.restParam != null) {
            BLangSimpleVariable restParam = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            func.restParam = restParam;
            BVarSymbol restSym = originalMemberFuncSymbol.restParam;
            restParam.name = ASTBuilderUtil.createIdentifier(pos, restSym.name.value);
            restParam.symbol = new BVarSymbol(0, restSym.name, env.enclPkg.packageID, restSym.type, funcSymbol, pos,
                    VIRTUAL);
            restParam.pos = pos;
            restParam.setBType(restSym.type);
            funcSymbol.restParam = restParam.symbol;
            funcSymbol.scope.define(restParam.symbol.name, restParam.symbol);

            BLangSimpleVarRef restArg = createVariableRef(pos, restParam.symbol);
            restArgs.add(createRestArgsExpression(restArg));
        }

        BLangIdentifier field = fieldAccessExpr.field;
        BLangReturn retStmt = (BLangReturn) TreeBuilder.createReturnNode();
        retStmt.expr = createObjectMethodInvocation(
                receiver, field, fieldAccessExpr.symbol, requiredArgs, restArgs);
        ((BLangBlockFunctionBody) func.body).addStatement(retStmt);

        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaFunction.function = func;
        lambdaFunction.capturedClosureEnv = env.createClone();
        env.enclPkg.functions.add(func);
        env.enclPkg.topLevelNodes.add(func);
        //env.enclPkg.lambdaFunctions.add(lambdaFunction);
        lambdaFunction.parent = env.enclInvokable;
        lambdaFunction.setBType(func.getBType());

        if (intermediateObjDef == null) {
            return rewrite(lambdaFunction, env);
        } else {
            BLangStatementExpression expr = createStatementExpression(intermediateObjDef, rewrite(lambdaFunction, env));
            expr.setBType(lambdaFunction.getBType());
            return rewrite(expr, env);
        }
    }

    private BLangInvocation createObjectMethodInvocation(BLangExpression receiver, BLangIdentifier field,
                                                         BSymbol invocableSymbol,
                                                         List<BLangExpression> requiredArgs,
                                                         List<BLangExpression> restArgs) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.name = field;
        invocationNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        invocationNode.expr = receiver;

        invocationNode.symbol = invocableSymbol;
        invocationNode.setBType(((BInvokableType) invocableSymbol.type).retType);
        invocationNode.requiredArgs = requiredArgs;
        invocationNode.restArgs = restArgs;
        return invocationNode;
    }

    private BLangStatementExpression rewriteLaxMapAccess(BLangFieldBasedAccess fieldAccessExpr) {
        BLangStatementExpression statementExpression = new BLangStatementExpression();
        BLangBlockStmt block = new BLangBlockStmt();
        statementExpression.stmt = block;
        BUnionType fieldAccessType = BUnionType.create(null, fieldAccessExpr.getBType(), symTable.errorType);
        Location pos = fieldAccessExpr.pos;
        BLangSimpleVariableDef result = createVarDef("$mapAccessResult$", fieldAccessType, null, pos);
        block.addStatement(result);
        BLangSimpleVarRef resultRef = ASTBuilderUtil.createVariableRef(pos, result.var.symbol);
        resultRef.setBType(fieldAccessType);
        statementExpression.setBType(fieldAccessType);


        // create map access expr to get the field from it.
        // if it's nil, then return error, else return xml value or what ever the map content is
        BLangLiteral mapIndex = ASTBuilderUtil.createLiteral(
                        fieldAccessExpr.field.pos, symTable.stringType, fieldAccessExpr.field.value);
        BLangMapAccessExpr mapAccessExpr = new BLangMapAccessExpr(pos, fieldAccessExpr.expr, mapIndex);
        BUnionType xmlOrNil = BUnionType.create(null, fieldAccessExpr.getBType(), symTable.nilType);
        mapAccessExpr.setBType(xmlOrNil);
        BLangSimpleVariableDef mapResult = createVarDef("$mapAccess", xmlOrNil, mapAccessExpr, pos);
        BLangSimpleVarRef mapResultRef = ASTBuilderUtil.createVariableRef(pos, mapResult.var.symbol);
        block.addStatement(mapResult);

        BLangIf ifStmt = ASTBuilderUtil.createIfStmt(pos, block);

        BLangIsLikeExpr isLikeNilExpr = createIsLikeExpression(pos, mapResultRef, symTable.nilType);

        ifStmt.expr = isLikeNilExpr;
        BLangBlockStmt resultNilBody = new BLangBlockStmt();
        ifStmt.body = resultNilBody;
        BLangBlockStmt resultHasValueBody = new BLangBlockStmt();
        ifStmt.elseStmt = resultHasValueBody;

        BLangErrorConstructorExpr errorConstructorExpr =
                (BLangErrorConstructorExpr) TreeBuilder.createErrorConstructorExpressionNode();
        BSymbol symbol = symResolver.lookupMainSpaceSymbolInPackage(errorConstructorExpr.pos, env,
                names.fromString(""), names.fromString("error"));
        errorConstructorExpr.setBType(symbol.type);

        List<BLangExpression> positionalArgs = new ArrayList<>();
        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        positionalArgs.add(createStringLiteral(pos, "{" + RuntimeConstants.MAP_LANG_LIB + "}InvalidKey"));
        BLangNamedArgsExpression message = new BLangNamedArgsExpression();
        message.name = ASTBuilderUtil.createIdentifier(pos, "key");
        message.expr = createStringLiteral(pos, fieldAccessExpr.field.value);
        namedArgs.add(message);
        errorConstructorExpr.positionalArgs = positionalArgs;
        errorConstructorExpr.namedArgs = namedArgs;

        BLangSimpleVariableDef errorDef =
                createVarDef("$_invalid_key_error", symTable.errorType, errorConstructorExpr, pos);
        resultNilBody.addStatement(errorDef);

        BLangSimpleVarRef errorRef = ASTBuilderUtil.createVariableRef(pos, errorDef.var.symbol);

        BLangAssignment errorVarAssignment = ASTBuilderUtil.createAssignmentStmt(pos, resultNilBody);
        errorVarAssignment.varRef = resultRef;
        errorVarAssignment.expr = errorRef;

        BLangAssignment mapResultAssignment = ASTBuilderUtil.createAssignmentStmt(
                pos, resultHasValueBody);
        mapResultAssignment.varRef = resultRef;
        mapResultAssignment.expr = mapResultRef;

        statementExpression.expr = resultRef;
        return statementExpression;
    }

    private BLangInvocation rewriteXMLAttributeOrElemNameAccess(BLangFieldBasedAccess fieldAccessExpr) {
        ArrayList<BLangExpression> args = new ArrayList<>();

        String fieldName = fieldAccessExpr.field.value;
        if (fieldAccessExpr.fieldKind == FieldKind.WITH_NS) {
            BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixAccess =
                    (BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) fieldAccessExpr;
            fieldName = createExpandedQName(nsPrefixAccess.nsSymbol.namespaceURI, fieldName);
        }

        // Handle element name access.
        if (fieldName.equals("_")) {
            return createLanglibXMLInvocation(fieldAccessExpr.pos, XML_INTERNAL_GET_ELEMENT_NAME_NIL_LIFTING,
                    fieldAccessExpr.expr, new ArrayList<>(), new ArrayList<>());
        }

        BLangLiteral attributeNameLiteral = createStringLiteral(fieldAccessExpr.field.pos, fieldName);
        args.add(attributeNameLiteral);
        args.add(isOptionalAccessToLiteral(fieldAccessExpr));

        return createLanglibXMLInvocation(fieldAccessExpr.pos, XML_INTERNAL_GET_ATTRIBUTE, fieldAccessExpr.expr, args,
                new ArrayList<>());
    }

    private BLangExpression isOptionalAccessToLiteral(BLangFieldBasedAccess fieldAccessExpr) {
        return rewrite(
                createLiteral(fieldAccessExpr.pos, symTable.booleanType, fieldAccessExpr.isOptionalFieldAccess()), env);
    }

    private String createExpandedQName(String nsURI, String localName) {
        return "{" + nsURI + "}" + localName;
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (safeNavigate(indexAccessExpr)) {
            result = rewriteExpr(rewriteSafeNavigationExpr(indexAccessExpr));
            return;
        }

        BLangIndexBasedAccess targetVarRef = indexAccessExpr;
        indexAccessExpr.indexExpr = rewriteExpr(indexAccessExpr.indexExpr);

        // First get the type and then visit the expr. Order matters, since the desugar
        // can change the type of the expression, if it is type narrowed.
        BType effectiveType = types.getTypeWithEffectiveIntersectionTypes(indexAccessExpr.expr.getBType());
        BType varRefType = Types.getReferredType(effectiveType);
        indexAccessExpr.expr = rewriteExpr(indexAccessExpr.expr);
        if (!types.isSameType(indexAccessExpr.expr.getBType(), varRefType)) {
            indexAccessExpr.expr = addConversionExprIfRequired(indexAccessExpr.expr, varRefType);
        }

        if (varRefType.tag == TypeTags.MAP) {
            targetVarRef = new BLangMapAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                                                  indexAccessExpr.indexExpr, indexAccessExpr.isStoreOnCreation);
        } else if (types.isSubTypeOfMapping(types.getSafeType(varRefType, true, false))) {
            targetVarRef = new BLangStructFieldAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                                                          indexAccessExpr.indexExpr,
                                                          (BVarSymbol) indexAccessExpr.symbol, false);
        } else if (types.isSubTypeOfList(varRefType)) {
            targetVarRef = new BLangArrayAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                                                    indexAccessExpr.indexExpr);
        } else if (TypeTags.isXMLTypeTag(varRefType.tag)) {
            targetVarRef = new BLangXMLAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        } else if (types.isAssignable(varRefType, symTable.stringType)) {
            indexAccessExpr.expr = addConversionExprIfRequired(indexAccessExpr.expr, symTable.stringType);
            targetVarRef = new BLangStringAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                                                     indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.TABLE) {
            targetVarRef = new BLangTableAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        }

        targetVarRef.isLValue = indexAccessExpr.isLValue;
        targetVarRef.setBType(indexAccessExpr.getBType());
        result = targetVarRef;
    }

    @Override
    public void visit(BLangInvocation iExpr) {
        BLangExpression invocation = rewriteInvocation(iExpr, false);
        if (invocation.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            ((BLangTypeConversionExpr) invocation).expr =
                                        createStmtExpr((BLangInvocation) ((BLangTypeConversionExpr) invocation).expr);
            result = invocation;
        } else {
            result = createStmtExpr((BLangInvocation) invocation);
        }
    }

    private BLangStatementExpression createStmtExpr(BLangInvocation invocation) {
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(invocation.pos);
        BInvokableTypeSymbol invokableTypeSymbol;
        BType type = invocation.symbol.type;
        if (type.getKind() == TypeKind.TYPEREFDESC) {
            invokableTypeSymbol = (BInvokableTypeSymbol) ((BTypeReferenceType) type).referredType.tsymbol;
        } else {
            invokableTypeSymbol = (BInvokableTypeSymbol) type.tsymbol;
        }
        if (invokableTypeSymbol == null) {
            BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, invocation);
            stmtExpr.setBType(invocation.getBType());
            return stmtExpr;
        }
        TreeMap<String, BLangExpression> arguments = new TreeMap<>();
        Map<String, BInvokableSymbol> defaultValues = invokableTypeSymbol.defaultValues;

        for (int i = 0; i < invokableTypeSymbol.params.size(); i++) {
            BLangExpression arg;
            BLangSimpleVariableDef variableDef;
            BLangSimpleVarRef simpleVarRef;
            if (invocation instanceof BLangInvocation.BLangAttachedFunctionInvocation) {
                arg = invocation.requiredArgs.get(i + 1);
            } else {
                arg = invocation.requiredArgs.get(i);
            }
            BVarSymbol param = invokableTypeSymbol.params.get(i);
            String paramName = param.name.value;
            if (arg.getKind() != NodeKind.IGNORE_EXPR) {
                if (invocation.expr == arg) {
                    arguments.put(paramName, arg);
                    continue;
                }
                if (arg.impConversionExpr != null) {
                    variableDef = createSimpleVarDef("$" + paramName + "$" + funcParamCount++, param.type, arg);
                } else {
                    variableDef = createSimpleVarDef("$" + paramName + "$" + funcParamCount++, arg.getBType(), arg);
                }
                simpleVarRef = ASTBuilderUtil.createVariableRef(invocation.pos, variableDef.var.symbol);
                simpleVarRef = rewrite(simpleVarRef, env);
                blockStmt.addStatement(variableDef);
                arguments.put(paramName, simpleVarRef);
                if (invocation instanceof BLangInvocation.BLangAttachedFunctionInvocation) {
                    invocation.requiredArgs.set(i + 1, simpleVarRef);
                } else {
                    invocation.requiredArgs.set(i, simpleVarRef);
                }
                continue;
            }

            BInvokableSymbol invokableSymbol = defaultValues.get(paramName);
            BLangInvocation closureInvocation = getInvocation(invokableSymbol);
            for (int m = 0; m < invokableSymbol.params.size(); m++) {
                String langLibFuncParam = invokableSymbol.params.get(m).name.value;
                closureInvocation.requiredArgs.add(arguments.get(langLibFuncParam));
            }
            variableDef = createVarDef("$" + paramName + "$" + funcParamCount++, closureInvocation.getBType(),
                                       closureInvocation, arg.pos);
            simpleVarRef = ASTBuilderUtil.createVariableRef(invocation.pos, variableDef.var.symbol);
            simpleVarRef = rewrite(simpleVarRef, env);
            blockStmt.addStatement(variableDef);
            arguments.put(paramName, simpleVarRef);
            if (invocation instanceof BLangInvocation.BLangAttachedFunctionInvocation) {
                invocation.requiredArgs.set(i + 1, simpleVarRef);
            } else {
                invocation.requiredArgs.set(i, simpleVarRef);
            }
        }
        BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, invocation);
        stmtExpr.setBType(invocation.getBType());

        return stmtExpr;
    }

    private BLangInvocation getInvocation(BInvokableSymbol symbol) {
        BLangInvocation funcInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        funcInvocation.setBType(symbol.retType);
        funcInvocation.symbol = symbol;
        funcInvocation.name = ASTBuilderUtil.createIdentifier(symbol.pos, symbol.name.value);
        return visitFunctionPointerInvocation(funcInvocation);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        if (errorConstructorExpr.positionalArgs.size() == 1) {
            errorConstructorExpr.positionalArgs.add(createNilLiteral());
        }
        errorConstructorExpr.positionalArgs.set(1,
                addConversionExprIfRequired(errorConstructorExpr.positionalArgs.get(1), symTable.errorType));
        rewriteExprs(errorConstructorExpr.positionalArgs);

        BLangExpression errorDetail;
        BLangRecordLiteral recordLiteral = ASTBuilderUtil.createEmptyRecordLiteral(errorConstructorExpr.pos,
                ((BErrorType) Types.getReferredType(errorConstructorExpr.getBType())).detailType);
        if (errorConstructorExpr.namedArgs.isEmpty()) {
            errorDetail = visitCloneReadonly(rewriteExpr(recordLiteral), recordLiteral.getBType());
        } else {
            for (BLangNamedArgsExpression namedArg : errorConstructorExpr.namedArgs) {
                BLangRecordLiteral.BLangRecordKeyValueField member = new BLangRecordLiteral.BLangRecordKeyValueField();
                member.key = new BLangRecordLiteral.BLangRecordKey(ASTBuilderUtil.createLiteral(namedArg.name.pos,
                        symTable.stringType, namedArg.name.value));

                if (Types.getReferredType(recordLiteral.getBType()).tag == TypeTags.RECORD) {
                    member.valueExpr = addConversionExprIfRequired(namedArg.expr, symTable.anyType);
                } else {
                    member.valueExpr = addConversionExprIfRequired(namedArg.expr, namedArg.expr.getBType());
                }
                recordLiteral.fields.add(member);
            }
            errorDetail = visitCloneReadonly(rewriteExpr(recordLiteral),
                    ((BErrorType) Types.getReferredType(errorConstructorExpr.getBType())).detailType);
        }
        errorConstructorExpr.errorDetail = errorDetail;
        result = errorConstructorExpr;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocation) {
        if (!actionInvocation.async && actionInvocation.invokedInsideTransaction) {
            transactionDesugar.startTransactionCoordinatorOnce(env, actionInvocation.pos);
        }

        // Add `@strand {thread: "any"}` annotation to an isolated start-action or
        // isolated named worker declaration.
        if (actionInvocation.async && Symbols.isFlagOn(actionInvocation.symbol.type.flags, Flags.ISOLATED)) {
            addStrandAnnotationWithThreadAny(actionInvocation);
        }

        BLangExpression invocation = rewriteInvocation(actionInvocation, actionInvocation.async);
        if (invocation.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            ((BLangTypeConversionExpr) invocation).expr =
                                        createStmtExpr((BLangInvocation) ((BLangTypeConversionExpr) invocation).expr);
            result = invocation;
        } else {
            result = createStmtExpr((BLangInvocation) invocation);
        }
    }

    private void addStrandAnnotationWithThreadAny(BLangInvocation.BLangActionInvocation actionInvocation) {
        if (this.strandAnnotAttachement == null) {
            BLangPackage pkgNode = env.enclPkg;
            List<BLangTypeDefinition> prevTypeDefinitions = new ArrayList<>(pkgNode.typeDefinitions);
            // Create strand annotation once and reuse it for all isolated start-actions and named workers.
            this.strandAnnotAttachement =
                    annotationDesugar.createStrandAnnotationWithThreadAny(actionInvocation.pos, env);
            // Add init function for record type node in type def introduced for internally added strand annotation.
            addInitFunctionForRecordTypeNodeInTypeDef(pkgNode, prevTypeDefinitions);
        }

        actionInvocation.addAnnotationAttachment(this.strandAnnotAttachement);
        ((BInvokableSymbol) actionInvocation.symbol)
                .addAnnotation(this.strandAnnotAttachement.annotationAttachmentSymbol);
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        // `BLangResourceAccessInvocation` will be desugared into a `BLangInvocation`
        // ex: 1
        // Target resource function
        // resource function get [string a]/[int b](string c) returns int {
        //      return 3;
        // }
        //
        // before desugar -
        // a->/path/[1].get("hello")
        //
        // after desugar -
        // a.get("path", 1, "hello")
        //
        // ex: 2
        // Target resource function
        // resource function get4 path/[int a]/[int... b](string c) returns int {
        //      return 3;
        // }
        //
        // before desugar -
        // a->/path/[...list].get("hello")
        //
        // after desugar -
        // a.get4(list[0], list.slice(1), "hello")

        if (resourceAccessInvocation.invokedInsideTransaction) {
            transactionDesugar.startTransactionCoordinatorOnce(env, resourceAccessInvocation.pos);
        }

        // Create virtual invocation to reorder resource path parameters
        BLangInvocation pathParamInvocation = createInvocationForPathParams(resourceAccessInvocation);
        reorderArguments(pathParamInvocation);

        BResourceFunction targetResourceFunc = resourceAccessInvocation.targetResourceFunc;
        List<BResourcePathSegmentSymbol> pathSegmentSymbols = targetResourceFunc.pathSegmentSymbols;

        int pathParamInvocationRequiredArgCount = pathParamInvocation.requiredArgs.size();

        BLangInvocation bLangInvocation = new BLangInvocation();

        // After reordering we have one to one mapping from resource access segments to resource path segments
        // We need to select only the arguments that are required as path parameters from the pathParamInvocation and
        // add them to final bLangInvocation.
        // In the case that the rest arg provides value(requiredArg) for a required parameter,
        // `reorderArguments` logic will create statement expression which contains a virtual variable referring to
        // rest arg inside that value.
        // If that value is not needed by the path parameters, we need to make sure that virtual variable is
        // included in an argument that is needed by path parameters. Otherwise we will get NPE in later stages.
        BLangStatementExpression firstRequiredArgFromRestArg = null;
        boolean isFirstRequiredArgFromRestArgIncluded = false;
        for (int i = 0; i < pathParamInvocationRequiredArgCount; i++) {
            BLangExpression requiredArg = pathParamInvocation.requiredArgs.get(i);
            // Resource path size is always >= pathParamInvocationRequiredArgCount
            Name resourcePathName = pathSegmentSymbols.get(i).name;
            if (firstRequiredArgFromRestArg == null && requiredArg.getKind() == NodeKind.STATEMENT_EXPRESSION) {
                firstRequiredArgFromRestArg = (BLangStatementExpression) requiredArg;
                if (resourcePathName.value.equals("^")) {
                    isFirstRequiredArgFromRestArgIncluded = true;
                    bLangInvocation.requiredArgs.add(requiredArg);
                    continue;
                }
            }

            if (resourcePathName.value.equals("^")) {
                if (firstRequiredArgFromRestArg != null && !isFirstRequiredArgFromRestArgIncluded) {
                    BLangStatementExpression statementExpression = new BLangStatementExpression();
                    statementExpression.expr = requiredArg;
                    statementExpression.stmt = firstRequiredArgFromRestArg.stmt;
                    statementExpression.setBType(requiredArg.getBType());
                    bLangInvocation.requiredArgs.add(statementExpression);
                    isFirstRequiredArgFromRestArgIncluded = true;
                } else {
                    bLangInvocation.requiredArgs.add(requiredArg);
                }
            }
        }

        Name lastResourcePathName = pathSegmentSymbols.get(pathSegmentSymbols.size() - 1).name;
        if (lastResourcePathName.value.equals("^^")) {
            // After reordering pathParamInvocation.restArgs size will always be 0 or 1
            for (BLangExpression restArg : pathParamInvocation.restArgs) {
                if (firstRequiredArgFromRestArg != null && !isFirstRequiredArgFromRestArgIncluded &&
                        restArg.getKind() == NodeKind.STATEMENT_EXPRESSION) {
                    BLangStatementExpression restArgStmtExpr = (BLangStatementExpression) restArg;
                    ((BLangBlockStmt) restArgStmtExpr.stmt).stmts.add(0,
                            ((BLangBlockStmt) firstRequiredArgFromRestArg.stmt).stmts.get(0));
                }
                bLangInvocation.requiredArgs.add(restArg);
            }
        }

        bLangInvocation.requiredArgs.addAll(resourceAccessInvocation.requiredArgs);
        bLangInvocation.pkgAlias = resourceAccessInvocation.pkgAlias;
        bLangInvocation.name = resourceAccessInvocation.name;
        bLangInvocation.expr = resourceAccessInvocation.expr;
        bLangInvocation.restArgs = resourceAccessInvocation.restArgs;
        bLangInvocation.symbol = resourceAccessInvocation.symbol;
        bLangInvocation.setBType(resourceAccessInvocation.getBType());
        bLangInvocation.parent = resourceAccessInvocation.parent;
        bLangInvocation.pos = resourceAccessInvocation.pos;

        result = rewriteExpr(bLangInvocation);
    }

    private BLangInvocation createInvocationForPathParams(
            BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        // This method will create an invocation in which all the resource path types will be a parameter of the
        // invokable symbol and all the path segments will be the arguments.
        // ex: 1
        // Target resource function
        // resource function get path/[int b](string c) returns int {
        //      return 3;
        // }
        // a->/path/[1].get("hello")
        //
        // Generated invokable symbol params: ("path" _, int _)
        // Generated invocation requiredArgs: ("path", 1)
        //
        // ex:2
        // Target resource function
        // resource function post books/["books"... a]() returns string[] {
        //      return a;
        // }
        // "books"[2] booksArray = ["books", "books"];
        // string[] b7 = myClient->/[...booksArray].post;
        //
        // Generated invokable symbol params: ("books" _)
        // Generated invokable symbol restParam: "books"[]
        // Generated invocation restArgs: BLangRestArgsExpression booksArray
        BLangInvocation bLangInvocation = new BLangInvocation();

        BInvokableSymbol invokableSymbol = new BInvokableSymbol(
                resourceAccessInvocation.symbol.tag,
                resourceAccessInvocation.symbol.flags,
                resourceAccessInvocation.symbol.name,
                resourceAccessInvocation.symbol.pkgID,
                resourceAccessInvocation.symbol.type,
                resourceAccessInvocation.symbol,
                resourceAccessInvocation.symbol.pos, VIRTUAL);

        BResourceFunction targetResourceFunc = resourceAccessInvocation.targetResourceFunc;
        List<BResourcePathSegmentSymbol> pathSegmentSymbols = targetResourceFunc.pathSegmentSymbols;
        List<BLangExpression> resourceAccessPathSegments = resourceAccessInvocation.resourceAccessPathSegments.exprs;

        List<BVarSymbol> invocationParams = new ArrayList<>(pathSegmentSymbols.size());

        int pathSegmentCount = pathSegmentSymbols.size();
        BResourcePathSegmentSymbol lastPathSegmentSym = pathSegmentSymbols.get(pathSegmentSymbols.size() - 1);
        if (lastPathSegmentSym.kind == SymbolKind.RESOURCE_PATH_REST_PARAM_SEGMENT) {
            invokableSymbol.restParam = new BVarSymbol(0, Names.EMPTY, this.env.scope.owner.pkgID,
                    new BArrayType(lastPathSegmentSym.type), this.env.scope.owner, lastPathSegmentSym.pos, VIRTUAL);
            pathSegmentCount--;
        }

        if (pathSegmentCount > 0 && lastPathSegmentSym.kind != SymbolKind.RESOURCE_ROOT_PATH_SEGMENT) {
            invocationParams.addAll(pathSegmentSymbols.subList(0, pathSegmentCount).stream()
                    .map(s -> new BVarSymbol(0, Names.EMPTY, this.env.scope.owner.pkgID, s.type, 
                            this.env.scope.owner, s.pos, VIRTUAL)).collect(Collectors.toList()));
        }

        invokableSymbol.params = invocationParams;

        bLangInvocation.symbol = invokableSymbol;

        for (int i = 0;  i < resourceAccessPathSegments.size(); i++) {
            BLangExpression resourceAccessPathSeg = resourceAccessPathSegments.get(i);
            if (resourceAccessPathSeg.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                bLangInvocation.restArgs.add(createRestArgsExpression(
                        ((BLangListConstructorSpreadOpExpr) resourceAccessPathSeg).expr));
            } else if (i > invocationParams.size() - 1) {
                bLangInvocation.restArgs.add(resourceAccessPathSeg);
            } else {
                bLangInvocation.requiredArgs.add(resourceAccessPathSeg);
            }
        }

        return bLangInvocation;
    }

    private BLangRestArgsExpression createRestArgsExpression(BLangExpression expr) {
        BLangRestArgsExpression bLangRestArgsExpression = new BLangRestArgsExpression();
        bLangRestArgsExpression.expr = expr;
        bLangRestArgsExpression.pos = expr.pos;
        bLangRestArgsExpression.setBType(expr.getBType());
        bLangRestArgsExpression.expectedType = bLangRestArgsExpression.getBType();
        return bLangRestArgsExpression;
    }

    private BLangExpression rewriteInvocation(BLangInvocation invocation, boolean async) {
        BLangInvocation invRef = invocation;

        if (!enclLocks.isEmpty()) {
            BLangLockStmt lock = enclLocks.peek();
            lock.lockVariables.addAll(((BInvokableSymbol) invocation.symbol).dependentGlobalVars);
        }

        // Reorder the arguments to match the original function signature.
        reorderArguments(invocation);

        rewriteExprs(invocation.requiredArgs);
        fixStreamTypeCastsInInvocationParams(invocation);
        fixNonRestArgTypeCastInTypeParamInvocation(invocation);

        rewriteExprs(invocation.restArgs);

        annotationDesugar.defineStatementAnnotations(invocation.annAttachments, invocation.pos,
                                                     invocation.symbol.pkgID, invocation.symbol.owner, env);

        if (invocation.functionPointerInvocation) {
            return visitFunctionPointerInvocation(invocation);
        }
        result = invRef;

        BInvokableSymbol invSym = (BInvokableSymbol) invocation.symbol;
        if (Symbols.isFlagOn(invSym.retType.flags, Flags.PARAMETERIZED)) {
            BType retType = unifier.build(invSym.retType);
            invocation.setBType(invocation.async ? new BFutureType(TypeTags.FUTURE, retType, null) : retType);
        }

        if (invocation.expr == null) {
            BLangExpression expression = fixTypeCastInTypeParamInvocation(invocation, invRef);
            if (invocation.exprSymbol == null) {
                return expression;
            }
            invocation.expr = ASTBuilderUtil.createVariableRef(invocation.pos, invocation.exprSymbol);
            invocation.expr = rewriteExpr(invocation.expr);
        }
        switch (Types.getReferredType(invocation.expr.getBType()).tag) {
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
                if (!invocation.langLibInvocation) {
                    invocation.expr = rewriteExpr(invocation.expr);
                    List<BLangExpression> argExprs = new ArrayList<>(invocation.requiredArgs);
                    argExprs.add(0, invocation.expr);
                    BLangAttachedFunctionInvocation attachedFunctionInvocation =
                            new BLangAttachedFunctionInvocation(invocation.pos, argExprs, invocation.restArgs,
                                                                invocation.symbol, invocation.getBType(),
                                                                invocation.expr, async);
                    attachedFunctionInvocation.name = invocation.name;
                    attachedFunctionInvocation.annAttachments = invocation.annAttachments;
                    invRef = attachedFunctionInvocation;
                }
                break;
        }
        populateOCEInvocation(invocation, invRef);
        return fixTypeCastInTypeParamInvocation(invocation, invRef);
    }

    private void populateOCEInvocation(BLangInvocation invocation,
                                       BLangInvocation invRef) {
        if (invocation.objectInitMethod && Symbols.isFlagOn(invocation.expr.getBType().flags, Flags.OBJECT_CTOR)) {
            BObjectType initializingObject = (BObjectType) invocation.expr.getBType();
            BLangClassDefinition classDef = initializingObject.classDef;
            if (classDef.hasClosureVars) {
                OCEDynamicEnvironmentData oceEnvData = initializingObject.classDef.oceEnvData;
                if (oceEnvData.attachedFunctionInvocation == null) {
                    oceEnvData.attachedFunctionInvocation = (BLangAttachedFunctionInvocation) invRef;
                }
            }
        }
    }

    private void fixNonRestArgTypeCastInTypeParamInvocation(BLangInvocation iExpr) {
        if (!iExpr.langLibInvocation) {
            return;
        }

        List<BLangExpression> requiredArgs = iExpr.requiredArgs;

        List<BVarSymbol> params = ((BInvokableSymbol) iExpr.symbol).params;

        for (int i = 0; i < requiredArgs.size(); i++) {
            requiredArgs.set(i, addConversionExprIfRequired(requiredArgs.get(i), params.get(i).type));
        }
    }

    /* This function is a workaround and need improvement
    *  Notes for improvement :
    *    1. Both arguments are same.
    *    2. Due to current type param logic we put type param flag on the original type.
    *    3. Error type having Cloneable type with type param flag, change expression type by this code.
    *    4. using error type is a problem as Cloneable type is an typeparm eg: ExprBodiedFunctionTest
    *       added never to CloneableType type param
    *       @typeParam type
    *       CloneableType Cloneable|never;
    *
    */
    private BLangExpression fixTypeCastInTypeParamInvocation(BLangInvocation iExpr, BLangInvocation genIExpr) {
        var returnTypeOfInvokable = ((BInvokableSymbol) iExpr.symbol).retType;
        if (!iExpr.langLibInvocation && !TypeParamAnalyzer.containsTypeParam(returnTypeOfInvokable)) {
            return genIExpr;
        }

        // why we dont consider whole action invocation
        BType originalInvType = genIExpr.getBType();
        if (!genIExpr.async) {
            genIExpr.setBType(returnTypeOfInvokable);
        }
        return addConversionExprIfRequired(genIExpr, originalInvType);
    }

    private void fixStreamTypeCastsInInvocationParams(BLangInvocation iExpr) {
        List<BLangExpression> requiredArgs = iExpr.requiredArgs;
        List<BVarSymbol> params = ((BInvokableSymbol) iExpr.symbol).params;
        if (!params.isEmpty()) {
            for (int i = 0; i < requiredArgs.size(); i++) {
                BVarSymbol param = params.get(i);
                if (Types.getReferredType(param.type).tag == TypeTags.STREAM) {
                    requiredArgs.set(i, addConversionExprIfRequired(requiredArgs.get(i), param.type));
                }
            }
        }
    }

    private BLangLiteral createNilLiteral() {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = null;
        literal.setBType(symTable.nilType);
        return literal;
    }

    public void visit(BLangTypeInit typeInitExpr) {
        if (Types.getReferredType(typeInitExpr.getBType()).tag == TypeTags.STREAM) {
            result = rewriteExpr(desugarStreamTypeInit(typeInitExpr));
        } else {
            result = rewrite(desugarObjectTypeInit(typeInitExpr), env);
        }
    }

    private BLangStatementExpression desugarObjectTypeInit(BLangTypeInit typeInitExpr) {
        typeInitExpr.desugared = true;
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(typeInitExpr.pos);

        // Person $obj$ = new;
        BLangInvocation initInvocation = (BLangInvocation) typeInitExpr.initInvocation;
        initInvocation.objectInitMethod = true;
        BType objType = getObjectType(typeInitExpr.getBType());
        BLangSimpleVariableDef objVarDef = createVarDef("$obj$", objType, typeInitExpr, typeInitExpr.pos);
        // var $temp$ = $obj$.init();
        BLangSimpleVariableDef initInvRetValVarDef = createVarDef("$temp$", initInvocation.getBType(),
                                                                  initInvocation, initInvocation.pos);
        objVarDef.var.name.pos = symTable.builtinPos;
        BLangSimpleVarRef objVarRef = ASTBuilderUtil.createVariableRef(typeInitExpr.pos, objVarDef.var.symbol);
        BLangSimpleVarRef objInitVarRef = ASTBuilderUtil.createVariableRef(initInvocation.pos,
                                                                           initInvRetValVarDef.var.symbol);
        blockStmt.addStatement(objVarDef);
        blockStmt.addStatement(initInvRetValVarDef);
        initInvocation.exprSymbol = objVarDef.var.symbol;
        initInvocation.symbol =
                ((BObjectTypeSymbol) Types.getReferredType(objType).tsymbol).generatedInitializerFunc.symbol;

        // init() returning nil is the common case and the type test is not needed for it.
        if (initInvocation.getBType().tag == TypeTags.NIL) {
            initInvocation.name.value = GENERATED_INIT_SUFFIX.value;
            BLangNode parent = initInvocation.parent;
            if (parent != null && parent.getKind() == NodeKind.OBJECT_CTOR_EXPRESSION) {
                BLangObjectConstructorExpression oceExpression = (BLangObjectConstructorExpression) parent;
                OCEDynamicEnvironmentData oceData = oceExpression.classNode.oceEnvData;
                oceData.initInvocation = typeInitExpr.initInvocation;
            }
            typeInitExpr.initInvocation = objInitVarRef;
            BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, objVarRef);
            stmtExpr.setBType(objVarRef.symbol.type);
            return stmtExpr;
        }

        // Person|error $result$;
        BLangSimpleVariableDef resultVarDef = createVarDef("$result$", typeInitExpr.getBType(), null, typeInitExpr.pos);
        blockStmt.addStatement(resultVarDef);

        // if ($temp$ is error) {
        //      $result$ = $temp$;
        // } else {
        //      $result$ = $obj$;
        // }

        // Condition
        BLangSimpleVarRef initRetValVarRefInCondition =
                ASTBuilderUtil.createVariableRef(symTable.builtinPos, initInvRetValVarDef.var.symbol);
        BLangBlockStmt thenStmt = ASTBuilderUtil.createBlockStmt(symTable.builtinPos);
        BLangTypeTestExpr isErrorTest =
                ASTBuilderUtil.createTypeTestExpr(symTable.builtinPos, initRetValVarRefInCondition, getErrorTypeNode());
        isErrorTest.setBType(symTable.booleanType);

        // If body
        BLangSimpleVarRef thenInitRetValVarRef =
                ASTBuilderUtil.createVariableRef(symTable.builtinPos, initInvRetValVarDef.var.symbol);
        BLangSimpleVarRef thenResultVarRef =
                ASTBuilderUtil.createVariableRef(symTable.builtinPos, resultVarDef.var.symbol);
        BLangAssignment errAssignment =
                ASTBuilderUtil.createAssignmentStmt(symTable.builtinPos, thenResultVarRef, thenInitRetValVarRef);
        thenStmt.addStatement(errAssignment);

        // Else body
        BLangSimpleVarRef elseResultVarRef =
                ASTBuilderUtil.createVariableRef(symTable.builtinPos, resultVarDef.var.symbol);
        BLangAssignment objAssignment =
                ASTBuilderUtil.createAssignmentStmt(symTable.builtinPos, elseResultVarRef, objVarRef);
        BLangBlockStmt elseStmt = ASTBuilderUtil.createBlockStmt(symTable.builtinPos);
        elseStmt.addStatement(objAssignment);

        BLangIf ifelse = ASTBuilderUtil.createIfElseStmt(symTable.builtinPos, isErrorTest, thenStmt, elseStmt);
        blockStmt.addStatement(ifelse);

        BLangSimpleVarRef resultVarRef =
                ASTBuilderUtil.createVariableRef(symTable.builtinPos, resultVarDef.var.symbol);
        BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, resultVarRef);
        stmtExpr.setBType(resultVarRef.symbol.type);
        return stmtExpr;
    }

    private BLangInvocation desugarStreamTypeInit(BLangTypeInit typeInitExpr) {
        BInvokableSymbol symbol = (BInvokableSymbol) symTable.langInternalModuleSymbol.scope
                .lookup(Names.CONSTRUCT_STREAM).symbol;

        BStreamType referredStreamType = (BStreamType) Types.getReferredType(typeInitExpr.getBType());
        BType constraintType = referredStreamType.constraint;
        BType constraintTdType = new BTypedescType(constraintType, symTable.typeDesc.tsymbol);
        BLangTypedescExpr constraintTdExpr = new BLangTypedescExpr();
        constraintTdExpr.resolvedType = constraintType;
        constraintTdExpr.setBType(constraintTdType);

        BType completionType = referredStreamType.completionType;
        BType completionTdType = new BTypedescType(completionType, symTable.typeDesc.tsymbol);
        BLangTypedescExpr completionTdExpr = new BLangTypedescExpr();
        completionTdExpr.resolvedType = completionType;
        completionTdExpr.setBType(completionTdType);

        List<BLangExpression> args = new ArrayList<>(Lists.of(constraintTdExpr, completionTdExpr));
        if (!typeInitExpr.argsExpr.isEmpty()) {
            args.add(typeInitExpr.argsExpr.get(0));
        }
        BLangInvocation streamConstructInvocation = ASTBuilderUtil.createInvocationExprForMethod(
                typeInitExpr.pos, symbol, args, symResolver);
        streamConstructInvocation.setBType(new BStreamType(TypeTags.STREAM, constraintType, completionType, null));
        return streamConstructInvocation;
    }

    private BLangSimpleVariableDef createSimpleVarDef(String name, BType type, BLangExpression expr) {
        BVarSymbol varSymbol = new BVarSymbol(0, Names.fromString(name), this.env.scope.owner.pkgID, type,
                                              this.env.scope.owner, expr.pos, VIRTUAL);
        BLangSimpleVariable simpleVariable = ASTBuilderUtil.createVariable(expr.pos, name, type, expr, varSymbol);
        BLangSimpleVariableDef simpleVariableDef = ASTBuilderUtil.createVariableDef(expr.pos);
        simpleVariableDef.var = simpleVariable;
        simpleVariableDef.setBType(simpleVariable.getBType());
        return simpleVariableDef;
    }

    private BLangSimpleVariableDef createVarDef(String name, BType type, BLangExpression expr, Location location) {
        BSymbol objSym = symResolver.lookupSymbolInMainSpace(env, names.fromString(name));
        // todo: check and remove this bit here
        if (objSym == null || objSym == symTable.notFoundSymbol) {
            objSym = new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID, type,
                                    this.env.scope.owner, location, VIRTUAL);
        }
        BLangSimpleVariable objVar = ASTBuilderUtil.createVariable(location, name, type, expr, (BVarSymbol) objSym);
        BLangSimpleVariableDef objVarDef = ASTBuilderUtil.createVariableDef(location);
        objVarDef.var = objVar;
        objVarDef.setBType(objVar.getBType());
        return objVarDef;
    }

    private BType getObjectType(BType bType) {
        BType type = Types.getReferredType(bType);
        if (type.tag == TypeTags.OBJECT) {
            return bType;
        } else if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream()
                    .filter(t -> Types.getReferredType(t).tag == TypeTags.OBJECT)
                    .findFirst()
                    .orElse(symTable.noType);
        }

        throw new IllegalStateException("None object type '" + type.toString() + "' found in object init context");
    }

    BLangErrorType getErrorTypeNode() {
        BLangErrorType errorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorTypeNode.setBType(symTable.errorType);
        errorTypeNode.pos = symTable.builtinPos;
        return errorTypeNode;
    }

    BLangErrorType getErrorOrNillTypeNode() {
        BLangErrorType errorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorTypeNode.setBType(symTable.errorOrNilType);
        return errorTypeNode;
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        /*
         * First desugar to if-else:
         *
         * T $result$;
         * if () {
         *    $result$ = thenExpr;
         * } else {
         *    $result$ = elseExpr;
         * }
         *
         */
        BLangSimpleVariableDef resultVarDef =
                createVarDef("$ternary_result$", ternaryExpr.getBType(), null, ternaryExpr.pos);
        BLangBlockStmt thenBody = ASTBuilderUtil.createBlockStmt(ternaryExpr.pos);
        BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(ternaryExpr.pos);

        // Create then assignment
        BLangSimpleVarRef thenResultVarRef = ASTBuilderUtil.createVariableRef(ternaryExpr.pos, resultVarDef.var.symbol);
        BLangAssignment thenAssignment =
                ASTBuilderUtil.createAssignmentStmt(ternaryExpr.pos, thenResultVarRef, ternaryExpr.thenExpr);
        thenBody.addStatement(thenAssignment);

        // Create else assignment
        BLangSimpleVarRef elseResultVarRef = ASTBuilderUtil.createVariableRef(ternaryExpr.pos, resultVarDef.var.symbol);
        BLangAssignment elseAssignment =
                ASTBuilderUtil.createAssignmentStmt(ternaryExpr.pos, elseResultVarRef, ternaryExpr.elseExpr);
        elseBody.addStatement(elseAssignment);

        // Then make it a expression-statement, with expression being the $result$
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(ternaryExpr.pos, resultVarDef.var.symbol);
        BLangIf ifElse = ASTBuilderUtil.createIfElseStmt(ternaryExpr.pos, ternaryExpr.expr, thenBody, elseBody);

        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(ternaryExpr.pos, Lists.of(resultVarDef, ifElse));
        BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, resultVarRef);
        stmtExpr.setBType(ternaryExpr.getBType());

        result = rewriteExpr(stmtExpr);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        // Wait for any
        if (waitExpr.getExpression().getKind() == NodeKind.BINARY_EXPR) {
            waitExpr.exprList = collectAllBinaryExprs((BLangBinaryExpr) waitExpr.getExpression(), new ArrayList<>());
        } else { // Wait for one
            waitExpr.exprList = Collections.singletonList(rewriteExpr(waitExpr.getExpression()));
        }
        result = waitExpr;
    }

    private List<BLangExpression> collectAllBinaryExprs(BLangBinaryExpr binaryExpr, List<BLangExpression> exprs) {
        visitBinaryExprOfWait(binaryExpr.lhsExpr, exprs);
        visitBinaryExprOfWait(binaryExpr.rhsExpr, exprs);
        return exprs;
    }

    private void visitBinaryExprOfWait(BLangExpression expr, List<BLangExpression> exprs) {
        if (expr.getKind() == NodeKind.BINARY_EXPR) {
            collectAllBinaryExprs((BLangBinaryExpr) expr, exprs);
        } else {
            expr = rewriteExpr(expr);
            exprs.add(expr);
        }
    }

    @Override
    public void visit(BLangWaitForAllExpr waitExpr) {
        waitExpr.keyValuePairs.forEach(keyValue -> {
            if (keyValue.valueExpr != null) {
                keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
            } else {
                keyValue.keyExpr = rewriteExpr(keyValue.keyExpr);
            }
        });
        BLangExpression expr = new BLangWaitForAllExpr.BLangWaitLiteral(waitExpr.keyValuePairs, waitExpr.getBType());
        expr.pos = waitExpr.pos;
        result = rewriteExpr(expr);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr = rewriteExpr(trapExpr.expr);
        if (Types.getReferredType(trapExpr.expr.getBType()).tag != TypeTags.NIL) {
            trapExpr.expr = addConversionExprIfRequired(trapExpr.expr, trapExpr.getBType());
        }
        result = trapExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (isNullableBinaryExpr(binaryExpr)) {
            BLangStatementExpression stmtExpr = createStmtExprForNullableBinaryExpr(binaryExpr);
            result = rewrite(stmtExpr, env);
            return;
        }

        if (binaryExpr.opKind == OperatorKind.HALF_OPEN_RANGE || binaryExpr.opKind == OperatorKind.CLOSED_RANGE) {
            BLangExpression lhsExpr = binaryExpr.lhsExpr;
            BLangExpression rhsExpr = binaryExpr.rhsExpr;

            // If type of either expression is a subtype of int, cast to int as method gen does not support subtypes.
            lhsExpr = createTypeCastExpr(lhsExpr, symTable.intType);
            rhsExpr = createTypeCastExpr(rhsExpr, symTable.intType);

            if (binaryExpr.opKind == OperatorKind.HALF_OPEN_RANGE) {
                rhsExpr = getModifiedIntRangeEndExpr(rhsExpr);
            }

            result = rewriteExpr(replaceWithIntRange(binaryExpr.pos, lhsExpr, rhsExpr));
            return;
        }

        if (binaryExpr.opKind == OperatorKind.AND || binaryExpr.opKind == OperatorKind.OR) {
            visitBinaryLogicalExpr(binaryExpr);
            return;
        }

        OperatorKind binaryOpKind = binaryExpr.opKind;

        if (binaryOpKind == OperatorKind.ADD || binaryOpKind == OperatorKind.SUB ||
                binaryOpKind == OperatorKind.MUL || binaryOpKind == OperatorKind.DIV ||
                binaryOpKind == OperatorKind.MOD || binaryOpKind == OperatorKind.BITWISE_AND ||
                binaryOpKind == OperatorKind.BITWISE_OR || binaryOpKind == OperatorKind.BITWISE_XOR) {
            checkByteTypeIncompatibleOperations(binaryExpr);
        }

        binaryExpr.lhsExpr = rewriteExpr(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = rewriteExpr(binaryExpr.rhsExpr);
        result = binaryExpr;

        int rhsExprTypeTag = Types.getReferredType(binaryExpr.rhsExpr.getBType()).tag;
        int lhsExprTypeTag = Types.getReferredType(binaryExpr.lhsExpr.getBType()).tag;

        // Check for int and byte ==, != or === comparison and add type conversion to int for byte
        if (rhsExprTypeTag != lhsExprTypeTag && (binaryExpr.opKind == OperatorKind.EQUAL ||
                                                         binaryExpr.opKind == OperatorKind.NOT_EQUAL ||
                                                         binaryExpr.opKind == OperatorKind.REF_EQUAL ||
                                                         binaryExpr.opKind == OperatorKind.REF_NOT_EQUAL)) {
            if (TypeTags.isIntegerTypeTag(lhsExprTypeTag) && rhsExprTypeTag == TypeTags.BYTE) {
                binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, symTable.intType);
                return;
            }

            if (lhsExprTypeTag == TypeTags.BYTE && TypeTags.isIntegerTypeTag(rhsExprTypeTag)) {
                binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, symTable.intType);
                return;
            }
        }

        boolean isBinaryShiftOperator = symResolver.isBinaryShiftOperator(binaryOpKind);
        boolean isArithmeticOperator = symResolver.isArithmeticOperator(binaryOpKind);

        // Check lhs and rhs type compatibility
        if (lhsExprTypeTag == rhsExprTypeTag) {
            if (!isBinaryShiftOperator && !isArithmeticOperator) {
                return;
            }
            if (types.isValueType(binaryExpr.lhsExpr.getBType())) {
                return;
            }
        }

        if (binaryExpr.opKind == OperatorKind.ADD && TypeTags.isStringTypeTag(lhsExprTypeTag) &&
                (rhsExprTypeTag == TypeTags.XML || rhsExprTypeTag == TypeTags.XML_TEXT)) {
            // string + xml ==> (xml string) + xml
            binaryExpr.lhsExpr = ASTBuilderUtil.createXMLTextLiteralNode(binaryExpr, binaryExpr.lhsExpr,
                    binaryExpr.lhsExpr.pos, symTable.xmlType);
            return;
        }

        if (binaryExpr.opKind == OperatorKind.ADD && TypeTags.isStringTypeTag(rhsExprTypeTag) &&
                (lhsExprTypeTag == TypeTags.XML || lhsExprTypeTag == TypeTags.XML_TEXT)) {
            // xml + string ==> xml + (xml string)
            binaryExpr.rhsExpr = ASTBuilderUtil.createXMLTextLiteralNode(binaryExpr, binaryExpr.rhsExpr,
                    binaryExpr.rhsExpr.pos, symTable.xmlType);
            return;
        }

        if (symResolver.isBinaryComparisonOperator(binaryOpKind)) {
            createTypeCastExprForRelationalExpr(binaryExpr, lhsExprTypeTag, rhsExprTypeTag);
            return;
        }

        if (lhsExprTypeTag == TypeTags.DECIMAL) {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.lhsExpr.getBType());
            return;
        }

        if (rhsExprTypeTag == TypeTags.DECIMAL) {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.rhsExpr.getBType());
            return;
        }

        if (lhsExprTypeTag == TypeTags.FLOAT) {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.lhsExpr.getBType());
            return;
        }

        if (rhsExprTypeTag == TypeTags.FLOAT) {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.rhsExpr.getBType());
            return;
        }

        if (isArithmeticOperator) {
            createTypeCastExprForArithmeticExpr(binaryExpr, lhsExprTypeTag, rhsExprTypeTag);
            return;
        }

        if (isBinaryShiftOperator) {
            createTypeCastExprForBinaryShiftExpr(binaryExpr, lhsExprTypeTag, rhsExprTypeTag);
            return;
        }
    }

    private BLangStatementExpression createStmtExprForNullableBinaryExpr(BLangBinaryExpr binaryExpr) {
        /*
         * int? x = 3;
         * int? y = 5;
         * int? z = x + y;
         * Above is desugared to
         * int? $result$;
         * // Evaluate both operands to avoid short-circuiting.
         * int? $lhsExprVar$ = x;
         * int? $rhsExprVar$ = y;
         * if (lhsVar is () or rhsVar is ()) {
         *    $result$ = ();
         * } else {
         *    $result$ = $lhsExprVar$ + $rhsExprVar$;
         * }
         * int z = $result$;
         */
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(binaryExpr.pos);

        BUnionType exprBType = (BUnionType) binaryExpr.getBType();
        BType nonNilType = exprBType.getMemberTypes().iterator().next();

        BType rhsType;
        BType lhsType;
        if (symResolver.isArithmeticOperator(binaryExpr.opKind)) {
            rhsType = nonNilType;
            lhsType = nonNilType;
        } else {
            // then it is a bitwise operator or a shift operator
            rhsType = getBinaryExprOperandNonNilType(binaryExpr.rhsExpr.getBType());
            lhsType = getBinaryExprOperandNonNilType(binaryExpr.lhsExpr.getBType());
        }

        if (binaryExpr.lhsExpr.getBType().isNullable()) {
            binaryExpr.lhsExpr = rewriteExpr(binaryExpr.lhsExpr);
        }

        BLangSimpleVariableDef tempVarDef = createVarDef("result",
                binaryExpr.getBType(), null, binaryExpr.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(binaryExpr.pos, tempVarDef.var.symbol);
        blockStmt.addStatement(tempVarDef);

        BLangSimpleVariableDef lhsVarDef = createVarDef("$lhsExprVar$", binaryExpr.lhsExpr.getBType(),
                binaryExpr.lhsExpr, binaryExpr.pos);
        BLangSimpleVarRef lhsVarRef = ASTBuilderUtil.createVariableRef(binaryExpr.pos, lhsVarDef.var.symbol);
        blockStmt.addStatement(lhsVarDef);

        BLangSimpleVariableDef rhsVarDef = createVarDef("$rhsExprVar$", binaryExpr.rhsExpr.getBType(),
                binaryExpr.rhsExpr, binaryExpr.pos);
        BLangSimpleVarRef rhsVarRef = ASTBuilderUtil.createVariableRef(binaryExpr.pos, rhsVarDef.var.symbol);
        blockStmt.addStatement(rhsVarDef);

        BLangTypeTestExpr typeTestExprOne = createTypeCheckExpr(binaryExpr.pos, lhsVarRef, getNillTypeNode());
        typeTestExprOne.setBType(symTable.booleanType);

        BLangTypeTestExpr typeTestExprTwo = createTypeCheckExpr(binaryExpr.pos, rhsVarRef, getNillTypeNode());
        typeTestExprTwo.setBType(symTable.booleanType);

        BLangBinaryExpr ifBlockCondition = ASTBuilderUtil.createBinaryExpr(binaryExpr.pos, typeTestExprOne,
                typeTestExprTwo, symTable.booleanType, OperatorKind.OR, binaryExpr.opSymbol);

        BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(binaryExpr.pos);
        BLangAssignment bLangAssignmentIf = ASTBuilderUtil.createAssignmentStmt(binaryExpr.pos, ifBody);
        bLangAssignmentIf.varRef = tempVarRef;
        bLangAssignmentIf.expr = createNilLiteral();

        BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(binaryExpr.pos);
        BLangAssignment bLangAssignmentElse = ASTBuilderUtil.createAssignmentStmt(binaryExpr.pos, elseBody);
        bLangAssignmentElse.varRef = tempVarRef;

        BLangBinaryExpr newBinaryExpr = ASTBuilderUtil.createBinaryExpr(binaryExpr.pos, lhsVarRef, rhsVarRef,
                nonNilType, binaryExpr.opKind, binaryExpr.opSymbol);
        newBinaryExpr.lhsExpr = createTypeCastExpr(lhsVarRef, lhsType);
        newBinaryExpr.rhsExpr = createTypeCastExpr(rhsVarRef, rhsType);
        bLangAssignmentElse.expr = newBinaryExpr;

        BLangIf ifStatement = ASTBuilderUtil.createIfStmt(binaryExpr.pos, blockStmt);
        ifStatement.expr = ifBlockCondition;
        ifStatement.body = ifBody;
        ifStatement.elseStmt = elseBody;

        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, tempVarRef);
        stmtExpr.setBType(binaryExpr.getBType());

        return stmtExpr;
    }

    private BType getBinaryExprOperandNonNilType(BType operandType) {
        return operandType.isNullable() ? types.getSafeType(operandType, true, false) : operandType;
    }

    private boolean isNullableBinaryExpr(BLangBinaryExpr binaryExpr) {
        if ((binaryExpr.lhsExpr.getBType() != null && binaryExpr.rhsExpr.getBType() != null) &&
                (binaryExpr.rhsExpr.getBType().isNullable() ||
                        binaryExpr.lhsExpr.getBType().isNullable())) {
            switch (binaryExpr.getOperatorKind()) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MOD:
                case BITWISE_LEFT_SHIFT:
                case BITWISE_RIGHT_SHIFT:
                case BITWISE_UNSIGNED_RIGHT_SHIFT:
                case BITWISE_AND:
                case BITWISE_OR:
                case BITWISE_XOR:
                    return true;
            }
        }
        return false;
    }

    private void createTypeCastExprForArithmeticExpr(BLangBinaryExpr binaryExpr, int lhsExprTypeTag,
                                                     int rhsExprTypeTag) {
        if ((TypeTags.isIntegerTypeTag(lhsExprTypeTag) && TypeTags.isIntegerTypeTag(rhsExprTypeTag)) ||
                (TypeTags.isStringTypeTag(lhsExprTypeTag) && TypeTags.isStringTypeTag(rhsExprTypeTag)) ||
                (TypeTags.isXMLTypeTag(lhsExprTypeTag) && TypeTags.isXMLTypeTag(rhsExprTypeTag))) {
            return;
        }
        if (TypeTags.isXMLTypeTag(lhsExprTypeTag) && !TypeTags.isXMLTypeTag(rhsExprTypeTag)) {
            if (types.checkTypeContainString(binaryExpr.rhsExpr.getBType())) {
                binaryExpr.rhsExpr = ASTBuilderUtil.createXMLTextLiteralNode(binaryExpr, binaryExpr.rhsExpr,
                        binaryExpr.rhsExpr.pos, symTable.xmlType);
                return;
            }
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, symTable.xmlType);
            return;
        }
        if (TypeTags.isXMLTypeTag(rhsExprTypeTag) && !TypeTags.isXMLTypeTag(lhsExprTypeTag)) {
            if (types.checkTypeContainString(binaryExpr.lhsExpr.getBType())) {
                binaryExpr.lhsExpr = ASTBuilderUtil.createXMLTextLiteralNode(binaryExpr, binaryExpr.lhsExpr,
                        binaryExpr.rhsExpr.pos, symTable.xmlType);
                return;
            }
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, symTable.xmlType);
            return;
        }
        binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.getBType());
        binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.getBType());
    }

    private void createTypeCastExprForBinaryShiftExpr(BLangBinaryExpr binaryExpr, int lhsExprTypeTag,
                                                      int rhsExprTypeTag) {
        boolean isLhsIntegerType = TypeTags.isIntegerTypeTag(lhsExprTypeTag);
        boolean isRhsIntegerType = TypeTags.isIntegerTypeTag(rhsExprTypeTag);
        if (isLhsIntegerType || lhsExprTypeTag == TypeTags.BYTE) {
            if (isRhsIntegerType || rhsExprTypeTag == TypeTags.BYTE) {
                return;
            }
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, symTable.intType);
            return;
        }

        if (isRhsIntegerType || rhsExprTypeTag == TypeTags.BYTE) {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, symTable.intType);
            return;
        }

        binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, symTable.intType);
        binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, symTable.intType);
    }

    private void createTypeCastExprForRelationalExpr(BLangBinaryExpr binaryExpr, int lhsExprTypeTag,
                                                     int rhsExprTypeTag) {
        boolean isLhsIntegerType = TypeTags.isIntegerTypeTag(lhsExprTypeTag);
        boolean isRhsIntegerType = TypeTags.isIntegerTypeTag(rhsExprTypeTag);
        BType lhsExprType = binaryExpr.lhsExpr.getBType();
        BType rhsExprType = binaryExpr.rhsExpr.getBType();

        if ((isLhsIntegerType && isRhsIntegerType) || (lhsExprTypeTag == TypeTags.BYTE &&
                rhsExprTypeTag == TypeTags.BYTE)) {
            return;
        }

        if (lhsExprTypeTag == TypeTags.DECIMAL) {
            addTypeCastForBinaryExprB(binaryExpr, lhsExprType, rhsExprType);
            return;
        }

        if (rhsExprTypeTag == TypeTags.DECIMAL) {
            addTypeCastForBinaryExprA(binaryExpr, rhsExprType, lhsExprType);
            return;
        }

        if (lhsExprTypeTag == TypeTags.FLOAT) {
            addTypeCastForBinaryExprB(binaryExpr, lhsExprType, rhsExprType);
            return;
        }

        if (rhsExprTypeTag == TypeTags.FLOAT) {
            addTypeCastForBinaryExprA(binaryExpr, rhsExprType, lhsExprType);
            return;
        }

        if (isLhsIntegerType && !isRhsIntegerType) {
            addTypeCastForBinaryExprB(binaryExpr, symTable.intType, rhsExprType);
            return;
        }

        if (!isLhsIntegerType && isRhsIntegerType) {
            addTypeCastForBinaryExprA(binaryExpr, symTable.intType, lhsExprType);
            return;
        }

        if (lhsExprTypeTag == TypeTags.BYTE || rhsExprTypeTag == TypeTags.BYTE) {
            if ((lhsExprTypeTag == TypeTags.UNION && lhsExprType.isNullable()) ||
                    (rhsExprTypeTag == TypeTags.UNION && rhsExprType.isNullable())) {
                binaryExpr.lhsExpr = addNilType(symTable.intType, binaryExpr.lhsExpr);
                binaryExpr.rhsExpr = addNilType(symTable.intType, binaryExpr.rhsExpr);
                return;
            }
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, symTable.intType);
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, symTable.intType);
            return;
        }

        boolean isLhsStringType = TypeTags.isStringTypeTag(lhsExprTypeTag);
        boolean isRhsStringType = TypeTags.isStringTypeTag(rhsExprTypeTag);

        if (isLhsStringType && isRhsStringType) {
            return;
        }

        if (isLhsStringType && !isRhsStringType) {
            addTypeCastForBinaryExprB(binaryExpr, symTable.stringType, rhsExprType);
            return;
        }

        if (!isLhsStringType && isRhsStringType) {
            addTypeCastForBinaryExprA(binaryExpr, symTable.stringType, lhsExprType);
        }
    }

    private void addTypeCastForBinaryExprA(BLangBinaryExpr binaryExpr, BType rhsExprType, BType lhsExprType) {
        if (lhsExprType.tag == TypeTags.UNION && lhsExprType.isNullable()) {
            binaryExpr.rhsExpr = addNilType(rhsExprType, binaryExpr.rhsExpr);
        } else {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, rhsExprType);
        }
    }

    private void addTypeCastForBinaryExprB(BLangBinaryExpr binaryExpr, BType lhsExprType, BType rhsExprType) {
        if (rhsExprType.tag == TypeTags.UNION && rhsExprType.isNullable()) {
            binaryExpr.lhsExpr = addNilType(lhsExprType, binaryExpr.lhsExpr);
        } else {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, lhsExprType);
        }
    }

    private BLangExpression addNilType(BType exprType, BLangExpression expr) {
        LinkedHashSet<BType> members = new LinkedHashSet<>(2);
        members.add(exprType);
        members.add(symTable.nilType);
        BUnionType unionType = new BUnionType(null, members, true, false);
        return createTypeCastExpr(expr, unionType);
    }

    private BLangInvocation replaceWithIntRange(Location location, BLangExpression lhsExpr,
                                                BLangExpression rhsExpr) {
        BInvokableSymbol symbol = (BInvokableSymbol) symTable.langInternalModuleSymbol.scope
                .lookup(Names.CREATE_INT_RANGE).symbol;
        BLangInvocation createIntRangeInvocation = ASTBuilderUtil.createInvocationExprForMethod(location, symbol,
                new ArrayList<>(Lists.of(lhsExpr, rhsExpr)), symResolver);
        createIntRangeInvocation.setBType(symTable.intRangeType);
        return createIntRangeInvocation;
    }

    private void checkByteTypeIncompatibleOperations(BLangBinaryExpr binaryExpr) {
        if (binaryExpr.expectedType == null) {
            return;
        }

        int rhsExprTypeTag = Types.getReferredType(binaryExpr.rhsExpr.getBType()).tag;
        int lhsExprTypeTag = Types.getReferredType(binaryExpr.lhsExpr.getBType()).tag;
        if (rhsExprTypeTag != TypeTags.BYTE && lhsExprTypeTag != TypeTags.BYTE) {
            return;
        }

        int resultTypeTag = binaryExpr.expectedType.tag;
        if (resultTypeTag == TypeTags.INT) {
            if (rhsExprTypeTag == TypeTags.BYTE) {
                binaryExpr.rhsExpr = addConversionExprIfRequired(binaryExpr.rhsExpr, symTable.intType);
            }

            if (lhsExprTypeTag == TypeTags.BYTE) {
                binaryExpr.lhsExpr = addConversionExprIfRequired(binaryExpr.lhsExpr, symTable.intType);
            }
        }
    }

    /**
     * This method checks whether given binary expression is related to shift operation.
     * If its true, then both lhs and rhs of the binary expression will be converted to 'int' type.
     * <p>
     * byte a = 12;
     * byte b = 34;
     * int i = 234;
     * int j = -4;
     * <p>
     * true: where binary expression's expected type is 'int'
     * int i1 = a >> b;
     * int i2 = a << b;
     * int i3 = a >> i;
     * int i4 = a << i;
     * int i5 = i >> j;
     * int i6 = i << j;
     */
    private boolean isBitwiseShiftOperation(BLangBinaryExpr binaryExpr) {
        return binaryExpr.opKind == OperatorKind.BITWISE_LEFT_SHIFT ||
                binaryExpr.opKind == OperatorKind.BITWISE_RIGHT_SHIFT ||
                binaryExpr.opKind == OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT;
    }

    public void visit(BLangElvisExpr elvisExpr) {
        Location pos = elvisExpr.pos;
        String resultVarName = "_$result$_";
        BType resultType = elvisExpr.getBType();
        BLangSimpleVariable resultVar =
                ASTBuilderUtil.createVariable(pos, resultVarName, resultType, null,
                        new BVarSymbol(0, names.fromString(resultVarName),
                                this.env.scope.owner.pkgID, resultType,
                                this.env.scope.owner, pos, VIRTUAL));
        BLangSimpleVariableDef resultVarDef = ASTBuilderUtil.createVariableDef(pos, resultVar);
        resultVarDef.desugared = true;
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVar.symbol);

        String lhsResultVarName = GEN_VAR_PREFIX.value;
        BLangSimpleVariable lhsResultVar =
                ASTBuilderUtil.createVariable(pos, lhsResultVarName, elvisExpr.lhsExpr.getBType(), elvisExpr.lhsExpr,
                        new BVarSymbol(0, names.fromString(lhsResultVarName),
                                this.env.scope.owner.pkgID, elvisExpr.lhsExpr.getBType(),
                                this.env.scope.owner, elvisExpr.pos, VIRTUAL));
        BLangSimpleVariableDef lhsResultVarDef = ASTBuilderUtil.createVariableDef(pos, lhsResultVar);
        BLangSimpleVarRef lhsResultVarRef = ASTBuilderUtil.createVariableRef(pos, lhsResultVar.symbol);

        BLangAssignment nilAssignment = ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef, elvisExpr.rhsExpr);
        BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(pos);
        ifBody.addStatement(nilAssignment);

        BLangAssignment notNilAssignment = ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef,
                createTypeCastExpr(lhsResultVarRef, resultType));
        BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(pos);
        elseBody.addStatement(notNilAssignment);

        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos,
                createTypeCheckExpr(pos, lhsResultVarRef, getNillTypeNode()), ifBody, elseBody);
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos, new ArrayList<>() {{
            add(resultVarDef);
            add(lhsResultVarDef);
            add(ifStmt);
        }});
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, resultVarRef);
        stmtExpr.setBType(resultType);
        result = rewriteExpr(stmtExpr);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {

        if (isNullableUnaryExpr(unaryExpr)) {
            BLangStatementExpression statementExpression = createStmtExprForNilableUnaryExpr(unaryExpr);
            result = rewrite(statementExpression, env);
            return;
        }

        if (OperatorKind.BITWISE_COMPLEMENT == unaryExpr.operator) {
            // If this is a bitwise complement (~) expression, then we desugar it to a binary xor expression with -1,
            // which is same as doing a bitwise 2's complement operation.
            rewriteBitwiseComplementOperator(unaryExpr);
            return;
        }

        // Since the support for singleton type changes are not complete, continuing with the finite type will require
        // significant changes, therefore we are constructing a numeric literal.
        if (types.isExpressionInUnaryValid(unaryExpr.expr) && unaryExpr.expectedType.tag == TypeTags.FINITE) {
            result = rewriteExpr(Types.constructNumericLiteralFromUnaryExpr(unaryExpr));
            return;
        }

        OperatorKind opKind = unaryExpr.operator;
        if (opKind == OperatorKind.ADD || opKind == OperatorKind.SUB) {
            createTypeCastExprForUnaryPlusAndMinus(unaryExpr);
        }
        unaryExpr.expr = rewriteExpr(unaryExpr.expr);
        result = unaryExpr;
    }

    private void createTypeCastExprForUnaryPlusAndMinus(BLangUnaryExpr unaryExpr) {
        BLangExpression expr = unaryExpr.expr;
        if (TypeTags.isIntegerTypeTag(expr.getBType().tag)) {
            return;
        }
        unaryExpr.expr = createTypeCastExpr(expr, unaryExpr.getBType());
    }

    /**
     * This method desugar a bitwise complement (~) unary expressions into a bitwise xor binary expression as below.
     * Example : ~a  -> a ^ -1;
     * ~ 11110011 -> 00001100
     * 11110011 ^ 11111111 -> 00001100
     *
     * @param unaryExpr the bitwise complement expression
     */
    private void rewriteBitwiseComplementOperator(BLangUnaryExpr unaryExpr) {
        final Location pos = unaryExpr.pos;
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.opKind = OperatorKind.BITWISE_XOR;
        binaryExpr.lhsExpr = unaryExpr.expr;
        if (TypeTags.BYTE == Types.getReferredType(unaryExpr.getBType()).tag) {
            binaryExpr.setBType(symTable.byteType);
            binaryExpr.rhsExpr = ASTBuilderUtil.createLiteral(pos, symTable.byteType, 0xffL);
            binaryExpr.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.BITWISE_XOR,
                    symTable.byteType, symTable.byteType);
        } else {
            binaryExpr.setBType(symTable.intType);
            binaryExpr.rhsExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType, -1L);
            binaryExpr.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.BITWISE_XOR,
                    symTable.intType, symTable.intType);
        }
        result = rewriteExpr(binaryExpr);
    }

    private BLangStatementExpression createStmtExprForNilableUnaryExpr(BLangUnaryExpr unaryExpr) {
        /*
         * int? x = 3;
         * int? y = +x;
         *
         *
         * Above is desugared to
         * int? $result$;
         * if (x is ()) {
         *    $result$ = ();
         * } else {
         *    $result$ = +x;
         * }
         * int y = $result$
         */
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(unaryExpr.pos);

        BUnionType exprBType = (BUnionType) unaryExpr.getBType();
        BType nilLiftType = exprBType.getMemberTypes().iterator().next();

        unaryExpr.expr = rewriteExpr(unaryExpr.expr);

        BLangSimpleVariableDef tempVarDef = createVarDef("$result",
                unaryExpr.getBType(), createNilLiteral(), unaryExpr.pos);
        BLangSimpleVarRef tempVarRef = ASTBuilderUtil.createVariableRef(unaryExpr.pos, tempVarDef.var.symbol);

        blockStmt.addStatement(tempVarDef);

        BLangTypeTestExpr typeTestExpr = createTypeCheckExpr(unaryExpr.pos, unaryExpr.expr,
                getNillTypeNode());
        typeTestExpr.setBType(symTable.booleanType);

        BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(unaryExpr.pos);
        BLangAssignment bLangAssignmentIf = ASTBuilderUtil.createAssignmentStmt(unaryExpr.pos, ifBody);
        bLangAssignmentIf.varRef = tempVarRef;
        bLangAssignmentIf.expr = createNilLiteral();

        BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(unaryExpr.pos);
        BLangAssignment bLangAssignmentElse = ASTBuilderUtil.createAssignmentStmt(unaryExpr.pos, elseBody);
        bLangAssignmentElse.varRef = tempVarRef;

        BLangExpression expr = createTypeCastExpr(unaryExpr.expr, nilLiftType);
        bLangAssignmentElse.expr = ASTBuilderUtil.createUnaryExpr(unaryExpr.pos, expr,
                                                                  nilLiftType, unaryExpr.operator, unaryExpr.opSymbol);

        BLangIf ifStatement = ASTBuilderUtil.createIfStmt(unaryExpr.pos, blockStmt);
        ifStatement.expr = typeTestExpr;
        ifStatement.body = ifBody;
        ifStatement.elseStmt = elseBody;

        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, tempVarRef);
        stmtExpr.setBType(unaryExpr.getBType());

        return stmtExpr;
    }

    private boolean isNullableUnaryExpr(BLangUnaryExpr unaryExpr) {
        if (unaryExpr.getBType() != null && unaryExpr.getBType().isNullable()) {
            switch (unaryExpr.operator) {
                case ADD:
                case SUB:
                case BITWISE_COMPLEMENT:
                    return true;
            }
        }
        return false;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // Usually the parameter for a type-cast-expr includes a type-descriptor.
        // However, it is also allowed for the parameter to consist only of annotations; in
        // this case, the only effect of the type cast is for the contextually expected
        // type for expression to be augmented with the specified annotations.

        // No actual type-cast is implied here.
        if (conversionExpr.typeNode == null && !conversionExpr.annAttachments.isEmpty()) {
            result = rewriteExpr(conversionExpr.expr);
            return;
        }

        BType targetType = conversionExpr.targetType;
        conversionExpr.typeNode = rewrite(conversionExpr.typeNode, env);

        conversionExpr.expr = rewriteExpr(conversionExpr.expr);
        result = conversionExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.function = rewrite(bLangLambdaFunction.function, bLangLambdaFunction.capturedClosureEnv);
        result = bLangLambdaFunction;
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        BLangFunction bLangFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        bLangFunction.setName(bLangArrowFunction.functionName);

        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaFunction.pos = bLangArrowFunction.pos;
        bLangFunction.addFlag(Flag.LAMBDA);
        lambdaFunction.function = bLangFunction;

        // Create function body with return node
        BLangValueType returnType = (BLangValueType) TreeBuilder.createValueTypeNode();
        returnType.setBType(bLangArrowFunction.body.expr.getBType());
        bLangFunction.setReturnTypeNode(returnType);
        bLangFunction.setBody(populateArrowExprBodyBlock(bLangArrowFunction));

        bLangArrowFunction.params.forEach(bLangFunction::addParameter);
        lambdaFunction.parent = bLangArrowFunction.parent;
        lambdaFunction.setBType(bLangArrowFunction.funcType);

        // Create function symbol.
        BLangFunction funcNode = lambdaFunction.function;
        BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                                                                   new Name(funcNode.name.value),
                                                                   new Name(funcNode.name.originalValue),
                                                                   env.enclPkg.symbol.pkgID,
                                                                   bLangArrowFunction.funcType,
                                                                   env.enclEnv.enclVarSym, true,
                                                                   bLangArrowFunction.pos, VIRTUAL);

        funcSymbol.originalName = new Name(funcNode.name.originalValue);

        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);

        List<BVarSymbol> paramSymbols = funcNode.requiredParams.stream().peek(varNode -> {
            Scope enclScope = invokableEnv.scope;
            varNode.symbol.kind = SymbolKind.FUNCTION;
            varNode.symbol.owner = invokableEnv.scope.owner;
            enclScope.define(varNode.symbol.name, varNode.symbol);
        }).map(varNode -> varNode.symbol).collect(Collectors.toList());

        funcSymbol.params = paramSymbols;
        funcSymbol.restParam = getRestSymbol(funcNode);
        funcSymbol.retType = funcNode.returnTypeNode.getBType();
        // Create function type.
        List<BType> paramTypes = paramSymbols.stream().map(paramSym -> paramSym.type).collect(Collectors.toList());
        funcNode.setBType(new BInvokableType(paramTypes, getRestType(funcSymbol), funcNode.returnTypeNode.getBType(),
                          funcSymbol.type.tsymbol));

        lambdaFunction.function.pos = bLangArrowFunction.pos;
        lambdaFunction.function.body.pos = bLangArrowFunction.pos;
        // At this phase lambda function is semantically correct. Therefore simply env can be assigned.
        lambdaFunction.capturedClosureEnv = env;
        env.enclPkg.addFunction(lambdaFunction.function);
        result = rewriteExpr(lambdaFunction);
    }

    private void defineInvokableSymbol(BLangInvokableNode invokableNode, BInvokableSymbol funcSymbol,
                                       SymbolEnv invokableEnv) {
        invokableNode.symbol = funcSymbol;
        funcSymbol.scope = new Scope(funcSymbol);
        invokableEnv.scope = funcSymbol.scope;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        result = xmlQName;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.name = rewriteExpr(xmlAttribute.name);
        xmlAttribute.value = rewriteExpr(xmlAttribute.value);
        result = xmlAttribute;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.attributes = rewriteExprs(xmlElementLiteral.attributes);

        // Separate the in-line namepsace declarations and attributes.
        Iterator<BLangXMLAttribute> attributesItr = xmlElementLiteral.attributes.iterator();
        while (attributesItr.hasNext()) {
            BLangXMLAttribute attribute = attributesItr.next();
            if (!attribute.isNamespaceDeclr) {
                continue;
            }

            // Create namepace declaration for all in-line namespace declarations
            BLangXMLNS xmlns;
            if ((xmlElementLiteral.scope.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE) {
                xmlns = new BLangPackageXMLNS();
            } else {
                xmlns = new BLangLocalXMLNS();
            }
            xmlns.namespaceURI = attribute.value.concatExpr;
            xmlns.prefix = ((BLangXMLQName) attribute.name).localname;
            xmlns.symbol = attribute.symbol;

            xmlElementLiteral.inlineNamespaces.add(xmlns);
        }

        List<BLangXMLNS> prevInlineNamespaces = this.inlineXMLNamespaces;
        if (isVisitingQuery && this.inlineXMLNamespaces != null) {
            // if xml has an interpolated query, the inline namespace should be propagated
            xmlElementLiteral.inlineNamespaces.addAll(this.inlineXMLNamespaces);
        }
        this.inlineXMLNamespaces = xmlElementLiteral.inlineNamespaces;

        xmlElementLiteral.startTagName = rewriteExpr(xmlElementLiteral.startTagName);
        xmlElementLiteral.endTagName = rewriteExpr(xmlElementLiteral.endTagName);
        xmlElementLiteral.modifiedChildren = rewriteExprs(xmlElementLiteral.modifiedChildren);

        this.inlineXMLNamespaces = prevInlineNamespaces;
        result = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSequenceLiteral) {
        for (BLangExpression xmlItem : xmlSequenceLiteral.xmlItems) {
            rewriteExpr(xmlItem);
        }
        result = xmlSequenceLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.concatExpr = rewriteExpr(constructStringTemplateConcatExpression(xmlTextLiteral.textFragments));
        result = xmlTextLiteral;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.concatExpr = rewriteExpr(
                constructStringTemplateConcatExpression(xmlCommentLiteral.textFragments));
        result = xmlCommentLiteral;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target = rewriteExpr(xmlProcInsLiteral.target);
        xmlProcInsLiteral.dataConcatExpr =
                rewriteExpr(constructStringTemplateConcatExpression(xmlProcInsLiteral.dataFragments));
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr = rewriteExpr(
                constructStringTemplateConcatExpression(xmlQuotedString.textFragments));
        result = xmlQuotedString;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        result = rewriteExpr(constructStringTemplateConcatExpression(stringTemplateLiteral.exprs));
    }

    /**
     * The raw template literal gets desugared to a type init expression. For each literal, a new object class type
     * def is generated from the object type. The type init expression creates an instance of this generated object
     * type. For example, consider the following statements:
     *      string name = "Pubudu";
     *      'object:RawTemplate rt = `Hello ${name}!`;
     *
     * The raw template literal above is desugared to:
     *      type RawTemplate$Impl$0 object {
     *          public string[] strings = ["Hello ", "!"];
     *          public (any|error)[] insertions;
     *
     *          function init((any|error)[] insertions) {
     *              self.insertions = insertions;
     *          }
     *      };
     *
     *      // somewhere in code
     *      'object:RawTemplate rt = new RawTemplate$Impl$0([name]);
     *
     * @param rawTemplateLiteral The raw template literal to be desugared.
     */
    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        Location pos = rawTemplateLiteral.pos;
        BObjectType objType = (BObjectType) Types.getReferredType(rawTemplateLiteral.getBType());
        BLangClassDefinition objClassDef =
                desugarTemplateLiteralObjectTypedef(rawTemplateLiteral.strings, objType, pos);
        BObjectType classObjType = (BObjectType) objClassDef.getBType();

        BVarSymbol insertionsSym = classObjType.fields.get("insertions").symbol;
        BLangListConstructorExpr insertionsList = ASTBuilderUtil.createListConstructorExpr(pos, insertionsSym.type);
        insertionsList.exprs.addAll(rawTemplateLiteral.insertions);
        insertionsList.expectedType = insertionsSym.type;

        // Create an instance of the generated object class
        BLangTypeInit typeNewExpr = ASTBuilderUtil.createEmptyTypeInit(pos, classObjType);
        typeNewExpr.argsExpr.add(insertionsList);
        BLangInvocation initInvocation = (BLangInvocation) typeNewExpr.initInvocation;
        initInvocation.argExprs.add(insertionsList);
        initInvocation.requiredArgs.add(insertionsList);

        result = rewriteExpr(typeNewExpr);
    }

    /**
     * This method desugars a raw template literal object class for the provided raw template object type as follows.
     * A literal defined as 'object:RawTemplate rt = `Hello ${name}!`;
     * is desugared to,
     *      type $anonType$0 object {
     *          public string[] strings = ["Hello ", "!"];
     *          public (any|error)[] insertions;
     *
     *          function init((any|error)[] insertions) {
     *              self.insertions = insertions;
     *          }
     *      };
     * @param strings    The string portions of the literal
     * @param objectType The abstract object type for which an object class needs to be generated
     * @param pos        The diagnostic position info for the type node
     * @return Returns the generated concrete object class def
     */
    private BLangClassDefinition desugarTemplateLiteralObjectTypedef(List<BLangLiteral> strings, BObjectType objectType,
                                                                    Location pos) {
        // TODO: Use the anon model helper to generate the object name?
        BObjectTypeSymbol tSymbol = (BObjectTypeSymbol) objectType.tsymbol;
        Name objectClassName = names.fromString(
                anonModelHelper.getNextRawTemplateTypeKey(env.enclPkg.packageID, tSymbol.name));

        BObjectTypeSymbol classTSymbol = Symbols.createClassSymbol(tSymbol.flags, objectClassName,
                                                                   env.enclPkg.packageID, null, env.enclPkg.symbol,
                                                                   pos, VIRTUAL, false);
        classTSymbol.flags |= Flags.CLASS;

        // Create a new concrete, class type for the provided abstract object type
        BObjectType objectClassType = new BObjectType(classTSymbol, classTSymbol.flags);
        objectClassType.fields = objectType.fields;
        classTSymbol.type = objectClassType;
        objectClassType.typeIdSet.add(objectType.typeIdSet);

        // Create a new object type node and a type def from the concrete class type
//        BLangObjectTypeNode objectClassNode = TypeDefBuilderHelper.createObjectTypeNode(objectClassType, pos);
//        BLangTypeDefinition typeDef = TypeDefBuilderHelper.addTypeDefinition(objectClassType, objectClassType.tsymbol,
//                                                                             objectClassNode, env);
        BLangClassDefinition classDef = TypeDefBuilderHelper.createClassDef(pos, classTSymbol, env);
        classDef.name = ASTBuilderUtil.createIdentifier(pos, objectClassType.tsymbol.name.value);

        // Create a list constructor expr for the strings field. This gets assigned to the corresponding field in the
        // object since this needs to be initialized in the generated init method.
        BType stringsType = objectClassType.fields.get("strings").symbol.type;
        BLangListConstructorExpr stringsList = ASTBuilderUtil.createListConstructorExpr(pos, stringsType);
        stringsList.exprs.addAll(strings);
        stringsList.expectedType = stringsType;
        classDef.fields.get(0).expr = stringsList;

        // Create the init() method
        BLangFunction userDefinedInitFunction = createUserDefinedObjectInitFn(classDef, env);
        classDef.initFunction = userDefinedInitFunction;
        env.enclPkg.functions.add(userDefinedInitFunction);
        env.enclPkg.topLevelNodes.add(userDefinedInitFunction);

        // Create the initializer method for initializing default values
        BLangFunction tempGeneratedInitFunction = createGeneratedInitializerFunction(classDef, env);
        tempGeneratedInitFunction.clonedEnv = SymbolEnv.createFunctionEnv(tempGeneratedInitFunction,
                                                                          tempGeneratedInitFunction.symbol.scope, env);
        SemanticAnalyzer.AnalyzerData data = new SemanticAnalyzer.AnalyzerData(env);
        this.semanticAnalyzer.analyzeNode(tempGeneratedInitFunction, data);
        classDef.generatedInitFunction = tempGeneratedInitFunction;
        env.enclPkg.functions.add(classDef.generatedInitFunction);
        env.enclPkg.topLevelNodes.add(classDef.generatedInitFunction);

        return rewrite(classDef, env);
    }

    /**
     * Creates a user-defined init() method for the provided object type node. If there are fields without default
     * values specified in the type node, this will add parameters for those fields in the init() method and assign the
     * param values to the respective fields in the method body.
     *
     * @param classDefn The object type node for which the init() method is generated
     * @param env            The symbol env for the object type node
     * @return The generated init() method
     */
    private BLangFunction createUserDefinedObjectInitFn(BLangClassDefinition classDefn, SymbolEnv env) {
        BLangFunction initFunction =
                TypeDefBuilderHelper.createInitFunctionForStructureType(classDefn.pos, classDefn.symbol, env,
                                                                        names, Names.USER_DEFINED_INIT_SUFFIX,
                                                                        symTable, classDefn.getBType());
        BObjectTypeSymbol typeSymbol = ((BObjectTypeSymbol) classDefn.getBType().tsymbol);
        typeSymbol.initializerFunc = new BAttachedFunction(Names.USER_DEFINED_INIT_SUFFIX, initFunction.symbol,
                                                           (BInvokableType) initFunction.getBType(), classDefn.pos);
        classDefn.initFunction = initFunction;
        initFunction.returnTypeNode.setBType(symTable.nilType);

        BLangBlockFunctionBody initFuncBody = (BLangBlockFunctionBody) initFunction.body;
        BInvokableType initFnType = (BInvokableType) initFunction.getBType();
        for (BLangSimpleVariable field : classDefn.fields) {
            if (field.expr != null) {
                continue;
            }
            BVarSymbol fieldSym = field.symbol;
            BVarSymbol paramSym = new BVarSymbol(Flags.FINAL, fieldSym.name, this.env.scope.owner.pkgID, fieldSym.type,
                                                 initFunction.symbol, classDefn.pos, VIRTUAL);
            BLangSimpleVariable param = ASTBuilderUtil.createVariable(classDefn.pos, fieldSym.name.value,
                                                                      fieldSym.type, null, paramSym);
            param.flagSet.add(Flag.FINAL);
            initFunction.symbol.scope.define(paramSym.name, paramSym);
            initFunction.symbol.params.add(paramSym);
            initFnType.paramTypes.add(param.getBType());
            initFunction.requiredParams.add(param);

            BLangSimpleVarRef paramRef = ASTBuilderUtil.createVariableRef(initFunction.pos, paramSym);
            BLangAssignment fieldInit = createStructFieldUpdate(initFunction, paramRef, fieldSym, field.getBType(),
                                                                initFunction.receiver.symbol, field.name);
            initFuncBody.addStatement(fieldInit);
        }

        return initFunction;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr = visitCloneInvocation(rewriteExpr(workerSendNode.expr), workerSendNode.expr.getBType());
        result = workerSendNode;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.expr = visitCloneInvocation(rewriteExpr(syncSendExpr.expr), syncSendExpr.expr.getBType());
        result = syncSendExpr;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        result = workerReceiveNode;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        workerFlushExpr.workerIdentifierList = workerFlushExpr.cachedWorkerSendStmts
                .stream().map(send -> send.workerIdentifier).distinct().collect(Collectors.toList());
        result = workerFlushExpr;
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        BInvokableSymbol isTransactionalSymbol =
                (BInvokableSymbol) transactionDesugar.getInternalTransactionModuleInvokableSymbol(IS_TRANSACTIONAL);
        result = ASTBuilderUtil
                .createInvocationExprMethod(transactionalExpr.pos, isTransactionalSymbol, Collections.emptyList(),
                        Collections.emptyList(), symResolver);
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        BLangStatementExpression stmtExpr = transactionDesugar.desugar(commitExpr, env);
        result = rewriteExpr(stmtExpr);
    }

    @Override
    public void visit(BLangFail failNode) {
        if (this.onFailClause != null && !this.isVisitingQuery) {
            if (this.onFailClause.bodyContainsFail) {
                result = rewriteNestedOnFail(this.onFailClause, failNode);
            } else {
                result = createOnFailInvocation(onFailClause, failNode);
            }
        } else {
            BLangReturn stmt = ASTBuilderUtil.createReturnStmt(failNode.pos, rewrite(failNode.expr, env));
            stmt.desugared = true;
            result = stmt;
        }
    }

    // Generated expressions. Following expressions are not part of the original syntax
    // tree which is coming out of the parser

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        result = localVarRef;
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        result = fieldVarRef;
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        result = packageVarRef;
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangStructFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        result = mapKeyAccessExpr;
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangTupleAccessExpr arrayIndexAccessExpr) {
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangTableAccessExpr tableKeyAccessExpr) {
        result = tableKeyAccessExpr;
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        result = mapLiteral;
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        result = structLiteral;
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        result = waitLiteral;
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        //todo: _ = short hand for getElementName;
        // todo: we need to handle multiple elements x.<a|b|c>
        xmlElementAccess.expr = rewriteExpr(xmlElementAccess.expr);

        ArrayList<BLangExpression> filters = expandFilters(xmlElementAccess.filters);

        BLangInvocation invocationNode = createLanglibXMLInvocation(xmlElementAccess.pos, XML_INTERNAL_GET_ELEMENTS,
                xmlElementAccess.expr, new ArrayList<>(), filters);
        result = rewriteExpr(invocationNode);
    }

    private ArrayList<BLangExpression> expandFilters(List<BLangXMLElementFilter> filters) {
        Map<Name, BXMLNSSymbol> nameBXMLNSSymbolMap = symResolver.resolveAllNamespaces(env);
        BXMLNSSymbol defaultNSSymbol = nameBXMLNSSymbolMap.get(names.fromString(XMLConstants.DEFAULT_NS_PREFIX));
        String defaultNS = defaultNSSymbol != null ? defaultNSSymbol.namespaceURI : null;

        ArrayList<BLangExpression> args = new ArrayList<>();
        for (BLangXMLElementFilter filter : filters) {
            BSymbol nsSymbol = symResolver.lookupSymbolInPrefixSpace(env, names.fromString(filter.namespace));
            if (nsSymbol == symTable.notFoundSymbol) {
                if (defaultNS != null && !filter.name.equals("*")) {
                    String expandedName = createExpandedQName(defaultNS, filter.name);
                    args.add(createStringLiteral(filter.elemNamePos, expandedName));
                } else {
                    args.add(createStringLiteral(filter.elemNamePos, filter.name));
                }
            } else {
                BXMLNSSymbol bxmlnsSymbol = (BXMLNSSymbol) nsSymbol;
                String expandedName = createExpandedQName(bxmlnsSymbol.namespaceURI, filter.name);
                BLangLiteral stringLiteral = createStringLiteral(filter.elemNamePos, expandedName);
                args.add(stringLiteral);
            }
        }
        return args;
    }

    private BLangInvocation createLanglibXMLInvocation(Location pos, String functionName,
                                                       BLangExpression invokeOnExpr,
                                                       ArrayList<BLangExpression> args,
                                                       ArrayList<BLangExpression> restArgs) {
        invokeOnExpr = rewriteExpr(invokeOnExpr);

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.setLiteral(false);
        name.setValue(functionName);
        name.pos = pos;
        invocationNode.name = name;
        invocationNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        invocationNode.expr = invokeOnExpr;

        invocationNode.symbol = symResolver.lookupLangLibMethod(symTable.xmlType, names.fromString(functionName), env);

        ArrayList<BLangExpression> requiredArgs = new ArrayList<>();
        requiredArgs.add(invokeOnExpr);
        requiredArgs.addAll(args);
        invocationNode.requiredArgs = requiredArgs;
        invocationNode.restArgs = rewriteExprs(restArgs);

        invocationNode.setBType(((BInvokableType) invocationNode.symbol.type).getReturnType());
        invocationNode.langLibInvocation = true;
        return invocationNode;
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        xmlNavigation.expr = rewriteExpr(xmlNavigation.expr);
        xmlNavigation.childIndex = rewriteExpr(xmlNavigation.childIndex);

        ArrayList<BLangExpression> filters = expandFilters(xmlNavigation.filters);

        // xml/**/<elemName>
        if (xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.DESCENDANTS) {
            BLangInvocation invocationNode = createLanglibXMLInvocation(xmlNavigation.pos,
                    XML_INTERNAL_SELECT_DESCENDANTS, xmlNavigation.expr, new ArrayList<>(), filters);
            result = rewriteExpr(invocationNode);
        } else if (xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.CHILDREN) {
            // xml/*
            BLangInvocation invocationNode = createLanglibXMLInvocation(xmlNavigation.pos, XML_INTERNAL_CHILDREN,
                    xmlNavigation.expr, new ArrayList<>(), new ArrayList<>());
            result = rewriteExpr(invocationNode);
        } else {
            BLangExpression childIndexExpr;
            // xml/<elem>
            if (xmlNavigation.childIndex == null) {
                childIndexExpr = new BLangLiteral(Long.valueOf(-1), symTable.intType);
            } else {
                // xml/<elem>[index]
                childIndexExpr = xmlNavigation.childIndex;
            }
            ArrayList<BLangExpression> args = new ArrayList<>();
            args.add(rewriteExpr(childIndexExpr));

            BLangInvocation invocationNode = createLanglibXMLInvocation(xmlNavigation.pos,
                    XML_INTERNAL_GET_FILTERED_CHILDREN_FLAT, xmlNavigation.expr, args, filters);
            result = rewriteExpr(invocationNode);
        }
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr = rewriteExpr(assignableExpr.lhsExpr);
        result = assignableExpr;
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        typedescExpr.typeNode = rewrite(typedescExpr.typeNode, env);
        result = typedescExpr;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        result = rewriteExpr(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.expr = rewriteExpr(bLangNamedArgsExpression.expr);
        result = bLangNamedArgsExpression.expr;
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        visitCheckAndCheckPanicExpr(checkedExpr, false);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkedExpr) {
        visitCheckAndCheckPanicExpr(checkedExpr, true);
    }

    private void visitCheckAndCheckPanicExpr(BLangCheckedExpr checkedExpr, boolean isCheckPanic) {
        // If the usage of checking-keyword is redundant, treat it as a normal expression.
        if (checkedExpr.isRedundantChecking) {
            result = rewriteExpr(checkedExpr.expr);
            return;
        }

        //
        //  person p = check foo(); // foo(): person|error
        //
        //    ==>
        //
        //  person _$result$_;
        //  person|error _$$_ = foo();
        //  if _$$_ is person {
        //      person _$result$_ = <person>_$$_;
        //  } else {
        //      return _$$_ or throw _$$_;
        //  }

        Location pos = checkedExpr.pos;
        // Create a temporary variable to hold the checked expression result value e.g. _$$_
        String resultVarName = "_$result$_";
        BType resultType = checkedExpr.getBType();
        BLangSimpleVariable resultVar =
                ASTBuilderUtil.createVariable(pos, resultVarName, resultType, null,
                                              new BVarSymbol(0, names.fromString(resultVarName),
                                                             this.env.scope.owner.pkgID, resultType,
                                                             this.env.scope.owner, pos, VIRTUAL));
        BLangSimpleVariableDef resultVarDef = ASTBuilderUtil.createVariableDef(pos, resultVar);
        resultVarDef.desugared = true;
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(pos, resultVar.symbol);

        String checkedExprVarName = GEN_VAR_PREFIX.value;
        BType checkedExprType = checkedExpr.expr.getBType();
        BLangSimpleVariable checkedExprVar =
                ASTBuilderUtil.createVariable(pos, checkedExprVarName, checkedExprType,
                        checkedExpr.expr, new BVarSymbol(0, names.fromString(checkedExprVarName),
                                this.env.scope.owner.pkgID, checkedExprType,
                                this.env.scope.owner, pos, VIRTUAL));
        BLangSimpleVariableDef checkedExprVarDef = ASTBuilderUtil.createVariableDef(pos, checkedExprVar);
        BLangSimpleVarRef checkedExprVarRef = ASTBuilderUtil.createVariableRef(pos, checkedExprVar.symbol);

        BLangAssignment successAssignment = ASTBuilderUtil.createAssignmentStmt(pos, resultVarRef,
                createTypeCastExpr(checkedExprVarRef, resultType));
        BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(pos);
        ifBody.addStatement(successAssignment);

        BLangBlockStmt elseBody = getSafeErrorAssignment(pos, checkedExprVarRef, this.env.enclInvokable.symbol,
                        checkedExpr.equivalentErrorTypeList, isCheckPanic);

        BLangValueType checkedExprTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        checkedExprTypeNode.setBType(resultType);
        checkedExprTypeNode.typeKind = resultType.getKind();
        BLangIf ifStmt = ASTBuilderUtil.createIfElseStmt(pos,
                createTypeCheckExpr(pos, checkedExprVarRef, checkedExprTypeNode), ifBody, elseBody);
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos, new ArrayList<>() {{
            add(resultVarDef);
            add(checkedExprVarDef);
            add(ifStmt);
        }});

        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, resultVarRef);
        stmtExpr.setBType(resultType);
        result = rewriteExpr(stmtExpr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        final BLangTypeInit typeInit = ASTBuilderUtil.createEmptyTypeInit(serviceConstructorExpr.pos,
                serviceConstructorExpr.serviceNode.serviceClass.symbol.type);
        serviceConstructorExpr.serviceNode.annAttachments.forEach(attachment ->  rewrite(attachment, env));
        result = rewriteExpr(typeInit);
    }

    @Override
    public void visit(BLangObjectConstructorExpression bLangObjectConstructorExpression) {
        visit(bLangObjectConstructorExpression.classNode);
        bLangObjectConstructorExpression.classNode.annAttachments.forEach(attachment -> rewrite(attachment, env));
        result = rewriteExpr(bLangObjectConstructorExpression.typeInit);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {

        BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = annotAccessExpr.pos;
        binaryExpr.opKind = OperatorKind.ANNOT_ACCESS;
        binaryExpr.lhsExpr = annotAccessExpr.expr;
        binaryExpr.rhsExpr = ASTBuilderUtil.createLiteral(annotAccessExpr.pkgAlias.pos, symTable.stringType,
                annotAccessExpr.annotationSymbol.bvmAlias());
        binaryExpr.setBType(annotAccessExpr.getBType());
        binaryExpr.opSymbol = new BOperatorSymbol(names.fromString(OperatorKind.ANNOT_ACCESS.value()), null,
                new BInvokableType(Lists.of(binaryExpr.lhsExpr.getBType(),
                        binaryExpr.rhsExpr.getBType()),
                        annotAccessExpr.getBType(), null), null,
                symTable.builtinPos, VIRTUAL);
        result = rewriteExpr(binaryExpr);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        BLangExpression expr = typeTestExpr.expr;
        if (types.isValueType(expr.getBType())) {
            expr = addConversionExprIfRequired(expr, symTable.anyType);
        }
        if (typeTestExpr.isNegation) {
            BLangTypeTestExpr bLangTypeTestExpr = ASTBuilderUtil.createTypeTestExpr(typeTestExpr.pos,
                    typeTestExpr.expr, typeTestExpr.typeNode);
            BLangGroupExpr bLangGroupExpr = (BLangGroupExpr) TreeBuilder.createGroupExpressionNode();
            bLangGroupExpr.expression = bLangTypeTestExpr;
            bLangGroupExpr.setBType(typeTestExpr.getBType());
            BLangUnaryExpr unaryExpr = ASTBuilderUtil.createUnaryExpr(typeTestExpr.pos, bLangGroupExpr,
                    typeTestExpr.getBType(),
                    OperatorKind.NOT, null);
            result = rewriteExpr(unaryExpr);
            return;
        }
        typeTestExpr.expr = rewriteExpr(expr);
        typeTestExpr.typeNode = rewrite(typeTestExpr.typeNode, env);
        result = typeTestExpr;
    }

    @Override
    public void visit(BLangIsLikeExpr isLikeExpr) {
        isLikeExpr.expr = rewriteExpr(isLikeExpr.expr);
        result = isLikeExpr;
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.expr = rewriteExpr(bLangStatementExpression.expr);
        bLangStatementExpression.stmt = rewrite(bLangStatementExpression.stmt, env);
        result = bLangStatementExpression;
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        boolean prevIsVisitingQuery = this.isVisitingQuery;
        this.isVisitingQuery = true;
        BLangStatementExpression stmtExpr = queryDesugar.desugar(queryExpr, env, getVisibleXMLNSStmts(env));
        result = rewrite(stmtExpr, env);
        this.isVisitingQuery = prevIsVisitingQuery;
    }

    List<BLangStatement> getVisibleXMLNSStmts(SymbolEnv env) {
        Map<Name, BXMLNSSymbol> nameBXMLNSSymbolMap = symResolver.resolveAllNamespaces(env);
        return nameBXMLNSSymbolMap.keySet().stream()
                .map(key -> this.stmtsToBePropagatedToQuery.get(key))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        boolean prevIsVisitingQuery = this.isVisitingQuery;
        this.isVisitingQuery = true;
        BLangStatementExpression stmtExpr = queryDesugar.desugar(queryAction, env, getVisibleXMLNSStmts(env));
        result = rewrite(stmtExpr, env);
        this.isVisitingQuery = prevIsVisitingQuery;
    }

    @Override
    public void visit(BLangJSONArrayLiteral jsonArrayLiteral) {
        jsonArrayLiteral.exprs = rewriteExprs(jsonArrayLiteral.exprs);
        result = jsonArrayLiteral;
    }

    @Override
    public void visit(BLangConstant constant) {

        BConstantSymbol constSymbol = constant.symbol;
        BType refType = Types.getReferredType(constSymbol.literalType);
        if (refType.tag <= TypeTags.BOOLEAN || refType.tag == TypeTags.NIL) {
            if (refType.tag != TypeTags.NIL && (constSymbol.value == null ||
                            constSymbol.value.value == null)) {
                throw new IllegalStateException();
            }
            BLangLiteral literal = ASTBuilderUtil.createLiteral(constant.expr.pos, constSymbol.literalType,
                    constSymbol.value.value);
            constant.expr = rewriteExpr(literal);
        } else {
            constant.expr = rewriteExpr(constant.expr);
        }
        constant.annAttachments.forEach(attachment ->  rewrite(attachment, env));
        result = constant;
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        result = ignoreExpr;
    }

    @Override
    public void visit(BLangDynamicArgExpr dynamicParamExpr) {
        dynamicParamExpr.conditionalArgument = rewriteExpr(dynamicParamExpr.conditionalArgument);
        dynamicParamExpr.condition = rewriteExpr(dynamicParamExpr.condition);
        result = dynamicParamExpr;
    }

    @Override
    public void visit(BLangConstRef constantRef) {
        result = ASTBuilderUtil.createLiteral(constantRef.pos, constantRef.getBType(), constantRef.value);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        regExpTemplateLiteral.reDisjunction = rewriteExpr(regExpTemplateLiteral.reDisjunction);
        result = regExpTemplateLiteral;
    }

    @Override
    public void visit(BLangReDisjunction reDisjunction) {
        reDisjunction.sequenceList.forEach(this::rewriteExpr);
        result = reDisjunction;
    }

    @Override
    public void visit(BLangReSequence reSequence) {
        reSequence.termList.forEach(this::rewriteExpr);
        result = reSequence;
    }

    @Override
    public void visit(BLangReAssertion reAssertion) {
        reAssertion.assertion = rewriteExpr(reAssertion.assertion);
        result = reAssertion;
    }

    @Override
    public void visit(BLangReAtomQuantifier reAtomQuantifier) {
        BLangExpression reAtom = reAtomQuantifier.atom;
        if (symResolver.isReAtomNode(reAtom.getKind())) {
            reAtomQuantifier.atom = rewriteExpr(reAtom);
        } else {
            // Handle interpolations.
            reAtomQuantifier.atom = rewriteExpr(getToStringInvocationOnExpr(reAtom));
        }

        // Create empty quantifier.
        if (reAtomQuantifier.quantifier == null) {
            reAtomQuantifier.quantifier = ASTBuilderUtil.createEmptyQuantifier(reAtomQuantifier.pos,
                    symTable.anydataType, symTable.stringType);
        }
        reAtomQuantifier.quantifier = rewriteExpr(reAtomQuantifier.quantifier);
        result = reAtomQuantifier;
    }

    @Override
    public void visit(BLangReAtomCharOrEscape reAtomCharOrEscape) {
        reAtomCharOrEscape.charOrEscape = rewriteExpr(reAtomCharOrEscape.charOrEscape);
        result = reAtomCharOrEscape;
    }

    @Override
    public void visit(BLangReQuantifier reQuantifier) {
        reQuantifier.quantifier = rewriteExpr(reQuantifier.quantifier);
        // Create empty nonGreedyChar.
        if (reQuantifier.nonGreedyChar == null) {
            reQuantifier.nonGreedyChar = ASTBuilderUtil.createLiteral(reQuantifier.pos,
                    symTable.stringType, "");
        }
        reQuantifier.nonGreedyChar = rewriteExpr(reQuantifier.nonGreedyChar);
        result = reQuantifier;
    }

    @Override
    public void visit(BLangReCharacterClass reCharacterClass) {
        reCharacterClass.characterClassStart = rewriteExpr(reCharacterClass.characterClassStart);
        // Create empty negation.
        if (reCharacterClass.negation == null) {
            reCharacterClass.negation = ASTBuilderUtil.createLiteral(reCharacterClass.pos,
                    symTable.stringType, "");
        }
        reCharacterClass.negation = rewriteExpr(reCharacterClass.negation);
        // Create empty charSet.
        if (reCharacterClass.charSet == null) {
            reCharacterClass.charSet = ASTBuilderUtil.createEmptyCharSet(symTable.anydataType);
        }
        reCharacterClass.charSet = rewriteExpr(reCharacterClass.charSet);
        reCharacterClass.characterClassEnd = rewriteExpr(reCharacterClass.characterClassEnd);
        result = reCharacterClass;
    }

    @Override
    public void visit(BLangReCharSet reCharSet) {
        reCharSet.charSetAtoms.forEach(this::rewriteExpr);
        result = reCharSet;
    }

    @Override
    public void visit(BLangReCharSetRange reCharSetRange) {
        reCharSetRange.lhsCharSetAtom = rewriteExpr(reCharSetRange.lhsCharSetAtom);
        reCharSetRange.dash = rewriteExpr(reCharSetRange.dash);
        reCharSetRange.rhsCharSetAtom = rewriteExpr(reCharSetRange.rhsCharSetAtom);
        result = reCharSetRange;
    }

    @Override
    public void visit(BLangReCapturingGroups reCapturingGroups) {
        reCapturingGroups.openParen = rewriteExpr(reCapturingGroups.openParen);
        // Create empty flagExpr.
        if (reCapturingGroups.flagExpr == null) {
            reCapturingGroups.flagExpr = ASTBuilderUtil.createEmptyFlagExpression(reCapturingGroups.pos,
                    symTable.anydataType, symTable.stringType);
        }
        reCapturingGroups.flagExpr = rewriteExpr(reCapturingGroups.flagExpr);
        reCapturingGroups.disjunction = rewriteExpr(reCapturingGroups.disjunction);
        reCapturingGroups.closeParen = rewriteExpr(reCapturingGroups.closeParen);
        result = reCapturingGroups;
    }

    @Override
    public void visit(BLangReFlagExpression reFlagExpression) {
        reFlagExpression.questionMark = rewriteExpr(reFlagExpression.questionMark);
        // Create empty flagOnOff.
        if (reFlagExpression.flagsOnOff == null) {
            reFlagExpression.flagsOnOff = ASTBuilderUtil.createEmptyFlagOnOff(reFlagExpression.pos,
                    symTable.anydataType, symTable.stringType);
        }
        reFlagExpression.flagsOnOff = rewriteExpr(reFlagExpression.flagsOnOff);
        reFlagExpression.colon = rewriteExpr(reFlagExpression.colon);
        result = reFlagExpression;
    }

    @Override
    public void visit(BLangReFlagsOnOff reFlagsOnOff) {
        reFlagsOnOff.flags = rewriteExpr(reFlagsOnOff.flags);
        result = reFlagsOnOff;
    }

    // private functions

    // Foreach desugar helper method.
    BLangSimpleVariableDef getIteratorVariableDefinition(Location pos, BVarSymbol collectionSymbol,
                                                         BInvokableSymbol iteratorInvokableSymbol,
                                                         boolean isIteratorFuncFromLangLib) {


        BLangSimpleVarRef dataReference = ASTBuilderUtil.createVariableRef(pos, collectionSymbol);
        BLangInvocation iteratorInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        iteratorInvocation.pos = pos;
        iteratorInvocation.expr = dataReference;
        iteratorInvocation.symbol = iteratorInvokableSymbol;
        iteratorInvocation.setBType(iteratorInvokableSymbol.retType);
        iteratorInvocation.argExprs = Lists.of(dataReference);
        iteratorInvocation.requiredArgs = iteratorInvocation.argExprs;
        iteratorInvocation.langLibInvocation = isIteratorFuncFromLangLib;
        BVarSymbol iteratorSymbol = new BVarSymbol(0, names.fromString("$iterator$"), this.env.scope.owner.pkgID,
                                                   iteratorInvokableSymbol.retType, this.env.scope.owner, pos, VIRTUAL);

        // Note - any $iterator$ = $data$.iterator();
        BLangSimpleVariable iteratorVariable = ASTBuilderUtil.createVariable(pos, "$iterator$",
                iteratorInvokableSymbol.retType, iteratorInvocation, iteratorSymbol);
        return ASTBuilderUtil.createVariableDef(pos, iteratorVariable);
    }

    // Foreach desugar helper method.
    BLangSimpleVariableDef getIteratorNextVariableDefinition(Location pos, BType nillableResultType,
                                                             BVarSymbol iteratorSymbol,
                                                             BVarSymbol resultSymbol) {
        BLangInvocation nextInvocation = createIteratorNextInvocation(pos, iteratorSymbol);
        BLangSimpleVariable resultVariable = ASTBuilderUtil.createVariable(pos, "$result$",
                nillableResultType, nextInvocation, resultSymbol);
        return ASTBuilderUtil.createVariableDef(pos, resultVariable);
    }

    BLangInvocation createIteratorNextInvocation(Location pos, BVarSymbol iteratorSymbol) {
        BLangIdentifier nextIdentifier = ASTBuilderUtil.createIdentifier(pos, "next");
        BLangSimpleVarRef iteratorReferenceInNext = ASTBuilderUtil.createVariableRef(pos, iteratorSymbol);
        BInvokableSymbol nextFuncSymbol =
                getNextFunc((BObjectType) Types.getReferredType(iteratorSymbol.type)).symbol;
        BLangInvocation nextInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        nextInvocation.pos = pos;
        nextInvocation.name = nextIdentifier;
        nextInvocation.expr = iteratorReferenceInNext;
        nextInvocation.requiredArgs = Lists.of(ASTBuilderUtil.createVariableRef(pos, iteratorSymbol));
        nextInvocation.argExprs = nextInvocation.requiredArgs;
        nextInvocation.symbol = nextFuncSymbol;
        nextInvocation.setBType(nextFuncSymbol.retType);
        return nextInvocation;
    }

    private BAttachedFunction getNextFunc(BObjectType iteratorType) {
        BObjectTypeSymbol iteratorSymbol = (BObjectTypeSymbol) iteratorType.tsymbol;
        for (BAttachedFunction bAttachedFunction : iteratorSymbol.attachedFuncs) {
            if (bAttachedFunction.funcName.value.equals("next")) {
                return bAttachedFunction;
            }
        }
        return null;
    }

    // Foreach desugar helper method.
    BLangFieldBasedAccess getValueAccessExpression(Location location, BType varType,
                                                   BVarSymbol resultSymbol) {
        return getFieldAccessExpression(location, "value", varType, resultSymbol);
    }

    BLangFieldBasedAccess getFieldAccessExpression(Location pos, String fieldName, BType varType,
                                                   BVarSymbol resultSymbol) {
        BLangSimpleVarRef resultReferenceInVariableDef = ASTBuilderUtil.createVariableRef(pos, resultSymbol);
        BLangIdentifier valueIdentifier = ASTBuilderUtil.createIdentifier(pos, fieldName);

        BLangFieldBasedAccess fieldBasedAccessExpression =
                ASTBuilderUtil.createFieldAccessExpr(resultReferenceInVariableDef, valueIdentifier);
        fieldBasedAccessExpression.pos = pos;
        fieldBasedAccessExpression.setBType(varType);
        fieldBasedAccessExpression.originalType = fieldBasedAccessExpression.getBType();
        return fieldBasedAccessExpression;
    }

    private BlockFunctionBodyNode populateArrowExprBodyBlock(BLangArrowFunction bLangArrowFunction) {
        BlockFunctionBodyNode blockNode = TreeBuilder.createBlockFunctionBodyNode();
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = bLangArrowFunction.body.expr.pos;
        returnNode.setExpression(bLangArrowFunction.body.expr);
        blockNode.addStatement(returnNode);
        return blockNode;
    }

    protected BLangInvocation createInvocationNode(String functionName, List<BLangExpression> args, BType retType) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.setLiteral(false);
        name.setValue(functionName);
        invocationNode.name = name;
        invocationNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        // TODO: 2/28/18 need to find a good way to refer to symbols
        invocationNode.symbol = symTable.rootScope.lookup(new Name(functionName)).symbol;
        invocationNode.setBType(retType);
        invocationNode.requiredArgs = args;
        return invocationNode;
    }

    private BLangInvocation createLangLibInvocationNode(String functionName,
                                                        BLangExpression onExpr,
                                                        List<BLangExpression> args,
                                                        BType retType,
                                                        Location pos) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.setLiteral(false);
        name.setValue(functionName);
        name.pos = pos;
        invocationNode.name = name;
        invocationNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        invocationNode.expr = onExpr;
        invocationNode.symbol = symResolver.lookupLangLibMethod(onExpr.getBType(), names.fromString(functionName), env);

        ArrayList<BLangExpression> requiredArgs = new ArrayList<>();
        requiredArgs.add(onExpr);
        requiredArgs.addAll(args);
        invocationNode.requiredArgs = requiredArgs;

        invocationNode.setBType(retType != null ? retType : ((BInvokableSymbol) invocationNode.symbol).retType);
        invocationNode.langLibInvocation = true;
        return invocationNode;
    }

    private BLangInvocation createLangLibInvocationNode(String functionName,
                                                        List<BLangExpression> args,
                                                        BType retType,
                                                        Location pos) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.setLiteral(false);
        name.setValue(functionName);
        name.pos = pos;
        invocationNode.name = name;
        invocationNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        invocationNode.symbol = symResolver.lookupMethodInModule(symTable.langInternalModuleSymbol,
                names.fromString(functionName), env);

        ArrayList<BLangExpression> requiredArgs = new ArrayList<>();
        requiredArgs.addAll(args);
        invocationNode.requiredArgs = requiredArgs;

        invocationNode.setBType(retType != null ? retType : ((BInvokableSymbol) invocationNode.symbol).retType);
        invocationNode.langLibInvocation = true;
        return invocationNode;
    }

    private BLangArrayLiteral createArrayLiteralExprNode() {
        BLangArrayLiteral expr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        expr.exprs = new ArrayList<>();
        expr.setBType(new BArrayType(symTable.anyType));
        return expr;
    }

    private BFunctionPointerInvocation visitFunctionPointerInvocation(BLangInvocation iExpr) {
        BLangValueExpression expr;
        if (iExpr.expr == null) {
            expr = new BLangSimpleVarRef();
        } else {
            BLangFieldBasedAccess fieldBasedAccess = new BLangFieldBasedAccess();
            fieldBasedAccess.expr = iExpr.expr;
            fieldBasedAccess.field = iExpr.name;
            expr = fieldBasedAccess;
        }
        expr.symbol = iExpr.symbol;
        expr.setBType(iExpr.symbol.type);

        BLangExpression rewritten = rewriteExpr(expr);
        return new BFunctionPointerInvocation(iExpr, rewritten);
    }

    private BLangExpression visitCloneInvocation(BLangExpression expr, BType lhsType) {
        if (types.isValueType(expr.getBType())) {
            return expr;
        }
        if (expr.getBType().tag == TypeTags.ERROR) {
            return expr;
        }
        BLangInvocation cloneInvok = createLangLibInvocationNode("clone", expr, new ArrayList<>(), null, expr.pos);
        return addConversionExprIfRequired(cloneInvok, lhsType);
    }

    private BLangExpression visitCloneReadonly(BLangExpression expr, BType lhsType) {
        if (types.isValueType(expr.getBType())) {
            return expr;
        }
        if (expr.getBType().tag == TypeTags.ERROR) {
            return expr;
        }
        BLangInvocation cloneInvok = createLangLibInvocationNode("cloneReadOnly", expr, new ArrayList<>(),
                                                                 expr.getBType(),
                                                                 expr.pos);
        return addConversionExprIfRequired(cloneInvok, lhsType);
    }

    @SuppressWarnings("unchecked")
    <E extends BLangNode> E rewrite(E node, SymbolEnv env) {
        if (node == null) {
            return null;
        }

        if (node.desugared) {
            return node;
        }

        SymbolEnv previousEnv = this.env;
        this.env = env;

        node.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        resultNode.desugared = true;

        this.env = previousEnv;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    <E extends BLangExpression> E rewriteExpr(E node) {
        if (node == null) {
            return null;
        }

        if (node.desugared) {
            return node;
        }

        BLangExpression expr = node;
        if (node.impConversionExpr != null) {
            expr = node.impConversionExpr;
            node.impConversionExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        resultNode.desugared = true;

        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    <E extends BLangStatement> E rewrite(E statement, SymbolEnv env) {
        if (statement == null) {
            return null;
        }
        BLangStatement stmt = (BLangStatement) rewrite((BLangNode) statement, env);
        return (E) stmt;
    }

    private <E extends BLangStatement> List<E> rewriteStmt(List<E> nodeList, SymbolEnv env) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i), env));
        }
        return nodeList;
    }

    private <E extends BLangNode> List<E> rewrite(List<E> nodeList, SymbolEnv env) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i), env));
        }
        return nodeList;
    }

    private <E extends BLangExpression> List<E> rewriteExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewriteExpr(nodeList.get(i)));
        }
        return nodeList;
    }

    private BLangLiteral createStringLiteral(Location pos, String value) {
        BLangLiteral stringLit = new BLangLiteral(value, symTable.stringType);
        stringLit.pos = pos;
        return stringLit;
    }

    private BLangLiteral createIntLiteral(long value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.setBType(symTable.intType);
        return literal;
    }

    private BLangLiteral createByteLiteral(Location pos, Byte value) {
        BLangLiteral byteLiteral = new BLangLiteral(Byte.toUnsignedInt(value), symTable.byteType);
        byteLiteral.pos = pos;
        return byteLiteral;
    }

    private BLangExpression createTypeCastExpr(BLangExpression expr, BType targetType) {
        if (types.isSameType(expr.getBType(), targetType)) {
            return expr;
        }

        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversionExpr.pos = expr.pos;
        conversionExpr.expr = expr;
        conversionExpr.setBType(targetType);
        conversionExpr.targetType = targetType;
        conversionExpr.internal = true;
        return conversionExpr;
    }

    private BType getElementType(BType bType) {
        BType type = Types.getReferredType(bType);
        if (type.tag != TypeTags.ARRAY) {
            return bType;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    // TODO: See if this is needed at all. Can't this be done when rewriting the function body?
    private void addReturnIfNotPresent(BLangInvokableNode invokableNode) {
        if (Symbols.isNative(invokableNode.symbol) ||
                (invokableNode.hasBody() && invokableNode.body.getKind() != NodeKind.BLOCK_FUNCTION_BODY)) {
            return;
        }
        //This will only check whether last statement is a return and just add a return statement.
        //This won't analyse if else blocks etc. to see whether return statements are present.
        BLangBlockFunctionBody funcBody = (BLangBlockFunctionBody) invokableNode.body;
        if (invokableNode.symbol.type.getReturnType().isNullable() && (funcBody.stmts.size() < 1
                || funcBody.stmts.get(funcBody.stmts.size() - 1).getKind() != NodeKind.RETURN)) {
            Location invPos = invokableNode.pos;
            Location returnStmtPos;
            if (invPos != null && !invokableNode.name.value.contains(GENERATED_INIT_SUFFIX.value)) {
                returnStmtPos = new BLangDiagnosticLocation(invPos.lineRange().filePath(),
                        invPos.lineRange().endLine().line(),
                        invPos.lineRange().endLine().line(),
                        invPos.lineRange().startLine().offset(),
                        invPos.lineRange().startLine().offset(), 0, 0);
            } else {
                returnStmtPos = null;
            }
            BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(returnStmtPos, symTable.nilType);
            funcBody.addStatement(returnStmt);
        }
    }

    /**
     * Reorder the invocation arguments to match the original function signature.
     *
     * @param iExpr Function invocation expressions to reorder the arguments
     */
    private void reorderArguments(BLangInvocation iExpr) {
        BSymbol symbol = iExpr.symbol;

        if (symbol == null || Types.getReferredType(symbol.type).tag != TypeTags.INVOKABLE) {
            return;
        }

        BInvokableSymbol invokableSymbol = (BInvokableSymbol) symbol;

        List<BLangExpression> restArgs = iExpr.restArgs;
        int originalRequiredArgCount = iExpr.requiredArgs.size();

        // Constructs used when the vararg provides args for required/defaultable params.
        BLangSimpleVarRef varargRef = null;
        BLangBlockStmt blockStmt = null;
        BType varargVarType = null;

        int restArgCount = restArgs.size();

        if (restArgCount > 0 &&
                restArgs.get(restArgCount - 1).getKind() == NodeKind.REST_ARGS_EXPR &&
                originalRequiredArgCount < invokableSymbol.params.size()) {
            // All or part of the args for the required and defaultable parameters are provided via the vararg.
            // We have to first evaluate the vararg's expression, define a variable, and pass a reference to it
            // to use for member access when adding such required arguments from the vararg.
            BLangExpression expr = ((BLangRestArgsExpression) restArgs.get(restArgCount - 1)).expr;
            Location varargExpPos = expr.pos;
            varargVarType = expr.getBType();
            String varargVarName = DESUGARED_VARARG_KEY + UNDERSCORE + this.varargCount++;

            BVarSymbol varargVarSymbol = new BVarSymbol(0, names.fromString(varargVarName), this.env.scope.owner.pkgID,
                                                        varargVarType, this.env.scope.owner, varargExpPos, VIRTUAL);
            varargRef = ASTBuilderUtil.createVariableRef(varargExpPos, varargVarSymbol);

            BLangSimpleVariable var = createVariable(varargExpPos, varargVarName, varargVarType, expr, varargVarSymbol);

            BLangSimpleVariableDef varDef = ASTBuilderUtil.createVariableDef(varargExpPos);
            varDef.var = var;
            varDef.setBType(varargVarType);

            blockStmt = createBlockStmt(varargExpPos);
            blockStmt.stmts.add(varDef);
        }

        if (!invokableSymbol.params.isEmpty()) {
            // Re-order the arguments
            reorderNamedArgs(iExpr, invokableSymbol, varargRef);
        }

        // There are no rest args at all or args for the rest param are only given as individual args (i.e., no vararg).
        if (restArgCount == 0 || restArgs.get(restArgCount - 1).getKind() != NodeKind.REST_ARGS_EXPR) {
            if (invokableSymbol.restParam == null) {
                return;
            }

            BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
            List<BLangExpression> exprs = new ArrayList<>();

            BArrayType arrayType = (BArrayType) invokableSymbol.restParam.type;
            BType elemType = arrayType.eType;

            for (BLangExpression restArg : restArgs) {
                exprs.add(addConversionExprIfRequired(restArg, elemType));
            }

            arrayLiteral.exprs = exprs;
            arrayLiteral.setBType(arrayType);

            if (restArgCount != 0) {
                iExpr.restArgs = new ArrayList<>();
            }

            iExpr.restArgs.add(arrayLiteral);
            return;
        }

        // There are no individual rest args, but there is a single vararg.
        if (restArgCount == 1 && restArgs.get(0).getKind() == NodeKind.REST_ARGS_EXPR) {

            // If the number of expressions in `iExpr.requiredArgs` hasn't changed, the vararg only contained
            // arguments for the rest parameter.
            if (iExpr.requiredArgs.size() == originalRequiredArgCount) {
                return;
            }

            // Args for some or all of the required/defaultable parameters have been provided via the vararg.
            // Remove the first required arg and add a statement expression instead.
            // The removed first arg is set as the expression and the vararg expression definition is set as
            // statement(s).
            BLangExpression firstNonRestArg = iExpr.requiredArgs.remove(0);
            BLangStatementExpression stmtExpression = createStatementExpression(blockStmt, firstNonRestArg);
            BType type = firstNonRestArg.impConversionExpr == null ?
                                              firstNonRestArg.getBType() : firstNonRestArg.impConversionExpr.targetType;
            stmtExpression.setBType(type);
            iExpr.requiredArgs.add(0, stmtExpression);

            // If there's no rest param, the vararg only provided for required/defaultable params.
            if (invokableSymbol.restParam == null) {
                restArgs.remove(0);
                return;
            }

            // If there is a rest param, the vararg could provide for the rest param too.
            // Create a new array with just the members of the original vararg specified for the rest param.
            // All the values in the original list passed as a vararg, that were not passed for a
            // required/defaultable parameter are added to the new array.
            BLangRestArgsExpression restArgsExpression = (BLangRestArgsExpression) restArgs.remove(0);
            BArrayType restParamType = (BArrayType) invokableSymbol.restParam.type;
            if (Types.getReferredType(restArgsExpression.getBType()).tag == TypeTags.RECORD) {
                BLangExpression expr = ASTBuilderUtil.createEmptyArrayLiteral(invokableSymbol.pos, restParamType);
                restArgs.add(expr);
                return;
            }
            Location pos = restArgsExpression.pos;

            BLangArrayLiteral newArrayLiteral = createArrayLiteralExprNode();
            newArrayLiteral.setBType(restParamType);

            String name = DESUGARED_VARARG_KEY + UNDERSCORE + this.varargCount++;
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID,
                                                  restParamType, this.env.scope.owner, pos, VIRTUAL);
            BLangSimpleVarRef arrayVarRef = ASTBuilderUtil.createVariableRef(pos, varSymbol);

            BLangSimpleVariable var = createVariable(pos, name, restParamType, newArrayLiteral, varSymbol);
            BLangSimpleVariableDef varDef = ASTBuilderUtil.createVariableDef(pos);
            varDef.var = var;
            varDef.setBType(restParamType);

            BLangLiteral startIndex = createIntLiteral(invokableSymbol.params.size() - originalRequiredArgCount);
            BLangInvocation lengthInvocation = createLengthInvocation(pos, varargRef);
            BLangInvocation intRangeInvocation = replaceWithIntRange(pos, startIndex,
                                                                     getModifiedIntRangeEndExpr(lengthInvocation));

            BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
            foreach.pos = pos;
            foreach.collection = intRangeInvocation;
            types.setForeachTypedBindingPatternType(foreach);

            final BLangSimpleVariable foreachVariable = ASTBuilderUtil.createVariable(pos, "$foreach$i",
                                                                                      foreach.varType);
            foreachVariable.symbol = new BVarSymbol(0, names.fromIdNode(foreachVariable.name),
                                                    this.env.scope.owner.pkgID, foreachVariable.getBType(),
                                                    this.env.scope.owner, pos, VIRTUAL);
            BLangSimpleVarRef foreachVarRef = ASTBuilderUtil.createVariableRef(pos, foreachVariable.symbol);
            foreach.variableDefinitionNode = ASTBuilderUtil.createVariableDef(pos, foreachVariable);
            foreach.isDeclaredWithVar = true;
            BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);

            BLangIndexBasedAccess valueExpr = ASTBuilderUtil.createIndexAccessExpr(varargRef, foreachVarRef);

            BType refType = Types.getReferredType(varargVarType);
            if (refType.tag == TypeTags.ARRAY) {
                BArrayType arrayType = (BArrayType) refType;
                if (arrayType.state == BArrayState.CLOSED &&
                        arrayType.size == (iExpr.requiredArgs.size() - originalRequiredArgCount)) {
                    // If the array was a closed array that provided only for the non rest params, set the rest param
                    // type as the element type to satisfy code gen. The foreach will not be executed at runtime.
                    valueExpr.setBType(restParamType.eType);
                } else {
                    valueExpr.setBType(arrayType.eType);
                }
            } else {
                valueExpr.setBType(symTable.anyOrErrorType); // Use any|error for tuple since it's a ref array.
            }

            BLangExpression pushExpr = addConversionExprIfRequired(valueExpr, restParamType.eType);
            BLangExpressionStmt expressionStmt = createExpressionStmt(pos, foreachBody);
            BLangInvocation pushInvocation = createLangLibInvocationNode(PUSH_LANGLIB_METHOD, arrayVarRef,
                                                                         List.of(pushExpr),
                                                                         restParamType, pos);
            pushInvocation.restArgs.add(pushInvocation.requiredArgs.remove(1));
            expressionStmt.expr = pushInvocation;
            foreach.body = foreachBody;
            BLangBlockStmt newArrayBlockStmt = createBlockStmt(pos);
            newArrayBlockStmt.addStatement(varDef);
            newArrayBlockStmt.addStatement(foreach);

            BLangStatementExpression newArrayStmtExpression = createStatementExpression(newArrayBlockStmt, arrayVarRef);
            newArrayStmtExpression.setBType(restParamType);

            restArgs.add(addConversionExprIfRequired(newArrayStmtExpression, restParamType));
            return;
        }

        // Now the `restArgs` list has both individual rest args and a vararg, all for the rest param.
        // We create a new array with the individual rest args and push the list passed as the vararg to it.
        BArrayType restParamType = (BArrayType) invokableSymbol.restParam.type;

        BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        arrayLiteral.setBType(restParamType);

        BType elemType = restParamType.eType;
        Location pos = restArgs.get(0).pos;

        List<BLangExpression> exprs = new ArrayList<>();

        for (int i = 0; i < restArgCount - 1; i++) {
            exprs.add(addConversionExprIfRequired(restArgs.get(i), elemType));
        }
        arrayLiteral.exprs = exprs;

        BLangRestArgsExpression pushRestArgsExpr = (BLangRestArgsExpression) TreeBuilder.createVarArgsNode();
        pushRestArgsExpr.pos = pos;
        pushRestArgsExpr.expr = restArgs.remove(restArgCount - 1);

        String name = DESUGARED_VARARG_KEY + UNDERSCORE + this.varargCount++;
        BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID, restParamType,
                                              this.env.scope.owner, pos, VIRTUAL);
        BLangSimpleVarRef arrayVarRef = ASTBuilderUtil.createVariableRef(pos, varSymbol);

        BLangSimpleVariable var = createVariable(pos, name, restParamType, arrayLiteral, varSymbol);
        BLangSimpleVariableDef varDef = ASTBuilderUtil.createVariableDef(pos);
        varDef.var = var;
        varDef.setBType(restParamType);

        BLangBlockStmt pushBlockStmt = createBlockStmt(pos);
        pushBlockStmt.stmts.add(varDef);

        BLangExpressionStmt expressionStmt = createExpressionStmt(pos, pushBlockStmt);
        BLangInvocation pushInvocation = createLangLibInvocationNode(PUSH_LANGLIB_METHOD, arrayVarRef,
                                                                     new ArrayList<BLangExpression>() {{
                                                                         add(pushRestArgsExpr);
                                                                     }}, restParamType, pos);
        pushInvocation.restArgs.add(pushInvocation.requiredArgs.remove(1));
        expressionStmt.expr = pushInvocation;

        BLangStatementExpression stmtExpression = createStatementExpression(pushBlockStmt, arrayVarRef);
        stmtExpression.setBType(restParamType);

        iExpr.restArgs = new ArrayList<BLangExpression>(1) {{ add(stmtExpression); }};
    }

    private void reorderNamedArgs(BLangInvocation iExpr, BInvokableSymbol invokableSymbol, BLangExpression varargRef) {
        List<BLangExpression> args = new ArrayList<>();
        Map<String, BLangExpression> namedArgs = new LinkedHashMap<>();
        iExpr.requiredArgs.stream()
                .filter(expr -> expr.getKind() == NodeKind.NAMED_ARGS_EXPR)
                .forEach(expr -> namedArgs.put(((NamedArgNode) expr).getName().value, expr));

        List<BVarSymbol> params = invokableSymbol.params;
        List<BLangRecordLiteral> incRecordLiterals = new ArrayList<>();
        BLangRecordLiteral incRecordParamAllowAdditionalFields = null;

        int varargIndex = 0;

        BType varargType = null;
        boolean tupleTypedVararg = false;

        if (varargRef != null) {
            varargType = Types.getReferredType(varargRef.getBType());
            tupleTypedVararg = varargType.tag == TypeTags.TUPLE;
        }

        // Iterate over the required args.
        for (int i = 0; i < params.size(); i++) {
            BVarSymbol param = params.get(i);
            if (iExpr.requiredArgs.size() > i && iExpr.requiredArgs.get(i).getKind() != NodeKind.NAMED_ARGS_EXPR) {
                // If a positional arg is given in the same position, it will be used.
                args.add(iExpr.requiredArgs.get(i));
            } else if (namedArgs.containsKey(param.name.value)) {
                // Else check if named arg is given.
                args.add(namedArgs.remove(param.name.value));
            } else if (param.getFlags().contains(Flag.INCLUDED)) {
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
                BType paramType = param.type;
                recordLiteral.setBType(paramType);
                args.add(recordLiteral);
                incRecordLiterals.add(recordLiteral);
                if (((BRecordType) Types.getReferredType(paramType)).restFieldType != symTable.noType) {
                    incRecordParamAllowAdditionalFields = recordLiteral;
                }
            } else if (varargRef == null) {
                // Else create a dummy expression with an ignore flag.
                BLangExpression expr = new BLangIgnoreExpr();
                expr.setBType(param.type);
                args.add(expr);
            } else {
                // If a vararg is provided, no parameter defaults are added and no named args are specified.
                // Thus, any missing args should come from the vararg.
                if (Types.getReferredType(varargRef.getBType()).tag == TypeTags.RECORD) {
                    if (param.isDefaultable) {
                        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(varargRef.pos);
                        BLangInvocation hasKeyInvocation = createLangLibInvocationNode(HAS_KEY, varargRef,
                                List.of(createStringLiteral(param.pos, param.name.value)), null, varargRef.pos);
                        BLangSimpleVariableDef variableDef = createVarDef("$hasKey$", hasKeyInvocation.getBType(),
                                                                          hasKeyInvocation, hasKeyInvocation.pos);
                        blockStmt.stmts.add(variableDef);
                        BLangSimpleVarRef simpleVarRef = ASTBuilderUtil.createVariableRef(variableDef.pos,
                                                                                          variableDef.var.symbol);
                        BLangExpression indexExpr = rewriteExpr(createStringLiteral(param.pos, param.name.value));
                        BLangIndexBasedAccess memberAccessExpr =
                                ASTBuilderUtil.createMemberAccessExprNode(param.type, varargRef, indexExpr);
                        BLangExpression ignoreExpr = ASTBuilderUtil.createIgnoreExprNode(param.type);
                        BLangTernaryExpr ternaryExpr = ASTBuilderUtil.createTernaryExprNode(param.type, simpleVarRef,
                                                                                          memberAccessExpr, ignoreExpr);
                        BLangDynamicArgExpr dynamicArgExpr =
                                ASTBuilderUtil.createDynamicParamExpression(simpleVarRef, param, ternaryExpr);
                        BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, dynamicArgExpr);
                        stmtExpr.setBType(dynamicArgExpr.getBType());
                        args.add(rewriteExpr(stmtExpr));
                    } else {
                        BLangFieldBasedAccess fieldBasedAccessExpression =
                                ASTBuilderUtil.createFieldAccessExpr(varargRef,
                                        ASTBuilderUtil.createIdentifier(param.pos, param.name.value));
                        fieldBasedAccessExpression.setBType(param.type);
                        args.add(fieldBasedAccessExpression);
                    }
                } else {
                    BLangExpression indexExpr = rewriteExpr(createIntLiteral(varargIndex));
                    BType memberAccessExprType = tupleTypedVararg ?
                            ((BTupleType) varargType).tupleTypes.get(varargIndex) : ((BArrayType) varargType).eType;
                    args.add(addConversionExprIfRequired(ASTBuilderUtil.createMemberAccessExprNode(memberAccessExprType,
                             varargRef, indexExpr), param.type));
                    varargIndex++;
                }
            }
        }
        if (namedArgs.size() > 0) {
            setFieldsForIncRecordLiterals(namedArgs, incRecordLiterals, incRecordParamAllowAdditionalFields);
        }
        iExpr.requiredArgs = args;
    }

    private void setFieldsForIncRecordLiterals(Map<String, BLangExpression> namedArgs,
                                               List<BLangRecordLiteral> incRecordLiterals,
                                               BLangRecordLiteral incRecordParamAllowAdditionalFields) {
        for (String name : namedArgs.keySet()) {
            boolean isAdditionalField = true;
            BLangNamedArgsExpression expr = (BLangNamedArgsExpression) namedArgs.get(name);
            for (BLangRecordLiteral recordLiteral : incRecordLiterals) {
                LinkedHashMap<String, BField> fields =
                        ((BRecordType) Types.getReferredType(recordLiteral.getBType())).fields;
                if (fields.containsKey(name) &&
                        Types.getReferredType(fields.get(name).type).tag != TypeTags.NEVER) {
                    isAdditionalField = false;
                    createAndAddRecordFieldForIncRecordLiteral(recordLiteral, expr);
                    break;
                }
            }
            if (isAdditionalField) {
                createAndAddRecordFieldForIncRecordLiteral(incRecordParamAllowAdditionalFields, expr);
            }
        }
    }

    private void createAndAddRecordFieldForIncRecordLiteral(BLangRecordLiteral recordLiteral,
                                                            BLangNamedArgsExpression expr) {
        BLangSimpleVarRef varRef = new BLangSimpleVarRef();
        varRef.variableName = expr.name;
        BLangRecordLiteral.BLangRecordKeyValueField recordKeyValueField = ASTBuilderUtil.
                createBLangRecordKeyValue(varRef, expr.expr);
        recordLiteral.fields.add(recordKeyValueField);
    }

    private BLangBlockStmt getSafeErrorAssignment(Location location, BLangSimpleVarRef ref,
                                                  BSymbol invokableSymbol,
                                                  List<BType> equivalentErrorTypes,
                                                  boolean isCheckPanicExpr) {
        // From here onwards we assume that this function has only one return type
        // Owner of the variable symbol must be an invokable symbol
        BType enclosingFuncReturnType = Types.getReferredType(((BInvokableType) invokableSymbol.type).retType);
        Set<BType> returnTypeSet = enclosingFuncReturnType.tag == TypeTags.UNION ?
                ((BUnionType) enclosingFuncReturnType).getMemberTypes() :
                new LinkedHashSet<>() {{
                    add(enclosingFuncReturnType);
                }};

        // For each error type, there has to be at least one equivalent return type in the enclosing function
        boolean returnOnError = equivalentErrorTypes.stream()
                .allMatch(errorType -> returnTypeSet.stream()
                        .anyMatch(retType -> types.isAssignable(errorType, retType)));

        String patternFailureCaseVarName = GEN_VAR_PREFIX.value + "t_failure";
        BLangSimpleVariable errorVar =
                ASTBuilderUtil.createVariable(location, patternFailureCaseVarName, symTable.errorType,
                                                createTypeCastExpr(ref, symTable.errorType),
                                              new BVarSymbol(0, names.fromString(patternFailureCaseVarName),
                                                             this.env.scope.owner.pkgID, symTable.errorType,
                                                             this.env.scope.owner, location, VIRTUAL));

        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(location);
        BLangSimpleVariableDef errorVarDef = ASTBuilderUtil.createVariableDef(location, errorVar);
        blockStmt.addStatement(errorVarDef);
        BLangVariableReference errorVarRef = ASTBuilderUtil.createVariableRef(location, errorVar.symbol);
        if (!isCheckPanicExpr && (returnOnError || this.onFailClause != null)) {
            // fail e;
            BLangFail failStmt = (BLangFail) TreeBuilder.createFailNode();
            failStmt.pos = location;
            failStmt.expr = errorVarRef;
            blockStmt.addStatement(failStmt);
            if (returnOnError && this.shouldReturnErrors) {
                BLangReturn errorReturn = ASTBuilderUtil.createReturnStmt(location, rewrite(errorVarRef, env));
                errorReturn.desugared = true;
                failStmt.exprStmt = errorReturn;
            }
        } else {
            // throw e
            BLangPanic panicNode = (BLangPanic) TreeBuilder.createPanicNode();
            panicNode.pos = location;
            panicNode.expr = errorVarRef;
            blockStmt.addStatement(panicNode);
        }

        return blockStmt;
    }

    BLangExpression addConversionExprIfRequired(BLangExpression expr, BType lhsType) {
        if (lhsType.tag == TypeTags.NONE) {
            return expr;
        }

        BType rhsType = expr.getBType();
        if (types.isSameType(rhsType, lhsType)) {
            return expr;
        }

        types.setImplicitCastExpr(expr, rhsType, lhsType);
        if (expr.impConversionExpr != null) {
            BLangExpression impConversionExpr = expr.impConversionExpr;
            expr.impConversionExpr = null;
            return impConversionExpr;
        }

        if (lhsType.tag == TypeTags.JSON && rhsType.tag == TypeTags.NIL) {
            return expr;
        }

        if (lhsType.tag == TypeTags.NIL && rhsType.isNullable()) {
            return expr;
        }

        if (lhsType.tag == TypeTags.ARRAY && rhsType.tag == TypeTags.TUPLE) {
            return expr;
        }

        // Create a type cast expression
        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr)
                TreeBuilder.createTypeConversionNode();
        conversionExpr.expr = expr;
        conversionExpr.targetType = lhsType;
        conversionExpr.setBType(lhsType);
        conversionExpr.pos = expr.pos;
        conversionExpr.checkTypes = false;
        conversionExpr.internal = true;
        return conversionExpr;
    }

    private BType getStructuredBindingPatternType(BLangVariable bindingPatternVariable) {
        if (NodeKind.TUPLE_VARIABLE == bindingPatternVariable.getKind()) {
            BLangTupleVariable tupleVariable = (BLangTupleVariable) bindingPatternVariable;
            List<BType> memberTypes = new ArrayList<>();
            for (int i = 0; i < tupleVariable.memberVariables.size(); i++) {
                memberTypes.add(getStructuredBindingPatternType(tupleVariable.memberVariables.get(i)));
            }
            BTupleType tupleType = new BTupleType(memberTypes);
            if (tupleVariable.restVariable != null) {
                BArrayType restArrayType = (BArrayType) getStructuredBindingPatternType(tupleVariable.restVariable);
                tupleType.restType = restArrayType.eType;
            }
            return tupleType;
        }

        if (NodeKind.RECORD_VARIABLE == bindingPatternVariable.getKind()) {
            BLangRecordVariable recordVariable = (BLangRecordVariable) bindingPatternVariable;

            BRecordTypeSymbol recordSymbol =
                    Symbols.createRecordSymbol(0, names.fromString("$anonRecordType$" + UNDERSCORE + recordCount++),
                                               env.enclPkg.symbol.pkgID, null, env.scope.owner, recordVariable.pos,
                                               VIRTUAL);
            recordSymbol.initializerFunc = createRecordInitFunc();
            recordSymbol.scope = new Scope(recordSymbol);
            recordSymbol.scope.define(
                    names.fromString(recordSymbol.name.value + "." + recordSymbol.initializerFunc.funcName.value),
                    recordSymbol.initializerFunc.symbol);

            LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
            List<BLangSimpleVariable> typeDefFields = new ArrayList<>();

            for (int i = 0; i < recordVariable.variableList.size(); i++) {
                String fieldNameStr = recordVariable.variableList.get(i).key.value;
                Name fieldName = names.fromString(fieldNameStr);
                BType fieldType = getStructuredBindingPatternType(
                        recordVariable.variableList.get(i).valueBindingPattern);
                BVarSymbol fieldSymbol = new BVarSymbol(Flags.REQUIRED, fieldName, env.enclPkg.symbol.pkgID, fieldType,
                                                        recordSymbol, bindingPatternVariable.pos, VIRTUAL);

                //TODO check below field position
                fields.put(fieldName.value, new BField(fieldName, bindingPatternVariable.pos, fieldSymbol));
                typeDefFields.add(ASTBuilderUtil.createVariable(null, fieldNameStr, fieldType, null, fieldSymbol));
                recordSymbol.scope.define(fieldName, fieldSymbol);
            }

            BRecordType recordVarType = new BRecordType(recordSymbol);
            recordVarType.fields = fields;

            // if rest param is null we treat it as an open record with anydata rest param
            recordVarType.restFieldType = recordVariable.restParam != null ?
                        ((BRecordType) recordVariable.restParam.getBType()).restFieldType :
                    symTable.anydataType;
            recordSymbol.type = recordVarType;
            recordVarType.tsymbol = recordSymbol;

            BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(typeDefFields,
                                                                                           recordVarType,
                                                                                           bindingPatternVariable.pos);
            recordTypeNode.initFunction =
                    rewrite(TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names, symTable),
                            env);
            TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordVarType, recordSymbol, recordTypeNode, env);

            return recordVarType;
        }

        if (NodeKind.ERROR_VARIABLE == bindingPatternVariable.getKind()) {
            BLangErrorVariable errorVariable = (BLangErrorVariable) bindingPatternVariable;
            BErrorTypeSymbol errorTypeSymbol = new BErrorTypeSymbol(
                    SymTag.ERROR,
                    Flags.PUBLIC,
                    names.fromString("$anonErrorType$" + UNDERSCORE + errorCount++),
                    env.enclPkg.symbol.pkgID,
                    null, null, errorVariable.pos, VIRTUAL);
            BType detailType;
            if ((errorVariable.detail == null || errorVariable.detail.isEmpty()) && errorVariable.restDetail != null) {
                detailType = symTable.detailType;
            } else {
                detailType = createDetailType(errorVariable.detail, errorVariable.restDetail, errorCount++,
                                              errorVariable.pos);

                BLangRecordTypeNode recordTypeNode = createRecordTypeNode(errorVariable, (BRecordType) detailType);
                recordTypeNode.initFunction = TypeDefBuilderHelper
                        .createInitFunctionForRecordType(recordTypeNode, env, names, symTable);
                TypeDefBuilderHelper.createTypeDefinitionForTSymbol(detailType, detailType.tsymbol,
                        recordTypeNode, env);
            }
            BErrorType errorType = new BErrorType(errorTypeSymbol, detailType);
            errorTypeSymbol.type = errorType;

            TypeDefBuilderHelper.createTypeDefinitionForTSymbol(errorType, errorTypeSymbol,
                    createErrorTypeNode(errorType), env);
            return errorType;
        }

        return bindingPatternVariable.getBType();
    }

    private BLangRecordTypeNode createRecordTypeNode(BLangErrorVariable errorVariable, BRecordType detailType) {
        List<BLangSimpleVariable> fieldList = new ArrayList<>();
        for (BLangErrorVariable.BLangErrorDetailEntry field : errorVariable.detail) {
            BVarSymbol symbol = field.valueBindingPattern.symbol;
            if (symbol == null) {
                symbol = new BVarSymbol(Flags.PUBLIC, names.fromString(field.key.value + "$"),
                                        this.env.enclPkg.packageID, symTable.pureType, null,
                                        field.valueBindingPattern.pos, VIRTUAL);
            }
            BLangSimpleVariable fieldVar = ASTBuilderUtil.createVariable(
                    field.valueBindingPattern.pos,
                    symbol.name.value,
                    field.valueBindingPattern.getBType(),
                    field.valueBindingPattern.expr,
                    symbol);
            fieldList.add(fieldVar);
        }
        return TypeDefBuilderHelper.createRecordTypeNode(fieldList, detailType, errorVariable.pos);
    }

    private BType createDetailType(List<BLangErrorVariable.BLangErrorDetailEntry> detail,
                                   BLangSimpleVariable restDetail, int errorNo, Location pos) {
        BRecordType detailRecordType = createAnonRecordType(pos);

        if (restDetail == null) {
            detailRecordType.sealed = true;
        }

        for (BLangErrorVariable.BLangErrorDetailEntry detailEntry : detail) {
            Name fieldName = names.fromIdNode(detailEntry.key);
            BType fieldType = getStructuredBindingPatternType(detailEntry.valueBindingPattern);
            BVarSymbol fieldSym = new BVarSymbol(Flags.PUBLIC, fieldName, detailRecordType.tsymbol.pkgID, fieldType,
                    detailRecordType.tsymbol, detailEntry.key.pos, VIRTUAL);
            detailRecordType.fields.put(fieldName.value, new BField(fieldName, detailEntry.key.pos, fieldSym));
            detailRecordType.tsymbol.scope.define(fieldName, fieldSym);
        }

        return detailRecordType;
    }

    private BRecordType createAnonRecordType(Location pos) {
        BRecordTypeSymbol detailRecordTypeSymbol = new BRecordTypeSymbol(
                SymTag.RECORD,
                Flags.PUBLIC,
                names.fromString(anonModelHelper.getNextRecordVarKey(env.enclPkg.packageID)),
                env.enclPkg.symbol.pkgID, null, null, pos, VIRTUAL);

        detailRecordTypeSymbol.initializerFunc = createRecordInitFunc();
        detailRecordTypeSymbol.scope = new Scope(detailRecordTypeSymbol);
        detailRecordTypeSymbol.scope.define(
                names.fromString(detailRecordTypeSymbol.name.value + "." +
                        detailRecordTypeSymbol.initializerFunc.funcName.value),
                detailRecordTypeSymbol.initializerFunc.symbol);

        BRecordType detailRecordType = new BRecordType(detailRecordTypeSymbol);
        detailRecordType.restFieldType = symTable.anydataType;
        return detailRecordType;
    }

    private BAttachedFunction createRecordInitFunc() {
        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        return new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol, bInvokableType, symTable.builtinPos);
    }

    BLangErrorType createErrorTypeNode(BErrorType errorType) {
        BLangErrorType errorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorTypeNode.setBType(errorType);
        return errorTypeNode;
    }

    private BLangExpression createBinaryExpression(Location pos, BLangSimpleVarRef varRef,
                                                   BLangExpression expression) {

        BLangBinaryExpr binaryExpr;
        if (NodeKind.GROUP_EXPR == expression.getKind()) {
            return createBinaryExpression(pos, varRef, ((BLangGroupExpr) expression).expression);
        }

        if (NodeKind.BINARY_EXPR == expression.getKind()) {
            binaryExpr = (BLangBinaryExpr) expression;
            BLangExpression lhsExpr = createBinaryExpression(pos, varRef, binaryExpr.lhsExpr);
            BLangExpression rhsExpr = createBinaryExpression(pos, varRef, binaryExpr.rhsExpr);

            binaryExpr = ASTBuilderUtil.createBinaryExpr(pos, lhsExpr, rhsExpr, symTable.booleanType, OperatorKind.OR,
                    (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.OR, symTable.booleanType, symTable.booleanType));
        } else if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                && ((BLangSimpleVarRef) expression).variableName.value.equals(IGNORE.value)) {
            BLangValueType anyType = (BLangValueType) TreeBuilder.createValueTypeNode();
            anyType.setBType(symTable.anyType);
            anyType.typeKind = TypeKind.ANY;
            return ASTBuilderUtil.createTypeTestExpr(pos, varRef, anyType);
        } else {
            binaryExpr = ASTBuilderUtil
                    .createBinaryExpr(pos, varRef, expression, symTable.booleanType, OperatorKind.EQUAL, null);
            BSymbol opSymbol = symResolver.resolveBinaryOperator(OperatorKind.EQUAL, varRef.getBType(),
                                                                 expression.getBType());
            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver
                        .getBinaryEqualityForTypeSets(OperatorKind.EQUAL, symTable.anydataType, expression.getBType(),
                                                      binaryExpr, env);
            }
            binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
        }
        return binaryExpr;
    }

    private BLangIsLikeExpr createIsLikeExpression(Location pos, BLangExpression expr, BType type) {
        return ASTBuilderUtil.createIsLikeExpr(pos, expr, ASTBuilderUtil.createTypeNode(type), symTable.booleanType);
    }

    private BLangAssignment createAssignmentStmt(BLangSimpleVariable variable) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = variable.name;
        varRef.symbol = variable.symbol;
        varRef.setBType(variable.getBType());

        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = variable.expr;
        assignmentStmt.pos = variable.pos;
        assignmentStmt.setVariable(varRef);
        return assignmentStmt;
    }

    private BLangAssignment createStructFieldUpdate(BLangFunction function, BLangSimpleVariable variable,
                                                    BVarSymbol selfSymbol) {
        return createStructFieldUpdate(function, variable.expr, variable.symbol, variable.getBType(), selfSymbol,
                                       variable.name);
    }

    private BLangAssignment createStructFieldUpdate(BLangFunction function, BLangExpression expr,
                                                    BVarSymbol fieldSymbol, BType fieldType, BVarSymbol selfSymbol,
                                                    BLangIdentifier fieldName) {
        BLangSimpleVarRef selfVarRef = ASTBuilderUtil.createVariableRef(function.pos, selfSymbol);
        BLangFieldBasedAccess fieldAccess = ASTBuilderUtil.createFieldAccessExpr(selfVarRef, fieldName);
        fieldAccess.symbol = fieldSymbol;
        fieldAccess.setBType(fieldType);
        fieldAccess.isStoreOnCreation = true;

        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = expr;
        assignmentStmt.pos = function.pos;
        assignmentStmt.setVariable(fieldAccess);

        SymbolEnv initFuncEnv = SymbolEnv.createFunctionEnv(function, function.symbol.scope, env);
        return rewrite(assignmentStmt, initFuncEnv);
    }

    private boolean safeNavigate(BLangAccessExpression accessExpr) {
        if (accessExpr.isLValue || accessExpr.expr == null) {
            return false;
        }

        if (accessExpr.errorSafeNavigation || accessExpr.nilSafeNavigation) {
            return true;
        }

        NodeKind kind = accessExpr.expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR ||
                kind == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            return safeNavigate((BLangAccessExpression) accessExpr.expr);
        }

        return false;
    }

    private BLangExpression rewriteSafeNavigationExpr(BLangAccessExpression accessExpr) {
        BType originalExprType = accessExpr.getBType();
        // Create a temp variable to hold the intermediate result of the acces expression.
        String matchTempResultVarName = GEN_VAR_PREFIX.value + "temp_result";
        BLangSimpleVariable tempResultVar =
                ASTBuilderUtil.createVariable(accessExpr.pos, matchTempResultVarName, accessExpr.getBType(), null,
                                              new BVarSymbol(0, names.fromString(matchTempResultVarName),
                                                             this.env.scope.owner.pkgID, accessExpr.getBType(),
                                                             this.env.scope.owner, accessExpr.pos, VIRTUAL));
        BLangSimpleVariableDef tempResultVarDef = ASTBuilderUtil.createVariableDef(accessExpr.pos, tempResultVar);
        BLangVariableReference tempResultVarRef =
                ASTBuilderUtil.createVariableRef(accessExpr.pos, tempResultVar.symbol);

        // Create a chain of match statements
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(accessExpr.pos);
        blockStmt.stmts.add(tempResultVarDef);
        handleSafeNavigation(blockStmt, accessExpr, accessExpr.getBType(), tempResultVar);

        // Create a statement-expression including the match statement
        BLangMatchStatement matchStmt = this.matchStmtStack.firstElement();
        blockStmt.stmts.add(matchStmt);
        BLangStatementExpression stmtExpression = createStatementExpression(blockStmt, tempResultVarRef);
        stmtExpression.setBType(originalExprType);
        // Reset the variables
        this.matchStmtStack = new Stack<>();
        this.accessExprStack = new Stack<>();
        this.successClause = null;
        this.safeNavigationAssignment = null;
        return stmtExpression;
    }

    private void handleSafeNavigation(BLangBlockStmt blockStmt, BLangAccessExpression accessExpr, BType type,
                                      BLangSimpleVariable tempResultVar) {
        if (accessExpr.expr == null) {
            return;
        }

        // If the parent of current expr is the root, terminate
        NodeKind kind = accessExpr.expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR || kind == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            handleSafeNavigation(blockStmt, (BLangAccessExpression) accessExpr.expr, type, tempResultVar);
        }

        if (!(accessExpr.errorSafeNavigation || accessExpr.nilSafeNavigation)) {
            BType originalType = Types.getReferredType(accessExpr.originalType);
            if (TypeTags.isXMLTypeTag(originalType.tag) || isMapJson(originalType, false)) {
                accessExpr.setBType(BUnionType.create(null, originalType, symTable.errorType));
            } else {
                accessExpr.setBType(originalType);
            }
            if (this.safeNavigationAssignment != null) {
                this.safeNavigationAssignment.expr = addConversionExprIfRequired(accessExpr, tempResultVar.getBType());
            }
            return;
        }

        /*
         * If the field access is a safe navigation, create a match expression.
         * Then chain the current expression as the success-pattern of the parent
         * match expr, if available.
         * eg:
         * x but {              <--- parent match expr
         *   error e => e,
         *   T t => t.y but {   <--- current expr
         *      error e => e,
         *      R r => r.z
         *   }
         * }
         */

        BLangExpression matchExpr = accessExpr.expr;
        BLangSimpleVariableDef variableDef =
                                         createVarDef("$varDef$", matchExpr.getBType(), matchExpr, matchExpr.pos);
        BLangSimpleVarRef simpleVarRef = ASTBuilderUtil.createVariableRef(variableDef.pos, variableDef.var.symbol);
        accessExpr.expr = simpleVarRef;
        blockStmt.stmts.add(variableDef);
        Location pos = accessExpr.pos;
        BLangMatchStatement matchStmt = ASTBuilderUtil.createMatchStatement(simpleVarRef, pos);
        BType matchExprType = accessExpr.expr.getBType();

        boolean isAllTypesRecords = false;
        LinkedHashSet<BType> memTypes = new LinkedHashSet<>();
        BType referredType = Types.getReferredType(matchExpr.getBType());
        if (referredType.tag == TypeTags.UNION) {
            memTypes = new LinkedHashSet<>(((BUnionType) referredType).getMemberTypes());
            isAllTypesRecords = isAllTypesAreRecordsInUnion(memTypes);
        }

        // Add pattern to lift nil
        if (accessExpr.nilSafeNavigation) {
            matchStmt.addMatchClause(getMatchNullClause(matchExpr, tempResultVar));
            matchStmt.setBType(type);
            memTypes.remove(symTable.nilType);
        }

        // Add pattern to lift error, only if the safe navigation is used
        if (accessExpr.errorSafeNavigation) {
            matchStmt.addMatchClause(getMatchErrorClause(matchExpr, tempResultVar));
            matchStmt.setBType(type);
            matchStmt.pos = pos;
            memTypes.remove(symTable.errorType);
        }

        BLangMatchClause successClause = null;
        Name field = getFieldName(accessExpr);
        if (field == Names.EMPTY) {
            successClause = getSuccessPatternClause(matchExprType, matchExpr, accessExpr, tempResultVar,
                    accessExpr.errorSafeNavigation);
            matchStmt.addMatchClause(successClause);
            pushToMatchStatementStack(matchStmt, successClause, pos);
            return;
        }

        if (isAllTypesRecords) {
            for (BType memberType : memTypes) {
                BRecordType recordType = (BRecordType) Types.getReferredType(memberType);
                if (recordType.fields.containsKey(field.value) || !recordType.sealed) {
                    successClause = getSuccessPatternClause(memberType, matchExpr, accessExpr, tempResultVar,
                            accessExpr.errorSafeNavigation);
                    matchStmt.addMatchClause(successClause);
                }
            }
            matchStmt.addMatchClause(getMatchAllAndNilReturnClause(matchExpr, tempResultVar));
            pushToMatchStatementStack(matchStmt, successClause, pos);
            return;
        }

        // Create the pattern for success scenario. i.e: not null and not error (if applicable).
        successClause = getSuccessPatternClause(matchExprType, matchExpr, accessExpr, tempResultVar,
                accessExpr.errorSafeNavigation);
        matchStmt.addMatchClause(successClause);
        pushToMatchStatementStack(matchStmt, successClause, pos);
    }

    private boolean isMapJson(BType originalType, boolean fromMap) {
        return ((originalType.tag == TypeTags.MAP) && isMapJson(((BMapType) originalType).getConstraint(), true))
                || ((originalType.tag == TypeTags.JSON) && fromMap);
    }

    private void pushToMatchStatementStack(BLangMatchStatement matchStmt, BLangMatchClause successClause,
                                           Location pos) {
        this.matchStmtStack.push(matchStmt);
        if (this.successClause != null) {
            this.successClause.blockStmt = ASTBuilderUtil.createBlockStmt(pos, this.env.scope, Lists.of(matchStmt));
        }
        this.successClause = successClause;
    }

    private Name getFieldName(BLangAccessExpression accessExpr) {
        Name field = Names.EMPTY;
        if (accessExpr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            field = new Name(((BLangFieldBasedAccess) accessExpr).field.value);
        } else if (accessExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            BLangExpression indexBasedExpression = ((BLangIndexBasedAccess) accessExpr).indexExpr;
            if (indexBasedExpression.getKind() == NodeKind.LITERAL) {
                field = new Name(((BLangLiteral) indexBasedExpression).value.toString());
            }
        }
        return field;
    }

    private boolean isAllTypesAreRecordsInUnion(LinkedHashSet<BType> memTypes) {
        for (BType memType : memTypes) {
            int typeTag = Types.getReferredType(memType).tag;
            if (typeTag != TypeTags.RECORD && typeTag != TypeTags.ERROR && typeTag != TypeTags.NIL) {
                return false;
            }
        }
        return true;
    }

    private BLangMatchClause getMatchErrorClause(BLangExpression matchExpr, BLangSimpleVariable tempResultVar) {
        String errorPatternVarName = GEN_VAR_PREFIX.value + "t_match_error";
        Location pos = matchExpr.pos;
        BVarSymbol errorPatternVarSymbol = new BVarSymbol(0, Names.fromString(errorPatternVarName),
                this.env.scope.owner.pkgID, symTable.anyOrErrorType, this.env.scope.owner, pos, VIRTUAL);
        BLangCaptureBindingPattern captureBindingPattern =
                ASTBuilderUtil.createCaptureBindingPattern(errorPatternVarSymbol, errorPatternVarName);
        BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern =
                ASTBuilderUtil.createVarBindingPatternMatchPattern(captureBindingPattern, matchExpr);

        BLangSimpleVarRef assignmentRhsExpr = ASTBuilderUtil.createVariableRef(pos, errorPatternVarSymbol);
        BLangVariableReference tempResultVarRef = ASTBuilderUtil.createVariableRef(pos, tempResultVar.symbol);
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(pos, tempResultVarRef, assignmentRhsExpr);
        BLangBlockStmt clauseBody = ASTBuilderUtil.createBlockStmt(pos, this.env.scope, Lists.of(assignmentStmt));
        BLangExpression matchGuard = ASTBuilderUtil.createTypeTestExpr(pos, assignmentRhsExpr, getErrorTypeNode());
        matchGuard.setBType(symTable.booleanType);

        return ASTBuilderUtil.createMatchClause(matchExpr, clauseBody, matchGuard, varBindingPatternMatchPattern);
    }

    private BLangMatchClause getMatchNullClause(BLangExpression matchExpr, BLangSimpleVariable tempResultVar) {
        String nullPatternVarName = GEN_VAR_PREFIX.value + "t_match_null";
        Location pos = matchExpr.pos;
        BVarSymbol nullPatternVarSymbol = new BVarSymbol(0, Names.fromString(nullPatternVarName),
                this.env.scope.owner.pkgID, symTable.anyOrErrorType, this.env.scope.owner, pos, VIRTUAL);
        BLangCaptureBindingPattern captureBindingPattern =
                ASTBuilderUtil.createCaptureBindingPattern(nullPatternVarSymbol, nullPatternVarName);
        BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern =
                ASTBuilderUtil.createVarBindingPatternMatchPattern(captureBindingPattern, matchExpr);

        BLangSimpleVarRef assignmentRhsExpr = ASTBuilderUtil.createVariableRef(pos, nullPatternVarSymbol);
        BLangVariableReference tempResultVarRef = ASTBuilderUtil.createVariableRef(pos, tempResultVar.symbol);
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(pos, tempResultVarRef, assignmentRhsExpr);
        BLangBlockStmt clauseBody = ASTBuilderUtil.createBlockStmt(pos, this.env.scope, Lists.of(assignmentStmt));
        BLangExpression matchGuard = ASTBuilderUtil.createTypeTestExpr(pos, assignmentRhsExpr, getNillTypeNode());
        matchGuard.setBType(symTable.booleanType);

        return ASTBuilderUtil.createMatchClause(matchExpr, clauseBody, matchGuard, varBindingPatternMatchPattern);
    }

    private BLangMatchClause getMatchAllAndNilReturnClause(BLangExpression matchExpr,
                                                           BLangSimpleVariable tempResultVar) {
        Location pos = matchExpr.pos;
        BLangVariableReference tempResultVarRef = ASTBuilderUtil.createVariableRef(pos, tempResultVar.symbol);
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(pos, tempResultVarRef, createLiteral(pos, symTable.nilType,
                        Names.NIL_VALUE));
        BLangBlockStmt clauseBody = ASTBuilderUtil.createBlockStmt(pos, this.env.scope, Lists.of(assignmentStmt));

        BLangWildCardMatchPattern wildCardMatchPattern = ASTBuilderUtil.createWildCardMatchPattern(matchExpr);
        wildCardMatchPattern.setBType(symTable.anyType);
        return ASTBuilderUtil.createMatchClause(matchExpr, clauseBody, null, wildCardMatchPattern);
    }

    private BLangMatchClause getSuccessPatternClause(BType type, BLangExpression matchExpr,
                                                     BLangAccessExpression accessExpr,
                                                     BLangSimpleVariable tempResultVar, boolean liftError) {
        type = types.getSafeType(type, true, liftError);
        String successPatternVarName = GEN_VAR_PREFIX.value + "t_match_success";

        Location pos = accessExpr.pos;
        BVarSymbol  successPatternSymbol;
        if (Types.getReferredType(type).tag == TypeTags.INVOKABLE) {
            successPatternSymbol = new BInvokableSymbol(SymTag.VARIABLE, 0, Names.fromString(successPatternVarName),
                    this.env.scope.owner.pkgID, symTable.anyOrErrorType, this.env.scope.owner, pos, VIRTUAL);
        } else {
            successPatternSymbol = new BVarSymbol(0, Names.fromString(successPatternVarName),
                    this.env.scope.owner.pkgID, symTable.anyOrErrorType, this.env.scope.owner, pos, VIRTUAL);
        }

        BLangSimpleVariable successPatternVar = ASTBuilderUtil.createVariable(accessExpr.pos, successPatternVarName,
                type, null, successPatternSymbol);
        BLangSimpleVarRef successPatternVarRef = ASTBuilderUtil.createVariableRef(accessExpr.pos,
                successPatternVar.symbol);

        BLangCaptureBindingPattern captureBindingPattern =
                ASTBuilderUtil.createCaptureBindingPattern(successPatternSymbol, successPatternVarName);
        BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern =
                ASTBuilderUtil.createVarBindingPatternMatchPattern(captureBindingPattern, matchExpr);

        BLangAccessExpression tempAccessExpr = nodeCloner.cloneNode(accessExpr);
        if (accessExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            ((BLangIndexBasedAccess) tempAccessExpr).indexExpr = ((BLangIndexBasedAccess) accessExpr).indexExpr;
        }
        if (accessExpr instanceof BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) {
            ((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) tempAccessExpr).nsSymbol =
                    ((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) accessExpr).nsSymbol;
        }

        tempAccessExpr.expr = addConversionExprIfRequired(successPatternVarRef, type);
        tempAccessExpr.errorSafeNavigation = false;
        tempAccessExpr.nilSafeNavigation = false;
        accessExpr.cloneRef = null;

        // Type of the field access expression should be always taken from the child type.
        // Because the type assigned to expression contains the inherited error/nil types,
        // and may not reflect the actual type of the child/field expr.
        if (TypeTags.isXMLTypeTag(Types.getReferredType(tempAccessExpr.expr.getBType()).tag)) {
            // todo: add discription why this is special here
            tempAccessExpr.setBType(BUnionType.create(null, accessExpr.originalType, symTable.errorType,
                    symTable.nilType));
        } else {
            tempAccessExpr.setBType(accessExpr.originalType);
        }
        tempAccessExpr.optionalFieldAccess = accessExpr.optionalFieldAccess;

        BLangVariableReference tempResultVarRef =
                ASTBuilderUtil.createVariableRef(accessExpr.pos, tempResultVar.symbol);

        BLangExpression assignmentRhsExpr = addConversionExprIfRequired(tempAccessExpr, tempResultVarRef.getBType());
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(accessExpr.pos, tempResultVarRef, assignmentRhsExpr);
        BLangBlockStmt clauseBody = ASTBuilderUtil.createBlockStmt(accessExpr.pos, this.env.scope,
                Lists.of(assignmentStmt));
        BLangExpression matchGuard = ASTBuilderUtil.createTypeTestExpr(pos, successPatternVarRef, createTypeNode(type));
        matchGuard.setBType(symTable.booleanType);

        return ASTBuilderUtil.createMatchClause(matchExpr, clauseBody, matchGuard, varBindingPatternMatchPattern);
    }

    BLangValueType getNillTypeNode() {
        BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nillTypeNode.typeKind = TypeKind.NIL;
        nillTypeNode.setBType(symTable.nilType);
        return nillTypeNode;
    }

    BLangValueType createTypeNode(BType type) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.typeKind = type.getKind();
        typeNode.setBType(type);
        return typeNode;
    }

    private BLangValueExpression cloneExpression(BLangExpression expr) {
        switch (expr.getKind()) {
            case SIMPLE_VARIABLE_REF:
                return ASTBuilderUtil.createVariableRef(expr.pos, ((BLangSimpleVarRef) expr).symbol);
            case FIELD_BASED_ACCESS_EXPR:
            case INDEX_BASED_ACCESS_EXPR:
                return cloneAccessExpr((BLangAccessExpression) expr);
            default:
                throw new IllegalStateException();
        }
    }

    private BLangAccessExpression cloneAccessExpr(BLangAccessExpression originalAccessExpr) {
        if (originalAccessExpr.expr == null) {
            return originalAccessExpr;
        }

        BLangExpression varRef;
        NodeKind kind = originalAccessExpr.expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR || kind == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            varRef = cloneAccessExpr((BLangAccessExpression) originalAccessExpr.expr);
        } else {
            varRef = cloneExpression(originalAccessExpr.expr);
        }
        varRef.setBType(types.getSafeType(originalAccessExpr.expr.getBType(), true, false));

        BLangAccessExpression accessExpr;
        switch (originalAccessExpr.getKind()) {
            case FIELD_BASED_ACCESS_EXPR:
                accessExpr = ASTBuilderUtil.createFieldAccessExpr(varRef,
                        ((BLangFieldBasedAccess) originalAccessExpr).field);
                break;
            case INDEX_BASED_ACCESS_EXPR:
                accessExpr = ASTBuilderUtil.createIndexAccessExpr(varRef,
                        ((BLangIndexBasedAccess) originalAccessExpr).indexExpr);
                break;
            default:
                throw new IllegalStateException();
        }

        accessExpr.originalType = originalAccessExpr.originalType;
        accessExpr.pos = originalAccessExpr.pos;
        accessExpr.isLValue = originalAccessExpr.isLValue;
        accessExpr.symbol = originalAccessExpr.symbol;
        accessExpr.errorSafeNavigation = false;
        accessExpr.nilSafeNavigation = false;

        // Type of the field access expression should be always taken from the child type.
        // Because the type assigned to expression contains the inherited error/nil types,
        // and may not reflect the actual type of the child/field expr.
        accessExpr.setBType(originalAccessExpr.originalType);
        return accessExpr;
    }

    private BLangBinaryExpr getModifiedIntRangeStartExpr(BLangExpression expr) {
        BLangLiteral constOneLiteral = ASTBuilderUtil.createLiteral(expr.pos, symTable.intType, 1L);
        return ASTBuilderUtil.createBinaryExpr(expr.pos, expr, constOneLiteral, symTable.intType, OperatorKind.ADD,
                (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.ADD,
                        symTable.intType,
                        symTable.intType));
    }

    private BLangBinaryExpr getModifiedIntRangeEndExpr(BLangExpression expr) {
        BLangLiteral constOneLiteral = ASTBuilderUtil.createLiteral(expr.pos, symTable.intType, 1L);
        return ASTBuilderUtil.createBinaryExpr(expr.pos, expr, constOneLiteral, symTable.intType, OperatorKind.SUB,
                (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.SUB,
                        symTable.intType,
                        symTable.intType));
    }

    private BLangLiteral getBooleanLiteral(boolean value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.setBType(symTable.booleanType);
        literal.pos = symTable.builtinPos;
        return literal;
    }

    private boolean isDefaultableMappingType(BType type) {
        switch (types.getSafeType(type, true, false).tag) {
            case TypeTags.JSON:
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return true;
            case TypeTags.TYPEREFDESC:
                return isDefaultableMappingType(Types.getReferredType(type));
            default:
                return false;
        }
    }

    private BLangFunction createInitFunctionForClassDefn(BLangClassDefinition classDefinition, SymbolEnv env) {
        BType returnType = symTable.nilType;

        // if available get return type of user defined init method
        if (classDefinition.initFunction != null) {
            returnType = classDefinition.initFunction.getBType().getReturnType();
        }

        BLangFunction initFunction =
                TypeDefBuilderHelper.createInitFunctionForStructureType(null, classDefinition.symbol,
                        env, names, GENERATED_INIT_SUFFIX,
                        classDefinition.getBType(), returnType);
        BObjectTypeSymbol typeSymbol = ((BObjectTypeSymbol) classDefinition.getBType().tsymbol);
        typeSymbol.generatedInitializerFunc = new BAttachedFunction(GENERATED_INIT_SUFFIX, initFunction.symbol,
                (BInvokableType) initFunction.getBType(), null);
        classDefinition.generatedInitFunction = initFunction;
        initFunction.returnTypeNode.setBType(returnType);
        return initFunction;
    }

    private void visitBinaryLogicalExpr(BLangBinaryExpr binaryExpr) {
        /*
         * Desugar (lhsExpr && rhsExpr) to following if-else:
         *
         * logical AND:
         * -------------
         * T $result$;
         * if (lhsExpr) {
         *    $result$ = rhsExpr;
         * } else {
         *    $result$ = false;
         * }
         *
         * logical OR:
         * -------------
         * T $result$;
         * if (lhsExpr) {
         *    $result$ = true;
         * } else {
         *    $result$ = rhsExpr;
         * }
         *
         */
        BLangSimpleVariableDef resultVarDef = createVarDef("$result$", binaryExpr.getBType(), null,
                                                           symTable.builtinPos);
        BLangBlockStmt thenBody = ASTBuilderUtil.createBlockStmt(binaryExpr.pos);
        BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(binaryExpr.pos);

        // Create then assignment
        BLangSimpleVarRef thenResultVarRef = ASTBuilderUtil.createVariableRef(symTable.builtinPos,
                                                                              resultVarDef.var.symbol);
        BLangExpression thenResult;
        if (binaryExpr.opKind == OperatorKind.AND) {
            thenResult = binaryExpr.rhsExpr;
        } else {
            thenResult = getBooleanLiteral(true);
        }
        BLangAssignment thenAssignment =
                ASTBuilderUtil.createAssignmentStmt(binaryExpr.pos, thenResultVarRef, thenResult);
        thenBody.addStatement(thenAssignment);

        // Create else assignment
        BLangExpression elseResult;
        BLangSimpleVarRef elseResultVarRef = ASTBuilderUtil.createVariableRef(symTable.builtinPos,
                                                                              resultVarDef.var.symbol);
        if (binaryExpr.opKind == OperatorKind.AND) {
            elseResult = getBooleanLiteral(false);
        } else {
            elseResult = binaryExpr.rhsExpr;
        }
        BLangAssignment elseAssignment =
                ASTBuilderUtil.createAssignmentStmt(binaryExpr.pos, elseResultVarRef, elseResult);
        elseBody.addStatement(elseAssignment);

        // Then make it a expression-statement, with expression being the $result$
        BLangSimpleVarRef resultVarRef = ASTBuilderUtil.createVariableRef(binaryExpr.pos, resultVarDef.var.symbol);
        BLangIf ifElse = ASTBuilderUtil.createIfElseStmt(binaryExpr.pos, binaryExpr.lhsExpr, thenBody, elseBody);

        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(binaryExpr.pos, Lists.of(resultVarDef, ifElse));
        BLangStatementExpression stmtExpr = createStatementExpression(blockStmt, resultVarRef);
        stmtExpr.setBType(binaryExpr.getBType());

        result = rewriteExpr(stmtExpr);
    }

    protected boolean isMappingOrObjectConstructorOrObjInit(BLangExpression expression) {
        switch (expression.getKind()) {
            case TYPE_INIT_EXPR:
            case RECORD_LITERAL_EXPR:
            case OBJECT_CTOR_EXPRESSION:
                return true;
            case CHECK_EXPR:
                return isMappingOrObjectConstructorOrObjInit(((BLangCheckedExpr) expression).expr);
            case TYPE_CONVERSION_EXPR:
                return isMappingOrObjectConstructorOrObjInit(((BLangTypeConversionExpr) expression).expr);
            default:
                return false;
        }
    }

    private BType getRestType(BInvokableSymbol invokableSymbol) {
        if (invokableSymbol != null && invokableSymbol.restParam != null) {
            return invokableSymbol.restParam.type;
        }
        return null;
    }

    private BType getRestType(BLangFunction function) {
        if (function != null && function.restParam != null) {
            return function.restParam.getBType();
        }
        return null;
    }

    private BVarSymbol getRestSymbol(BLangFunction function) {
        if (function != null && function.restParam != null) {
            return function.restParam.symbol;
        }
        return null;
    }

    private boolean isComputedKey(RecordLiteralNode.RecordField field) {
        if (!field.isKeyValueField()) {
            return false;
        }
        return ((BLangRecordLiteral.BLangRecordKeyValueField) field).key.computedKey;
    }

    private BLangRecordLiteral rewriteMappingConstructor(BLangRecordLiteral mappingConstructorExpr) {
        List<RecordLiteralNode.RecordField> fields = mappingConstructorExpr.fields;

        BType type = mappingConstructorExpr.getBType();
        Location pos = mappingConstructorExpr.pos;

        List<RecordLiteralNode.RecordField> rewrittenFields = new ArrayList<>(fields.size());

        for (RecordLiteralNode.RecordField field : fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;

                BLangRecordLiteral.BLangRecordKey key = keyValueField.key;
                BLangExpression origKey = key.expr;
                BLangExpression keyExpr;
                if (key.computedKey) {
                    keyExpr = origKey;
                } else {
                    keyExpr = origKey.getKind() == NodeKind.SIMPLE_VARIABLE_REF ? createStringLiteral(pos,
                            StringEscapeUtils.unescapeJava(((BLangSimpleVarRef) origKey).variableName.value)) :
                            ((BLangLiteral) origKey);
                }

                BLangRecordLiteral.BLangRecordKeyValueField rewrittenField =
                        ASTBuilderUtil.createBLangRecordKeyValue(rewriteExpr(keyExpr),
                                                                 rewriteExpr(keyValueField.valueExpr));
                rewrittenField.pos = keyValueField.pos;
                rewrittenField.key.pos = key.pos;
                rewrittenFields.add(rewrittenField);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRefField = (BLangSimpleVarRef) field;
                rewrittenFields.add(ASTBuilderUtil.createBLangRecordKeyValue(
                        rewriteExpr(createStringLiteral(pos,
                                StringEscapeUtils.unescapeJava(varRefField.variableName.value))),
                        rewriteExpr(varRefField)));
            } else {
                BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOpField =
                        (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
                spreadOpField.expr = rewriteExpr(spreadOpField.expr);
                rewrittenFields.add(spreadOpField);
            }
        }

        fields.clear();
        BType refType = Types.getReferredType(type);
        return refType.tag == TypeTags.RECORD ?
                new BLangStructLiteral(pos, type, refType.tsymbol, rewrittenFields) :
                new BLangMapLiteral(pos, type, rewrittenFields);
    }

    protected void addTransactionInternalModuleImport() {
        if (!env.enclPkg.packageID.equals(PackageID.TRANSACTION_INTERNAL)) {
            BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
            List<BLangIdentifier> pkgNameComps = new ArrayList<>();
            pkgNameComps.add(ASTBuilderUtil.createIdentifier(env.enclPkg.pos, Names.TRANSACTION.value));
            importDcl.pkgNameComps = pkgNameComps;
            importDcl.pos = env.enclPkg.symbol.pos;
            importDcl.orgName = ASTBuilderUtil.createIdentifier(env.enclPkg.pos, Names.BALLERINA_INTERNAL_ORG.value);
            importDcl.alias = ASTBuilderUtil.createIdentifier(env.enclPkg.pos, "trx");
            importDcl.version = ASTBuilderUtil.createIdentifier(env.enclPkg.pos, "");
            importDcl.symbol = symTable.internalTransactionModuleSymbol;
            env.enclPkg.imports.add(importDcl);
            env.enclPkg.symbol.imports.add(importDcl.symbol);
        }
    }
}
