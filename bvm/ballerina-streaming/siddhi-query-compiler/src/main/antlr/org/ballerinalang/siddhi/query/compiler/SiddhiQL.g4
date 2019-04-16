/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

grammar SiddhiQL;

@header {
	//import SiddhiParserException;
}

parse
    : siddhi_app EOF
    ;

error
    : UNEXPECTED_CHAR
  //      {throw new SiddhiParserException("You have an error in your SiddhiQL at line " + $UNEXPECTED_CHAR.line + ", unexpected charecter '"+ $UNEXPECTED_CHAR.text +"'");}
    ;

siddhi_app
    : (app_annotation|error)*
      (definition_stream|definition_table|definition_trigger|definition_function|definition_window|definition_aggregation|error) (';' (definition_stream|definition_table|definition_trigger|definition_function|definition_window|definition_aggregation|error))* (';' (execution_element|error))* ';'?
    ;

execution_element
    :query|partition
    ;

definition_stream_final
    : definition_stream ';'? EOF
    ;

definition_stream
    : annotation* DEFINE STREAM source '(' attribute_name attribute_type (',' attribute_name attribute_type )* ')'
    ;

definition_table_final
    :definition_table ';'? EOF
    ;

definition_table
    : annotation* DEFINE TABLE source '(' attribute_name attribute_type (',' attribute_name attribute_type )* ')'
    ;

definition_window_final
    :definition_window ';'? EOF
    ;

definition_window
    : annotation* DEFINE WINDOW source '(' attribute_name attribute_type (',' attribute_name attribute_type )* ')' function_operation ( OUTPUT output_event_type )?
    ;

store_query_final
    : store_query ';'? EOF
    ;

store_query
    : FROM store_input query_section?
    ;

store_input
    : source_id (AS alias)? (ON expression)? (within_time_range per)?
    ;

definition_function_final
    : definition_function ';'? EOF
    ;

definition_function
    : DEFINE FUNCTION function_name '[' language_name ']' RETURN attribute_type function_body
    ;

function_name
    : id
    ;

language_name
    : id
    ;

function_body
    : SCRIPT
    ;

definition_trigger_final
    : definition_trigger ';'? EOF
    ;

definition_trigger
    : DEFINE TRIGGER trigger_name AT (EVERY time_value | string_value )
    ;

trigger_name
    : id
    ;

definition_aggregation_final
    : definition_aggregation ';'? EOF
    ;

definition_aggregation
    : annotation* DEFINE AGGREGATION aggregation_name FROM standard_stream group_by_query_selection AGGREGATE (BY attribute_reference)? EVERY aggregation_time
    ;

aggregation_name
    : id
    ;

aggregation_time
    : aggregation_time_range
    | aggregation_time_interval
    ;

aggregation_time_duration
    : (SECONDS | MINUTES | HOURS | DAYS | WEEKS | MONTHS | YEARS)
    ;

aggregation_time_range
    : aggregation_time_duration TRIPLE_DOT aggregation_time_duration
    ;

aggregation_time_interval
    :  aggregation_time_duration (COMMA aggregation_time_duration)*
    ;

annotation
    : '@' name ('(' (annotation_element|annotation) (',' (annotation_element|annotation) )* ')' )?
    ;

app_annotation
    : '@' APP ':' name ('(' annotation_element (',' annotation_element )* ')' )?
    ;

annotation_element
    :(property_name '=')? property_value
    ;

partition
    : annotation* PARTITION WITH '('partition_with_stream (','partition_with_stream)* ')' BEGIN (query|error) (';' (query|error))* ';'? END
    ;

partition_final
    : partition ';'? EOF
    ;

partition_with_stream
    :attribute OF stream_id
    |condition_ranges OF stream_id 
    ;

condition_ranges
    :condition_range (OR condition_range)*
    ;

condition_range
    :expression AS string_value
    ;

query_final
    : query ';'? EOF
    ;

query
    : annotation* FROM query_input query_section? output_rate? query_output
    ;

query_input
    : (standard_stream|join_stream|pattern_stream|sequence_stream|anonymous_stream)
    ;

standard_stream
    : source pre_window_handlers=basic_source_stream_handlers? window? post_window_handlers=basic_source_stream_handlers?
    ;

join_stream
    :left_source=join_source (join right_source=join_source (right_unidirectional=UNIDIRECTIONAL)? | left_unidirectional=UNIDIRECTIONAL join right_source=join_source) (ON expression)? (within_time_range per)?
    ;

