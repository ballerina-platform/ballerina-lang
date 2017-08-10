parser grammar BallerinaParser;

options {
    language = Java;
    tokenVocab = BallerinaLexer;
}

//todo comment statment
//todo revisit blockStatement

// starting point for parsing a bal file
compilationUnit
    :   packageDeclaration?
        (importDeclaration | namespaceDeclaration)*
        (annotationAttachment* definition)*
        EOF
    ;

packageDeclaration
    :   PACKAGE packageName SEMICOLON
    ;

packageName
    :   Identifier (DOT Identifier)*
    ;

importDeclaration
    :   IMPORT packageName (AS Identifier)? SEMICOLON
    ;

definition
    :   serviceDefinition
    |   functionDefinition
    |   connectorDefinition
    |   structDefinition
    |   typeMapperDefinition
    |   constantDefinition
    |   annotationDefinition
    |   globalVariableDefinition
    ;

serviceDefinition
    :   SERVICE (LT Identifier GT) Identifier serviceBody
    ;

serviceBody
    :   LEFT_BRACE variableDefinitionStatement* resourceDefinition* RIGHT_BRACE
    ;

resourceDefinition
    :   annotationAttachment* RESOURCE Identifier LEFT_PARENTHESIS parameterList RIGHT_PARENTHESIS callableUnitBody
    ;

callableUnitBody
    : LEFT_BRACE statement* workerDeclaration* RIGHT_BRACE
    ;

functionDefinition
    :   NATIVE FUNCTION  callableUnitSignature SEMICOLON
    |   FUNCTION callableUnitSignature callableUnitBody
    ;

lambdaFunction
    :  FUNCTION LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? callableUnitBody
    ;

callableUnitSignature
    :   Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters?
    ;

connectorDefinition
    :   CONNECTOR Identifier (LT parameter GT)? LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS connectorBody
    ;

connectorBody
    :   LEFT_BRACE variableDefinitionStatement* actionDefinition* RIGHT_BRACE
    ;

actionDefinition
    :   annotationAttachment* NATIVE ACTION  callableUnitSignature SEMICOLON
    |   annotationAttachment* ACTION callableUnitSignature callableUnitBody
    ;

structDefinition
    :   STRUCT Identifier structBody
    ;

structBody
    :   LEFT_BRACE fieldDefinition* RIGHT_BRACE
    ;

annotationDefinition
    : ANNOTATION Identifier (ATTACH attachmentPoint (COMMA attachmentPoint)*)? annotationBody
    ;

globalVariableDefinition
    :   typeName Identifier (ASSIGN expression )? SEMICOLON
    ;

attachmentPoint
     : SERVICE (LT Identifier? GT)?         # serviceAttachPoint
     | RESOURCE                             # resourceAttachPoint
     | CONNECTOR                            # connectorAttachPoint
     | ACTION                               # actionAttachPoint
     | FUNCTION                             # functionAttachPoint
     | TYPEMAPPER                           # typemapperAttachPoint
     | STRUCT                               # structAttachPoint
     | CONST                                # constAttachPoint
     | PARAMETER                            # parameterAttachPoint
     | ANNOTATION                           # annotationAttachPoint
     ;

annotationBody
    :  LEFT_BRACE fieldDefinition* RIGHT_BRACE
    ;

typeMapperDefinition
    :   NATIVE typeMapperSignature SEMICOLON
    |   typeMapperSignature typeMapperBody
    ;

typeMapperSignature
    :   TYPEMAPPER Identifier LEFT_PARENTHESIS parameter RIGHT_PARENTHESIS LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS
    ;

typeMapperBody
    :   LEFT_BRACE statement* RIGHT_BRACE
    ;

constantDefinition
    :   CONST valueTypeName Identifier ASSIGN expression SEMICOLON
    ;

workerDeclaration
    :   workerDefinition LEFT_BRACE statement* workerDeclaration*RIGHT_BRACE
    ;

workerDefinition
    :   WORKER Identifier
    ;

typeName
    :   TYPE_ANY
    |   TYPE_TYPE
    |   valueTypeName
    |   referenceTypeName
    |   typeName (LEFT_BRACKET RIGHT_BRACKET)+
    ;

referenceTypeName
    :   builtInReferenceTypeName
    |   nameReference
    ;

valueTypeName
    :   TYPE_BOOL
    |   TYPE_INT
    |   TYPE_FLOAT
    |   TYPE_STRING
    |   TYPE_BLOB
    ;

builtInReferenceTypeName
    :   TYPE_MESSAGE
    |   TYPE_MAP (LT typeName GT)?
    |   TYPE_XML (LT (LEFT_BRACE xmlNamespaceName RIGHT_BRACE)? xmlLocalName GT)?
    |   TYPE_JSON (LT nameReference GT)?
    |   TYPE_DATATABLE
    |   functionTypeName
    ;

functionTypeName
    :   FUNCTION LEFT_PARENTHESIS (parameterList | typeList)? RIGHT_PARENTHESIS returnParameters?
    ;

xmlNamespaceName
    :   QuotedStringLiteral
    ;

xmlLocalName
    :   Identifier
    ;

 annotationAttachment
     :   AT nameReference LEFT_BRACE annotationAttributeList? RIGHT_BRACE
     ;

 annotationAttributeList
     :   annotationAttribute (COMMA annotationAttribute)*
     ;

 annotationAttribute
     :    Identifier COLON annotationAttributeValue
     ;

 annotationAttributeValue
     :   simpleLiteral
     |   annotationAttachment
     |   annotationAttributeArray
     ;

 annotationAttributeArray
     :   LEFT_BRACKET (annotationAttributeValue (COMMA annotationAttributeValue)*)? RIGHT_BRACKET
     ;

 //============================================================================================================
