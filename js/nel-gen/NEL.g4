/*
 * Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

grammar NEL;

/* --- PARSER RULES --- */

// Definition of the script

script
    : ( handler | NEWLINE )* EOF;

/* Definition of the handler which contains the comments, @startuml,
statements, @enduml */

handler
    : (commentStatement NEWLINE+)?
        STARTUMLX NEWLINE+
          statementList
      ENDUMLX ;

// Definition of the statement list in the script
statementList
    : ( statement NEWLINE+ )* ;

// Definition of different types of statements
statement
    : titleStatement
    | participantStatement
    | constStatement
    | groupStatement
    | messageflowStatementList
    | variableStatement
    | commentStatement;

// Definition of the high level name for this message flow
titleStatement
    : IDENTIFIER WS+ COLON WS+ INTEGRATIONFLOWX;

/* Definition of the participating components which represents a
lifeline in the visual representation */
participantStatement
    : integrationFlowDefStatement
    | inboundEndpointDefStatement
    | pipelineDefStatement
    | outboundEndpointDefStatement
    ;

// Definition for a IntegrationFlow
integrationFlowDefStatement
    : PARTICIPANT WS+ IDENTIFIER WS+ COLON WS+ integrationFlowDef;

// Definition for a Inbound Endpoint
inboundEndpointDefStatement
    : PARTICIPANT WS+ IDENTIFIER WS+ COLON WS+ inboundEndpointDef;

// Definition for a pipeline
pipelineDefStatement
    : PARTICIPANT WS+ IDENTIFIER WS+ COLON WS+ pipelineDef;

// Definition for an Outbound Endpoint
outboundEndpointDefStatement
    : PARTICIPANT WS+ IDENTIFIER WS+ COLON WS+ outboundEndpointDef;

groupStatement: groupDefStatement
                messageflowStatementList
                END;

groupDefStatement: GROUP WS+  GROUP_NAME_DEF WS*
                                 COMMA_SYMBOL WS* GROUP_PATH_DEF
                                 COMMA_SYMBOL WS* GROUP_METHOD_DEF NEWLINE+;

GROUP_NAME_DEF: NAME WS* EQ_SYMBOL STRINGX;
GROUP_PATH_DEF: PATH WS* EQ_SYMBOL WS* URLTEMPLATEX;
GROUP_METHOD_DEF: METHOD WS* EQ_SYMBOL WS* STRINGX;

messageflowStatementList: (messageflowStatement NEWLINE+)*;

messageflowStatement: routingStatement
                          | mediatorStatement
                          | parallelStatement
                          | ifStatement
                          | loopStatement
                          | refStatement
                          | variableStatement
                          | commentStatement;

// Definition of a mediator statement
mediatorStatement : mediatorStatementDef;

mediatorStatementDef: MEDIATORDEFINITIONX ARGUMENTLISTDEF? logMediatorStatementDef? (headerMediatorStatementDef)?;


//Definition of a log mediator statement

logMediatorStatementDef: LPAREN LEVELDEF PARAMX*   (logPropertyStatementDef)* RPAREN;
logPropertyStatementDef: COMMA_SYMBOL PROPERTYDEF LPAREN KEYDEF  VALUEDEF?
                                           XPATHDEF?  (nameSpaceStatementDef)* JSONPATHDEF? RPAREN;
nameSpaceStatementDef: COMMA_SYMBOL  NAMESPACEDEF LPAREN PREFIXDEF URIDEF RPAREN;

//Definition of a header mediator statement

headerMediatorStatementDef: LPAREN  NAMEDEF PARAMX* VALUEDEF?  XPATHDEF? (nameSpaceStatementDef)*  RPAREN;

// Integration Flow constructor statement
integrationFlowDef: INTEGRATIONFLOWX LPAREN STRINGX RPAREN;

// Inbound Endpoint constructor statement
//inboundEndpointDef: INBOUNDENDPOINTX LPAREN PROTOCOLDEF COMMA_SYMBOL PORTDEF
//                    COMMA_SYMBOL CONTEXTDEF RPAREN;
inboundEndpointDef: INBOUNDENDPOINTX LPAREN PROTOCOLDEF PARAMX* RPAREN;

// Pipeline constructor statement
pipelineDef: PIPELINEX LPAREN COMMENTSTRINGX RPAREN;

// Outbound Endpoint constructor statement
outboundEndpointDef: OUTBOUNDENDPOINTX LPAREN PROTOCOLDEF PARAMX* RPAREN;

routingStatement: routingStatementDef;

