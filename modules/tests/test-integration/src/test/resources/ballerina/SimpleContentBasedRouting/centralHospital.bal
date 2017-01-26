import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.json;


@BasePath ("/centralChannel")
service CentralEchannelService {

    @POST
    @Path ("/checkAvailability")
    resource availability (message m) {
	       message response;
	       json payload;
	       json incomingMsg;
	         
	  	payload = `{"AvailabilityDetails": {"ID": "456", "Name": "TestDoctorCentral","Speclization": "test"}}`;
		message:setJsonPayload(response, payload);
        	reply response;
        
   }
}

