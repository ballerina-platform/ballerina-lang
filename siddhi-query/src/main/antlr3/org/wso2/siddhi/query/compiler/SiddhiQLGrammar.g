grammar SiddhiQLGrammar;

options {
  language = Java;
  backtrack=true;
  output    = AST;
  ASTLabelType=CommonTree;

}

tokens {
  COLLECT;
  REGEX;
  HANDLERS;
  CONDITION;
  CONDITION_FUNCTION;
  EXTENSION_FUNCTION;
  INSERT_INTO_STREAM;
  DELETE_STREAM;
  UPDATE_STREAM;
  OUT_STREAM;
  OUTPUT;
  OUT_ATTRIBUTES;
  OUT_ATTRIBUTE;
  SEQUENCE;
  PATTERN;
  JOIN; 
  STREAM;
  STREAM_DEFINITION;
  TABLE_DEFINITION;
  TABLE;
  QUERY;
  FUNCTION;
  PARAMETERS;
  ATTRIBUTE;
  IN_ATTRIBUTE;
  CONSTANT;
  ANONYMOUS;
  RETURN_QUERY;
  PATTERN_FULL;
  SEQUENCE_FULL;
  WINDOW;
  SIGNED_VAL;
  TIME_EXP;
  TRANSFORM;
  EXTENSION;
  FILTER;
  EXTENSION_FUNCTION;
  PARTITION_DEFINITION;
  PARTITION;
  PARTITION_TYPE;
  LAST;
  SNAPSHOT;
  TABLE_PARAMETER;
  YEAR;
  MONTH;
  WEEK;
  DAY;
  HOUR;
  MIN;
  SEC;
  MILLI_SEC;

}

@header {
	package org.wso2.siddhi.query.compiler;
	import java.util.LinkedList;
	import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

}

@lexer::header {
	package org.wso2.siddhi.query.compiler;
	import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

}


@parser::members {
  @Override
  public void emitErrorMessage(String errorMessage) {
    throw new SiddhiParserException(errorMessage);
  }
}

@lexer::members {
  @Override
   public void emitErrorMessage(String errorMessage) {
     throw new SiddhiParserException(errorMessage);
   }
}

executionPlan
	:(definitionPartition|definitionStream|definitionTable|query) (';' (definitionPartition| definitionStream|definitionTable|query))* ';'? EOF  -> (^(PARTITION_DEFINITION definitionPartition))* (^(STREAM_DEFINITION definitionStream))*  (^(TABLE_DEFINITION definitionTable))*  ( query)*
	; 

definitionStreamFinal
    : definitionStream ';'? EOF -> definitionStream
    ;

definitionStream 
	:'define' 'stream' streamId '(' attributeName type (',' attributeName type )* ')'  ->  ^(streamId (^(IN_ATTRIBUTE attributeName type))+)
	;

definitionPartitionFinal
   	:definitionPartition ';'? EOF -> definitionPartition
   	;


definitionPartition
	:'define' 'partition' partitionId 'by' partitionType (',' partitionType)*  -> ^(partitionId (^(PARTITION_TYPE partitionType))+)
	;
	
partitionType
        : streamAttributeName
        | attributeName
        | 'range' condition 'as' stringVal -> condition stringVal
        ;

definitionTableFinal
    :definitionTable ';'? EOF -> definitionTable
    ;

definitionTable 
	:'define' 'table' id '(' attributeName type (',' attributeName type )* ')' ('from' '(' tableParamName '=' tableParamValue (',' tableParamName '=' tableParamValue )* ')' )?  ->  ^(id (^(IN_ATTRIBUTE attributeName type))+ ^(TABLE (^(TABLE_PARAMETER tableParamName tableParamValue))+ )? )
	;

queryFinal
    :query ';'? EOF -> query
    ;

query
	:inputStream outputSelection output? outputStream partition? ->  ^(QUERY inputStream outputSelection outputStream output? partition?)
	|inputStream outputSelection output? 'return'? partition? ->  ^(QUERY inputStream outputSelection output? partition? )
	;

outputStream
	:'insert' 'into' streamId outputTypeCondition? -> ^(INSERT_INTO_STREAM streamId outputTypeCondition?)
	|'delete' id outputTypeCondition? ('on' condition)?  -> ^(DELETE_STREAM id outputTypeCondition? condition?)
	|'update' id outputTypeCondition? ('on' condition)?  -> ^(UPDATE_STREAM id outputTypeCondition? condition?)
	;

outputTypeCondition
	: 'for'! 'expired-events' | 'for'! 'current-events' | 'for'! 'all-events'
	;

inputStream
	:'from' ( sequenceFullStream -> ^(SEQUENCE_FULL sequenceFullStream)
		| patternFullStream ->  ^(PATTERN_FULL  patternFullStream )
		| joinStream -> ^(JOIN joinStream) 
		| windowStream -> windowStream
		| basicStream  -> basicStream
		)
	;  

