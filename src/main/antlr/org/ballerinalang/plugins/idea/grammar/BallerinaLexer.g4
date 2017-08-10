lexer grammar BallerinaLexer;

@members {
    boolean inTemplate = false;
}

// Reserved words

PACKAGE         : 'package';
IMPORT          : 'import';
AS              : 'as';
NATIVE          : 'native';
SERVICE         : 'service';
RESOURCE        : 'resource';
FUNCTION        : 'function';
CONNECTOR       : 'connector';
ACTION          : 'action';
STRUCT          : 'struct';
ANNOTATION      : 'annotation';
PARAMETER       : 'parameter';
CONST           : 'const';
TYPEMAPPER      : 'typemapper';
WORKER          : 'worker';
XMLNS           : 'xmlns';
RETURNS         : 'returns';

TYPE_INT        : 'int';
TYPE_FLOAT      : 'float';
TYPE_BOOL       : 'boolean';
TYPE_STRING     : 'string';
TYPE_BLOB       : 'blob';
TYPE_MAP        : 'map';
TYPE_JSON       : 'json';
TYPE_XML        : 'xml';
TYPE_MESSAGE    : 'message';
TYPE_DATATABLE  : 'datatable';
TYPE_ANY        : 'any';
TYPE_TYPE       : 'type' ;

VAR             : 'var';
CREATE          : 'create';
ATTACH          : 'attach';
TRANSFORM       : 'transform';
IF              : 'if';
ELSE            : 'else';
ITERATE         : 'iterate';
WHILE           : 'while';
CONTINUE        : 'continue';
BREAK           : 'break';
FORK            : 'fork';
JOIN            : 'join';
SOME            : 'some';
ALL             : 'all';
TIMEOUT         : 'timeout';
TRY             : 'try';
CATCH           : 'catch';
FINALLY         : 'finally';
THROW           : 'throw';
RETURN          : 'return';
REPLY           : 'reply';
TRANSACTION     : 'transaction';
ABORT           : 'abort';
ABORTED         : 'aborted';
COMMITTED       : 'committed';
LENGTHOF        : 'lengthof' ;
TYPEOF          : 'typeof' ;
WITH            : 'with' ;

// Separators

SEMICOLON           : ';';
COLON               : ':';
DOT                 : '.';
COMMA               : ',';
LEFT_BRACE          : '{';
RIGHT_BRACE         : '}';
LEFT_PARENTHESIS    : '(';
RIGHT_PARENTHESIS   : ')';
LEFT_BRACKET        : '[';
RIGHT_BRACKET       : ']';

// Arithmetic operators

ASSIGN          : '=';
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';
POW             : '^';
MOD             : '%';

// Relational operators

NOT         : '!';
EQUAL       : '==';
NOT_EQUAL   : '!=';
GT          : '>';
LT          : '<';
GT_EQUAL    : '>=';
LT_EQUAL    : '<=';
AND         : '&&';
OR          : '||';

// Additional symbols

RARROW      : '->' ;
LARROW      : '<-';
AT          : '@';
BACKTICK    : '`';

TILDE           : '~';
BITAND          : '&';
BITOR           : '|';
DOUBLEQUOTE     : '"';

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
    :   '"' StringCharacters? '"'?
    ;

fragment
StringCharacters
    :   StringCharacter+
    ;

fragment
StringCharacter
    :   ~["\n\r]
    |   ~'\\'? ('\\' '\\')* '\\' ["]
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
    :   ~[`\n\r]
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
    :   ( Letter LetterOrDigit* )
    |   IdentifierLiteral
    ;

fragment
Letter
    :   [a-zA-Z_] // these are the "letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

fragment
LetterOrDigit
    :   [a-zA-Z0-9_] // these are the "letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

StringTemplateLiteralStart
    :   TYPE_STRING WS* BACKTICK   { inTemplate = true; } -> pushMode(STRING_TEMPLATE)
    ;

ExpressionEnd
    :   {inTemplate}? RIGHT_BRACE WS* RIGHT_BRACE   ->  popMode
    ;

// Whitespace and comments

WS  :  [ \t\r\n\u000C]+ -> channel(HIDDEN)
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> channel(HIDDEN)
    ;

fragment
IdentifierLiteral
    : '|' IdentifierLiteralChar+ '|' ;

fragment
IdentifierLiteralChar
    : ~[|\\\b\f\n\r\t]
    | IdentifierLiteralEscapeSequence
    ;

fragment
IdentifierLiteralEscapeSequence
    : '\\' [|"\\/]
    | '\\\\' [btnfr]
    | UnicodeEscape
    ;

ERRCHAR
	:	.	-> channel(HIDDEN)
	;


fragment
ExpressionStart
    :   '{{'
    ;

mode STRING_TEMPLATE;

StringTemplateLiteralEnd
    :   '`' { inTemplate = false; }          -> popMode
    ;

StringTemplateExpressionStart
    :   StringTemplateText? ExpressionStart            -> pushMode(DEFAULT_MODE)
    ;

// We cannot use "StringTemplateBracesSequence? (StringTemplateStringChar StringTemplateBracesSequence?)*" because it
// can match an empty string.
StringTemplateText
    :   StringTemplateValidCharSequence? (StringTemplateStringChar StringTemplateValidCharSequence?)+
    |   StringTemplateValidCharSequence (StringTemplateStringChar StringTemplateValidCharSequence?)*
    ;

fragment
StringTemplateStringChar
    :   ~[`{\\]
    |   '\\' [`{]
    |   WS
    |   StringLiteralEscapedSequence
    ;

fragment
StringLiteralEscapedSequence
    :   '\\\\'
    |   '\\{{'
    ;

fragment
StringTemplateValidCharSequence
    :   '{'
    |   '\\' ~'\\'
    ;

TEMPLATE_ERRCHAR
	:	.	-> channel(HIDDEN)
	;
