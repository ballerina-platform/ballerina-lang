package samples.passthrough;

import ballerina.net.http;

@BasePath ("/PassthroughService")
@Service(title = "NYSEService", description = "NYSE service")
service PassthroughService;


@GET
@POST
@Path("/stocks")
resource echoResource (message m) {
    actor HttpEndpoint nyse_ep =  new HttpEndpoint ("http://localhost:6060/nyse");
    var message r = http.sendPost (nyse_ep, m, "/*");
    reply r;
}

