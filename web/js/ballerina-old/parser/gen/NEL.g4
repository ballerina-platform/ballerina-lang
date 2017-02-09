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

// starting point for parsing a java file
sourceFile
    :   definition
        constants?
        globalVariables?
        resources
        EOF
    ;

definition
    :   path?
        source
        api?
        packageDef
    ;

constants
    :   constant*
    ;

globalVariables
    :   globalVariable*
    ;

resources
    :   resource+
    ;

packageDef
    :   PACKAGE qualifiedName ';'
    ;

path
    :   AT 'Path'  LPAREN  StringLiteral RPAREN
    ;

source
    :   AT 'Source'  LPAREN sourceElementValuePairs RPAREN
    ;

api
    :   '@' 'Service'  ( '(' ( apiElementValuePairs ) ')' )
    ;

resourcePath
    :   '@' 'Path' ( '(' StringLiteral ')' )
    ;


getMethod
    :   '@' 'GET' ( '(' ')' )?
    ;

postMethod
    :   '@' 'POST' ( '('  ')' )?
    ;

putMethod
    :   '@' 'PUT' ( '(' ')' )?
    ;

deleteMethod
    :   '@' 'DELETE' ( '(' ')' )?
    ;

headMethod
    :   '@' 'HEAD' ( '(' ')' )?
    ;

prodAnt
    :   '@' 'Produces' ( '(' elementValue ? ')' )?
    ;

conAnt
    :   '@' 'Consumes' ( '(' elementValue ? ')' )?
    ;

antApiOperation
    :   '@' 'ApiOperation' ( '(' ( elementValuePairs | elementValue )? ')' )?
    ;

antApiResponses
    :   '@' 'ApiResponses' '(' ( antApiResponseSet )? ')'
    ;

antApiResponseSet
    :   antApiResponse (',' antApiResponse)*
    ;

antApiResponse
    :   '@' 'ApiResponse' '(' ( elementValuePairs | elementValue )? ')'
    ;

elementValuePairs
    :   elementValuePair (',' elementValuePair)*
    ;

sourceElementValuePairs
    :   interfaceDeclaration
    |   protocol (',' host)?  (','  port)?
    ;

interfaceDeclaration
    :   'interface' '=' StringLiteral
    ;

apiElementValuePairs
    :  (tags ',')?  (descripton ',')? producer
    ;

protocol
    :   'protocol' '=' StringLiteral
    ;

host
    :   'host' '=' StringLiteral
    ;

port
    :   'port' '=' IntegerLiteral
    ;

tags
    :   'tags' '=' tag+
    ;

tag
    :   '{' StringLiteral (',' StringLiteral)* '}'
    ;

descripton
    :   'description' '=' StringLiteral
    ;

producer
    :   'produces' '=' mediaType
    ;

constant
    :   CONSTANT type Identifier  '=' literal ';'
    |   CONSTANT classType Identifier  '=' 'new' Identifier '(' (StringLiteral)? ')' ';'
    ;

globalVariable
    :   type    Identifier  '='   literal ';'
    |   endpointDeclaration '=' newTypeObjectCreation   ';'
    ;

elementValuePair
    :   Identifier '=' elementValue
    ;

elementValue
    :   StringLiteral
    |   IntegerLiteral
;

// Resource Level
resource
    :   httpMethods
        prodAnt?
        conAnt ?
        antApiOperation?
        antApiResponses?
        resourcePath
        resourceDeclaration
    ;

httpMethods
    :(getMethod
    | postMethod
    | putMethod
    | deleteMethod
    | headMethod)*
    ;

qualifiedName
    :   Identifier ('.' Identifier)*
    ;

resourceDeclaration
    :   'resource' resourceName '(' 'message' Identifier ')' block
    ;

resourceName
    :   Identifier
    ;

// block is anything that starts with '{' and and ends with '}'
block
    :   '{' blockStatement* '}'
    ;

