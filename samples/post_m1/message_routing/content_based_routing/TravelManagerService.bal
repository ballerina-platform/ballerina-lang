package samples.message_routing.content_based_routing;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.jsons;


@BasePath ("/travelmgr")
@Source (interface = "default_http_listener")
@Service(description = "Service to do content based routing of request between Hotel and Car-rental services")
service TravelManagerService {

    http:ClientConnector hotelEP = new http:ClientConnector("http://localhost:9090/hotel", {"timeOut" : 30000});
    http:ClientConnector carRentalEP = new http:ClientConnector("http://localhost:9090/carrental", {"timeOut" : 60000});

    @POST
    @Path ("/reservation")
    resource passthrough (message m) {
        message response;
        json jsonMsg;
        json errorMsg;
        jsonMsg = messages:getJsonPayload(m);
        try {
          if (jsons:get(jsonMsg, "$.TravelpediaReservation.reservationType") == "CAR-RENTAL") {
              response = http:ClientConnector.sendPost(hotelEP, m);
          } else {
              response = http:ClientConnector.sendPost(carRentalEP, m);
          }
        } catch (exception e) {
            errorMsg = `{"error" : "Error while sending to backend"}`;
            messages:setJsonPayload(response, errorMsg);
            messages:setHeader(response, "Status", strings:valueOf(500));
        }
        reply response;
    }
}
