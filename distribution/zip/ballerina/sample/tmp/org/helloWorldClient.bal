import ballerina.io;


function main (string[] args) {
    endpoint<helloWorldBlockingStub> helloServiceStub {
        create helloWorldBlockingStub("localhost", 9090);
    }

    io:println("------Event 2-----------");
    var res,ee = helloServiceStub.hello("Danesh");
    if (ee != null) {
        io:println("Error from Connector: " + ee.message);        
    } else {
    	io:println("Client Got Response : ");
    	io:println(res);
   }
}
