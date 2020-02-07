type 'family\ \ \ person record {
    string 'first\ \ name;
    string 'last\ name;
    int 'current\ \ \ \ \ \ age;
    int 'masd\ sddd\ asd;
    string myLastName;
};

function passILValuesAsParams(string 'first\ name, string 'last\ name, int 'current\ age) returns [string, int] {
    string 'full\ name\ \ here = 'first\ name + " " + 'last\ name;
    return ['full\ name\ \ here, 'current\ age];
}

function useILInStructName() returns [string, string, int, string?] {
    'family\ person 'person\ one = {'first\ name: "Tom", 'last\ name: "hank", 'current\ age: 50};
    return ['person\ one.'first\ name, 'person\ one.'last\ name, 'person\ one.'current\ age, 'person\ one["first name"]];
}

function testUnicodeInIL() returns (string) {
    string 'සිංහල\ වචනය = "සිංහල වාක්‍යක්";
    return 'සිංහල\ වචනය;
}

function testAcessILWithoutPipe() returns [string, string] {
    string 'x = "hello";
    return ['x, x];
}