//Anything that contains inside a block
blockStatement
    :   localVariableDeclarationStatement   //  eg: int i;
    |   localVariableInitializationStatement    // eg: string endpoint = "my_endpoint";
    |   localVariableAssignmentStatement    //  eg: i =45; msgModification mediators also falls under this
    |   messageModificationStatement    //  eg: response.setHeader(HTTP.StatusCode, 500);
    |   returnStatement //  eg: reply response;
    |   mediatorCallStatement // eg: log(level="custom", log_value="log message");
    |   tryCatchBlock   // flowControl Mediator
    |   ifElseBlock // flowControl Mediator
    ;

// try catch definition
tryCatchBlock
    :   tryClause   catchClause+
    ;

tryClause
    :   'try' block
    ;

catchClause
    :   'catch' '(' exceptionHandler ')' block
    ;

exceptionHandler
    :   exceptionType   Identifier
    ;

exceptionType   //Identifier can be added to give custom exception handling
    : 'ConnectionClosedException'
    | 'ConnectionFailedException'
    | 'ConnectionTimeoutException'
    | 'Exception'  //default exception
    ;

// if else definition
ifElseBlock
    :   ifBlock ( elseBlock )?
    ;

ifBlock
    :   'if' parExpression block
    ;

elseBlock
    :   'else'  block
    ;

// local variable handling statements
localVariableDeclarationStatement
    :   endpointDeclaration ';' // endpointDeclaration has given more priority over (classType Identifier)
    |   (type|classType)    Identifier  ';'
    ;

localVariableInitializationStatement
    :   type    Identifier  '='   literal ';'
    |   endpointDeclaration '=' newTypeObjectCreation   ';'
    |   classType Identifier  '=' newTypeObjectCreation ';' // only used for new message creation
    |   classType Identifier '=' mediatorCall ';' // calling a mediator that will return a message
    ;

localVariableAssignmentStatement
    :   Identifier  '='   literal ';'
    |   Identifier '='  newTypeObjectCreation ';'
    |   Identifier '=' mediatorCall ';'
    ;

mediatorCallStatement
    :   mediatorCall ';'
    ;

 // this is only used when "m = new message ()" and "ep = new endpoint("http://google.lk")" called
newTypeObjectCreation
    :  'new' classType '(' literal?  ')'
    ;

//mediator calls
mediatorCall
    :   Identifier '(' ( keyValuePairs )? ')'
    ;

//Endpoints
endpointDeclaration
    :   parametersAnnotation?
        circuitBreakerAnnotation?
        'endpoint' Identifier
    ;

parametersAnnotation
    :   '@Parameters' '(' keyValuePairs? ')'
    ;

circuitBreakerAnnotation
    :   '@CircuitBreaker' '(' keyValuePairs? ')'
    ;

keyValuePairs
    : keyValuePair ( ',' keyValuePair )*
    ;

// classType is also used as a parameter identifier because, 'endpoint' and 'message' is also commenly used as
// method argument identifiers
keyValuePair
    :   (Identifier | classType) '='  ( literal | Identifier )
    ;

// Message Modification statements
messageModificationStatement
    :   Identifier  '.' Identifier '('  messagePropertyName ','  literal ')' ';'
    ;

//return (reply) Statement specification
returnStatement
    :   'reply' (Identifier | mediatorCall)? ';'
    ;

// expression, which will be used to build the parExpression used inside if condition
parExpression
    :   '(' expression ( ( GT | LT | EQUAL | LE | GE | NOTEQUAL | AND | OR ) expression )? ')'
    ;

expression
    :   evalExpression
    |   literal
    ;

evalExpression
    :   'eval' '(' Identifier '=' Identifier ',' 'path' '=' StringLiteral ')'
    ;

literal
      :   IntegerLiteral
      |   FloatingPointLiteral
      |   CharacterLiteral
      |   StringLiteral
      |   BooleanLiteral
      |   'null'
 ;

