PackageDeclaration
   : package <packageName-joined-by.>* ;

Import
   : <isInternal?>
   : <userDefinedAlias?> import <orgName.value> / <packageName-joined-by.>* as <alias.value> ;
   : <userDefinedAlias?> import                   <packageName-joined-by.>* as <alias.value> ;
   :                     import <orgName.value> / <packageName-joined-by.>* ;
   :                     import                   <packageName-joined-by.>* ;

Identifier
   : <valueWithBar>
   ;

Abort
   : abort ;
   ;

Action
   : <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* action <name.value> ( <parameters-joined-by,>* ) ( <returnParameters-joined-by,>+ ) { <body.source> <workers>* }
   | <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* action <name.value> ( <parameters-joined-by,>* )                                    { <body.source> <workers>* }
   ;

Annotation
   : <annotationAttachments>* annotation < <attachmentPoints-joined-by-,>* > <name.value> <typeNode.source> ;
   ;

AnnotationAttachment
   : <builtin?> @                        <annotationName.value> <expression.source>
   : <builtin?> @                        <annotationName.value>
   :            @ <packageAlias.value> : <annotationName.value> <expression.source>
   :            @ <packageAlias.value> : <annotationName.value>
   |            @                        <annotationName.value> <expression.source>
   |            @                        <annotationName.value>
   ;

ArrayLiteralExpr
   : [ <expressions-joined-by,>* ]
   ;

Assignment
   : <declaredWithVar?var> <variable.source> = <expression.source> ;
   ;

AwaitExpr
   : await <expression.source>
   ;

BinaryExpr
   : <inTemplateLiteral?> {{ <leftExpression.source> <operatorKind> <rightExpression.source> }}
   |             <leftExpression.source> <operatorKind> <rightExpression.source>
   ;

Bind
   : bind <expression.source> with <variable.source> ;
   ;

Block
   : <statements>*
   | 
   ;

Break
   : break ;
   ;

BracedTupleExpr
   : ( <expressions-joined-by,>* )

BuiltInRefType
   : <typeKind>
   ;

Catch
   : catch ( <parameter.source> ) { <body.source> }
   ;

CheckExpr
   : check <expression.source>
   ;

Comment
   : <comment>
   ;

CompoundAssignment
   : <variable.source> += <expression.source> ;
   ;

Connector
   : <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> connector <name.value> ( <parameters-joined-by,>* ) { <variableDefs>* <actions>* }
   | <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> connector <name.value> ( <parameters-joined-by,>* ) { <actions>* }
   ;

ConnectorInitExpr
   : create <connectorType.source> ( <expressions-joined-by,>* )
   | create <connectorType.source> ( )
   ;

ConstrainedType
   : <type.source> < <constraint.source> >
   ;

Documentation
   : documentation { <documentationText> <attributes>* }
   ;

DocumentationAttribute
   :  <paramType> {{ <documentationField.value> }} <documentationText>
   ;

Deprecated
   : deprecated { <documentationText> }
   ;

Done
   : done ;
   ;

ElvisExpr
   : <leftExpression.source> ?: <rightExpression.source>
   ;

Endpoint
   :  <skipSourceGen?>
   |                   <annotationAttachments>* endpoint <endPointType.source> <name.value> <configurationExpression.source> ;
   ;

EndpointType
   : < <constraint.source> >
   ;

ExpressionStatement
   : <expression.source> ;
   ;

FieldBasedAccessExpr
   : <expression.source> . <fieldName.value>
   ;

Foreach
   : foreach <variables-joined-by,>* in <collection.source> { <body.source> }
   ;

Forever
   : forever { <streamingQueryStatements>* }
   ;