join_source
    :source basic_source_stream_handlers? window? (AS alias)?
    ;

pattern_stream
    : every_pattern_source_chain
    | absent_pattern_source_chain
    ;

every_pattern_source_chain
    : '('every_pattern_source_chain')' within_time? 
    | EVERY '('pattern_source_chain ')' within_time?   
    | every_pattern_source_chain  '->' every_pattern_source_chain
    | pattern_source_chain
    | EVERY pattern_source within_time? 
    ;

pattern_source_chain
    : '('pattern_source_chain')' within_time?
    | pattern_source_chain  '->' pattern_source_chain
    | pattern_source within_time?
    ;

absent_pattern_source_chain
    : EVERY? '('absent_pattern_source_chain')' within_time?
    | every_absent_pattern_source
    | left_absent_pattern_source
    | right_absent_pattern_source
    ;

left_absent_pattern_source
    : EVERY? '('left_absent_pattern_source')' within_time?
    | every_absent_pattern_source '->' every_pattern_source_chain
    | left_absent_pattern_source '->' left_absent_pattern_source
    | left_absent_pattern_source '->' every_absent_pattern_source
    | every_pattern_source_chain '->' left_absent_pattern_source
    ;

right_absent_pattern_source
    : EVERY? '('right_absent_pattern_source')' within_time?
    | every_pattern_source_chain '->' every_absent_pattern_source
    | right_absent_pattern_source '->' right_absent_pattern_source
    | every_absent_pattern_source '->' right_absent_pattern_source
    | right_absent_pattern_source '->' every_pattern_source_chain
    ;

pattern_source
    :logical_stateful_source|pattern_collection_stateful_source|standard_stateful_source|logical_absent_stateful_source
    ;

logical_stateful_source
    :standard_stateful_source AND standard_stateful_source
    |standard_stateful_source OR standard_stateful_source
    ;

logical_absent_stateful_source
    : '(' logical_absent_stateful_source ')'
    | standard_stateful_source AND NOT basic_source
    | NOT basic_source AND standard_stateful_source
    | standard_stateful_source AND basic_absent_pattern_source
    | basic_absent_pattern_source AND standard_stateful_source
    | basic_absent_pattern_source AND basic_absent_pattern_source
    | standard_stateful_source OR basic_absent_pattern_source
    | basic_absent_pattern_source OR standard_stateful_source
    | basic_absent_pattern_source OR basic_absent_pattern_source
    ;

every_absent_pattern_source
    : EVERY? basic_absent_pattern_source
    ;

basic_absent_pattern_source
    : NOT basic_source for_time
    ;

pattern_collection_stateful_source
    :standard_stateful_source '<' collect '>'
    ;

standard_stateful_source
    : (event '=')? basic_source
    ;

basic_source
    : source basic_source_stream_handlers?
    ;

basic_source_stream_handlers
    :(basic_source_stream_handler)+ 
    ;

basic_source_stream_handler
    : filter | stream_function
    ;

sequence_stream
    :every_sequence_source_chain
    |every_absent_sequence_source_chain
    ;

every_sequence_source_chain
    : EVERY? sequence_source  within_time?  ',' sequence_source_chain
    ;

every_absent_sequence_source_chain
    : EVERY? absent_sequence_source_chain  within_time? ',' sequence_source_chain
    | EVERY? sequence_source  within_time? ',' absent_sequence_source_chain
    ;

absent_sequence_source_chain
    : '('absent_sequence_source_chain')' within_time?
    | basic_absent_pattern_source
    | left_absent_sequence_source
    | right_absent_sequence_source
    ;

left_absent_sequence_source
    : '('left_absent_sequence_source')' within_time?
    | basic_absent_pattern_source ',' sequence_source_chain
    | left_absent_sequence_source ',' left_absent_sequence_source
    | left_absent_sequence_source ',' basic_absent_pattern_source
    | sequence_source_chain ',' left_absent_sequence_source
    ;

right_absent_sequence_source
    : '('right_absent_sequence_source')' within_time?
    | sequence_source_chain ',' basic_absent_pattern_source
    | right_absent_sequence_source ',' right_absent_sequence_source
    | basic_absent_pattern_source ',' right_absent_sequence_source
    | right_absent_sequence_source ',' sequence_source_chain
    ;

sequence_source_chain
    :'('sequence_source_chain ')' within_time? 
    | sequence_source_chain ',' sequence_source_chain
    | sequence_source  within_time? 
    ;

