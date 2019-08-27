// This is an auto-generated file. Do not edit.
// Run 'BALLERINA_HOME="your/ballerina/home" npm run gen-ast-utils' to generate.
// tslint:disable:ban-types
export interface NodePosition {
  endColumn: number;
  endLine: number;
  startColumn: number;
  startLine: number;
}
export interface ASTNode {
  id: string;
  kind: string;
  viewState?: any;
  ws?: any[];
  position?: NodePosition;
  parent?: ASTNode;
}

export interface Abort extends ASTNode {
  ws: any[];
}

export interface AnnotAccessExpression extends ASTNode {
  annotationName: Identifier;
  expression: SimpleVariableRef;
  isExpression?: boolean | string;
  packageAlias: Identifier;
  symbolType: string[];
  ws: any[];
}

export interface Annotation extends ASTNode {
  abstract: boolean;
  annotationAttachments: AnnotationAttachment[];
  attached: boolean;
  attachmentPoints: string[];
  client: boolean;
  compensate: boolean;
  constant: boolean;
  final: boolean;
  function_final: boolean;
  interface: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  native: boolean;
  noAttachmentPoints?: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  service: boolean;
  testable: boolean;
  typeNode?:
    | ArrayType
    | ConstrainedType
    | FunctionType
    | UserDefinedType
    | ValueType;
  type_param: boolean;
  worker: boolean;
  ws: any[];
}

export interface AnnotationAttachment extends ASTNode {
  annotationName: Identifier;
  expression?: RecordLiteralExpr;
  packageAlias: Identifier;
  ws?: any[];
}

export interface ArrayType extends ASTNode {
  dimensionAsString?: string;
  dimensions: number;
  elementType:
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  grouped: boolean;
  isRestParam?: boolean;
  nullable: boolean;
  sizes: number[];
  symbolType: string[];
  ws?: any[];
}

export interface ArrowExpr extends ASTNode {
  expression:
    | ArrowExpr
    | BinaryExpr
    | ElvisExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | SimpleVariableRef
    | StringTemplateLiteral
    | UnaryExpr;
  hasParantheses?: boolean;
  isExpression?: boolean | string;
  parameters: Variable[];
  symbolType?: string[];
  ws: any[];
}

export interface Assignment extends ASTNode {
  declaredWithVar: boolean;
  expression:
    | BinaryExpr
    | CheckExpr
    | CheckPanicExpr
    | ElvisExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | Table
    | TableQueryExpression
    | TernaryExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | TypedescExpression
    | UnaryExpr
    | WaitExpr
    | WorkerReceive
    | WorkerSyncSend
    | XmlAttributeAccessExpr
    | XmlElementLiteral
    | XmlTextLiteral;
  variable:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | SimpleVariableRef
    | XmlAttributeAccessExpr;
  ws: any[];
}

export interface BinaryExpr extends ASTNode {
  inTemplateLiteral?: boolean;
  isExpression?: boolean | string;
  leftExpression:
    | BinaryExpr
    | CheckExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | StringTemplateLiteral
    | TypeConversionExpr
    | TypeTestExpr
    | UnaryExpr;
  operatorKind: string;
  rightExpression:
    | BinaryExpr
    | CheckExpr
    | CheckPanicExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | StringTemplateLiteral
    | TypeConversionExpr
    | TypeTestExpr
    | UnaryExpr
    | WaitExpr
    | XmlElementLiteral;
  symbolType?: string[];
  ws?: any[];
}

export interface Block extends ASTNode {
  VisibleEndpoints?: VisibleEndpoint[];
  isElseBlock?: boolean;
  statements: Array<
    | Abort
    | Assignment
    | Break
    | CompoundAssignment
    | ErrorDestructure
    | ExpressionStatement
    | Foreach
    | Forever
    | ForkJoin
    | If
    | Lock
    | Match
    | Next
    | Panic
    | RecordDestructure
    | Retry
    | Return
    | Transaction
    | TupleDestructure
    | VariableDef
    | While
    | WorkerSend
    | Xmlns
  >;
  ws?: any[];
}

export interface Break extends ASTNode {
  ws: any[];
}

export interface BuiltInRefType extends ASTNode {
  grouped: boolean;
  nullable: boolean;
  symbolType: string[];
  typeKind: string;
  ws: any[];
}

