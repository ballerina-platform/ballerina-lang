lexer grammar BallerinaLexer;

@members {
    boolean inTemplate = false;
    boolean inDocTemplate = false;
    boolean inDeprecatedTemplate = false;
    boolean inSiddhi = false;
    boolean inTableSqlQuery = false;
    boolean inSiddhiInsertQuery = false;
    boolean inSiddhiTimeScaleQuery = false;
    boolean inSiddhiOutputRateLimit = false;
}

// Reserved words

PACKAGE     : 'package' ;
IMPORT      : 'import' ;
AS          : 'as' ;
PUBLIC      : 'public' ;
PRIVATE     : 'private' ;
NATIVE      : 'native' ;
SERVICE     : 'service' ;
RESOURCE    : 'resource' ;
FUNCTION    : 'function' ;
STRUCT      : 'struct' ;
OBJECT      : 'object' ;
ANNOTATION  : 'annotation' ;
ENUM        : 'enum' ;
PARAMETER   : 'parameter' ;
CONST       : 'const' ;
TRANSFORMER : 'transformer' ;
WORKER      : 'worker' ;
ENDPOINT    : 'endpoint' ;
BIND        : 'bind' ;
XMLNS       : 'xmlns' ;
RETURNS     : 'returns';
VERSION     : 'version';
DOCUMENTATION  : 'documentation';
DEPRECATED  :  'deprecated';

FROM        : 'from' { inSiddhi = true; inTableSqlQuery = true; inSiddhiInsertQuery = true; inSiddhiOutputRateLimit = true; } ;
ON          : 'on' ;
SELECT      : {inTableSqlQuery}? 'select' { inTableSqlQuery = false; } ;
GROUP       : 'group' ;
BY          : 'by' ;
HAVING      : 'having' ;
ORDER       : 'order' ;
WHERE       : 'where' ;
FOLLOWED    : 'followed' ;
INSERT      : {inSiddhi}? 'insert' { inSiddhi = false; } ;
INTO        : 'into' ;
UPDATE      : {inSiddhi}? 'update' { inSiddhi = false; } ;
DELETE      : {inSiddhi}? 'delete' { inSiddhi = false; } ;
SET         : 'set' ;
FOR         : 'for' ;
WINDOW      : 'window' ;
QUERY       : 'query' ;
EXPIRED     : 'expired' ;
CURRENT     : 'current' ;
EVENTS      : {inSiddhiInsertQuery}? 'events' { inSiddhiInsertQuery = false; } ;
EVERY       : 'every' ;
WITHIN      : 'within' ;
LAST        : {inSiddhiOutputRateLimit}? 'last' { inSiddhiOutputRateLimit = false; } ;
FIRST       : {inSiddhiOutputRateLimit}? 'first' { inSiddhiOutputRateLimit = false; } ;
SNAPSHOT    : 'snapshot' ;
OUTPUT      : {inSiddhiOutputRateLimit}? 'output' { inSiddhiTimeScaleQuery = true; } ;
INNER       : 'inner' ;
OUTER       : 'outer' ;
RIGHT       : 'right' ;
LEFT        : 'left' ;
FULL        : 'full' ;
UNIDIRECTIONAL  : 'unidirectional' ;
REDUCE      : 'reduce' ;
SECOND      : {inSiddhiTimeScaleQuery}? 'second' { inSiddhiTimeScaleQuery = false; } ;
MINUTE      : {inSiddhiTimeScaleQuery}? 'minute' { inSiddhiTimeScaleQuery = false; } ;
HOUR        : {inSiddhiTimeScaleQuery}? 'hour' { inSiddhiTimeScaleQuery = false; } ;
DAY         : {inSiddhiTimeScaleQuery}? 'day' { inSiddhiTimeScaleQuery = false; } ;
MONTH       : {inSiddhiTimeScaleQuery}? 'month' { inSiddhiTimeScaleQuery = false; } ;
YEAR        : {inSiddhiTimeScaleQuery}? 'year' { inSiddhiTimeScaleQuery = false; } ;
WHENEVER     : 'whenever' ;

