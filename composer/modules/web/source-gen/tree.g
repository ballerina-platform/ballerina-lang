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
   :            @ <packageAlias.value> : <annotationName.value> <expression.source>
   |            @ <annotationName.value>                        <expression.source>
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

Endpoint
   :  <annotationAttachments>* endpoint <endPointType.source> <name.value> <configurationExpression.source> ;
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
   | <isConstructor?>          <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>*                                                <name.value> ( <parameters-joined-by,>* <restParameters.source> )                                                   { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* function                                                    ( <parameters-joined-by,>* <restParameters.source> )                ( <returnParameters-joined-by,>+ ) { <endpointNodes>* <body.source> <workers>* }
   | <lambda?>                 <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* function                                                    ( <parameters-joined-by,>* <restParameters.source> )                                                   { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <restParameters.source> ) <hasReturns?>  returns <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <noVisibleReceiver?>      <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <restParameters.source> )                                                   { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> > <name.value> ( <parameters-joined-by,>* <restParameters.source> )                returns <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   | <hasReturns?>             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <restParameters.source> )                returns <returnTypeNode.source>    { <endpointNodes>* <body.source> <workers>* }
   |                           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function < <receiver.source> > <name.value> ( <parameters-joined-by,>* <restParameters.source> )                                                   { <endpointNodes>* <body.source> <workers>* }
   |                           <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* <public?public> function                       <name.value> ( <parameters-joined-by,>* <restParameters.source> )                                                   { <endpointNodes>* <body.source> <workers>* }
   ;

FunctionType
   : function ( <paramTypeNode-joined-by,>* ) <returnKeywordExists?returns> ( <returnParamTypeNode>+ )
   | function ( <paramTypeNode-joined-by,>* )
   ;

If
   : <ladderParent?> if ( <condition.source> ) { <body.source> } else   <elseStatement.source>
   |                 if ( <condition.source> ) { <body.source> } else { <elseStatement.source> }
   |                 if ( <condition.source> ) { <body.source> }
   ;

IndexBasedAccessExpr
   : <expression.source> [ <index.source> ]
   ;

Invocation
   : <actionInvocation?>      <async?async> <expression.source>  ->   <name.value> ( <argumentExpressions-joined-by,>* )
   | <expression.source>  .   <async?async> <name.value> ( <argumentExpressions-joined-by,>* )
   | <packageAlias.value> :   <async?async> <name.value> ( <argumentExpressions-joined-by,>* )
   |                          <async?async> <name.value> ( <argumentExpressions-joined-by,>* )
   ;

Lambda
   : <functionNode.source>
   ;

Literal
   : <inTemplateLiteral?> <unescapedValue>
   : <inTemplateLiteral?>
   | <value>
   ;

Match
   : match <expression.source> { <patternClauses>* }
   ;

MatchPatternClause
   : <withoutCurlies?> <variableNode.source> =>   <statement.source>
   :                   <variableNode.source> => { <statement.source> }
   ;

MatchExpression
   : <expr> but { <patternClauses>* }
   ;

MatchExpressionPatternClause
   : <variable.source> => <statement.source>
   ;

NamedArgsExpr
   : <name.value> = <expression.source>
   ;

Next
   : next ;
   ;

Object
   : <noFieldsAvailable?>        <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* type <name.value> object {                                                                                     <initFunction.source> <functions>* };
   | <noPrivateFieldsAvailable?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* type <name.value> object { public { <publicFields-suffixed-by-;>* }                                            <initFunction.source> <functions>* };
   | <noPublicFieldAvailable?>   <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* type <name.value> object {                                          private { <privateFields-suffixed-by-;>* } <initFunction.source> <functions>* };
   |                             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* type <name.value> object { public { <publicFields-suffixed-by-;>* } private { <privateFields-suffixed-by-;>* } <initFunction.source> <functions>* };
   ;

Record
   : <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* type <name.value> { <fields-suffixed-by-;>* };
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

Return
   : <noExpressionAvailable?> return                       ;
   | <emptyBrackets?>         return                     ();
   |                          return <expression.source>   ;
   ;

Service
   : <isServiceTypeUnavailable?> <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* service                                <name.value> bind <boundEndpoints-joined-by,>* { <variables>* <resources>* }
   |                             <annotationAttachments>* <documentationAttachments>* <deprecatedAttachments>* service < <serviceTypeStruct.source> > <name.value> bind <boundEndpoints-joined-by,>* { <variables>* <resources>* }
   ;

SimpleVariableRef
   : <inTemplateLiteral?> {{ <packageAlias.value> : <variableName.value> }}
   | <inTemplateLiteral?> {{                        <variableName.value> }}
   |                         <packageAlias.value> : <variableName.value>
   |                                                <variableName.value>
   ;

StringTemplateLiteral
   : string\u0020` <expressions>* `
   ;

TernaryExpr
   : <condition.source> ? <thenExpression.source> : <elseExpression.source>
   ;

Throw
   : throw <expressions.source> ;
   ;

Transaction
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source> ,                                    { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> ,                                        onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> ,                                                                           { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source> ,                                    { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with                                                                        onabort = <onAbortFunction.source> { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction                                                                                                                { <transactionBody.source> } onretry { <onRetryBody.source> }
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction with retries = <retryCount.source> , oncommit = <onCommitFunction.source> ,                                    { <transactionBody.source> }
   : transaction with retries = <retryCount.source> ,                                        onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source> , onabort = <onAbortFunction.source> { <transactionBody.source> }
   : transaction with retries = <retryCount.source> ,                                                                           { <transactionBody.source> }
   : transaction with                                 oncommit = <onCommitFunction.source> ,                                    { <transactionBody.source> }
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
   : <declaredWithVar?> var ( <variableRefs-joined-by,>+ ) = <expression.source>;
   |                        ( <variableRefs-joined-by,>+ ) = <expression.source>;
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

TypeofExpression
   : typeof <typeNode.source>
   ;

TypeInitExpr
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
   | <packageAlias.value> : <typeName.value>
   | <typeName.value>
   |
   ;

ValueType
   : <withParantheses?> ( <typeKind> )
   :                      <typeKind>
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

While
   : while ( <condition.source> ) { <body.source> }
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
