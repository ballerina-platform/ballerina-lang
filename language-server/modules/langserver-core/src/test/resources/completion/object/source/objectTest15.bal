import ballerina/http;

type TestAbstractObject abstract  object {
    public int field1;
};

type TestObject object {
    *http:
};