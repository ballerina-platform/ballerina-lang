import ballerina/lang.regexp;

// DataType<:Anydata
// Anydata<:DataType

type Anydata anydata;

type DataType ()|boolean|int|float|decimal|string|xml|regexp:RegExp|DataType[]|map<DataType>|table<map<DataType>>;