mediaType
      : 'MediaType.APPLICATION_JSON'
      | 'MediaType.APPLICATION_XML'
;

type
      :   'boolean'
      |   'char'
      |   'byte'
      |   'short'
      |   'int'
      |   'long'
      |   'float'
      |   'double'
      |   'string'
      ;

classType
      :   'endpoint'
      |   'message'
      ;

messagePropertyName
    :   Identifier ('.' Identifier)*
    ;

// LEXER

// §3.9 Keywords

ABSTRACT      : 'abstract';
ASSERT        : 'assert';
BOOLEAN       : 'boolean';
BREAK         : 'break';
BYTE          : 'byte';
CASE          : 'case';
CATCH         : 'catch';
CHAR          : 'char';
CLASS         : 'class';
CONST         : 'const';
CONTINUE      : 'continue';
CONSTANT      : 'constant';
DEFAULT       : 'default';
DO            : 'do';
DOUBLE        : 'double';
ELSE          : 'else';
ENUM          : 'enum';
EXTENDS       : 'extends';
FINAL         : 'final';
FINALLY       : 'finally';
FLOAT         : 'float';
FOR           : 'for';
IF            : 'if';
GOTO          : 'goto';
IMPLEMENTS    : 'implements';
IMPORT        : 'import';
INSTANCEOF    : 'instanceof';
INT           : 'int';
INTERFACE     : 'interface';
LONG          : 'long';
NATIVE        : 'native';
NEW           : 'new';
PACKAGE       : 'package';
PRIVATE       : 'private';
PROTECTED     : 'protected';
PUBLIC        : 'public';
RETURN        : 'return';
SHORT         : 'short';
STATIC        : 'static';
STRICTFP      : 'strictfp';
SUPER         : 'super';
SWITCH        : 'switch';
SYNCHRONIZED  : 'synchronized';
THIS          : 'this';
THROW         : 'throw';
THROWS        : 'throws';
TRANSIENT     : 'transient';
TRY           : 'try';
VOID          : 'void';
VOLATILE      : 'volatile';
WHILE         : 'while';

// §3.10.1 Integer Literals

IntegerLiteral
    :   DecimalIntegerLiteral
    |   HexIntegerLiteral
    |   OctalIntegerLiteral
    |   BinaryIntegerLiteral
    |   VaribaleLiteral
    ;

fragment
DecimalIntegerLiteral
    :   DecimalNumeral IntegerTypeSuffix?
    ;

fragment
HexIntegerLiteral
    :   HexNumeral IntegerTypeSuffix?
    ;

fragment
OctalIntegerLiteral
    :   OctalNumeral IntegerTypeSuffix?
    ;

fragment
BinaryIntegerLiteral
    :   BinaryNumeral IntegerTypeSuffix?
    ;

fragment
IntegerTypeSuffix
    :   [lL]
    ;

fragment
DecimalNumeral
    :   '0'
    |   NonZeroDigit (Digits? | Underscores Digits)
    ;

fragment
Digits
    :   Digit (DigitOrUnderscore* Digit)?
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
DigitOrUnderscore
    :   Digit
    |   '_'
    ;

fragment
Underscores
    :   '_'+
    ;

fragment
HexNumeral
    :   '0' [xX] HexDigits
    ;

fragment
HexDigits
    :   HexDigit (HexDigitOrUnderscore* HexDigit)?
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
HexDigitOrUnderscore
    :   HexDigit
    |   '_'
    ;

fragment
OctalNumeral
    :   '0' Underscores? OctalDigits
    ;

fragment
OctalDigits
    :   OctalDigit (OctalDigitOrUnderscore* OctalDigit)?
    ;

fragment
OctalDigit
    :   [0-7]
    ;

fragment
OctalDigitOrUnderscore
    :   OctalDigit
    |   '_'
    ;

fragment
BinaryNumeral
    :   '0' [bB] BinaryDigits
    ;

