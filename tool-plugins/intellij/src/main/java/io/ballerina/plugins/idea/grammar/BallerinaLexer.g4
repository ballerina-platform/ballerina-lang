lexer grammar BallerinaLexer;

@members {
    boolean inStringTemplate = false;
    boolean inQueryExpression = false;
}

// Reserved words

IMPORT      : 'import' ;
AS          : 'as' ;
PUBLIC      : 'public' ;
PRIVATE     : 'private' ;
EXTERNAL    : 'external' ;
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
RETURNS     : 'returns' ;
VERSION     : 'version' ;
CHANNEL     : 'channel' ;
ABSTRACT    : 'abstract' ;
CLIENT      : 'client' ;
CONST       : 'const' ;
TYPEOF      : 'typeof';
SOURCE      : 'source' ;
ON          : 'on' ;

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
TYPE_HANDLE     : 'handle' ;

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
CHECKPANIC  : 'checkpanic' ;
PRIMARYKEY  : 'primarykey' ;
IS          : 'is' ;
FLUSH       : 'flush' ;
WAIT        : 'wait' ;
DEFAULT     : 'default' ;
FROM        : 'from' { inQueryExpression = true; } ;
SELECT      : {inQueryExpression}? 'select' { inQueryExpression = false; } ;
DO          : {inQueryExpression}? 'do' { inQueryExpression = false; } ;
WHERE       : {inQueryExpression}? 'where' ;
LET         : 'let' ;

// Separators

SEMICOLON           : ';' ;
COLON               : ':' ;
DOT                 : '.' ;
COMMA               : ',' ;
LEFT_BRACE          : '{' ;
RIGHT_BRACE         : '}'
{
if (inStringTemplate)
{
    popMode();
}
};
LEFT_PARENTHESIS    : '(' ;
RIGHT_PARENTHESIS   : ')' ;
LEFT_BRACKET        : '[' ;
RIGHT_BRACKET       : ']' ;
QUESTION_MARK       : '?' ;

OPTIONAL_FIELD_ACCESS   : '?.' ;

// Delimiters
LEFT_CLOSED_RECORD_DELIMITER     : '{|' ;
RIGHT_CLOSED_RECORD_DELIMITER    : '|}' ;

// Documentation markdown

fragment
HASH                : '#' ;

// Arithmetic operators

ASSIGN  : '=' ;
ADD     : '+' ;
SUB     : '-' ;
MUL     : '*' ;
DIV     : '/' ;
MOD     : '%' ;

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

// Annotation Access.
ANNOTATION_ACCESS   : '.@' ;

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
    :   DecimalNumeral ExponentPart DecimalFloatSelector?
    |   DottedDecimalNumber ExponentPart? DecimalFloatSelector?
    ;

DecimalExtendedFloatingPointNumber
    :   DecimalFloatingPointNumber DOT DecimalNumeral
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
DecimalFloatSelector
    : [dDfF]
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

fragment
StringCharacters
    :   StringCharacter+
    ;

