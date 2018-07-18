lexer grammar BallerinaIRLexer;

PACKAGE     : 'package' ;
TYPE        : 'type' ;

STRING      : 'string' ;
INT         : 'int' ;
FUNCTION    : 'function';
BB          : 'bb' Int;

LEFT_BRACE          : '{' ;
RIGHT_BRACE         : '}' ;
QUOTE               : '"' ;
LEFT_PARENTHESIS    : '(' ;
RIGHT_PARENTHESIS   : ')' ;

Identifier
    :  '"' IdentifierLiteralChar+ '"'
    ;


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
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
Int
   :[1-9] [0-9]*
   | '0';


WS  :  [ \t]+ -> channel(HIDDEN)
    ;
NEW_LINE  :  [\r\n\u000C]+ -> channel(HIDDEN)
    ;

LINE_COMMENT
    :   '//' ~[\r\n]*   -> channel(HIDDEN)
    ;
