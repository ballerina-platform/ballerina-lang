parser grammar BallerinaParser;

options {   tokenVocab = BallerinaLexer; }

//todo comment statment
//todo revisit blockStatement

// starting point for parsing a bal file
compilationUnit
    :   packageDeclaration?
        importDeclaration*
    (   serviceDefinition
    |   functionDefinition
    |   connectorDefinition
    |   typeDefinition
    |   typeConvertorDefinition
    |   constantDefinition
    )+
        EOF
    ;

packageDeclaration
    :   'package' packageName ('version' '1.0')? ';'
    ;

importDeclaration
    :   'import' packageName ('version' '1.0')? ('as' Identifier)? ';'
    ;

serviceDefinition
    :   annotation* 'service' Identifier serviceBody
    ;

serviceBody
    :   '{' serviceBodyDeclaration '}'
    ;

serviceBodyDeclaration
    :  connectorDeclaration* variableDeclaration* resourceDefinition+
    ;


resourceDefinition
    :   annotation* 'resource' Identifier '(' parameterList ')' functionBody
    ;

functionDefinition
    :   annotation* 'public'? 'function' Identifier '(' parameterList? ')' returnTypeList? ('throws' Identifier)? functionBody
    ;

//todo rename, this is used in resource, action and funtion
functionBody
    : '{' connectorDeclaration* variableDeclaration* workerDeclaration* statement+ '}'
    ;

connectorDefinition
    :   annotation* 'connector' Identifier '(' parameterList ')' connectorBody
    ;

connectorBody
    :   '{' connectorDeclaration* variableDeclaration* actionDefinition+ '}'
    ;

actionDefinition
    :   annotation* 'action' Identifier '(' parameterList ')' returnTypeList?  ('throws' Identifier)? functionBody
    ;

connectorDeclaration
    :   qualifiedReference Identifier '=' 'new' qualifiedReference '(' expressionList? ')'';'
    ;

typeDefinition
    :   'public'? 'type' Identifier typeDefinitionBody
    ;

typeDefinitionBody
    :   '{' (typeName Identifier ';')+ '}'
    ;

typeConvertorDefinition
    :   'typeconvertor' Identifier '(' typeNameWithOptionalSchema Identifier ')' '('typeNameWithOptionalSchema')' typeConvertorBody
    ;

typeConvertorBody
    :   '{' variableDeclaration* statement+ '}'
    ;

constantDefinition
    :   'const' typeName Identifier '=' literalValue;

variableDeclaration
    :   typeName Identifier ';'
    |   inlineAssignmentExpression
    ;

// typeName below is only 'message' type
workerDeclaration
    :   'worker' Identifier '(' typeName Identifier ')'  '{' variableDeclaration* statement+ '}'
    ;

returnTypeList
    : '(' typeNameList ')'
    ;

typeNameList
    :   typeName (',' typeName)*
    ;

qualifiedTypeName
    :   packageName ':' unqualifiedTypeName
    ;

unqualifiedTypeName
    :   typeNameWithOptionalSchema
    |   typeNameWithOptionalSchema '[]'
    |   typeNameWithOptionalSchema '~'
    ;

typeNameWithOptionalSchema
    :   Identifier  ('<' ('{' DoubleQuotedStringLiteral '}')? Identifier '>')
    |   Identifier  ('<' ('{' DoubleQuotedStringLiteral '}') '>')
    |   Identifier
    ;

typeName
    :   unqualifiedTypeName
    |   qualifiedTypeName
    ;

qualifiedReference
    :   packageName ':' Identifier
    ;

parameterList
    :   parameter ',' parameterList
    |   parameter
    ;

parameter
    :   annotation* typeName Identifier
    ;

packageName
    :   Identifier ('.' Identifier)*
    ;

literalValue
    :   IntegerLiteral
    |   FloatingPointLiteral
    |   DoubleQuotedStringLiteral
    |   BooleanLiteral
    |   NullLiteral
    ;
 //============================================================================================================
 // ANNOTATIONS

 annotation
     :   '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )?
     ;

 annotationName : packageName ;

 elementValuePairs
     :   elementValuePair (',' elementValuePair)*
     ;

 elementValuePair
     :   assignmentExpression
     ;

 elementValue
     :   expression
     |   annotation
     |   elementValueArrayInitializer
     ;

 elementValueArrayInitializer
     :   '{' (elementValue (',' elementValue)*)? (',')? '}'
     ;

assignmentStatement
    :   (assignmentExpression ';'
    |   inlineAssignmentExpression
    |   statementExpression ';')+
    ;

variableAccessor
    :   Identifier
    |   Identifier '['IntegerLiteral']'
    |   Identifier'['DoubleQuotedStringLiteral']'
    |   Identifier '.' variableAccessor
    ;

