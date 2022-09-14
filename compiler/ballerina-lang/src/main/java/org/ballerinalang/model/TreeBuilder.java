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
package org.ballerinalang.model;

import org.ballerinalang.model.clauses.DoClauseNode;
import org.ballerinalang.model.clauses.InputClauseNode;
import org.ballerinalang.model.clauses.LetClauseNode;
import org.ballerinalang.model.clauses.LimitClauseNode;
import org.ballerinalang.model.clauses.MatchClauseNode;
import org.ballerinalang.model.clauses.OnClauseNode;
import org.ballerinalang.model.clauses.OnConflictClauseNode;
import org.ballerinalang.model.clauses.OnFailClauseNode;
import org.ballerinalang.model.clauses.OrderByClauseNode;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.clauses.SelectClauseNode;
import org.ballerinalang.model.clauses.WhereClauseNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.BlockFunctionBodyNode;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ErrorVariableNode;
import org.ballerinalang.model.tree.FunctionBodyNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.MarkdownDocumentationReferenceAttributeNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.RecordVariableNode;
import org.ballerinalang.model.tree.RetrySpecNode;
import org.ballerinalang.model.tree.RetryTransactionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TableKeySpecifierNode;
import org.ballerinalang.model.tree.TupleVariableNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.model.tree.bindingpattern.CaptureBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.ErrorBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.ErrorCauseBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.ErrorFieldBindingPatternsNode;
import org.ballerinalang.model.tree.bindingpattern.ErrorMessageBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.FieldBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.ListBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.MappingBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.NamedArgBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.RestBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.SimpleBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.WildCardBindingPatternNode;
import org.ballerinalang.model.tree.expressions.AnnotAccessNode;
import org.ballerinalang.model.tree.expressions.ArrowFunctionNode;
import org.ballerinalang.model.tree.expressions.BinaryExpressionNode;
import org.ballerinalang.model.tree.expressions.CheckPanickedExpressionNode;
import org.ballerinalang.model.tree.expressions.CheckedExpressionNode;
import org.ballerinalang.model.tree.expressions.ElvisExpressionNode;
import org.ballerinalang.model.tree.expressions.ErrorConstructorExpressionNode;
import org.ballerinalang.model.tree.expressions.ErrorVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.FieldBasedAccessNode;
import org.ballerinalang.model.tree.expressions.GroupExpressionNode;
import org.ballerinalang.model.tree.expressions.IgnoreNode;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.ballerinalang.model.tree.expressions.InferredTypedescDefaultNode;
import org.ballerinalang.model.tree.expressions.InvocationNode;
import org.ballerinalang.model.tree.expressions.IsLikeExpressionNode;
import org.ballerinalang.model.tree.expressions.LambdaFunctionNode;
import org.ballerinalang.model.tree.expressions.LetExpressionNode;
import org.ballerinalang.model.tree.expressions.ListConstructorExprNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.MarkDownDocumentationDeprecatedParametersAttributeNode;
import org.ballerinalang.model.tree.expressions.MarkDownDocumentationDeprecationAttributeNode;
import org.ballerinalang.model.tree.expressions.MarkdownDocumentationParameterAttributeNode;
import org.ballerinalang.model.tree.expressions.MarkdownDocumentationReturnParameterAttributeNode;
import org.ballerinalang.model.tree.expressions.MarkdownDocumentationTextAttributeNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.tree.expressions.QueryExpressionNode;
import org.ballerinalang.model.tree.expressions.RawTemplateLiteralNode;
import org.ballerinalang.model.tree.expressions.ReAssertionNode;
import org.ballerinalang.model.tree.expressions.ReAtomCharOrEscapeNode;
import org.ballerinalang.model.tree.expressions.ReAtomQuantifierNode;
import org.ballerinalang.model.tree.expressions.ReCapturingGroupsNode;
import org.ballerinalang.model.tree.expressions.ReCharSetNode;
import org.ballerinalang.model.tree.expressions.ReCharSetRangeNode;
import org.ballerinalang.model.tree.expressions.ReCharacterClassNode;
import org.ballerinalang.model.tree.expressions.ReDisjunctionNode;
import org.ballerinalang.model.tree.expressions.ReFlagExpressionNode;
import org.ballerinalang.model.tree.expressions.ReFlagsOnOffNode;
import org.ballerinalang.model.tree.expressions.ReQuantifierNode;
import org.ballerinalang.model.tree.expressions.ReSequenceNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.RecordVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.RegExpTemplateLiteralNode;
import org.ballerinalang.model.tree.expressions.RestArgsNode;
import org.ballerinalang.model.tree.expressions.ServiceConstructorNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.StatementExpressionNode;
import org.ballerinalang.model.tree.expressions.StringTemplateLiteralNode;
import org.ballerinalang.model.tree.expressions.TableConstructorExprNode;
import org.ballerinalang.model.tree.expressions.TernaryExpressionNode;
import org.ballerinalang.model.tree.expressions.TrapExpressionNode;
import org.ballerinalang.model.tree.expressions.TupleVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.TypeConversionNode;
import org.ballerinalang.model.tree.expressions.TypeInitNode;
import org.ballerinalang.model.tree.expressions.TypeTestExpressionNode;
import org.ballerinalang.model.tree.expressions.TypedescExpressionNode;
import org.ballerinalang.model.tree.expressions.UnaryExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLCommentLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLElementLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLProcessingInstructionLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLQNameNode;
import org.ballerinalang.model.tree.expressions.XMLQuotedStringNode;
import org.ballerinalang.model.tree.expressions.XMLTextLiteralNode;
import org.ballerinalang.model.tree.matchpatterns.ConstPatternNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorCauseMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorFieldMatchPatternsNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorMessageMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.FieldMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.ListMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.MappingMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.NamedArgMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.RestMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.SimpleMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.VarBindingPatternMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.WildCardMatchPatternNode;
import org.ballerinalang.model.tree.statements.AssignmentNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.BreakNode;
import org.ballerinalang.model.tree.statements.CompoundAssignmentNode;
import org.ballerinalang.model.tree.statements.ConstantNode;
import org.ballerinalang.model.tree.statements.ContinueNode;
import org.ballerinalang.model.tree.statements.DoNode;
import org.ballerinalang.model.tree.statements.ErrorDestructureNode;
import org.ballerinalang.model.tree.statements.ExpressionStatementNode;
import org.ballerinalang.model.tree.statements.FailStatementNode;
import org.ballerinalang.model.tree.statements.ForeachNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.LockNode;
import org.ballerinalang.model.tree.statements.MatchStatementNode;
import org.ballerinalang.model.tree.statements.PanicNode;
import org.ballerinalang.model.tree.statements.QueryActionNode;
import org.ballerinalang.model.tree.statements.RecordDestructureNode;
import org.ballerinalang.model.tree.statements.RetryNode;
import org.ballerinalang.model.tree.statements.ReturnNode;
import org.ballerinalang.model.tree.statements.RollbackNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.TupleDestructureNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.statements.WhileNode;
import org.ballerinalang.model.tree.statements.WorkerReceiveNode;
import org.ballerinalang.model.tree.statements.WorkerSendNode;
import org.ballerinalang.model.tree.statements.XMLNSDeclStatementNode;
import org.ballerinalang.model.tree.types.ArrayTypeNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.tree.types.ConstrainedTypeNode;
import org.ballerinalang.model.tree.types.ErrorTypeNode;
import org.ballerinalang.model.tree.types.FiniteTypeNode;
import org.ballerinalang.model.tree.types.FunctionTypeNode;
import org.ballerinalang.model.tree.types.IntersectionTypeNode;
import org.ballerinalang.model.tree.types.ObjectTypeNode;
import org.ballerinalang.model.tree.types.RecordTypeNode;
import org.ballerinalang.model.tree.types.StreamTypeNode;
import org.ballerinalang.model.tree.types.TableTypeNode;
import org.ballerinalang.model.tree.types.TupleTypeNode;
import org.ballerinalang.model.tree.types.UnionTypeNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.model.tree.types.ValueTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * This contains the functionality of building the nodes in the AST.
 *
 * @since 0.94
 */
