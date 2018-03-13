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
    :   PACKAGE packageName SEMICOLON
    ;

packageName
    :   Identifier (DOT Identifier)* version?
    ;

version
    : (VERSION Identifier)
    ;

importDeclaration
    :   IMPORT (orgName DIV)?  packageName (AS Identifier)? SEMICOLON
    ;

orgName
    :   Identifier
    ;

definition
    :   serviceDefinition
    |   functionDefinition
    |   connectorDefinition
    |   structDefinition
    |   streamletDefinition
    |   enumDefinition
    |   constantDefinition
    |   annotationDefinition
    |   globalVariableDefinition
    |   globalEndpointDefinition
    |   transformerDefinition
    ;

serviceDefinition
    :   SERVICE LT nameReference GT Identifier serviceBody
    ;

serviceBody
    :   LEFT_BRACE endpointDeclaration* variableDefinitionStatement* resourceDefinition* RIGHT_BRACE
    ;

resourceDefinition
    :   annotationAttachment* documentationAttachment? deprecatedAttachment? RESOURCE Identifier LEFT_PARENTHESIS parameterList RIGHT_PARENTHESIS callableUnitBody
    ;

callableUnitBody
    : LEFT_BRACE endpointDeclaration* statement* RIGHT_BRACE
    | LEFT_BRACE endpointDeclaration* workerDeclaration+ RIGHT_BRACE
    ;


functionDefinition
    :   (PUBLIC)? NATIVE FUNCTION (LT parameter GT)? callableUnitSignature SEMICOLON
    |   (PUBLIC)? FUNCTION (LT parameter GT)? callableUnitSignature callableUnitBody
    ;

lambdaFunction
    :  FUNCTION LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? callableUnitBody
    ;

callableUnitSignature
    :   Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters?
    ;

connectorDefinition
    :   (PUBLIC)? CONNECTOR Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS connectorBody
    ;

connectorBody
    :   LEFT_BRACE endpointDeclaration* variableDefinitionStatement* actionDefinition* RIGHT_BRACE
    ;

actionDefinition
    :   annotationAttachment* documentationAttachment? deprecatedAttachment? NATIVE ACTION  callableUnitSignature SEMICOLON
    |   annotationAttachment* documentationAttachment? deprecatedAttachment? ACTION callableUnitSignature callableUnitBody
    ;

structDefinition
    :   (PUBLIC)? STRUCT Identifier structBody
    ;

structBody
    :   LEFT_BRACE fieldDefinition* privateStructBody? RIGHT_BRACE
    ;

streamletDefinition
    :   STREAMLET Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS streamletBody
    ;

streamletBody
    :   LEFT_BRACE streamingQueryDeclaration  RIGHT_BRACE
    ;

streamingQueryDeclaration
    : (TYPE_STREAM (LT nameReference GT)?)* (streamingQueryStatement | queryDeclaration+)
    ;

privateStructBody
    :   PRIVATE COLON fieldDefinition*
    ;

annotationDefinition
    : (PUBLIC)? ANNOTATION  (LT attachmentPoint (COMMA attachmentPoint)* GT)?  Identifier userDefineTypeName? SEMICOLON
    ;

enumDefinition
    : (PUBLIC)? ENUM Identifier LEFT_BRACE enumerator (COMMA enumerator)* RIGHT_BRACE
    ;

enumerator
    : Identifier
    ;

globalVariableDefinition
    :   (PUBLIC)? typeName Identifier (ASSIGN expression )? SEMICOLON
    ;

transformerDefinition
    :   (PUBLIC)? TRANSFORMER LT parameterList GT (Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS)? callableUnitBody
    ;

attachmentPoint
     : SERVICE
     | RESOURCE
     | CONNECTOR
     | ACTION
     | FUNCTION
     | STRUCT
     | STREAMLET
     | ENUM
     | ENDPOINT
     | CONST
     | PARAMETER
     | ANNOTATION
     | TRANSFORMER
     ;

