import ballerina/lang.'int;

function parse(string num) returns int|error {
   return 'int:fromString(num);
}
function foo() {
    int    y    =   checkpanic    parse("120");
     int |  error   result =  trap   parse  ( "test"  ) ;
}