output
	: 'output' outputType? 'every' ( timeExpr   ->  ^(OUTPUT timeExpr outputType? )
	                                | POSITIVE_INT_VAL 'events' ->  ^(OUTPUT POSITIVE_INT_VAL outputType? )
	)
	| 'output' 'snapshot' 'every' timeExpr   ->  ^(OUTPUT timeExpr SNAPSHOT)
	;

outputType
	: 'all'
	| 'last'
	| 'first'
	;

partition
	:'partition' 'by' partitionId -> ^(PARTITION partitionId)
	;
			 
patternFullStream
	:'(' patternStream ')' ('within' time)? ->  ^(PATTERN  patternStream  time? ) 
	|patternStream  ('within' time)?  ->  ^(PATTERN  patternStream  time? ) 
	;
 
basicStream
	: rawStream transformHandler? ('as' id)? -> ^(STREAM rawStream transformHandler?  id?)
	;

windowStream
	: streamId  filterHandler? transformHandler? windowHandler ('as' id)?-> ^(STREAM ^(streamId  filterHandler? transformHandler?  windowHandler) id?)
	| '(' returnQuery ')'  filterHandler? transformHandler? windowHandler ('as' id)?  ->  ^(STREAM ^( ANONYMOUS returnQuery   filterHandler? transformHandler?  windowHandler) id?)
	;

rawStream
	: streamId  filterHandler?   -> ^(streamId   filterHandler?  )
	| '(' returnQuery ')'  filterHandler?  ->  ^( ANONYMOUS returnQuery filterHandler? )
	;

joinStream
	:leftStream join rightStream 'unidirectional' ('on' condition)? ('within' time)? -> leftStream  join rightStream 'unidirectional' condition? time?
	|leftStream join rightStream ('on' condition)? ('within' time)? ->  leftStream  join rightStream condition? time?
	|leftStream 'unidirectional' join rightStream ('on' condition)? ('within' time)? -> leftStream 'unidirectional'  join  rightStream condition? time?
	;

leftStream
    :  windowStream
    | basicStream
    ;

rightStream
    :  windowStream
    |  basicStream
    ;

returnQuery
	: inputStream outputSelection 'return' -> ^(RETURN_QUERY  inputStream outputSelection)
	;

patternStream
	: patternItem ( FOLLOWED_BY patternStream )?  ->   patternItem patternStream?
	| 'every' patternItem ( FOLLOWED_BY patternStream )?  ->  ^( 'every'  patternItem ) patternStream?
	| 'every' '('nonEveryPatternStream')' ( FOLLOWED_BY patternStream )? -> ^( 'every' nonEveryPatternStream )   patternStream?
	;

nonEveryPatternStream
	: patternItem  ( FOLLOWED_BY nonEveryPatternStream )?  ->  patternItem nonEveryPatternStream?
	;

sequenceFullStream
	:sequenceStream ('within' time)? ->  ^(SEQUENCE  sequenceStream time? )
	;

sequenceStream
	: sequenceItem ',' sequenceItem  (',' sequenceItem )*   ->  sequenceItem+
	;

FOLLOWED_BY
	: '->'/*|'-['countEnd']>'*/
	;

patternItem
	: itemStream 'and'^ itemStream
	| itemStream 'or'^ itemStream
	| itemStream '<'collect '>' -> ^(COLLECT itemStream collect)
	| itemStream
	;

sequenceItem
	: itemStream 'or'^ itemStream
	| itemStream regex -> ^(REGEX itemStream regex)
	| itemStream
	;

itemStream
	: attributeName'='rawStream  ->   ^(STREAM rawStream attributeName?)
	;

regex
	: ('*'|'+'|'?') '?'?
	;

outputSelection
	:  outputAttributeList groupBy? having? ->  ^(OUT_ATTRIBUTES outputAttributeList ) groupBy? having?
	;

outputAttributeList
	: 'select' '*' -> '*'
	| 'select' outputItem (',' outputItem)* ->( ^(OUT_ATTRIBUTE outputItem))+
	|-> '*'
	;

outputItem
	: extensionOutFunction 'as' id ->   id extensionOutFunction
	| outFunction 'as' id ->  outFunction id
	| expression  'as' id  ->   expression id
	| attributeVariable
	;

extensionOutFunction
	: extensionId ':' functionId  '(' parameters? ')' -> ^( EXTENSION_FUNCTION extensionId functionId parameters?)
	;

outFunction
	: ID '(' parameters? ')' -> ^( FUNCTION ID parameters?)
	;

groupBy
	: 'group' 'by' attributeVariable (',' attributeVariable)*  ->   ^('group' attributeVariable+)
	;

