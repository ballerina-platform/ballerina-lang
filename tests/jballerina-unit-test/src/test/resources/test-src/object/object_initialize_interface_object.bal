
function test () returns int {
    Person p = new();
    return 1;
}

type Person abstract object {
    public int age = 0;

    function test(int a, string n) returns string;
};
