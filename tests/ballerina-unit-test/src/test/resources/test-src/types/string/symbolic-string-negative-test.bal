type Student record {
    string name;
    int age;
};

function testWithSpaces() returns (string) {
    string var1 = 'Hello World;
    string var2 = 'Hello$;
    string var3 = 'U+001E;
    string var4 = 'Hello\nWorld;
    string var5 = 'Hello\uFFFEWorld;
    // Maps
    map addrMap = { road: 'Mount Lavinia, country: 'SriLanka };
    addrMap['country sl] = 'PO00300;
    // Records
    Student stu = { name: 'Adam Page, age: 17};
    return stu['full$name];
    // Arrays
    string [] stringArr = ['hello, 'hello world];
    // Json
    json p = { name: 'John\uFFFFStallone };
}