having
	: 'having' condition  -> ^('having' condition)
	;

filterHandler
	: '[' condition ']'  ->    ^( FILTER condition)
	;

transformHandler
	: '#' transform '.' extensibleId  ('(' parameters? ')')?  ->   ^( TRANSFORM extensibleId parameters?)
	;

windowHandler
	: '#' window  '.' extensibleId  ('(' parameters? ')')?  ->   ^( WINDOW extensibleId parameters?)
	;

extensibleId
	: extensionId ':' functionId ->  ^( EXTENSION  extensionId functionId)
	| id
	;

parameters
	: parameter (',' parameter)*  ->  ^(PARAMETERS parameter+)
	;

time
	: constant
	;
	
parameter
	: expression
	;

collect
	: countStart ':' countEnd
	| countStart ':'
	| ':' countEnd
	| countStartAndEnd
	;

countStart :POSITIVE_INT_VAL;

countEnd :POSITIVE_INT_VAL;

countStartAndEnd :POSITIVE_INT_VAL;

//conditions start

condition
	:conditionExpression  -> ^(CONDITION conditionExpression)
	;

conditionExpression
   	: andCondition ('or'^ conditionExpression )?
	;

andCondition
	: inCondition ('and'^ conditionExpression)?
	;

inCondition	
   	: compareCondition ('in'^ streamId )?
	;

compareCondition
	:expression compareOperation^ expression
	|boolVal
    |'('conditionExpression ')' -> conditionExpression
    |notCondition
    |extensionCondition
    |functionCondition
	;

expression
   	:minusExpression ('+'^ expression)?
   	;

minusExpression
   	:multiplyExpression ('-'^ minusExpression)?
   	;

multiplyExpression
   	:divisionExpression ('*'^ multiplyExpression)?
   	;

divisionExpression
   	:modExpression ('/'^ divisionExpression)?
   	;

modExpression
    :valueExpression ('%'^ modExpression)?
    ;

valueExpression
    : constant
    | attributeVariable
    | type
    | '('expression ')' -> expression
    | extensionExpression
    | functionExpression
    ;

notCondition
	:'not' '('conditionExpression')' ->  ^('not' conditionExpression)
	|'not' conditionExpression ->  ^('not' conditionExpression)
	;

extensionCondition
    :extensionId ':' functionId  ('(' parameters? ')')?  ->   ^( CONDITION_FUNCTION functionId extensionId parameters?)
    ;

functionCondition
    :functionId  ('(' parameters? ')')?  ->   ^( CONDITION_FUNCTION functionId parameters?)
    ;

extensionExpression
    :extensionId ':' functionId  ('(' parameters? ')')?  ->   ^( EXTENSION_FUNCTION functionId extensionId parameters?)
    ;

functionExpression
    :functionId  ('(' parameters? ')')?  ->   ^( EXTENSION_FUNCTION functionId parameters?)
    ;

//conditions end

constant
	:intVal -> ^( CONSTANT intVal)
	|longVal -> ^( CONSTANT longVal)
	|floatVal  -> ^( CONSTANT floatVal)
	|doubleVal -> ^( CONSTANT doubleVal)
	|boolVal -> ^( CONSTANT boolVal)
	|stringVal -> ^( CONSTANT stringVal)
	|timeExpr   -> ^( CONSTANT timeExpr)
	;

partitionId: id;

streamId: id;

attributeVariable
	:streamPositionAttributeName|streamAttributeName|attributeName;

streamPositionAttributeName
	:streamId '['POSITIVE_INT_VAL']''.' id ->  ^( ATTRIBUTE ^(streamId POSITIVE_INT_VAL) id)
	|streamId '[' 'last' ']''.' id ->  ^( ATTRIBUTE ^(streamId LAST) id)
	;

streamAttributeName
	: streamId '.' id  ->  ^( ATTRIBUTE streamId id)
	;

attributeName
	: id  ->  ^( ATTRIBUTE id)
	;

join
	: 'left''outer' 'join' ->  ^('join' ^('outer' 'left'))
	| 'right' 'outer' 'join' -> ^('join' ^('outer' 'right'))
	| 'full''outer' 'join' -> ^('join' ^('outer' 'full'))
	| 'outer' 'join'  -> ^('join' ^('outer' 'full'))
	| 'inner' 'join'  -> ^('join' 'inner')
	| 'join' -> ^('join' 'inner')
	;

window
    : 'window'
    ;

transform
    : 'transform'
    ;

compareOperation
	:'==' |'!=' |'<='|'>=' |'<' |'>'  |'contains' | 'instanceof'
	;

id: ID|ID_QUOTES ;

