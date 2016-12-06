grammar Ballerina;

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

// LEXER

// §3.9 Ballerina keywords

ACTION	        :	'action';
BREAK	        :	'break';
CATCH	        :	'catch';
CONNECTOR	    :	'connector';
CONST	        :	'const';
ELSE	        :	'else';
FORK	        :	'fork';
FUNCTION	    :	'function';
IF	            :	'if';
IMPORT	        :	'import';
ITERATE	        :	'iterate';
JOIN	        :	'join';
NEW	            :	'new';
PACKAGE	        :	'package';
REPLY	        :	'reply';
RESOURCE	    :	'resource';
RETURN	        :	'return';
SERVICE	        :	'service';
THROW	        :	'throw';
THROWS	        :	'throws';
TRY	            :	'try';
TYPE	        :	'type';
TYPECONVERTOR	:	'typeconvertor';
WHILE	        :	'while';
WORKER	        :	'worker';
BACKTICK        :   '`';
VERSION         :   'version';
ONEZERO         :   '1.0';
PUBLIC          :   'public';
ANY             :   'any';
ALL             :   'all';
AS              :   'as';
EMPTYARRAY      :  '[]';
TIMEOUT         :   'timeout';
SENDARROW       :   '->';
RECEIVEARROW    :   '<-';

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
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';
BITAND          : '&';
BITOR           : '|';
CARET           : '^';
MOD             : '%';
DOLLAR_SIGN     : '$';

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

// §3.10.5 String Literals

DoubleQuotedStringLiteral
    :   '"' StringCharacters? '"'
    ;

SingleQuotedStringLiteral
    :   '\'' StringCharacters? '\''
    ;

BacktickStringLiteral
   :   '`' ValidBackTickStringCharacters '`'
   ;
fragment
ValidBackTickStringCharacters
   :     ValidBackTickStringCharacter+
   ;

fragment
ValidBackTickStringCharacter
   :   ~[`]
   |   '\\' [btnfr\\]
   |   OctalEscape
   |   UnicodeEscape
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

NullLiteral
    :   'null'
    ;

VariableReference
    : DOLLAR_SIGN Identifier
    ;

Identifier
    :   Letter LetterOrDigit*
    ;

fragment
Letter
    :   [a-zA-Z$_] // these are the "letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

fragment
LetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
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

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;