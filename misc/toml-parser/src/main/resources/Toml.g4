grammar Toml;
toml : expression ( newline expression )*;

ALPHA : [A-Z] | [a-z];
alpha : ALPHA | TRUE | FALSE | NAN | INF | E | UPPERCASE_T | LOWERCASE_T | UPPERCASE_Z;
SPACE : ' ';
HYPHEN : '-';
PERIOD : '.';
QUOTATION_MARK : '"';
UNDERSCORE : '_';
COLON : ':';
COMMA : ',';
SLASH : '/';
APOSTROPHE : '\'';
EQUALS : '=';
HASH : '#';
LEFT_BRACKET : '[';
RIGHT_BRACKET : ']';
LEFT_BRACE : '{';
RIGHT_BRACE : '}';

expression
           : ws
           | ws keyVal ws
           | ws table ws;

// Whitespace

ws : wschar*;
wschar :   SPACE | '\t' |'\r'| '\n' ;

// Newline

newline : '\n' | '\r\n';

// Comment

COMMENT : '#' .*? ('\n' | '\r' | '\t') -> channel(2) ;

//Key-Value pairs

keyVal : key keyValSep val;

key : dottedKey | simpleKey;
simpleKey : unquotedKey | quotedKey;

unquotedKey : ( alpha | digit | HYPHEN | UNDERSCORE )* ;
quotedKey : basicString | literalString;
dottedKey : simpleKey (dotSep simpleKey)*;

keyValSep : ws EQUALS ws ;
dotSep : ws PERIOD ws;

val : string | bool | array | dateTime | floatingPoint | integer | inlineTable;

// String

string : mlBasicString | basicString | mlLiteralString | literalString;

// Basic String

basicString : QUOTATION_MARK basicStringValue QUOTATION_MARK;

basicStringValue : basicChar*;

basicChar : escaped | alpha | BASICUNESCAPED | SPACE | PLUS | HYPHEN | PERIOD | UNDERSCORE | COLON | COMMA
            | SLASH | APOSTROPHE| EQUALS | HASH | LEFT_BRACKET | RIGHT_BRACKET | LEFT_BRACE | RIGHT_BRACE | digit;

DIGIT19 : [1-9] ;
digit: '0' | DIGIT19;

BASICUNESCAPED : '\u0021' | '\u0023'..'\u005B' | '\u005D'..'\u007E' | '\u0080'..'\u10FF';

escaped : escapeSeqChar;

escapeSeqChar : '\\"' | '\\\\' | '\\/' | '\\b' | '\\f'| '\\n' | '\\r' | '\\t';

// Multiline Basic String

mlBasicString : mlBasicStringDelim mlBasicBody mlBasicStringDelim;

mlBasicStringDelim : QUOTATION_MARK QUOTATION_MARK QUOTATION_MARK;

mlBasicBody : ( mlBasicChar | newline | ( '\\' ws newline ) )*;
mlBasicChar : MLBASICUNESCAPED | escaped;

MLBASICUNESCAPED :   '\u0020'..'\u005B' |'\u005D'..'\u10FF';

// Literal String

literalString : APOSTROPHE LITERALCHAR* APOSTROPHE;


LITERALCHAR :  '\u0009' |'\u0020'..'\u0026'| '\u0028'..'\u10FF';

// Multiline Literal String

mlLiteralString : mlLiteralStringDelim mlLiteralBody mlLiteralStringDelim;

mlLiteralStringDelim : APOSTROPHE APOSTROPHE APOSTROPHE;

mlLiteralBody : ( MLLITERALCHAR| newline )* ;
// mlLiteralChar : %x09 / %x20-10FFFF

MLLITERALCHAR : '\u0009' | '\u0020'..'\u10FF';

// Integer

integer : decInt | hexInt | octInt | binInt;

minus   : '-';
PLUS    : '+';
DIGIT07 : [0-7];
DIGIT01 : [0-1];

hexPrefix : '0x';
octPrefix : '0o';
binPrefix : '0b';

decInt : ( minus | PLUS )? unsignedDecInt;
unsignedDecInt : digit | DIGIT19 ( digit | UNDERSCORE digit )*;

hexInt : hexPrefix HEXDIG *( HEXDIG | UNDERSCORE HEXDIG );
octInt : octPrefix DIGIT07 *( DIGIT07 | UNDERSCORE DIGIT07 );
binInt : binPrefix DIGIT01 *( DIGIT01 | UNDERSCORE DIGIT01 );

// Float

floatingPoint : floatIntPart ( exp | frac exp? ) | specialFloat;

floatIntPart : decInt;
frac : decimalPoint zeroPrefixableInt;
decimalPoint : PERIOD;
zeroPrefixableInt : digit ( digit | UNDERSCORE digit )*;

exp : E floatIntPart;

E : 'e';

specialFloat :  minus | PLUS ? ( INF | NAN );
INF : 'inf';
NAN : 'nan';

// Boolean

bool : TRUE | FALSE;

TRUE    : 'true';
FALSE   : 'false';

// Date and Time (as defined in RFC 3339)

dateTime      : offsetDateTime | localDateTime | localDate | localTime;

dateFullyear  : digit digit digit digit;
dateMonth     : digit digit;
dateMday      : digit digit;
timeDelim     : UPPERCASE_T | LOWERCASE_T | ' ';
timeHour      : digit digit;
timeMinute    : digit digit;
timeSecond    : digit digit;
timeSecfrac   : PERIOD digit;
timeNumoffset : ( '+' | HYPHEN) timeHour COLON timeMinute;
timeOffset    : UPPERCASE_Z | timeNumoffset;

partialTime   : timeHour COLON timeMinute COLON timeSecond timeSecfrac?;
fullDate      : dateFullyear HYPHEN dateMonth HYPHEN dateMday;
fullTime      : partialTime timeOffset;

UPPERCASE_T : 'T';
LOWERCASE_T : 't';
UPPERCASE_Z : 'Z';

// Offset Date-Time

offsetDateTime : fullDate timeDelim fullTime;

// Local Date-Time

localDateTime : fullDate timeDelim partialTime;

// Local Date

localDate : fullDate;

// Local Time

localTime : partialTime;

// Array

array : arrayOpen arrayValues? ws arrayClose;

arrayOpen  : '[';
arrayClose : ']';

arrayValues : ws arrayvalsNonEmpty (arraySep ws arrayvalsNonEmpty)*;

arrayvalsNonEmpty : val ws ;

arraySep : COMMA;

// Inline Table

inlineTable : inlineTableOpen inlineTableKeyvals? ws inlineTableClose;

inlineTableOpen     : '{';
inlineTableClose    : '}';
inlineTableSep      : ',';

inlineTableKeyvals :  ws inlineTableKeyvalsNonEmpty (inlineTableSep ws inlineTableKeyvalsNonEmpty)* ;
inlineTableKeyvalsNonEmpty : key keyValSep val ;

// Table

table : stdTable;

// Standard Table

stdTable : stdTableOpen key stdTableClose;

stdTableOpen  : '[';
stdTableClose : ']';