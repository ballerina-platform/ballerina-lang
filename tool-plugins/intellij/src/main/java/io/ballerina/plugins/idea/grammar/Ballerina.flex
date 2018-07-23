package io.ballerina.plugins.idea.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.*;

%%

%{
    private boolean inXmlMode = false;
    private boolean inXmlTagMode = false;
    private boolean inDoubleQuotedXmlStringMode = false;
    private boolean inSingleQuotedXmlStringMode = false;
    private boolean inXmlPiMode = false;
    private boolean inXmlCommentMode = false;

    private boolean inMarkdownMode = false;
    private boolean inStringTemplate = false;

    private boolean inDeprecatedTemplate = false;
    private boolean inDocTemplate = false;

    private boolean inSiddhi = false;
    private boolean inTableSqlQuery = false;
    private boolean inSiddhiInsertQuery = false;
    private boolean inSiddhiTimeScaleQuery = false;
    private boolean inSiddhiOutputRateLimit = false;

    public BallerinaLexer() {
        this((java.io.Reader)null);
    }

    private IElementType checkExpressionEnd() {
        if (inXmlMode) {
            inXmlMode = false;
            yybegin(XML_MODE);
            return EXPRESSION_END;
        } else if (inXmlTagMode) {
            inXmlTagMode = false;
            yybegin(XML_TAG_MODE);
            return EXPRESSION_END;
        } else if (inDoubleQuotedXmlStringMode) {
            inDoubleQuotedXmlStringMode = false;
            yybegin(DOUBLE_QUOTED_XML_STRING_MODE);
            return EXPRESSION_END;
        } else if (inSingleQuotedXmlStringMode) {
            inSingleQuotedXmlStringMode = false;
            yybegin(SINGLE_QUOTED_XML_STRING_MODE);
            return EXPRESSION_END;
        } else if (inXmlPiMode) {
            inXmlPiMode = false;
            yybegin(XML_PI_MODE);
            return EXPRESSION_END;
        } else if (inXmlCommentMode) {
            inXmlCommentMode = false;
            yybegin(XML_COMMENT_MODE);
            return EXPRESSION_END;
        } else if (inStringTemplate) {
            yybegin(STRING_TEMPLATE_MODE);
            return EXPRESSION_END;
        } else if (inDocTemplate) {
            yybegin(DOCUMENTATION_TEMPLATE_MODE);
            return DOCUMENTATION_TEMPLATE_ATTRIBUTE_END;
        } else if (inDeprecatedTemplate) {
            yybegin(DEPRECATED_TEMPLATE_MODE);
            return DOCUMENTATION_TEMPLATE_ATTRIBUTE_END;
        } else {
            yypushback(1);
            return RIGHT_BRACE;
        }
    }
%}

%public
%class BallerinaLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

DECIMAL_INTEGER_LITERAL = {DecimalNumeral}
HEX_INTEGER_LITERAL = {HexNumeral}
OCTAL_INTEGER_LITERAL = {OctalNumeral}
BINARY_INTEGER_LITERAL = {BinaryNumeral}

DecimalNumeral = 0 | {NonZeroDigit} {Digits}?
Digits = {Digit}+
Digit = 0 | {NonZeroDigit}
NonZeroDigit = [1-9]

HexNumeral = 0 [xX] {HexDigits}
HexDigits = {HexDigit}+
HexDigit = [0-9a-fA-F]

OctalNumeral = 0 {OctalDigits}
OctalDigits = {OctalDigit}+
OctalDigit = [0-7]

BinaryNumeral = 0 [bB] {BinaryDigits}
BinaryDigits = {BinaryDigit}+
BinaryDigit = [01]

// §3.10.2 Floating-Point Literals

FLOATING_POINT_LITERAL = {DecimalFloatingPointLiteral} | {HexadecimalFloatingPointLiteral}

DecimalFloatingPointLiteral = {Digits} "." ({Digits} {ExponentPart}? | {Digits}? {ExponentPart})
    | "." {Digits} {ExponentPart}?
    | {Digits} {ExponentPart}
    | {Digits}
ExponentPart = {ExponentIndicator} {SignedInteger}
ExponentIndicator = [eE]
SignedInteger = {Sign}? {Digits}
Sign = [+-]

HexadecimalFloatingPointLiteral = {HexSignificand} {BinaryExponent}
HexSignificand = {HexNumeral} "."? | '0' [xX] {HexDigits}? "." {HexDigits}
BinaryExponent = {BinaryExponentIndicator} {SignedInteger}
BinaryExponentIndicator = [pP]

// §3.10.3 Boolean Literals

BOOLEAN_LITERAL = "true" | "false"