sequence_source
    :logical_stateful_source|sequence_collection_stateful_source|standard_stateful_source|logical_absent_stateful_source
    ;

sequence_collection_stateful_source
    :standard_stateful_source ('<' collect '>'|zero_or_more='*'|zero_or_one='?'|one_or_more='+') 
    ;

anonymous_stream
    : '('anonymous_stream')'
    | FROM query_input query_section? output_rate? RETURN output_event_type?
    ;

filter
    :'#'? '['expression']'
    ;

stream_function
    :'#' function_operation
    ;

window
    :'#' WINDOW '.' function_operation
    ;

group_by_query_selection
    : (SELECT ('*'| (output_attribute (',' output_attribute)* ))) group_by?
    ;

query_section
    : (SELECT ('*'| (output_attribute (',' output_attribute)* ))) group_by? having? order_by? limit?
    ;

group_by
    : GROUP BY attribute_reference (',' attribute_reference)*
    ;

having
    : HAVING expression
    ;

order_by
    : ORDER BY order_by_reference (',' order_by_reference )*
    ;

order_by_reference
    : attribute_reference order?
    ;

order
    : ASC | DESC
    ;

limit
    : LIMIT expression
    ;

query_output
    :INSERT output_event_type? INTO target
    |DELETE target (FOR output_event_type)? ON expression
    |UPDATE OR INSERT INTO target (FOR output_event_type)? set_clause? ON expression
    |UPDATE target (FOR output_event_type)? set_clause? ON expression
    |RETURN output_event_type?
    ;

set_clause
    : SET set_assignment (',' set_assignment)*
    ;

set_assignment
    : attribute_reference '=' expression
    ;

output_event_type
    : ALL EVENTS | EXPIRED EVENTS | CURRENT? EVENTS   
    ;

output_rate
    : OUTPUT output_rate_type? EVERY ( time_value | INT_LITERAL EVENTS )
    | OUTPUT SNAPSHOT EVERY time_value
    ;

output_rate_type
    : ALL
    | LAST
    | FIRST
    ;

for_time
    : FOR time_value
    ;

within_time
    :WITHIN time_value
    ;

within_time_range
    :WITHIN start_pattern=expression (',' end_pattern=expression)?
    ;

per :PER expression
    ;

output_attribute
    :attribute AS attribute_name
    |attribute_reference
    ;

attribute
    :math_operation
    ;

expression
    :math_operation
    ;


math_operation
    :'('math_operation')'                         #basic_math_operation
    |null_check                                   #basic_math_operation
    |NOT math_operation                           #not_math_operation
    |math_operation (multiply='*'|devide='/'|mod='%') math_operation    #multiplication_math_operation
    |math_operation (add='+'|substract='-') math_operation              #addition_math_operation
    |math_operation (gt_eq='>='|lt_eq='<='|gt='>'|lt='<') math_operation #greaterthan_lessthan_math_operation
    |math_operation (eq='=='|not_eq='!=') math_operation                #equality_math_operation
    |math_operation IN name                       #in_math_operation
    |math_operation AND math_operation            #and_math_operation
    |math_operation OR math_operation             #or_math_operation
    |function_operation                           #basic_math_operation
    |constant_value                               #basic_math_operation
    |attribute_reference                          #basic_math_operation
    ;

function_operation
    : (function_namespace ':')? function_id '(' attribute_list?  ')'
    ;

attribute_list
    :( attribute (',' attribute)* )  | '*'
    ;

null_check
    :( stream_reference  | attribute_reference | function_operation ) IS NULL
    ;

stream_reference
    :hash='#'? name ('['attribute_index']')?
    ;

attribute_reference
    : hash1='#'? name1=name ('['attribute_index1=attribute_index']')? (hash2='#' name2=name ('['attribute_index2=attribute_index']')?)? '.'  attribute_name
    | attribute_name
    ;

attribute_index
    : INT_LITERAL| LAST ('-' INT_LITERAL)?
    ;

function_id
    :name
    ;

function_namespace
    :name
    ;

stream_id
    :name
    ;

source_id
    :name
    ;

alias
    :name
    ;

property_name
    : name (property_separator name )*
    ;

attribute_name
    :name
    ;

type
    :name
    ;

property_value
    :string_value
    ;

property_separator
    : DOT | MINUS | COL
    ;

source
    :inner='#'? stream_id
    ;

target
    :source
    ;

event
    :name
    ;

name
    :id|keyword
    ;

