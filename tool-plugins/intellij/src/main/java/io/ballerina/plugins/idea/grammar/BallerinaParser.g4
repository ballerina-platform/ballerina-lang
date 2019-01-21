parser grammar BallerinaParser;

options {
    language = Java;
    tokenVocab = BallerinaLexer;
}

//todo revisit blockStatement

// starting point for parsing a bal file
compilationUnit
    :   (importDeclaration | namespaceDeclaration)*
        (documentationString? deprecatedAttachment? annotationAttachment* definition)*
        EOF
    ;

packageName
    :   Identifier (DOT Identifier)* version?
    ;

version
    :   (VERSION Identifier)
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
    |   typeDefinition
    |   annotationDefinition
    |   globalVariableDefinition
    |   constantDefinition
    ;

serviceDefinition
    :   SERVICE Identifier? ON expressionList serviceBody
    ;

serviceBody
    :   LEFT_BRACE serviceBodyMember* RIGHT_BRACE
    ;

serviceBodyMember
    :   objectFieldDefinition
    |   objectFunctionDefinition
    ;

callableUnitBody
    : LEFT_BRACE statement* (workerDeclaration+ statement*)? RIGHT_BRACE
    ;

functionDefinition
    :   (PUBLIC)? (REMOTE)? (EXTERN)? FUNCTION ((Identifier | typeName) DOT)? callableUnitSignature (callableUnitBody | SEMICOLON)
    ;

lambdaFunction
    :  FUNCTION LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS (RETURNS lambdaReturnParameter)? callableUnitBody
    ;

arrowFunction
    :   arrowParam EQUAL_GT expression
    |   LEFT_PARENTHESIS (arrowParam (COMMA arrowParam)*)? RIGHT_PARENTHESIS EQUAL_GT expression
    ;

arrowParam
    :   Identifier
    ;

callableUnitSignature
    :   anyIdentifierName LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS returnParameter?
    ;

typeDefinition
    :   (PUBLIC)? TYPE Identifier finiteType SEMICOLON
    ;

objectBody
    :   (objectFieldDefinition | objectFunctionDefinition | typeReference)*
    ;

typeReference
    :   MUL simpleTypeName SEMICOLON
    ;

objectFieldDefinition
    :   annotationAttachment* deprecatedAttachment? (PUBLIC | PRIVATE)? typeName Identifier (ASSIGN expression)? SEMICOLON
    ;

fieldDefinition
    :   annotationAttachment* typeName Identifier QUESTION_MARK? (ASSIGN expression)? SEMICOLON
    ;

recordRestFieldDefinition
    :   typeName restDescriptorPredicate ELLIPSIS
    |   sealedLiteral
    ;

sealedLiteral
    :   NOT restDescriptorPredicate ELLIPSIS
    ;

restDescriptorPredicate : {_input.get(_input.index() -1).getType() != WS}? ;

objectFunctionDefinition
    :   documentationString? annotationAttachment* deprecatedAttachment? (PUBLIC | PRIVATE)? (REMOTE|RESOURCE)? (EXTERN)? FUNCTION callableUnitSignature (callableUnitBody | SEMICOLON)
    ;

annotationDefinition
    :   (PUBLIC)? ANNOTATION  (LT attachmentPoint (COMMA attachmentPoint)* GT)?  Identifier typeName? SEMICOLON
    ;

constantDefinition
    :   PUBLIC? CONST typeName? Identifier ASSIGN expression SEMICOLON
    ;

globalVariableDefinition
    :   PUBLIC? LISTENER? typeName Identifier (ASSIGN expression)? SEMICOLON
    |   PUBLIC? FINAL (typeName | VAR) Identifier ASSIGN expression SEMICOLON
    |   channelType Identifier ASSIGN expression SEMICOLON
    ;

channelType
    : CHANNEL (LT typeName GT)
    ;

attachmentPoint
    :   SERVICE
    |   RESOURCE
    |   FUNCTION
    |   REMOTE
    |   OBJECT
    |   CLIENT
    |   LISTENER
    |   TYPE
    |   PARAMETER
    |   ANNOTATION
    ;

workerDeclaration
    :   workerDefinition LEFT_BRACE statement* RIGHT_BRACE
    ;

workerDefinition
    :   WORKER Identifier returnParameter?
    ;

finiteType
    :   finiteTypeUnit (PIPE finiteTypeUnit)*
    ;

finiteTypeUnit
    :   simpleLiteral
    |   typeName
    ;

