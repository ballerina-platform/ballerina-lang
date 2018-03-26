parser grammar BallerinaParser;

options {
    language = Java;
    tokenVocab = BallerinaLexer;
}

//todo revisit blockStatement

// starting point for parsing a bal file
compilationUnit
    :   packageDeclaration?
        (importDeclaration | namespaceDeclaration)*
        (annotationAttachment* documentationAttachment? deprecatedAttachment? definition)*
        EOF
    ;

packageDeclaration
    :   PACKAGE fullyQualifiedPackageName version? SEMICOLON
    ;

version
    : (VERSION Identifier)
    ;

importDeclaration
    :   IMPORT (packageName DIV)? fullyQualifiedPackageName version? (AS packageAlias)? SEMICOLON
    ;

fullyQualifiedPackageName
    :   packageName (DOT packageName)*
    ;

packageName
    :   Identifier
    ;

packageAlias
    :   packageName
    ;

definition
    :   serviceDefinition
    |   functionDefinition
    |   structDefinition
    |   typeDefinition
    |   enumDefinition
    |   constantDefinition
    |   annotationDefinition
    |   globalVariableDefinition
    |   globalEndpointDefinition
    |   transformerDefinition
    ;

serviceDefinition
    :   SERVICE (LT nameReference GT)? Identifier serviceEndpointAttachments? LEFT_BRACE serviceBody RIGHT_BRACE
    ;

serviceEndpointAttachments
    :   BIND nameReference (COMMA nameReference)*
    ;

serviceBody
    :  endpointDeclaration* variableDefinitionStatement* resourceDefinition*
    ;

resourceDefinition
    :   annotationAttachment* documentationAttachment? deprecatedAttachment? Identifier LEFT_PARENTHESIS resourceParameterList? RIGHT_PARENTHESIS LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

resourceParameterList
    :   ENDPOINT Identifier (COMMA parameterList)?
    |   parameterList
    ;

callableUnitBody
    :  endpointDeclaration* statement*
    |  endpointDeclaration* workerDeclaration+
    ;


functionDefinition
    :   (PUBLIC)? (NATIVE)? FUNCTION (LT parameter GT)? Identifier LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS returnParameter? (LEFT_BRACE callableUnitBody RIGHT_BRACE | SEMICOLON)
    |   (PUBLIC)? (NATIVE)? FUNCTION Identifier DOUBLE_COLON Identifier LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS returnParameter? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

lambdaFunction
    :  FUNCTION LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS returnParameter? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

structDefinition
    :   (PUBLIC)? STRUCT Identifier LEFT_BRACE structBody RIGHT_BRACE
    ;

structBody
    :   fieldDefinition* privateStructBody?
    ;

privateStructBody
    :   PRIVATE COLON fieldDefinition*
    ;

// Object rule is specifically defined here to fix formatting.
typeDefinition
    :   (PUBLIC)? TYPE_TYPE Identifier OBJECT LEFT_BRACE objectBody RIGHT_BRACE | (PUBLIC)? TYPE_TYPE Identifier typeName
    ;

objectBody
    : publicObjectFields? privateObjectFields? objectInitializer? objectFunctions?
    ;

publicObjectFields
    :   PUBLIC LEFT_BRACE objectFieldDefinition* RIGHT_BRACE
    ;

privateObjectFields
    :   PRIVATE LEFT_BRACE objectFieldDefinition* RIGHT_BRACE
    ;

objectInitializer
    :   annotationAttachment* documentationAttachment? (PUBLIC)? (NATIVE)? NEW objectInitializerParameterList LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

objectInitializerParameterList
    :   LEFT_PARENTHESIS objectParameterList? RIGHT_PARENTHESIS
    ;

objectFunctions
    : (annotationAttachment* documentationAttachment? deprecatedAttachment? objectFunctionDefinition)+
    ;

// TODO merge with fieldDefinition later
objectFieldDefinition
    :   typeName Identifier (COLON expression)? (COMMA | SEMICOLON)
    ;