public class TreeBuilder {

    public static CompilationUnitNode createCompilationUnit() {
        return new BLangCompilationUnit();
    }

    public static PackageNode createPackageNode() {
        return new BLangPackage();
    }

    public static BLangTestablePackage createTestablePackageNode() {
        return new BLangTestablePackage();
    }

    public static IdentifierNode createIdentifierNode() {
        return new BLangIdentifier();
    }

    public static ImportPackageNode createImportPackageNode() {
        return new BLangImportPackage();
    }

    public static XMLNSDeclarationNode createXMLNSNode() {
        return new BLangXMLNS();
    }

    public static XMLNSDeclStatementNode createXMLNSDeclrStatementNode() {
        return new BLangXMLNSStatement();
    }

    public static SimpleVariableNode createSimpleVariableNode() {
        return new BLangSimpleVariable();
    }

    public static BLangLetVariable createLetVariableNode() {
        return new BLangLetVariable();
    }

    public static TupleVariableNode createTupleVariableNode() {
        return new BLangTupleVariable();
    }

    public static RecordVariableNode createRecordVariableNode() {
        return new BLangRecordVariable();
    }

    public static ErrorVariableNode createErrorVariableNode() {
        return new BLangErrorVariable();
    }

    public static FunctionNode createFunctionNode() {
        return new BLangFunction();
    }

