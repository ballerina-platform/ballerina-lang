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

export interface Annotation extends ASTNode {
  abstract: boolean;
  annotationAttachments: any;
  attached: boolean;
  attachmentPoints: string[];
  client: boolean;
  compensate: boolean;
  constant: boolean;
  deprecated: boolean;
  deprecatedAttachments: any;
  final: boolean;
  function_final: boolean;
  interface: boolean;
  lambda: boolean;
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
  typeNode: UserDefinedType;
  worker: boolean;
  ws: any[];
}

export interface AnnotationAttachment extends ASTNode {
  annotationName: Identifier;
  expression?: RecordLiteralExpr;
  packageAlias: Identifier;
  ws: any[];
}

export interface ArrayLiteralExpr extends ASTNode {
  expressions: Array<
    | ArrayLiteralExpr
    | BracedTupleExpr
    | FieldBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
  >;
  isExpression?: boolean | string;
  symbolType?: string[];
  ws: any[];
}

export interface ArrayType extends ASTNode {
  dimensionAsString?: string;
  dimensions: number;
  elementType:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | FunctionType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  grouped: boolean;
  isRestParam?: boolean;
  nullable: boolean;
  nullableOperatorAvailable?: boolean;
  sizes: number[];
  symbolType: string[];
  ws?: any[];
}

export interface ArrowExpr extends ASTNode {
  expression:
    | ArrowExpr
    | BinaryExpr
    | BracedTupleExpr
    | ElvisExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
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
    | ArrayLiteralExpr
    | BinaryExpr
    | BracedTupleExpr
    | CheckExpr
    | CheckPanicExpr
    | ElvisExpr
    | ErrorConstructor
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
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
    | WaitExpr
    | WorkerReceive
    | XmlAttributeAccessExpr
    | XmlElementLiteral;
  variable:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | SimpleVariableRef
    | XmlAttributeAccessExpr;
  ws: any[];
}

export interface BinaryExpr extends ASTNode {
  isExpression?: boolean | string;
  leftExpression:
    | BinaryExpr
    | BracedTupleExpr
    | CheckExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TypeConversionExpr
    | TypeTestExpr
    | UnaryExpr;
  operatorKind: string;
  rightExpression:
    | BinaryExpr
    | BracedTupleExpr
    | CheckExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TypeConversionExpr
    | TypeTestExpr
    | UnaryExpr
    | WaitExpr;
  symbolType?: string[];
  ws: any[];
}

