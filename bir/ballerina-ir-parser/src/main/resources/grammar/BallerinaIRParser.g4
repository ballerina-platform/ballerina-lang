parser grammar BallerinaIRParser;

options {
    language = Java;
    tokenVocab = BallerinaIRLexer;
}

irPackage : PACKAGE  Identifier? LEFT_BRACE function* RIGHT_BRACE EOF;

function : FUNCTION Identifier LEFT_PARENTHESIS RIGHT_PARENTHESIS LEFT_BRACE basicBlock+ RIGHT_BRACE;

basicBlock : BB LEFT_BRACE op* RIGHT_BRACE;

op : (
       opGoTo |
       opReturn
     )
     SEMICOLON;

opGoTo : GOTO BB;

opReturn : RETURN;
