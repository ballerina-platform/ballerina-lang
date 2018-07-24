lexer grammar BallerinaIRLexer;

// keywords
PACKAGE     : 'package' ;
TYPE        : 'type' ;
FUNCTION    : 'function';

STRING      : 'string' ;
INT         : 'int' ;
BB          : 'bb' Int;

// op
GOTO        : 'goto';
RETURN      : 'return' ;

// delemters
LEFT_BRACE          : '{' ;
RIGHT_BRACE         : '}' ;
QUOTE               : '"' ;
LEFT_PARENTHESIS    : '(' ;
RIGHT_PARENTHESIS   : ')' ;
SEMICOLON           : ';' ;

// variables
Identifier
    :  '"' IdentifierLiteralChar+ '"'
    ;

// fragments
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

// hidden

WS  :  [ \t]+ -> channel(HIDDEN)
    ;
NEW_LINE  :  [\r\n\u000C]+ -> channel(HIDDEN)
    ;

LINE_COMMENT
    :   '//' ~[\r\n]*   -> channel(HIDDEN)
    ;
