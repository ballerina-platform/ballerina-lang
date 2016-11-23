package samples.echo;

@BasePath ("/echo")
@Source (interface="protocolDefinitionKey")?
@Service(
   	   tags = {"tag1", "tag2"},
   description = "Description" 
  )?
@Description "description"?
service EchoService;

@GET
@Path ("/*")
resource echoResource (message m) {
    reply m;
}