    public static BlockFunctionBodyNode createBlockFunctionBodyNode() {
        return new BLangBlockFunctionBody();
    }

    public static FunctionBodyNode createExprFunctionBodyNode() {
        return new BLangExprFunctionBody();
    }

    public static FunctionBodyNode createExternFunctionBodyNode() {
        return new BLangExternalFunctionBody();
    }

    public static BlockStatementNode createBlockNode() {
        return new BLangBlockStmt();
    }

    public static PanicNode createPanicNode() {
        return new BLangPanic();
    }

    public static TupleDestructureNode createTupleDestructureStatementNode() {
        return new BLangTupleDestructure();
    }

    public static RecordDestructureNode createRecordDestructureStatementNode() {
        return new BLangRecordDestructure();
    }

    public static ErrorDestructureNode createErrorDestructureStatementNode() {
        return new BLangErrorDestructure();
    }

    public static ErrorConstructorExpressionNode createErrorConstructorExpressionNode() {
        return new BLangErrorConstructorExpr();
    }

    public static ExpressionStatementNode createExpressionStatementNode() {
        return new BLangExpressionStmt();
    }

    public static LiteralNode createLiteralExpression() {
        return new BLangLiteral();
    }

    public static LiteralNode createConstLiteralNode() {
        return new BLangConstRef();
    }

    public static LiteralNode createNumericLiteralExpression() {
        return new BLangNumericLiteral();
    }

    public static ConstantNode createConstantNode() {
        return new BLangConstant();
    }

    public static RecordLiteralNode createRecordLiteralNode() {
        return new BLangRecordLiteral();
    }

    public static RecordLiteralNode.RecordKeyValueFieldNode createRecordKeyValue() {
        return new BLangRecordLiteral.BLangRecordKeyValueField();
    }

    public static RecordLiteralNode.RecordSpreadOperatorFieldNode createRecordSpreadOperatorField() {
        return new BLangRecordLiteral.BLangRecordSpreadOperatorField();
    }

    public static VariableDefinitionNode createSimpleVariableDefinitionNode() {
        return new BLangSimpleVariableDef();
    }

    public static VariableDefinitionNode createTupleVariableDefinitionNode() {
        return new BLangTupleVariableDef();
    }

    public static VariableDefinitionNode createRecordVariableDefinitionNode() {
        return new BLangRecordVariableDef();
    }

    public static VariableDefinitionNode createErrorVariableDefinitionNode() {
        return new BLangErrorVariableDef();
    }

    public static ValueTypeNode createValueTypeNode() {
        return new BLangValueType();
    }

    public static ArrayTypeNode createArrayTypeNode() {
        return new BLangArrayType();
    }

    public static UnionTypeNode createUnionTypeNode() {
        return new BLangUnionTypeNode();
    }

    public static IntersectionTypeNode createIntersectionTypeNode() {
        return new BLangIntersectionTypeNode();
    }

    public static FiniteTypeNode createFiniteTypeNode() {
        return new BLangFiniteTypeNode();
    }

    public static TupleTypeNode createTupleTypeNode() {
        return new BLangTupleTypeNode();
    }

    public static UserDefinedTypeNode createUserDefinedTypeNode() {
        return new BLangUserDefinedType();
    }

    public static BuiltInReferenceTypeNode createBuiltInReferenceTypeNode() {
        return new BLangBuiltInRefTypeNode();
    }

    public static ErrorTypeNode createErrorTypeNode() {
        return new BLangErrorType();
    }