collect
    : start=INT_LITERAL ':' end=INT_LITERAL
    | start=INT_LITERAL ':'
    | ':' end=INT_LITERAL
    | INT_LITERAL
    ;

attribute_type
    :STRING     
    |INT        
    |LONG       
    |FLOAT      
    |DOUBLE     
    |BOOL      
    |OBJECT     
    ;

join
    : LEFT OUTER JOIN
    | RIGHT OUTER JOIN
    | FULL OUTER JOIN
    | OUTER JOIN
    | INNER? JOIN
    ;


constant_value
    :bool_value
    |signed_double_value
    |signed_float_value
    |signed_long_value
    |signed_int_value
    |time_value
    |string_value
    ;

id: ID_QUOTES|ID ;

keyword
    : STREAM
    | DEFINE
    | TABLE
    | FROM
    | PARTITION
    | WINDOW
    | SELECT
    | GROUP
    | BY
    | ORDER
    | ASC
    | DESC
    | LIMIT
    | HAVING
    | INSERT
    | DELETE
    | UPDATE
    | RETURN
    | EVENTS
    | INTO
    | OUTPUT
    | EXPIRED
    | CURRENT
    | SNAPSHOT
    | FOR
    | RAW
    | OF
    | AS
    | OR
    | AND
    | ON
    | IS
    | NOT
    | WITHIN
    | WITH
    | BEGIN
    | END
    | NULL
    | EVERY
    | LAST
    | ALL
    | FIRST
    | JOIN
    | INNER
    | OUTER
    | RIGHT
    | LEFT
    | FULL
    | UNIDIRECTIONAL
    | YEARS
    | MONTHS
    | WEEKS
    | DAYS
    | HOURS
    | MINUTES
    | SECONDS
    | MILLISECONDS
    | FALSE
    | TRUE
    | STRING
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    | BOOL
    | OBJECT
    ;

time_value
    :  year_value  ( month_value)? ( week_value)? ( day_value)? ( hour_value)? ( minute_value)? ( second_value)?  ( millisecond_value)?
    |  month_value ( week_value)? ( day_value)? ( hour_value)? ( minute_value)? ( second_value)?  ( millisecond_value)?
    |  week_value ( day_value)? ( hour_value)? ( minute_value)? ( second_value)?  ( millisecond_value)?
    |  day_value ( hour_value)? ( minute_value)? ( second_value)?  ( millisecond_value)?
    |  hour_value ( minute_value)? ( second_value)?  ( millisecond_value)?       
    |  minute_value ( second_value)?  ( millisecond_value)?
    |  second_value ( millisecond_value)?
    |  millisecond_value
    ;

year_value 
    : INT_LITERAL YEARS
    ;

month_value
    : INT_LITERAL MONTHS
    ;

week_value
    : INT_LITERAL WEEKS
    ;

day_value
    : INT_LITERAL DAYS
    ;

hour_value
    : INT_LITERAL HOURS
    ;

minute_value
    : INT_LITERAL MINUTES
    ;

second_value
    : INT_LITERAL SECONDS
    ;

millisecond_value
    : INT_LITERAL MILLISECONDS
    ;

signed_double_value: ('-' |'+')? DOUBLE_LITERAL;
signed_long_value: ('-' |'+')? LONG_LITERAL;
signed_float_value: ('-' |'+')? FLOAT_LITERAL;
signed_int_value: ('-' |'+')? INT_LITERAL;
bool_value: TRUE|FALSE;
string_value: STRING_LITERAL;

INT_LITERAL 
    :  DIGIT+
    ;

LONG_LITERAL
    : DIGIT+ L
    ; 

FLOAT_LITERAL
    : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )? F
    | (DIGIT+)? '.' DIGIT+ ( E [-+]? DIGIT+ )? F
    ; 

DOUBLE_LITERAL
    : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )? D
    | DIGIT+ ( '.' DIGIT* )?  E [-+]? DIGIT+  D?
    | (DIGIT+)? '.' DIGIT+ ( E [-+]? DIGIT+ )? D?
    ; 

/*
ID_QUOTES : '`'('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*'`' {setText(getText().substring(1, getText().length()-1));};

ID_NO_QUOTES : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;


STRING_VAL
    :('\'' ( ~('\u0000'..'\u001f' | '\\' | '\''| '\"' ) )* '\'' 
    |'"' ( ~('\u0000'..'\u001f' | '\\'  |'\"') )* '"' )         {setText(getText().substring(1, getText().length()-1));}
    ;

*/

