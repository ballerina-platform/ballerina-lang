const string stringConstant = "String Constant";
const int intConstant = 1;

var varString = "String var";
var varInt = 1;
var varBoolean = true;

string[] stringPathParameters = ["foo", "bar"];
string[] stringPathParameters2 = ["foo", "bar", "foo2"];
string[] stringPathParameters3 = ["foo"];
int[] intPathParameters = [1, 2, 3];

int? intNilArgument = ();
() nilArgument = ();
int? intOrNilArgument = 3;

record {int a;} recordValue = {a: 3};

public function testUndefineResourcePath() {
    var successClient = client object {
        resource function get path() returns int {
            return 1;
        }

        resource function get path/[int a]() returns int {
            return 1;
        }

        resource function get path/[int a]/foo(string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string address) returns int {
            return 1;
        }

        resource function get path/foo/bar() {

        }

        resource function get stringPath/[string]() returns int {
            return 1;
        }

        resource function get intQuotedPath/'5() returns int {
            return 1;
        }

        resource function get intPath/[int]() returns int {
            return 1;
        }
    };

    int result = successClient->/path2.post();
    result = successClient->/path.put();
    result = successClient->/path/[...intPathParameters];
    result = successClient->/path/[...stringPathParameters3]/bar;
    result = successClient->/path/[stringPathParameters];
    result = successClient->/path/[...stringPathParameters];
    result = successClient->/path/[...stringPathParameters2];
    result = successClient->/path/["abc"];
    result = successClient->/path/[varString];
    result = successClient->/path/[varBoolean];
    result = successClient->/path/[stringConstant];
    result = successClient->/stringPath/[recordValue.a];
    result = successClient->/stringPath/[intConstant];
    result = successClient->/stringPath/[varBoolean];
    result = successClient->/stringPath/[varInt];
    result = successClient->/intQuotedPath/[5];
    result = successClient->/intPath/'5;
    result = successClient->/intPath/foo/foo2/bar/bar2/foo3;
    result = successClient->/path/[intNilArgument];
    result = successClient->/path/[nilArgument];
    result = successClient->/path/[getIntOrNilArgument()];
}

function getIntOrNilArgument() returns int? {
    return intOrNilArgument;
}