routingStatementDef: IDENTIFIER WS+ ARROWX WS+ IDENTIFIER WS+
                  COLON WS+ COMMENTSTRINGX;

// A variable statement
variableStatement: variableDeclarationStatement
                    | variableAssignmentStatement
                    ;

// Variable definition statement
variableDeclarationStatement: VARX WS+ TYPEDEFINITIONX WS+ IDENTIFIER WS* EQ_SYMBOL WS*  COMMENTSTRINGX;

// Variable assignment statement
variableAssignmentStatement: VAR_IDENTIFIER WS* COMMENTSTRINGX;

// Constant definition statement
constStatement: CONSTX WS+ TYPEDEFINITIONX WS+ IDENTIFIER WS* EQ_SYMBOL WS*  COMMENTSTRINGX;

// Message routing statement
/*
routingStatement
    : genericRoutingStatement;

genericRoutingStatement: IDENTIFIER WS+ ARROW WS+ IDENTIFIER WS+ COMMENTX
                         WS+ STRINGX;
*/

// Definition of 'par' statement for parallel execution
parallelStatement
    : PAR NEWLINE?
      NEWLINE parMultiThenBlock
      END
    ;

parMultiThenBlock
    : messageflowStatementList NEWLINE (parElseBlock)? ;


parElseBlock
    : (ELSE NEWLINE messageflowStatementList)+ ;

// Definition of 'if' statement for if condition
ifStatement
    : IF WS WITH WS conditionStatement NEWLINE
      NEWLINE? ifMultiThenBlock
      END
    ;

conditionStatement
    : conditionDef;

conditionDef: CONDITIONX LPAREN SOURCEDEF PARAMX* RPAREN;

ifMultiThenBlock
    : messageflowStatementList NEWLINE (ifElseBlock)? ;


ifElseBlock
    : (ELSE NEWLINE messageflowStatementList)+ ;

// Definition of loop statement
loopStatement
    : LOOP WS expression NEWLINE
      NEWLINE? messageflowStatementList
      END
    ;
// Definition of reference statement
refStatement
    : REF WS IDENTIFIER NEWLINE?;


// Definition of internal comment statement
commentStatement
    : COMMENTST
    | HASHCOMMENTST
    | DOUBLESLASHCOMMENTST;


expression
    : EXPRESSIONX;


/* --- LEXER rules --- */

/* LEXER: keyword rules */

COMMENTST
    :  '/*' .*? '*/'
    ;

HASHCOMMENTST
    : '#' COMMENTPARAMS
    ;

DOUBLESLASHCOMMENTST
    : '//' COMMENTPARAMS
    ;

TYPEDEFINITIONX: TYPEDEFINITION;

//ROUTINGSTATEMENTX: ROUTINGSTATEMENT;

SOURCEDEF: SOURCE LPAREN CONFIGPARAMS RPAREN;

//PATTERNDEF: PATTERN LPAREN STRINGX RPAREN;

PROTOCOLDEF: PROTOCOL LPAREN STRINGX RPAREN;

//PORTDEF: PORT LPAREN NUMBER RPAREN;
PARAMX: COMMA_SYMBOL IDENTIFIER LPAREN DOUBLEQUOTES ANY_STRING DOUBLEQUOTES RPAREN;

NAMEDEF:  NAME LPAREN DOUBLEQUOTES ANY_STRING DOUBLEQUOTES RPAREN;

CONTEXTDEF: CONTEXT LPAREN URLSTRINGX RPAREN;

HOSTDEF: HOST LPAREN URLSTRINGX RPAREN;

CONFIGSDEF: CONFIGS LPAREN (CONFIGPARAMS COMMA_SYMBOL)* (CONFIGPARAMS)* RPAREN;

ARGUMENTLISTDEF: LPAREN (CONFIGPARAMS COMMA_SYMBOL)* (CONFIGPARAMS)* RPAREN;


 VALUEDEF:  VALUE LPAREN CONFIGPARAMS RPAREN;
 XPATHDEF:  XPATH LPAREN CONFIGPARAMS  RPAREN;
 PREFIXDEF: PREFIX LPAREN CONFIGPARAMS RPAREN;
 URIDEF:  URI LPAREN CONFIGPARAMS RPAREN;
 NAMESPACEDEF: NAMESPACE;
 JSONPATHDEF:   JSONPATH  LPAREN  CONFIGPARAMS RPAREN;

EXPRESSIONX: EXPRESSION;

CONDITIONX: CONDITION;

TIMEOUTDEF: TIMEOUT LPAREN NUMBER RPAREN;

INTEGRATIONFLOWX: INTEGRATIONFLOW;

