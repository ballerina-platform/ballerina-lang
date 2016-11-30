grammar Ballerina;

//todo xml, json schema definition
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
    :   'package' qualifiedName ';'
    ;

importDeclaration
    :   'import' qualifiedName ('as' Identifier)?';'
    ;

serviceDefinition
    :   annotation* 'service' Identifier serviceBody
    ;

serviceBody
    :   '{' serviceBodyDeclaration '}'
    ;

serviceBodyDeclaration
    :   (resourceDefinition | connectionDeclaration | variableDeclaration)* resourceDefinition
    ;


resourceDefinition
    :   annotation* 'resource' Identifier resourceParameters resourceBody
    ;

resourceParameters
    :   '(' 'message' Identifier (',' formalParameterList)? ')'
    ;

resourceBody
    : '{' resourceBodyDeclaration '}'
    ;

resourceBodyDeclaration
    :   (connectionDeclaration | variableDeclaration | workerDeclaration | statement)* statement
    ;

functionDefinition
    :   annotation* 'public'? 'function' Identifier formalParameters typeList? 'throws exception'? functionBody
    ;

functionBody
    : '{' functionBodyDeclaration '}'
    ;

functionBodyDeclaration
    :   (connectionDeclaration | variableDeclaration | workerDeclaration | statement)* statement
    ;

connectorDefinition
    :   annotation* 'connector' Identifier formalParameters connectorBody
    ;

connectorBody
    :   '{' connectorBodyDeclaration '}'
    ;

connectorBodyDeclaration
    :   (connectionDeclaration | variableDeclaration | actionDefinition)* actionDefinition
    ;

actionDefinition
    :   annotation* 'action' Identifier formalParameter typeList? actionBody
    ;

actionBody
    :   '{' actionBodyDeclaration '}'
    ;

actionBodyDeclaration
    :   (connectionDeclaration | variableDeclaration | workerDeclaration | statement)* statement
    ;

connectionDeclaration
    :   (Identifier '.')? Identifier Identifier '=' 'new'  (Identifier '.')? Identifier '(' expressionList ')'';'
    ;

typeDefinition
    :   'public'? 'type' Identifier typeDefinitionBody
    ;

typeDefinitionBody
    :   '{' typeBodyDeclaration '}'
    ;

typeBodyDeclaration
    :   fieldDeclaration+
    ;

typeConvertorDefinition
    :   'typeconvertor' Identifier '(' typeType Identifier ')' '('typeType')' typeConvertorBody
    ;

typeConvertorBody
    :   '{' typeConvertorBodyDeclaration '}'
    ;

typeConvertorBodyDeclaration
    :   (variableDeclaration | statement)* statement
    ;

constantDefinition
    :   'const' variableDeclaration;


variableDeclaration
    :   localVariableDeclaration ';'
    ;


workerDeclaration
    :   'worker' Identifier '(' 'message' Identifier ')' workerBody
    ;


workerBody
    : '{' workerBodyDeclaration '}'
    ;

workerBodyDeclaration
    :   (connectionDeclaration | variableDeclaration | statement)* statement
    ;

variableModifier
    :   annotation
    ;


typeList
    :   typeType (',' typeType)*
    ;


fieldDeclaration
    :   typeType variableDeclarators ';'
    ;


variableDeclarators
    :   variableDeclarator (',' variableDeclarator)*
    ;

variableDeclarator
    :   variableDeclaratorId ('=' variableInitializer)?
    ;

variableDeclaratorId
    :   Identifier ('[' ']')*
    ;

variableInitializer
    :   arrayInitializer
    |   expression
    ;

arrayInitializer
    :   '{' (variableInitializer (',' variableInitializer)* (',')? )? '}'
    ;
    
    
typeType
    :   userDefineType (('[' ']') | '~')?
    |   primitiveType (('[' ']') | '~')?
    |   nonPrimitiveType (('[' ']') | '~')?
    |   buildInDataType (('[' ']') | '~')?
    ;

userDefineType
    :   Identifier ('.' Identifier )*
    ;

primitiveType
    :   'boolean'
    |   'int'
    |   'long'
    |   'float'
    |   'double'
    ;

nonPrimitiveType
    :   'string'
    |   'message'
    |   'map'
    |   'exception'
    ;

