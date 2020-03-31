// This is an auto-generated file. Do not edit.
// Run 'BALLERINA_HOME="your/ballerina/home" npm run gen-ast-utils' to generate.
import * as Ballerina from "./ast-interfaces";

export class ASTKindChecker {
  public static isAbort(node: Ballerina.ASTNode): node is Ballerina.Abort {
    return node.kind === "Abort";
  }

  public static isAnnotAccessExpression(
    node: Ballerina.ASTNode
  ): node is Ballerina.AnnotAccessExpression {
    return node.kind === "AnnotAccessExpression";
  }

  public static isAnnotation(
    node: Ballerina.ASTNode
  ): node is Ballerina.Annotation {
    return node.kind === "Annotation";
  }

  public static isAnnotationAttachment(
    node: Ballerina.ASTNode
  ): node is Ballerina.AnnotationAttachment {
    return node.kind === "AnnotationAttachment";
  }

  public static isArrayType(
    node: Ballerina.ASTNode
  ): node is Ballerina.ArrayType {
    return node.kind === "ArrayType";
  }

  public static isArrowExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.ArrowExpr {
    return node.kind === "ArrowExpr";
  }

  public static isAssignment(
    node: Ballerina.ASTNode
  ): node is Ballerina.Assignment {
    return node.kind === "Assignment";
  }

  public static isBinaryExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.BinaryExpr {
    return node.kind === "BinaryExpr";
  }

  public static isBlock(node: Ballerina.ASTNode): node is Ballerina.Block {
    return node.kind === "Block";
  }

  public static isBlockFunctionBody(
    node: Ballerina.ASTNode
  ): node is Ballerina.BlockFunctionBody {
    return node.kind === "BlockFunctionBody";
  }

  public static isBreak(node: Ballerina.ASTNode): node is Ballerina.Break {
    return node.kind === "Break";
  }

  public static isBuiltInRefType(
    node: Ballerina.ASTNode
  ): node is Ballerina.BuiltInRefType {
    return node.kind === "BuiltInRefType";
  }

  public static isCheckExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.CheckExpr {
    return node.kind === "CheckExpr";
  }

  public static isCheckPanicExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.CheckPanicExpr {
    return node.kind === "CheckPanicExpr";
  }

  public static isCompilationUnit(
    node: Ballerina.ASTNode
  ): node is Ballerina.CompilationUnit {
    return node.kind === "CompilationUnit";
  }

  public static isCompoundAssignment(
    node: Ballerina.ASTNode
  ): node is Ballerina.CompoundAssignment {
    return node.kind === "CompoundAssignment";
  }

  public static isConstant(
    node: Ballerina.ASTNode
  ): node is Ballerina.Constant {
    return node.kind === "Constant";
  }

  public static isConstantRef(
    node: Ballerina.ASTNode
  ): node is Ballerina.ConstantRef {
    return node.kind === "ConstantRef";
  }

  public static isConstrainedType(
    node: Ballerina.ASTNode
  ): node is Ballerina.ConstrainedType {
    return node.kind === "ConstrainedType";
  }

  public static isDocumentationDescription(
    node: Ballerina.ASTNode
  ): node is Ballerina.DocumentationDescription {
    return node.kind === "DocumentationDescription";
  }

  public static isDocumentationParameter(
    node: Ballerina.ASTNode
  ): node is Ballerina.DocumentationParameter {
    return node.kind === "DocumentationParameter";
  }

  public static isDocumentationReference(
    node: Ballerina.ASTNode
  ): node is Ballerina.DocumentationReference {
    return node.kind === "DocumentationReference";
  }

  public static isElvisExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.ElvisExpr {
    return node.kind === "ElvisExpr";
  }

  public static isErrorDestructure(
    node: Ballerina.ASTNode
  ): node is Ballerina.ErrorDestructure {
    return node.kind === "ErrorDestructure";
  }

  public static isErrorType(
    node: Ballerina.ASTNode
  ): node is Ballerina.ErrorType {
    return node.kind === "ErrorType";
  }

