import ballerina/module1;

public module1:Listener ln = new(9090);

service on ln {

}