TYPE_INT        : 'int' ;
TYPE_FLOAT      : 'float' ;
TYPE_BOOL       : 'boolean' ;
TYPE_STRING     : 'string' ;
TYPE_BLOB       : 'blob' ;
TYPE_MAP        : 'map' ;
TYPE_JSON       : 'json' ;
TYPE_XML        : 'xml' ;
TYPE_TABLE      : 'table' ;
TYPE_STREAM     : 'stream' ;
TYPE_ANY        : 'any' ;
TYPE_DESC       : 'typedesc' ;
TYPE_TYPE       : 'type' ;
TYPE_FUTURE     : 'future' ;

VAR         : 'var' ;
NEW         : 'new' ;
IF          : 'if' ;
MATCH       : 'match' ;
ELSE        : 'else' ;
FOREACH     : 'foreach' ;
WHILE       : 'while' ;
NEXT        : 'next' ;
BREAK       : 'break' ;
FORK        : 'fork' ;
JOIN        : 'join' ;
SOME        : 'some' ;
ALL         : 'all' ;
TIMEOUT     : 'timeout' ;
TRY         : 'try' ;
CATCH       : 'catch' ;
FINALLY     : 'finally' ;
THROW       : 'throw' ;
RETURN      : 'return' ;
TRANSACTION : 'transaction' ;
ABORT       : 'abort' ;
FAIL        : 'fail' ;
ONRETRY     : 'onretry' ;
RETRIES     : 'retries' ;
ONABORT     : 'onabort' ;
ONCOMMIT    : 'oncommit' ;
LENGTHOF    : 'lengthof' ;
TYPEOF      : 'typeof' ;
WITH        : 'with' ;
IN          : 'in' ;
LOCK        : 'lock' ;
UNTAINT     : 'untaint' ;
ASYNC       : 'async' ;
AWAIT       : 'await' ;
BUTX        : 'but' ;

// Separators

SEMICOLON           : ';' ;
COLON               : ':' ;
DOUBLE_COLON        : '::' ;
DOT                 : '.' ;
COMMA               : ',' ;
LEFT_BRACE          : '{' ;
RIGHT_BRACE         : '}' ;
LEFT_PARENTHESIS    : '(' ;
RIGHT_PARENTHESIS   : ')' ;
LEFT_BRACKET        : '[' ;
RIGHT_BRACKET       : ']' ;
QUESTION_MARK       : '?' ;

// Arithmetic operators

ASSIGN  : '=' ;
ADD     : '+' ;
SUB     : '-' ;
MUL     : '*' ;
DIV     : '/' ;
POW     : '^' ;
MOD     : '%';

// Relational operators

NOT         : '!' ;
EQUAL       : '==' ;
NOT_EQUAL   : '!=' ;
GT          : '>' ;
LT          : '<' ;
GT_EQUAL    : '>=' ;
LT_EQUAL    : '<=' ;
AND         : '&&' ;
OR          : '||' ;

// Additional symbols

RARROW      : '->' ;
LARROW      : '<-' ;
AT          : '@' ;
BACKTICK    : '`' ;
RANGE       : '..' ;
ELLIPSIS    : '...' ;
PIPE        : '|' ;
EQUAL_GT    : '=>' ;

TILDE           : '~';
BITAND          : '&';
BITOR           : '|';
DOUBLEQUOTE     : '"';

// Compound Assignment operators.

COMPOUND_ADD   : '+=' ;
COMPOUND_SUB   : '-=' ;
COMPOUND_MUL   : '*=' ;
COMPOUND_DIV   : '/=' ;

// Safe assignment operator

SAFE_ASSIGNMENT   : '=?' ;

// Post Arithmetic operators.

INCREMENT      : '++' ;
DECREMENT      : '--' ;

DecimalIntegerLiteral
    :   DecimalNumeral IntegerTypeSuffix?
    ;

HexIntegerLiteral
    :   HexNumeral IntegerTypeSuffix?
    ;