    public static ConstrainedTypeNode createConstrainedTypeNode() {
        return new BLangConstrainedType();
    }

    public static StreamTypeNode createStreamTypeNode() {
        return new BLangStreamType();
    }

    public static FunctionTypeNode createFunctionTypeNode() {
        return new BLangFunctionTypeNode();
    }

    public static RecordTypeNode createRecordTypeNode() {
        return new BLangRecordTypeNode();
    }

    public static TupleVariableReferenceNode createTupleVariableReferenceNode() {
        return new BLangTupleVarRef();
    }

    public static RecordVariableReferenceNode createRecordVariableReferenceNode() {
        return new BLangRecordVarRef();
    }

    public static ErrorVariableReferenceNode createErrorVariableReferenceNode() {
        return new BLangErrorVarRef();
    }

    public static SimpleVariableReferenceNode createSimpleVariableReferenceNode() {
        return new BLangSimpleVarRef();
    }

    public static RecordLiteralNode.RecordVarNameFieldNode createRecordVarRefNameFieldNode() {
        return new BLangRecordLiteral.BLangRecordVarNameField();
    }

    public static InvocationNode createInvocationNode() {
        return new BLangInvocation();
    }

    public static InvocationNode createActionInvocation() {
        return new BLangInvocation.BLangActionInvocation();
    }

    public static FieldBasedAccessNode createFieldBasedAccessNode() {
        return new BLangFieldBasedAccess();
    }

    public static FieldBasedAccessNode createFieldBasedAccessWithPrefixNode() {
        return new BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess();
    }

    public static TableKeySpecifierNode createTableKeySpecifierNode() {
        return new BLangTableKeySpecifier();
    }

    public static TableTypeNode createTableTypeNode() {
        return new BLangTableTypeNode();
    }

    public static IndexBasedAccessNode createIndexBasedAccessNode() {
        return new BLangIndexBasedAccess();
    }

    public static TernaryExpressionNode createTernaryExpressionNode() {
        return new BLangTernaryExpr();
    }

    public static CheckedExpressionNode createCheckExpressionNode() {
        return new BLangCheckedExpr();
    }

    public static FailStatementNode createFailNode() {
        return new BLangFail();
    }

    public static QueryExpressionNode createQueryExpressionNode() {
        return new BLangQueryExpr();
    }

    public static CheckPanickedExpressionNode createCheckPanicExpressionNode() {
        return new BLangCheckPanickedExpr();
    }

    public static BLangWaitExpr createWaitExpressionNode() {
        return new BLangWaitExpr();
    }

    public static TrapExpressionNode createTrapExpressionNode() {
        return new BLangTrapExpr();
    }

    public static BinaryExpressionNode createBinaryExpressionNode() {
        return new BLangBinaryExpr();
    }

    public static ElvisExpressionNode createElvisExpressionNode() {
        return new BLangElvisExpr();
    }

    public static GroupExpressionNode createGroupExpressionNode() {
        return new BLangGroupExpr();
    }

    public static ListConstructorExprNode createListConstructorExpressionNode() {
        return new BLangListConstructorExpr();
    }

    public static TableConstructorExprNode createTableConstructorExpressionNode() {
        return new BLangTableConstructorExpr();
    }

    public static LetExpressionNode createLetExpressionNode() {
        return new BLangLetExpression();
    }

    public static ListConstructorExprNode createTupleLiteralExpressionNode() {
        return new BLangListConstructorExpr.BLangTupleLiteral();
    }

    public static ListConstructorExprNode createArrayLiteralExpressionNode() {
        return new BLangListConstructorExpr.BLangArrayLiteral();
    }

    public static TypedescExpressionNode createTypeAccessNode() {
        return new BLangTypedescExpr();
    }

    public static TypeConversionNode createTypeConversionNode() {
        return new BLangTypeConversionExpr();
    }

    public static UnaryExpressionNode createUnaryExpressionNode() {
        return new BLangUnaryExpr();
    }

    public static LambdaFunctionNode createLambdaFunctionNode() {
        return new BLangLambdaFunction();
    }

    public static ArrowFunctionNode createArrowFunctionNode() {
        return new BLangArrowFunction();
    }

    public static TypeInitNode createInitNode() {
        return new BLangTypeInit();
    }

    public static ServiceConstructorNode createServiceConstructorNode() {
        return new BLangServiceConstructorExpr();
    }