// TODO try to merge with formalParameterList later
objectParameterList
    :   (objectParameter | objectDefaultableParameter) (COMMA (objectParameter | objectDefaultableParameter))* (COMMA restParameter)?
    |   restParameter
    ;

// TODO try to merge with parameter later
objectParameter
    :   annotationAttachment* typeName? Identifier
    ;

// TODO try to merge with defaultableParameter later
objectDefaultableParameter
    :   objectParameter COLON expression
    ;

// TODO merge with functionDefinition later
objectFunctionDefinition
    :   (PUBLIC)? (NATIVE)? FUNCTION objectCallableUnitSignature (LEFT_BRACE callableUnitBody RIGHT_BRACE | SEMICOLON)
    ;

//TODO merge with callableUnitSignature later
objectCallableUnitSignature
    :   Identifier LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS returnParameter?
    ;


annotationDefinition
    : (PUBLIC)? ANNOTATION  (LT attachmentPoint (COMMA attachmentPoint)* GT)?  Identifier userDefineTypeName? SEMICOLON
    ;

enumDefinition
    : (PUBLIC)? ENUM Identifier LEFT_BRACE enumFieldList RIGHT_BRACE
    ;

enumFieldList
    :  enumField (COMMA enumField)*
    ;

enumField
    : Identifier
    ;

globalVariableDefinition
    :   (PUBLIC)? typeName Identifier ((ASSIGN | SAFE_ASSIGNMENT) expression )? SEMICOLON
    ;

transformerDefinition
    :   (PUBLIC)? TRANSFORMER LT parameterList GT (Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS)? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

attachmentPoint
     : SERVICE
     | RESOURCE
     | FUNCTION
     | STRUCT
     | ENUM
     | ENDPOINT
     | CONST
     | PARAMETER
     | ANNOTATION
     | TRANSFORMER
     ;

constantDefinition
    :   (PUBLIC)? CONST valueTypeName Identifier (ASSIGN | SAFE_ASSIGNMENT) expression SEMICOLON
    ;

workerDeclaration
    :   WORKER Identifier LEFT_BRACE workerBody RIGHT_BRACE
    ;

workerBody
    :   statement*
    ;

globalEndpointDefinition
    :   PUBLIC? endpointDeclaration
    ;

endpointDeclaration
    :   annotationAttachment* ENDPOINT endpointType Identifier (LEFT_BRACE (recordKeyValue (COMMA recordKeyValue)*)? RIGHT_BRACE | ASSIGN variableReference)? SEMICOLON
    ;

endpointType
    :   nameReference
    ;

// Removed to fix formatting
//endpointInitlization
//    :   recordLiteral
//    |   ASSIGN variableReference
//    ;

typeName
    :   simpleTypeName                                                      # simpleTypeNameLabel
    |   annotatedTypeName                                                   # annotatedTypeNameLabel
    |   typeName (LEFT_BRACKET RIGHT_BRACKET)+                              # arrayTypeNameLabel
    |   typeName PIPE NullLiteral                                           # nullableTypeNameLabel
    |   typeName (PIPE typeName)+                                           # unionTypeNameLabel
    |   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS                         # groupTypeNameLabel
    |   LEFT_PARENTHESIS typeName (COMMA typeName)* RIGHT_PARENTHESIS       # tupleTypeName
    |   OBJECT LEFT_BRACE objectBody RIGHT_BRACE                            # objectTypeNameLabel
    ;

annotatedTypeName
    :   annotationAttachment+ simpleTypeName
    ;

// Temporary production rule name
simpleTypeName
    :   NullLiteral
    |   TYPE_ANY
    |   TYPE_DESC
    |   valueTypeName
    |   referenceTypeName
    ;

builtInTypeName
     :   TYPE_ANY
     |   TYPE_DESC
     |   valueTypeName
     |   builtInReferenceTypeName
     |   simpleTypeName (LEFT_BRACKET RIGHT_BRACKET)+
     ;

referenceTypeName
    :   builtInReferenceTypeName
    |   userDefineTypeName
    |   anonStructTypeName
    ;

