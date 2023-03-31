import ballerina/module1;

service / on new module1:Listener(9090) {

    resource function get . () {
        string value = "";
    }
}
