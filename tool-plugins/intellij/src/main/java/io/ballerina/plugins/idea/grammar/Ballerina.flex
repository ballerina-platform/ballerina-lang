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

    private boolean inStringTemplate = false;

    private boolean inDeprecatedTemplate = false;

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

DecimalNumeral = 0 | {NonZeroDigit} {Digits}?
Digits = {Digit}+
Digit = 0 | {NonZeroDigit}
NonZeroDigit = [1-9]

HexNumeral = 0 [xX] {HexDigits}

DottedHexNumber = {HexDigits} "." {HexDigits} | "." {HexDigits}

DottedDecimalNumber = {DecimalNumeral} "." {Digits} | "." {Digits}

HexDigits = {HexDigit}+
HexDigit = [0-9a-fA-F]

HexadecimalFloatingPointLiteral =  {HexIndicator} {HexFloatingPointNumber}
HexIndicator = 0 [xX]

DecimalFloatingPointNumber = {DecimalNumeral} {ExponentPart} | {DottedDecimalNumber} {ExponentPart}?
ExponentPart = {ExponentIndicator} {SignedInteger}
ExponentIndicator = [eE]
SignedInteger = {Sign}? {Digits}
Sign = [+-]


HexFloatingPointNumber = {HexDigits} {BinaryExponent} | {DottedHexNumber} {BinaryExponent}?
BinaryExponent = {BinaryExponentIndicator} {SignedInteger}
BinaryExponentIndicator = [pP]

// §3.10.3 Boolean Literals

BOOLEAN_LITERAL = "true" | "false"

