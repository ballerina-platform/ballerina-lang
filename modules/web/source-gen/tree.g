PackageDeclaration
   : package <packageName-joined-by.>* ;

Import
   : import <packageName-joined-by.>* ;

Identifier
   : <value>
   ;

Action
   : action <name.value> ( <parameters-joined-by,>* ) ( <returnParameters-joined-by,>+ ) { <body.source> }
   | action <name.value> ( <parameters-joined-by,>* ) { <body.source> }
   ;

Annotation
   : annotation <name.value> { <attributes-suffixed-by-;>* }
   | annotation <name.value> attach resource { }
   ;

AnnotationAttachment
   : @ <packageAlias.value> : <annotationName.value> { <attributes-joined-by,>* }
   ;

AnnotationAttachmentAttribute
   : <name> : <value.source>
   ;

AnnotationAttachmentAttributeValue
   : <value.source>
   | [ <valueArray-joined-by,>+ ]
   ;

AnnotationAttribute
   : <typeNode.source> <name.value>
   ;

ArrayLiteralExpr
   : [ <expressions-joined-by,>* ]
   ;

ArrayType
   : <elementType.source> [ ]
   ;

Assignment
   : <declaredWithVar?var> <variables-joined-by,>* = <expression.source> ;
   | <variables-joined-by,>* = <expression.source> ;
   ;

BinaryExpr
   : <leftExpression.source> <operatorKind> <rightExpression.source>
   ;

Block
   : <statements>*
   | 
   ;

Break
   : break ;
   ;

BuiltInRefType
   : <typeKind>
   ;

Comment
   : <comment>
   ;

Connector
   : connector <name.value> ( <parameters-joined-by,>* ) { <variableDefs>* <actions>* }
   | connector <name.value> ( <parameters-joined-by,>* ) { <actions>* }
   ;

ConnectorInitExpr
   : create <connectorType.source> ( <expressions-joined-by,>* )
   | create <connectorType.source> ( )
   ;

ConstrainedType
   : 
   ;

Continue
   : continue ;
   ;

ExpressionStatement
   : <expression.source> ;
   ;

FieldBasedAccessExpr
   : <expression.source> . <fieldName.value>
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
   : <annotationAttachments>* function <name.value> ( <parameters-joined-by,>* ) ( <returnParameters-joined-by,>+ ) { <body.source> }
   | <annotationAttachments>* function <name.value> ( <parameters-joined-by,>* ) { <body.source> }
   ;

FunctionType
   : function ( <paramTypeNode-joined-by,>* ) <returnKeywordExists?returns> ( <returnParamTypeNode>* )
   ;

If
   : if ( <condition.source> ) { <body.source> } else <elseStatement.source> <ladderParent?>
   | if ( <condition.source> ) { <body.source> } else { <elseStatement.source> }
   | if ( <condition.source> ) { <body.source> }
   ;

IndexBasedAccessExpr
   : <expression.source> [ <index.source> ]
   ;

Invocation
   : <packageAlias.value> : <name.value> ( <argumentExpressions-joined-by,>* )
   | <expression.source> . <name.value> ( <argumentExpressions-joined-by,>* )
   | <name.value> ( <argumentExpressions-joined-by,>* )
   ;

Lambda
   : <functionNode.source>
   ;

Literal
   : <value>
   ;

RecordLiteralExpr
   : { <keyValuePairs-joined-by,>* }
   | { }
   ;

RecordLiteralKeyValue
   : <key.source> : <value.source>
   ;

Resource
   : <annotationAttachments>* resource <name.value> ( <parameters-joined-by,>* ) { <workers>+ }
   : <annotationAttachments>* resource <name.value> ( <parameters-joined-by,>* ) { <body.source> }
   ;

Return
   : return <expressions-joined-by-,>* ;
   ;

Service
   : <annotationAttachments>* service < <protocolPackageIdentifier.value> > <name.value> { <variables>* <resources>* }
   ;

SimpleVariableRef
   : <packageAlias.value> : <variableName.value>
   | <variableName.value>
   ;

Struct
   : <annotationAttachments>* struct <name.value> { <fields-suffixed-by-;>* }
   ;

Throw
   : throw <expressions.source> ;
   ;

Transform
   : transform { <body.source> }
   ;

TypeCastExpr
   : ( <typeNode.source> ) <expression.source>
   ;

TypeConversionExpr
   : < <typeNode.source> > <expression.source>
   ;

UnaryExpr
   : <operatorKind> <expression.source>
   ;

UserDefinedType
   : <packageAlias.value> : <typeName.value>
   | <typeName.value>
   | 
   ;

ValueType
   : <typeKind>
   ;

Variable
   : <const?const> <typeNode.source> <name.value> = <initialExpression.source> ;
   | <typeNode.source> <name.value> = <initialExpression.source> <global?;>
   | <typeNode.source> <name.value> = <initialExpression.source>
   | <typeNode.source> <name.value>
   | <typeNode.source>
   ;

VariableDef
   : <variable.source> ;
   ;

While
   : while ( <condition.source> ) { <body.source> }
   ;

Worker
   : worker <name.value> { <body.source> }
   ;

WorkerReceive
   : <expressions>* <- <workerName.value> ;
   ;

WorkerSend
   : <expressions>* -> <workerName.value> ;
   ;

XmlAttribute
   : <name.source> <value.source> =
   | <value.source> =
   | =
   ;

XmlCommentLiteral
   : <textFragments>*
   | 
   ;

XmlElementLiteral
   : < <attributes>* > <content>* <endTagName.source>
   | < <attributes>* />
   ;

XmlPiLiteral
   : <target.source> <dataTextFragments>*
   | <dataTextFragments>*
   | <target.source>
   ;

XmlQname
   : </ <localname.value> >
   | 
   ;

XmlQuotedString
   : <textFragments>*
   | 
   ;

XmlTextLiteral
   : <textFragments>*
   ;
