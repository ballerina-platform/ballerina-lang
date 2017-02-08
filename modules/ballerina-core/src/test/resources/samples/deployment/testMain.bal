import ballerina.lang.system;
import ballerina.lang.array;

function main(string[] args){
    echoEach(args);
}

function echoEach(string[] args){
    int i;

    i = 0;

	while(i < 10){
		i = i + 1;
	}
}

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
      reply m;
  }

}