userDefineTypeName
    :   nameReference
    ;

anonStructTypeName
    : STRUCT LEFT_BRACE structBody RIGHT_BRACE
    ;

valueTypeName
    :   TYPE_BOOL
    |   TYPE_INT
    |   TYPE_FLOAT
    |   TYPE_STRING
    |   TYPE_BLOB
    ;

builtInReferenceTypeName
    :   TYPE_MAP (LT typeName GT)?
    |   TYPE_FUTURE (LT typeName GT)?
    |   TYPE_XML (LT (LEFT_BRACE xmlNamespaceName RIGHT_BRACE)? xmlLocalName GT)?
    |   TYPE_JSON (LT nameReference GT)?
    |   TYPE_TABLE (LT nameReference GT)?
    |   TYPE_STREAM (LT nameReference GT)?
    |   functionTypeName
    ;

functionTypeName
    :   FUNCTION LEFT_PARENTHESIS (parameterList | parameterTypeNameList)? RIGHT_PARENTHESIS returnParameter?
    ;

xmlNamespaceName
    :   QuotedStringLiteral
    ;

xmlLocalName
    :   Identifier
    ;

annotationAttachment
    :   AT nameReference (LEFT_BRACE (recordKeyValue (COMMA recordKeyValue)*)? RIGHT_BRACE)?
    ;

 //============================================================================================================
// STATEMENTS / BLOCKS

statement
    :   variableDefinitionStatement
    |   assignmentStatement
    |   tupleDestructuringStatement
    |   compoundAssignmentStatement
    |   postIncrementStatement
    |   ifElseStatement
    |   matchStatement
    |   foreachStatement
    |   whileStatement
    |   nextStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   returnStatement
    |   (triggerWorker | workerReply)
    |   expressionStmt
    |   transactionStatement
    |   abortStatement
    |   failStatement
    |   lockStatement
    |   namespaceDeclarationStatement
    |   wheneverStatement
    |   streamingQueryStatement
    ;

variableDefinitionStatement
    :   typeName Identifier ((ASSIGN | SAFE_ASSIGNMENT) (expression | actionInvocation))? SEMICOLON
    ;

recordLiteral
    :   LEFT_BRACE (recordKeyValue (COMMA recordKeyValue)*)? RIGHT_BRACE
    ;

recordKeyValue
    :   recordKey COLON recordValue
    ;

recordKey
    :   Identifier
    |   simpleLiteral
    ;

recordValue
    :   expression
    ;

arrayLiteral
    :   LEFT_BRACKET expressionList? RIGHT_BRACKET
    ;

typeInitExpr
    :   NEW (LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS)?
    |   NEW userDefineTypeName LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

assignmentStatement
    :   (VAR)? variableReference (ASSIGN | SAFE_ASSIGNMENT) (expression | actionInvocation) SEMICOLON
    ;

tupleDestructuringStatement
    :   VAR? LEFT_PARENTHESIS variableReferenceList RIGHT_PARENTHESIS ASSIGN (expression | actionInvocation) SEMICOLON
    |   LEFT_PARENTHESIS parameterList RIGHT_PARENTHESIS ASSIGN (expression | actionInvocation) SEMICOLON
    ;

compoundAssignmentStatement
    :   variableReference compoundOperator expression SEMICOLON
    ;

compoundOperator
    :   COMPOUND_ADD
    |   COMPOUND_SUB
    |   COMPOUND_MUL
    |   COMPOUND_DIV
    ;

postIncrementStatement
    :   variableReference postArithmeticOperator SEMICOLON
    ;

postArithmeticOperator
    :   INCREMENT
    |   DECREMENT
    ;

variableReferenceList
    :   variableReference (COMMA variableReference)*
    ;

ifElseStatement
    :  ifClause elseIfClause* elseClause? RIGHT_BRACE
    ;

ifClause
    :   IF LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody
    ;

elseIfClause
    :   RIGHT_BRACE ELSE IF LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody
    ;

