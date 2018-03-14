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
    :   IMPORT fullyQualifiedPackageName version? (AS packageAlias)? SEMICOLON
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
    :   SERVICE LT nameReference GT Identifier LEFT_BRACE serviceBody RIGHT_BRACE
    ;

serviceBody
    :  endpointDeclaration* variableDefinitionStatement* resourceDefinition*
    ;

resourceDefinition
    :   annotationAttachment* documentationAttachment? deprecatedAttachment? RESOURCE Identifier LEFT_PARENTHESIS parameterList RIGHT_PARENTHESIS LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

callableUnitBody
    :  endpointDeclaration* statement*
    |  endpointDeclaration* workerDeclaration+
    ;

functionDefinition
    :   (PUBLIC)? NATIVE FUNCTION (LT codeBlockParameter GT)? Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? SEMICOLON
    |   (PUBLIC)? FUNCTION (LT codeBlockParameter GT)? Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

lambdaFunction
    :  FUNCTION LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

connectorDefinition
    :   (PUBLIC)? CONNECTOR Identifier (LT codeBlockParameter GT)? LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS LEFT_BRACE connectorBody RIGHT_BRACE
    ;

connectorBody
    :  endpointDeclaration* variableDefinitionStatement* (annotationAttachment* documentationAttachment? deprecatedAttachment? actionDefinition)*
    ;

actionDefinition
    :   NATIVE ACTION Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? SEMICOLON
    |   ACTION Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

structDefinition
    :   (PUBLIC)? STRUCT Identifier LEFT_BRACE structBody RIGHT_BRACE
    ;

structBody
    :   fieldDefinition* privateStructBody?
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
    : (PUBLIC)? ENUM Identifier LEFT_BRACE enumFieldList RIGHT_BRACE
    ;

enumFieldList
    :  enumField (COMMA enumField)*
    ;

enumField
    :   Identifier
    ;

globalVariableDefinition
    :   (PUBLIC)? typeName Identifier (ASSIGN expression )? SEMICOLON
    ;

transformerDefinition
    :   (PUBLIC)? TRANSFORMER LT parameterList GT (Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS)? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

attachmentPoint
     : SERVICE (LT Identifier? GT)?         # serviceAttachPoint
     | RESOURCE                             # resourceAttachPoint
     | CONNECTOR                            # connectorAttachPoint
     | ACTION                               # actionAttachPoint
     | FUNCTION                             # functionAttachPoint
     | STRUCT                               # structAttachPoint
     | STREAMLET                            # streamletAttachPoint
     | ENUM                                 # enumAttachPoint
     | ENDPOINT                             # endpointAttachPoint
     | CONST                                # constAttachPoint
     | PARAMETER                            # parameterAttachPoint
     | ANNOTATION                           # annotationAttachPoint
     | TRANSFORMER                          # transformerAttachPoint
     ;

annotationBody
    :   fieldDefinition*
    ;

constantDefinition
    :   (PUBLIC)? CONST valueTypeName Identifier ASSIGN expression SEMICOLON
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
    :   annotationAttachment* endpointType Identifier recordLiteral?
    ;

endpointType
    :   ENDPOINT (LT nameReference GT)
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
    |   TYPE_XML (LT (LEFT_BRACE xmlNamespaceName RIGHT_BRACE)? xmlLocalName GT)?
    |   TYPE_JSON (LT structReference GT)?
    |   TYPE_TABLE (LT structReference GT)?
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
     :    AT nameReference recordLiteral?
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
    |   (triggerWorker | workerReply)
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
    :   NEW userDefineTypeName LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
    ;

assignmentStatement
    :   (VAR)? variableReferenceList ASSIGN (expression | actionInvocation) SEMICOLON
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

foreachStatement
    :   FOREACH LEFT_PARENTHESIS? variableReferenceList IN  (expression | intRangeExpression) RIGHT_PARENTHESIS? LEFT_BRACE codeBlockBody RIGHT_BRACE
    ;

intRangeExpression
    : expression RANGE expression
    | (LEFT_BRACKET|LEFT_PARENTHESIS) expression RANGE expression (RIGHT_BRACKET|RIGHT_PARENTHESIS)
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
    :   SOME IntegerLiteral (workerReference (COMMA workerReference)*)?     # anyJoinCondition
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
    : functionReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
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
    :   TRANSACTION (WITH transactionPropertyInitStatementList)? LEFT_BRACE codeBlockBody failedClause? RIGHT_BRACE
    ;

transactionPropertyInitStatement
    : retriesStatement
    ;

transactionPropertyInitStatementList
    : transactionPropertyInitStatement (COMMA transactionPropertyInitStatement)*
    ;

lockStatement
    : LOCK LEFT_BRACE codeBlockBody RIGHT_BRACE
    ;

failedClause
    :   RIGHT_BRACE FAILED LEFT_BRACE codeBlockBody
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
    |   typeCast                                                            # typeCastingExpression
    |   typeConversion                                                      # typeConversionExpression
    |   TYPEOF builtInTypeName                                              # typeAccessExpression
    |   (ADD | SUB | NOT | LENGTHOF | TYPEOF | UNTAINT) simpleExpression    # unaryExpression
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

simpleExpression
    :   expression
    ;

typeCast
    :   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS simpleExpression
    ;

typeConversion
    :   LT typeName (COMMA transformerInvocation)? GT expression
    ;

//reusable productions

nameReference
    :   (packageName COLON)? Identifier
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

codeBlockParameter
    :   typeName Identifier
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
    :   (ADD | SUB)? IntegerLiteral
    |   (ADD | SUB)? FloatingPointLiteral
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   NullLiteral
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