typeName
    :   simpleTypeName                                                                          # simpleTypeNameLabel
    |   typeName (LEFT_BRACKET (integerLiteral | sealedLiteral)? RIGHT_BRACKET)+                # arrayTypeNameLabel
    |   typeName (PIPE typeName)+                                                               # unionTypeNameLabel
    |   typeName QUESTION_MARK                                                                  # nullableTypeNameLabel
    |   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS                                             # groupTypeNameLabel
    |   LEFT_PARENTHESIS typeName (COMMA typeName)* RIGHT_PARENTHESIS                           # tupleTypeNameLabel
    |   ABSTRACT? CLIENT? OBJECT LEFT_BRACE objectBody RIGHT_BRACE                              # objectTypeNameLabel
    |   RECORD LEFT_BRACE recordFieldDefinitionList RIGHT_BRACE                                 # recordTypeNameLabel
    ;

recordFieldDefinitionList
    :   (fieldDefinition | typeReference)* recordRestFieldDefinition?
    ;

// Temporary production rule name
simpleTypeName
    :   TYPE_ANY
    |   TYPE_ANYDATA
    |   TYPE_DESC
    |   valueTypeName
    |   referenceTypeName
    |   emptyTupleLiteral // nil type name ()
    ;

referenceTypeName
    :   builtInReferenceTypeName
    |   userDefineTypeName
    ;

userDefineTypeName
    :   nameReference
    ;

valueTypeName
    :   TYPE_BOOL
    |   TYPE_INT
    |   TYPE_BYTE
    |   TYPE_FLOAT
    |   TYPE_DECIMAL
    |   TYPE_STRING
    ;

builtInReferenceTypeName
    :   TYPE_MAP (LT typeName GT)
    |   TYPE_FUTURE (LT typeName GT)
    |   TYPE_XML (LT (LEFT_BRACE xmlNamespaceName RIGHT_BRACE)? xmlLocalName GT)?
    |   TYPE_JSON (LT nameReference GT)?
    |   TYPE_TABLE (LT typeName GT)
    |   TYPE_STREAM (LT typeName GT)
    |   SERVICE
    |   errorTypeName
    |   functionTypeName
    ;

functionTypeName
    :   FUNCTION LEFT_PARENTHESIS (parameterList | parameterTypeNameList)? RIGHT_PARENTHESIS returnParameter?
    ;

errorTypeName
    :   TYPE_ERROR (LT typeName (COMMA typeName)? GT)?
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
    :   errorDestructuringStatement
    |   variableDefinitionStatement
    |   assignmentStatement
    |   tupleDestructuringStatement
    |   recordDestructuringStatement
    |   compoundAssignmentStatement
    |   ifElseStatement
    |   matchStatement
    |   foreachStatement
    |   whileStatement
    |   continueStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   panicStatement
    |   returnStatement
    |   workerSendAsyncStatement
    |   expressionStmt
    |   transactionStatement
    |   abortStatement
    |   retryStatement
    |   lockStatement
    |   namespaceDeclarationStatement
    |   foreverStatement
    |   streamingQueryStatement
    ;

variableDefinitionStatement
    :   typeName Identifier SEMICOLON
    |   FINAL? (typeName | VAR) bindingPattern ASSIGN expression SEMICOLON
    ;

recordLiteral
    :   LEFT_BRACE (recordKeyValue (COMMA recordKeyValue)*)? RIGHT_BRACE
    ;

staticMatchLiterals
    :   simpleLiteral                                                       # staticMatchSimpleLiteral
    |   recordLiteral                                                       # staticMatchRecordLiteral
    |   tupleLiteral                                                        # staticMatchTupleLiteral
    |   Identifier                                                          # staticMatchIdentifierLiteral
    |   staticMatchLiterals PIPE staticMatchLiterals                        # staticMatchOrExpression
    ;

 tupleLiteral
    :   LEFT_PARENTHESIS expression (COMMA expression)* RIGHT_PARENTHESIS
    ;

recordKeyValue
    :   recordKey COLON expression
    ;

recordKey
    :   Identifier
    |   expression
    ;

tableLiteral
    :   TYPE_TABLE LEFT_BRACE tableColumnDefinition? (COMMA tableDataArray)? RIGHT_BRACE
    ;

tableColumnDefinition
    :   LEFT_BRACE (tableColumn (COMMA tableColumn)*)? RIGHT_BRACE
    ;

tableColumn
    :   Identifier? Identifier
    ;