    public static ObjectTypeNode createObjectTypeNode() {
        return new BLangObjectTypeNode();
    }

    public static TypeDefinition createTypeDefinition() {
        return new BLangTypeDefinition();
    }

    public static AssignmentNode createAssignmentNode() {
        return new BLangAssignment();
    }

    public static CompoundAssignmentNode createCompoundAssignmentNode() {
        return new BLangCompoundAssignment();
    }

    public static RetryNode createRetryNode() {
        return new BLangRetry();
    }

    public static RetryTransactionNode createRetryTransactionNode() {
        return new BLangRetryTransaction();
    }

    public static RetrySpecNode createRetrySpecNode() {
        return new BLangRetrySpec();
    }

    public static TransactionNode createTransactionNode() {
        return new BLangTransaction();
    }

    public static ReturnNode createReturnNode() {
        return new BLangReturn();
    }

    public static RollbackNode createRollbackNode() {
        return new BLangRollback();
    }

    public static AnnotationNode createAnnotationNode() {
        return new BLangAnnotation();
    }

    public static AnnotationAttachmentNode createAnnotAttachmentNode() {
        return new BLangAnnotationAttachment();
    }

    public static MarkdownDocumentationNode createMarkdownDocumentationNode() {
        return new BLangMarkdownDocumentation();
    }

    public static MarkdownDocumentationTextAttributeNode createMarkdownDocumentationTextNode() {
        return new BLangMarkdownDocumentationLine();
    }

    public static MarkdownDocumentationParameterAttributeNode createMarkdownParameterDocumentationNode() {
        return new BLangMarkdownParameterDocumentation();
    }

    public static MarkdownDocumentationReferenceAttributeNode createMarkdownReferenceDocumentationNode() {
        return new BLangMarkdownReferenceDocumentation();
    }

    public static MarkdownDocumentationReturnParameterAttributeNode createMarkdownReturnParameterDocumentationNode() {
        return new BLangMarkdownReturnParameterDocumentation();
    }

    public static MarkDownDocumentationDeprecationAttributeNode createMarkDownDeprecationAttributeNode() {
        return new BLangMarkDownDeprecationDocumentation();
    }

    public static MarkDownDocumentationDeprecatedParametersAttributeNode
    createMarkDownDeprecatedParametersAttributeNode() {
        return new BLangMarkDownDeprecatedParametersDocumentation();
    }

    public static IfNode createIfElseStatementNode() {
        return new BLangIf();
    }

    public static MatchStatementNode createMatchStatementNode() {
        return new BLangMatchStatement();
    }

    public static WildCardMatchPatternNode createWildCardMatchPattern() {
        return new BLangWildCardMatchPattern();
    }

    public static ConstPatternNode createConstMatchPattern() {
        return new BLangConstPattern();
    }

    public static VarBindingPatternMatchPatternNode createVarBindingPattern() {
        return new BLangVarBindingPatternMatchPattern();
    }

    public static ListMatchPatternNode createListMatchPattern() {
        return new BLangListMatchPattern();
    }

    public static MappingMatchPatternNode createMappingMatchPattern() {
        return new BLangMappingMatchPattern();
    }

    public static RestMatchPatternNode createRestMatchPattern() {
        return new BLangRestMatchPattern();
    }

    public static FieldMatchPatternNode createFieldMatchPattern() {
        return new BLangFieldMatchPattern();
    }

    public static MatchClauseNode createMatchClause() {
        return new BLangMatchClause();
    }

    public static ExpressionNode createMatchGuard() {
        return new BLangMatchGuard();
    }

    public static WildCardBindingPatternNode createWildCardBindingPattern() {
        return new BLangWildCardBindingPattern();
    }

    public static CaptureBindingPatternNode createCaptureBindingPattern() {
        return new BLangCaptureBindingPattern();
    }

    public static RestBindingPatternNode createRestBindingPattern() {
        return new BLangRestBindingPattern();
    }

    public static ListBindingPatternNode createListBindingPattern() {
        return new BLangListBindingPattern();
    }

    public static MappingBindingPatternNode createMappingBindingPattern() {
        return new BLangMappingBindingPattern();
    }

    public static FieldBindingPatternNode createFieldBindingPattern() {
        return new BLangFieldBindingPattern();
    }