  public static isErrorVariable(
    node: Ballerina.ASTNode
  ): node is Ballerina.ErrorVariable {
    return node.kind === "ErrorVariable";
  }

  public static isErrorVariableRef(
    node: Ballerina.ASTNode
  ): node is Ballerina.ErrorVariableRef {
    return node.kind === "ErrorVariableRef";
  }

  public static isExprFunctionBody(
    node: Ballerina.ASTNode
  ): node is Ballerina.ExprFunctionBody {
    return node.kind === "ExprFunctionBody";
  }

  public static isExpressionStatement(
    node: Ballerina.ASTNode
  ): node is Ballerina.ExpressionStatement {
    return node.kind === "ExpressionStatement";
  }

  public static isExternFunctionBody(
    node: Ballerina.ASTNode
  ): node is Ballerina.ExternFunctionBody {
    return node.kind === "ExternFunctionBody";
  }

  public static isFieldBasedAccessExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.FieldBasedAccessExpr {
    return node.kind === "FieldBasedAccessExpr";
  }

  public static isFiniteTypeNode(
    node: Ballerina.ASTNode
  ): node is Ballerina.FiniteTypeNode {
    return node.kind === "FiniteTypeNode";
  }

  public static isForeach(node: Ballerina.ASTNode): node is Ballerina.Foreach {
    return node.kind === "Foreach";
  }

  public static isForkJoin(
    node: Ballerina.ASTNode
  ): node is Ballerina.ForkJoin {
    return node.kind === "ForkJoin";
  }

  public static isFrom(node: Ballerina.ASTNode): node is Ballerina.From {
    return node.kind === "From";
  }

  public static isFunction(
    node: Ballerina.ASTNode
  ): node is Ballerina.Function {
    return node.kind === "Function";
  }

  public static isFunctionType(
    node: Ballerina.ASTNode
  ): node is Ballerina.FunctionType {
    return node.kind === "FunctionType";
  }

  public static isGroupExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.GroupExpr {
    return node.kind === "GroupExpr";
  }

  public static isIdentifier(
    node: Ballerina.ASTNode
  ): node is Ballerina.Identifier {
    return node.kind === "Identifier";
  }

  public static isIf(node: Ballerina.ASTNode): node is Ballerina.If {
    return node.kind === "If";
  }

  public static isImport(node: Ballerina.ASTNode): node is Ballerina.Import {
    return node.kind === "Import";
  }

  public static isIndexBasedAccessExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.IndexBasedAccessExpr {
    return node.kind === "IndexBasedAccessExpr";
  }

  public static isInvocation(
    node: Ballerina.ASTNode
  ): node is Ballerina.Invocation {
    return node.kind === "Invocation";
  }

  public static isLambda(node: Ballerina.ASTNode): node is Ballerina.Lambda {
    return node.kind === "Lambda";
  }

  public static isListConstructorExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.ListConstructorExpr {
    return node.kind === "ListConstructorExpr";
  }

  public static isLiteral(node: Ballerina.ASTNode): node is Ballerina.Literal {
    return node.kind === "Literal";
  }

  public static isLock(node: Ballerina.ASTNode): node is Ballerina.Lock {
    return node.kind === "Lock";
  }

  public static isMarkdownDocumentation(
    node: Ballerina.ASTNode
  ): node is Ballerina.MarkdownDocumentation {
    return node.kind === "MarkdownDocumentation";
  }

  public static isMatch(node: Ballerina.ASTNode): node is Ballerina.Match {
    return node.kind === "Match";
  }

  public static isMatchStaticPatternClause(
    node: Ballerina.ASTNode
  ): node is Ballerina.MatchStaticPatternClause {
    return node.kind === "MatchStaticPatternClause";
  }

  public static isMatchStructuredPatternClause(
    node: Ballerina.ASTNode
  ): node is Ballerina.MatchStructuredPatternClause {
    return node.kind === "MatchStructuredPatternClause";
  }

  public static isNamedArgsExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.NamedArgsExpr {
    return node.kind === "NamedArgsExpr";
  }

