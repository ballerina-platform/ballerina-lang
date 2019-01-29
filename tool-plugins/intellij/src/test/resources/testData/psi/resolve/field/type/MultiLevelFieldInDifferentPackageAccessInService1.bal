import org/test;
import ballerina/http;

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        test:Name name = {firstName:""};
        test:Person person = {/*ref*/name:name};
    }
}
