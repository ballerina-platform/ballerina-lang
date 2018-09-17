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
   : <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* action <name.value> ( <parameters-joined-by,>* ) ( <returnParameters-joined-by,>+ ) { <body.source> <workers>* }
   | <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* action <name.value> ( <parameters-joined-by,>* )                                    { <body.source> <workers>* }
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

ArrayType
   : <isRestParam?> <grouped?> ( <elementType.source> )
   | <isRestParam?>              <elementType.source>
   |                <grouped?> ( <elementType.source> <dimensionAsString> )
   |                             <elementType.source> <dimensionAsString>
   ;

ArrowExpr
   : <hasParantheses?> ( <parameters-joined-by,>* ) => <expression.source>
   |                     <parameters-joined-by,>*   => <expression.source>
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
   : <isElseBlock?> else { <statements>* }
   |                       <statements>*
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
   : <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> connector <name.value> ( <parameters-joined-by,>* ) { <variableDefs>* <actions>* }
   | <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> connector <name.value> ( <parameters-joined-by,>* ) { <actions>* }
   ;

ConnectorInitExpr
   : create <connectorType.source> ( <expressions-joined-by,>* )
   | create <connectorType.source> ( )
   ;

ConstrainedType
   : <type.source> < <constraint.source> >
   ;

Documentation
   : <startDoc> <documentationText> <attributes>* }
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
   |  <isConfigAssignment?> <annotationAttachments>* endpoint <endPointType.source> <name.value> = <configurationExpression.source> ;
   |                        <annotationAttachments>* endpoint <endPointType.source> <name.value>   <configurationExpression.source> ;
   ;

EndpointType
   : < <constraint.source> >
   ;

ExpressionStatement
   : <expression.source> ;
   ;

FieldBasedAccessExpr
   : <errorLifting?> <expression.source> ! <fieldName.value>
   |                 <expression.source> . <fieldName.value>
   ;

Foreach
   : <withParantheses?> foreach ( <variables-joined-by,>* in <collection.source> ) { <body.source> }
   |                    foreach   <variables-joined-by,>* in <collection.source>   { <body.source> }
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
   | <isConstructor?>          <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public>                                                  <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <isConstructor?>          <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public>                                                  <name.value> ( <allParams-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <interface?>              <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>                                                  ;
   | <interface?>              <documentationAttachments>* <annotationAttachments>*  <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                                                                       ;
   | <interface?>              <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>*                                           ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>                                                  ;
   | <interface?>              <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>*                                           )                                                                                                                                       ;
   | <lambda?>                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <isStreamAction?> =>                                                          ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <isStreamAction?> =>                                                          ( <allParams-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                               ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> ) <hasReturns?> =>                                             <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                               ( <allParams-joined-by,>*                                           ) <hasReturns?> =>                                             <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                               ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )               =>                                                                        { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                               ( <allParams-joined-by,>*                                           )               =>                                                                        { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>*                                           ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <objectOuterFunction?>    <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function  <objectOuterFunctionTypeName.value> :: <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <objectOuterFunction?>    <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function  <objectOuterFunctionTypeName.value> :: <name.value> ( <allParams-joined-by,>*                                           ) <hasReturns?>    returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <objectOuterFunction?>    <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function  <objectOuterFunctionTypeName.value> :: <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <objectOuterFunction?>    <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function  <objectOuterFunctionTypeName.value> :: <name.value> ( <allParams-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> >                   <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> >                   <name.value> ( <allParams-joined-by,>*                                           )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>*                                           )                  returns <returnTypeAnnotationAttachments>*  <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   |                           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> >                   <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   |                           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> >                   <name.value> ( <allParams-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   |                           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>* <hasRestParams?,> <restParameters.source> )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   |                           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> function                                         <name.value> ( <allParams-joined-by,>*                                           )                                                                                         { <endpointNodes>* <body.source> <workers>* }
   ;

FunctionType
   : <hasReturn?> <withParantheses?> ( function ( <paramTypeNode-joined-by,>* ) <returnKeywordExists?returns> <returnTypeNode.source> )
   : <hasReturn?>                      function ( <paramTypeNode-joined-by,>* ) <returnKeywordExists?returns> <returnTypeNode.source>
   |              <withParantheses?> ( function ( <paramTypeNode-joined-by,>* ) )
   |                                   function ( <paramTypeNode-joined-by,>* )
   ;

GroupBy
   : group by <variables-joined-by,>*
   ;

Having
   : having <expression.source>
   ;

If
   : <ladderParent?> <isElseIfBlock?> else if <condition.source> { <body.source> } <elseStatement.source>
   |                 <isElseIfBlock?> else if <condition.source> { <body.source> } <elseStatement.source>
   |                 <isElseIfBlock?> else if <condition.source> { <body.source> }
   | <ladderParent?>                       if <condition.source> { <body.source> } <elseStatement.source>
   |                                       if <condition.source> { <body.source> }
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
   | <startTemplateLiteral?> <endTemplateLiteral?> }}            <value>
   | <lastNodeValue?>        <endTemplateLiteral?> }}            <value>
   |                         <endTemplateLiteral?> }}
   | <startTemplateLiteral?>                                     <value>
   : <inTemplateLiteral?>                               <unescapedValue>
   | <inTemplateLiteral?>
   | <emptyParantheses?>                              (                  )
   |                                                             <value>
   ;

