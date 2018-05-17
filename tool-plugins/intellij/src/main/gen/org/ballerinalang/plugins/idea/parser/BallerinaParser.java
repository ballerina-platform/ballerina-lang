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
package org.ballerinalang.plugins.idea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.ballerinalang.plugins.idea.psi.BallerinaTypes.*;
import static org.ballerinalang.plugins.idea.parser.BallerinaParserUtil.*;
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
    else if (t == AGGREGATION_QUERY) {
      r = AggregationQuery(b, 0);
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
    else if (t == ANY_IDENTIFIER_NAME) {
      r = AnyIdentifierName(b, 0);
    }
    else if (t == ANY_TYPE_NAME) {
      r = AnyTypeName(b, 0);
    }
    else if (t == ARRAY_LITERAL) {
      r = ArrayLiteral(b, 0);
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
    else if (t == CLOSE_TAG) {
      r = CloseTag(b, 0);
    }
    else if (t == COMMENT) {
      r = Comment(b, 0);
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
    else if (t == CONTENT) {
      r = Content(b, 0);
    }
    else if (t == DEFAULTABLE_PARAMETER) {
      r = DefaultableParameter(b, 0);
    }
    else if (t == DEFINITION) {
      r = Definition(b, 0);
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
    else if (t == ENDPOINT_DEFINITION) {
      r = EndpointDefinition(b, 0);
    }
    else if (t == ENDPOINT_INITIALIZATION) {
      r = EndpointInitialization(b, 0);
    }
    else if (t == ENDPOINT_TYPE) {
      r = EndpointType(b, 0);
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
    else if (t == FIELD_DEFINITION_LIST) {
      r = FieldDefinitionList(b, 0);
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
    else if (t == GLOBAL_ENDPOINT_DEFINITION) {
      r = GlobalEndpointDefinition(b, 0);
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
    else if (t == NEXT_STATEMENT) {
      r = NextStatement(b, 0);
    }
    else if (t == OBJECT_BODY) {
      r = ObjectBody(b, 0);
    }
    else if (t == OBJECT_CALLABLE_UNIT_SIGNATURE) {
      r = ObjectCallableUnitSignature(b, 0);
    }
    else if (t == OBJECT_DEFAULTABLE_PARAMETER) {
      r = ObjectDefaultableParameter(b, 0);
    }
    else if (t == OBJECT_FUNCTION_DEFINITION) {
      r = ObjectFunctionDefinition(b, 0);
    }
    else if (t == OBJECT_FUNCTIONS) {
      r = ObjectFunctions(b, 0);
    }
    else if (t == OBJECT_INITIALIZER) {
      r = ObjectInitializer(b, 0);
    }
    else if (t == OBJECT_INITIALIZER_PARAMETER_LIST) {
      r = ObjectInitializerParameterList(b, 0);
    }
    else if (t == OBJECT_PARAMETER) {
      r = ObjectParameter(b, 0);
    }
    else if (t == OBJECT_PARAMETER_LIST) {
      r = ObjectParameterList(b, 0);
    }
    else if (t == ONABORT_STATEMENT) {
      r = OnabortStatement(b, 0);
    }
    else if (t == ONCOMMIT_STATEMENT) {
      r = OncommitStatement(b, 0);
    }
    else if (t == ONRETRY_CLAUSE) {
      r = OnretryClause(b, 0);
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
    else if (t == POST_ARITHMETIC_OPERATOR) {
      r = PostArithmeticOperator(b, 0);
    }
    else if (t == POST_INCREMENT_STATEMENT) {
      r = PostIncrementStatement(b, 0);
    }
    else if (t == PRIVATE_OBJECT_FIELDS) {
      r = PrivateObjectFields(b, 0);
    }
    else if (t == PROC_INS) {
      r = ProcIns(b, 0);
    }
    else if (t == PUBLIC_OBJECT_FIELDS) {
      r = PublicObjectFields(b, 0);
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
    else if (t == REFERENCE_TYPE_NAME) {
      r = ReferenceTypeName(b, 0);
    }
    else if (t == RESERVED_WORD) {
      r = ReservedWord(b, 0);
    }
    else if (t == RESOURCE_DEFINITION) {
      r = ResourceDefinition(b, 0);
    }
    else if (t == REST_ARGS) {
      r = RestArgs(b, 0);
    }
    else if (t == REST_PARAMETER) {
      r = RestParameter(b, 0);
    }
    else if (t == RETRIES_STATEMENT) {
      r = RetriesStatement(b, 0);
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
    else if (t == SERVICE_DEFINITION) {
      r = ServiceDefinition(b, 0);
    }
    else if (t == SERVICE_ENDPOINT_ATTACHMENTS) {
      r = ServiceEndpointAttachments(b, 0);
    }
    else if (t == SET_ASSIGNMENT_CLAUSE) {
      r = SetAssignmentClause(b, 0);
    }
    else if (t == SET_CLAUSE) {
      r = SetClause(b, 0);
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
    else if (t == TABLE_INITIALIZATION) {
      r = TableInitialization(b, 0);
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
    else if (t == TYPE_DEFINITION) {
      r = TypeDefinition(b, 0);
    }
    else if (t == TYPE_DESC_TYPE_NAME) {
      r = TypeDescTypeName(b, 0);
    }
    else if (t == TYPE_INIT_EXPR) {
      r = TypeInitExpr(b, 0);
    }
    else if (t == TYPE_NAME) {
      r = TypeName(b, 0, -1);
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
    else if (t == DEPRECATED_ATTACHMENT) {
      r = deprecatedAttachment(b, 0);
    }
    else if (t == DEPRECATED_TEMPLATE_INLINE_CODE) {
      r = deprecatedTemplateInlineCode(b, 0);
    }
    else if (t == DEPRECATED_TEXT) {
      r = deprecatedText(b, 0);
    }
    else if (t == DOC_TEXT) {
      r = docText(b, 0);
    }
    else if (t == DOCUMENTATION_ATTACHMENT) {
      r = documentationAttachment(b, 0);
    }
    else if (t == DOCUMENTATION_TEMPLATE_ATTRIBUTE_DESCRIPTION) {
      r = documentationTemplateAttributeDescription(b, 0);
    }
    else if (t == DOCUMENTATION_TEMPLATE_CONTENT) {
      r = documentationTemplateContent(b, 0);
    }
    else if (t == DOCUMENTATION_TEMPLATE_INLINE_CODE) {
      r = documentationTemplateInlineCode(b, 0);
    }
    else if (t == DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
      r = doubleBackTickDeprecatedInlineCode(b, 0);
    }
    else if (t == DOUBLE_BACK_TICK_DOC_INLINE_CODE) {
      r = doubleBackTickDocInlineCode(b, 0);
    }
    else if (t == ENDPOINT_PARAMETER) {
      r = endpointParameter(b, 0);
    }
    else if (t == FIELD_DEFINITION) {
      r = fieldDefinition(b, 0);
    }
    else if (t == FUNCTION_NAME_REFERENCE) {
      r = functionNameReference(b, 0);
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
    else if (t == PARAMETER_TYPE_NAME) {
      r = parameterTypeName(b, 0);
    }
    else if (t == PARAMETER_WITH_TYPE) {
      r = parameterWithType(b, 0);
    }
    else if (t == RESOURCE_PARAMETER_LIST) {
      r = resourceParameterList(b, 0);
    }
    else if (t == RETRY_STATEMENT) {
      r = retryStatement(b, 0);
    }
    else if (t == SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
      r = singleBackTickDeprecatedInlineCode(b, 0);
    }
    else if (t == SINGLE_BACK_TICK_DOC_INLINE_CODE) {
      r = singleBackTickDocInlineCode(b, 0);
    }
    else if (t == TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
      r = tripleBackTickDeprecatedInlineCode(b, 0);
    }
    else if (t == TRIPLE_BACK_TICK_DOC_INLINE_CODE) {
      r = tripleBackTickDocInlineCode(b, 0);
    }
    else if (t == TUPLE_DESTRUCTURING_STATEMENT) {
      r = tupleDestructuringStatement(b, 0);
    }
    else if (t == UNNAMED_PATTERN) {
      r = unnamedPattern(b, 0);
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
    create_token_set_(ACTION_INVOCATION_EXPRESSION, ARRAY_LITERAL_EXPRESSION, AWAIT_EXPRESSION, BINARY_ADD_SUB_EXPRESSION,
      BINARY_AND_EXPRESSION, BINARY_COMPARE_EXPRESSION, BINARY_DIV_MUL_MOD_EXPRESSION, BINARY_EQUAL_EXPRESSION,
      BINARY_OR_EXPRESSION, BINARY_POW_EXPRESSION, BRACED_OR_TUPLE_EXPRESSION, BUILT_IN_REFERENCE_TYPE_TYPE_EXPRESSION,
      CHECKED_EXPRESSION, ELVIS_EXPRESSION, EXPRESSION, LAMBDA_FUNCTION_EXPRESSION,
      MATCH_EXPR_EXPRESSION, RECORD_LITERAL_EXPRESSION, SIMPLE_LITERAL_EXPRESSION, STRING_TEMPLATE_LITERAL_EXPRESSION,
      TABLE_LITERAL_EXPRESSION, TABLE_QUERY_EXPRESSION, TERNARY_EXPRESSION, TYPE_ACCESS_EXPRESSION,
      TYPE_CONVERSION_EXPRESSION, TYPE_INIT_EXPRESSION, UNARY_EXPRESSION, VALUE_TYPE_TYPE_EXPRESSION,
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
  // start? NameReference RARROW FunctionInvocation {/*pin=3 recoverWhile=StatementRecover*/}
  public static boolean ActionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACTION_INVOCATION, "<action invocation>");
    r = ActionInvocation_0(b, l + 1);
    r = r && NameReference(b, l + 1);
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
  // from StreamingInput SelectClause? OrderByClause?
  public static boolean AggregationQuery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AggregationQuery")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FROM);
    r = r && StreamingInput(b, l + 1);
    r = r && AggregationQuery_2(b, l + 1);
    r = r && AggregationQuery_3(b, l + 1);
    exit_section_(b, m, AGGREGATION_QUERY, r);
    return r;
  }

  // SelectClause?
  private static boolean AggregationQuery_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AggregationQuery_2")) return false;
    SelectClause(b, l + 1);
    return true;
  }

  // OrderByClause?
  private static boolean AggregationQuery_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AggregationQuery_3")) return false;
    OrderByClause(b, l + 1);
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationDefinition_2_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "AnnotationDefinition_2_0_2", c)) break;
      c = current_position_(b);
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
  // withVar | withoutVar
  public static boolean AssignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssignmentStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    r = withVar(b, l + 1);
    if (!r) r = withoutVar(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
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
  // service | resource | function | object | type | endpoint | TYPE_PARAMETER | annotation {
  //     /*recoverWhile=AttachmentPointRecover*/
  // }
  public static boolean AttachmentPoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachmentPoint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTACHMENT_POINT, "<attachment point>");
    r = consumeToken(b, SERVICE);
    if (!r) r = consumeToken(b, RESOURCE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, ENDPOINT);
    if (!r) r = consumeToken(b, TYPE_PARAMETER);
    if (!r) r = AttachmentPoint_7(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation {
  //     /*recoverWhile=AttachmentPointRecover*/
  // }
  private static boolean AttachmentPoint_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachmentPoint_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANNOTATION);
    r = r && AttachmentPoint_7_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  //     /*recoverWhile=AttachmentPointRecover*/
  // }
  private static boolean AttachmentPoint_7_1(PsiBuilder b, int l) {
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
  // Statement*
  public static boolean Block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Block")) return false;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    int c = current_position_(b);
    while (true) {
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Block", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // EndpointDefinition* Statement*
  public static boolean BlockWithEndpoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockWithEndpoint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block with endpoint>");
    r = BlockWithEndpoint_0(b, l + 1);
    r = r && BlockWithEndpoint_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EndpointDefinition*
  private static boolean BlockWithEndpoint_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockWithEndpoint_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!EndpointDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockWithEndpoint_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // Statement*
  private static boolean BlockWithEndpoint_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockWithEndpoint_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockWithEndpoint_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // EndpointDefinition* WorkerDefinition+
  public static boolean BlockWithEndpointAndWorker(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockWithEndpointAndWorker")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block with endpoint and worker>");
    r = BlockWithEndpointAndWorker_0(b, l + 1);
    r = r && BlockWithEndpointAndWorker_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EndpointDefinition*
  private static boolean BlockWithEndpointAndWorker_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockWithEndpointAndWorker_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!EndpointDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockWithEndpointAndWorker_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // WorkerDefinition+
  private static boolean BlockWithEndpointAndWorker_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockWithEndpointAndWorker_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WorkerDefinition(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!WorkerDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockWithEndpointAndWorker_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
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
    if (!r) r = FunctionTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE (RIGHT_BRACE | BlockWithEndpoint RIGHT_BRACE | BlockWithEndpointAndWorker RIGHT_BRACE)
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

  // RIGHT_BRACE | BlockWithEndpoint RIGHT_BRACE | BlockWithEndpointAndWorker RIGHT_BRACE
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

  // BlockWithEndpoint RIGHT_BRACE
  private static boolean CallableUnitBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = BlockWithEndpoint(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // BlockWithEndpointAndWorker RIGHT_BRACE
  private static boolean CallableUnitBody_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = BlockWithEndpointAndWorker(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
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
  // catch LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS LEFT_BRACE Block RIGHT_BRACE
  public static boolean CatchClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause")) return false;
    if (!nextTokenIs(b, CATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CLAUSE, null);
    r = consumeTokens(b, 1, CATCH, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, RIGHT_PARENTHESIS, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
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
    int c = current_position_(b);
    while (r) {
      if (!CatchClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CatchClauses_0_0", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!Comment_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Comment_1", c)) break;
      c = current_position_(b);
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
  // (ImportDeclaration | NamespaceDeclaration)* Definition* <<eof>>
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
    int c = current_position_(b);
    while (true) {
      if (!CompilationUnit_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompilationUnit_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ImportDeclaration | NamespaceDeclaration
  private static boolean CompilationUnit_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ImportDeclaration(b, l + 1);
    if (!r) r = NamespaceDeclaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Definition*
  private static boolean CompilationUnit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!Definition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompilationUnit_1", c)) break;
      c = current_position_(b);
    }
    return true;
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
    int c = current_position_(b);
    while (true) {
      if (!CompletePackageName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompletePackageName_1", c)) break;
      c = current_position_(b);
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
  // COMPOUND_ADD | COMPOUND_SUB | COMPOUND_MUL | COMPOUND_DIV
  public static boolean CompoundOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompoundOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_OPERATOR, "<compound operator>");
    r = consumeToken(b, COMPOUND_ADD);
    if (!r) r = consumeToken(b, COMPOUND_SUB);
    if (!r) r = consumeToken(b, COMPOUND_MUL);
    if (!r) r = consumeToken(b, COMPOUND_DIV);
    exit_section_(b, l, m, r, false, null);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!Content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Content_1", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = ProcIns(b, l + 1);
    if (!r) r = Comment(b, l + 1);
    if (!r) r = Element(b, l + 1);
    if (!r) r = consumeToken(b, CDATA);
    exit_section_(b, m, null, r);
    return r;
  }

  // XmlText?
  private static boolean Content_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Content_1_0_1")) return false;
    XmlText(b, l + 1);
    return true;
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
  // documentationAttachment? deprecatedAttachment? AnnotationAttachment*
  //                ( TypeDefinition
  //                | GlobalVariableDefinition
  //                | ServiceDefinition
  //                | FunctionDefinition
  //                | AnnotationDefinition
  //                | GlobalEndpointDefinition
  //                )
  public static boolean Definition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition>");
    r = Definition_0(b, l + 1);
    r = r && Definition_1(b, l + 1);
    r = r && Definition_2(b, l + 1);
    r = r && Definition_3(b, l + 1);
    exit_section_(b, l, m, r, false, TopLevelDefinitionRecover_parser_);
    return r;
  }

  // documentationAttachment?
  private static boolean Definition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition_0")) return false;
    documentationAttachment(b, l + 1);
    return true;
  }

  // deprecatedAttachment?
  private static boolean Definition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition_1")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean Definition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Definition_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // TypeDefinition
  //                | GlobalVariableDefinition
  //                | ServiceDefinition
  //                | FunctionDefinition
  //                | AnnotationDefinition
  //                | GlobalEndpointDefinition
  private static boolean Definition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeDefinition(b, l + 1);
    if (!r) r = GlobalVariableDefinition(b, l + 1);
    if (!r) r = ServiceDefinition(b, l + 1);
    if (!r) r = FunctionDefinition(b, l + 1);
    if (!r) r = AnnotationDefinition(b, l + 1);
    if (!r) r = GlobalEndpointDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // else LEFT_BRACE Block RIGHT_BRACE
  public static boolean ElseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_CLAUSE, null);
    r = consumeTokens(b, 1, ELSE, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // else if LEFT_PARENTHESIS Expression RIGHT_PARENTHESIS LEFT_BRACE Block RIGHT_BRACE
  public static boolean ElseIfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseIfClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_IF_CLAUSE, null);
    r = consumeTokens(b, 2, ELSE, IF, LEFT_PARENTHESIS);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, RIGHT_PARENTHESIS, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
    int c = current_position_(b);
    while (true) {
      if (!Attribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EmptyTag_2", c)) break;
      c = current_position_(b);
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
  // AnnotationAttachment* endpoint EndpointType identifier EndpointInitialization? SEMICOLON
  public static boolean EndpointDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndpointDefinition")) return false;
    if (!nextTokenIs(b, "<endpoint definition>", AT, ENDPOINT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENDPOINT_DEFINITION, "<endpoint definition>");
    r = EndpointDefinition_0(b, l + 1);
    r = r && consumeToken(b, ENDPOINT);
    p = r; // pin = 2
    r = r && report_error_(b, EndpointType(b, l + 1));
    r = p && report_error_(b, consumeToken(b, IDENTIFIER)) && r;
    r = p && report_error_(b, EndpointDefinition_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean EndpointDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndpointDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EndpointDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // EndpointInitialization?
  private static boolean EndpointDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndpointDefinition_4")) return false;
    EndpointInitialization(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RecordLiteral | ASSIGN VariableReference
  public static boolean EndpointInitialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndpointInitialization")) return false;
    if (!nextTokenIs(b, "<endpoint initialization>", ASSIGN, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENDPOINT_INITIALIZATION, "<endpoint initialization>");
    r = RecordLiteral(b, l + 1);
    if (!r) r = EndpointInitialization_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ASSIGN VariableReference
  private static boolean EndpointInitialization_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndpointInitialization_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NameReference
  public static boolean EndpointType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndpointType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENDPOINT_TYPE, "<endpoint type>");
    r = NameReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
    int c = current_position_(b);
    while (true) {
      if (!ExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ExpressionList_1", c)) break;
      c = current_position_(b);
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
  // !(NULL_LITERAL|int|string|float|boolean|blob|any|map|table|function|stream|'}'|';'|var |while|match|foreach|next|break|fork|try|throw|return|abort|fail|lock|xmlns|transaction|but|if|forever|object|check)
  static boolean ExpressionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !ExpressionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NULL_LITERAL|int|string|float|boolean|blob|any|map|table|function|stream|'}'|';'|var |while|match|foreach|next|break|fork|try|throw|return|abort|fail|lock|xmlns|transaction|but|if|forever|object|check
  private static boolean ExpressionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, BLOB);
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
    if (!r) r = consumeToken(b, NEXT);
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
  // //    stubClass="org.ballerinalang.plugins.idea.stubs.BallerinaFieldStub"
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
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // identifier | MUL
  private static boolean Field_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, MUL);
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  //     /*pin=1*/
  // //    stubClass="org.ballerinalang.plugins.idea.stubs.BallerinaFieldStub"
  // }
  private static boolean Field_2(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // fieldDefinition*
  public static boolean FieldDefinitionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinitionList")) return false;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION_LIST, "<field definition list>");
    int c = current_position_(b);
    while (true) {
      if (!fieldDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDefinitionList", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // finally LEFT_BRACE Block RIGHT_BRACE
  public static boolean FinallyClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FinallyClause")) return false;
    if (!nextTokenIs(b, FINALLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FINALLY_CLAUSE, null);
    r = consumeTokens(b, 1, FINALLY, LEFT_BRACE);
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
    int c = current_position_(b);
    while (true) {
      if (!FiniteType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FiniteType_1", c)) break;
      c = current_position_(b);
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
  // foreach LEFT_PARENTHESIS? VariableReferenceList in (IntRangeExpression | Expression) RIGHT_PARENTHESIS? LEFT_BRACE Block RIGHT_BRACE
  public static boolean ForeachStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement")) return false;
    if (!nextTokenIs(b, FOREACH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOREACH_STATEMENT, null);
    r = consumeToken(b, FOREACH);
    p = r; // pin = 1
    r = r && report_error_(b, ForeachStatement_1(b, l + 1));
    r = p && report_error_(b, VariableReferenceList(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, IN)) && r;
    r = p && report_error_(b, ForeachStatement_4(b, l + 1)) && r;
    r = p && report_error_(b, ForeachStatement_5(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS?
  private static boolean ForeachStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1")) return false;
    consumeToken(b, LEFT_PARENTHESIS);
    return true;
  }

  // IntRangeExpression | Expression
  private static boolean ForeachStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = IntRangeExpression(b, l + 1);
    if (!r) r = Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // RIGHT_PARENTHESIS?
  private static boolean ForeachStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_5")) return false;
    consumeToken(b, RIGHT_PARENTHESIS);
    return true;
  }

  /* ********************************************************** */
  // forever LEFT_BRACE ForeverStatementBody RIGHT_BRACE
  public static boolean ForeverStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatement")) return false;
    if (!nextTokenIs(b, FOREVER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOREVER_STATEMENT, null);
    r = consumeTokens(b, 2, FOREVER, LEFT_BRACE);
    p = r; // pin = 2
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
    int c = current_position_(b);
    while (r) {
      if (!StreamingQueryStatement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForeverStatementBody", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, FOREVER_STATEMENT_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // fork LEFT_BRACE ForkStatementBody RIGHT_BRACE JoinClause? TimeoutClause?
  public static boolean ForkJoinStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement")) return false;
    if (!nextTokenIs(b, FORK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FORK_JOIN_STATEMENT, null);
    r = consumeTokens(b, 1, FORK, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ForkStatementBody(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_BRACE)) && r;
    r = p && report_error_(b, ForkJoinStatement_4(b, l + 1)) && r;
    r = p && ForkJoinStatement_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // JoinClause?
  private static boolean ForkJoinStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement_4")) return false;
    JoinClause(b, l + 1);
    return true;
  }

  // TimeoutClause?
  private static boolean ForkJoinStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement_5")) return false;
    TimeoutClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // WorkerDefinition*
  public static boolean ForkStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkStatementBody")) return false;
    Marker m = enter_section_(b, l, _NONE_, FORK_STATEMENT_BODY, "<fork statement body>");
    int c = current_position_(b);
    while (true) {
      if (!WorkerDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForkStatementBody", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = DefaultableParameter(b, l + 1);
    if (!r) r = Parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA (DefaultableParameter | Parameter))*
  private static boolean FormalParameterList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!FormalParameterList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FormalParameterList_0_1", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = DefaultableParameter(b, l + 1);
    if (!r) r = Parameter(b, l + 1);
    exit_section_(b, m, null, r);
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
  // (public)? (native)? function (FunctionWithoutReceiver | FunctionWithReceiver)
  public static boolean FunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION, "<function definition>");
    r = FunctionDefinition_0(b, l + 1);
    r = r && FunctionDefinition_1(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 3
    r = r && FunctionDefinition_3(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public)?
  private static boolean FunctionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // (native)?
  private static boolean FunctionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_1")) return false;
    consumeToken(b, NATIVE);
    return true;
  }

  // FunctionWithoutReceiver | FunctionWithReceiver
  private static boolean FunctionDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FunctionWithoutReceiver(b, l + 1);
    if (!r) r = FunctionWithReceiver(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // functionNameReference LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean FunctionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_INVOCATION, "<function invocation>");
    r = functionNameReference(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && FunctionInvocation_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, l, m, r, false, null);
    return r;
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
    Marker m = enter_section_(b);
    r = ParameterList(b, l + 1);
    if (!r) r = ParameterTypeNameList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ReturnParameter?
  private static boolean FunctionTypeName_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_4")) return false;
    ReturnParameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (LT Parameter GT)? CallableUnitSignature (CallableUnitBody | SEMICOLON)
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

  // (LT Parameter GT)?
  private static boolean FunctionWithReceiver_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver_0")) return false;
    FunctionWithReceiver_0_0(b, l + 1);
    return true;
  }

  // LT Parameter GT
  private static boolean FunctionWithReceiver_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && Parameter(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // CallableUnitBody | SEMICOLON
  private static boolean FunctionWithReceiver_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithReceiver_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AttachedObject DOUBLE_COLON CallableUnitSignature CallableUnitBody
  static boolean FunctionWithoutReceiver(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionWithoutReceiver")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = AttachedObject(b, l + 1);
    r = r && consumeToken(b, DOUBLE_COLON);
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
  // public? EndpointDefinition
  public static boolean GlobalEndpointDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalEndpointDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_ENDPOINT_DEFINITION, "<global endpoint definition>");
    r = GlobalEndpointDefinition_0(b, l + 1);
    r = r && EndpointDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // public?
  private static boolean GlobalEndpointDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalEndpointDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // (public)? TypeName identifier (ASSIGN  Expression)? SEMICOLON
  public static boolean GlobalVariableDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE_DEFINITION, "<global variable definition>");
    r = GlobalVariableDefinition_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, IDENTIFIER));
    r = p && report_error_(b, GlobalVariableDefinition_3(b, l + 1)) && r;
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

  // (ASSIGN  Expression)?
  private static boolean GlobalVariableDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition_3")) return false;
    GlobalVariableDefinition_3_0(b, l + 1);
    return true;
  }

  // ASSIGN  Expression
  private static boolean GlobalVariableDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition_3_0")) return false;
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
  // if LEFT_PARENTHESIS Expression RIGHT_PARENTHESIS LEFT_BRACE Block RIGHT_BRACE
  public static boolean IfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfClause")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_CLAUSE, null);
    r = consumeTokens(b, 1, IF, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, RIGHT_PARENTHESIS, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
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
    int c = current_position_(b);
    while (true) {
      if (!ElseIfClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "IfElseStatement_1", c)) break;
      c = current_position_(b);
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
  // DECIMAL_INTEGER_LITERAL | HEX_INTEGER_LITERAL | OCTAL_INTEGER_LITERAL | BINARY_INTEGER_LITERAL
  public static boolean IntegerLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntegerLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INTEGER_LITERAL, "<integer literal>");
    r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, HEX_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, OCTAL_INTEGER_LITERAL);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NOT);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!InvocationArgList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InvocationArgList_1", c)) break;
      c = current_position_(b);
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
  // join (LEFT_PARENTHESIS JoinConditions RIGHT_PARENTHESIS)? LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS JoinClauseBody
  public static boolean JoinClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClause")) return false;
    if (!nextTokenIs(b, JOIN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CLAUSE, null);
    r = consumeToken(b, JOIN);
    p = r; // pin = 1
    r = r && report_error_(b, JoinClause_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, LEFT_PARENTHESIS)) && r;
    r = p && report_error_(b, TypeName(b, l + 1, -1)) && r;
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, RIGHT_PARENTHESIS)) && r;
    r = p && JoinClauseBody(b, l + 1) && r;
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && JoinConditions(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean JoinClauseBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinClauseBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, JOIN_CLAUSE_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // (some IntegerLiteral | all) (identifier (COMMA identifier)*)?
  public static boolean JoinConditions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions")) return false;
    if (!nextTokenIs(b, "<join conditions>", ALL, SOME)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CONDITIONS, "<join conditions>");
    r = JoinConditions_0(b, l + 1);
    p = r; // pin = 1
    r = r && JoinConditions_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SOME);
    r = r && IntegerLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && JoinConditions_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA identifier)*
  private static boolean JoinConditions_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_1_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!JoinConditions_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "JoinConditions_1_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA identifier
  private static boolean JoinConditions_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinConditions_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (unidirectional JoinType | JoinType unidirectional | JoinType) StreamingInput on Expression
  public static boolean JoinStreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_STREAMING_INPUT, "<join streaming input>");
    r = JoinStreamingInput_0(b, l + 1);
    r = r && StreamingInput(b, l + 1);
    r = r && consumeToken(b, ON);
    r = r && Expression(b, l + 1, -1);
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
  // LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS EQUAL_GT LambdaReturnParameter? CallableUnitBody
  public static boolean LambdaFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA_FUNCTION, null);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && LambdaFunction_1(b, l + 1);
    r = r && consumeTokens(b, 2, RIGHT_PARENTHESIS, EQUAL_GT);
    p = r; // pin = 4
    r = r && report_error_(b, LambdaFunction_4(b, l + 1));
    r = p && CallableUnitBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FormalParameterList?
  private static boolean LambdaFunction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_1")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // LambdaReturnParameter?
  private static boolean LambdaFunction_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_4")) return false;
    LambdaReturnParameter(b, l + 1);
    return true;
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LambdaReturnParameter_0", c)) break;
      c = current_position_(b);
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
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATCH_EXPRESSION_PATTERN_CLAUSE, "<match expression pattern clause>");
    r = TypeName(b, l + 1, -1);
    r = r && MatchExpressionPatternClause_1(b, l + 1);
    r = r && consumeToken(b, EQUAL_GT);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
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
  // next SEMICOLON
  public static boolean NextStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NextStatement")) return false;
    if (!nextTokenIs(b, NEXT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NEXT_STATEMENT, null);
    r = consumeTokens(b, 1, NEXT, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Statement+
  public static boolean NonEmptyBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NonEmptyBlock")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<non empty block>");
    r = Statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "NonEmptyBlock", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PublicObjectFields? PrivateObjectFields? ObjectInitializer? ObjectFunctions?
  public static boolean ObjectBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_BODY, "<object body>");
    r = ObjectBody_0(b, l + 1);
    r = r && ObjectBody_1(b, l + 1);
    r = r && ObjectBody_2(b, l + 1);
    r = r && ObjectBody_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // PublicObjectFields?
  private static boolean ObjectBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_0")) return false;
    PublicObjectFields(b, l + 1);
    return true;
  }

  // PrivateObjectFields?
  private static boolean ObjectBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_1")) return false;
    PrivateObjectFields(b, l + 1);
    return true;
  }

  // ObjectInitializer?
  private static boolean ObjectBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_2")) return false;
    ObjectInitializer(b, l + 1);
    return true;
  }

  // ObjectFunctions?
  private static boolean ObjectBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_3")) return false;
    ObjectFunctions(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // AnyIdentifierName LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS ReturnParameter?
  public static boolean ObjectCallableUnitSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectCallableUnitSignature")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_CALLABLE_UNIT_SIGNATURE, "<object callable unit signature>");
    r = AnyIdentifierName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && ObjectCallableUnitSignature_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    r = r && ObjectCallableUnitSignature_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FormalParameterList?
  private static boolean ObjectCallableUnitSignature_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectCallableUnitSignature_2")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // ReturnParameter?
  private static boolean ObjectCallableUnitSignature_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectCallableUnitSignature_4")) return false;
    ReturnParameter(b, l + 1);
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
  // (public)? (native)? function ObjectCallableUnitSignature (CallableUnitBody | SEMICOLON)
  public static boolean ObjectFunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FUNCTION_DEFINITION, "<object function definition>");
    r = ObjectFunctionDefinition_0(b, l + 1);
    r = r && ObjectFunctionDefinition_1(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 3
    r = r && report_error_(b, ObjectCallableUnitSignature(b, l + 1));
    r = p && ObjectFunctionDefinition_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public)?
  private static boolean ObjectFunctionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // (native)?
  private static boolean ObjectFunctionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_1")) return false;
    consumeToken(b, NATIVE);
    return true;
  }

  // CallableUnitBody | SEMICOLON
  private static boolean ObjectFunctionDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (AnnotationAttachment* documentationAttachment? deprecatedAttachment? ObjectFunctionDefinition)+
  public static boolean ObjectFunctions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctions")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FUNCTIONS, "<object functions>");
    r = ObjectFunctions_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!ObjectFunctions_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFunctions", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment* documentationAttachment? deprecatedAttachment? ObjectFunctionDefinition
  private static boolean ObjectFunctions_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctions_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectFunctions_0_0(b, l + 1);
    r = r && ObjectFunctions_0_1(b, l + 1);
    r = r && ObjectFunctions_0_2(b, l + 1);
    r = r && ObjectFunctionDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment*
  private static boolean ObjectFunctions_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctions_0_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFunctions_0_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // documentationAttachment?
  private static boolean ObjectFunctions_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctions_0_1")) return false;
    documentationAttachment(b, l + 1);
    return true;
  }

  // deprecatedAttachment?
  private static boolean ObjectFunctions_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctions_0_2")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // AnnotationAttachment* documentationAttachment? (public)? new ObjectInitializerParameterList CallableUnitBody
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

  // AnnotationAttachment*
  private static boolean ObjectInitializer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializer_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectInitializer_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // documentationAttachment?
  private static boolean ObjectInitializer_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectInitializer_1")) return false;
    documentationAttachment(b, l + 1);
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectParameter_0", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = ObjectDefaultableParameter(b, l + 1);
    if (!r) r = ObjectParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA (ObjectDefaultableParameter | ObjectParameter))*
  private static boolean ObjectParameterList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectParameterList_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ObjectParameterList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectParameterList_0_1", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = ObjectDefaultableParameter(b, l + 1);
    if (!r) r = ObjectParameter(b, l + 1);
    exit_section_(b, m, null, r);
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
  public static boolean OnabortStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnabortStatement")) return false;
    if (!nextTokenIs(b, ONABORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ONABORT_STATEMENT, null);
    r = consumeTokens(b, 1, ONABORT, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // oncommit ASSIGN Expression
  public static boolean OncommitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OncommitStatement")) return false;
    if (!nextTokenIs(b, ONCOMMIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ONCOMMIT_STATEMENT, null);
    r = consumeTokens(b, 1, ONCOMMIT, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // onretry LEFT_BRACE Block RIGHT_BRACE
  public static boolean OnretryClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnretryClause")) return false;
    if (!nextTokenIs(b, ONRETRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ONRETRY_CLAUSE, null);
    r = consumeTokens(b, 1, ONRETRY, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
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
    int c = current_position_(b);
    while (true) {
      if (!OrderByClause_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "OrderByClause_3", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, ALL);
    if (!r) r = consumeToken(b, LAST);
    if (!r) r = consumeToken(b, FIRST);
    exit_section_(b, m, null, r);
    return r;
  }

  // TimeScale | events
  private static boolean OutputRateLimit_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TimeScale(b, l + 1);
    if (!r) r = consumeToken(b, EVENTS);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!ParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterList_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!ParameterTypeNameList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterTypeNameList_1", c)) break;
      c = current_position_(b);
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
  // NOT PatternStreamingEdgeInput (AND PatternStreamingEdgeInput | for SimpleLiteral)
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

  // AND PatternStreamingEdgeInput | for SimpleLiteral
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

  // for SimpleLiteral
  private static boolean Pattern4_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, FOR);
    p = r; // pin = 1
    r = r && SimpleLiteral(b, l + 1);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    exit_section_(b, m, null, r);
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, AS, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // INCREMENT | DECREMENT
  public static boolean PostArithmeticOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PostArithmeticOperator")) return false;
    if (!nextTokenIs(b, "<post arithmetic operator>", DECREMENT, INCREMENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POST_ARITHMETIC_OPERATOR, "<post arithmetic operator>");
    r = consumeToken(b, INCREMENT);
    if (!r) r = consumeToken(b, DECREMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VariableReference PostArithmeticOperator SEMICOLON
  public static boolean PostIncrementStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PostIncrementStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, POST_INCREMENT_STATEMENT, "<post increment statement>");
    r = VariableReference(b, l + 1, -1);
    r = r && PostArithmeticOperator(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // private LEFT_BRACE fieldDefinition* RIGHT_BRACE
  public static boolean PrivateObjectFields(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PrivateObjectFields")) return false;
    if (!nextTokenIs(b, PRIVATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PRIVATE_OBJECT_FIELDS, null);
    r = consumeTokens(b, 1, PRIVATE, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, PrivateObjectFields_2(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // fieldDefinition*
  private static boolean PrivateObjectFields_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PrivateObjectFields_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!fieldDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "PrivateObjectFields_2", c)) break;
      c = current_position_(b);
    }
    return true;
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
    int c = current_position_(b);
    while (true) {
      if (!ProcIns_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ProcIns_1", c)) break;
      c = current_position_(b);
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
  // public LEFT_BRACE fieldDefinition* RIGHT_BRACE
  public static boolean PublicObjectFields(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PublicObjectFields")) return false;
    if (!nextTokenIs(b, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PUBLIC_OBJECT_FIELDS, null);
    r = consumeTokens(b, 2, PUBLIC, LEFT_BRACE);
    p = r; // pin = 2
    r = r && report_error_(b, PublicObjectFields_2(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // fieldDefinition*
  private static boolean PublicObjectFields_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PublicObjectFields_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!fieldDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "PublicObjectFields_2", c)) break;
      c = current_position_(b);
    }
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
    int c = current_position_(b);
    while (true) {
      if (!RecordLiteralBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RecordLiteralBody_1", c)) break;
      c = current_position_(b);
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
  // foreach | map | start
  public static boolean ReservedWord(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReservedWord")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESERVED_WORD, "<reserved word>");
    r = consumeToken(b, FOREACH);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, START);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* documentationAttachment? deprecatedAttachment? identifier LEFT_PARENTHESIS resourceParameterList? RIGHT_PARENTHESIS CallableUnitBody
  public static boolean ResourceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ResourceDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RESOURCE_DEFINITION, "<resource definition>");
    r = ResourceDefinition_0(b, l + 1);
    r = r && ResourceDefinition_1(b, l + 1);
    r = r && ResourceDefinition_2(b, l + 1);
    r = r && consumeTokens(b, 1, IDENTIFIER, LEFT_PARENTHESIS);
    p = r; // pin = 4
    r = r && report_error_(b, ResourceDefinition_5(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && CallableUnitBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ResourceDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ResourceDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ResourceDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // documentationAttachment?
  private static boolean ResourceDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ResourceDefinition_1")) return false;
    documentationAttachment(b, l + 1);
    return true;
  }

  // deprecatedAttachment?
  private static boolean ResourceDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ResourceDefinition_2")) return false;
    deprecatedAttachment(b, l + 1);
    return true;
  }

  // resourceParameterList?
  private static boolean ResourceDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ResourceDefinition_5")) return false;
    resourceParameterList(b, l + 1);
    return true;
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RestParameter_0", c)) break;
      c = current_position_(b);
    }
    return true;
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
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
    Marker m = enter_section_(b);
    r = consumeToken(b, MUL);
    if (!r) r = SelectExpressionList(b, l + 1);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!SelectExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SelectExpressionList_1", c)) break;
      c = current_position_(b);
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
  // LEFT_BRACE (/*ResourceDefinition+ | EndpointDefinition+ ResourceDefinition* |*/ EndpointDefinition* (VariableDefinitionStatementInService | NamespaceDeclarationStatement)* ResourceDefinition*) RIGHT_BRACE
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

  // EndpointDefinition* (VariableDefinitionStatementInService | NamespaceDeclarationStatement)* ResourceDefinition*
  private static boolean ServiceBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ServiceBody_1_0(b, l + 1);
    r = r && ServiceBody_1_1(b, l + 1);
    r = r && ServiceBody_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EndpointDefinition*
  private static boolean ServiceBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!EndpointDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceBody_1_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (VariableDefinitionStatementInService | NamespaceDeclarationStatement)*
  private static boolean ServiceBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ServiceBody_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceBody_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // VariableDefinitionStatementInService | NamespaceDeclarationStatement
  private static boolean ServiceBody_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = VariableDefinitionStatementInService(b, l + 1);
    if (!r) r = NamespaceDeclarationStatement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ResourceDefinition*
  private static boolean ServiceBody_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ResourceDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceBody_1_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // service (LT NameReference GT)? identifier ServiceEndpointAttachments?  ServiceBody
  public static boolean ServiceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition")) return false;
    if (!nextTokenIs(b, SERVICE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_DEFINITION, null);
    r = consumeToken(b, SERVICE);
    p = r; // pin = 1
    r = r && report_error_(b, ServiceDefinition_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, IDENTIFIER)) && r;
    r = p && report_error_(b, ServiceDefinition_3(b, l + 1)) && r;
    r = p && ServiceBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT NameReference GT)?
  private static boolean ServiceDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition_1")) return false;
    ServiceDefinition_1_0(b, l + 1);
    return true;
  }

  // LT NameReference GT
  private static boolean ServiceDefinition_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && NameReference(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // ServiceEndpointAttachments?
  private static boolean ServiceDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition_3")) return false;
    ServiceEndpointAttachments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // bind (NameReference (COMMA NameReference)* | RecordLiteral)
  public static boolean ServiceEndpointAttachments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceEndpointAttachments")) return false;
    if (!nextTokenIs(b, BIND)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_ENDPOINT_ATTACHMENTS, null);
    r = consumeToken(b, BIND);
    p = r; // pin = 1
    r = r && ServiceEndpointAttachments_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NameReference (COMMA NameReference)* | RecordLiteral
  private static boolean ServiceEndpointAttachments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceEndpointAttachments_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ServiceEndpointAttachments_1_0(b, l + 1);
    if (!r) r = RecordLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NameReference (COMMA NameReference)*
  private static boolean ServiceEndpointAttachments_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceEndpointAttachments_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NameReference(b, l + 1);
    r = r && ServiceEndpointAttachments_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA NameReference)*
  private static boolean ServiceEndpointAttachments_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceEndpointAttachments_1_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ServiceEndpointAttachments_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceEndpointAttachments_1_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA NameReference
  private static boolean ServiceEndpointAttachments_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceEndpointAttachments_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && NameReference(b, l + 1);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!SetClause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SetClause_2", c)) break;
      c = current_position_(b);
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
  // (ADD|SUB)? IntegerLiteral
  //                   | (ADD|SUB)? FLOATING_POINT_LITERAL
  //                   | QUOTED_STRING_LITERAL
  //                   | BOOLEAN_LITERAL
  //                   | EmptyTupleLiteral
  //                   | NULL_LITERAL
  public static boolean SimpleLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_LITERAL, "<simple literal>");
    r = SimpleLiteral_0(b, l + 1);
    if (!r) r = SimpleLiteral_1(b, l + 1);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    if (!r) r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = EmptyTupleLiteral(b, l + 1);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ADD|SUB)? FLOATING_POINT_LITERAL
  private static boolean SimpleLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleLiteral_1_0(b, l + 1);
    r = r && consumeToken(b, FLOATING_POINT_LITERAL);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SimpleParameter_0", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!Attribute(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StartTag_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // WhileStatement
  //     |   ForeverStatement
  //     |   NextStatement
  //     |   ForeachStatement
  //     |   matchStatement
  //     |   BreakStatement
  //     |   ThrowStatement
  //     |   ReturnStatement
  //     |   AbortStatement
  //     |   retryStatement
  //     |   LockStatement
  //     |   NamespaceDeclarationStatement
  //     |   TransactionStatement
  //     |   IfElseStatement
  //     |   TryCatchStatement
  //     |   ForkJoinStatement
  //     |   tupleDestructuringStatement
  //     |   WorkerInteractionStatement
  //     |   AssignmentStatement
  //     |   VariableDefinitionStatement
  //     |   CompoundAssignmentStatement
  //     |   PostIncrementStatement
  //     |   ExpressionStmt
  //     |   StreamingQueryStatement
  public static boolean Statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = WhileStatement(b, l + 1);
    if (!r) r = ForeverStatement(b, l + 1);
    if (!r) r = NextStatement(b, l + 1);
    if (!r) r = ForeachStatement(b, l + 1);
    if (!r) r = matchStatement(b, l + 1);
    if (!r) r = BreakStatement(b, l + 1);
    if (!r) r = ThrowStatement(b, l + 1);
    if (!r) r = ReturnStatement(b, l + 1);
    if (!r) r = AbortStatement(b, l + 1);
    if (!r) r = retryStatement(b, l + 1);
    if (!r) r = LockStatement(b, l + 1);
    if (!r) r = NamespaceDeclarationStatement(b, l + 1);
    if (!r) r = TransactionStatement(b, l + 1);
    if (!r) r = IfElseStatement(b, l + 1);
    if (!r) r = TryCatchStatement(b, l + 1);
    if (!r) r = ForkJoinStatement(b, l + 1);
    if (!r) r = tupleDestructuringStatement(b, l + 1);
    if (!r) r = WorkerInteractionStatement(b, l + 1);
    if (!r) r = AssignmentStatement(b, l + 1);
    if (!r) r = VariableDefinitionStatement(b, l + 1);
    if (!r) r = CompoundAssignmentStatement(b, l + 1);
    if (!r) r = PostIncrementStatement(b, l + 1);
    if (!r) r = ExpressionStmt(b, l + 1);
    if (!r) r = StreamingQueryStatement(b, l + 1);
    exit_section_(b, l, m, r, false, StatementRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|int|string|float|boolean|blob|any|json|xml|xmlns|map|table|function|stream|'('|'}'|';'|typedesc|future|await|var|while|match|foreach|next|break|fork|try|throw|return|abort|fail|lock|transaction|if|forever|object|check|identifier)
  static boolean StatementRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !StatementRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|int|string|float|boolean|blob|any|json|xml|xmlns|map|table|function|stream|'('|'}'|';'|typedesc|future|await|var|while|match|foreach|next|break|fork|try|throw|return|abort|fail|lock|transaction|if|forever|object|check|identifier
  private static boolean StatementRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    if (!r) r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, HEX_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, OCTAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, BINARY_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, BLOB);
    if (!r) r = consumeToken(b, ANY);
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
    if (!r) r = consumeToken(b, NEXT);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, FORK);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, THROW);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, ABORT);
    if (!r) r = consumeToken(b, FAIL);
    if (!r) r = consumeToken(b, LOCK);
    if (!r) r = consumeToken(b, TRANSACTION);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, CHECK);
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
  // EQUAL_GT LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS LEFT_BRACE Block RIGHT_BRACE
  public static boolean StreamingAction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingAction")) return false;
    if (!nextTokenIs(b, EQUAL_GT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_ACTION, null);
    r = consumeTokens(b, 1, EQUAL_GT, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingAction_2(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, RIGHT_PARENTHESIS, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FormalParameterList?
  private static boolean StreamingAction_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingAction_2")) return false;
    FormalParameterList(b, l + 1);
    return true;
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
    int c = current_position_(b);
    while (true) {
      if (!FunctionInvocation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StreamingInput_2", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!FunctionInvocation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StreamingInput_4", c)) break;
      c = current_position_(b);
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, AS, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
    int c = current_position_(b);
    while (r) {
      if (!StringTemplateExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StringTemplateExpressionContent_0", c)) break;
      c = current_position_(b);
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
  // RecordLiteral
  public static boolean TableInitialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableInitialization")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RecordLiteral(b, l + 1);
    exit_section_(b, m, TABLE_INITIALIZATION, r);
    return r;
  }

  /* ********************************************************** */
  // table TableInitialization
  public static boolean TableLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral")) return false;
    if (!nextTokenIs(b, TABLE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_LITERAL, null);
    r = consumeToken(b, TABLE);
    p = r; // pin = 1
    r = r && TableInitialization(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // timeout LEFT_PARENTHESIS Expression RIGHT_PARENTHESIS LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS TimeoutClauseBody
  public static boolean TimeoutClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeoutClause")) return false;
    if (!nextTokenIs(b, TIMEOUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TIMEOUT_CLAUSE, null);
    r = consumeTokens(b, 1, TIMEOUT, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, RIGHT_PARENTHESIS, LEFT_PARENTHESIS)) && r;
    r = p && report_error_(b, TypeName(b, l + 1, -1)) && r;
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, TIMEOUT_CLAUSE_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // !(DOCUMENTATION_TEMPLATE_START|DEPRECATED_TEMPLATE_START|'@'|native|public|type|typedesc|service|function|enum|annotation|endpoint|int|float|boolean|string|blob|map|xml|xmlns|json|table|any|stream|object|future|identifier|'{')
  static boolean TopLevelDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !TopLevelDefinitionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOCUMENTATION_TEMPLATE_START|DEPRECATED_TEMPLATE_START|'@'|native|public|type|typedesc|service|function|enum|annotation|endpoint|int|float|boolean|string|blob|map|xml|xmlns|json|table|any|stream|object|future|identifier|'{'
  private static boolean TopLevelDefinitionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOCUMENTATION_TEMPLATE_START);
    if (!r) r = consumeToken(b, DEPRECATED_TEMPLATE_START);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, TYPEDESC);
    if (!r) r = consumeToken(b, SERVICE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, ANNOTATION);
    if (!r) r = consumeToken(b, ENDPOINT);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, BLOB);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, XML);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, JSON);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, FUTURE);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // transaction (WITH TransactionPropertyInitStatementList)? LEFT_BRACE Block RIGHT_BRACE
  public static boolean TransactionClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause")) return false;
    if (!nextTokenIs(b, TRANSACTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_CLAUSE, null);
    r = consumeToken(b, TRANSACTION);
    p = r; // pin = 1
    r = r && report_error_(b, TransactionClause_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (WITH TransactionPropertyInitStatementList)?
  private static boolean TransactionClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_1")) return false;
    TransactionClause_1_0(b, l + 1);
    return true;
  }

  // WITH TransactionPropertyInitStatementList
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

  /* ********************************************************** */
  // RetriesStatement | OncommitStatement | OnabortStatement
  public static boolean TransactionPropertyInitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_PROPERTY_INIT_STATEMENT, "<transaction property init statement>");
    r = RetriesStatement(b, l + 1);
    if (!r) r = OncommitStatement(b, l + 1);
    if (!r) r = OnabortStatement(b, l + 1);
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
    int c = current_position_(b);
    while (true) {
      if (!TransactionPropertyInitStatementList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TransactionPropertyInitStatementList_1", c)) break;
      c = current_position_(b);
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
  // TransactionClause OnretryClause?
  public static boolean TransactionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement")) return false;
    if (!nextTokenIs(b, TRANSACTION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TransactionClause(b, l + 1);
    r = r && TransactionStatement_1(b, l + 1);
    exit_section_(b, m, TRANSACTION_STATEMENT, r);
    return r;
  }

  // OnretryClause?
  private static boolean TransactionStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement_1")) return false;
    OnretryClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression RARROW (fork | identifier) SEMICOLON
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

  // fork | identifier
  private static boolean TriggerWorker_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TriggerWorker_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FORK);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // try LEFT_BRACE Block RIGHT_BRACE CatchClauses
  public static boolean TryCatchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryCatchStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRY_CATCH_STATEMENT, null);
    r = consumeTokens(b, 1, TRY, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_BRACE)) && r;
    r = p && CatchClauses(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleParameter_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (COMMA parameterWithType)*
  private static boolean TupleParameter_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleParameter_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!TupleParameter_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleParameter_3", c)) break;
      c = current_position_(b);
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
  // new UserDefineTypeName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS | new (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  public static boolean TypeInitExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeInitExpr_0(b, l + 1);
    if (!r) r = TypeInitExpr_1(b, l + 1);
    exit_section_(b, m, TYPE_INIT_EXPR, r);
    return r;
  }

  // new UserDefineTypeName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  private static boolean TypeInitExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEW);
    r = r && UserDefineTypeName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && TypeInitExpr_0_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // InvocationArgList?
  private static boolean TypeInitExpr_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr_0_3")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  // new (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  private static boolean TypeInitExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEW);
    r = r && TypeInitExpr_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  private static boolean TypeInitExpr_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr_1_1")) return false;
    TypeInitExpr_1_1_0(b, l + 1);
    return true;
  }

  // LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  private static boolean TypeInitExpr_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && TypeInitExpr_1_1_0_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // InvocationArgList?
  private static boolean TypeInitExpr_1_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpr_1_1_0_1")) return false;
    InvocationArgList(b, l + 1);
    return true;
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
  // boolean | int | float | string | blob
  public static boolean ValueTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ValueTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE_TYPE_NAME, "<value type name>");
    r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, BLOB);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN Expression
  static boolean VariableAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableAssignment")) return false;
    if (!nextTokenIs(b, ASSIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TypeName identifier VariableAssignment? SEMICOLON
  public static boolean VariableDefinitionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableDefinitionStatement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT, "<variable definition statement>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 2
    r = r && report_error_(b, VariableDefinitionStatement_2(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // VariableAssignment?
  private static boolean VariableDefinitionStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableDefinitionStatement_2")) return false;
    VariableAssignment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TypeName identifier VariableAssignment? SEMICOLON
  public static boolean VariableDefinitionStatementInService(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableDefinitionStatementInService")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT, "<variable definition statement in service>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && VariableDefinitionStatementInService_2(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // VariableAssignment?
  private static boolean VariableDefinitionStatementInService_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableDefinitionStatementInService_2")) return false;
    VariableAssignment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // VariableReference (COMMA VariableReference)*
  public static boolean VariableReferenceList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_REFERENCE_LIST, "<variable reference list>");
    r = VariableReference(b, l + 1, -1);
    r = r && VariableReferenceList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA VariableReference)*
  private static boolean VariableReferenceList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!VariableReferenceList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "VariableReferenceList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA VariableReference
  private static boolean VariableReferenceList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
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
  // while LEFT_PARENTHESIS Expression RIGHT_PARENTHESIS WhileStatementBody
  public static boolean WhileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, WHILE, LEFT_PARENTHESIS);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    r = r && WhileStatementBody(b, l + 1);
    exit_section_(b, m, WHILE_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean WhileStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatementBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, WHILE_STATEMENT_BODY, r);
    return r;
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
  // within Expression
  public static boolean WithinClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WithinClause")) return false;
    if (!nextTokenIs(b, WITHIN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WITHIN_CLAUSE, null);
    r = consumeToken(b, WITHIN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean WorkerBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, WORKER_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // worker identifier WorkerBody
  public static boolean WorkerDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinition")) return false;
    if (!nextTokenIs(b, WORKER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_DEFINITION, null);
    r = consumeTokens(b, 1, WORKER, IDENTIFIER);
    p = r; // pin = 1
    r = r && WorkerBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // Expression LARROW identifier SEMICOLON
  public static boolean WorkerReply(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReply")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_REPLY, "<worker reply>");
    r = Expression(b, l + 1, -1);
    r = r && consumeTokens(b, 1, LARROW, IDENTIFIER, SEMICOLON);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
    int c = current_position_(b);
    while (true) {
      if (!XmlDoubleQuotedString_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlDoubleQuotedString_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!XmlSingleQuotedString_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlSingleQuotedString_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (r) {
      if (!XmlText_0_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlText_0_0", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // RIGHT_BRACKET|RIGHT_PARENTHESIS
  private static boolean closedRange_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!deprecatedText_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deprecatedText_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode
  private static boolean deprecatedText_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEPRECATED_TEMPLATE_TEXT);
    if (!r) r = deprecatedTemplateInlineCode(b, l + 1);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!deprecatedText_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deprecatedText_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DEPRECATED_TEMPLATE_TEXT | deprecatedTemplateInlineCode
  private static boolean deprecatedText_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedText_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEPRECATED_TEMPLATE_TEXT);
    if (!r) r = deprecatedTemplateInlineCode(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // documentationTemplateInlineCode (DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode)*
  //             | DOCUMENTATION_TEMPLATE_TEXT  (DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode)*
  public static boolean docText(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOC_TEXT, "<doc text>");
    r = docText_0(b, l + 1);
    if (!r) r = docText_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationTemplateInlineCode (DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode)*
  private static boolean docText_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationTemplateInlineCode(b, l + 1);
    r = r && docText_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode)*
  private static boolean docText_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!docText_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "docText_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode
  private static boolean docText_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOCUMENTATION_TEMPLATE_TEXT);
    if (!r) r = documentationTemplateInlineCode(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOCUMENTATION_TEMPLATE_TEXT  (DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode)*
  private static boolean docText_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOCUMENTATION_TEMPLATE_TEXT);
    r = r && docText_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode)*
  private static boolean docText_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!docText_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "docText_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DOCUMENTATION_TEMPLATE_TEXT | documentationTemplateInlineCode
  private static boolean docText_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docText_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOCUMENTATION_TEMPLATE_TEXT);
    if (!r) r = documentationTemplateInlineCode(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DOCUMENTATION_TEMPLATE_START documentationTemplateContent? DOCUMENTATION_TEMPLATE_END
  public static boolean documentationAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationAttachment")) return false;
    if (!nextTokenIs(b, DOCUMENTATION_TEMPLATE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_ATTACHMENT, null);
    r = consumeToken(b, DOCUMENTATION_TEMPLATE_START);
    p = r; // pin = 1
    r = r && report_error_(b, documentationAttachment_1(b, l + 1));
    r = p && consumeToken(b, DOCUMENTATION_TEMPLATE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationTemplateContent?
  private static boolean documentationAttachment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationAttachment_1")) return false;
    documentationTemplateContent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DOCUMENTATION_TEMPLATE_ATTRIBUTE_START identifier? DOCUMENTATION_TEMPLATE_ATTRIBUTE_END docText?
  public static boolean documentationTemplateAttributeDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateAttributeDescription")) return false;
    if (!nextTokenIs(b, DOCUMENTATION_TEMPLATE_ATTRIBUTE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_TEMPLATE_ATTRIBUTE_DESCRIPTION, null);
    r = consumeToken(b, DOCUMENTATION_TEMPLATE_ATTRIBUTE_START);
    p = r; // pin = 1
    r = r && report_error_(b, documentationTemplateAttributeDescription_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, DOCUMENTATION_TEMPLATE_ATTRIBUTE_END)) && r;
    r = p && documentationTemplateAttributeDescription_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // identifier?
  private static boolean documentationTemplateAttributeDescription_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateAttributeDescription_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  // docText?
  private static boolean documentationTemplateAttributeDescription_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateAttributeDescription_3")) return false;
    docText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // docText? documentationTemplateAttributeDescription+ | docText
  public static boolean documentationTemplateContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_TEMPLATE_CONTENT, "<documentation template content>");
    r = documentationTemplateContent_0(b, l + 1);
    if (!r) r = docText(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // docText? documentationTemplateAttributeDescription+
  private static boolean documentationTemplateContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateContent_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationTemplateContent_0_0(b, l + 1);
    r = r && documentationTemplateContent_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // docText?
  private static boolean documentationTemplateContent_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateContent_0_0")) return false;
    docText(b, l + 1);
    return true;
  }

  // documentationTemplateAttributeDescription+
  private static boolean documentationTemplateContent_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateContent_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationTemplateAttributeDescription(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!documentationTemplateAttributeDescription(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationTemplateContent_0_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // singleBackTickDocInlineCode | doubleBackTickDocInlineCode | tripleBackTickDocInlineCode
  public static boolean documentationTemplateInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTemplateInlineCode")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_TEMPLATE_INLINE_CODE, "<documentation template inline code>");
    r = singleBackTickDocInlineCode(b, l + 1);
    if (!r) r = doubleBackTickDocInlineCode(b, l + 1);
    if (!r) r = tripleBackTickDocInlineCode(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // DB_DOC_INLINE_CODE_START DOUBLE_BACK_TICK_INLINE_CODE? DOUBLE_BACK_TICK_INLINE_CODE_END
  public static boolean doubleBackTickDocInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBackTickDocInlineCode")) return false;
    if (!nextTokenIs(b, DB_DOC_INLINE_CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOUBLE_BACK_TICK_DOC_INLINE_CODE, null);
    r = consumeToken(b, DB_DOC_INLINE_CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, doubleBackTickDocInlineCode_1(b, l + 1));
    r = p && consumeToken(b, DOUBLE_BACK_TICK_INLINE_CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // DOUBLE_BACK_TICK_INLINE_CODE?
  private static boolean doubleBackTickDocInlineCode_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBackTickDocInlineCode_1")) return false;
    consumeToken(b, DOUBLE_BACK_TICK_INLINE_CODE);
    return true;
  }

  /* ********************************************************** */
  // endpoint identifier (COMMA ParameterList)?
  public static boolean endpointParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endpointParameter")) return false;
    if (!nextTokenIs(b, ENDPOINT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENDPOINT_PARAMETER, null);
    r = consumeTokens(b, 1, ENDPOINT, IDENTIFIER);
    p = r; // pin = 1
    r = r && endpointParameter_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA ParameterList)?
  private static boolean endpointParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endpointParameter_2")) return false;
    endpointParameter_2_0(b, l + 1);
    return true;
  }

  // COMMA ParameterList
  private static boolean endpointParameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endpointParameter_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && ParameterList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName identifier (ASSIGN Expression)? (COMMA | SEMICOLON)
  public static boolean fieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION, "<field definition>");
    r = fieldDefinition_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 3
    r = r && report_error_(b, fieldDefinition_3(b, l + 1));
    r = p && fieldDefinition_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean fieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (ASSIGN Expression)?
  private static boolean fieldDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDefinition_3")) return false;
    fieldDefinition_3_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean fieldDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDefinition_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA | SEMICOLON
  private static boolean fieldDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldDefinition_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
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
  // but LEFT_BRACE MatchExpressionPatternClause (COMMA MatchExpressionPatternClause)* RIGHT_BRACE
  public static boolean matchExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression")) return false;
    if (!nextTokenIs(b, BUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MATCH_EXPRESSION, null);
    r = consumeTokens(b, 1, BUT, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, MatchExpressionPatternClause(b, l + 1));
    r = p && report_error_(b, matchExpression_3(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA MatchExpressionPatternClause)*
  private static boolean matchExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!matchExpression_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "matchExpression_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA MatchExpressionPatternClause
  private static boolean matchExpression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchExpression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && MatchExpressionPatternClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // namedPattern | unnamedPattern
  public static boolean matchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATCH_PATTERN_CLAUSE, "<match pattern clause>");
    r = namedPattern(b, l + 1);
    if (!r) r = unnamedPattern(b, l + 1);
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && matchStatementBody_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, MATCH_STATEMENT_BODY, r);
    return r;
  }

  // matchPatternClause+
  private static boolean matchStatementBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "matchStatementBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = matchPatternClause(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!matchPatternClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "matchStatementBody_1", c)) break;
      c = current_position_(b);
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
    int c = current_position_(b);
    while (true) {
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterTypeName_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // TypeName identifier
  public static boolean parameterWithType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterWithType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_WITH_TYPE, "<parameter with type>");
    r = TypeName(b, l + 1, -1);
    p = r; // pin = 1
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // endpointParameter | ParameterList
  public static boolean resourceParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "resourceParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESOURCE_PARAMETER_LIST, "<resource parameter list>");
    r = endpointParameter(b, l + 1);
    if (!r) r = ParameterList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // retry SEMICOLON
  public static boolean retryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "retryStatement")) return false;
    if (!nextTokenIs(b, RETRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRY_STATEMENT, null);
    r = consumeTokens(b, 1, RETRY, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // SB_DOC_INLINE_CODE_START SINGLE_BACK_TICK_INLINE_CODE? SINGLE_BACK_TICK_INLINE_CODE_END
  public static boolean singleBackTickDocInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBackTickDocInlineCode")) return false;
    if (!nextTokenIs(b, SB_DOC_INLINE_CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SINGLE_BACK_TICK_DOC_INLINE_CODE, null);
    r = consumeToken(b, SB_DOC_INLINE_CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, singleBackTickDocInlineCode_1(b, l + 1));
    r = p && consumeToken(b, SINGLE_BACK_TICK_INLINE_CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SINGLE_BACK_TICK_INLINE_CODE?
  private static boolean singleBackTickDocInlineCode_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBackTickDocInlineCode_1")) return false;
    consumeToken(b, SINGLE_BACK_TICK_INLINE_CODE);
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
  // TB_DOC_INLINE_CODE_START TRIPLE_BACK_TICK_INLINE_CODE? TRIPLE_BACK_TICK_INLINE_CODE_END
  public static boolean tripleBackTickDocInlineCode(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBackTickDocInlineCode")) return false;
    if (!nextTokenIs(b, TB_DOC_INLINE_CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRIPLE_BACK_TICK_DOC_INLINE_CODE, null);
    r = consumeToken(b, TB_DOC_INLINE_CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, tripleBackTickDocInlineCode_1(b, l + 1));
    r = p && consumeToken(b, TRIPLE_BACK_TICK_INLINE_CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TRIPLE_BACK_TICK_INLINE_CODE?
  private static boolean tripleBackTickDocInlineCode_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBackTickDocInlineCode_1")) return false;
    consumeToken(b, TRIPLE_BACK_TICK_INLINE_CODE);
    return true;
  }

  /* ********************************************************** */
  // var? LEFT_PARENTHESIS VariableReferenceList RIGHT_PARENTHESIS ASSIGN Expression SEMICOLON
  //                                 | LEFT_PARENTHESIS ParameterList RIGHT_PARENTHESIS ASSIGN Expression SEMICOLON
  public static boolean tupleDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tupleDestructuringStatement")) return false;
    if (!nextTokenIs(b, "<tuple destructuring statement>", LEFT_PARENTHESIS, VAR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TUPLE_DESTRUCTURING_STATEMENT, "<tuple destructuring statement>");
    r = tupleDestructuringStatement_0(b, l + 1);
    if (!r) r = tupleDestructuringStatement_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // var? LEFT_PARENTHESIS VariableReferenceList RIGHT_PARENTHESIS ASSIGN Expression SEMICOLON
  private static boolean tupleDestructuringStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tupleDestructuringStatement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tupleDestructuringStatement_0_0(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && VariableReferenceList(b, l + 1);
    r = r && consumeTokens(b, 0, RIGHT_PARENTHESIS, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // var?
  private static boolean tupleDestructuringStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tupleDestructuringStatement_0_0")) return false;
    consumeToken(b, VAR);
    return true;
  }

  // LEFT_PARENTHESIS ParameterList RIGHT_PARENTHESIS ASSIGN Expression SEMICOLON
  private static boolean tupleDestructuringStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tupleDestructuringStatement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && ParameterList(b, l + 1);
    r = r && consumeTokens(b, 0, RIGHT_PARENTHESIS, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
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
  // var VariableReference ASSIGN Expression SEMICOLON
  static boolean withVar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withVar")) return false;
    if (!nextTokenIs(b, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, VAR);
    p = r; // pin = 1
    r = r && report_error_(b, VariableReference(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, ASSIGN)) && r;
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // VariableReference ASSIGN Expression SEMICOLON
  static boolean withoutVar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withoutVar")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = VariableReference(b, l + 1, -1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Expression root: Expression
  // Operator priority table:
  // 0: ATOM(LambdaFunctionExpression)
  // 1: ATOM(SimpleLiteralExpression)
  // 2: ATOM(StringTemplateLiteralExpression)
  // 3: ATOM(XmlLiteralExpression)
  // 4: ATOM(TableLiteralExpression)
  // 5: ATOM(RecordLiteralExpression)
  // 6: ATOM(BracedOrTupleExpression)
  // 7: BINARY(TernaryExpression)
  // 8: ATOM(ArrayLiteralExpression)
  // 9: ATOM(ValueTypeTypeExpression)
  // 10: ATOM(BuiltInReferenceTypeTypeExpression)
  // 11: ATOM(ActionInvocationExpression)
  // 12: ATOM(VariableReferenceExpression)
  // 13: ATOM(TypeInitExpression)
  // 14: ATOM(TypeConversionExpression)
  // 15: ATOM(UnaryExpression)
  // 16: BINARY(BinaryPowExpression)
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
  // 28: ATOM(typeAccessExpression)
  public static boolean Expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = LambdaFunctionExpression(b, l + 1);
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
    if (!r) r = typeAccessExpression(b, l + 1);
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
      if (g < 7 && consumeTokenSmart(b, QUESTION_MARK)) {
        r = report_error_(b, Expression(b, l, 7));
        r = TernaryExpression_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_EXPRESSION, r, true, null);
      }
      else if (g < 16 && consumeTokenSmart(b, POW)) {
        r = Expression(b, l, 16);
        exit_section_(b, l, m, BINARY_POW_EXPRESSION, r, true, null);
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
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = LambdaFunction(b, l + 1);
    exit_section_(b, m, LAMBDA_FUNCTION_EXPRESSION, r);
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
    int c = current_position_(b);
    while (true) {
      if (!BracedOrTupleExpression_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BracedOrTupleExpression_2", c)) break;
      c = current_position_(b);
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

  // TypeInitExpr
  public static boolean TypeInitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpression")) return false;
    if (!nextTokenIsSmart(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeInitExpr(b, l + 1);
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

  // (ADD | SUB | NOT | lengthof | untaint) Expression
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

  // ADD | SUB | NOT | lengthof | untaint
  private static boolean UnaryExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnaryExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    if (!r) r = consumeTokenSmart(b, NOT);
    if (!r) r = consumeTokenSmart(b, LENGTHOF);
    if (!r) r = consumeTokenSmart(b, UNTAINT);
    exit_section_(b, m, null, r);
    return r;
  }

  // DIV | MUL | MOD
  private static boolean BinaryDivMulModExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryDivMulModExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, MUL);
    if (!r) r = consumeTokenSmart(b, MOD);
    exit_section_(b, m, null, r);
    return r;
  }

  // ADD | SUB
  private static boolean BinaryAddSubExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryAddSubExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT_EQUAL | GT_EQUAL | GT | LT
  private static boolean BinaryCompareExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryCompareExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LT_EQUAL);
    if (!r) r = consumeTokenSmart(b, GT_EQUAL);
    if (!r) r = consumeTokenSmart(b, GT);
    if (!r) r = consumeTokenSmart(b, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQUAL | NOT_EQUAL
  private static boolean BinaryEqualExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryEqualExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, EQUAL);
    if (!r) r = consumeTokenSmart(b, NOT_EQUAL);
    exit_section_(b, m, null, r);
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

  // TypeName
  public static boolean typeAccessExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeAccessExpression")) return false;
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
    int c = current_position_(b);
    while (true) {
      if (!TupleTypeName_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleTypeName_2", c)) break;
      c = current_position_(b);
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

  // (LEFT_BRACKET RIGHT_BRACKET)+
  private static boolean ArrayTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrayTypeName_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!ArrayTypeName_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrayTypeName_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACKET RIGHT_BRACKET
  private static boolean ArrayTypeName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, LEFT_BRACKET, RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // object LEFT_BRACE ObjectBody RIGHT_BRACE
  public static boolean ObjectTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName")) return false;
    if (!nextTokenIsSmart(b, OBJECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_TYPE_NAME, null);
    r = consumeTokensSmart(b, 1, OBJECT, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ObjectBody(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE FieldDefinitionList RIGHT_BRACE
  public static boolean RecordTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordTypeName")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LEFT_BRACE);
    r = r && FieldDefinitionList(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, RECORD_TYPE_NAME, r);
    return r;
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
}