ForkJoin
   : fork { <workers>* } join ( <joinType> <joinCount> <joinedWorkerIdentifiers-joined-by,>* ) ( <joinResultVar.source> ) { <joinBody.source> } timeout ( <timeOutExpression.source> ) ( <timeOutVariable.source> ) { <timeoutBody.source> }
   : fork { <workers>* } join ( <joinType> <joinCount> <joinedWorkerIdentifiers-joined-by,>* ) ( <joinResultVar.source> ) { <joinBody.source> }
   : fork { <workers>* } join ( <joinType> <joinCount> <joinedWorkerIdentifiers-joined-by,>* ) ( <joinResultVar.source> ) { }
   : fork { <workers>* } join ( <joinType> <joinedWorkerIdentifiers-joined-by,>* ) ( <joinResultVar.source> ) { <joinBody.source> } timeout ( <timeOutExpression.source> ) ( <timeOutVariable.source> ) { <timeoutBody.source> }
   : fork { <workers>* } join ( <joinType> <joinedWorkerIdentifiers-joined-by,>* ) ( <joinResultVar.source> ) { <joinBody.source> }
   | fork { <workers>* } join ( <joinType> <joinedWorkerIdentifiers-joined-by,>* ) ( <joinResultVar.source> ) { }
   ;

Function
   : <defaultConstructor?>
   | <isConstructor?>          <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <isConstructor?>          <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                <name.value> ( <parameters-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <isStreamAction?> =>                                        ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <isStreamAction?> =>                                        ( <parameters-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                             ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> ) <hasReturns?> =>                                             <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                             ( <parameters-joined-by,>*                                           ) <hasReturns?> =>                                             <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                             ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )               =>                                           (                         )  { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                             ( <parameters-joined-by,>*                                           )               =>                                           (                         )  { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>*                                           ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> > <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> > <name.value> ( <parameters-joined-by,>*                                           )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>*                                           )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   |                           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> > <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   |                           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> > <name.value> ( <parameters-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   |                           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   |                           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   ;

FunctionType
   : <hasReturn?> function ( <paramTypeNode-joined-by,>* ) <returnKeywordExists?returns> <returnTypeNode.source>
   |              function ( <paramTypeNode-joined-by,>* )
   ;

GroupBy
   : group by <variables-joined-by,>*
   ;

Having
   : having <expression.source>
   ;

If
   : <ladderParent?> if ( <condition.source> ) { <body.source> } else   <elseStatement.source>
   |                 if ( <condition.source> ) { <body.source> } else { <elseStatement.source> }
   |                 if ( <condition.source> ) { <body.source> }
   ;

IndexBasedAccessExpr
   : <expression.source> [ <index.source> ]
   ;

IntRangeExpr
   : <isWrappedWithParenthesis?> ( <startExpression.source> .. <endExpression.source> )
   | <isWrappedWithParenthesis?> ( <startExpression.source> ..                        )
   | <isWrappedWithBracket?>     [ <startExpression.source> .. <endExpression.source> ]
   | <isWrappedWithBracket?>     [ <startExpression.source> ..                        ]
   ;

Invocation
   : <actionInvocation?>      <async?start> <expression.source>  ->   <name.value> ( <argumentExpressions-joined-by,>* )
   | <expression.source>  .   <async?start>                           <name.value> ( <argumentExpressions-joined-by,>* )
   | <packageAlias.value> :   <async?start>                           <name.value> ( <argumentExpressions-joined-by,>* )
   |                          <async?start>                           <name.value> ( <argumentExpressions-joined-by,>* )
   ;

JoinStreamingInput
   : <unidirectionalBeforeJoin?> unidirectional join                <streamingInput.source> on <onExpression.source>
   | <unidirectionalAfterJoin?>                 join unidirectional <streamingInput.source> on <onExpression.source>
   |                                            join                <streamingInput.source> on <onExpression.source>
   ;

Lambda
   : <functionNode.source>
   ;

Limit
   : limit <limitValue>
   ;

Literal
   : <inTemplateLiteral?> <unescapedValue>
   : <inTemplateLiteral?>
   | <value>
   ;

Lock
   : lock { <body.source> }
   ;

Match
   : match <expression.source> { <patternClauses>* }
   ;

MatchPatternClause
   : <withoutCurlies?> <variableNode.source> =>   <statement.source>
   :                   <variableNode.source> => { <statement.source> }
   ;

MatchExpression
   : <expression.source> but { <patternClauses>* }
   ;

MatchExpressionPatternClause
   : <variableNode.source> => <statement.source>
   ;

NamedArgsExpr
   : <name.value> = <expression.source>
   ;

Next
   : next ;
   ;

Object
   : <noFieldsAvailable?>        <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> object {                                                                                     <initFunction.source> <functions>* };
   | <noPrivateFieldsAvailable?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> object { public { <publicFields-suffixed-by-;>* }                                            <initFunction.source> <functions>* };
   | <noPublicFieldAvailable?>   <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> object {                                          private { <privateFields-suffixed-by-;>* } <initFunction.source> <functions>* };
   |                             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> object { public { <publicFields-suffixed-by-;>* } private { <privateFields-suffixed-by-;>* } <initFunction.source> <functions>* };
   ;

OrderBy
   : order by <variables-joined-by,>*
   ;

OrderByVariable
   : <variableReference.source> <orderByType>
   ;

PostIncrement
   : <variable.source> <operator> ;
   ;

Record
   : <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> { <fields-suffixed-by-;>* };
   ;

RecordLiteralExpr
   : { <keyValuePairs-joined-by,>* }
   | { }
   ;

RecordLiteralKeyValue
   : <key.source> : <value.source>
   ;

Resource
   : <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <name.value> ( <parameters-joined-by,>* ) { <body.source> <workers>* }
   ;

RestArgsExpr
   : ... <expression.source>
   ;

Retry
   : retry ;
   ;

Return
   : <noExpressionAvailable?> return                       ;
   | <emptyBrackets?>         return                     ();
   |                          return <expression.source>   ;
   ;

SelectClause
   : <selectAll?> select                               * <groupBy.source> <having.source>
   : <selectAll?> select                               * <groupBy.source>
   : <selectAll?> select                               *                  <having.source>
   : <selectAll?> select                               *
   :              select <selectExpressions-joined-by,>* <groupBy.source> <having.source>
   :              select <selectExpressions-joined-by,>* <groupBy.source>
   :              select <selectExpressions-joined-by,>* <having.source>
   :              select <selectExpressions-joined-by,>*
   ;

SelectExpression
   : <identifierAvailable?> <expression.source> as <identifier>
   |                        <expression.source>
   ;

Service
   : <isServiceTypeUnavailable?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* service                                <name.value> bind <anonymousEndpointBind.source> <boundEndpoints-joined-by,>* { <variables>* <resources>* }
   | <isServiceTypeUnavailable?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* service                                <name.value> bind                                <boundEndpoints-joined-by,>* { <variables>* <resources>* }
   |                             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* service < <serviceTypeStruct.source> > <name.value> bind <anonymousEndpointBind.source> <boundEndpoints-joined-by,>* { <variables>* <resources>* }
   |                             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* service < <serviceTypeStruct.source> > <name.value> bind                                <boundEndpoints-joined-by,>* { <variables>* <resources>* }
   ;

SimpleVariableRef
   : <inTemplateLiteral?> {{ <packageAlias.value> : <variableName.value> }}
   | <inTemplateLiteral?> {{                        <variableName.value> }}
   |                         <packageAlias.value> : <variableName.value>
   |                                                <variableName.value>
   ;

StreamAction
   : <invokableBody.source>
   ;

StreamingInput
   : <windowTraversedAfterWhere?> <streamReference.source> <beforeStreamingCondition.source> <windowClause.source> <afterStreamingCondition.source> <aliasAvailable?> as <alias>
   | <windowTraversedAfterWhere?> <streamReference.source> <beforeStreamingCondition.source> <windowClause.source> <afterStreamingCondition.source>
   | <windowTraversedAfterWhere?> <streamReference.source>                                   <windowClause.source> <afterStreamingCondition.source> <aliasAvailable?> as <alias>
   | <windowTraversedAfterWhere?> <streamReference.source>                                   <windowClause.source> <afterStreamingCondition.source>
   | <windowTraversedAfterWhere?> <streamReference.source>                                   <windowClause.source> <afterStreamingCondition.source> <aliasAvailable?> as <alias>
   | <windowTraversedAfterWhere?> <streamReference.source>                                   <windowClause.source> <afterStreamingCondition.source>
   | <windowTraversedAfterWhere?> <streamReference.source> <beforeStreamingCondition.source> <windowClause.source>                                  <aliasAvailable?> as <alias>
   | <windowTraversedAfterWhere?> <streamReference.source> <beforeStreamingCondition.source> <windowClause.source>
   |                              <streamReference.source> <beforeStreamingCondition.source>                       <afterStreamingCondition.source> <aliasAvailable?> as <alias>
   |                              <streamReference.source> <beforeStreamingCondition.source>                       <afterStreamingCondition.source>
   |                              <streamReference.source> <beforeStreamingCondition.source>                                                        <aliasAvailable?> as <alias>
   |                              <streamReference.source> <beforeStreamingCondition.source>
   |                              <streamReference.source>                                                         <afterStreamingCondition.source> <aliasAvailable?> as <alias>
   |                              <streamReference.source>                                                         <afterStreamingCondition.source>
   |                              <streamReference.source>                                                                                          <aliasAvailable?> as <alias>
   |                              <streamReference.source>
   ;

StreamingQuery
   : from <streamingInput.source> <joinStreamingInput.source> <selectClause.source> <orderbyClause.source> <streamingAction.source>
   | from <streamingInput.source>                             <selectClause.source> <orderbyClause.source> <streamingAction.source>
   | from <streamingInput.source> <joinStreamingInput.source> <selectClause.source>                        <streamingAction.source>
   | from <streamingInput.source>                             <selectClause.source>                        <streamingAction.source>
   | from <streamingInput.source> <joinStreamingInput.source> <selectClause.source> <orderbyClause.source>
   | from <streamingInput.source>                             <selectClause.source> <orderbyClause.source>
   | from <streamingInput.source> <joinStreamingInput.source> <selectClause.source>
   | from <streamingInput.source>                             <selectClause.source>
   ;

StringTemplateLiteral
   : string\u0020` <expressions>* `
   ;

Table
   : table <configurationExpression.source>
   ;

TableQueryExpression
   : <tableQuery.source>
   ;

TableQuery
   : from <streamingInput.source> <joinStreamingInput.source> <selectClauseNode.source> <orderByNode.source> <limitClause.source>
   | from <streamingInput.source>                             <selectClauseNode.source> <orderByNode.source> <limitClause.source>
   | from <streamingInput.source>                             <selectClauseNode.source>                      <limitClause.source>
   | from <streamingInput.source> <joinStreamingInput.source> <selectClauseNode.source>                      <limitClause.source>
   | from <streamingInput.source> <joinStreamingInput.source> <selectClauseNode.source> <orderByNode.source>
   | from <streamingInput.source>                             <selectClauseNode.source> <orderByNode.source>
   | from <streamingInput.source>                             <selectClauseNode.source>
   ;

TernaryExpr
   : <condition.source> ? <thenExpression.source> : <elseExpression.source>
   ;

Throw
   : throw <expressions.source> ;
   ;

Transaction
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source>                                      { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> ,                                        onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source>                                                                             { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source>                                      { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with                                                                        onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction                                                                                                                { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source>                                      { <transactionBody.source> }
   : transaction with retries = <retryCount.source> ,                                        onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction with retries = <retryCount.source>                                                                             { <transactionBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source>                                      { <transactionBody.source> }
   : transaction with                                                                        onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction                                                                                                                { <transactionBody.source> }
   ;

Transform
   : transform { <body.source> }
   ;

Transformer
   : <public?public> transformer < <source.source> , <returnParameters-joined-by,>+ > <name.value> ( <parameters-joined-by,>* ) { <body.source> }
   | <public?public> transformer < <source.source> , <returnParameters-joined-by,>+ > <name.value> { <body.source> }
   | <public?public> transformer < <source.source> , <returnParameters-joined-by,>* >              { <body.source> }
   ;

Try
   : try { <body.source> } <catchBlocks>*  finally { <finallyBody.source> }
   | try { <body.source> } <catchBlocks>*
   ;

TupleDestructure
   : <declaredWithVar?> var ( <variableRefs-joined-by,>+ ) = <expression.source> ;
   |                        ( <variableRefs-joined-by,>+ ) = <expression.source> ;
   ;

TupleTypeNode
   : ( <memberTypeNodes-joined-by,>+ )
   ;

TypeCastExpr
   : ( <typeNode.source> ) <expression.source>
   ;

TypeConversionExpr
   : < <typeNode.source> , <transformerInvocation.source> > <expression.source>
   | < <typeNode.source> > <expression.source>
   ;

TypeDefinition
   : <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> <typeNode.source> | <valueSet-joined-by|>* ;
   | <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> type <name.value>                     <valueSet-joined-by|>* ;
   ;

TypedescExpression
   : <typeNode.source>
   ;

TypeofExpression
   : typeof <typeNode.source>
   ;

TypeInitExpr
   : <noExpressionAvailable?> new <hasParantheses?> (                           )
   : <noExpressionAvailable?> new
   | <noTypeAttached?>        new                   ( <expressions-joined-by,>* )
   |                          new <typeName.source> ( <expressions-joined-by,>* )
   ;

UnaryExpr
   : <operatorKind> <expression.source>
   ;

UnionTypeNode
   : <withParantheses?> ( <memberTypeNodes-joined-by|>* )
   |                      <memberTypeNodes-joined-by|>*
   ;

UserDefinedType
   : <anonStruct.source>
   | <nullableOperatorAvailable?> <packageAlias.value> : <typeName.value> ?
   | <nullableOperatorAvailable?>                        <typeName.value> ?
   |                              <packageAlias.value> : <typeName.value>
   |                                                     <typeName.value>
   |
   ;

ValueType
   : <emptyParantheses?> (            )
   : <withParantheses?>  ( <typeKind> )
   :                       <typeKind>
   ;

Variable
   : <endpoint?>                                                                                                                             endpoint <typeNode.source> <name.value> { <initialExpression.source> ; }
   | <endpoint?>                                                                                                                             endpoint <typeNode.source> <name.value> { }
   | <serviceEndpoint?>                                                                                                                      endpoint                   <name.value> 
   | <global?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> <const?const> <safeAssignment?>          <typeNode.source> <name.value> =? <initialExpression.source> ;
   | <global?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> <const?const>                            <typeNode.source> <name.value> =  <initialExpression.source> ;
   | <global?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                          <typeNode.source> <name.value>                               ;
   |                                                                                                                       <safeAssignment?>          <typeNode.source> <name.value> =? <initialExpression.source>
   |                                                                                                                                                  <typeNode.source> <name.value> =  <initialExpression.source>
   |           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                          <typeNode.source> <rest?...> <name.value>
   |                                                                                                                                                  <typeNode.source>
   ;

VariableDef
   : <endpoint?> <variable.source>
   : <variable.source> ;
   ;

Where
   : where <expression.source>
   ;

While
   : while ( <condition.source> ) { <body.source> }
   ;

WindowClause
   : window <functionInvocation.source>
   ;

Worker
   : worker <name.value> { <body.source> }
   ;

WorkerReceive
   : <expression.source> <- <workerName.value> ;
   ;

WorkerSend
   : <forkJoinedSend?> <expression.source> -> fork ;
   |                   <expression.source> -> <workerName.value> ;
   ;

XmlAttribute
   : <name.source> = <value.source>
   ;

XmlAttributeAccessExpr
   : <expression.source> @ [ <index.source> ]
   | <expression.source> @
   ;

XmlCommentLiteral
   : <root?> xml` <!-- <textFragments>* --> `
   |              <!-- <textFragments>* -->
   ;

XmlElementLiteral
   : <root?> xml` < <startTagName.source> <attributes>* > <content>* </ <endTagName.source> > `
   |              < <startTagName.source> <attributes>* > <content>* </ <endTagName.source> >
   | <root?> xml` < <startTagName.source> <attributes>* />`
   :              < <startTagName.source> <attributes>* />
   ;

XmlPiLiteral
   : <target.source> <dataTextFragments>*
   | <dataTextFragments>*
   | <target.source>
   ;

XmlQname
   : <prefix.value> : <localname.value>
   | <localname.value>
   ;

XmlQuotedString
   : <textFragments>*
   ;

XmlTextLiteral
   : <textFragments>*
   ;

Xmlns
   : xmlns <namespaceURI.source> as <prefix.value> ;
   | xmlns <namespaceURI.source> ;
   | <namespaceDeclaration.source>
   ;
