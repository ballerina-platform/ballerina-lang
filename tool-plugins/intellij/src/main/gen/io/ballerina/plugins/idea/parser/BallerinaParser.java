/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// This is a generated file. Not intended for manual editing.
package io.ballerina.plugins.idea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.*;
import static io.ballerina.plugins.idea.parser.BallerinaParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class BallerinaParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ABORT_STATEMENT) {
      r = AbortStatement(b, 0);
    }
    else if (t == ACTION_INVOCATION) {
      r = ActionInvocation(b, 0);
    }
    else if (t == ALIAS) {
      r = Alias(b, 0);
    }
    else if (t == ANNOTATION_ATTACHMENT) {
      r = AnnotationAttachment(b, 0);
    }
    else if (t == ANNOTATION_DEFINITION) {
      r = AnnotationDefinition(b, 0);
    }
    else if (t == ANY_DATA_TYPE_NAME) {
      r = AnyDataTypeName(b, 0);
    }
    else if (t == ANY_IDENTIFIER_NAME) {
      r = AnyIdentifierName(b, 0);
    }
    else if (t == ANY_TYPE_NAME) {
      r = AnyTypeName(b, 0);
    }
    else if (t == ARRAY_LITERAL) {
      r = ArrayLiteral(b, 0);
    }
    else if (t == ARROW_FUNCTION) {
      r = ArrowFunction(b, 0);
    }
    else if (t == ARROW_PARAM) {
      r = ArrowParam(b, 0);
    }
    else if (t == ASSIGNMENT_STATEMENT) {
      r = AssignmentStatement(b, 0);
    }
    else if (t == ATTACHED_OBJECT) {
      r = AttachedObject(b, 0);
    }
    else if (t == ATTACHMENT_POINT) {
      r = AttachmentPoint(b, 0);
    }
    else if (t == ATTRIBUTE) {
      r = Attribute(b, 0);
    }
    else if (t == BINDING_PATTERN) {
      r = BindingPattern(b, 0);
    }
    else if (t == BINDING_REF_PATTERN) {
      r = BindingRefPattern(b, 0);
    }
    else if (t == BLOB_LITERAL) {
      r = BlobLiteral(b, 0);
    }
    else if (t == BLOCK) {
      r = Block(b, 0);
    }
    else if (t == BREAK_STATEMENT) {
      r = BreakStatement(b, 0);
    }
    else if (t == BUILT_IN_REFERENCE_TYPE_NAME) {
      r = BuiltInReferenceTypeName(b, 0);
    }
    else if (t == CALLABLE_UNIT_BODY) {
      r = CallableUnitBody(b, 0);
    }
    else if (t == CALLABLE_UNIT_SIGNATURE) {
      r = CallableUnitSignature(b, 0);
    }
    else if (t == CATCH_CLAUSE) {
      r = CatchClause(b, 0);
    }
    else if (t == CATCH_CLAUSES) {
      r = CatchClauses(b, 0);
    }
    else if (t == CHANNEL_DEFINITION) {
      r = ChannelDefinition(b, 0);
    }
    else if (t == CHANNEL_TYPE) {
      r = ChannelType(b, 0);
    }
    else if (t == CLOSE_TAG) {
      r = CloseTag(b, 0);
    }
    else if (t == COMMENT) {
      r = Comment(b, 0);
    }
    else if (t == COMPENSATE_STATEMENT) {
      r = CompensateStatement(b, 0);
    }
    else if (t == COMPENSATION_CLAUSE) {
      r = CompensationClause(b, 0);
    }
    else if (t == COMPLETE_PACKAGE_NAME) {
      r = CompletePackageName(b, 0);
    }
    else if (t == COMPOUND_ASSIGNMENT_STATEMENT) {
      r = CompoundAssignmentStatement(b, 0);
    }
    else if (t == COMPOUND_OPERATOR) {
      r = CompoundOperator(b, 0);
    }
    else if (t == CONSTANT_DEFINITION) {
      r = ConstantDefinition(b, 0);
    }
    else if (t == CONTENT) {
      r = Content(b, 0);
    }
    else if (t == CONTINUE_STATEMENT) {
      r = ContinueStatement(b, 0);
    }
    else if (t == DEFAULTABLE_PARAMETER) {
      r = DefaultableParameter(b, 0);
    }
    else if (t == DEFINITION) {
      r = Definition(b, 0);
    }
    else if (t == DONE_STATEMENT) {
      r = DoneStatement(b, 0);
    }
    else if (t == ELEMENT) {
      r = Element(b, 0);
    }
    else if (t == ELSE_CLAUSE) {
      r = ElseClause(b, 0);
    }
    else if (t == ELSE_IF_CLAUSE) {
      r = ElseIfClause(b, 0);
    }
    else if (t == EMPTY_TAG) {
      r = EmptyTag(b, 0);
    }
    else if (t == EMPTY_TUPLE_LITERAL) {
      r = EmptyTupleLiteral(b, 0);
    }
    else if (t == ENTRY_BINDING_PATTERN) {
      r = EntryBindingPattern(b, 0);
    }
    else if (t == ENTRY_REF_BINDING_PATTERN) {
      r = EntryRefBindingPattern(b, 0);
    }
    else if (t == ERROR_TYPE_NAME) {
      r = ErrorTypeName(b, 0);
    }
    else if (t == EXPRESSION) {
      r = Expression(b, 0, -1);
    }
    else if (t == EXPRESSION_LIST) {
      r = ExpressionList(b, 0);
    }
    else if (t == EXPRESSION_STMT) {
      r = ExpressionStmt(b, 0);
    }
    else if (t == FIELD) {
      r = Field(b, 0);
    }
    else if (t == FIELD_BINDING_PATTERN) {
      r = FieldBindingPattern(b, 0);
    }
    else if (t == FIELD_DEFINITION) {
      r = FieldDefinition(b, 0);
    }
    else if (t == FIELD_REF_BINDING_PATTERN) {
      r = FieldRefBindingPattern(b, 0);
    }
    else if (t == FINALLY_CLAUSE) {
      r = FinallyClause(b, 0);
    }
    else if (t == FINITE_TYPE) {
      r = FiniteType(b, 0);
    }
    else if (t == FINITE_TYPE_UNIT) {
      r = FiniteTypeUnit(b, 0);
    }
    else if (t == FLOATING_POINT_LITERAL) {
      r = FloatingPointLiteral(b, 0);
    }
    else if (t == FOREACH_STATEMENT) {
      r = ForeachStatement(b, 0);
    }
    else if (t == FOREVER_STATEMENT) {
      r = ForeverStatement(b, 0);
    }
    else if (t == FOREVER_STATEMENT_BODY) {
      r = ForeverStatementBody(b, 0);
    }
    else if (t == FORK_JOIN_STATEMENT) {
      r = ForkJoinStatement(b, 0);
    }
    else if (t == FORK_STATEMENT_BODY) {
      r = ForkStatementBody(b, 0);
    }
    else if (t == FORMAL_PARAMETER_LIST) {
      r = FormalParameterList(b, 0);
    }
    else if (t == FUNCTION_DEFINITION) {
      r = FunctionDefinition(b, 0);
    }
    else if (t == FUNCTION_INVOCATION) {
      r = FunctionInvocation(b, 0);
    }
    else if (t == FUNCTION_TYPE_NAME) {
      r = FunctionTypeName(b, 0);
    }
    else if (t == FUTURE_TYPE_NAME) {
      r = FutureTypeName(b, 0);
    }
    else if (t == GLOBAL_VARIABLE) {
      r = GlobalVariable(b, 0);
    }
    else if (t == GLOBAL_VARIABLE_DEFINITION) {
      r = GlobalVariableDefinition(b, 0);
    }
    else if (t == GROUP_BY_CLAUSE) {
      r = GroupByClause(b, 0);
    }
    else if (t == HAVING_CLAUSE) {
      r = HavingClause(b, 0);
    }
    else if (t == IF_CLAUSE) {
      r = IfClause(b, 0);
    }
    else if (t == IF_ELSE_STATEMENT) {
      r = IfElseStatement(b, 0);
    }
    else if (t == IMPORT_DECLARATION) {
      r = ImportDeclaration(b, 0);
    }
    else if (t == INDEX) {
      r = Index(b, 0);
    }
    else if (t == INT_RANGE_EXPRESSION) {
      r = IntRangeExpression(b, 0);
    }
    else if (t == INTEGER_LITERAL) {
      r = IntegerLiteral(b, 0);
    }
    else if (t == INVOCATION) {
      r = Invocation(b, 0);
    }
    else if (t == INVOCATION_ARG) {
      r = InvocationArg(b, 0);
    }
    else if (t == INVOCATION_ARG_LIST) {
      r = InvocationArgList(b, 0);
    }
    else if (t == JOIN_CLAUSE) {
      r = JoinClause(b, 0);
    }
    else if (t == JOIN_CLAUSE_BODY) {
      r = JoinClauseBody(b, 0);
    }
    else if (t == JOIN_CONDITIONS) {
      r = JoinConditions(b, 0);
    }
    else if (t == JOIN_STREAMING_INPUT) {
      r = JoinStreamingInput(b, 0);
    }
    else if (t == JOIN_TYPE) {
      r = JoinType(b, 0);
    }
    else if (t == JSON_TYPE_NAME) {
      r = JsonTypeName(b, 0);
    }
    else if (t == LAMBDA_FUNCTION) {
      r = LambdaFunction(b, 0);
    }
    else if (t == LAMBDA_RETURN_PARAMETER) {
      r = LambdaReturnParameter(b, 0);
    }
    else if (t == LIMIT_CLAUSE) {
      r = LimitClause(b, 0);
    }
    else if (t == LOCK_STATEMENT) {
      r = LockStatement(b, 0);
    }
    else if (t == MAP_TYPE_NAME) {
      r = MapTypeName(b, 0);
    }
    else if (t == MATCH_EXPRESSION_PATTERN_CLAUSE) {
      r = MatchExpressionPatternClause(b, 0);
    }
    else if (t == NAME_REFERENCE) {
      r = NameReference(b, 0);
    }
    else if (t == NAMED_ARGS) {
      r = NamedArgs(b, 0);
    }
    else if (t == NAMESPACE_DECLARATION) {
      r = NamespaceDeclaration(b, 0);
    }
    else if (t == NAMESPACE_DECLARATION_STATEMENT) {
      r = NamespaceDeclarationStatement(b, 0);
    }
    else if (t == OBJECT_BODY) {
      r = ObjectBody(b, 0);
    }
    else if (t == OBJECT_DEFAULTABLE_PARAMETER) {
      r = ObjectDefaultableParameter(b, 0);
    }
    else if (t == OBJECT_FIELD_DEFINITION) {
      r = ObjectFieldDefinition(b, 0);
    }
    else if (t == OBJECT_FUNCTION_DEFINITION) {
      r = ObjectFunctionDefinition(b, 0);
    }
    else if (t == OBJECT_INITIALIZER) {
      r = ObjectInitializer(b, 0);
    }
    else if (t == OBJECT_INITIALIZER_PARAMETER_LIST) {
      r = ObjectInitializerParameterList(b, 0);
    }
    else if (t == OBJECT_MEMBER) {
      r = ObjectMember(b, 0);
    }
    else if (t == OBJECT_PARAMETER) {
      r = ObjectParameter(b, 0);
    }
    else if (t == OBJECT_PARAMETER_LIST) {
      r = ObjectParameterList(b, 0);
    }
    else if (t == ON_ABORT_STATEMENT) {
      r = OnAbortStatement(b, 0);
    }
    else if (t == ON_COMMIT_STATEMENT) {
      r = OnCommitStatement(b, 0);
    }
    else if (t == ON_RETRY_CLAUSE) {
      r = OnRetryClause(b, 0);
    }
    else if (t == ORDER_BY_CLAUSE) {
      r = OrderByClause(b, 0);
    }
    else if (t == ORDER_BY_TYPE) {
      r = OrderByType(b, 0);
    }
    else if (t == ORDER_BY_VARIABLE) {
      r = OrderByVariable(b, 0);
    }
    else if (t == ORG_NAME) {
      r = OrgName(b, 0);
    }
    else if (t == OUTPUT_RATE_LIMIT) {
      r = OutputRateLimit(b, 0);
    }
    else if (t == PACKAGE_NAME) {
      r = PackageName(b, 0);
    }
    else if (t == PACKAGE_REFERENCE) {
      r = PackageReference(b, 0);
    }
    else if (t == PACKAGE_VERSION) {
      r = PackageVersion(b, 0);
    }
    else if (t == PANIC_STATEMENT) {
      r = PanicStatement(b, 0);
    }
    else if (t == PARAMETER) {
      r = Parameter(b, 0);
    }
    else if (t == PARAMETER_LIST) {
      r = ParameterList(b, 0);
    }
    else if (t == PARAMETER_TYPE_NAME_LIST) {
      r = ParameterTypeNameList(b, 0);
    }
    else if (t == PATTERN_CLAUSE) {
      r = PatternClause(b, 0);
    }
    else if (t == PATTERN_STREAMING_EDGE_INPUT) {
      r = PatternStreamingEdgeInput(b, 0);
    }
    else if (t == PATTERN_STREAMING_INPUT) {
      r = PatternStreamingInput(b, 0);
    }
    else if (t == PROC_INS) {
      r = ProcIns(b, 0);
    }
    else if (t == RECORD_BINDING_PATTERN) {
      r = RecordBindingPattern(b, 0);
    }
    else if (t == RECORD_DESTRUCTURING_STATEMENT) {
      r = RecordDestructuringStatement(b, 0);
    }
    else if (t == RECORD_FIELD_DEFINITION_LIST) {
      r = RecordFieldDefinitionList(b, 0);
    }
    else if (t == RECORD_KEY) {
      r = RecordKey(b, 0);
    }
    else if (t == RECORD_KEY_VALUE) {
      r = RecordKeyValue(b, 0);
    }
    else if (t == RECORD_LITERAL) {
      r = RecordLiteral(b, 0);
    }
    else if (t == RECORD_LITERAL_BODY) {
      r = RecordLiteralBody(b, 0);
    }
    else if (t == RECORD_REF_BINDING_PATTERN) {
      r = RecordRefBindingPattern(b, 0);
    }
    else if (t == RECORD_REST_FIELD_DEFINITION) {
      r = RecordRestFieldDefinition(b, 0);
    }
    else if (t == REFERENCE_TYPE_NAME) {
      r = ReferenceTypeName(b, 0);
    }
    else if (t == RESERVED_WORD) {
      r = ReservedWord(b, 0);
    }
    else if (t == REST_ARGS) {
      r = RestArgs(b, 0);
    }
    else if (t == REST_BINDING_PATTERN) {
      r = RestBindingPattern(b, 0);
    }
    else if (t == REST_PARAMETER) {
      r = RestParameter(b, 0);
    }
    else if (t == REST_REF_BINDING_PATTERN) {
      r = RestRefBindingPattern(b, 0);
    }
    else if (t == RETRIES_STATEMENT) {
      r = RetriesStatement(b, 0);
    }
    else if (t == RETRY_STATEMENT) {
      r = RetryStatement(b, 0);
    }
    else if (t == RETURN_PARAMETER) {
      r = ReturnParameter(b, 0);
    }
    else if (t == RETURN_STATEMENT) {
      r = ReturnStatement(b, 0);
    }
    else if (t == RETURN_TYPE) {
      r = ReturnType(b, 0);
    }
    else if (t == SCOPE_CLAUSE) {
      r = ScopeClause(b, 0);
    }
    else if (t == SCOPE_STATEMENT) {
      r = ScopeStatement(b, 0);
    }
    else if (t == SEALED_LITERAL) {
      r = SealedLiteral(b, 0);
    }
    else if (t == SELECT_CLAUSE) {
      r = SelectClause(b, 0);
    }
    else if (t == SELECT_EXPRESSION) {
      r = SelectExpression(b, 0);
    }
    else if (t == SELECT_EXPRESSION_LIST) {
      r = SelectExpressionList(b, 0);
    }
    else if (t == SERVICE_BODY) {
      r = ServiceBody(b, 0);
    }
    else if (t == SERVICE_BODY_MEMBER) {
      r = ServiceBodyMember(b, 0);
    }
    else if (t == SERVICE_DEFINITION) {
      r = ServiceDefinition(b, 0);
    }
    else if (t == SERVICE_TYPE_NAME) {
      r = ServiceTypeName(b, 0);
    }
    else if (t == SET_ASSIGNMENT_CLAUSE) {
      r = SetAssignmentClause(b, 0);
    }
    else if (t == SET_CLAUSE) {
      r = SetClause(b, 0);
    }
    else if (t == SHIFT_EXPRESSION) {
      r = ShiftExpression(b, 0);
    }
    else if (t == SIMPLE_LITERAL) {
      r = SimpleLiteral(b, 0);
    }
    else if (t == START_TAG) {
      r = StartTag(b, 0);
    }
    else if (t == STATEMENT) {
      r = Statement(b, 0);
    }
    else if (t == STREAM_TYPE_NAME) {
      r = StreamTypeName(b, 0);
    }
    else if (t == STREAMING_ACTION) {
      r = StreamingAction(b, 0);
    }
    else if (t == STREAMING_INPUT) {
      r = StreamingInput(b, 0);
    }
    else if (t == STREAMING_QUERY_STATEMENT) {
      r = StreamingQueryStatement(b, 0);
    }
    else if (t == STRING_TEMPLATE_CONTENT) {
      r = StringTemplateContent(b, 0);
    }
    else if (t == STRING_TEMPLATE_LITERAL) {
      r = StringTemplateLiteral(b, 0);
    }
    else if (t == STRUCTURED_BINDING_PATTERN) {
      r = StructuredBindingPattern(b, 0);
    }
    else if (t == STRUCTURED_REF_BINDING_PATTERN) {
      r = StructuredRefBindingPattern(b, 0);
    }
    else if (t == TABLE_COLUMN) {
      r = TableColumn(b, 0);
    }
    else if (t == TABLE_COLUMN_DEFINITION) {
      r = TableColumnDefinition(b, 0);
    }
    else if (t == TABLE_DATA) {
      r = TableData(b, 0);
    }
    else if (t == TABLE_DATA_ARRAY) {
      r = TableDataArray(b, 0);
    }
    else if (t == TABLE_DATA_LIST) {
      r = TableDataList(b, 0);
    }
    else if (t == TABLE_LITERAL) {
      r = TableLiteral(b, 0);
    }
    else if (t == TABLE_QUERY) {
      r = TableQuery(b, 0);
    }
    else if (t == TABLE_TYPE_NAME) {
      r = TableTypeName(b, 0);
    }
    else if (t == THROW_STATEMENT) {
      r = ThrowStatement(b, 0);
    }
    else if (t == TIME_SCALE) {
      r = TimeScale(b, 0);
    }
    else if (t == TIMEOUT_CLAUSE) {
      r = TimeoutClause(b, 0);
    }
    else if (t == TIMEOUT_CLAUSE_BODY) {
      r = TimeoutClauseBody(b, 0);
    }
    else if (t == TRANSACTION_CLAUSE) {
      r = TransactionClause(b, 0);
    }
    else if (t == TRANSACTION_PROPERTY_INIT_STATEMENT) {
      r = TransactionPropertyInitStatement(b, 0);
    }
    else if (t == TRANSACTION_PROPERTY_INIT_STATEMENT_LIST) {
      r = TransactionPropertyInitStatementList(b, 0);
    }
    else if (t == TRANSACTION_STATEMENT) {
      r = TransactionStatement(b, 0);
    }
    else if (t == TRIGGER_WORKER) {
      r = TriggerWorker(b, 0);
    }
    else if (t == TRY_CATCH_STATEMENT) {
      r = TryCatchStatement(b, 0);
    }
    else if (t == TUPLE_BINDING_PATTERN) {
      r = TupleBindingPattern(b, 0);
    }
    else if (t == TUPLE_REF_BINDING_PATTERN) {
      r = TupleRefBindingPattern(b, 0);
    }
    else if (t == TYPE_DEFINITION) {
      r = TypeDefinition(b, 0);
    }
    else if (t == TYPE_DESC_TYPE_NAME) {
      r = TypeDescTypeName(b, 0);
    }
    else if (t == TYPE_NAME) {
      r = TypeName(b, 0, -1);
    }
    else if (t == TYPE_REFERENCE) {
      r = TypeReference(b, 0);
    }
    else if (t == USER_DEFINE_TYPE_NAME) {
      r = UserDefineTypeName(b, 0);
    }
    else if (t == VALUE_TYPE_NAME) {
      r = ValueTypeName(b, 0);
    }
    else if (t == VARIABLE_DEFINITION_STATEMENT) {
      r = VariableDefinitionStatement(b, 0);
    }
    else if (t == VARIABLE_REFERENCE) {
      r = VariableReference(b, 0, -1);
    }
    else if (t == VARIABLE_REFERENCE_LIST) {
      r = VariableReferenceList(b, 0);
    }
    else if (t == WHERE_CLAUSE) {
      r = WhereClause(b, 0);
    }
    else if (t == WHILE_STATEMENT) {
      r = WhileStatement(b, 0);
    }
    else if (t == WHILE_STATEMENT_BODY) {
      r = WhileStatementBody(b, 0);
    }
    else if (t == WINDOW_CLAUSE) {
      r = WindowClause(b, 0);
    }
    else if (t == WITHIN_CLAUSE) {
      r = WithinClause(b, 0);
    }
    else if (t == WORKER_BODY) {
      r = WorkerBody(b, 0);
    }
    else if (t == WORKER_DEFINITION) {
      r = WorkerDefinition(b, 0);
    }
    else if (t == WORKER_INTERACTION_STATEMENT) {
      r = WorkerInteractionStatement(b, 0);
    }
    else if (t == WORKER_REPLY) {
      r = WorkerReply(b, 0);
    }
    else if (t == XML_ATTRIB) {
      r = XmlAttrib(b, 0);
    }
    else if (t == XML_DOUBLE_QUOTED_STRING) {
      r = XmlDoubleQuotedString(b, 0);
    }
    else if (t == XML_ITEM) {
      r = XmlItem(b, 0);
    }
    else if (t == XML_LITERAL) {
      r = XmlLiteral(b, 0);
    }
    else if (t == XML_LOCAL_NAME) {
      r = XmlLocalName(b, 0);
    }
    else if (t == XML_NAMESPACE_NAME) {
      r = XmlNamespaceName(b, 0);
    }
    else if (t == XML_QUALIFIED_NAME) {
      r = XmlQualifiedName(b, 0);
    }
    else if (t == XML_QUOTED_STRING) {
      r = XmlQuotedString(b, 0);
    }
    else if (t == XML_SINGLE_QUOTED_STRING) {
      r = XmlSingleQuotedString(b, 0);
    }
    else if (t == XML_TEXT) {
      r = XmlText(b, 0);
    }
    else if (t == XML_TYPE_NAME) {
      r = XmlTypeName(b, 0);
    }
    else if (t == BACKTICKED_BLOCK) {
      r = backtickedBlock(b, 0);
    }
    else if (t == BINDING_PATTERN_PATTERN) {
      r = bindingPatternPattern(b, 0);
    }
    else if (t == DEFINITION_REFERENCE_TYPE) {
      r = definitionReferenceType(b, 0);
    }
    else if (t == DEPRECATED_ATTACHMENT) {
      r = deprecatedAttachment(b, 0);
    }
    else if (t == DEPRECATED_TEMPLATE_INLINE_CODE) {
      r = deprecatedTemplateInlineCode(b, 0);
    }
    else if (t == DEPRECATED_TEXT) {
      r = deprecatedText(b, 0);
    }
    else if (t == DOC_PARAMETER_DESCRIPTION) {
      r = docParameterDescription(b, 0);
    }
    else if (t == DOCUMENTATION_CONTENT) {
      r = documentationContent(b, 0);
    }
    else if (t == DOCUMENTATION_DEFINITION_REFERENCE) {
      r = documentationDefinitionReference(b, 0);
    }
    else if (t == DOCUMENTATION_LINE) {
      r = documentationLine(b, 0);
    }
    else if (t == DOCUMENTATION_REFERENCE) {
      r = documentationReference(b, 0);
    }
    else if (t == DOCUMENTATION_STRING) {
      r = documentationString(b, 0);
    }
    else if (t == DOCUMENTATION_TEXT) {
      r = documentationText(b, 0);
    }
    else if (t == DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
      r = doubleBackTickDeprecatedInlineCode(b, 0);
    }
    else if (t == DOUBLE_BACKTICKED_BLOCK) {
      r = doubleBacktickedBlock(b, 0);
    }
    else if (t == EXPRESSION_PATTERN) {
      r = expressionPattern(b, 0);
    }
    else if (t == FUNCTION_NAME_REFERENCE) {
      r = functionNameReference(b, 0);
    }
    else if (t == INIT_WITH_TYPE) {
      r = initWithType(b, 0);
    }
    else if (t == INIT_WITHOUT_TYPE) {
      r = initWithoutType(b, 0);
    }
    else if (t == MATCH_EXPRESSION) {
      r = matchExpression(b, 0);
    }
    else if (t == MATCH_PATTERN_CLAUSE) {
      r = matchPatternClause(b, 0);
    }
    else if (t == MATCH_STATEMENT) {
      r = matchStatement(b, 0);
    }
    else if (t == MATCH_STATEMENT_BODY) {
      r = matchStatementBody(b, 0);
    }
    else if (t == NAMED_PATTERN) {
      r = namedPattern(b, 0);
    }
    else if (t == PARAMETER_DESCRIPTION) {
      r = parameterDescription(b, 0);
    }
    else if (t == PARAMETER_DOCUMENTATION) {
      r = parameterDocumentation(b, 0);
    }
    else if (t == PARAMETER_DOCUMENTATION_LINE) {
      r = parameterDocumentationLine(b, 0);
    }
    else if (t == PARAMETER_TYPE_NAME) {
      r = parameterTypeName(b, 0);
    }
    else if (t == PARAMETER_WITH_TYPE) {
      r = parameterWithType(b, 0);
    }
    else if (t == RETURN_PARAMETER_DESCRIPTION) {
      r = returnParameterDescription(b, 0);
    }
    else if (t == RETURN_PARAMETER_DOCUMENTATION) {
      r = returnParameterDocumentation(b, 0);
    }
    else if (t == RETURN_PARAMETER_DOCUMENTATION_LINE) {
      r = returnParameterDocumentationLine(b, 0);
    }
    else if (t == SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
      r = singleBackTickDeprecatedInlineCode(b, 0);
    }
    else if (t == SINGLE_BACKTICKED_BLOCK) {
      r = singleBacktickedBlock(b, 0);
    }
    else if (t == TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
      r = tripleBackTickDeprecatedInlineCode(b, 0);
    }
    else if (t == TRIPLE_BACKTICKED_BLOCK) {
      r = tripleBacktickedBlock(b, 0);
    }
    else if (t == TUPLE_DESTRUCTURING_STATEMENT) {
      r = tupleDestructuringStatement(b, 0);
    }
    else if (t == UNNAMED_PATTERN) {
      r = unnamedPattern(b, 0);
    }
    else if (t == VARIABLE_DEFINITION_STATEMENT_WITH_ASSIGNMENT) {
      r = variableDefinitionStatementWithAssignment(b, 0);
    }
    else if (t == VARIABLE_DEFINITION_STATEMENT_WITHOUT_ASSIGNMENT) {
      r = variableDefinitionStatementWithoutAssignment(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return CompilationUnit(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(FIELD_VARIABLE_REFERENCE, FUNCTION_INVOCATION_REFERENCE, INVOCATION_REFERENCE, MAP_ARRAY_VARIABLE_REFERENCE,
      SIMPLE_VARIABLE_REFERENCE, VARIABLE_REFERENCE, XML_ATTRIB_VARIABLE_REFERENCE),
    create_token_set_(ARRAY_TYPE_NAME, GROUP_TYPE_NAME, NULLABLE_TYPE_NAME, OBJECT_TYPE_NAME,
      RECORD_TYPE_NAME, SIMPLE_TYPE_NAME, TUPLE_TYPE_NAME, TYPE_NAME,
      UNION_TYPE_NAME),
    create_token_set_(ACTION_INVOCATION_EXPRESSION, ARRAY_LITERAL_EXPRESSION, ARROW_FUNCTION_EXPRESSION, AWAIT_EXPRESSION,
      BINARY_ADD_SUB_EXPRESSION, BINARY_AND_EXPRESSION, BINARY_COMPARE_EXPRESSION, BINARY_DIV_MUL_MOD_EXPRESSION,
      BINARY_EQUAL_EXPRESSION, BINARY_OR_EXPRESSION, BINARY_REF_EQUAL_EXPRESSION, BITWISE_EXPRESSION,
      BITWISE_SHIFT_EXPRESSION, BRACED_OR_TUPLE_EXPRESSION, BUILT_IN_REFERENCE_TYPE_TYPE_EXPRESSION, CHECKED_EXPRESSION,
      ELVIS_EXPRESSION, ERROR_CONSTRUCTOR_EXPRESSION, EXPRESSION, INTEGER_RANGE_EXPRESSION,
      LAMBDA_FUNCTION_EXPRESSION, MATCH_EXPR_EXPRESSION, RECORD_LITERAL_EXPRESSION, SERVICE_CONSTRUCTOR_EXPRESSION,
      SIMPLE_LITERAL_EXPRESSION, STRING_TEMPLATE_LITERAL_EXPRESSION, TABLE_LITERAL_EXPRESSION, TABLE_QUERY_EXPRESSION,
      TERNARY_EXPRESSION, TRAP_EXPRESSION, TYPE_ACCESS_EXPRESSION, TYPE_CONVERSION_EXPRESSION,
      TYPE_INIT_EXPRESSION, TYPE_TEST_EXPRESSION, UNARY_EXPRESSION, VALUE_TYPE_TYPE_EXPRESSION,
      VARIABLE_REFERENCE_EXPRESSION, XML_LITERAL_EXPRESSION),
  };

  /* ********************************************************** */
  // abort SEMICOLON
  public static boolean AbortStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AbortStatement")) return false;
    if (!nextTokenIs(b, ABORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ABORT_STATEMENT, null);
    r = consumeTokens(b, 1, ABORT, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // start? VariableReference RARROW FunctionInvocation {/*pin=3 recoverWhile=StatementRecover*/}
  public static boolean ActionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACTION_INVOCATION, "<action invocation>");
    r = ActionInvocation_0(b, l + 1);
    r = r && VariableReference(b, l + 1, -1);
    r = r && consumeToken(b, RARROW);
    r = r && FunctionInvocation(b, l + 1);
    r = r && ActionInvocation_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // start?
  private static boolean ActionInvocation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation_0")) return false;
    consumeToken(b, START);
    return true;
  }

  // {/*pin=3 recoverWhile=StatementRecover*/}
  private static boolean ActionInvocation_4(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // as identifier
  public static boolean Alias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Alias")) return false;
    if (!nextTokenIs(b, AS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ALIAS, null);
    r = consumeTokens(b, 1, AS, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AT NameReference RecordLiteral?
  public static boolean AnnotationAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationAttachment")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_ATTACHMENT, null);
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && report_error_(b, NameReference(b, l + 1));
    r = p && AnnotationAttachment_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RecordLiteral?
  private static boolean AnnotationAttachment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationAttachment_2")) return false;
    RecordLiteral(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (public)? annotation (LT AttachmentPoint (COMMA AttachmentPoint)* GT)? identifier UserDefineTypeName? SEMICOLON
  public static boolean AnnotationDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition")) return false;
    if (!nextTokenIs(b, "<annotation definition>", ANNOTATION, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_DEFINITION, "<annotation definition>");
    r = AnnotationDefinition_0(b, l + 1);
    r = r && consumeToken(b, ANNOTATION);
    p = r; // pin = 2
    r = r && report_error_(b, AnnotationDefinition_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, IDENTIFIER)) && r;
    r = p && report_error_(b, AnnotationDefinition_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public)?
  private static boolean AnnotationDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // (LT AttachmentPoint (COMMA AttachmentPoint)* GT)?
  private static boolean AnnotationDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_2")) return false;
    AnnotationDefinition_2_0(b, l + 1);
    return true;
  }

  // LT AttachmentPoint (COMMA AttachmentPoint)* GT
  private static boolean AnnotationDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && AttachmentPoint(b, l + 1);
    r = r && AnnotationDefinition_2_0_2(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA AttachmentPoint)*
  private static boolean AnnotationDefinition_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_2_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationDefinition_2_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "AnnotationDefinition_2_0_2", c)) break;
    }
    return true;
  }

  // COMMA AttachmentPoint
  private static boolean AnnotationDefinition_2_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_2_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && AttachmentPoint(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // UserDefineTypeName?
  private static boolean AnnotationDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_4")) return false;
    UserDefineTypeName(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // anydata
  public static boolean AnyDataTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyDataTypeName")) return false;
    if (!nextTokenIs(b, ANYDATA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANYDATA);
    exit_section_(b, m, ANY_DATA_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // identifier | ReservedWord
  public static boolean AnyIdentifierName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyIdentifierName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANY_IDENTIFIER_NAME, "<any identifier name>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = ReservedWord(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // any
  public static boolean AnyTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyTypeName")) return false;
    if (!nextTokenIs(b, ANY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANY);
    exit_section_(b, m, ANY_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET ExpressionList? RIGHT_BRACKET
  public static boolean ArrayLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayLiteral")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && ArrayLiteral_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, ARRAY_LITERAL, r);
    return r;
  }

  // ExpressionList?
  private static boolean ArrayLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayLiteral_1")) return false;
    ExpressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ArrowFunctionWithoutParenthesis | ArrowFunctionWithParenthesis
  public static boolean ArrowFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunction")) return false;
    if (!nextTokenIs(b, "<arrow function>", IDENTIFIER, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARROW_FUNCTION, "<arrow function>");
    r = ArrowFunctionWithoutParenthesis(b, l + 1);
    if (!r) r = ArrowFunctionWithParenthesis(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS (ArrowParam (COMMA ArrowParam)*)? RIGHT_PARENTHESIS EQUAL_GT Expression
  static boolean ArrowFunctionWithParenthesis(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && ArrowFunctionWithParenthesis_1(b, l + 1);
    r = r && consumeTokens(b, 2, RIGHT_PARENTHESIS, EQUAL_GT);
    p = r; // pin = 4
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (ArrowParam (COMMA ArrowParam)*)?
  private static boolean ArrowFunctionWithParenthesis_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1")) return false;
    ArrowFunctionWithParenthesis_1_0(b, l + 1);
    return true;
  }

  // ArrowParam (COMMA ArrowParam)*
  private static boolean ArrowFunctionWithParenthesis_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrowParam(b, l + 1);
    r = r && ArrowFunctionWithParenthesis_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ArrowParam)*
  private static boolean ArrowFunctionWithParenthesis_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ArrowFunctionWithParenthesis_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrowFunctionWithParenthesis_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA ArrowParam
  private static boolean ArrowFunctionWithParenthesis_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ArrowParam(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ArrowParam EQUAL_GT Expression
  static boolean ArrowFunctionWithoutParenthesis(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithoutParenthesis")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ArrowParam(b, l + 1);
    r = r && consumeToken(b, EQUAL_GT);
    p = r; // pin = 2
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean ArrowParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowParam")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ARROW_PARAM, r);
    return r;
  }

  /* ********************************************************** */
  // VariableReference ASSIGN Expression SEMICOLON
  public static boolean AssignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssignmentStatement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    r = VariableReference(b, l + 1, -1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean AttachedObject(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachedObject")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ATTACHED_OBJECT, r);
    return r;
  }

  /* ********************************************************** */
  // service | resource | function | remote | object | client | listener | type | TYPE_PARAMETER | annotation {
  //     /*recoverWhile=AttachmentPointRecover*/
  // }
  public static boolean AttachmentPoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachmentPoint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTACHMENT_POINT, "<attachment point>");
    r = consumeToken(b, SERVICE);
    if (!r) r = consumeToken(b, RESOURCE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, CLIENT);
    if (!r) r = consumeToken(b, LISTENER);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, TYPE_PARAMETER);
    if (!r) r = AttachmentPoint_9(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation {
  //     /*recoverWhile=AttachmentPointRecover*/
  // }
  private static boolean AttachmentPoint_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachmentPoint_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANNOTATION);
    r = r && AttachmentPoint_9_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  //     /*recoverWhile=AttachmentPointRecover*/
  // }
  private static boolean AttachmentPoint_9_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // XmlQualifiedName EQUALS XmlQuotedString
  public static boolean Attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Attribute")) return false;
    if (!nextTokenIs(b, "<attribute>", XML_QNAME, XML_TAG_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTRIBUTE, "<attribute>");
    r = XmlQualifiedName(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && XmlQuotedString(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier | StructuredBindingPattern
  public static boolean BindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDING_PATTERN, "<binding pattern>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = StructuredBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VariableReference | StructuredRefBindingPattern
  public static boolean BindingRefPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BindingRefPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDING_REF_PATTERN, "<binding ref pattern>");
    r = VariableReference(b, l + 1, -1);
    if (!r) r = StructuredRefBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BASE_16_BLOB_LITERAL | BASE_64_BLOB_LITERAL
  public static boolean BlobLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlobLiteral")) return false;
    if (!nextTokenIs(b, "<blob literal>", BASE_16_BLOB_LITERAL, BASE_64_BLOB_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOB_LITERAL, "<blob literal>");
    r = consumeToken(b, BASE_16_BLOB_LITERAL);
    if (!r) r = consumeToken(b, BASE_64_BLOB_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Statement*
  public static boolean Block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Block")) return false;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // break SEMICOLON
  public static boolean BreakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BreakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BREAK_STATEMENT, null);
    r = consumeTokens(b, 1, BREAK, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MapTypeName
  //                              | FutureTypeName
  //                              | XmlTypeName
  //                              | JsonTypeName
  //                              | StreamTypeName
  //                              | TableTypeName
  //                              | ServiceTypeName
  //                              | ErrorTypeName
  //                              | FunctionTypeName
  public static boolean BuiltInReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BuiltInReferenceTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BUILT_IN_REFERENCE_TYPE_NAME, "<built in reference type name>");
    r = MapTypeName(b, l + 1);
    if (!r) r = FutureTypeName(b, l + 1);
    if (!r) r = XmlTypeName(b, l + 1);
    if (!r) r = JsonTypeName(b, l + 1);
    if (!r) r = StreamTypeName(b, l + 1);
    if (!r) r = TableTypeName(b, l + 1);
    if (!r) r = ServiceTypeName(b, l + 1);
    if (!r) r = ErrorTypeName(b, l + 1);
    if (!r) r = FunctionTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE (RIGHT_BRACE | Block RIGHT_BRACE | WorkerDefinition+ RIGHT_BRACE)
  public static boolean CallableUnitBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CALLABLE_UNIT_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && CallableUnitBody_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RIGHT_BRACE | Block RIGHT_BRACE | WorkerDefinition+ RIGHT_BRACE
  private static boolean CallableUnitBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = CallableUnitBody_1_1(b, l + 1);
    if (!r) r = CallableUnitBody_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Block RIGHT_BRACE
  private static boolean CallableUnitBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // WorkerDefinition+ RIGHT_BRACE
  private static boolean CallableUnitBody_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody_1_2_0(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // WorkerDefinition+
  private static boolean CallableUnitBody_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WorkerDefinition(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!WorkerDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CallableUnitBody_1_2_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnyIdentifierName LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS ReturnParameter?
  public static boolean CallableUnitSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitSignature")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CALLABLE_UNIT_SIGNATURE, "<callable unit signature>");
    r = AnyIdentifierName(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LEFT_PARENTHESIS));
    r = p && report_error_(b, CallableUnitSignature_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && CallableUnitSignature_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FormalParameterList?
  private static boolean CallableUnitSignature_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitSignature_2")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // ReturnParameter?
  private static boolean CallableUnitSignature_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitSignature_4")) return false;
    ReturnParameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // catch (LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS (LEFT_BRACE Block RIGHT_BRACE))
  public static boolean CatchClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause")) return false;
    if (!nextTokenIs(b, CATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CLAUSE, null);
    r = consumeToken(b, CATCH);
    p = r; // pin = 1
    r = r && CatchClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS (LEFT_BRACE Block RIGHT_BRACE)
  private static boolean CatchClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, RIGHT_PARENTHESIS)) && r;
    r = p && CatchClause_1_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean CatchClause_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause_1_4")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CatchClause+ FinallyClause? | FinallyClause
  public static boolean CatchClauses(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses")) return false;
    if (!nextTokenIs(b, "<catch clauses>", CATCH, FINALLY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CLAUSES, "<catch clauses>");
    r = CatchClauses_0(b, l + 1);
    if (!r) r = FinallyClause(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CatchClause+ FinallyClause?
  private static boolean CatchClauses_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CatchClauses_0_0(b, l + 1);
    r = r && CatchClauses_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CatchClause+
  private static boolean CatchClauses_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CatchClause(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!CatchClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CatchClauses_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // FinallyClause?
  private static boolean CatchClauses_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses_0_1")) return false;
    FinallyClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ChannelType identifier SEMICOLON
  public static boolean ChannelDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ChannelDefinition")) return false;
    if (!nextTokenIs(b, CHANNEL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ChannelType(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, SEMICOLON);
    exit_section_(b, m, CHANNEL_DEFINITION, r);
    return r;
  }

  /* ********************************************************** */
  // channel (LT TypeName GT)
  public static boolean ChannelType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ChannelType")) return false;
    if (!nextTokenIs(b, CHANNEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CHANNEL_TYPE, null);
    r = consumeToken(b, CHANNEL);
    p = r; // pin = 1
    r = r && ChannelType_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LT TypeName GT
  private static boolean ChannelType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ChannelType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // XML_TAG_OPEN_SLASH XmlQualifiedName XML_TAG_CLOSE
  public static boolean CloseTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CloseTag")) return false;
    if (!nextTokenIs(b, XML_TAG_OPEN_SLASH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLOSE_TAG, null);
    r = consumeToken(b, XML_TAG_OPEN_SLASH);
    p = r; // pin = 1
    r = r && report_error_(b, XmlQualifiedName(b, l + 1));
    r = p && consumeToken(b, XML_TAG_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // XML_COMMENT_START (XML_COMMENT_TEMPLATE_TEXT Expression EXPRESSION_END)* XML_COMMENT_TEXT
  public static boolean Comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Comment")) return false;
    if (!nextTokenIs(b, XML_COMMENT_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMMENT, null);
    r = consumeToken(b, XML_COMMENT_START);
    p = r; // pin = 1
    r = r && report_error_(b, Comment_1(b, l + 1));
    r = p && consumeToken(b, XML_COMMENT_TEXT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (XML_COMMENT_TEMPLATE_TEXT Expression EXPRESSION_END)*
  private static boolean Comment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Comment_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Comment_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Comment_1", c)) break;
    }
    return true;
  }

  // XML_COMMENT_TEMPLATE_TEXT Expression EXPRESSION_END
  private static boolean Comment_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Comment_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_COMMENT_TEMPLATE_TEXT);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // compensate identifier SEMICOLON
  public static boolean CompensateStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompensateStatement")) return false;
    if (!nextTokenIs(b, COMPENSATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMPENSATE_STATEMENT, null);
    r = consumeTokens(b, 1, COMPENSATE, IDENTIFIER, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // compensation CallableUnitBody
  public static boolean CompensationClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompensationClause")) return false;
    if (!nextTokenIs(b, COMPENSATION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMPENSATION_CLAUSE, null);
    r = consumeToken(b, COMPENSATION);
    p = r; // pin = 1
    r = r && CallableUnitBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (ImportDeclaration | NamespaceDeclaration)* (DefinitionWithoutAnnotationAttachments |  DefinitionWithMultipleAnnotationAttachments | DefinitionWithSingleAnnotationAttachment)* <<eof>>
  static boolean CompilationUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CompilationUnit_0(b, l + 1);
    r = r && CompilationUnit_1(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ImportDeclaration | NamespaceDeclaration)*
  private static boolean CompilationUnit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CompilationUnit_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompilationUnit_0", c)) break;
    }
    return true;
  }

  // ImportDeclaration | NamespaceDeclaration
  private static boolean CompilationUnit_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_0_0")) return false;
    boolean r;
    r = ImportDeclaration(b, l + 1);
    if (!r) r = NamespaceDeclaration(b, l + 1);
    return r;
  }

  // (DefinitionWithoutAnnotationAttachments |  DefinitionWithMultipleAnnotationAttachments | DefinitionWithSingleAnnotationAttachment)*
  private static boolean CompilationUnit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CompilationUnit_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompilationUnit_1", c)) break;
    }
    return true;
  }

  // DefinitionWithoutAnnotationAttachments |  DefinitionWithMultipleAnnotationAttachments | DefinitionWithSingleAnnotationAttachment
  private static boolean CompilationUnit_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_1_0")) return false;
    boolean r;
    r = DefinitionWithoutAnnotationAttachments(b, l + 1);
    if (!r) r = DefinitionWithMultipleAnnotationAttachments(b, l + 1);
    if (!r) r = DefinitionWithSingleAnnotationAttachment(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // PackageName (DOT PackageName)*
  public static boolean CompletePackageName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompletePackageName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMPLETE_PACKAGE_NAME, null);
    r = PackageName(b, l + 1);
    p = r; // pin = 1
    r = r && CompletePackageName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (DOT PackageName)*
  private static boolean CompletePackageName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompletePackageName_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CompletePackageName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompletePackageName_1", c)) break;
    }
    return true;
  }

  // DOT PackageName
  private static boolean CompletePackageName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompletePackageName_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DOT);
    p = r; // pin = 1
    r = r && PackageName(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // VariableReference CompoundOperator Expression SEMICOLON
  public static boolean CompoundAssignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompoundAssignmentStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_ASSIGNMENT_STATEMENT, "<compound assignment statement>");
    r = VariableReference(b, l + 1, -1);
    r = r && CompoundOperator(b, l + 1);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMPOUND_ADD
  //                     | COMPOUND_SUB
  //                     | COMPOUND_MUL
  //                     | COMPOUND_DIV
  //                     | COMPOUND_BIT_AND
  //                     | COMPOUND_BIT_OR
  //                     | COMPOUND_BIT_XOR
  //                     | COMPOUND_LEFT_SHIFT
  //                     | COMPOUND_RIGHT_SHIFT
  //                     | COMPOUND_LOGICAL_SHIFT
  public static boolean CompoundOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompoundOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_OPERATOR, "<compound operator>");
    r = consumeToken(b, COMPOUND_ADD);
    if (!r) r = consumeToken(b, COMPOUND_SUB);
    if (!r) r = consumeToken(b, COMPOUND_MUL);
    if (!r) r = consumeToken(b, COMPOUND_DIV);
    if (!r) r = consumeToken(b, COMPOUND_BIT_AND);
    if (!r) r = consumeToken(b, COMPOUND_BIT_OR);
    if (!r) r = consumeToken(b, COMPOUND_BIT_XOR);
    if (!r) r = consumeToken(b, COMPOUND_LEFT_SHIFT);
    if (!r) r = consumeToken(b, COMPOUND_RIGHT_SHIFT);
    if (!r) r = consumeToken(b, COMPOUND_LOGICAL_SHIFT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PUBLIC? CONST TypeName? identifier ASSIGN Expression SEMICOLON
  public static boolean ConstantDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition")) return false;
    if (!nextTokenIs(b, "<constant definition>", CONST, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT_DEFINITION, "<constant definition>");
    r = ConstantDefinition_0(b, l + 1);
    r = r && consumeToken(b, CONST);
    p = r; // pin = 2
    r = r && report_error_(b, ConstantDefinition_2(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, ASSIGN)) && r;
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // PUBLIC?
  private static boolean ConstantDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // TypeName?
  private static boolean ConstantDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition_2")) return false;
    TypeName(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // XmlText? ((ProcIns | Comment | Element | cdata) XmlText?)*
  public static boolean Content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONTENT, "<content>");
    r = Content_0(b, l + 1);
    r = r && Content_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // XmlText?
  private static boolean Content_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content_0")) return false;
    XmlText(b, l + 1);
    return true;
  }

  // ((ProcIns | Comment | Element | cdata) XmlText?)*
  private static boolean Content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Content_1", c)) break;
    }
    return true;
  }

  // (ProcIns | Comment | Element | cdata) XmlText?
  private static boolean Content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Content_1_0_0(b, l + 1);
    r = r && Content_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ProcIns | Comment | Element | cdata
  private static boolean Content_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content_1_0_0")) return false;
    boolean r;
    r = ProcIns(b, l + 1);
    if (!r) r = Comment(b, l + 1);
    if (!r) r = Element(b, l + 1);
    if (!r) r = consumeToken(b, CDATA);
    return r;
  }

  // XmlText?
  private static boolean Content_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content_1_0_1")) return false;
    XmlText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // continue SEMICOLON
  public static boolean ContinueStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ContinueStatement")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONTINUE_STATEMENT, null);
    r = consumeTokens(b, 1, CONTINUE, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Parameter ASSIGN Expression
  public static boolean DefaultableParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefaultableParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFAULTABLE_PARAMETER, "<defaultable parameter>");
    r = Parameter(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TypeDefinition
  //                | ServiceDefinition
  //                | GlobalVariable
  //                | FunctionDefinition
  //                | AnnotationDefinition
  //                | ConstantDefinition
  public static boolean Definition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition>");
    r = TypeDefinition(b, l + 1);
    if (!r) r = ServiceDefinition(b, l + 1);
    if (!r) r = GlobalVariable(b, l + 1);
    if (!r) r = FunctionDefinition(b, l + 1);
    if (!r) r = AnnotationDefinition(b, l + 1);
    if (!r) r = ConstantDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, TopLevelDefinitionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // documentationString? deprecatedAttachment? AnnotationAttachment AnnotationAttachment+ Definition
  public static boolean DefinitionWithMultipleAnnotationAttachments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition with multiple annotation attachments>");
    r = DefinitionWithMultipleAnnotationAttachments_0(b, l + 1);
    r = r && DefinitionWithMultipleAnnotationAttachments_1(b, l + 1);
    r = r && AnnotationAttachment(b, l + 1);
    r = r && DefinitionWithMultipleAnnotationAttachments_3(b, l + 1);
    p = r; // pin = 4
    r = r && Definition(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean DefinitionWithMultipleAnnotationAttachments_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // deprecatedAttachment?
  private static boolean DefinitionWithMultipleAnnotationAttachments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments_1")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  // AnnotationAttachment+
  private static boolean DefinitionWithMultipleAnnotationAttachments_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = AnnotationAttachment(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "DefinitionWithMultipleAnnotationAttachments_3", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // documentationString? deprecatedAttachment? AnnotationAttachment Definition
  public static boolean DefinitionWithSingleAnnotationAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithSingleAnnotationAttachment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition with single annotation attachment>");
    r = DefinitionWithSingleAnnotationAttachment_0(b, l + 1);
    r = r && DefinitionWithSingleAnnotationAttachment_1(b, l + 1);
    r = r && AnnotationAttachment(b, l + 1);
    p = r; // pin = 3
    r = r && Definition(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean DefinitionWithSingleAnnotationAttachment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithSingleAnnotationAttachment_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // deprecatedAttachment?
  private static boolean DefinitionWithSingleAnnotationAttachment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithSingleAnnotationAttachment_1")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // documentationString? deprecatedAttachment? Definition
  public static boolean DefinitionWithoutAnnotationAttachments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithoutAnnotationAttachments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, DEFINITION, "<definition without annotation attachments>");
    r = DefinitionWithoutAnnotationAttachments_0(b, l + 1);
    r = r && DefinitionWithoutAnnotationAttachments_1(b, l + 1);
    r = r && Definition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationString?
  private static boolean DefinitionWithoutAnnotationAttachments_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithoutAnnotationAttachments_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // deprecatedAttachment?
  private static boolean DefinitionWithoutAnnotationAttachments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithoutAnnotationAttachments_1")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // done SEMICOLON
  public static boolean DoneStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DoneStatement")) return false;
    if (!nextTokenIs(b, DONE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DONE_STATEMENT, null);
    r = consumeTokens(b, 1, DONE, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // StartTag Content CloseTag | EmptyTag
  public static boolean Element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Element")) return false;
    if (!nextTokenIs(b, XML_TAG_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Element_0(b, l + 1);
    if (!r) r = EmptyTag(b, l + 1);
    exit_section_(b, m, ELEMENT, r);
    return r;
  }

  // StartTag Content CloseTag
  private static boolean Element_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Element_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StartTag(b, l + 1);
    r = r && Content(b, l + 1);
    r = r && CloseTag(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // else (LEFT_BRACE Block RIGHT_BRACE)
  public static boolean ElseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_CLAUSE, null);
    r = consumeToken(b, ELSE);
    p = r; // pin = 1
    r = r && ElseClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean ElseClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // else pinnedElseClause
  public static boolean ElseIfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseIfClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && pinnedElseClause(b, l + 1);
    exit_section_(b, m, ELSE_IF_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // XML_TAG_OPEN XmlQualifiedName Attribute* XML_TAG_SLASH_CLOSE
  public static boolean EmptyTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EmptyTag")) return false;
    if (!nextTokenIs(b, XML_TAG_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EMPTY_TAG, null);
    r = consumeToken(b, XML_TAG_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, XmlQualifiedName(b, l + 1));
    r = p && report_error_(b, EmptyTag_2(b, l + 1)) && r;
    r = p && consumeToken(b, XML_TAG_SLASH_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Attribute*
  private static boolean EmptyTag_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EmptyTag_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Attribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EmptyTag_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS RIGHT_PARENTHESIS
  public static boolean EmptyTupleLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EmptyTupleLiteral")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LEFT_PARENTHESIS, RIGHT_PARENTHESIS);
    exit_section_(b, m, EMPTY_TUPLE_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // FieldBindingPattern (COMMA FieldBindingPattern)* (COMMA RestBindingPattern)? | RestBindingPattern
  public static boolean EntryBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_BINDING_PATTERN, "<entry binding pattern>");
    r = EntryBindingPattern_0(b, l + 1);
    if (!r) r = RestBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FieldBindingPattern (COMMA FieldBindingPattern)* (COMMA RestBindingPattern)?
  private static boolean EntryBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FieldBindingPattern(b, l + 1);
    r = r && EntryBindingPattern_0_1(b, l + 1);
    r = r && EntryBindingPattern_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA FieldBindingPattern)*
  private static boolean EntryBindingPattern_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!EntryBindingPattern_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EntryBindingPattern_0_1", c)) break;
    }
    return true;
  }

  // COMMA FieldBindingPattern
  private static boolean EntryBindingPattern_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FieldBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestBindingPattern)?
  private static boolean EntryBindingPattern_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_2")) return false;
    EntryBindingPattern_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestBindingPattern
  private static boolean EntryBindingPattern_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FieldRefBindingPattern (COMMA FieldRefBindingPattern)* (COMMA RestRefBindingPattern)? | RestRefBindingPattern
  public static boolean EntryRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_REF_BINDING_PATTERN, "<entry ref binding pattern>");
    r = EntryRefBindingPattern_0(b, l + 1);
    if (!r) r = RestRefBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FieldRefBindingPattern (COMMA FieldRefBindingPattern)* (COMMA RestRefBindingPattern)?
  private static boolean EntryRefBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FieldRefBindingPattern(b, l + 1);
    r = r && EntryRefBindingPattern_0_1(b, l + 1);
    r = r && EntryRefBindingPattern_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA FieldRefBindingPattern)*
  private static boolean EntryRefBindingPattern_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!EntryRefBindingPattern_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EntryRefBindingPattern_0_1", c)) break;
    }
    return true;
  }

  // COMMA FieldRefBindingPattern
  private static boolean EntryRefBindingPattern_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FieldRefBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestRefBindingPattern)?
  private static boolean EntryRefBindingPattern_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_2")) return false;
    EntryRefBindingPattern_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestRefBindingPattern
  private static boolean EntryRefBindingPattern_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestRefBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // error (LT TypeName (COMMA TypeName)? GT)?
  public static boolean ErrorTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName")) return false;
    if (!nextTokenIs(b, ERROR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ERROR_TYPE_NAME, null);
    r = consumeToken(b, ERROR);
    p = r; // pin = 1
    r = r && ErrorTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName (COMMA TypeName)? GT)?
  private static boolean ErrorTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1")) return false;
    ErrorTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName (COMMA TypeName)? GT
  private static boolean ErrorTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && ErrorTypeName_1_0_2(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TypeName)?
  private static boolean ErrorTypeName_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0_2")) return false;
    ErrorTypeName_1_0_2_0(b, l + 1);
    return true;
  }

  // COMMA TypeName
  private static boolean ErrorTypeName_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression (COMMA Expression)*
  public static boolean ExpressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, "<expression list>");
    r = Expression(b, l + 1, -1);
    p = r; // pin = 1
    r = r && ExpressionList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Expression)*
  private static boolean ExpressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ExpressionList_1", c)) break;
    }
    return true;
  }

  // COMMA Expression
  private static boolean ExpressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(NULL_LITERAL|int|string|float|boolean|byte|any|map|table|function|stream|'}'|';'|var|while|match|foreach|continue|break|fork|try|throw|return|abort|fail|lock|xmlns|transaction|but|if|forever|object|trap|error|check)
  static boolean ExpressionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !ExpressionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NULL_LITERAL|int|string|float|boolean|byte|any|map|table|function|stream|'}'|';'|var|while|match|foreach|continue|break|fork|try|throw|return|abort|fail|lock|xmlns|transaction|but|if|forever|object|trap|error|check
  private static boolean ExpressionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, MATCH);
    if (!r) r = consumeToken(b, FOREACH);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, FORK);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, THROW);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, ABORT);
    if (!r) r = consumeToken(b, FAIL);
    if (!r) r = consumeToken(b, LOCK);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, TRANSACTION);
    if (!r) r = consumeToken(b, BUT);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, TRAP);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, CHECK);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression SEMICOLON
  public static boolean ExpressionStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionStmt")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_STMT, "<expression stmt>");
    r = Expression(b, l + 1, -1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (DOT | NOT) (identifier | MUL) {
  //     /*pin=1*/
  // //    stubClass="io.ballerina.plugins.idea.stubs.BallerinaFieldStub"
  // }
  public static boolean Field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field")) return false;
    if (!nextTokenIs(b, "<field>", DOT, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD, "<field>");
    r = Field_0(b, l + 1);
    r = r && Field_1(b, l + 1);
    r = r && Field_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOT | NOT
  private static boolean Field_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_0")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NOT);
    return r;
  }

  // identifier | MUL
  private static boolean Field_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, MUL);
    return r;
  }

  // {
  //     /*pin=1*/
  // //    stubClass="io.ballerina.plugins.idea.stubs.BallerinaFieldStub"
  // }
  private static boolean Field_2(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // identifier (COLON BindingPattern)?
  public static boolean FieldBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldBindingPattern")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_BINDING_PATTERN, null);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && FieldBindingPattern_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COLON BindingPattern)?
  private static boolean FieldBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldBindingPattern_1")) return false;
    FieldBindingPattern_1_0(b, l + 1);
    return true;
  }

  // COLON BindingPattern
  private static boolean FieldBindingPattern_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldBindingPattern_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && BindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName identifier QUESTION_MARK? (ASSIGN Expression)? SEMICOLON
  public static boolean FieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION, "<field definition>");
    r = FieldDefinition_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 3
    r = r && report_error_(b, FieldDefinition_3(b, l + 1));
    r = p && report_error_(b, FieldDefinition_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean FieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDefinition_0", c)) break;
    }
    return true;
  }

  // QUESTION_MARK?
  private static boolean FieldDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_3")) return false;
    consumeToken(b, QUESTION_MARK);
    return true;
  }

  // (ASSIGN Expression)?
  private static boolean FieldDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_4")) return false;
    FieldDefinition_4_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean FieldDefinition_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier (COLON BindingRefPattern)?
  public static boolean FieldRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldRefBindingPattern")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_REF_BINDING_PATTERN, null);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && FieldRefBindingPattern_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COLON BindingRefPattern)?
  private static boolean FieldRefBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldRefBindingPattern_1")) return false;
    FieldRefBindingPattern_1_0(b, l + 1);
    return true;
  }

  // COLON BindingRefPattern
  private static boolean FieldRefBindingPattern_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldRefBindingPattern_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && BindingRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // finally {LEFT_BRACE Block RIGHT_BRACE}
  public static boolean FinallyClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FinallyClause")) return false;
    if (!nextTokenIs(b, FINALLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FINALLY_CLAUSE, null);
    r = consumeToken(b, FINALLY);
    p = r; // pin = 1
    r = r && FinallyClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean FinallyClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FinallyClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FiniteTypeUnit (PIPE FiniteTypeUnit)*
  public static boolean FiniteType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FINITE_TYPE, "<finite type>");
    r = FiniteTypeUnit(b, l + 1);
    r = r && FiniteType_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (PIPE FiniteTypeUnit)*
  private static boolean FiniteType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteType_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FiniteType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FiniteType_1", c)) break;
    }
    return true;
  }

  // PIPE FiniteTypeUnit
  private static boolean FiniteType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && FiniteTypeUnit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SimpleLiteral | TypeName
  public static boolean FiniteTypeUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteTypeUnit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FINITE_TYPE_UNIT, "<finite type unit>");
    r = SimpleLiteral(b, l + 1);
    if (!r) r = TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL_FLOATING_POINT_NUMBER | HEXADECIMAL_FLOATING_POINT_LITERAL
  public static boolean FloatingPointLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FloatingPointLiteral")) return false;
    if (!nextTokenIs(b, "<floating point literal>", DECIMAL_FLOATING_POINT_NUMBER, HEXADECIMAL_FLOATING_POINT_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FLOATING_POINT_LITERAL, "<floating point literal>");
    r = consumeToken(b, DECIMAL_FLOATING_POINT_NUMBER);
    if (!r) r = consumeToken(b, HEXADECIMAL_FLOATING_POINT_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // foreach (LEFT_PARENTHESIS? VariableReferenceList in Expression RIGHT_PARENTHESIS? (LEFT_BRACE Block RIGHT_BRACE))
  public static boolean ForeachStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement")) return false;
    if (!nextTokenIs(b, FOREACH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOREACH_STATEMENT, null);
    r = consumeToken(b, FOREACH);
    p = r; // pin = 1
    r = r && ForeachStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS? VariableReferenceList in Expression RIGHT_PARENTHESIS? (LEFT_BRACE Block RIGHT_BRACE)
  private static boolean ForeachStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ForeachStatement_1_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, VariableReferenceList(b, l + 1));
    r = p && report_error_(b, consumeToken(b, IN)) && r;
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
    r = p && report_error_(b, ForeachStatement_1_4(b, l + 1)) && r;
    r = p && ForeachStatement_1_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS?
  private static boolean ForeachStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_0")) return false;
    consumeToken(b, LEFT_PARENTHESIS);
    return true;
  }

  // RIGHT_PARENTHESIS?
  private static boolean ForeachStatement_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_4")) return false;
    consumeToken(b, RIGHT_PARENTHESIS);
    return true;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean ForeachStatement_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_5")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // forever (LEFT_BRACE ForeverStatementBody RIGHT_BRACE)
  public static boolean ForeverStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatement")) return false;
    if (!nextTokenIs(b, FOREVER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOREVER_STATEMENT, null);
    r = consumeToken(b, FOREVER);
    p = r; // pin = 1
    r = r && ForeverStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE ForeverStatementBody RIGHT_BRACE
  private static boolean ForeverStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ForeverStatementBody(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // StreamingQueryStatement+
  public static boolean ForeverStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatementBody")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StreamingQueryStatement(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!StreamingQueryStatement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForeverStatementBody", c)) break;
    }
    exit_section_(b, m, FOREVER_STATEMENT_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // fork (LEFT_BRACE ForkStatementBody RIGHT_BRACE JoinClause? TimeoutClause?)
  public static boolean ForkJoinStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement")) return false;
    if (!nextTokenIs(b, FORK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FORK_JOIN_STATEMENT, null);
    r = consumeToken(b, FORK);
    p = r; // pin = 1
    r = r && ForkJoinStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE ForkStatementBody RIGHT_BRACE JoinClause? TimeoutClause?
  private static boolean ForkJoinStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ForkStatementBody(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_BRACE)) && r;
    r = p && report_error_(b, ForkJoinStatement_1_3(b, l + 1)) && r;
    r = p && ForkJoinStatement_1_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // JoinClause?
  private static boolean ForkJoinStatement_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement_1_3")) return false;
    JoinClause(b, l + 1);
    return true;
  }

  // TimeoutClause?
  private static boolean ForkJoinStatement_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement_1_4")) return false;
    TimeoutClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // WorkerDefinition*
  public static boolean ForkStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkStatementBody")) return false;
    Marker m = enter_section_(b, l, _NONE_, FORK_STATEMENT_BODY, "<fork statement body>");
    while (true) {
      int c = current_position_(b);
      if (!WorkerDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForkStatementBody", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // (DefaultableParameter | Parameter) (COMMA (DefaultableParameter | Parameter))* (COMMA RestParameter)? | RestParameter
  public static boolean FormalParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FORMAL_PARAMETER_LIST, "<formal parameter list>");
    r = FormalParameterList_0(b, l + 1);
    if (!r) r = RestParameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (DefaultableParameter | Parameter) (COMMA (DefaultableParameter | Parameter))* (COMMA RestParameter)?
  private static boolean FormalParameterList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FormalParameterList_0_0(b, l + 1);
    r = r && FormalParameterList_0_1(b, l + 1);
    r = r && FormalParameterList_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DefaultableParameter | Parameter
  private static boolean FormalParameterList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_0")) return false;
    boolean r;
    r = DefaultableParameter(b, l + 1);
    if (!r) r = Parameter(b, l + 1);
    return r;
  }

  // (COMMA (DefaultableParameter | Parameter))*
  private static boolean FormalParameterList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FormalParameterList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FormalParameterList_0_1", c)) break;
    }
    return true;
  }

  // COMMA (DefaultableParameter | Parameter)
  private static boolean FormalParameterList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FormalParameterList_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DefaultableParameter | Parameter
  private static boolean FormalParameterList_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1_0_1")) return false;
    boolean r;
    r = DefaultableParameter(b, l + 1);
    if (!r) r = Parameter(b, l + 1);
    return r;
  }

  // (COMMA RestParameter)?
  private static boolean FormalParameterList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_2")) return false;
    FormalParameterList_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestParameter
  private static boolean FormalParameterList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (public)? (remote)? (extern)? function (FunctionWithoutReceiver | FunctionWithReceiver)
  public static boolean FunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION, "<function definition>");
    r = FunctionDefinition_0(b, l + 1);
    r = r && FunctionDefinition_1(b, l + 1);
    r = r && FunctionDefinition_2(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 4
    r = r && FunctionDefinition_4(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public)?
  private static boolean FunctionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // (remote)?
  private static boolean FunctionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_1")) return false;
    consumeToken(b, REMOTE);
    return true;
  }

  // (extern)?
  private static boolean FunctionDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_2")) return false;
    consumeToken(b, EXTERN);
    return true;
  }

  // FunctionWithoutReceiver | FunctionWithReceiver
  private static boolean FunctionDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_4")) return false;
    boolean r;
    r = FunctionWithoutReceiver(b, l + 1);
    if (!r) r = FunctionWithReceiver(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // functionNameReference LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean FunctionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocation")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_INVOCATION, "<function invocation>");
    r = functionNameReference(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 2
    r = r && report_error_(b, FunctionInvocation_2(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // InvocationArgList?
  private static boolean FunctionInvocation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocation_2")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // function LEFT_PARENTHESIS (ParameterList | ParameterTypeNameList)? RIGHT_PARENTHESIS ReturnParameter?
  public static boolean FunctionTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_TYPE_NAME, null);
    r = consumeTokens(b, 0, FUNCTION, LEFT_PARENTHESIS);
    r = r && FunctionTypeName_2(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS));
    r = p && FunctionTypeName_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (ParameterList | ParameterTypeNameList)?
  private static boolean FunctionTypeName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_2")) return false;
    FunctionTypeName_2_0(b, l + 1);
    return true;
  }

  // ParameterList | ParameterTypeNameList
  private static boolean FunctionTypeName_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_2_0")) return false;
    boolean r;
    r = ParameterList(b, l + 1);
    if (!r) r = ParameterTypeNameList(b, l + 1);
    return r;
  }

  // ReturnParameter?
  private static boolean FunctionTypeName_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_4")) return false;
    ReturnParameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (TypeName DOT)? CallableUnitSignature (CallableUnitBody | SEMICOLON)
  static boolean FunctionWithReceiver(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = FunctionWithReceiver_0(b, l + 1);
    r = r && CallableUnitSignature(b, l + 1);
    p = r; // pin = 2
    r = r && FunctionWithReceiver_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (TypeName DOT)?
  private static boolean FunctionWithReceiver_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver_0")) return false;
    FunctionWithReceiver_0_0(b, l + 1);
    return true;
  }

  // TypeName DOT
  private static boolean FunctionWithReceiver_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // CallableUnitBody | SEMICOLON
  private static boolean FunctionWithReceiver_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver_2")) return false;
    boolean r;
    r = CallableUnitBody(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // AttachedObject DOT CallableUnitSignature CallableUnitBody
  static boolean FunctionWithoutReceiver(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithoutReceiver")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = AttachedObject(b, l + 1);
    r = r && consumeToken(b, DOT);
    p = r; // pin = 2
    r = r && report_error_(b, CallableUnitSignature(b, l + 1));
    r = p && CallableUnitBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // future (LT TypeName GT)?
  public static boolean FutureTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName")) return false;
    if (!nextTokenIs(b, FUTURE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUTURE_TYPE_NAME, null);
    r = consumeToken(b, FUTURE);
    p = r; // pin = 1
    r = r && FutureTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName GT)?
  private static boolean FutureTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName_1")) return false;
    FutureTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName GT
  private static boolean FutureTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // GlobalVariableDefinition | ChannelDefinition
  public static boolean GlobalVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariable")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE, "<global variable>");
    r = GlobalVariableDefinition(b, l + 1);
    if (!r) r = ChannelDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (public)? (listener)? TypeName identifier (ASSIGN  Expression)? SEMICOLON
  public static boolean GlobalVariableDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE_DEFINITION, "<global variable definition>");
    r = GlobalVariableDefinition_0(b, l + 1);
    r = r && GlobalVariableDefinition_1(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    p = r; // pin = 3
    r = r && report_error_(b, consumeToken(b, IDENTIFIER));
    r = p && report_error_(b, GlobalVariableDefinition_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public)?
  private static boolean GlobalVariableDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // (listener)?
  private static boolean GlobalVariableDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition_1")) return false;
    consumeToken(b, LISTENER);
    return true;
  }

  // (ASSIGN  Expression)?
  private static boolean GlobalVariableDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition_4")) return false;
    GlobalVariableDefinition_4_0(b, l + 1);
    return true;
  }

  // ASSIGN  Expression
  private static boolean GlobalVariableDefinition_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // group by VariableReferenceList
  public static boolean GroupByClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupByClause")) return false;
    if (!nextTokenIs(b, GROUP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP_BY_CLAUSE, null);
    r = consumeTokens(b, 1, GROUP, BY);
    p = r; // pin = 1
    r = r && VariableReferenceList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // having Expression
  public static boolean HavingClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "HavingClause")) return false;
    if (!nextTokenIs(b, HAVING)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HAVING_CLAUSE, null);
    r = consumeToken(b, HAVING);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // if Expression (LEFT_BRACE Block RIGHT_BRACE)
  public static boolean IfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfClause")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_CLAUSE, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && IfClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean IfClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IfClause ElseIfClause* ElseClause?
  public static boolean IfElseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = IfClause(b, l + 1);
    r = r && IfElseStatement_1(b, l + 1);
    r = r && IfElseStatement_2(b, l + 1);
    exit_section_(b, m, IF_ELSE_STATEMENT, r);
    return r;
  }

  // ElseIfClause*
  private static boolean IfElseStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ElseIfClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "IfElseStatement_1", c)) break;
    }
    return true;
  }

  // ElseClause?
  private static boolean IfElseStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement_2")) return false;
    ElseClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // import (OrgName DIV)? CompletePackageName PackageVersion? Alias? SEMICOLON
  public static boolean ImportDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IMPORT_DECLARATION, null);
    r = consumeToken(b, IMPORT);
    p = r; // pin = 1
    r = r && report_error_(b, ImportDeclaration_1(b, l + 1));
    r = p && report_error_(b, CompletePackageName(b, l + 1)) && r;
    r = p && report_error_(b, ImportDeclaration_3(b, l + 1)) && r;
    r = p && report_error_(b, ImportDeclaration_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (OrgName DIV)?
  private static boolean ImportDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_1")) return false;
    ImportDeclaration_1_0(b, l + 1);
    return true;
  }

  // OrgName DIV
  private static boolean ImportDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = OrgName(b, l + 1);
    r = r && consumeToken(b, DIV);
    exit_section_(b, m, null, r);
    return r;
  }

  // PackageVersion?
  private static boolean ImportDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_3")) return false;
    PackageVersion(b, l + 1);
    return true;
  }

  // Alias?
  private static boolean ImportDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_4")) return false;
    Alias(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LEFT_BRACKET Expression RIGHT_BRACKET
  public static boolean Index(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Index")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, INDEX, r);
    return r;
  }

  /* ********************************************************** */
  // openRange | closedRange
  public static boolean IntRangeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntRangeExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INT_RANGE_EXPRESSION, "<int range expression>");
    r = openRange(b, l + 1);
    if (!r) r = closedRange(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL_INTEGER_LITERAL | HEX_INTEGER_LITERAL | BINARY_INTEGER_LITERAL
  public static boolean IntegerLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntegerLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INTEGER_LITERAL, "<integer literal>");
    r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, HEX_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, BINARY_INTEGER_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (DOT | NOT) AnyIdentifierName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean Invocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation")) return false;
    if (!nextTokenIs(b, "<invocation>", DOT, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INVOCATION, "<invocation>");
    r = Invocation_0(b, l + 1);
    r = r && AnyIdentifierName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && Invocation_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOT | NOT
  private static boolean Invocation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation_0")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NOT);
    return r;
  }

  // InvocationArgList?
  private static boolean Invocation_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation_3")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // NamedArgs | RestArgs | Expression
  public static boolean InvocationArg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArg")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INVOCATION_ARG, "<invocation arg>");
    r = NamedArgs(b, l + 1);
    if (!r) r = RestArgs(b, l + 1);
    if (!r) r = Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // InvocationArg (COMMA InvocationArg)*
  public static boolean InvocationArgList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INVOCATION_ARG_LIST, "<invocation arg list>");
    r = InvocationArg(b, l + 1);
    p = r; // pin = 1
    r = r && InvocationArgList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA InvocationArg)*
  private static boolean InvocationArgList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!InvocationArgList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InvocationArgList_1", c)) break;
    }
    return true;
  }

  // COMMA InvocationArg
  private static boolean InvocationArgList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && InvocationArg(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // join (LEFT_PARENTHESIS JoinConditions RIGHT_PARENTHESIS)? (LEFT_PARENTHESIS TypeName identifier (RIGHT_PARENTHESIS JoinClauseBody))
  public static boolean JoinClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClause")) return false;
    if (!nextTokenIs(b, JOIN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CLAUSE, null);
    r = consumeToken(b, JOIN);
    p = r; // pin = 1
    r = r && report_error_(b, JoinClause_1(b, l + 1));
    r = p && JoinClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LEFT_PARENTHESIS JoinConditions RIGHT_PARENTHESIS)?
  private static boolean JoinClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClause_1")) return false;
    JoinClause_1_0(b, l + 1);
    return true;
  }

  // LEFT_PARENTHESIS JoinConditions RIGHT_PARENTHESIS
  private static boolean JoinClause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClause_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, JoinConditions(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS TypeName identifier (RIGHT_PARENTHESIS JoinClauseBody)
  private static boolean JoinClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, IDENTIFIER)) && r;
    r = p && JoinClause_2_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RIGHT_PARENTHESIS JoinClauseBody
  private static boolean JoinClause_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClause_2_3")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, RIGHT_PARENTHESIS);
    p = r; // pin = 1
    r = r && JoinClauseBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean JoinClauseBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClauseBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CLAUSE_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(')'|'}')
  static boolean JoinConditionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !JoinConditionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')'|'}'
  private static boolean JoinConditionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditionRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_PARENTHESIS);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (some IntegerLiteral | all) (identifier (COMMA identifier)*)?
  public static boolean JoinConditions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CONDITIONS, "<join conditions>");
    r = JoinConditions_0(b, l + 1);
    p = r; // pin = 1
    r = r && JoinConditions_1(b, l + 1);
    exit_section_(b, l, m, r, p, JoinConditionRecover_parser_);
    return r || p;
  }

  // some IntegerLiteral | all
  private static boolean JoinConditions_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinConditions_0_0(b, l + 1);
    if (!r) r = consumeToken(b, ALL);
    exit_section_(b, m, null, r);
    return r;
  }

  // some IntegerLiteral
  private static boolean JoinConditions_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_0_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, SOME);
    p = r; // pin = 1
    r = r && IntegerLiteral(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (identifier (COMMA identifier)*)?
  private static boolean JoinConditions_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_1")) return false;
    JoinConditions_1_0(b, l + 1);
    return true;
  }

  // identifier (COMMA identifier)*
  private static boolean JoinConditions_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && JoinConditions_1_0_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA identifier)*
  private static boolean JoinConditions_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!JoinConditions_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "JoinConditions_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA identifier
  private static boolean JoinConditions_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_1_0_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, COMMA, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (unidirectional JoinType | JoinType unidirectional | JoinType) StreamingInput (on Expression)?
  public static boolean JoinStreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_STREAMING_INPUT, "<join streaming input>");
    r = JoinStreamingInput_0(b, l + 1);
    r = r && StreamingInput(b, l + 1);
    r = r && JoinStreamingInput_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // unidirectional JoinType | JoinType unidirectional | JoinType
  private static boolean JoinStreamingInput_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinStreamingInput_0_0(b, l + 1);
    if (!r) r = JoinStreamingInput_0_1(b, l + 1);
    if (!r) r = JoinType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unidirectional JoinType
  private static boolean JoinStreamingInput_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, UNIDIRECTIONAL);
    r = r && JoinType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // JoinType unidirectional
  private static boolean JoinStreamingInput_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinType(b, l + 1);
    r = r && consumeToken(b, UNIDIRECTIONAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // (on Expression)?
  private static boolean JoinStreamingInput_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_2")) return false;
    JoinStreamingInput_2_0(b, l + 1);
    return true;
  }

  // on Expression
  private static boolean JoinStreamingInput_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // left outer join | right outer join | full outer join | outer join | inner? join
  public static boolean JoinType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_TYPE, "<join type>");
    r = parseTokens(b, 0, LEFT, OUTER, JOIN);
    if (!r) r = parseTokens(b, 0, RIGHT, OUTER, JOIN);
    if (!r) r = parseTokens(b, 0, FULL, OUTER, JOIN);
    if (!r) r = parseTokens(b, 0, OUTER, JOIN);
    if (!r) r = JoinType_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // inner? join
  private static boolean JoinType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinType_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinType_4_0(b, l + 1);
    r = r && consumeToken(b, JOIN);
    exit_section_(b, m, null, r);
    return r;
  }

  // inner?
  private static boolean JoinType_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinType_4_0")) return false;
    consumeToken(b, INNER);
    return true;
  }

  /* ********************************************************** */
  // json (LT NameReference GT)?
  public static boolean JsonTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JsonTypeName")) return false;
    if (!nextTokenIs(b, JSON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, JSON_TYPE_NAME, null);
    r = consumeToken(b, JSON);
    p = r; // pin = 1
    r = r && JsonTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT NameReference GT)?
  private static boolean JsonTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JsonTypeName_1")) return false;
    JsonTypeName_1_0(b, l + 1);
    return true;
  }

  // LT NameReference GT
  private static boolean JsonTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JsonTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && NameReference(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // function LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS (returns LambdaReturnParameter)? CallableUnitBody
  public static boolean LambdaFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA_FUNCTION, null);
    r = consumeTokens(b, 1, FUNCTION, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, LambdaFunction_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && report_error_(b, LambdaFunction_4(b, l + 1)) && r;
    r = p && CallableUnitBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FormalParameterList?
  private static boolean LambdaFunction_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_2")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // (returns LambdaReturnParameter)?
  private static boolean LambdaFunction_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_4")) return false;
    LambdaFunction_4_0(b, l + 1);
    return true;
  }

  // returns LambdaReturnParameter
  private static boolean LambdaFunction_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_4_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, RETURNS);
    p = r; // pin = 1
    r = r && LambdaReturnParameter(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName
  public static boolean LambdaReturnParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaReturnParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA_RETURN_PARAMETER, "<lambda return parameter>");
    r = LambdaReturnParameter_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean LambdaReturnParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaReturnParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LambdaReturnParameter_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // limit DECIMAL_INTEGER_LITERAL
  public static boolean LimitClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LimitClause")) return false;
    if (!nextTokenIs(b, LIMIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIMIT_CLAUSE, null);
    r = consumeTokens(b, 1, LIMIT, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // lock LEFT_BRACE Block RIGHT_BRACE
  public static boolean LockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LockStatement")) return false;
    if (!nextTokenIs(b, LOCK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LOCK_STATEMENT, null);
    r = consumeTokens(b, 1, LOCK, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // map (LT TypeName GT)?
  public static boolean MapTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MapTypeName")) return false;
    if (!nextTokenIs(b, MAP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MAP_TYPE_NAME, null);
    r = consumeToken(b, MAP);
    p = r; // pin = 1
    r = r && MapTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName GT)?
  private static boolean MapTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MapTypeName_1")) return false;
    MapTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName GT
  private static boolean MapTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MapTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TypeName identifier? EQUAL_GT Expression
  public static boolean MatchExpressionPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MatchExpressionPatternClause")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MATCH_EXPRESSION_PATTERN_CLAUSE, "<match expression pattern clause>");
    r = TypeName(b, l + 1, -1);
    p = r; // pin = 1
    r = r && report_error_(b, MatchExpressionPatternClause_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, EQUAL_GT)) && r;
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // identifier?
  private static boolean MatchExpressionPatternClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MatchExpressionPatternClause_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // <<isPackageExpected>> PackageReference identifier | identifier
  public static boolean NameReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NameReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAME_REFERENCE, "<name reference>");
    r = NameReference_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<isPackageExpected>> PackageReference identifier
  private static boolean NameReference_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NameReference_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isPackageExpected(b, l + 1);
    r = r && PackageReference(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier ASSIGN Expression
  public static boolean NamedArgs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedArgs")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMED_ARGS, null);
    r = consumeTokens(b, 2, IDENTIFIER, ASSIGN);
    p = r; // pin = 2
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // xmlns QUOTED_STRING_LITERAL (as identifier)? SEMICOLON
  public static boolean NamespaceDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclaration")) return false;
    if (!nextTokenIs(b, XMLNS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMESPACE_DECLARATION, null);
    r = consumeTokens(b, 1, XMLNS, QUOTED_STRING_LITERAL);
    p = r; // pin = 1
    r = r && report_error_(b, NamespaceDeclaration_2(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (as identifier)?
  private static boolean NamespaceDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclaration_2")) return false;
    NamespaceDeclaration_2_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean NamespaceDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NamespaceDeclaration
  public static boolean NamespaceDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclarationStatement")) return false;
    if (!nextTokenIs(b, XMLNS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamespaceDeclaration(b, l + 1);
    exit_section_(b, m, NAMESPACE_DECLARATION_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // ObjectMember* ObjectInitializer? ObjectMember*
  public static boolean ObjectBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_BODY, "<object body>");
    r = ObjectBody_0(b, l + 1);
    r = r && ObjectBody_1(b, l + 1);
    r = r && ObjectBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ObjectMember*
  private static boolean ObjectBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ObjectMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectBody_0", c)) break;
    }
    return true;
  }

  // ObjectInitializer?
  private static boolean ObjectBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_1")) return false;
    ObjectInitializer(b, l + 1);
    return true;
  }

  // ObjectMember*
  private static boolean ObjectBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ObjectMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectBody_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ObjectParameter ASSIGN Expression
  public static boolean ObjectDefaultableParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectDefaultableParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_DEFAULTABLE_PARAMETER, "<object defaultable parameter>");
    r = ObjectParameter(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* deprecatedAttachment? (public | private)? TypeName identifier (ASSIGN Expression)? (COMMA | SEMICOLON)
  public static boolean ObjectFieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FIELD_DEFINITION, "<object field definition>");
    r = ObjectFieldDefinition_0(b, l + 1);
    r = r && ObjectFieldDefinition_1(b, l + 1);
    r = r && ObjectFieldDefinition_2(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 5
    r = r && report_error_(b, ObjectFieldDefinition_5(b, l + 1));
    r = p && ObjectFieldDefinition_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ObjectFieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFieldDefinition_0", c)) break;
    }
    return true;
  }

  // deprecatedAttachment?
  private static boolean ObjectFieldDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_1")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  // (public | private)?
  private static boolean ObjectFieldDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_2")) return false;
    ObjectFieldDefinition_2_0(b, l + 1);
    return true;
  }

  // public | private
  private static boolean ObjectFieldDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_2_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (ASSIGN Expression)?
  private static boolean ObjectFieldDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_5")) return false;
    ObjectFieldDefinition_5_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean ObjectFieldDefinition_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA | SEMICOLON
  private static boolean ObjectFieldDefinition_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_6")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* deprecatedAttachment? (public|private)? (remote|resource)? (extern)? function CallableUnitSignature (CallableUnitBody | SEMICOLON)
  public static boolean ObjectFunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FUNCTION_DEFINITION, "<object function definition>");
    r = ObjectFunctionDefinition_0(b, l + 1);
    r = r && ObjectFunctionDefinition_1(b, l + 1);
    r = r && ObjectFunctionDefinition_2(b, l + 1);
    r = r && ObjectFunctionDefinition_3(b, l + 1);
    r = r && ObjectFunctionDefinition_4(b, l + 1);
    r = r && ObjectFunctionDefinition_5(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 7
    r = r && report_error_(b, CallableUnitSignature(b, l + 1));
    r = p && ObjectFunctionDefinition_8(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean ObjectFunctionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean ObjectFunctionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFunctionDefinition_1", c)) break;
    }
    return true;
  }

  // deprecatedAttachment?
  private static boolean ObjectFunctionDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_2")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  // (public|private)?
  private static boolean ObjectFunctionDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_3")) return false;
    ObjectFunctionDefinition_3_0(b, l + 1);
    return true;
  }

  // public|private
  private static boolean ObjectFunctionDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_3_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (remote|resource)?
  private static boolean ObjectFunctionDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_4")) return false;
    ObjectFunctionDefinition_4_0(b, l + 1);
    return true;
  }

  // remote|resource
  private static boolean ObjectFunctionDefinition_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_4_0")) return false;
    boolean r;
    r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, RESOURCE);
    return r;
  }

  // (extern)?
  private static boolean ObjectFunctionDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_5")) return false;
    consumeToken(b, EXTERN);
    return true;
  }

  // CallableUnitBody | SEMICOLON
  private static boolean ObjectFunctionDefinition_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_8")) return false;
    boolean r;
    r = CallableUnitBody(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* (public)? new ObjectInitializerParameterList CallableUnitBody
  public static boolean ObjectInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializer")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_INITIALIZER, "<object initializer>");
    r = ObjectInitializer_0(b, l + 1);
    r = r && ObjectInitializer_1(b, l + 1);
    r = r && ObjectInitializer_2(b, l + 1);
    r = r && consumeToken(b, NEW);
    p = r; // pin = 4
    r = r && report_error_(b, ObjectInitializerParameterList(b, l + 1));
    r = p && CallableUnitBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean ObjectInitializer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializer_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean ObjectInitializer_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializer_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectInitializer_1", c)) break;
    }
    return true;
  }

  // (public)?
  private static boolean ObjectInitializer_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializer_2")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS ObjectParameterList? RIGHT_PARENTHESIS
  public static boolean ObjectInitializerParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializerParameterList")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_INITIALIZER_PARAMETER_LIST, null);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, ObjectInitializerParameterList_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ObjectParameterList?
  private static boolean ObjectInitializerParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializerParameterList_1")) return false;
    ObjectParameterList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ObjectFieldDefinition | ObjectFunctionDefinition | TypeReference
  public static boolean ObjectMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectMember")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_MEMBER, "<object member>");
    r = ObjectFieldDefinition(b, l + 1);
    if (!r) r = ObjectFunctionDefinition(b, l + 1);
    if (!r) r = TypeReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* (TypeName identifier | identifier)
  public static boolean ObjectParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_PARAMETER, "<object parameter>");
    r = ObjectParameter_0(b, l + 1);
    r = r && ObjectParameter_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean ObjectParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectParameter_0", c)) break;
    }
    return true;
  }

  // TypeName identifier | identifier
  private static boolean ObjectParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectParameter_1_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName identifier
  private static boolean ObjectParameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameter_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (ObjectDefaultableParameter | ObjectParameter) (COMMA (ObjectDefaultableParameter | ObjectParameter))* (COMMA RestParameter)? | RestParameter
  public static boolean ObjectParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_PARAMETER_LIST, "<object parameter list>");
    r = ObjectParameterList_0(b, l + 1);
    if (!r) r = RestParameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (ObjectDefaultableParameter | ObjectParameter) (COMMA (ObjectDefaultableParameter | ObjectParameter))* (COMMA RestParameter)?
  private static boolean ObjectParameterList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectParameterList_0_0(b, l + 1);
    r = r && ObjectParameterList_0_1(b, l + 1);
    r = r && ObjectParameterList_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ObjectDefaultableParameter | ObjectParameter
  private static boolean ObjectParameterList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_0")) return false;
    boolean r;
    r = ObjectDefaultableParameter(b, l + 1);
    if (!r) r = ObjectParameter(b, l + 1);
    return r;
  }

  // (COMMA (ObjectDefaultableParameter | ObjectParameter))*
  private static boolean ObjectParameterList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ObjectParameterList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectParameterList_0_1", c)) break;
    }
    return true;
  }

  // COMMA (ObjectDefaultableParameter | ObjectParameter)
  private static boolean ObjectParameterList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ObjectParameterList_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ObjectDefaultableParameter | ObjectParameter
  private static boolean ObjectParameterList_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_1_0_1")) return false;
    boolean r;
    r = ObjectDefaultableParameter(b, l + 1);
    if (!r) r = ObjectParameter(b, l + 1);
    return r;
  }

  // (COMMA RestParameter)?
  private static boolean ObjectParameterList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_2")) return false;
    ObjectParameterList_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestParameter
  private static boolean ObjectParameterList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // onabort ASSIGN Expression
  public static boolean OnAbortStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnAbortStatement")) return false;
    if (!nextTokenIs(b, ONABORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ON_ABORT_STATEMENT, null);
    r = consumeTokens(b, 1, ONABORT, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // oncommit ASSIGN Expression
  public static boolean OnCommitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnCommitStatement")) return false;
    if (!nextTokenIs(b, ONCOMMIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ON_COMMIT_STATEMENT, null);
    r = consumeTokens(b, 1, ONCOMMIT, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // onretry (LEFT_BRACE (Block RIGHT_BRACE))
  public static boolean OnRetryClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnRetryClause")) return false;
    if (!nextTokenIs(b, ONRETRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ON_RETRY_CLAUSE, null);
    r = consumeToken(b, ONRETRY);
    p = r; // pin = 1
    r = r && OnRetryClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE (Block RIGHT_BRACE)
  private static boolean OnRetryClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnRetryClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && OnRetryClause_1_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Block RIGHT_BRACE
  private static boolean OnRetryClause_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnRetryClause_1_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = Block(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // order by OrderByVariable (COMMA OrderByVariable)*
  public static boolean OrderByClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByClause")) return false;
    if (!nextTokenIs(b, ORDER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_CLAUSE, null);
    r = consumeTokens(b, 1, ORDER, BY);
    p = r; // pin = 1
    r = r && report_error_(b, OrderByVariable(b, l + 1));
    r = p && OrderByClause_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA OrderByVariable)*
  private static boolean OrderByClause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByClause_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!OrderByClause_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "OrderByClause_3", c)) break;
    }
    return true;
  }

  // COMMA OrderByVariable
  private static boolean OrderByClause_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByClause_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && OrderByVariable(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ascending | descending
  public static boolean OrderByType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByType")) return false;
    if (!nextTokenIs(b, "<order by type>", ASCENDING, DESCENDING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_TYPE, "<order by type>");
    r = consumeToken(b, ASCENDING);
    if (!r) r = consumeToken(b, DESCENDING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VariableReference OrderByType?
  public static boolean OrderByVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByVariable")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_VARIABLE, "<order by variable>");
    r = VariableReference(b, l + 1, -1);
    r = r && OrderByVariable_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // OrderByType?
  private static boolean OrderByVariable_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByVariable_1")) return false;
    OrderByType(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean OrgName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrgName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ORG_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // output ((all | last | first) every DECIMAL_INTEGER_LITERAL (TimeScale | events)
  //     | snapshot every DECIMAL_INTEGER_LITERAL TimeScale)
  public static boolean OutputRateLimit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit")) return false;
    if (!nextTokenIs(b, OUTPUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OUTPUT_RATE_LIMIT, null);
    r = consumeToken(b, OUTPUT);
    p = r; // pin = 1
    r = r && OutputRateLimit_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (all | last | first) every DECIMAL_INTEGER_LITERAL (TimeScale | events)
  //     | snapshot every DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean OutputRateLimit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = OutputRateLimit_1_0(b, l + 1);
    if (!r) r = OutputRateLimit_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (all | last | first) every DECIMAL_INTEGER_LITERAL (TimeScale | events)
  private static boolean OutputRateLimit_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = OutputRateLimit_1_0_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeTokens(b, -1, EVERY, DECIMAL_INTEGER_LITERAL));
    r = p && OutputRateLimit_1_0_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // all | last | first
  private static boolean OutputRateLimit_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, ALL);
    if (!r) r = consumeToken(b, LAST);
    if (!r) r = consumeToken(b, FIRST);
    return r;
  }

  // TimeScale | events
  private static boolean OutputRateLimit_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0_3")) return false;
    boolean r;
    r = TimeScale(b, l + 1);
    if (!r) r = consumeToken(b, EVENTS);
    return r;
  }

  // snapshot every DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean OutputRateLimit_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, SNAPSHOT, EVERY, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    r = r && TimeScale(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean PackageName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, PACKAGE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // identifier COLON
  public static boolean PackageReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageReference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 2, IDENTIFIER, COLON);
    exit_section_(b, m, PACKAGE_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // version identifier
  public static boolean PackageVersion(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageVersion")) return false;
    if (!nextTokenIs(b, VERSION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PACKAGE_VERSION, null);
    r = consumeTokens(b, 1, VERSION, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // panic Expression SEMICOLON
  public static boolean PanicStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PanicStatement")) return false;
    if (!nextTokenIs(b, PANIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PANIC_STATEMENT, null);
    r = consumeToken(b, PANIC);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // SimpleParameter | TupleParameter
  public static boolean Parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Parameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = SimpleParameter(b, l + 1);
    if (!r) r = TupleParameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Parameter (COMMA Parameter)*
  public static boolean ParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, "<parameter list>");
    r = Parameter(b, l + 1);
    p = r; // pin = 1
    r = r && ParameterList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Parameter)*
  private static boolean ParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterList_1", c)) break;
    }
    return true;
  }

  // COMMA Parameter
  private static boolean ParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && Parameter(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // parameterTypeName (COMMA parameterTypeName)*
  public static boolean ParameterTypeNameList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TYPE_NAME_LIST, "<parameter type name list>");
    r = parameterTypeName(b, l + 1);
    p = r; // pin = 1
    r = r && ParameterTypeNameList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA parameterTypeName)*
  private static boolean ParameterTypeNameList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ParameterTypeNameList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterTypeNameList_1", c)) break;
    }
    return true;
  }

  // COMMA parameterTypeName
  private static boolean ParameterTypeNameList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameterTypeName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PatternStreamingEdgeInput (followed by | COMMA) PatternStreamingInput
  static boolean Pattern1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = PatternStreamingEdgeInput(b, l + 1);
    r = r && Pattern1_1(b, l + 1);
    p = r; // pin = 2
    r = r && PatternStreamingInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // followed by | COMMA
  private static boolean Pattern1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, FOLLOWED, BY);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS PatternStreamingInput RIGHT_PARENTHESIS
  static boolean Pattern2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern2")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && PatternStreamingInput(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // foreach PatternStreamingInput
  static boolean Pattern3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern3")) return false;
    if (!nextTokenIs(b, FOREACH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, FOREACH);
    p = r; // pin = 1
    r = r && PatternStreamingInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NOT PatternStreamingEdgeInput (AND PatternStreamingEdgeInput | for DECIMAL_INTEGER_LITERAL TimeScale)
  static boolean Pattern4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4")) return false;
    if (!nextTokenIs(b, NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, NOT);
    p = r; // pin = 1
    r = r && report_error_(b, PatternStreamingEdgeInput(b, l + 1));
    r = p && Pattern4_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AND PatternStreamingEdgeInput | for DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean Pattern4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Pattern4_2_0(b, l + 1);
    if (!r) r = Pattern4_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AND PatternStreamingEdgeInput
  private static boolean Pattern4_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, AND);
    p = r; // pin = 1
    r = r && PatternStreamingEdgeInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // for DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean Pattern4_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, FOR, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    r = r && TimeScale(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PatternStreamingEdgeInput (AND | OR) PatternStreamingEdgeInput
  static boolean Pattern5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern5")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = PatternStreamingEdgeInput(b, l + 1);
    r = r && Pattern5_1(b, l + 1);
    p = r; // pin = 2
    r = r && PatternStreamingEdgeInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AND | OR
  private static boolean Pattern5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern5_1")) return false;
    boolean r;
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    return r;
  }

  /* ********************************************************** */
  // every? PatternStreamingInput WithinClause?
  public static boolean PatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_CLAUSE, "<pattern clause>");
    r = PatternClause_0(b, l + 1);
    r = r && PatternStreamingInput(b, l + 1);
    r = r && PatternClause_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // every?
  private static boolean PatternClause_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternClause_0")) return false;
    consumeToken(b, EVERY);
    return true;
  }

  // WithinClause?
  private static boolean PatternClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternClause_2")) return false;
    WithinClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // VariableReference WhereClause? IntRangeExpression? (as identifier)?
  public static boolean PatternStreamingEdgeInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_STREAMING_EDGE_INPUT, "<pattern streaming edge input>");
    r = VariableReference(b, l + 1, -1);
    p = r; // pin = 1
    r = r && report_error_(b, PatternStreamingEdgeInput_1(b, l + 1));
    r = p && report_error_(b, PatternStreamingEdgeInput_2(b, l + 1)) && r;
    r = p && PatternStreamingEdgeInput_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // WhereClause?
  private static boolean PatternStreamingEdgeInput_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_1")) return false;
    WhereClause(b, l + 1);
    return true;
  }

  // IntRangeExpression?
  private static boolean PatternStreamingEdgeInput_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_2")) return false;
    IntRangeExpression(b, l + 1);
    return true;
  }

  // (as identifier)?
  private static boolean PatternStreamingEdgeInput_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_3")) return false;
    PatternStreamingEdgeInput_3_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean PatternStreamingEdgeInput_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Pattern4
  //     |   Pattern5
  //     |   Pattern1
  //     |   Pattern2
  //     |   Pattern3
  //     |   PatternStreamingEdgeInput
  public static boolean PatternStreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingInput")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_STREAMING_INPUT, "<pattern streaming input>");
    r = Pattern4(b, l + 1);
    if (!r) r = Pattern5(b, l + 1);
    if (!r) r = Pattern1(b, l + 1);
    if (!r) r = Pattern2(b, l + 1);
    if (!r) r = Pattern3(b, l + 1);
    if (!r) r = PatternStreamingEdgeInput(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // XML_TAG_SPECIAL_OPEN (XML_PI_TEMPLATE_TEXT Expression EXPRESSION_END)* XML_PI_TEXT
  public static boolean ProcIns(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcIns")) return false;
    if (!nextTokenIs(b, XML_TAG_SPECIAL_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROC_INS, null);
    r = consumeToken(b, XML_TAG_SPECIAL_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, ProcIns_1(b, l + 1));
    r = p && consumeToken(b, XML_PI_TEXT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (XML_PI_TEMPLATE_TEXT Expression EXPRESSION_END)*
  private static boolean ProcIns_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcIns_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ProcIns_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ProcIns_1", c)) break;
    }
    return true;
  }

  // XML_PI_TEMPLATE_TEXT Expression EXPRESSION_END
  private static boolean ProcIns_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcIns_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_PI_TEMPLATE_TEXT);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE EntryBindingPattern RIGHT_BRACE
  public static boolean RecordBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_BINDING_PATTERN, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, EntryBindingPattern(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // var? RecordRefBindingPattern ASSIGN Expression SEMICOLON
  public static boolean RecordDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordDestructuringStatement")) return false;
    if (!nextTokenIs(b, "<record destructuring statement>", LEFT_BRACE, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_DESTRUCTURING_STATEMENT, "<record destructuring statement>");
    r = RecordDestructuringStatement_0(b, l + 1);
    r = r && RecordRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 3
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // var?
  private static boolean RecordDestructuringStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordDestructuringStatement_0")) return false;
    consumeToken(b, VAR);
    return true;
  }

  /* ********************************************************** */
  // (FieldDefinition | TypeReference)* RecordRestFieldDefinition?
  public static boolean RecordFieldDefinitionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordFieldDefinitionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_FIELD_DEFINITION_LIST, "<record field definition list>");
    r = RecordFieldDefinitionList_0(b, l + 1);
    r = r && RecordFieldDefinitionList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (FieldDefinition | TypeReference)*
  private static boolean RecordFieldDefinitionList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordFieldDefinitionList_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!RecordFieldDefinitionList_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RecordFieldDefinitionList_0", c)) break;
    }
    return true;
  }

  // FieldDefinition | TypeReference
  private static boolean RecordFieldDefinitionList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordFieldDefinitionList_0_0")) return false;
    boolean r;
    r = FieldDefinition(b, l + 1);
    if (!r) r = TypeReference(b, l + 1);
    return r;
  }

  // RecordRestFieldDefinition?
  private static boolean RecordFieldDefinitionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordFieldDefinitionList_1")) return false;
    RecordRestFieldDefinition(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression | identifier
  public static boolean RecordKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordKey")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_KEY, "<record key>");
    r = Expression(b, l + 1, -1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // RecordKey COLON Expression
  public static boolean RecordKeyValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordKeyValue")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_KEY_VALUE, "<record key value>");
    r = RecordKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE RecordLiteralBody? RIGHT_BRACE
  public static boolean RecordLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteral")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_LITERAL, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, RecordLiteral_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RecordLiteralBody?
  private static boolean RecordLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteral_1")) return false;
    RecordLiteralBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RecordKeyValue (COMMA RecordKeyValue)*
  public static boolean RecordLiteralBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_LITERAL_BODY, "<record literal body>");
    r = RecordKeyValue(b, l + 1);
    p = r; // pin = 1
    r = r && RecordLiteralBody_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA RecordKeyValue)*
  private static boolean RecordLiteralBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!RecordLiteralBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RecordLiteralBody_1", c)) break;
    }
    return true;
  }

  // COMMA RecordKeyValue
  private static boolean RecordLiteralBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && RecordKeyValue(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE EntryRefBindingPattern RIGHT_BRACE
  public static boolean RecordRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordRefBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_REF_BINDING_PATTERN, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, EntryRefBindingPattern(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TypeName <<restDescriptorPredicate>> ELLIPSIS | SealedLiteral
  public static boolean RecordRestFieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordRestFieldDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_REST_FIELD_DEFINITION, "<record rest field definition>");
    r = RecordRestFieldDefinition_0(b, l + 1);
    if (!r) r = SealedLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TypeName <<restDescriptorPredicate>> ELLIPSIS
  private static boolean RecordRestFieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordRestFieldDefinition_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && restDescriptorPredicate(b, l + 1);
    r = r && consumeToken(b, ELLIPSIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BuiltInReferenceTypeName
  //                       | UserDefineTypeName
  public static boolean ReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReferenceTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REFERENCE_TYPE_NAME, "<reference type name>");
    r = BuiltInReferenceTypeName(b, l + 1);
    if (!r) r = UserDefineTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // foreach | map | start | continue
  public static boolean ReservedWord(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReservedWord")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESERVED_WORD, "<reserved word>");
    r = consumeToken(b, FOREACH);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, START);
    if (!r) r = consumeToken(b, CONTINUE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS Expression
  public static boolean RestArgs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestArgs")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REST_ARGS, null);
    r = consumeToken(b, ELLIPSIS);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ELLIPSIS identifier | SealedLiteral
  public static boolean RestBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestBindingPattern")) return false;
    if (!nextTokenIs(b, "<rest binding pattern>", ELLIPSIS, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REST_BINDING_PATTERN, "<rest binding pattern>");
    r = parseTokens(b, 0, ELLIPSIS, IDENTIFIER);
    if (!r) r = SealedLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName ELLIPSIS identifier
  public static boolean RestParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestParameter")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REST_PARAMETER, "<rest parameter>");
    r = RestParameter_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 1, ELLIPSIS, IDENTIFIER);
    p = r; // pin = 3
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean RestParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RestParameter_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ELLIPSIS VariableReference | SealedLiteral
  public static boolean RestRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestRefBindingPattern")) return false;
    if (!nextTokenIs(b, "<rest ref binding pattern>", ELLIPSIS, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REST_REF_BINDING_PATTERN, "<rest ref binding pattern>");
    r = RestRefBindingPattern_0(b, l + 1);
    if (!r) r = SealedLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ELLIPSIS VariableReference
  private static boolean RestRefBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestRefBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELLIPSIS);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // retries ASSIGN Expression
  public static boolean RetriesStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RetriesStatement")) return false;
    if (!nextTokenIs(b, RETRIES)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRIES_STATEMENT, null);
    r = consumeTokens(b, 1, RETRIES, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // retry SEMICOLON
  public static boolean RetryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RetryStatement")) return false;
    if (!nextTokenIs(b, RETRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRY_STATEMENT, null);
    r = consumeTokens(b, 1, RETRY, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // returns ReturnType
  public static boolean ReturnParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnParameter")) return false;
    if (!nextTokenIs(b, RETURNS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETER, null);
    r = consumeToken(b, RETURNS);
    p = r; // pin = 1
    r = r && ReturnType(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // return Expression? SEMICOLON
  public static boolean ReturnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnStatement")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STATEMENT, null);
    r = consumeToken(b, RETURN);
    p = r; // pin = 1
    r = r && report_error_(b, ReturnStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Expression?
  private static boolean ReturnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnStatement_1")) return false;
    Expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName
  public static boolean ReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_TYPE, "<return type>");
    r = ReturnType_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean ReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnType_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ReturnType_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // scope identifier LEFT_BRACE Statement* RIGHT_BRACE
  public static boolean ScopeClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ScopeClause")) return false;
    if (!nextTokenIs(b, SCOPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCOPE_CLAUSE, null);
    r = consumeTokens(b, 1, SCOPE, IDENTIFIER, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ScopeClause_3(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Statement*
  private static boolean ScopeClause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ScopeClause_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ScopeClause_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ScopeClause CompensationClause
  public static boolean ScopeStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ScopeStatement")) return false;
    if (!nextTokenIs(b, SCOPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ScopeClause(b, l + 1);
    r = r && CompensationClause(b, l + 1);
    exit_section_(b, m, SCOPE_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // NOT <<restDescriptorPredicate>> ELLIPSIS
  public static boolean SealedLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SealedLiteral")) return false;
    if (!nextTokenIs(b, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT);
    r = r && restDescriptorPredicate(b, l + 1);
    r = r && consumeToken(b, ELLIPSIS);
    exit_section_(b, m, SEALED_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // select (MUL| SelectExpressionList) GroupByClause? HavingClause?
  public static boolean SelectClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SELECT_CLAUSE, null);
    r = consumeToken(b, SELECT);
    p = r; // pin = 1
    r = r && report_error_(b, SelectClause_1(b, l + 1));
    r = p && report_error_(b, SelectClause_2(b, l + 1)) && r;
    r = p && SelectClause_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // MUL| SelectExpressionList
  private static boolean SelectClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause_1")) return false;
    boolean r;
    r = consumeToken(b, MUL);
    if (!r) r = SelectExpressionList(b, l + 1);
    return r;
  }

  // GroupByClause?
  private static boolean SelectClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause_2")) return false;
    GroupByClause(b, l + 1);
    return true;
  }

  // HavingClause?
  private static boolean SelectClause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause_3")) return false;
    HavingClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression (as identifier)?
  public static boolean SelectExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECT_EXPRESSION, "<select expression>");
    r = Expression(b, l + 1, -1);
    r = r && SelectExpression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (as identifier)?
  private static boolean SelectExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpression_1")) return false;
    SelectExpression_1_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean SelectExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SelectExpression (COMMA SelectExpression)*
  public static boolean SelectExpressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpressionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECT_EXPRESSION_LIST, "<select expression list>");
    r = SelectExpression(b, l + 1);
    r = r && SelectExpressionList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA SelectExpression)*
  private static boolean SelectExpressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!SelectExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SelectExpressionList_1", c)) break;
    }
    return true;
  }

  // COMMA SelectExpression
  private static boolean SelectExpressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpressionList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && SelectExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE ServiceBodyMember* RIGHT_BRACE
  public static boolean ServiceBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && ServiceBody_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, SERVICE_BODY, r);
    return r;
  }

  // ServiceBodyMember*
  private static boolean ServiceBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ServiceBodyMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceBody_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ObjectFieldDefinition | ObjectFunctionDefinition
  public static boolean ServiceBodyMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBodyMember")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_BODY_MEMBER, "<service body member>");
    r = ObjectFieldDefinition(b, l + 1);
    if (!r) r = ObjectFunctionDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // service identifier? on Expression  ServiceBody
  public static boolean ServiceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition")) return false;
    if (!nextTokenIs(b, SERVICE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_DEFINITION, null);
    r = consumeToken(b, SERVICE);
    p = r; // pin = 1
    r = r && report_error_(b, ServiceDefinition_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ON)) && r;
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
    r = p && ServiceBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // identifier?
  private static boolean ServiceDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // service
  public static boolean ServiceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceTypeName")) return false;
    if (!nextTokenIs(b, SERVICE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SERVICE);
    exit_section_(b, m, SERVICE_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // VariableReference ASSIGN Expression
  public static boolean SetAssignmentClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SetAssignmentClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SET_ASSIGNMENT_CLAUSE, "<set assignment clause>");
    r = VariableReference(b, l + 1, -1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // set SetAssignmentClause (COMMA SetAssignmentClause)*
  public static boolean SetClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SetClause")) return false;
    if (!nextTokenIs(b, SET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SET_CLAUSE, null);
    r = consumeToken(b, SET);
    p = r; // pin = 1
    r = r && report_error_(b, SetAssignmentClause(b, l + 1));
    r = p && SetClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA SetAssignmentClause)*
  private static boolean SetClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SetClause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!SetClause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SetClause_2", c)) break;
    }
    return true;
  }

  // COMMA SetAssignmentClause
  private static boolean SetClause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SetClause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && SetAssignmentClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // GT <<shiftExprPredicate>> GT
  //     | LT <<shiftExprPredicate>> LT
  //     | GT <<shiftExprPredicate>> GT <<shiftExprPredicate>> GT
  public static boolean ShiftExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression")) return false;
    if (!nextTokenIs(b, "<shift expression>", GT, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SHIFT_EXPRESSION, "<shift expression>");
    r = ShiftExpression_0(b, l + 1);
    if (!r) r = ShiftExpression_1(b, l + 1);
    if (!r) r = ShiftExpression_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // GT <<shiftExprPredicate>> GT
  private static boolean ShiftExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT <<shiftExprPredicate>> LT
  private static boolean ShiftExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // GT <<shiftExprPredicate>> GT <<shiftExprPredicate>> GT
  private static boolean ShiftExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, GT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (ADD|SUB)? IntegerLiteral
  //                   | (ADD|SUB)? FloatingPointLiteral
  //                   | QUOTED_STRING_LITERAL
  //                   | SYMBOLIC_STRING_LITERAL
  //                   | BOOLEAN_LITERAL
  //                   | EmptyTupleLiteral
  //                   | BlobLiteral
  //                   | NULL_LITERAL
  public static boolean SimpleLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_LITERAL, "<simple literal>");
    r = SimpleLiteral_0(b, l + 1);
    if (!r) r = SimpleLiteral_1(b, l + 1);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    if (!r) r = consumeToken(b, SYMBOLIC_STRING_LITERAL);
    if (!r) r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = EmptyTupleLiteral(b, l + 1);
    if (!r) r = BlobLiteral(b, l + 1);
    if (!r) r = consumeToken(b, NULL_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (ADD|SUB)? IntegerLiteral
  private static boolean SimpleLiteral_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleLiteral_0_0(b, l + 1);
    r = r && IntegerLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ADD|SUB)?
  private static boolean SimpleLiteral_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_0_0")) return false;
    SimpleLiteral_0_0_0(b, l + 1);
    return true;
  }

  // ADD|SUB
  private static boolean SimpleLiteral_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_0_0_0")) return false;
    boolean r;
    r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    return r;
  }

  // (ADD|SUB)? FloatingPointLiteral
  private static boolean SimpleLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleLiteral_1_0(b, l + 1);
    r = r && FloatingPointLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ADD|SUB)?
  private static boolean SimpleLiteral_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_1_0")) return false;
    SimpleLiteral_1_0_0(b, l + 1);
    return true;
  }

  // ADD|SUB
  private static boolean SimpleLiteral_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* parameterWithType
  static boolean SimpleParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleParameter")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleParameter_0(b, l + 1);
    r = r && parameterWithType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment*
  private static boolean SimpleParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SimpleParameter_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // XML_TAG_OPEN XmlQualifiedName Attribute* XML_TAG_CLOSE
  public static boolean StartTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StartTag")) return false;
    if (!nextTokenIs(b, XML_TAG_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, START_TAG, null);
    r = consumeToken(b, XML_TAG_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, XmlQualifiedName(b, l + 1));
    r = p && report_error_(b, StartTag_2(b, l + 1)) && r;
    r = p && consumeToken(b, XML_TAG_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Attribute*
  private static boolean StartTag_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StartTag_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Attribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StartTag_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // WhileStatement
  //     |   ForeverStatement
  //     |   ContinueStatement
  //     |   ForeachStatement
  //     |   matchStatement
  //     |   BreakStatement
  //     |   ThrowStatement
  //     |   PanicStatement
  //     |   ReturnStatement
  //     |   AbortStatement
  //     |   RetryStatement
  //     |   LockStatement
  //     |   NamespaceDeclarationStatement
  //     |   TransactionStatement
  //     |   IfElseStatement
  //     |   TryCatchStatement
  //     |   ForkJoinStatement
  //     |   tupleDestructuringStatement
  //     |   RecordDestructuringStatement
  //     |   WorkerInteractionStatement
  //     |   AssignmentStatement
  //     |   VariableDefinitionStatement
  //     |   CompoundAssignmentStatement
  //     |   ExpressionStmt
  //     |   StreamingQueryStatement
  //     |   DoneStatement
  //     |   ScopeStatement
  //     |   CompensateStatement
  public static boolean Statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = WhileStatement(b, l + 1);
    if (!r) r = ForeverStatement(b, l + 1);
    if (!r) r = ContinueStatement(b, l + 1);
    if (!r) r = ForeachStatement(b, l + 1);
    if (!r) r = matchStatement(b, l + 1);
    if (!r) r = BreakStatement(b, l + 1);
    if (!r) r = ThrowStatement(b, l + 1);
    if (!r) r = PanicStatement(b, l + 1);
    if (!r) r = ReturnStatement(b, l + 1);
    if (!r) r = AbortStatement(b, l + 1);
    if (!r) r = RetryStatement(b, l + 1);
    if (!r) r = LockStatement(b, l + 1);
    if (!r) r = NamespaceDeclarationStatement(b, l + 1);
    if (!r) r = TransactionStatement(b, l + 1);
    if (!r) r = IfElseStatement(b, l + 1);
    if (!r) r = TryCatchStatement(b, l + 1);
    if (!r) r = ForkJoinStatement(b, l + 1);
    if (!r) r = tupleDestructuringStatement(b, l + 1);
    if (!r) r = RecordDestructuringStatement(b, l + 1);
    if (!r) r = WorkerInteractionStatement(b, l + 1);
    if (!r) r = AssignmentStatement(b, l + 1);
    if (!r) r = VariableDefinitionStatement(b, l + 1);
    if (!r) r = CompoundAssignmentStatement(b, l + 1);
    if (!r) r = ExpressionStmt(b, l + 1);
    if (!r) r = StreamingQueryStatement(b, l + 1);
    if (!r) r = DoneStatement(b, l + 1);
    if (!r) r = ScopeStatement(b, l + 1);
    if (!r) r = CompensateStatement(b, l + 1);
    exit_section_(b, l, m, r, false, StatementRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|SYMBOLIC_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|int|string|float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'('|'}'|';'|typedesc|future|await|var|while|match|foreach|continue|break|fork|try|throw|return|abort|retry|fail|lock|transaction|if|forever|object|check|error|panic|from|worker|done|identifier)
  static boolean StatementRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !StatementRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|SYMBOLIC_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|int|string|float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'('|'}'|';'|typedesc|future|await|var|while|match|foreach|continue|break|fork|try|throw|return|abort|retry|fail|lock|transaction|if|forever|object|check|error|panic|from|worker|done|identifier
  private static boolean StatementRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    if (!r) r = consumeToken(b, SYMBOLIC_STRING_LITERAL);
    if (!r) r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, HEX_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, OCTAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, BINARY_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DECIMAL);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ANYDATA);
    if (!r) r = consumeToken(b, JSON);
    if (!r) r = consumeToken(b, XML);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, LEFT_PARENTHESIS);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, TYPEDESC);
    if (!r) r = consumeToken(b, FUTURE);
    if (!r) r = consumeToken(b, AWAIT);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, MATCH);
    if (!r) r = consumeToken(b, FOREACH);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, FORK);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, THROW);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, ABORT);
    if (!r) r = consumeToken(b, RETRY);
    if (!r) r = consumeToken(b, FAIL);
    if (!r) r = consumeToken(b, LOCK);
    if (!r) r = consumeToken(b, TRANSACTION);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, CHECK);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, PANIC);
    if (!r) r = consumeToken(b, FROM);
    if (!r) r = consumeToken(b, WORKER);
    if (!r) r = consumeToken(b, DONE);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stream (LT TypeName GT)?
  public static boolean StreamTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName")) return false;
    if (!nextTokenIs(b, STREAM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAM_TYPE_NAME, null);
    r = consumeToken(b, STREAM);
    p = r; // pin = 1
    r = r && StreamTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName GT)?
  private static boolean StreamTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName_1")) return false;
    StreamTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName GT
  private static boolean StreamTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_GT LEFT_PARENTHESIS Parameter RIGHT_PARENTHESIS LEFT_BRACE Block RIGHT_BRACE
  public static boolean StreamingAction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingAction")) return false;
    if (!nextTokenIs(b, EQUAL_GT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_ACTION, null);
    r = consumeTokens(b, 1, EQUAL_GT, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, Parameter(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, RIGHT_PARENTHESIS, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // VariableReference WhereClause? FunctionInvocation* WindowClause? FunctionInvocation* WhereClause? (as identifier)?
  public static boolean StreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_INPUT, "<streaming input>");
    r = VariableReference(b, l + 1, -1);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingInput_1(b, l + 1));
    r = p && report_error_(b, StreamingInput_2(b, l + 1)) && r;
    r = p && report_error_(b, StreamingInput_3(b, l + 1)) && r;
    r = p && report_error_(b, StreamingInput_4(b, l + 1)) && r;
    r = p && report_error_(b, StreamingInput_5(b, l + 1)) && r;
    r = p && StreamingInput_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // WhereClause?
  private static boolean StreamingInput_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_1")) return false;
    WhereClause(b, l + 1);
    return true;
  }

  // FunctionInvocation*
  private static boolean StreamingInput_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionInvocation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StreamingInput_2", c)) break;
    }
    return true;
  }

  // WindowClause?
  private static boolean StreamingInput_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_3")) return false;
    WindowClause(b, l + 1);
    return true;
  }

  // FunctionInvocation*
  private static boolean StreamingInput_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionInvocation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StreamingInput_4", c)) break;
    }
    return true;
  }

  // WhereClause?
  private static boolean StreamingInput_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_5")) return false;
    WhereClause(b, l + 1);
    return true;
  }

  // (as identifier)?
  private static boolean StreamingInput_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_6")) return false;
    StreamingInput_6_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean StreamingInput_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // from (StreamingInput (JoinStreamingInput)? | PatternClause)
  //         SelectClause?
  //         OrderByClause?
  //         OutputRateLimit?
  //         StreamingAction
  public static boolean StreamingQueryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_QUERY_STATEMENT, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingQueryStatement_1(b, l + 1));
    r = p && report_error_(b, StreamingQueryStatement_2(b, l + 1)) && r;
    r = p && report_error_(b, StreamingQueryStatement_3(b, l + 1)) && r;
    r = p && report_error_(b, StreamingQueryStatement_4(b, l + 1)) && r;
    r = p && StreamingAction(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // StreamingInput (JoinStreamingInput)? | PatternClause
  private static boolean StreamingQueryStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StreamingQueryStatement_1_0(b, l + 1);
    if (!r) r = PatternClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // StreamingInput (JoinStreamingInput)?
  private static boolean StreamingQueryStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StreamingInput(b, l + 1);
    r = r && StreamingQueryStatement_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (JoinStreamingInput)?
  private static boolean StreamingQueryStatement_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1_0_1")) return false;
    StreamingQueryStatement_1_0_1_0(b, l + 1);
    return true;
  }

  // (JoinStreamingInput)
  private static boolean StreamingQueryStatement_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinStreamingInput(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SelectClause?
  private static boolean StreamingQueryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_2")) return false;
    SelectClause(b, l + 1);
    return true;
  }

  // OrderByClause?
  private static boolean StreamingQueryStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_3")) return false;
    OrderByClause(b, l + 1);
    return true;
  }

  // OutputRateLimit?
  private static boolean StreamingQueryStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_4")) return false;
    OutputRateLimit(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // StringTemplateExpressionContent | StringTemplateTextContent
  public static boolean StringTemplateContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateContent")) return false;
    if (!nextTokenIs(b, "<string template content>", STRING_TEMPLATE_EXPRESSION_START, STRING_TEMPLATE_TEXT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_TEMPLATE_CONTENT, "<string template content>");
    r = StringTemplateExpressionContent(b, l + 1);
    if (!r) r = StringTemplateTextContent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING_TEMPLATE_EXPRESSION_START Expression EXPRESSION_END
  static boolean StringTemplateExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpression")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING_TEMPLATE_EXPRESSION_START);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // StringTemplateExpression+ STRING_TEMPLATE_TEXT?
  static boolean StringTemplateExpressionContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpressionContent")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StringTemplateExpressionContent_0(b, l + 1);
    r = r && StringTemplateExpressionContent_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // StringTemplateExpression+
  private static boolean StringTemplateExpressionContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpressionContent_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StringTemplateExpression(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!StringTemplateExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StringTemplateExpressionContent_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // STRING_TEMPLATE_TEXT?
  private static boolean StringTemplateExpressionContent_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpressionContent_1")) return false;
    consumeToken(b, STRING_TEMPLATE_TEXT);
    return true;
  }

  /* ********************************************************** */
  // STRING_TEMPLATE_LITERAL_START StringTemplateContent? STRING_TEMPLATE_LITERAL_END
  public static boolean StringTemplateLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateLiteral")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_LITERAL_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STRING_TEMPLATE_LITERAL, null);
    r = consumeToken(b, STRING_TEMPLATE_LITERAL_START);
    p = r; // pin = 1
    r = r && report_error_(b, StringTemplateLiteral_1(b, l + 1));
    r = p && consumeToken(b, STRING_TEMPLATE_LITERAL_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // StringTemplateContent?
  private static boolean StringTemplateLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateLiteral_1")) return false;
    StringTemplateContent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // STRING_TEMPLATE_TEXT
  static boolean StringTemplateTextContent(PsiBuilder b, int l) {
    return consumeToken(b, STRING_TEMPLATE_TEXT);
  }

  /* ********************************************************** */
  // TupleBindingPattern | RecordBindingPattern
  public static boolean StructuredBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StructuredBindingPattern")) return false;
    if (!nextTokenIs(b, "<structured binding pattern>", LEFT_BRACE, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCTURED_BINDING_PATTERN, "<structured binding pattern>");
    r = TupleBindingPattern(b, l + 1);
    if (!r) r = RecordBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TupleRefBindingPattern | RecordRefBindingPattern
  public static boolean StructuredRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StructuredRefBindingPattern")) return false;
    if (!nextTokenIs(b, "<structured ref binding pattern>", LEFT_BRACE, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCTURED_REF_BINDING_PATTERN, "<structured ref binding pattern>");
    r = TupleRefBindingPattern(b, l + 1);
    if (!r) r = RecordRefBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier identifier | identifier
  public static boolean TableColumn(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumn")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, IDENTIFIER, IDENTIFIER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, TABLE_COLUMN, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE (TableColumn (COMMA TableColumn)*)? RIGHT_BRACE
  public static boolean TableColumnDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && TableColumnDefinition_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, TABLE_COLUMN_DEFINITION, r);
    return r;
  }

  // (TableColumn (COMMA TableColumn)*)?
  private static boolean TableColumnDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1")) return false;
    TableColumnDefinition_1_0(b, l + 1);
    return true;
  }

  // TableColumn (COMMA TableColumn)*
  private static boolean TableColumnDefinition_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableColumn(b, l + 1);
    r = r && TableColumnDefinition_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TableColumn)*
  private static boolean TableColumnDefinition_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TableColumnDefinition_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TableColumnDefinition_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA TableColumn
  private static boolean TableColumnDefinition_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TableColumn(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE ExpressionList RIGHT_BRACE
  public static boolean TableData(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableData")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && ExpressionList(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, TABLE_DATA, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET TableDataList? RIGHT_BRACKET
  public static boolean TableDataArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataArray")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && TableDataArray_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, TABLE_DATA_ARRAY, r);
    return r;
  }

  // TableDataList?
  private static boolean TableDataArray_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataArray_1")) return false;
    TableDataList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TableData (COMMA TableData)* | ExpressionList
  public static boolean TableDataList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TABLE_DATA_LIST, "<table data list>");
    r = TableDataList_0(b, l + 1);
    if (!r) r = ExpressionList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TableData (COMMA TableData)*
  private static boolean TableDataList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableData(b, l + 1);
    r = r && TableDataList_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TableData)*
  private static boolean TableDataList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TableDataList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TableDataList_0_1", c)) break;
    }
    return true;
  }

  // COMMA TableData
  private static boolean TableDataList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TableData(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // table LEFT_BRACE TableColumnDefinition? (COMMA TableDataArray)? RIGHT_BRACE
  public static boolean TableLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral")) return false;
    if (!nextTokenIs(b, TABLE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_LITERAL, null);
    r = consumeTokens(b, 1, TABLE, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, TableLiteral_2(b, l + 1));
    r = p && report_error_(b, TableLiteral_3(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TableColumnDefinition?
  private static boolean TableLiteral_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral_2")) return false;
    TableColumnDefinition(b, l + 1);
    return true;
  }

  // (COMMA TableDataArray)?
  private static boolean TableLiteral_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral_3")) return false;
    TableLiteral_3_0(b, l + 1);
    return true;
  }

  // COMMA TableDataArray
  private static boolean TableLiteral_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TableDataArray(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // from StreamingInput JoinStreamingInput? SelectClause? OrderByClause? LimitClause?
  public static boolean TableQuery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_QUERY, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingInput(b, l + 1));
    r = p && report_error_(b, TableQuery_2(b, l + 1)) && r;
    r = p && report_error_(b, TableQuery_3(b, l + 1)) && r;
    r = p && report_error_(b, TableQuery_4(b, l + 1)) && r;
    r = p && TableQuery_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // JoinStreamingInput?
  private static boolean TableQuery_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_2")) return false;
    JoinStreamingInput(b, l + 1);
    return true;
  }

  // SelectClause?
  private static boolean TableQuery_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_3")) return false;
    SelectClause(b, l + 1);
    return true;
  }

  // OrderByClause?
  private static boolean TableQuery_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_4")) return false;
    OrderByClause(b, l + 1);
    return true;
  }

  // LimitClause?
  private static boolean TableQuery_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_5")) return false;
    LimitClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // table (LT NameReference GT)?
  public static boolean TableTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableTypeName")) return false;
    if (!nextTokenIs(b, TABLE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_TYPE_NAME, null);
    r = consumeToken(b, TABLE);
    p = r; // pin = 1
    r = r && TableTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT NameReference GT)?
  private static boolean TableTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableTypeName_1")) return false;
    TableTypeName_1_0(b, l + 1);
    return true;
  }

  // LT NameReference GT
  private static boolean TableTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && NameReference(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // throw Expression SEMICOLON
  public static boolean ThrowStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ThrowStatement")) return false;
    if (!nextTokenIs(b, THROW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, THROW_STATEMENT, null);
    r = consumeToken(b, THROW);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // second | seconds
  //             | minute | minutes
  //             | hour | hours
  //             | day | days
  //             | month | months
  //             | year | years
  public static boolean TimeScale(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeScale")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TIME_SCALE, "<time scale>");
    r = consumeToken(b, SECOND);
    if (!r) r = consumeToken(b, SECONDS);
    if (!r) r = consumeToken(b, MINUTE);
    if (!r) r = consumeToken(b, MINUTES);
    if (!r) r = consumeToken(b, HOUR);
    if (!r) r = consumeToken(b, HOURS);
    if (!r) r = consumeToken(b, DAY);
    if (!r) r = consumeToken(b, DAYS);
    if (!r) r = consumeToken(b, MONTH);
    if (!r) r = consumeToken(b, MONTHS);
    if (!r) r = consumeToken(b, YEAR);
    if (!r) r = consumeToken(b, YEARS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // timeout (LEFT_PARENTHESIS Expression RIGHT_PARENTHESIS (LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS TimeoutClauseBody))
  public static boolean TimeoutClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeoutClause")) return false;
    if (!nextTokenIs(b, TIMEOUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TIMEOUT_CLAUSE, null);
    r = consumeToken(b, TIMEOUT);
    p = r; // pin = 1
    r = r && TimeoutClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS Expression RIGHT_PARENTHESIS (LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS TimeoutClauseBody)
  private static boolean TimeoutClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeoutClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && TimeoutClause_1_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS TimeoutClauseBody
  private static boolean TimeoutClause_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeoutClause_1_3")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, RIGHT_PARENTHESIS)) && r;
    r = p && TimeoutClauseBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean TimeoutClauseBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeoutClauseBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TIMEOUT_CLAUSE_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(MARKDOWN_DOCUMENTATION_LINE_START|PARAMETER_DOCUMENTATION_START|RETURN_PARAMETER_DOCUMENTATION_START|DEPRECATED_TEMPLATE_START|'@'|extern|remote|client|abstract|public|type|typedesc|service|listener|function|enum|annotation|int|float|decimal|boolean|string|byte|map|xml|xmlns|json|table|any|stream|object|channel|const|future|identifier|'{')
  static boolean TopLevelDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !TopLevelDefinitionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START|PARAMETER_DOCUMENTATION_START|RETURN_PARAMETER_DOCUMENTATION_START|DEPRECATED_TEMPLATE_START|'@'|extern|remote|client|abstract|public|type|typedesc|service|listener|function|enum|annotation|int|float|decimal|boolean|string|byte|map|xml|xmlns|json|table|any|stream|object|channel|const|future|identifier|'{'
  private static boolean TopLevelDefinitionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    if (!r) r = consumeToken(b, PARAMETER_DOCUMENTATION_START);
    if (!r) r = consumeToken(b, RETURN_PARAMETER_DOCUMENTATION_START);
    if (!r) r = consumeToken(b, DEPRECATED_TEMPLATE_START);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, EXTERN);
    if (!r) r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, CLIENT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, TYPEDESC);
    if (!r) r = consumeToken(b, SERVICE);
    if (!r) r = consumeToken(b, LISTENER);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, ANNOTATION);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DECIMAL);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, XML);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, JSON);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, CHANNEL);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, FUTURE);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // transaction (with TransactionPropertyInitStatementList)? (LEFT_BRACE (Block RIGHT_BRACE))
  public static boolean TransactionClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause")) return false;
    if (!nextTokenIs(b, TRANSACTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_CLAUSE, null);
    r = consumeToken(b, TRANSACTION);
    p = r; // pin = 1
    r = r && report_error_(b, TransactionClause_1(b, l + 1));
    r = p && TransactionClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (with TransactionPropertyInitStatementList)?
  private static boolean TransactionClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_1")) return false;
    TransactionClause_1_0(b, l + 1);
    return true;
  }

  // with TransactionPropertyInitStatementList
  private static boolean TransactionClause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, WITH);
    p = r; // pin = 1
    r = r && TransactionPropertyInitStatementList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE (Block RIGHT_BRACE)
  private static boolean TransactionClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && TransactionClause_2_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Block RIGHT_BRACE
  private static boolean TransactionClause_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_2_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = Block(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // RetriesStatement | OnCommitStatement | OnAbortStatement
  public static boolean TransactionPropertyInitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_PROPERTY_INIT_STATEMENT, "<transaction property init statement>");
    r = RetriesStatement(b, l + 1);
    if (!r) r = OnCommitStatement(b, l + 1);
    if (!r) r = OnAbortStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TransactionPropertyInitStatement (COMMA TransactionPropertyInitStatement)*
  public static boolean TransactionPropertyInitStatementList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatementList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_PROPERTY_INIT_STATEMENT_LIST, "<transaction property init statement list>");
    r = TransactionPropertyInitStatement(b, l + 1);
    p = r; // pin = 1
    r = r && TransactionPropertyInitStatementList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA TransactionPropertyInitStatement)*
  private static boolean TransactionPropertyInitStatementList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatementList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TransactionPropertyInitStatementList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TransactionPropertyInitStatementList_1", c)) break;
    }
    return true;
  }

  // COMMA TransactionPropertyInitStatement
  private static boolean TransactionPropertyInitStatementList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatementList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && TransactionPropertyInitStatement(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TransactionClause OnRetryClause?
  public static boolean TransactionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement")) return false;
    if (!nextTokenIs(b, TRANSACTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_STATEMENT, null);
    r = TransactionClause(b, l + 1);
    p = r; // pin = 1
    r = r && TransactionStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // OnRetryClause?
  private static boolean TransactionStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement_1")) return false;
    OnRetryClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression RARROW (fork | identifier (COMMA Expression)?) SEMICOLON
  public static boolean TriggerWorker(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TriggerWorker")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TRIGGER_WORKER, "<trigger worker>");
    r = Expression(b, l + 1, -1);
    r = r && consumeToken(b, RARROW);
    r = r && TriggerWorker_2(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // fork | identifier (COMMA Expression)?
  private static boolean TriggerWorker_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TriggerWorker_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FORK);
    if (!r) r = TriggerWorker_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // identifier (COMMA Expression)?
  private static boolean TriggerWorker_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TriggerWorker_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && TriggerWorker_2_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA Expression)?
  private static boolean TriggerWorker_2_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TriggerWorker_2_1_1")) return false;
    TriggerWorker_2_1_1_0(b, l + 1);
    return true;
  }

  // COMMA Expression
  private static boolean TriggerWorker_2_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TriggerWorker_2_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // try (LEFT_BRACE Block RIGHT_BRACE CatchClauses)
  public static boolean TryCatchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryCatchStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRY_CATCH_STATEMENT, null);
    r = consumeToken(b, TRY);
    p = r; // pin = 1
    r = r && TryCatchStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE CatchClauses
  private static boolean TryCatchStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryCatchStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_BRACE)) && r;
    r = p && CatchClauses(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS BindingPattern (COMMA BindingPattern)+ RIGHT_PARENTHESIS
  public static boolean TupleBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TUPLE_BINDING_PATTERN, null);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, BindingPattern(b, l + 1));
    r = p && report_error_(b, TupleBindingPattern_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA BindingPattern)+
  private static boolean TupleBindingPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleBindingPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TupleBindingPattern_2_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!TupleBindingPattern_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleBindingPattern_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA BindingPattern
  private static boolean TupleBindingPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleBindingPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && BindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* LEFT_PARENTHESIS parameterWithType (COMMA parameterWithType)* RIGHT_PARENTHESIS
  static boolean TupleParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleParameter")) return false;
    if (!nextTokenIs(b, "", AT, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TupleParameter_0(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && parameterWithType(b, l + 1);
    r = r && TupleParameter_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment*
  private static boolean TupleParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleParameter_0", c)) break;
    }
    return true;
  }

  // (COMMA parameterWithType)*
  private static boolean TupleParameter_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleParameter_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TupleParameter_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleParameter_3", c)) break;
    }
    return true;
  }

  // COMMA parameterWithType
  private static boolean TupleParameter_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleParameter_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameterWithType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS BindingRefPattern (COMMA BindingRefPattern)+ RIGHT_PARENTHESIS
  public static boolean TupleRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleRefBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TUPLE_REF_BINDING_PATTERN, null);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, BindingRefPattern(b, l + 1));
    r = p && report_error_(b, TupleRefBindingPattern_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA BindingRefPattern)+
  private static boolean TupleRefBindingPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleRefBindingPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TupleRefBindingPattern_2_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!TupleRefBindingPattern_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleRefBindingPattern_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA BindingRefPattern
  private static boolean TupleRefBindingPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleRefBindingPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && BindingRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (public)? type identifier FiniteType SEMICOLON
  public static boolean TypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDefinition")) return false;
    if (!nextTokenIs(b, "<type definition>", PUBLIC, TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION, "<type definition>");
    r = TypeDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 1, TYPE, IDENTIFIER);
    p = r; // pin = 2
    r = r && report_error_(b, FiniteType(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public)?
  private static boolean TypeDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // typedesc
  public static boolean TypeDescTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescTypeName")) return false;
    if (!nextTokenIs(b, TYPEDESC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TYPEDESC);
    exit_section_(b, m, TYPE_DESC_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // MUL SimpleTypeName SEMICOLON
  public static boolean TypeReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeReference")) return false;
    if (!nextTokenIs(b, MUL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MUL);
    r = r && SimpleTypeName(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, TYPE_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // NameReference
  public static boolean UserDefineTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UserDefineTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_DEFINE_TYPE_NAME, "<user define type name>");
    r = NameReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // boolean | int | float | decimal | string | byte
  public static boolean ValueTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ValueTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE_TYPE_NAME, "<value type name>");
    r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DECIMAL);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, BYTE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // variableDefinitionStatementWithAssignment | variableDefinitionStatementWithoutAssignment
  public static boolean VariableDefinitionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableDefinitionStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT, "<variable definition statement>");
    r = variableDefinitionStatementWithAssignment(b, l + 1);
    if (!r) r = variableDefinitionStatementWithoutAssignment(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VariableReference (COMMA VariableReference)*
  public static boolean VariableReferenceList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_REFERENCE_LIST, "<variable reference list>");
    r = VariableReference(b, l + 1, -1);
    p = r; // pin = 1
    r = r && VariableReferenceList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA VariableReference)*
  private static boolean VariableReferenceList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!VariableReferenceList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "VariableReferenceList_1", c)) break;
    }
    return true;
  }

  // COMMA VariableReference
  private static boolean VariableReferenceList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // where Expression
  public static boolean WhereClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhereClause")) return false;
    if (!nextTokenIs(b, WHERE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHERE_CLAUSE, null);
    r = consumeToken(b, WHERE);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // while Expression WhileStatementBody
  public static boolean WhileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT, null);
    r = consumeToken(b, WHILE);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && WhileStatementBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean WhileStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatementBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // window FunctionInvocation
  public static boolean WindowClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WindowClause")) return false;
    if (!nextTokenIs(b, WINDOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WINDOW_CLAUSE, null);
    r = consumeToken(b, WINDOW);
    p = r; // pin = 1
    r = r && FunctionInvocation(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // within DECIMAL_INTEGER_LITERAL TimeScale
  public static boolean WithinClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WithinClause")) return false;
    if (!nextTokenIs(b, WITHIN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WITHIN_CLAUSE, null);
    r = consumeTokens(b, 1, WITHIN, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    r = r && TimeScale(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean WorkerBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // worker identifier WorkerBody
  public static boolean WorkerDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_DEFINITION, "<worker definition>");
    r = consumeTokens(b, 1, WORKER, IDENTIFIER);
    p = r; // pin = 1
    r = r && WorkerBody(b, l + 1);
    exit_section_(b, l, m, r, p, WorkerDefinitionRecover_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // !(worker|'}')
  static boolean WorkerDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !WorkerDefinitionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // worker|'}'
  private static boolean WorkerDefinitionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinitionRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WORKER);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TriggerWorker | WorkerReply
  public static boolean WorkerInteractionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerInteractionStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WORKER_INTERACTION_STATEMENT, "<worker interaction statement>");
    r = TriggerWorker(b, l + 1);
    if (!r) r = WorkerReply(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Expression LARROW identifier (COMMA Expression)? SEMICOLON
  public static boolean WorkerReply(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReply")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_REPLY, "<worker reply>");
    r = Expression(b, l + 1, -1);
    r = r && consumeTokens(b, 1, LARROW, IDENTIFIER);
    p = r; // pin = 2
    r = r && report_error_(b, WorkerReply_3(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Expression)?
  private static boolean WorkerReply_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReply_3")) return false;
    WorkerReply_3_0(b, l + 1);
    return true;
  }

  // COMMA Expression
  private static boolean WorkerReply_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReply_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AT (LEFT_BRACKET Expression RIGHT_BRACKET)?
  public static boolean XmlAttrib(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAttrib")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && XmlAttrib_1(b, l + 1);
    exit_section_(b, m, XML_ATTRIB, r);
    return r;
  }

  // (LEFT_BRACKET Expression RIGHT_BRACKET)?
  private static boolean XmlAttrib_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAttrib_1")) return false;
    XmlAttrib_1_0(b, l + 1);
    return true;
  }

  // LEFT_BRACKET Expression RIGHT_BRACKET
  private static boolean XmlAttrib_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAttrib_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DOUBLE_QUOTE (XML_DOUBLE_QUOTED_TEMPLATE_STRING Expression EXPRESSION_END)* XML_DOUBLE_QUOTED_STRING_SEQUENCE? DOUBLE_QUOTE_END
  public static boolean XmlDoubleQuotedString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlDoubleQuotedString")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOUBLE_QUOTE);
    r = r && XmlDoubleQuotedString_1(b, l + 1);
    r = r && XmlDoubleQuotedString_2(b, l + 1);
    r = r && consumeToken(b, DOUBLE_QUOTE_END);
    exit_section_(b, m, XML_DOUBLE_QUOTED_STRING, r);
    return r;
  }

  // (XML_DOUBLE_QUOTED_TEMPLATE_STRING Expression EXPRESSION_END)*
  private static boolean XmlDoubleQuotedString_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlDoubleQuotedString_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!XmlDoubleQuotedString_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlDoubleQuotedString_1", c)) break;
    }
    return true;
  }

  // XML_DOUBLE_QUOTED_TEMPLATE_STRING Expression EXPRESSION_END
  private static boolean XmlDoubleQuotedString_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlDoubleQuotedString_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_DOUBLE_QUOTED_TEMPLATE_STRING);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // XML_DOUBLE_QUOTED_STRING_SEQUENCE?
  private static boolean XmlDoubleQuotedString_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlDoubleQuotedString_2")) return false;
    consumeToken(b, XML_DOUBLE_QUOTED_STRING_SEQUENCE);
    return true;
  }

  /* ********************************************************** */
  // ProcIns | Comment | Element | XmlText | cdata
  public static boolean XmlItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlItem")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, XML_ITEM, "<xml item>");
    r = ProcIns(b, l + 1);
    if (!r) r = Comment(b, l + 1);
    if (!r) r = Element(b, l + 1);
    if (!r) r = XmlText(b, l + 1);
    if (!r) r = consumeToken(b, CDATA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // XML_LITERAL_START XmlItem XML_LITERAL_END
  public static boolean XmlLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlLiteral")) return false;
    if (!nextTokenIs(b, XML_LITERAL_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, XML_LITERAL, null);
    r = consumeToken(b, XML_LITERAL_START);
    p = r; // pin = 1
    r = r && report_error_(b, XmlItem(b, l + 1));
    r = p && consumeToken(b, XML_LITERAL_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean XmlLocalName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlLocalName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, XML_LOCAL_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // QUOTED_STRING_LITERAL
  public static boolean XmlNamespaceName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlNamespaceName")) return false;
    if (!nextTokenIs(b, QUOTED_STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUOTED_STRING_LITERAL);
    exit_section_(b, m, XML_NAMESPACE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // (XML_QNAME QNAME_SEPARATOR)? XML_QNAME | XML_TAG_EXPRESSION_START Expression EXPRESSION_END
  public static boolean XmlQualifiedName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlQualifiedName")) return false;
    if (!nextTokenIs(b, "<xml qualified name>", XML_QNAME, XML_TAG_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, XML_QUALIFIED_NAME, "<xml qualified name>");
    r = XmlQualifiedName_0(b, l + 1);
    if (!r) r = XmlQualifiedName_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (XML_QNAME QNAME_SEPARATOR)? XML_QNAME
  private static boolean XmlQualifiedName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlQualifiedName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = XmlQualifiedName_0_0(b, l + 1);
    r = r && consumeToken(b, XML_QNAME);
    exit_section_(b, m, null, r);
    return r;
  }

  // (XML_QNAME QNAME_SEPARATOR)?
  private static boolean XmlQualifiedName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlQualifiedName_0_0")) return false;
    XmlQualifiedName_0_0_0(b, l + 1);
    return true;
  }

  // XML_QNAME QNAME_SEPARATOR
  private static boolean XmlQualifiedName_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlQualifiedName_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, XML_QNAME, QNAME_SEPARATOR);
    exit_section_(b, m, null, r);
    return r;
  }

  // XML_TAG_EXPRESSION_START Expression EXPRESSION_END
  private static boolean XmlQualifiedName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlQualifiedName_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_TAG_EXPRESSION_START);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // XmlSingleQuotedString | XmlDoubleQuotedString
  public static boolean XmlQuotedString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlQuotedString")) return false;
    if (!nextTokenIs(b, "<xml quoted string>", DOUBLE_QUOTE, SINGLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, XML_QUOTED_STRING, "<xml quoted string>");
    r = XmlSingleQuotedString(b, l + 1);
    if (!r) r = XmlDoubleQuotedString(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SINGLE_QUOTE (XML_SINGLE_QUOTED_TEMPLATE_STRING Expression EXPRESSION_END)* XML_SINGLE_QUOTED_STRING_SEQUENCE? SINGLE_QUOTE_END
  public static boolean XmlSingleQuotedString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlSingleQuotedString")) return false;
    if (!nextTokenIs(b, SINGLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SINGLE_QUOTE);
    r = r && XmlSingleQuotedString_1(b, l + 1);
    r = r && XmlSingleQuotedString_2(b, l + 1);
    r = r && consumeToken(b, SINGLE_QUOTE_END);
    exit_section_(b, m, XML_SINGLE_QUOTED_STRING, r);
    return r;
  }

  // (XML_SINGLE_QUOTED_TEMPLATE_STRING Expression EXPRESSION_END)*
  private static boolean XmlSingleQuotedString_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlSingleQuotedString_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!XmlSingleQuotedString_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlSingleQuotedString_1", c)) break;
    }
    return true;
  }

  // XML_SINGLE_QUOTED_TEMPLATE_STRING Expression EXPRESSION_END
  private static boolean XmlSingleQuotedString_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlSingleQuotedString_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_SINGLE_QUOTED_TEMPLATE_STRING);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // XML_SINGLE_QUOTED_STRING_SEQUENCE?
  private static boolean XmlSingleQuotedString_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlSingleQuotedString_2")) return false;
    consumeToken(b, XML_SINGLE_QUOTED_STRING_SEQUENCE);
    return true;
  }

  /* ********************************************************** */
  // (XML_TEMPLATE_TEXT Expression EXPRESSION_END)+ XML_TEXT_SEQUENCE? | XML_TEXT_SEQUENCE
  public static boolean XmlText(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlText")) return false;
    if (!nextTokenIs(b, "<xml text>", XML_TEMPLATE_TEXT, XML_TEXT_SEQUENCE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, XML_TEXT, "<xml text>");
    r = XmlText_0(b, l + 1);
    if (!r) r = consumeToken(b, XML_TEXT_SEQUENCE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (XML_TEMPLATE_TEXT Expression EXPRESSION_END)+ XML_TEXT_SEQUENCE?
  private static boolean XmlText_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlText_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = XmlText_0_0(b, l + 1);
    r = r && XmlText_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (XML_TEMPLATE_TEXT Expression EXPRESSION_END)+
  private static boolean XmlText_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlText_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = XmlText_0_0_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!XmlText_0_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlText_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // XML_TEMPLATE_TEXT Expression EXPRESSION_END
  private static boolean XmlText_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlText_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_TEMPLATE_TEXT);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  // XML_TEXT_SEQUENCE?
  private static boolean XmlText_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlText_0_1")) return false;
    consumeToken(b, XML_TEXT_SEQUENCE);
    return true;
  }

  /* ********************************************************** */
  // xml (LT (LEFT_BRACE XmlNamespaceName RIGHT_BRACE)? XmlLocalName GT)?
  public static boolean XmlTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName")) return false;
    if (!nextTokenIs(b, XML)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, XML_TYPE_NAME, null);
    r = consumeToken(b, XML);
    p = r; // pin = 1
    r = r && XmlTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT (LEFT_BRACE XmlNamespaceName RIGHT_BRACE)? XmlLocalName GT)?
  private static boolean XmlTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName_1")) return false;
    XmlTypeName_1_0(b, l + 1);
    return true;
  }

  // LT (LEFT_BRACE XmlNamespaceName RIGHT_BRACE)? XmlLocalName GT
  private static boolean XmlTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && XmlTypeName_1_0_1(b, l + 1);
    r = r && XmlLocalName(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_BRACE XmlNamespaceName RIGHT_BRACE)?
  private static boolean XmlTypeName_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName_1_0_1")) return false;
    XmlTypeName_1_0_1_0(b, l + 1);
    return true;
  }

  // LEFT_BRACE XmlNamespaceName RIGHT_BRACE
  private static boolean XmlTypeName_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && XmlNamespaceName(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // tripleBacktickedBlock | doubleBacktickedBlock | singleBacktickedBlock
  public static boolean backtickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "backtickedBlock")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BACKTICKED_BLOCK, "<backticked block>");
    r = tripleBacktickedBlock(b, l + 1);
    if (!r) r = doubleBacktickedBlock(b, l + 1);
    if (!r) r = singleBacktickedBlock(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // var BindingPattern (if Expression)? EQUAL_GT (LEFT_BRACE Block RIGHT_BRACE | Statement)
  public static boolean bindingPatternPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingPatternPattern")) return false;
    if (!nextTokenIs(b, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BINDING_PATTERN_PATTERN, null);
    r = consumeToken(b, VAR);
    r = r && BindingPattern(b, l + 1);
    r = r && bindingPatternPattern_2(b, l + 1);
    r = r && consumeToken(b, EQUAL_GT);
    p = r; // pin = 4
    r = r && bindingPatternPattern_4(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (if Expression)?
  private static boolean bindingPatternPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingPatternPattern_2")) return false;
    bindingPatternPattern_2_0(b, l + 1);
    return true;
  }

  // if Expression
  private static boolean bindingPatternPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingPatternPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE | Statement
  private static boolean bindingPatternPattern_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingPatternPattern_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = bindingPatternPattern_4_0(b, l + 1);
    if (!r) r = Statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean bindingPatternPattern_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingPatternPattern_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (LEFT_BRACKET|LEFT_PARENTHESIS) Expression RANGE Expression (RIGHT_BRACKET|RIGHT_PARENTHESIS)
  static boolean closedRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange")) return false;
    if (!nextTokenIs(b, "", LEFT_BRACKET, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = closedRange_0(b, l + 1);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RANGE);
    p = r; // pin = 3
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && closedRange_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACKET|LEFT_PARENTHESIS
  private static boolean closedRange_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange_0")) return false;
    boolean r;
    r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_PARENTHESIS);
    return r;
  }

  // RIGHT_BRACKET|RIGHT_PARENTHESIS
  private static boolean closedRange_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange_4")) return false;
    boolean r;
    r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_PARENTHESIS);
    return r;
  }

  /* ********************************************************** */
  // DEFINITION_REFERENCE
  public static boolean definitionReferenceType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "definitionReferenceType")) return false;
    if (!nextTokenIs(b, DEFINITION_REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEFINITION_REFERENCE);
    exit_section_(b, m, DEFINITION_REFERENCE_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // DEPRECATED_TEMPLATE_START deprecatedText? DEPRECATED_TEMPLATE_END
  public static boolean deprecatedAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedAttachment")) return false;
    if (!nextTokenIs(b, DEPRECATED_TEMPLATE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEPRECATED_ATTACHMENT, null);
    r = consumeToken(b, DEPRECATED_TEMPLATE_START);
    p = r; // pin = 1
    r = r && report_error_(b, deprecatedAttachment_1(b, l + 1));
    r = p && consumeToken(b, DEPRECATED_TEMPLATE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // deprecatedText?
  private static boolean deprecatedAttachment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedAttachment_1")) return false;
    deprecatedText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // singleBackTickDeprecatedInlineCode | doubleBackTickDeprecatedInlineCode | tripleBackTickDeprecatedInlineCode
  public static boolean deprecatedTemplateInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedTemplateInlineCode")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEPRECATED_TEMPLATE_INLINE_CODE, "<deprecated template inline code>");
    r = singleBackTickDeprecatedInlineCode(b, l + 1);
    if (!r) r = doubleBackTickDeprecatedInlineCode(b, l + 1);
    if (!r) r = tripleBackTickDeprecatedInlineCode(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // deprecatedTemplateInlineCode (DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode)*
  //                    | DEPRECATED_TEMPLATE_TEXT (DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode)*
  public static boolean deprecatedText(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEPRECATED_TEXT, "<deprecated text>");
    r = deprecatedText_0(b, l + 1);
    if (!r) r = deprecatedText_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // deprecatedTemplateInlineCode (DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode)*
  private static boolean deprecatedText_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = deprecatedTemplateInlineCode(b, l + 1);
    r = r && deprecatedText_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode)*
  private static boolean deprecatedText_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!deprecatedText_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deprecatedText_0_1", c)) break;
    }
    return true;
  }

  // DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode
  private static boolean deprecatedText_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_0_1_0")) return false;
    boolean r;
    r = consumeToken(b, DEPRECATED_TEMPLATE_TEXT);
    if (!r) r = deprecatedTemplateInlineCode(b, l + 1);
    return r;
  }

  // DEPRECATED_TEMPLATE_TEXT (DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode)*
  private static boolean deprecatedText_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEPRECATED_TEMPLATE_TEXT);
    r = r && deprecatedText_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode)*
  private static boolean deprecatedText_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!deprecatedText_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deprecatedText_1_1", c)) break;
    }
    return true;
  }

  // DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode
  private static boolean deprecatedText_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_1_1_0")) return false;
    boolean r;
    r = consumeToken(b, DEPRECATED_TEMPLATE_TEXT);
    if (!r) r = deprecatedTemplateInlineCode(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean docParameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docParameterDescription")) return false;
    Marker m = enter_section_(b, l, _NONE_, DOC_PARAMETER_DESCRIPTION, "<doc parameter description>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean documentationContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationContent")) return false;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_CONTENT, "<documentation content>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // definitionReferenceType singleBacktickedBlock
  public static boolean documentationDefinitionReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationDefinitionReference")) return false;
    if (!nextTokenIs(b, DEFINITION_REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = definitionReferenceType(b, l + 1);
    r = r && singleBacktickedBlock(b, l + 1);
    exit_section_(b, m, DOCUMENTATION_DEFINITION_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // MARKDOWN_DOCUMENTATION_LINE_START documentationContent
  public static boolean documentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationLine")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_LINE, null);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    p = r; // pin = 1
    r = r && documentationContent(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // documentationDefinitionReference
  public static boolean documentationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationReference")) return false;
    if (!nextTokenIs(b, DEFINITION_REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationDefinitionReference(b, l + 1);
    exit_section_(b, m, DOCUMENTATION_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // documentationLine+ parameterDocumentationLine* returnParameterDocumentationLine?
  public static boolean documentationString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationString_0(b, l + 1);
    r = r && documentationString_1(b, l + 1);
    r = r && documentationString_2(b, l + 1);
    exit_section_(b, m, DOCUMENTATION_STRING, r);
    return r;
  }

  // documentationLine+
  private static boolean documentationString_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationLine(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!documentationLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationString_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // parameterDocumentationLine*
  private static boolean documentationString_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDocumentationLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationString_1", c)) break;
    }
    return true;
  }

  // returnParameterDocumentationLine?
  private static boolean documentationString_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_2")) return false;
    returnParameterDocumentationLine(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (MARKDOWN_DOCUMENTATION_TEXT | REFERENCE_TYPE | DOCUMENTATION_ESCAPED_CHARACTERS | documentationReference | backtickedBlock | DEFINITION_REFERENCE)+
  public static boolean documentationText(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationText")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_TEXT, "<documentation text>");
    r = documentationText_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!documentationText_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationText", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // MARKDOWN_DOCUMENTATION_TEXT | REFERENCE_TYPE | DOCUMENTATION_ESCAPED_CHARACTERS | documentationReference | backtickedBlock | DEFINITION_REFERENCE
  private static boolean documentationText_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationText_0")) return false;
    boolean r;
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_TEXT);
    if (!r) r = consumeToken(b, REFERENCE_TYPE);
    if (!r) r = consumeToken(b, DOCUMENTATION_ESCAPED_CHARACTERS);
    if (!r) r = documentationReference(b, l + 1);
    if (!r) r = backtickedBlock(b, l + 1);
    if (!r) r = consumeToken(b, DEFINITION_REFERENCE);
    return r;
  }

  /* ********************************************************** */
  // DB_DEPRECATED_INLINE_CODE_START DOUBLE_BACK_TICK_INLINE_CODE? DOUBLE_BACK_TICK_INLINE_CODE_END
  public static boolean doubleBackTickDeprecatedInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBackTickDeprecatedInlineCode")) return false;
    if (!nextTokenIs(b, DB_DEPRECATED_INLINE_CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE, null);
    r = consumeToken(b, DB_DEPRECATED_INLINE_CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, doubleBackTickDeprecatedInlineCode_1(b, l + 1));
    r = p && consumeToken(b, DOUBLE_BACK_TICK_INLINE_CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // DOUBLE_BACK_TICK_INLINE_CODE?
  private static boolean doubleBackTickDeprecatedInlineCode_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBackTickDeprecatedInlineCode_1")) return false;
    consumeToken(b, DOUBLE_BACK_TICK_INLINE_CODE);
    return true;
  }

  /* ********************************************************** */
  // DOUBLE_BACKTICK_MARKDOWN_START DOUBLE_BACKTICK_CONTENT? DOUBLE_BACKTICK_MARKDOWN_END
  public static boolean doubleBacktickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBacktickedBlock")) return false;
    if (!nextTokenIs(b, DOUBLE_BACKTICK_MARKDOWN_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOUBLE_BACKTICKED_BLOCK, null);
    r = consumeToken(b, DOUBLE_BACKTICK_MARKDOWN_START);
    p = r; // pin = 1
    r = r && report_error_(b, doubleBacktickedBlock_1(b, l + 1));
    r = p && consumeToken(b, DOUBLE_BACKTICK_MARKDOWN_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // DOUBLE_BACKTICK_CONTENT?
  private static boolean doubleBacktickedBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBacktickedBlock_1")) return false;
    consumeToken(b, DOUBLE_BACKTICK_CONTENT);
    return true;
  }

  /* ********************************************************** */
  // Expression EQUAL_GT (LEFT_BRACE Block RIGHT_BRACE | Statement)
  public static boolean expressionPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionPattern")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_PATTERN, "<expression pattern>");
    r = Expression(b, l + 1, -1);
    r = r && consumeToken(b, EQUAL_GT);
    p = r; // pin = 2
    r = r && expressionPattern_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE | Statement
  private static boolean expressionPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expressionPattern_2_0(b, l + 1);
    if (!r) r = Statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean expressionPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<isPackageExpected>> PackageReference? AnyIdentifierName
  public static boolean functionNameReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNameReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_NAME_REFERENCE, "<function name reference>");
    r = isPackageExpected(b, l + 1);
    r = r && functionNameReference_1(b, l + 1);
    r = r && AnyIdentifierName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // PackageReference?
  private static boolean functionNameReference_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNameReference_1")) return false;
    PackageReference(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // new UserDefineTypeName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean initWithType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithType")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INIT_WITH_TYPE, null);
    r = consumeToken(b, NEW);
    r = r && UserDefineTypeName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 3
    r = r && report_error_(b, initWithType_3(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // InvocationArgList?
  private static boolean initWithType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithType_3")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // new (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  public static boolean initWithoutType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEW);
    r = r && initWithoutType_1(b, l + 1);
    exit_section_(b, m, INIT_WITHOUT_TYPE, r);
    return r;
  }

  // (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  private static boolean initWithoutType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType_1")) return false;
    initWithoutType_1_0(b, l + 1);
    return true;
  }

  // LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  private static boolean initWithoutType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && initWithoutType_1_0_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // InvocationArgList?
  private static boolean initWithoutType_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType_1_0_1")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // but (LEFT_BRACE MatchExpressionPatternClause (COMMA MatchExpressionPatternClause)* RIGHT_BRACE)
  public static boolean matchExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression")) return false;
    if (!nextTokenIs(b, BUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MATCH_EXPRESSION, null);
    r = consumeToken(b, BUT);
    p = r; // pin = 1
    r = r && matchExpression_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE MatchExpressionPatternClause (COMMA MatchExpressionPatternClause)* RIGHT_BRACE
  private static boolean matchExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, MatchExpressionPatternClause(b, l + 1));
    r = p && report_error_(b, matchExpression_1_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA MatchExpressionPatternClause)*
  private static boolean matchExpression_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression_1_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!matchExpression_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "matchExpression_1_2", c)) break;
    }
    return true;
  }

  // COMMA MatchExpressionPatternClause
  private static boolean matchExpression_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression_1_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && MatchExpressionPatternClause(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // namedPattern | unnamedPattern | expressionPattern | bindingPatternPattern
  public static boolean matchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATCH_PATTERN_CLAUSE, "<match pattern clause>");
    r = namedPattern(b, l + 1);
    if (!r) r = unnamedPattern(b, l + 1);
    if (!r) r = expressionPattern(b, l + 1);
    if (!r) r = bindingPatternPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // match Expression matchStatementBody
  public static boolean matchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchStatement")) return false;
    if (!nextTokenIs(b, MATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MATCH_STATEMENT, null);
    r = consumeToken(b, MATCH);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && matchStatementBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE matchPatternClause+ RIGHT_BRACE
  public static boolean matchStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchStatementBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MATCH_STATEMENT_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, matchStatementBody_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // matchPatternClause+
  private static boolean matchStatementBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchStatementBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = matchPatternClause(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!matchPatternClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "matchStatementBody_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TypeName identifier EQUAL_GT (LEFT_BRACE Block RIGHT_BRACE | Statement)
  public static boolean namedPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedPattern")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMED_PATTERN, "<named pattern>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 2, IDENTIFIER, EQUAL_GT);
    p = r; // pin = 3
    r = r && namedPattern_3(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE | Statement
  private static boolean namedPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedPattern_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namedPattern_3_0(b, l + 1);
    if (!r) r = Statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean namedPattern_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedPattern_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression RANGE Expression
  static boolean openRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "openRange")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = Expression(b, l + 1, -1);
    r = r && consumeToken(b, RANGE);
    p = r; // pin = 2
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean parameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDescription")) return false;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DESCRIPTION, "<parameter description>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // PARAMETER_NAME DESCRIPTION_SEPARATOR documentationText?
  public static boolean parameterDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentation")) return false;
    if (!nextTokenIs(b, PARAMETER_NAME)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DOCUMENTATION, null);
    r = consumeTokens(b, 2, PARAMETER_NAME, DESCRIPTION_SEPARATOR);
    p = r; // pin = 2
    r = r && parameterDocumentation_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationText?
  private static boolean parameterDocumentation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentation_2")) return false;
    documentationText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (PARAMETER_DOCUMENTATION_START parameterDocumentation) (MARKDOWN_DOCUMENTATION_LINE_START parameterDescription)*
  public static boolean parameterDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine")) return false;
    if (!nextTokenIs(b, PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameterDocumentationLine_0(b, l + 1);
    r = r && parameterDocumentationLine_1(b, l + 1);
    exit_section_(b, m, PARAMETER_DOCUMENTATION_LINE, r);
    return r;
  }

  // PARAMETER_DOCUMENTATION_START parameterDocumentation
  private static boolean parameterDocumentationLine_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PARAMETER_DOCUMENTATION_START);
    r = r && parameterDocumentation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (MARKDOWN_DOCUMENTATION_LINE_START parameterDescription)*
  private static boolean parameterDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDocumentationLine_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterDocumentationLine_1", c)) break;
    }
    return true;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START parameterDescription
  private static boolean parameterDocumentationLine_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && parameterDescription(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName
  public static boolean parameterTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TYPE_NAME, "<parameter type name>");
    r = parameterTypeName_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean parameterTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeName_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterTypeName_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // <<isNotARestParameter>> TypeName identifier
  public static boolean parameterWithType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterWithType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_WITH_TYPE, "<parameter with type>");
    r = isNotARestParameter(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // if Expression (LEFT_BRACE Block RIGHT_BRACE)
  static boolean pinnedElseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pinnedElseClause")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && pinnedElseClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean pinnedElseClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pinnedElseClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean returnParameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDescription")) return false;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETER_DESCRIPTION, "<return parameter description>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // docParameterDescription
  public static boolean returnParameterDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETER_DOCUMENTATION, "<return parameter documentation>");
    r = docParameterDescription(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (RETURN_PARAMETER_DOCUMENTATION_START returnParameterDocumentation) (MARKDOWN_DOCUMENTATION_LINE_START returnParameterDescription)*
  public static boolean returnParameterDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine")) return false;
    if (!nextTokenIs(b, RETURN_PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnParameterDocumentationLine_0(b, l + 1);
    r = r && returnParameterDocumentationLine_1(b, l + 1);
    exit_section_(b, m, RETURN_PARAMETER_DOCUMENTATION_LINE, r);
    return r;
  }

  // RETURN_PARAMETER_DOCUMENTATION_START returnParameterDocumentation
  private static boolean returnParameterDocumentationLine_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN_PARAMETER_DOCUMENTATION_START);
    r = r && returnParameterDocumentation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (MARKDOWN_DOCUMENTATION_LINE_START returnParameterDescription)*
  private static boolean returnParameterDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!returnParameterDocumentationLine_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "returnParameterDocumentationLine_1", c)) break;
    }
    return true;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START returnParameterDescription
  private static boolean returnParameterDocumentationLine_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && returnParameterDescription(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SB_DEPRECATED_INLINE_CODE_START SINGLE_BACK_TICK_INLINE_CODE? SINGLE_BACK_TICK_INLINE_CODE_END
  public static boolean singleBackTickDeprecatedInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBackTickDeprecatedInlineCode")) return false;
    if (!nextTokenIs(b, SB_DEPRECATED_INLINE_CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE, null);
    r = consumeToken(b, SB_DEPRECATED_INLINE_CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, singleBackTickDeprecatedInlineCode_1(b, l + 1));
    r = p && consumeToken(b, SINGLE_BACK_TICK_INLINE_CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SINGLE_BACK_TICK_INLINE_CODE?
  private static boolean singleBackTickDeprecatedInlineCode_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBackTickDeprecatedInlineCode_1")) return false;
    consumeToken(b, SINGLE_BACK_TICK_INLINE_CODE);
    return true;
  }

  /* ********************************************************** */
  // SINGLE_BACKTICK_MARKDOWN_START SINGLE_BACKTICK_CONTENT? SINGLE_BACKTICK_MARKDOWN_END
  public static boolean singleBacktickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBacktickedBlock")) return false;
    if (!nextTokenIs(b, SINGLE_BACKTICK_MARKDOWN_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SINGLE_BACKTICKED_BLOCK, null);
    r = consumeToken(b, SINGLE_BACKTICK_MARKDOWN_START);
    p = r; // pin = 1
    r = r && report_error_(b, singleBacktickedBlock_1(b, l + 1));
    r = p && consumeToken(b, SINGLE_BACKTICK_MARKDOWN_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SINGLE_BACKTICK_CONTENT?
  private static boolean singleBacktickedBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBacktickedBlock_1")) return false;
    consumeToken(b, SINGLE_BACKTICK_CONTENT);
    return true;
  }

  /* ********************************************************** */
  // TB_DEPRECATED_INLINE_CODE_START TRIPLE_BACK_TICK_INLINE_CODE? TRIPLE_BACK_TICK_INLINE_CODE_END
  public static boolean tripleBackTickDeprecatedInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBackTickDeprecatedInlineCode")) return false;
    if (!nextTokenIs(b, TB_DEPRECATED_INLINE_CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE, null);
    r = consumeToken(b, TB_DEPRECATED_INLINE_CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, tripleBackTickDeprecatedInlineCode_1(b, l + 1));
    r = p && consumeToken(b, TRIPLE_BACK_TICK_INLINE_CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TRIPLE_BACK_TICK_INLINE_CODE?
  private static boolean tripleBackTickDeprecatedInlineCode_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBackTickDeprecatedInlineCode_1")) return false;
    consumeToken(b, TRIPLE_BACK_TICK_INLINE_CODE);
    return true;
  }

  /* ********************************************************** */
  // TRIPLE_BACKTICK_MARKDOWN_START TRIPLE_BACKTICK_CONTENT? TRIPLE_BACKTICK_MARKDOWN_END
  public static boolean tripleBacktickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBacktickedBlock")) return false;
    if (!nextTokenIs(b, TRIPLE_BACKTICK_MARKDOWN_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRIPLE_BACKTICKED_BLOCK, null);
    r = consumeToken(b, TRIPLE_BACKTICK_MARKDOWN_START);
    p = r; // pin = 1
    r = r && report_error_(b, tripleBacktickedBlock_1(b, l + 1));
    r = p && consumeToken(b, TRIPLE_BACKTICK_MARKDOWN_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TRIPLE_BACKTICK_CONTENT?
  private static boolean tripleBacktickedBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBacktickedBlock_1")) return false;
    consumeToken(b, TRIPLE_BACKTICK_CONTENT);
    return true;
  }

  /* ********************************************************** */
  // TupleRefBindingPattern ASSIGN Expression SEMICOLON
  public static boolean tupleDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tupleDestructuringStatement")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TUPLE_DESTRUCTURING_STATEMENT, null);
    r = TupleRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TypeName EQUAL_GT (LEFT_BRACE Block RIGHT_BRACE | Statement)
  public static boolean unnamedPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedPattern")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNNAMED_PATTERN, "<unnamed pattern>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, EQUAL_GT);
    p = r; // pin = 2
    r = r && unnamedPattern_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE | Statement
  private static boolean unnamedPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unnamedPattern_2_0(b, l + 1);
    if (!r) r = Statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean unnamedPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unnamedPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (TypeName | var) BindingPattern ASSIGN Expression SEMICOLON
  public static boolean variableDefinitionStatementWithAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithAssignment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT_WITH_ASSIGNMENT, "<variable definition statement with assignment>");
    r = variableDefinitionStatementWithAssignment_0(b, l + 1);
    r = r && BindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 3
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TypeName | var
  private static boolean variableDefinitionStatementWithAssignment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithAssignment_0")) return false;
    boolean r;
    r = TypeName(b, l + 1, -1);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  /* ********************************************************** */
  // TypeName identifier SEMICOLON
  public static boolean variableDefinitionStatementWithoutAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithoutAssignment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT_WITHOUT_ASSIGNMENT, "<variable definition statement without assignment>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 1, IDENTIFIER, SEMICOLON);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Expression root: Expression
  // Operator priority table:
  // 0: ATOM(LambdaFunctionExpression)
  // 1: ATOM(ArrowFunctionExpression)
  // 2: ATOM(SimpleLiteralExpression)
  // 3: ATOM(StringTemplateLiteralExpression)
  // 4: ATOM(XmlLiteralExpression)
  // 5: ATOM(TableLiteralExpression)
  // 6: ATOM(RecordLiteralExpression)
  // 7: ATOM(BracedOrTupleExpression)
  // 8: BINARY(TernaryExpression)
  // 9: ATOM(ArrayLiteralExpression)
  // 10: ATOM(ValueTypeTypeExpression)
  // 11: ATOM(BuiltInReferenceTypeTypeExpression)
  // 12: ATOM(ActionInvocationExpression)
  // 13: ATOM(VariableReferenceExpression)
  // 14: ATOM(TypeInitExpression)
  // 15: ATOM(TypeConversionExpression)
  // 16: ATOM(UnaryExpression)
  // 17: BINARY(BinaryDivMulModExpression)
  // 18: BINARY(BinaryAddSubExpression)
  // 19: BINARY(BinaryCompareExpression)
  // 20: BINARY(BinaryEqualExpression)
  // 21: BINARY(BinaryAndExpression)
  // 22: BINARY(BinaryOrExpression)
  // 23: ATOM(TableQueryExpression)
  // 24: POSTFIX(MatchExprExpression)
  // 25: PREFIX(CheckedExpression)
  // 26: BINARY(ElvisExpression)
  // 27: PREFIX(AwaitExpression)
  // 28: BINARY(IntegerRangeExpression)
  // 29: BINARY(BitwiseExpression)
  // 30: BINARY(BitwiseShiftExpression)
  // 31: ATOM(ErrorConstructorExpression)
  // 32: ATOM(ServiceConstructorExpression)
  // 33: POSTFIX(TypeTestExpression)
  // 34: BINARY(BinaryRefEqualExpression)
  // 35: ATOM(TrapExpression)
  // 36: ATOM(TypeAccessExpression)
  public static boolean Expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = LambdaFunctionExpression(b, l + 1);
    if (!r) r = ArrowFunctionExpression(b, l + 1);
    if (!r) r = SimpleLiteralExpression(b, l + 1);
    if (!r) r = StringTemplateLiteralExpression(b, l + 1);
    if (!r) r = XmlLiteralExpression(b, l + 1);
    if (!r) r = TableLiteralExpression(b, l + 1);
    if (!r) r = RecordLiteralExpression(b, l + 1);
    if (!r) r = BracedOrTupleExpression(b, l + 1);
    if (!r) r = ArrayLiteralExpression(b, l + 1);
    if (!r) r = ValueTypeTypeExpression(b, l + 1);
    if (!r) r = BuiltInReferenceTypeTypeExpression(b, l + 1);
    if (!r) r = ActionInvocationExpression(b, l + 1);
    if (!r) r = VariableReferenceExpression(b, l + 1);
    if (!r) r = TypeInitExpression(b, l + 1);
    if (!r) r = TypeConversionExpression(b, l + 1);
    if (!r) r = UnaryExpression(b, l + 1);
    if (!r) r = TableQueryExpression(b, l + 1);
    if (!r) r = CheckedExpression(b, l + 1);
    if (!r) r = AwaitExpression(b, l + 1);
    if (!r) r = ErrorConstructorExpression(b, l + 1);
    if (!r) r = ServiceConstructorExpression(b, l + 1);
    if (!r) r = TrapExpression(b, l + 1);
    if (!r) r = TypeAccessExpression(b, l + 1);
    p = r;
    r = r && Expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean Expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 8 && consumeTokenSmart(b, QUESTION_MARK)) {
        r = report_error_(b, Expression(b, l, 8));
        r = TernaryExpression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_EXPRESSION, r, true, null);
      }
      else if (g < 17 && BinaryDivMulModExpression_0(b, l + 1)) {
        r = Expression(b, l, 17);
        exit_section_(b, l, m, BINARY_DIV_MUL_MOD_EXPRESSION, r, true, null);
      }
      else if (g < 18 && BinaryAddSubExpression_0(b, l + 1)) {
        r = Expression(b, l, 18);
        exit_section_(b, l, m, BINARY_ADD_SUB_EXPRESSION, r, true, null);
      }
      else if (g < 19 && BinaryCompareExpression_0(b, l + 1)) {
        r = Expression(b, l, 19);
        exit_section_(b, l, m, BINARY_COMPARE_EXPRESSION, r, true, null);
      }
      else if (g < 20 && BinaryEqualExpression_0(b, l + 1)) {
        r = Expression(b, l, 20);
        exit_section_(b, l, m, BINARY_EQUAL_EXPRESSION, r, true, null);
      }
      else if (g < 21 && BinaryAndExpression_0(b, l + 1)) {
        r = Expression(b, l, 21);
        exit_section_(b, l, m, BINARY_AND_EXPRESSION, r, true, null);
      }
      else if (g < 22 && consumeTokenSmart(b, OR)) {
        r = Expression(b, l, 22);
        exit_section_(b, l, m, BINARY_OR_EXPRESSION, r, true, null);
      }
      else if (g < 24 && matchExpression(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, MATCH_EXPR_EXPRESSION, r, true, null);
      }
      else if (g < 26 && consumeTokenSmart(b, ELVIS)) {
        r = Expression(b, l, 26);
        exit_section_(b, l, m, ELVIS_EXPRESSION, r, true, null);
      }
      else if (g < 28 && IntegerRangeExpression_0(b, l + 1)) {
        r = Expression(b, l, 28);
        exit_section_(b, l, m, INTEGER_RANGE_EXPRESSION, r, true, null);
      }
      else if (g < 29 && BitwiseExpression_0(b, l + 1)) {
        r = Expression(b, l, 29);
        exit_section_(b, l, m, BITWISE_EXPRESSION, r, true, null);
      }
      else if (g < 30 && BitwiseShiftExpression_0(b, l + 1)) {
        r = Expression(b, l, 30);
        exit_section_(b, l, m, BITWISE_SHIFT_EXPRESSION, r, true, null);
      }
      else if (g < 33 && TypeTestExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TYPE_TEST_EXPRESSION, r, true, null);
      }
      else if (g < 34 && BinaryRefEqualExpression_0(b, l + 1)) {
        r = Expression(b, l, 34);
        exit_section_(b, l, m, BINARY_REF_EQUAL_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // LambdaFunction
  public static boolean LambdaFunctionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunctionExpression")) return false;
    if (!nextTokenIsSmart(b, FUNCTION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = LambdaFunction(b, l + 1);
    exit_section_(b, m, LAMBDA_FUNCTION_EXPRESSION, r);
    return r;
  }

  // ArrowFunction
  public static boolean ArrowFunctionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionExpression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARROW_FUNCTION_EXPRESSION, "<arrow function expression>");
    r = ArrowFunction(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SimpleLiteral
  public static boolean SimpleLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteralExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_LITERAL_EXPRESSION, "<simple literal expression>");
    r = SimpleLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // StringTemplateLiteral
  public static boolean StringTemplateLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, STRING_TEMPLATE_LITERAL_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StringTemplateLiteral(b, l + 1);
    exit_section_(b, m, STRING_TEMPLATE_LITERAL_EXPRESSION, r);
    return r;
  }

  // XmlLiteral
  public static boolean XmlLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, XML_LITERAL_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = XmlLiteral(b, l + 1);
    exit_section_(b, m, XML_LITERAL_EXPRESSION, r);
    return r;
  }

  // TableLiteral
  public static boolean TableLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, TABLE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableLiteral(b, l + 1);
    exit_section_(b, m, TABLE_LITERAL_EXPRESSION, r);
    return r;
  }

  // RecordLiteral
  public static boolean RecordLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RecordLiteral(b, l + 1);
    exit_section_(b, m, RECORD_LITERAL_EXPRESSION, r);
    return r;
  }

  // LEFT_PARENTHESIS Expression (COMMA Expression)* RIGHT_PARENTHESIS
  public static boolean BracedOrTupleExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BracedOrTupleExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    r = r && Expression(b, l + 1, -1);
    r = r && BracedOrTupleExpression_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, BRACED_OR_TUPLE_EXPRESSION, r);
    return r;
  }

  // (COMMA Expression)*
  private static boolean BracedOrTupleExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BracedOrTupleExpression_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!BracedOrTupleExpression_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BracedOrTupleExpression_2", c)) break;
    }
    return true;
  }

  // COMMA Expression
  private static boolean BracedOrTupleExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BracedOrTupleExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COLON Expression
  private static boolean TernaryExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TernaryExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ArrayLiteral
  public static boolean ArrayLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrayLiteral(b, l + 1);
    exit_section_(b, m, ARRAY_LITERAL_EXPRESSION, r);
    return r;
  }

  // ValueTypeName DOT identifier
  public static boolean ValueTypeTypeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ValueTypeTypeExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE_TYPE_TYPE_EXPRESSION, "<value type type expression>");
    r = ValueTypeName(b, l + 1);
    r = r && consumeTokensSmart(b, 0, DOT, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BuiltInReferenceTypeName DOT identifier
  public static boolean BuiltInReferenceTypeTypeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BuiltInReferenceTypeTypeExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BUILT_IN_REFERENCE_TYPE_TYPE_EXPRESSION, "<built in reference type type expression>");
    r = BuiltInReferenceTypeName(b, l + 1);
    r = r && consumeTokensSmart(b, 0, DOT, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ActionInvocation
  public static boolean ActionInvocationExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocationExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACTION_INVOCATION_EXPRESSION, "<action invocation expression>");
    r = ActionInvocation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // start? VariableReference
  public static boolean VariableReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_REFERENCE_EXPRESSION, "<variable reference expression>");
    r = VariableReferenceExpression_0(b, l + 1);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // start?
  private static boolean VariableReferenceExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceExpression_0")) return false;
    consumeTokenSmart(b, START);
    return true;
  }

  // initWithoutType | initWithType
  public static boolean TypeInitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpression")) return false;
    if (!nextTokenIsSmart(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = initWithoutType(b, l + 1);
    if (!r) r = initWithType(b, l + 1);
    exit_section_(b, m, TYPE_INIT_EXPRESSION, r);
    return r;
  }

  // LT TypeName (COMMA FunctionInvocation)? GT Expression
  public static boolean TypeConversionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression")) return false;
    if (!nextTokenIsSmart(b, LT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_CONVERSION_EXPRESSION, null);
    r = consumeTokenSmart(b, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, TypeConversionExpression_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, GT)) && r;
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA FunctionInvocation)?
  private static boolean TypeConversionExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression_2")) return false;
    TypeConversionExpression_2_0(b, l + 1);
    return true;
  }

  // COMMA FunctionInvocation
  private static boolean TypeConversionExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && FunctionInvocation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ADD | SUB | BIT_COMPLEMENT | NOT | lengthof | untaint) Expression
  public static boolean UnaryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnaryExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _COLLAPSE_, UNARY_EXPRESSION, "<unary expression>");
    r = UnaryExpression_0(b, l + 1);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ADD | SUB | BIT_COMPLEMENT | NOT | lengthof | untaint
  private static boolean UnaryExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnaryExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    if (!r) r = consumeTokenSmart(b, BIT_COMPLEMENT);
    if (!r) r = consumeTokenSmart(b, NOT);
    if (!r) r = consumeTokenSmart(b, LENGTHOF);
    if (!r) r = consumeTokenSmart(b, UNTAINT);
    return r;
  }

  // DIV | MUL | MOD
  private static boolean BinaryDivMulModExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryDivMulModExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, MUL);
    if (!r) r = consumeTokenSmart(b, MOD);
    return r;
  }

  // ADD | SUB
  private static boolean BinaryAddSubExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryAddSubExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    return r;
  }

  // LT_EQUAL | GT_EQUAL | GT | LT
  private static boolean BinaryCompareExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryCompareExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, LT_EQUAL);
    if (!r) r = consumeTokenSmart(b, GT_EQUAL);
    if (!r) r = consumeTokenSmart(b, GT);
    if (!r) r = consumeTokenSmart(b, LT);
    return r;
  }

  // EQUAL | NOT_EQUAL
  private static boolean BinaryEqualExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryEqualExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, EQUAL);
    if (!r) r = consumeTokenSmart(b, NOT_EQUAL);
    return r;
  }

  // <<isNotInStreams>> AND
  private static boolean BinaryAndExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryAndExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isNotInStreams(b, l + 1);
    r = r && consumeToken(b, AND);
    exit_section_(b, m, null, r);
    return r;
  }

  // TableQuery
  public static boolean TableQueryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQueryExpression")) return false;
    if (!nextTokenIsSmart(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableQuery(b, l + 1);
    exit_section_(b, m, TABLE_QUERY_EXPRESSION, r);
    return r;
  }

  public static boolean CheckedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CheckedExpression")) return false;
    if (!nextTokenIsSmart(b, CHECK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, CHECK);
    p = r;
    r = p && Expression(b, l, 25);
    exit_section_(b, l, m, CHECKED_EXPRESSION, r, p, null);
    return r || p;
  }

  public static boolean AwaitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AwaitExpression")) return false;
    if (!nextTokenIsSmart(b, AWAIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, AWAIT);
    p = r;
    r = p && Expression(b, l, 27);
    exit_section_(b, l, m, AWAIT_EXPRESSION, r, p, null);
    return r || p;
  }

  // ELLIPSIS | HALF_OPEN_RANGE
  private static boolean IntegerRangeExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntegerRangeExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ELLIPSIS);
    if (!r) r = consumeTokenSmart(b, HALF_OPEN_RANGE);
    return r;
  }

  // BITAND | PIPE | BITXOR
  private static boolean BitwiseExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BitwiseExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, BITAND);
    if (!r) r = consumeTokenSmart(b, PIPE);
    if (!r) r = consumeTokenSmart(b, BITXOR);
    return r;
  }

  // (ShiftExpression)
  private static boolean BitwiseShiftExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BitwiseShiftExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ShiftExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // error LEFT_PARENTHESIS Expression (COMMA Expression)? RIGHT_PARENTHESIS
  public static boolean ErrorConstructorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorConstructorExpression")) return false;
    if (!nextTokenIsSmart(b, ERROR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ERROR_CONSTRUCTOR_EXPRESSION, null);
    r = consumeTokensSmart(b, 1, ERROR, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && report_error_(b, ErrorConstructorExpression_3(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Expression)?
  private static boolean ErrorConstructorExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorConstructorExpression_3")) return false;
    ErrorConstructorExpression_3_0(b, l + 1);
    return true;
  }

  // COMMA Expression
  private static boolean ErrorConstructorExpression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorConstructorExpression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment* service ServiceBody
  public static boolean ServiceConstructorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceConstructorExpression")) return false;
    if (!nextTokenIsSmart(b, AT, SERVICE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_CONSTRUCTOR_EXPRESSION, "<service constructor expression>");
    r = ServiceConstructorExpression_0(b, l + 1);
    r = r && consumeToken(b, SERVICE);
    p = r; // pin = 2
    r = r && ServiceBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ServiceConstructorExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceConstructorExpression_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceConstructorExpression_0", c)) break;
    }
    return true;
  }

  // is TypeName
  private static boolean TypeTestExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeTestExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IS);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // REF_EQUAL | REF_NOT_EQUAL
  private static boolean BinaryRefEqualExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryRefEqualExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, REF_EQUAL);
    if (!r) r = consumeTokenSmart(b, REF_NOT_EQUAL);
    return r;
  }

  // trap Expression
  public static boolean TrapExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TrapExpression")) return false;
    if (!nextTokenIsSmart(b, TRAP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRAP_EXPRESSION, null);
    r = consumeTokenSmart(b, TRAP);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TypeName
  public static boolean TypeAccessExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeAccessExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_ACCESS_EXPRESSION, "<type access expression>");
    r = TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Expression root: TypeName
  // Operator priority table:
  // 0: ATOM(TupleTypeName)
  // 1: ATOM(SimpleTypeName)
  // 2: PREFIX(GroupTypeName)
  // 3: POSTFIX(ArrayTypeName)
  // 4: N_ARY(UnionTypeName)
  // 5: ATOM(ObjectTypeName)
  // 6: POSTFIX(NullableTypeName)
  // 7: ATOM(RecordTypeName)
  public static boolean TypeName(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "TypeName")) return false;
    addVariant(b, "<type name>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<type name>");
    r = TupleTypeName(b, l + 1);
    if (!r) r = SimpleTypeName(b, l + 1);
    if (!r) r = GroupTypeName(b, l + 1);
    if (!r) r = ObjectTypeName(b, l + 1);
    if (!r) r = RecordTypeName(b, l + 1);
    p = r;
    r = r && TypeName_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean TypeName_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "TypeName_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 3 && ArrayTypeName_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, ARRAY_TYPE_NAME, r, true, null);
      }
      else if (g < 4 && consumeTokenSmart(b, PIPE)) {
        while (true) {
          r = report_error_(b, TypeName(b, l, 4));
          if (!consumeTokenSmart(b, PIPE)) break;
        }
        exit_section_(b, l, m, UNION_TYPE_NAME, r, true, null);
      }
      else if (g < 6 && consumeTokenSmart(b, QUESTION_MARK)) {
        r = true;
        exit_section_(b, l, m, NULLABLE_TYPE_NAME, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // LEFT_PARENTHESIS TypeName (COMMA TypeName)* RIGHT_PARENTHESIS
  public static boolean TupleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    r = r && TypeName(b, l + 1, -1);
    r = r && TupleTypeName_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, TUPLE_TYPE_NAME, r);
    return r;
  }

  // (COMMA TypeName)*
  private static boolean TupleTypeName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TupleTypeName_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleTypeName_2", c)) break;
    }
    return true;
  }

  // COMMA TypeName
  private static boolean TupleTypeName_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NULL_LITERAL
  //                    | AnyTypeName
  //                    | AnyDataTypeName
  //                    | TypeDescTypeName
  //                    | ValueTypeName
  //                    | ReferenceTypeName
  //                    | EmptyTupleLiteral
  public static boolean SimpleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_TYPE_NAME, "<simple type name>");
    r = consumeTokenSmart(b, NULL_LITERAL);
    if (!r) r = AnyTypeName(b, l + 1);
    if (!r) r = AnyDataTypeName(b, l + 1);
    if (!r) r = TypeDescTypeName(b, l + 1);
    if (!r) r = ValueTypeName(b, l + 1);
    if (!r) r = ReferenceTypeName(b, l + 1);
    if (!r) r = EmptyTupleLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  public static boolean GroupTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupTypeName")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = GroupTypeName_0(b, l + 1);
    p = r;
    r = p && TypeName(b, l, 2);
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    exit_section_(b, l, m, GROUP_TYPE_NAME, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS <<isGroupType>>
  private static boolean GroupTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupTypeName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    r = r && isGroupType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_BRACKET (IntegerLiteral | SealedLiteral)? RIGHT_BRACKET)+
  private static boolean ArrayTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrayTypeName_0_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ArrayTypeName_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrayTypeName_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACKET (IntegerLiteral | SealedLiteral)? RIGHT_BRACKET
  private static boolean ArrayTypeName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LEFT_BRACKET);
    r = r && ArrayTypeName_0_0_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // (IntegerLiteral | SealedLiteral)?
  private static boolean ArrayTypeName_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0_1")) return false;
    ArrayTypeName_0_0_1_0(b, l + 1);
    return true;
  }

  // IntegerLiteral | SealedLiteral
  private static boolean ArrayTypeName_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0_1_0")) return false;
    boolean r;
    r = IntegerLiteral(b, l + 1);
    if (!r) r = SealedLiteral(b, l + 1);
    return r;
  }

  // abstract? client? object LEFT_BRACE ObjectBody RIGHT_BRACE
  public static boolean ObjectTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_TYPE_NAME, "<object type name>");
    r = ObjectTypeName_0(b, l + 1);
    r = r && ObjectTypeName_1(b, l + 1);
    r = r && consumeTokensSmart(b, 1, OBJECT, LEFT_BRACE);
    p = r; // pin = 3
    r = r && report_error_(b, ObjectBody(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // abstract?
  private static boolean ObjectTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0")) return false;
    consumeTokenSmart(b, ABSTRACT);
    return true;
  }

  // client?
  private static boolean ObjectTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1")) return false;
    consumeTokenSmart(b, CLIENT);
    return true;
  }

  // record? LEFT_BRACE RecordFieldDefinitionList RIGHT_BRACE
  public static boolean RecordTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordTypeName")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACE, RECORD)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_TYPE_NAME, "<record type name>");
    r = RecordTypeName_0(b, l + 1);
    r = r && consumeToken(b, LEFT_BRACE);
    r = r && RecordFieldDefinitionList(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // record?
  private static boolean RecordTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordTypeName_0")) return false;
    consumeTokenSmart(b, RECORD);
    return true;
  }

  /* ********************************************************** */
  // Expression root: VariableReference
  // Operator priority table:
  // 0: POSTFIX(MapArrayVariableReference)
  // 1: POSTFIX(InvocationReference)
  // 2: POSTFIX(FieldVariableReference)
  // 3: POSTFIX(XmlAttribVariableReference)
  // 4: ATOM(FunctionInvocationReference)
  // 5: ATOM(SimpleVariableReference)
  public static boolean VariableReference(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "VariableReference")) return false;
    addVariant(b, "<variable reference>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<variable reference>");
    r = FunctionInvocationReference(b, l + 1);
    if (!r) r = SimpleVariableReference(b, l + 1);
    p = r;
    r = r && VariableReference_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean VariableReference_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "VariableReference_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 0 && Index(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, MAP_ARRAY_VARIABLE_REFERENCE, r, true, null);
      }
      else if (g < 1 && Invocation(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INVOCATION_REFERENCE, r, true, null);
      }
      else if (g < 2 && Field(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, FIELD_VARIABLE_REFERENCE, r, true, null);
      }
      else if (g < 3 && XmlAttrib(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, XML_ATTRIB_VARIABLE_REFERENCE, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // FunctionInvocation
  public static boolean FunctionInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocationReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_INVOCATION_REFERENCE, "<function invocation reference>");
    r = FunctionInvocation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NameReference
  public static boolean SimpleVariableReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleVariableReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_VARIABLE_REFERENCE, "<simple variable reference>");
    r = NameReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  final static Parser JoinConditionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return JoinConditionRecover(b, l + 1);
    }
  };
  final static Parser StatementRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return StatementRecover(b, l + 1);
    }
  };
  final static Parser TopLevelDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return TopLevelDefinitionRecover(b, l + 1);
    }
  };
  final static Parser WorkerDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return WorkerDefinitionRecover(b, l + 1);
    }
  };
}