INBOUNDENDPOINTX: INBOUNDENDPOINT;

PIPELINEX: PIPELINE;

MEDIATORDEFINITIONX: MEDIATORDEFINITION;

OUTBOUNDENDPOINTX: OUTBOUNDENDPOINT;

PROCESS_MESSAGEX: PROCESS_MESSAGE;

LEVELDEF: LEVEL LPAREN STRINGX RPAREN;

KEYDEF: KEY  LPAREN STRINGX RPAREN COMMA_SYMBOL;

PROPERTYDEF: PROPERTY;
LOGDEF: LOG;

ASX: AS;

COMMENTX: COMMENT;

COMMENTSTRINGX: COMMENTSTRING | NUMBER;

STRINGX: STRING;

URLSTRINGX: URLSTRING;

URLTEMPLATEX: URLTEMPLATESTR;

ARROWX: ARROW;


STRINGTYPEX: STRINGTYPE;
INTEGERTYPEX: INTEGERTYPE;
BOOLEANTYPEX: BOOLEANTYPE;
DOUBLETYPEX: DOUBLETYPE;
FLOATTYPEX: FLOATTYPE;
LONGTYPEX: LONGTYPE;
SHORTTYPEX: SHORTTYPE;
XMLTYPEX: XMLTYPE;
JSONTYPEX: JSONTYPE;

VARX: VAR;
CONSTX: CONST;

// LEXER: Keywords

STARTUMLX: STARTUML;
ENDUMLX: ENDUML;
PARTICIPANT: P A R T I C I P A N T;
PAR: P A R;
IF: I F;
REF: R E F;
END: E N D;
ELSE: E L S E;
LOOP: L O O P;
GROUP: G R O U P;
WITH : W I T H ;
NAME: N A M E;
PATH: P A T H;
METHOD: M E T H O D;



// LEXER: symbol rules

AMP_SYMBOL : '&' ;
AMPAMP_SYMBOL : '&&' ;
CARET_SYMBOL : '^' ;
COMMA_SYMBOL : ',' ;
COMMENT_SYMBOL : '--' ;
CONTINUATION_SYMBOL : '\\' | '\u00AC' ;
EQ_SYMBOL : '=' ;
GE_SYMBOL : '>=' | '\u2265' ;
GT_SYMBOL : '>' ;
LE_SYMBOL : '<=' | '\u2264' ;
LT_SYMBOL : '<' ;
MINUS_SYMBOL : '-' ;
NE_SYMBOL : '<>' | '\u2260';
PLUS_SYMBOL : '+' ;
STAR_SYMBOL : '*' ;
SLASH_SYMBOL : '/' ;
UNDERSCORE : '-';
COLON: ':';
fragment ARROW: '->';
fragment MEDIATORDEFINITION: IDENTIFIER COLON COLON IDENTIFIER;
SINGLEQUOTES: '\'';

// LEXER: miscellaneaous

LPAREN : '(' ;
RPAREN : ')' ;

NEWLINE
    : ( '\r\n' | '\n' | '\r' ) ;

WS
    : ' ';

IDENTIFIER
    : ('$')? ('a'..'z' | 'A'..'Z' ) ( 'a'..'z' | 'A'..'Z' | DIGIT | '_')+ ;

VAR_IDENTIFIER
    : ('$') ('a'..'z' | 'A'..'Z' ) ( 'a'..'z' | 'A'..'Z' | DIGIT | '_')+ WS* ('=');

ANY_STRING: ('$')? ('a'..'z' | 'A'..'Z' | DIGIT | '_' | '\\' | '/' | ':')+ ;

NUMBER
    : ( '0' | '1'..'9' DIGIT*) ('.' DIGIT+ )? ;

URL: ([a-zA-Z/\?&] | COLON | [0-9])+;

URLTEMPLATE: ([a-zA-Z/\?&] | COLON | [0-9] | '{' | '}' | '.' | '*' | '#' )+;

CONTINUATION
    : CONTINUATION_SYMBOL ~[\r\n]* NEWLINE -> skip ;

WHITESPACE
    : [ \t]+ -> skip ;


// LEXER: fragments to evaluate only within statements

