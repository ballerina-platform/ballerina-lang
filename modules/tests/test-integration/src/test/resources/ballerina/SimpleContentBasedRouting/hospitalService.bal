import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.string;



@BasePath ("/echannel")
service EchannelService {

    @POST
    @Path ("/checkAvailability")
    resource availability (message m) {
	       http:HTTPConnector nawalokaChannel = new http:HTTPConnector("http://localhost:9090");
	       message response;
	       json payload;
	       json jsonMsg;
	       string hospitalName;
	       jsonMsg = message:getJsonPayload(m);
	       hospitalName=json:getString(jsonMsg, "$.CheckAvailability.hospital");	       
               
	      if ( string:equalsIgnoreCase(hospitalName, "nawaloka") ) {
             		response = http:HTTPConnector.post(nawalokaChannel, "/nawalokaChannel/checkAvailability", m);
        		reply response;
              } 
              else if (string:equalsIgnoreCase(hospitalName, "central")){
			response = http:HTTPConnector.post(nawalokaChannel, "/centralChannel/checkAvailability", m);
        		reply response;
	      }else {
            		payload = `{"Status":"Not Found."}`;
              }
	       
	       
        
   }
}

