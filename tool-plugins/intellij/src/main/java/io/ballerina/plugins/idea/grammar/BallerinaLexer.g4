/*
 * NOTE - Do not modify.
 */

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

IMPORT      : 'import' ;
AS          : 'as' ;
PUBLIC      : 'public' ;
PRIVATE     : 'private' ;
NATIVE      : 'native' ;
SERVICE     : 'service' ;
RESOURCE    : 'resource' ;
FUNCTION    : 'function' ;
OBJECT      : 'object' ;
RECORD      : 'record';
ANNOTATION  : 'annotation' ;
PARAMETER   : 'parameter' ;
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
FOR         : 'for' { inSiddhiTimeScaleQuery = true; } ;
WINDOW      : 'window' ;
QUERY       : 'query' ;
EXPIRED     : 'expired' ;
CURRENT     : 'current' ;
EVENTS      : {inSiddhiInsertQuery}? 'events' { inSiddhiInsertQuery = false; } ;
EVERY       : 'every' ;
WITHIN      : 'within' { inSiddhiTimeScaleQuery = true; } ;
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
SECONDS      : {inSiddhiTimeScaleQuery}? 'seconds' { inSiddhiTimeScaleQuery = false; } ;
MINUTES      : {inSiddhiTimeScaleQuery}? 'minutes' { inSiddhiTimeScaleQuery = false; } ;
HOURS        : {inSiddhiTimeScaleQuery}? 'hours' { inSiddhiTimeScaleQuery = false; } ;
DAYS         : {inSiddhiTimeScaleQuery}? 'days' { inSiddhiTimeScaleQuery = false; } ;
MONTHS      : {inSiddhiTimeScaleQuery}? 'months' { inSiddhiTimeScaleQuery = false; } ;
YEARS        : {inSiddhiTimeScaleQuery}? 'years' { inSiddhiTimeScaleQuery = false; } ;
FOREVER     : 'forever' ;
LIMIT       : 'limit' ;
ASCENDING   : 'ascending' ;
DESCENDING  : 'descending' ;

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
TYPE            : 'type' ;
TYPE_FUTURE     : 'future' ;

VAR         : 'var' ;
NEW         : 'new' ;
IF          : 'if' ;
MATCH       : 'match' ;
ELSE        : 'else' ;
FOREACH     : 'foreach' ;
WHILE       : 'while' ;
CONTINUE    : 'continue' ;
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
RETRY        : 'retry' ;
ONRETRY     : 'onretry' ;
RETRIES     : 'retries' ;
ONABORT     : 'onabort' ;
ONCOMMIT    : 'oncommit' ;
LENGTHOF    : 'lengthof' ;
WITH        : 'with' ;
IN          : 'in' ;
LOCK        : 'lock' ;
UNTAINT     : 'untaint' ;
START       : 'start' ;
AWAIT       : 'await' ;
BUT         : 'but' ;
CHECK       : 'check' ;
DONE        : 'done' ;

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
ELVIS       : '?:' ;


// Compound Assignment operators.

COMPOUND_ADD   : '+=' ;
COMPOUND_SUB   : '-=' ;
COMPOUND_MUL   : '*=' ;
COMPOUND_DIV   : '/=' ;

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

// Blob Literal

Base16BlobLiteral
    : 'base16' WS* BACKTICK HexGroup* WS* BACKTICK
    ;

fragment
HexGroup
    : WS* HexDigit WS* HexDigit
    ;

Base64BlobLiteral
    : 'base64' WS* BACKTICK Base64Group* PaddedBase64Group? WS* BACKTICK
    ;

fragment
Base64Group
    : WS* Base64Char WS* Base64Char WS* Base64Char WS* Base64Char
    ;

fragment
PaddedBase64Group
    : WS* Base64Char WS* Base64Char WS* Base64Char WS* PaddingChar
    | WS* Base64Char WS* Base64Char WS* PaddingChar WS* PaddingChar
    ;

