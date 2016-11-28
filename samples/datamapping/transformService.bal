package samples.datamapping;

import ballerina.lang.message;
import ballerina.lang.json;

@Path ("/transform")
@Source (interface = "localhost")
@Service (title = "TransformService", description = "Transforms the inbound message")
service transformService {

	@POST
	resource transform (message m) {
		json in = m.getPayload(m);
	  json out  = mapPerson2Driver(in);
	  m.setPayload(out);
	  reply m;
	}
}