export interface CheckExpr extends ASTNode {
  expression:
    | FieldBasedAccessExpr
    | Invocation
    | SimpleVariableRef
    | TrapExpr
    | TypeConversionExpr
    | WorkerReceive;
  isExpression?: boolean | string;
  operatorKind: string;
  symbolType: string[];
  ws: any[];
}

export interface CheckPanicExpr extends ASTNode {
  expression: FieldBasedAccessExpr | Invocation;
  isExpression?: boolean | string;
  operatorKind: string;
  symbolType: string[];
  ws: any[];
}

export interface CompilationUnit extends ASTNode {
  name: string;
  topLevelNodes: Array<
    | Annotation
    | Constant
    | Function
    | Import
    | Service
    | TypeDefinition
    | Variable
    | Xmlns
  >;
  ws: any[];
}

export interface CompoundAssignment extends ASTNode {
  compoundOperator: string;
  expression:
    | BinaryExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TypeConversionExpr;
  operatorKind: string;
  variable:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | SimpleVariableRef
    | XmlAttributeAccessExpr;
  ws: any[];
}

export interface Constant extends ASTNode {
  abstract: boolean;
  annotationAttachments: AnnotationAttachment[];
  associatedTypeDefinition?: TypeDefinition;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  final: boolean;
  function_final: boolean;
  initialExpression: Literal | NumericLiteral | RecordLiteralExpr;
  interface: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  native: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  service: boolean;
  testable: boolean;
  typeNode?: ConstrainedType | UserDefinedType | ValueType;
  type_param: boolean;
  worker: boolean;
  ws: any[];
}

export interface ConstrainedType extends ASTNode {
  constraint:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  grouped: boolean;
  nullable: boolean;
  symbolType: string[];
  type: BuiltInRefType;
  ws: any[];
}

export interface DocumentationDescription extends ASTNode {
  text: string;
  ws?: any[];
}

export interface DocumentationParameter extends ASTNode {
  parameterDocumentation?: string;
  parameterDocumentationLines?: string[];
  parameterName?: Identifier;
  returnParameterDocumentation?: string;
  returnParameterDocumentationLines?: string[];
  returnType?: string;
  symbol?: string;
  ws: any[];
}

export interface ElvisExpr extends ASTNode {
  isExpression?: boolean | string;
  leftExpression:
    | ElvisExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
  rightExpression: GroupExpr | Literal | NumericLiteral | SimpleVariableRef;
  symbolType: string[];
  ws: any[];
}

export interface ErrorDestructure extends ASTNode {
  expression: Invocation | SimpleVariableRef;
  varRef: ErrorVariableRef;
  ws: any[];
}

export interface ErrorType extends ASTNode {
  detailsTypeNode?: ConstrainedType | UserDefinedType | ValueType;
  grouped: boolean;
  isAnonType?: boolean;
  nullable: boolean;
  reasonTypeNode?: UnionTypeNode | UserDefinedType | ValueType;
  symbolType: string[];
  ws: any[];
}

export interface ErrorVariable extends ASTNode {
  abstract: boolean;
  annotationAttachments: any;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  detail: Array<Identifier | Variable>;
  final: boolean;
  function_final: boolean;
  initialExpression?: Invocation | NumericLiteral | SimpleVariableRef;
  interface: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  native: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  reason: Variable;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  restDetail?: Variable;
  service: boolean;
  symbolType?: string[];
  testable: boolean;
  typeNode?: UserDefinedType;
  type_param: boolean;
  worker: boolean;
  ws: any[];
}

export interface ErrorVariableRef extends ASTNode {
  detail: NamedArgsExpr[];
  reason: IndexBasedAccessExpr | SimpleVariableRef;
  restVar?: IndexBasedAccessExpr | SimpleVariableRef;
  symbolType?: string[];
  ws: any[];
}

export interface ExpressionStatement extends ASTNode {
  expression:
    | CheckExpr
    | CheckPanicExpr
    | Invocation
    | ListConstructorExpr
    | RecordLiteralExpr
    | SimpleVariableRef
    | TypedescExpression
    | WaitExpr
    | WorkerFlush
    | WorkerReceive
    | XmlTextLiteral;
  ws: any[];
}

