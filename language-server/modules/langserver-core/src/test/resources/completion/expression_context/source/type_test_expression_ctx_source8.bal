import ballerina/module1;

type Type1 string | int;

type Type2 string;

type Type3 record {|
      int a;
|};

type Type4 record {||};

type Error error;

public function main() {
    module1:TestMap2 | module1:TestMap3 | int | Type1 | Error | Type3 myVar = 10;
    if myVar is 
}
