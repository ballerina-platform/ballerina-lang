lexer grammar BallerinaLexer;

@members {
    boolean inTemplate = false;
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
EXTERN      : 'extern' ;
FINAL       : 'final' ;
SERVICE     : 'service' ;
RESOURCE    : 'resource' ;
FUNCTION    : 'function' ;
OBJECT      : 'object' ;
RECORD      : 'record' ;
ANNOTATION  : 'annotation' ;
PARAMETER   : 'parameter' ;
TRANSFORMER : 'transformer' ;
WORKER      : 'worker' ;
LISTENER    : 'listener' ;
REMOTE      : 'remote' ;
XMLNS       : 'xmlns' ;
RETURNS     : 'returns';
VERSION     : 'version';
DEPRECATED  : 'deprecated';
CHANNEL     : 'channel';
ABSTRACT    : 'abstract';
CLIENT      : 'client';
CONST       : 'const';

FROM        : 'from' { inTableSqlQuery = true; inSiddhiInsertQuery = true; inSiddhiOutputRateLimit = true; } ;
ON          : 'on' ;
SELECT      : {inTableSqlQuery}? 'select' { inTableSqlQuery = false; } ;
GROUP       : 'group' ;
BY          : 'by' ;
HAVING      : 'having' ;
ORDER       : 'order' ;
WHERE       : 'where' ;
FOLLOWED    : 'followed' ;
INTO        : 'into' ;
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
SECONDS     : {inSiddhiTimeScaleQuery}? 'seconds' { inSiddhiTimeScaleQuery = false; } ;
MINUTES     : {inSiddhiTimeScaleQuery}? 'minutes' { inSiddhiTimeScaleQuery = false; } ;
HOURS       : {inSiddhiTimeScaleQuery}? 'hours' { inSiddhiTimeScaleQuery = false; } ;
DAYS        : {inSiddhiTimeScaleQuery}? 'days' { inSiddhiTimeScaleQuery = false; } ;
MONTHS      : {inSiddhiTimeScaleQuery}? 'months' { inSiddhiTimeScaleQuery = false; } ;
YEARS       : {inSiddhiTimeScaleQuery}? 'years' { inSiddhiTimeScaleQuery = false; } ;
FOREVER     : 'forever' ;
LIMIT       : 'limit' ;
ASCENDING   : 'ascending' ;
DESCENDING  : 'descending' ;

TYPE_INT        : 'int' ;
TYPE_BYTE       : 'byte' ;
TYPE_FLOAT      : 'float' ;
TYPE_DECIMAL    : 'decimal' ;
TYPE_BOOL       : 'boolean' ;
TYPE_STRING     : 'string' ;
TYPE_ERROR      : 'error' ;
TYPE_MAP        : 'map' ;
TYPE_JSON       : 'json' ;
TYPE_XML        : 'xml' ;
TYPE_TABLE      : 'table' ;
TYPE_STREAM     : 'stream' ;
TYPE_ANY        : 'any' ;
TYPE_DESC       : 'typedesc' ;
TYPE            : 'type' ;
TYPE_FUTURE     : 'future' ;
TYPE_ANYDATA    : 'anydata' ;

VAR         : 'var' ;
NEW         : 'new' ;
OBJECT_INIT : '__init' ;
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
TRY         : 'try' ;
CATCH       : 'catch' ;
FINALLY     : 'finally' ;
THROW       : 'throw' ;
PANIC       : 'panic' ;
TRAP        : 'trap' ;
RETURN      : 'return' ;
TRANSACTION : 'transaction' ;
ABORT       : 'abort' ;
RETRY       : 'retry' ;
ONRETRY     : 'onretry' ;
RETRIES     : 'retries' ;
COMMITTED   : 'committed' ;
ABORTED     : 'aborted' ;
WITH        : 'with' ;
IN          : 'in' ;
LOCK        : 'lock' ;
UNTAINT     : 'untaint' ;
START       : 'start' ;
BUT         : 'but' ;
CHECK       : 'check' ;
PRIMARYKEY  : 'primarykey' ;
IS          : 'is' ;
FLUSH       : 'flush' ;
WAIT        : 'wait' ;

// Separators

SEMICOLON           : ';' ;
COLON               : ':' ;
DOT                 : '.' ;
COMMA               : ',' ;
LEFT_BRACE          : '{' ;
RIGHT_BRACE         : '}' ;
LEFT_PARENTHESIS    : '(' ;
RIGHT_PARENTHESIS   : ')' ;
LEFT_BRACKET        : '[' ;
RIGHT_BRACKET       : ']' ;
QUESTION_MARK       : '?' ;

// Documentation markdown

fragment
HASH                : '#' ;

// Arithmetic operators

ASSIGN  : '=' ;
ADD     : '+' ;
SUB     : '-' ;
MUL     : '*' ;
DIV     : '/' ;
MOD     : '%';

// Relational operators

NOT             : '!' ;
EQUAL           : '==' ;
NOT_EQUAL       : '!=' ;
GT              : '>' ;
LT              : '<' ;
GT_EQUAL        : '>=' ;
LT_EQUAL        : '<=' ;
AND             : '&&' ;
OR              : '||' ;
REF_EQUAL       : '===' ;
REF_NOT_EQUAL   : '!==' ;

// Bitwise Operators

BIT_AND          : '&' ;
BIT_XOR          : '^' ;
BIT_COMPLEMENT   : '~' ;

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
SYNCRARROW  : '->>' ;

// Compound Assignment operators.

COMPOUND_ADD   : '+=' ;
COMPOUND_SUB   : '-=' ;
COMPOUND_MUL   : '*=' ;
COMPOUND_DIV   : '/=' ;

COMPOUND_BIT_AND   : '&=' ;
COMPOUND_BIT_OR    : '|=' ;
COMPOUND_BIT_XOR   : '^=' ;

COMPOUND_LEFT_SHIFT      : '<<=' ;
COMPOUND_RIGHT_SHIFT     : '>>=' ;
COMPOUND_LOGICAL_SHIFT   : '>>>=' ;

// Integer Range Operators.
// CLOSED_RANGE - ELLIPSIS
HALF_OPEN_RANGE   : '..<' ;

DecimalIntegerLiteral
    :   DecimalNumeral
    ;

HexIntegerLiteral
    :   HexNumeral
    ;

fragment
DecimalNumeral
    :   '0'
    |   NonZeroDigit Digits?
    ;

fragment
Digits
    :   Digit+
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
HexNumeral
    :   '0' [xX] HexDigits
    ;

fragment
DottedHexNumber
    :   HexDigits DOT HexDigits
    |   DOT HexDigits
    ;

fragment
DottedDecimalNumber
    :   DecimalNumeral DOT Digits
    |   DOT Digits
    ;

fragment
HexDigits
    :   HexDigit+
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

// §3.10.2 Floating-Point Literals

HexadecimalFloatingPointLiteral
    :   HexIndicator HexFloatingPointNumber
    ;

DecimalFloatingPointNumber
    :   DecimalNumeral ExponentPart
    |   DottedDecimalNumber ExponentPart?
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
HexIndicator
    :   '0' [xX]
    ;

fragment
HexFloatingPointNumber
    :   HexDigits BinaryExponent
    |   DottedHexNumber BinaryExponent?
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

SymbolicStringLiteral
    :   '\'' (UndelimeteredInitialChar UndelimeteredFollowingChar*)
    ;

fragment
UndelimeteredInitialChar
    : [a-zA-Z_]
    // Negates ASCII characters
    // Negates unicode whitespace characters : 0x200E, 0x200F, 0x2028 and 0x2029
    // Negates unicode characters with property Pattern_Syntax=True (http://unicode.org/reports/tr31/tr31-2.html#Pattern_Syntax)
    // Negates unicode characters of category "Private Use" ranging from: 0xE000 .. 0xF8FF | 0xF0000 .. 0xFFFFD | 0x100000 .. 0x10FFFD
    | ~ [\u0000-\u007F\uE000-\uF8FF\u200E\u200F\u2028\u2029\u00A1-\u00A7\u00A9\u00AB-\u00AC\u00AE\u00B0-\u00B1\u00B6-\u00B7\u00BB\u00BF\u00D7\u00F7\u2010-\u2027\u2030-\u205E\u2190-\u2BFF\u3001-\u3003\u3008-\u3020\u3030\uFD3E-\uFD3F\uFE45-\uFE46\uDB80-\uDBBF\uDBC0-\uDBFF\uDC00-\uDFFF]
    ;

fragment
UndelimeteredFollowingChar
    : UndelimeteredInitialChar
    | DIGIT
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
    |   UnicodeEscape
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
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

DocumentationLineStart
    :   HASH DocumentationSpace? -> pushMode(MARKDOWN_DOCUMENTATION)
    ;

ParameterDocumentationStart
    :   HASH DocumentationSpace? ADD DocumentationSpace* -> pushMode(MARKDOWN_DOCUMENTATION_PARAM)
    ;

ReturnParameterDocumentationStart
    :   HASH DocumentationSpace? ADD DocumentationSpace* RETURN DocumentationSpace* SUB DocumentationSpace* -> pushMode(MARKDOWN_DOCUMENTATION)
    ;


DeprecatedTemplateStart
    :   DEPRECATED WS* LEFT_BRACE   { inDeprecatedTemplate = true; } -> pushMode(DEPRECATED_TEMPLATE)
    ;

ExpressionEnd
    :   {inTemplate}? RIGHT_BRACE RIGHT_BRACE   ->  popMode
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

mode MARKDOWN_DOCUMENTATION;

VARIABLE    : 'variable';
MODULE      : 'module';

ReferenceType
    :   TYPE|SERVICE|VARIABLE|VAR|ANNOTATION|MODULE|FUNCTION|PARAMETER
    ;

DocumentationText
    :   (DocumentationTextCharacter | DocumentationEscapedCharacters)+
    ;

SingleBacktickStart
    :   BACKTICK -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION)
    ;