elseClause
    :   RIGHT_BRACE ELSE LEFT_BRACE codeBlockBody
    ;

matchStatement
    :   MATCH  expression  LEFT_BRACE matchPatternClause+ RIGHT_BRACE
    ;

matchPatternClause
    :   typeName EQUAL_GT (statement | (LEFT_BRACE nonEmptyCodeBlockBody RIGHT_BRACE))
    |   typeName Identifier EQUAL_GT (statement | (LEFT_BRACE nonEmptyCodeBlockBody RIGHT_BRACE))
    ;

foreachStatement
    :   FOREACH LEFT_PARENTHESIS? variableReferenceList IN  (expression | intRangeExpression) RIGHT_PARENTHESIS? LEFT_BRACE codeBlockBody RIGHT_BRACE
    ;

intRangeExpression
    :   (LEFT_BRACKET|LEFT_PARENTHESIS) expression RANGE expression? (RIGHT_BRACKET|RIGHT_PARENTHESIS)
    ;

whileStatement
    :   WHILE LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody RIGHT_BRACE
    ;

nextStatement
    :   NEXT SEMICOLON
    ;

breakStatement
    :   BREAK SEMICOLON
    ;

// typeName is only message
forkJoinStatement
    : FORK LEFT_BRACE workerDeclaration* joinClause? timeoutClause? RIGHT_BRACE
    ;

// below typeName is only 'message[]'
joinClause
    :   RIGHT_BRACE JOIN (LEFT_PARENTHESIS joinConditions RIGHT_PARENTHESIS)? LEFT_PARENTHESIS codeBlockParameter RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody
    ;

joinConditions
    :   SOME integerLiteral (workerReference (COMMA workerReference)*)?     # anyJoinCondition
    |   ALL (workerReference (COMMA workerReference)*)?                     # allJoinCondition
    ;

// below typeName is only 'message[]'
timeoutClause
    :   RIGHT_BRACE TIMEOUT LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_PARENTHESIS codeBlockParameter RIGHT_PARENTHESIS  LEFT_BRACE codeBlockBody
    ;

tryCatchStatement
    :   TRY LEFT_BRACE codeBlockBody catchClauses RIGHT_BRACE
    ;

catchClauses
    : catchClause+ finallyClause?
    | finallyClause
    ;

catchClause
    :  RIGHT_BRACE CATCH LEFT_PARENTHESIS codeBlockParameter RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody
    ;

finallyClause
    : RIGHT_BRACE FINALLY LEFT_BRACE codeBlockBody
    ;

throwStatement
    :   THROW expression SEMICOLON
    ;

returnStatement
    :   RETURN expressionList? SEMICOLON
    ;

// below left Identifier is of type TYPE_MESSAGE and the right Identifier is of type WORKER
triggerWorker
    :   expressionList RARROW workerReference SEMICOLON #invokeWorker
    |   expressionList RARROW FORK SEMICOLON     #invokeFork
    ;

// below left Identifier is of type WORKER and the right Identifier is of type message
workerReply
    :   expressionList LARROW workerReference SEMICOLON
    ;

variableReference
    :   nameReference                                                           # simpleVariableReference
    |   functionInvocation                                                      # functionInvocationReference
    |   awaitExpression                                                         # awaitExpressionReference
    |   variableReference index                                                 # mapArrayVariableReference
    |   variableReference field                                                 # fieldVariableReference
    |   variableReference xmlAttrib                                             # xmlAttribVariableReference
    |   variableReference invocation                                            # invocationReference
    ;

field
    : DOT Identifier
    ;

index
    : LEFT_BRACKET expression RIGHT_BRACKET
    ;

xmlAttrib
    : AT (LEFT_BRACKET expression RIGHT_BRACKET)?
    ;

functionInvocation
    : ASYNC? functionReference LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

invocation
    : DOT anyIdentifierName LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

invocationArgList
    :   invocationArg (COMMA invocationArg)*
    ;

invocationArg
    :   expression  // required args
    |   namedArgs   // named args
    |   restArgs    // rest args
    ;