timeExpr
    : (yearValue)? (monthValue)? (weekValue)? (dayValue)? (hourValue)? (minuteValue)? (secondValue)?  (milliSecondValue)?
	-> ^(TIME_EXP yearValue? monthValue? weekValue? dayValue? hourValue? minuteValue? secondValue? milliSecondValue?  )
	;

yearValue
	: POSITIVE_INT_VAL ( years | year)  ->  ^(YEAR POSITIVE_INT_VAL)
	;

monthValue
	: POSITIVE_INT_VAL ( months | month)  ->   ^(MONTH POSITIVE_INT_VAL)
	;

weekValue
	: POSITIVE_INT_VAL ( weeks | week) ->   ^(WEEK POSITIVE_INT_VAL)
	;

dayValue
	: POSITIVE_INT_VAL ( days | day)  ->   ^(DAY POSITIVE_INT_VAL)
	;

hourValue
	: POSITIVE_INT_VAL ( hours |   hour ) ->   ^(HOUR POSITIVE_INT_VAL)
	;

minuteValue
	: POSITIVE_INT_VAL ( minutes |  min  | minute  ) ->   ^(MIN POSITIVE_INT_VAL)
	;

secondValue
	: POSITIVE_INT_VAL (seconds | second | sec  )  ->   ^(SEC POSITIVE_INT_VAL)
	;

milliSecondValue
	: POSITIVE_INT_VAL ( milliseconds |  millisecond  )  ->   ^(MILLI_SEC POSITIVE_INT_VAL)
	;

intVal: '-'? POSITIVE_INT_VAL -> ^(SIGNED_VAL  POSITIVE_INT_VAL '-'?);

longVal: '-'? POSITIVE_LONG_VAL -> ^(SIGNED_VAL  POSITIVE_LONG_VAL '-'?);

floatVal: '-'? POSITIVE_FLOAT_VAL -> ^(SIGNED_VAL POSITIVE_FLOAT_VAL  '-'?);

doubleVal: '-'? POSITIVE_DOUBLE_VAL -> ^(SIGNED_VAL  POSITIVE_DOUBLE_VAL '-'?);

boolVal: BOOL_VAL;

extensionId: id;

functionId: id;

tableType: id;

databaseName: id;

tableName: id;

dataSourceName : id;

stringVal: STRING_VAL;

tableParamName : stringVal;

tableParamValue : stringVal;

type: 'string' |'int' |'long' |'float' |'double' |'bool';

POSITIVE_INT_VAL:  NUM('I'|'i')?;

POSITIVE_LONG_VAL:  NUM ('L'|'l');

POSITIVE_FLOAT_VAL:  NUM ('.' NUM)? NUM_SCI? ('F'|'f');

POSITIVE_DOUBLE_VAL : NUM ('.' NUM NUM_SCI? ('D'|'d')?| NUM_SCI? ('D'|'d'));

BOOL_VAL: ('true'|'false');

//Need to be in the top to get high priority
ID_QUOTES : '`'('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*'`' {setText(getText().substring(1, getText().length()-1));};

ID : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;

//('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-'|','|' '|'\t')*

years :  {input.LT(1).getText().equals("years")}? ID ;

year :  {input.LT(1).getText().equals("year")}? ID ;

months :  {input.LT(1).getText().equals("months")}? ID ;

month :  {input.LT(1).getText().equals("month")}? ID ;

weeks :  {input.LT(1).getText().equals("weeks")}? ID ;

week :  {input.LT(1).getText().equals("week")}? ID ;

days :  {input.LT(1).getText().equals("days")}? ID ;

day :  {input.LT(1).getText().equals("day")}? ID ;

hours :  {input.LT(1).getText().equals("hours")}? ID ;

hour :  {input.LT(1).getText().equals("hour")}? ID ;

minutes :  {input.LT(1).getText().equals("minutes")}? ID ;

min :  {input.LT(1).getText().equals("min")}? ID ;

minute :  {input.LT(1).getText().equals("minute")}? ID ;

seconds :  {input.LT(1).getText().equals("seconds")}? ID ;

second :  {input.LT(1).getText().equals("second")}? ID ;

sec :  {input.LT(1).getText().equals("sec")}? ID ;

milliseconds :  {input.LT(1).getText().equals("milliseconds")}? ID ;

millisecond :  {input.LT(1).getText().equals("millisecond")}? ID ;



STRING_VAL
	:'\'' ( ~('\u0000'..'\u001f' | '\''| '\"' ) )* '\'' {setText(getText().substring(1, getText().length()-1));}
	|'"' ( ~('\u0000'..'\u001f'  |'\"') )* '"'          {setText(getText().substring(1, getText().length()-1));}
	;	

fragment NUM: '0'..'9'+;

fragment NUM_SCI: ('e'|'E') '-'? NUM;

//Hidden channels 

WS  : (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;
COMMENT
    : '/*' .* '*/' {$channel=HIDDEN;}
    ;
LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