  public static isNext(node: Ballerina.ASTNode): node is Ballerina.Next {
    return node.kind === "Next";
  }

  public static isNumericLiteral(
    node: Ballerina.ASTNode
  ): node is Ballerina.NumericLiteral {
    return node.kind === "NumericLiteral";
  }

  public static isObjectType(
    node: Ballerina.ASTNode
  ): node is Ballerina.ObjectType {
    return node.kind === "ObjectType";
  }

  public static isPanic(node: Ballerina.ASTNode): node is Ballerina.Panic {
    return node.kind === "Panic";
  }

  public static isQueryExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.QueryExpr {
    return node.kind === "QueryExpr";
  }

  public static isRecordDestructure(
    node: Ballerina.ASTNode
  ): node is Ballerina.RecordDestructure {
    return node.kind === "RecordDestructure";
  }

  public static isRecordLiteralExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.RecordLiteralExpr {
    return node.kind === "RecordLiteralExpr";
  }

  public static isRecordLiteralKeyValue(
    node: Ballerina.ASTNode
  ): node is Ballerina.RecordLiteralKeyValue {
    return node.kind === "RecordLiteralKeyValue";
  }

  public static isRecordType(
    node: Ballerina.ASTNode
  ): node is Ballerina.RecordType {
    return node.kind === "RecordType";
  }

  public static isRecordVariable(
    node: Ballerina.ASTNode
  ): node is Ballerina.RecordVariable {
    return node.kind === "RecordVariable";
  }

  public static isRecordVariableRef(
    node: Ballerina.ASTNode
  ): node is Ballerina.RecordVariableRef {
    return node.kind === "RecordVariableRef";
  }

  public static isRestArgsExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.RestArgsExpr {
    return node.kind === "RestArgsExpr";
  }

  public static isRetry(node: Ballerina.ASTNode): node is Ballerina.Retry {
    return node.kind === "Retry";
  }

  public static isReturn(node: Ballerina.ASTNode): node is Ballerina.Return {
    return node.kind === "Return";
  }

  public static isSelect(node: Ballerina.ASTNode): node is Ballerina.Select {
    return node.kind === "Select";
  }

  public static isService(node: Ballerina.ASTNode): node is Ballerina.Service {
    return node.kind === "Service";
  }

  public static isServiceConstructor(
    node: Ballerina.ASTNode
  ): node is Ballerina.ServiceConstructor {
    return node.kind === "ServiceConstructor";
  }

  public static isSimpleVariableRef(
    node: Ballerina.ASTNode
  ): node is Ballerina.SimpleVariableRef {
    return node.kind === "SimpleVariableRef";
  }

  public static isStringTemplateLiteral(
    node: Ballerina.ASTNode
  ): node is Ballerina.StringTemplateLiteral {
    return node.kind === "StringTemplateLiteral";
  }

  public static isTable(node: Ballerina.ASTNode): node is Ballerina.Table {
    return node.kind === "Table";
  }

  public static isTableColumn(
    node: Ballerina.ASTNode
  ): node is Ballerina.TableColumn {
    return node.kind === "TableColumn";
  }

  public static isTernaryExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.TernaryExpr {
    return node.kind === "TernaryExpr";
  }

  public static isTransaction(
    node: Ballerina.ASTNode
  ): node is Ballerina.Transaction {
    return node.kind === "Transaction";
  }

  public static isTrapExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.TrapExpr {
    return node.kind === "TrapExpr";
  }

  public static isTupleDestructure(
    node: Ballerina.ASTNode
  ): node is Ballerina.TupleDestructure {
    return node.kind === "TupleDestructure";
  }

  public static isTupleTypeNode(
    node: Ballerina.ASTNode
  ): node is Ballerina.TupleTypeNode {
    return node.kind === "TupleTypeNode";
  }

  public static isTupleVariable(
    node: Ballerina.ASTNode
  ): node is Ballerina.TupleVariable {
    return node.kind === "TupleVariable";
  }

  public static isTupleVariableRef(
    node: Ballerina.ASTNode
  ): node is Ballerina.TupleVariableRef {
    return node.kind === "TupleVariableRef";
  }

