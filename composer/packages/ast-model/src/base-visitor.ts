// This is an auto-generated file. Do not edit.
// Run 'BALLERINA_HOME="your/ballerina/home" npm run gen-ast-utils' to generate.
import * as Ballerina from "./ast-interfaces";

export interface Visitor {
  beginVisitASTNode?(node: Ballerina.ASTNode, parent?: Ballerina.ASTNode): void;
  endVisitASTNode?(node: Ballerina.ASTNode, parent?: Ballerina.ASTNode): void;

  beginVisitAbort?(node: Ballerina.Abort, parent?: Ballerina.ASTNode): void;
  endVisitAbort?(node: Ballerina.Abort, parent?: Ballerina.ASTNode): void;

  beginVisitAnnotAccessExpression?(
    node: Ballerina.AnnotAccessExpression,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitAnnotAccessExpression?(
    node: Ballerina.AnnotAccessExpression,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitAnnotation?(
    node: Ballerina.Annotation,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitAnnotation?(
    node: Ballerina.Annotation,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitAnnotationAttachment?(
    node: Ballerina.AnnotationAttachment,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitAnnotationAttachment?(
    node: Ballerina.AnnotationAttachment,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitArrayType?(
    node: Ballerina.ArrayType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitArrayType?(
    node: Ballerina.ArrayType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitArrowExpr?(
    node: Ballerina.ArrowExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitArrowExpr?(
    node: Ballerina.ArrowExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitAssignment?(
    node: Ballerina.Assignment,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitAssignment?(
    node: Ballerina.Assignment,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitBinaryExpr?(
    node: Ballerina.BinaryExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitBinaryExpr?(
    node: Ballerina.BinaryExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitBlock?(node: Ballerina.Block, parent?: Ballerina.ASTNode): void;
  endVisitBlock?(node: Ballerina.Block, parent?: Ballerina.ASTNode): void;

  beginVisitBlockFunctionBody?(
    node: Ballerina.BlockFunctionBody,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitBlockFunctionBody?(
    node: Ballerina.BlockFunctionBody,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitBreak?(node: Ballerina.Break, parent?: Ballerina.ASTNode): void;
  endVisitBreak?(node: Ballerina.Break, parent?: Ballerina.ASTNode): void;

  beginVisitBuiltInRefType?(
    node: Ballerina.BuiltInRefType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitBuiltInRefType?(
    node: Ballerina.BuiltInRefType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitCheckExpr?(
    node: Ballerina.CheckExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitCheckExpr?(
    node: Ballerina.CheckExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitCheckPanicExpr?(
    node: Ballerina.CheckPanicExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitCheckPanicExpr?(
    node: Ballerina.CheckPanicExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitCompilationUnit?(
    node: Ballerina.CompilationUnit,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitCompilationUnit?(
    node: Ballerina.CompilationUnit,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitCompoundAssignment?(
    node: Ballerina.CompoundAssignment,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitCompoundAssignment?(
    node: Ballerina.CompoundAssignment,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitConstant?(
    node: Ballerina.Constant,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitConstant?(node: Ballerina.Constant, parent?: Ballerina.ASTNode): void;

  beginVisitConstantRef?(
    node: Ballerina.ConstantRef,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitConstantRef?(
    node: Ballerina.ConstantRef,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitConstrainedType?(
    node: Ballerina.ConstrainedType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitConstrainedType?(
    node: Ballerina.ConstrainedType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitDocumentationDescription?(
    node: Ballerina.DocumentationDescription,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitDocumentationDescription?(
    node: Ballerina.DocumentationDescription,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitDocumentationParameter?(
    node: Ballerina.DocumentationParameter,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitDocumentationParameter?(
    node: Ballerina.DocumentationParameter,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitDocumentationReference?(
    node: Ballerina.DocumentationReference,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitDocumentationReference?(
    node: Ballerina.DocumentationReference,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitElvisExpr?(
    node: Ballerina.ElvisExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitElvisExpr?(
    node: Ballerina.ElvisExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitErrorDestructure?(
    node: Ballerina.ErrorDestructure,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitErrorDestructure?(
    node: Ballerina.ErrorDestructure,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitErrorType?(
    node: Ballerina.ErrorType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitErrorType?(
    node: Ballerina.ErrorType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitErrorVariable?(
    node: Ballerina.ErrorVariable,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitErrorVariable?(
    node: Ballerina.ErrorVariable,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitErrorVariableRef?(
    node: Ballerina.ErrorVariableRef,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitErrorVariableRef?(
    node: Ballerina.ErrorVariableRef,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitExprFunctionBody?(
    node: Ballerina.ExprFunctionBody,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitExprFunctionBody?(
    node: Ballerina.ExprFunctionBody,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitExpressionStatement?(
    node: Ballerina.ExpressionStatement,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitExpressionStatement?(
    node: Ballerina.ExpressionStatement,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitExternFunctionBody?(
    node: Ballerina.ExternFunctionBody,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitExternFunctionBody?(
    node: Ballerina.ExternFunctionBody,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitFieldBasedAccessExpr?(
    node: Ballerina.FieldBasedAccessExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitFieldBasedAccessExpr?(
    node: Ballerina.FieldBasedAccessExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitFiniteTypeNode?(
    node: Ballerina.FiniteTypeNode,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitFiniteTypeNode?(
    node: Ballerina.FiniteTypeNode,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitForeach?(node: Ballerina.Foreach, parent?: Ballerina.ASTNode): void;
  endVisitForeach?(node: Ballerina.Foreach, parent?: Ballerina.ASTNode): void;

  beginVisitForkJoin?(
    node: Ballerina.ForkJoin,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitForkJoin?(node: Ballerina.ForkJoin, parent?: Ballerina.ASTNode): void;

  beginVisitFrom?(node: Ballerina.From, parent?: Ballerina.ASTNode): void;
  endVisitFrom?(node: Ballerina.From, parent?: Ballerina.ASTNode): void;

  beginVisitFunction?(
    node: Ballerina.Function,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitFunction?(node: Ballerina.Function, parent?: Ballerina.ASTNode): void;

  beginVisitFunctionType?(
    node: Ballerina.FunctionType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitFunctionType?(
    node: Ballerina.FunctionType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitGroupExpr?(
    node: Ballerina.GroupExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitGroupExpr?(
    node: Ballerina.GroupExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitIdentifier?(
    node: Ballerina.Identifier,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitIdentifier?(
    node: Ballerina.Identifier,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitIf?(node: Ballerina.If, parent?: Ballerina.ASTNode): void;
  endVisitIf?(node: Ballerina.If, parent?: Ballerina.ASTNode): void;

  beginVisitImport?(node: Ballerina.Import, parent?: Ballerina.ASTNode): void;
  endVisitImport?(node: Ballerina.Import, parent?: Ballerina.ASTNode): void;

  beginVisitIndexBasedAccessExpr?(
    node: Ballerina.IndexBasedAccessExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitIndexBasedAccessExpr?(
    node: Ballerina.IndexBasedAccessExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitInvocation?(
    node: Ballerina.Invocation,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitInvocation?(
    node: Ballerina.Invocation,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitLambda?(node: Ballerina.Lambda, parent?: Ballerina.ASTNode): void;
  endVisitLambda?(node: Ballerina.Lambda, parent?: Ballerina.ASTNode): void;

  beginVisitListConstructorExpr?(
    node: Ballerina.ListConstructorExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitListConstructorExpr?(
    node: Ballerina.ListConstructorExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitLiteral?(node: Ballerina.Literal, parent?: Ballerina.ASTNode): void;
  endVisitLiteral?(node: Ballerina.Literal, parent?: Ballerina.ASTNode): void;

  beginVisitLock?(node: Ballerina.Lock, parent?: Ballerina.ASTNode): void;
  endVisitLock?(node: Ballerina.Lock, parent?: Ballerina.ASTNode): void;

  beginVisitMarkdownDocumentation?(
    node: Ballerina.MarkdownDocumentation,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitMarkdownDocumentation?(
    node: Ballerina.MarkdownDocumentation,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitMatch?(node: Ballerina.Match, parent?: Ballerina.ASTNode): void;
  endVisitMatch?(node: Ballerina.Match, parent?: Ballerina.ASTNode): void;

  beginVisitMatchStaticPatternClause?(
    node: Ballerina.MatchStaticPatternClause,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitMatchStaticPatternClause?(
    node: Ballerina.MatchStaticPatternClause,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitMatchStructuredPatternClause?(
    node: Ballerina.MatchStructuredPatternClause,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitMatchStructuredPatternClause?(
    node: Ballerina.MatchStructuredPatternClause,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitNamedArgsExpr?(
    node: Ballerina.NamedArgsExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitNamedArgsExpr?(
    node: Ballerina.NamedArgsExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitNext?(node: Ballerina.Next, parent?: Ballerina.ASTNode): void;
  endVisitNext?(node: Ballerina.Next, parent?: Ballerina.ASTNode): void;

  beginVisitNumericLiteral?(
    node: Ballerina.NumericLiteral,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitNumericLiteral?(
    node: Ballerina.NumericLiteral,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitObjectType?(
    node: Ballerina.ObjectType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitObjectType?(
    node: Ballerina.ObjectType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitPanic?(node: Ballerina.Panic, parent?: Ballerina.ASTNode): void;
  endVisitPanic?(node: Ballerina.Panic, parent?: Ballerina.ASTNode): void;

  beginVisitQueryExpr?(
    node: Ballerina.QueryExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitQueryExpr?(
    node: Ballerina.QueryExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRecordDestructure?(
    node: Ballerina.RecordDestructure,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRecordDestructure?(
    node: Ballerina.RecordDestructure,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRecordLiteralExpr?(
    node: Ballerina.RecordLiteralExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRecordLiteralExpr?(
    node: Ballerina.RecordLiteralExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRecordLiteralKeyValue?(
    node: Ballerina.RecordLiteralKeyValue,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRecordLiteralKeyValue?(
    node: Ballerina.RecordLiteralKeyValue,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRecordType?(
    node: Ballerina.RecordType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRecordType?(
    node: Ballerina.RecordType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRecordVariable?(
    node: Ballerina.RecordVariable,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRecordVariable?(
    node: Ballerina.RecordVariable,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRecordVariableRef?(
    node: Ballerina.RecordVariableRef,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRecordVariableRef?(
    node: Ballerina.RecordVariableRef,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRestArgsExpr?(
    node: Ballerina.RestArgsExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitRestArgsExpr?(
    node: Ballerina.RestArgsExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitRetry?(node: Ballerina.Retry, parent?: Ballerina.ASTNode): void;
  endVisitRetry?(node: Ballerina.Retry, parent?: Ballerina.ASTNode): void;

  beginVisitReturn?(node: Ballerina.Return, parent?: Ballerina.ASTNode): void;
  endVisitReturn?(node: Ballerina.Return, parent?: Ballerina.ASTNode): void;

  beginVisitSelect?(node: Ballerina.Select, parent?: Ballerina.ASTNode): void;
  endVisitSelect?(node: Ballerina.Select, parent?: Ballerina.ASTNode): void;

  beginVisitService?(node: Ballerina.Service, parent?: Ballerina.ASTNode): void;
  endVisitService?(node: Ballerina.Service, parent?: Ballerina.ASTNode): void;

  beginVisitServiceConstructor?(
    node: Ballerina.ServiceConstructor,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitServiceConstructor?(
    node: Ballerina.ServiceConstructor,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitSimpleVariableRef?(
    node: Ballerina.SimpleVariableRef,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitSimpleVariableRef?(
    node: Ballerina.SimpleVariableRef,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitStringTemplateLiteral?(
    node: Ballerina.StringTemplateLiteral,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitStringTemplateLiteral?(
    node: Ballerina.StringTemplateLiteral,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTable?(node: Ballerina.Table, parent?: Ballerina.ASTNode): void;
  endVisitTable?(node: Ballerina.Table, parent?: Ballerina.ASTNode): void;

  beginVisitTableColumn?(
    node: Ballerina.TableColumn,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTableColumn?(
    node: Ballerina.TableColumn,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTernaryExpr?(
    node: Ballerina.TernaryExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTernaryExpr?(
    node: Ballerina.TernaryExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTransaction?(
    node: Ballerina.Transaction,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTransaction?(
    node: Ballerina.Transaction,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTrapExpr?(
    node: Ballerina.TrapExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTrapExpr?(node: Ballerina.TrapExpr, parent?: Ballerina.ASTNode): void;

  beginVisitTupleDestructure?(
    node: Ballerina.TupleDestructure,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTupleDestructure?(
    node: Ballerina.TupleDestructure,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTupleTypeNode?(
    node: Ballerina.TupleTypeNode,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTupleTypeNode?(
    node: Ballerina.TupleTypeNode,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTupleVariable?(
    node: Ballerina.TupleVariable,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTupleVariable?(
    node: Ballerina.TupleVariable,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTupleVariableRef?(
    node: Ballerina.TupleVariableRef,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTupleVariableRef?(
    node: Ballerina.TupleVariableRef,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTypeConversionExpr?(
    node: Ballerina.TypeConversionExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTypeConversionExpr?(
    node: Ballerina.TypeConversionExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTypeDefinition?(
    node: Ballerina.TypeDefinition,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTypeDefinition?(
    node: Ballerina.TypeDefinition,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTypeInitExpr?(
    node: Ballerina.TypeInitExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTypeInitExpr?(
    node: Ballerina.TypeInitExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTypeTestExpr?(
    node: Ballerina.TypeTestExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTypeTestExpr?(
    node: Ballerina.TypeTestExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitTypedescExpression?(
    node: Ballerina.TypedescExpression,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitTypedescExpression?(
    node: Ballerina.TypedescExpression,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitUnaryExpr?(
    node: Ballerina.UnaryExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitUnaryExpr?(
    node: Ballerina.UnaryExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitUnionTypeNode?(
    node: Ballerina.UnionTypeNode,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitUnionTypeNode?(
    node: Ballerina.UnionTypeNode,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitUserDefinedType?(
    node: Ballerina.UserDefinedType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitUserDefinedType?(
    node: Ballerina.UserDefinedType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitValueType?(
    node: Ballerina.ValueType,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitValueType?(
    node: Ballerina.ValueType,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitVariable?(
    node: Ballerina.Variable,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitVariable?(node: Ballerina.Variable, parent?: Ballerina.ASTNode): void;

  beginVisitVariableDef?(
    node: Ballerina.VariableDef,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitVariableDef?(
    node: Ballerina.VariableDef,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitVisibleEndpoint?(
    node: Ballerina.VisibleEndpoint,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitVisibleEndpoint?(
    node: Ballerina.VisibleEndpoint,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitWaitExpr?(
    node: Ballerina.WaitExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitWaitExpr?(node: Ballerina.WaitExpr, parent?: Ballerina.ASTNode): void;

  beginVisitWaitLiteralKeyValue?(
    node: Ballerina.WaitLiteralKeyValue,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitWaitLiteralKeyValue?(
    node: Ballerina.WaitLiteralKeyValue,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitWhile?(node: Ballerina.While, parent?: Ballerina.ASTNode): void;
  endVisitWhile?(node: Ballerina.While, parent?: Ballerina.ASTNode): void;

  beginVisitWorkerFlush?(
    node: Ballerina.WorkerFlush,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitWorkerFlush?(
    node: Ballerina.WorkerFlush,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitWorkerReceive?(
    node: Ballerina.WorkerReceive,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitWorkerReceive?(
    node: Ballerina.WorkerReceive,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitWorkerSend?(
    node: Ballerina.WorkerSend,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitWorkerSend?(
    node: Ballerina.WorkerSend,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitWorkerSyncSend?(
    node: Ballerina.WorkerSyncSend,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitWorkerSyncSend?(
    node: Ballerina.WorkerSyncSend,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlAttribute?(
    node: Ballerina.XmlAttribute,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlAttribute?(
    node: Ballerina.XmlAttribute,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlAttributeAccessExpr?(
    node: Ballerina.XmlAttributeAccessExpr,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlAttributeAccessExpr?(
    node: Ballerina.XmlAttributeAccessExpr,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlCommentLiteral?(
    node: Ballerina.XmlCommentLiteral,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlCommentLiteral?(
    node: Ballerina.XmlCommentLiteral,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlElementLiteral?(
    node: Ballerina.XmlElementLiteral,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlElementLiteral?(
    node: Ballerina.XmlElementLiteral,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlPiLiteral?(
    node: Ballerina.XmlPiLiteral,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlPiLiteral?(
    node: Ballerina.XmlPiLiteral,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlQname?(
    node: Ballerina.XmlQname,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlQname?(node: Ballerina.XmlQname, parent?: Ballerina.ASTNode): void;

  beginVisitXmlQuotedString?(
    node: Ballerina.XmlQuotedString,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlQuotedString?(
    node: Ballerina.XmlQuotedString,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlTextLiteral?(
    node: Ballerina.XmlTextLiteral,
    parent?: Ballerina.ASTNode
  ): void;
  endVisitXmlTextLiteral?(
    node: Ballerina.XmlTextLiteral,
    parent?: Ballerina.ASTNode
  ): void;

  beginVisitXmlns?(node: Ballerina.Xmlns, parent?: Ballerina.ASTNode): void;
  endVisitXmlns?(node: Ballerina.Xmlns, parent?: Ballerina.ASTNode): void;
}