tableDataArray
    :   LEFT_BRACKET tableDataList? RIGHT_BRACKET
    ;

tableDataList
    :   tableData (COMMA tableData)*
    |   expressionList
    ;

tableData
    :   LEFT_BRACE expressionList RIGHT_BRACE
    ;

arrayLiteral
    :   LEFT_BRACKET expressionList? RIGHT_BRACKET
    ;

assignmentStatement
    :   variableReference ASSIGN expression SEMICOLON
    ;

tupleDestructuringStatement
    :   tupleRefBindingPattern ASSIGN expression SEMICOLON
    ;

recordDestructuringStatement
    :   recordRefBindingPattern ASSIGN expression SEMICOLON
    ;

 errorDestructuringStatement
    :   errorRefBindingPattern ASSIGN expression SEMICOLON
    ;

compoundAssignmentStatement
    :   variableReference compoundOperator expression SEMICOLON
    ;

compoundOperator
    :   COMPOUND_ADD
    |   COMPOUND_SUB
    |   COMPOUND_MUL
    |   COMPOUND_DIV
    |   COMPOUND_BIT_AND
    |   COMPOUND_BIT_OR
    |   COMPOUND_BIT_XOR
    |   COMPOUND_LEFT_SHIFT
    |   COMPOUND_RIGHT_SHIFT
    |   COMPOUND_LOGICAL_SHIFT
    ;

variableReferenceList
    :   variableReference (COMMA variableReference)*
    ;

ifElseStatement
    :  ifClause elseIfClause* elseClause?
    ;

ifClause
    :   IF expression LEFT_BRACE statement* RIGHT_BRACE
    ;

elseIfClause
    :   ELSE IF expression LEFT_BRACE statement* RIGHT_BRACE
    ;

elseClause
    :   ELSE LEFT_BRACE statement*RIGHT_BRACE
    ;

matchStatement
    :   MATCH  expression  LEFT_BRACE matchPatternClause+ RIGHT_BRACE
    ;

matchPatternClause
    :   staticMatchLiterals EQUAL_GT (statement | (LEFT_BRACE statement* RIGHT_BRACE))
    |   VAR bindingPattern (IF expression)? EQUAL_GT (statement | (LEFT_BRACE statement* RIGHT_BRACE))
    ;

bindingPattern
    :   Identifier
    |   structuredBindingPattern
    ;

structuredBindingPattern
    :   tupleBindingPattern
    |   recordBindingPattern
    |   errorBindingPattern
    ;

errorBindingPattern
    :   TYPE_ERROR LEFT_PARENTHESIS Identifier (COMMA (Identifier | recordBindingPattern))? RIGHT_PARENTHESIS
    ;

tupleBindingPattern
    :   LEFT_PARENTHESIS bindingPattern (COMMA bindingPattern)+ RIGHT_PARENTHESIS
    ;

recordBindingPattern
    :   LEFT_BRACE entryBindingPattern RIGHT_BRACE
    ;

entryBindingPattern
    :   fieldBindingPattern (COMMA fieldBindingPattern)* (COMMA restBindingPattern)?
    |   restBindingPattern
    ;

fieldBindingPattern
    :   Identifier (COLON bindingPattern)?
    ;

restBindingPattern
    :   ELLIPSIS Identifier
    |   sealedLiteral
    ;

bindingRefPattern
    :   variableReference
    |   structuredRefBindingPattern
    |   errorRefBindingPattern
    ;

structuredRefBindingPattern
    :   tupleRefBindingPattern
    |   recordRefBindingPattern
    ;

tupleRefBindingPattern
    :   LEFT_PARENTHESIS bindingRefPattern (COMMA bindingRefPattern)+ RIGHT_PARENTHESIS
    ;

recordRefBindingPattern
    :   LEFT_BRACE entryRefBindingPattern RIGHT_BRACE
    ;

errorRefBindingPattern
    :   TYPE_ERROR LEFT_PARENTHESIS variableReference (COMMA (variableReference | recordRefBindingPattern))? RIGHT_PARENTHESIS
    ;

entryRefBindingPattern
    :   fieldRefBindingPattern (COMMA fieldRefBindingPattern)* (COMMA restRefBindingPattern)?
    |   restRefBindingPattern
    ;

fieldRefBindingPattern
    :   Identifier (COLON bindingRefPattern)?
    ;

restRefBindingPattern
    :   ELLIPSIS variableReference
    |   sealedLiteral
    ;