export interface FieldBasedAccessExpr extends ASTNode {
  expression:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
  fieldName: Identifier;
  isExpression?: boolean | string;
  optionalFieldAccess: boolean;
  symbolType?: string[];
  ws: any[];
}

export interface FiniteTypeNode extends ASTNode {
  grouped: boolean;
  isAnonType?: boolean;
  nullable: boolean;
  symbolType: string[];
  valueSet: Array<Literal | NumericLiteral>;
  ws?: any[];
}

export interface Foreach extends ASTNode {
  body: Block;
  collection:
    | BinaryExpr
    | CheckExpr
    | FieldBasedAccessExpr
    | Invocation
    | SimpleVariableRef
    | WaitExpr;
  declaredWithVar: boolean;
  variableDefinitionNode: VariableDef;
  ws: any[];
}

export interface Forever extends ASTNode {
  siddhiRuntimeEnabled: boolean;
  streamingQueryStatements: StreamingQuery[];
  ws: any[];
}

export interface ForkJoin extends ASTNode {
  workers: VariableDef[];
  ws: any[];
}

export interface Function extends ASTNode {
  abstract: boolean;
  annotationAttachments: AnnotationAttachment[];
  attached: boolean;
  body?: Block;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  endpointNodes: any;
  externalAnnotationAttachments: any;
  final: boolean;
  function_final: boolean;
  hasReturns?: boolean;
  interface: boolean;
  isConstructor?: boolean;
  isStreamAction?: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  native: boolean;
  noVisibleReceiver?: boolean;
  optional: boolean;
  parallel: boolean;
  parameters: Variable[];
  private: boolean;
  public: boolean;
  readonly: boolean;
  receiver?: Variable;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  restParameters?: Variable;
  returnTypeAnnotationAttachments: AnnotationAttachment[];
  returnTypeNode:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  service: boolean;
  skip?: boolean;
  testable: boolean;
  type_param: boolean;
  worker: boolean;
  workers: any;
  ws: any[];
}

export interface FunctionType extends ASTNode {
  grouped: boolean;
  hasReturn?: boolean;
  nullable: boolean;
  params: Variable[];
  returnKeywordExists: boolean;
  returnTypeNode:
    | ArrayType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  symbolType: string[];
  withParantheses?: boolean;
  ws: any[];
}

export interface GroupBy extends ASTNode {
  variables: FieldBasedAccessExpr[];
  ws: any[];
}

export interface GroupExpr extends ASTNode {
  expression:
    | ArrowExpr
    | BinaryExpr
    | ElvisExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TernaryExpr
    | TypeTestExpr
    | UnaryExpr
    | WaitExpr;
  isExpression?: boolean | string;
  symbolType?: string[];
  ws: any[];
}

export interface Identifier extends ASTNode {
  literal: boolean;
  value: string;
  valueWithBar: string;
  ws?: any[];
}

export interface If extends ASTNode {
  body: Block;
  condition:
    | BinaryExpr
    | GroupExpr
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TypeTestExpr
    | UnaryExpr;
  elseStatement?: Block | If;
  isElseIfBlock?: boolean;
  ladderParent?: boolean;
  ws: any[];
}

export interface Import extends ASTNode {
  alias: Identifier;
  isInternal?: boolean;
  orgName: Identifier;
  packageName: Identifier[];
  packageVersion: Identifier;
  userDefinedAlias?: boolean;
  ws?: any[];
}

export interface IndexBasedAccessExpr extends ASTNode {
  expression:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
  index:
    | BinaryExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TypeConversionExpr;
  isExpression?: boolean | string;
  symbolType?: string[];
  ws: any[];
}

export interface Invocation extends ASTNode {
  actionInvocation: boolean;
  annotationAttachments: any;
  argumentExpressions: Array<
    | ArrowExpr
    | BinaryExpr
    | CheckExpr
    | CheckPanicExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | NamedArgsExpr
    | NumericLiteral
    | RecordLiteralExpr
    | RestArgsExpr
    | SimpleVariableRef
    | StringTemplateLiteral
    | TernaryExpr
    | TrapExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | TypedescExpression
    | UnaryExpr
    | WorkerFlush
    | WorkerReceive
    | XmlAttributeAccessExpr
    | XmlElementLiteral
  >;
  async: boolean;
  definition?: any[];
  expression?:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | SimpleVariableRef
    | TypedescExpression;
  invocationType?: string;
  isExpression?: boolean | string;
  iterableOperation: boolean;
  name: Identifier;
  packageAlias: Identifier;
  requiredArgs: Array<
    | ArrowExpr
    | BinaryExpr
    | CheckExpr
    | CheckPanicExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | NamedArgsExpr
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | StringTemplateLiteral
    | TernaryExpr
    | TrapExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | TypedescExpression
    | UnaryExpr
    | WorkerFlush
    | WorkerReceive
    | XmlAttributeAccessExpr
    | XmlElementLiteral
  >;
  symbolType: any;
  ws?: any[];
}

