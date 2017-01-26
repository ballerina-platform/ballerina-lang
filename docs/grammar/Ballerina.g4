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
    |   structDefinition
    |   typeConvertorDefinition
    |   constantDefinition
    )+
        EOF
    ;

packageDeclaration
    :   'package' packageName ';'
    ;

importDeclaration
    :   'import' packageName ('as' Identifier)? ';'
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
    :   annotation* 'public'? 'function' Identifier '(' parameterList? ')' returnParameters? ('throws' Identifier)? functionBody
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
    :   annotation* 'action' Identifier '(' parameterList ')' returnParameters?  ('throws' Identifier)? functionBody
    ;

connectorDeclaration
    :   qualifiedReference Identifier '=' 'new' qualifiedReference '(' expressionList? ')'';'
    ;

structDefinition
    :   'public'? 'type' Identifier structDefinitionBody
    ;

structDefinitionBody
    :   '{' (typeName Identifier ';')+ '}'
    ;

typeConvertorDefinition
    :   'typeconvertor' Identifier '(' typeConvertorInput ')' '('typeConvertorType')' typeConvertorBody
    ;

typeConvertorInput
    :   typeConvertorType Identifier
    ;

typeConvertorBody
    :   '{' variableDeclaration* statement+ '}'
    ;

constantDefinition
    :   'const' typeName Identifier '=' literalValue ';'
    ;

variableDeclaration
    :   typeName Identifier ';'
    ;

// typeName below is only 'message' type
workerDeclaration
    :   'worker' Identifier '(' typeName Identifier ')'  '{' variableDeclaration* statement+ '}'
    ;

returnParameters
    : '(' (namedParameterList | returnTypeList) ')'
    ;

namedParameterList
    :   namedParameter (',' namedParameter)*
    ;

namedParameter
    :   typeName Identifier
    ;

returnTypeList
    :   typeName (',' typeName)*
    ;

qualifiedTypeName
    :   packageName ':' unqualifiedTypeName
    ;

typeConvertorType
    :   simpleType
    |   withFullSchemaType
    |   withSchemaIdType
    |   withScheamURLType
    ;


unqualifiedTypeName
    :   simpleType
    |   simpleTypeArray
    |   simpleTypeIterate
    |   withFullSchemaType
    |   withFullSchemaTypeArray
    |   withFullSchemaTypeIterate
    |   withScheamURLType
    |   withSchemaURLTypeArray
    |   withSchemaURLTypeIterate
    |   withSchemaIdType
    |   withScheamIdTypeArray
    |   withScheamIdTypeIterate
    ;

simpleType
    :   Identifier
    ;

simpleTypeArray
    :   Identifier '[]'
    ;

simpleTypeIterate
    : Identifier '~'
    ;

withFullSchemaType
	:	Identifier '<' '{' QuotedStringLiteral '}' Identifier '>'
	;

withFullSchemaTypeArray
	:	Identifier '<' '{' QuotedStringLiteral '}' Identifier '>' '[]'
	;

withFullSchemaTypeIterate
	:	Identifier '<' '{' QuotedStringLiteral '}' Identifier '>' '~'
	;

withScheamURLType
	:	Identifier '<' '{' QuotedStringLiteral '}' '>'
	;

withSchemaURLTypeArray
	:	Identifier '<' '{' QuotedStringLiteral '}' '>' '[]'
	;

withSchemaURLTypeIterate
	:	Identifier '<' '{' QuotedStringLiteral '}' '>' '~'
	;

withSchemaIdType
	:	Identifier '<' Identifier '>'
	;

withScheamIdTypeArray
	:	Identifier '<' Identifier '>' '[]'
	;

withScheamIdTypeIterate
	:	Identifier '<' Identifier '>' '~'
	;

typeName
    :   unqualifiedTypeName
    |   qualifiedTypeName
    ;

qualifiedReference
    :   packageName ':' Identifier
    ;

parameterList
    :   parameter (',' parameter)*
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
    |   NullLiteral
    ;
 //============================================================================================================
 // ANNOTATIONS

 annotation
     :   '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )?
     ;

 annotationName : Identifier ;

 elementValuePairs
     :   elementValuePair (',' elementValuePair)*
     ;

 elementValuePair
     :    Identifier '=' elementValue
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
    |   functionInvocationStatement
    ;

assignmentStatement
    :   variableReferenceList '=' expression ';'
    ;

variableReferenceList
    :   variableReference (',' variableReference)*
    ;

ifElseStatement
    :   'if' '(' expression ')' '{' statement* '}' elseIfClause* elseClause?
    ;

elseIfClause
    :   'else' 'if' '(' expression ')' '{' statement* '}'
    ;

elseClause
    :   'else' '{' statement*'}'
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
    :   'reply' expression ';'
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

actionInvocationStatement
    :   actionInvocation argumentList ';'
    ;

variableReference
    :   Identifier                                  # simpleVariableIdentifier// simple identifier
    |   Identifier '['expression']'                 # mapArrayVariableIdentifier// array and map reference
    |   variableReference ('.' variableReference)+  # structFieldIdentifier// struct field reference
    ;

argumentList
    :   '(' expressionList? ')'
    ;

expressionList
    :   expression (',' expression)*
    ;

functionInvocationStatement
    :   functionName argumentList ';'
    ;

functionName
    :   (packageName ':')? Identifier
    ;

actionInvocation
    :   packageName ':' Identifier '.' Identifier
    ;

backtickString
   :   BacktickStringLiteral
   ;

expression
    :   literalValue                                                        # literalExpression
    |   variableReference                                                   # variableReferenceExpression
    |   backtickString                                                      # templateExpression
    |   functionName argumentList                                           # functionInvocationExpression
    |   actionInvocation argumentList                                       # actionInvocationExpression
    |   '(' typeName ')' expression                                         # typeCastingExpression
    |   ('+'|'-'|'!') expression                                            # unaryExpression
    |   '(' expression ')'                                                  # bracedExpression
    |   expression ('^') expression                                         # binaryPowExpression
    |   expression ('/') expression                                         # binaryDivisionExpression
    |   expression ('*') expression                                         # binaryMultiplicationExpression
    |   expression ('%') expression                                         # binaryModExpression
    |   expression ('&&') expression                                        # binaryAndExpression
    |   expression ('+') expression                                         # binaryAddExpression
    |   expression ('-') expression                                         # binarySubExpression
    |   expression ('||') expression                                        # binaryOrExpression
    |   expression ('>') expression                                         # binaryGTExpression
    |   expression ('>=') expression                                        # binaryGEExpression
    |   expression ('<') expression                                         # binaryLTExpression
    |   expression ('<=') expression                                        # binaryLEExpression
    |   expression ('==') expression                                        # binaryEqualExpression
    |   expression ('!=') expression                                        # binaryNotEqualExpression
    |   '[' expressionList ']'                                              # arrayInitializerExpression
    |   '{' mapInitKeyValueList '}'                                         # mapInitializerExpression
    |   'new' (packageName ':' )? Identifier ('(' expressionList? ')')?     # structInitializeExpression
    ;

mapInitKeyValueList
    :   mapInitKeyValue (',' mapInitKeyValue)*
    ;

mapInitKeyValue
    :   QuotedStringLiteral ':' expression
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
PUBLIC          :   'public';
ANY             :   'any';
ALL             :   'all';
AS              :   'as';
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

QuotedStringLiteral
    :   '"' StringCharacters? '"'
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
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;