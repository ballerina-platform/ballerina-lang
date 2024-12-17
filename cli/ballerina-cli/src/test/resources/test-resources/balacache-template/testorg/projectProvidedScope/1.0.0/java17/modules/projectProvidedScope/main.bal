import ballerina/jballerina.java;

public function hello() returns handle? = @java:Method {
    'class: "org.example.Hello"
} external;


public function helloCall() returns string {
    handle? msg = hello();
    return msg.toString();
}
public function main() {
}