    public static ErrorBindingPatternNode createErrorBindingPattern() {
        return new BLangErrorBindingPattern();
    }

    public static ErrorMessageBindingPatternNode createErrorMessageBindingPattern() {
        return new BLangErrorMessageBindingPattern();
    }

    public static ErrorCauseBindingPatternNode createErrorCauseBindingPattern() {
        return new BLangErrorCauseBindingPattern();
    }

    public static ErrorFieldBindingPatternsNode createErrorFieldBindingPattern() {
        return new BLangErrorFieldBindingPatterns();
    }

    public static SimpleBindingPatternNode createSimpleBindingPattern() {
        return new BLangSimpleBindingPattern();
    }

    public static NamedArgBindingPatternNode createNamedArgBindingPattern() {
        return new BLangNamedArgBindingPattern();
    }

    public static ErrorMatchPatternNode createErrorMatchPattern() {
        return new BLangErrorMatchPattern();
    }

    public static ErrorMessageMatchPatternNode createErrorMessageMatchPattern() {
        return new BLangErrorMessageMatchPattern();
    }

    public static ErrorCauseMatchPatternNode createErrorCauseMatchPattern() {
        return new BLangErrorCauseMatchPattern();
    }

    public static ErrorFieldMatchPatternsNode createErrorFieldMatchPattern() {
        return new BLangErrorFieldMatchPatterns();
    }

    public static SimpleMatchPatternNode createSimpleMatchPattern() {
        return new BLangSimpleMatchPattern();
    }

    public static NamedArgMatchPatternNode createNamedArgMatchPattern() {
        return new BLangNamedArgMatchPattern();
    }

    public static ServiceNode createServiceNode() {
        return new BLangService();
    }

    public static WorkerReceiveNode createWorkerReceiveNode() {
        return new BLangWorkerReceive();
    }

    public static WorkerSendNode createWorkerSendNode() {
        return new BLangWorkerSend();
    }

    public static ForkJoinNode createForkJoinNode() {
        return new BLangForkJoin();
    }

    public static ForeachNode createForeachNode() {
        return new BLangForeach();
    }

    public static DoNode createDoNode() {
        return new BLangDo();
    }

    public static InputClauseNode createFromClauseNode() {
        return new BLangFromClause();
    }

    public static InputClauseNode createJoinClauseNode() {
        return new BLangJoinClause();
    }

    public static LetClauseNode createLetClauseNode() {
        return new BLangLetClause();
    }

    public static OnClauseNode createOnClauseNode() {
        return new BLangOnClause();
    }

    public static OrderKeyNode createOrderKeyNode() {
        return new BLangOrderKey();
    }

    public static OrderByClauseNode createOrderByClauseNode() {
        return new BLangOrderByClause();
    }

    public static SelectClauseNode createSelectClauseNode() {
        return new BLangSelectClause();
    }

    public static OnConflictClauseNode createOnConflictClauseNode() {
        return new BLangOnConflictClause();
    }

    public static DoClauseNode createDoClauseNode() {
        return new BLangDoClause();
    }

    public static OnFailClauseNode createOnFailClauseNode() {
        return new BLangOnFailClause();
    }

    public static LimitClauseNode createLimitClauseNode() {
        return new BLangLimitClause();
    }

    public static QueryActionNode createQueryActionNode() {
        return new BLangQueryAction();
    }

    public static WhereClauseNode createWhereClauseNode() {
        return new BLangWhereClause();
    }

    public static WhileNode createWhileNode() {
        return new BLangWhile();
    }

    public static LockNode createLockNode() {
        return new BLangLock();
    }

    public static ContinueNode createContinueNode() {
        return new BLangContinue();
    }

    public static BreakNode createBreakNode() {
        return new BLangBreak();
    }

    public static XMLQNameNode createXMLQNameNode() {
        return new BLangXMLQName();
    }

    public static XMLAttributeNode createXMLAttributeNode() {
        return new BLangXMLAttribute();
    }

    public static XMLElementLiteralNode createXMLElementLiteralNode() {
        return new BLangXMLElementLiteral();
    }

    public static XMLTextLiteralNode createXMLTextLiteralNode() {
        return new BLangXMLTextLiteral();
    }

    public static XMLLiteralNode createXMLSequenceLiteralNode() {
        return new BLangXMLSequenceLiteral();
    }

    public static XMLCommentLiteralNode createXMLCommentLiteralNode() {
        return new BLangXMLCommentLiteral();
    }