actionInvocation
    : variableReference RARROW functionInvocation
    ;

expressionList
    :   expression (COMMA expression)*
    ;

expressionStmt
    :   (variableReference | actionInvocation) SEMICOLON
    ;

transactionStatement
    :   TRANSACTION (WITH transactionPropertyInitStatementList)? LEFT_BRACE codeBlockBody onretryClause? RIGHT_BRACE
    ;

transactionPropertyInitStatement
    : retriesStatement
    | oncommitStatement
    | onabortStatement
    ;

transactionPropertyInitStatementList
    : transactionPropertyInitStatement (COMMA transactionPropertyInitStatement)*
    ;

lockStatement
    : LOCK LEFT_BRACE statement* RIGHT_BRACE
    ;

onretryClause
    :   RIGHT_BRACE ONRETRY LEFT_BRACE codeBlockBody
    ;
abortStatement
    :   ABORT SEMICOLON
    ;

failStatement
    :   FAIL SEMICOLON
    ;

retriesStatement
    :   RETRIES LEFT_PARENTHESIS expression RIGHT_PARENTHESIS
    ;

oncommitStatement
    :   ONCOMMIT LEFT_PARENTHESIS expression RIGHT_PARENTHESIS
    ;

onabortStatement
    :   ONABORT LEFT_PARENTHESIS expression RIGHT_PARENTHESIS
    ;

namespaceDeclarationStatement
    :   namespaceDeclaration
    ;

namespaceDeclaration
    :   XMLNS QuotedStringLiteral (AS Identifier)? SEMICOLON
    ;

expression
    :   simpleLiteral                                                       # simpleLiteralExpression
    |   arrayLiteral                                                        # arrayLiteralExpression
    |   recordLiteral                                                       # recordLiteralExpression
    |   xmlLiteral                                                          # xmlLiteralExpression
    |   stringTemplateLiteral                                               # stringTemplateLiteralExpression
    |   valueTypeName DOT Identifier                                        # valueTypeTypeExpression
    |   builtInReferenceTypeName DOT Identifier                             # builtInReferenceTypeTypeExpression
    |   variableReference                                                   # variableReferenceExpression
    |   lambdaFunction                                                      # lambdaFunctionExpression
    |   typeInitExpr                                                        # typeInitExpression
    |   tableQuery                                                          # tableQueryExpression
    |   typeConversion                                                      # typeConversionExpression
    |   TYPEOF builtInTypeName                                              # typeAccessExpression
    |   (ADD | SUB | NOT | LENGTHOF | TYPEOF | UNTAINT) simpleExpression    # unaryExpression
    |   LEFT_PARENTHESIS expression (COMMA expression)* RIGHT_PARENTHESIS   # bracedOrTupleExpression
    |   expression POW expression                                           # binaryPowExpression
    |   expression (DIV | MUL | MOD) expression                             # binaryDivMulModExpression
    |   expression (ADD | SUB) expression                                   # binaryAddSubExpression
    |   expression (LT_EQUAL | GT_EQUAL | GT | LT) expression               # binaryCompareExpression
    |   expression (EQUAL | NOT_EQUAL) expression                           # binaryEqualExpression
    |   expression AND expression                                           # binaryAndExpression
    |   expression OR expression                                            # binaryOrExpression
    |   expression QUESTION_MARK expression COLON expression                # ternaryExpression
    |   awaitExpression                                                     # awaitExprExpression
    ;

awaitExpression
    :   AWAIT expression                                                    # awaitExpr
    ;

simpleExpression
    :   expression
    ;

typeConversion
    :   LT typeName (COMMA transformerInvocation)? GT expression
    ;

//reusable productions

nameReference
    :   (packageName COLON)? Identifier
    ;

returnParameter
    : RETURNS typeName
    ;

functionReference
    :   (packageName COLON)? Identifier
    ;

structReference
    :   (packageName COLON)? Identifier
    ;

workerReference
    :   Identifier
    ;

transformerInvocation
    : transformerReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
    ;