export interface JoinStreamingInput extends ASTNode {
  joinType: string;
  onExpression: BinaryExpr;
  streamingInput: StreamingInput;
  unidirectionalAfterJoin: boolean;
  unidirectionalBeforeJoin: boolean;
  ws: any[];
}

export interface Lambda extends ASTNode {
  functionNode: Function;
  isExpression?: boolean | string;
  symbolType?: string[];
}

export interface Limit extends ASTNode {
  limitValue: string;
  ws: any[];
}

export interface ListConstructorExpr extends ASTNode {
  expressions: Array<
    | BinaryExpr
    | ElvisExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | TypedescExpression
    | UnaryExpr
    | XmlAttributeAccessExpr
    | XmlElementLiteral
  >;
  isExpression?: boolean | string;
  symbolType?: string[];
  ws: any[];
}

export interface Literal extends ASTNode {
  emptyParantheses?: boolean;
  endTemplateLiteral?: boolean;
  inTemplateLiteral?: boolean;
  isExpression?: boolean | string;
  lastNodeValue?: boolean;
  originalValue?: string;
  startTemplateLiteral?: boolean;
  symbolType: string[];
  unescapedValue?: string;
  value: string;
  ws?: any[];
}

export interface Lock extends ASTNode {
  body: Block;
  ws: any[];
}

export interface MarkdownDocumentation extends ASTNode {
  documentation: string;
  documentationLines: DocumentationDescription[];
  parameterDocumentations: string;
  parameters: DocumentationParameter[];
  returnParameter?: DocumentationParameter;
  returnParameterDocumentation?: string;
  ws: any[];
}

export interface Match extends ASTNode {
  expression: GroupExpr | SimpleVariableRef;
  patternClauses: Array<
    MatchStaticPatternClause | MatchStructuredPatternClause
  >;
  staticPatternClauses: MatchStaticPatternClause[];
  structuredPatternClauses: MatchStructuredPatternClause[];
  ws: any[];
}

export interface MatchStaticPatternClause extends ASTNode {
  literal:
    | BinaryExpr
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef;
  skip?: boolean;
  statement: Block;
  withCurlies?: boolean;
  ws?: any[];
}

export interface MatchStructuredPatternClause extends ASTNode {
  skip?: boolean;
  statement: Block;
  typeGuardExpr?: BinaryExpr | GroupExpr | TypeTestExpr;
  variableNode: ErrorVariable | RecordVariable | TupleVariable | Variable;
  withCurlies?: boolean;
  ws: any[];
}

export interface NamedArgsExpr extends ASTNode {
  expression:
    | BinaryExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TypeConversionExpr
    | TypeInitExpr;
  name: Identifier;
  symbolType?: string[];
  ws: any[];
}

export interface Next extends ASTNode {
  ws: any[];
}

export interface NumericLiteral extends ASTNode {
  isExpression?: boolean | string;
  originalValue?: string;
  symbolType: string[];
  unescapedValue?: string;
  value: string;
  ws?: any[];
}

export interface ObjectType extends ASTNode {
  fields: Variable[];
  functions: Function[];
  grouped: boolean;
  initFunction?: Function;
  isAnonType?: boolean;
  nullable: boolean;
  symbolType: string[];
  typeReferences: Array<ConstrainedType | UserDefinedType | ValueType>;
  ws?: any[];
}

export interface Panic extends ASTNode {
  expressions:
    | FieldBasedAccessExpr
    | Invocation
    | SimpleVariableRef
    | TypeConversionExpr;
  ws: any[];
}

export interface RecordDestructure extends ASTNode {
  expression:
    | Invocation
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef;
  variableRefs: RecordVariableRef;
  ws: any[];
}