fragment
Base64Char
    : [a-zA-Z0-9+/]
    ;

fragment
PaddingChar : '=';

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

WS  :  [ \t]+ -> channel(HIDDEN)
    ;
NEW_LINE  :  [\r\n\u000C]+ -> channel(HIDDEN)
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


// XML lexer rules

// Everything in an XML Literal (inside backtick)
mode XML;

XML_COMMENT_START
    :   '<!--'  -> pushMode(XML_COMMENT)
    ;

CDATA
    :   '<![CDATA[' .*? ']]>'
    ;

DTD
    :   '<!' (~[-].|.~[-]).*? '>'    -> skip
    ;

EntityRef
    :   '&' XMLQName ';'
    ;

CharRef
    :   '&#' Digit+ ';'
    |   '&#x' HexDigits+ ';'
    ;

fragment
XML_WS
    :   ' '|'\t'|'\r'? '\n'
    ;

XML_TAG_OPEN            :   LT                              -> pushMode(XML_TAG) ;
XML_TAG_OPEN_SLASH      :   '</'                            -> pushMode(XML_TAG) ;

XML_TAG_SPECIAL_OPEN
    :   '<?' (XMLQName QNAME_SEPARATOR)? XMLQName XML_WS    -> pushMode(XML_PI)
    ;

XMLLiteralEnd
    :   '`' { inTemplate = false; }          -> popMode
    ;

fragment
ExpressionStart
    :   '{{'
    ;

XMLTemplateText
    :   XMLText? ExpressionStart            -> pushMode(DEFAULT_MODE)
    ;

XMLText
    :   XMLBracesSequence? (XMLTextChar XMLBracesSequence?)+
    |   XMLBracesSequence (XMLTextChar XMLBracesSequence?)*
    ;

fragment
XMLTextChar
    :    ~[<&`{}]
    |    '\\' [`]
    |    XML_WS
    |    XMLEscapedSequence
    ;

fragment
XMLEscapedSequence
    :   '\\\\'
    |   '\\{{'
    |   '\\}}'
    ;

fragment
XMLBracesSequence
    :   '{}'+
    |   '}{'
    |   ('{}')* '{'
    |   '}' ('{}')*
    ;


// Everything inside an XML tag
mode XML_TAG;

XML_TAG_CLOSE           : GT        -> popMode ;
XML_TAG_SPECIAL_CLOSE   : '?>'      -> popMode ;     // close <?xml...?>
XML_TAG_SLASH_CLOSE     : '/>'      -> popMode ;
SLASH                   : DIV ;
QNAME_SEPARATOR         : COLON ;
EQUALS                  : ASSIGN ;
DOUBLE_QUOTE            : '"'       -> pushMode(DOUBLE_QUOTED_XML_STRING);
SINGLE_QUOTE            : '\''      -> pushMode(SINGLE_QUOTED_XML_STRING);

XMLQName
    :   NameStartChar NameChar*
    ;

XML_TAG_WS
    :   [ \t\r\n]   -> skip
    ;

XMLTagExpressionStart
    :   ExpressionStart             -> pushMode(DEFAULT_MODE)
    ;

fragment
HEXDIGIT
    :   [a-fA-F0-9]
    ;

fragment
DIGIT
    :   [0-9]
    ;

fragment
NameChar
    :   NameStartChar
    |   '-' | '_' | '.' | DIGIT
    |   '\u00B7'
    |   '\u0300'..'\u036F'
    |   '\u203F'..'\u2040'
    ;

fragment
NameStartChar
    :   [a-zA-Z]
    |   '\u2070'..'\u218F'
    |   '\u2C00'..'\u2FEF'
    |   '\u3001'..'\uD7FF'
    |   '\uF900'..'\uFDCF'
    |   '\uFDF0'..'\uFFFD'
    ;


// Everything inside a double-quoted xml string (e.g: attribute values)
mode DOUBLE_QUOTED_XML_STRING;

