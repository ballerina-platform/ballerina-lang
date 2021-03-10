import ballerina/module1;

service on new module1:Listener(9090) {
    int field1;
    resource function testResource . (module1:RequestMessage req) {
        // Test the class field filter
        self.field1.
    }
};
