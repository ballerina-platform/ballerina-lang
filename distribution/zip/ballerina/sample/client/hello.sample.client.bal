package client;

function main (string[] args) {
    
    endpoint<helloBlockingStub> helloStubBlocking {
        create helloBlockingStub("localhost", 8080);
    }

    endpoint<helloNonBlockingStub> helloStubNonBlocking {
        create helloNonBlockingStub("localhost", 8080);
    }

}