// Note - Invalid escaped characters should be annotated at runtime.
// This is done becuase otherwise the string wont be identified correctly.
// Also the strings can either be enclosed in single or double quotes or no quotes at all.
ESCAPE_SEQUENCE = \\ [btnfr\"'\\] | {UnicodeEscape}
STRING_CHARACTER =  [^\\\"] | {ESCAPE_SEQUENCE}
STRING_CHARACTERS = {STRING_CHARACTER}+
QUOTED_STRING_LITERAL = \" {STRING_CHARACTERS}? \"

SYMBOLIC_STRING_LITERAL =  \' {UNDELIMETERED_INITIAL_CHAR} {UNDELIMETERED_FOLLOWING_CHAR}*

UNDELIMETERED_INITIAL_CHAR = [a-zA-Z_]
    // Negates ASCII characters
    // Negates unicode whitespace characters : 0x200E, 0x200F, 0x2028 and 0x2029
    // Negates unicode characters with property Pattern_Syntax=True (http://unicode.org/reports/tr31/tr31-2.html#Pattern_Syntax)
    // Negates unicode characters of category "Private Use" ranging from: 0xE000 .. 0xF8FF | 0xF0000 .. 0xFFFFD | 0x100000 .. 0x10FFFD
    | [^\u0000-\u007F\uE000-\uF8FF\u200E\u200F\u2028\u2029\u00A1-\u00A7\u00A9\u00AB-\u00AC\u00AE\u00B0-\u00B1\u00B6-\u00B7\u00BB\u00BF\u00D7\u00F7\u2010-\u2027\u2030-\u205E\u2190-\u2BFF\u3001-\u3003\u3008-\u3020\u3030\uFD3E-\uFD3F\uFE45-\uFE46\uDB80-\uDBBF\uDBC0-\uDBFF\uDC00-\uDFFF]

UNDELIMETERED_FOLLOWING_CHAR = {UNDELIMETERED_INITIAL_CHAR} | {DIGIT}

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


DEPRECATED = "deprecated"
DEPRECATED_TEMPLATE_START = {DEPRECATED} {WHITE_SPACE}* {LEFT_BRACE}

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
MARKDOWN_DOCUMENTATION_LINE_START =  {HASH} {DOCUMENTATION_SPACE}?
PARAMETER_DOCUMENTATION_START = {HASH} {DOCUMENTATION_SPACE}? {ADD} {DOCUMENTATION_SPACE}*
RETURN_PARAMETER_DOCUMENTATION_START = {HASH} {DOCUMENTATION_SPACE}? {ADD} {DOCUMENTATION_SPACE}* {RETURN} {DOCUMENTATION_SPACE}* {SUB} {DOCUMENTATION_SPACE}*
DOCUMENTATION_SPACE = [ ]

// MARKDOWN_DOCUMENTATION_MODE
MARKDOWN_DOCUMENTATION_TEXT = ({DOCUMENTATION_TEXT_CHARACTER} | {DOCUMENTATION_ESCAPED_CHARACTERS})+
DOCUMENTATION_TEXT_CHARACTER =  [^`\n] | '\\' {BACKTICK}
DOCUMENTATION_ESCAPED_CHARACTERS = {DOCUMENTATION_SPACE}
MARKDOWN_DOCUMENTATION_LINE_END = [\n]
HASH = "#"
ADD = "+"
SUB = "-"
RETURN = "return"

// SINGLE_BACKTICKED_MARKDOWN
SINGLE_BACKTICK_CONTENT = (([^`\n] | '\\' {BACKTICK})* [\n])? ({MARKDOWN_DOCUMENTATION_LINE_START} ([^`\n] | '\\' {BACKTICK})* [\n]?)+ | ([^`\n] | '\\' {BACKTICK})+
SINGLE_BACKTICK_MARKDOWN_START = {BACKTICK}
SINGLE_BACKTICK_MARKDOWN_END =  {BACKTICK}

// DOUBLE_BACKTICKED_MARKDOWN
DOUBLE_BACKTICK_CONTENT = (([^`\n] | {BACKTICK} [^`])* [\n])? ({MARKDOWN_DOCUMENTATION_LINE_START} ([^`\n] |
{BACKTICK} [^`])* [\n]?)+ | ([^`\n] | {BACKTICK} [^`])+

DOUBLE_BACKTICK_MARKDOWN_START = {BACKTICK} {BACKTICK}
DOUBLE_BACKTICK_MARKDOWN_END = {BACKTICK} {BACKTICK}

// TRIPLE_BACKTICKED_MARKDOWN
TRIPLE_BACKTICK_CONTENT = (([^`\n] | {BACKTICK} [^`] | {BACKTICK} {BACKTICK} [^`])* [\n])? ({MARKDOWN_DOCUMENTATION_LINE_START} ([^`\n] | {BACKTICK} [^`] | {BACKTICK} {BACKTICK} [^`])* [\n]?)+
                          | ([^`\n] | {BACKTICK} [^`] | {BACKTICK} {BACKTICK} [^`])+
TRIPLE_BACKTICK_MARKDOWN_START = {BACKTICK} {BACKTICK} {BACKTICK}
TRIPLE_BACKTICK_MARKDOWN_END = {BACKTICK} {BACKTICK} {BACKTICK}

// MARKDOWN_PARAMETER_DOCUMENTATION
PARAMETER_NAME = {IDENTIFIER}
DESCRIPTION_SEPARATOR = {DOCUMENTATION_SPACE}* {SUB} {DOCUMENTATION_SPACE}*
PARAMETER_DOCUMENTATION_END = [\n]

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
%state MARKDOWN_PARAMETER_DOCUMENTATION_MODE
%state SINGLE_BACKTICKED_MARKDOWN_MODE
%state DOUBLE_BACKTICKED_MARKDOWN_MODE
%state TRIPLE_BACKTICKED_MARKDOWN_MODE

%state SINGLE_BACKTICK_INLINE_CODE_MODE
%state DOUBLE_BACKTICK_INLINE_CODE_MODE
%state TRIPLE_BACKTICK_INLINE_CODE_MODE
%state DEPRECATED_TEMPLATE_MODE

%state STRING_TEMPLATE_MODE

%%
<YYINITIAL> {
    "__init"                                    { return OBJECT_INIT; }

    "abort"                                     { return ABORT; }
    "aborted"                                   { return ABORTED; }
    "abstract"                                  { return ABSTRACT; }
    "all"                                       { return ALL; }
    "annotation"                                { return ANNOTATION; }
    "any"                                       { return ANY; }
    "anydata"                                   { return ANYDATA; }
    "as"                                        { return AS; }
    "ascending"                                 { return ASCENDING; }

    "boolean"                                   { return BOOLEAN; }
    "break"                                     { return BREAK; }
    "byte"                                      { return BYTE; }

    "catch"                                     { return CATCH; }
    "channel"                                   { return CHANNEL; }
    "check"                                     { return CHECK; }
    "client"                                    { return CLIENT; }
    "committed"                                 { return COMMITTED; }
    "const"                                     { return CONST; }
    "continue"                                  { return CONTINUE; }

    "decimal"                                   { return DECIMAL; }
    "deprecated"                                { return DEPRECATED; }
    "descending"                                { return DESCENDING; }

    "else"                                      { return ELSE; }
    "error"                                     { return ERROR; }
    "extern"                                    { return EXTERN; }

    "final"                                     { return FINAL; }
    "finally"                                   { return FINALLY; }
    "float"                                     { return FLOAT; }
    "flush"                                     { return FLUSH; }
    "foreach"                                   { return FOREACH; }
    "fork"                                      { return FORK; }
    "function"                                  { return FUNCTION; }
    "future"                                    { return FUTURE; }

    "if"                                        { return IF; }
    "import"                                    { return IMPORT; }
    "in"                                        { return IN; }
    "int"                                       { return INT; }
    "is"                                        { return IS; }

    "join"                                      { return JOIN; }
    "json"                                      { return JSON; }

    "limit"                                     { return LIMIT; }
    "listener"                                  { return LISTENER; }
    "lock"                                      { return LOCK; }

    "map"                                       { return MAP; }
    "match"                                     { return MATCH; }

    "new"                                       { return NEW; }

    "object"                                    { return OBJECT; }
    "onretry"                                   { return ONRETRY; }

    "panic"                                     { return PANIC; }
    "parameter"                                 { return TYPE_PARAMETER; }
    "private"                                   { return PRIVATE; }
    "public"                                    { return PUBLIC; }

    "record"                                    { return RECORD; }
    "remote"                                    { return REMOTE; }
    "resource"                                  { return RESOURCE; }
    "retry"                                     { return RETRY; }
    "retries"                                   { return RETRIES; }
    "return"                                    { return RETURN; }
    "returns"                                   { return RETURNS; }

    "service"                                   { return SERVICE; }
    "start"                                     { return START; }
    "stream"                                    { return STREAM; }
    "string"                                    { return STRING; }

    "table"                                     { return TABLE; }
    "transaction"                               { return TRANSACTION; }
    "trap"                                      { return TRAP; }
    "try"                                       { return TRY; }
    "type"                                      { return TYPE; }
    "typedesc"                                  { return TYPEDESC; }
    "throw"                                     { return THROW; }

    "untaint"                                   { return UNTAINT; }

    "wait"                                      { return WAIT; }
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
    "==="                                       { return REF_EQUAL; }
    "!=="                                       { return REF_NOT_EQUAL; }
    ">"                                         { return GT; }
    "<"                                         { return LT; }
    ">="                                        { return GT_EQUAL; }
    "<="                                        { return LT_EQUAL; }
    "&&"                                        { return AND; }
    "||"                                        { return OR; }

    "&"                                         { return BITAND; }
    "^"                                         { return BITXOR; }

    "~"                                         { return BIT_COMPLEMENT; }

    "->"                                        { return RARROW; }
    "<-"                                        { return LARROW; }
    "@"                                         { return AT; }
    "`"                                         { return BACKTICK; }
    ".."                                        { return RANGE; }
    "..."                                       { return ELLIPSIS; }
    "|"                                         { return PIPE; }
    "=>"                                        { return EQUAL_GT; }
    "?:"                                        { return ELVIS; }
    "->>"                                       { return SYNCRARROW; }

    "+="                                        { return COMPOUND_ADD; }
    "-="                                        { return COMPOUND_SUB; }
    "*="                                        { return COMPOUND_MUL; }
    "/="                                        { return COMPOUND_DIV; }

    "&="                                        { return COMPOUND_BIT_AND; }
    "|="                                        { return COMPOUND_BIT_OR; }
    "^="                                        { return COMPOUND_BIT_XOR; }

    "<<="                                       { return COMPOUND_LEFT_SHIFT; }
    ">>="                                       { return COMPOUND_RIGHT_SHIFT; }
    ">>>="                                      { return COMPOUND_LOGICAL_SHIFT; }

    "..<"                                       { return HALF_OPEN_RANGE; }

    "from"                                      { inTableSqlQuery = true; inSiddhiInsertQuery = true; inSiddhiOutputRateLimit = true; return FROM; }
    "on"                                        { return ON; }
    "select"                                    { if(inTableSqlQuery) { inTableSqlQuery = false; return SELECT; } return IDENTIFIER; }
    "group"                                     { return GROUP; }
    "by"                                        { return BY; }
    "having"                                    { return HAVING; }
    "order"                                     { return ORDER; }
    "where"                                     { return WHERE; }
    "followed"                                  { return FOLLOWED; }
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
    {QUOTED_STRING_LITERAL}                     { return QUOTED_STRING_LITERAL; }
    {SYMBOLIC_STRING_LITERAL}                   { return SYMBOLIC_STRING_LITERAL; }

    {DecimalFloatingPointNumber}                { return DECIMAL_FLOATING_POINT_NUMBER; }
    {HexadecimalFloatingPointLiteral}           { return HEXADECIMAL_FLOATING_POINT_LITERAL; }


    {BASE_16_BLOB_LITERAL}                      { return BASE_16_BLOB_LITERAL; }
    {BASE_64_BLOB_LITERAL}                      { return BASE_64_BLOB_LITERAL; }

    {IDENTIFIER}                                { return IDENTIFIER; }
    {LINE_COMMENT}                              { return LINE_COMMENT; }

    {XML_LITERAL_START}                         { yybegin(XML_MODE); return XML_LITERAL_START; }
    {STRING_TEMPLATE_LITERAL_START}             { inStringTemplate = true; yybegin(STRING_TEMPLATE_MODE); return STRING_TEMPLATE_LITERAL_START; }
    {EXPRESSION_END}                            { return checkExpressionEnd(); }

    {RETURN_PARAMETER_DOCUMENTATION_START}      { yybegin(MARKDOWN_DOCUMENTATION_MODE); return RETURN_PARAMETER_DOCUMENTATION_START; }
    {PARAMETER_DOCUMENTATION_START}             { yybegin(MARKDOWN_PARAMETER_DOCUMENTATION_MODE); return PARAMETER_DOCUMENTATION_START; }
    {MARKDOWN_DOCUMENTATION_LINE_START}         { yybegin(MARKDOWN_DOCUMENTATION_MODE); return MARKDOWN_DOCUMENTATION_LINE_START; }

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

<SINGLE_BACKTICK_INLINE_CODE_MODE>{
    {SINGLE_BACK_TICK_INLINE_CODE_END}          { yybegin(DEPRECATED_TEMPLATE_MODE); return SINGLE_BACK_TICK_INLINE_CODE_END; }
    {SINGLE_BACK_TICK_INLINE_CODE}              { return SINGLE_BACK_TICK_INLINE_CODE; }
     .                                          { yybegin(DEPRECATED_TEMPLATE_MODE); return BAD_CHARACTER; }
}

<DOUBLE_BACKTICK_INLINE_CODE_MODE>{
    {DOUBLE_BACK_TICK_INLINE_CODE_END}          { yybegin(DEPRECATED_TEMPLATE_MODE); return DOUBLE_BACK_TICK_INLINE_CODE_END; }
    {DOUBLE_BACK_TICK_INLINE_CODE}              { return DOUBLE_BACK_TICK_INLINE_CODE; }
     .                                          { yybegin(DEPRECATED_TEMPLATE_MODE); return BAD_CHARACTER; }
}

<TRIPLE_BACKTICK_INLINE_CODE_MODE>{
    {TRIPLE_BACK_TICK_INLINE_CODE_END}          { yybegin(DEPRECATED_TEMPLATE_MODE); return TRIPLE_BACK_TICK_INLINE_CODE_END; }
    {TRIPLE_BACK_TICK_INLINE_CODE}              { return TRIPLE_BACK_TICK_INLINE_CODE; }
     .                                          { yybegin(DEPRECATED_TEMPLATE_MODE); return BAD_CHARACTER; }
}

<DEPRECATED_TEMPLATE_MODE>{
    {DEPRECATED_TEMPLATE_END}                   { inDeprecatedTemplate = false; yybegin(YYINITIAL); return DEPRECATED_TEMPLATE_END; }
    {SB_DEPRECATED_INLINE_CODE_START}           { yybegin(SINGLE_BACKTICK_INLINE_CODE_MODE); return SB_DEPRECATED_INLINE_CODE_START; }
    {DB_DEPRECATED_INLINE_CODE_START}           { yybegin(DOUBLE_BACKTICK_INLINE_CODE_MODE); return DB_DEPRECATED_INLINE_CODE_START; }
    {TB_DEPRECATED_INLINE_CODE_START}           { yybegin(TRIPLE_BACKTICK_INLINE_CODE_MODE); return TB_DEPRECATED_INLINE_CODE_START; }
    {DEPRECATED_TEMPLATE_TEXT}                  { return DEPRECATED_TEMPLATE_TEXT; }
    .                                           { yybegin(YYINITIAL); return BAD_CHARACTER; }
}

<MARKDOWN_DOCUMENTATION_MODE>{
    {MARKDOWN_DOCUMENTATION_LINE_END}           { yybegin(YYINITIAL); }
    {SINGLE_BACKTICK_MARKDOWN_START}            { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return SINGLE_BACKTICK_MARKDOWN_START; }
    {DOUBLE_BACKTICK_MARKDOWN_START}            { yybegin(DOUBLE_BACKTICKED_MARKDOWN_MODE); return DOUBLE_BACKTICK_MARKDOWN_START; }
    {TRIPLE_BACKTICK_MARKDOWN_START}            { yybegin(TRIPLE_BACKTICKED_MARKDOWN_MODE); return TRIPLE_BACKTICK_MARKDOWN_START; }
    {MARKDOWN_DOCUMENTATION_TEXT}               { return MARKDOWN_DOCUMENTATION_TEXT; }
    .                                           { yybegin(YYINITIAL); return BAD_CHARACTER; }
}

<SINGLE_BACKTICKED_MARKDOWN_MODE>{
    {SINGLE_BACKTICK_MARKDOWN_END}              { yybegin(MARKDOWN_DOCUMENTATION_MODE); return SINGLE_BACKTICK_MARKDOWN_END; }
    {SINGLE_BACKTICK_CONTENT}                   { return SINGLE_BACKTICK_CONTENT; }
    .                                           { return BAD_CHARACTER; }
}

<DOUBLE_BACKTICKED_MARKDOWN_MODE>{
    {DOUBLE_BACKTICK_MARKDOWN_END}              { yybegin(MARKDOWN_DOCUMENTATION_MODE); return DOUBLE_BACKTICK_MARKDOWN_END; }
    {DOUBLE_BACKTICK_CONTENT}                   { return DOUBLE_BACKTICK_CONTENT; }
    .                                           { return BAD_CHARACTER; }
}

<TRIPLE_BACKTICKED_MARKDOWN_MODE>{
    {TRIPLE_BACKTICK_MARKDOWN_END}              { yybegin(MARKDOWN_DOCUMENTATION_MODE); return TRIPLE_BACKTICK_MARKDOWN_END; }
    {TRIPLE_BACKTICK_CONTENT}                   { return TRIPLE_BACKTICK_CONTENT; }
    .                                           { return BAD_CHARACTER; }
}

<MARKDOWN_PARAMETER_DOCUMENTATION_MODE> {
    {PARAMETER_DOCUMENTATION_END}               { yybegin(YYINITIAL); }
    {PARAMETER_NAME}                            { return PARAMETER_NAME; }
    {DESCRIPTION_SEPARATOR}                     { yybegin(MARKDOWN_DOCUMENTATION_MODE); return DESCRIPTION_SEPARATOR; }
    .                                           { return BAD_CHARACTER; }
}

[^]                                             { return BAD_CHARACTER; }