export interface Block extends ASTNode {
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

export interface BracedTupleExpr extends ASTNode {
  expressions: Array<
    | ArrayLiteralExpr
    | ArrowExpr
    | BinaryExpr
    | BracedTupleExpr
    | ElvisExpr
    | ErrorConstructor
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr
    | TypeTestExpr
    | UnaryExpr
    | XmlTextLiteral
  >;
  isExpression?: boolean | string;
  symbolType?: string[];
  ws: any[];
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
  expression: Invocation | SimpleVariableRef | TrapExpr;
  isExpression?: boolean | string;
  operatorKind: string;
  symbolType: string[];
  ws: any[];
}

export interface CheckPanicExpr extends ASTNode {
  expression: Invocation;
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
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef;
  operatorKind: string;
  variable: FieldBasedAccessExpr | IndexBasedAccessExpr | SimpleVariableRef;
  ws: any[];
}

export interface Constant extends ASTNode {
  abstract: boolean;
  annotationAttachments: any;
  associatedTypeDefinition: TypeDefinition;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  deprecated: boolean;
  deprecatedAttachments: any;
  final: boolean;
  function_final: boolean;
  interface: boolean;
  lambda: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
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
  typeNode?: ValueType;
  value: Literal | NumericLiteral;
  worker: boolean;
  ws: any[];
}

export interface ConstrainedType extends ASTNode {
  constraint:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  grouped: boolean;
  nullable: boolean;
  nullableOperatorAvailable?: boolean;
  symbolType: string[];
  type: BuiltInRefType;
  ws: any[];
}

export interface Deprecated extends ASTNode {
  deprecatedStart: string;
  documentationText: string;
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
  rightExpression:
    | BracedTupleExpr
    | Literal
    | NumericLiteral
    | SimpleVariableRef;
  symbolType: string[];
  ws: any[];
}

export interface ErrorConstructor extends ASTNode {
  detailsExpression?: RecordLiteralExpr | SimpleVariableRef;
  isExpression?: boolean | string;
  reasonExpression:
    | BinaryExpr
    | FieldBasedAccessExpr
    | Literal
    | SimpleVariableRef;
  symbolType?: string[];
  ws: any[];
}

export interface ErrorDestructure extends ASTNode {
  expression: ErrorConstructor | SimpleVariableRef;
  varRef: ErrorVariableRef;
  ws: any[];
}

export interface ErrorType extends ASTNode {
  detailsTypeNode?: ConstrainedType | UserDefinedType;
  grouped: boolean;
  nullable: boolean;
  nullableOperatorAvailable?: boolean;
  reasonTypeNode?: UserDefinedType | ValueType;
  symbolType: string[];
  ws: any[];
}

export interface ErrorVariableRef extends ASTNode {
  detail:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | RecordVariableRef
    | SimpleVariableRef;
  reason: FieldBasedAccessExpr | IndexBasedAccessExpr | SimpleVariableRef;
  symbolType?: string[];
  ws: any[];
}

export interface ExpressionStatement extends ASTNode {
  expression:
    | CheckExpr
    | CheckPanicExpr
    | Invocation
    | RecordLiteralExpr
    | WaitExpr;
  ws: any[];
}

export interface FieldBasedAccessExpr extends ASTNode {
  errorLifting?: boolean;
  expression:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
  fieldName: Identifier;
  isExpression?: boolean | string;
  symbolType?: string[];
  ws: any[];
}

export interface FiniteTypeNode extends ASTNode {
  grouped: boolean;
  nullable: boolean;
  symbolType: string[];
  valueSet: Array<Literal | NumericLiteral>;
  ws?: any[];
}

export interface Foreach extends ASTNode {
  VisibleEndpoints?: VisibleEndpoint[];
  body: Block;
  collection:
    | BinaryExpr
    | FieldBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
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
  VisibleEndpoints?: VisibleEndpoint[];
  abstract: boolean;
  allParams?: Array<Variable | VariableDef>;
  annotationAttachments: AnnotationAttachment[];
  attached: boolean;
  body?: Block;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  defaultableParameters: VariableDef[];
  deprecated: boolean;
  deprecatedAttachments: Deprecated[];
  endpointNodes: any;
  final: boolean;
  function_final: boolean;
  hasRestParams?: boolean;
  hasReturns?: boolean;
  interface: boolean;
  isConstructor?: boolean;
  isStreamAction?: boolean;
  lambda: boolean;
  listener: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  native: boolean;
  noVisibleReceiver?: boolean;
  optional: boolean;
  parallel: boolean;
  parameters: Array<Variable | VariableDef>;
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
  variables: Array<FieldBasedAccessExpr | SimpleVariableRef>;
  ws: any[];
}

export interface Having extends ASTNode {
  expression: BinaryExpr;
  ws: any[];
}

export interface Identifier extends ASTNode {
  literal: boolean;
  value: string;
  valueWithBar: string;
}

export interface If extends ASTNode {
  VisibleEndpoints?: VisibleEndpoint[];
  body: Block;
  condition:
    | BinaryExpr
    | BracedTupleExpr
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
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef;
  isExpression?: boolean | string;
  symbolType: string[];
  ws: any[];
}

export interface Invocation extends ASTNode {
  actionInvocation: boolean;
  argumentExpressions: Array<
    | ArrayLiteralExpr
    | ArrowExpr
    | BinaryExpr
    | BracedTupleExpr
    | CheckExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | Literal
    | NamedArgsExpr
    | NumericLiteral
    | RecordLiteralExpr
    | RestArgsExpr
    | SimpleVariableRef
    | StringTemplateLiteral
    | TernaryExpr
    | TypeConversionExpr
    | TypeInitExpr
    | TypeTestExpr
    | UnaryExpr
    | XmlAttributeAccessExpr
    | XmlElementLiteral
  >;
  async: boolean;
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
  symbolType: any;
  ws?: any[];
}

export interface JoinStreamingInput extends ASTNode {
  joinType: string;
  onExpression?: BinaryExpr;
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
  expression: BracedTupleExpr | SimpleVariableRef;
  patternClauses: Array<
    MatchStaticPatternClause | MatchStructuredPatternClause
  >;
  staticPatternClauses: MatchStaticPatternClause[];
  structuredPatternClauses: MatchStructuredPatternClause[];
  ws: any[];
}

export interface MatchStaticPatternClause extends ASTNode {
  literal: BinaryExpr | Literal | NumericLiteral | SimpleVariableRef;
  skip?: boolean;
  statement: Block;
  withCurlies?: boolean;
  ws: any[];
}

export interface MatchStructuredPatternClause extends ASTNode {
  skip?: boolean;
  statement: Block;
  typeGuardExpr?: BracedTupleExpr | TypeTestExpr;
  variableNode: RecordVariable | TupleVariable | Variable;
  ws: any[];
}

export interface NamedArgsExpr extends ASTNode {
  expression:
    | BinaryExpr
    | IndexBasedAccessExpr
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TypeConversionExpr
    | UnaryExpr;
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
  nullable: boolean;
  symbolType: string[];
  typeReferences: UserDefinedType[];
  ws?: any[];
}

export interface OrderBy extends ASTNode {
  variables: OrderByVariable[];
  ws: any[];
}

export interface OrderByVariable extends ASTNode {
  noVisibleType: boolean;
  orderByType: string;
  variableReference: SimpleVariableRef;
}

export interface Panic extends ASTNode {
  expressions: ErrorConstructor | Invocation | SimpleVariableRef;
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
  ws: any[];
}

export interface RecordLiteralKeyValue extends ASTNode {
  key: Literal | SimpleVariableRef;
  value:
    | ArrayLiteralExpr
    | BinaryExpr
    | BracedTupleExpr
    | ErrorConstructor
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeInitExpr
    | TypedescExpression
    | UnaryExpr;
  ws?: any[];
}

export interface RecordType extends ASTNode {
  fields: Variable[];
  grouped: boolean;
  isRestFieldAvailable?: boolean;
  nullable: boolean;
  restFieldType?: UnionTypeNode | UserDefinedType | ValueType;
  sealed: boolean;
  symbolType: string[];
  typeReferences: UserDefinedType[];
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
  deprecated: boolean;
  deprecatedAttachments: any;
  final: boolean;
  function_final: boolean;
  initialExpression?: Invocation | SimpleVariableRef;
  interface: boolean;
  lambda: boolean;
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
  symbolType: string[];
  testable: boolean;
  typeNode?: UserDefinedType;
  variables: Array<Identifier | RecordVariable | Variable>;
  worker: boolean;
  ws: any[];
}

export interface RecordVariableRef extends ASTNode {
  recordRefFields: Array<
    | ErrorVariableRef
    | FieldBasedAccessExpr
    | Identifier
    | IndexBasedAccessExpr
    | RecordVariableRef
    | SimpleVariableRef
    | TupleVariableRef
  >;
  restParam?: SimpleVariableRef;
  symbolType?: string[];
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
    | ArrayLiteralExpr
    | ArrowExpr
    | BinaryExpr
    | BracedTupleExpr
    | CheckExpr
    | CheckPanicExpr
    | ElvisExpr
    | ErrorConstructor
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr
    | TypeTestExpr
    | UnaryExpr
    | WaitExpr
    | XmlElementLiteral;
  noExpressionAvailable?: boolean;
  ws: any[];
}

export interface SelectClause extends ASTNode {
  groupBy?: GroupBy;
  having?: Having;
  selectAll: boolean;
  selectExpressions?: SelectExpression[];
  ws: any[];
}

export interface SelectExpression extends ASTNode {
  expression: FieldBasedAccessExpr | Invocation | SimpleVariableRef;
  identifier?: string;
  identifierAvailable?: boolean;
  ws?: any[];
}

export interface Service extends ASTNode {
  annotationAttachments: AnnotationAttachment[];
  anonymousService: boolean;
  attachedExprs: Array<SimpleVariableRef | TypeInitExpr>;
  deprecatedAttachments: any;
  isServiceTypeUnavailable: boolean;
  markdownDocumentationAttachment?: MarkdownDocumentation;
  name: Identifier;
  resources: Function[];
  typeDefinition: TypeDefinition;
  ws: any[];
}

export interface ServiceConstructor extends ASTNode {
  isExpression: boolean;
  symbolType: string[];
  ws: any[];
}

export interface SimpleVariableRef extends ASTNode {
  inTemplateLiteral?: boolean;
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
  streamReference: Invocation | SimpleVariableRef;
  windowClause?: WindowClause;
  windowTraversedAfterWhere: boolean;
  ws?: any[];
}

export interface StreamingQuery extends ASTNode {
  joiningInput?: JoinStreamingInput;
  selectClause: SelectClause;
  streamingAction: StreamAction;
  streamingInput: StreamingInput;
  ws: any[];
}

export interface StringTemplateLiteral extends ASTNode {
  expressions: Array<
    | FieldBasedAccessExpr
    | Invocation
    | Literal
    | SimpleVariableRef
    | TernaryExpr
    | UnaryExpr
  >;
  isExpression?: boolean;
  startTemplate: string;
  symbolType: string[];
  ws: any[];
}

export interface Table extends ASTNode {
  dataRows: Array<RecordLiteralExpr | SimpleVariableRef>;
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
  orderByNode?: OrderBy;
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
    | BracedTupleExpr
    | Literal
    | SimpleVariableRef
    | TypeTestExpr;
  elseExpression:
    | BracedTupleExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TernaryExpr
    | TypeConversionExpr;
  isExpression?: boolean | string;
  symbolType: string[];
  thenExpression:
    | BinaryExpr
    | BracedTupleExpr
    | FieldBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TernaryExpr;
  ws: any[];
}

export interface Transaction extends ASTNode {
  abortedBody?: Block;
  committedBody?: Block;
  onRetryBody?: Block;
  retryCount?: NumericLiteral;
  transactionBody: Block;
  ws: any[];
}

export interface TrapExpr extends ASTNode {
  expression: IndexBasedAccessExpr | Invocation | TypeConversionExpr | WaitExpr;
  isExpression?: boolean;
  symbolType: string[];
  ws: any[];
}

export interface TupleDestructure extends ASTNode {
  expression: BracedTupleExpr | Invocation | SimpleVariableRef;
  variableRefs: Array<
    FieldBasedAccessExpr | SimpleVariableRef | TupleVariableRef
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
  deprecated: boolean;
  deprecatedAttachments: any;
  final: boolean;
  function_final: boolean;
  initialExpression?:
    | BracedTupleExpr
    | FieldBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
  interface: boolean;
  lambda: boolean;
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
  typeNode?: TupleTypeNode;
  variables: Array<TupleVariable | Variable>;
  worker: boolean;
  ws: any[];
}

export interface TupleVariableRef extends ASTNode {
  expressions: Array<
    | ErrorVariableRef
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | RecordVariableRef
    | SimpleVariableRef
    | TupleVariableRef
  >;
  symbolType: string[];
  ws: any[];
}

export interface TypeConversionExpr extends ASTNode {
  expression:
    | ArrayLiteralExpr
    | BracedTupleExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef;
  isExpression?: boolean | string;
  symbolType: string[];
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

export interface TypeDefinition extends ASTNode {
  abstract: boolean;
  annotationAttachments: any;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  deprecated: boolean;
  deprecatedAttachments: any;
  final: boolean;
  function_final: boolean;
  interface: boolean;
  isAbstractKeywordAvailable?: boolean;
  isObjectType?: boolean;
  isRecordType?: boolean;
  lambda: boolean;
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
    | ErrorType
    | FiniteTypeNode
    | FunctionType
    | ObjectType
    | RecordType
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  worker: boolean;
  ws?: any[];
}

export interface TypeInitExpr extends ASTNode {
  expressions: Array<
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NamedArgsExpr
    | NumericLiteral
    | RecordLiteralExpr
    | SimpleVariableRef
  >;
  hasParantheses?: boolean;
  isExpression?: boolean;
  noExpressionAvailable?: boolean;
  noTypeAttached?: boolean;
  symbolType: string[];
  type?: UserDefinedType;
  typeName?: Identifier;
  ws: any[];
}

export interface TypeTestExpr extends ASTNode {
  expression:
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | SimpleVariableRef;
  isExpression?: boolean | string;
  symbolType: string[];
  typeNode:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | ErrorType
    | TupleTypeNode
    | UnionTypeNode
    | UserDefinedType
    | ValueType;
  ws: any[];
}

export interface TypedescExpression extends ASTNode {
  isExpression?: boolean;
  symbolType: string[];
  typeNode:
    | ArrayType
    | BuiltInRefType
    | ConstrainedType
    | TupleTypeNode
    | UnionTypeNode
    | ValueType;
}

export interface UnaryExpr extends ASTNode {
  expression:
    | BracedTupleExpr
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Literal
    | NumericLiteral
    | SimpleVariableRef
    | TypeConversionExpr
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
  symbolType: string[];
  withParantheses?: boolean;
  ws?: any[];
}

export interface UserDefinedType extends ASTNode {
  abstract: boolean;
  anonType?: FiniteTypeNode | ObjectType | RecordType;
  attached: boolean;
  client: boolean;
  compensate: boolean;
  constant: boolean;
  deprecated: boolean;
  final: boolean;
  function_final: boolean;
  grouped: boolean;
  interface: boolean;
  isAnonType?: boolean;
  lambda: boolean;
  listener: boolean;
  native: boolean;
  nullable: boolean;
  nullableOperatorAvailable?: boolean;
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
  symbolType: string[];
  testable: boolean;
  typeName: Identifier;
  worker: boolean;
  ws?: any[];
}

export interface ValueType extends ASTNode {
  emptyParantheses?: boolean;
  grouped: boolean;
  nullable: boolean;
  nullableOperatorAvailable?: boolean;
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
  defaultable?: boolean;
  deprecated: boolean;
  deprecatedAttachments: any;
  endWithSemicolon?: boolean;
  final: boolean;
  function_final: boolean;
  global?: boolean;
  inObject?: boolean;
  initialExpression?:
    | ArrayLiteralExpr
    | ArrowExpr
    | BinaryExpr
    | BracedTupleExpr
    | CheckExpr
    | CheckPanicExpr
    | ElvisExpr
    | ErrorConstructor
    | FieldBasedAccessExpr
    | IndexBasedAccessExpr
    | Invocation
    | Lambda
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
  lambda: boolean;
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
  worker: boolean;
  ws?: any[];
}

export interface VariableDef extends ASTNode {
  defaultable?: boolean;
  param?: boolean;
  variable: RecordVariable | TupleVariable | Variable;
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
  expression?: BinaryExpr | SimpleVariableRef;
  isExpression?: boolean | string;
  keyValuePairs?: WaitLiteralKeyValue[];
  symbolType: string[];
  ws: any[];
}

export interface WaitLiteralKeyValue extends ASTNode {
  key: Identifier;
  value?: SimpleVariableRef;
  ws: any[];
}

export interface Where extends ASTNode {
  expression: BinaryExpr;
  ws: any[];
}

export interface While extends ASTNode {
  VisibleEndpoints?: VisibleEndpoint[];
  body: Block;
  condition: BinaryExpr | BracedTupleExpr;
  ws: any[];
}

export interface WindowClause extends ASTNode {
  functionInvocation: Invocation;
  ws: any[];
}

export interface WorkerFlush extends ASTNode {
  isExpression: boolean;
  symbolType: string[];
  workerName: Identifier;
  ws: any[];
}

export interface WorkerReceive extends ASTNode {
  isExpression: boolean;
  keyExpression?: NumericLiteral | SimpleVariableRef;
  symbolType: string[];
  workerName: Identifier;
  ws: any[];
}

export interface WorkerSend extends ASTNode {
  expression: BracedTupleExpr | Literal | NumericLiteral | SimpleVariableRef;
  keyExpression?: NumericLiteral | SimpleVariableRef;
  symbolType: string[];
  workerName: Identifier;
  ws: any[];
}

export interface WorkerSyncSend extends ASTNode {
  expression: SimpleVariableRef;
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
  expression: FieldBasedAccessExpr | IndexBasedAccessExpr | SimpleVariableRef;
  index?: Literal | SimpleVariableRef;
  isExpression?: boolean;
  symbolType: string[];
  ws: any[];
}

export interface XmlCommentLiteral extends ASTNode {
  inTemplateLiteral?: boolean;
  isExpression?: boolean;
  root?: boolean;
  startLiteral?: string;
  symbolType: string[];
  textFragments: Array<BinaryExpr | Literal>;
  ws: any[];
}

export interface XmlElementLiteral extends ASTNode {
  attributes: XmlAttribute[];
  content: Array<
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
  dataTextFragments: Literal[];
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
  textFragments: Literal[];
}

export interface XmlTextLiteral extends ASTNode {
  isExpression?: boolean;
  root: boolean;
  startLiteral: string;
  symbolType: string[];
  textFragments: Literal[];
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
