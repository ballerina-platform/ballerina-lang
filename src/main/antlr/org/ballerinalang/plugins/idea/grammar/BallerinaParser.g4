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
    :   PACKAGE fullyQualifiedPackageName SEMICOLON
    ;

importDeclaration
    :   IMPORT fullyQualifiedPackageName (AS alias)? SEMICOLON
    ;

fullyQualifiedPackageName
    :   packageName (DOT packageName)*
    ;

packageName
    :   Identifier
    ;

alias
    :   packageName
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
    :   SERVICE sourceNotation Identifier LEFT_BRACE serviceBody RIGHT_BRACE
    ;

sourceNotation
    :   LT packageName GT
    ;

serviceBody
    :   variableDefinitionStatement* resourceDefinition*
    ;

resourceDefinition
    :   annotationAttachment* RESOURCE Identifier LEFT_PARENTHESIS parameterList RIGHT_PARENTHESIS LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

callableUnitBody
    :    statement* workerDeclaration*
    ;

functionDefinition
    :   NATIVE FUNCTION Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? SEMICOLON
    |   FUNCTION Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

lambdaFunction
    :  FUNCTION LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

connectorDefinition
    :   CONNECTOR Identifier (LT codeBlockParameter GT)? LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS LEFT_BRACE connectorBody RIGHT_BRACE
    ;

connectorBody
    :   variableDefinitionStatement* (annotationAttachment* actionDefinition)*
    ;

actionDefinition
    :   NATIVE ACTION Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? SEMICOLON
    |   ACTION Identifier LEFT_PARENTHESIS parameterList? RIGHT_PARENTHESIS returnParameters? LEFT_BRACE callableUnitBody RIGHT_BRACE
    ;

structDefinition
    :   STRUCT Identifier LEFT_BRACE structBody RIGHT_BRACE
    ;

structBody
    :   fieldDefinition*
    ;

annotationDefinition
    : ANNOTATION Identifier (ATTACH attachmentPoint (COMMA attachmentPoint)*)? LEFT_BRACE annotationBody RIGHT_BRACE
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
    :   fieldDefinition*
    ;

typeMapperDefinition
    :   NATIVE TYPEMAPPER Identifier LEFT_PARENTHESIS parameter RIGHT_PARENTHESIS typeName SEMICOLON
    |   TYPEMAPPER Identifier LEFT_PARENTHESIS parameter RIGHT_PARENTHESIS typeName LEFT_BRACE typeMapperBody RIGHT_BRACE
    ;

typeMapperBody
    :   statement*
    ;

constantDefinition
    :   CONST valueTypeName Identifier ASSIGN expression SEMICOLON
    ;

workerDeclaration
    :   WORKER Identifier LEFT_BRACE workerBody RIGHT_BRACE
    ;

workerBody
    :   statement* workerDeclaration*
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
    |   TYPE_JSON (LT structReference GT)?
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
     :   AT annotationReference LEFT_BRACE annotationAttributeList? RIGHT_BRACE
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
    :   TRANSFORM LEFT_BRACE transformStatementBody RIGHT_BRACE
    ;

transformStatementBody
    :   (expressionAssignmentStatement
    |   expressionVariableDefinitionStatement
    |   transformStatement
    |   commentStatement)*
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
    :   mapStructKey COLON mapStructValue
    ;

mapStructKey
    :   expression
    ;

mapStructValue
    :   expression
    ;

arrayLiteral
    :   LEFT_BRACKET expressionList? RIGHT_BRACKET
    ;

connectorInitExpression
    :   CREATE connectorReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS (WITH filterInitExpressionList)?
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

codeBlockBody
    :   statement*
    ;

codeBlockParameter
    :   typeName Identifier
    ;

//todo replace with 'foreach'
iterateStatement
    :   ITERATE LEFT_PARENTHESIS codeBlockParameter COLON expression RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody RIGHT_BRACE
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
    : FORK LEFT_BRACE workerDeclaration* joinClause? timeoutClause? RIGHT_BRACE
    ;

// below typeName is only 'message[]'
joinClause
    :   RIGHT_BRACE JOIN (LEFT_PARENTHESIS joinConditions RIGHT_PARENTHESIS)? LEFT_PARENTHESIS codeBlockParameter RIGHT_PARENTHESIS LEFT_BRACE codeBlockBody
    ;

joinConditions
    :   SOME IntegerLiteral (Identifier (COMMA Identifier)*)? 	# anyJoinCondition
    |   ALL (Identifier (COMMA Identifier)*)? 		            # allJoinCondition
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
    :   variableReference (COMMA variableReference)* RARROW Identifier SEMICOLON #invokeWorker
    |   variableReference (COMMA variableReference)* RARROW FORK SEMICOLON     #invokeFork
    ;

// below left Identifier is of type WORKER and the right Identifier is of type message
workerReply
    :   variableReference (COMMA variableReference)* LARROW Identifier SEMICOLON
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
    :   DOT Identifier
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
    :   functionReference LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS SEMICOLON
    ;

actionInvocationStatement
    :   actionInvocation SEMICOLON
    |   variableReferenceList ASSIGN actionInvocation SEMICOLON
    ;

transactionStatement
    :   TRANSACTION LEFT_BRACE codeBlockBody transactionHandlers? RIGHT_BRACE
    ;

transactionHandlers
    : abortedClause committedClause
    | committedClause abortedClause
    ;

abortedClause
    :   RIGHT_BRACE ABORTED LEFT_BRACE codeBlockBody
    ;

committedClause
    :   RIGHT_BRACE COMMITTED LEFT_BRACE codeBlockBody
    ;

abortStatement
    :   ABORT SEMICOLON
    ;

actionInvocation
    :   connectorReference DOT Identifier LEFT_PARENTHESIS expressionList? RIGHT_PARENTHESIS
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
    |   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS simpleExpression        # typeCastingExpression
    |   LT typeName GT simpleExpression                                     # typeConversionExpression
    |   (ADD | SUB | NOT | LENGTHOF | TYPEOF) simpleExpression              # unaryExpression
    |   LEFT_PARENTHESIS expression RIGHT_PARENTHESIS                       # bracedExpression
    |   expression POW expression                                           # binaryPowExpression
    |   expression (DIV | MUL | MOD) expression                             # binaryDivMulModExpression
    |   expression (ADD | SUB) expression                                   # binaryAddSubExpression
    |   expression (LT_EQUAL | GT_EQUAL | GT | LT) expression               # binaryCompareExpression
    |   expression (EQUAL | NOT_EQUAL) expression                           # binaryEqualExpression
    |   expression AND expression                                           # binaryAndExpression
    |   expression OR expression                                            # binaryOrExpression
    ;

simpleExpression
    :   expression
    ;

//reusable productions

nameReference
    :   (packageName COLON)? Identifier
    ;

functionReference
    :   (packageName COLON)? Identifier
    ;

connectorReference
    :   (packageName COLON)? Identifier
    ;

annotationReference
    :   (packageName COLON)? Identifier
    ;

structReference
    :   (packageName COLON)? Identifier
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
    :   (ADD | SUB)? IntegerLiteral
    |   (ADD | SUB)? FloatingPointLiteral
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   NullLiteral
    ;

// XML parsing

xmlLiteral
    :   TYPE_XML BacktickStringLiteral
    ;
