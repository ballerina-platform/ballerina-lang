package bar;

import foo;

function test1 () (error) {
    try {
        foo:TestStruct test = {};
        _, _, _, _, _ = test.getDefault();
    } catch (error e) {
        return e;
    }
    return null;
}

function test2 () (string) {
    foo:TestAnonymousStruct test = {publicStruct:{}};
    return test.publicStruct.cValue;
}