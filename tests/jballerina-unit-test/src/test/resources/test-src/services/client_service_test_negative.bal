public function testClientDeclaration() {
    var a = client object {
        resource function get path() returns int {
            return 3;
        }

        resource function get path2/[int a]() returns int {
            return 3;
        }
    };

    string result = a->/path;
    int result2 = a->/path/[1];
    int result3 = a->/path2/["abc"];
}