export interface RecordLiteralExpr extends ASTNode {
  isExpression?: boolean | string;
  keyValuePairs: RecordLiteralKeyValue[];
  symbolType?: string[];
  ws?: any[];
}

export interface RecordLiteralKeyValue extends ASTNode {
  key:
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | SimpleVariableRef
    | StringTemplateLiteral;
  value:
    | BinaryExpr
    | CheckPanicExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypedescExpression;
  ws?: any[];
}

export interface RecordType extends ASTNode {
  fields: Variable[];
  grouped: boolean;
  isAnonType?: boolean;
  isRestFieldAvailable?: boolean;
  nullable: boolean;
  restFieldType?:
    | ConstrainedType
    | ErrorType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  sealed: boolean;
  symbolType: string[];
  typeReferences: Array<BuiltInRefType | UserDefinedType | ValueType>;
  ws: any[];
}

export interface RecordVariable extends ASTNode {
  abstract: boolean;
  annotationAttachments: any;
  attached: boolean;
  client: boolean;
  closed: boolean;
  compensate: boolean;
  constant: boolean;
  final: boolean;
  function_final: boolean;
  initialExpression?: Invocation | SimpleVariableRef;
  interface: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  native: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  restParam?: Variable;
  service: boolean;
  symbolType?: string[];
  testable: boolean;
  typeNode?: ConstrainedType | UnionTypeNode | UserDefinedType;
  type_param: boolean;
  variables: Array<
    ErrorVariable | Identifier | RecordVariable | TupleVariable | Variable
  >;
  worker: boolean;
  ws: any[];
}

export interface RecordVariableRef extends ASTNode {
  recordRefFields: Array<
    | ErrorVariableRef
    | Identifier
    | IndexBasedAccessExpr
    | RecordVariableRef
    | SimpleVariableRef
    | TupleVariableRef
  >;
  restParam?: SimpleVariableRef;
  symbolType: string[];
  ws: any[];
}

export interface RestArgsExpr extends ASTNode {
  expression: Invocation | SimpleVariableRef;
  symbolType?: string[];
  ws: any[];
}

export interface Retry extends ASTNode {
  ws: any[];
}

export interface Return extends ASTNode {
  expression:
    | AnnotAccessExpression
    | ArrowExpr
    | BinaryExpr
    | CheckExpr
    | CheckPanicExpr
    | ElvisExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | StringTemplateLiteral
    | TernaryExpr
    | TrapExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | UnaryExpr
    | WaitExpr
    | XmlAttributeAccessExpr
    | XmlElementLiteral;
  noExpressionAvailable?: boolean;
  ws: any[];
}

export interface SelectClause extends ASTNode {
  groupBy?: GroupBy;
  selectAll: boolean;
  selectExpressions?: SelectExpression[];
  ws: any[];
}

export interface SelectExpression extends ASTNode {
  expression: FieldBasedAccessExpr | SimpleVariableRef;
  identifier?: string;
  identifierAvailable?: boolean;
  ws?: any[];
}

export interface Service extends ASTNode {
  annotationAttachments: AnnotationAttachment[];
  anonymousService: boolean;
  attachedExprs: Array<SimpleVariableRef | TypeInitExpr>;
  isServiceTypeUnavailable: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  resources: Function[];
  skip?: boolean;
  typeDefinition: TypeDefinition;
  ws: any[];
}

export interface ServiceConstructor extends ASTNode {
  isExpression: boolean;
  serviceNode: Service;
  symbolType: string[];
  ws: any[];
}

export interface SimpleVariableRef extends ASTNode {
  inTemplateLiteral?: boolean;
  isEndpoint: boolean;
  isExpression?: boolean | string;
  packageAlias?: Identifier;
  symbolType?: string[];
  variableName: Identifier;
  ws?: any[];
}

export interface StreamAction extends ASTNode {
  invokableBody: Lambda;
  ws: any[];
}

export interface StreamingInput extends ASTNode {
  afterStreamingCondition?: Where;
  alias?: string;
  aliasAvailable?: boolean;
  streamReference: SimpleVariableRef;
  windowTraversedAfterWhere: boolean;
  ws?: any[];
}

export interface StreamingQuery extends ASTNode {
  selectClause: SelectClause;
  streamingAction: StreamAction;
  streamingInput: StreamingInput;
  ws: any[];
}