fragment
StringCharacter
    :   ~["\\\u000A\u000D]
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
    :   '\\' 'u' LEFT_BRACE HexDigit+ RIGHT_BRACE
    ;

// Blob Literal

Base16BlobLiteral
    :   'base16' WS* BACKTICK HexGroup* WS* BACKTICK
    ;

fragment
HexGroup
    :   WS* HexDigit WS* HexDigit
    ;

Base64BlobLiteral
    :   'base64' WS* BACKTICK Base64Group* PaddedBase64Group? WS* BACKTICK
    ;

fragment
Base64Group
    :   WS* Base64Char WS* Base64Char WS* Base64Char WS* Base64Char
    ;

fragment
PaddedBase64Group
    :   WS* Base64Char WS* Base64Char WS* Base64Char WS* PaddingChar
    |   WS* Base64Char WS* Base64Char WS* PaddingChar WS* PaddingChar
    ;

fragment
Base64Char
    :   [a-zA-Z0-9+/]
    ;

fragment
PaddingChar : '=';

// §3.10.7 The Null Literal

NullLiteral
    :   'null'
    ;

Identifier
    :   UnquotedIdentifier
    |   QuotedIdentifier
    ;

fragment
UnquotedIdentifier
    :   IdentifierInitialChar IdentifierFollowingChar*
    ;

fragment
QuotedIdentifier
    :   '\'' QuotedIdentifierChar+
    ;

fragment
QuotedIdentifierChar
    :   IdentifierFollowingChar
    |   QuotedIdentifierEscape
    |   StringNumericEscape
    ;

// IdentifierInitialChar :=  AsciiLetter | _ | UnicodeIdentifierChar
// UnicodeIdentifierChar := ^ ( AsciiChar | UnicodeNonIdentifierChar )
// AsciiChar := 0x0 .. 0x7F
// UnicodeNonIdentifierChar := UnicodePrivateUseChar | UnicodePatternWhiteSpaceChar | UnicodePatternSyntaxChar
// UnicodePrivateUseChar := 0xE000 .. 0xF8FF | 0xF0000 .. 0xFFFFD | 0x100000 .. 0x10FFFD
// UnicodePatternWhiteSpaceChar := 0x200E | 0x200F | 0x2028 | 0x2029
// UnicodePatternSyntaxChar := character with Unicode property Pattern_Syntax=True (http://unicode.org/reports/tr31/tr31-2.html#Pattern_Syntax)
fragment
IdentifierInitialChar
    : [a-zA-Z_]
    // Negates ( AsciiChar | UnicodeNonIdentifierChar )
    | ~ [\u0000-\u007F\uE000-\uF8FF\u200E\u200F\u2028\u2029\u00A1-\u00A7\u00A9\u00AB-\u00AC\u00AE\u00B0-\u00B1\u00B6-\u00B7\u00BB\u00BF\u00D7\u00F7\u2010-\u2027\u2030-\u205E\u2190-\u2BFF\u3001-\u3003\u3008-\u3020\u3030\uFD3E-\uFD3F\uFE45-\uFE46\uDB80-\uDBBF\uDBC0-\uDBFF\uDC00-\uDFFF]
    ;

fragment
IdentifierFollowingChar
    :   IdentifierInitialChar
    |   DIGIT
    ;

// QuotedIdentifierEscape := \ ^ ( AsciiLetter | 0x9 | 0xA | 0xD | UnicodePatternWhiteSpaceChar )
// AsciiLetter := A .. Z | a .. z
// UnicodePatternWhiteSpaceChar := 0x200E | 0x200F | 0x2028 | 0x2029
fragment
QuotedIdentifierEscape
    :   '\\' ~([a-zA-Z]|'\u0009'|'\u000A'|'\u000D'|'\u200E'|'\u200F'|'\u2028'|'\u2029')
    ;

fragment
StringNumericEscape
    :   '\\' [|"\\/]
    |   '\\\\' [btnfr]
    |   UnicodeEscape
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
    :   TYPE_XML WS* BACKTICK   { inStringTemplate = true; } -> pushMode(XML)
    ;

StringTemplateLiteralStart
    :   TYPE_STRING WS* BACKTICK   { inStringTemplate = true; } -> pushMode(STRING_TEMPLATE)
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

// Whitespace and comments

WS
    :   [ \t]+ -> channel(HIDDEN)
    ;

NEW_LINE
    :   [\r\n\u000C]+ -> channel(HIDDEN)
    ;

LINE_COMMENT
    :   '//' ~[\r\n]*   -> channel(HIDDEN)
    ;

mode MARKDOWN_DOCUMENTATION;

DOCTYPE         :   'type' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCSERVICE      :   'service' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCVARIABLE     :   'variable' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCVAR          :   'var' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCANNOTATION   :   'annotation' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCMODULE       :   'module' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCFUNCTION     :   'function' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCPARAMETER    :   'parameter' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);
DOCCONST        :   'const' DocumentationEscapedCharacters+ '`' -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION);

SingleBacktickStart
    :   BACKTICK -> pushMode(SINGLE_BACKTICKED_DOCUMENTATION)
    ;

DocumentationText
    :   DocumentationTextCharacter+
    ;

DoubleBacktickStart
    :   BACKTICK BACKTICK -> pushMode(DOUBLE_BACKTICKED_DOCUMENTATION)
    ;

TripleBacktickStart
    :   BACKTICK BACKTICK BACKTICK -> pushMode(TRIPLE_BACKTICKED_DOCUMENTATION)
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
    :   '`' { inStringTemplate = false; }                   -> popMode
    ;

fragment
INTERPOLATION_START
    :   '${'
    ;

XMLTemplateText
    :   XMLText? INTERPOLATION_START                        -> pushMode(DEFAULT_MODE)
    ;

XMLText
    :   XMLTextChar+
    ;

fragment
XMLTextChar
    :   ~[<&$`{]
    |   '\\' [`]
    |   XML_WS
    |   XMLEscapedSequence
    |   DollarSequence
    |   XMLBracesSequence
    ;

fragment
DollarSequence
    :  '$'+ LookAheadTokenIsNotOpenBrace
    ;

fragment
XMLEscapedSequence
    :   '\\\\'
    |   '\\${'
    |   '\\}'
    |   '\\{'
    |   '&' ('gt' | 'lt' | 'amp') ';'
    ;

fragment
XMLBracesSequence
    :   '{}'+ '{'* '}'*
    |   '}{'+ '{'* '}'*
    |   '{{'+ '{'* '}'*
    |   '}}'+ '{'* '}'*
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
    |   '-' |'.' | DIGIT
    |   '\u00B7'
    |   '\u0300'..'\u036F'
    |   '\u203F'..'\u2040'
    ;

fragment
NameStartChar
    :   [a-zA-Z_]
    |   '\u2070'..'\u218F'
    |   '\u2C00'..'\u2FEF'
    |   '\u3001'..'\uD7FF'
    |   '\uF900'..'\uFDCF'
    |   '\uFDF0'..'\uFFFD'
    ;