  public static isTypeConversionExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.TypeConversionExpr {
    return node.kind === "TypeConversionExpr";
  }

  public static isTypeDefinition(
    node: Ballerina.ASTNode
  ): node is Ballerina.TypeDefinition {
    return node.kind === "TypeDefinition";
  }

  public static isTypeInitExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.TypeInitExpr {
    return node.kind === "TypeInitExpr";
  }

  public static isTypeTestExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.TypeTestExpr {
    return node.kind === "TypeTestExpr";
  }

  public static isTypedescExpression(
    node: Ballerina.ASTNode
  ): node is Ballerina.TypedescExpression {
    return node.kind === "TypedescExpression";
  }

  public static isUnaryExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.UnaryExpr {
    return node.kind === "UnaryExpr";
  }

  public static isUnionTypeNode(
    node: Ballerina.ASTNode
  ): node is Ballerina.UnionTypeNode {
    return node.kind === "UnionTypeNode";
  }

  public static isUserDefinedType(
    node: Ballerina.ASTNode
  ): node is Ballerina.UserDefinedType {
    return node.kind === "UserDefinedType";
  }

  public static isValueType(
    node: Ballerina.ASTNode
  ): node is Ballerina.ValueType {
    return node.kind === "ValueType";
  }

  public static isVariable(
    node: Ballerina.ASTNode
  ): node is Ballerina.Variable {
    return node.kind === "Variable";
  }

  public static isVariableDef(
    node: Ballerina.ASTNode
  ): node is Ballerina.VariableDef {
    return node.kind === "VariableDef";
  }

  public static isVisibleEndpoint(
    node: Ballerina.ASTNode
  ): node is Ballerina.VisibleEndpoint {
    return node.kind === "VisibleEndpoint";
  }

  public static isWaitExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.WaitExpr {
    return node.kind === "WaitExpr";
  }

  public static isWaitLiteralKeyValue(
    node: Ballerina.ASTNode
  ): node is Ballerina.WaitLiteralKeyValue {
    return node.kind === "WaitLiteralKeyValue";
  }

  public static isWhile(node: Ballerina.ASTNode): node is Ballerina.While {
    return node.kind === "While";
  }

  public static isWorkerFlush(
    node: Ballerina.ASTNode
  ): node is Ballerina.WorkerFlush {
    return node.kind === "WorkerFlush";
  }

  public static isWorkerReceive(
    node: Ballerina.ASTNode
  ): node is Ballerina.WorkerReceive {
    return node.kind === "WorkerReceive";
  }

  public static isWorkerSend(
    node: Ballerina.ASTNode
  ): node is Ballerina.WorkerSend {
    return node.kind === "WorkerSend";
  }

  public static isWorkerSyncSend(
    node: Ballerina.ASTNode
  ): node is Ballerina.WorkerSyncSend {
    return node.kind === "WorkerSyncSend";
  }

  public static isXmlAttribute(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlAttribute {
    return node.kind === "XmlAttribute";
  }

  public static isXmlAttributeAccessExpr(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlAttributeAccessExpr {
    return node.kind === "XmlAttributeAccessExpr";
  }

  public static isXmlCommentLiteral(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlCommentLiteral {
    return node.kind === "XmlCommentLiteral";
  }

  public static isXmlElementLiteral(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlElementLiteral {
    return node.kind === "XmlElementLiteral";
  }

  public static isXmlPiLiteral(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlPiLiteral {
    return node.kind === "XmlPiLiteral";
  }

  public static isXmlQname(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlQname {
    return node.kind === "XmlQname";
  }

  public static isXmlQuotedString(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlQuotedString {
    return node.kind === "XmlQuotedString";
  }

  public static isXmlTextLiteral(
    node: Ballerina.ASTNode
  ): node is Ballerina.XmlTextLiteral {
    return node.kind === "XmlTextLiteral";
  }

  public static isXmlns(node: Ballerina.ASTNode): node is Ballerina.Xmlns {
    return node.kind === "Xmlns";
  }
}
