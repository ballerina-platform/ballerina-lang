import ballerina.lang.system;
import ballerina.lang.array;

function main(string[] args){
    system:println("Starting Main.");
    echoEach(args);
    system:print("Exiting Main.");
}

function echoEach(string[] args){
    int l;
    int position;

    position = 0;
    l = array:length(args);

    if(l != 0){
        while(position < l){
            system:println(args[position]);
            position = position + 1;
        }
    }
}

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
      ballerina.lang.system:log(3, "Echo Invoked.");
      reply m;
  }

}