foreachStatement
    :   FOREACH LEFT_PARENTHESIS? (typeName | VAR) bindingPattern IN expression RIGHT_PARENTHESIS? LEFT_BRACE statement* RIGHT_BRACE
    ;

intRangeExpression
    :   (LEFT_BRACKET|LEFT_PARENTHESIS) expression RANGE expression? (RIGHT_BRACKET|RIGHT_PARENTHESIS)
    ;

whileStatement
    :   WHILE expression LEFT_BRACE statement* RIGHT_BRACE
    ;

continueStatement
    :   CONTINUE SEMICOLON
    ;

breakStatement
    :   BREAK SEMICOLON
    ;

// typeName is only message
forkJoinStatement
    :   FORK LEFT_BRACE workerDeclaration* RIGHT_BRACE
    ;

// Depricated since 0.983.0, use trap expressoin. TODO : Remove this.
tryCatchStatement
    :   TRY LEFT_BRACE statement* RIGHT_BRACE catchClauses
    ;

// TODO : Remove this.
catchClauses
    :   catchClause+ finallyClause?
    |   finallyClause
    ;

// TODO : Remove this.
catchClause
    :   CATCH LEFT_PARENTHESIS typeName Identifier RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

// TODO : Remove this.
finallyClause
    :   FINALLY LEFT_BRACE statement* RIGHT_BRACE
    ;

// Depricated since 0.983.0, use panic instead. TODO : Remove this.
throwStatement
    :   THROW expression SEMICOLON
    ;

panicStatement
    :   PANIC expression SEMICOLON
    ;

returnStatement
    :   RETURN expression? SEMICOLON
    ;

workerSendAsyncStatement
    :   expression RARROW Identifier (COMMA expression)? SEMICOLON
    ;

flushWorker
    :   FLUSH Identifier?
    ;

waitForCollection
    :   LEFT_BRACE waitKeyValue (COMMA waitKeyValue)* RIGHT_BRACE
    ;

waitKeyValue
    :   Identifier
    |   Identifier COLON expression
    ;

variableReference
    :   nameReference                                                           # simpleVariableReference
    |   functionInvocation                                                      # functionInvocationReference
    |   variableReference index                                                 # mapArrayVariableReference
    |   variableReference field                                                 # fieldVariableReference
    |   variableReference xmlAttrib                                             # xmlAttribVariableReference
    |   variableReference invocation                                            # invocationReference
    |   typeDescExpr invocation                                                 # typeDescExprInvocationReference
    |   QuotedStringLiteral invocation                                          # stringFunctionInvocationReference
    ;

field
    :   (DOT | NOT) (Identifier | MUL)
    ;

index
    :   LEFT_BRACKET expression RIGHT_BRACKET
    ;

xmlAttrib
    :   AT (LEFT_BRACKET expression RIGHT_BRACKET)?
    ;

functionInvocation
    :   functionNameReference LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

invocation
    :   (DOT | NOT) anyIdentifierName LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
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
    :   START? variableReference RARROW functionInvocation
    ;

expressionList
    :   expression (COMMA expression)*
    ;

expressionStmt
    :   expression SEMICOLON
    ;

transactionStatement
    :   transactionClause onretryClause? committedAbortedClauses
    ;

committedAbortedClauses
    :  ((committedClause? abortedClause?) | (abortedClause? committedClause?))
    ;

transactionClause
    :   TRANSACTION (WITH transactionPropertyInitStatementList)? LEFT_BRACE statement* RIGHT_BRACE
    ;

transactionPropertyInitStatement
    :   retriesStatement
    ;

transactionPropertyInitStatementList
    :   transactionPropertyInitStatement (COMMA transactionPropertyInitStatement)*
    ;

lockStatement
    :   LOCK LEFT_BRACE statement* RIGHT_BRACE
    ;

onretryClause
    :   ONRETRY LEFT_BRACE statement* RIGHT_BRACE
    ;

committedClause
    :   COMMITTED LEFT_BRACE statement* RIGHT_BRACE
    ;

abortedClause
    :   ABORTED LEFT_BRACE statement* RIGHT_BRACE
    ;

abortStatement
    :   ABORT SEMICOLON
    ;

retryStatement
    :   RETRY SEMICOLON
    ;

