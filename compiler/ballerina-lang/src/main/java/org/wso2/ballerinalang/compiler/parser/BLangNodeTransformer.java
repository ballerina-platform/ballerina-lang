/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.BreakStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.CommitActionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ContinueStatementNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.DocumentationReferenceNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.FlushActionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.ForkStatementNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionParameters;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ImportVersionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.KeyTypeConstraintNode;
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.LimitClauseNode;
import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MatchClauseNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgBindingPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.NewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OnClauseNode;
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.OrderKeyNode;
import io.ballerina.compiler.syntax.tree.PanicStatementNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryActionNode;
import io.ballerina.compiler.syntax.tree.QueryConstructTypeNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.ReceiveActionNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerina.compiler.syntax.tree.RestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RollbackStatementNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceBodyNode;
import io.ballerina.compiler.syntax.tree.ServiceConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyncSendActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TemplateMemberNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.TransactionalExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastParamNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.TypedescTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import io.ballerina.compiler.syntax.tree.WaitFieldNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.compiler.syntax.tree.WildcardBindingPatternNode;
import io.ballerina.compiler.syntax.tree.XMLAtomicNamePatternNode;
import io.ballerina.compiler.syntax.tree.XMLAttributeNode;
import io.ballerina.compiler.syntax.tree.XMLAttributeValue;
import io.ballerina.compiler.syntax.tree.XMLComment;
import io.ballerina.compiler.syntax.tree.XMLElementNode;
import io.ballerina.compiler.syntax.tree.XMLEmptyElementNode;
import io.ballerina.compiler.syntax.tree.XMLEndTagNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLNameNode;
import io.ballerina.compiler.syntax.tree.XMLNamePatternChainingNode;
import io.ballerina.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.XMLProcessingInstruction;
import io.ballerina.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerina.compiler.syntax.tree.XMLSimpleNameNode;
import io.ballerina.compiler.syntax.tree.XMLStartTagNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLTextNode;
import io.ballerina.compiler.syntax.tree.XmlTypeDescriptorNode;
import io.ballerina.runtime.IdentifierUtils;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr.BLangWaitKeyValue;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
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
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import static org.ballerinalang.model.elements.Flag.ISOLATED;
import static org.ballerinalang.model.elements.Flag.SERVICE;
import static org.wso2.ballerinalang.compiler.util.Constants.INFERRED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * Generates a {@code BLandCompilationUnit} from the given {@code ModulePart}.
 *
 * @since 1.3.0
 */
public class BLangNodeTransformer extends NodeTransformer<BLangNode> {
    private static final String IDENTIFIER_LITERAL_PREFIX = "'";
    private BLangDiagnosticLog dlog;
    private SymbolTable symTable;
    private BDiagnosticSource diagnosticSource;

    private BLangCompilationUnit currentCompilationUnit;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;

    /* To keep track of additional statements produced from multi-BLangNode resultant transformations */
    private Stack<BLangStatement> additionalStatements = new Stack<>();
    /* To keep track if we are inside a block statment for the use of type definition creation */
    private boolean isInLocalContext = false;

    public BLangNodeTransformer(CompilerContext context, BDiagnosticSource diagnosticSource) {
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.diagnosticSource = diagnosticSource;
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
    }

    public List<org.ballerinalang.model.tree.Node> accept(Node node) {
        BLangNode bLangNode = node.apply(this);
        List<org.ballerinalang.model.tree.Node> nodes = new ArrayList<>();
        // if not already consumed, add left-over statements
        while (!additionalStatements.empty()) {
            nodes.add(additionalStatements.pop());
        }
        nodes.add(bLangNode);
        return nodes;
    }

    @Override
    public BLangNode transform(IdentifierToken identifierToken) {
        return this.createIdentifier(getPosition(identifierToken), identifierToken);
    }

    private Optional<Node> getDocumentationString(Optional<MetadataNode> metadataNode) {
        return metadataNode.map(MetadataNode::documentationString).orElse(null);
    }

