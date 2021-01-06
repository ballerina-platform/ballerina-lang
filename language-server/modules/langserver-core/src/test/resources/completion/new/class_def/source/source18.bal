import ballerina/module1;

readonly class testClassWithReadOnly {
    int & readonly field1 = 12;
    int field2 = 12;
}

class testClass {
    int field1 = 12;
}

type testObject object {
    int field1;
};

class testClass2 {
    *
}