//todo maybe need this
qualifiedNameList
    :   qualifiedName (',' qualifiedName)*
    ;

formalParameters
    :   '(' formalParameterList? ')'
    ;

formalParameterList
    :   formalParameter (',' formalParameter)* (',' lastFormalParameter)?
    |   lastFormalParameter
    ;

formalParameter
    :   variableModifier* typeType variableDeclaratorId
    ;

lastFormalParameter
    :   variableModifier* typeType '...' variableDeclaratorId
    ;

//todo add schemaDefinition part
buildInDataType
    :   'xml' //schemaDefinition?
    |   'xmlDocument'
    |   'json' //schemaDefinition?
    ;
// todo define schem definition
//schemaDefinition
//    :   '<' '>';

//todo revisit and remove
//typeArguments
//    :   '<' typeArgument (',' typeArgument)* '>'
//    ;
//
//typeArgument
//    :   typeType
//    |   '?' (('extends' | 'super') typeType)?
//    ;

qualifiedName
    :   Identifier ('.' Identifier)*
    ;

literal
    :   IntegerLiteral
    |   FloatingPointLiteral
    |   StringLiteral
    |   BooleanLiteral
    |   'nil'
    ;
 //============================================================================================================
 // ANNOTATIONS

 annotation
     :   '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )?
     ;

 annotationName : qualifiedName ;

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

block
    :   '{' blockStatement* '}'
    ;

blockStatement
    :   localVariableDeclarationStatement
    |   statement
    ;

localVariableDeclarationStatement
    :    localVariableDeclaration ';'
    ;

localVariableDeclaration
    :   variableModifier* typeType variableDeclarators
    ;

statement
    :   block
    |   ifElseStatement
    |   iterateStatement
    |   whileStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   returnStatement
    |   replyStatement
    |   workerInitiatingStatement
//    |   commentStatement
    |   ';'
    |   statementExpression ';'
    |   Identifier ':' statement
    ;
ifElseStatement
    :   'if' parExpression statement ('else' statement)?
    ;

iterateStatement
    :   'iterate' '(' iterateControl ')' statement
    ;

iterateControl
    :   variableModifier* typeType variableDeclaratorId ':' expression
    ;

whileStatement
    :   'while' parExpression statement
    ;

breakStatement
    :   'break' Identifier? ';'
    ;

forkJoinStatement
    :   'fork' '(' Identifier ')' block joinClause?
    ;

joinClause
    :   'join' joinConditions '(' 'message' '['']' Identifier ')' joinBody
    ;

joinConditions
    :   'any' literal (Identifier (',' Identifier)*)?
    |   'all' (Identifier (',' Identifier)*)?
    ;

joinBody
    :   '{' joinBodyDeclaration '}'
    ;

joinBodyDeclaration
    :   connectionDeclaration
    |   variableDeclaration
    |   statement
    ;

tryCatchStatement
    :   'try' block catchClause+
    ;

catchClause
    :   'catch' '(' 'exception' Identifier ')' block
    ;

throwStatement
    :   'throw' expression ';'
    ;

returnStatement
    :   'return' expression? ';'
    ;

replyStatement
    :   'reply' expression? ';'
    ;

workerInitiatingStatement
    :   triggerWorker
    |   workerReply
    ;

triggerWorker
    :   Identifier '->' Identifier ';'
    ;

workerReply
    :   Identifier '<-' Identifier ';'
    ;

//todo
//commentStatement
//    :   '//' ~('\r|\n')*
//    ;
// EXPRESSIONS

parExpression
    :   '(' expression ')'
    ;

expressionList
    :   expression (',' expression)*
    ;

statementExpression
    :   expression
    ;