OctalIntegerLiteral
    :   OctalNumeral IntegerTypeSuffix?
    ;

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
    :   Digits '.' (Digits ExponentPart? FloatTypeSuffix? | Digits? ExponentPart FloatTypeSuffix? | Digits? ExponentPart? FloatTypeSuffix)
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
    :   ~["\\]
    |   ('\\' '\\')+
    |   ('\\' .)
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

XMLLiteralStart
    :   TYPE_XML WS* BACKTICK   { inTemplate = true; } -> pushMode(XML)
    ;

StringTemplateLiteralStart
    :   TYPE_STRING WS* BACKTICK   { inTemplate = true; } -> pushMode(STRING_TEMPLATE)
    ;

DocumentationTemplateStart
    :   DOCUMENTATION WS* LEFT_BRACE   { inDocTemplate = true; } -> pushMode(DOCUMENTATION_TEMPLATE)
    ;

DeprecatedTemplateStart
    :   DEPRECATED WS* LEFT_BRACE   { inDeprecatedTemplate = true; } -> pushMode(DEPRECATED_TEMPLATE)
    ;

ExpressionEnd
    :   {inTemplate}? RIGHT_BRACE WS* RIGHT_BRACE   ->  popMode
    ;

DocumentationTemplateAttributeEnd
    :   {inDocTemplate}? RIGHT_BRACE WS* RIGHT_BRACE               ->  popMode
    ;

// Whitespace and comments

WS  :  [ \t\r\n\u000C]+ -> channel(HIDDEN)
    ;

LINE_COMMENT
    :   '//' ~[\r\n]*   -> channel(HIDDEN)
    ;

fragment
IdentifierLiteral
    : '^"' IdentifierLiteralChar+ '"' ;

fragment
IdentifierLiteralChar
    : ~[|"\\\b\f\n\r\t]
    | IdentifierLiteralEscapeSequence
    ;

fragment
IdentifierLiteralEscapeSequence
    : '\\' [|"\\/]
    | '\\\\' [btnfr]
    | UnicodeEscape
    ;

fragment
ExpressionStart
    :   '{{'
    ;

ERRCHAR
	:	.	-> channel(HIDDEN)
	;

mode XML;

XMLEnd
    :   '`' { inTemplate = false; }          -> popMode
    ;

XMLExpressionStart
    :   XMLText? ExpressionStart            -> pushMode(DEFAULT_MODE)
    ;

// We cannot use "StringTemplateBracesSequence? (XMLChar XMLValidCharSequence?)*" because it
// can match an empty string.
XMLText
    :   XMLValidCharSequence? (XMLChar XMLValidCharSequence?)+
    |   XMLValidCharSequence (XMLChar XMLValidCharSequence?)*
    ;

fragment
XMLChar
    :   ~[`{\\]
    |   '\\' [`{]
    |   WS
    |   XMLEscapedSequence
    ;

fragment
XMLEscapedSequence
    :   '\\\\'
    |   '\\{{'
    |   '\\}}'
    ;

fragment
XMLValidCharSequence
    :   '{'
    |   '\\' ~'\\'
    ;

XML_ERRCHAR
	:	.	-> channel(HIDDEN)
	;

mode DOCUMENTATION_TEMPLATE;

DocumentationTemplateEnd
    :   RIGHT_BRACE { inDocTemplate = false; }                                 -> popMode
    ;

DocumentationTemplateAttributeStart
    :   AttributePrefix ExpressionStart                                        -> pushMode(DEFAULT_MODE)
    ;

SBDocInlineCodeStart
    :  AttributePrefix? DocBackTick                                            -> pushMode(SINGLE_BACKTICK_INLINE_CODE)
    ;

DBDocInlineCodeStart
    :  AttributePrefix? DocBackTick DocBackTick                                -> pushMode(DOUBLE_BACKTICK_INLINE_CODE)
    ;

TBDocInlineCodeStart
    :  AttributePrefix? DocBackTick DocBackTick DocBackTick                    -> pushMode(TRIPLE_BACKTICK_INLINE_CODE)
    ;

DocumentationTemplateText
    :   DocumentationValidCharSequence? (DocumentationTemplateStringChar DocumentationValidCharSequence?)+
    |   DocumentationValidCharSequence  (DocumentationTemplateStringChar DocumentationValidCharSequence?)*
    ;

fragment
DocumentationTemplateStringChar
    :   ~[`{}\\FPTRV]
    |   '\\' [{}`]
    |   WS
    |   DocumentationEscapedSequence
    ;

fragment
AttributePrefix
    :   [FPTRV]
    ;

fragment
DocBackTick
    :   '`'
    ;

fragment
DocumentationEscapedSequence
    :   '\\\\'
    ;

fragment
DocumentationValidCharSequence
     :  [FPTRV] ~[`{}\\]
     |  [FPTRV] '\\' [{}`]
     |  [FPTRV] '\\' ~[{}`]
     |  '\\' ~'\\'
     ;

DOCUMENTATION_TEMPLATE_ERRCHAR
	:	.	-> channel(HIDDEN)
	;

mode TRIPLE_BACKTICK_INLINE_CODE;

TripleBackTickInlineCodeEnd
    : BACKTICK BACKTICK BACKTICK              -> popMode
    ;

TripleBackTickInlineCode
    : TripleBackTickInlineCodeChar+
    ;

fragment
TripleBackTickInlineCodeChar
    :  ~[`]
    |   [`] ~[`]
    |   [`] [`] ~[`]
    ;

TRIPLE_BACKTICK_INLINE_CODE_ERRCHAR
	:	.	-> channel(HIDDEN)
	;

mode DOUBLE_BACKTICK_INLINE_CODE;

DoubleBackTickInlineCodeEnd
    : BACKTICK BACKTICK                       -> popMode
    ;

DoubleBackTickInlineCode
    : DoubleBackTickInlineCodeChar+
    ;

fragment
DoubleBackTickInlineCodeChar
    :  ~[`]
    |   [`] ~[`]
    ;

DOUBLE_BACKTICK_INLINE_CODE_ERRCHAR
	:	.	-> channel(HIDDEN)
	;

mode SINGLE_BACKTICK_INLINE_CODE;

SingleBackTickInlineCodeEnd
    : BACKTICK                                -> popMode
    ;

SingleBackTickInlineCode
    : SingleBackTickInlineCodeChar+
    ;

fragment
SingleBackTickInlineCodeChar
    :  ~[`]
    ;

SINGLE_BACKTICK_INLINE_CODE_ERRCHAR
	:	.	-> channel(HIDDEN)
	;

mode DEPRECATED_TEMPLATE;

DeprecatedTemplateEnd
    :   RIGHT_BRACE { inDeprecatedTemplate = false; }                         -> popMode
    ;

SBDeprecatedInlineCodeStart
    :  DeprecatedBackTick                                                     -> pushMode(SINGLE_BACKTICK_INLINE_CODE)
    ;

DBDeprecatedInlineCodeStart
    :  DeprecatedBackTick DeprecatedBackTick                                  -> pushMode(DOUBLE_BACKTICK_INLINE_CODE)
    ;

TBDeprecatedInlineCodeStart
    :  DeprecatedBackTick DeprecatedBackTick DeprecatedBackTick               -> pushMode(TRIPLE_BACKTICK_INLINE_CODE)
    ;

DeprecatedTemplateText
    :   DeprecatedValidCharSequence? (DeprecatedTemplateStringChar DeprecatedValidCharSequence?)+
    |   DeprecatedValidCharSequence (DeprecatedTemplateStringChar DeprecatedValidCharSequence?)*
    ;

fragment
DeprecatedTemplateStringChar
    :   ~[`{}\\]
    |   '\\' [{}`]
    |   WS
    |   DeprecatedEscapedSequence
    ;

fragment
DeprecatedBackTick
    :   '`'
    ;

fragment
DeprecatedEscapedSequence
    :   '\\\\'
    ;

fragment
DeprecatedValidCharSequence
     :  '\\' ~'\\'
     ;

DEPRECATED_TEMPLATE_ERRCHAR
	:	.	-> channel(HIDDEN)
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

STRING_TEMPLATE_ERRCHAR
	:	.	-> channel(HIDDEN)
	;