export interface StringTemplateLiteral extends ASTNode {
  expressions: Array<
    | BinaryExpr
    | FieldBasedAccessExpr
    | Invocation
    | Literal
    | SimpleVariableRef
    | TernaryExpr
  >;
  isExpression?: boolean | string;
  startTemplate: string;
  symbolType: string[];
  ws: any[];
}

export interface Table extends ASTNode {
  dataRows: Array<NumericLiteral | RecordLiteralExpr | SimpleVariableRef>;
  isExpression: boolean;
  symbolType: string[];
  tableColumns: TableColumn[];
  ws: any[];
}

export interface TableColumn extends ASTNode {
  flagSet: string[];
  name: string;
  ws: any[];
}

export interface TableQuery extends ASTNode {
  joinStreamingInput?: JoinStreamingInput;
  limitClause?: Limit;
  selectClauseNode: SelectClause;
  streamingInput: StreamingInput;
  ws: any[];
}

export interface TableQueryExpression extends ASTNode {
  isExpression: boolean;
  symbolType: string[];
  tableQuery: TableQuery;
}

export interface TernaryExpr extends ASTNode {
  condition:
    | BinaryExpr
    | GroupExpr
    | Literal
    | SimpleVariableRef
    | TypeConversionExpr
    | TypeTestExpr;
  elseExpression:
    | BinaryExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr
    | XmlTextLiteral;
  isExpression?: boolean | string;
  symbolType: string[];
  thenExpression:
    | BinaryExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr;
  ws: any[];
}

export interface Transaction extends ASTNode {
  abortedBody?: Block;
  committedBody?: Block;
  onRetryBody?: Block;
  retryCount?: NumericLiteral | SimpleVariableRef;
  transactionBody: Block;
  ws: any[];
}

export interface TrapExpr extends ASTNode {
  expression:
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | TypeConversionExpr
    | WaitExpr
    | WorkerReceive;
  isExpression?: boolean | string;
  symbolType: string[];
  ws: any[];
}

export interface TupleDestructure extends ASTNode {
  expression: Invocation | ListConstructorExpr | SimpleVariableRef;
  variableRefs: Array<
    IndexBasedAccessExpr | SimpleVariableRef | TupleVariableRef
  >;
  ws: any[];
}

export interface TupleTypeNode extends ASTNode {
  grouped: boolean;
  memberTypeNodes: Array<
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType
  >;
  nullable: boolean;
  symbolType: string[];
  ws: any[];
}

export interface TupleVariable extends ASTNode {
  abstract: boolean;
  annotationAttachments: any;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  final: boolean;
  function_final: boolean;
  initialExpression?: Invocation | ListConstructorExpr | SimpleVariableRef;
  interface: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  native: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  service: boolean;
  symbolType?: string[];
  testable: boolean;
  typeNode?: TupleTypeNode | UnionTypeNode;
  type_param: boolean;
  variables: Array<ErrorVariable | RecordVariable | TupleVariable | Variable>;
  worker: boolean;
  ws: any[];
}

export interface TupleVariableRef extends ASTNode {
  expressions: Array<
    | ErrorVariableRef
    | IndexBasedAccessExpr
    | RecordVariableRef
    | SimpleVariableRef
    | TupleVariableRef
  >;
  symbolType: string[];
  ws: any[];
}

export interface TypeConversionExpr extends ASTNode {
  abstract: boolean;
  annotationAttachments: AnnotationAttachment[];
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  expression:
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TypeConversionExpr
    | XmlAttributeAccessExpr;
  final: boolean;
  function_final: boolean;
  interface: boolean;
  isExpression?: boolean | string;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  native: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  service: boolean;
  symbolType: string[];
  testable: boolean;
  typeNode?:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  type_param: boolean;
  worker: boolean;
  ws: any[];
}

export interface TypeDefinition extends ASTNode {
  abstract: boolean;
  annotationAttachments: AnnotationAttachment[];
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  final: boolean;
  function_final: boolean;
  interface: boolean;
  isAbstractKeywordAvailable?: boolean;
  isObjectType?: boolean;
  isRecordType?: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  native: boolean;
  notVisible?: boolean;
  optional: boolean;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  service: boolean;
  skip?: boolean;
  testable: boolean;
  typeNode:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FiniteTypeNode
    | FunctionType
    | ObjectType
    | RecordType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  type_param: boolean;
  worker: boolean;
  ws?: any[];
}