expression
    :   primary
    |   expression '.' Identifier
    |   expression '[' expression ']'
    |   expression '(' expressionList? ')'
    |   'new' creator
    |   '(' typeType ')' expression
    |   expression ('++' | '--')
    |   ('+'|'-'|'++'|'--') expression
    |   ('~'|'!') expression
    |   expression ('*'|'/'|'%') expression
    |   expression ('+'|'-') expression
    |   expression ('<' '<' | '>' '>' '>' | '>' '>') expression
    |   expression ('<=' | '>=' | '>' | '<') expression
    |   expression ('==' | '!=') expression
    |   expression '&' expression
    |   expression '^' expression
    |   expression '|' expression
    |   expression '&&' expression
    |   expression '||' expression
    |   expression '?' expression ':' expression
    |   '{' expression ':' expression '}'
    |   '`' expression '`'
    |   <assoc=right> expression
        (   '='
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
        expression
    ;

primary
    :   '(' expression ')'
    |   literal
    |   Identifier
    ;

creator
    :   createdName arrayCreatorRest
    ;

//todo think of the xml, json schema (removed Diamon syntax)
createdName
    :   Identifier ('.' Identifier)*
    |   primitiveType
    ;


arrayCreatorRest
    :   '['
        (   ']' ('[' ']')* arrayInitializer
        |   expression ']' ('[' expression ']')* ('[' ']')*
        )
    ;

//todo is something worng ?
arguments
    :   '(' expressionList? ')'
    ;

// LEXER

// §3.9 Ballerina keywords

ACTION	        :	'action';
BOOLEAN	        :	'boolean';
BREAK	        :	'break';
CATCH	        :	'catch';
CONNECTOR	    :	'connector';
CONST	        :	'const';
DOUBLE	        :	'double';
ELSE	        :	'else';
EXCEPTION	    :	'exception';
FLOAT	        :	'float';
FORK	        :	'fork';
FUNCTION	    :	'function';
IF	            :	'if';
IMPORT	        :	'import';
INT	            :	'int';
ITERATE	        :	'iterate';
JOIN	        :	'join';
JSON	        :	'json';
LONG	        :	'long';
MAP	            :	'map';
MESSAGE	        :	'message';
NEW	            :	'new';
PACKAGE	        :	'package';
REPLY	        :	'reply';
RESOURCE	    :	'resource';
RETURN	        :	'return';
SERVICE	        :	'service';
STRING	        :	'string';
THROW	        :	'throw';
THROWS	        :	'throws';
TRY	            :	'try';
TYPE	        :	'type';
TYPECONVERTOR	:	'typeconvertor';
WHILE	        :	'while';
WORKER	        :	'worker';
XML	            :	'xml';
XMLDOCUMENT	    :   'xmlDocument';

// §3.10.1 Integer Literals
IntegerLiteral
    :   DecimalIntegerLiteral
    |   HexIntegerLiteral
    |   OctalIntegerLiteral
    |   BinaryIntegerLiteral
    ;

fragment
DecimalIntegerLiteral
    :   DecimalNumeral IntegerTypeSuffix?
    ;

fragment
HexIntegerLiteral
    :   HexNumeral IntegerTypeSuffix?
    ;

fragment
OctalIntegerLiteral
    :   OctalNumeral IntegerTypeSuffix?
    ;

fragment
BinaryIntegerLiteral
    :   BinaryNumeral IntegerTypeSuffix?
    ;

fragment
IntegerTypeSuffix
    :   [lL]
    ;

fragment
DecimalNumeral
    :   '0'
    |   NonZeroDigit (Digits? | Underscores Digits)
    ;

fragment
Digits
    :   Digit (DigitOrUnderscore* Digit)?
    ;

fragment
Digit
    :   '0'
    |   NonZeroDigit
    ;

fragment
NonZeroDigit
    :   [1-9]
    ;

fragment
DigitOrUnderscore
    :   Digit
    |   '_'
    ;

fragment
Underscores
    :   '_'+
    ;

fragment
HexNumeral
    :   '0' [xX] HexDigits
    ;

fragment
HexDigits
    :   HexDigit (HexDigitOrUnderscore* HexDigit)?
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
HexDigitOrUnderscore
    :   HexDigit
    |   '_'
    ;

fragment
OctalNumeral
    :   '0' Underscores? OctalDigits
    ;

fragment
OctalDigits
    :   OctalDigit (OctalDigitOrUnderscore* OctalDigit)?
    ;

fragment
OctalDigit
    :   [0-7]
    ;

fragment
OctalDigitOrUnderscore
    :   OctalDigit
    |   '_'
    ;

fragment
BinaryNumeral
    :   '0' [bB] BinaryDigits
    ;

fragment
BinaryDigits
    :   BinaryDigit (BinaryDigitOrUnderscore* BinaryDigit)?
    ;

fragment
BinaryDigit
    :   [01]
    ;

fragment
BinaryDigitOrUnderscore
    :   BinaryDigit
    |   '_'
    ;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
    :   DecimalFloatingPointLiteral
    |   HexadecimalFloatingPointLiteral
    ;

fragment
DecimalFloatingPointLiteral
    :   Digits '.' Digits? ExponentPart? FloatTypeSuffix?
    |   '.' Digits ExponentPart? FloatTypeSuffix?
    |   Digits ExponentPart FloatTypeSuffix?
    |   Digits FloatTypeSuffix
    ;

fragment
ExponentPart
    :   ExponentIndicator SignedInteger
    ;

fragment
ExponentIndicator
    :   [eE]
    ;

fragment
SignedInteger
    :   Sign? Digits
    ;

fragment
Sign
    :   [+-]
    ;

fragment
FloatTypeSuffix
    :   [fFdD]
    ;

fragment
HexadecimalFloatingPointLiteral
    :   HexSignificand BinaryExponent FloatTypeSuffix?
    ;

fragment
HexSignificand
    :   HexNumeral '.'?
    |   '0' [xX] HexDigits? '.' HexDigits
    ;

fragment
BinaryExponent
    :   BinaryExponentIndicator SignedInteger
    ;

fragment
BinaryExponentIndicator
    :   [pP]
    ;

// §3.10.3 Boolean Literals

BooleanLiteral
    :   'true'
    |   'false'
    ;

//todo remove it after verifying
// §3.10.4 Character Literals
//
//CharacterLiteral
//    :   '\'' SingleCharacter '\''
//    |   '\'' EscapeSequence '\''
//    ;
//
//fragment
//SingleCharacter
//    :   ~['\\]
//    ;

// §3.10.5 String Literals

StringLiteral
    :   '"' StringCharacters? '"'
    ;

fragment
StringCharacters
    :   StringCharacter+
    ;

fragment
StringCharacter
    :   ~["\\]
    |   EscapeSequence
    ;

// §3.10.6 Escape Sequences for Character and String Literals

fragment
EscapeSequence
    :   '\\' [btnfr"'\\]
    |   OctalEscape
    |   UnicodeEscape
    ;

fragment
OctalEscape
    :   '\\' OctalDigit
    |   '\\' OctalDigit OctalDigit
    |   '\\' ZeroToThree OctalDigit OctalDigit
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment
ZeroToThree
    :   [0-3]
    ;

// §3.10.7 The Null Literal

NilLiteral
    :   'nil'
    ;

// §3.11 Separators

LPAREN          : '(';
RPAREN          : ')';
LBRACE          : '{';
RBRACE          : '}';
LBRACK          : '[';
RBRACK          : ']';
SEMI            : ';';
COMMA           : ',';
DOT             : '.';

// §3.12 Operators

ASSIGN          : '=';
GT              : '>';
LT              : '<';
BANG            : '!';
TILDE           : '~';
QUESTION        : '?';
COLON           : ':';
EQUAL           : '==';
LE              : '<=';
GE              : '>=';
NOTEQUAL        : '!=';
AND             : '&&';
OR              : '||';
INC             : '++';
DEC             : '--';
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';
BITAND          : '&';
BITOR           : '|';
CARET           : '^';
MOD             : '%';

ADD_ASSIGN      : '+=';
SUB_ASSIGN      : '-=';
MUL_ASSIGN      : '*=';
DIV_ASSIGN      : '/=';
AND_ASSIGN      : '&=';
OR_ASSIGN       : '|=';
XOR_ASSIGN      : '^=';
MOD_ASSIGN      : '%=';
LSHIFT_ASSIGN   : '<<=';
RSHIFT_ASSIGN   : '>>=';
URSHIFT_ASSIGN  : '>>>=';


Identifier
    :   JavaLetter JavaLetterOrDigit*
    ;

fragment
JavaLetter
    :   [a-zA-Z$_] // these are the "java letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment
JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

//
// Additional symbols not defined in the lexical specification
//

AT : '@';
ELLIPSIS : '...';

//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
    ;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;