// Note - Invalid escaped characters should be annotated at runtime.
// This is done becuase otherwise the string wont be identified correctly.
// Also the strings can either be enclosed in single or double quotes or no quotes at all.
ESCAPE_SEQUENCE = \\ [btnfr\"'\\]
STRING_CHARACTER =  [^\"] | {ESCAPE_SEQUENCE}
STRING_CHARACTERS = {STRING_CHARACTER}+
QUOTED_STRING_LITERAL = \" {STRING_CHARACTERS}? \"?

// Blob Literal

BASE_16_BLOB_LITERAL = "base16" {WHITE_SPACE}* {BACKTICK} {HEX_GROUP}* {WHITE_SPACE}* {BACKTICK}
HEX_GROUP = {WHITE_SPACE}* {HexDigit} {WHITE_SPACE}* {HexDigit}

BASE_64_BLOB_LITERAL = "base64" {WHITE_SPACE}* {BACKTICK} {BASE_64_GROUP}* {PADDED_BASE_64_GROUP}? {WHITE_SPACE}* {BACKTICK}
BASE_64_GROUP = {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {BASE_64_CHAR}
PADDED_BASE_64_GROUP = {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {PADDING_CHAR} | {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {BASE_64_CHAR} {WHITE_SPACE}* {PADDING_CHAR} {WHITE_SPACE}* {PADDING_CHAR}
BASE_64_CHAR = [a-zA-Z0-9+/]
PADDING_CHAR = =

NULL_LITERAL = null

LETTER = [a-zA-Z_] | [^\u0000-\u007F\uD800-\uDBFF] | [\uD800-\uDBFF] [\uDC00-\uDFFF]
DIGIT = [0-9]
LETTER_OR_DIGIT = [a-zA-Z0-9_] | [^\u0000-\u007F\uD800-\uDBFF] | [\uD800-\uDBFF] [\uDC00-\uDFFF]

IDENTIFIER = {LETTER} {LETTER_OR_DIGIT}* | {IdentifierLiteral}
IdentifierLiteral = \^ \" {IdentifierLiteralChar}+ \"
IdentifierLiteralChar = [^|\"\\\b\f\n\r\t] | {IdentifierLiteralEscapeSequence}
IdentifierLiteralEscapeSequence = \\ [|\"\\\/] | \\\\ [btnfr] | {UnicodeEscape}
UnicodeEscape = "\\u" {HexDigit} {HexDigit} {HexDigit} {HexDigit}

WHITE_SPACE=\s+

BACKTICK = "`"
LEFT_BRACE = "{"
RIGHT_BRACE = "}"

// Todo - Add inspection
LINE_COMMENT = "/" "/" [^\r\n]*

XML_LITERAL_START = xml[ \t\n\x0B\f\r]*`

STRING_TEMPLATE_LITERAL_START = string[ \t\n\x0B\f\r]*`
STRING_TEMPLATE_LITERAL_END = "`"

DOCUMENTATION = "documentation"
DEPRECATED = "deprecated"

DOCUMENTATION_LINE_START =  {HASH} {DocumentationSpace}?
PARAMETER_DOCUMENTATION_START = {HASH} DocumentationSpace? ADD DocumentationSpace* -> pushMode
(MARKDOWN_DOCUMENTATION_PARAM)

DOCUMENTATION_TEMPLATE_START = {DOCUMENTATION} {WHITE_SPACE}* {LEFT_BRACE}
DEPRECATED_TEMPLATE_START = {DEPRECATED} {WHITE_SPACE}* {LEFT_BRACE}

//Markdown documentation
HASH = "#"

// Todo - Need to add spaces between braces?
// Note - This is used in checkExpressionEnd() function.
DOCUMENTATION_TEMPLATE_ATTRIBUTE_END = {RIGHT_BRACE} {RIGHT_BRACE}

EXPRESSION_START = "{{"
EXPRESSION_END = "}}"

HEX_DIGITS = {HEX_DIGIT} ({HEX_DIGIT_OR_UNDERSCORE}* {HEX_DIGIT})?
HEX_DIGIT_OR_UNDERSCORE = {HEX_DIGIT} | "_"

// XML
XML_COMMENT_START = "<!--"
CDATA = "<![CDATA[" .*? "]]>"
DTD = "<!" ([^-].|.[^-]).*? ">"
ENTITY_REF = "&" {XML_QNAME} ";"
CHAR_REF = "&#" {DIGIT}+ ';' | "&#x" {HEX_DIGITS}+ ";"
XML_WS = " " | \t | \r? \n
XML_TAG_OPEN_SLASH = "</"
XML_TAG_SPECIAL_OPEN = "<?" ({XML_QNAME} {QNAME_SEPARATOR})? {XML_QNAME} {XML_WS} // Todo - Fix
XML_TAG_OPEN = "<"
XML_LITERAL_END = "`"
XML_TEMPLATE_TEXT = {XML_TEXT_SEQUENCE}? {EXPRESSION_START}
XML_TEXT_SEQUENCE = {XML_BRACES_SEQUENCE}? ({XML_TEXT_CHAR} {XML_BRACES_SEQUENCE}?)+ | {XML_BRACES_SEQUENCE} ({XML_TEXT_CHAR} {XML_BRACES_SEQUENCE}?)*
XML_TEXT_CHAR = [^<&`{}] | '\\' [`] | {XML_WS} | {XML_ESCAPED_SEQUENCE}
XML_ESCAPED_SEQUENCE =  \\\\ | \\\{\{ | \\}}
XML_BRACES_SEQUENCE = (\{})+ | (}\{) | (\{})* \{ | } (\{})*

// XML_TAG
XML_TAG_CLOSE = ">"
XML_TAG_SPECIAL_CLOSE = \?>
XML_TAG_SLASH_CLOSE = "/>"
SLASH = "/"
QNAME_SEPARATOR = :
EQUALS = "="
DOUBLE_QUOTE = "\""
SINGLE_QUOTE = "'"
XML_QNAME = {NAME_START_CHAR} {NAME_CHAR}*
XML_TAG_WS = [ \t\r\n]
XML_TAG_EXPRESSION_START = {EXPRESSION_START}
HEX_DIGIT = [0-9a-fA-F]
NAME_CHAR = {NAME_START_CHAR} | "-" | "_" | "." | {DIGIT} | \u00B7 | [\u0300-\u036F] | [\u203F-\u2040]
NAME_START_CHAR = [a-zA-Z] | [\u2070-\u218F] | [\u2C00-\u2FEF] | [\u3001-\uD7FF] | [\uF900-\uFDCF] | [\uFDF0-\uFFFD]

// DOUBLE_QUOTED_XML_STRING
DOUBLE_QUOTE_END = "\""
XML_DOUBLE_QUOTED_TEMPLATE_STRING = {XML_DOUBLE_QUOTED_STRING_SEQUENCE}? {EXPRESSION_START}
XML_DOUBLE_QUOTED_STRING_SEQUENCE = {XML_BRACES_SEQUENCE}? ({XMLDoubleQuotedStringChar} {XML_BRACES_SEQUENCE}?)+ | {XML_BRACES_SEQUENCE} ({XMLDoubleQuotedStringChar} {XML_BRACES_SEQUENCE}?)*
XMLDoubleQuotedStringChar = [^<\"{}\\] | {XML_ESCAPED_SEQUENCE}

// SINGLE_QUOTED_XML_STRING
SINGLE_QUOTE_END = "'"
XML_SINGLE_QUOTED_TEMPLATE_STRING = {XML_SINGLE_QUOTED_STRING_SEQUENCE}? {EXPRESSION_START}
XML_SINGLE_QUOTED_STRING_SEQUENCE = {XML_BRACES_SEQUENCE}? ({XMLSingleQuotedStringChar} {XML_BRACES_SEQUENCE}?)+ | {XML_BRACES_SEQUENCE} ({XMLSingleQuotedStringChar} {XML_BRACES_SEQUENCE}?)*
XMLSingleQuotedStringChar = [^<'{}\\] | {XML_ESCAPED_SEQUENCE}

// XML_PI
XML_PI_END = {XML_TAG_SPECIAL_CLOSE}
XML_PI_TEXT = {XML_PI_TEXT_FRAGMENT} {XML_PI_END}
XML_PI_TEMPLATE_TEXT = {XML_PI_TEXT_FRAGMENT} {EXPRESSION_START}
XML_PI_TEXT_FRAGMENT = {XML_PI_ALLOWED_SEQUENCE}? ({XML_PI_CHAR} {XML_PI_ALLOWED_SEQUENCE}?)*
XML_PI_CHAR = [^{}?>] | {XML_ESCAPED_SEQUENCE}
XML_PI_ALLOWED_SEQUENCE = {XML_BRACES_SEQUENCE} | {XML_PI_SPECIAL_SEQUENCE} | ({XML_BRACES_SEQUENCE} {XML_PI_SPECIAL_SEQUENCE})+ {XML_BRACES_SEQUENCE}? | ({XML_PI_SPECIAL_SEQUENCE} {XML_BRACES_SEQUENCE})+ {XML_PI_SPECIAL_SEQUENCE}?
XML_PI_SPECIAL_SEQUENCE = ">"+ | ">"* "?"+

// XML_COMMENT
XML_COMMENT_END = "-->"
XML_COMMENT_TEXT = {XML_COMMENT_TEXT_FRAGMENT} {XML_COMMENT_END}
XML_COMMENT_TEMPLATE_TEXT = {XML_COMMENT_TEXT_FRAGMENT} {EXPRESSION_START}
XML_COMMENT_TEXT_FRAGMENT = {XML_COMMENT_ALLOWED_SEQUENCE}? ({XML_COMMENT_CHAR} {XML_COMMENT_ALLOWED_SEQUENCE}?)*
XML_COMMENT_CHAR = [^{}>\\-] | {XML_ESCAPED_SEQUENCE}
XML_COMMENT_ALLOWED_SEQUENCE = {XML_BRACES_SEQUENCE} | {XML_COMMENT_SPECIAL_SEQUENCE} | ({XML_BRACES_SEQUENCE} {XML_COMMENT_SPECIAL_SEQUENCE})+ {XML_BRACES_SEQUENCE}? | ({XML_COMMENT_SPECIAL_SEQUENCE} {XML_BRACES_SEQUENCE})+ {XML_COMMENT_SPECIAL_SEQUENCE}?
XML_COMMENT_SPECIAL_SEQUENCE = ">"+ | (">"* "-" ">"+)+ | "-"? ">"* "-"+

// MARKDOWN_DOCUMENTATION


// DOCUMENTATION_TEMPLATE
DOCUMENTATION_TEMPLATE_END = {RIGHT_BRACE}
DOCUMENTATION_TEMPLATE_ATTRIBUTE_START = {ATTRIBUTE_PREFIX} {EXPRESSION_START}
SB_DOC_INLINE_CODE_START = {ATTRIBUTE_PREFIX}? {DOC_BACK_TICK}
DB_DOC_INLINE_CODE_START = {ATTRIBUTE_PREFIX}? {DOC_BACK_TICK} {DOC_BACK_TICK}
TB_DOC_INLINE_CODE_START = {ATTRIBUTE_PREFIX}? {DOC_BACK_TICK} {DOC_BACK_TICK} {DOC_BACK_TICK}
DOCUMENTATION_TEMPLATE_TEXT = {DOCUMENTATION_VALID_CHAR_SEQUENCE}? ({DOCUMENTATION_TEMPLATE_STRING_CHAR} {DOCUMENTATION_VALID_CHAR_SEQUENCE}?)+ | {DOCUMENTATION_VALID_CHAR_SEQUENCE} ({DOCUMENTATION_TEMPLATE_STRING_CHAR} {DOCUMENTATION_VALID_CHAR_SEQUENCE}?)*
DOCUMENTATION_TEMPLATE_STRING_CHAR = [^`{}\\FPTRVE] | \\ [{}`] | {WHITE_SPACE} | {DOCUMENTATION_ESCAPED_SEQUENCE}
ATTRIBUTE_PREFIX = [FPTRVE]
DOC_BACK_TICK = "`"
DOCUMENTATION_ESCAPED_SEQUENCE = \\\\
DOCUMENTATION_VALID_CHAR_SEQUENCE = [FPTRVE] [^`{}\\] | [FPTRVE] \\ [{}`] | [FPTRVE] \\ [^{}`] | \\ [^\\]

// TRIPLE_BACKTICK_INLINE_CODE
TRIPLE_BACK_TICK_INLINE_CODE_END = {BACKTICK} {BACKTICK} {BACKTICK}
TRIPLE_BACK_TICK_INLINE_CODE = {TRIPLE_BACK_TICK_INLINE_CODE_CHAR}+
TRIPLE_BACK_TICK_INLINE_CODE_CHAR = [^`] | [`] [^`] | [`] [`] [^`]

// DOUBLE_BACKTICK_INLINE_CODE
DOUBLE_BACK_TICK_INLINE_CODE_END = {BACKTICK} {BACKTICK}
DOUBLE_BACK_TICK_INLINE_CODE = {DOUBLE_BACK_TICK_INLINE_CODE_CHAR}+
DOUBLE_BACK_TICK_INLINE_CODE_CHAR = [^`] | [`] [^`]

// SINGLE_BACKTICK_INLINE_CODE
SINGLE_BACK_TICK_INLINE_CODE_END = {BACKTICK}
SINGLE_BACK_TICK_INLINE_CODE = {SINGLE_BACK_TICK_INLINE_CODE_CHAR}+
SINGLE_BACK_TICK_INLINE_CODE_CHAR = [^`]

// DEPRECATED_TEMPLATE
DEPRECATED_TEMPLATE_END = {RIGHT_BRACE}
SB_DEPRECATED_INLINE_CODE_START = {DEPRECATED_BACK_TICK}
DB_DEPRECATED_INLINE_CODE_START = {DEPRECATED_BACK_TICK} {DEPRECATED_BACK_TICK}
TB_DEPRECATED_INLINE_CODE_START = {DEPRECATED_BACK_TICK} {DEPRECATED_BACK_TICK} {DEPRECATED_BACK_TICK}
DEPRECATED_TEMPLATE_TEXT = {DEPRECATED_VALID_CHAR_SEQUENCE}? ({DEPRECATED_TEMPLATE_STRING_CHAR} {DEPRECATED_VALID_CHAR_SEQUENCE}?)+ | {DEPRECATED_VALID_CHAR_SEQUENCE} ({DEPRECATED_TEMPLATE_STRING_CHAR} {DEPRECATED_VALID_CHAR_SEQUENCE}?)*
DEPRECATED_TEMPLATE_STRING_CHAR = [^`{}\\] | '\\' [{}`] | {WHITE_SPACE} | {DEPRECATED_ESCAPED_SEQUENCE}
DEPRECATED_BACK_TICK = "`"
DEPRECATED_ESCAPED_SEQUENCE = '\\\\'
DEPRECATED_VALID_CHAR_SEQUENCE = '\\' ~'\\'

// STRING_TEMPLATE
STRING_LITERAL_ESCAPED_SEQUENCE = \\\\ | \\\{\{
STRING_TEMPLATE_VALID_CHAR_SEQUENCE = "{" | "\\" [^\\]
STRING_TEMPLATE_STRING_CHAR = [^\`\{\\] | \\ [\`\{] | {WHITE_SPACE} | {STRING_LITERAL_ESCAPED_SEQUENCE}
STRING_TEMPLATE_EXPRESSION_START = {STRING_TEMPLATE_TEXT}? {EXPRESSION_START}
STRING_TEMPLATE_TEXT = {STRING_TEMPLATE_VALID_CHAR_SEQUENCE}? ({STRING_TEMPLATE_STRING_CHAR} {STRING_TEMPLATE_VALID_CHAR_SEQUENCE}?)+
                       | {STRING_TEMPLATE_VALID_CHAR_SEQUENCE} ({STRING_TEMPLATE_STRING_CHAR} {STRING_TEMPLATE_VALID_CHAR_SEQUENCE}?)*

%state XML_MODE
%state XML_TAG_MODE
%state DOUBLE_QUOTED_XML_STRING_MODE
%state SINGLE_QUOTED_XML_STRING_MODE
%state XML_PI_MODE
%state XML_COMMENT_MODE

%state MARKDOWN_DOCUMENTATION_MODE
%state SINGLE_BACKTICKED_DOCUMENTATION
%state DOUBLE_BACKTICKED_DOCUMENTATION
%state TRIPLE_BACKTICKED_DOCUMENTATION

%state DOCUMENTATION_TEMPLATE_MODE
%state TRIPLE_BACKTICK_INLINE_CODE_MODE
%state DOUBLE_BACKTICK_INLINE_CODE_MODE
%state TRIPLE_BACKTICK_INLINE_CODE_MODE
%state DEPRECATED_TEMPLATE_MODE

%state STRING_TEMPLATE_MODE

%%
<YYINITIAL> {
    "abort"                                     { return ABORT; }
    "all"                                       { return ALL; }
    "annotation"                                { return ANNOTATION; }
    "any"                                       { return ANY; }
    "as"                                        { return AS; }
    "ascending"                                 { return ASCENDING; }
    "await"                                     { return AWAIT; }

    "bind"                                      { return BIND; }
    "boolean"                                   { return BOOLEAN; }
    "break"                                     { return BREAK; }
    "but"                                       { return BUT; }
    "byte"                                      { return BYTE; }

    "catch"                                     { return CATCH; }
    "check"                                     { return CHECK; }
    "compensation"                              { return COMPENSATION; }
    "compensate"                                { return COMPENSATE; }
    "continue"                                  { return CONTINUE; }

    "documentation"                             { return DOCUMENTATION; }
    "done"                                      { return DONE; }
    "deprecated"                                { return DEPRECATED; }
    "descending"                                { return DESCENDING; }

    "else"                                      { return ELSE; }
    "endpoint"                                  { return ENDPOINT; }

    "finally"                                   { return FINALLY; }
    "float"                                     { return FLOAT; }
    "foreach"                                   { return FOREACH; }
    "fork"                                      { return FORK; }
    "function"                                  { return FUNCTION; }
    "future"                                    { return FUTURE; }

    "if"                                        { return IF; }
    "import"                                    { return IMPORT; }
    "in"                                        { return IN; }
    "int"                                       { return INT; }

    "join"                                      { return JOIN; }
    "json"                                      { return JSON; }

    "lengthof"                                  { return LENGTHOF; }
    "limit"                                     { return LIMIT; }
    "lock"                                      { return LOCK; }

    "map"                                       { return MAP; }
    "match"                                     { return MATCH; }

    "native"                                    { return NATIVE; }
    "new"                                       { return NEW; }

    "object"                                    { return OBJECT; }
    "onabort"                                   { return ONABORT; }
    "oncommit"                                  { return ONCOMMIT; }
    "onretry"                                   { return ONRETRY; }

    "parameter"                                 { return TYPE_PARAMETER; }
    "private"                                   { return PRIVATE; }
    "primarykey"                                { return PRIMARYKEY; }
    "public"                                    { return PUBLIC; }

    "record"                                    { return RECORD; }
    "resource"                                  { return RESOURCE; }
    "retry"                                     { return RETRY; }
    "retries"                                   { return RETRIES; }
    "return"                                    { return RETURN; }
    "returns"                                   { return RETURNS; }

    "service"                                   { return SERVICE; }
    "scope"                                     { return SCOPE; }
    "some"                                      { return SOME; }
    "start"                                     { return START; }
    "stream"                                    { return STREAM; }
    "string"                                    { return STRING; }

    "table"                                     { return TABLE; }
    "timeout"                                   { return TIMEOUT; }
    "transaction"                               { return TRANSACTION; }
    "try"                                       { return TRY; }
    "type"                                      { return TYPE; }
    "typedesc"                                  { return TYPEDESC; }
    "throw"                                     { return THROW; }

    "untaint"                                   { return UNTAINT; }

    "while"                                     { return WHILE; }
    "with"                                      { return WITH; }
    "worker"                                    { return WORKER; }

    "var"                                       { return VAR; }
    "version"                                   { return VERSION; }

    "xml"                                       { return XML; }
    "xmlns"                                     { return XMLNS; }

    ";"                                         { return SEMICOLON; }
    ":"                                         { return COLON; }
    "::"                                        { return DOUBLE_COLON; }
    "."                                         { return DOT; }
    ","                                         { return COMMA; }
    "{"                                         { return LEFT_BRACE; }
    "}"                                         { return RIGHT_BRACE; }
    "("                                         { return LEFT_PARENTHESIS; }
    ")"                                         { return RIGHT_PARENTHESIS; }
    "["                                         { return LEFT_BRACKET; }
    "]"                                         { return RIGHT_BRACKET; }
    "?"                                         { return QUESTION_MARK; }

    "="                                         { return ASSIGN; }
    "+"                                         { return ADD; }
    "-"                                         { return SUB; }
    "*"                                         { return MUL; }
    "/"                                         { return DIV; }
    "%"                                         { return MOD; }

    "!"                                         { return NOT; }
    "=="                                        { return EQUAL; }
    "!="                                        { return NOT_EQUAL; }
    ">"                                         { return GT; }
    "<"                                         { return LT; }
    ">="                                        { return GT_EQUAL; }
    "<="                                        { return LT_EQUAL; }
    "&&"                                        { return AND; }
    "||"                                        { return OR; }

    "&"                                         { return BITAND; }
    "^"                                         { return BITXOR; }

    "->"                                        { return RARROW; }
    "<-"                                        { return LARROW; }
    "@"                                         { return AT; }
    "`"                                         { return BACKTICK; }
    ".."                                        { return RANGE; }
    "..."                                       { return ELLIPSIS; }
    "|"                                         { return PIPE; }
    "=>"                                        { return EQUAL_GT; }
    "?:"                                        { return ELVIS; }

    "+="                                        { return COMPOUND_ADD; }
    "-="                                        { return COMPOUND_SUB; }
    "*="                                        { return COMPOUND_MUL; }
    "/="                                        { return COMPOUND_DIV; }

    "++"                                        { return INCREMENT; }
    "--"                                        { return DECREMENT; }

    "..<"                                       { return HALF_OPEN_RANGE; }

    "from"                                      { inSiddhi = false; inTableSqlQuery = true; inSiddhiInsertQuery = true; inSiddhiOutputRateLimit = true; return FROM; }
    "on"                                        { return ON; }
    "select"                                    { if(inTableSqlQuery) { inTableSqlQuery = false; return SELECT; } return IDENTIFIER; }
    "group"                                     { return GROUP; }
    "by"                                        { return BY; }
    "having"                                    { return HAVING; }
    "order"                                     { return ORDER; }
    "where"                                     { return WHERE; }
    "followed"                                  { return FOLLOWED; }
    "set"                                       { return SET; }
    "for"                                       { inSiddhiTimeScaleQuery = true; return FOR; }
    "window"                                    { return WINDOW; }
    "events"                                    { if(inSiddhiInsertQuery) { inSiddhiInsertQuery = false; return EVENTS; } return IDENTIFIER; }
    "every"                                     { return EVERY; }
    "within"                                    { inSiddhiTimeScaleQuery = true; return WITHIN; }
    "last"                                      { if(inSiddhiOutputRateLimit) { inSiddhiOutputRateLimit = false; return LAST; } return IDENTIFIER; }
    "first"                                     { if(inSiddhiOutputRateLimit) { inSiddhiOutputRateLimit = false; return FIRST; } return IDENTIFIER; }
    "snapshot"                                  { return SNAPSHOT; }
    "output"                                    { if(inSiddhiOutputRateLimit) { inSiddhiTimeScaleQuery = true; return OUTPUT; } return IDENTIFIER; }
    "inner"                                     { return INNER; }
    "outer"                                     { return OUTER; }
    "right"                                     { return RIGHT; }
    "left"                                      { return LEFT; }
    "full"                                      { return FULL; }
    "unidirectional"                            { return UNIDIRECTIONAL; }
    "second"                                    { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return SECOND; } return IDENTIFIER; }
    "minute"                                    { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return MINUTE; } return IDENTIFIER; }
    "hour"                                      { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return HOUR; } return IDENTIFIER; }
    "day"                                       { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return DAY; } return IDENTIFIER; }
    "month"                                     { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return MONTH; } return IDENTIFIER; }
    "year"                                      { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return YEAR; } return IDENTIFIER; }
    "seconds"                                   { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return SECONDS; } return IDENTIFIER; }
    "minutes"                                   { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return MINUTES; } return IDENTIFIER; }
    "hours"                                     { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return HOURS; } return IDENTIFIER; }
    "days"                                      { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return DAYS; } return IDENTIFIER; }
    "months"                                    { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return MONTHS; } return IDENTIFIER; }
    "years"                                     { if(inSiddhiTimeScaleQuery) { inSiddhiTimeScaleQuery = false; return YEARS; } return IDENTIFIER; }
    "forever"                                   { return FOREVER; }

    {WHITE_SPACE}                               { return WHITE_SPACE; }

    {NULL_LITERAL}                              { return NULL_LITERAL; }
    {BOOLEAN_LITERAL}                           { return BOOLEAN_LITERAL; }
    {DECIMAL_INTEGER_LITERAL}                   { return DECIMAL_INTEGER_LITERAL; }
    {HEX_INTEGER_LITERAL}                       { return HEX_INTEGER_LITERAL; }
    {OCTAL_INTEGER_LITERAL}                     { return OCTAL_INTEGER_LITERAL; }
    {BINARY_INTEGER_LITERAL}                    { return BINARY_INTEGER_LITERAL; }
    {FLOATING_POINT_LITERAL}                    { return FLOATING_POINT_LITERAL; }
    {QUOTED_STRING_LITERAL}                     { return QUOTED_STRING_LITERAL; }

    {BASE_16_BLOB_LITERAL}                      { return BASE_16_BLOB_LITERAL; }
    {BASE_64_BLOB_LITERAL}                      { return BASE_64_BLOB_LITERAL; }

    {IDENTIFIER}                                { return IDENTIFIER; }
    {LINE_COMMENT}                              { return LINE_COMMENT; }

    {XML_LITERAL_START}                         { yybegin(XML_MODE); return XML_LITERAL_START; }
    {STRING_TEMPLATE_LITERAL_START}             { inStringTemplate = true; yybegin(STRING_TEMPLATE_MODE); return STRING_TEMPLATE_LITERAL_START; }
    {EXPRESSION_END}                            { return checkExpressionEnd(); }







    {DOCUMENTATION_TEMPLATE_START}              { inDocTemplate = true; yybegin(DOCUMENTATION_TEMPLATE_MODE); return DOCUMENTATION_TEMPLATE_START; }
    {DEPRECATED_TEMPLATE_START}                 { inDeprecatedTemplate = true; yybegin(DEPRECATED_TEMPLATE_MODE); return DEPRECATED_TEMPLATE_START; }
    .                                           { return BAD_CHARACTER; }
}


<XML_MODE>{
    {XML_COMMENT_START}                         { yybegin(XML_COMMENT_MODE); return XML_COMMENT_START; }
    {CDATA}                                     { return CDATA; }
    {DTD}                                       { } // Todo - Need to return a value?
    {ENTITY_REF}                                { /*return ENTITY_REF;*/ } // Do not need
    {CHAR_REF}                                  { /*return CHAR_REF;*/ } // Do not need
    {XML_TAG_SPECIAL_OPEN}                      { yybegin(XML_PI_MODE); return XML_TAG_SPECIAL_OPEN; }
    {XML_TAG_OPEN_SLASH}                        { yybegin(XML_TAG_MODE); return XML_TAG_OPEN_SLASH; }
    {XML_TAG_OPEN}                              { yybegin(XML_TAG_MODE); return XML_TAG_OPEN; }
    {XML_LITERAL_END}                           { yybegin(YYINITIAL); return XML_LITERAL_END; }
    {XML_TEMPLATE_TEXT}                         { inXmlMode = true; yybegin(YYINITIAL); return XML_TEMPLATE_TEXT; }
    {XML_TEXT_SEQUENCE}                         { return XML_TEXT_SEQUENCE; }
    .                                           { return BAD_CHARACTER; }
}

<XML_TAG_MODE>{
    {XML_TAG_CLOSE}                             { yybegin(XML_MODE); return XML_TAG_CLOSE; }
    {XML_TAG_SPECIAL_CLOSE}                     { /*yybegin(XML_MODE); return XML_TAG_SPECIAL_CLOSE;*/ } // Do not need
    {XML_TAG_SLASH_CLOSE}                       { yybegin(XML_MODE); return XML_TAG_SLASH_CLOSE; }
    {SLASH}                                     { /*return SLASH;*/ } // Do not need
    {QNAME_SEPARATOR}                           { return QNAME_SEPARATOR; }
    {EQUALS}                                    { return EQUALS; }
    {DOUBLE_QUOTE}                              { yybegin(DOUBLE_QUOTED_XML_STRING_MODE); return DOUBLE_QUOTE; }
    {SINGLE_QUOTE}                              { yybegin(SINGLE_QUOTED_XML_STRING_MODE); return SINGLE_QUOTE; }
    {XML_QNAME}                                 { return XML_QNAME; }
    {XML_TAG_WS}                                { } // Todo - Need to return a value?
    {XML_TAG_EXPRESSION_START}                  { inXmlTagMode = true; yybegin(YYINITIAL); return XML_TAG_EXPRESSION_START; }
    .                                           { return BAD_CHARACTER; }
}

<DOUBLE_QUOTED_XML_STRING_MODE>{
    {DOUBLE_QUOTE_END}                          { yybegin(XML_TAG_MODE); return DOUBLE_QUOTE_END; }
    {XML_DOUBLE_QUOTED_TEMPLATE_STRING}         { inDoubleQuotedXmlStringMode = true; yybegin(YYINITIAL); return XML_DOUBLE_QUOTED_TEMPLATE_STRING; }
    {XML_DOUBLE_QUOTED_STRING_SEQUENCE}         { return XML_DOUBLE_QUOTED_STRING_SEQUENCE; }
    .                                           { return BAD_CHARACTER; }
}

<SINGLE_QUOTED_XML_STRING_MODE>{
    {SINGLE_QUOTE_END}                          { yybegin(XML_TAG_MODE); return SINGLE_QUOTE_END; }
    {XML_SINGLE_QUOTED_TEMPLATE_STRING}         { inSingleQuotedXmlStringMode = true; yybegin(YYINITIAL); return XML_SINGLE_QUOTED_TEMPLATE_STRING; }
    {XML_SINGLE_QUOTED_STRING_SEQUENCE}         { return XML_SINGLE_QUOTED_STRING_SEQUENCE; }
    .                                           { return BAD_CHARACTER; }
}

<XML_PI_MODE>{
    {XML_PI_TEMPLATE_TEXT}                      { inXmlPiMode = true; yybegin(YYINITIAL); return XML_PI_TEMPLATE_TEXT; }
    {XML_PI_TEXT}                               { yybegin(XML_MODE); return XML_PI_TEXT; }
    .                                           { return BAD_CHARACTER; }
}

<XML_COMMENT_MODE>{
    {XML_COMMENT_TEXT}                          { yybegin(XML_MODE); return XML_COMMENT_TEXT; }
    {XML_COMMENT_TEMPLATE_TEXT}                 { inXmlCommentMode = true; yybegin(YYINITIAL); return XML_COMMENT_TEMPLATE_TEXT; }
    .                                           { return BAD_CHARACTER; }
}

<STRING_TEMPLATE_MODE>{
    {STRING_TEMPLATE_LITERAL_END}               { inStringTemplate = false; yybegin(YYINITIAL); return STRING_TEMPLATE_LITERAL_END; }
    {STRING_TEMPLATE_EXPRESSION_START}          { yybegin(YYINITIAL); return STRING_TEMPLATE_EXPRESSION_START; }
    {STRING_TEMPLATE_TEXT}                      { return STRING_TEMPLATE_TEXT; }
    .                                           { inStringTemplate = false; yybegin(YYINITIAL); return BAD_CHARACTER; }
}

<MARKDOWN_DOCUMENTATION_MODE>{
    {DOCUMENTATION_MARKDOWN_END} {inMarkdownMode = false; yybegin(YYINITIAL); return DOCUMENTATION_MARKDOWN_END;}
    {SINGLE_BACKTICK_DOCUMENTATION_START} { }
    {DOUBLE_BACKTICK__DOCUMENTATION_START} { }
    {TRIPLE_BACKTICK__DOCUMENTATION_START} { }

}



<DOCUMENTATION_TEMPLATE_MODE>{
    {DOCUMENTATION_TEMPLATE_END}                { inDocTemplate = false; yybegin(YYINITIAL); return DOCUMENTATION_TEMPLATE_END; }
    {SB_DOC_INLINE_CODE_START}                  { yybegin(SINGLE_BACKTICK_INLINE_CODE_MODE); return SB_DOC_INLINE_CODE_START; }
    {DB_DOC_INLINE_CODE_START}                  { yybegin(DOUBLE_BACKTICK_INLINE_CODE_MODE); return DB_DOC_INLINE_CODE_START; }
    {TB_DOC_INLINE_CODE_START}                  { yybegin(TRIPLE_BACKTICK_INLINE_CODE_MODE); return TB_DOC_INLINE_CODE_START; }
    {DOCUMENTATION_TEMPLATE_ATTRIBUTE_START}    { yybegin(YYINITIAL); return DOCUMENTATION_TEMPLATE_ATTRIBUTE_START; }
    {DOCUMENTATION_TEMPLATE_TEXT}               { return DOCUMENTATION_TEMPLATE_TEXT; }
    .                                           { yybegin(YYINITIAL); return BAD_CHARACTER; }
}

<SINGLE_BACKTICK_INLINE_CODE_MODE>{
    {SINGLE_BACK_TICK_INLINE_CODE_END}          { if(inDocTemplate) { yybegin(DOCUMENTATION_TEMPLATE_MODE); } else if(inDeprecatedTemplate) { yybegin(DEPRECATED_TEMPLATE_MODE); } return SINGLE_BACK_TICK_INLINE_CODE_END; }
    {SINGLE_BACK_TICK_INLINE_CODE}              { return SINGLE_BACK_TICK_INLINE_CODE; }
     .                                          { if(inDocTemplate) { yybegin(DOCUMENTATION_TEMPLATE_MODE); } else if(inDeprecatedTemplate) { yybegin(DEPRECATED_TEMPLATE_MODE); } return BAD_CHARACTER; }
}

<DOUBLE_BACKTICK_INLINE_CODE_MODE>{
    {DOUBLE_BACK_TICK_INLINE_CODE_END}          { if(inDocTemplate) { yybegin(DOCUMENTATION_TEMPLATE_MODE); } else if(inDeprecatedTemplate) { yybegin(DEPRECATED_TEMPLATE_MODE); } return DOUBLE_BACK_TICK_INLINE_CODE_END; }
    {DOUBLE_BACK_TICK_INLINE_CODE}              { return DOUBLE_BACK_TICK_INLINE_CODE; }
     .                                          { if(inDocTemplate) { yybegin(DOCUMENTATION_TEMPLATE_MODE); } else if(inDeprecatedTemplate) { yybegin(DEPRECATED_TEMPLATE_MODE); } return BAD_CHARACTER; }
}

<TRIPLE_BACKTICK_INLINE_CODE_MODE>{
    {TRIPLE_BACK_TICK_INLINE_CODE_END}          { if(inDocTemplate) { yybegin(DOCUMENTATION_TEMPLATE_MODE); } else if(inDeprecatedTemplate) { yybegin(DEPRECATED_TEMPLATE_MODE); } return TRIPLE_BACK_TICK_INLINE_CODE_END; }
    {TRIPLE_BACK_TICK_INLINE_CODE}              { return TRIPLE_BACK_TICK_INLINE_CODE; }
     .                                          { if(inDocTemplate) { yybegin(DOCUMENTATION_TEMPLATE_MODE); } else if(inDeprecatedTemplate) { yybegin(DEPRECATED_TEMPLATE_MODE); } return BAD_CHARACTER; }
}

<DEPRECATED_TEMPLATE_MODE>{
    {DEPRECATED_TEMPLATE_END}                   { inDeprecatedTemplate = false; yybegin(YYINITIAL); return DEPRECATED_TEMPLATE_END; }
    {SB_DEPRECATED_INLINE_CODE_START}           { yybegin(SINGLE_BACKTICK_INLINE_CODE_MODE); return SB_DEPRECATED_INLINE_CODE_START; }
    {DB_DEPRECATED_INLINE_CODE_START}           { yybegin(DOUBLE_BACKTICK_INLINE_CODE_MODE); return DB_DEPRECATED_INLINE_CODE_START; }
    {TB_DEPRECATED_INLINE_CODE_START}           { yybegin(TRIPLE_BACKTICK_INLINE_CODE_MODE); return TB_DEPRECATED_INLINE_CODE_START; }
    {DEPRECATED_TEMPLATE_TEXT}                  { return DEPRECATED_TEMPLATE_TEXT; }
    .                                           { yybegin(YYINITIAL); return BAD_CHARACTER; }
}

[^]                                             { return BAD_CHARACTER; }
