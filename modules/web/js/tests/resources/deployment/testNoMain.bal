import ballerina.lang.system;
import ballerina.lang.array;

function echoEach(string[] args){
    int l;
    int position;

    position = 0;
    l = array:length(args);

    if(l != 0){
        while(position < l){
            system:log(3, args[position]);
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
