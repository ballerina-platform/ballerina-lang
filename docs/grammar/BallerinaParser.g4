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
    :   annotation* PublicString? 'function' Identifier '(' parameterList? ')' returnTypeList? ('throws' Identifier)? functionBody
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
    :   qualifiedReference Identifier ASSIGN NEW qualifiedReference '(' expressionList? ')'';'
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
    :   'const' typeName Identifier ASSIGN literalValue;

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
    |   typeNameWithOptionalSchema EmptyArrayString
    |   typeNameWithOptionalSchema '~'
    ;

typeNameWithOptionalSchema
    :   Identifier  (LT ('{' QuotedStringLiteral '}')? Identifier GT)
    |   Identifier  (LT ('{' QuotedStringLiteral '}') GT)
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

variableReference
    : DOLLAR_SIGN Identifier
    ;

variableAccessor
    :   Identifier
    |   Identifier '['DecimalNumeral']'
    |   Identifier'['QuotedStringLiteral']'
    |   Identifier '.' variableAccessor
    ;

initializeTypeStatement
    :   NEW (packageName ':' )? Identifier ('(' expressionList? ')')?
    ;

assignmentExpression
    :   variableAccessor ASSIGN statementExpression
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

//assignmentStatement
//    :   variableReference '=' expression ';'
//    |   variableReference '=' 'new' (packageName ':' )? Identifier ('(' expressionList ')')? ';'
//    ;

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
    :   'reply' (Identifier | expression)? ';'
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

//actionInvocationStatement
//    :    qualifiedReference '.' Identifier '(' expressionList ')' ';'
//    ;

//// EXPRESSIONS
//
//expressionList
//    :   expression (',' expression)*
//    ;
//
//
//expression
//    :   literalValue
//    |   unaryExpression
//    |   multExpression (('+' | '-' | '||') multExpression)*
//    |   functionInvocation
//    |   templateExpression
//    |   '(' expression ')'
//    |   variableReference
//
//    ;
//
//multExpression
//    :   <assoc=right> (('*' | '/' | '&&') expression)+
//    ;
//
//
//variableReference
//    :   Identifier // simple identifier
//    |   Identifier'[' expression ']' // array and map reference
//    |   Identifier ('.' variableReference)+ // struct field reference
//    ;
//
//unaryExpression
//    :   '-' expression
//    |   '+' expression
//    |   '!' expression
//    ;
//
////    expr:  mult ('+' mult)* ;
////    mult:  atom ('*' atom)* ;
////    atom:  INT | '(' expr ')' ;
//
//
////(a + b * c) ==> a + (b*c)
//
//binaryOperator
//    :   '+'
//    |   '-'
//    |   '*'
//    |   '/'
//    |   '&&'
//    |   '||'
//    |   '=='
//    |   '!='
//    |   '<'
//    |   '<='
//    |   '>'
//    |   '>='
//    |   '%'
//    |   '^'
//    ;
//
//functionInvocation
//    :   functionName '(' expressionList? ')'
//    ;
//
//functionName
//    :   Identifier
//    |   qualifiedReference
//    ;
//
//templateExpression
//    :   BacktickStringLiteral
//    ;

expression
    :   primary                                             # literalExpression
    |   backtickjson                                        # templateExpression
    |   expression '.' Identifier                           # accessMemberDotExpression
    |   (packageName | Identifier) ':' Identifier           # inlineFunctionInovcationExpression
    |   expression '[' expression ']'                       # accessArrayElementExpression
    |   expression '(' expressionList? ')'                  # argumentListExpression
    |   '(' typeName ')' expression                         # typeCastingExpression
    |   expression ('++' | '--')                            # postDualOperationExpression
    |   ('+'|'-'|'++'|'--') expression                      # preSingleDualExpression
    |   ('~'|'!') expression                                # preUnaryExpression
    |   expression ('*'|DIV|'%') expression                 # binrayMulDivPercentExpression
    |   expression ('+'|'-') expression                     # binaryPlusMinusExpression
    |   expression (LT LT | GT GT GT | GT GT) expression    # binrayBitShiftExpression
    |   expression ('<=' | '>=' | GT | LT) expression       # binaryComparisonExpression
    |   expression ('==' | '!=') expression                 # binrayEqualExpression
    |   expression '&' expression                           # binrayBitwiseAndExpression
    |   expression '^' expression                           # binrayBitwiseXorExpression
    |   expression '|' expression                           # binrayBitwiseOrExpression
    |   expression '&&' expression                          # binrayAndExpression
    |   expression '||' expression                          # binrayOrExpression
    |   expression '?' expression ':' expression            # ternaryIfExpression
    |   '{' QuotedStringLiteral ':' literal '}'             # mapInitializerExpression
    |   <assoc=right> expression
        (   ASSIGN
        |   '+='
        |   '-='
        |   '*='
        |   '/='
        |   '&='
        |   '|='
        |   '^='
        |   '>>='
        |   '>>>='
        |   '<<='
        |   '%='
        )
        expression                                          # binrayOpEqualsOpExpression
    ;

typeType
    :   userDefineType (('[' ']') | '~')?
    |   primitiveType (('[' ']') | '~')?
    |   nonPrimitiveType (('[' ']') | '~')?
    |   buildInDataType (('[' ']') | '~')?
    ;

buildInDataType
    :   XML (schemaDefinition)?
    |   XMLDOCUMENT
    |   JSON(schemaDefinition)?
    ;

literal
    :   IntegerLiteral
    |   FloatingPointLiteral
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   'nil'
    ;

schemaDefinition
    :   LT ('{' literal '}')? Identifier GT;

userDefineType
    :   Identifier ('.' Identifier )*
    ;

primitiveType
    :   BOOLEAN
    |   INT
    |   'long'
    |   'float'
    |   'double'
    ;

nonPrimitiveType
    :   STRING
    |   MESSAGE
    |   MAP
    |   EXCEPTION
    ;

primary
    :   '(' expression ')'
    |   literal
    |   Identifier
    ;


//todo think of the xml, json schema (removed Diamon syntax)
createdName
    :   Identifier ('.' Identifier)*
    |   primitiveType
    ;

//todo is something worng ?
arguments
    :   '(' expressionList? ')'
    ;

// JSON Parsing
backtickjson
    :   BACKTICK jsonorxml BACKTICK
    ;

jsonorxml
    :   json
    |   document
    ;

json
   : value
   ;

object
   : '{' pair (',' pair)* '}'
   | '{' '}'
   ;

pair
   : QuotedStringLiteral ':' value
   ;

array
   : '[' value (',' value)* ']'
   | '[' ']'
   ;


value
   : object
   | array
   | literal
   | JSONNULL
   ;

// XML Parsing
//backtickxml   : OPENBACKTICK document CLOSEBACKTICK;

document    :   prolog? misc* element misc*;

prolog      :   XMLDeclOpen attribute* SPECIAL_CLOSE ;

content     :   chardata?
                ( (element | reference | CDATA | PI | COMMENT) chardata?)* ;

element     :   OPEN Name attribute* CLOSE content OPEN SLASH Name CLOSE
            |   OPEN Name attribute* SLASH CLOSE
            ;

reference   :   EntityRef | CharRef ;

attribute   :   Name EQUALS XMLSTRING ; // Our STRING is AttValue in spec

/** ``All text that is not markup constitutes the character data of
 *  the document.''
 */
chardata    :   QuotedStringLiteral | SEA_WS ;

misc        :   COMMENT | PI | SEA_WS ;