DoubleBacktickStart
    :   BACKTICK BACKTICK -> pushMode(DOUBLE_BACKTICKED_DOCUMENTATION)
    ;

TripleBacktickStart
    :   BACKTICK BACKTICK BACKTICK -> pushMode(TRIPLE_BACKTICKED_DOCUMENTATION)
    ;

DefinitionReference
    :   ReferenceType DocumentationSpace+
    ;

fragment
DocumentationTextCharacter
    :   ~[`\n ]
    |   '\\' BACKTICK
    ;

DocumentationEscapedCharacters
    :   DocumentationSpace
    ;

DocumentationSpace
    :   [ ]
    ;

DocumentationEnd
    :   [\n] -> channel(HIDDEN), popMode
    ;

mode MARKDOWN_DOCUMENTATION_PARAM;

ParameterName
    :   Identifier
    ;

DescriptionSeparator
    :   DocumentationSpace* SUB DocumentationSpace* -> popMode, pushMode(MARKDOWN_DOCUMENTATION)
    ;

DocumentationParamEnd
    :   [\n] -> channel(HIDDEN), popMode
    ;

mode SINGLE_BACKTICKED_DOCUMENTATION;

SingleBacktickContent
    :   ((~[`\n] | '\\' BACKTICK)* [\n])? (DocumentationLineStart (~[`\n] | '\\' BACKTICK)* [\n]?)+
    |   (~[`\n] | '\\' BACKTICK)+
    ;

SingleBacktickEnd
    :   BACKTICK -> popMode
    ;

mode DOUBLE_BACKTICKED_DOCUMENTATION;

DoubleBacktickContent
    :   ((~[`\n] | BACKTICK ~[`])* [\n])? (DocumentationLineStart (~[`\n] | BACKTICK ~[`])* [\n]?)+
    |   (~[`\n] | BACKTICK ~[`])+
    ;

DoubleBacktickEnd
    :   BACKTICK BACKTICK -> popMode
    ;

mode TRIPLE_BACKTICKED_DOCUMENTATION;

TripleBacktickContent
    :   ((~[`\n] | BACKTICK ~[`] | BACKTICK BACKTICK ~[`])* [\n])? (DocumentationLineStart (~[`\n] | BACKTICK ~[`] | BACKTICK BACKTICK ~[`])* [\n]?)+
    |   (~[`\n] | BACKTICK ~[`] | BACKTICK BACKTICK ~[`])+
    ;

TripleBacktickEnd
    :   BACKTICK BACKTICK BACKTICK -> popMode
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
    :   [ \t\r\n]   -> channel(HIDDEN)
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

// Todo - Remove after finalizing the new deprecated annotation

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
