import org/test;
import ballerina/http;

service<http:Service> hello bind { port: 9090 }  {
    sayHello(endpoint caller, http:Request req) {
        test:Name name = {firstName:""};
        test:Person person = {/*ref*/name:name};
    }
}