retriesStatement
    :   RETRIES ASSIGN expression
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
    |   tableLiteral                                                        # tableLiteralExpression
    |   stringTemplateLiteral                                               # stringTemplateLiteralExpression
    |   START? variableReference                                            # variableReferenceExpression
    |   actionInvocation                                                    # actionInvocationExpression
    |   lambdaFunction                                                      # lambdaFunctionExpression
    |   arrowFunction                                                       # arrowFunctionExpression
    |   typeInitExpr                                                        # typeInitExpression
    |   errorConstructorExpr                                                # errorConstructorExpression
    |   serviceConstructorExpr                                              # serviceConstructorExpression
    |   tableQuery                                                          # tableQueryExpression
    |   LT typeName (COMMA functionInvocation)? GT expression               # typeConversionExpression
    |   (ADD | SUB | BIT_COMPLEMENT | NOT | LENGTHOF | UNTAINT) expression  # unaryExpression
    |   tupleLiteral                                                        # bracedOrTupleExpression
    |	CHECK expression										            # checkedExpression
    |   expression IS typeName                                              # typeTestExpression
    |   expression (DIV | MUL | MOD) expression                             # binaryDivMulModExpression
    |   expression (ADD | SUB) expression                                   # binaryAddSubExpression
    |   expression (shiftExpression) expression                             # bitwiseShiftExpression
    |   expression (LT_EQUAL | GT_EQUAL | GT | LT) expression               # binaryCompareExpression
    |   expression (EQUAL | NOT_EQUAL) expression                           # binaryEqualExpression
    |   expression (REF_EQUAL | REF_NOT_EQUAL) expression                   # binaryRefEqualExpression
    |   expression (BIT_AND | BIT_XOR | PIPE) expression                    # bitwiseExpression
    |   expression AND expression                                           # binaryAndExpression
    |   expression OR expression                                            # binaryOrExpression
    |   expression (ELLIPSIS | HALF_OPEN_RANGE) expression                  # integerRangeExpression
    |   expression QUESTION_MARK expression COLON expression                # ternaryExpression
    |   expression SYNCRARROW Identifier                                    # workerSendSyncExpression
    |   WAIT (waitForCollection | expression)                               # waitExpression
    |   trapExpr                                                            # trapExpression
    |   expression ELVIS expression                                         # elvisExpression
    |   LARROW Identifier (COMMA expression)?                               # workerReceiveExpression
    |   flushWorker                                                         # flushWorkerExpression
    |   typeDescExpr                                                        # typeAccessExpression
    ;

typeDescExpr
    : typeName
    ;

typeInitExpr
    :   NEW (LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS)?
    |   NEW userDefineTypeName LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

errorConstructorExpr
    :   TYPE_ERROR LEFT_PARENTHESIS expression (COMMA expression)? RIGHT_PARENTHESIS
    ;

serviceConstructorExpr
    :   annotationAttachment* SERVICE serviceBody
    ;

trapExpr
    :   TRAP expression
    ;

shiftExpression
    :   GT shiftExprPredicate GT
    |   LT shiftExprPredicate LT
    |   GT shiftExprPredicate GT shiftExprPredicate GT
    ;

shiftExprPredicate : {_input.get(_input.index() -1).getType() != WS}? ;

//reusable productions

nameReference
    :   (Identifier COLON)? Identifier
    ;

functionNameReference
    :   (Identifier COLON)? anyIdentifierName
    ;

returnParameter
    :   RETURNS annotationAttachment* typeName
    ;

lambdaReturnParameter
    :   annotationAttachment* typeName
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

simpleLiteral
    :   (SUB)? integerLiteral
    |   (SUB)? floatingPointLiteral
    |   QuotedStringLiteral
    |   SymbolicStringLiteral
    |   BooleanLiteral
    |   emptyTupleLiteral
    |   blobLiteral
    |   NullLiteral
    ;

floatingPointLiteral
    :   DecimalFloatingPointNumber
    |   HexadecimalFloatingPointLiteral
    ;

// §3.10.1 Integer Literals
integerLiteral
    :   DecimalIntegerLiteral
    |   HexIntegerLiteral
    |   BinaryIntegerLiteral
    ;

emptyTupleLiteral
    :   LEFT_PARENTHESIS RIGHT_PARENTHESIS
    ;

blobLiteral
    :   Base16BlobLiteral
    |   Base64BlobLiteral
    ;

namedArgs
    :   Identifier ASSIGN expression
    ;

restArgs
    :   ELLIPSIS expression
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
    |   START
    |   CONTINUE
    |   OBJECT_INIT
    ;


//Siddhi Streams and Tables related
tableQuery
    :   FROM streamingInput joinStreamingInput?
        selectClause?
        orderByClause?
        limitClause?
    ;