constantDefinition
    :   (PUBLIC)? CONST valueTypeName Identifier ASSIGN expression SEMICOLON
    ;

workerDeclaration
    :   workerDefinition LEFT_BRACE statement* RIGHT_BRACE
    ;

workerDefinition
    :   WORKER Identifier
    ;

globalEndpointDefinition
    :   PUBLIC? endpointDeclaration
    ;

endpointDeclaration
    :   annotationAttachment* ENDPOINT (LT endpointType GT) Identifier recordLiteral?
    ;

endpointType
    :   nameReference
    ;

typeName
    :   TYPE_ANY
    |   TYPE_TYPE
    |   valueTypeName
    |   referenceTypeName
    |   typeName (LEFT_BRACKET RIGHT_BRACKET)+
    ;

builtInTypeName
     :   TYPE_ANY
     |   TYPE_TYPE
     |   valueTypeName
     |   builtInReferenceTypeName
     |   typeName (LEFT_BRACKET RIGHT_BRACKET)+
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
    : STRUCT structBody
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
    |   TYPE_XML (LT (LEFT_BRACE xmlNamespaceName RIGHT_BRACE)? xmlLocalName GT)?
    |   TYPE_JSON (LT nameReference GT)?
    |   TYPE_TABLE (LT nameReference GT)?
    |   TYPE_STREAM (LT nameReference GT)?
    |   TYPE_AGGREGTION (LT nameReference GT)?
    |   functionTypeName
    ;

functionTypeName
    :   FUNCTION LEFT_PARENTHESIS (parameterList | parameterTypeNameList)? RIGHT_PARENTHESIS returnParameters?
    ;

xmlNamespaceName
    :   QuotedStringLiteral
    ;

xmlLocalName
    :   Identifier
    ;

annotationAttachment
    :   AT nameReference recordLiteral?
    ;

 //============================================================================================================
// STATEMENTS / BLOCKS

statement
    :   variableDefinitionStatement
    |   assignmentStatement
    |   ifElseStatement
    |   foreachStatement
    |   whileStatement
    |   nextStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   returnStatement
    |   workerInteractionStatement
    |   expressionStmt
    |   transactionStatement
    |   abortStatement
    |   lockStatement
    |   namespaceDeclarationStatement
    |   streamingQueryStatement
    ;

variableDefinitionStatement
    :   typeName Identifier (ASSIGN (expression | actionInvocation))? SEMICOLON
    ;

recordLiteral
    :   LEFT_BRACE (recordKeyValue (COMMA recordKeyValue)*)? RIGHT_BRACE
    ;

recordKeyValue
    :   recordKey COLON expression
    ;

recordKey
    :   Identifier
    |   simpleLiteral
    ;

arrayLiteral
    :   LEFT_BRACKET expressionList? RIGHT_BRACKET
    ;

typeInitExpr
    :   NEW userDefineTypeName LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
    ;

assignmentStatement
    :   (VAR)? variableReferenceList ASSIGN (expression | actionInvocation) SEMICOLON
    ;

variableReferenceList
    :   variableReference (COMMA variableReference)*
    ;

ifElseStatement
    :  ifClause elseIfClause* elseClause?
    ;

ifClause
    :   IF LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

elseIfClause
    :   ELSE IF LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

elseClause
    :   ELSE LEFT_BRACE statement*RIGHT_BRACE
    ;

foreachStatement
    :   FOREACH LEFT_PARENTHESIS? variableReferenceList IN  (expression | intRangeExpression) RIGHT_PARENTHESIS? LEFT_BRACE statement* RIGHT_BRACE
    ;

intRangeExpression
    : expression RANGE expression
    | (LEFT_BRACKET|LEFT_PARENTHESIS) expression RANGE expression (RIGHT_BRACKET|RIGHT_PARENTHESIS)
    ;

whileStatement
    :   WHILE LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

nextStatement
    :   NEXT SEMICOLON
    ;

breakStatement
    :   BREAK SEMICOLON
    ;

