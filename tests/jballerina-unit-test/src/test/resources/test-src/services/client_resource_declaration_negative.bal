string? stringNilArgument = ();

public function testClientDeclarationError() {
    var errorClient = client object {
        resource function get path/[int b]/foo(string name2) returns int {
            return 1;
        }

        resource function get path/[int a]/foo/[string a](string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string address) {

        }

        resource function get path/[int... a]/[int... a]() returns int {
            return 1;
        }

        resource function post path/[string? c]/foo(string name2) returns string? {
            return stringNilArgument;
        }

        resource function path() returns int {
            return 1;
        }

        resource function get recordPath/[record {int a;}]() returns int {
            return 1;
        }

        resource function get recordPath2/[record {int a;} a]() returns int {
            return 1;
        }

        resource function get xmlPath/[xml... a]() returns int {
            return 1;
        }

        resource function get xmlPath2/[xml a]() returns int {
            return 1;
        }

        resource function get xmlPath2/[xml]() returns int {
            return 1;
        }

        resource function get mapPath/[map<string>]() returns int {
            return 1;
        }

        resource function get mapPath2/[map<string> a]() returns int {
            return 1;
        }

        resource function get intOrErrorPath/[int|error]() returns int {
            return 1;
        }

        resource function get intOrErrorPath2/[int|error a]() returns int {
            return 1;
        }

        resource function get errorPath/[error]() returns int {
            return 1;
        }

        resource function get errorPath2/[error a]() returns int {
            return 1;
        }

    };
}