initializeTypeStatement
    :   'new' (packageName ':' )? Identifier ('(' expressionList? ')')?
    ;

assignmentExpression
    :   variableAccessor '=' statementExpression
    ;

inlineAssignmentExpression
    :   typeName assignmentExpression ';'
    ;

statementExpression
    :   expression
    |   functionInovationExpression
    |   actionInovationExpression
    |   initializeTypeStatement
    ;

argumentList
    :   '(' expressionList ')'
    ;

expressionList
    :   expression (',' expression)*
    ;

functionInovationExpression
    :   functionInovation argumentList
    ;

actionInovationExpression
    :   actionInovation argumentList
    ;

functionInovation
    :   (packageName | Identifier) ':' Identifier
    ;

actionInovation
    :   packageName ':' Identifier '.' Identifier
    ;


 //============================================================================================================
// STATEMENTS / BLOCKS

statement
    :   assignmentStatement
    |   ifElseStatement
    |   iterateStatement
    |   whileStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   returnStatement
    |   replyStatement
    |   workerInteractionStatement
    |   commentStatement
    |   actionInovationExpression
    ;

ifElseStatement
    :   'if' '(' expression ')' '{' statement* '}' ('else' 'if' '(' expression ')' '{' statement* '}')* ('else' '{' statement*'}' )?
    ;

iterateStatement
    :   'iterate' '(' typeName Identifier ':' expression ')' '{' statement+ '}'
    ;

whileStatement
    :   'while' '(' expression ')' '{' statement+ '}'
    ;

breakStatement
    :   'break' ';'
    ;

// typeName is only message
forkJoinStatement
    :   'fork' '(' typeName Identifier ')' '{' workerDeclaration+ '}' joinClause? timeoutClause?
    ;

// below typeName is only 'message[]'
joinClause
    :   'join' '(' joinConditions ')' '(' typeName Identifier ')'  '{' statement+ '}'
    ;

joinConditions
    :   'any' IntegerLiteral (Identifier (',' Identifier)*)?
    |   'all' (Identifier (',' Identifier)*)?
    ;

// below typeName is only 'message[]'
timeoutClause
    :   'timeout' '(' expression ')' '(' typeName Identifier ')'  '{' statement+ '}'
    ;

tryCatchStatement
    :   'try' '{' statement+ '}' catchClause
    ;


// below tyeName is only 'exception'
catchClause
    :   'catch' '(' typeName Identifier ')' '{' statement+ '}'
    ;

throwStatement
    :   'throw' expression ';'
    ;

returnStatement
    :   'return' expressionList? ';'
    ;

// below Identifier is only a type of 'message'
replyStatement
    :   'reply' (Identifier | expression)? ';'
    ;

workerInteractionStatement
    :   triggerWorker
    |   workerReply
    ;

// below left Identifier is of type 'message' and the right Identifier is of type 'worker'
triggerWorker
    :   Identifier '->' Identifier ';'
    ;

// below left Identifier is of type 'worker' and the right Identifier is of type 'message'
workerReply
    :   Identifier '<-' Identifier ';'
    ;

commentStatement
    :   LINE_COMMENT
    ;

backQuoteString
   :   BacktickStringLiteral
   ;

expression
    :   primary                                             # literalExpression
    |   backQuoteString                                     # templateExpression
    |   expression '.' Identifier                           # accessMemberDotExpression
    |   (packageName | Identifier) ':' Identifier           # inlineFunctionInovcationExpression
    |   expression '[' expression ']'                       # accessArrayElementExpression
    |   expression '(' expressionList? ')'                  # argumentListExpression
    |   '(' typeName ')' expression                         # typeCastingExpression
    |   ('+'|'-'|'!') expression                      # preSingleDualExpression
    |   expression ('*'|'/'|'%') expression                 # binrayMulDivPercentExpression
    |   expression ('+'|'-') expression                     # binaryPlusMinusExpression
    |   expression ('<=' | '>=' | '>' | '<') expression       # binaryComparisonExpression
    |   expression ('==' | '!=') expression                 # binrayEqualExpression
    |   expression '&&' expression                          # binrayAndExpression
    |   expression '||' expression                          # binrayOrExpression
    |   '{' DoubleQuotedStringLiteral ':' literal '}'             # mapInitializerExpression
    ;

literal
    :   IntegerLiteral
    |   FloatingPointLiteral
    |   DoubleQuotedStringLiteral
    |   SingleQuotedStringLiteral
    |   BooleanLiteral
    |   NullLiteral
    ;

primary
    :   '(' expression ')'
    |   literal
    |   Identifier
    ;