// typeName is only message
forkJoinStatement
    : FORK LEFT_BRACE workerDeclaration* RIGHT_BRACE joinClause? timeoutClause?
    ;

// below typeName is only 'message[]'
joinClause
    :   JOIN (LEFT_PARENTHESIS joinConditions RIGHT_PARENTHESIS)? LEFT_PARENTHESIS typeName Identifier RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

joinConditions
    : SOME IntegerLiteral (Identifier (COMMA Identifier)*)?     # anyJoinCondition
    | ALL (Identifier (COMMA Identifier)*)?                     # allJoinCondition
    ;

// below typeName is only 'message[]'
timeoutClause
    :   TIMEOUT LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_PARENTHESIS typeName Identifier RIGHT_PARENTHESIS  LEFT_BRACE statement* RIGHT_BRACE
    ;

tryCatchStatement
    :   TRY LEFT_BRACE statement* RIGHT_BRACE catchClauses
    ;

catchClauses
    : catchClause+ finallyClause?
    | finallyClause
    ;

catchClause
    :  CATCH LEFT_PARENTHESIS typeName Identifier RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

finallyClause
    : FINALLY LEFT_BRACE statement* RIGHT_BRACE
    ;

throwStatement
    :   THROW expression SEMICOLON
    ;

returnStatement
    :   RETURN expressionList? SEMICOLON
    ;

workerInteractionStatement
    :   triggerWorker
    |   workerReply
    ;

// below left Identifier is of type TYPE_MESSAGE and the right Identifier is of type WORKER
triggerWorker
    :   expressionList RARROW Identifier SEMICOLON #invokeWorker
    |   expressionList RARROW FORK SEMICOLON     #invokeFork
    ;

// below left Identifier is of type WORKER and the right Identifier is of type message
workerReply
    :   expressionList LARROW Identifier SEMICOLON
    ;

variableReference
    :   nameReference                                                           # simpleVariableReference
    |   functionInvocation                                                      # functionInvocationReference
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
    : nameReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
    ;

invocation
    : DOT anyIdentifierName LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
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
    :   transactionClause failedClause?
    ;

transactionClause
    : TRANSACTION (WITH transactionPropertyInitStatementList)? LEFT_BRACE statement* RIGHT_BRACE
    ;

transactionPropertyInitStatement
    : retriesStatement
    ;

transactionPropertyInitStatementList
    : transactionPropertyInitStatement (COMMA transactionPropertyInitStatement)*
    ;

lockStatement
    : LOCK LEFT_BRACE statement* RIGHT_BRACE
    ;

failedClause
    :   FAILED LEFT_BRACE statement* RIGHT_BRACE
    ;
abortStatement
    :   ABORT SEMICOLON
    ;

retriesStatement
    :   RETRIES LEFT_PARENTHESIS expression RIGHT_PARENTHESIS
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
    |   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS expression              # typeCastingExpression
    |   LT typeName (COMMA functionInvocation)? GT expression               # typeConversionExpression
    |   TYPEOF builtInTypeName                                              # typeAccessExpression
    |   (ADD | SUB | NOT | LENGTHOF | TYPEOF | UNTAINT) expression          # unaryExpression
    |   LEFT_PARENTHESIS expression RIGHT_PARENTHESIS                       # bracedExpression
    |   expression POW expression                                           # binaryPowExpression
    |   expression (DIV | MUL | MOD) expression                             # binaryDivMulModExpression
    |   expression (ADD | SUB) expression                                   # binaryAddSubExpression
    |   expression (LT_EQUAL | GT_EQUAL | GT | LT) expression               # binaryCompareExpression
    |   expression (EQUAL | NOT_EQUAL) expression                           # binaryEqualExpression
    |   expression AND expression                                           # binaryAndExpression
    |   expression OR expression                                            # binaryOrExpression
    |   expression QUESTION_MARK expression COLON expression                # ternaryExpression
    ;

//reusable productions