export interface TypeInitExpr extends ASTNode {
  expressions: Array<
    | ArrowExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Lambda
    | Literal
    | NamedArgsExpr
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TypeInitExpr
  >;
  hasParantheses?: boolean;
  isExpression?: boolean | string;
  noExpressionAvailable?: boolean;
  noTypeAttached?: boolean;
  symbolType: string[];
  type?: UserDefinedType;
  typeName?: Identifier;
  ws: any[];
}

export interface TypeTestExpr extends ASTNode {
  expression:
    | AnnotAccessExpression
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | SimpleVariableRef
    | XmlAttributeAccessExpr;
  isExpression?: boolean | string;
  symbolType?: string[];
  typeNode:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  ws: any[];
}

export interface TypedescExpression extends ASTNode {
  isExpression?: boolean;
  isObject?: boolean;
  symbolType?: string[];
  typeNode:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  ws?: any[];
}

export interface UnaryExpr extends ASTNode {
  expression:
    | FieldBasedAccessExpr
    | GroupExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | UnaryExpr;
  isExpression?: boolean | string;
  operatorKind: string;
  symbolType?: string[];
  ws: any[];
}

export interface UnionTypeNode extends ASTNode {
  grouped: boolean;
  memberTypeNodes: Array<
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UserDefinedType
    | ValueType
  >;
  nullable: boolean;
  nullableOperatorAvailable?: boolean;
  symbolType: string[];
  withParantheses?: boolean;
  ws?: any[];
}

export interface UserDefinedType extends ASTNode {
  abstract: boolean;
  anonType?: ErrorType | FiniteTypeNode | ObjectType | RecordType;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  final: boolean;
  function_final: boolean;
  grouped: boolean;
  interface: boolean;
  isAnonType?: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  native: boolean;
  nullable: boolean;
  optional: boolean;
  packageAlias: Identifier;
  parallel: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  service: boolean;
  symbolType?: string[];
  testable: boolean;
  typeName: Identifier;
  type_param: boolean;
  worker: boolean;
  ws?: any[];
}

export interface ValueType extends ASTNode {
  emptyParantheses?: boolean;
  grouped: boolean;
  nullable: boolean;
  symbolType?: string[];
  typeKind: string;
  withParantheses?: boolean;
  ws?: any[];
}

export interface Variable extends ASTNode {
  abstract: boolean;
  annotationAttachments: AnnotationAttachment[];
  arrowExprParam?: boolean;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  endWithComma?: boolean;
  endWithSemicolon?: boolean;
  final: boolean;
  function_final: boolean;
  global?: boolean;
  inObject?: boolean;
  initialExpression?:
    | AnnotAccessExpression
    | ArrowExpr
    | BinaryExpr
    | CheckExpr
    | CheckPanicExpr
    | ElvisExpr
    | FieldBasedAccessExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | ListConstructorExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | ServiceConstructor
    | SimpleVariableRef
    | StringTemplateLiteral
    | Table
    | TableQueryExpression
    | TernaryExpr
    | TrapExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | TypedescExpression
    | UnaryExpr
    | WaitExpr
    | WorkerFlush
    | WorkerReceive
    | WorkerSyncSend
    | XmlAttributeAccessExpr
    | XmlCommentLiteral
    | XmlElementLiteral
    | XmlPiLiteral
    | XmlTextLiteral;
  interface: boolean;
  isAnonType?: boolean;
  isEndpoint: boolean;
  lambda: boolean;
  lang_lib: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  native: boolean;
  noVisibleName?: boolean;
  optional: boolean;
  parallel: boolean;
  param?: boolean;
  private: boolean;
  public: boolean;
  readonly: boolean;
  record: boolean;
  remote: boolean;
  required: boolean;
  resource: boolean;
  rest?: boolean;
  service: boolean;
  skip?: boolean;
  symbolType?: string[];
  testable: boolean;
  typeNode?:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  type_param: boolean;
  worker: boolean;
  ws?: any[];
}

export interface VariableDef extends ASTNode {
  isEndpoint?: boolean;
  isInFork: boolean;
  skip?: boolean;
  variable: ErrorVariable | RecordVariable | TupleVariable | Variable;
  ws?: any[];
}