// STATEMENTS / BLOCKS

statement
    :   variableDefinitionStatement
    |   assignmentStatement
    |   ifElseStatement
    |   iterateStatement
    |   whileStatement
    |   continueStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   returnStatement
    |   replyStatement
    |   workerInteractionStatement
    |   commentStatement
    |   actionInvocationStatement
    |   functionInvocationStatement
    |   transformStatement
    |   transactionStatement
    |   abortStatement
    |   namespaceDeclarationStatement
    ;

transformStatement
    :   TRANSFORM LEFT_BRACE transformStatementBody* RIGHT_BRACE
    ;

transformStatementBody
    :   expressionAssignmentStatement
    |   expressionVariableDefinitionStatement
    |   transformStatement
    |   commentStatement
    ;

expressionAssignmentStatement
    :   variableReferenceList ASSIGN expression SEMICOLON
    ;

expressionVariableDefinitionStatement
    :   typeName Identifier ASSIGN expression SEMICOLON
    ;

variableDefinitionStatement
    :   typeName Identifier (ASSIGN (connectorInitExpression | actionInvocation | expression) )? SEMICOLON
    ;

mapStructLiteral
    :   LEFT_BRACE (mapStructKeyValue (COMMA mapStructKeyValue)*)? RIGHT_BRACE
    ;

mapStructKeyValue
    :   expression COLON expression
    ;

arrayLiteral
    :   LEFT_BRACKET expressionList? RIGHT_BRACKET
    ;

connectorInitExpression
    :   CREATE nameReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS (WITH filterInitExpressionList)?
    ;

filterInitExpression
    : nameReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
    ;

filterInitExpressionList
    : filterInitExpression (COMMA filterInitExpression)*
    ;

assignmentStatement
    :   (VAR)? variableReferenceList ASSIGN (connectorInitExpression | actionInvocation | expression) SEMICOLON
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

//todo replace with 'foreach'
iterateStatement
    :   ITERATE LEFT_PARENTHESIS typeName Identifier COLON expression RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

whileStatement
    :   WHILE LEFT_PARENTHESIS expression RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

continueStatement
    :   CONTINUE SEMICOLON
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

// below Identifier is only a type of TYPE_MESSAGE
replyStatement
    :   REPLY expression SEMICOLON
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

commentStatement
    :   LINE_COMMENT
    ;

variableReference
    :   nameReference                                                           # simpleVariableReference
    |   variableReference index                                                 # mapArrayVariableReference
    |   variableReference field                                                 # fieldVariableReference
    |   variableReference xmlAttrib                                             # xmlAttribVariableReference
    |   variableReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS    # functionInvocationReference
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

expressionList
    :   expression (COMMA expression)*
    ;

functionInvocationStatement
    :   variableReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS SEMICOLON
    ;

actionInvocationStatement
    :   actionInvocation SEMICOLON
    |   variableReferenceList ASSIGN actionInvocation SEMICOLON
    ;

transactionStatement
    :   TRANSACTION LEFT_BRACE statement* RIGHT_BRACE transactionHandlers
    ;

transactionHandlers
    : abortedClause? committedClause?
    | committedClause? abortedClause?
    ;
abortedClause
    :   ABORTED LEFT_BRACE statement* RIGHT_BRACE
    ;

committedClause
    :   COMMITTED LEFT_BRACE statement* RIGHT_BRACE
    ;

abortStatement
    :   ABORT SEMICOLON
    ;

actionInvocation
    :   nameReference DOT Identifier LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
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
    |   mapStructLiteral                                                    # mapStructLiteralExpression
    |   xmlLiteral                                                          # xmlLiteralExpression
    |   valueTypeName DOT Identifier                                        # valueTypeTypeExpression
    |   builtInReferenceTypeName DOT Identifier                             # builtInReferenceTypeTypeExpression
    |   variableReference                                                   # variableReferenceExpression
    |   lambdaFunction                                                      # lambdaFunctionExpression
    |   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS expression              # typeCastingExpression
    |   LT typeName GT expression                                           # typeConversionExpression
    |   (ADD | SUB | NOT | LENGTHOF | TYPEOF) expression                    # unaryExpression
    |   LEFT_PARENTHESIS expression RIGHT_PARENTHESIS                       # bracedExpression
    |   expression POW expression                                           # binaryPowExpression
    |   expression (DIV | MUL | MOD) expression                             # binaryDivMulModExpression
    |   expression (ADD | SUB) expression                                   # binaryAddSubExpression
    |   expression (LT_EQUAL | GT_EQUAL | GT | LT) expression               # binaryCompareExpression
    |   expression (EQUAL | NOT_EQUAL) expression                           # binaryEqualExpression
    |   expression AND expression                                           # binaryAndExpression
    |   expression OR expression                                            # binaryOrExpression
    ;

//reusable productions

nameReference
    :   (Identifier COLON)? Identifier
    ;

returnParameters
    : RETURNS? LEFT_PARENTHESIS (parameterList | typeList) RIGHT_PARENTHESIS
    ;

typeList
    :   typeName (COMMA typeName)*
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