COL : ':';
SCOL : ';';
DOT : '.';
TRIPLE_DOT : '...';
OPEN_PAR : '(';
CLOSE_PAR : ')';
OPEN_SQUARE_BRACKETS : '[';
CLOSE_SQUARE_BRACKETS : ']';
COMMA : ',';
ASSIGN : '=';
STAR : '*';
PLUS : '+';
QUESTION: '?';
MINUS : '-';
DIV : '/';
MOD : '%';
LT : '<';
LT_EQ : '<=';
GT : '>';
GT_EQ : '>=';
EQ : '==';
NOT_EQ : '!=';
AT_SYMBOL: '@';
FOLLOWED_BY:'->';
HASH:'#';

STREAM:   S T R E A M;
DEFINE:   D E F I N E;
FUNCTION: F U N C T I O N;
TRIGGER:  T R I G G E R;
TABLE:    T A B L E;
APP:      A P P;
FROM:     F R O M;
PARTITION:    P A R T I T I O N; 
WINDOW:   W I N D O W;
SELECT:   S E L E C T;
GROUP:    G R O U P;
BY:       B Y;
ORDER:    O R D E R;
LIMIT:    L I M I T;
ASC:      A S C;
DESC:     D E S C;
HAVING:   H A V I N G;
INSERT:   I N S E R T;
DELETE:   D E L E T E;
UPDATE:   U P D A T E;
SET:      S E T;
RETURN:   R E T U R N;
EVENTS:   E V E N T S;
INTO:     I N T O;
OUTPUT:   O U T P U T;
EXPIRED:  E X P I R E D;
CURRENT:  C U R R E N T;
SNAPSHOT: S N A P S H O T;
FOR:      F O R;
RAW:      R A W;
OF:       O F;
AS:       A S;
AT:       A T;
OR:       O R;
AND:      A N D;
IN:       I N;
ON:       O N;
IS:       I S;
NOT:      N O T;
WITHIN:   W I T H I N;
WITH:     W I T H; 
BEGIN:    B E G I N;
END:      E N D;
NULL:     N U L L;
EVERY:    E V E R Y;
LAST:     L A S T;
ALL:      A L L;
FIRST:    F I R S T;
JOIN:     J O I N;
INNER:    I N N E R;
OUTER:    O U T E R;
RIGHT:    R I G H T;
LEFT:     L E F T;
FULL:     F U L L;
UNIDIRECTIONAL: U N I D I R E C T I O N A L;
YEARS:     Y E A R S?;
MONTHS:    M O N T H S?;
WEEKS:     W E E K S?;
DAYS:      D A Y S?;
HOURS:     H O U R S?;
MINUTES:   M I N (U T E S?)?;
SECONDS:  S E C (O N D S?)?;
MILLISECONDS: M I L L I S E C (O N D S?)?;
FALSE:    F A L S E;
TRUE:     T R U E;
STRING:   S T R I N G;
INT:  I N T;
LONG:     L O N G;
FLOAT:    F L O A T;
DOUBLE:   D O U B L E;
BOOL:     B O O L;
OBJECT:   O B J E C T;
AGGREGATION: A G G R E G A T I O N;
AGGREGATE: A G G R E G A T E;
PER:      P E R;

ID_QUOTES : '`'[a-zA-Z_] [a-zA-Z_0-9]*'`' {setText(getText().substring(1, getText().length()-1));};

ID : [a-zA-Z_] [a-zA-Z_0-9]* ;

STRING_LITERAL
    :(
        '\'' ( ~('\u0000'..'\u001f' | '\''| '\"' ) )* '\''
        |'"' ( ~('\u0000'..'\u001f'  |'\"') )* '"'
     )  {setText(getText().substring(1, getText().length()-1));}
     |('"""'(.*?)'"""')  {setText(getText().substring(3, getText().length()-3));}
    ;

//Hidden channels
SINGLE_LINE_COMMENT
 : '--' ~[\r\n]* -> channel(HIDDEN)
 ;

MULTILINE_COMMENT
 : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
 ;

SPACES
 : [ \u000B\t\r\n] -> channel(HIDDEN)
 ;

UNEXPECTED_CHAR
 : .
 ;

SCRIPT
 : '{' SCRIPT_ATOM* '}'
 ;

 fragment SCRIPT_ATOM
  : ~[{}]
  | '"' ~["]* '"'
  | '//' ~[\r\n]*
  | SCRIPT
  ;

fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

