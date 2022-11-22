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
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerina.compiler.syntax.tree.BallerinaNameReferenceNode;
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
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.CommitActionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ContinueStatementNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.FieldMatchPatternNode;
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
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.InferredTypedescDefaultNode;
import io.ballerina.compiler.syntax.tree.InlineCodeReferenceNode;
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
import io.ballerina.compiler.syntax.tree.ListMatchPatternNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
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
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NamedArgBindingPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgMatchPatternNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.NewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
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
import io.ballerina.compiler.syntax.tree.ReAssertionNode;
import io.ballerina.compiler.syntax.tree.ReAtomCharOrEscapeNode;
import io.ballerina.compiler.syntax.tree.ReAtomQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReBracedQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReCapturingGroupsNode;
import io.ballerina.compiler.syntax.tree.ReCharSetAtomNoDashWithReCharSetNoDashNode;
import io.ballerina.compiler.syntax.tree.ReCharSetAtomWithReCharSetNoDashNode;
import io.ballerina.compiler.syntax.tree.ReCharSetRangeNoDashNode;
import io.ballerina.compiler.syntax.tree.ReCharSetRangeNoDashWithReCharSetNode;
import io.ballerina.compiler.syntax.tree.ReCharSetRangeNode;
import io.ballerina.compiler.syntax.tree.ReCharSetRangeWithReCharSetNode;
import io.ballerina.compiler.syntax.tree.ReCharacterClassNode;
import io.ballerina.compiler.syntax.tree.ReFlagExpressionNode;
import io.ballerina.compiler.syntax.tree.ReFlagsNode;
import io.ballerina.compiler.syntax.tree.ReFlagsOnOffNode;
import io.ballerina.compiler.syntax.tree.ReQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReQuoteEscapeNode;
import io.ballerina.compiler.syntax.tree.ReSequenceNode;
import io.ballerina.compiler.syntax.tree.ReSimpleCharClassEscapeNode;
import io.ballerina.compiler.syntax.tree.ReUnicodeGeneralCategoryNode;
import io.ballerina.compiler.syntax.tree.ReUnicodePropertyEscapeNode;
import io.ballerina.compiler.syntax.tree.ReUnicodeScriptNode;
import io.ballerina.compiler.syntax.tree.ReceiveActionNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourceAccessRestSegmentNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerina.compiler.syntax.tree.RestDescriptorNode;
import io.ballerina.compiler.syntax.tree.RestMatchPatternNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RollbackStatementNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadMemberNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyncSendActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
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
import io.ballerina.compiler.syntax.tree.XMLCDATANode;
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
import io.ballerina.identifier.Utils;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
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
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangResourcePathSegment;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangListConstructorSpreadOpExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReTerm;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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

import static org.ballerinalang.model.elements.Flag.INCLUDED;
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
public class BLangNodeBuilder extends NodeTransformer<BLangNode> {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 10; // -10 was added due to the JVM limitations
    private static final String IDENTIFIER_LITERAL_PREFIX = "'";
    private static final String DOLLAR = "$";
    private BLangDiagnosticLog dlog;
    private SymbolTable symTable;

    private PackageCache packageCache;
    private PackageID packageID;
    private String currentCompUnitName;

    private BLangCompilationUnit currentCompilationUnit;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;

    /* To keep track of additional statements produced from multi-BLangNode resultant transformations */
    private Stack<BLangStatement> additionalStatements = new Stack<>();
    private final Stack<String> anonTypeNameSuffixes = new Stack<>();
    /* To keep track if we are inside a block statment for the use of type definition creation */
    private boolean isInLocalContext = false;
    /* To keep track if we are inside a finite context */
    boolean isInFiniteContext = false;
    /* To keep track if we are inside a character class in a regular expresssion */
    boolean isInCharacterClass = false;

    private  HashSet<String> constantSet = new HashSet<String>();

    public BLangNodeBuilder(CompilerContext context,
                            PackageID packageID, String entryName) {
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.dlog.setCurrentPackageId(packageID);
        this.symTable = SymbolTable.getInstance(context);
        this.packageID = packageID;
        this.currentCompUnitName = entryName;
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

    private Location getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange lineRange = node.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        TextRange textRange = node.textRange();
        return new BLangDiagnosticLocation(currentCompUnitName,
                startPos.line(),
                endPos.line(),
                startPos.offset(),
                endPos.offset(),
                textRange.startOffset(),
                textRange.length());
    }

    private Location getPosition(Node startNode, Node endNode) {
        if (startNode == null || endNode == null) {
            return null;
        }
        LinePosition startPos = startNode.lineRange().startLine();
        LinePosition endPos = endNode.lineRange().endLine();
        TextRange startNodeTextRange = startNode.textRange();
        int length = startNodeTextRange.length() + endNode.textRange().length();
        return new BLangDiagnosticLocation(currentCompUnitName, startPos.line(), endPos.line(),
                startPos.offset(), endPos.offset(), startNodeTextRange.startOffset(), length);
    }

    private Location getPositionWithoutMetadata(Node node) {
        if (node == null) {
            return null;
        }
        LineRange nodeLineRange = node.lineRange();
        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        ChildNodeList children = nonTerminalNode.children();
        // If there's metadata it will be the first child.
        // Hence set start position and startOffSet from next immediate child.
        LinePosition startPos;
        int startOffSet;
        int length;
        Node firstChild = children.get(0);
        if (firstChild.kind() == SyntaxKind.METADATA) {
            Node secondChild = children.get(1);
            startPos = secondChild.lineRange().startLine();
            startOffSet = secondChild.textRange().startOffset();
            length = node.textRange().length() - firstChild.textRange().length();
        } else {
            startPos = nodeLineRange.startLine();
            startOffSet = node.textRange().startOffset();
            length = node.textRange().length();
        }
        LinePosition endPos = nodeLineRange.endLine();
        return new BLangDiagnosticLocation(currentCompUnitName,
                startPos.line(),
                endPos.line(),
                startPos.offset(),
                endPos.offset(),
                startOffSet,
                length);
    }

    @Override
    public BLangNode transform(ModulePartNode modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        this.currentCompilationUnit = compilationUnit;
        compilationUnit.name = currentCompUnitName;
        compilationUnit.setPackageID(packageID);
        Location pos = getPosition(modulePart);
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

        Location newLocation = new BLangDiagnosticLocation(pos.lineRange().filePath(), 0, 0, 0, 0, 0, 0);

        compilationUnit.pos = newLocation;
        compilationUnit.setPackageID(packageID);
        this.currentCompilationUnit = null;
        constantSet.clear();
        return compilationUnit;
    }