    public static XMLProcessingInstructionLiteralNode createXMLProcessingIntsructionLiteralNode() {
        return new BLangXMLProcInsLiteral();
    }

    public static XMLQuotedStringNode createXMLQuotedStringNode() {
        return new BLangXMLQuotedString();
    }

    public static StringTemplateLiteralNode createStringTemplateLiteralNode() {
        return new BLangStringTemplateLiteral();
    }

    public static RegExpTemplateLiteralNode createRegExpTemplateLiteralNode() {
        return new BLangRegExpTemplateLiteral();
    }

    public static ReDisjunctionNode createReDisjunctionNode() {
        return new BLangReDisjunction();
    }

    public static ReSequenceNode createReSequenceNode() {
        return new BLangReSequence();
    }

    public static ReAtomQuantifierNode createReAtomQuantifierNode() {
        return new BLangReAtomQuantifier();
    }

    public static ReCharacterClassNode createReCharacterClassNode() {
        return new BLangReCharacterClass();
    }

    public static ReCharSetNode createReCharSetNode() {
        return new BLangReCharSet();
    }

    public static ReCharSetRangeNode createReCharSetRangeNode() {
        return new BLangReCharSetRange();
    }

    public static ReQuantifierNode createReQuantifierNode() {
        return new BLangReQuantifier();
    }

    public static ReAtomCharOrEscapeNode createReAtomCharOrEscapeNode() {
        return new BLangReAtomCharOrEscape();
    }

    public static ReAssertionNode createReAssertionNode() {
        return new BLangReAssertion();
    }

    public static ReCapturingGroupsNode createReCapturingGroupsNode() {
        return new BLangReCapturingGroups();
    }

    public static ReFlagExpressionNode createReFlagExpressionNode() {
        return new BLangReFlagExpression();
    }

    public static ReFlagsOnOffNode createReFlagsOnOffNode() {
        return new BLangReFlagsOnOff();
    }

    public static RawTemplateLiteralNode createRawTemplateLiteralNode() {
        return new BLangRawTemplateLiteral();
    }

    public static RestArgsNode createVarArgsNode() {
        return new BLangRestArgsExpression();
    }

    public static NamedArgNode createNamedArgNode() {
        return new BLangNamedArgsExpression();
    }

    public static StatementExpressionNode creatStatementExpression() {
        return new BLangStatementExpression();
    }

    public static TypeTestExpressionNode createTypeTestExpressionNode() {
        return new BLangTypeTestExpr();
    }

    public static AnnotAccessNode createAnnotAccessExpressionNode() {
        return new BLangAnnotAccessExpr();
    }

    public static BLangWorkerFlushExpr createWorkerFlushExpressionNode() {
        return new BLangWorkerFlushExpr();
    }

    public static BLangTransactionalExpr createTransactionalExpressionNode() {
        return new BLangTransactionalExpr();
    }

    public static BLangCommitExpr createCommitExpressionNode() {
        return new BLangCommitExpr();
    }

    public static BLangWorkerSyncSendExpr createWorkerSendSyncExprNode() {
        return new BLangWorkerSyncSendExpr();
    }

    public static BLangWaitForAllExpr createWaitForAllExpressionNode() {
        return new BLangWaitForAllExpr();
    }

    public static BLangWaitForAllExpr.BLangWaitKeyValue createWaitKeyValueNode() {
        return new BLangWaitForAllExpr.BLangWaitKeyValue();
    }

    public static IsLikeExpressionNode createIsLikeExpressionNode() {
        return new BLangIsLikeExpr();
    }

    public static ClassDefinition createClassDefNode() {
        return new BLangClassDefinition();
    }

    public static BLangObjectConstructorExpression createObjectCtorExpression() {
        return new BLangObjectConstructorExpression();
    }

    public static FunctionNode createResourceFunctionNode() {
        return new BLangResourceFunction();
    }

    public static IgnoreNode createIgnoreExprNode() {
        return new BLangIgnoreExpr();
    }

    public static InferredTypedescDefaultNode createInferTypedescExpressionNode() {
        return new BLangInferredTypedescDefaultNode();
    }

    public static BLangInvocation.BLangResourceAccessInvocation createResourceAccessInvocation() {
        return new BLangInvocation.BLangResourceAccessInvocation();
    }
}
