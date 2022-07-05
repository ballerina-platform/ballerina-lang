import ballerina/module1;

service on new module1:Listener(9090) {
    isolated resource function get . () {
            int myInt = getInt();
    } 
}
