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
    :   'package' packageName (VersionString OneZeroString)? ';'
    ;

importDeclaration
    :   'import' packageName (VersionString OneZeroString)? (AsString Identifier)? ';'
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
    :   annotation* PublicString? 'function' Identifier '(' parameterList ')' returnTypeList? ('throws' Identifier)? functionBody
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
    :   PublicString? 'type' Identifier typeDefinitionBody
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

fieldDeclaration
    :   typeName Identifier ';'
    ;

qualifiedTypeName
    :   packageName ':' unqualifiedTypeName
    ;

unqualifiedTypeName
    :   typeNameWithOptionalSchema
    |   typeNameWithOptionalSchema EmptyArrayString
    |   typeNameWithOptionalSchema '~'
    ;

typeNameWithOptionalSchema
    :   Identifier  ('<' ('{' QuotedStringLiteral '}')? Identifier '>')
    |   Identifier  ('<' ('{' QuotedStringLiteral '}') '>')
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
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   'nil'
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
     :   Identifier '=' elementValue
     ;

 elementValue
     :   expression
     |   annotation
     |   elementValueArrayInitializer
     ;

 elementValueArrayInitializer
     :   '{' (elementValue (',' elementValue)*)? (',')? '}'
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
    |   actionInvocationStatement
    ;

assignmentStatement
    :   variableReference '=' expression ';'
    |   variableReference '=' 'new' (packageName ':' )? Identifier ('(' expressionList ')')? ';'
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

forkJoinStatement
    :   'fork' '(' Identifier ')' '{' workerDeclaration+ '}' joinClause? timeoutClause?
    ;

// below typeName is only 'message[]'
joinClause
    :   'join' '(' joinConditions ')' '(' typeName Identifier ')'  '{' statement+ '}'
    ;

joinConditions
    :   AnyString IntegerLiteral (Identifier (',' Identifier)*)?
    |   AllString (Identifier (',' Identifier)*)?
    ;

// below typeName is only 'message[]'
timeoutClause
    :   TimeoutString '(' expression ')' '(' typeName Identifier ')'  '{' statement+ '}'
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
    :   'reply' Identifier? ';'
    ;

workerInteractionStatement
    :   triggerWorker
    |   workerReply
    ;

// below left Identifier is of type 'message' and the right Identifier is of type 'worker'
triggerWorker
    :   Identifier SendArrow Identifier ';'
    ;

// below left Identifier is of type 'worker' and the right Identifier is of type 'message'
workerReply
    :   Identifier ReceiveArrow Identifier ';'
    ;

commentStatement
    :   LINE_COMMENT
    ;

actionInvocationStatement
    :    qualifiedReference '.' Identifier '(' expressionList ')' ';'
    ;

// EXPRESSIONS

expressionList
    :   expression (',' expression)*
    ;


expression
    :   literalValue
    |   unaryExpression
    |   multExpression (('+' | '-' | '||') multExpression)*
    |   functionInvocation
    |   templateExpression
    |   '(' expression ')'
    |   variableReference

    ;

multExpression
    :   <assoc=right> (('*' | '/' | '&&') expression)+
    ;


variableReference
    :   Identifier // simple identifier
    |   Identifier'[' expression ']' // array and map reference
    |   Identifier ('.' variableReference)+ // struct field reference
    ;

unaryExpression
    :   '-' expression
    |   '+' expression
    |   '!' expression
    ;

//    expr:  mult ('+' mult)* ;
//    mult:  atom ('*' atom)* ;
//    atom:  INT | '(' expr ')' ;


//(a + b * c) ==> a + (b*c)

binaryOperator
    :   '+'
    |   '-'
    |   '*'
    |   '/'
    |   '&&'
    |   '||'
    |   '=='
    |   '!='
    |   '<'
    |   '<='
    |   '>'
    |   '>='
    |   '%'
    |   '^'
    ;

functionInvocation
    :   functionName '(' expressionList? ')'
    ;

functionName
    :   Identifier
    |   qualifiedReference
    ;

templateExpression
    :   BacktickStringLiteral
    ;