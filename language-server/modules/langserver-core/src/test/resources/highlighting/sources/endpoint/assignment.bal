import ballerina/http;

public function main(){
    http:Client myClient = new("");
    http:Client myClient2 = new("");

    myClient2 = myClient;
}