transformerReference
    :   (packageName COLON)? Identifier
    ;

codeBlockBody
    :   statement*
    ;

nonEmptyCodeBlockBody
    :   statement+
    ;

codeBlockParameter
    :   typeName Identifier
    ;

parameterTypeNameList
    :   parameterTypeName (COMMA parameterTypeName)*
    ;

parameterTypeName
    :   typeName
    ;

parameterList
    :   parameter (COMMA parameter)*
    ;

parameter
    :   annotationAttachment* typeName Identifier                                                                       #simpleParameter
    |   annotationAttachment* LEFT_PARENTHESIS typeName Identifier (COMMA typeName Identifier)* RIGHT_PARENTHESIS       #tupleParameter
    ;

defaultableParameter
    :   parameter ASSIGN expression
    ;

restParameter
    :   annotationAttachment* typeName ELLIPSIS Identifier
    ;

formalParameterList
    :   (parameter | defaultableParameter) (COMMA (parameter | defaultableParameter))* (COMMA restParameter)?
    |   restParameter
    ;

fieldDefinition
    :   typeName Identifier (ASSIGN expression)? SEMICOLON
    ;

simpleLiteral
    :   (ADD | SUB)? integerLiteral
    |   (ADD | SUB)? FloatingPointLiteral
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   NullLiteral
    ;

// §3.10.1 Integer Literals
integerLiteral
    :   DecimalIntegerLiteral
    |   HexIntegerLiteral
    |   OctalIntegerLiteral
    |   BinaryIntegerLiteral
    ;

namedArgs
    :   Identifier ASSIGN expression
    ;

restArgs
    :   ELLIPSIS expression
    ;

// XML parsing

xmlLiteral
    :   XMLLiteralStart xmlContent? XMLEnd
    ;

xmlContent
    :   (XMLExpressionStart expression ExpressionEnd)+ xmlText?
    |   xmlText
    ;

xmlText
    :   XMLText
    ;

stringTemplateLiteral
    :   StringTemplateLiteralStart stringTemplateContent? StringTemplateLiteralEnd
    ;

stringTemplateContent
    :   (StringTemplateExpressionStart expression ExpressionEnd)+ StringTemplateText?
    |   StringTemplateText
    ;

anyIdentifierName
    : Identifier
    | reservedWord
    ;

reservedWord
    :   FOREACH
    |   TYPE_MAP
    ;


//Siddhi Streams and Tables related
tableQuery
    :   FROM streamingInput joinStreamingInput?
        selectClause?
        orderByClause?
    ;

aggregationQuery
    :   FROM streamingInput
        selectClause?
        orderByClause?

    ;

wheneverStatement
    :   WHENEVER LEFT_BRACE streamingQueryStatement+ RIGHT_BRACE
    ;

streamingQueryStatement
    :   FROM (streamingInput (joinStreamingInput)? | patternClause)
        selectClause?
        orderByClause?
        outputRateLimit?
        streamingAction
    ;

patternClause
    :   EVERY? patternStreamingInput withinClause?
    ;

withinClause
    :   WITHIN expression
    ;

orderByClause
    :   ORDER BY variableReferenceList
    ;

selectClause
    :   SELECT (MUL| selectExpressionList )
            groupByClause?
            havingClause?
    ;

selectExpressionList
    :   selectExpression (COMMA selectExpression)*
    ;

selectExpression
    :   expression (AS Identifier)?
    ;

groupByClause
    : GROUP BY variableReferenceList
    ;

havingClause
    :   HAVING expression
    ;

streamingAction
    :   EQUAL_GT LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

setClause
    :   SET setAssignmentClause (COMMA setAssignmentClause)*
    ;

setAssignmentClause
    :   variableReference ASSIGN expression
    ;

streamingInput
    :   variableReference whereClause?  windowClause? whereClause? (AS alias=Identifier)?
    ;

joinStreamingInput
    :   (UNIDIRECTIONAL joinType | joinType UNIDIRECTIONAL | joinType) streamingInput ON expression
    ;