fragment
BinaryDigits
    :   BinaryDigit (BinaryDigitOrUnderscore* BinaryDigit)?
    ;

fragment
BinaryDigit
    :   [01]
    ;

fragment
BinaryDigitOrUnderscore
    :   BinaryDigit
    |   '_'
    ;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
    :   DecimalFloatingPointLiteral
    |   HexadecimalFloatingPointLiteral
    |   VaribaleLiteral
    ;

fragment
DecimalFloatingPointLiteral
    :   Digits '.' Digits? ExponentPart? FloatTypeSuffix?
    |   '.' Digits ExponentPart? FloatTypeSuffix?
    |   Digits ExponentPart FloatTypeSuffix?
    |   Digits FloatTypeSuffix
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
FloatTypeSuffix
    :   [fFdD]
    ;

fragment
HexadecimalFloatingPointLiteral
    :   HexSignificand BinaryExponent FloatTypeSuffix?
    ;

fragment
HexSignificand
    :   HexNumeral '.'?
    |   '0' [xX] HexDigits? '.' HexDigits
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
    |   VaribaleLiteral
    ;

// §3.10.4 Character Literals

CharacterLiteral
    :   '\'' SingleCharacter '\''
    |   '\'' EscapeSequence '\''
    ;

fragment
SingleCharacter
    :   ~['\\]
    ;
// §3.10.5 String Literals

StringLiteral
    :   '"' StringCharacters? '"'
    |   VaribaleLiteral
    ;
fragment
StringCharacters
    :   StringCharacter+
    ;
fragment
StringCharacter
    :   ~["\\]
    |   EscapeSequence
    ;
// §3.10.6 Escape Sequences for Character and String Literals
fragment
EscapeSequence
    :   '\\' [btnfr"'\\]
    |   OctalEscape
    |   UnicodeEscape
    ;

fragment
OctalEscape
    :   '\\' OctalDigit
    |   '\\' OctalDigit OctalDigit
    |   '\\' ZeroToThree OctalDigit OctalDigit
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment
ZeroToThree
    :   [0-3]
    ;

// §3.10.7 The Null Literal

NullLiteral
    :   'null'
    ;

// §3.11 Separators

LPAREN          : '(';
RPAREN          : ')';
LBRACE          : '{';
RBRACE          : '}';
LBRACK          : '[';
RBRACK          : ']';
SEMI            : ';';
COMMA           : ',';
DOT             : '.';

// §3.12 Operators

ASSIGN          : '=';
GT              : '>';
LT              : '<';
BANG            : '!';
TILDE           : '~';
QUESTION        : '?';
COLON           : ':';
EQUAL           : '==';
LE              : '<=';
GE              : '>=';
NOTEQUAL        : '!=';
AND             : '&&';
OR              : '||';
INC             : '++';
DEC             : '--';
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';
BITAND          : '&';
BITOR           : '|';
CARET           : '^';
MOD             : '%';

ADD_ASSIGN      : '+=';
SUB_ASSIGN      : '-=';
MUL_ASSIGN      : '*=';
DIV_ASSIGN      : '/=';
AND_ASSIGN      : '&=';
OR_ASSIGN       : '|=';
XOR_ASSIGN      : '^=';
MOD_ASSIGN      : '%=';
LSHIFT_ASSIGN   : '<<=';
RSHIFT_ASSIGN   : '>>=';
URSHIFT_ASSIGN  : '>>>=';


// WSO2 NEL Addition***********************************************
fragment
VaribaleLiteral
    :   '$' Identifier
    ;
//*****************************************************************

// §3.8 Identifiers (must appear after all keywords in the grammar)

Identifier
    :   JavaLetter JavaLetterOrDigit*
    ;

fragment
JavaLetter
    :   [a-zA-Z$_] // these are the "java letters" below 0x7F
    ;

fragment
JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
    ;

//
// Additional symbols not defined in the lexical specification
//

AT : '@';
ELLIPSIS : '...';

//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
    ;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;