// Everything inside a double-quoted xml string (e.g: attribute values)
mode DOUBLE_QUOTED_XML_STRING;

DOUBLE_QUOTE_END
    :   DOUBLE_QUOTE                                        -> popMode
    ;

XMLDoubleQuotedTemplateString
    :   XMLDoubleQuotedString? INTERPOLATION_START          -> pushMode(DEFAULT_MODE)
    ;

XMLDoubleQuotedString
    :   XMLBracesSequence? (XMLDoubleQuotedStringChar XMLBracesSequence?)+
    |   XMLBracesSequence (XMLDoubleQuotedStringChar XMLBracesSequence?)*
    ;

fragment
XMLDoubleQuotedStringChar
    :   ~[$<"{}\\]
    |   XMLEscapedSequence
    |   DollarSequence
    ;


// Everything inside a single-quoted xml string (e.g: attribute values)
mode SINGLE_QUOTED_XML_STRING;

SINGLE_QUOTE_END
    :   SINGLE_QUOTE    -> popMode
    ;

XMLSingleQuotedTemplateString
    :   XMLSingleQuotedString? INTERPOLATION_START    -> pushMode(DEFAULT_MODE)
    ;

XMLSingleQuotedString
    :   XMLBracesSequence? (XMLSingleQuotedStringChar XMLBracesSequence?)+
    |   XMLBracesSequence (XMLSingleQuotedStringChar XMLBracesSequence?)*
    ;

fragment
XMLSingleQuotedStringChar
    :   ~[$<'{}\\]
    |   XMLEscapedSequence
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
    :   XMLPITextFragment INTERPOLATION_START    -> pushMode(DEFAULT_MODE)
    ;

fragment
XMLPITextFragment
    :   XMLPIAllowedSequence? (XMLPIChar XMLPIAllowedSequence?)*
    ;

fragment
XMLPIChar
    :   ~[${}?>]
    |   XMLEscapedSequence
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


XML_COMMENT_END
    :   '-->'                                               -> popMode
    ;

XMLCommentTemplateText
    :   XMLCommentTextFragment INTERPOLATION_START          -> pushMode(DEFAULT_MODE)
    ;

fragment
XMLCommentTextFragment
    :   XMLCommentAllowedSequence? (XMLCommentChar XMLCommentAllowedSequence?)*
    ;

XMLCommentText
    :   XMLCommentAllowedSequence? (XMLCommentChar+ XMLCommentAllowedSequence?)
    ;

fragment
XMLCommentChar
    :   ~[>${\-]
    |   XMLBracesSequence
    |   XMLEscapedSequence
    |   '\\' [`]
    |   '$' LookAheadTokenIsNotOpenBrace
    ;

fragment
LookAheadTokenIsNotOpenBrace
    : {_input.LA(1) != '{'}?
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
    |   '>'? '-' LookAheadTokenIsNotHypen
    ;

fragment
LookAheadTokenIsNotHypen
    : {_input.LA(1) != '-'}?
    ;

mode TRIPLE_BACKTICK_INLINE_CODE;

TripleBackTickInlineCodeEnd
    :   BACKTICK BACKTICK BACKTICK              -> popMode
    ;

TripleBackTickInlineCode
    :   TripleBackTickInlineCodeChar+
    ;

fragment
TripleBackTickInlineCodeChar
    :   ~[`]
    |   [`] ~[`]
    |   [`] [`] ~[`]
    ;

mode DOUBLE_BACKTICK_INLINE_CODE;

DoubleBackTickInlineCodeEnd
    :   BACKTICK BACKTICK                       -> popMode
    ;

DoubleBackTickInlineCode
    :   DoubleBackTickInlineCodeChar+
    ;

fragment
DoubleBackTickInlineCodeChar
    :   ~[`]
    |   [`] ~[`]
    ;

mode SINGLE_BACKTICK_INLINE_CODE;

SingleBackTickInlineCodeEnd
    :   BACKTICK                                -> popMode
    ;

SingleBackTickInlineCode
    :   SingleBackTickInlineCodeChar+
    ;

fragment
SingleBackTickInlineCodeChar
    :   ~[`]
    ;

mode STRING_TEMPLATE;

StringTemplateLiteralEnd
    :   '`' { inStringTemplate = false; }          -> popMode
    ;

StringTemplateExpressionStart
    :   StringTemplateText? INTERPOLATION_START            -> pushMode(DEFAULT_MODE)
    ;

StringTemplateText
    :   StringTemplateValidCharSequence+ DOLLAR*
    |   DOLLAR+
    ;

fragment
DOLLAR
    :   '$'
    ;

fragment
StringTemplateValidCharSequence
    :   ~[`$\\]
    |   DOLLAR+ ~[`${\\]
    |   WS
    |   StringLiteralEscapedSequence
    ;

fragment
StringLiteralEscapedSequence
    :   DOLLAR* '\\' [\\'"btnfr`{]
    ;
