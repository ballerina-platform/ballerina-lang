package samples.datamapping;

import ballerina.lang.message;
import ballerina.lang.json;

@Path ("/transform")
@Source (interface = "localhost")
@Service (title = "TransformService", description = "Transforms the inbound message")
service transformService {

	@POST
	resource transform (message m) {
	    json<Person> in = message:getPayload(m);
	    //here type converter kicks in
	    json<Driver> out  = (json<Driver>)in;
	    message:setPayload(m, out);
	    reply m;
	}
}