    private NodeList<AnnotationNode> getAnnotations(Optional<MetadataNode> metadataNode) {
        return metadataNode.map(MetadataNode::annotations).orElse(null);
    }

    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange lineRange = node.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line(), endPos.line(), startPos.offset(),
                                 endPos.offset());
    }

    private DiagnosticPos getPositionWithoutMetadata(Node node) {
        if (node == null) {
            return null;
        }
        LineRange nodeLineRange = node.lineRange();
        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        ChildNodeList children = nonTerminalNode.children();
        // If there's metadata it will be the first child.
        // Hence set start position from next immediate child.
        LinePosition startPos;
        if (children.get(0).kind() == SyntaxKind.METADATA) {
            startPos = children.get(1).lineRange().startLine();
        } else {
            startPos = nodeLineRange.startLine();
        }
        LinePosition endPos = nodeLineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line(), endPos.line(), startPos.offset(),
                                 endPos.offset());
    }

    @Override
    public BLangNode transform(ModulePartNode modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        this.currentCompilationUnit = compilationUnit;
        compilationUnit.name = diagnosticSource.cUnitName;
        DiagnosticPos pos = getPosition(modulePart);

        // Generate import declarations
        for (ImportDeclarationNode importDecl : modulePart.imports()) {
            BLangImportPackage bLangImport = (BLangImportPackage) importDecl.apply(this);
            bLangImport.compUnit = this.createIdentifier(pos, compilationUnit.getName());
            compilationUnit.addTopLevelNode(bLangImport);
        }

        // Generate other module-level declarations
        for (ModuleMemberDeclarationNode member : modulePart.members()) {
            compilationUnit.addTopLevelNode((TopLevelNode) member.apply(this));
        }
        pos.sLine = 0;
        pos.sCol = 0;
        pos.eLine = 0;
        pos.eCol = 0;

        compilationUnit.pos = pos;
        this.currentCompilationUnit = null;
        return compilationUnit;
    }

    @Override
    public BLangNode transform(ModuleVariableDeclarationNode modVarDeclrNode) {
        TypedBindingPatternNode typedBindingPattern = modVarDeclrNode.typedBindingPattern();
        CaptureBindingPatternNode bindingPattern = (CaptureBindingPatternNode) typedBindingPattern.bindingPattern();
        BLangSimpleVariable simpleVar = createSimpleVar(bindingPattern.variableName(),
                typedBindingPattern.typeDescriptor(), modVarDeclrNode.initializer().orElse(null),
                modVarDeclrNode.finalKeyword().isPresent(), false, null,
                getAnnotations(modVarDeclrNode.metadata()));
        simpleVar.pos = getPositionWithoutMetadata(modVarDeclrNode);
        simpleVar.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(modVarDeclrNode.metadata()));
        return simpleVar;
    }

    @Override
    public BLangNode transform(ImportDeclarationNode importDeclaration) {
        ImportOrgNameNode orgNameNode = importDeclaration.orgName().orElse(null);
        ImportVersionNode versionNode = importDeclaration.version().orElse(null);
        ImportPrefixNode prefixNode = importDeclaration.prefix().orElse(null);

        Token orgName = null;
        if (orgNameNode != null) {
            orgName = orgNameNode.orgName();
        }

        String version = null;
        if (versionNode != null) {
            if (versionNode.isMissing()) {
                version = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
            } else {
                version = extractVersion(versionNode.versionNumber());
            }
        }

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        NodeList<IdentifierToken> names = importDeclaration.moduleName();
        DiagnosticPos position = getPosition(importDeclaration);
        names.forEach(name -> pkgNameComps.add(this.createIdentifier(getPosition(name), name.text(), null)));

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = position;
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.orgName = this.createIdentifier(getPosition(orgNameNode), orgName);
        importDcl.version = this.createIdentifier(getPosition(versionNode), version);
        importDcl.alias = (prefixNode != null) ? this.createIdentifier(getPosition(prefixNode), prefixNode.prefix())
                                               : pkgNameComps.get(pkgNameComps.size() - 1);

        return importDcl;
    }

    @Override
    public BLangNode transform(MethodDeclarationNode methodDeclarationNode) {
        BLangFunction bLFunction = createFunctionNode(methodDeclarationNode.methodName(),
                methodDeclarationNode.qualifierList(), methodDeclarationNode.methodSignature(), null);

        bLFunction.annAttachments = applyAll(getAnnotations(methodDeclarationNode.metadata()));
        bLFunction.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(methodDeclarationNode.metadata()));
        bLFunction.pos = getPositionWithoutMetadata(methodDeclarationNode);
        return bLFunction;
    }

    @Override
    public BLangNode transform(ConstantDeclarationNode constantDeclarationNode) {
        BLangConstant constantNode = (BLangConstant) TreeBuilder.createConstantNode();
        DiagnosticPos pos = getPositionWithoutMetadata(constantDeclarationNode);
        DiagnosticPos identifierPos = getPosition(constantDeclarationNode.variableName());
        constantNode.name = createIdentifier(identifierPos, constantDeclarationNode.variableName());
        constantNode.expr = createExpression(constantDeclarationNode.initializer());
        constantNode.pos = pos;
        if (constantDeclarationNode.typeDescriptor().isPresent()) {
            constantNode.typeNode = createTypeNode(constantDeclarationNode.typeDescriptor().orElse(null));
        }

        constantNode.annAttachments = applyAll(getAnnotations(constantDeclarationNode.metadata()));
        constantNode.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(constantDeclarationNode.metadata()));

        constantNode.flagSet.add(Flag.CONSTANT);
        if (constantDeclarationNode.visibilityQualifier().isPresent() &&
                constantDeclarationNode.visibilityQualifier().orElse(null).kind() == SyntaxKind.PUBLIC_KEYWORD) {
            constantNode.flagSet.add(Flag.PUBLIC);
        }

        // Check whether the value is a literal. If it is not a literal, it is an invalid case. So we don't need to
        // consider it.
        NodeKind nodeKind = constantNode.expr.getKind();
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
            // Note - If the RHS is a literal, we need to create an anonymous type definition which can later be used
            // in type definitions.

            // Create a new literal.
            BLangLiteral literal = nodeKind == NodeKind.LITERAL ?
                    (BLangLiteral) TreeBuilder.createLiteralExpression() :
                    (BLangLiteral) TreeBuilder.createNumericLiteralExpression();
            literal.setValue(((BLangLiteral) constantNode.expr).value);
            literal.type = constantNode.expr.type;
            literal.isConstant = true;

            // Create a new finite type node.
            BLangFiniteTypeNode finiteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
            finiteTypeNode.valueSpace.add(literal);

            // Create a new anonymous type definition.
            BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
            String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
            IdentifierNode anonTypeGenName = createIdentifier(identifierPos, genName);
            typeDef.setName(anonTypeGenName);
            typeDef.flagSet.add(Flag.PUBLIC);
            typeDef.flagSet.add(Flag.ANONYMOUS);
            typeDef.typeNode = finiteTypeNode;
            typeDef.pos = pos;

            // We add this type definition to the `associatedTypeDefinition` field of the constant node. Then when we
            // visit the constant node, we visit this type definition as well. By doing this, we don't need to change
            // any of the type def visiting logic in symbol enter.
            constantNode.associatedTypeDefinition = typeDef;
        }
        return constantNode;
    }

    public BLangNode transform(TypeDefinitionNode typeDefNode) {
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier identifierNode =
                this.createIdentifier(typeDefNode.typeName());
        typeDef.setName(identifierNode);
        typeDef.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(typeDefNode.metadata()));

        typeDef.typeNode = createTypeNode(typeDefNode.typeDescriptor());

        typeDefNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                typeDef.flagSet.add(Flag.PUBLIC);
            }
        });
        typeDef.pos = getPositionWithoutMetadata(typeDefNode);
        typeDef.annAttachments = applyAll(getAnnotations(typeDefNode.metadata()));
        return typeDef;
    }

    @Override
    public BLangNode transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        List<TypeDescriptorNode> nodes = flattenUnionType(unionTypeDescriptorNode);

        List<TypeDescriptorNode> finiteTypeElements = new ArrayList<>();
        List<List<TypeDescriptorNode>> unionTypeElementsCollection = new ArrayList<>();
        for (TypeDescriptorNode type : nodes) {
            if (type.kind() == SyntaxKind.SINGLETON_TYPE_DESC) {
                finiteTypeElements.add(type);
                unionTypeElementsCollection.add(new ArrayList<>());
            } else {
                List<TypeDescriptorNode> lastOfOthers;
                if (unionTypeElementsCollection.isEmpty()) {
                    lastOfOthers = new ArrayList<>();
                    unionTypeElementsCollection.add(lastOfOthers);
                } else {
                    lastOfOthers = unionTypeElementsCollection.get(unionTypeElementsCollection.size() - 1);
                }

                lastOfOthers.add(type);
            }
        }

        List<TypeDescriptorNode> unionElements = new ArrayList<>();
        reverseFlatMap(unionTypeElementsCollection, unionElements);

        BLangFiniteTypeNode bLangFiniteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
        for (TypeDescriptorNode finiteTypeEl : finiteTypeElements) {
            SingletonTypeDescriptorNode singletonTypeNode = (SingletonTypeDescriptorNode) finiteTypeEl;
            BLangLiteral literal = createSimpleLiteral(singletonTypeNode.simpleContExprNode(), true);
            bLangFiniteTypeNode.addValue(literal);
        }

        if (unionElements.isEmpty()) {
            return bLangFiniteTypeNode;
        }

        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.pos = getPosition(unionTypeDescriptorNode);
        for (TypeDescriptorNode unionElement : unionElements) {
            unionTypeNode.memberTypeNodes.add(createTypeNode(unionElement));
        }

        if (!finiteTypeElements.isEmpty()) {
            unionTypeNode.memberTypeNodes.add(deSugarTypeAsUserDefType(bLangFiniteTypeNode));
        }
        return unionTypeNode;
    }

    private List<TypeDescriptorNode> flattenUnionType(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        List<TypeDescriptorNode> list = new ArrayList<>();
        list.add(unionTypeDescriptorNode.leftTypeDesc());
        while (unionTypeDescriptorNode.rightTypeDesc().kind() == SyntaxKind.UNION_TYPE_DESC) {
            unionTypeDescriptorNode = (UnionTypeDescriptorNode) unionTypeDescriptorNode.rightTypeDesc();
            list.add(unionTypeDescriptorNode.leftTypeDesc());
        }
        list.add(unionTypeDescriptorNode.rightTypeDesc());
        return list;
    }

    private <T> void reverseFlatMap(List<List<T>> listOfLists, List<T> result) {
        for (int i = listOfLists.size() - 1; i >= 0; i--) {
            result.addAll(listOfLists.get(i));
        }
    }

    private BLangUserDefinedType deSugarTypeAsUserDefType(BLangType toIndirect) {
        BLangTypeDefinition bLTypeDef = createTypeDefinitionWithTypeNode(toIndirect);
        DiagnosticPos pos = toIndirect.pos;
        addToTop(bLTypeDef);

        return createUserDefinedType(pos, (BLangIdentifier) TreeBuilder.createIdentifierNode(), bLTypeDef.name);
    }

    private BLangTypeDefinition createTypeDefinitionWithTypeNode(BLangType toIndirect) {
        DiagnosticPos pos = toIndirect.pos;
        BLangTypeDefinition bLTypeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();

        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(diagnosticSource.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        bLTypeDef.setName(anonTypeGenName);
        bLTypeDef.flagSet.add(Flag.PUBLIC);
        bLTypeDef.flagSet.add(Flag.ANONYMOUS);

        bLTypeDef.typeNode = toIndirect;
        bLTypeDef.pos = pos;
        return bLTypeDef;
    }

    @Override
    public BLangNode transform(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        BLangType typeNode = createTypeNode(parenthesisedTypeDescriptorNode.typedesc());
        typeNode.grouped = true;
        return typeNode;
    }

    @Override
    public BLangNode transform(TypeParameterNode typeParameterNode) {
        return createTypeNode(typeParameterNode.typeNode());
    }

    @Override
    public BLangNode transform(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        SeparatedNodeList<Node> types = tupleTypeDescriptorNode.memberTypeDesc();
        for (int i = 0; i < types.size(); i++) {
            Node node = types.get(i);
            if (node.kind() == SyntaxKind.REST_TYPE) {
                RestDescriptorNode restDescriptor = (RestDescriptorNode) node;
                tupleTypeNode.restParamType = createTypeNode(restDescriptor.typeDescriptor());
            } else {
                tupleTypeNode.memberTypeNodes.add(createTypeNode(node));
            }
        }
        tupleTypeNode.pos = getPosition(tupleTypeDescriptorNode);

        return tupleTypeNode;
    }

    @Override
    public BLangNode transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        BLangErrorType errorType = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        Optional<ErrorTypeParamsNode> typeParam = errorTypeDescriptorNode.errorTypeParamsNode();
        errorType.pos = getPosition(errorTypeDescriptorNode);
        if (typeParam.isPresent()) {
            ErrorTypeParamsNode typeNode = typeParam.get();
            BLangType detail = null;
            if (isAnonymousTypeNode(typeNode)) {
                detail = deSugarTypeAsUserDefType(createTypeNode(typeNode));
            } else {
                detail = createTypeNode(typeNode);
            }

            if (detail != null) {
                errorType.detailType = detail;
                NonTerminalNode parent = errorTypeDescriptorNode.parent();
                if (parent.kind() == SyntaxKind.DISTINCT_TYPE_DESC) {
                    parent = parent.parent();
                }
                if (parent.kind() != SyntaxKind.TYPE_DEFINITION) {
                    return deSugarTypeAsUserDefType(errorType);
                }
            } else {
                errorType.inferErrorType = true;
            }
        }

        return errorType;
    }

    private boolean isAnonymousTypeNode(ErrorTypeParamsNode typeNode) {
        SyntaxKind paramKind = typeNode.parameter().kind();
        if (paramKind == SyntaxKind.RECORD_TYPE_DESC || paramKind == SyntaxKind.OBJECT_TYPE_DESC
                || paramKind == SyntaxKind.ERROR_TYPE_DESC) {
            return checkIfAnonymous(typeNode);
        }
        return false;
    }

    @Override
    public BLangNode transform(ErrorTypeParamsNode errorTypeParamsNode) {
        Node param = errorTypeParamsNode.parameter();
        if (param.kind() == SyntaxKind.ASTERISK_TOKEN) {
            return null;
        }

        return createTypeNode(errorTypeParamsNode.parameter());
    }

    @Override
    public BLangNode transform(DistinctTypeDescriptorNode distinctTypeDesc) {
        BLangType typeNode = createTypeNode(distinctTypeDesc.typeDescriptor());
        typeNode.flagSet.add(Flag.DISTINCT);
        return typeNode;
    }

    @Override
    public BLangNode transform(ObjectTypeDescriptorNode objTypeDescNode) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();

        for (Token qualifier : objTypeDescNode.objectTypeQualifiers()) {
            SyntaxKind kind = qualifier.kind();
            if (kind == SyntaxKind.CLIENT_KEYWORD) {
                objectTypeNode.flagSet.add(Flag.CLIENT);
                continue;
            }

            if (kind == SyntaxKind.SERVICE_KEYWORD) {
                objectTypeNode.flagSet.add(SERVICE);
                continue;
            }

            if (kind == SyntaxKind.ISOLATED_KEYWORD) {
                objectTypeNode.flagSet.add(ISOLATED);
                continue;
            }

            throw new RuntimeException("Syntax kind is not supported: " + kind);
        }

        NodeList<Node> members = objTypeDescNode.members();
        for (Node node : members) {
            // TODO: Check for fields other than SimpleVariableNode
            BLangNode bLangNode = node.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    if (objectTypeNode.initFunction == null) {
                        bLangFunction.objInitFunction = true;
                        objectTypeNode.initFunction = bLangFunction;
                    } else {
                        objectTypeNode.addFunction(bLangFunction);
                    }
                } else {
                    objectTypeNode.addFunction(bLangFunction);
                }
            } else if (bLangNode.getKind() == NodeKind.VARIABLE) {
                objectTypeNode.addField((BLangSimpleVariable) bLangNode);
            } else if (bLangNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                objectTypeNode.addTypeReference((BLangType) bLangNode);
            }
        }

        objectTypeNode.pos = getPosition(objTypeDescNode);

        if (members.size() > 0) {
            trimLeft(objectTypeNode.pos, getPosition(members.get(0)));
            trimRight(objectTypeNode.pos, getPosition(members.get(members.size() - 1)));
        } else {
            trimLeft(objectTypeNode.pos, getPosition(objTypeDescNode.closeBrace()));
            trimRight(objectTypeNode.pos, getPosition(objTypeDescNode.openBrace()));
        }

        boolean isAnonymous = checkIfAnonymous(objTypeDescNode);
        objectTypeNode.isAnonymous = isAnonymous;

        if (!isAnonymous) {
            return objectTypeNode;
        }

        return deSugarTypeAsUserDefType(objectTypeNode);
    }

    public BLangClassDefinition createObjectExpressionBody(NodeList<Node> members) {
        BLangClassDefinition classDefinition = (BLangClassDefinition) TreeBuilder.createClassDefNode();
        classDefinition.flagSet.add(Flag.ANONYMOUS);

        for (Node node : members) {
            BLangNode bLangNode = node.apply(this);
            NodeKind nodeKind =  bLangNode.getKind();
            if (nodeKind == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                if (!Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    classDefinition.addFunction(bLangFunction);
                    continue;
                }
                if (classDefinition.initFunction != null) {
                    classDefinition.addFunction(bLangFunction);
                    continue;
                }
                if (bLangFunction.requiredParams.size() != 0) {
                    dlog.error(bLangFunction.pos, DiagnosticCode.OBJECT_CTOR_INIT_CANNOT_HAVE_PARAMETERS);
                    continue;
                }
                bLangFunction.objInitFunction = true;
                classDefinition.initFunction = bLangFunction;
            } else if (nodeKind == NodeKind.VARIABLE) {
                classDefinition.addField((BLangSimpleVariable) bLangNode);
            } else if (nodeKind == NodeKind.USER_DEFINED_TYPE) {
                dlog.error(bLangNode.pos, DiagnosticCode.OBJECT_CTOR_DOES_NOT_SUPPORT_TYPE_REFERENCE_MEMBERS);
            }
        }

        classDefinition.internal = true;
        return classDefinition;
    }

    /**
     * Object constructor expression creates a class definition for the type defined through the object constructor.
     * Then add the class definition as a top level node. Using the class definition initialize the object defined in
     * the object constructor. Therefore this can be considered as a desugar.
     * example:
     *  var objVariable = object { int n; };
     *  // will be desugared to
     *  class anonType0 { int n; }
     *  var objVariable = new anonType0();
     *
     * @param objectConstructorExpressionNode object ctor expression node
     * @return BLangTypeInit node which initialize the class definition
     */
    @Override
    public BLangNode transform(ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        DiagnosticPos pos = getPositionWithoutMetadata(objectConstructorExpressionNode);
        BLangClassDefinition annonClassDef = createObjectExpressionBody(objectConstructorExpressionNode.members());
        annonClassDef.pos = pos;
        BLangObjectConstructorExpression objectCtorExpression = TreeBuilder.createObjectCtorExpression();
        objectCtorExpression.pos = pos;
        objectCtorExpression.classNode = annonClassDef;

        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(diagnosticSource.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        annonClassDef.setName(anonTypeGenName);
        annonClassDef.flagSet.add(Flag.PUBLIC);

        Optional<TypeDescriptorNode> typeReference = objectConstructorExpressionNode.typeReference();
        typeReference.ifPresent(typeReferenceNode -> {
            objectCtorExpression.addTypeReference(createTypeNode(typeReferenceNode));
        });

        annonClassDef.annAttachments = applyAll(objectConstructorExpressionNode.annotations());
        addToTop(annonClassDef);

        NodeList<Token> objectConstructorQualifierList = objectConstructorExpressionNode.objectTypeQualifiers();
        for (Token qualifier : objectConstructorQualifierList) {
            if (qualifier.kind() == SyntaxKind.CLIENT_KEYWORD) {
                annonClassDef.flagSet.add(Flag.CLIENT);
                objectCtorExpression.isClient = true;
            } else {
                throw new RuntimeException("Syntax kind is not supported: " + qualifier.kind());
            }
        }

        BLangIdentifier identifier = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        BLangUserDefinedType userDefinedType = createUserDefinedType(pos, identifier, annonClassDef.name);

        BLangTypeInit initNode = (BLangTypeInit) TreeBuilder.createInitNode();
        initNode.pos = pos;
        initNode.userDefinedType = userDefinedType;

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        BLangIdentifier pkgAlias = createIdentifier(pos, "");
        BLangNameReference nameReference =  new BLangNameReference(pos, null, pkgAlias, annonClassDef.name);

        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;

        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;

        objectCtorExpression.classNode = annonClassDef;
        objectCtorExpression.typeInit = initNode;
        return initNode;
    }

    @Override
    public BLangNode transform(ObjectFieldNode objFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(objFieldNode.fieldName(), objFieldNode.typeName(),
                                                        objFieldNode.expression().orElse(null),
                                                        false, false, objFieldNode.visibilityQualifier().orElse(null),
                                                        getAnnotations(objFieldNode.metadata()));
        // Transform documentation
        Optional<Node> doc = getDocumentationString(objFieldNode.metadata());
        simpleVar.markdownDocumentationAttachment = createMarkdownDocumentationAttachment(doc);

        addFinalQualifier(objFieldNode.finalKeyword(), simpleVar);
        simpleVar.pos = getPositionWithoutMetadata(objFieldNode);
        return simpleVar;
    }

    @Override
    public BLangNode transform(ServiceDeclarationNode serviceDeclrNode) {
        return createService(serviceDeclrNode, serviceDeclrNode.serviceName().orElse(null), false);
    }

    private BLangNode createService(Node serviceNode, IdentifierToken serviceNameNode, boolean isAnonServiceValue) {
        // Any Service can be represented in two major components.
        //  1) A anonymous type node (Object)
        //  2) Variable assignment with "serviceName".
        //      This is a global variable if the service is defined in module level.
        //      Otherwise (isAnonServiceValue = true) it is a local variable definition, which is written by user.
        ServiceDeclarationNode serviceDeclrNode = null;
        ServiceConstructorExpressionNode serviceConstructorNode;
        BLangService bLService = (BLangService) TreeBuilder.createServiceNode();
        //TODO handle service.expression
        // TODO: Look for generify this into sepearte method for type as well
        bLService.isAnonymousServiceValue = isAnonServiceValue;

        DiagnosticPos pos = getPositionWithoutMetadata(serviceNode);
        if (serviceNode instanceof ServiceDeclarationNode) {
            trimLeft(pos, getPosition(((ServiceDeclarationNode) serviceNode).serviceKeyword()));
        }
        String serviceName;
        DiagnosticPos identifierPos;
        if (isAnonServiceValue || serviceNameNode == null) {
            serviceName = this.anonymousModelHelper.getNextAnonymousServiceVarKey(diagnosticSource.pkgID);
            identifierPos = pos;
        } else {
            if (serviceNameNode == null || serviceNameNode.isMissing()) {
                serviceName = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
            } else {
                serviceName = serviceNameNode.text();
            }
            identifierPos = getPosition(serviceNameNode);
        }

        String serviceTypeName =
                this.anonymousModelHelper.getNextAnonymousServiceTypeKey(diagnosticSource.pkgID, serviceName);
        BLangIdentifier serviceVar = createIdentifier(identifierPos, serviceName);
        serviceVar.pos = identifierPos;
        bLService.setName(serviceVar);
        if (!isAnonServiceValue) {
            serviceDeclrNode = (ServiceDeclarationNode) serviceNode;
            for (Node expr : serviceDeclrNode.expressions()) {
                bLService.attachedExprs.add(createExpression(expr));
            }
        }

        if (isAnonServiceValue) {
            bLService.annAttachments = applyAll(((ServiceConstructorExpressionNode) serviceNode).annotations());
        } else {
            bLService.annAttachments = applyAll(getAnnotations(serviceDeclrNode.metadata()));
        }

        // We add all service nodes to top level, only for future reference.
        addToTop(bLService);

        // 1) Define type nodeDefinition for service type.
        BLangClassDefinition classDef = (BLangClassDefinition) TreeBuilder.createClassDefNode();
        BLangIdentifier serviceTypeID = createIdentifier(identifierPos, serviceTypeName);
        serviceTypeID.pos = pos;
        classDef.setName(serviceTypeID);
        classDef.flagSet.add(SERVICE);

        if (!isAnonServiceValue) {
            addServiceConstructsToClassDefinition((ServiceBodyNode) serviceDeclrNode.serviceBody(), classDef);
            bLService.markdownDocumentationAttachment =
                    createMarkdownDocumentationAttachment(getDocumentationString(serviceDeclrNode.metadata()));
        } else {
            serviceConstructorNode = (ServiceConstructorExpressionNode) serviceNode;
            addServiceConstructsToClassDefinition((ServiceBodyNode) serviceConstructorNode.serviceBody(), classDef);
            bLService.annAttachments = applyAll(serviceConstructorNode.annotations());
        }

        classDef.pos = pos;
        addToTop(classDef);
        bLService.serviceClass = classDef;

        // 2) Create service constructor.
        final BLangServiceConstructorExpr serviceConstNode = (BLangServiceConstructorExpr) TreeBuilder
                .createServiceConstructorNode();
        serviceConstNode.serviceNode = bLService;
        serviceConstNode.pos = pos;

        // Crate Global variable for service.
        bLService.pos = pos;
        if (!isAnonServiceValue) {
            BLangSimpleVariable var = (BLangSimpleVariable) createBasicVarNodeWithoutType(identifierPos,
                    Collections.emptySet(),
                    serviceName, symTable.builtinPos,
                    serviceConstNode);
            var.flagSet.add(Flag.FINAL);
            var.flagSet.add(SERVICE);

            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            bLUserDefinedType.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            bLUserDefinedType.typeName = classDef.name;
            bLUserDefinedType.pos = symTable.builtinPos;

            var.typeNode = bLUserDefinedType;
            bLService.variableNode = var;
            return var;
        } else {
            BLangServiceConstructorExpr serviceConstructorExpr =
                    (BLangServiceConstructorExpr) TreeBuilder.createServiceConstructorNode();
            serviceConstructorExpr.serviceNode = bLService;
            return serviceConstructorExpr;
        }
    }

    public void addServiceConstructsToClassDefinition(ServiceBodyNode serviceBodyNode,
                                                      BLangClassDefinition classDefinition) {
        classDefinition.flagSet.add(SERVICE);
        for (Node resourceNode : serviceBodyNode.resources()) {
            BLangNode bLangNode = resourceNode.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                classDefinition.addFunction(bLangFunction);
            }
        }
    }

    @Override
    public BLangNode transform(ServiceBodyNode serviceBodyNode) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();
        objectTypeNode.flagSet.add(SERVICE);
        for (Node resourceNode : serviceBodyNode.resources()) {
            BLangNode bLangNode = resourceNode.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                objectTypeNode.addFunction(bLangFunction);
            }
        }
        objectTypeNode.isAnonymous = false;
        objectTypeNode.pos = getPosition(serviceBodyNode);
        return objectTypeNode;
    }

    @Override
    public BLangNode transform(ExpressionFunctionBodyNode expressionFunctionBodyNode) {
        BLangExprFunctionBody bLExprFunctionBody = (BLangExprFunctionBody) TreeBuilder.createExprFunctionBodyNode();
        bLExprFunctionBody.expr = createExpression(expressionFunctionBodyNode.expression());
        bLExprFunctionBody.pos = getPosition(expressionFunctionBodyNode);
        return bLExprFunctionBody;
    }

    @Override
    public BLangNode transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        boolean hasRestField = false;
        boolean isAnonymous = checkIfAnonymous(recordTypeDescriptorNode);

        for (Node field : recordTypeDescriptorNode.fields()) {
            if (field.kind() == SyntaxKind.RECORD_FIELD) {
                BLangSimpleVariable bLFiled = (BLangSimpleVariable) field.apply(this);
                Optional<Node> doc = getDocumentationString(((RecordFieldNode) field).metadata());
                bLFiled.markdownDocumentationAttachment = createMarkdownDocumentationAttachment(doc);
                recordTypeNode.fields.add(bLFiled);
            } else if (field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
                BLangSimpleVariable bLFiled = (BLangSimpleVariable) field.apply(this);
                Optional<Node> doc = getDocumentationString(((RecordFieldWithDefaultValueNode) field).metadata());
                bLFiled.markdownDocumentationAttachment = createMarkdownDocumentationAttachment(doc);
                recordTypeNode.fields.add(bLFiled);
            } else {
                recordTypeNode.addTypeReference(createTypeNode(field));
            }
        }
        Optional<RecordRestDescriptorNode> recordRestDesc = recordTypeDescriptorNode.recordRestDescriptor();
        if (recordRestDesc.isPresent()) {
            recordTypeNode.restFieldType = createTypeNode(recordRestDesc.get());
            hasRestField = true;
        }
        boolean isOpen = recordTypeDescriptorNode.bodyStartDelimiter().kind() == SyntaxKind.OPEN_BRACE_TOKEN;
        recordTypeNode.sealed = !(hasRestField || isOpen);
        recordTypeNode.pos = getPosition(recordTypeDescriptorNode);
        recordTypeNode.isAnonymous = isAnonymous;
        recordTypeNode.isLocal = this.isInLocalContext;

        // If anonymous type, create a user defined type and return it.
        if (!isAnonymous || this.isInLocalContext) {
            return recordTypeNode;
        }

        return createAnonymousRecordType(recordTypeDescriptorNode, recordTypeNode);
    }

    @Override
    public BLangNode transform(SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        BLangFiniteTypeNode bLangFiniteTypeNode = new BLangFiniteTypeNode();
        BLangLiteral simpleLiteral = createSimpleLiteral(singletonTypeDescriptorNode.simpleContExprNode());
        bLangFiniteTypeNode.valueSpace.add(simpleLiteral);
        return bLangFiniteTypeNode;
    }

    @Override
    public BLangNode transform(BuiltinSimpleNameReferenceNode singletonTypeDescriptorNode) {
        return createTypeNode(singletonTypeDescriptorNode);
    }

    @Override
    public BLangNode transform(TypeReferenceNode typeReferenceNode) {
        return createTypeNode(typeReferenceNode.typeName());
    }

    @Override
    public BLangNode transform(RecordFieldNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName(), recordFieldNode.typeName(),
                getAnnotations(recordFieldNode.metadata()));
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (recordFieldNode.questionMarkToken().isPresent()) {
            simpleVar.flagSet.add(Flag.OPTIONAL);
        } else {
            simpleVar.flagSet.add(Flag.REQUIRED);
        }

        addRedonlyQualifier(recordFieldNode.readonlyKeyword(), recordFieldNode.typeName(), simpleVar);

        simpleVar.pos = getPositionWithoutMetadata(recordFieldNode);
        return simpleVar;
    }

    @Override
    public BLangNode transform(RecordFieldWithDefaultValueNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName(), recordFieldNode.typeName(),
                getAnnotations(recordFieldNode.metadata()));
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (isPresent(recordFieldNode.expression())) {
            simpleVar.setInitialExpression(createExpression(recordFieldNode.expression()));
        }

        addRedonlyQualifier(recordFieldNode.readonlyKeyword(), recordFieldNode.typeName(), simpleVar);

        simpleVar.pos = getPositionWithoutMetadata(recordFieldNode);
        return simpleVar;
    }

    private void addRedonlyQualifier(Optional<Token> readonlyKeyword, Node typeDesc, BLangSimpleVariable simpleVar) {
        if (readonlyKeyword.isPresent()) {
            BLangValueType readOnlyTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            readOnlyTypeNode.pos = getPosition(readonlyKeyword.get());
            readOnlyTypeNode.typeKind = TypeKind.READONLY;
            if (simpleVar.typeNode.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
                ((BLangIntersectionTypeNode) simpleVar.typeNode).constituentTypeNodes.add(readOnlyTypeNode);
            } else {
                BLangIntersectionTypeNode intersectionTypeNode =
                        (BLangIntersectionTypeNode) TreeBuilder.createIntersectionTypeNode();
                intersectionTypeNode.constituentTypeNodes.add(simpleVar.typeNode);
                intersectionTypeNode.constituentTypeNodes.add(readOnlyTypeNode);
                intersectionTypeNode.pos = getPosition(typeDesc);
                simpleVar.typeNode = intersectionTypeNode;
            }

            simpleVar.flagSet.add(Flag.READONLY);
        }
    }

    @Override
    public BLangNode transform(RecordRestDescriptorNode recordFieldNode) {
        return createTypeNode(recordFieldNode.typeName());
    }

    @Override
    public BLangNode transform(FunctionDefinitionNode funcDefNode) {
        BLangFunction bLFunction = createFunctionNode(funcDefNode.functionName(), funcDefNode.qualifierList(),
                funcDefNode.functionSignature(), funcDefNode.functionBody());

        bLFunction.annAttachments = applyAll(getAnnotations(funcDefNode.metadata()));
        bLFunction.pos = getPositionWithoutMetadata(funcDefNode);

        bLFunction.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(funcDefNode.metadata()));
        return bLFunction;
    }

    private BLangFunction createFunctionNode(IdentifierToken funcName, NodeList<Token> qualifierList,
            FunctionSignatureNode functionSignature, FunctionBodyNode functionBody) {

        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();

        // Set function name
        bLFunction.name = createIdentifier(getPosition(funcName), funcName);
        //Set method qualifiers
        setFunctionQualifiers(bLFunction, qualifierList);
        // Set function signature
        populateFuncSignature(bLFunction, functionSignature);

        // Set the function body
        if (functionBody == null) {
            bLFunction.body = null;
            bLFunction.flagSet.add(Flag.INTERFACE);
            bLFunction.interfaceFunction = true;
        } else {
            bLFunction.body = (BLangFunctionBody) functionBody.apply(this);
            if (bLFunction.body.getKind() == NodeKind.EXTERN_FUNCTION_BODY) {
                bLFunction.flagSet.add(Flag.NATIVE);
            }
        }
        return bLFunction;
    }

    private void setFunctionQualifiers(BLangFunction bLFunction, NodeList<Token> qualifierList) {

        for (Token qualifier : qualifierList) {
            switch (qualifier.kind()) {
                case PUBLIC_KEYWORD:
                    bLFunction.flagSet.add(Flag.PUBLIC);
                    break;
                case PRIVATE_KEYWORD:
                    bLFunction.flagSet.add(Flag.PRIVATE);
                    break;
                case REMOTE_KEYWORD:
                    bLFunction.flagSet.add(Flag.REMOTE);
                    break;
                case TRANSACTIONAL_KEYWORD:
                    bLFunction.flagSet.add(Flag.TRANSACTIONAL);
                    break;
                case RESOURCE_KEYWORD:
                    bLFunction.flagSet.add(Flag.RESOURCE);
                    break;
                case ISOLATED_KEYWORD:
                    bLFunction.flagSet.add(Flag.ISOLATED);
                    break;
                default:
                    continue;
            }
        }
    }

    @Override
    public BLangNode transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        BLangExternalFunctionBody externFunctionBodyNode =
                (BLangExternalFunctionBody) TreeBuilder.createExternFunctionBodyNode();
        externFunctionBodyNode.annAttachments = applyAll(externalFunctionBodyNode.annotations());
        return externFunctionBodyNode;
    }

    @Override
    public BLangNode transform(ExplicitAnonymousFunctionExpressionNode anonFuncExprNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        DiagnosticPos pos = getPosition(anonFuncExprNode);

        // Set function name
        bLFunction.name = createIdentifier(pos,
                                           anonymousModelHelper.getNextAnonymousFunctionKey(diagnosticSource.pkgID));

        // Set function signature
        populateFuncSignature(bLFunction, anonFuncExprNode.functionSignature());

        // Set the function body
        bLFunction.body = (BLangFunctionBody) anonFuncExprNode.functionBody().apply(this);

//        attachAnnotations(function, annCount, false);
        bLFunction.pos = pos;

        bLFunction.addFlag(Flag.LAMBDA);
        bLFunction.addFlag(Flag.ANONYMOUS);

        setFunctionQualifiers(bLFunction, anonFuncExprNode.qualifierList());

        addToTop(bLFunction);

        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = bLFunction;
        lambdaExpr.pos = pos;
        return lambdaExpr;
    }

    @Override
    public BLangNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        BLangBlockFunctionBody bLFuncBody = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        this.isInLocalContext = true;
        List<BLangStatement> statements = new ArrayList<>();
        if (functionBodyBlockNode.namedWorkerDeclarator().isPresent()) {
            NamedWorkerDeclarator namedWorkerDeclarator = functionBodyBlockNode.namedWorkerDeclarator().get();
            generateAndAddBLangStatements(namedWorkerDeclarator.workerInitStatements(), statements);

            for (NamedWorkerDeclarationNode workerDeclarationNode : namedWorkerDeclarator.namedWorkerDeclarations()) {
                statements.add((BLangStatement) workerDeclarationNode.apply(this));
                // Consume resultant additional statements
                while (!this.additionalStatements.empty()) {
                    statements.add(additionalStatements.pop());
                }
            }
        }

        generateAndAddBLangStatements(functionBodyBlockNode.statements(), statements);

        bLFuncBody.stmts = statements;
        bLFuncBody.pos = getPosition(functionBodyBlockNode);
        this.isInLocalContext = false;
        return bLFuncBody;
    }

    @Override
    public BLangNode transform(ForEachStatementNode forEachStatementNode) {
        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = getPosition(forEachStatementNode);
        TypedBindingPatternNode typedBindingPatternNode = forEachStatementNode.typedBindingPattern();
        VariableDefinitionNode variableDefinitionNode = createBLangVarDef(getPosition(typedBindingPatternNode),
                typedBindingPatternNode, Optional.empty(), Optional.empty());
        foreach.setVariableDefinitionNode(variableDefinitionNode);
        foreach.isDeclaredWithVar = typedBindingPatternNode.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC;

        BLangBlockStmt foreachBlock = (BLangBlockStmt) forEachStatementNode.blockStatement().apply(this);
        foreachBlock.pos = getPosition(forEachStatementNode.blockStatement());
        foreach.setBody(foreachBlock);
        foreach.setCollection(createExpression(forEachStatementNode.actionOrExpressionNode()));

        forEachStatementNode.onFailClause().ifPresent(onFailClauseNode -> {
            foreach.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });

        return foreach;
    }

    @Override
    public BLangNode transform(ForkStatementNode forkStatementNode) {
        BLangForkJoin forkJoin = (BLangForkJoin) TreeBuilder.createForkJoinNode();
        DiagnosticPos forkStmtPos = getPosition(forkStatementNode);
        forkJoin.pos = forkStmtPos;
        return forkJoin;
    }

    @Override
    public BLangNode transform(NamedWorkerDeclarationNode namedWorkerDeclNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        DiagnosticPos workerBodyPos = getPosition(namedWorkerDeclNode.workerBody());

        // Set function name
        bLFunction.name = createIdentifier(symTable.builtinPos,
                                           anonymousModelHelper.getNextAnonymousFunctionKey(diagnosticSource.pkgID));

        // Set the function body
        BLangBlockStmt blockStmt = (BLangBlockStmt) namedWorkerDeclNode.workerBody().apply(this);
        BLangBlockFunctionBody bodyNode = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        bodyNode.stmts = blockStmt.stmts;
        bodyNode.pos = workerBodyPos;
        bLFunction.body = bodyNode;
        bLFunction.internal = true;

        bLFunction.pos = workerBodyPos;

        bLFunction.addFlag(Flag.LAMBDA);
        bLFunction.addFlag(Flag.ANONYMOUS);
        bLFunction.addFlag(Flag.WORKER);

        if (namedWorkerDeclNode.transactionalKeyword().isPresent()) {
            bLFunction.addFlag(Flag.TRANSACTIONAL);
        }

        // change default worker name
        String workerName = namedWorkerDeclNode.workerName().text();
        if (workerName.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            bLFunction.defaultWorkerName.originalValue = workerName;
            workerName = IdentifierUtils.unescapeUnicodeCodepoints(workerName.substring(1));
        }
        bLFunction.defaultWorkerName.value = workerName;
        bLFunction.defaultWorkerName.pos = getPosition(namedWorkerDeclNode.workerName());

        NodeList<AnnotationNode> annotations = namedWorkerDeclNode.annotations();
        bLFunction.annAttachments = applyAll(annotations);

        // Set Return Type
        Optional<Node> retNode = namedWorkerDeclNode.returnTypeDesc();
        if (retNode.isPresent()) {
            ReturnTypeDescriptorNode returnType = (ReturnTypeDescriptorNode) retNode.get();
            bLFunction.setReturnTypeNode(createTypeNode(returnType.type()));
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = getPosition(namedWorkerDeclNode);
            bLValueType.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(bLValueType);
        }

        addToTop(bLFunction);

        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = bLFunction;
        lambdaExpr.pos = workerBodyPos;
        lambdaExpr.internal = true;

        String workerLambdaName = WORKER_LAMBDA_VAR_PREFIX + workerName;

        DiagnosticPos workerNamePos = getPosition(namedWorkerDeclNode.workerName());
        // Check if the worker is in a fork. If so add the lambda function to the worker list in fork, else ignore.
        BLangSimpleVariable var = new SimpleVarBuilder()
                .with(workerLambdaName, workerNamePos)
                .setExpression(lambdaExpr)
                .isDeclaredWithVar()
                .isFinal()
                .build();

        if (namedWorkerDeclNode.transactionalKeyword().isPresent()) {
            var.addFlag(Flag.TRANSACTIONAL);
        }

        BLangSimpleVariableDef lamdaWrkr = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        lamdaWrkr.pos = workerBodyPos;
        var.pos = workerBodyPos;
        lamdaWrkr.setVariable(var);
        lamdaWrkr.isWorker = true;
        lamdaWrkr.internal = var.internal = true;
        if (namedWorkerDeclNode.parent().kind() == SyntaxKind.FORK_STATEMENT) {
            lamdaWrkr.isInFork = true;
            lamdaWrkr.var.flagSet.add(Flag.FORKED);
        }

//        if (!this.forkJoinNodesStack.empty()) {
//            // TODO: Revisit the fork join worker declaration and decide whether move this to desugar.
//            lamdaWrkr.isInFork = true;
//            lamdaWrkr.var.flagSet.add(Flag.FORKED);
//            this.forkJoinNodesStack.peek().addWorkers(lamdaWrkr);
//        }

        BLangInvocation bLInvocation = (BLangInvocation) TreeBuilder.createActionInvocation();
        BLangIdentifier nameInd = this.createIdentifier(workerNamePos, workerLambdaName);
        BLangNameReference reference = new BLangNameReference(workerNamePos, null, TreeBuilder.createIdentifierNode(),
                                                              nameInd);
        bLInvocation.pkgAlias = (BLangIdentifier) reference.pkgAlias;
        bLInvocation.name = (BLangIdentifier) reference.name;
        bLInvocation.pos = workerNamePos;
        bLInvocation.flagSet = new HashSet<>();
        bLInvocation.annAttachments = bLFunction.annAttachments;

        if (bLInvocation.getKind() == NodeKind.INVOCATION) {
            bLInvocation.async = true;
//            attachAnnotations(invocation, numAnnotations, false);
        } else {
            dlog.error(workerBodyPos, DiagnosticCode.START_REQUIRE_INVOCATION);
        }

        BLangSimpleVariable invoc = new SimpleVarBuilder()
                .with(workerName, workerNamePos)
                .isDeclaredWithVar()
                .isWorkerVar()
                .setExpression(bLInvocation)
                .isFinal()
                .setPos(workerNamePos)
                .build();

        BLangSimpleVariableDef workerInvoc = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        workerInvoc.pos = workerNamePos;
        workerInvoc.setVariable(invoc);
        workerInvoc.isWorker = true;
        invoc.flagSet.add(Flag.WORKER);
        this.additionalStatements.push(workerInvoc);

        return lamdaWrkr;
    }

    private <A extends BLangNode, B extends Node> List<A> applyAll(NodeList<B> annotations) {
        ArrayList<A> annAttachments = new ArrayList<>();

        if (annotations == null) {
            return annAttachments;
        }

        for (B annotation : annotations) {
            A blNode = (A) annotation.apply(this);
            annAttachments.add(blNode);
        }
        return annAttachments;
    }

    @Override
    public BLangNode transform(AnnotationNode annotation) {
        Node name = annotation.annotReference();
        BLangAnnotationAttachment bLAnnotationAttachment =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        if (annotation.annotValue().isPresent()) {
            MappingConstructorExpressionNode map = annotation.annotValue().get();
            BLangExpression bLExpression = (BLangExpression) map.apply(this);
            bLAnnotationAttachment.setExpression(bLExpression);
        }
        BLangNameReference nameReference = createBLangNameReference(name);
        bLAnnotationAttachment.setAnnotationName(nameReference.name);
        bLAnnotationAttachment.setPackageAlias(nameReference.pkgAlias);
        bLAnnotationAttachment.pos = getPosition(annotation);
        return bLAnnotationAttachment;
    }

    @Override
    public BLangNode transform(QueryActionNode queryActionNode) {
        BLangQueryAction bLQueryAction = (BLangQueryAction) TreeBuilder.createQueryActionNode();
        BLangDoClause doClause = (BLangDoClause) TreeBuilder.createDoClauseNode();
        doClause.body = (BLangBlockStmt) queryActionNode.blockStatement().apply(this);
        expandLeft(doClause.body.pos, getPosition(queryActionNode.doKeyword()));
        doClause.pos = doClause.body.pos;
        bLQueryAction.queryClauseList.add(queryActionNode.queryPipeline().fromClause().apply(this));
        bLQueryAction.queryClauseList.addAll(applyAll(queryActionNode.queryPipeline().intermediateClauses()));
        bLQueryAction.queryClauseList.add(doClause);
        bLQueryAction.doClause = doClause;
        bLQueryAction.pos = getPosition(queryActionNode);
        return bLQueryAction;
    }

    @Override
    public BLangNode transform(AnnotationDeclarationNode annotationDeclarationNode) {
        BLangAnnotation annotationDecl = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        DiagnosticPos pos = getPositionWithoutMetadata(annotationDeclarationNode);
        annotationDecl.pos = pos;
        annotationDecl.name = createIdentifier(annotationDeclarationNode.annotationTag());

        if (annotationDeclarationNode.visibilityQualifier().isPresent()) {
            annotationDecl.addFlag(Flag.PUBLIC);
        }

        if (annotationDeclarationNode.constKeyword().isPresent()) {
            annotationDecl.addFlag(Flag.CONSTANT);
        }

        annotationDecl.annAttachments = applyAll(getAnnotations(annotationDeclarationNode.metadata()));

        annotationDecl.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(annotationDeclarationNode.metadata()));

        Optional<Node> typedesc = annotationDeclarationNode.typeDescriptor();
        if (typedesc.isPresent()) {
            annotationDecl.typeNode = createTypeNode(typedesc.get());
        }

        SeparatedNodeList<Node> paramList = annotationDeclarationNode.attachPoints();

        for (Node child : paramList) {
            AnnotationAttachPointNode attachPoint = (AnnotationAttachPointNode) child;
            boolean source = attachPoint.sourceKeyword().isPresent();
            AttachPoint bLAttachPoint;
            Token firstIndent =  attachPoint.firstIdent();

            switch (firstIndent.kind()) {
                case OBJECT_KEYWORD:
                    Token secondIndent = attachPoint.secondIdent().get();
                    switch (secondIndent.kind()) {
                        case FUNCTION_KEYWORD:
                            bLAttachPoint =
                                    AttachPoint.getAttachmentPoint(AttachPoint.Point.OBJECT_METHOD.getValue(), source);
                            break;
                        case FIELD_KEYWORD:
                            bLAttachPoint =
                                    AttachPoint.getAttachmentPoint(AttachPoint.Point.OBJECT_FIELD.getValue(), source);
                            break;
                        default:
                            throw new RuntimeException("Syntax kind is not supported: " + secondIndent.kind());
                    }
                    break;
                case RESOURCE_KEYWORD:
                    bLAttachPoint = AttachPoint.getAttachmentPoint(AttachPoint.Point.RESOURCE.getValue(), source);
                    break;
                case RECORD_KEYWORD:
                    bLAttachPoint = AttachPoint.getAttachmentPoint(AttachPoint.Point.RECORD_FIELD.getValue(), source);
                    break;
                default:
                    bLAttachPoint = AttachPoint.getAttachmentPoint(firstIndent.text(), source);
            }
            annotationDecl.addAttachPoint(bLAttachPoint);
        }

        return annotationDecl;
    }

    @Override
    public BLangNode transform(AnnotAccessExpressionNode annotAccessExpressionNode) {
        BLangAnnotAccessExpr annotAccessExpr = (BLangAnnotAccessExpr) TreeBuilder.createAnnotAccessExpressionNode();
        Node annotTagReference = annotAccessExpressionNode.annotTagReference();
        if (annotAccessExpressionNode.annotTagReference().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode annotName = (SimpleNameReferenceNode) annotTagReference;
            annotAccessExpr.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            annotAccessExpr.annotationName = createIdentifier(annotName.name());
        } else {
            QualifiedNameReferenceNode qulifiedName =
                    (QualifiedNameReferenceNode) annotTagReference;
            annotAccessExpr.pkgAlias = createIdentifier(qulifiedName.modulePrefix());
            annotAccessExpr.annotationName = createIdentifier(qulifiedName.identifier());
        }

        annotAccessExpr.pos = getPosition(annotAccessExpressionNode);
        annotAccessExpr.expr = createExpression(annotAccessExpressionNode.expression());
        return annotAccessExpr;
    }

    // -----------------------------------------------Expressions-------------------------------------------------------
    @Override
    public BLangNode transform(ConditionalExpressionNode conditionalExpressionNode) {
        BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.pos = getPosition(conditionalExpressionNode);
        ternaryExpr.elseExpr = createExpression(conditionalExpressionNode.endExpression());
        ternaryExpr.thenExpr = createExpression(conditionalExpressionNode.middleExpression());
        ternaryExpr.expr = createExpression(conditionalExpressionNode.lhsExpression());
        if (ternaryExpr.expr.getKind() == NodeKind.TERNARY_EXPR) {
            // Re-organizing ternary expression tree if there nested ternary expressions.
            BLangTernaryExpr root = (BLangTernaryExpr) ternaryExpr.expr;
            BLangTernaryExpr parent = root;
            while (parent.elseExpr.getKind() == NodeKind.TERNARY_EXPR) {
                parent = (BLangTernaryExpr) parent.elseExpr;
            }
            ternaryExpr.expr = parent.elseExpr;
            parent.elseExpr = ternaryExpr;
            ternaryExpr = root;
        }
        return ternaryExpr;
    }

    @Override
    public BLangNode transform(CheckExpressionNode checkExpressionNode) {
        DiagnosticPos pos = getPosition(checkExpressionNode);
        BLangExpression expr = createExpression(checkExpressionNode.expression());
        if (checkExpressionNode.checkKeyword().kind() == SyntaxKind.CHECK_KEYWORD) {
            return createCheckExpr(pos, expr);
        }
        return createCheckPanickedExpr(pos, expr);
    }

    @Override
    public BLangNode transform(TypeTestExpressionNode typeTestExpressionNode) {
        BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        typeTestExpr.expr = createExpression(typeTestExpressionNode.expression());
        typeTestExpr.typeNode = createTypeNode(typeTestExpressionNode.typeDescriptor());
        typeTestExpr.pos = getPosition(typeTestExpressionNode);

        return typeTestExpr;
    }

    @Override
    public BLangNode transform(MappingConstructorExpressionNode mapConstruct) {
        BLangRecordLiteral bLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        for (MappingFieldNode field : mapConstruct.fields()) {
            if (field.kind() == SyntaxKind.SPREAD_FIELD) {
                SpreadFieldNode spreadFieldNode = (SpreadFieldNode) field;
                BLangRecordSpreadOperatorField bLRecordSpreadOpField =
                        (BLangRecordSpreadOperatorField) TreeBuilder.createRecordSpreadOperatorField();
                bLRecordSpreadOpField.expr = createExpression(spreadFieldNode.valueExpr());
                bLiteralNode.fields.add(bLRecordSpreadOpField);
            } else if (field.kind() == SyntaxKind.COMPUTED_NAME_FIELD) {
                ComputedNameFieldNode computedNameField = (ComputedNameFieldNode) field;
                BLangRecordKeyValueField bLRecordKeyValueField =
                        (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
                bLRecordKeyValueField.valueExpr = createExpression(computedNameField.valueExpr());
                bLRecordKeyValueField.key =
                        new BLangRecordLiteral.BLangRecordKey(createExpression(computedNameField.fieldNameExpr()));
                bLRecordKeyValueField.key.computedKey = true;
                bLiteralNode.fields.add(bLRecordKeyValueField);
            } else {
                SpecificFieldNode specificField = (SpecificFieldNode) field;
                io.ballerina.compiler.syntax.tree.ExpressionNode valueExpr = specificField.valueExpr().orElse(null);
                if (valueExpr == null) {
                    BLangRecordLiteral.BLangRecordVarNameField fieldVar =
                            (BLangRecordLiteral.BLangRecordVarNameField) TreeBuilder.createRecordVarRefNameFieldNode();
                    fieldVar.variableName = createIdentifier((Token) ((SpecificFieldNode) field).fieldName());
                    fieldVar.pkgAlias = createIdentifier(null, "");
                    fieldVar.pos = fieldVar.variableName.pos;
                    fieldVar.readonly = specificField.readonlyKeyword().isPresent();
                    bLiteralNode.fields.add(fieldVar);
                } else {
                    BLangRecordKeyValueField bLRecordKeyValueField =
                            (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
                    bLRecordKeyValueField.valueExpr = createExpression(valueExpr);
                    bLRecordKeyValueField.key =
                            new BLangRecordLiteral.BLangRecordKey(createExpression(specificField.fieldName()));
                    bLRecordKeyValueField.key.computedKey = false;
                    bLRecordKeyValueField.readonly = specificField.readonlyKeyword().isPresent();
                    bLiteralNode.fields.add(bLRecordKeyValueField);
                }
            }
        }
        bLiteralNode.pos = getPosition(mapConstruct);
        return bLiteralNode;
    }

    @Override
    public BLangNode transform(ListConstructorExpressionNode listConstructorExprNode) {
        List<BLangExpression> argExprList = new ArrayList<>();
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        for (Node expr : listConstructorExprNode.expressions()) {
            argExprList.add(createExpression(expr));
        }
        listConstructorExpr.exprs = argExprList;
        listConstructorExpr.pos = getPosition(listConstructorExprNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(UnaryExpressionNode unaryExprNode) {
        DiagnosticPos pos = getPosition(unaryExprNode);
        SyntaxKind expressionKind = unaryExprNode.expression().kind();
        if (expressionKind == SyntaxKind.NUMERIC_LITERAL) {
            BLangNumericLiteral numericLiteral = (BLangNumericLiteral) createSimpleLiteral(unaryExprNode);
            return numericLiteral;
        }
        OperatorKind operator = OperatorKind.valueFrom(unaryExprNode.unaryOperator().text());
        BLangExpression expr = createExpression(unaryExprNode.expression());
        return createBLangUnaryExpr(pos, operator, expr);
    }

    @Override
    public BLangNode transform(TypeofExpressionNode typeofExpressionNode) {
        DiagnosticPos pos = getPosition(typeofExpressionNode);
        OperatorKind operator = OperatorKind.valueFrom(typeofExpressionNode.typeofKeyword().text());
        BLangExpression expr = createExpression(typeofExpressionNode.expression());
        return createBLangUnaryExpr(pos, operator, expr);
    }

    @Override
    public BLangNode transform(BinaryExpressionNode binaryExprNode) {
        if (binaryExprNode.operator().kind() == SyntaxKind.ELVIS_TOKEN) {
            BLangElvisExpr elvisExpr = (BLangElvisExpr) TreeBuilder.createElvisExpressionNode();
            elvisExpr.pos = getPosition(binaryExprNode);
            elvisExpr.lhsExpr = createExpression(binaryExprNode.lhsExpr());
            elvisExpr.rhsExpr = createExpression(binaryExprNode.rhsExpr());
            return elvisExpr;
        }

        BLangBinaryExpr bLBinaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        bLBinaryExpr.pos = getPosition(binaryExprNode);
        bLBinaryExpr.lhsExpr = createExpression(binaryExprNode.lhsExpr());
        bLBinaryExpr.rhsExpr = createExpression(binaryExprNode.rhsExpr());
        bLBinaryExpr.opKind = OperatorKind.valueFrom(binaryExprNode.operator().text());
        return bLBinaryExpr;
    }

    @Override
    public BLangNode transform(FieldAccessExpressionNode fieldAccessExprNode) {
        BLangFieldBasedAccess bLFieldBasedAccess;
        Node fieldName = fieldAccessExprNode.fieldName();
        if (fieldName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qualifiedFieldName = (QualifiedNameReferenceNode) fieldName;
            BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess accessWithPrefixNode =
                    (BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess)
                            TreeBuilder.createFieldBasedAccessWithPrefixNode();
            accessWithPrefixNode.nsPrefix = createIdentifier(qualifiedFieldName.modulePrefix());
            accessWithPrefixNode.field = createIdentifier(qualifiedFieldName.identifier());
            bLFieldBasedAccess = accessWithPrefixNode;
            bLFieldBasedAccess.fieldKind = FieldKind.WITH_NS;
        } else {
            bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
            bLFieldBasedAccess.field =
                    createIdentifier(((SimpleNameReferenceNode) fieldName).name());
            bLFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        }

        io.ballerina.compiler.syntax.tree.ExpressionNode containerExpr = fieldAccessExprNode.expression();
        if (containerExpr.kind() == SyntaxKind.BRACED_EXPRESSION) {
            bLFieldBasedAccess.expr = createExpression(((BracedExpressionNode) containerExpr).expression());
        } else {
            bLFieldBasedAccess.expr = createExpression(containerExpr);
        }

        bLFieldBasedAccess.pos = getPosition(fieldAccessExprNode);
        bLFieldBasedAccess.field.pos = getPosition(fieldAccessExprNode);
        trimLeft(bLFieldBasedAccess.field.pos, getPosition(fieldAccessExprNode.dotToken()));
        bLFieldBasedAccess.optionalFieldAccess = false;
        return bLFieldBasedAccess;
    }

    @Override
    public BLangNode transform(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        BLangFieldBasedAccess bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        Node fieldName = optionalFieldAccessExpressionNode.fieldName();

        if (fieldName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qualifiedFieldName = (QualifiedNameReferenceNode) fieldName;
            BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess accessWithPrefixNode =
                    (BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) TreeBuilder
                            .createFieldBasedAccessWithPrefixNode();
            accessWithPrefixNode.nsPrefix = createIdentifier(qualifiedFieldName.modulePrefix());
            accessWithPrefixNode.field = createIdentifier(qualifiedFieldName.identifier());
            bLFieldBasedAccess = accessWithPrefixNode;
            bLFieldBasedAccess.fieldKind = FieldKind.WITH_NS;
        } else {
            bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
            bLFieldBasedAccess.field = createIdentifier(((SimpleNameReferenceNode) fieldName).name());
            bLFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        }

        bLFieldBasedAccess.pos = getPosition(optionalFieldAccessExpressionNode);
        bLFieldBasedAccess.field.pos = getPosition(optionalFieldAccessExpressionNode);
        bLFieldBasedAccess.expr = createExpression(optionalFieldAccessExpressionNode.expression());
        bLFieldBasedAccess.optionalFieldAccess = true;
        return bLFieldBasedAccess;
    }

    @Override
    public BLangNode transform(BracedExpressionNode brcExprOut) {
        return createExpression(brcExprOut.expression());
    }

    @Override
    public BLangNode transform(FunctionCallExpressionNode functionCallNode) {
        return createBLangInvocation(functionCallNode.functionName(), functionCallNode.arguments(),
                                     getPosition(functionCallNode), isFunctionCallAsync(functionCallNode));
    }

    public BLangNode transform(MethodCallExpressionNode methodCallExprNode) {
        BLangInvocation bLInvocation = createBLangInvocation(methodCallExprNode.methodName(),
                                                             methodCallExprNode.arguments(),
                                                             getPosition(methodCallExprNode), false);
        bLInvocation.expr = createExpression(methodCallExprNode.expression());
        return bLInvocation;
    }

    @Override
    public BLangNode transform(ImplicitNewExpressionNode implicitNewExprNode) {
        BLangTypeInit initNode = createTypeInit(implicitNewExprNode);
        BLangInvocation invocationNode = createInvocation(implicitNewExprNode, implicitNewExprNode.newKeyword());
        // Populate the argument expressions on initNode as well.
        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;

        return initNode;
    }

    @Override
    public BLangNode transform(ExplicitNewExpressionNode explicitNewExprNode) {
        BLangTypeInit initNode = createTypeInit(explicitNewExprNode);
        BLangInvocation invocationNode = createInvocation(explicitNewExprNode, explicitNewExprNode.newKeyword());
        // Populate the argument expressions on initNode as well.
        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;
        return initNode;
    }

    private boolean isFunctionCallAsync(FunctionCallExpressionNode functionCallExpressionNode) {
        return functionCallExpressionNode.parent().kind() == SyntaxKind.START_ACTION;
    }

    private BLangTypeInit createTypeInit(NewExpressionNode expression) {
        BLangTypeInit initNode = (BLangTypeInit) TreeBuilder.createInitNode();
        initNode.pos = getPosition(expression);
        if (expression.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            Node type = ((ExplicitNewExpressionNode) expression).typeDescriptor();
            initNode.userDefinedType = createTypeNode(type);
        }

        return initNode;
    }

    private BLangInvocation createInvocation(NewExpressionNode expression, Token newKeyword) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = getPosition(expression);

        populateArgsInvocation(expression, invocationNode);

        BLangNameReference nameReference = createBLangNameReference(newKeyword);
        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;

        return invocationNode;
    }

    private void populateArgsInvocation(NewExpressionNode expression, BLangInvocation invocationNode) {
        Iterator<FunctionArgumentNode> argumentsIter = getArgumentNodesIterator(expression);
        if (argumentsIter != null) {
            while (argumentsIter.hasNext()) {
                BLangExpression argument = createExpression(argumentsIter.next());
                invocationNode.argExprs.add(argument);
            }
        }
    }

    private Iterator<FunctionArgumentNode> getArgumentNodesIterator(NewExpressionNode expression) {
        Iterator<FunctionArgumentNode> argumentsIter = null;

        if (expression.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            Optional<ParenthesizedArgList> argsList = ((ImplicitNewExpressionNode) expression).parenthesizedArgList();
            if (argsList.isPresent()) {
                ParenthesizedArgList argList = argsList.get();
                argumentsIter = argList.arguments().iterator();
            }
        } else {
            ParenthesizedArgList argList =
                    (ParenthesizedArgList) ((ExplicitNewExpressionNode) expression).parenthesizedArgList();
            argumentsIter = argList.arguments().iterator();
        }

        return argumentsIter;
    }

    @Override
    public BLangNode transform(IndexedExpressionNode indexedExpressionNode) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = getPosition(indexedExpressionNode);
        SeparatedNodeList<io.ballerina.compiler.syntax.tree.ExpressionNode> keys =
                indexedExpressionNode.keyExpression();
        if (keys.size() == 1) {
            indexBasedAccess.indexExpr = createExpression(indexedExpressionNode.keyExpression().get(0));
        } else {
            BLangTableMultiKeyExpr multiKeyExpr =
                    (BLangTableMultiKeyExpr) TreeBuilder.createTableMultiKeyExpressionNode();
            multiKeyExpr.pos = getPosition(indexedExpressionNode);
            List<BLangExpression> multiKeyIndexExprs = new ArrayList<>();
            for (io.ballerina.compiler.syntax.tree.ExpressionNode keyExpr : keys) {
                multiKeyIndexExprs.add(createExpression(keyExpr));
            }
            multiKeyExpr.multiKeyIndexExprs = multiKeyIndexExprs;
            indexBasedAccess.indexExpr = multiKeyExpr;
        }

        Node containerExpr = indexedExpressionNode.containerExpression();
        BLangExpression expression = createExpression(containerExpr);
        if (containerExpr.kind() == SyntaxKind.BRACED_EXPRESSION) {
            indexBasedAccess.expr = ((BLangGroupExpr) expression).expression;
            BLangGroupExpr group = (BLangGroupExpr) TreeBuilder.createGroupExpressionNode();
            group.expression = indexBasedAccess;
            group.pos = getPosition(indexedExpressionNode);
            return group;
        } else if (containerExpr.kind() == SyntaxKind.XML_STEP_EXPRESSION) {
            // TODO : This check will be removed after changes are done for spec issue #536
            ((BLangXMLNavigationAccess) expression).childIndex = indexBasedAccess.indexExpr;
            return expression;
        }
        indexBasedAccess.expr = expression;

        return indexBasedAccess;
    }

    @Override
    public BLangTypeConversionExpr transform(TypeCastExpressionNode typeCastExpressionNode) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        // TODO : Attach annotations if available
        typeConversionNode.pos = getPosition(typeCastExpressionNode);
        TypeCastParamNode typeCastParamNode = typeCastExpressionNode.typeCastParam();
        if (typeCastParamNode != null && typeCastParamNode.type() != null) {
            typeConversionNode.typeNode = createTypeNode(typeCastParamNode.type());
        }
        typeConversionNode.expr = createExpression(typeCastExpressionNode.expression());
        typeConversionNode.annAttachments = applyAll(typeCastParamNode.annotations());
        return typeConversionNode;
    }

    @Override
    public BLangNode transform(Token token) {
        SyntaxKind kind = token.kind();
        switch (kind) {
            case XML_TEXT_CONTENT:
            case TEMPLATE_STRING:
            case CLOSE_BRACE_TOKEN:
                return createSimpleLiteral(token);
            default:
                throw new RuntimeException("Syntax kind is not supported: " + kind);
        }
    }

    @Override
    public BLangNode transform(InterpolationNode interpolationNode) {
        return createExpression(interpolationNode.expression());
    }

    @Override
    public BLangNode transform(TemplateExpressionNode expressionNode) {
        SyntaxKind kind = expressionNode.kind();
        switch (kind) {
            case XML_TEMPLATE_EXPRESSION:
                SyntaxKind contentKind = expressionNode.content().get(0).kind();
                switch (contentKind) {
                    case XML_COMMENT:
                    case XML_PI:
                    case XML_ELEMENT:
                    case XML_EMPTY_ELEMENT:
                        return createExpression(expressionNode.content().get(0));
                    default:
                        return createXMLLiteral(expressionNode);
                }
            case STRING_TEMPLATE_EXPRESSION:
                return createStringTemplateLiteral(expressionNode.content(), getPosition(expressionNode));
            case RAW_TEMPLATE_EXPRESSION:
                return createRawTemplateLiteral(expressionNode.content(), getPosition(expressionNode));
            default:
                throw new RuntimeException("Syntax kind is not supported: " + kind);
        }
    }

    @Override
    public BLangNode transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        BLangTableConstructorExpr tableConstructorExpr =
                (BLangTableConstructorExpr) TreeBuilder.createTableConstructorExpressionNode();
        tableConstructorExpr.pos = getPosition(tableConstructorExpressionNode);

        for (Node row : tableConstructorExpressionNode.rows()) {
            tableConstructorExpr.addRecordLiteral((BLangRecordLiteral) row.apply(this));
        }
        if (tableConstructorExpressionNode.keySpecifier().isPresent()) {
            tableConstructorExpr.tableKeySpecifier =
                    (BLangTableKeySpecifier) tableConstructorExpressionNode.keySpecifier().orElse(null).apply(this);
        }
        return tableConstructorExpr;
    }

    @Override
    public BLangNode transform(TrapExpressionNode trapExpressionNode) {
        BLangTrapExpr trapExpr = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        trapExpr.expr = createExpression(trapExpressionNode.expression());
        trapExpr.pos = getPosition(trapExpressionNode);
        return trapExpr;
    }

    @Override
    public BLangNode transform(ReceiveActionNode receiveActionNode) {
        BLangWorkerReceive workerReceiveExpr = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveExpr.setWorkerName(createIdentifier(receiveActionNode.receiveWorkers().name()));
        workerReceiveExpr.pos = getPosition(receiveActionNode);
        return workerReceiveExpr;
    }

    @Override
    public BLangNode transform(SyncSendActionNode syncSendActionNode) {
        BLangWorkerSyncSendExpr workerSendExpr = TreeBuilder.createWorkerSendSyncExprNode();
        workerSendExpr.setWorkerName(createIdentifier(
                syncSendActionNode.peerWorker().name()));
        workerSendExpr.expr = createExpression(syncSendActionNode.expression());
        workerSendExpr.pos = getPosition(syncSendActionNode);
        return workerSendExpr;
    }

    @Override
    public BLangNode transform(ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        BLangArrowFunction arrowFunction = (BLangArrowFunction) TreeBuilder.createArrowFunctionNode();
        arrowFunction.pos = getPosition(implicitAnonymousFunctionExpressionNode);
        arrowFunction.functionName = createIdentifier(arrowFunction.pos,
                anonymousModelHelper.getNextAnonymousFunctionKey(diagnosticSource.pkgID));
        // TODO initialize other attributes
        // arrowFunction.funcType;
        // arrowFunction.function;

        // Set Parameters
        Node param = implicitAnonymousFunctionExpressionNode.params();
        if (param.kind() == SyntaxKind.INFER_PARAM_LIST) {

            ImplicitAnonymousFunctionParameters paramsNode = (ImplicitAnonymousFunctionParameters) param;
            SeparatedNodeList<SimpleNameReferenceNode> paramList = paramsNode.parameters();

            for (SimpleNameReferenceNode child : paramList) {
                BLangUserDefinedType userDefinedType = (BLangUserDefinedType) child.apply(this);
                BLangSimpleVariable parameter = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
                parameter.name = userDefinedType.typeName;
                arrowFunction.params.add(parameter);
            }

        } else {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) param.apply(this);
            BLangSimpleVariable parameter = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            parameter.name = userDefinedType.typeName;
            arrowFunction.params.add(parameter);
        }
        arrowFunction.body = new BLangExprFunctionBody();
        arrowFunction.body.expr = createExpression(implicitAnonymousFunctionExpressionNode.expression());
        return arrowFunction;
    }

    @Override
    public BLangNode transform(CommitActionNode commitActionNode) {
        BLangCommitExpr commitExpr = TreeBuilder.createCommitExpressionNode();
        commitExpr.pos = getPosition(commitActionNode);
        return commitExpr;
    }

    @Override
    public BLangNode transform(FlushActionNode flushActionNode) {
        BLangWorkerFlushExpr workerFlushExpr = TreeBuilder.createWorkerFlushExpressionNode();
        Node optionalPeerWorker = flushActionNode.peerWorker().orElse(null);
        if (optionalPeerWorker != null) {
            SimpleNameReferenceNode peerWorker = (SimpleNameReferenceNode) optionalPeerWorker;
            workerFlushExpr.workerIdentifier = createIdentifier(peerWorker.name());
        }
        workerFlushExpr.pos = getPosition(flushActionNode);
        return workerFlushExpr;
    }

    @Override
    public BLangNode transform(LetExpressionNode letExpressionNode) {
        BLangLetExpression letExpr = (BLangLetExpression) TreeBuilder.createLetExpressionNode();
        letExpr.pos = getPosition(letExpressionNode);
        letExpr.expr = createExpression(letExpressionNode.expression());
        List<BLangLetVariable> letVars = new ArrayList<>();
        for (LetVariableDeclarationNode letVarDecl : letExpressionNode.letVarDeclarations()) {
            letVars.add(createLetVariable(letVarDecl));
        }

        letExpr.letVarDeclarations = letVars;
        return letExpr;
    }

    public BLangLetVariable createLetVariable(LetVariableDeclarationNode letVarDecl) {
        BLangLetVariable letVar = TreeBuilder.createLetVariableNode();
        VariableDefinitionNode varDefNode = createBLangVarDef(getPosition(letVarDecl), letVarDecl.typedBindingPattern(),
                Optional.of(letVarDecl.expression()), Optional.empty());
        varDefNode.getVariable().addFlag(Flag.FINAL);
        List<BLangNode> annots = applyAll(letVarDecl.annotations());
        for (BLangNode node : annots) {
            varDefNode.getVariable().addAnnotationAttachment((AnnotationAttachmentNode) node);
        }

        letVar.definitionNode = varDefNode;
        return letVar;
    }

    @Override
    public BLangNode transform(ServiceConstructorExpressionNode serviceConstructorExpressionNode) {
        return createService(serviceConstructorExpressionNode, null, true);
    }

    @Override
    public BLangNode transform(MappingBindingPatternNode mappingBindingPatternNode) {
        BLangRecordVarRef recordVarRef = (BLangRecordVarRef) TreeBuilder.createRecordVariableReferenceNode();
        recordVarRef.pos = getPosition(mappingBindingPatternNode);

        List<BLangRecordVarRefKeyValue> expressions = new ArrayList<>();
        for (FieldBindingPatternNode expr : mappingBindingPatternNode.fieldBindingPatterns()) {
            expressions.add(createRecordVarKeyValue(expr));
        }
        recordVarRef.recordRefFields = expressions;

        Optional<RestBindingPatternNode> restBindingPattern = mappingBindingPatternNode.restBindingPattern();
        if (restBindingPattern.isPresent()) {
            recordVarRef.restParam = createExpression(restBindingPattern.get());
        }

        return recordVarRef;
    }

    private BLangRecordVarRefKeyValue createRecordVarKeyValue(FieldBindingPatternNode expr) {
        BLangRecordVarRefKeyValue keyValue = new BLangRecordVarRefKeyValue();
        if (expr instanceof FieldBindingPatternFullNode) {
            FieldBindingPatternFullNode fullNode = (FieldBindingPatternFullNode) expr;
            keyValue.variableName = createIdentifier(fullNode.variableName().name());
            keyValue.variableReference = createExpression(fullNode.bindingPattern());
        } else {
            FieldBindingPatternVarnameNode varnameNode = (FieldBindingPatternVarnameNode) expr;
            keyValue.variableName = createIdentifier(varnameNode.variableName().name());
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            varRef.pos = getPosition(varnameNode.variableName());
            varRef.variableName = createIdentifier(varnameNode.variableName().name());
            varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            keyValue.variableReference = varRef;
        }

        return keyValue;
    }

    @Override
    public BLangNode transform(ListBindingPatternNode listBindingPatternNode) {
        BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) TreeBuilder.createTupleVariableReferenceNode();
        List<BLangExpression> expressions = new ArrayList<>();
        for (BindingPatternNode expr : listBindingPatternNode.bindingPatterns()) {
            expressions.add(createExpression(expr));
        }
        tupleVarRef.expressions = expressions;
        tupleVarRef.pos = getPosition(listBindingPatternNode);
        Optional<RestBindingPatternNode> restBindingPattern = listBindingPatternNode.restBindingPattern();
        if (restBindingPattern.isPresent()) {
            tupleVarRef.restParam = createExpression(restBindingPattern.get());
        }

        return tupleVarRef;
    }

    @Override
    public BLangNode transform(RestBindingPatternNode restBindingPatternNode) {
        return createExpression(restBindingPatternNode.variableName());
    }

    @Override
    public BLangNode transform(CaptureBindingPatternNode captureBindingPatternNode) {
        return createExpression(captureBindingPatternNode.variableName());
    }

    @Override
    public BLangNode transform(WildcardBindingPatternNode wildcardBindingPatternNode) {
        BLangSimpleVarRef ignoreVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        BLangIdentifier ignore = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        ignore.value = Names.IGNORE.value;
        ignoreVarRef.variableName = ignore;
        ignore.pos = getPosition(wildcardBindingPatternNode);
        return ignoreVarRef;
    }

    @Override
    public BLangNode transform(ErrorBindingPatternNode errorBindingPatternNode) {
        BLangErrorVarRef errorVarRef = (BLangErrorVarRef) TreeBuilder.createErrorVariableReferenceNode();
        errorVarRef.pos = getPosition(errorBindingPatternNode);

        Optional<Node> errorTypeRef = errorBindingPatternNode.typeReference();
        if (errorTypeRef.isPresent()) {
            errorVarRef.typeNode = createTypeNode(errorTypeRef.get());
        }

        SeparatedNodeList<BindingPatternNode> argListBindingPatterns = errorBindingPatternNode.argListBindingPatterns();
        int numberOfArgs = argListBindingPatterns.size();
        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        for (int position = 0; position < numberOfArgs; position++) {
            BindingPatternNode bindingPatternNode = argListBindingPatterns.get(position);
            switch (bindingPatternNode.kind()) {
                case CAPTURE_BINDING_PATTERN:
                case WILDCARD_BINDING_PATTERN:
                    if (position == 0) {
                        errorVarRef.message = (BLangVariableReference) createExpression(bindingPatternNode);
                        break;
                    }
                    // Fall through.
                case ERROR_BINDING_PATTERN:
                    errorVarRef.cause = (BLangVariableReference) createExpression(bindingPatternNode);
                    break;
                case NAMED_ARG_BINDING_PATTERN:
                    namedArgs.add((BLangNamedArgsExpression) bindingPatternNode.apply(this));
                    break;
                default:// Rest binding pattern
                    errorVarRef.restVar = (BLangVariableReference) createExpression(bindingPatternNode);
            }
        }
        errorVarRef.detail = namedArgs;
        return errorVarRef;
    }

    @Override
    public BLangNode transform(NamedArgBindingPatternNode namedArgBindingPatternNode) {
        BLangNamedArgsExpression namedArgsExpression = (BLangNamedArgsExpression) TreeBuilder.createNamedArgNode();
        namedArgsExpression.pos = getPosition(namedArgBindingPatternNode);
        namedArgsExpression.name = createIdentifier(namedArgBindingPatternNode.argName());
        namedArgsExpression.expr = createExpression(namedArgBindingPatternNode.bindingPattern());
        return namedArgsExpression;
    }

    // -----------------------------------------------Statements--------------------------------------------------------
    @Override
    public BLangNode transform(ReturnStatementNode returnStmtNode) {
        BLangReturn bLReturn = (BLangReturn) TreeBuilder.createReturnNode();
        bLReturn.pos = getPosition(returnStmtNode);
        if (returnStmtNode.expression().isPresent()) {
            bLReturn.expr = createExpression(returnStmtNode.expression().get());
        } else {
            BLangLiteral nilLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            nilLiteral.pos = getPosition(returnStmtNode);
            nilLiteral.value = Names.NIL_VALUE;
            nilLiteral.type = symTable.nilType;
            bLReturn.expr = nilLiteral;
        }
        return bLReturn;
    }

    @Override
    public BLangNode transform(PanicStatementNode panicStmtNode) {
        BLangPanic bLPanic = (BLangPanic) TreeBuilder.createPanicNode();
        bLPanic.pos = getPosition(panicStmtNode);
        bLPanic.expr = createExpression(panicStmtNode.expression());
        return bLPanic;
    }

    @Override
    public BLangNode transform(ContinueStatementNode continueStmtNode) {
        BLangContinue bLContinue = (BLangContinue) TreeBuilder.createContinueNode();
        bLContinue.pos = getPosition(continueStmtNode);
        return bLContinue;
    }

    @Override
    public BLangNode transform(ListenerDeclarationNode listenerDeclarationNode) {
        Token visibilityQualifier = null;
        if (listenerDeclarationNode.visibilityQualifier().isPresent()) {
            visibilityQualifier = listenerDeclarationNode.visibilityQualifier().get();
        }

        BLangSimpleVariable var = new SimpleVarBuilder()
                .with(listenerDeclarationNode.variableName())
                .setTypeByNode(listenerDeclarationNode.typeDescriptor())
                .setExpressionByNode(listenerDeclarationNode.initializer())
                .setVisibility(visibilityQualifier)
                .isListenerVar()
                .build();
        var.pos = getPositionWithoutMetadata(listenerDeclarationNode);
        var.name.pos = getPosition(listenerDeclarationNode.variableName());
        var.annAttachments = applyAll(getAnnotations(listenerDeclarationNode.metadata()));
        return var;
    }

    @Override
    public BLangNode transform(BreakStatementNode breakStmtNode) {
        BLangBreak bLBreak = (BLangBreak) TreeBuilder.createBreakNode();
        bLBreak.pos = getPosition(breakStmtNode);
        return bLBreak;
    }

    @Override
    public BLangNode transform(AssignmentStatementNode assignmentStmtNode) {
        SyntaxKind lhsKind = assignmentStmtNode.varRef().kind();
        switch (lhsKind) {
            case LIST_BINDING_PATTERN:
                return createTupleDestructureStatement(assignmentStmtNode);
            case MAPPING_BINDING_PATTERN: // ignored for now
                return createRecordDestructureStatement(assignmentStmtNode);
            case ERROR_BINDING_PATTERN:
                return createErrorDestructureStatement(assignmentStmtNode);
            default:
                break;
        }

        BLangAssignment bLAssignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        BLangExpression lhsExpr = createExpression(assignmentStmtNode.varRef());
        validateLvexpr(lhsExpr, DiagnosticCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT);

        bLAssignment.setExpression(createExpression(assignmentStmtNode.expression()));
        bLAssignment.pos = getPosition(assignmentStmtNode);
        bLAssignment.varRef = lhsExpr;
        return bLAssignment;
    }

    public BLangNode createTupleDestructureStatement(AssignmentStatementNode assignmentStmtNode) {
        BLangTupleDestructure tupleDestructure =
                (BLangTupleDestructure) TreeBuilder.createTupleDestructureStatementNode();
        tupleDestructure.varRef = (BLangTupleVarRef) createExpression(assignmentStmtNode.varRef());
        tupleDestructure.setExpression(createExpression(assignmentStmtNode.expression()));
        tupleDestructure.pos = getPosition(assignmentStmtNode);
        return tupleDestructure;
    }

    public BLangNode createRecordDestructureStatement(AssignmentStatementNode assignmentStmtNode) {
        BLangRecordDestructure recordDestructure =
                (BLangRecordDestructure) TreeBuilder.createRecordDestructureStatementNode();
        recordDestructure.varRef = (BLangRecordVarRef) createExpression(assignmentStmtNode.varRef());
        recordDestructure.setExpression(createExpression(assignmentStmtNode.expression()));
        return recordDestructure;
    }

    public BLangNode createErrorDestructureStatement(AssignmentStatementNode assignmentStmtNode) {
        BLangErrorDestructure errorDestructure =
                (BLangErrorDestructure) TreeBuilder.createErrorDestructureStatementNode();
        errorDestructure.varRef = (BLangErrorVarRef) createExpression(assignmentStmtNode.varRef());
        errorDestructure.setExpression(createExpression(assignmentStmtNode.expression()));
        errorDestructure.pos = getPosition(assignmentStmtNode);
        return errorDestructure;
    }

    @Override
    public BLangNode transform(CompoundAssignmentStatementNode compoundAssignmentStmtNode) {
        BLangCompoundAssignment bLCompAssignment = (BLangCompoundAssignment) TreeBuilder.createCompoundAssignmentNode();
        bLCompAssignment.setExpression(createExpression(compoundAssignmentStmtNode.rhsExpression()));

        bLCompAssignment
                .setVariable((BLangVariableReference) createExpression(compoundAssignmentStmtNode.lhsExpression()));
        bLCompAssignment.pos = getPosition(compoundAssignmentStmtNode);
        bLCompAssignment.opKind = OperatorKind.valueFrom(compoundAssignmentStmtNode.binaryOperator().text());
        return bLCompAssignment;
    }

    private void validateLvexpr(ExpressionNode lExprNode, DiagnosticCode errorCode) {
        if (lExprNode.getKind() == NodeKind.INVOCATION) {
            dlog.error(((BLangInvocation) lExprNode).pos, errorCode);
        }
        if (lExprNode.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || lExprNode.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            validateLvexpr(((BLangAccessExpression) lExprNode).expr, errorCode);
        }
    }

    @Override
    public BLangNode transform(DoStatementNode doStatementNode) {
        BLangDo bLDo = (BLangDo) TreeBuilder.createDoNode();
        bLDo.pos = getPosition(doStatementNode);

        BLangBlockStmt bLBlockStmt = (BLangBlockStmt) doStatementNode.blockStatement().apply(this);
        bLBlockStmt.pos = getPosition(doStatementNode.blockStatement());
        bLDo.setBody(bLBlockStmt);
        doStatementNode.onFailClause().ifPresent(onFailClauseNode -> {
            bLDo.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });
        return bLDo;
    }

    @Override
    public BLangNode transform(FailStatementNode failStatementNode) {
        BLangFail bLFail = (BLangFail) TreeBuilder.createFailNode();
        bLFail.pos = getPosition(failStatementNode);
        bLFail.expr = createExpression(failStatementNode.expression());
        return bLFail;
    }

    @Override
    public BLangNode transform(WhileStatementNode whileStmtNode) {
        BLangWhile bLWhile = (BLangWhile) TreeBuilder.createWhileNode();
        bLWhile.setCondition(createExpression(whileStmtNode.condition()));
        bLWhile.pos = getPosition(whileStmtNode);

        BLangBlockStmt bLBlockStmt = (BLangBlockStmt) whileStmtNode.whileBody().apply(this);
        bLBlockStmt.pos = getPosition(whileStmtNode.whileBody());
        bLWhile.setBody(bLBlockStmt);
        whileStmtNode.onFailClause().ifPresent(onFailClauseNode -> {
            bLWhile.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });
        return bLWhile;
    }

    @Override
    public BLangNode transform(IfElseStatementNode ifElseStmtNode) {
        BLangIf bLIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
        bLIf.pos = getPosition(ifElseStmtNode);
        bLIf.setCondition(createExpression(ifElseStmtNode.condition()));
        bLIf.setBody((BLangBlockStmt) ifElseStmtNode.ifBody().apply(this));

        ifElseStmtNode.elseBody().ifPresent(elseBody -> {
            ElseBlockNode elseNode = (ElseBlockNode) elseBody;
            bLIf.setElseStatement(
                    (org.ballerinalang.model.tree.statements.StatementNode) elseNode.elseBody().apply(this));
        });
        return bLIf;
    }

    @Override
    public BLangNode transform(BlockStatementNode blockStatement) {
        BLangBlockStmt bLBlockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
        this.isInLocalContext = true;
        bLBlockStmt.stmts = generateBLangStatements(blockStatement.statements());
        this.isInLocalContext = false;
        bLBlockStmt.pos = getPosition(blockStatement);
        SyntaxKind parent = blockStatement.parent().kind();
        if (parent == SyntaxKind.IF_ELSE_STATEMENT || parent == SyntaxKind.ELSE_BLOCK) {
            expandLeft(bLBlockStmt.pos, getPosition(blockStatement.parent()));
        }
        return bLBlockStmt;
    }

    @Override
    public BLangNode transform(RollbackStatementNode rollbackStatementNode) {
        BLangRollback rollbackStmt = (BLangRollback) TreeBuilder.createRollbackNode();
        rollbackStmt.pos = getPosition(rollbackStatementNode);
        if (rollbackStatementNode.expression().isPresent()) {
            rollbackStmt.expr = createExpression(rollbackStatementNode.expression().get());
        }

        return rollbackStmt;
    }

    @Override
    public BLangNode transform(LockStatementNode lockStatementNode) {
        BLangLock lockNode = (BLangLock) TreeBuilder.createLockNode();
        lockNode.pos = getPosition(lockStatementNode);
        BLangBlockStmt lockBlock = (BLangBlockStmt) lockStatementNode.blockStatement().apply(this);
        lockBlock.pos = getPosition(lockStatementNode.blockStatement());
        lockNode.setBody(lockBlock);

        lockStatementNode.onFailClause().ifPresent(onFailClauseNode -> {
            lockNode.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });

        return lockNode;
    }

    @Override
    public BLangNode transform(TypedescTypeDescriptorNode typedescTypeDescriptorNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TypeKind.TYPEDESC;

        Optional<TypeParameterNode> node = typedescTypeDescriptorNode.typedescTypeParamsNode();
        if (node.isPresent()) {
            BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
            constrainedType.type = refType;
            constrainedType.constraint = createTypeNode(node.get().typeNode());
            return constrainedType;
        }

        return refType;
    }

    @Override
    public BLangNode transform(VariableDeclarationNode varDeclaration) {
        return (BLangNode) createBLangVarDef(getPosition(varDeclaration), varDeclaration.typedBindingPattern(),
                varDeclaration.initializer(), varDeclaration.finalKeyword());
    }

    public BLangNode transform(XmlTypeDescriptorNode xmlTypeDescriptorNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TypeKind.XML;
        refType.pos = getPosition(xmlTypeDescriptorNode);

        Optional<TypeParameterNode> node = xmlTypeDescriptorNode.xmlTypeParamsNode();
        if (node.isPresent()) {
            BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
            constrainedType.type = refType;
            constrainedType.constraint = createTypeNode(node.get().typeNode());
            constrainedType.pos = getPosition(xmlTypeDescriptorNode);
            return constrainedType;
        }

        return refType;
    }

    private VariableDefinitionNode createBLangVarDef(DiagnosticPos pos, TypedBindingPatternNode typedBindingPattern,
            Optional<io.ballerina.compiler.syntax.tree.ExpressionNode> initializer, Optional<Token> finalKeyword) {
        BindingPatternNode bindingPattern = typedBindingPattern.bindingPattern();
        BLangVariable variable = getBLangVariableNode(bindingPattern);
        switch (bindingPattern.kind()) {
            case CAPTURE_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                BLangSimpleVariableDef bLVarDef =
                        (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
                bLVarDef.pos = variable.pos = pos;
                BLangExpression expr = initializer.isPresent() ? createExpression(initializer.get()) : null;
                variable.setInitialExpression(expr);
                bLVarDef.setVariable(variable);
                if (finalKeyword.isPresent()) {
                    variable.flagSet.add(Flag.FINAL);
                }

                TypeDescriptorNode typeDesc = typedBindingPattern.typeDescriptor();
                variable.isDeclaredWithVar = isDeclaredWithVar(typeDesc);
                if (!variable.isDeclaredWithVar) {
                    variable.setTypeNode(createTypeNode(typeDesc));
                }

                return bLVarDef;
            case MAPPING_BINDING_PATTERN:
                return createRecordVariableDef(variable, typedBindingPattern.typeDescriptor(), initializer,
                        finalKeyword.isPresent());
            case LIST_BINDING_PATTERN:
                return createTupleVariableDef(variable, typedBindingPattern.typeDescriptor(), initializer,
                        finalKeyword.isPresent());
            case ERROR_BINDING_PATTERN:
                return createErrorVariableDef(variable, typedBindingPattern.typeDescriptor(), initializer,
                        finalKeyword.isPresent());
            default:
                throw new RuntimeException(
                        "Syntax kind is not a valid binding pattern " + typedBindingPattern.bindingPattern().kind());
        }
    }

    private VariableDefinitionNode createRecordVariableDef(BLangVariable var, TypeDescriptorNode type,
            Optional<io.ballerina.compiler.syntax.tree.ExpressionNode> initializer, boolean isFinal) {

        if (isFinal) {
            markVariableAsFinal(var);
        }

        var.isDeclaredWithVar = isDeclaredWithVar(type);
        if (!var.isDeclaredWithVar) {
            var.setTypeNode(createTypeNode(type));
        }

        if (initializer.isPresent()) {
            var.setInitialExpression(createExpression(initializer.get()));
        }

        BLangRecordVariableDef varDefNode = (BLangRecordVariableDef) TreeBuilder.createRecordVariableDefinitionNode();
        varDefNode.pos = getPosition(null);
        varDefNode.setVariable(var);
        return varDefNode;
    }

    private BLangTupleVariableDef createTupleVariableDef(BLangVariable tupleVar, TypeDescriptorNode typeDesc,
            Optional<io.ballerina.compiler.syntax.tree.ExpressionNode> initializer, boolean isFinal) {
        if (isFinal) {
            markVariableAsFinal(tupleVar);
        }

        tupleVar.isDeclaredWithVar = isDeclaredWithVar(typeDesc);
        if (!tupleVar.isDeclaredWithVar) {
            tupleVar.setTypeNode(createTypeNode(typeDesc));
        }

        if (initializer.isPresent()) {
            tupleVar.setInitialExpression(createExpression(initializer.get()));
        }

        BLangTupleVariableDef varDefNode = (BLangTupleVariableDef) TreeBuilder.createTupleVariableDefinitionNode();
        varDefNode.pos = getPosition(null);
        varDefNode.setVariable(tupleVar);
        return varDefNode;
    }

    private BLangErrorVariableDef createErrorVariableDef(BLangVariable tupleVar, TypeDescriptorNode typeDesc,
              Optional<io.ballerina.compiler.syntax.tree.ExpressionNode> initializer, boolean isFinal) {
        if (isFinal) {
            markVariableAsFinal(tupleVar);
        }

        tupleVar.isDeclaredWithVar = isDeclaredWithVar(typeDesc);
        if (!tupleVar.isDeclaredWithVar) {
            tupleVar.setTypeNode(createTypeNode(typeDesc));
        }

        if (initializer.isPresent()) {
            tupleVar.setInitialExpression(createExpression(initializer.get()));
        }

        BLangErrorVariableDef varDefNode = (BLangErrorVariableDef) TreeBuilder.createErrorVariableDefinitionNode();
        varDefNode.pos = getPosition(null);
        varDefNode.setVariable(tupleVar);
        return varDefNode;
    }

    @Override
    public BLangNode transform(ExpressionStatementNode expressionStatement) {
        SyntaxKind kind = expressionStatement.expression().kind();
        switch (kind) {
            case ASYNC_SEND_ACTION:
                return expressionStatement.expression().apply(this);
            default:
                BLangExpressionStmt bLExpressionStmt =
                        (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
                bLExpressionStmt.expr = createExpression(expressionStatement.expression());
                bLExpressionStmt.pos = getPosition(expressionStatement);
                return bLExpressionStmt;
        }
    }

    @Override
    public BLangNode transform(AsyncSendActionNode asyncSendActionNode) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.setWorkerName(createIdentifier(getPosition(asyncSendActionNode.peerWorker()),
                asyncSendActionNode.peerWorker().name()));
        workerSendNode.expr = createExpression(asyncSendActionNode.expression());
        workerSendNode.pos = getPosition(asyncSendActionNode);
        return workerSendNode;
    }

    @Override
    public BLangNode transform(WaitActionNode waitActionNode) {
        Node waitFutureExpr = waitActionNode.waitFutureExpr();
        if (waitFutureExpr.kind() == SyntaxKind.WAIT_FIELDS_LIST) {
            return getWaitForAllExpr((WaitFieldsListNode) waitFutureExpr);
        }

        BLangWaitExpr waitExpr = TreeBuilder.createWaitExpressionNode();
        waitExpr.pos = getPosition(waitActionNode);
        waitExpr.exprList = Collections.singletonList(createExpression(waitFutureExpr));
        return waitExpr;
    }

    private BLangWaitForAllExpr getWaitForAllExpr(WaitFieldsListNode waitFields) {
        BLangWaitForAllExpr bLangWaitForAll = TreeBuilder.createWaitForAllExpressionNode();

        List<BLangWaitKeyValue> exprs = new ArrayList<>();
        for (Node waitField : waitFields.waitFields()) {
            exprs.add(getWaitForAllExpr(waitField));
        }

        bLangWaitForAll.keyValuePairs = exprs;
        bLangWaitForAll.pos = getPosition(waitFields);
        return bLangWaitForAll;
    }

    private BLangWaitKeyValue getWaitForAllExpr(Node waitFields) {
        BLangWaitForAllExpr.BLangWaitKeyValue keyValue = TreeBuilder.createWaitKeyValueNode();
        keyValue.pos = getPosition(waitFields);

        if (waitFields.kind() == SyntaxKind.WAIT_FIELD) {
            WaitFieldNode waitFieldNode = (WaitFieldNode) waitFields;
            BLangIdentifier key = createIdentifier(waitFieldNode.fieldName().name());
            key.setLiteral(false);
            keyValue.key = key;
            keyValue.valueExpr = createExpression(waitFieldNode.waitFutureExpr());
            return keyValue;
        }

        SimpleNameReferenceNode varName = (SimpleNameReferenceNode) waitFields;
        BLangIdentifier key = createIdentifier(varName.name());
        key.setLiteral(false);
        keyValue.key = key;

        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = getPosition(varName);
        varRef.variableName = key;
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        keyValue.keyExpr = varRef;
        return keyValue;
    }

    @Override
    public BLangNode transform(StartActionNode startActionNode) {
        BLangNode expression = createActionOrExpression(startActionNode.expression());

        BLangInvocation invocation;
        if (!(expression instanceof BLangWorkerSend)) {
            invocation = (BLangInvocation) expression;
        } else {
            invocation = (BLangInvocation) ((BLangWorkerSend) expression).expr;
            expression = ((BLangWorkerSend) expression).expr;
        }

        if (expression.getKind() == NodeKind.INVOCATION) {
            BLangActionInvocation actionInvocation = (BLangActionInvocation) TreeBuilder.createActionInvocation();
            actionInvocation.expr = invocation.expr;
            actionInvocation.pkgAlias = invocation.pkgAlias;
            actionInvocation.name = invocation.name;
            actionInvocation.argExprs = invocation.argExprs;
            actionInvocation.flagSet = invocation.flagSet;
            actionInvocation.pos = invocation.pos;
            invocation = actionInvocation;
        }

        invocation.async = true;
        invocation.annAttachments = applyAll(startActionNode.annotations());
        return invocation;
    }

    @Override
    public BLangNode transform(TransactionStatementNode transactionStatementNode) {
        BLangTransaction transaction = (BLangTransaction) TreeBuilder.createTransactionNode();
        BLangBlockStmt transactionBlock = (BLangBlockStmt) transactionStatementNode.blockStatement().apply(this);
        transactionBlock.pos = getPosition(transactionStatementNode.blockStatement());
        transaction.setTransactionBody(transactionBlock);
        transaction.pos = getPosition(transactionStatementNode);

        transactionStatementNode.onFailClause().ifPresent(onFailClauseNode -> {
            transaction.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });

        return transaction;
    }

    // -------------------------------------------------Misc------------------------------------------------------------

    @Override
    public BLangNode transform(PositionalArgumentNode argumentNode) {
        return createExpression(argumentNode.expression());
    }

    @Override
    public BLangNode transform(NamedArgumentNode namedArgumentNode) {
        BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) TreeBuilder.createNamedArgNode();
        namedArg.pos = getPosition(namedArgumentNode);
        namedArg.name = this.createIdentifier(namedArgumentNode.argumentName().name());
        namedArg.expr = createExpression(namedArgumentNode.expression());
        return namedArg;
    }

    @Override
    public BLangNode transform(RestArgumentNode restArgumentNode) {
        BLangRestArgsExpression varArgs = (BLangRestArgsExpression) TreeBuilder.createVarArgsNode();
        varArgs.pos = getPosition(restArgumentNode.ellipsis());
        varArgs.expr = createExpression(restArgumentNode.expression());
        return varArgs;
    }

    @Override
    public BLangNode transform(RequiredParameterNode requiredParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(requiredParameter.paramName(),
                                                        requiredParameter.typeName(), requiredParameter.annotations());

        simpleVar.pos = getPosition(requiredParameter);
        if (requiredParameter.paramName().isPresent()) {
            simpleVar.name.pos = getPosition(requiredParameter.paramName().get());
        }
        trimLeft(simpleVar.pos, getPosition(requiredParameter.typeName()));
        return simpleVar;
    }

    @Override
    public BLangNode transform(DefaultableParameterNode defaultableParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(defaultableParameter.paramName(),
                                                        defaultableParameter.typeName(),
                                                        defaultableParameter.annotations());

        simpleVar.setInitialExpression(createExpression(defaultableParameter.expression()));

        simpleVar.pos = getPosition(defaultableParameter);
        return simpleVar;
    }

    @Override
    public BLangNode transform(RestParameterNode restParameter) {
        BLangSimpleVariable bLSimpleVar = createSimpleVar(restParameter.paramName(), restParameter.typeName(),
                                                          restParameter.annotations());

        BLangArrayType bLArrayType = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        bLArrayType.elemtype = bLSimpleVar.typeNode;
        bLArrayType.dimensions = 1;
        bLSimpleVar.typeNode = bLArrayType;
        bLArrayType.pos = getPosition(restParameter.typeName());

        bLSimpleVar.pos = getPosition(restParameter);
        return bLSimpleVar;
    }

    @Override
    public BLangNode transform(OptionalTypeDescriptorNode optTypeDescriptor) {
        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.pos = getPosition(optTypeDescriptor.questionMarkToken());
        nilTypeNode.typeKind = TypeKind.NIL;

        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.memberTypeNodes.add(createTypeNode(optTypeDescriptor.typeDescriptor()));
        unionTypeNode.memberTypeNodes.add(nilTypeNode);
        unionTypeNode.nullable = true;

        unionTypeNode.pos = getPosition(optTypeDescriptor);
        return unionTypeNode;
    }

    @Override
    public BLangNode transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = getPosition(functionTypeDescriptorNode);
        functionTypeNode.returnsKeywordExists = true;

        FunctionSignatureNode funcSignature = functionTypeDescriptorNode.functionSignature();

        // Set Parameters
        for (ParameterNode child : funcSignature.parameters()) {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameterNode) {
                functionTypeNode.restParam = (BLangSimpleVariable) param;
            } else {
                functionTypeNode.params.add((BLangVariable) param);
            }
        }

        // Set Return Type
        Optional<ReturnTypeDescriptorNode> retNode = funcSignature.returnTypeDesc();
        if (retNode.isPresent()) {
            ReturnTypeDescriptorNode returnType = retNode.get();
            functionTypeNode.returnTypeNode = createTypeNode(returnType.type());
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = getPosition(funcSignature);
            bLValueType.typeKind = TypeKind.NIL;
            functionTypeNode.returnTypeNode = bLValueType;
        }

        functionTypeNode.flagSet.add(Flag.PUBLIC);

        for (Token token : functionTypeDescriptorNode.qualifierList()) {
            if (token.kind() == SyntaxKind.ISOLATED_KEYWORD) {
                functionTypeNode.flagSet.add(Flag.ISOLATED);
                break;
            }
        }

        return functionTypeNode;
    }

    @Override
    public BLangNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        BLangBuiltInRefTypeNode typeNode =
                (BLangBuiltInRefTypeNode) createBuiltInTypeNode(parameterizedTypeDescNode.parameterizedType());
        refType.typeKind = typeNode.typeKind;
        refType.pos = typeNode.pos;

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = createTypeNode(parameterizedTypeDescNode.typeParameter().typeNode());
        constrainedType.pos = getPosition(parameterizedTypeDescNode);
        return constrainedType;
    }

    @Override
    public BLangNode transform(KeySpecifierNode keySpecifierNode) {
        BLangTableKeySpecifier tableKeySpecifierNode =
                (BLangTableKeySpecifier) TreeBuilder.createTableKeySpecifierNode();
        tableKeySpecifierNode.pos = getPosition(keySpecifierNode);

        for (Token field : keySpecifierNode.fieldNames()) {
            tableKeySpecifierNode.addFieldNameIdentifier(createIdentifier(field));
        }
        return tableKeySpecifierNode;
    }

    @Override
    public BLangNode transform(KeyTypeConstraintNode keyTypeConstraintNode) {
        BLangTableKeyTypeConstraint tableKeyTypeConstraint = new BLangTableKeyTypeConstraint();
        tableKeyTypeConstraint.pos = getPosition(keyTypeConstraintNode);
        tableKeyTypeConstraint.keyType = createTypeNode(keyTypeConstraintNode.typeParameterNode());
        return tableKeyTypeConstraint;
    }

    @Override
    public BLangNode transform(TableTypeDescriptorNode tableTypeDescriptorNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(tableTypeDescriptorNode.tableKeywordToken().text());
        refType.pos = getPosition(tableTypeDescriptorNode);

        BLangTableTypeNode tableTypeNode = (BLangTableTypeNode) TreeBuilder.createTableTypeNode();
        tableTypeNode.pos = getPosition(tableTypeDescriptorNode);
        tableTypeNode.type = refType;
        tableTypeNode.constraint = createTypeNode(tableTypeDescriptorNode.rowTypeParameterNode());
        if (tableTypeDescriptorNode.keyConstraintNode().isPresent()) {
            Node constraintNode = tableTypeDescriptorNode.keyConstraintNode().get();
            if (constraintNode.kind() == SyntaxKind.KEY_TYPE_CONSTRAINT) {
                tableTypeNode.tableKeyTypeConstraint = (BLangTableKeyTypeConstraint) constraintNode.apply(this);
            } else if (constraintNode.kind() == SyntaxKind.KEY_SPECIFIER) {
                tableTypeNode.tableKeySpecifier = (BLangTableKeySpecifier) constraintNode.apply(this);
            }
        }
        return tableTypeNode;
    }

    @Override
    public BLangNode transform(SimpleNameReferenceNode simpleNameRefNode) {
        BLangUserDefinedType bLUserDefinedType = new BLangUserDefinedType();

        bLUserDefinedType.pos = getPosition(simpleNameRefNode);
        bLUserDefinedType.typeName =
                createIdentifier(simpleNameRefNode.name());
        bLUserDefinedType.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        return bLUserDefinedType;
    }

    @Override
    public BLangNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = getPosition(qualifiedNameReferenceNode);
        varRef.variableName = createIdentifier(qualifiedNameReferenceNode.identifier());
        varRef.pkgAlias = createIdentifier(qualifiedNameReferenceNode.modulePrefix());
        return varRef;
    }

    @Override
    public BLangNode transform(XMLProcessingInstruction xmlProcessingInstruction) {
        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        if (xmlProcessingInstruction.data().isEmpty()) {
            BLangLiteral emptyLiteral = createEmptyLiteral();
            emptyLiteral.pos = getPosition(xmlProcessingInstruction);
            xmlProcInsLiteral.dataFragments.add(emptyLiteral);
        } else {
            for (Node dataNode : xmlProcessingInstruction.data()) {
                xmlProcInsLiteral.dataFragments.add(createExpression(dataNode));
            }
        }

        XMLNameNode target = xmlProcessingInstruction.target();
        if (target.kind() == SyntaxKind.XML_SIMPLE_NAME) {
            xmlProcInsLiteral.target = createSimpleLiteral(((XMLSimpleNameNode) target).name());
        } else {
            // this could be a bug in the old parser
            xmlProcInsLiteral.target = createSimpleLiteral(((XMLQualifiedNameNode) target).prefix());
        }

        xmlProcInsLiteral.pos = getPosition(xmlProcessingInstruction);
        return xmlProcInsLiteral;
    }

    @Override
    public BLangNode transform(XMLComment xmlComment) {
        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        DiagnosticPos pos = getPosition(xmlComment);

        if (xmlComment.content().isEmpty()) {
            BLangLiteral emptyLiteral = createEmptyLiteral();
            emptyLiteral.pos = pos;
            xmlCommentLiteral.textFragments.add(emptyLiteral);
        } else {
            for (Node commentNode : xmlComment.content()) {
                xmlCommentLiteral.textFragments.add(createExpression(commentNode));
            }
        }
        xmlCommentLiteral.pos = pos;
        return xmlCommentLiteral;
    }

    @Override
    public BLangNode transform(XMLElementNode xmlElementNode) {
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        xmlElement.startTagName = createExpression(xmlElementNode.startTag());
        xmlElement.endTagName = createExpression(xmlElementNode.endTag());

        for (Node node : xmlElementNode.content()) {
            if (node.kind() == SyntaxKind.XML_TEXT) {
                xmlElement.children.add(createSimpleLiteral(((XMLTextNode) node).content()));
                continue;
            }
            xmlElement.children.add(createExpression(node));
        }

        for (XMLAttributeNode attribute : xmlElementNode.startTag().attributes()) {
            xmlElement.attributes.add((BLangXMLAttribute) attribute.apply(this));
        }

        xmlElement.pos = getPosition(xmlElementNode);
        xmlElement.isRoot = true; // TODO : check this
        return xmlElement;
    }

    @Override
    public BLangNode transform(XMLAttributeNode xmlAttributeNode) {
        BLangXMLAttribute xmlAttribute = (BLangXMLAttribute) TreeBuilder.createXMLAttributeNode();
        xmlAttribute.value = (BLangXMLQuotedString) xmlAttributeNode.value().apply(this);
        xmlAttribute.name = createExpression(xmlAttributeNode.attributeName());
        xmlAttribute.pos = getPosition(xmlAttributeNode);
        return xmlAttribute;
    }

    @Override
    public BLangNode transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.pos = getPosition(byteArrayLiteralNode);
        literal.type = symTable.getTypeFromTag(TypeTags.BYTE_ARRAY);
        literal.type.tag = TypeTags.BYTE_ARRAY;
        literal.value = getValueFromByteArrayNode(byteArrayLiteralNode);
        literal.originalValue = String.valueOf(literal.value);
        return literal;
    }

    @Override
    public BLangNode transform(XMLAttributeValue xmlAttributeValue) {
        BLangXMLQuotedString quotedString = (BLangXMLQuotedString) TreeBuilder.createXMLQuotedStringNode();
        quotedString.pos = getPosition(xmlAttributeValue);
        if (xmlAttributeValue.startQuote().kind() == SyntaxKind.SINGLE_QUOTE_TOKEN) {
            quotedString.quoteType = QuoteType.SINGLE_QUOTE;
        } else {
            quotedString.quoteType = QuoteType.DOUBLE_QUOTE;
        }

        if (xmlAttributeValue.value().isEmpty()) {
            BLangLiteral emptyLiteral = createEmptyLiteral();
            emptyLiteral.pos = getPosition(xmlAttributeValue);
            quotedString.textFragments.add(emptyLiteral);
        } else if (xmlAttributeValue.value().size() == 1 &&
                xmlAttributeValue.value().get(0).kind() == SyntaxKind.INTERPOLATION) {
            quotedString.textFragments.add(createExpression(xmlAttributeValue.value().get(0)));
            BLangLiteral emptyLiteral = createEmptyLiteral();
            emptyLiteral.pos = getPosition(xmlAttributeValue);
            quotedString.textFragments.add(emptyLiteral);
        } else {
            for (Node value : xmlAttributeValue.value()) {
                quotedString.textFragments.add(createExpression(value));
            }
        }

        return quotedString;
    }

    @Override
    public BLangNode transform(XMLStartTagNode startTagNode) {
        return startTagNode.name().apply(this);
    }

    @Override
    public BLangNode transform(XMLEndTagNode endTagNode) {
        return endTagNode.name().apply(this);
    }

    @Override
    public BLangNode transform(XMLTextNode xmlTextNode) {
        return createExpression(xmlTextNode.content());
    }

    private BLangNode createXMLLiteral(TemplateExpressionNode expressionNode) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.pos = getPosition(expressionNode.content().get(0));
        for (Node node : expressionNode.content()) {
            xmlTextLiteral.textFragments.add(createExpression(node));
        }
        return xmlTextLiteral;
    }

    @Override
    public BLangNode transform(XMLNamespaceDeclarationNode xmlnsDeclNode) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifier = createIdentifier(xmlnsDeclNode.namespacePrefix().orElse(null));

        BLangExpression namespaceUri = createExpression(xmlnsDeclNode.namespaceuri());
        xmlns.namespaceURI = namespaceUri;
        xmlns.prefix = prefixIdentifier;
        xmlns.pos = getPosition(xmlnsDeclNode);

        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = getPosition(xmlnsDeclNode);
        return xmlnsStmt;
    }

    @Override
    public BLangNode transform(ModuleXMLNamespaceDeclarationNode xmlnsDeclNode) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifier = createIdentifier(xmlnsDeclNode.namespacePrefix().orElse(null));
        BLangExpression namespaceUri = createExpression(xmlnsDeclNode.namespaceuri());
        xmlns.namespaceURI = namespaceUri;
        xmlns.prefix = prefixIdentifier;
        xmlns.pos = getPosition(xmlnsDeclNode);
        return xmlns;
    }

    @Override
    public BLangNode transform(XMLQualifiedNameNode xmlQualifiedNameNode) {
        BLangXMLQName xmlName = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        xmlName.localname = createIdentifier(getPosition(xmlQualifiedNameNode.name()),
                xmlQualifiedNameNode.name().name());
        xmlName.prefix = createIdentifier(getPosition(xmlQualifiedNameNode.prefix()),
                xmlQualifiedNameNode.prefix().name());
        xmlName.pos = getPosition(xmlQualifiedNameNode);
        return xmlName;
    }

    @Override
    public BLangNode transform(XMLSimpleNameNode xmlSimpleNameNode) {
        BLangXMLQName xmlName = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        xmlName.localname = createIdentifier(xmlSimpleNameNode.name());
        xmlName.prefix = createIdentifier(null, "");
        xmlName.pos = getPosition(xmlSimpleNameNode);
        return xmlName;
    }

    @Override
    public BLangNode transform(XMLEmptyElementNode xMLEmptyElementNode) {
        BLangXMLElementLiteral xmlEmptyElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        xmlEmptyElement.startTagName = createExpression(xMLEmptyElementNode.name());
        for (XMLAttributeNode attribute : xMLEmptyElementNode.attributes()) {
            xmlEmptyElement.attributes.add((BLangXMLAttribute) attribute.apply(this));
        }
        xmlEmptyElement.pos = getPosition(xMLEmptyElementNode);
        return xmlEmptyElement;
    }

    @Override
    public BLangNode transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        BLangInvocation.BLangActionInvocation bLangActionInvocation = (BLangInvocation.BLangActionInvocation)
                TreeBuilder.createActionInvocation();
        bLangActionInvocation.expr = createExpression(remoteMethodCallActionNode.expression());
        bLangActionInvocation.argExprs = applyAll(remoteMethodCallActionNode.arguments());

        BLangNameReference nameReference = createBLangNameReference(remoteMethodCallActionNode.methodName().name());
        bLangActionInvocation.name = (BLangIdentifier) nameReference.name;
        bLangActionInvocation.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        bLangActionInvocation.pos = getPosition(remoteMethodCallActionNode);
        return bLangActionInvocation;
    }

    @Override
    public BLangNode transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        BLangType constraint, error = null;
        DiagnosticPos pos = getPosition(streamTypeDescriptorNode);
        Optional<Node> paramsNode = streamTypeDescriptorNode.streamTypeParamsNode();

        boolean hasConstraint = paramsNode.isPresent();
        if (!hasConstraint) {
            constraint = addValueType(pos, TypeKind.ANY);
        } else {
            StreamTypeParamsNode params = (StreamTypeParamsNode) paramsNode.get();
            if (params.rightTypeDescNode().isPresent()) {
                error = createTypeNode(params.rightTypeDescNode().get());
            }
            constraint = createTypeNode(params.leftTypeDescNode());
        }

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TypeKind.STREAM;
        refType.pos = pos;

        BLangStreamType streamType = (BLangStreamType) TreeBuilder.createStreamTypeNode();
        streamType.type = refType;
        streamType.constraint = constraint;
        streamType.error = error;
        streamType.pos = pos;

        return streamType;
    }

    @Override
    public BLangNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        int dimensions = 1;
        List<BLangExpression> sizes = new ArrayList<>();
        DiagnosticPos position = getPosition(arrayTypeDescriptorNode);
        while (true) {
            if (!arrayTypeDescriptorNode.arrayLength().isPresent()) {
                sizes.add(new BLangLiteral(Integer.valueOf(OPEN_ARRAY_INDICATOR), symTable.intType));
            } else {
                Node keyExpr = arrayTypeDescriptorNode.arrayLength().get();
                if (keyExpr.kind() == SyntaxKind.NUMERIC_LITERAL) {
                    BasicLiteralNode numericLiteralNode = (BasicLiteralNode) keyExpr;
                    if (numericLiteralNode.literalToken().kind() == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
                        sizes.add(new BLangLiteral(Integer.parseInt(keyExpr.toString()), symTable.intType));
                    } else {
                        sizes.add(new BLangLiteral(Integer.parseInt(keyExpr.toString(), 16), symTable.intType));
                    }
                } else if (keyExpr.kind() == SyntaxKind.ASTERISK_LITERAL) {
                    sizes.add(new BLangLiteral(Integer.valueOf(INFERRED_ARRAY_INDICATOR), symTable.intType));
                } else {
                    sizes.add(createExpression(keyExpr));
                }
            }

            if (arrayTypeDescriptorNode.memberTypeDesc().kind() != SyntaxKind.ARRAY_TYPE_DESC) {
                break;
            }

            arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) arrayTypeDescriptorNode.memberTypeDesc();
            dimensions++;
        }

        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.pos = position;
        arrayTypeNode.elemtype = createTypeNode(arrayTypeDescriptorNode.memberTypeDesc());
        arrayTypeNode.dimensions = dimensions;
        arrayTypeNode.sizes = sizes.toArray(new BLangExpression[0]);
        return arrayTypeNode;
    }

    public BLangNode transform(EnumDeclarationNode enumDeclarationNode) {
        Boolean publicQualifier = false;
        if (enumDeclarationNode.qualifier().isPresent() && enumDeclarationNode.qualifier().get().kind()
                == SyntaxKind.PUBLIC_KEYWORD) {
            publicQualifier = true;
        }
        for (Node member : enumDeclarationNode.enumMemberList()) {
            addToTop(transformEnumMember((EnumMemberNode) member, publicQualifier));
        }

        BLangTypeDefinition bLangTypeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        if (publicQualifier) {
            bLangTypeDefinition.flagSet.add(Flag.PUBLIC);
        }

        bLangTypeDefinition.setName((BLangIdentifier) transform(enumDeclarationNode.identifier()));
        bLangTypeDefinition.pos = getPosition(enumDeclarationNode);

        BLangUnionTypeNode bLangUnionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        bLangUnionTypeNode.pos = bLangTypeDefinition.pos;
        for (Node member : enumDeclarationNode.enumMemberList()) {
            bLangUnionTypeNode.memberTypeNodes.add(createTypeNode(((EnumMemberNode) member).identifier()));
        }
        Collections.reverse(bLangUnionTypeNode.memberTypeNodes);
        bLangTypeDefinition.setTypeNode(bLangUnionTypeNode);
        return bLangTypeDefinition;
    }

    public BLangConstant transformEnumMember(EnumMemberNode member, Boolean publicQualifier) {
        BLangConstant bLangConstant = (BLangConstant) TreeBuilder.createConstantNode();
        bLangConstant.pos = getPosition(member);
        bLangConstant.flagSet.add(Flag.CONSTANT);
        if (publicQualifier) {
            bLangConstant.flagSet.add(Flag.PUBLIC);
        }

        bLangConstant.setName((BLangIdentifier) transform(member.identifier()));

        BLangLiteral literal;
        BLangLiteral deepLiteral;
        if (member.constExprNode().isPresent()) {
            literal = createSimpleLiteral(member.constExprNode().orElse(null));
            deepLiteral = createSimpleLiteral(member.constExprNode().orElse(null));
        } else {
            literal = createSimpleLiteral(member.identifier());
            deepLiteral = createSimpleLiteral(member.identifier());
        }
        if (literal.originalValue != "" || member.identifier().isMissing()) {
            bLangConstant.setInitialExpression(literal);
        } else {
            bLangConstant.setInitialExpression(createExpression(member.constExprNode().orElse(null)));
        }

        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.typeKind = TypeKind.STRING;
        bLangConstant.setTypeNode(typeNode);

        if (deepLiteral.originalValue != "") {
            BLangFiniteTypeNode typeNodeAssosiated = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
            deepLiteral.originalValue = null;
            typeNodeAssosiated.addValue(deepLiteral);
            bLangConstant.associatedTypeDefinition = createTypeDefinitionWithTypeNode(typeNodeAssosiated);
        } else {
            bLangConstant.associatedTypeDefinition = null;
        }
        return bLangConstant;
    }

    @Override
    public BLangNode transform(QueryExpressionNode queryExprNode) {
        BLangQueryExpr queryExpr = (BLangQueryExpr) TreeBuilder.createQueryExpressionNode();
        queryExpr.pos = getPosition(queryExprNode);

        BLangFromClause fromClause = (BLangFromClause) queryExprNode.queryPipeline().fromClause().apply(this);
        queryExpr.queryClauseList.add(fromClause);

        for (Node clauseNode : queryExprNode.queryPipeline().intermediateClauses()) {
            queryExpr.queryClauseList.add(clauseNode.apply(this));
        }

        BLangSelectClause selectClause = (BLangSelectClause) queryExprNode.selectClause().apply(this);
        queryExpr.queryClauseList.add(selectClause);

        Optional<OnConflictClauseNode> onConflict = queryExprNode.onConflictClause();
        onConflict.ifPresent(onConflictClauseNode -> queryExpr.queryClauseList.add(onConflictClauseNode.apply(this)));

        boolean isTable = false;
        boolean isStream = false;

        Optional<QueryConstructTypeNode> optionalQueryConstructTypeNode = queryExprNode.queryConstructType();
        if (optionalQueryConstructTypeNode.isPresent()) {
            QueryConstructTypeNode queryConstructTypeNode = optionalQueryConstructTypeNode.get();
            isTable = queryConstructTypeNode.keyword().kind() == SyntaxKind.TABLE_KEYWORD;
            isStream = queryConstructTypeNode.keyword().kind() == SyntaxKind.STREAM_KEYWORD;
            if (queryConstructTypeNode.keySpecifier().isPresent()) {
                for (IdentifierToken fieldNameNode : queryConstructTypeNode.keySpecifier().get().fieldNames()) {
                    queryExpr.fieldNameIdentifierList.add(createIdentifier(getPosition(fieldNameNode), fieldNameNode));
                }
            }
        }
        queryExpr.isStream = isStream;
        queryExpr.isTable = isTable;
        return queryExpr;
    }

    public BLangNode transform(OnFailClauseNode onFailClauseNode) {
        DiagnosticPos pos = getPosition(onFailClauseNode);
        BLangSimpleVariableDef variableDefinitionNode = (BLangSimpleVariableDef) TreeBuilder.
                createSimpleVariableDefinitionNode();
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        boolean isDeclaredWithVar = onFailClauseNode.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC;
        var.isDeclaredWithVar = isDeclaredWithVar;
        if (!isDeclaredWithVar) {
            var.setTypeNode(createTypeNode(onFailClauseNode.typeDescriptor()));
        }
        var.pos = getPosition(onFailClauseNode);
        var.setName(this.createIdentifier(onFailClauseNode.failErrorName()));
        var.name.pos = getPosition(onFailClauseNode.failErrorName());
        variableDefinitionNode.setVariable(var);


        BLangOnFailClause onFailClause = (BLangOnFailClause) TreeBuilder.createOnFailClauseNode();
        onFailClause.pos = pos;

        onFailClause.isDeclaredWithVar = isDeclaredWithVar;
        markVariableAsFinal(variableDefinitionNode.getVariable());
        onFailClause.variableDefinitionNode = variableDefinitionNode;
        BLangBlockStmt blockNode = (BLangBlockStmt) transform(onFailClauseNode.blockStatement());
        blockNode.pos = getPosition(onFailClauseNode);
        onFailClause.body = blockNode;
        return onFailClause;
    }

    @Override
    public BLangNode transform(LetClauseNode letClauseNode) {
        BLangLetClause bLLetClause = (BLangLetClause) TreeBuilder.createLetClauseNode();
        bLLetClause.pos = getPosition(letClauseNode);
        List<BLangLetVariable> letVars = new ArrayList<>();
        for (LetVariableDeclarationNode letVarDeclr : letClauseNode.letVarDeclarations()) {
            BLangLetVariable letVar = createLetVariable(letVarDeclr);
            letVar.definitionNode.getVariable().addFlag(Flag.FINAL);
            letVars.add(letVar);
        }
        if (!letVars.isEmpty()) {
            bLLetClause.letVarDeclarations = letVars;
        }
        return bLLetClause;
    }

    @Override
    public BLangNode transform(FromClauseNode fromClauseNode) {
        BLangFromClause fromClause = (BLangFromClause) TreeBuilder.createFromClauseNode();
        fromClause.pos = getPosition(fromClauseNode);
        fromClause.collection = createExpression(fromClauseNode.expression());
        TypedBindingPatternNode bindingPatternNode = fromClauseNode.typedBindingPattern();
        fromClause.variableDefinitionNode = createBLangVarDef(getPosition(bindingPatternNode), bindingPatternNode,
                Optional.empty(), Optional.empty());

        boolean isDeclaredWithVar = bindingPatternNode.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC;
        fromClause.isDeclaredWithVar = isDeclaredWithVar;

        return fromClause;
    }

    @Override
    public BLangNode transform(WhereClauseNode whereClauseNode) {
        BLangWhereClause whereClause = (BLangWhereClause) TreeBuilder.createWhereClauseNode();
        whereClause.pos = getPosition(whereClauseNode);
        whereClause.expression = createExpression(whereClauseNode.expression());
        return whereClause;
    }

    @Override
    public BLangNode transform(SelectClauseNode selectClauseNode) {
        BLangSelectClause selectClause = (BLangSelectClause) TreeBuilder.createSelectClauseNode();
        selectClause.pos = getPosition(selectClauseNode);
        selectClause.expression = createExpression(selectClauseNode.expression());
        return selectClause;
    }

    @Override
    public BLangNode transform(OnConflictClauseNode onConflictClauseNode) {
        BLangOnConflictClause onConflictClause = (BLangOnConflictClause) TreeBuilder.createOnConflictClauseNode();
        onConflictClause.pos = getPosition(onConflictClauseNode);
        onConflictClause.expression = createExpression(onConflictClauseNode.expression());
        return onConflictClause;
    }

    @Override
    public BLangNode transform(LimitClauseNode limitClauseNode) {
        BLangLimitClause selectClause = (BLangLimitClause) TreeBuilder.createLimitClauseNode();
        selectClause.pos = getPosition(limitClauseNode);
        selectClause.expression = createExpression(limitClauseNode.expression());
        return selectClause;
    }

    @Override
    public BLangNode transform(OnClauseNode onClauseNode) {
        BLangOnClause onClause = (BLangOnClause) TreeBuilder.createOnClauseNode();
        onClause.pos = getPosition(onClauseNode);
        onClause.lhsExpr = createExpression(onClauseNode.lhsExpression());
        onClause.rhsExpr = createExpression(onClauseNode.rhsExpression());
        return onClause;
    }

    @Override
    public BLangNode transform(JoinClauseNode joinClauseNode) {
        BLangJoinClause joinClause = (BLangJoinClause) TreeBuilder.createJoinClauseNode();
        joinClause.pos = getPosition(joinClauseNode);
        TypedBindingPatternNode typedBindingPattern = joinClauseNode.typedBindingPattern();
        joinClause.variableDefinitionNode = createBLangVarDef(getPosition(joinClauseNode),
                typedBindingPattern, Optional.empty(), Optional.empty());
        joinClause.collection = createExpression(joinClauseNode.expression());
        joinClause.isDeclaredWithVar = typedBindingPattern.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC;
        joinClause.isOuterJoin = joinClauseNode.outerKeyword().isPresent();

        OnClauseNode onClauseNode = joinClauseNode.joinOnCondition();
        BLangOnClause onClause = (BLangOnClause) TreeBuilder.createOnClauseNode();
        onClause.pos = getPosition(onClauseNode);
        onClause.lhsExpr = createExpression(onClauseNode.lhsExpression());
        onClause.rhsExpr = createExpression(onClauseNode.rhsExpression());
        joinClause.onClause = onClause;

        return joinClause;
    }

    @Override
    public BLangNode transform(OrderByClauseNode orderByClauseNode) {
        BLangOrderByClause orderByClause = (BLangOrderByClause) TreeBuilder.createOrderByClauseNode();
        orderByClause.pos = getPosition(orderByClauseNode);
        for (OrderKeyNode orderKeyNode : orderByClauseNode.orderKey()) {
            orderByClause.addOrderKey(createOrderKey(orderKeyNode));
        }
        return orderByClause;
    }

    public BLangOrderKey createOrderKey(OrderKeyNode orderKeyNode) {
        BLangOrderKey orderKey = (BLangOrderKey) TreeBuilder.createOrderKeyNode();
        orderKey.pos = getPosition(orderKeyNode);
        orderKey.expression = createExpression(orderKeyNode.expression());
        if (orderKeyNode.orderDirection().isPresent() &&
                orderKeyNode.orderDirection().get().text().equals("descending")) {
            orderKey.isAscending = false;
        } else {
            orderKey.isAscending = true;
        }
        return orderKey;
    }

    @Override
    public BLangNode transform(IntersectionTypeDescriptorNode intersectionTypeDescriptorNode) {
        BLangType lhsType = (BLangType) createTypeNode(intersectionTypeDescriptorNode.leftTypeDesc());
        BLangType rhsType = (BLangType) createTypeNode(intersectionTypeDescriptorNode.rightTypeDesc());

        BLangIntersectionTypeNode intersectionType;
        if (rhsType.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
            intersectionType = (BLangIntersectionTypeNode) rhsType;
            intersectionType.constituentTypeNodes.add(0, lhsType);
        } else if (lhsType.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
            intersectionType = (BLangIntersectionTypeNode) lhsType;
            intersectionType.constituentTypeNodes.add(rhsType);
        } else {
            intersectionType = (BLangIntersectionTypeNode) TreeBuilder.createIntersectionTypeNode();
            intersectionType.constituentTypeNodes.add(lhsType);
            intersectionType.constituentTypeNodes.add(rhsType);
        }

        intersectionType.pos = getPosition(intersectionTypeDescriptorNode);
        return intersectionType;
    }

    @Override
    protected BLangNode transformSyntaxNode(Node node) {
        // TODO: Remove this RuntimeException once all nodes covered
        throw new RuntimeException("Node not supported: " + node.getClass().getSimpleName());
    }

    @Override
    public BLangNode transform(ClassDefinitionNode classDefinitionNode) {
        BLangClassDefinition blangClass = (BLangClassDefinition) TreeBuilder.createClassDefNode();
        blangClass.pos = getPositionWithoutMetadata(classDefinitionNode);
        blangClass.annAttachments = applyAll(getAnnotations(classDefinitionNode.metadata()));

        BLangIdentifier identifierNode = createIdentifier(classDefinitionNode.className());
        blangClass.setName(identifierNode);
        blangClass.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(classDefinitionNode.metadata()));

        classDefinitionNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                blangClass.flagSet.add(Flag.PUBLIC);
            }
        });

        for (Token qualifier : classDefinitionNode.classTypeQualifiers()) {
            SyntaxKind kind = qualifier.kind();

            switch (kind) {
                case DISTINCT_KEYWORD:
                    blangClass.flagSet.add(Flag.DISTINCT);
                    break;
                case CLIENT_KEYWORD:
                    blangClass.flagSet.add(Flag.CLIENT);
                    break;
                case READONLY_KEYWORD:
                    blangClass.flagSet.add(Flag.READONLY);
                    break;
                case SERVICE_KEYWORD:
                    blangClass.flagSet.add(Flag.SERVICE);
                    break;
                case ISOLATED_KEYWORD:
                    blangClass.flagSet.add(Flag.ISOLATED);
                    break;
                default:
                    throw new RuntimeException("Syntax kind is not supported: " + kind);
            }
        }

        NodeList<Node> members = classDefinitionNode.members();
        for (Node node : members) {
            // TODO: Check for fields other than SimpleVariableNode
            BLangNode bLangNode = node.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    if (blangClass.initFunction == null) {
                        bLangFunction.objInitFunction = true;
                        // TODO: verify removing NULL check for blangClass.initFunction has no side-effects
                        blangClass.initFunction = bLangFunction;
                    } else {
                        blangClass.addFunction(bLangFunction);
                    }
                } else {
                    blangClass.addFunction(bLangFunction);
                }
            } else if (bLangNode.getKind() == NodeKind.VARIABLE) {
                blangClass.addField((BLangSimpleVariable) bLangNode);
            } else if (bLangNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                blangClass.addTypeReference((BLangType) bLangNode);
            }
        }

        return blangClass;
    }

    @Override
    public BLangNode transform(RetryStatementNode retryStatementNode) {
        BLangRetrySpec retrySpec = createRetrySpec(retryStatementNode);
        DiagnosticPos pos = getPosition(retryStatementNode);
        StatementNode retryBody = retryStatementNode.retryBody();

        if (retryBody.kind() == SyntaxKind.TRANSACTION_STATEMENT) {
            BLangRetryTransaction retryTransaction = (BLangRetryTransaction) TreeBuilder.createRetryTransactionNode();
            retryTransaction.pos = pos;
            retryTransaction.setRetrySpec(retrySpec);
            retryTransaction.setTransaction((BLangTransaction) retryBody.apply(this));
            return retryTransaction;
        }

        BLangRetry retryNode = (BLangRetry) TreeBuilder.createRetryNode();
        retryNode.pos = pos;
        retryNode.setRetrySpec(retrySpec);
        BLangBlockStmt retryBlock = (BLangBlockStmt) retryBody.apply(this);
        retryNode.setRetryBody(retryBlock);

        retryStatementNode.onFailClause().ifPresent(onFailClauseNode -> {
            retryNode.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });

        return retryNode;
    }

    private BLangRetrySpec createRetrySpec(RetryStatementNode retryStatementNode) {
        BLangRetrySpec retrySpec = (BLangRetrySpec) TreeBuilder.createRetrySpecNode();
        if (retryStatementNode.typeParameter().isPresent()) {
            TypeParameterNode typeParam = retryStatementNode.typeParameter().get();
            retrySpec.retryManagerType = createTypeNode(typeParam.typeNode());
            retrySpec.pos = getPosition(typeParam);
        }

        if (retryStatementNode.arguments().isPresent()) {
            ParenthesizedArgList arg = retryStatementNode.arguments().get();
            retrySpec.pos = getPosition(arg);
            for (Node argNode : arg.arguments()) {
                retrySpec.argExprs.add(createExpression(argNode));
            }
        }

        if (retrySpec.pos == null) {
            retrySpec.pos = getPosition(retryStatementNode);
        }
        return retrySpec;
    }

    @Override
    public BLangNode transform(TransactionalExpressionNode transactionalExpressionNode) {
        BLangTransactionalExpr transactionalExpr = TreeBuilder.createTransactionalExpressionNode();
        transactionalExpr.pos = getPosition(transactionalExpressionNode);
        return transactionalExpr;
    }

    @Override
    public BLangNode transform(XMLFilterExpressionNode xmlFilterExpressionNode) {
        List<BLangXMLElementFilter> filters = new ArrayList<>();

        XMLNamePatternChainingNode xmlNamePatternChainingNode = xmlFilterExpressionNode.xmlPatternChain();
        for (Node node : xmlNamePatternChainingNode.xmlNamePattern()) {
            filters.add(createXMLElementFilter(node));
        }

        BLangExpression expr = createExpression(xmlFilterExpressionNode.expression());
        BLangXMLElementAccess elementAccess = new BLangXMLElementAccess(getPosition(xmlFilterExpressionNode), null,
                expr, filters);
        return elementAccess;
    }

    @Override
    public BLangNode transform(XMLStepExpressionNode xmlStepExpressionNode) {
        List<BLangXMLElementFilter> filters = new ArrayList<>();

        int starCount = 0;
        if (xmlStepExpressionNode.xmlStepStart().kind() == SyntaxKind.SLASH_ASTERISK_TOKEN) {
            starCount = 1;
        } else if (xmlStepExpressionNode.xmlStepStart().kind() == SyntaxKind.XML_NAME_PATTERN_CHAIN) {
            XMLNamePatternChainingNode xmlNamePatternChainingNode =
                    (XMLNamePatternChainingNode) xmlStepExpressionNode.xmlStepStart();
            for (Node node : xmlNamePatternChainingNode.xmlNamePattern()) {
                filters.add(createXMLElementFilter(node));
            }
            switch (xmlNamePatternChainingNode.startToken().kind()) {
                case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
                    starCount = 2;
                    break;
                case SLASH_ASTERISK_TOKEN:
                    starCount = 1;
                    break;
            }
        }

        BLangExpression expr = createExpression(xmlStepExpressionNode.expression());
        // TODO : implement the value for childIndex
        BLangXMLNavigationAccess xmlNavigationAccess =
                new BLangXMLNavigationAccess(getPosition(xmlStepExpressionNode), null, expr, filters,
                XMLNavigationAccess.NavAccessType.fromInt(starCount), null);
        return xmlNavigationAccess;
    }

    @Override
    public BLangNode transform(MatchStatementNode matchStatementNode) {
        BLangMatchStatement matchStatement = (BLangMatchStatement) TreeBuilder.createMatchStatementNode();
        BLangExpression matchStmtExpr = createExpression(matchStatementNode.condition());
        matchStatement.setExpression(matchStmtExpr);

        for (MatchClauseNode matchClauseNode : matchStatementNode.matchClauses()) {
            BLangMatchClause bLangMatchClause = (BLangMatchClause) TreeBuilder.createMatchClause();
            bLangMatchClause.pos = getPosition(matchClauseNode);
            bLangMatchClause.expr = matchStmtExpr;
            boolean matchGuardAvailable = false;

            if (matchClauseNode.matchGuard().isPresent()) {
                matchGuardAvailable = true;
                BLangMatchGuard bLangMatchGuard = (BLangMatchGuard) TreeBuilder.createMatchGuard();
                bLangMatchGuard.expr = createExpression(matchClauseNode.matchGuard().get().expression());
                bLangMatchClause.setMatchGuard(bLangMatchGuard);
            }

            for (Node matchPattern : matchClauseNode.matchPatterns()) {
                DiagnosticPos matchPatternPos = getPosition(matchPattern);
                BLangMatchPattern bLangMatchPattern = transformMatchPattern(matchPattern, matchPatternPos);
                // TODO : Remove this check after all binding patterns are implemented
                if (bLangMatchPattern != null) {
                    bLangMatchPattern.matchExpr = matchStmtExpr;
                    bLangMatchPattern.matchGuardIsAvailable = matchGuardAvailable;
                    bLangMatchClause.addMatchPattern(bLangMatchPattern);
                }
            }

            bLangMatchClause.setBlockStatement((BLangBlockStmt) transform(matchClauseNode.blockStatement()));
            matchStatement.addMatchClause(bLangMatchClause);
        }

        matchStatementNode.onFailClause().ifPresent(onFailClauseNode -> {
            matchStatement.setOnFailClause(
                    (org.ballerinalang.model.clauses.OnFailClauseNode) (onFailClauseNode.apply(this)));
        });

        return matchStatement;
    }

    private BLangMatchPattern transformMatchPattern(Node matchPattern, DiagnosticPos matchPatternPos) {

        if (matchPattern.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE &&
                ((SimpleNameReferenceNode) matchPattern).name().text().equals("_")) {
            // wildcard match
            BLangWildCardMatchPattern bLangWildCardMatchPattern =
                    (BLangWildCardMatchPattern) TreeBuilder.createWildCardMatchPattern();
            bLangWildCardMatchPattern.pos = matchPatternPos;
            return bLangWildCardMatchPattern;
        } else if (matchPattern.kind() == SyntaxKind.NUMERIC_LITERAL ||
                matchPattern.kind() == SyntaxKind.STRING_LITERAL ||
                matchPattern.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                matchPattern.kind() == SyntaxKind.NULL_LITERAL ||
                matchPattern.kind() == SyntaxKind.NIL_LITERAL ||
                matchPattern.kind() == SyntaxKind.BOOLEAN_LITERAL) {
            BLangConstPattern bLangConstMatchPattern =
                    (BLangConstPattern) TreeBuilder.createConstMatchPattern();
            bLangConstMatchPattern.setExpression(createExpression(matchPattern));
            bLangConstMatchPattern.pos = matchPatternPos;
            return bLangConstMatchPattern;
        } else if (matchPattern.kind() == SyntaxKind.TYPED_BINDING_PATTERN) { // var a
            TypedBindingPatternNode typedBindingPatternNode = (TypedBindingPatternNode) matchPattern;
            BLangVarBindingPatternMatchPattern bLangVarBindingPattern =
                    (BLangVarBindingPatternMatchPattern) TreeBuilder.createVarBindingPattern();
            bLangVarBindingPattern.pos = matchPatternPos;

            SyntaxKind patternKind = typedBindingPatternNode.bindingPattern().kind();
            switch (patternKind) {
                case CAPTURE_BINDING_PATTERN:
                    // TODO : check whether why cant we call the existing transform method
                    CaptureBindingPatternNode captureBindingPattern =
                            (CaptureBindingPatternNode) typedBindingPatternNode.bindingPattern();
                    BLangCaptureBindingPattern bLangCaptureBindingPattern =
                            createCaptureBindingPattern(captureBindingPattern);
                    bLangVarBindingPattern.setBindingPattern(bLangCaptureBindingPattern);
                    break;
                default:
                    // TODO : Remove this after all binding patterns are implemented
                    dlog.error(matchPatternPos, DiagnosticCode.MATCH_PATTERN_NOT_SUPPORTED);
            }
            return bLangVarBindingPattern;
        } else {
            // TODO : Remove this after all binding patterns are implemented
            dlog.error(matchPatternPos, DiagnosticCode.MATCH_PATTERN_NOT_SUPPORTED);
            return null;
        }
    }

    private BLangCaptureBindingPattern createCaptureBindingPattern(CaptureBindingPatternNode
                                                                           captureBindingPatternNode) {
        BLangCaptureBindingPattern bLangCaptureBindingPattern =
                (BLangCaptureBindingPattern) TreeBuilder.createCaptureBindingPattern();
        bLangCaptureBindingPattern.setIdentifier(createIdentifier(captureBindingPatternNode
                .variableName()));
        bLangCaptureBindingPattern.pos = getPosition(captureBindingPatternNode);
        return bLangCaptureBindingPattern;
    }

    private BLangXMLElementFilter createXMLElementFilter(Node node) {
        String ns = "";
        String elementName = "*";
        DiagnosticPos nsPos = null;
        DiagnosticPos elemNamePos = null;
        SyntaxKind kind = node.kind();

        switch (kind) {
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode simpleNameReferenceNode = (SimpleNameReferenceNode) node;
                elementName = simpleNameReferenceNode.name().text();
                elemNamePos = getPosition(simpleNameReferenceNode);
                break;
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode qualifiedNameReferenceNode = (QualifiedNameReferenceNode) node;
                elementName = qualifiedNameReferenceNode.identifier().text();
                elemNamePos = getPosition(qualifiedNameReferenceNode.identifier());
                ns = qualifiedNameReferenceNode.modulePrefix().text();
                nsPos = getPosition(qualifiedNameReferenceNode.modulePrefix());
                break;
            case XML_ATOMIC_NAME_PATTERN:
                XMLAtomicNamePatternNode atomicNamePatternNode = (XMLAtomicNamePatternNode) node;
                elementName = atomicNamePatternNode.name().text();
                elemNamePos = getPosition(atomicNamePatternNode.name());
                ns = atomicNamePatternNode.prefix().text();
                nsPos = getPosition(atomicNamePatternNode.prefix());
                break;
            case ASTERISK_TOKEN:
                elemNamePos = getPosition(node);
        }

        // Escape names starting with '.
        if (stringStartsWithSingleQuote(ns)) {
            ns = ns.substring(1);
        }
        if (stringStartsWithSingleQuote(elementName)) {
            elementName = elementName.substring(1);
        }

        return new BLangXMLElementFilter(getPosition(node), null, ns, nsPos, elementName, elemNamePos);
    }

    private boolean stringStartsWithSingleQuote(String ns) {
        return ns != null && ns.length() > 0 && ns.charAt(0) == '\'';
    }

    // ------------------------------------------private methods--------------------------------------------------------
    private String getValueFromByteArrayNode(ByteArrayLiteralNode byteArrayLiteralNode) {
        StringBuilder value = new StringBuilder();
        value.append(byteArrayLiteralNode.type().text());
        value.append(" ");
        value.append("`");
        if (byteArrayLiteralNode.content().isPresent()) {
            value.append(byteArrayLiteralNode.content().get().text());
        }
        value.append("`");
        return value.toString();
    }

    private List<BLangRecordVariableKeyValue>
            createVariableListForMappingBindingPattern(MappingBindingPatternNode mappingBindingPatternNode) {

        List<BLangRecordVariableKeyValue> fieldBindingPatternsList = new ArrayList<>();
        for (FieldBindingPatternNode node : mappingBindingPatternNode.fieldBindingPatterns()) {
            BLangRecordVariableKeyValue recordKeyValue = new BLangRecordVariableKeyValue();
            if (node instanceof FieldBindingPatternFullNode) {
                FieldBindingPatternFullNode fullNode = (FieldBindingPatternFullNode) node;
                recordKeyValue.key = createIdentifier(fullNode.variableName().name());
                recordKeyValue.valueBindingPattern = getBLangVariableNode(fullNode.bindingPattern());
            } else {
                FieldBindingPatternVarnameNode varnameNode = (FieldBindingPatternVarnameNode) node;
                recordKeyValue.key = createIdentifier(varnameNode.variableName().name());
                BLangSimpleVariable value = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
                value.pos = getPosition(varnameNode);
                IdentifierNode name = createIdentifier(varnameNode.variableName().name());
                ((BLangIdentifier) name).pos = value.pos;
                value.setName(name);
                recordKeyValue.valueBindingPattern = value;
            }

            fieldBindingPatternsList.add(recordKeyValue);
        }

        return fieldBindingPatternsList;
    }

    private BLangLiteral createEmptyLiteral() {
        BLangLiteral bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        bLiteral.value = "";
        bLiteral.originalValue = "";
        bLiteral.type = symTable.getTypeFromTag(TypeTags.STRING);
        return bLiteral;
    }

    private BLangVariable createSimpleVariable(DiagnosticPos pos, String identifier, DiagnosticPos identifierPos) {
        BLangSimpleVariable memberVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        memberVar.pos = pos;
        IdentifierNode name = createIdentifier(identifierPos, identifier);
        ((BLangIdentifier) name).pos = identifierPos;
        memberVar.setName(name);
        return memberVar;
    }

    private BLangVariable getBLangVariableNode(BindingPatternNode bindingPattern) {
        Token varName;
        switch (bindingPattern.kind()) {
            case MAPPING_BINDING_PATTERN:
                MappingBindingPatternNode mappingBindingPatternNode = (MappingBindingPatternNode) bindingPattern;
                BLangRecordVariable recordVariable = (BLangRecordVariable) TreeBuilder.createRecordVariableNode();
                recordVariable.pos = getPosition(mappingBindingPatternNode);
                recordVariable.variableList = createVariableListForMappingBindingPattern(mappingBindingPatternNode);
                if (mappingBindingPatternNode.restBindingPattern().isPresent()) {
                    recordVariable.restParam =
                            getBLangVariableNode(mappingBindingPatternNode.restBindingPattern().get());
                }

                return recordVariable;
            case LIST_BINDING_PATTERN:
                ListBindingPatternNode listBindingPatternNode = (ListBindingPatternNode) bindingPattern;
                BLangTupleVariable tupleVariable = (BLangTupleVariable) TreeBuilder.createTupleVariableNode();
                tupleVariable.pos = getPosition(listBindingPatternNode);

                Optional<RestBindingPatternNode> restBindingPattern = listBindingPatternNode.restBindingPattern();
                if (restBindingPattern.isPresent()) {
                    tupleVariable.restVariable = getBLangVariableNode(restBindingPattern.get());
                }

                for (BindingPatternNode memberBindingPattern : listBindingPatternNode.bindingPatterns()) {
                    BLangVariable member = getBLangVariableNode(memberBindingPattern);
                    tupleVariable.memberVariables.add(member);
                }

                return tupleVariable;
            case ERROR_BINDING_PATTERN:
                ErrorBindingPatternNode errorBindingPatternNode = (ErrorBindingPatternNode) bindingPattern;
                BLangErrorVariable bLangErrorVariable = (BLangErrorVariable) TreeBuilder.createErrorVariableNode();
                bLangErrorVariable.pos = getPosition(errorBindingPatternNode);

                Optional<Node> errorTypeRef = errorBindingPatternNode.typeReference();
                if (errorTypeRef.isPresent()) {
                    bLangErrorVariable.typeNode = createTypeNode(errorTypeRef.get());
                }

                SeparatedNodeList<BindingPatternNode> argListBindingPatterns =
                        errorBindingPatternNode.argListBindingPatterns();
                int numberOfArgs = argListBindingPatterns.size();
                List<BLangErrorVariable.BLangErrorDetailEntry> namedArgs = new ArrayList<>();
                for (int position = 0; position < numberOfArgs; position++) {
                    BindingPatternNode bindingPatternNode = argListBindingPatterns.get(position);
                    switch (bindingPatternNode.kind()) {
                        case CAPTURE_BINDING_PATTERN:
                        case WILDCARD_BINDING_PATTERN:
                            if (position == 0) {
                                bLangErrorVariable.message =
                                        (BLangSimpleVariable) getBLangVariableNode(bindingPatternNode);
                                break;
                            }
                            // Fall through.
                        case ERROR_BINDING_PATTERN:
                            bLangErrorVariable.cause = getBLangVariableNode(bindingPatternNode);
                            break;
                        case NAMED_ARG_BINDING_PATTERN:
                            NamedArgBindingPatternNode namedArgBindingPatternNode =
                                    (NamedArgBindingPatternNode) bindingPatternNode;
                            BLangIdentifier key =
                                    createIdentifier(namedArgBindingPatternNode.argName());
                            BLangVariable valueBindingPattern =
                                    getBLangVariableNode(namedArgBindingPatternNode.bindingPattern());
                            BLangErrorVariable.BLangErrorDetailEntry detailEntry =
                                    new BLangErrorVariable.BLangErrorDetailEntry(key, valueBindingPattern);
                            namedArgs.add(detailEntry);
                            break;
                        default:// Rest binding pattern
                            bLangErrorVariable.restDetail =
                                    (BLangSimpleVariable) getBLangVariableNode(bindingPatternNode);
                    }
                }
                bLangErrorVariable.detail = namedArgs;
                return bLangErrorVariable;
            case REST_BINDING_PATTERN:
                RestBindingPatternNode restBindingPatternNode = (RestBindingPatternNode) bindingPattern;
                varName = restBindingPatternNode.variableName().name();
                break;
            case WILDCARD_BINDING_PATTERN:
                WildcardBindingPatternNode wildcardBindingPatternNode = (WildcardBindingPatternNode) bindingPattern;
                varName = wildcardBindingPatternNode.underscoreToken();
                break;
            case CAPTURE_BINDING_PATTERN:
            default:
                CaptureBindingPatternNode captureBindingPatternNode = (CaptureBindingPatternNode) bindingPattern;
                varName = captureBindingPatternNode.variableName();
                break;
        }

        DiagnosticPos pos = getPosition(bindingPattern);
        return createSimpleVariable(pos, varName.text(), getPosition(varName));
    }

    BLangValueType addValueType(DiagnosticPos pos, TypeKind typeKind) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = typeKind;
        return typeNode;
    }

    private List<BLangStatement> generateBLangStatements(NodeList<StatementNode> statementNodes) {
        List<BLangStatement> statements = new ArrayList<>();
        return generateAndAddBLangStatements(statementNodes, statements);
    }

    private List<BLangStatement> generateAndAddBLangStatements(NodeList<StatementNode> statementNodes,
                                                               List<BLangStatement> statements) {
        for (StatementNode statement : statementNodes) {
            // TODO: Remove this check once statements are non null guaranteed
            if (statement != null) {
                if (statement.kind() == SyntaxKind.FORK_STATEMENT) {
                    generateForkStatements(statements, (ForkStatementNode) statement);
                    continue;
                }
                statements.add((BLangStatement) statement.apply(this));
            }
        }
        return statements;
    }

    private String extractVersion(SeparatedNodeList<Token> versionNumbers) {
        StringBuilder version = new StringBuilder();
        int size = versionNumbers.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                version.append(".");
            }
            version.append(versionNumbers.get(i).text());
        }
        return version.toString();
    }

    private void generateForkStatements(List<BLangStatement> statements, ForkStatementNode forkStatementNode) {
        BLangForkJoin forkJoin = (BLangForkJoin) forkStatementNode.apply(this);
        String nextAnonymousForkKey = anonymousModelHelper.getNextAnonymousForkKey(forkJoin.pos.src.pkgID);
        for (NamedWorkerDeclarationNode workerDeclarationNode : forkStatementNode.namedWorkerDeclarations()) {
            BLangSimpleVariableDef workerDef = (BLangSimpleVariableDef) workerDeclarationNode.apply(this);
            workerDef.isWorker = true;
            workerDef.isInFork = true;
            workerDef.var.flagSet.add(Flag.FORKED);

            BLangFunction function = ((BLangLambdaFunction) workerDef.var.expr).function;
            function.addFlag(Flag.FORKED);
            function.anonForkName = nextAnonymousForkKey;

            statements.add(workerDef);
            while (!this.additionalStatements.empty()) {
                statements.add(additionalStatements.pop());
            }
            forkJoin.addWorkers(workerDef);
        }
        statements.add(forkJoin);
    }

    private BLangCheckedExpr createCheckExpr(DiagnosticPos pos, BLangExpression expr) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkedExpr.pos = pos;
        checkedExpr.expr = expr;
        return checkedExpr;
    }

    private BLangCheckPanickedExpr createCheckPanickedExpr(DiagnosticPos pos, BLangExpression expr) {
        BLangCheckPanickedExpr checkPanickedExpr =
                (BLangCheckPanickedExpr) TreeBuilder.createCheckPanicExpressionNode();
        checkPanickedExpr.pos = pos;
        checkPanickedExpr.expr = expr;
        return checkPanickedExpr;
    }

    private void populateFuncSignature(BLangFunction bLFunction, FunctionSignatureNode funcSignature) {
        // Set Parameters
        for (ParameterNode child : funcSignature.parameters()) {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameterNode) {
                bLFunction.setRestParameter(param);
            } else {
                bLFunction.addParameter(param);
            }
        }

        // Set Return Type
        Optional<ReturnTypeDescriptorNode> retNode = funcSignature.returnTypeDesc();
        if (retNode.isPresent()) {
            ReturnTypeDescriptorNode returnType = retNode.get();
            bLFunction.setReturnTypeNode(createTypeNode(returnType.type()));
            bLFunction.returnTypeAnnAttachments = applyAll(returnType.annotations());
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = symTable.builtinPos;
            bLValueType.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(bLValueType);
        }
    }

    private BLangUnaryExpr createBLangUnaryExpr(DiagnosticPos pos, OperatorKind operatorKind, BLangExpression expr) {
        BLangUnaryExpr bLUnaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        bLUnaryExpr.pos = pos;
        bLUnaryExpr.operator = operatorKind;
        bLUnaryExpr.expr = expr;
        return bLUnaryExpr;
    }

    private BLangExpression createExpression(Node expression) {
        return (BLangExpression) createActionOrExpression(expression);
    }

    private BLangNode createActionOrExpression(Node actionOrExpression) {
        if (isSimpleLiteral(actionOrExpression.kind())) {
            return createSimpleLiteral(actionOrExpression);
        } else if (actionOrExpression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                   actionOrExpression.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE ||
                   actionOrExpression.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Variable References
            BLangNameReference nameReference = createBLangNameReference(actionOrExpression);
            BLangSimpleVarRef bLVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            bLVarRef.pos = getPosition(actionOrExpression);
            bLVarRef.pkgAlias = this.createIdentifier((DiagnosticPos) nameReference.pkgAlias.getPosition(),
                                                      nameReference.pkgAlias.getValue());
            bLVarRef.variableName = this.createIdentifier((DiagnosticPos) nameReference.name.getPosition(),
                                                          nameReference.name.getValue());
            return bLVarRef;
        } else if (actionOrExpression.kind() == SyntaxKind.BRACED_EXPRESSION) {
            BLangGroupExpr group = (BLangGroupExpr) TreeBuilder.createGroupExpressionNode();
            group.expression = (BLangExpression) actionOrExpression.apply(this);
            group.pos = getPosition(actionOrExpression);
            return group;
        } else if (isType(actionOrExpression.kind())) {
            BLangTypedescExpr typeAccessExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
            typeAccessExpr.pos = getPosition(actionOrExpression);
            typeAccessExpr.typeNode = createTypeNode(actionOrExpression);
            return typeAccessExpr;
        } else {
            return actionOrExpression.apply(this);
        }
    }

    private BLangNode createStringTemplateLiteral(NodeList<TemplateMemberNode> memberNodes, DiagnosticPos pos) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        for (Node memberNode : memberNodes) {
            stringTemplateLiteral.exprs.add((BLangExpression) memberNode.apply(this));
        }

        if (stringTemplateLiteral.exprs.isEmpty()) {
            BLangLiteral emptyLiteral = createEmptyLiteral();
            emptyLiteral.pos = pos;
            stringTemplateLiteral.exprs.add(emptyLiteral);
        }

        stringTemplateLiteral.pos = pos;
        return stringTemplateLiteral;
    }

    private BLangRawTemplateLiteral createRawTemplateLiteral(NodeList<TemplateMemberNode> members, DiagnosticPos pos) {
        BLangRawTemplateLiteral literal = (BLangRawTemplateLiteral) TreeBuilder.createRawTemplateLiteralNode();
        literal.pos = pos;

        boolean prevNodeWasInterpolation = false;
        Node firstMember = members.isEmpty() ? null : members.get(0); // will be empty for empty raw template

        if (firstMember != null && firstMember.kind() == SyntaxKind.INTERPOLATION) {
            literal.strings.add(createStringLiteral("", getPosition(firstMember)));
        }

        for (Node member : members) {
            if (member.kind() == SyntaxKind.INTERPOLATION) {
                literal.insertions.add((BLangExpression) member.apply(this));

                if (prevNodeWasInterpolation) {
                    literal.strings.add(createStringLiteral("", getPosition(member)));
                }

                prevNodeWasInterpolation = true;
            } else {
                literal.strings.add((BLangLiteral) member.apply(this));
                prevNodeWasInterpolation = false;
            }
        }

        if (prevNodeWasInterpolation) {
            literal.strings.add(createStringLiteral("", getPosition(members.get(members.size() - 1))));
        }

        return literal;
    }

    private BLangSimpleVariable createSimpleVar(Optional<Token> name, Node type, NodeList<AnnotationNode> annotations) {
        if (name.isPresent()) {
            Token nameToken = name.get();
            return createSimpleVar(nameToken, type, null, false, false, null, annotations);
        }

        return createSimpleVar(null, type, null, false, false, null, annotations);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node type, NodeList<AnnotationNode> annotations) {
        return createSimpleVar(name, type, null, false, false, null, annotations);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node typeName, Node initializer, boolean isFinal,
                                                boolean isListenerVar, Token visibilityQualifier,
                                                NodeList<AnnotationNode> annotations) {
        BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        bLSimpleVar.setName(this.createIdentifier(name));
        bLSimpleVar.name.pos = getPosition(name);

        if (isDeclaredWithVar(typeName)) {
            bLSimpleVar.isDeclaredWithVar = true;
        } else {
            bLSimpleVar.setTypeNode(createTypeNode(typeName));
        }

        if (visibilityQualifier != null) {
            if (visibilityQualifier.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                bLSimpleVar.flagSet.add(Flag.PRIVATE);
            } else if (visibilityQualifier.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                bLSimpleVar.flagSet.add(Flag.PUBLIC);
            }
        }

        if (isFinal) {
            markVariableAsFinal(bLSimpleVar);
        }
        if (initializer != null) {
            bLSimpleVar.setInitialExpression(createExpression(initializer));
        }
        if (isListenerVar) {
            bLSimpleVar.flagSet.add(Flag.LISTENER);
            bLSimpleVar.flagSet.add(Flag.FINAL);
        }

        if (annotations != null) {
            bLSimpleVar.annAttachments = applyAll(annotations);
        }

        return bLSimpleVar;
    }

    private boolean isDeclaredWithVar(Node typeNode) {
        if (typeNode == null || typeNode.kind() == SyntaxKind.VAR_TYPE_DESC) {
            return true;
        }

        return false;
    }

    private BLangIdentifier createIdentifier(Token token) {
        return createIdentifier(getPosition(token), token);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, Token token) {
        if (token == null) {
            return createIdentifier(pos, null, null);
        }

        String identifierName;
        if (token.isMissing()) {
            identifierName = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
        } else {
            identifierName = token.text();
        }

        return createIdentifier(pos, identifierName);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        return createIdentifier(pos, value, null);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value, Set<Whitespace> ws) {
        BLangIdentifier bLIdentifer = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        if (value == null) {
            return bLIdentifer;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            bLIdentifer.setValue(IdentifierUtils.unescapeUnicodeCodepoints(value.substring(1)));
            bLIdentifer.originalValue = value;
            bLIdentifer.setLiteral(true);
        } else {
            bLIdentifer.setValue(value);
            bLIdentifer.setLiteral(false);
        }
        bLIdentifer.pos = pos;
        if (ws != null) {
            bLIdentifer.addWS(ws);
        }
        return bLIdentifer;
    }

    private BLangLiteral createSimpleLiteral(Node literal) {
        return createSimpleLiteral(literal, false);
    }

    private BLangLiteral createSimpleLiteral(Node literal, boolean isFiniteType) {
        if (literal.kind() == SyntaxKind.UNARY_EXPRESSION) {
            UnaryExpressionNode unaryExpr = (UnaryExpressionNode) literal;
            BLangLiteral bLangLiteral =
                    createSimpleLiteral(unaryExpr.expression(), unaryExpr.unaryOperator().kind(), isFiniteType);
            bLangLiteral.pos = getPosition(unaryExpr); // setting the proper pos, else only the expr pos is set
            return bLangLiteral;
        }

        return createSimpleLiteral(literal, SyntaxKind.NONE, isFiniteType);
    }

    private BLangLiteral createSimpleLiteral(Node literal, SyntaxKind sign, boolean isFiniteType) {
        BLangLiteral bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        SyntaxKind type = literal.kind();
        int typeTag = -1;
        Object value = null;
        String originalValue = null;

        String textValue;
        if (literal instanceof BasicLiteralNode) {
            textValue = ((BasicLiteralNode) literal).literalToken().text();
        } else if (literal instanceof Token) {
            textValue = ((Token) literal).text();
        } else {
            textValue = "";
        }

        if (sign == SyntaxKind.PLUS_TOKEN) {
            textValue = "+" + textValue;
        } else if (sign == SyntaxKind.MINUS_TOKEN) {
            textValue = "-" + textValue;
        }

        //TODO: Verify all types, only string type tested
        if (type == SyntaxKind.NUMERIC_LITERAL) {
            SyntaxKind literalTokenKind = ((BasicLiteralNode) literal).literalToken().kind();
            if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN ||
                    literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
                typeTag = TypeTags.INT;
                value = getIntegerLiteral(literal, textValue, sign);
                originalValue = textValue;
                bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            } else if (literalTokenKind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN) {
                //TODO: Check effect of mapping negative(-) numbers as unary-expr
                typeTag = NumericLiteralSupport.isDecimalDiscriminated(textValue) ? TypeTags.DECIMAL : TypeTags.FLOAT;
                if (isFiniteType) {
                    value = textValue.replaceAll("[fd+]", "");
                    originalValue = textValue.replace("+", "");
                } else {
                    value = textValue;
                    originalValue = textValue;
                }
                bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            } else if (literalTokenKind == SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN) {
                //TODO: Check effect of mapping negative(-) numbers as unary-expr
                typeTag = TypeTags.FLOAT;
                value = getHexNodeValue(textValue);
                originalValue = textValue;
                bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            }
        } else if (type == SyntaxKind.BOOLEAN_LITERAL) {
            typeTag = TypeTags.BOOLEAN;
            value = Boolean.parseBoolean(textValue);
            originalValue = textValue;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.STRING_LITERAL || type == SyntaxKind.XML_TEXT_CONTENT ||
                type == SyntaxKind.TEMPLATE_STRING || type == SyntaxKind.IDENTIFIER_TOKEN) {
            String text = textValue;
            if (type == SyntaxKind.STRING_LITERAL) {
                text = text.substring(1, text.length() - 1);
            }
            String originalText = text; // to log the errors
            Matcher matcher = IdentifierUtils.UNICODE_PATTERN.matcher(text);
            int position = 0;
            while (matcher.find(position)) {
                String hexStringVal = matcher.group(1);
                int hexDecimalVal = Integer.parseInt(hexStringVal, 16);
                if ((hexDecimalVal >= Constants.MIN_UNICODE && hexDecimalVal <= Constants.MIDDLE_LIMIT_UNICODE)
                        || hexDecimalVal > Constants.MAX_UNICODE) {
                    String hexStringWithBraces = matcher.group(0);
                    int offset = originalText.indexOf(hexStringWithBraces) + 1;
                    DiagnosticPos pos = getPosition(literal);
                    dlog.error(new DiagnosticPos(this.diagnosticSource, pos.sLine, pos.eLine, pos.sCol + offset,
                                                 pos.sCol + offset + hexStringWithBraces.length()),
                               DiagnosticCode.INVALID_UNICODE, hexStringWithBraces);
                }
                text = matcher.replaceFirst("\\\\u" + fillWithZeros(hexStringVal));
                position = matcher.end() - 2;
                matcher = IdentifierUtils.UNICODE_PATTERN.matcher(text);
            }
            if (type != SyntaxKind.TEMPLATE_STRING && type != SyntaxKind.XML_TEXT_CONTENT) {
                text = StringEscapeUtils.unescapeJava(text);
            }

            typeTag = TypeTags.STRING;
            value = text;
            originalValue = textValue;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NIL_LITERAL) {
            originalValue = "()";
            typeTag = TypeTags.NIL;
            value = null;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        }  else if (type == SyntaxKind.NULL_LITERAL) {
            originalValue = "null";
            typeTag = TypeTags.NIL;
            value = null;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.BINARY_EXPRESSION) { // Should be base16 and base64
            typeTag = TypeTags.BYTE_ARRAY;
            value = textValue;
            originalValue = textValue;

            // If numeric literal create a numeric literal expression; otherwise create a literal expression
            if (isNumericLiteral(type)) {
                bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            } else {
                bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            }
        } else if (type == SyntaxKind.BYTE_ARRAY_LITERAL) {
            return (BLangLiteral) literal.apply(this);
        }

        bLiteral.pos = getPosition(literal);
        bLiteral.type = symTable.getTypeFromTag(typeTag);
        bLiteral.type.tag = typeTag;
        bLiteral.value = value;
        bLiteral.originalValue = originalValue;
        return bLiteral;
    }

    private BLangLiteral createStringLiteral(String value, DiagnosticPos pos) {
        BLangLiteral strLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        strLiteral.value = strLiteral.originalValue = value;
        strLiteral.type = symTable.stringType;
        strLiteral.pos = pos;
        return strLiteral;
    }

    private BLangType createTypeNode(Node type) {
        if (type instanceof BuiltinSimpleNameReferenceNode || type.kind() == SyntaxKind.NIL_TYPE_DESC) {
            return createBuiltInTypeNode(type);
        } else if (type.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE || type.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Exclusive type
            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            BLangNameReference nameReference = createBLangNameReference(type);
            bLUserDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
            bLUserDefinedType.typeName = (BLangIdentifier) nameReference.name;
            bLUserDefinedType.pos = getPosition(type);
            return bLUserDefinedType;
        } else if (type.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            // Map name reference as a type
            if (type.hasDiagnostics()) {
                // if it hasDiagnostics then its missing type desc.
                BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
                BLangIdentifier pkgAlias = this.createIdentifier(null, "");
                BLangIdentifier name = this.createIdentifier(((SimpleNameReferenceNode) type).name());
                BLangNameReference nameReference = new BLangNameReference(getPosition(type), null, pkgAlias, name);
                bLUserDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
                bLUserDefinedType.typeName = (BLangIdentifier) nameReference.name;
                bLUserDefinedType.pos = getPosition(type);
                return bLUserDefinedType;
            }
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) type;
            return createTypeNode(nameReferenceNode.name());
        }
        return (BLangType) type.apply(this);
    }

    private BLangType createBuiltInTypeNode(Node type) {
        String typeText;
        if (type.kind() == SyntaxKind.NIL_TYPE_DESC) {
            typeText = "()";
        } else if (type instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode simpleNameRef = (BuiltinSimpleNameReferenceNode) type;
            if (simpleNameRef.kind() == SyntaxKind.VAR_TYPE_DESC) {
                return null;
            } else if (simpleNameRef.name().isMissing()) {
                String name = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
                BLangIdentifier identifier = createIdentifier(getPosition(simpleNameRef.name()), name);
                BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
                return createUserDefinedType(getPosition(type), pkgAlias, identifier);
            }
            typeText = simpleNameRef.name().text();
        } else {
            typeText = ((Token) type).text(); // TODO: Remove this once map<string> returns Nodes for `map`
        }

        TypeKind typeKind = TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", ""));

        SyntaxKind kind = type.kind();
        switch (kind) {
            case BOOLEAN_TYPE_DESC:
            case INT_TYPE_DESC:
            case BYTE_TYPE_DESC:
            case FLOAT_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case STRING_TYPE_DESC:
            case ANY_TYPE_DESC:
            case NIL_TYPE_DESC:
            case HANDLE_TYPE_DESC:
            case ANYDATA_TYPE_DESC:
            case READONLY_TYPE_DESC:
                BLangValueType valueType = (BLangValueType) TreeBuilder.createValueTypeNode();
                valueType.typeKind = typeKind;
                valueType.pos = getPosition(type);
                return valueType;
            default:
                BLangBuiltInRefTypeNode builtInValueType =
                        (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
                builtInValueType.typeKind = typeKind;
                builtInValueType.pos = getPosition(type);
                return builtInValueType;
        }
    }

    private VariableNode createBasicVarNodeWithoutType(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                       DiagnosticPos identifierPos, ExpressionNode expr) {
        BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        bLSimpleVar.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier, ws);
        ((BLangIdentifier) name).pos = identifierPos;
        bLSimpleVar.setName(name);
        bLSimpleVar.addWS(ws);
        if (expr != null) {
            bLSimpleVar.setInitialExpression(expr);
        }
        return bLSimpleVar;
    }

    private BLangInvocation createBLangInvocation(Node nameNode, NodeList<FunctionArgumentNode> arguments,
                                                  DiagnosticPos position, boolean isAsync) {
        BLangInvocation bLInvocation;
        if (isAsync) {
            bLInvocation = (BLangInvocation) TreeBuilder.createActionInvocation();
        } else {
            bLInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        }
        BLangNameReference reference = createBLangNameReference(nameNode);
        bLInvocation.pkgAlias = (BLangIdentifier) reference.pkgAlias;
        bLInvocation.name = (BLangIdentifier) reference.name;

        List<BLangExpression> args = new ArrayList<>();
        arguments.iterator().forEachRemaining(arg -> args.add(createExpression(arg)));
        bLInvocation.argExprs = args;
        bLInvocation.pos = position;
        return bLInvocation;
    }

    private BLangNameReference createBLangNameReference(Node node) {
        switch (node.kind()) {
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode iNode = (QualifiedNameReferenceNode) node;
                Token modulePrefix = iNode.modulePrefix();
                IdentifierToken identifier = iNode.identifier();
                BLangIdentifier pkgAlias = this.createIdentifier(getPosition(modulePrefix), modulePrefix);
                DiagnosticPos namePos = getPosition(identifier);
                BLangIdentifier name = this.createIdentifier(namePos, identifier);
                return new BLangNameReference(getPosition(node), null, pkgAlias, name);
            case ERROR_TYPE_DESC:
                node = ((BuiltinSimpleNameReferenceNode) node).name();
                break;
            case NEW_KEYWORD:
            case IDENTIFIER_TOKEN:
                break;
            case SIMPLE_NAME_REFERENCE:
            default:
                node = ((SimpleNameReferenceNode) node).name();
                break;
        }

        Token iToken = (Token) node;
        BLangIdentifier pkgAlias = this.createIdentifier(symTable.builtinPos, "");
        BLangIdentifier name = this.createIdentifier(iToken);
        return new BLangNameReference(getPosition(node), null, pkgAlias, name);
    }

    private BLangMarkdownDocumentation createMarkdownDocumentationAttachment(Optional<Node> markdownDocumentationNode) {
        if (markdownDocumentationNode == null || !markdownDocumentationNode.isPresent()) {
            return null;
        }
        BLangMarkdownDocumentation doc = (BLangMarkdownDocumentation) TreeBuilder.createMarkdownDocumentationNode();

        LinkedList<BLangMarkdownDocumentationLine> documentationLines = new LinkedList<>();
        LinkedList<BLangMarkdownParameterDocumentation> parameters = new LinkedList<>();
        LinkedList<BLangMarkdownReferenceDocumentation> references = new LinkedList<>();

        MarkdownDocumentationNode markdownDocNode = (MarkdownDocumentationNode) markdownDocumentationNode.get();
        NodeList<Node> docLineList = markdownDocNode.documentationLines();

        BLangMarkdownParameterDocumentation bLangParaDoc = null;
        BLangMarkdownReturnParameterDocumentation bLangReturnParaDoc = null;
        BLangMarkDownDeprecationDocumentation bLangDeprecationDoc = null;
        BLangMarkDownDeprecatedParametersDocumentation bLangDeprecatedParaDoc = null;
        for (Node singleDocLine : docLineList) {
            switch (singleDocLine.kind()) {
                case MARKDOWN_DOCUMENTATION_LINE:
                case MARKDOWN_REFERENCE_DOCUMENTATION_LINE:
                    MarkdownDocumentationLineNode docLineNode = (MarkdownDocumentationLineNode) singleDocLine;
                    NodeList<Node> docElements = docLineNode.documentElements();
                    String docText = addReferencesAndReturnDocumentationText(references, docElements);

                    if (bLangDeprecationDoc != null) {
                        // reaching here means, a deprecation doc line has already passed.
                        // therefore, add this line to the deprecation documentation.
                        bLangDeprecationDoc.deprecationDocumentationLines.add(docText);
                    } else if (bLangReturnParaDoc != null) {
                        // reaching here means, a return parameter doc line has already passed.
                        // therefore, add this line to the return parameter documentation.
                        bLangReturnParaDoc.returnParameterDocumentationLines.add(docText);
                    } else if (bLangParaDoc != null) {
                        // reaching here means, a parameter doc line has already passed.
                        // therefore, add this line to the parameter documentation.
                        bLangParaDoc.parameterDocumentationLines.add(docText);
                    } else {
                        BLangMarkdownDocumentationLine bLangDocLine =
                                (BLangMarkdownDocumentationLine) TreeBuilder.createMarkdownDocumentationTextNode();
                        bLangDocLine.text = docText;
                        bLangDocLine.pos = getPosition(docLineNode);
                        documentationLines.add(bLangDocLine);
                    }
                    break;
                case MARKDOWN_PARAMETER_DOCUMENTATION_LINE:
                    bLangParaDoc = new BLangMarkdownParameterDocumentation();
                    MarkdownParameterDocumentationLineNode parameterDocLineNode =
                            (MarkdownParameterDocumentationLineNode) singleDocLine;

                    BLangIdentifier paraName = new BLangIdentifier();
                    Token parameterName = parameterDocLineNode.parameterName();
                    paraName.value = parameterName.isMissing() ? "" : parameterName.text();
                    bLangParaDoc.parameterName = paraName;

                    NodeList<Node> paraDocElements = parameterDocLineNode.documentElements();
                    String paraDocText = addReferencesAndReturnDocumentationText(references, paraDocElements);

                    bLangParaDoc.parameterDocumentationLines.add(paraDocText);
                    bLangParaDoc.pos = getPosition(parameterName);

                    if (bLangDeprecatedParaDoc != null) {
                        // reaching here means, a deprecated parameter doc line has already passed.
                        // therefore, add this parameter doc line to the same parameter documentation.
                        bLangDeprecatedParaDoc.parameters.add(bLangParaDoc);
                    } else if (bLangDeprecationDoc != null) {
                        // reaching here means, a deprecation doc line has already passed.
                        // therefore, all parameter doc lines after that should be treated as
                        // deprecated parameter documentation.
                        bLangDeprecatedParaDoc =
                                new BLangMarkDownDeprecatedParametersDocumentation();
                        bLangDeprecatedParaDoc.parameters.add(bLangParaDoc);
                        // passed deprecation doc line is not a normal deprecation doc line.
                        // it is a deprecated parameter doc line. therefore, reset bLangDeprecationDoc.
                        bLangDeprecationDoc = null;
                    } else {
                        parameters.add(bLangParaDoc);
                    }
                    break;
                case MARKDOWN_RETURN_PARAMETER_DOCUMENTATION_LINE:
                    bLangReturnParaDoc = new BLangMarkdownReturnParameterDocumentation();
                    MarkdownParameterDocumentationLineNode returnParaDocLineNode =
                            (MarkdownParameterDocumentationLineNode) singleDocLine;

                    NodeList<Node> returnParaDocElements = returnParaDocLineNode.documentElements();
                    String returnParaDocText =
                            addReferencesAndReturnDocumentationText(references, returnParaDocElements);

                    bLangReturnParaDoc.returnParameterDocumentationLines.add(returnParaDocText);
                    bLangReturnParaDoc.pos = getPosition(returnParaDocLineNode);
                    doc.returnParameter = bLangReturnParaDoc;
                    break;
                case MARKDOWN_DEPRECATION_DOCUMENTATION_LINE:
                    bLangDeprecationDoc = new BLangMarkDownDeprecationDocumentation();
                    MarkdownDocumentationLineNode deprecationDocLineNode =
                            (MarkdownDocumentationLineNode) singleDocLine;

                    String lineText = ((Token) deprecationDocLineNode.documentElements().get(0)).text();
                    bLangDeprecationDoc.addDeprecationLine("# " + lineText);
                    bLangDeprecationDoc.pos = getPosition(deprecationDocLineNode);
                    break;
                default:
                    break;
            }
        }

        doc.documentationLines = documentationLines;
        doc.parameters = parameters;
        doc.references = references;
        doc.deprecationDocumentation = bLangDeprecationDoc;
        doc.deprecatedParametersDocumentation = bLangDeprecatedParaDoc;
        doc.pos = getPosition(markdownDocNode);
        return doc;
    }

    private String addReferencesAndReturnDocumentationText(LinkedList<BLangMarkdownReferenceDocumentation> references,
                                                           NodeList<Node> docElements) {
        StringBuilder docText = new StringBuilder();
        for (Node element : docElements) {
            if (element.kind() == SyntaxKind.DOCUMENTATION_REFERENCE) {
                BLangMarkdownReferenceDocumentation bLangRefDoc = new BLangMarkdownReferenceDocumentation();
                DocumentationReferenceNode docReferenceNode = (DocumentationReferenceNode) element;

                DiagnosticPos pos = getPosition(docReferenceNode);
                bLangRefDoc.pos = pos;

                Token startBacktick = docReferenceNode.startBacktick();
                Node backtickContent = docReferenceNode.backtickContent();
                Token endBacktick = docReferenceNode.endBacktick();

                String contentString = backtickContent.isMissing() ? "" : backtickContent.toString();
                bLangRefDoc.referenceName = contentString;

                bLangRefDoc.type = DocumentationReferenceType.BACKTICK_CONTENT;
                Optional<Token> referenceType = docReferenceNode.referenceType();
                referenceType.ifPresent(
                        refType -> {
                            bLangRefDoc.type = stringToRefType(refType.text());
                            docText.append(refType.toString());
                        }
                );

                transformDocumentationBacktickContent(backtickContent, bLangRefDoc);

                docText.append(startBacktick.isMissing() ? "" : startBacktick.text());
                docText.append(contentString);
                docText.append(endBacktick.isMissing() ? "" : endBacktick.text());
                references.add(bLangRefDoc);
            } else if (element.kind() == SyntaxKind.DOCUMENTATION_DESCRIPTION) {
                Token docDescription = (Token) element;
                docText.append(docDescription.text());
            }
        }

        return trimLeftAtMostOne(docText.toString());
    }

    private String trimLeftAtMostOne(String text) {
        int countToStrip = 0;
        if (!text.isEmpty() && Character.isWhitespace(text.charAt(0))) {
            countToStrip = 1;
        }
        return text.substring(countToStrip);
    }

    private void transformDocumentationBacktickContent(Node backtickContent,
                                                       BLangMarkdownReferenceDocumentation bLangRefDoc) {
        QualifiedNameReferenceNode qualifiedRef;
        SimpleNameReferenceNode simpleRef;

        switch (backtickContent.kind()) {
            case BACKTICK_CONTENT:
                // reaching here means backtick content is invalid.
                // therefore, set hasParserWarnings to true. so that,
                // doc analyzer will avoid further checks on this.
                bLangRefDoc.hasParserWarnings = true;
                break;
            case QUALIFIED_NAME_REFERENCE:
                qualifiedRef = (QualifiedNameReferenceNode) backtickContent;
                bLangRefDoc.qualifier = qualifiedRef.modulePrefix().text();
                bLangRefDoc.identifier = qualifiedRef.identifier().text();
                break;
            case SIMPLE_NAME_REFERENCE:
                simpleRef = (SimpleNameReferenceNode) backtickContent;
                bLangRefDoc.identifier = simpleRef.name().text();
                break;
            case FUNCTION_CALL:
                Node funcName = (((FunctionCallExpressionNode) backtickContent).functionName());
                if (funcName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    qualifiedRef = (QualifiedNameReferenceNode) funcName;
                    bLangRefDoc.qualifier = qualifiedRef.modulePrefix().text();
                    bLangRefDoc.identifier = qualifiedRef.identifier().text();
                } else {
                    simpleRef = (SimpleNameReferenceNode) funcName;
                    bLangRefDoc.identifier = simpleRef.name().text();
                }
                break;
            case METHOD_CALL:
                MethodCallExpressionNode methodCallExprNode = (MethodCallExpressionNode) backtickContent;
                bLangRefDoc.identifier =
                        ((SimpleNameReferenceNode) methodCallExprNode.methodName()).name().text();
                Node refName = methodCallExprNode.expression();
                if (refName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    qualifiedRef = (QualifiedNameReferenceNode) refName;
                    bLangRefDoc.qualifier = qualifiedRef.modulePrefix().text();
                    bLangRefDoc.typeName = qualifiedRef.identifier().text();
                } else {
                    simpleRef = (SimpleNameReferenceNode) refName;
                    bLangRefDoc.typeName = simpleRef.name().text();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid backtick content transformation");
        }
    }

    private DocumentationReferenceType stringToRefType(String refTypeName) {
        switch (refTypeName) {
            case "type":
                return DocumentationReferenceType.TYPE;
            case "service":
                return DocumentationReferenceType.SERVICE;
            case "variable":
                return DocumentationReferenceType.VARIABLE;
            case "var":
                return DocumentationReferenceType.VAR;
            case "annotation":
                return DocumentationReferenceType.ANNOTATION;
            case "module":
                return DocumentationReferenceType.MODULE;
            case "function":
                return DocumentationReferenceType.FUNCTION;
            case "parameter":
                return DocumentationReferenceType.PARAMETER;
            case "const":
                return DocumentationReferenceType.CONST;
            default:
                return DocumentationReferenceType.BACKTICK_CONTENT;
        }
    }

    private Object getIntegerLiteral(Node literal, String nodeValue, SyntaxKind sign) {
        SyntaxKind literalTokenKind = ((BasicLiteralNode) literal).literalToken().kind();
        if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
            return parseLong(literal, nodeValue, nodeValue, 10, sign, DiagnosticCode.INTEGER_TOO_SMALL,
                    DiagnosticCode.INTEGER_TOO_LARGE);
        } else if (literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
            String processedNodeValue = nodeValue.toLowerCase().replace("0x", "");
            return parseLong(literal, nodeValue, processedNodeValue, 16, sign, DiagnosticCode.HEXADECIMAL_TOO_SMALL,
                    DiagnosticCode.HEXADECIMAL_TOO_LARGE);
        }
        return null;
    }

    private Object parseLong(Node literal, String originalNodeValue,
                             String processedNodeValue, int radix, SyntaxKind sign,
                             DiagnosticCode code1, DiagnosticCode code2) {
        try {
            return Long.parseLong(processedNodeValue, radix);
        } catch (Exception e) {
            DiagnosticPos pos = getPosition(literal);
            if (sign == SyntaxKind.MINUS_TOKEN) {
                pos.sCol--;
                dlog.error(pos, code1, originalNodeValue);
            } else {
                dlog.error(pos, code2, originalNodeValue);
            }
        }
        return originalNodeValue;
    }

    private String getHexNodeValue(String value) {
        if (!(value.contains("p") || value.contains("P"))) {
            value = value + "p0";
        }
        return value;
    }

    private String fillWithZeros(String str) {
        while (str.length() < 4) {
            str = "0".concat(str);
        }
        return str;
    }

    private void markVariableAsFinal(BLangVariable variable) {
        // Set the final flag to the variable.
        variable.flagSet.add(Flag.FINAL);

        switch (variable.getKind()) {
            case TUPLE_VARIABLE:
                // If the variable is a tuple variable, we need to set the final flag to the all member variables.
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.memberVariables.forEach(this::markVariableAsFinal);
                if (tupleVariable.restVariable != null) {
                    markVariableAsFinal(tupleVariable.restVariable);
                }
                break;
            case RECORD_VARIABLE:
                // If the variable is a record variable, we need to set the final flag to the all the variables in
                // the record.
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.variableList.stream().map(BLangRecordVariable.BLangRecordVariableKeyValue::getValue)
                        .forEach(this::markVariableAsFinal);
                if (recordVariable.restParam != null) {
                    markVariableAsFinal((BLangVariable) recordVariable.restParam);
                }
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                markVariableAsFinal(errorVariable.message);
                errorVariable.detail.forEach(entry -> markVariableAsFinal(entry.valueBindingPattern));
                if (errorVariable.restDetail != null) {
                    markVariableAsFinal(errorVariable.restDetail);
                }
                break;
        }
    }

    private boolean isSimpleLiteral(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case STRING_LITERAL:
            case NUMERIC_LITERAL:
            case BOOLEAN_LITERAL:
            case NIL_LITERAL:
            case NULL_LITERAL:
                return true;
            default:
                return false;
        }
    }

    static boolean isType(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case RECORD_TYPE_DESC:
            case OBJECT_TYPE_DESC:
            case NIL_TYPE_DESC:
            case OPTIONAL_TYPE_DESC:
            case ARRAY_TYPE_DESC:
            case INT_TYPE_DESC:
            case BYTE_TYPE_DESC:
            case FLOAT_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case STRING_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
            case XML_TYPE_DESC:
            case JSON_TYPE_DESC:
            case HANDLE_TYPE_DESC:
            case ANY_TYPE_DESC:
            case ANYDATA_TYPE_DESC:
            case NEVER_TYPE_DESC:
            case VAR_TYPE_DESC:
            case SERVICE_TYPE_DESC:
            case PARAMETERIZED_TYPE_DESC:
            case UNION_TYPE_DESC:
            case ERROR_TYPE_DESC:
            case STREAM_TYPE_DESC:
            case TABLE_TYPE_DESC:
            case FUNCTION_TYPE_DESC:
            case TUPLE_TYPE_DESC:
            case PARENTHESISED_TYPE_DESC:
            case READONLY_TYPE_DESC:
            case DISTINCT_TYPE_DESC:
            case INTERSECTION_TYPE_DESC:
            case SINGLETON_TYPE_DESC:
            case TYPE_REFERENCE_TYPE_DESC:
                return true;
            default:
                return false;
        }
    }

    private boolean isNumericLiteral(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case NUMERIC_LITERAL:
                return true;
            default:
                return false;
        }
    }

    private boolean isPresent(Node node) {
        return node.kind() != SyntaxKind.NONE;
    }

    private boolean checkIfAnonymous(Node node) {
        Node parent = node.parent();
        return parent.kind() != SyntaxKind.TYPE_DEFINITION;
    }

    private boolean ifInLocalContext(Node parent) {
        while (parent != null) {
            if (parent instanceof StatementNode) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
    }

    private BLangType createAnonymousRecordType(RecordTypeDescriptorNode recordTypeDescriptorNode,
            BLangRecordTypeNode recordTypeNode) {
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        DiagnosticPos pos = getPosition(recordTypeDescriptorNode);
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(this.diagnosticSource.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName, null);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);

        typeDef.typeNode = recordTypeNode;
        typeDef.pos = pos;
        addToTop(typeDef);
        return createUserDefinedType(pos, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name);
    }

    private BLangUserDefinedType createUserDefinedType(DiagnosticPos pos,
            BLangIdentifier pkgAlias,
            BLangIdentifier name) {
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = pos;
        userDefinedType.pkgAlias = pkgAlias;
        userDefinedType.typeName = name;
        return userDefinedType;
    }

    private class SimpleVarBuilder {
        private BLangIdentifier name;
        private BLangType type;
        private boolean isDeclaredWithVar;
        private Set<Flag> flags = new HashSet<>();
        private boolean isFinal;
        private ExpressionNode expr;
        private DiagnosticPos pos;

        public BLangSimpleVariable build() {
            BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            bLSimpleVar.setName(this.name);
            bLSimpleVar.setTypeNode(this.type);
            bLSimpleVar.isDeclaredWithVar = this.isDeclaredWithVar;
            bLSimpleVar.setTypeNode(this.type);
            bLSimpleVar.flagSet.addAll(this.flags);
            if (this.isFinal) {
                markVariableAsFinal(bLSimpleVar);
            }
            bLSimpleVar.setInitialExpression(this.expr);
            bLSimpleVar.pos = pos;
            return bLSimpleVar;
        }

        public SimpleVarBuilder with(String name) {
            this.name = createIdentifier(null, name);
            return this;
        }

        public SimpleVarBuilder with(String name, DiagnosticPos identifierPos) {
            this.name = createIdentifier(identifierPos, name);
            return this;
        }

        public SimpleVarBuilder with(Token token) {
            this.name = createIdentifier(token);
            return this;
        }

        public SimpleVarBuilder setTypeByNode(Node typeName) {
            this.isDeclaredWithVar = typeName == null || typeName.kind() == SyntaxKind.VAR_TYPE_DESC;
            if (typeName == null) {
                return this;
            }
            this.type = createTypeNode(typeName);
            return this;
        }

        public SimpleVarBuilder setExpressionByNode(Node initExprNode) {
            this.expr = initExprNode != null ? createExpression(initExprNode) : null;
            return this;
        }

        public SimpleVarBuilder setExpression(ExpressionNode expression) {
            this.expr = expression;
            return this;
        }

        public SimpleVarBuilder isDeclaredWithVar() {
            this.isDeclaredWithVar = true;
            return this;
        }

        public SimpleVarBuilder isFinal() {
            this.isFinal = true;
            return this;
        }

        public SimpleVarBuilder isListenerVar() {
            this.flags.add(Flag.LISTENER);
            this.flags.add(Flag.FINAL);
            return this;
        }

        public SimpleVarBuilder setVisibility(Token visibilityQualifier) {
            if (visibilityQualifier != null) {
                if (visibilityQualifier.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                    this.flags.add(Flag.PRIVATE);
                } else if (visibilityQualifier.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                    this.flags.add(Flag.PUBLIC);
                }
            }
            return this;
        }

        public SimpleVarBuilder setFinal(boolean present) {
            this.isFinal = present;
            return this;
        }

        public SimpleVarBuilder setOptional(boolean present) {
            if (present) {
                this.flags.add(Flag.PUBLIC);
            } else {
                this.flags.remove(Flag.PUBLIC);
            }
            return this;
        }

        public SimpleVarBuilder setRequired(boolean present) {
            if (present) {
                this.flags.add(Flag.REQUIRED);
            } else {
                this.flags.remove(Flag.REQUIRED);
            }
            return this;
        }

        public SimpleVarBuilder isPublic() {
            this.flags.add(Flag.PUBLIC);
            return this;
        }

        public SimpleVarBuilder isWorkerVar() {
            this.flags.add(Flag.WORKER);
            return this;
        }

        public SimpleVarBuilder setPos(DiagnosticPos pos) {
            this.pos = pos;
            return this;
        }
    }

    private void addFinalQualifier(Optional<Token> finalKeyword, BLangSimpleVariable simpleVar) {
        if (!finalKeyword.isPresent()) {
            return;
        }

        simpleVar.flagSet.add(Flag.FINAL);
    }

    private void addToTop(TopLevelNode topLevelNode) {
        if (currentCompilationUnit != null) {
            currentCompilationUnit.addTopLevelNode(topLevelNode);
        }
    }

    private void expandLeft(DiagnosticPos pos, DiagnosticPos upTo) {
//      pos        |------------|
//      upTo    |-----..
//      result  |---------------|

        assert pos.sLine > upTo.sLine ||
               (pos.sLine == upTo.sLine && pos.sCol >= upTo.sCol);
        pos.sLine = upTo.sLine;
        pos.sCol = upTo.sCol;
    }

    private void trimLeft(DiagnosticPos pos, DiagnosticPos upTo) {
//      pos     |----------------|
//      upTo       |-----..
//      result     |-------------|

        assert pos.sLine < upTo.sLine ||
               (pos.sLine == upTo.sLine && pos.sCol <= upTo.sCol);
        pos.sLine = upTo.sLine;
        pos.sCol = upTo.sCol;
    }

    private void trimRight(DiagnosticPos pos, DiagnosticPos upTo) {

//      pos     |----------------|
//      upTo       ..-----|
//      result  |---------|

        assert pos.eLine > upTo.eLine ||
               (pos.eLine == upTo.eLine && pos.eCol >= upTo.eCol);
        pos.eLine = upTo.eLine;
        pos.eCol = upTo.eCol;
    }
}