//fragment ROUTINGSTATEMENT: IDENTIFIER WS+ ARROW WS+ IDENTIFIER WS+ COMMENTX
//                           WS+ STRINGX;
fragment STRING: DOUBLEQUOTES IDENTIFIER DOUBLEQUOTES;
fragment URLSTRING: DOUBLEQUOTES URL DOUBLEQUOTES;
fragment URLTEMPLATESTR: DOUBLEQUOTES URLTEMPLATE DOUBLEQUOTES;
fragment COMMENTSTRING: DOUBLEQUOTES COMMENTPARAMS DOUBLEQUOTES;
fragment EXPRESSION: LPAREN CONFIGPARAMS RPAREN;
fragment STARTUML: '@startuml';
fragment ENDUML: '@enduml';
fragment DOUBLEQUOTES: '"';
fragment POSTSCIPRT
    : ( 'a'..'z' | 'A'..'Z' | DIGIT | '_')*;
fragment CONFIGPARAMS: (WS | [a-zA-Z\?] | COLON | [0-9] | '$' | '.' | '@' |
                        SINGLEQUOTES | DOUBLEQUOTES | '{' | '}' | AMP_SYMBOL |
                        AMPAMP_SYMBOL | CARET_SYMBOL | COMMA_SYMBOL |
                        COMMENT_SYMBOL | CONTINUATION_SYMBOL | EQ_SYMBOL |
                        GE_SYMBOL | GT_SYMBOL | LE_SYMBOL | LT_SYMBOL |
                        MINUS_SYMBOL | NE_SYMBOL | PLUS_SYMBOL | STAR_SYMBOL |
                        SLASH_SYMBOL )+;
fragment COMMENTPARAMS: (WS | [a-zA-Z\?] | COLON | [0-9] | '$' | '.' | '@' |
                        SINGLEQUOTES | '{' | '}' | AMP_SYMBOL |
                        AMPAMP_SYMBOL | CARET_SYMBOL | COMMA_SYMBOL |
                        COMMENT_SYMBOL | CONTINUATION_SYMBOL | EQ_SYMBOL |
                        GE_SYMBOL | GT_SYMBOL | LE_SYMBOL | LT_SYMBOL |
                        MINUS_SYMBOL | NE_SYMBOL | PLUS_SYMBOL | STAR_SYMBOL |
                        SLASH_SYMBOL | '_')+;
fragment DIGIT : '0'..'9' ;
fragment INTEGRATIONFLOW: I N T E G R A T I O N F L O W;
fragment INBOUNDENDPOINT: I N B O U N D E N D P O I N T;
fragment HTTP: H T T P;
fragment PIPELINE: P I P E L I N E;
fragment PROCESSMESSAGE: P R O C E S S M E S S A G E;
fragment OUTBOUNDENDPOINT: O U T B O U N D E N D P O I N T ;
fragment PROTOCOL: P R O T O C O L;
fragment PORT: P O R T;
fragment ENDPOINT: E N D P O I N T;
fragment CONTEXT: C O N T E X T;
fragment TIMEOUT: T I M E O U T;
fragment HOST: H O S T;
fragment CONFIGS: C O N F I G S;
fragment CONDITION: C O N D I T I O N;
fragment SOURCE: S O U R C E;
fragment PATTERN: P A T T E R N;
fragment PROCESS_MESSAGE: 'process_message';
fragment AS: A S;
fragment COMMENT: C O M M E N T;
fragment CALL: C A L L;
fragment FILTER: F I L T E R;
fragment RESPOND: R E S P O N D;
fragment LOG: L O G;
fragment ENRICH: E N R I C H;
fragment TRANSFORM: T R A N S F O R M;
fragment STRINGTYPE: S T R I N G;
fragment INTEGERTYPE: I N T E G E R;
fragment BOOLEANTYPE: B O O L E A N;
fragment DOUBLETYPE: D O U B L E;
fragment FLOATTYPE: F L O A T;
fragment LONGTYPE: L O N G;
fragment SHORTTYPE: S H O R T;
fragment XMLTYPE: X M L;
fragment JSONTYPE: J S O N;
fragment VAR: V A R;
fragment CONST: C O N S T;
fragment LEVEL: L E V E L;
fragment KEY: K E Y;
fragment VALUE: V A L U E;
fragment PROPERTY: P R O P E R T Y;
fragment EXPRESSIOND: E X P R E S S I O N;
fragment XPATH: X P A T H;
fragment JSONPATH: J S O N P A T H;
fragment NAMESPACE: N A M E S P A C E;
fragment PREFIX: P R E F I X;
fragment URI: U R I;


fragment TYPEDEFINITION
    : INTEGERTYPE
    | STRINGTYPE
    | BOOLEANTYPE
    | LONGTYPE
    | SHORTTYPE
    | FLOATTYPE
    | DOUBLETYPE
    | XMLTYPE
    | JSONTYPE
    ;


// case insensitive lexer matching
fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');