foreverStatement
    :   FOREVER LEFT_BRACE  streamingQueryStatement+ RIGHT_BRACE
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
    :   WITHIN DecimalIntegerLiteral timeScale
    ;

orderByClause
    :   ORDER BY orderByVariable (COMMA orderByVariable)*
    ;

orderByVariable
    :   variableReference orderByType?
    ;

limitClause
    :   LIMIT DecimalIntegerLiteral
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
    :   GROUP BY variableReferenceList
    ;

havingClause
    :   HAVING expression
    ;

streamingAction
    :   EQUAL_GT LEFT_PARENTHESIS parameter RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

setClause
    :   SET setAssignmentClause (COMMA setAssignmentClause)*
    ;

setAssignmentClause
    :   variableReference ASSIGN expression
    ;

streamingInput
    :   variableReference whereClause? functionInvocation* windowClause? functionInvocation*
        whereClause? (AS alias=Identifier)?
    ;

joinStreamingInput
    :   (UNIDIRECTIONAL joinType | joinType UNIDIRECTIONAL | joinType) streamingInput (ON expression)?
    ;

outputRateLimit
    :   OUTPUT (ALL | LAST | FIRST) EVERY ( DecimalIntegerLiteral timeScale | DecimalIntegerLiteral EVENTS )
    |   OUTPUT SNAPSHOT EVERY DecimalIntegerLiteral timeScale
    ;

patternStreamingInput
    :   patternStreamingEdgeInput ( FOLLOWED BY | COMMA ) patternStreamingInput
    |   LEFT_PARENTHESIS patternStreamingInput RIGHT_PARENTHESIS
    |   NOT patternStreamingEdgeInput (AND patternStreamingEdgeInput | FOR DecimalIntegerLiteral timeScale)
    |   patternStreamingEdgeInput (AND | OR ) patternStreamingEdgeInput
    |   patternStreamingEdgeInput
    ;

patternStreamingEdgeInput
    :   variableReference whereClause? intRangeExpression? (AS alias=Identifier)?
    ;

whereClause
    :   WHERE expression
    ;

windowClause
    :   WINDOW functionInvocation
    ;

orderByType
    :   ASCENDING | DESCENDING
    ;

joinType
    :   LEFT OUTER JOIN
    |   RIGHT OUTER JOIN
    |   FULL OUTER JOIN
    |   OUTER JOIN
    |   INNER? JOIN
    ;

timeScale
    :   SECOND | SECONDS
    |   MINUTE | MINUTES
    |   HOUR | HOURS
    |   DAY | DAYS
    |   MONTH | MONTHS
    |   YEAR | YEARS
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

// Markdown documentation
documentationString
    :   documentationLine+ parameterDocumentationLine* returnParameterDocumentationLine?
    ;

documentationLine
    :   DocumentationLineStart documentationContent
    ;

parameterDocumentationLine
    :   parameterDocumentation parameterDescriptionLine*
    ;

returnParameterDocumentationLine
    :   returnParameterDocumentation returnParameterDescriptionLine*
    ;

documentationContent
    :   documentationText?
    ;

parameterDescriptionLine
    :   DocumentationLineStart documentationText?
    ;

returnParameterDescriptionLine
    :   DocumentationLineStart documentationText?
    ;

documentationText
    :   (DocumentationText | ReferenceType | VARIABLE | MODULE | documentationReference | singleBacktickedBlock | doubleBacktickedBlock | tripleBacktickedBlock | DefinitionReference)+
    ;

documentationReference
    :   definitionReference
    ;

definitionReference
    :   definitionReferenceType singleBacktickedBlock
    ;

definitionReferenceType
    :   DefinitionReference
    ;

parameterDocumentation
    :   ParameterDocumentationStart docParameterName DescriptionSeparator documentationText?
    ;

returnParameterDocumentation
    :   ReturnParameterDocumentationStart documentationText?
    ;

docParameterName
    :   ParameterName
    ;

singleBacktickedBlock
    :   SingleBacktickStart singleBacktickedContent SingleBacktickEnd
    ;

singleBacktickedContent
    :   SingleBacktickContent
    ;

doubleBacktickedBlock
    :   DoubleBacktickStart doubleBacktickedContent DoubleBacktickEnd
    ;

doubleBacktickedContent
    :   DoubleBacktickContent
    ;

tripleBacktickedBlock
    :   TripleBacktickStart tripleBacktickedContent TripleBacktickEnd
    ;

tripleBacktickedContent
    :   TripleBacktickContent
    ;
