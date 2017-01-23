package org.ballerinalang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.psi.BallerinaTypes;
import com.intellij.psi.TokenType;

%%

%class BallerinaLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

EOL=\R
WHITE_SPACE=\s+

INTEGERLITERAL=0|[1-9][0-9]*
FLOATINGPOINTLITERAL=(([0-9]+\.[0-9]*)|(\.[0-9]+)|([0-9]+))([eE][+-]?[0-9]+)?[fF]
BOOLEANLITERAL=true|false
QUOTEDSTRINGLITERAL=\"([^\\\"\r\n]|\\[^\r\n])*\"
VALIDBACKTICKSTRINGCHARACTERS=`[^`]*`
IDENTIFIER=[a-zA-Z$_][a-zA-Z0-9$_]*
LINE_COMMENT="//"[^\r\n]*

%%
<YYINITIAL> {
  {WHITE_SPACE}                        { return TokenType.WHITE_SPACE; }

  "action"                             { return BallerinaTypes.ACTION; }
  "break"                              { return BallerinaTypes.BREAK; }
  "catch"                              { return BallerinaTypes.CATCH; }
  "connector"                          { return BallerinaTypes.CONNECTOR; }
  "const"                              { return BallerinaTypes.CONST; }
  "else"                               { return BallerinaTypes.ELSE; }
  "fork"                               { return BallerinaTypes.FORK; }
  "function"                           { return BallerinaTypes.FUNCTION; }
  "if"                                 { return BallerinaTypes.IF; }
  "import"                             { return BallerinaTypes.IMPORT; }
  "iterate"                            { return BallerinaTypes.ITERATE; }
  "join"                               { return BallerinaTypes.JOIN; }
  "new"                                { return BallerinaTypes.NEW; }
  "package"                            { return BallerinaTypes.PACKAGE; }
  "reply"                              { return BallerinaTypes.REPLY; }
  "resource"                           { return BallerinaTypes.RESOURCE; }
  "return"                             { return BallerinaTypes.RETURN; }
  "service"                            { return BallerinaTypes.SERVICE; }
  "throw"                              { return BallerinaTypes.THROW; }
  "throws"                             { return BallerinaTypes.THROWS; }
  "try"                                { return BallerinaTypes.TRY; }
  "type"                               { return BallerinaTypes.TYPE; }
  "typeconvertor"                      { return BallerinaTypes.TYPECONVERTOR; }
  "while"                              { return BallerinaTypes.WHILE; }
  "worker"                             { return BallerinaTypes.WORKER; }
  "`"                                  { return BallerinaTypes.BACKTICK; }
  "version"                            { return BallerinaTypes.VERSION; }
  "public"                             { return BallerinaTypes.PUBLIC; }
  "any"                                { return BallerinaTypes.ANY; }
  "all"                                { return BallerinaTypes.ALL; }
  "as"                                 { return BallerinaTypes.AS; }
  "timeout"                            { return BallerinaTypes.TIMEOUT; }
  "->"                                 { return BallerinaTypes.SENDARROW; }
  "<-"                                 { return BallerinaTypes.RECEIVEARROW; }
  "("                                  { return BallerinaTypes.LPAREN; }
  ")"                                  { return BallerinaTypes.RPAREN; }
  "{"                                  { return BallerinaTypes.LBRACE; }
  "}"                                  { return BallerinaTypes.RBRACE; }
  "["                                  { return BallerinaTypes.LBRACK; }
  "]"                                  { return BallerinaTypes.RBRACK; }
  ";"                                  { return BallerinaTypes.SEMI; }
  ","                                  { return BallerinaTypes.COMMA; }
  "."                                  { return BallerinaTypes.DOT; }
  "="                                  { return BallerinaTypes.ASSIGN; }
  ">"                                  { return BallerinaTypes.GT; }
  "<"                                  { return BallerinaTypes.LT; }
  "!"                                  { return BallerinaTypes.BANG; }
  "~"                                  { return BallerinaTypes.TILDE; }
  "?"                                  { return BallerinaTypes.QUESTION; }
  ":"                                  { return BallerinaTypes.COLON; }
  "=="                                 { return BallerinaTypes.EQUAL; }
  "<="                                 { return BallerinaTypes.LE; }
  ">="                                 { return BallerinaTypes.GE; }
  "!="                                 { return BallerinaTypes.NOTEQUAL; }
  "&&"                                 { return BallerinaTypes.AND; }
  "||"                                 { return BallerinaTypes.OR; }
  "+"                                  { return BallerinaTypes.ADD; }
  "-"                                  { return BallerinaTypes.SUB; }
  "*"                                  { return BallerinaTypes.MUL; }
  "/"                                  { return BallerinaTypes.DIV; }
  "&"                                  { return BallerinaTypes.BITAND; }
  "|"                                  { return BallerinaTypes.BITOR; }
  "^"                                  { return BallerinaTypes.CARET; }
  "%"                                  { return BallerinaTypes.MOD; }
  "$"                                  { return BallerinaTypes.DOLLAR_SIGN; }
  "null"                               { return BallerinaTypes.NULLLITERAL; }

  {INTEGERLITERAL}                     { return BallerinaTypes.INTEGERLITERAL; }
  {FLOATINGPOINTLITERAL}               { return BallerinaTypes.FLOATINGPOINTLITERAL; }
  {BOOLEANLITERAL}                     { return BallerinaTypes.BOOLEANLITERAL; }
  {QUOTEDSTRINGLITERAL}                { return BallerinaTypes.QUOTEDSTRINGLITERAL; }
  {VALIDBACKTICKSTRINGCHARACTERS}      { return BallerinaTypes.VALIDBACKTICKSTRINGCHARACTERS; }
  {IDENTIFIER}                         { return BallerinaTypes.IDENTIFIER; }
  {WHITE_SPACE}                        { return BallerinaTypes.WHITE_SPACE; }
  {LINE_COMMENT}                       { return BallerinaTypes.LINE_COMMENT; }

}

[^] { return TokenType.BAD_CHARACTER; }