outputRateLimit
    : OUTPUT (ALL | LAST | FIRST) EVERY ( DecimalIntegerLiteral timeScale | DecimalIntegerLiteral EVENTS )
    | OUTPUT SNAPSHOT EVERY DecimalIntegerLiteral timeScale
    ;

patternStreamingInput
    :   patternStreamingEdgeInput FOLLOWED BY patternStreamingInput
    |   LEFT_PARENTHESIS patternStreamingInput RIGHT_PARENTHESIS
    |   FOREACH patternStreamingInput
    |   NOT patternStreamingEdgeInput (AND patternStreamingEdgeInput | FOR StringTemplateText)
    |   patternStreamingEdgeInput (AND | OR ) patternStreamingEdgeInput
    |   patternStreamingEdgeInput
    ;

patternStreamingEdgeInput
    :   variableReference whereClause? intRangeExpression? (AS alias=Identifier)?
    ;

whereClause
    :   WHERE expression
    ;

functionClause
    :   FUNCTION functionInvocation
    ;

windowClause
    :   WINDOW functionInvocation
    ;

outputEventType
    : ALL EVENTS | EXPIRED EVENTS | CURRENT EVENTS
    ;

joinType
    : LEFT OUTER JOIN
    | RIGHT OUTER JOIN
    | FULL OUTER JOIN
    | OUTER JOIN
    | INNER? JOIN
    ;

timeScale
    : SECOND
    | MINUTE
    | HOUR
    | DAY
    | MONTH
    | YEAR
    ;

// Deprecated parsing.

deprecatedAttachment
    :   DeprecatedTemplateStart deprecatedText? DeprecatedTemplateEnd
    ;

deprecatedText
    :   deprecatedTemplateInlineCode (DeprecatedTemplateText | deprecatedTemplateInlineCode)*
    |   DeprecatedTemplateText  (DeprecatedTemplateText | deprecatedTemplateInlineCode)*
    ;

deprecatedTemplateInlineCode
    :   singleBackTickDeprecatedInlineCode
    |   doubleBackTickDeprecatedInlineCode
    |   tripleBackTickDeprecatedInlineCode
    ;

singleBackTickDeprecatedInlineCode
    :   SBDeprecatedInlineCodeStart SingleBackTickInlineCode? SingleBackTickInlineCodeEnd
    ;

doubleBackTickDeprecatedInlineCode
    :   DBDeprecatedInlineCodeStart DoubleBackTickInlineCode? DoubleBackTickInlineCodeEnd
    ;

tripleBackTickDeprecatedInlineCode
    :   TBDeprecatedInlineCodeStart TripleBackTickInlineCode? TripleBackTickInlineCodeEnd
    ;


// Documentation parsing.

documentationAttachment
    :   DocumentationTemplateStart documentationTemplateContent? DocumentationTemplateEnd
    ;

documentationTemplateContent
    :   docText? documentationTemplateAttributeDescription+
    |   docText
    ;

documentationTemplateAttributeDescription
    :   DocumentationTemplateAttributeStart Identifier DocumentationTemplateAttributeEnd docText?
    ;

docText
    :   documentationTemplateInlineCode (DocumentationTemplateText | documentationTemplateInlineCode)*
    |   DocumentationTemplateText  (DocumentationTemplateText | documentationTemplateInlineCode)*
    ;

documentationTemplateInlineCode
    :   singleBackTickDocInlineCode
    |   doubleBackTickDocInlineCode
    |   tripleBackTickDocInlineCode
    ;

singleBackTickDocInlineCode
    :   SBDocInlineCodeStart SingleBackTickInlineCode? SingleBackTickInlineCodeEnd
    ;

doubleBackTickDocInlineCode
    :   DBDocInlineCodeStart DoubleBackTickInlineCode? DoubleBackTickInlineCodeEnd
    ;

tripleBackTickDocInlineCode
    :   TBDocInlineCodeStart TripleBackTickInlineCode? TripleBackTickInlineCodeEnd
    ;
