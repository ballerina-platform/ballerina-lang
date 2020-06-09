import ballerina/lang.'xml;

const ASSERTION_ERROR_REASON = "AssertionError";

//------------ Testing a function with 'never' return type ---------

function functionWithNeverReturnType() returns never {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else {
        a = "c";
    }
}

function testTypeOfNeverReturnTypedFunction() {
    any|error expectedFunctionType = typedesc<function () returns (never)>;

    typedesc <any|error> actualFunctionType = typeof functionWithNeverReturnType;
    
    if (actualFunctionType is typedesc<function () returns (never)>) {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedFunctionType.toString() + "', found '" + actualFunctionType.toString () + "'");
}

function testNeverReturnTypedFunctionCall() {
    functionWithNeverReturnType();
}

//------------ Testing record type with 'never' field ---------

type InclusiveRecord record {
    int j;
    never p?;
}; 

type ExclusiveRecord record {|
    int j;
    never p?;
|}; 

function testInclusiveRecord() {
    InclusiveRecord inclusiveRecord = {j:0, "q":1};
}

function testExclusiveRecord() {
    ExclusiveRecord exclusiveRecord = {j:0};
}


//------------- Testing XML<never> -----------------

function testXMLWithNeverType() {
    xml<never> x = <xml<never>> 'xml:concat();  //generates an empty XML sequence and assign it to XML<never>
}


//---------------Test 'never' types with 'union-type' descriptors ------------
function testNeverWithUnionType1() {
    string|never j;
}

function testNeverWithUnionType2() {
    float|(int|never) j;
}

function testNeverWithUnionType3() {
    string|never j = "sample";
    string h = j;
}

// -------------Test 'never' with table key constraints --------------
type Person record {
  readonly string name;
  int age;
};

type PersonalTable table<Person> key<never>;

function testNeverWithKeyLessTable() {
    PersonalTable personalTable = table [
        { name: "DD", age: 33},
        { name: "XX", age: 34}
    ];
}

type SomePersonalTable table<Person> key<never|string>;

function testNeverInUnionTypedKeyConstraints() {
    SomePersonalTable somePersonalTable = table key(name) [
        { name: "MM", age: 33},
        { name: "PP", age: 34}
    ];
}

// --------------Test 'never' with 'future' type ----------------------

function testNeverAsFutureTypeParam() {
    future<never> someFuture;
}


// --------------Test 'never' with 'map' type ----------------------

function testNeverAsMappingTypeParam() {
    map<never> mp;
}