export interface VisibleEndpoint extends ASTNode {
  caller: boolean;
  isLocal: boolean;
  name: string;
  pkgAlias: string;
  pkgName: string;
  pkgOrgName: string;
  typeName: string;
}

export interface WaitExpr extends ASTNode {
  expression?: BinaryExpr | Invocation | SimpleVariableRef;
  isExpression?: boolean | string;
  keyValuePairs?: WaitLiteralKeyValue[];
  symbolType: string[];
  ws: any[];
}

export interface WaitLiteralKeyValue extends ASTNode {
  key: Identifier;
  value?: Invocation | SimpleVariableRef;
  ws: any[];
}

export interface Where extends ASTNode {
  expression: BinaryExpr;
  ws: any[];
}

export interface While extends ASTNode {
  body: Block;
  condition:
    | BinaryExpr
    | GroupExpr
    | ListConstructorExpr
    | Literal
    | NumericLiteral;
  ws: any[];
}

export interface WorkerFlush extends ASTNode {
  isExpression?: boolean;
  symbolType: string[];
  workerName?: Identifier;
  ws: any[];
}

export interface WorkerReceive extends ASTNode {
  isExpression?: boolean;
  keyExpression?: NumericLiteral | SimpleVariableRef;
  symbolType: string[];
  workerName: Identifier;
  ws: any[];
}

export interface WorkerSend extends ASTNode {
  expression:
    | BinaryExpr
    | GroupExpr
    | IndexBasedAccessExpr
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | WaitExpr;
  keyExpression?: NumericLiteral | SimpleVariableRef;
  symbolType: string[];
  workerName: Identifier;
  ws: any[];
}

export interface WorkerSyncSend extends ASTNode {
  expression: BinaryExpr | NumericLiteral | SimpleVariableRef;
  isExpression: boolean;
  symbolType: string[];
  workerName: Identifier;
  ws: any[];
}

export interface XmlAttribute extends ASTNode {
  inTemplateLiteral: boolean;
  name: XmlQname;
  symbolType: string[];
  value: XmlQuotedString;
  ws: any[];
}

export interface XmlAttributeAccessExpr extends ASTNode {
  expression: SimpleVariableRef;
  index?: Literal | SimpleVariableRef;
  isExpression?: boolean | string;
  symbolType: string[];
  ws: any[];
}

export interface XmlCommentLiteral extends ASTNode {
  inTemplateLiteral?: boolean;
  isExpression?: boolean;
  root?: boolean;
  startLiteral?: string;
  symbolType: string[];
  textFragments: Array<Literal | SimpleVariableRef>;
  ws: any[];
}

export interface XmlElementLiteral extends ASTNode {
  attributes: XmlAttribute[];
  content: Array<
    | BinaryExpr
    | Literal
    | SimpleVariableRef
    | XmlCommentLiteral
    | XmlElementLiteral
    | XmlPiLiteral
  >;
  endTagName?: XmlQname;
  inTemplateLiteral?: boolean;
  isExpression?: boolean | string;
  namespaces: string;
  root?: boolean;
  startLiteral?: string;
  startTagName: XmlQname;
  symbolType: string[];
  ws: any[];
}

export interface XmlPiLiteral extends ASTNode {
  dataTextFragments: Array<Literal | SimpleVariableRef>;
  inTemplateLiteral?: boolean;
  isExpression?: boolean;
  root?: boolean;
  startLiteral?: string;
  symbolType: string[];
  target: Literal;
  ws?: any[];
}

export interface XmlQname extends ASTNode {
  inTemplateLiteral?: boolean;
  localname: Identifier;
  prefix: Identifier;
  symbolType: string[];
  ws: any[];
}

export interface XmlQuotedString extends ASTNode {
  symbolType: string[];
  textFragments: Array<BinaryExpr | Literal | SimpleVariableRef>;
}

export interface XmlTextLiteral extends ASTNode {
  isExpression?: boolean;
  root: boolean;
  startLiteral: string;
  symbolType: string[];
  textFragments: Array<Literal | SimpleVariableRef>;
  ws: any[];
}

export interface Xmlns extends ASTNode {
  global?: boolean;
  namespaceDeclaration?: Xmlns;
  namespaceURI?: Literal;
  prefix?: Identifier;
  symbolType?: string[];
  ws?: any[];
}

// tslint:enable:ban-types