Lock
   : lock { <body.source> }
   ;

Match
   : match <expression.source> { <patternClauses>* }
   ;

MatchPatternClause
   : <withCurlies?> <variableNode.source> => { <statement.source> }
   |                <variableNode.source> =>   <statement.source>
   ;

MatchExpression
   : <expression.source> but { <patternClauses-joined-by,>* }
   ;

MatchExpressionPatternClause
   : <withCurlies?> <variableNode.source> => { <statement.source> }
   |                <variableNode.source> =>   <statement.source>
   ;

NamedArgsExpr
   : <name.value> = <expression.source>
   ;

Next
   : continue ;
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

RecordLiteralExpr
   : { <keyValuePairs-joined-by,>* }
   | {                             }
   ;

RecordLiteralKeyValue
   : <key.source> : <value.source>
   ;

Resource
   : <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <name.value> ( <parameters-joined-by,>* ) { <endpointNodes>* <body.source> <workers>* }
   ;

RestArgsExpr
   : ... <expression.source>
   ;

Retry
   : retry ;
   ;

Return
   : <noExpressionAvailable?> return                     ;
   |                          return <expression.source> ;
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
   | <isServiceTypeUnavailable?> <bindNotAvailable?> <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* service                                <name.value>                                                                  { <endpointNodes>* <variables>* <resources>* }
   : <isServiceTypeUnavailable?>                     <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* service                                <name.value> bind <anonymousEndpointBind.source> <boundEndpoints-joined-by,>* { <endpointNodes>* <variables>* <resources>* }
   | <isServiceTypeUnavailable?>                     <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* service                                <name.value> bind                                <boundEndpoints-joined-by,>* { <endpointNodes>* <variables>* <resources>* }
   |                             <bindNotAvailable?> <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* service < <serviceTypeStruct.source> > <name.value>                                                                  { <endpointNodes>* <variables>* <resources>* }
   |                                                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* service < <serviceTypeStruct.source> > <name.value> bind <anonymousEndpointBind.source> <boundEndpoints-joined-by,>* { <endpointNodes>* <variables>* <resources>* }
   |                                                 <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* service < <serviceTypeStruct.source> > <name.value> bind                                <boundEndpoints-joined-by,>* { <endpointNodes>* <variables>* <resources>* }
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
   : <startTemplate> <expressions>* `
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
   : <notVisible?>
   : <isObjectType?>                                                          <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> object { <typeNode.source> }                           ;
   | <isRecordType?> <isRecordKeywordAvailable?>                              <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> type <name.value> record { <typeNode.source> }                           ;
   | <isRecordType?>                                                          <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> type <name.value>        { <typeNode.source> }                           ;
   |                                                                          <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> type <name.value>          <typeNode.source>    | <valueSet-joined-by|>* ;
   |                                                                          <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> type <name.value>                                 <valueSet-joined-by|>* ;
   ;

ObjectType
   : <fields>* <initFunction.source> <functions>*
   ;

RecordType
   : <isRestFieldAvailable?> <fields>* <restFieldType.source> ...
   | <sealed?>               <fields>*                      ! ...
   |                         <fields>*
   ;

TypedescExpression
   : <typeNode.source>
   ;

TypeofExpression
   : typeof <typeNode.source>
   ;

TypeInitExpr
   : <noExpressionAvailable?> <noTypeAttached?> new               <hasParantheses?> (                           )
   : <noExpressionAvailable?> <noTypeAttached?> new
   : <noExpressionAvailable?>                   new <type.source>                   (                           )
   | <noTypeAttached?>                          new                                 ( <expressions-joined-by,>* )
   |                                            new <type.source>                   ( <expressions-joined-by,>* )
   ;

UnaryExpr
   : <inTemplateLiteral?> {{ <operatorKind> <expression.source> }}
   |                         <operatorKind> <expression.source>
   ;

UnionTypeNode
   : <emptyParantheses?> (                               )
   | <withParantheses?>  ( <memberTypeNodes-joined-by|>* )
   |                       <memberTypeNodes-joined-by|>*
   ;

UserDefinedType
   : <isAnonType?> <anonType.source>
   | <nullableOperatorAvailable?> <grouped?> ( <packageAlias.value> : <typeName.value> ? )
   | <nullableOperatorAvailable?>              <packageAlias.value> : <typeName.value> ?
   | <nullableOperatorAvailable?> <grouped?> (                        <typeName.value> ? )
   | <nullableOperatorAvailable?>                                     <typeName.value> ?
   |                              <grouped?> ( <packageAlias.value> : <typeName.value>   )
   |                                           <packageAlias.value> : <typeName.value>
   |                              <grouped?> (                        <typeName.value>   )
   |                                                                  <typeName.value>
   ;

ValueType
   : <emptyParantheses?> (                                           )
   | <withParantheses?>  ( <typeKind> <nullableOperatorAvailable?> ? )
   | <withParantheses?>  ( <typeKind>                                )
   |                       <typeKind> <nullableOperatorAvailable?> ?
   |                       <typeKind>
   ;

Variable
   : <isAnonType?> <endWithSemicolon?> <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                     <const?const>     record    { <typeNode.source> }          <name.value>                                ;
   | <isAnonType?> <endWithComma?>     <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                     <const?const>     record    { <typeNode.source> }          <name.value>                                ,
   | <isAnonType?>                     <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                     <const?const>     record    { <typeNode.source> }          <name.value>
   | <noVisibleName?>                                                                                                                                                  <typeNode.source>
   | <endpoint?>                                                                                                                                           endpoint    <typeNode.source>            <name.value> {  <initialExpression.source> ; }
   | <endpoint?>                                                                                                                                           endpoint    <typeNode.source>            <name.value> {                               }
   | <serviceEndpoint?>                                                                                                                                    endpoint                                 <name.value>
   | <defaultable?>      <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                 <typeNode.source>            <name.value> =  <initialExpression.source>
   | <defaultable?>      <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                                              <name.value> =  <initialExpression.source>
   | <global?>           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> <const?const>     <safeAssignment?>             <typeNode.source>            <name.value> =? <initialExpression.source> ;
   | <global?>           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> <const?const>                                   <typeNode.source>            <name.value> =  <initialExpression.source> ;
   | <global?>           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> <const?const>                                   <typeNode.source>            <name.value>                                ;
   | <global?>           <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                 <typeNode.source>            <name.value>                                ;
   | <endWithSemicolon?>                                                                                                                 <safeAssignment?>             <typeNode.source>            <name.value> =? <initialExpression.source>  ;
   | <endWithComma?>                                                                                                                     <safeAssignment?>             <typeNode.source>            <name.value> =? <initialExpression.source>  ,
   | <endWithSemicolon?> <inObject?>                                                                   <public?public> <private?private>                               <typeNode.source>            <name.value> =  <initialExpression.source>  ;
   | <endWithComma?>     <inObject?>                                                                   <public?public> <private?private>                               <typeNode.source>            <name.value> =  <initialExpression.source>  ,
   | <endWithSemicolon?> <inObject?> <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> <private?private>                   <typeNode.source> <rest?...> <name.value>                                ;
   | <endWithComma?>     <inObject?> <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public> <private?private>                   <typeNode.source> <rest?...> <name.value>                                ,
   | <endWithSemicolon?>                                                                                                                                               <typeNode.source>            <name.value> =  <initialExpression.source>  ;
   | <endWithComma?>                                                                                                                                                   <typeNode.source>            <name.value> =  <initialExpression.source>  ,
   | <endWithSemicolon?> <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                 <typeNode.source> <rest?...> <name.value>                                ;
   | <endWithComma?>     <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>*                                                                 <typeNode.source> <rest?...> <name.value>                                ,
   |                                                                                                                                     <safeAssignment?>             <typeNode.source>            <name.value> =? <initialExpression.source>
   |                                                                                                                                                                   <typeNode.source>            <name.value> =  <initialExpression.source>
   | <noVisibleType?>    <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public>                                                                   <rest?...> <name.value>
   |                     <documentationAttachments>* <annotationAttachments>* <deprecatedAttachments>* <public?public>                                                 <typeNode.source> <rest?...> <name.value>
   ;

VariableDef
   : <endpoint?>    <variable.source>
   | <defaultable?> <variable.source>
   |                <variable.source> ;
   ;

Where
   : where <expression.source>
   ;

While
   : while <condition.source> { <body.source> }
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
   : <root?> <startLiteral> <!-- <textFragments>* --> `
   |                        <!-- <textFragments>* -->
   ;

XmlElementLiteral
   : <root?> <startLiteral> < <startTagName.source> <attributes>*  > <content>* </ <endTagName.source> > `
   | <root?> <startLiteral> < <startTagName.source> <attributes>* />                                     `
   |                        < <startTagName.source> <attributes>*  > <content>* </ <endTagName.source> >
   |                        < <startTagName.source> <attributes>* />
   ;

XmlPiLiteral
   : <root?> <startLiteral> <? <target.source> <dataTextFragments>* ?> `
   |                        <? <target.source> <dataTextFragments>* ?>
   ;

XmlQname
   : <prefix.value> : <localname.value>
   | <localname.value>
   ;

XmlQuotedString
   : <textFragments>*
   ;

XmlTextLiteral
   : <root?> <startLiteral> <textFragments>* `
   |                        <textFragments>*
   ;

Xmlns
   : xmlns <namespaceURI.source> as <prefix.value> ;
   | xmlns <namespaceURI.source> ;
   | <namespaceDeclaration.source>
   ;