    @Override
    public BLangNode transform(ModuleVariableDeclarationNode modVarDeclrNode) {
        TypedBindingPatternNode typedBindingPattern = modVarDeclrNode.typedBindingPattern();
        BindingPatternNode bindingPatternNode = typedBindingPattern.bindingPattern();
        BLangVariable variable = getBLangVariableNode(bindingPatternNode, getPositionWithoutMetadata(modVarDeclrNode));

        if (modVarDeclrNode.visibilityQualifier().isPresent()) {
            markVariableWithFlag(variable, Flag.PUBLIC);
        }

        this.anonTypeNameSuffixes.push(getVariableName(variable));
        initializeBLangVariable(variable, typedBindingPattern.typeDescriptor(), modVarDeclrNode.initializer(),
                modVarDeclrNode.qualifiers());
        this.anonTypeNameSuffixes.pop();

        NodeList<AnnotationNode> annotations = getAnnotations(modVarDeclrNode.metadata());
        if (annotations != null) {
            variable.annAttachments = applyAll(annotations);
        }

        variable.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(modVarDeclrNode.metadata()));
        return variable;
    }

    @Override
    public BLangNode transform(ImportDeclarationNode importDeclaration) {
        ImportOrgNameNode orgNameNode = importDeclaration.orgName().orElse(null);
        Optional<ImportPrefixNode> prefixNode = importDeclaration.prefix();

        Token orgName = null;
        if (orgNameNode != null) {
            orgName = orgNameNode.orgName();
        }

        String version = null;

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        NodeList<IdentifierToken> names = importDeclaration.moduleName();
        Location position = getPosition(importDeclaration);
        names.forEach(name -> pkgNameComps.add(this.createIdentifier(getPosition(name), name.text())));

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = position;
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.orgName = this.createIdentifier(getPosition(orgNameNode), orgName);
        importDcl.version = this.createIdentifier(null, version);

        if (prefixNode.isEmpty()) {
            importDcl.alias = pkgNameComps.get(pkgNameComps.size() - 1);
            return importDcl;
        }

        ImportPrefixNode importPrefixNode = prefixNode.get();
        Token prefix = importPrefixNode.prefix();
        Location prefixPos = getPosition(prefix);
        if (prefix.kind() == SyntaxKind.UNDERSCORE_KEYWORD) {
            importDcl.alias = createIgnoreIdentifier(prefix);
        } else {
            importDcl.alias = createIdentifier(prefixPos, prefix);
        }

        return importDcl;
    }

    @Override
    public BLangNode transform(MethodDeclarationNode methodDeclarationNode) {
        BLangFunction bLFunction;
        if (methodDeclarationNode.relativeResourcePath().isEmpty()) {
            bLFunction = createFunctionNode(methodDeclarationNode.methodName(),
                    methodDeclarationNode.qualifierList(), methodDeclarationNode.methodSignature(), null);
        } else {
            bLFunction = createResourceFunctionNode(methodDeclarationNode.methodName(),
                    methodDeclarationNode.qualifierList(), methodDeclarationNode.relativeResourcePath(),
                    methodDeclarationNode.methodSignature(), null);
        }

        bLFunction.annAttachments = applyAll(getAnnotations(methodDeclarationNode.metadata()));
        bLFunction.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(methodDeclarationNode.metadata()));
        bLFunction.pos = getPositionWithoutMetadata(methodDeclarationNode);
        return bLFunction;
    }

    @Override
    public BLangNode transform(ResourcePathParameterNode resourcePathParameterNode) {

        BLangSimpleVariable pathParam = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        pathParam.name = createIdentifier(resourcePathParameterNode.paramName().orElse(null));
        BLangType typeNode = createTypeNode(resourcePathParameterNode.typeDescriptor());
        typeNode.pos = getPosition(resourcePathParameterNode.typeDescriptor());
        pathParam.pos = getPosition(resourcePathParameterNode);
        pathParam.annAttachments = applyAll(resourcePathParameterNode.annotations());

        if (resourcePathParameterNode.kind() == SyntaxKind.RESOURCE_PATH_REST_PARAM) {
            BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
            arrayTypeNode.elemtype = typeNode;
            arrayTypeNode.dimensions = 1;
            typeNode = arrayTypeNode;
        }
        pathParam.typeNode = typeNode;
        return pathParam;
    }

    private BLangFunction createResourceFunctionNode(IdentifierToken accessorName,
                                                     NodeList<Token> qualifierList,
                                                     NodeList<Node> relativeResourcePath,
                                                     FunctionSignatureNode methodSignature,
                                                     FunctionBodyNode functionBody) {

        BLangResourceFunction bLFunction = (BLangResourceFunction) TreeBuilder.createResourceFunctionNode();

        String resourceFuncName = calculateResourceFunctionName(accessorName, relativeResourcePath);
        BLangIdentifier name = createIdentifier(getPosition(accessorName), resourceFuncName);
        populateFunctionNode(name, qualifierList, methodSignature, functionBody, bLFunction);
        bLFunction.methodName = createIdentifier(accessorName);

        List<BLangSimpleVariable> params = new ArrayList<>();
        List<BLangResourcePathSegment> resourcePathSegments = new ArrayList<>();
        for (Node pathSegment : relativeResourcePath) {
            BLangResourcePathSegment bLangPathSegment;
            switch (pathSegment.kind()) {
                case SLASH_TOKEN:
                    continue;
                case RESOURCE_PATH_SEGMENT_PARAM:
                    BLangSimpleVariable param = (BLangSimpleVariable) pathSegment.apply(this);
                    bLangPathSegment = TreeBuilder.createResourcePathSegmentNode(NodeKind.RESOURCE_PATH_PARAM_SEGMENT);
                    if (!param.name.value.equals(Names.EMPTY.value)) {
                        params.add(param);
                        bLFunction.addPathParam(param);
                        bLangPathSegment.name = createIdentifier(getPosition(pathSegment), "^");
                    } else {
                        bLangPathSegment.name = createIdentifier(getPosition(pathSegment), "$^");
                    }

                    bLangPathSegment.typeNode = param.typeNode;
                    bLangPathSegment.pos = param.pos;
                    break;
                case RESOURCE_PATH_REST_PARAM:
                    BLangSimpleVariable restParam = (BLangSimpleVariable) pathSegment.apply(this);
                    bLangPathSegment = 
                            TreeBuilder.createResourcePathSegmentNode(NodeKind.RESOURCE_PATH_REST_PARAM_SEGMENT);
                    if (!restParam.name.value.equals(Names.EMPTY.value)) {
                        params.add(restParam);
                        bLFunction.setRestPathParam(restParam);
                        bLangPathSegment.name = createIdentifier(getPosition(pathSegment), "^^");
                    } else {
                        bLangPathSegment.name = createIdentifier(getPosition(pathSegment), "$^^");
                    }
                    
                    bLangPathSegment.typeNode = ((BLangArrayType) restParam.typeNode).elemtype;
                    bLangPathSegment.pos = restParam.pos;
                    break;
                case DOT_TOKEN:
                    bLangPathSegment = TreeBuilder.createResourcePathSegmentNode(NodeKind.RESOURCE_ROOT_PATH_SEGMENT);
                    bLangPathSegment.name = createIdentifier((Token) pathSegment);
                    bLangPathSegment.pos = bLangPathSegment.name.pos;
                    break;
                default:
                    bLangPathSegment = 
                            TreeBuilder.createResourcePathSegmentNode(NodeKind.RESOURCE_PATH_IDENTIFIER_SEGMENT);
                    bLangPathSegment.name = createIdentifier((Token) pathSegment);
                    BLangFiniteTypeNode bLangFiniteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
                    BLangLiteral simpleLiteral = createSimpleLiteral(pathSegment, true);
                    bLangFiniteTypeNode.valueSpace.add(simpleLiteral);
                    bLangPathSegment.typeNode = bLangFiniteTypeNode;
                    bLangPathSegment.pos = bLangPathSegment.name.pos;
            }
            resourcePathSegments.add(bLangPathSegment);
        }
        bLFunction.getParameters().addAll(0, params);
        bLFunction.resourcePathSegments = resourcePathSegments;

        return bLFunction;
    }

    private String calculateResourceFunctionName(IdentifierToken accessorName, NodeList<Node> relativeResourcePath) {
        StringBuilder sb = new StringBuilder();
        sb.append("$");
        sb.append(createIdentifier(accessorName).getValue());
        for (Node token : relativeResourcePath) {
            switch (token.kind()) {
                case SLASH_TOKEN:
                    continue;
                case RESOURCE_PATH_SEGMENT_PARAM:
                    sb.append("$^");
                    break;
                case RESOURCE_PATH_REST_PARAM:
                    sb.append("$^^");
                    break;
                default:
                    sb.append("$");
                    String value = createIdentifier((Token) token).getValue();
                    sb.append(value);
            }
        }
        return sb.toString();
    }

    private void createAnonymousTypeDefForConstantDeclaration(BLangConstant constantNode, Location pos,
                                                               Location identifierPos) {
        // Create a new finite type node.
        BLangFiniteTypeNode finiteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();

        NodeKind nodeKind = constantNode.expr.getKind();
        // Create a new literal.
        BLangLiteral literal;
        if (nodeKind == NodeKind.LITERAL) {
            literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else {
            literal = (BLangLiteral) TreeBuilder.createNumericLiteralExpression();
        }
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
            literal.setValue(((BLangLiteral) constantNode.expr).value);
            literal.setOriginalValue(((BLangLiteral) constantNode.expr).originalValue);
            literal.setBType(constantNode.expr.getBType());
            literal.isConstant = true;
            finiteTypeNode.valueSpace.add(literal);
        } else {
            // Since we only allow unary expressions to come to this point we can straightaway cast to unary
            BLangUnaryExpr unaryConstant = (BLangUnaryExpr) constantNode.expr;
            BLangUnaryExpr unaryExpr = createBLangUnaryExpr(unaryConstant.pos, unaryConstant.operator,
                    unaryConstant.expr);
            unaryExpr.setBType(unaryConstant.expr.getBType());
            unaryExpr.isConstant = true;
            finiteTypeNode.valueSpace.add(unaryExpr);
        }
        finiteTypeNode.pos = identifierPos;

        // Create a new anonymous type definition.
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        this.anonTypeNameSuffixes.push(constantNode.name.value);
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(packageID, anonTypeNameSuffixes);
        this.anonTypeNameSuffixes.pop();
        IdentifierNode anonTypeGenName = createIdentifier(symTable.builtinPos, genName);
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

    @Override
    public BLangNode transform(ConstantDeclarationNode constantDeclarationNode) {
        BLangConstant constantNode = (BLangConstant) TreeBuilder.createConstantNode();
        Location pos = getPositionWithoutMetadata(constantDeclarationNode);
        Location identifierPos = getPosition(constantDeclarationNode.variableName());
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

        NodeKind nodeKind = constantNode.expr.getKind();

        // Check whether the value is a literal or a unary expression and if it is not any one of the before mentioned
        // kinds it is an invalid case, so we don't need to consider it.
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL ||
                nodeKind == NodeKind.UNARY_EXPR) {
            // Note - If the RHS is a literal, we need to create an anonymous type definition which can later be used
            // in type definitions.h
            createAnonymousTypeDefForConstantDeclaration(constantNode, pos, identifierPos);
        }
        String constantName = constantNode.name.value;
        if (constantSet.contains(constantName)) {
            dlog.error(constantNode.name.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, constantName);
        } else {
            constantSet.add(constantName);
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

        this.anonTypeNameSuffixes.push(typeDef.name.value);
        typeDef.typeNode = createTypeNode(typeDefNode.typeDescriptor());
        this.anonTypeNameSuffixes.pop();

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
        this.isInFiniteContext = true;
        for (TypeDescriptorNode finiteTypeEl : finiteTypeElements) {
            SingletonTypeDescriptorNode singletonTypeNode = (SingletonTypeDescriptorNode) finiteTypeEl;
            BLangExpression literalOrExpression = createLiteralOrExpression(singletonTypeNode.simpleContExprNode());
            bLangFiniteTypeNode.addValue(literalOrExpression);
        }
        this.isInFiniteContext = false;

        if (unionElements.isEmpty()) {
            return bLangFiniteTypeNode;
        }

        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.pos = getPosition(unionTypeDescriptorNode);
        for (TypeDescriptorNode unionElement : unionElements) {
            unionTypeNode.memberTypeNodes.add(createTypeNode(unionElement));
        }

        bLangFiniteTypeNode.setPosition(unionTypeNode.pos);
        if (!finiteTypeElements.isEmpty()) {
            unionTypeNode.memberTypeNodes.add(deSugarTypeAsUserDefType(bLangFiniteTypeNode));
        }
        return unionTypeNode;
    }

    private List<TypeDescriptorNode> flattenUnionType(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        List<TypeDescriptorNode> list = new ArrayList<>();
        flattenUnionType(list, unionTypeDescriptorNode);
        return list;
    }

    private void flattenUnionType(List<TypeDescriptorNode> list, TypeDescriptorNode typeDescriptorNode) {
        if (typeDescriptorNode.kind() != SyntaxKind.UNION_TYPE_DESC) {
            list.add(typeDescriptorNode);
            return;
        }

        UnionTypeDescriptorNode unionTypeDescriptorNode = (UnionTypeDescriptorNode) typeDescriptorNode;
        updateListWithNonUnionTypes(list, unionTypeDescriptorNode.leftTypeDesc());
        updateListWithNonUnionTypes(list, unionTypeDescriptorNode.rightTypeDesc());
    }

    private void updateListWithNonUnionTypes(List<TypeDescriptorNode> list, TypeDescriptorNode typeDescNode) {
        if (typeDescNode.kind() != SyntaxKind.UNION_TYPE_DESC) {
            list.add(typeDescNode);
        } else {
            flattenUnionType(list, typeDescNode);
        }
    }

    private <T> void reverseFlatMap(List<List<T>> listOfLists, List<T> result) {
        for (int i = listOfLists.size() - 1; i >= 0; i--) {
            result.addAll(listOfLists.get(i));
        }
    }

    private BLangUserDefinedType deSugarTypeAsUserDefType(BLangType toIndirect) {
        BLangTypeDefinition bLTypeDef = createTypeDefinitionWithTypeNode(toIndirect);
        Location pos = toIndirect.pos;
        addToTop(bLTypeDef);

        return createUserDefinedType(pos, (BLangIdentifier) TreeBuilder.createIdentifierNode(), bLTypeDef.name);
    }

    private BLangTypeDefinition createTypeDefinitionWithTypeNode(BLangType toIndirect) {
        Location pos = toIndirect.pos;
        BLangTypeDefinition bLTypeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();

        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(packageID, this.anonTypeNameSuffixes);
        IdentifierNode anonTypeGenName = createIdentifier(symTable.builtinPos, genName);
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
    public BLangNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescNode) {
        if (parameterizedTypeDescNode.kind() == SyntaxKind.ERROR_TYPE_DESC) {
            return transformErrorTypeDescriptor(parameterizedTypeDescNode);
        }
        
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = getParameterizedTypeKind(parameterizedTypeDescNode.kind());
        refType.pos = getPosition(parameterizedTypeDescNode);

        Optional<TypeParameterNode> typeParam = parameterizedTypeDescNode.typeParamNode();
        if (typeParam.isPresent()) {
            BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
            constrainedType.type = refType;
            constrainedType.constraint = createTypeNode(typeParam.get().typeNode());
            constrainedType.pos = refType.pos;
            return constrainedType;
        }
        
        return refType;
    }

    private TypeKind getParameterizedTypeKind(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case TYPEDESC_TYPE_DESC:
                return TypeKind.TYPEDESC;
            case FUTURE_TYPE_DESC:
                return TypeKind.FUTURE;
            case XML_TYPE_DESC:
            default:
                return TypeKind.XML;
        }
    }
    
    private BLangNode transformErrorTypeDescriptor(ParameterizedTypeDescriptorNode parameterizedTypeDescNode) {
        BLangErrorType errorType = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        Optional<TypeParameterNode> typeParam = parameterizedTypeDescNode.typeParamNode();
        errorType.pos = getPosition(parameterizedTypeDescNode);
        if (typeParam.isPresent()) {
            TypeParameterNode typeNode = typeParam.get();
            errorType.detailType = createTypeNode(typeNode);
        }

        NonTerminalNode parent = parameterizedTypeDescNode.parent();
        boolean isDistinctError = parent.kind() == SyntaxKind.DISTINCT_TYPE_DESC;
        if (isDistinctError) {
            parent = parent.parent();
        }

        errorType.isAnonymous = this.isInLocalContext || checkIfAnonymous(parameterizedTypeDescNode);
        errorType.isLocal = this.isInLocalContext;

        if (parent.kind() != SyntaxKind.TYPE_DEFINITION
                && (!errorType.isLocal && (isDistinctError || typeParam.isPresent()))) {
            if (isDistinctError) {
                errorType.flagSet.add(Flag.DISTINCT);
            }
            return deSugarTypeAsUserDefType(errorType);
        }

        return errorType;
    }

    private boolean isAnonymousTypeNode(TypeParameterNode typeNode) {
        SyntaxKind paramKind = typeNode.typeNode().kind();
        if (paramKind == SyntaxKind.RECORD_TYPE_DESC || paramKind == SyntaxKind.OBJECT_TYPE_DESC
                || paramKind == SyntaxKind.ERROR_TYPE_DESC) {
            return checkIfAnonymous(typeNode);
        }
        return false;
    }

    @Override
    public BLangNode transform(DistinctTypeDescriptorNode distinctTypeDesc) {
        TypeDescriptorNode type = distinctTypeDesc.typeDescriptor();
        BLangType typeNode = createTypeNode(type);

        // DISTINCT flag is already added to defined error type
        if (!(type.kind() == SyntaxKind.ERROR_TYPE_DESC && typeNode.getKind() == NodeKind.USER_DEFINED_TYPE)) {
            typeNode.flagSet.add(Flag.DISTINCT);
        }
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
            if (bLangNode.getKind() == NodeKind.FUNCTION || bLangNode.getKind() == NodeKind.RESOURCE_FUNC) {
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

        boolean isAnonymous = checkIfAnonymous(objTypeDescNode);
        objectTypeNode.isAnonymous = isAnonymous;

        if (!isAnonymous) {
            return objectTypeNode;
        }

        if (objTypeDescNode.parent().kind() == SyntaxKind.DISTINCT_TYPE_DESC) {
            ((BLangType) objectTypeNode).flagSet.add(Flag.DISTINCT);
        }
        return deSugarTypeAsUserDefType(objectTypeNode);
    }

    public BLangClassDefinition transformObjectCtorExpressionBody(NodeList<Node> members) {
        BLangClassDefinition classDefinition = (BLangClassDefinition) TreeBuilder.createClassDefNode();
        classDefinition.flagSet.add(Flag.ANONYMOUS);
        classDefinition.flagSet.add(Flag.OBJECT_CTOR);
        classDefinition.isObjectContructorDecl = true;

        for (Node node : members) {
            BLangNode bLangNode = node.apply(this);
            NodeKind nodeKind =  bLangNode.getKind();
            if (nodeKind == NodeKind.FUNCTION || bLangNode.getKind() == NodeKind.RESOURCE_FUNC) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                bLangFunction.flagSet.add(Flag.OBJECT_CTOR);
                if (!Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    classDefinition.addFunction(bLangFunction);
                    continue;
                }
                if (classDefinition.initFunction != null) {
                    classDefinition.addFunction(bLangFunction);
                    continue;
                }
                if (bLangFunction.requiredParams.size() != 0) {
                    dlog.error(bLangFunction.pos, DiagnosticErrorCode.OBJECT_CTOR_INIT_CANNOT_HAVE_PARAMETERS);
                    continue;
                }
                bLangFunction.objInitFunction = true;
                classDefinition.initFunction = bLangFunction;
            } else if (nodeKind == NodeKind.VARIABLE) {
                BLangSimpleVariable simpleVariable = (BLangSimpleVariable) bLangNode;
                simpleVariable.flagSet.add(Flag.OBJECT_CTOR);
                classDefinition.addField((BLangSimpleVariable) bLangNode);
            } else if (nodeKind == NodeKind.USER_DEFINED_TYPE) {
                dlog.error(bLangNode.pos, DiagnosticErrorCode.OBJECT_CTOR_DOES_NOT_SUPPORT_TYPE_REFERENCE_MEMBERS);
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
        Location pos = getPositionWithoutMetadata(objectConstructorExpressionNode);
        BLangObjectConstructorExpression objectCtorExpression = TreeBuilder.createObjectCtorExpression();
        BLangClassDefinition anonClass = transformObjectCtorExpressionBody(objectConstructorExpressionNode.members());
        anonClass.pos = pos;
        objectCtorExpression.pos = pos;
        objectCtorExpression.classNode = anonClass;

        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(packageID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        anonClass.setName(anonTypeGenName);
        anonClass.flagSet.add(Flag.PUBLIC);
        anonClass.isObjectContructorDecl = true; // not available for service

        Optional<TypeDescriptorNode> typeReference = objectConstructorExpressionNode.typeReference();
        typeReference.ifPresent(typeReferenceNode -> {
            objectCtorExpression.addTypeReference(createTypeNode(typeReferenceNode));
        });

        anonClass.annAttachments = applyAll(objectConstructorExpressionNode.annotations());
//        addToTop(anonClass);

        NodeList<Token> objectConstructorQualifierList = objectConstructorExpressionNode.objectTypeQualifiers();
        for (Token qualifier : objectConstructorQualifierList) {
            SyntaxKind kind = qualifier.kind();
            if (kind == SyntaxKind.CLIENT_KEYWORD) {
                anonClass.flagSet.add(Flag.CLIENT);
                objectCtorExpression.isClient = true;
            } else if (kind == SyntaxKind.ISOLATED_KEYWORD) {
                anonClass.flagSet.add(Flag.ISOLATED);
            } else if (qualifier.kind() == SyntaxKind.SERVICE_KEYWORD) {
                anonClass.flagSet.add(SERVICE);
                objectCtorExpression.isService = true;
            } else {
                throw new RuntimeException("Syntax kind is not supported: " + kind);
            }
        }

        addToTop(anonClass);
        anonClass.oceEnvData.originalClass = anonClass;
        BLangIdentifier identifier = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        BLangUserDefinedType userDefinedType = createUserDefinedType(pos, identifier, anonClass.name);
        userDefinedType.flagSet.add(Flag.OBJECT_CTOR);

        BLangTypeInit initNode = (BLangTypeInit) TreeBuilder.createInitNode();
        initNode.pos = pos;
        initNode.userDefinedType = userDefinedType;

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        BLangIdentifier pkgAlias = createIdentifier(pos, "");
        invocationNode.name = createIdentifier(pos, genName);
        invocationNode.pkgAlias = pkgAlias;

        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;

        objectCtorExpression.typeInit = initNode;
        return objectCtorExpression;
    }

    @Override
    public BLangNode transform(ObjectFieldNode objFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(objFieldNode.fieldName(), objFieldNode.typeName(),
                objFieldNode.expression().orElse(null), objFieldNode.visibilityQualifier().orElse(null),
                getAnnotations(objFieldNode.metadata()));
        // Transform documentation
        Optional<Node> doc = getDocumentationString(objFieldNode.metadata());
        simpleVar.markdownDocumentationAttachment = createMarkdownDocumentationAttachment(doc);

        NodeList<Token> qualifierList = objFieldNode.qualifierList();
        for (Token token : qualifierList) {
            if (token.kind() == SyntaxKind.FINAL_KEYWORD) {
                addFinalQualifier(simpleVar);
            } else if (token.kind() == SyntaxKind.RESOURCE_KEYWORD) {
                addResourceQualifier(simpleVar);
            }
        }

        simpleVar.flagSet.add(Flag.FIELD);
        simpleVar.pos = getPositionWithoutMetadata(objFieldNode);
        return simpleVar;
    }

    private void addResourceQualifier(BLangSimpleVariable simpleVar) {
        simpleVar.flagSet.add(Flag.RESOURCE);
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
        BLangExpression literalOrExpression =
                createLiteralOrExpression(singletonTypeDescriptorNode.simpleContExprNode());
        bLangFiniteTypeNode.pos = literalOrExpression.pos;
        bLangFiniteTypeNode.valueSpace.add(literalOrExpression);
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
        Token fieldName = recordFieldNode.fieldName();
        this.anonTypeNameSuffixes.push(fieldName.text());
        BLangSimpleVariable simpleVar = createSimpleVar(fieldName, recordFieldNode.typeName(),
                getAnnotations(recordFieldNode.metadata()));
        this.anonTypeNameSuffixes.pop();
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (recordFieldNode.questionMarkToken().isPresent()) {
            simpleVar.flagSet.add(Flag.OPTIONAL);
        } else {
            simpleVar.flagSet.add(Flag.REQUIRED);
        }
        simpleVar.flagSet.add(Flag.FIELD);
        addReadOnlyQualifier(recordFieldNode.readonlyKeyword(), simpleVar);

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

        addReadOnlyQualifier(recordFieldNode.readonlyKeyword(), simpleVar);

        simpleVar.pos = getPositionWithoutMetadata(recordFieldNode);
        return simpleVar;
    }

    private void addReadOnlyQualifier(Optional<Token> readonlyKeyword, BLangSimpleVariable simpleVar) {
        if (readonlyKeyword.isPresent()) {
            simpleVar.flagSet.add(Flag.READONLY);
        }
    }

    @Override
    public BLangNode transform(RecordRestDescriptorNode recordFieldNode) {
        return createTypeNode(recordFieldNode.typeName());
    }

    @Override
    public BLangNode transform(FunctionDefinitionNode funcDefNode) {
        BLangFunction bLFunction;
        if (funcDefNode.relativeResourcePath().isEmpty()) {
            bLFunction = createFunctionNode(funcDefNode.functionName(), funcDefNode.qualifierList(),
                    funcDefNode.functionSignature(), funcDefNode.functionBody());
        } else {
            bLFunction = createResourceFunctionNode(funcDefNode.functionName(),
                    funcDefNode.qualifierList(), funcDefNode.relativeResourcePath(),
                    funcDefNode.functionSignature(), funcDefNode.functionBody());
        }

        bLFunction.annAttachments = applyAll(getAnnotations(funcDefNode.metadata()));
        bLFunction.pos = getPositionWithoutMetadata(funcDefNode);

        bLFunction.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(funcDefNode.metadata()));
        return bLFunction;
    }

    private BLangFunction createFunctionNode(IdentifierToken funcName, NodeList<Token> qualifierList,
            FunctionSignatureNode functionSignature, FunctionBodyNode functionBody) {

        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();

        BLangIdentifier name = createIdentifier(getPosition(funcName), funcName);
        populateFunctionNode(name, qualifierList, functionSignature, functionBody, bLFunction);
        return bLFunction;
    }

    private void populateFunctionNode(BLangIdentifier name, NodeList<Token> qualifierList,
                                      FunctionSignatureNode functionSignature, FunctionBodyNode functionBody,
                                      BLangFunction bLFunction) {
        // Set function name
        bLFunction.name = name;
        //Set method qualifiers
        setFunctionQualifiers(bLFunction, qualifierList);
        // Set function signature
        this.anonTypeNameSuffixes.push(name.value);
        populateFuncSignature(bLFunction, functionSignature);
        this.anonTypeNameSuffixes.pop();

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
        externFunctionBodyNode.pos = getPosition(externalFunctionBodyNode);
        return externFunctionBodyNode;
    }

    @Override
    public BLangNode transform(ExplicitAnonymousFunctionExpressionNode anonFuncExprNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        Location pos = getPosition(anonFuncExprNode);

        // Set function name
        bLFunction.name = createIdentifier(symTable.builtinPos,
                                           anonymousModelHelper.getNextAnonymousFunctionKey(packageID));

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
        List<BLangStatement> stmtList = statements;
        if (functionBodyBlockNode.namedWorkerDeclarator().isPresent()) {
            NamedWorkerDeclarator namedWorkerDeclarator = functionBodyBlockNode.namedWorkerDeclarator().get();
            NodeList<StatementNode> workerInitStmts = namedWorkerDeclarator.workerInitStatements();
            generateAndAddBLangStatements(workerInitStmts, statements, 0, functionBodyBlockNode);

            stmtList = getStatementList(statements, workerInitStmts);

            for (NamedWorkerDeclarationNode workerDeclarationNode : namedWorkerDeclarator.namedWorkerDeclarations()) {
                stmtList.add((BLangStatement) workerDeclarationNode.apply(this));
                // Consume resultant additional statements
                while (!this.additionalStatements.empty()) {
                    stmtList.add(additionalStatements.pop());
                }
            }
        }

        generateAndAddBLangStatements(functionBodyBlockNode.statements(), stmtList, 0, functionBodyBlockNode);

        bLFuncBody.stmts = statements;
        bLFuncBody.pos = getPosition(functionBodyBlockNode);
        this.isInLocalContext = false;
        return bLFuncBody;
    }

    private List<BLangStatement> getStatementList(List<BLangStatement> statements,
                                                  NodeList<StatementNode> workerInitStmts) {
        int stmtSize = statements.size();
        int workerInitStmtSize = workerInitStmts.size();
        // If there's a worker defined after an `if` statement without an `else`, need to add it to the
        // newly created block statement.
        if (stmtSize > 1 && workerInitStmtSize > 0 && statements.get(stmtSize - 1).getKind() == NodeKind.BLOCK &&
                statements.get(stmtSize - 2).getKind() == NodeKind.IF &&
                workerInitStmts.get(workerInitStmtSize - 1).kind() != SyntaxKind.BLOCK_STATEMENT) {
            return ((BLangBlockStmt) statements.get(stmtSize - 1)).stmts;
        }
        return statements;
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
        Location forkStmtPos = getPosition(forkStatementNode);
        forkJoin.pos = forkStmtPos;
        return forkJoin;
    }

    @Override
    public BLangNode transform(NamedWorkerDeclarationNode namedWorkerDeclNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        Location workerBodyPos = getPosition(namedWorkerDeclNode.workerBody());

        // Set function name
        bLFunction.name = createIdentifier(symTable.builtinPos,
                                           anonymousModelHelper.getNextAnonymousFunctionKey(packageID));

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
        if (namedWorkerDeclNode.workerName().isMissing() || workerName.equals(IDENTIFIER_LITERAL_PREFIX)) {
            workerName = missingNodesHelper.getNextMissingNodeName(packageID);
        }

        String workerOriginalName = workerName;
        if (workerName.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            bLFunction.defaultWorkerName.setOriginalValue(workerName);
            workerName = Utils.unescapeUnicodeCodepoints(workerName.substring(1));
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

        Location workerNamePos = getPosition(namedWorkerDeclNode.workerName());
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
        bLInvocation.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        bLInvocation.name = nameInd;
        bLInvocation.pos = workerNamePos;
        bLInvocation.flagSet = new HashSet<>();
        bLInvocation.annAttachments = bLFunction.annAttachments;

        if (bLInvocation.getKind() == NodeKind.INVOCATION) {
            bLInvocation.async = true;
//            attachAnnotations(invocation, numAnnotations, false);
        } else {
            dlog.error(workerBodyPos, DiagnosticErrorCode.START_REQUIRE_INVOCATION);
        }

        BLangSimpleVariable invoc = new SimpleVarBuilder()
                .with(workerOriginalName, workerNamePos)
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
        final BLangIdentifier[] nameReference = createBLangNameReference(name);
        bLAnnotationAttachment.setPackageAlias(nameReference[0]);
        bLAnnotationAttachment.setAnnotationName(nameReference[1]);
        bLAnnotationAttachment.pos = getPosition(annotation);
        return bLAnnotationAttachment;
    }

    @Override
    public BLangNode transform(QueryActionNode queryActionNode) {
        BLangQueryAction bLQueryAction = (BLangQueryAction) TreeBuilder.createQueryActionNode();
        BLangDoClause doClause = (BLangDoClause) TreeBuilder.createDoClauseNode();
        doClause.body = (BLangBlockStmt) queryActionNode.blockStatement().apply(this);
        doClause.body.pos = expandLeft(doClause.body.pos, getPosition(queryActionNode.doKeyword()));
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
        Location pos = getPositionWithoutMetadata(annotationDeclarationNode);
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
            NodeList<Token> idents = attachPoint.identifiers();
            Token firstIndent =  idents.get(0);

            switch (firstIndent.kind()) {
                case OBJECT_KEYWORD:
                    Token secondIndent = idents.get(1);
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
                case SERVICE_KEYWORD:
                    String value;
                    if (idents.size() == 1) {
                        value = AttachPoint.Point.SERVICE.getValue();
                    } else if (idents.size() == 3) {
                        // the only case we are having 3 idents is service remote function
                        value = AttachPoint.Point.SERVICE_REMOTE.getValue();
                    } else {
                        throw new RuntimeException("Invalid annotation attach point");
                    }
                    bLAttachPoint = AttachPoint.getAttachmentPoint(value, source);
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
        Location pos = getPosition(checkExpressionNode);
        BLangExpression expr = createExpression(checkExpressionNode.expression());
        if (checkExpressionNode.checkKeyword().kind() == SyntaxKind.CHECK_KEYWORD) {
            return createCheckExpr(pos, expr);
        }
        return createCheckPanickedExpr(pos, expr);
    }

    @Override
    public BLangNode transform(TypeTestExpressionNode typeTestExpressionNode) {
        BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        if (typeTestExpressionNode.isKeyword().kind() == SyntaxKind.NOT_IS_KEYWORD) {
            typeTestExpr.isNegation = true;
        }
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
                bLRecordSpreadOpField.pos = getPosition(spreadFieldNode);
                bLiteralNode.fields.add(bLRecordSpreadOpField);
            } else if (field.kind() == SyntaxKind.COMPUTED_NAME_FIELD) {
                ComputedNameFieldNode computedNameField = (ComputedNameFieldNode) field;
                BLangRecordKeyValueField bLRecordKeyValueField =
                        (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
                bLRecordKeyValueField.valueExpr = createExpression(computedNameField.valueExpr());
                bLRecordKeyValueField.pos = getPosition(computedNameField);

                bLRecordKeyValueField.key =
                        new BLangRecordLiteral.BLangRecordKey(createExpression(computedNameField.fieldNameExpr()));
                bLRecordKeyValueField.key.computedKey = true;
                bLRecordKeyValueField.key.pos = getPosition(computedNameField.fieldNameExpr());

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
                    bLRecordKeyValueField.pos = getPosition(specificField);
                    bLRecordKeyValueField.readonly = specificField.readonlyKeyword().isPresent();

                    bLRecordKeyValueField.valueExpr = createExpression(valueExpr);
                    bLRecordKeyValueField.key =
                            new BLangRecordLiteral.BLangRecordKey(createExpression(specificField.fieldName()));
                    bLRecordKeyValueField.key.computedKey = false;
                    bLRecordKeyValueField.key.pos = getPosition(specificField.fieldName());

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

        for (Node listMember : listConstructorExprNode.expressions()) {
            BLangExpression memberExpr;
            if (listMember.kind() == SyntaxKind.SPREAD_MEMBER) {
                Node spreadMemberExpr = ((SpreadMemberNode) listMember).expression();
                memberExpr = createSpreadMemberExpr(spreadMemberExpr, getPosition(spreadMemberExpr));
            } else {
                memberExpr = createExpression(listMember);
            }
            argExprList.add(memberExpr);
        }

        listConstructorExpr.exprs = argExprList;
        listConstructorExpr.pos = getPosition(listConstructorExprNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(UnaryExpressionNode unaryExprNode) {
        Location pos = getPosition(unaryExprNode);
        OperatorKind operator = OperatorKind.valueFrom(unaryExprNode.unaryOperator().text());
        BLangExpression expr = createExpression(unaryExprNode.expression());
        return createBLangUnaryExpr(pos, operator, expr);
    }

    @Override
    public BLangNode transform(TypeofExpressionNode typeofExpressionNode) {
        Location pos = getPosition(typeofExpressionNode);
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
        bLFieldBasedAccess.field.pos = getPosition(fieldAccessExprNode.fieldName());
        bLFieldBasedAccess.optionalFieldAccess = false;
        return bLFieldBasedAccess;
    }

    @Override
    public BLangNode transform(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        BLangFieldBasedAccess bLFieldBasedAccess;
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
        bLFieldBasedAccess.field.pos = getPosition(optionalFieldAccessExpressionNode.fieldName());
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

    @Override
    public BLangNode transform(ErrorConstructorExpressionNode errorConstructorExprNode) {
        BLangErrorConstructorExpr errorConstructorExpr =
                (BLangErrorConstructorExpr) TreeBuilder.createErrorConstructorExpressionNode();
        errorConstructorExpr.pos = getPosition(errorConstructorExprNode);
        if (errorConstructorExprNode.typeReference().isPresent()) {
            errorConstructorExpr.errorTypeRef =
                    (BLangUserDefinedType) createTypeNode(errorConstructorExprNode.typeReference().get());
        }

        List<BLangExpression> positionalArgs = new ArrayList<>();
        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        for (Node argNode : errorConstructorExprNode.arguments()) {
            if (argNode.kind() == SyntaxKind.POSITIONAL_ARG) {
                positionalArgs.add((BLangExpression) transform((PositionalArgumentNode) argNode));
            } else if (argNode.kind() == SyntaxKind.NAMED_ARG) {
                namedArgs.add((BLangNamedArgsExpression) transform((NamedArgumentNode) argNode));
            }
        }

        errorConstructorExpr.positionalArgs = positionalArgs;
        errorConstructorExpr.namedArgs = namedArgs;
        return errorConstructorExpr;
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

        BLangIdentifier[] nameReference = createBLangNameReference(newKeyword);
        invocationNode.pkgAlias = nameReference[0];
        invocationNode.name = nameReference[1];

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
        if (keys.size() == 0) {
            // TODO : This should be handled by Parser, issue #31536
            dlog.error(getPosition(indexedExpressionNode.closeBracket()),
                    DiagnosticErrorCode.MISSING_KEY_EXPR_IN_MEMBER_ACCESS_EXPR);
            Token missingIdentifier = NodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN,
                    NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList());
            Node expression = NodeFactory.createSimpleNameReferenceNode(missingIdentifier);
            indexBasedAccess.indexExpr = createExpression(expression);
        } else if (keys.size() == 1) {
            indexBasedAccess.indexExpr = createExpression(indexedExpressionNode.keyExpression().get(0));
        } else {
            BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                    TreeBuilder.createListConstructorExpressionNode();
            listConstructorExpr.pos = getPosition(keys.get(0), keys.get(keys.size() - 1));
            List<BLangExpression> exprs = new ArrayList<>();
            for (io.ballerina.compiler.syntax.tree.ExpressionNode keyExpr : keys) {
                exprs.add(createExpression(keyExpr));
            }
            listConstructorExpr.exprs = exprs;
            indexBasedAccess.indexExpr = listConstructorExpr;
        }

        Node containerExpr = indexedExpressionNode.containerExpression();
        BLangExpression expression = createExpression(containerExpr);
        if (containerExpr.kind() == SyntaxKind.XML_STEP_EXPRESSION) {
            // TODO : This check will be removed after changes are done for spec issue #536

            // The original expression position is overwritten here since the modeling of BLangXMLNavigationAccess is
            // different from the normal index based access.
            expression.pos = indexBasedAccess.pos;
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
        if (typeCastParamNode != null && typeCastParamNode.type().isPresent()) {
            typeConversionNode.typeNode = createTypeNode(typeCastParamNode.type().get());
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
                if (isTokenInRegExp(kind)) {
                    return createSimpleLiteral(token);
                }
                throw new RuntimeException("Syntax kind is not supported: " + kind);
        }
    }

    private boolean isTokenInRegExp(SyntaxKind kind) {
        switch (kind) {
            case RE_ASSERTION_VALUE:
            case RE_CHAR:
            case RE_ESCAPE:
            case RE_SYNTAX_CHAR:
            case RE_SIMPLE_CHAR_CLASS_CODE:
            case RE_PROPERTY:
            case RE_UNICODE_SCRIPT_START:
            case RE_UNICODE_PROPERTY_VALUE:
            case RE_UNICODE_GENERAL_CATEGORY_START:
            case RE_UNICODE_GENERAL_CATEGORY_NAME:
            case RE_CHAR_SET_ATOM:
            case RE_CHAR_SET_ATOM_NO_DASH:
            case RE_CHAR_SET_RANGE_LHS_CHAR_SET_ATOM:
            case RE_CHAR_SET_RANGE_NO_DASH_LHS_CHAR_SET_ATOM_NO_DASH:
            case RE_FLAGS_VALUE:
            case RE_BASE_QUANTIFIER_VALUE:
            case RE_BRACED_QUANTIFIER_DIGIT:
            case OPEN_BRACE_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case OPEN_PAREN_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case QUESTION_MARK_TOKEN:
            case BITWISE_XOR_TOKEN:
            case COLON_TOKEN:
            case BACK_SLASH_TOKEN:
            case MINUS_TOKEN:
            case PIPE_TOKEN:
            case COMMA_TOKEN:
                return true;
            default:
                return false;
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
                BLangNode xmlTemplateLiteral = createXmlTemplateLiteral(expressionNode);
                xmlTemplateLiteral.pos = getPosition(expressionNode);
                return xmlTemplateLiteral;
            case STRING_TEMPLATE_EXPRESSION:
                return createStringTemplateLiteral(expressionNode.content(), getPosition(expressionNode));
            case RAW_TEMPLATE_EXPRESSION:
                return createRawTemplateLiteral(expressionNode.content(), getPosition(expressionNode));
            case REGEX_TEMPLATE_EXPRESSION:
                return createRegExpTemplateLiteral(expressionNode.content(), getPosition(expressionNode));
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
        Node receiveWorkers = receiveActionNode.receiveWorkers();
        Token workerName;
        if (receiveWorkers.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            workerName = ((SimpleNameReferenceNode) receiveWorkers).name();
        } else {
            // TODO: implement multiple-receive-action support
            Location receiveFieldsPos = getPosition(receiveWorkers);
            dlog.error(receiveFieldsPos, DiagnosticErrorCode.MULTIPLE_RECEIVE_ACTION_NOT_YET_SUPPORTED);
            workerName = NodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN,
                    NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList());
        }
        workerReceiveExpr.setWorkerName(createIdentifier(workerName));
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
                anonymousModelHelper.getNextAnonymousFunctionKey(packageID));
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
                parameter.pos = getPosition(child);
                parameter.addFlag(Flag.REQUIRED_PARAM);
                arrowFunction.params.add(parameter);
            }

        } else {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) param.apply(this);
            BLangSimpleVariable parameter = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            parameter.name = userDefinedType.typeName;
            parameter.pos = getPosition(param);
            arrowFunction.params.add(parameter);
        }
        arrowFunction.body = new BLangExprFunctionBody();
        arrowFunction.body.expr = createExpression(implicitAnonymousFunctionExpressionNode.expression());
        arrowFunction.body.pos = arrowFunction.body.expr.pos;
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
    public BLangNode transform(MappingBindingPatternNode mappingBindingPatternNode) {
        BLangRecordVarRef recordVarRef = (BLangRecordVarRef) TreeBuilder.createRecordVariableReferenceNode();
        recordVarRef.pos = getPosition(mappingBindingPatternNode);

        List<BLangRecordVarRefKeyValue> expressions = new ArrayList<>();
        for (BindingPatternNode expr : mappingBindingPatternNode.fieldBindingPatterns()) {
            if (expr.kind() == SyntaxKind.REST_BINDING_PATTERN) {
                recordVarRef.restParam = createExpression(expr);
            } else {
                expressions.add(createRecordVarKeyValue(expr));
            }
        }
        recordVarRef.recordRefFields = expressions;
        return recordVarRef;
    }

    private BLangRecordVarRefKeyValue createRecordVarKeyValue(BindingPatternNode expr) {
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
            if (expr.kind() == SyntaxKind.REST_BINDING_PATTERN) {
                tupleVarRef.restParam = createExpression(expr);
            } else {
                expressions.add(createExpression(expr));
            }
        }
        tupleVarRef.expressions = expressions;
        tupleVarRef.pos = getPosition(listBindingPatternNode);
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
        BLangIdentifier ignore = createIgnoreIdentifier(wildcardBindingPatternNode);
        ignoreVarRef.variableName = ignore;
        ignoreVarRef.pos = ignore.pos;
        return ignoreVarRef;
    }

    private BLangIdentifier createIgnoreIdentifier(Node node) {
        BLangIdentifier ignore = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        ignore.value = Names.IGNORE.value;
        ignore.setOriginalValue(Names.IGNORE.value);
        ignore.pos = getPosition(node);
        return ignore;
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
            nilLiteral.setBType(symTable.nilType);
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
                .setTypeByNode(listenerDeclarationNode.typeDescriptor().orElse(null))
                .setExpressionByNode(listenerDeclarationNode.initializer())
                .setVisibility(visibilityQualifier)
                .isListenerVar()
                .build();
        var.pos = getPositionWithoutMetadata(listenerDeclarationNode);
        var.name.pos = getPosition(listenerDeclarationNode.variableName());
        var.annAttachments = applyAll(getAnnotations(listenerDeclarationNode.metadata()));
        var.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(listenerDeclarationNode.metadata()));
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
        validateLvexpr(lhsExpr, DiagnosticErrorCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT);

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
        recordDestructure.pos = getPosition(assignmentStmtNode);
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
                .setVariable((VariableReferenceNode) createExpression(compoundAssignmentStmtNode.lhsExpression()));
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
        bLBlockStmt.stmts = generateBLangStatements(blockStatement.statements(), blockStatement);
        this.isInLocalContext = false;
        bLBlockStmt.pos = getPosition(blockStatement);
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
    public BLangNode transform(VariableDeclarationNode varDeclaration) {
        VariableDefinitionNode varNode =
                createBLangVarDef(getPosition(varDeclaration), varDeclaration.typedBindingPattern(),
                                  varDeclaration.initializer(), varDeclaration.finalKeyword());
        ((BLangVariable) varNode.getVariable()).annAttachments = applyAll(varDeclaration.annotations());
        return (BLangNode) varNode;
    }

    private VariableDefinitionNode createBLangVarDef(Location location,
                                                TypedBindingPatternNode typedBindingPattern,
                                                Optional<io.ballerina.compiler.syntax.tree.ExpressionNode> initializer,
                                                Optional<Token> finalKeyword) {
        BindingPatternNode bindingPattern = typedBindingPattern.bindingPattern();
        BLangVariable variable = getBLangVariableNode(bindingPattern, location);
        List<Token> qualifiers = new ArrayList<>();

        if (finalKeyword.isPresent()) {
            qualifiers.add(finalKeyword.get());
        }
        NodeList<Token> qualifierList =  NodeFactory.createNodeList(qualifiers);

        switch (bindingPattern.kind()) {
            case CAPTURE_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                BLangSimpleVariableDef bLVarDef =
                        (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
                bLVarDef.pos = variable.pos = location;
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
                initializeBLangVariable(variable, typedBindingPattern.typeDescriptor(), initializer, qualifierList);
                return createRecordVariableDef(variable, location);
            case LIST_BINDING_PATTERN:
                initializeBLangVariable(variable, typedBindingPattern.typeDescriptor(), initializer, qualifierList);
                return createTupleVariableDef(variable);
            case ERROR_BINDING_PATTERN:
                initializeBLangVariable(variable, typedBindingPattern.typeDescriptor(), initializer, qualifierList);
                return createErrorVariableDef(variable);
            default:
                throw new RuntimeException(
                        "Syntax kind is not a valid binding pattern " + typedBindingPattern.bindingPattern().kind());
        }
    }

    private void initializeBLangVariable(BLangVariable var, TypeDescriptorNode type,
                                         Optional<io.ballerina.compiler.syntax.tree.ExpressionNode> initializer,
                                         NodeList<Token> qualifiers) {

        for (Token qualifier : qualifiers) {
            SyntaxKind kind = qualifier.kind();
            if (kind == SyntaxKind.FINAL_KEYWORD) {
                markVariableWithFlag(var, Flag.FINAL);
            } else if (qualifier.kind() == SyntaxKind.CONFIGURABLE_KEYWORD) {
                var.flagSet.add(Flag.CONFIGURABLE);
                var.flagSet.add(Flag.FINAL);
                // Initializer is always present for configurable, hence get directly
                if (initializer.get().kind() == SyntaxKind.REQUIRED_EXPRESSION) {
                    var.flagSet.add(Flag.REQUIRED);
                    initializer = Optional.empty();
                }
            } else if (kind == SyntaxKind.ISOLATED_KEYWORD) {
                var.flagSet.add(Flag.ISOLATED);
            }
        }

        boolean errorBindingPatternWithTypeRef = var.getKind() == NodeKind.ERROR_VARIABLE && var.typeNode != null;
        var.isDeclaredWithVar = isDeclaredWithVar(type);
        if (!errorBindingPatternWithTypeRef && !var.isDeclaredWithVar) {
            var.setTypeNode(createTypeNode(type));
        }

        if (initializer.isPresent()) {
            var.setInitialExpression(createExpression(initializer.get()));
        }
    }

    private String getVariableName(BLangVariable variable) {
        NodeKind kind = variable.getKind();
        StringBuilder name = new StringBuilder(DOLLAR);
        switch (kind) {
            case VARIABLE:
                name.append(((BLangSimpleVariable) variable).name.value).append(DOLLAR);
                return name.toString();
            case RECORD_VARIABLE:
                for (BLangRecordVariableKeyValue keyValue : ((BLangRecordVariable) variable).getVariables()) {
                    name.append(keyValue.key.value).append(DOLLAR);
                }
                return name.toString();
            case TUPLE_VARIABLE:
                for (BLangVariable var : ((BLangTupleVariable) variable).memberVariables) {
                    name.append(getVariableName(var)).append(DOLLAR);
                }
                return name.toString();
            case ERROR_VARIABLE:
            default:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                BLangSimpleVariable message = errorVariable.message;
                if (message != null) {
                    name.append(message.name.value).append(DOLLAR);
                }
                BLangVariable cause = errorVariable.cause;
                if (cause != null) {
                    name.append(getVariableName(cause)).append(DOLLAR);
                }
                BLangSimpleVariable restDetail = errorVariable.restDetail;
                if (restDetail != null) {
                    name.append(restDetail.name.value).append(DOLLAR);
                }
                for (BLangErrorVariable.BLangErrorDetailEntry detailEntry : errorVariable.detail) {
                    name.append(detailEntry.key.value);
                }
                return name.toString();
        }
    }

    private BLangRecordVariableDef createRecordVariableDef(BLangVariable var, Location nodePos) {

        BLangRecordVariableDef varDefNode = (BLangRecordVariableDef) TreeBuilder.createRecordVariableDefinitionNode();
        varDefNode.pos = nodePos;
        varDefNode.setVariable(var);
        return varDefNode;
    }

    private BLangTupleVariableDef createTupleVariableDef(BLangVariable tupleVar) {

        BLangTupleVariableDef varDefNode = (BLangTupleVariableDef) TreeBuilder.createTupleVariableDefinitionNode();
        varDefNode.pos = tupleVar.pos;
        varDefNode.setVariable(tupleVar);
        return varDefNode;
    }

    private BLangErrorVariableDef createErrorVariableDef(BLangVariable errorVar) {

        BLangErrorVariableDef varDefNode = (BLangErrorVariableDef) TreeBuilder.createErrorVariableDefinitionNode();
        varDefNode.pos = errorVar.pos;
        varDefNode.setVariable(errorVar);
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
            actionInvocation.pos = getPosition(startActionNode);
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
        varArgs.pos = getPosition(restArgumentNode);
        varArgs.expr = createExpression(restArgumentNode.expression());
        return varArgs;
    }

    @Override
    public BLangNode transform(RequiredParameterNode requiredParameter) {
        Optional<Token> paramName = requiredParameter.paramName();
        paramName.ifPresent(token -> this.anonTypeNameSuffixes.push(token.text()));
        BLangSimpleVariable simpleVar = createSimpleVar(paramName,
                                                        requiredParameter.typeName(), requiredParameter.annotations());
        simpleVar.pos = getPosition(requiredParameter);
        if (paramName.isPresent()) {
            simpleVar.name.pos = getPosition(paramName.get());
            this.anonTypeNameSuffixes.pop();
        } else if (simpleVar.name.pos == null) {
            // Param doesn't have a name and also is not a missing node
            // Therefore, assigning the built-in location
            simpleVar.name.pos = symTable.builtinPos;
        }
        simpleVar.flagSet.add(Flag.REQUIRED_PARAM);
        return simpleVar;
    }

    @Override
    public BLangNode transform(IncludedRecordParameterNode includedRecordParameterNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(includedRecordParameterNode.paramName(),
                includedRecordParameterNode.typeName(), includedRecordParameterNode.annotations());
        simpleVar.flagSet.add(INCLUDED);
        simpleVar.pos = getPosition(includedRecordParameterNode.typeName(), includedRecordParameterNode);
        if (includedRecordParameterNode.paramName().isPresent()) {
            simpleVar.name.pos = getPosition(includedRecordParameterNode.paramName().get());
        }
        return simpleVar;
    }

    @Override
    public BLangNode transform(DefaultableParameterNode defaultableParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(defaultableParameter.paramName(),
                                                        defaultableParameter.typeName(),
                                                        defaultableParameter.annotations());

        simpleVar.setInitialExpression(createExpression(defaultableParameter.expression()));
        simpleVar.flagSet.add(Flag.DEFAULTABLE_PARAM);
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
        bLArrayType.elemtype.pos = bLArrayType.pos;

        bLSimpleVar.flagSet.add(Flag.REST_PARAM);
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
        functionTypeNode.inTypeDefinitionContext = isInTypeDefinitionContext(functionTypeDescriptorNode.parent());

        if (functionTypeDescriptorNode.functionSignature().isPresent()) {
            FunctionSignatureNode funcSignature = functionTypeDescriptorNode.functionSignature().get();

            // Set Parameters
            for (ParameterNode child : funcSignature.parameters()) {
                SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
                if (child.kind() == SyntaxKind.REST_PARAM) {
                    functionTypeNode.restParam = (BLangSimpleVariable) param;
                } else {
                    functionTypeNode.params.add((BLangSimpleVariable) param);
                }
            }

            // Set Return Type
            Optional<ReturnTypeDescriptorNode> retNode = funcSignature.returnTypeDesc();
            if (retNode.isPresent()) {
                ReturnTypeDescriptorNode returnType = retNode.get();
                functionTypeNode.returnTypeNode = createTypeNode(returnType.type());
            } else {
                BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
                bLValueType.pos = symTable.builtinPos;
                bLValueType.typeKind = TypeKind.NIL;
                functionTypeNode.returnTypeNode = bLValueType;
            }
        } else {
            functionTypeNode.flagSet.add(Flag.ANY_FUNCTION);
        }

        functionTypeNode.flagSet.add(Flag.PUBLIC);

        for (Token token : functionTypeDescriptorNode.qualifierList()) {
            if (token.kind() == SyntaxKind.ISOLATED_KEYWORD) {
                functionTypeNode.flagSet.add(Flag.ISOLATED);
            } else if (token.kind() == SyntaxKind.TRANSACTIONAL_KEYWORD) {
                functionTypeNode.flagSet.add(Flag.TRANSACTIONAL);
            }
        }

        return functionTypeNode;
    }

    @Override
    public BLangNode transform(MapTypeDescriptorNode mapTypeDescNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TypeKind.MAP;
        refType.pos = getPosition(mapTypeDescNode);

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = createTypeNode(mapTypeDescNode.mapTypeParamsNode().typeNode());
        constrainedType.pos = refType.pos;
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

        tableTypeNode.isTypeInlineDefined = checkIfAnonymous(tableTypeDescriptorNode);
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
        Location pos = getPosition(xmlComment);

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
            if (node.kind() == SyntaxKind.XML_CDATA) {
                XMLCDATANode xmlcdataNode = (XMLCDATANode) node;
                for (Node characterData : xmlcdataNode.content()) {
                    xmlElement.children.add(createExpression(characterData));
                }
            } else {
                xmlElement.children.add(createExpression(node));
            }
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
        literal.setBType(symTable.getTypeFromTag(TypeTags.BYTE_ARRAY));
        literal.getBType().tag = TypeTags.BYTE_ARRAY;
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
                if (value.kind() == SyntaxKind.XML_TEXT_CONTENT) {
                    Token token = (Token) value;
                    String normalizedValue = XmlFactory.XMLTextUnescape.unescape(token.text());
                    quotedString.textFragments.add(createStringLiteral(normalizedValue, getPosition(value)));
                } else {
                    quotedString.textFragments.add(createExpression(value));
                }
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

    private BLangNode createXMLEmptyLiteral(Node expressionNode) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.pos = getPosition(expressionNode);
        xmlTextLiteral.textFragments.add(createEmptyStringLiteral(xmlTextLiteral.pos));
        return xmlTextLiteral;
    }

    private BLangNode createXMLTextLiteral(List<Node> expressionNode) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.pos = getPosition(expressionNode.get(0), expressionNode.get(expressionNode.size() - 1));
        for (Node node : expressionNode) {
            xmlTextLiteral.textFragments.add(createExpression(node));
        }
        xmlTextLiteral.textFragments.add(createEmptyStringLiteral(xmlTextLiteral.pos));
        return xmlTextLiteral;
    }

    private BLangNode createXMLTextLiteral(Node expressionNode) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.pos = getPosition(expressionNode);
        xmlTextLiteral.textFragments.add(createExpression(expressionNode));
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
        Token xmlSimpleName = xmlSimpleNameNode.name();
        xmlName.localname = createIdentifier(getPosition(xmlSimpleName), xmlSimpleName, true);
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

        BLangIdentifier[] nameReference = createBLangNameReference(remoteMethodCallActionNode.methodName().name());
        bLangActionInvocation.pkgAlias = nameReference[0];
        bLangActionInvocation.name = nameReference[1];
        bLangActionInvocation.pos = getPosition(remoteMethodCallActionNode);
        return bLangActionInvocation;
    }

    @Override
    public BLangNode transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        BLangType constraint, error = null;
        Location pos = getPosition(streamTypeDescriptorNode);
        Optional<Node> paramsNode = streamTypeDescriptorNode.streamTypeParamsNode();

        BLangStreamType streamType = (BLangStreamType) TreeBuilder.createStreamTypeNode();
        if (!paramsNode.isPresent()) {
            constraint = getbLangUnionTypeNode(pos, TypeKind.ANY, TypeKind.ERROR);
            error = getbLangUnionTypeNode(pos, TypeKind.NIL, TypeKind.ERROR);
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

        streamType.type = refType;
        streamType.constraint = constraint;
        streamType.error = error;
        streamType.pos = pos;

        return streamType;
    }

    private BLangUnionTypeNode getbLangUnionTypeNode(Location pos, TypeKind... typeKinds) {
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.pos = pos;
        for (TypeKind kind : typeKinds) {
            unionTypeNode.memberTypeNodes.add(addValueType(pos, kind));
        }
        return unionTypeNode;
    }

    @Override
    public BLangNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        Location position = getPosition(arrayTypeDescriptorNode);
        NodeList<ArrayDimensionNode> dimensionNodes = arrayTypeDescriptorNode.dimensions();
        int dimensionSize = dimensionNodes.size();
        List<BLangExpression> sizes = new ArrayList<>(dimensionSize);

        for (int i = dimensionSize - 1; i >= 0; i--) {
            ArrayDimensionNode dimensionNode = dimensionNodes.get(i);
            if (dimensionNode.arrayLength().isEmpty()) {
                sizes.add(new BLangLiteral(OPEN_ARRAY_INDICATOR, symTable.intType));
            } else {
                Node keyExpr = dimensionNode.arrayLength().get();
                if (keyExpr.kind() == SyntaxKind.NUMERIC_LITERAL) {
                    int length = 0;
                    long lengthCheck = 0;
                    Token literalToken = ((BasicLiteralNode) keyExpr).literalToken();

                    try {
                        if (literalToken.kind() == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
                            lengthCheck = Long.parseLong(literalToken.text());
                        } else {
                            lengthCheck = Long.parseLong(literalToken.text().substring(2), 16);
                        }
                    } catch (NumberFormatException e) {
                        dlog.error(literalToken.location(), DiagnosticErrorCode.INVALID_ARRAY_LENGTH);
                    }

                    if (lengthCheck > MAX_ARRAY_SIZE) {
                        dlog.error(literalToken.location(),
                                DiagnosticErrorCode.ARRAY_LENGTH_GREATER_THAT_2147483637_NOT_YET_SUPPORTED);
                    } else {
                        length = (int) lengthCheck;
                    }
                    sizes.add(new BLangLiteral(length, symTable.intType));
                } else if (keyExpr.kind() == SyntaxKind.ASTERISK_LITERAL) {
                    sizes.add(new BLangLiteral(INFERRED_ARRAY_INDICATOR, symTable.intType));
                } else {
                    sizes.add(createExpression(keyExpr));
                }
            }
        }

        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.pos = position;
        arrayTypeNode.elemtype = createTypeNode(arrayTypeDescriptorNode.memberTypeDesc());
        arrayTypeNode.dimensions = dimensionSize;
        arrayTypeNode.sizes = sizes;
        return arrayTypeNode;
    }

    public BLangNode transform(EnumDeclarationNode enumDeclarationNode) {
        Boolean publicQualifier = false;
        if (enumDeclarationNode.qualifier().isPresent() && enumDeclarationNode.qualifier().get().kind()
                == SyntaxKind.PUBLIC_KEYWORD) {
            publicQualifier = true;
        }
        for (Node member : enumDeclarationNode.enumMemberList()) {
            EnumMemberNode enumMember = (EnumMemberNode) member;
            if (enumMember.identifier().isMissing()) {
                continue;
            }
            addToTop(transformEnumMember(enumMember, publicQualifier));
        }

        BLangTypeDefinition bLangTypeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        if (publicQualifier) {
            bLangTypeDefinition.flagSet.add(Flag.PUBLIC);
        }
        bLangTypeDefinition.flagSet.add(Flag.ENUM);

        bLangTypeDefinition.setName((BLangIdentifier) transform(enumDeclarationNode.identifier()));
        bLangTypeDefinition.pos = getPosition(enumDeclarationNode);

        BLangUnionTypeNode bLangUnionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        bLangUnionTypeNode.pos = bLangTypeDefinition.pos;
        for (Node member : enumDeclarationNode.enumMemberList()) {
            Node enumMemberIdentifier = ((EnumMemberNode) member).identifier();
            if (enumMemberIdentifier.isMissing()) {
                continue;
            }
            bLangUnionTypeNode.memberTypeNodes.add(createTypeNode(enumMemberIdentifier));
        }
        Collections.reverse(bLangUnionTypeNode.memberTypeNodes);
        bLangTypeDefinition.setTypeNode(bLangUnionTypeNode);

        bLangTypeDefinition.annAttachments = applyAll(getAnnotations(enumDeclarationNode.metadata()));
        bLangTypeDefinition.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(enumDeclarationNode.metadata()));
        return bLangTypeDefinition;
    }

    public BLangConstant transformEnumMember(EnumMemberNode member, Boolean publicQualifier) {
        BLangConstant bLangConstant = (BLangConstant) TreeBuilder.createConstantNode();
        bLangConstant.pos = getPosition(member);
        bLangConstant.flagSet.add(Flag.CONSTANT);
        bLangConstant.flagSet.add(Flag.ENUM_MEMBER);
        if (publicQualifier) {
            bLangConstant.flagSet.add(Flag.PUBLIC);
        }

        bLangConstant.annAttachments = applyAll(getAnnotations(member.metadata()));
        bLangConstant.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(member.metadata()));

        BLangIdentifier memberName = (BLangIdentifier) transform(member.identifier());
        bLangConstant.setName(memberName);
        this.anonTypeNameSuffixes.push(memberName.value);

        BLangExpression deepLiteral;
        if (member.constExprNode().isPresent()) {
            BLangExpression expression = createExpression(member.constExprNode().orElse(null));
            bLangConstant.setInitialExpression(expression);
            deepLiteral = createExpression(member.constExprNode().orElse(null));
        } else {
            BLangLiteral literal = createSimpleLiteral(member.identifier());
            bLangConstant.setInitialExpression(literal);
            deepLiteral = createSimpleLiteral(member.identifier());
        }

        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = symTable.builtinPos;
        typeNode.typeKind = TypeKind.STRING;
        bLangConstant.setTypeNode(typeNode);

        if (deepLiteral instanceof BLangLiteral) {
            BLangLiteral literal = (BLangLiteral) deepLiteral;
            if (!literal.originalValue.equals("")) {
                BLangFiniteTypeNode typeNodeAssociated = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
                literal.originalValue = null;
                typeNodeAssociated.addValue(deepLiteral);
                bLangConstant.associatedTypeDefinition = createTypeDefinitionWithTypeNode(typeNodeAssociated);
            } else {
                bLangConstant.associatedTypeDefinition = null;
            }
        } else {
            BLangFiniteTypeNode typeNodeAssociated = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
            typeNodeAssociated.addValue(deepLiteral);
            bLangConstant.associatedTypeDefinition = createTypeDefinitionWithTypeNode(typeNodeAssociated);
        }
        this.anonTypeNameSuffixes.pop();
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
        boolean isMap = false;

        Optional<QueryConstructTypeNode> optionalQueryConstructTypeNode = queryExprNode.queryConstructType();
        if (optionalQueryConstructTypeNode.isPresent()) {
            QueryConstructTypeNode queryConstructTypeNode = optionalQueryConstructTypeNode.get();
            isTable = queryConstructTypeNode.keyword().kind() == SyntaxKind.TABLE_KEYWORD;
            isStream = queryConstructTypeNode.keyword().kind() == SyntaxKind.STREAM_KEYWORD;
            isMap = queryConstructTypeNode.keyword().kind() == SyntaxKind.MAP_KEYWORD;
            if (queryConstructTypeNode.keySpecifier().isPresent()) {
                for (IdentifierToken fieldNameNode : queryConstructTypeNode.keySpecifier().get().fieldNames()) {
                    queryExpr.fieldNameIdentifierList.add(createIdentifier(getPosition(fieldNameNode), fieldNameNode));
                }
            }
        }
        queryExpr.isStream = isStream;
        queryExpr.isTable = isTable;
        queryExpr.isMap = isMap;
        return queryExpr;
    }

    public BLangNode transform(OnFailClauseNode onFailClauseNode) {
        Location pos = getPosition(onFailClauseNode);
        BLangOnFailClause onFailClause = (BLangOnFailClause) TreeBuilder.createOnFailClauseNode();
        onFailClause.pos = pos;
        onFailClauseNode.typeDescriptor().ifPresent(typeDescriptorNode -> {
            BLangSimpleVariableDef variableDefinitionNode =
                    (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
            BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            boolean isDeclaredWithVar = typeDescriptorNode.kind() == SyntaxKind.VAR_TYPE_DESC;
            var.isDeclaredWithVar = isDeclaredWithVar;
            if (!isDeclaredWithVar) {
                var.setTypeNode(createTypeNode(typeDescriptorNode));
            }
            var.pos = pos;
            onFailClauseNode.failErrorName().ifPresent(identifierToken -> {
                var.setName(this.createIdentifier(identifierToken));
                var.name.pos = getPosition(identifierToken);
                variableDefinitionNode.setVariable(var);
                variableDefinitionNode.pos = getPosition(typeDescriptorNode,
                        identifierToken);
            });
            onFailClause.isDeclaredWithVar = isDeclaredWithVar;
            markVariableWithFlag(variableDefinitionNode.getVariable(), Flag.FINAL);
            onFailClause.variableDefinitionNode = variableDefinitionNode;
        });
        BLangBlockStmt blockNode = (BLangBlockStmt) transform(onFailClauseNode.blockStatement());
        blockNode.pos = pos;
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
        joinClause.variableDefinitionNode = createBLangVarDef(getPosition(typedBindingPattern),
                                                              typedBindingPattern, Optional.empty(), Optional.empty());
        joinClause.collection = createExpression(joinClauseNode.expression());
        joinClause.isDeclaredWithVar = typedBindingPattern.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC;
        joinClause.isOuterJoin = joinClauseNode.outerKeyword().isPresent();

        OnClauseNode onClauseNode = joinClauseNode.joinOnCondition();
        BLangOnClause onClause = (BLangOnClause) TreeBuilder.createOnClauseNode();
        onClause.pos = getPosition(onClauseNode);
        if (!onClauseNode.equalsKeyword().isMissing()) {
            onClause.equalsKeywordPos = getPosition(onClauseNode.equalsKeyword());
        }
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
    public BLangNode transform(InferredTypedescDefaultNode inferDefaultValueNode) {
        BLangInferredTypedescDefaultNode inferTypedescExpr =
                (BLangInferredTypedescDefaultNode) TreeBuilder.createInferTypedescExpressionNode();
        inferTypedescExpr.pos = getPosition(inferDefaultValueNode);
        return inferTypedescExpr;
    }

    @Override
    protected BLangNode transformSyntaxNode(Node node) {
        // TODO: Remove this RuntimeException once all nodes covered
        throw new RuntimeException("Node not supported: " + node.getClass().getSimpleName());
    }

    @Override
    public BLangNode transform(ServiceDeclarationNode serviceDeclarationNode) {
        Location pos = getPositionWithoutMetadata(serviceDeclarationNode);
        BLangClassDefinition anonClassDef = transformObjectCtorExpressionBody(serviceDeclarationNode.members());
        anonClassDef.isServiceDecl = true;
        anonClassDef.isObjectContructorDecl = false;
        anonClassDef.pos = pos;
        anonClassDef.flagSet.add(SERVICE);

        setClassQualifiers(serviceDeclarationNode.qualifiers(), anonClassDef);

        List<IdentifierNode> absResourcePathPath = new ArrayList<>();
        NodeList<Node> pathList = serviceDeclarationNode.absoluteResourcePath();
        BLangLiteral serviceNameLiteral = null;
        if (pathList.size() == 1 && pathList.get(0).kind() == SyntaxKind.STRING_LITERAL) {
            serviceNameLiteral = (BLangLiteral) createExpression(pathList.get(0));
        } else {
            for (var token : pathList) {
                String text = ((Token) token).text();
                // if it's a single '/' then add, else ignore '/' chars and only add other pieces.
                if (pathList.size() == 1 && text.equals("/")) {
                    absResourcePathPath.add(createIdentifier((Token) token));
                } else if (!text.equals("/")) {
                    absResourcePathPath.add(createIdentifier((Token) token));
                }
            }
        }

        // Generate a name for the anonymous class
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(packageID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        anonClassDef.setName(anonTypeGenName);
        anonClassDef.flagSet.add(Flag.PUBLIC);

        Optional<TypeDescriptorNode> typeReference = serviceDeclarationNode.typeDescriptor();
        typeReference.ifPresent(typeReferenceNode -> {
            BLangType typeNode = createTypeNode(typeReferenceNode);
            anonClassDef.typeRefs.add(typeNode);
        });

        anonClassDef.annAttachments = applyAll(getAnnotations(serviceDeclarationNode.metadata()));
        anonClassDef.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(getDocumentationString(serviceDeclarationNode.metadata()));

        addToTop(anonClassDef);
        anonClassDef.oceEnvData.originalClass = anonClassDef;

        BLangIdentifier identifier = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        BLangUserDefinedType userDefinedType = createUserDefinedType(pos, identifier, anonClassDef.name);

        BLangTypeInit initNode = (BLangTypeInit) TreeBuilder.createInitNode();
        initNode.pos = pos;
        initNode.userDefinedType = userDefinedType;

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        BLangIdentifier pkgAlias = createIdentifier(pos, "");
        invocationNode.name = createIdentifier(pos, genName);
        invocationNode.pkgAlias = pkgAlias;

        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;

        BLangSimpleVariable serviceVariable = createServiceVariable(pos, anonClassDef, initNode);

        List<BLangExpression> exprs = new ArrayList<>();
        for (var exp : serviceDeclarationNode.expressions()) {
            exprs.add(createExpression(exp));
        }

        BLangService service = (BLangService) TreeBuilder.createServiceNode();
        service.serviceVariable = serviceVariable;
        service.attachedExprs = exprs;
        service.serviceClass = anonClassDef;
        service.absoluteResourcePath = absResourcePathPath;
        service.serviceNameLiteral = serviceNameLiteral;
        service.annAttachments = anonClassDef.annAttachments;
        service.pos = pos;
        service.name = createIdentifier(pos, anonymousModelHelper.getNextAnonymousServiceVarKey(packageID));
        return service;
    }

    private BLangSimpleVariable createServiceVariable(Location pos, BLangClassDefinition annonClassDef,
                                                         BLangTypeInit initNode) {
        BLangUserDefinedType typeName = createUserDefinedType(pos,
                (BLangIdentifier) TreeBuilder.createIdentifierNode(), annonClassDef.name);

        BLangSimpleVariable serviceInstance =
                (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        serviceInstance.typeNode = typeName;

        String serviceVarName = anonymousModelHelper.getNextAnonymousServiceVarKey(packageID);
        serviceInstance.name =  createIdentifier(pos, serviceVarName);

        serviceInstance.expr = initNode;
        serviceInstance.internal = true;

        return serviceInstance;
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

        setClassQualifiers(classDefinitionNode.classTypeQualifiers(), blangClass);

        NodeList<Node> members = classDefinitionNode.members();
        for (Node node : members) {
            // TODO: Check for fields other than SimpleVariableNode
            BLangNode bLangNode = node.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION || bLangNode.getKind() == NodeKind.RESOURCE_FUNC) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                bLangFunction.flagSet.add(Flag.ATTACHED);
                if (!Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    blangClass.addFunction(bLangFunction);
                    continue;
                }
                if (blangClass.initFunction != null) {
                    blangClass.addFunction(bLangFunction);
                    continue;
                }
                bLangFunction.objInitFunction = true;
                blangClass.initFunction = bLangFunction;
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
        Location pos = getPosition(retryStatementNode);
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
            // If type param is present, retry spec spans from type param to args
            if (retryStatementNode.typeParameter().isPresent()) {
                retrySpec.pos = getPosition(retryStatementNode.typeParameter().get(), arg);
            } else {
                retrySpec.pos = getPosition(arg);
            }
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
        BLangXMLElementAccess elementAccess = new BLangXMLElementAccess(getPosition(xmlFilterExpressionNode),
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
                new BLangXMLNavigationAccess(getPosition(xmlStepExpressionNode), expr, filters,
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
                bLangMatchGuard.pos = getPosition(matchClauseNode.matchGuard().get());
                bLangMatchClause.setMatchGuard(bLangMatchGuard);
            }

            for (Node matchPattern : matchClauseNode.matchPatterns()) {
                BLangMatchPattern bLangMatchPattern = transformMatchPattern(matchPattern);
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
        matchStatement.pos = getPosition(matchStatementNode);
        return matchStatement;
    }
    
    @Override
    public BLangNode transform(ClientResourceAccessActionNode clientResourceAccessActionNode) {
        BLangInvocation.BLangResourceAccessInvocation resourceInvocation = TreeBuilder.createResourceAccessInvocation();
        resourceInvocation.pos = getPosition(clientResourceAccessActionNode);
        resourceInvocation.expr = createExpression(clientResourceAccessActionNode.expression());

        SeparatedNodeList<Node> resourceAccessPath = clientResourceAccessActionNode.resourceAccessPath();
        List<BLangExpression> pathSegments = new ArrayList<>(resourceAccessPath.size());
        for (Node resourceAccessSegment : resourceAccessPath) {
            switch (resourceAccessSegment.kind()) {
                case COMPUTED_RESOURCE_ACCESS_SEGMENT:
                    pathSegments.add(
                            createExpression(((ComputedResourceAccessSegmentNode) resourceAccessSegment).expression()));
                    break;
                case RESOURCE_ACCESS_REST_SEGMENT:
                    ResourceAccessRestSegmentNode resourceAccessRestSegment =
                            (ResourceAccessRestSegmentNode) resourceAccessSegment;
                    BLangExpression spreadMemberExpr = createSpreadMemberExpr(resourceAccessRestSegment.expression(),
                            getPosition(resourceAccessRestSegment));
                    pathSegments.add(spreadMemberExpr);
                    break;
                default:
                    pathSegments.add(createSimpleLiteral(resourceAccessSegment));
            }
        }

        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        listConstructorExpr.exprs = pathSegments;
        if (pathSegments.size() == 0) {
            listConstructorExpr.pos = getPosition(clientResourceAccessActionNode.slashToken());
        } else {
            listConstructorExpr.pos = 
                    getPosition(clientResourceAccessActionNode.slashToken(), 
                            resourceAccessPath.get(pathSegments.size() - 1));
        }
        
        resourceInvocation.resourceAccessPathSegments = listConstructorExpr;
        
        if (clientResourceAccessActionNode.methodName().isPresent()) {
            resourceInvocation.name = createIdentifier(clientResourceAccessActionNode.methodName().get().name());
        } else {
            resourceInvocation.name = createIdentifier(resourceInvocation.pos, "get");
        }

        if (clientResourceAccessActionNode.arguments().isPresent()) {
            resourceInvocation.argExprs = applyAll(clientResourceAccessActionNode.arguments().get().arguments());
        }
        
        if (clientResourceAccessActionNode.expression().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode iNode = (QualifiedNameReferenceNode) clientResourceAccessActionNode.expression();
            Token modulePrefix = iNode.modulePrefix();
            resourceInvocation.pkgAlias = this.createIdentifier(getPosition(modulePrefix), modulePrefix);
        } else {
            resourceInvocation.pkgAlias = this.createIdentifier(symTable.builtinPos, "");
        }
        
        return resourceInvocation;
    }

    private BLangXMLSequenceLiteral createXmlSequence(TemplateExpressionNode expressionNode) {
        BLangXMLSequenceLiteral xmlSequenceLiteral = (BLangXMLSequenceLiteral)
                TreeBuilder.createXMLSequenceLiteralNode();
        xmlSequenceLiteral.pos = getPosition(expressionNode);

        Node lastNode = null;
        List<Node> adjacentTextNodes = new ArrayList<>();
        int xmlContentSize = expressionNode.content().size();

        for (int index = 0; index < xmlContentSize; index++) {
            Node childItem = expressionNode.content().get(index);
            if (childItem.kind() == SyntaxKind.XML_TEXT || childItem.kind() == SyntaxKind.INTERPOLATION) {
                adjacentTextNodes.add(childItem);
                lastNode = childItem;
                if (index != xmlContentSize - 1) {
                    continue;
                }
            }
            // Handle previous node if it was of xml:Text or interpolation type
            if (lastNode != null && (lastNode.kind() == SyntaxKind.XML_TEXT ||
                    lastNode.kind() == SyntaxKind.INTERPOLATION)) {
                if (adjacentTextNodes.size() > 1) {
                    // Adjacent XML Text Literals (contains interpolated items and xml:Text items) should be
                    // concatenated together
                    xmlSequenceLiteral.xmlItems.add((BLangExpression) createXMLTextLiteral(adjacentTextNodes));
                } else {
                    xmlSequenceLiteral.xmlItems.add(createXmlSingletonItem(lastNode));
                }
                adjacentTextNodes.clear();
                // Identify if sequence ends with xml:Text type or interpolation type
                if (lastNode.kind() == childItem.kind()) {
                    continue;
                }
            }
            // Handle current node that is not of xml:Text type or interpolation type
            xmlSequenceLiteral.xmlItems.add(createXmlSingletonItem(childItem));
            lastNode = childItem;
        }
        return xmlSequenceLiteral;
    }

    public BLangExpression createXmlSingletonItem(Node xmlTypeNode) {
        switch (xmlTypeNode.kind()) {
            case XML_COMMENT:
            case XML_PI:
            case XML_ELEMENT:
            case XML_EMPTY_ELEMENT:
                return createExpression(xmlTypeNode);
            case XML_CDATA:
                NodeList<Node> cdataContent = ((XMLCDATANode) xmlTypeNode).content();
                if (cdataContent.size() == 0) {
                    return (BLangExpression) createXMLEmptyLiteral(xmlTypeNode);
                }

                List<Node> characterDataList = new ArrayList<>();
                for (Node item : cdataContent) {
                    characterDataList.add(item);
                }
                return (BLangExpression) createXMLTextLiteral(characterDataList);
            default:
                return (BLangExpression) createXMLTextLiteral(xmlTypeNode);
        }
    }

    public BLangNode createXmlTemplateLiteral(TemplateExpressionNode expressionNode) {
        if (expressionNode.content().isEmpty()) {
            return createXMLEmptyLiteral(expressionNode);
        }
        if (expressionNode.content().size() == 1) {
            return createXmlSingletonItem(expressionNode.content().get(0));
        }
        return createXmlSequence(expressionNode);
    }

    private BLangMatchPattern transformMatchPattern(Node matchPattern) {
        Location matchPatternPos = matchPattern.location();
        SyntaxKind kind = matchPattern.kind();

        if (kind == SyntaxKind.SIMPLE_NAME_REFERENCE &&
                ((SimpleNameReferenceNode) matchPattern).name().text().equals("_")) {
            // wildcard match
            BLangWildCardMatchPattern bLangWildCardMatchPattern =
                    (BLangWildCardMatchPattern) TreeBuilder.createWildCardMatchPattern();
            bLangWildCardMatchPattern.pos = matchPatternPos;
            return bLangWildCardMatchPattern;
        }

        if (kind == SyntaxKind.IDENTIFIER_TOKEN && ((IdentifierToken) matchPattern).text().equals("_")) {
            BLangWildCardMatchPattern bLangWildCardMatchPattern =
                    (BLangWildCardMatchPattern) TreeBuilder.createWildCardMatchPattern();
            bLangWildCardMatchPattern.pos = matchPatternPos;
            return bLangWildCardMatchPattern;
        }

        if (kind == SyntaxKind.TYPED_BINDING_PATTERN) { // var a
            TypedBindingPatternNode typedBindingPatternNode = (TypedBindingPatternNode) matchPattern;
            BLangVarBindingPatternMatchPattern bLangVarBindingPattern =
                    (BLangVarBindingPatternMatchPattern) TreeBuilder.createVarBindingPattern();
            bLangVarBindingPattern.pos = matchPatternPos;
            bLangVarBindingPattern.setBindingPattern(transformBindingPattern(typedBindingPatternNode.bindingPattern()));
            return bLangVarBindingPattern;
        }

        if (kind == SyntaxKind.ERROR_MATCH_PATTERN) {
            return transformErrorMatchPattern((ErrorMatchPatternNode) matchPattern, matchPatternPos);
        }

        if (kind == SyntaxKind.NAMED_ARG_MATCH_PATTERN) {
            return transformNamedArgMatchPattern((NamedArgMatchPatternNode) matchPattern, matchPatternPos);
        }

        if (kind == SyntaxKind.LIST_MATCH_PATTERN) {
            return transformListMatchPattern((ListMatchPatternNode) matchPattern, matchPatternPos);
        }

        if (kind == SyntaxKind.REST_MATCH_PATTERN) {
            return transformRestMatchPattern((RestMatchPatternNode) matchPattern, matchPatternPos);
        }

        if (kind == SyntaxKind.MAPPING_MATCH_PATTERN) {
            return transformMappingMatchPattern((MappingMatchPatternNode) matchPattern, matchPatternPos);
        }

        if (kind == SyntaxKind.FIELD_MATCH_PATTERN) {
            return transformFieldMatchPattern((FieldMatchPatternNode) matchPattern, matchPatternPos);
        }

        // We reach here for simple-const-expr
        assert (kind == SyntaxKind.NUMERIC_LITERAL ||
                kind == SyntaxKind.STRING_LITERAL ||
                kind == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                kind == SyntaxKind.QUALIFIED_NAME_REFERENCE ||
                kind == SyntaxKind.IDENTIFIER_TOKEN ||
                kind == SyntaxKind.NULL_LITERAL ||
                kind == SyntaxKind.NIL_LITERAL ||
                kind == SyntaxKind.BOOLEAN_LITERAL ||
                // [Sign] int/float -> unary-expr
                kind == SyntaxKind.UNARY_EXPRESSION);

        BLangConstPattern bLangConstMatchPattern = (BLangConstPattern) TreeBuilder.createConstMatchPattern();
        bLangConstMatchPattern.setExpression(createExpression(matchPattern));
        bLangConstMatchPattern.pos = matchPatternPos;
        return bLangConstMatchPattern;
    }

    private BLangErrorMatchPattern transformErrorMatchPattern(ErrorMatchPatternNode errorMatchPatternNode,
                                                              Location pos) {
        BLangErrorMatchPattern bLangErrorMatchPattern =
                (BLangErrorMatchPattern) TreeBuilder.createErrorMatchPattern();
        bLangErrorMatchPattern.pos = pos;

        NameReferenceNode nameReferenceNode;
        if (errorMatchPatternNode.typeReference().isPresent()) {
            nameReferenceNode = errorMatchPatternNode.typeReference().get();
            bLangErrorMatchPattern.errorTypeReference = (BLangUserDefinedType) createTypeNode(nameReferenceNode);
        }

        if (errorMatchPatternNode.argListMatchPatternNode().size() == 0) {
            return bLangErrorMatchPattern;
        }

        Node node = errorMatchPatternNode.argListMatchPatternNode().get(0);
        if (isErrorFieldMatchPattern(node)) {
            createErrorFieldMatchPatterns(0, errorMatchPatternNode, bLangErrorMatchPattern);
            return bLangErrorMatchPattern;
        }

        bLangErrorMatchPattern.errorMessageMatchPattern = createErrorMessageMatchPattern(node);
        if (errorMatchPatternNode.argListMatchPatternNode().size() == 1) {
            return bLangErrorMatchPattern;
        }

        node = errorMatchPatternNode.argListMatchPatternNode().get(1);
        if (isErrorFieldMatchPattern(node)) {
            createErrorFieldMatchPatterns(1, errorMatchPatternNode, bLangErrorMatchPattern);
            return bLangErrorMatchPattern;
        }
        bLangErrorMatchPattern.errorCauseMatchPattern = createErrorCauseMatchPattern(node);
        createErrorFieldMatchPatterns(2, errorMatchPatternNode, bLangErrorMatchPattern);

        return bLangErrorMatchPattern;
    }

    private BLangNamedArgMatchPattern transformNamedArgMatchPattern(NamedArgMatchPatternNode namedArgMatchPatternNode,
                                                                    Location pos) {
        BLangNamedArgMatchPattern bLangNamedArgMatchPattern =
                (BLangNamedArgMatchPattern) TreeBuilder.createNamedArgMatchPattern();
        bLangNamedArgMatchPattern.argName = createIdentifier(namedArgMatchPatternNode.identifier());
        bLangNamedArgMatchPattern.matchPattern = transformMatchPattern(namedArgMatchPatternNode.matchPattern());
        bLangNamedArgMatchPattern.pos = pos;

        return bLangNamedArgMatchPattern;
    }

    private BLangListMatchPattern transformListMatchPattern(ListMatchPatternNode listMatchPatternNode,
                                                            Location pos) {
        BLangListMatchPattern bLangListMatchPattern =
                (BLangListMatchPattern) TreeBuilder.createListMatchPattern();
        bLangListMatchPattern.pos = pos;

        SeparatedNodeList<Node> matchPatterns = listMatchPatternNode.matchPatterns();
        int matchPatternListSize = matchPatterns.size();

        if (matchPatternListSize == 0) {
            return bLangListMatchPattern;
        }

        for (int i = 0; i < matchPatternListSize - 1; i++) {
            BLangMatchPattern bLangMemberMatchPattern = transformMatchPattern(matchPatterns.get(i));
            if (bLangMemberMatchPattern == null) {
                continue;
            }
            bLangListMatchPattern.addMatchPattern(bLangMemberMatchPattern);
        }

        BLangMatchPattern lastMember = transformMatchPattern(matchPatterns.get(matchPatternListSize - 1));
        if (lastMember.getKind() == NodeKind.REST_MATCH_PATTERN) {
            bLangListMatchPattern.setRestMatchPattern((BLangRestMatchPattern) lastMember);
        } else {
            bLangListMatchPattern.addMatchPattern(lastMember);
        }

        return bLangListMatchPattern;
    }

    private BLangRestMatchPattern transformRestMatchPattern(RestMatchPatternNode restMatchPatternNode, Location pos) {
        BLangRestMatchPattern bLangRestMatchPattern = (BLangRestMatchPattern) TreeBuilder.createRestMatchPattern();
        bLangRestMatchPattern.pos = pos;

        SimpleNameReferenceNode variableName = restMatchPatternNode.variableName();
        bLangRestMatchPattern.setIdentifier(createIdentifier(getPosition(variableName), variableName.name()));
        return bLangRestMatchPattern;
    }

    private BLangMappingMatchPattern transformMappingMatchPattern(MappingMatchPatternNode mappingMatchPatternNode,
                                                                  Location pos) {
        BLangMappingMatchPattern bLangMappingMatchPattern =
                (BLangMappingMatchPattern) TreeBuilder.createMappingMatchPattern();
        bLangMappingMatchPattern.pos = pos;

            SeparatedNodeList<Node> fieldMatchPatterns = mappingMatchPatternNode.fieldMatchPatterns();
            int fieldMatchPatternListSize = fieldMatchPatterns.size();

            if (fieldMatchPatternListSize == 0) {
                return bLangMappingMatchPattern;
            }

            for (int i = 0; i < fieldMatchPatternListSize - 1; i++) {
                bLangMappingMatchPattern.fieldMatchPatterns.add((BLangFieldMatchPattern)
                        transformMatchPattern(fieldMatchPatterns.get(i)));
            }

            BLangMatchPattern lastMember = transformMatchPattern(fieldMatchPatterns.get(fieldMatchPatternListSize - 1));
            if (lastMember.getKind() == NodeKind.REST_MATCH_PATTERN) {
                bLangMappingMatchPattern.setRestMatchPattern((BLangRestMatchPattern) lastMember);
            } else {
                bLangMappingMatchPattern.addFieldMatchPattern((BLangFieldMatchPattern) lastMember);
            }

        return bLangMappingMatchPattern;
    }

    private BLangFieldMatchPattern transformFieldMatchPattern(FieldMatchPatternNode fieldMatchPatternNode,
                                                              Location pos) {
        BLangFieldMatchPattern bLangFieldMatchPattern =
                (BLangFieldMatchPattern) TreeBuilder.createFieldMatchPattern();
        bLangFieldMatchPattern.pos = pos;

        bLangFieldMatchPattern.fieldName =
                createIdentifier(fieldMatchPatternNode.fieldNameNode());
        bLangFieldMatchPattern.matchPattern = transformMatchPattern(fieldMatchPatternNode.matchPattern());
        return bLangFieldMatchPattern;
    }

    private BLangBindingPattern transformBindingPattern(Node bindingPattern) {
        Location pos = getPosition(bindingPattern);
        SyntaxKind patternKind = bindingPattern.kind();
        switch (patternKind) {
            case CAPTURE_BINDING_PATTERN:
                return transformCaptureBindingPattern((CaptureBindingPatternNode) bindingPattern, pos);
            case LIST_BINDING_PATTERN:
                return transformListBindingPattern((ListBindingPatternNode) bindingPattern, pos);
            case NAMED_ARG_BINDING_PATTERN:
                return transformNamedArgBindingPattern((NamedArgBindingPatternNode) bindingPattern, pos);
            case REST_BINDING_PATTERN:
                return transformRestBindingPattern((RestBindingPatternNode) bindingPattern, pos);
            case MAPPING_BINDING_PATTERN:
                return transformMappingBindingPattern((MappingBindingPatternNode) bindingPattern, pos);
            case FIELD_BINDING_PATTERN:
                return transformFieldBindingPattern(bindingPattern, pos);
            case ERROR_BINDING_PATTERN:
                return transformErrorBindingPattern((ErrorBindingPatternNode) bindingPattern, pos);
            case WILDCARD_BINDING_PATTERN:
            default:
                assert patternKind == SyntaxKind.WILDCARD_BINDING_PATTERN;
                return transformWildCardBindingPattern(pos);
        }
    }

    private BLangWildCardBindingPattern transformWildCardBindingPattern(Location pos) {
        BLangWildCardBindingPattern bLangWildCardBindingPattern =
                (BLangWildCardBindingPattern) TreeBuilder.createWildCardBindingPattern();
        bLangWildCardBindingPattern.pos = pos;
        return bLangWildCardBindingPattern;
    }

    private BLangCaptureBindingPattern transformCaptureBindingPattern(CaptureBindingPatternNode captureBindingPattern,
                                                                      Location pos) {
        BLangCaptureBindingPattern bLangCaptureBindingPattern =
                (BLangCaptureBindingPattern) TreeBuilder.createCaptureBindingPattern();
        bLangCaptureBindingPattern.setIdentifier(createIdentifier(captureBindingPattern.variableName()));
        bLangCaptureBindingPattern.pos = pos;
        return bLangCaptureBindingPattern;
    }

    private BLangRestBindingPattern transformRestBindingPattern(RestBindingPatternNode restBindingPatternNode,
                                                                Location pos) {
        BLangRestBindingPattern bLangRestBindingPattern =
                (BLangRestBindingPattern) TreeBuilder.createRestBindingPattern();
        bLangRestBindingPattern.pos = pos;
        SimpleNameReferenceNode variableName = restBindingPatternNode.variableName();
        bLangRestBindingPattern.setIdentifier(createIdentifier(getPosition(variableName), variableName.name()));
        return bLangRestBindingPattern;
    }

    private BLangListBindingPattern transformListBindingPattern(ListBindingPatternNode listBindingPatternNode,
                                                                Location pos) {
        BLangListBindingPattern bLangListBindingPattern =
                (BLangListBindingPattern) TreeBuilder.createListBindingPattern();
        bLangListBindingPattern.pos = pos;

        for (Node listMemberBindingPattern : listBindingPatternNode.bindingPatterns()) {
            if (listMemberBindingPattern.kind() != SyntaxKind.REST_BINDING_PATTERN) {
                bLangListBindingPattern.addBindingPattern(transformBindingPattern(listMemberBindingPattern));
                continue;
            }
            bLangListBindingPattern.restBindingPattern =
                    (BLangRestBindingPattern) transformBindingPattern(listMemberBindingPattern);
        }
        return bLangListBindingPattern;
    }

    private BLangMappingBindingPattern transformMappingBindingPattern(MappingBindingPatternNode
                                                                              mappingBindingPatternNode,
                                                                      Location pos) {
        BLangMappingBindingPattern bLangMappingBindingPattern =
                (BLangMappingBindingPattern) TreeBuilder.createMappingBindingPattern();
        bLangMappingBindingPattern.pos = pos;

        for (Node fieldBindingPattern : mappingBindingPatternNode.fieldBindingPatterns()) {
            if (fieldBindingPattern.kind() == SyntaxKind.REST_BINDING_PATTERN) {
                bLangMappingBindingPattern.restBindingPattern =
                        (BLangRestBindingPattern) transformBindingPattern(fieldBindingPattern);
                continue;
            }
            bLangMappingBindingPattern.fieldBindingPatterns.add(
                    (BLangFieldBindingPattern) transformBindingPattern(fieldBindingPattern));
        }
        return bLangMappingBindingPattern;
    }

    private BLangFieldBindingPattern transformFieldBindingPattern(Node bindingPattern, Location pos) {
        BLangFieldBindingPattern bLangFieldBindingPattern =
                (BLangFieldBindingPattern) TreeBuilder.createFieldBindingPattern();
        bLangFieldBindingPattern.pos = pos;
        if (bindingPattern instanceof FieldBindingPatternVarnameNode) {
            FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode =
                    (FieldBindingPatternVarnameNode) bindingPattern;
            BLangIdentifier fieldName = createIdentifier(fieldBindingPatternVarnameNode.variableName().name());
            bLangFieldBindingPattern.fieldName = fieldName;
            BLangCaptureBindingPattern bLangCaptureBindingPatternInFieldBindingPattern =
                    (BLangCaptureBindingPattern) TreeBuilder.createCaptureBindingPattern();
            bLangCaptureBindingPatternInFieldBindingPattern.setIdentifier(fieldName);
            bLangCaptureBindingPatternInFieldBindingPattern.pos = pos;

            bLangFieldBindingPattern.bindingPattern = bLangCaptureBindingPatternInFieldBindingPattern;
            return bLangFieldBindingPattern;
        }
        FieldBindingPatternFullNode fieldBindingPatternNode = (FieldBindingPatternFullNode) bindingPattern;
        bLangFieldBindingPattern.fieldName = createIdentifier(fieldBindingPatternNode.variableName().name());
        bLangFieldBindingPattern.bindingPattern =
                transformBindingPattern(fieldBindingPatternNode.bindingPattern());
        return bLangFieldBindingPattern;
    }

    private BLangNamedArgBindingPattern transformNamedArgBindingPattern(NamedArgBindingPatternNode
                                                                                namedArgBindingPattern,
                                                                        Location pos) {
        BLangNamedArgBindingPattern bLangNamedArgBindingPattern =
                (BLangNamedArgBindingPattern) TreeBuilder.createNamedArgBindingPattern();
        bLangNamedArgBindingPattern.pos = pos;
        bLangNamedArgBindingPattern.argName = createIdentifier(namedArgBindingPattern.argName());
        bLangNamedArgBindingPattern.bindingPattern =
                transformBindingPattern(namedArgBindingPattern.bindingPattern());
        return bLangNamedArgBindingPattern;
    }

    private BLangErrorBindingPattern transformErrorBindingPattern(ErrorBindingPatternNode errorBindingPatternNode,
                                                                  Location pos) {
        BLangErrorBindingPattern bLangErrorBindingPattern =
                (BLangErrorBindingPattern) TreeBuilder.createErrorBindingPattern();
        bLangErrorBindingPattern.pos = pos;

        if (errorBindingPatternNode.typeReference().isPresent()) {
            Node nameReferenceNode = errorBindingPatternNode.typeReference().get();
            bLangErrorBindingPattern.errorTypeReference =
                    (BLangUserDefinedType) createTypeNode(nameReferenceNode);
        }

        if (errorBindingPatternNode.argListBindingPatterns().size() == 0) {
            return bLangErrorBindingPattern;
        }

        Node node = errorBindingPatternNode.argListBindingPatterns().get(0);
        if (isErrorFieldBindingPattern(node)) {
            createErrorFieldBindingPatterns(0, errorBindingPatternNode, bLangErrorBindingPattern);
            return bLangErrorBindingPattern;
        }

        bLangErrorBindingPattern.errorMessageBindingPattern = createErrorMessageBindingPattern(node);
        if (errorBindingPatternNode.argListBindingPatterns().size() == 1) {
            return bLangErrorBindingPattern;
        }

        node = errorBindingPatternNode.argListBindingPatterns().get(1);
        if (isErrorFieldBindingPattern(node)) {
            createErrorFieldBindingPatterns(1, errorBindingPatternNode, bLangErrorBindingPattern);
            return bLangErrorBindingPattern;
        }
        bLangErrorBindingPattern.errorCauseBindingPattern = createErrorCauseBindingPattern(node);
        createErrorFieldBindingPatterns(2, errorBindingPatternNode, bLangErrorBindingPattern);

        return bLangErrorBindingPattern;
    }

    private boolean isErrorFieldMatchPattern(Node node) {
        return node.kind() == SyntaxKind.NAMED_ARG_MATCH_PATTERN || node.kind() == SyntaxKind.REST_MATCH_PATTERN;
    }

    private boolean isErrorFieldBindingPattern(Node node) {
        return node.kind() == SyntaxKind.NAMED_ARG_BINDING_PATTERN || node.kind() == SyntaxKind.REST_BINDING_PATTERN;
    }

    private BLangErrorMessageMatchPattern createErrorMessageMatchPattern(Node node) {
        BLangMatchPattern matchPattern = transformMatchPattern(node);

        BLangErrorMessageMatchPattern bLangErrorMessageMatchPattern =
                (BLangErrorMessageMatchPattern) TreeBuilder.createErrorMessageMatchPattern();
        bLangErrorMessageMatchPattern.pos = getPosition(node);
        bLangErrorMessageMatchPattern.simpleMatchPattern = createSimpleMatchPattern(matchPattern);
        return bLangErrorMessageMatchPattern;
    }

    private BLangErrorMessageBindingPattern createErrorMessageBindingPattern(Node node) {
        BLangBindingPattern bindingPattern = transformBindingPattern(node);

        BLangErrorMessageBindingPattern bLangErrorMessageBindingPattern =
                (BLangErrorMessageBindingPattern) TreeBuilder.createErrorMessageBindingPattern();
        bLangErrorMessageBindingPattern.pos = getPosition(node);
        bLangErrorMessageBindingPattern.simpleBindingPattern = createSimpleBindingPattern(bindingPattern);
        return bLangErrorMessageBindingPattern;
    }

    private BLangErrorCauseMatchPattern createErrorCauseMatchPattern(Node node) {
        BLangMatchPattern matchPattern = transformMatchPattern(node);

        BLangErrorCauseMatchPattern bLangErrorCauseMatchPattern =
                (BLangErrorCauseMatchPattern) TreeBuilder.createErrorCauseMatchPattern();
        bLangErrorCauseMatchPattern.pos = getPosition(node);

        if (matchPattern.getKind() == NodeKind.ERROR_MATCH_PATTERN) {
            bLangErrorCauseMatchPattern.errorMatchPattern = (BLangErrorMatchPattern) matchPattern;
            return bLangErrorCauseMatchPattern;
        }
        bLangErrorCauseMatchPattern.simpleMatchPattern = createSimpleMatchPattern(matchPattern);
        return bLangErrorCauseMatchPattern;
    }

    private BLangErrorCauseBindingPattern createErrorCauseBindingPattern(Node node) {
        BLangBindingPattern bindingPattern = transformBindingPattern(node);

        BLangErrorCauseBindingPattern bLangErrorCauseBindingPattern =
                (BLangErrorCauseBindingPattern) TreeBuilder.createErrorCauseBindingPattern();
        bLangErrorCauseBindingPattern.pos = getPosition(node);

        if (bindingPattern.getKind() == NodeKind.ERROR_BINDING_PATTERN) {
            bLangErrorCauseBindingPattern.errorBindingPattern = (BLangErrorBindingPattern) bindingPattern;
            return bLangErrorCauseBindingPattern;
        }
        bLangErrorCauseBindingPattern.simpleBindingPattern = createSimpleBindingPattern(bindingPattern);
        return bLangErrorCauseBindingPattern;
    }

    private BLangErrorFieldMatchPatterns createErrorFieldMatchPattern(Node errorFieldMatchPatternNode,
                                                         BLangErrorFieldMatchPatterns bLangErrorFieldMatchPatterns) {
        BLangMatchPattern matchPattern = transformMatchPattern(errorFieldMatchPatternNode);
        bLangErrorFieldMatchPatterns.pos = getPosition(errorFieldMatchPatternNode);

        if (matchPattern.getKind() == NodeKind.NAMED_ARG_MATCH_PATTERN) {
            bLangErrorFieldMatchPatterns.addNamedArgMatchPattern(
                    (org.ballerinalang.model.tree.matchpatterns.NamedArgMatchPatternNode) matchPattern);
        } else if (matchPattern.getKind() == NodeKind.REST_MATCH_PATTERN) {
            bLangErrorFieldMatchPatterns.restMatchPattern = (BLangRestMatchPattern) matchPattern;
        }

        return bLangErrorFieldMatchPatterns;
    }

    private BLangErrorFieldBindingPatterns createErrorFieldBindingPattern(Node errorFieldBindingPatternNode,
                                                                          BLangErrorFieldBindingPatterns
                                                                                  bLangErrorFieldBindingPatterns) {
        BLangBindingPattern bindingPattern = transformBindingPattern(errorFieldBindingPatternNode);
        bLangErrorFieldBindingPatterns.pos = getPosition(errorFieldBindingPatternNode);
        if (bindingPattern.getKind() == NodeKind.NAMED_ARG_BINDING_PATTERN) {
            bLangErrorFieldBindingPatterns.
                    addNamedArgBindingPattern(
                            (org.ballerinalang.model.tree.bindingpattern.NamedArgBindingPatternNode) bindingPattern);
        } else if (bindingPattern.getKind() == NodeKind.REST_BINDING_PATTERN) {
            bLangErrorFieldBindingPatterns.restBindingPattern = (BLangRestBindingPattern) bindingPattern;
        }
        return bLangErrorFieldBindingPatterns;
    }

    private void createErrorFieldMatchPatterns(int index, ErrorMatchPatternNode errorMatchPatternNode,
                                               BLangErrorMatchPattern bLangErrorMatchPattern) {
        BLangErrorFieldMatchPatterns bLangErrorFieldMatchPatterns =
                (BLangErrorFieldMatchPatterns) TreeBuilder.createErrorFieldMatchPattern();
        for (int i = index; i < errorMatchPatternNode.argListMatchPatternNode().size(); i++) {
            Node errorFieldMatchPatternNode = errorMatchPatternNode.argListMatchPatternNode().get(i);
            bLangErrorMatchPattern.errorFieldMatchPatterns = createErrorFieldMatchPattern(errorFieldMatchPatternNode,
                    bLangErrorFieldMatchPatterns);
        }
    }

    private void createErrorFieldBindingPatterns(int index, ErrorBindingPatternNode errorBindingPatternNode,
                                                 BLangErrorBindingPattern bLangErrorBindingPattern) {
        BLangErrorFieldBindingPatterns bLangErrorFieldBindingPatterns =
                (BLangErrorFieldBindingPatterns) TreeBuilder.createErrorFieldBindingPattern();
        for (int i = index; i < errorBindingPatternNode.argListBindingPatterns().size(); i++) {
            Node errorFieldBindingPatternNode = errorBindingPatternNode.argListBindingPatterns().get(i);
            bLangErrorBindingPattern.errorFieldBindingPatterns =
                    createErrorFieldBindingPattern(errorFieldBindingPatternNode, bLangErrorFieldBindingPatterns);
        }
    }

    private BLangSimpleMatchPattern createSimpleMatchPattern(BLangNode bLangNode) {
        BLangSimpleMatchPattern bLangSimpleMatchPattern =
                (BLangSimpleMatchPattern) TreeBuilder.createSimpleMatchPattern();

        NodeKind kind = bLangNode.getKind();
        switch (kind) {
            case WILDCARD_MATCH_PATTERN:
                bLangSimpleMatchPattern.wildCardMatchPattern = (BLangWildCardMatchPattern) bLangNode;
                break;
            case CONST_MATCH_PATTERN:
                bLangSimpleMatchPattern.constPattern = (BLangConstPattern) bLangNode;
                break;
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                bLangSimpleMatchPattern.varVariableName = (BLangVarBindingPatternMatchPattern) bLangNode;
                break;
        }
        return bLangSimpleMatchPattern;
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

    private BLangSimpleBindingPattern createSimpleBindingPattern(BLangNode bLangNode) {
        BLangSimpleBindingPattern bLangSimpleBindingPattern =
                (BLangSimpleBindingPattern) TreeBuilder.createSimpleBindingPattern();

        NodeKind kind = bLangNode.getKind();
        switch (kind) {
            case WILDCARD_BINDING_PATTERN:
                bLangSimpleBindingPattern.wildCardBindingPattern = (BLangWildCardBindingPattern) bLangNode;
                break;
            case CAPTURE_BINDING_PATTERN:
                bLangSimpleBindingPattern.captureBindingPattern = (BLangCaptureBindingPattern) bLangNode;
                break;
        }
        return bLangSimpleBindingPattern;
    }

    private BLangXMLElementFilter createXMLElementFilter(Node node) {
        String ns = "";
        String elementName = "*";
        Location nsPos = null;
        Location elemNamePos = null;
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

        return new BLangXMLElementFilter(getPosition(node), ns, nsPos, elementName, elemNamePos);
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

    private BLangRecordVariable createBLangRecordVariable(MappingBindingPatternNode mappingBindingPatternNode) {
        BLangRecordVariable recordVariable = (BLangRecordVariable) TreeBuilder.createRecordVariableNode();
        List<BLangRecordVariableKeyValue> fieldBindingPatternsList = new ArrayList<>();

        for (BindingPatternNode node : mappingBindingPatternNode.fieldBindingPatterns()) {
            BLangRecordVariableKeyValue recordKeyValue = new BLangRecordVariableKeyValue();
            if (node instanceof FieldBindingPatternFullNode) {
                FieldBindingPatternFullNode fullNode = (FieldBindingPatternFullNode) node;
                recordKeyValue.key = createIdentifier(fullNode.variableName().name());
                recordKeyValue.valueBindingPattern = getBLangVariableNode(fullNode.bindingPattern(),
                                                                          getPosition(fullNode.bindingPattern()));
            } else if (node instanceof FieldBindingPatternVarnameNode) {
                FieldBindingPatternVarnameNode varnameNode = (FieldBindingPatternVarnameNode) node;
                recordKeyValue.key = createIdentifier(varnameNode.variableName().name());
                BLangSimpleVariable value = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
                value.pos = getPosition(varnameNode);
                IdentifierNode name = createIdentifier(varnameNode.variableName().name());
                ((BLangIdentifier) name).pos = value.pos;
                value.setName(name);
                recordKeyValue.valueBindingPattern = value;
            } else { // rest-binding-pattern
                recordVariable.restParam = getBLangVariableNode(node, getPosition(node));
                break;
            }

            fieldBindingPatternsList.add(recordKeyValue);
        }

        recordVariable.variableList = fieldBindingPatternsList;
        return recordVariable;
    }

    private BLangLiteral createEmptyLiteral() {
        BLangLiteral bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        bLiteral.value = "";
        bLiteral.originalValue = "";
        bLiteral.setBType(symTable.getTypeFromTag(TypeTags.STRING));
        return bLiteral;
    }

    private BLangVariable createSimpleVariable(Location location,
                                               Token identifier,
                                               Location identifierPos) {
        BLangSimpleVariable memberVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        memberVar.pos = location;
        IdentifierNode name = createIdentifier(identifierPos, identifier);
        ((BLangIdentifier) name).pos = identifierPos;
        memberVar.setName(name);
        return memberVar;
    }

    private BLangVariable getBLangVariableNode(BindingPatternNode bindingPattern, Location varPos) {
        Token varName;
        switch (bindingPattern.kind()) {
            case MAPPING_BINDING_PATTERN:
                MappingBindingPatternNode mappingBindingPatternNode = (MappingBindingPatternNode) bindingPattern;
                BLangRecordVariable recordVar = createBLangRecordVariable(mappingBindingPatternNode);
                recordVar.pos = varPos;
                return recordVar;
            case LIST_BINDING_PATTERN:
                ListBindingPatternNode listBindingPatternNode = (ListBindingPatternNode) bindingPattern;
                BLangTupleVariable tupleVariable = (BLangTupleVariable) TreeBuilder.createTupleVariableNode();
                tupleVariable.pos = varPos;

                for (BindingPatternNode memberBindingPattern : listBindingPatternNode.bindingPatterns()) {
                    if (memberBindingPattern.kind() == SyntaxKind.REST_BINDING_PATTERN) {
                        tupleVariable.restVariable = getBLangVariableNode(memberBindingPattern,
                                                                          getPosition(memberBindingPattern));
                    } else {
                        BLangVariable member = getBLangVariableNode(memberBindingPattern,
                                                                    getPosition(memberBindingPattern));
                        tupleVariable.memberVariables.add(member);
                    }
                }

                return tupleVariable;
            case ERROR_BINDING_PATTERN:
                ErrorBindingPatternNode errorBindingPatternNode = (ErrorBindingPatternNode) bindingPattern;
                BLangErrorVariable bLangErrorVariable = (BLangErrorVariable) TreeBuilder.createErrorVariableNode();
                bLangErrorVariable.pos = varPos;

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
                                        (BLangSimpleVariable) getBLangVariableNode(bindingPatternNode,
                                                                                   getPosition(bindingPatternNode));
                                break;
                            }
                            // Fall through.
                        case ERROR_BINDING_PATTERN:
                            bLangErrorVariable.cause = getBLangVariableNode(bindingPatternNode,
                                                                            getPosition(bindingPatternNode));
                            break;
                        case NAMED_ARG_BINDING_PATTERN:
                            NamedArgBindingPatternNode namedArgBindingPatternNode =
                                    (NamedArgBindingPatternNode) bindingPatternNode;
                            BLangIdentifier key =
                                    createIdentifier(namedArgBindingPatternNode.argName());
                            BLangVariable valueBindingPattern =
                                    getBLangVariableNode(namedArgBindingPatternNode.bindingPattern(),
                                                         getPosition(namedArgBindingPatternNode.bindingPattern()));
                            BLangErrorVariable.BLangErrorDetailEntry detailEntry =
                                    new BLangErrorVariable.BLangErrorDetailEntry(key, valueBindingPattern);
                            detailEntry.pos = getPosition(namedArgBindingPatternNode);
                            namedArgs.add(detailEntry);
                            break;
                        default:// Rest binding pattern
                            bLangErrorVariable.restDetail =
                                    (BLangSimpleVariable) getBLangVariableNode(bindingPatternNode,
                                                                               getPosition(bindingPatternNode));
                    }
                }
                bLangErrorVariable.detail = namedArgs;
                return bLangErrorVariable;
            case REST_BINDING_PATTERN:
                RestBindingPatternNode restBindingPatternNode = (RestBindingPatternNode) bindingPattern;
                varName = restBindingPatternNode.variableName().name();
                break;
            case WILDCARD_BINDING_PATTERN:
                BLangIdentifier ignore = createIgnoreIdentifier(bindingPattern);
                BLangSimpleVariable simpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
                simpleVar.setName(ignore);
                simpleVar.pos = varPos;
                return simpleVar;
            case CAPTURE_BINDING_PATTERN:
            default:
                CaptureBindingPatternNode captureBindingPatternNode = (CaptureBindingPatternNode) bindingPattern;
                varName = captureBindingPatternNode.variableName();
                break;
        }

        return createSimpleVariable(varPos, varName, getPosition(varName));
    }

    BLangValueType addValueType(Location pos, TypeKind typeKind) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = typeKind;
        return typeNode;
    }

    private List<BLangStatement> generateBLangStatements(NodeList<StatementNode> statementNodes, Node endNode) {
        List<BLangStatement> statements = new ArrayList<>(statementNodes.size());
        return generateAndAddBLangStatements(statementNodes, statements, 0, endNode);
    }

    private List<BLangStatement> generateAndAddBLangStatements(NodeList<StatementNode> statementNodes,
                                                               List<BLangStatement> statements,
                                                               int startPosition, Node endNode) {
        int lastStmtIndex = statementNodes.size() - 1;
        for (int j = startPosition; j < statementNodes.size(); j++) {
            StatementNode currentStatement = statementNodes.get(j);
            // TODO: Remove this check once statements are non null guaranteed
            if (currentStatement == null) {
                continue;
            }
            if (currentStatement.kind() == SyntaxKind.FORK_STATEMENT) {
                generateForkStatements(statements, (ForkStatementNode) currentStatement);
                continue;
            }
            // If there is an `if` statement without an `else`, all the statements following that `if` statement
            // are added to a new block statement.
            if (currentStatement.kind() == SyntaxKind.IF_ELSE_STATEMENT &&
                    ((IfElseStatementNode) currentStatement).elseBody().isEmpty()) {
                statements.add((BLangStatement) currentStatement.apply(this));
                if (j == lastStmtIndex) {
                    // Add an empty block statement if there are no statements following the `if` statement.
                    statements.add((BLangBlockStmt) TreeBuilder.createBlockNode());
                    break;
                }
                BLangBlockStmt bLBlockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
                int nextStmtIndex = j + 1;
                this.isInLocalContext = true;
                generateAndAddBLangStatements(statementNodes, bLBlockStmt.stmts, nextStmtIndex, endNode);
                this.isInLocalContext = false;
                if (nextStmtIndex <= lastStmtIndex) {
                    bLBlockStmt.pos = getPosition(statementNodes.get(nextStmtIndex), endNode);
                }
                statements.add(bLBlockStmt);
                break;
            } else {
                statements.add((BLangStatement) currentStatement.apply(this));
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
        String nextAnonymousForkKey = anonymousModelHelper.getNextAnonymousForkKey(packageID);
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

    private BLangCheckedExpr createCheckExpr(Location pos, BLangExpression expr) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkedExpr.pos = pos;
        checkedExpr.expr = expr;
        return checkedExpr;
    }

    private BLangCheckPanickedExpr createCheckPanickedExpr(Location pos, BLangExpression expr) {
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
            this.anonTypeNameSuffixes.push("return");
            bLFunction.setReturnTypeNode(createTypeNode(returnType.type()));
            this.anonTypeNameSuffixes.pop();
            bLFunction.returnTypeAnnAttachments = applyAll(returnType.annotations());
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = symTable.builtinPos;
            bLValueType.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(bLValueType);
        }
    }

    private BLangUnaryExpr createBLangUnaryExpr(Location location,
                                                OperatorKind operatorKind,
                                                BLangExpression expr) {
        BLangUnaryExpr bLUnaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        bLUnaryExpr.pos = location;
        bLUnaryExpr.operator = operatorKind;
        bLUnaryExpr.expr = expr;
        return bLUnaryExpr;
    }

    private BLangExpression createExpression(Node expression) {
        if (expression.kind() == SyntaxKind.ASYNC_SEND_ACTION) {
            // TODO: support async send as expression #24849
            dlog.error(getPosition(expression), DiagnosticErrorCode.ASYNC_SEND_NOT_YET_SUPPORTED_AS_EXPRESSION);
            Token missingIdentifier = NodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN,
                    NodeFactory.createEmptyMinutiaeList(), NodeFactory.createEmptyMinutiaeList());
            expression = NodeFactory.createSimpleNameReferenceNode(missingIdentifier);
        }

        return (BLangExpression) createActionOrExpression(expression);
    }

    private BLangNode createActionOrExpression(Node actionOrExpression) {
        if (isSimpleLiteral(actionOrExpression.kind())) {
            return createSimpleLiteral(actionOrExpression);
        } else if (actionOrExpression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                   actionOrExpression.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE ||
                   actionOrExpression.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Variable References
            BLangIdentifier[] nameReference = createBLangNameReference(actionOrExpression);
            BLangSimpleVarRef bLVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            bLVarRef.pos = getPosition(actionOrExpression);
            bLVarRef.pkgAlias = this.createIdentifier(nameReference[0].getPosition(), nameReference[0].getValue());
            bLVarRef.variableName = this.createIdentifier(nameReference[1].getPosition(), nameReference[1].getValue());
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

    private BLangNode createStringTemplateLiteral(NodeList<Node> memberNodes, Location location) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        for (Node memberNode : memberNodes) {
            stringTemplateLiteral.exprs.add((BLangExpression) memberNode.apply(this));
        }

        if (stringTemplateLiteral.exprs.isEmpty()) {
            BLangLiteral emptyLiteral = createEmptyLiteral();
            emptyLiteral.pos = location;
            stringTemplateLiteral.exprs.add(emptyLiteral);
        }

        stringTemplateLiteral.pos = location;
        return stringTemplateLiteral;
    }

    private BLangRawTemplateLiteral createRawTemplateLiteral(NodeList<Node> members, Location location) {
        BLangRawTemplateLiteral literal = (BLangRawTemplateLiteral) TreeBuilder.createRawTemplateLiteralNode();
        literal.pos = location;

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

    private BLangNode createRegExpTemplateLiteral(NodeList<Node> reSequences, Location location) {
        BLangRegExpTemplateLiteral regExpTemplateLiteral =
                (BLangRegExpTemplateLiteral) TreeBuilder.createRegExpTemplateLiteralNode();
        regExpTemplateLiteral.reDisjunction = (BLangReDisjunction) createReDisjunctionNode(reSequences, location);
        regExpTemplateLiteral.pos = location;
        return regExpTemplateLiteral;
    }

    private BLangNode createReDisjunctionNode(NodeList<Node> reSequences, Location location) {
        BLangReDisjunction reDisjunction =
                (BLangReDisjunction) TreeBuilder.createReDisjunctionNode();
        for (Node sequence : reSequences) {
            BLangExpression reSequence = (BLangExpression) sequence.apply(this);
            reDisjunction.sequenceList.add(reSequence);
        }
        reDisjunction.pos = location;
        return reDisjunction;
    }

    @Override
    public BLangNode transform(ReSequenceNode reSequenceNode) {
        BLangReSequence reSequence = (BLangReSequence) TreeBuilder.createReSequenceNode();
        for (Node reTermNode : reSequenceNode.reTerm()) {
            BLangReTerm reTerm = (BLangReTerm) reTermNode.apply(this);
            reSequence.termList.add(reTerm);
        }
        reSequence.pos = getPosition(reSequenceNode);
        return reSequence;
    }

    @Override
    public BLangNode transform(ReAssertionNode reAssertionNode) {
        BLangReAssertion reAssertion = (BLangReAssertion) TreeBuilder.createReAssertionNode();
        reAssertion.assertion = (BLangExpression) reAssertionNode.reAssertion().apply(this);
        reAssertion.pos = getPosition(reAssertionNode);
        return reAssertion;
    }

    @Override
    public BLangNode transform(ReAtomQuantifierNode reAtomQuantifierNode) {
        BLangReAtomQuantifier reAtomQuantifier = (BLangReAtomQuantifier) TreeBuilder.createReAtomQuantifierNode();
        Node reAtomNode = reAtomQuantifierNode.reAtom();
        reAtomQuantifier.atom = (BLangExpression) reAtomNode.apply(this);
        Location pos = getPosition(reAtomQuantifierNode);
        Optional<ReQuantifierNode> quantifier = reAtomQuantifierNode.reQuantifier();
        quantifier.ifPresent(reQuantifierNode -> reAtomQuantifier.quantifier =
                (BLangReQuantifier) reQuantifierNode.apply(this));
        reAtomQuantifier.pos = pos;
        return reAtomQuantifier;
    }

    @Override
    public BLangNode transform(ReQuantifierNode reQuantifierNode) {
        BLangReQuantifier reQuantifier = (BLangReQuantifier) TreeBuilder.createReQuantifierNode();
        BLangExpression quantifier = (BLangExpression) reQuantifierNode.reBaseQuantifier().apply(this);
        Optional<Token> nonGreedyChar = reQuantifierNode.nonGreedyChar();
        nonGreedyChar.ifPresent(token -> reQuantifier.nonGreedyChar = (BLangExpression) token.apply(this));
        reQuantifier.pos = getPosition(reQuantifierNode);
        reQuantifier.quantifier = quantifier;
        return reQuantifier;
    }

    @Override
    public BLangNode transform(ReBracedQuantifierNode reBracedQuantifierNode) {
        BLangLiteral openBrace = (BLangLiteral) reBracedQuantifierNode.openBraceToken().apply(this);
        BLangLiteral closeBrace = (BLangLiteral) reBracedQuantifierNode.closeBraceToken().apply(this);
        StringBuilder leastTimesMatchedDigits = new StringBuilder();
        String quantifier;
        Location leastTimesMatchedDigitPos = null;
        for (Node digit : reBracedQuantifierNode.leastTimesMatchedDigit()) {
            BLangLiteral leastDigit = (BLangLiteral) digit.apply(this);
            leastTimesMatchedDigitPos = leastDigit.pos;
            leastTimesMatchedDigits.append(leastDigit.value.toString());
        }
        Optional<Token> commaToken = reBracedQuantifierNode.commaToken();
        if (commaToken.isEmpty()) {
             quantifier = openBrace.value.toString() + leastTimesMatchedDigits.toString() + closeBrace.value.toString();
        } else {
            BLangLiteral comma = (BLangLiteral) commaToken.get().apply(this);
            StringBuilder mostTimesMatchedDigits = new StringBuilder();
            for (Node digit : reBracedQuantifierNode.mostTimesMatchedDigit()) {
                BLangLiteral mostDigit = (BLangLiteral) digit.apply(this);
                mostTimesMatchedDigits.append(mostDigit.value.toString());
            }
            String leastTimesMatched = leastTimesMatchedDigits.toString();
            String mostTimesMatched = mostTimesMatchedDigits.toString();
            // leastTimesMatched value should be less than mostTimesMatched value.
            if (!mostTimesMatched.isEmpty()) {
                checkValidityOfOccurrences(leastTimesMatched, mostTimesMatched, leastTimesMatchedDigitPos);
            }
            quantifier = openBrace.value.toString() + leastTimesMatched +
                    comma.value.toString() + mostTimesMatched + closeBrace.value.toString();
        }
        return createStringLiteral(quantifier, getPosition(reBracedQuantifierNode));
    }

    private void checkValidityOfOccurrences(String lhsValue, String rhsValue, Location pos) {
        if (Long.parseLong(lhsValue) > Long.parseLong(rhsValue)) {
            dlog.error(pos, DiagnosticErrorCode.INVALID_QUANTIFIER_MINIMUM);
        }
    }

    @Override
    public BLangNode transform(ReAtomCharOrEscapeNode reAtomCharOrEscapeNode) {
        BLangReAtomCharOrEscape reAtomCharOrEscape =
                (BLangReAtomCharOrEscape) TreeBuilder.createReAtomCharOrEscapeNode();
        reAtomCharOrEscape.charOrEscape = (BLangExpression) reAtomCharOrEscapeNode.reAtomCharOrEscape().apply(this);
        reAtomCharOrEscape.pos = getPosition(reAtomCharOrEscapeNode);
        return reAtomCharOrEscape;
    }

    @Override
    public BLangNode transform(ReQuoteEscapeNode reQuoteEscapeNode) {
        BLangLiteral backSlash = (BLangLiteral) reQuoteEscapeNode.slashToken().apply(this);
        BLangLiteral syntaxChar = (BLangLiteral) reQuoteEscapeNode.reSyntaxChar().apply(this);
        Location pos = getPosition(reQuoteEscapeNode);
        BLangExpression escapeChar = createStringLiteral(backSlash.value.toString() +
                syntaxChar.value.toString(), pos);
        // Return the literal for escape sequence if it's in a character class.
        if (this.isInCharacterClass) {
            return escapeChar;
        }
        BLangReAtomCharOrEscape reAtomCharOrEscape =
                (BLangReAtomCharOrEscape) TreeBuilder.createReAtomCharOrEscapeNode();
        reAtomCharOrEscape.charOrEscape = escapeChar;
        reAtomCharOrEscape.pos = pos;
        return reAtomCharOrEscape;
    }

    @Override
    public BLangNode transform(ReSimpleCharClassEscapeNode reSimpleCharClassEscapeNode) {
        BLangLiteral backSlash = (BLangLiteral) reSimpleCharClassEscapeNode.slashToken().apply(this);
        BLangLiteral simpleCharClassEscapeNode =
                (BLangLiteral) reSimpleCharClassEscapeNode.reSimpleCharClassCode().apply(this);
        Location pos = getPosition(reSimpleCharClassEscapeNode);
        BLangExpression escapeChar = createStringLiteral(backSlash.value.toString() +
                simpleCharClassEscapeNode.value.toString(), pos);
        // Return the literal for escape sequence if it's in a character class.
        if (this.isInCharacterClass) {
            return escapeChar;
        }
        BLangReAtomCharOrEscape reAtomCharOrEscape =
                (BLangReAtomCharOrEscape) TreeBuilder.createReAtomCharOrEscapeNode();
        reAtomCharOrEscape.charOrEscape = escapeChar;
        reAtomCharOrEscape.pos = pos;
        return reAtomCharOrEscape;
    }

    @Override
    public BLangNode transform(ReUnicodePropertyEscapeNode reUnicodePropertyEscapeNode) {
        BLangLiteral backSlash = (BLangLiteral) reUnicodePropertyEscapeNode.slashToken().apply(this);
        BLangLiteral property = (BLangLiteral) reUnicodePropertyEscapeNode.property().apply(this);
        BLangLiteral openBrace = (BLangLiteral) reUnicodePropertyEscapeNode.openBraceToken().apply(this);
        BLangLiteral unicodeProperty = (BLangLiteral) reUnicodePropertyEscapeNode.reUnicodeProperty().apply(this);
        BLangLiteral closeBrace = (BLangLiteral) reUnicodePropertyEscapeNode.closeBraceToken().apply(this);
        Location pos = getPosition(reUnicodePropertyEscapeNode);
        BLangExpression escapeChar =
                createStringLiteral(backSlash.value.toString() + property.value.toString() +
                        openBrace.value.toString() + unicodeProperty.value.toString() +
                        closeBrace.value.toString(), pos);
        // Return the literal for escape sequence if it's in a character class.
        if (this.isInCharacterClass) {
            return escapeChar;
        }
        BLangReAtomCharOrEscape reAtomCharOrEscape =
                (BLangReAtomCharOrEscape) TreeBuilder.createReAtomCharOrEscapeNode();
        reAtomCharOrEscape.charOrEscape = escapeChar;
        reAtomCharOrEscape.pos = pos;
        return reAtomCharOrEscape;
    }

    @Override
    public BLangNode transform(ReUnicodeScriptNode reUnicodeScriptNode) {
        BLangLiteral scriptStart = (BLangLiteral) reUnicodeScriptNode.scriptStart().apply(this);
        BLangLiteral unicodePropertyValue = (BLangLiteral) reUnicodeScriptNode.reUnicodePropertyValue().apply(this);
        return createStringLiteral(scriptStart.value.toString() + unicodePropertyValue.value.toString(),
                getPosition(reUnicodeScriptNode));
    }

    @Override
    public BLangNode transform(ReUnicodeGeneralCategoryNode reUnicodeGeneralCategoryNode) {
        BLangLiteral unicodeGeneralCategoryName =
                (BLangLiteral) reUnicodeGeneralCategoryNode.reUnicodeGeneralCategoryName().apply(this);
        Optional<Node> categoryStartNode = reUnicodeGeneralCategoryNode.categoryStart();
        if (categoryStartNode.isPresent()) {
            BLangLiteral categoryStart = (BLangLiteral) categoryStartNode.get().apply(this);
            return createStringLiteral(categoryStart.value.toString() +
                    unicodeGeneralCategoryName.value.toString(), getPosition(reUnicodeGeneralCategoryNode));
        }
        return createStringLiteral(String.valueOf(unicodeGeneralCategoryName.value),
                getPosition(reUnicodeGeneralCategoryNode));
    }

    @Override
    public BLangNode transform(ReCharacterClassNode reCharacterClassNode) {
        boolean prevIsInCharacterClass = this.isInCharacterClass;
        this.isInCharacterClass = true;
        BLangReCharacterClass reCharacterClass = (BLangReCharacterClass) TreeBuilder.createReCharacterClassNode();
        reCharacterClass.characterClassStart = (BLangExpression) reCharacterClassNode.openBracket().apply(this);
        Optional<Token> negation = reCharacterClassNode.negation();
        negation.ifPresent(token -> reCharacterClass.negation = (BLangExpression) token.apply(this));
        Optional<Node> reCharSet = reCharacterClassNode.reCharSet();
        if (reCharSet.isPresent()) {
            BLangReCharSet charSet = (BLangReCharSet) TreeBuilder.createReCharSetNode();
            BLangExpression charSetAtoms = (BLangExpression) reCharSet.get().apply(this);
            if (charSetAtoms.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                charSet.charSetAtoms.addAll(((BLangListConstructorExpr) charSetAtoms).exprs);
            } else {
                charSet.charSetAtoms.add(charSetAtoms);
            }
            charSet.pos = charSetAtoms.pos;
            reCharacterClass.charSet = charSet;
        }
        reCharacterClass.characterClassEnd = (BLangExpression) reCharacterClassNode.closeBracket().apply(this);
        reCharacterClass.pos = getPosition(reCharacterClassNode);
        this.isInCharacterClass = prevIsInCharacterClass;
        return reCharacterClass;
    }

    @Override
    public BLangNode transform(ReCharSetAtomWithReCharSetNoDashNode charSetNode) {
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        List<BLangExpression> exprs = new ArrayList<>();
        exprs.add((BLangExpression) charSetNode.reCharSetAtom().apply(this));
        BLangExpression charSetNoDash =
                (BLangExpression) charSetNode.reCharSetNoDash().apply(this);
        if (charSetNoDash.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
            exprs.addAll(((BLangListConstructorExpr) charSetNoDash).exprs);
        } else {
            exprs.add(charSetNoDash);
        }
        listConstructorExpr.exprs = exprs;
        listConstructorExpr.pos = getPosition(charSetNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(ReCharSetAtomNoDashWithReCharSetNoDashNode charSetNode) {
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        List<BLangExpression> exprs = new ArrayList<>();
        exprs.add((BLangExpression) charSetNode.reCharSetAtomNoDash().apply(this));
        BLangExpression charSetNoDash =
                (BLangExpression) charSetNode.reCharSetNoDash().apply(this);
        if (charSetNoDash.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
            exprs.addAll(((BLangListConstructorExpr) charSetNoDash).exprs);
        } else {
            exprs.add(charSetNoDash);
        }
        listConstructorExpr.exprs = exprs;
        listConstructorExpr.pos = getPosition(charSetNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(ReCharSetRangeWithReCharSetNode charSetNode) {
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        List<BLangExpression> exprs = new ArrayList<>();
        exprs.add((BLangExpression) charSetNode.reCharSetRange().apply(this));
        Optional<Node> reCharSet = charSetNode.reCharSet();
        if (reCharSet.isPresent()) {
            BLangExpression charSet = (BLangExpression) reCharSet.get().apply(this);
            if (charSet.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                exprs.addAll(((BLangListConstructorExpr) charSet).exprs);
            } else {
                exprs.add(charSet);
            }
        }
        listConstructorExpr.exprs = exprs;
        listConstructorExpr.pos = getPosition(charSetNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(ReCharSetRangeNode charSetRangeNode) {
        BLangReCharSetRange charSetRange = (BLangReCharSetRange) TreeBuilder.createReCharSetRangeNode();
        charSetRange.lhsCharSetAtom = (BLangExpression) charSetRangeNode.lhsReCharSetAtom().apply(this);
        charSetRange.dash = (BLangExpression) charSetRangeNode.minusToken().apply(this);
        charSetRange.rhsCharSetAtom = (BLangExpression) charSetRangeNode.rhsReCharSetAtom().apply(this);
        charSetRange.pos = getPosition(charSetRangeNode);
        // lhsCharSetAtom value should be less than rhsCharSetAtom value.
        checkRangeValidity((BLangLiteral) charSetRange.lhsCharSetAtom, (BLangLiteral) charSetRange.rhsCharSetAtom);
        return charSetRange;
    }

    private void checkRangeValidity(BLangLiteral lhsValue, BLangLiteral rhsValue) {
        if (lhsValue.value.toString().compareTo(rhsValue.value.toString()) > 0) {
            dlog.error(lhsValue.pos, DiagnosticErrorCode.INVALID_START_CHAR_CODE_IN_RANGE);
        }
    }

    @Override
    public BLangNode transform(ReCharSetRangeNoDashWithReCharSetNode charSetNode) {
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        List<BLangExpression> exprs = new ArrayList<>();
        exprs.add((BLangExpression) charSetNode.reCharSetRangeNoDash().apply(this));
        Optional<Node> reCharSet = charSetNode.reCharSet();
        if (reCharSet.isPresent()) {
            BLangExpression charSet = (BLangExpression) reCharSet.get().apply(this);
            if (charSet.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                exprs.addAll(((BLangListConstructorExpr) charSet).exprs);
            } else {
                exprs.add(charSet);
            }
        }
        listConstructorExpr.exprs = exprs;
        listConstructorExpr.pos = getPosition(charSetNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(ReCharSetRangeNoDashNode charSetRangeNode) {
        BLangReCharSetRange charSetRange = (BLangReCharSetRange) TreeBuilder.createReCharSetRangeNode();
        charSetRange.lhsCharSetAtom = (BLangExpression) charSetRangeNode.reCharSetAtomNoDash().apply(this);
        charSetRange.dash = (BLangExpression) charSetRangeNode.minusToken().apply(this);
        charSetRange.rhsCharSetAtom = (BLangExpression) charSetRangeNode.reCharSetAtom().apply(this);
        charSetRange.pos = getPosition(charSetRangeNode);
        return charSetRange;
    }

    @Override
    public BLangNode transform(ReCapturingGroupsNode reCapturingGroupsNode) {
        BLangReCapturingGroups reCapturingGroups = (BLangReCapturingGroups) TreeBuilder.createReCapturingGroupsNode();
        reCapturingGroups.openParen = (BLangExpression) reCapturingGroupsNode.openParenthesis().apply(this);
        Optional<ReFlagExpressionNode> flagExpr = reCapturingGroupsNode.reFlagExpression();
        flagExpr.ifPresent(reFlagExpressionNode -> reCapturingGroups.flagExpr =
                (BLangReFlagExpression) reFlagExpressionNode.apply(this));
        Location pos = getPosition(reCapturingGroupsNode);
        reCapturingGroups.disjunction =
                (BLangReDisjunction) createReDisjunctionNode(reCapturingGroupsNode.reSequences(), pos);
        reCapturingGroups.closeParen = (BLangLiteral) reCapturingGroupsNode.closeParenthesis().apply(this);
        reCapturingGroups.pos = pos;
        return reCapturingGroups;
    }

    @Override
    public BLangNode transform(ReFlagExpressionNode reFlagExpressionNode) {
        BLangReFlagExpression reFlagExpression = (BLangReFlagExpression) TreeBuilder.createReFlagExpressionNode();
        reFlagExpression.questionMark = (BLangExpression) reFlagExpressionNode.questionMark().apply(this);
        if (reFlagExpressionNode.reFlagsOnOff() != null) {
            reFlagExpression.flagsOnOff = (BLangReFlagsOnOff) reFlagExpressionNode.reFlagsOnOff().apply(this);
        }
        reFlagExpression.colon = (BLangExpression) reFlagExpressionNode.colon().apply(this);
        reFlagExpression.pos = getPosition(reFlagExpressionNode);
        return reFlagExpression;
    }

    @Override
    public BLangNode transform(ReFlagsOnOffNode reFlagsOnOffNode) {
        BLangReFlagsOnOff reFlagsOnOff = (BLangReFlagsOnOff) TreeBuilder.createReFlagsOnOffNode();
        BLangLiteral lhsFlags = (BLangLiteral) reFlagsOnOffNode.lhsReFlags().apply(this);
        String flags;
        Optional<Token> minusToken = reFlagsOnOffNode.minusToken();
        if (minusToken.isEmpty()) {
            flags = lhsFlags.value.toString();
        } else {
            BLangLiteral dash = (BLangLiteral) minusToken.get().apply(this);
            Optional<ReFlagsNode> rhsFlagsNode = reFlagsOnOffNode.rhsReFlags();
            if (rhsFlagsNode.isEmpty()) {
                flags = lhsFlags.value.toString() + dash.value.toString();
            } else {
                BLangLiteral rhsFlags = (BLangLiteral) rhsFlagsNode.get().apply(this);
                flags = lhsFlags.value.toString() + dash.value.toString() + rhsFlags.value.toString();
            }
        }
        Location pos = getPosition(reFlagsOnOffNode);
        checkValidityOfFlags(flags, pos);
        reFlagsOnOff.pos = pos;
        reFlagsOnOff.flags = createStringLiteral(flags, getPosition(reFlagsOnOffNode));
        return reFlagsOnOff;
    }

    private void checkValidityOfFlags(String flags, Location pos) {
        List<Character> charList = new ArrayList<>();
        for (int i = 0; i < flags.length(); i++) {
            char flag = flags.charAt(i);
            if (charList.contains(flag)) {
                dlog.error(pos, DiagnosticErrorCode.DUPLICATE_FLAGS, flag);
                return;
            }
            charList.add(flag);
        }
    }

    @Override
    public BLangNode transform(ReFlagsNode reFlagsNode) {
        StringBuilder flags = new StringBuilder();
        for (Node reFlag : reFlagsNode.reFlag()) {
            BLangLiteral flag = (BLangLiteral) reFlag.apply(this);
            flags.append(flag.value.toString());
        }
        return createStringLiteral(flags.toString(), getPosition(reFlagsNode));
    }

    private BLangSimpleVariable createSimpleVar(Optional<Token> name, Node type, NodeList<AnnotationNode> annotations) {
        if (name.isPresent()) {
            Token nameToken = name.get();
            return createSimpleVar(nameToken, type, null, null, annotations);
        }

        return createSimpleVar(null, type, null, null, annotations);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node type, NodeList<AnnotationNode> annotations) {
        return createSimpleVar(name, type, null, null, annotations);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node typeName, Node initializer,
                                                Token visibilityQualifier, NodeList<AnnotationNode> annotations) {
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

        if (initializer != null) {
            bLSimpleVar.setInitialExpression(createExpression(initializer));
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

    private BLangIdentifier createIdentifier(Location pos, Token token) {
        return createIdentifier(pos, token, false);
    }

    private BLangIdentifier createIdentifier(Location pos, Token token, boolean isXML) {
        if (token == null) {
            return createIdentifier(pos, (String) null);
        }

        String identifierName = token.text();
        if (token.isMissing() || identifierName.equals(IDENTIFIER_LITERAL_PREFIX)) {
            identifierName = missingNodesHelper.getNextMissingNodeName(packageID);
        } else if (!isXML && (identifierName.equals("_") || identifierName.equals(IDENTIFIER_LITERAL_PREFIX + "_"))) {
            dlog.error(pos, DiagnosticErrorCode.UNDERSCORE_NOT_ALLOWED_AS_IDENTIFIER);
            identifierName = missingNodesHelper.getNextMissingNodeName(packageID);
        }

        return createIdentifier(pos, identifierName);
    }

    private BLangIdentifier createIdentifier(Location pos, String value) {
        BLangIdentifier bLIdentifer = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        if (value == null) {
            return bLIdentifer;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            bLIdentifer.setValue(Utils.unescapeUnicodeCodepoints(value.substring(1)));
            bLIdentifer.setLiteral(true);
        } else {
            bLIdentifer.setValue(Utils.unescapeUnicodeCodepoints(value));
            bLIdentifer.setLiteral(false);
        }
        bLIdentifer.setOriginalValue(value);
        bLIdentifer.pos = pos;
        return bLIdentifer;
    }

    private BLangLiteral createEmptyStringLiteral(Location pos) {
        BLangLiteral bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        bLiteral.pos = pos;
        bLiteral.setBType(symTable.stringType);
        bLiteral.value = "";
        bLiteral.originalValue = "";
        return bLiteral;
    }

    private BLangLiteral createSimpleLiteral(Node literal) {
        return createSimpleLiteral(literal, this.isInFiniteContext);
    }

    private BLangExpression createLiteralOrExpression(Node literal) {
        if (literal.kind() == SyntaxKind.UNARY_EXPRESSION) {
            return createExpression(literal);
        }
        return createSimpleLiteral(literal, true);
    }

    private BLangLiteral createSimpleLiteral(Node literal, boolean isFiniteType) {
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

        //TODO: Verify all types, only string type tested
        if (type == SyntaxKind.NUMERIC_LITERAL) {
            SyntaxKind literalTokenKind = ((BasicLiteralNode) literal).literalToken().kind();
            NodeKind kind;
            if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN ||
                    literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
                kind = NodeKind.INTEGER_LITERAL;
                typeTag = TypeTags.INT;
                value = getIntegerLiteral(literal, textValue);
                originalValue = textValue;
                if (literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN && withinByteRange(value)) {
                    typeTag = TypeTags.BYTE;
                }
            } else if (literalTokenKind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN) {
                //TODO: Check effect of mapping negative(-) numbers as unary-expr
                kind = NodeKind.DECIMAL_FLOATING_POINT_LITERAL;
                typeTag = NumericLiteralSupport.isDecimalDiscriminated(textValue) ? TypeTags.DECIMAL : TypeTags.FLOAT;
                if (isFiniteType) {
                    value = textValue.replaceAll("[fd+]", "");
                    originalValue = textValue.replace("+", "");
                } else {
                    value = textValue;
                    originalValue = textValue;
                }
            } else {
                //TODO: Check effect of mapping negative(-) numbers as unary-expr
                kind = NodeKind.HEX_FLOATING_POINT_LITERAL;
                typeTag = TypeTags.FLOAT;
                value = getHexNodeValue(textValue);
                originalValue = textValue;
            }
            BLangNumericLiteral numericLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            numericLiteral.kind = kind;
            numericLiteral.pos = getPosition(literal);
            numericLiteral.setBType(symTable.getTypeFromTag(typeTag));
            numericLiteral.value = value;
            numericLiteral.originalValue = originalValue;
            return numericLiteral;
        } else if (type == SyntaxKind.BOOLEAN_LITERAL) {
            typeTag = TypeTags.BOOLEAN;
            value = Boolean.parseBoolean(textValue);
            originalValue = textValue;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.STRING_LITERAL || type == SyntaxKind.XML_TEXT_CONTENT ||
                type == SyntaxKind.TEMPLATE_STRING || type == SyntaxKind.IDENTIFIER_TOKEN ||
                isTokenInRegExp(type)) {
            String text = textValue;
            if (type == SyntaxKind.STRING_LITERAL) {
                if (text.length() > 1 && text.charAt(text.length() - 1) == '"') {
                    text = text.substring(1, text.length() - 1);
                } else {
                    // Missing end quote case
                    text = text.substring(1);
                }
            }

            if (type == SyntaxKind.IDENTIFIER_TOKEN && text.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
                text = text.substring(1);
            }

            if (type != SyntaxKind.TEMPLATE_STRING && type != SyntaxKind.XML_TEXT_CONTENT &&
                    !isTokenInRegExp(type)) {
                Location pos = getPosition(literal);
                validateUnicodePoints(text, pos);

                try {
                    text = Utils.unescapeBallerina(text);
                } catch (Exception e) {
                    // We may reach here when the string literal has syntax diagnostics.
                    // Therefore mock the compiler with an empty string.
                    text = "";
                }
            }

            typeTag = TypeTags.STRING;
            value = text;
            originalValue = textValue;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NIL_LITERAL) {
            originalValue = "()";
            typeTag = TypeTags.NIL;
            value = "()";
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        }  else if (type == SyntaxKind.NULL_LITERAL) {
            originalValue = "null";
            typeTag = TypeTags.NIL;
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
        bLiteral.setBType(symTable.getTypeFromTag(typeTag));
        bLiteral.getBType().tag = typeTag;
        bLiteral.value = value;
        bLiteral.originalValue = originalValue;
        return bLiteral;
    }

    private void validateUnicodePoints(String text, Location pos) {
        Matcher matcher = Utils.UNICODE_PATTERN.matcher(text);
        while (matcher.find()) {
            String leadingBackSlashes = matcher.group(1);
            if (Utils.isEscapedNumericEscape(leadingBackSlashes)) {
                // e.g. \\u{61}, \\\\u{61}
                continue;
            }

            String hexCodePoint = matcher.group(2);
            int decimalCodePoint = Integer.parseInt(hexCodePoint, 16);

            if ((decimalCodePoint >= Constants.MIN_UNICODE && decimalCodePoint <= Constants.MIDDLE_LIMIT_UNICODE)
                    || decimalCodePoint > Constants.MAX_UNICODE) {

                int offset = matcher.end(1);
                offset += "\\u{".length();
                BLangDiagnosticLocation numericEscapePos = new BLangDiagnosticLocation(currentCompUnitName,
                        pos.lineRange().startLine().line(),
                        pos.lineRange().endLine().line(),
                        pos.lineRange().startLine().offset() + offset,
                        pos.lineRange().startLine().offset() + offset + hexCodePoint.length());
                dlog.error(numericEscapePos, DiagnosticErrorCode.INVALID_UNICODE, hexCodePoint);
            }
        }
    }

    private BLangLiteral createStringLiteral(String value, Location pos) {
        BLangLiteral strLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        strLiteral.value = strLiteral.originalValue = value;
        strLiteral.setBType(symTable.stringType);
        strLiteral.pos = pos;
        return strLiteral;
    }

    private BLangType createTypeNode(Node type) {
        if (type instanceof BuiltinSimpleNameReferenceNode || type.kind() == SyntaxKind.NIL_TYPE_DESC) {
            return createBuiltInTypeNode(type);
        } else if (type.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE || type.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Exclusive type
            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            BLangIdentifier[] nameReference = createBLangNameReference(type);
            bLUserDefinedType.pkgAlias = nameReference[0];
            bLUserDefinedType.typeName = nameReference[1];
            bLUserDefinedType.pos = getPosition(type);
            return bLUserDefinedType;
        } else if (type.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            // Map name reference as a type
            if (type.hasDiagnostics()) {
                // if it hasDiagnostics then its missing type desc.
                BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
                BLangIdentifier pkgAlias = this.createIdentifier(null, "");
                BLangIdentifier name = this.createIdentifier(((SimpleNameReferenceNode) type).name());
                bLUserDefinedType.pkgAlias = pkgAlias;
                bLUserDefinedType.typeName = name;
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
                String name = missingNodesHelper.getNextMissingNodeName(packageID);
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

    private BLangInvocation createBLangInvocation(Node nameNode, NodeList<FunctionArgumentNode> arguments,
                                                  Location position, boolean isAsync) {
        BLangInvocation bLInvocation;
        if (isAsync) {
            bLInvocation = (BLangInvocation) TreeBuilder.createActionInvocation();
        } else {
            bLInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        }
        BLangIdentifier[] nameReference = createBLangNameReference(nameNode);
        bLInvocation.pkgAlias = nameReference[0];
        bLInvocation.name = nameReference[1];

        List<BLangExpression> args = new ArrayList<>();
        arguments.iterator().forEachRemaining(arg -> args.add(createExpression(arg)));
        bLInvocation.argExprs = args;
        bLInvocation.pos = position;
        return bLInvocation;
    }

    private BLangIdentifier[] createBLangNameReference(Node node) {
        switch (node.kind()) {
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode iNode = (QualifiedNameReferenceNode) node;
                Token modulePrefix = iNode.modulePrefix();
                IdentifierToken identifier = iNode.identifier();
                BLangIdentifier pkgAlias = this.createIdentifier(getPosition(modulePrefix), modulePrefix);
                Location namePos = getPosition(identifier);
                BLangIdentifier name = this.createIdentifier(namePos, identifier);
                return new BLangIdentifier[]{pkgAlias, name};
            case ERROR_TYPE_DESC:
                node = ((BuiltinSimpleNameReferenceNode) node).name();
                break;
            case NEW_KEYWORD:
            case IDENTIFIER_TOKEN:
            case ERROR_KEYWORD:
                break;
            case SIMPLE_NAME_REFERENCE:
            default:
                node = ((SimpleNameReferenceNode) node).name();
                break;
        }

        Token iToken = (Token) node;
        BLangIdentifier pkgAlias = this.createIdentifier(symTable.builtinPos, "");
        BLangIdentifier name = this.createIdentifier(iToken);
        return new BLangIdentifier[]{pkgAlias, name};
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
                    String parameterNameValue = parameterName.isMissing() ? "" :
                            Utils.unescapeUnicodeCodepoints(parameterName.text());
                    if (stringStartsWithSingleQuote(parameterNameValue)) {
                        parameterNameValue = parameterNameValue.substring(1);
                    }
                    paraName.value = parameterNameValue;
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
                case MARKDOWN_CODE_BLOCK:
                    MarkdownCodeBlockNode codeBlockNode = (MarkdownCodeBlockNode) singleDocLine;
                    transformCodeBlock(documentationLines, codeBlockNode);
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

    private void transformCodeBlock(LinkedList<BLangMarkdownDocumentationLine> documentationLines,
                                    MarkdownCodeBlockNode codeBlockNode) {
        BLangMarkdownDocumentationLine bLangDocLine =
                (BLangMarkdownDocumentationLine) TreeBuilder.createMarkdownDocumentationTextNode();

        StringBuilder docText = new StringBuilder();

        if (codeBlockNode.langAttribute().isPresent()) {
            docText.append(codeBlockNode.startBacktick().text());
            docText.append(codeBlockNode.langAttribute().get().toString());
        } else {
            docText.append(codeBlockNode.startBacktick().toString());
        }

        codeBlockNode.codeLines().forEach(codeLine -> docText.append(codeLine.codeDescription().toString()));
        docText.append(codeBlockNode.endBacktick().text());

        bLangDocLine.text = docText.toString();
        bLangDocLine.pos = getPosition(codeBlockNode.startLineHashToken());
        documentationLines.add(bLangDocLine);
    }

    private String addReferencesAndReturnDocumentationText(LinkedList<BLangMarkdownReferenceDocumentation> references,
                                                           NodeList<Node> docElements) {
        StringBuilder docText = new StringBuilder();
        for (Node element : docElements) {
            if (element.kind() == SyntaxKind.BALLERINA_NAME_REFERENCE) {
                BLangMarkdownReferenceDocumentation bLangRefDoc = new BLangMarkdownReferenceDocumentation();
                BallerinaNameReferenceNode balNameRefNode = (BallerinaNameReferenceNode) element;

                bLangRefDoc.pos = getPosition(balNameRefNode);

                Token startBacktick = balNameRefNode.startBacktick();
                Node backtickContent = balNameRefNode.nameReference();
                Token endBacktick = balNameRefNode.endBacktick();

                String contentString = backtickContent.isMissing() ? "" : backtickContent.toString();
                bLangRefDoc.referenceName = contentString;

                bLangRefDoc.type = DocumentationReferenceType.BACKTICK_CONTENT;
                Optional<Token> referenceType = balNameRefNode.referenceType();
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
            } else if (element.kind() == SyntaxKind.INLINE_CODE_REFERENCE) {
                InlineCodeReferenceNode inlineCodeRefNode = (InlineCodeReferenceNode) element;
                docText.append(inlineCodeRefNode.startBacktick().text());
                docText.append(inlineCodeRefNode.codeReference().text());
                docText.append(inlineCodeRefNode.endBacktick().text());
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
            case CODE_CONTENT:
                // reaching here means ballerina name reference is syntactically invalid.
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
        if (bLangRefDoc.identifier != null) {
            bLangRefDoc.identifier = Utils.unescapeUnicodeCodepoints(bLangRefDoc.identifier);
            if (stringStartsWithSingleQuote(bLangRefDoc.identifier)) {
                bLangRefDoc.identifier = bLangRefDoc.identifier.substring(1);
            }
        }
        if (bLangRefDoc.qualifier != null) {
            bLangRefDoc.qualifier = Utils.unescapeUnicodeCodepoints(bLangRefDoc.qualifier);
            if (stringStartsWithSingleQuote(bLangRefDoc.qualifier)) {
                bLangRefDoc.qualifier = bLangRefDoc.qualifier.substring(1);
            }
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

    private Object getIntegerLiteral(Node literal, String nodeValue) {
        SyntaxKind literalTokenKind = ((BasicLiteralNode) literal).literalToken().kind();
        if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
            return parseLong(nodeValue, nodeValue, 10);
        } else if (literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
            String processedNodeValue = nodeValue.toLowerCase().replace("0x", "");
            return parseLong(nodeValue, processedNodeValue, 16);
        }
        return null;
    }

    private Object parseLong(String originalNodeValue, String processedNodeValue, int radix) {
        try {
            return Long.parseLong(processedNodeValue, radix);
        } catch (NumberFormatException e) {
            try {
                Double val = Double.parseDouble(processedNodeValue);
                if (!Double.isInfinite(val)) {
                    return val;
                } else {
                    // Out of range values for Java Long and Double will be returned as a string and evaluated in
                    // the TypeChecker.
                    // This handles decimal type out of range values
                    return originalNodeValue;
                }
            } catch (NumberFormatException f) {
                // To handle Out of range values for Java Long and Double for hex type values
                return originalNodeValue;
            }
        }
    }

    private String getHexNodeValue(String value) {
        if (!(value.contains("p") || value.contains("P"))) {
            value = value + "p0";
        }
        return value;
    }

    private void markVariableWithFlag(BLangVariable variable, Flag flag) {
        // Set the flag to the variable.
        variable.flagSet.add(flag);

        switch (variable.getKind()) {
            case TUPLE_VARIABLE:
                // If the variable is a tuple variable, we need to set the flag to the all member variables.
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                for (BLangVariable var : tupleVariable.memberVariables) {
                    markVariableWithFlag(var, flag);
                }
                if (tupleVariable.restVariable != null) {
                    markVariableWithFlag(tupleVariable.restVariable, flag);
                }
                break;
            case RECORD_VARIABLE:
                // If the variable is a record variable, we need to set the flag to the all the variables in
                // the record.
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                for (BLangRecordVariableKeyValue keyValue : recordVariable.variableList) {
                    markVariableWithFlag(keyValue.getValue(), flag);
                }
                if (recordVariable.restParam != null) {
                    markVariableWithFlag(recordVariable.restParam, flag);
                }
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                BLangSimpleVariable message = errorVariable.message;
                if (message != null) {
                    markVariableWithFlag(message, flag);
                }

                BLangVariable cause = errorVariable.cause;
                if (cause != null) {
                    markVariableWithFlag(cause, flag);
                }

                errorVariable.detail.forEach(entry -> markVariableWithFlag(entry.valueBindingPattern, flag));
                if (errorVariable.restDetail != null) {
                    markVariableWithFlag(errorVariable.restDetail, flag);
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
            case MAP_TYPE_DESC:
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

    private boolean isInTypeDefinitionContext(Node parent) {
        while (parent != null) {
            if (parent instanceof TypeDefinitionNode) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
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
        SyntaxKind parentKind = node.parent().kind();
        // public type CustomType distinct object {...}; => return false
        // public type CustomType readonly & distinct object {...}; => return true
        if (node.kind() == SyntaxKind.OBJECT_TYPE_DESC && parentKind == SyntaxKind.DISTINCT_TYPE_DESC) {
            if (node.parent().parent() != null) {
                return node.parent().parent().kind() != SyntaxKind.TYPE_DEFINITION;
            }
        }
        return parentKind != SyntaxKind.DISTINCT_TYPE_DESC && parentKind != SyntaxKind.TYPE_DEFINITION;
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
        Location pos = getPosition(recordTypeDescriptorNode);
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(this.packageID, this.anonTypeNameSuffixes);
        IdentifierNode anonTypeGenName = createIdentifier(symTable.builtinPos, genName);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);

        typeDef.typeNode = recordTypeNode;
        typeDef.pos = pos;
        addToTop(typeDef);
        return createUserDefinedType(pos, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name);
    }

    private BLangUserDefinedType createUserDefinedType(Location pos,
                                                       BLangIdentifier pkgAlias,
                                                       BLangIdentifier name) {
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = pos;
        userDefinedType.pkgAlias = pkgAlias;
        userDefinedType.typeName = name;
        return userDefinedType;
    }
    
    private BLangListConstructorSpreadOpExpr createSpreadMemberExpr(Node expr, Location pos) {
        BLangExpression bLangExpr = createExpression(expr);
        BLangListConstructorSpreadOpExpr spreadOpExpr = new BLangListConstructorSpreadOpExpr();
        spreadOpExpr.setExpression(bLangExpr);
        spreadOpExpr.pos = pos;
        return spreadOpExpr;
    }

    private boolean withinByteRange(Object num) {
        if (num instanceof Long) {
            return (Long) num <= 255 && (Long) num >= 0;
        }
        return false;
    }

    private class SimpleVarBuilder {
        private BLangIdentifier name;
        private BLangType type;
        private boolean isDeclaredWithVar;
        private Set<Flag> flags = new HashSet<>();
        private boolean isFinal;
        private ExpressionNode expr;
        private Location pos;

        public BLangSimpleVariable build() {
            BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            bLSimpleVar.setName(this.name);
            bLSimpleVar.setTypeNode(this.type);
            bLSimpleVar.isDeclaredWithVar = this.isDeclaredWithVar;
            bLSimpleVar.setTypeNode(this.type);
            bLSimpleVar.flagSet.addAll(this.flags);
            if (this.isFinal) {
                markVariableWithFlag(bLSimpleVar, Flag.FINAL);
            }
            bLSimpleVar.setInitialExpression(this.expr);
            bLSimpleVar.pos = pos;
            return bLSimpleVar;
        }

        public SimpleVarBuilder with(String name) {
            this.name = createIdentifier(null, name);
            return this;
        }

        public SimpleVarBuilder with(String name, Location identifierPos) {
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

        public SimpleVarBuilder setPos(Location pos) {
            this.pos = pos;
            return this;
        }
    }

    private void addFinalQualifier(BLangSimpleVariable simpleVar) {
        simpleVar.flagSet.add(Flag.FINAL);
    }

    private void addToTop(TopLevelNode topLevelNode) {
        if (currentCompilationUnit != null) {
            currentCompilationUnit.addTopLevelNode(topLevelNode);
        }
    }

    private Location expandLeft(Location location, Location upTo) {
//      pos        |------------|
//      upTo    |-----..
//      result  |---------------|

        assert location.lineRange().startLine().line() > upTo.lineRange().startLine().line() ||
               (location.lineRange().startLine().line() == upTo.lineRange().startLine().line() &&
                       location.lineRange().startLine().offset() >= upTo.lineRange().startLine().offset());

        Location expandedLocation = new BLangDiagnosticLocation(location.lineRange().filePath(),
                upTo.lineRange().startLine().line(),
                location.lineRange().endLine().line(),
                upTo.lineRange().startLine().offset(),
                location.lineRange().endLine().offset());

        return expandedLocation;
    }

    private void setClassQualifiers(NodeList<Token> qualifiers, BLangClassDefinition blangClass) {
        for (Token qualifier : qualifiers) {
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
    }
}