nameReference
    :   (Identifier COLON)? Identifier
    ;

returnParameters
    : RETURNS? LEFT_PARENTHESIS (parameterList | parameterTypeNameList) RIGHT_PARENTHESIS
    ;

parameterTypeNameList
    :   parameterTypeName (COMMA parameterTypeName)*
    ;

parameterTypeName
    :   annotationAttachment* typeName
    ;

parameterList
    :   parameter (COMMA parameter)*
    ;

parameter
    :   annotationAttachment* typeName Identifier
    ;

fieldDefinition
    :   typeName Identifier (ASSIGN simpleLiteral)? SEMICOLON
    ;

simpleLiteral
    :   (SUB)? IntegerLiteral
    |   (SUB)? FloatingPointLiteral
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   NullLiteral
    ;

// XML parsing

xmlLiteral
    :   XMLLiteralStart xmlItem XMLLiteralEnd
    ;

xmlItem
    :   element
    |   procIns
    |   comment
    |   text
    |   CDATA
    ;

content
    :   text? ((element | CDATA | procIns | comment) text?)*
    ;

comment
    :   XML_COMMENT_START (XMLCommentTemplateText expression ExpressionEnd)* XMLCommentText
    ;

element
    :   startTag content closeTag
    |   emptyTag
    ;

startTag
    :   XML_TAG_OPEN xmlQualifiedName attribute* XML_TAG_CLOSE
    ;

closeTag
    :   XML_TAG_OPEN_SLASH xmlQualifiedName XML_TAG_CLOSE
    ;

emptyTag
    :   XML_TAG_OPEN xmlQualifiedName attribute* XML_TAG_SLASH_CLOSE
    ;

procIns
    :   XML_TAG_SPECIAL_OPEN (XMLPITemplateText expression ExpressionEnd)* XMLPIText
    ;

attribute
    :   xmlQualifiedName EQUALS xmlQuotedString;

text
    :   (XMLTemplateText expression ExpressionEnd)+ XMLText?
    |   XMLText
    ;

xmlQuotedString
    :   xmlSingleQuotedString
    |   xmlDoubleQuotedString
    ;

xmlSingleQuotedString
    :   SINGLE_QUOTE (XMLSingleQuotedTemplateString expression ExpressionEnd)* XMLSingleQuotedString? SINGLE_QUOTE_END
    ;

xmlDoubleQuotedString
    :   DOUBLE_QUOTE (XMLDoubleQuotedTemplateString expression ExpressionEnd)* XMLDoubleQuotedString? DOUBLE_QUOTE_END
    ;

xmlQualifiedName
    :   (XMLQName QNAME_SEPARATOR)? XMLQName
    |   XMLTagExpressionStart expression ExpressionEnd
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

streamingQueryStatement
    :   FROM (streamingInput (joinStreamingInput)?  | pattenStreamingInput)
        selectClause?
        orderByClause?
        streamingAction
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
    :   INSERT INTO Identifier
    |   UPDATE (OR INSERT INTO)? Identifier setClause ? ON expression
    |   DELETE Identifier ON expression
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
    :   JOIN streamingInput ON expression
    ;

pattenStreamingInput
    :   pattenStreamingInput FOLLOWED BY pattenStreamingInput
    |   LEFT_PARENTHESIS pattenStreamingInput RIGHT_PARENTHESIS
    |   FOREACH pattenStreamingInput
    |   NOT pattenStreamingEdgeInput (AND pattenStreamingEdgeInput | FOR StringTemplateText)
    |   pattenStreamingEdgeInput (AND | OR ) pattenStreamingEdgeInput
    |   pattenStreamingEdgeInput
    ;

pattenStreamingEdgeInput
    :   Identifier whereClause? intRangeExpression? (AS alias=Identifier)?
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

queryDeclaration
     :   queryDefinition LEFT_BRACE streamingQueryStatement RIGHT_BRACE
     ;

queryDefinition
     :   QUERY Identifier
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