DOUBLE_QUOTE_END
    :   DOUBLE_QUOTE  -> popMode
    ;

XMLDoubleQuotedTemplateString
    :   XMLDoubleQuotedString? ExpressionStart    -> pushMode(DEFAULT_MODE)
    ;

XMLDoubleQuotedString
    :   XMLBracesSequence? (XMLDoubleQuotedStringChar XMLBracesSequence?)+
    |   XMLBracesSequence (XMLDoubleQuotedStringChar XMLBracesSequence?)*
    ;

fragment
XMLDoubleQuotedStringChar
    :    ~[<"{}\\]
    |    XMLEscapedSequence
    ;


// Everything inside a single-quoted xml string (e.g: attribute values)
mode SINGLE_QUOTED_XML_STRING;

SINGLE_QUOTE_END
    :   SINGLE_QUOTE    -> popMode
    ;

XMLSingleQuotedTemplateString
    :   XMLSingleQuotedString? ExpressionStart    -> pushMode(DEFAULT_MODE)
    ;

XMLSingleQuotedString
    :   XMLBracesSequence? (XMLSingleQuotedStringChar XMLBracesSequence?)+
    |   XMLBracesSequence (XMLSingleQuotedStringChar XMLBracesSequence?)*
    ;

fragment
XMLSingleQuotedStringChar
    :    ~[<'{}\\]
    |    XMLEscapedSequence
    ;

mode XML_PI;

fragment
XML_PI_END
    :   XML_TAG_SPECIAL_CLOSE
    ;

XMLPIText
    :   XMLPITextFragment XML_PI_END    -> popMode
    ;

XMLPITemplateText
    :   XMLPITextFragment ExpressionStart    -> pushMode(DEFAULT_MODE)
    ;

fragment
XMLPITextFragment
    :    XMLPIAllowedSequence? (XMLPIChar XMLPIAllowedSequence?)*
    ;

fragment
XMLPIChar
    :    ~[{}?>]
    |    XMLEscapedSequence
    ;


fragment
XMLPIAllowedSequence
    :   XMLBracesSequence
    |   XMLPISpecialSequence
    |   (XMLBracesSequence XMLPISpecialSequence)+ XMLBracesSequence?
    |   (XMLPISpecialSequence XMLBracesSequence)+ XMLPISpecialSequence?
    ;

fragment
XMLPISpecialSequence
    :   '>'+
    |   '>'* '?'+
    ;


// Everything inside an XML comment
mode XML_COMMENT;

fragment
XML_COMMENT_END
    :   '-->'
    ;

XMLCommentText
    :    XMLCommentTextFragment XML_COMMENT_END    -> popMode
    ;

XMLCommentTemplateText
    :   XMLCommentTextFragment ExpressionStart    -> pushMode(DEFAULT_MODE)
    ;

fragment
XMLCommentTextFragment
    :   XMLCommentAllowedSequence? (XMLCommentChar XMLCommentAllowedSequence?)*
    ;

fragment
XMLCommentChar
    :    ~[{}>\-]
    |    XMLEscapedSequence
    ;

fragment
XMLCommentAllowedSequence
    :   XMLBracesSequence
    |   XMLCommentSpecialSequence
    |   (XMLBracesSequence XMLCommentSpecialSequence)+ XMLBracesSequence?
    |   (XMLCommentSpecialSequence XMLBracesSequence)+ XMLCommentSpecialSequence?
    ;

fragment
XMLCommentSpecialSequence
    :   '>'+
    |   ('>'* '-' '>'+)+
    |   '-'? '>'* '-'+
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
    :   ~[`{}\\FPTRVE]
    |   '\\' [{}`]
    |   WS
    |   DocumentationEscapedSequence
    ;

fragment
AttributePrefix
    :   [FPTRVE]
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
     :  [FPTRVE] ~[`{}\\]
     |  [FPTRVE] '\\' [{}`]
     |  [FPTRVE] '\\' ~[{}`]
     |  '\\' ~'\\'
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
