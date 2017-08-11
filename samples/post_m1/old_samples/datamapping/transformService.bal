package samples.datamapping;

import ballerina.lang.messages;
import ballerina.lang.jsons;

@Path ("/transform")
@Source (interface = "default_http_listener")
@Service (title = "TransformService", description = "Transforms the inbound message")
service transformService {

	@POST
	resource transform (message m) {
	    json<Person> in;
	    json<Driver> out;
	    in = m:getPayload(m);
	    //here type mapper kicks in
	    out  = (json<Driver>)in;
	    m:setPayload(out);
	    reply m;
	}
}
