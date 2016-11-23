package samples.passthrough;

@BasePath ("/PassthroughService")
@Service(title = "NYSEService", description = "NYSE service")

service PassthroughService;

import ballerina.net.http;

@GET
@POST
@Path("/stocks")
resource echoResource (message m) {
    actor HttpEndpoint nyse_ep =  new HttpEndpoint ("http://localhost:6060/nyse");
    message r = sendPost (nyse_ep, m, "/*");
    reply r;
}

