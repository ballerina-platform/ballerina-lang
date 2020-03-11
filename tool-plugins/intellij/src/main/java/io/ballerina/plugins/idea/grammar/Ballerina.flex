package io.ballerina.plugins.idea.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.*;

%%

%{
    private boolean inXmlExpressionMode = false;
    private boolean inXmlTagMode = false;
    private boolean inDoubleQuotedXmlStringMode = false;
    private boolean inSingleQuotedXmlStringMode = false;
    private boolean inXmlPiMode = false;
    private boolean inXmlCommentMode = false;

    private boolean inStringTemplate = false;
    private boolean inStringTemplateExpression = false;

   private boolean inQueryExpression = false;

    public BallerinaLexer() {
        this((java.io.Reader)null);
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

DecimalFloatingPointNumber = {DecimalNumeral} {ExponentPart} {DecimalFloatSelector}? | {DottedDecimalNumber} {ExponentPart}? {DecimalFloatSelector}?
ExponentPart = {ExponentIndicator} {SignedInteger}
ExponentIndicator = [eE]
SignedInteger = {Sign}? {Digits}
Sign = [+-]
DecimalFloatSelector = [dDfF]

DecimalExtendedFloatingPointNumber = {DecimalFloatingPointNumber} "." {DecimalNumeral}

HexFloatingPointNumber = {HexDigits} {BinaryExponent} | {DottedHexNumber} {BinaryExponent}?
BinaryExponent = {BinaryExponentIndicator} {SignedInteger}
BinaryExponentIndicator = [pP]

// §3.10.3 Boolean Literals

BOOLEAN_LITERAL = "true" | "false"

// Note - Invalid escaped characters should be annotated at runtime.
// This is done becuase otherwise the string wont be identified correctly.
// Also the strings can either be enclosed in single or double quotes or no quotes at all.
ESCAPE_SEQUENCE = \\ [btnfr\"'\\] | {UnicodeEscape}
STRING_CHARACTER =  [^\\\u000A\u000D] | {ESCAPE_SEQUENCE}
STRING_CHARACTERS = {STRING_CHARACTER}+
QUOTED_STRING_LITERAL = {DOUBLE_QUOTE} {STRING_CHARACTERS}? {DOUBLE_QUOTE}

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

IDENTIFIER = {UnquotedIdentifier} | {QuotedIdentifier}
UnquotedIdentifier =  {IdentifierInitialChar} {IdentifierFollowingChar}*
QuotedIdentifier =  \' {QuotedIdentifierChar}+
QuotedIdentifierChar = {IdentifierFollowingChar} | {QuotedIdentifierEscape} | {StringNumericEscape}

IdentifierInitialChar = [a-zA-Z_]
    // Negates ( AsciiChar | UnicodeNonIdentifierChar )
    | [^\u0000-\u007F\uE000-\uF8FF\u200E\u200F\u2028\u2029\u00A1-\u00A7\u00A9\u00AB-\u00AC\u00AE\u00B0-\u00B1\u00B6-\u00B7\u00BB\u00BF\u00D7\u00F7\u2010-\u2027\u2030-\u205E\u2190-\u2BFF\u3001-\u3003\u3008-\u3020\u3030\uFD3E-\uFD3F\uFE45-\uFE46\uDB80-\uDBBF\uDBC0-\uDBFF\uDC00-\uDFFF]

IdentifierFollowingChar = {IdentifierInitialChar} | {DIGIT}

// QuotedIdentifierEscape := \ ^ ( AsciiLetter | 0x9 | 0xA | 0xD | UnicodePatternWhiteSpaceChar )
// AsciiLetter := A .. Z | a .. z
// UnicodePatternWhiteSpaceChar := 0x200E | 0x200F | 0x2028 | 0x2029
QuotedIdentifierEscape = \\ [^a-zA-Z\u0009\u000A\u000D\u200E\u200F\u2028\u2029]
StringNumericEscape = \\ [|\"\\/] | \\\\ [btnfr] | {UnicodeEscape}
UnicodeEscape = \\u "{" {HexDigit} "}"

WHITE_SPACE=\s+

BACKTICK = "`"

// Todo - Add inspection
LINE_COMMENT = "/" "/" [^\r\n]*

XML_LITERAL_START = xml[ \t\n\x0B\f\r]*`

INTERPOLATION_START = "${"

HEX_DIGITS = {HEX_DIGIT} ({HEX_DIGIT_OR_UNDERSCORE}* {HEX_DIGIT})?
HEX_DIGIT_OR_UNDERSCORE = {HEX_DIGIT} | "_"

//Todo - Remove after restoring xml grammar support
// XML
XML_ALL_CHAR = [^`]
XML_LITERAL_END = "`"
DOUBLE_QUOTE = "\""

// MARKDOWN_DOCUMENTATION
MARKDOWN_DOCUMENTATION_LINE_START =  {HASH} {DOCUMENTATION_SPACE}?
PARAMETER_DOCUMENTATION_START = {HASH} {DOCUMENTATION_SPACE}? {ADD} {DOCUMENTATION_SPACE}*
RETURN_PARAMETER_DOCUMENTATION_START = {HASH} {DOCUMENTATION_SPACE}? {ADD} {DOCUMENTATION_SPACE}* {RETURN} {DOCUMENTATION_SPACE}* {SUB} {DOCUMENTATION_SPACE}*
DOCUMENTATION_SPACE = [ ]

// MARKDOWN_DOCUMENTATION_MODE
DOCTYPE = "type" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCSERVICE = "service" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCVARIABLE = "variable" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCVAR = "var" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCANNOTATION = "annotation" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCMODULE = "module" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCFUNCTION = "function" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCPARAMETER = "parameter" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
DOCCONST = "const" {DOCUMENTATION_ESCAPED_CHARACTERS}+ {SINGLE_BACKTICK_MARKDOWN_START}
MARKDOWN_DOCUMENTATION_TEXT = {DOCUMENTATION_TEXT_CHARACTER}+
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

// STRING_TEMPLATE
STRING_TEMPLATE_LITERAL_START = string[ \t\n\x0B\f\r]*`
STRING_TEMPLATE_LITERAL_END = "`"
STRING_LITERAL_ESCAPED_SEQUENCE = {DOLLAR}** \\ [\\'\"bnftr\{`]
STRING_TEMPLATE_VALID_CHAR_SEQUENCE = [^`$\\] | {DOLLAR}+ [^`$\{\\] | {WHITE_SPACE} | {STRING_LITERAL_ESCAPED_SEQUENCE}
STRING_TEMPLATE_EXPRESSION_START = {STRING_TEMPLATE_TEXT}? {INTERPOLATION_START}
STRING_TEMPLATE_EXPRESSION_END = "}"
STRING_TEMPLATE_TEXT = {STRING_TEMPLATE_VALID_CHAR_SEQUENCE}+ {DOLLAR}* | {DOLLAR}+
DOLLAR = \$

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

%state STRING_TEMPLATE_MODE

%%
<YYINITIAL> {
    "__init"                                    { return OBJECT_INIT; }

    "abort"                                     { return ABORT; }
    "aborted"                                   { return ABORTED; }
    "abstract"                                  { return ABSTRACT; }
    "annotation"                                { return ANNOTATION; }
    "any"                                       { return ANY; }
    "anydata"                                   { return ANYDATA; }
    "as"                                        { return AS; }

    "boolean"                                   { return BOOLEAN; }
    "break"                                     { return BREAK; }
    "byte"                                      { return BYTE; }

    "catch"                                     { return CATCH; }
    "channel"                                   { return CHANNEL; }
    "check"                                     { return CHECK; }
    "checkpanic"                                { return CHECKPANIC; }
    "client"                                    { return CLIENT; }
    "committed"                                 { return COMMITTED; }
    "const"                                     { return CONST; }
    "continue"                                  { return CONTINUE; }

    "decimal"                                   { return DECIMAL; }
    "default"                                   { return DEFAULT; }

    "else"                                      { return ELSE; }
    "error"                                     { return ERROR; }
    "external"                                  { return EXTERNAL; }

    "final"                                     { return FINAL; }
    "finally"                                   { return FINALLY; }
    "float"                                     { return FLOAT; }
    "flush"                                     { return FLUSH; }
    "foreach"                                   { return FOREACH; }
    "fork"                                      { return FORK; }
    "function"                                  { return FUNCTION; }
    "future"                                    { return FUTURE; }

    "handle"                                    { return HANDLE; }

    "if"                                        { return IF; }
    "import"                                    { return IMPORT; }
    "in"                                        { return IN; }
    "int"                                       { return INT; }
    "is"                                        { return IS; }

    "json"                                      { return JSON; }

    "let"                                       { return LET; }
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
    "source"                                    { return SOURCE; }
    "start"                                     { return START; }
    "stream"                                    { return STREAM; }
    "string"                                    { return STRING; }

    "table"                                     { return TABLE; }
    "transaction"                               { return TRANSACTION; }
    "trap"                                      { return TRAP; }
    "try"                                       { return TRY; }
    "type"                                      { return TYPE; }
    "typedesc"                                  { return TYPEDESC; }
    "typeof"                                    { return TYPEOF; }
    "throw"                                     { return THROW; }

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
    "}"                                         { if (inStringTemplateExpression) {
                                                        inStringTemplateExpression = false;
                                                        inStringTemplate = true;
                                                        yybegin(STRING_TEMPLATE_MODE);
                                                        return STRING_TEMPLATE_EXPRESSION_END;
                                                  }
                                                  if (inXmlExpressionMode) {
                                                      inXmlExpressionMode = false;
                                                      yybegin(XML_MODE);
                                                      return RIGHT_BRACE;
                                                  }
                                                  if (inXmlCommentMode) {
                                                      inXmlCommentMode = false;
                                                      yybegin(XML_COMMENT_MODE);
                                                      return RIGHT_BRACE;
                                                  }
                                                  return RIGHT_BRACE;
                                                }
    "("                                         { return LEFT_PARENTHESIS; }
    ")"                                         { return RIGHT_PARENTHESIS; }
    "["                                         { return LEFT_BRACKET; }
    "]"                                         { return RIGHT_BRACKET; }
    "?"                                         { return QUESTION_MARK; }
    "{|"                                        { return LEFT_CLOSED_RECORD_DELIMITER; }
    "|}"                                        { return RIGHT_CLOSED_RECORD_DELIMITER; }

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

    ".@"                                        { return ANNOTATION_ACCESS; }
    "?."                                        { return OPTIONAL_FIELD_ACCESS; }

    "on"                                        { return ON; }
    "from"                                      { inQueryExpression=true; return FROM; }
    "select"                                    { if(inQueryExpression){ inQueryExpression = false; return SELECT; } return IDENTIFIER; }
    "do"                                        { if(inQueryExpression){ inQueryExpression = false; return DO; } return IDENTIFIER; }
    "where"                                     { if(inQueryExpression){ return WHERE; } return IDENTIFIER; }

    {WHITE_SPACE}                               { return WHITE_SPACE; }

    {NULL_LITERAL}                              { return NULL_LITERAL; }
    {BOOLEAN_LITERAL}                           { return BOOLEAN_LITERAL; }
    {DECIMAL_INTEGER_LITERAL}                   { return DECIMAL_INTEGER_LITERAL; }
    {HEX_INTEGER_LITERAL}                       { return HEX_INTEGER_LITERAL; }
    {QUOTED_STRING_LITERAL}                     { return QUOTED_STRING_LITERAL; }

    {DecimalFloatingPointNumber}                { return DECIMAL_FLOATING_POINT_NUMBER; }
    {DecimalExtendedFloatingPointNumber}        { return DECIMAL_EXTENDED_FLOATING_POINT_NUMBER; }
    {HexadecimalFloatingPointLiteral}           { return HEXADECIMAL_FLOATING_POINT_LITERAL; }


    {BASE_16_BLOB_LITERAL}                      { return BASE_16_BLOB_LITERAL; }
    {BASE_64_BLOB_LITERAL}                      { return BASE_64_BLOB_LITERAL; }

    {IDENTIFIER}                                { return IDENTIFIER; }
    {LINE_COMMENT}                              { return LINE_COMMENT; }

    {XML_LITERAL_START}                         { yybegin(XML_MODE); return XML_LITERAL_START; }
    {STRING_TEMPLATE_LITERAL_START}             { inStringTemplate = true; yybegin(STRING_TEMPLATE_MODE); return STRING_TEMPLATE_LITERAL_START; }

    {RETURN_PARAMETER_DOCUMENTATION_START}      { yybegin(MARKDOWN_DOCUMENTATION_MODE); return RETURN_PARAMETER_DOCUMENTATION_START; }
    {PARAMETER_DOCUMENTATION_START}             { yybegin(MARKDOWN_PARAMETER_DOCUMENTATION_MODE); return PARAMETER_DOCUMENTATION_START; }
    {MARKDOWN_DOCUMENTATION_LINE_START}         { yybegin(MARKDOWN_DOCUMENTATION_MODE); return MARKDOWN_DOCUMENTATION_LINE_START; }

    .                                           { return BAD_CHARACTER; }
}

<XML_MODE>{
    {XML_LITERAL_END}                           { yybegin(YYINITIAL); return XML_LITERAL_END; }
    {XML_ALL_CHAR}                              { return XML_ALL_CHAR; }
    .                                           { return BAD_CHARACTER; }
}

<STRING_TEMPLATE_MODE>{
    {STRING_TEMPLATE_LITERAL_END}               { inStringTemplate = false; yybegin(YYINITIAL); return STRING_TEMPLATE_LITERAL_END; }
    {STRING_TEMPLATE_EXPRESSION_START}          { inStringTemplate = false; inStringTemplateExpression = true; yybegin(YYINITIAL); return STRING_TEMPLATE_EXPRESSION_START; }
    {STRING_TEMPLATE_TEXT}                      { return STRING_TEMPLATE_TEXT; }
    .                                           { inStringTemplate = false; yybegin(YYINITIAL); return BAD_CHARACTER; }
}

<MARKDOWN_DOCUMENTATION_MODE>{
    {MARKDOWN_DOCUMENTATION_LINE_END}           { yybegin(YYINITIAL); }
    {DOCTYPE}                                   { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCTYPE; }
    {DOCSERVICE}                                { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCSERVICE; }
    {DOCVARIABLE}                               { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCVARIABLE; }
    {DOCVAR}                                    { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCVAR; }
    {DOCANNOTATION}                             { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCANNOTATION; }
    {DOCMODULE}                                 { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCMODULE; }
    {DOCFUNCTION}                               { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCFUNCTION; }
    {DOCPARAMETER}                              { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCPARAMETER; }
    {DOCCONST}                                  { yybegin(SINGLE_BACKTICKED_MARKDOWN_MODE); return DOCCONST; }
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
