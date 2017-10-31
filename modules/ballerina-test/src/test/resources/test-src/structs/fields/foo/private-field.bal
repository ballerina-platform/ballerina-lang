package foo;

public struct TestStruct {
    private string aString = "a";
    private int aInt = 1;
    private boolean aBoolean = true;
    private float aFloat = 1.1;
    private Child aChild;
}

struct Child {
    private string aValue = "value";
}

public struct TestAnonymousStruct {
    struct {
            string cValue = "c";
            private string bValue = "b";
    } publicStruct;
    private string aString = "z";
    private struct {
        private string aValue = "a";
    } privateStruct;
}

public function <TestStruct test> getDefault () (string v1, int v2, boolean v3, float v4, string v5) {
    v1 = test.aString;
    v2 = test.aInt;
    v3 = test.aBoolean;
    v4 = test.aFloat;
    v5 = test.aChild.aValue;
    return;
}

function test1 () (string v1, int v2, boolean v3, float v4, string v5) {
    TestStruct test = {aChild:{}};
    v1 = test.aString;
    v2 = test.aInt;
    v3 = test.aBoolean;
    v4 = test.aFloat;
    v5 = test.aChild.aValue;
    return;
}

function test2 () (string v1, int v2, boolean v3, float v4, string v5) {
    TestStruct test = {aString:"b", aBoolean:false, aFloat:1.2, aInt:2, aChild:{aValue:"new"}};
    v1 = test.aString;
    v2 = test.aInt;
    v3 = test.aBoolean;
    v4 = test.aFloat;
    v5 = test.aChild.aValue;
    return;
}

function test3 () (string v1, int v2, boolean v3, float v4, string v5) {
    TestStruct test = {aChild:{}};
    return test.getDefault();
}

function test4 () (string v1, string v2, string v3, string v4) {
    TestAnonymousStruct test = {privateStruct:{}, publicStruct:{}};
    v1 = test.aString;
    v2 = test.privateStruct.aValue;
    v3 = test.publicStruct.bValue;
    v4 = test.publicStruct.cValue;
    return;
}