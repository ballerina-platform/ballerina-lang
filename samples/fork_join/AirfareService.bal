package samples.fork_join;

import ballerina.lang.message;
import ballerina.net.http;

@BasePath ("/AirfareProviderService")
@Service(description = "Airfare service")
service AirfareProviderService {

    http:HttpConnector abcAirlineEP = new http:HttpConnector("http://localhost:8080/ABCAirline");
    http:HttpConnector xyzAirlineEP = new http:HttpConnector("http://localhost:8080/XYZAirline");

    @POST
    @Path ("/airfare")
    resource airfareResource (message m) {
        log:info("Airfare ");
        fork (message m) {
            worker ABC_Airline (message m) {
                xml payload = message:getXmlPayload(msg);
                string from = xml:get(payload, 'reservationInfo/from');
                string to = xml:get(payload, 'reservationInfo/to');
                string date = xml:get(payload, 'reservationInfo/date');
                string query = "?departure_city=" + from + "&destination_city=" + to + "&date=" + date;
                response = http:HttpConnector.sendGet (abcAirlineEP, query, m);
                reply response;
            }

            worker XYZ_Airline (message m) {
                xml payload = message:getXmlPayload(msg);
                string from = xml:get(payload, 'reservationInfo/from');
                string to = xml:get(payload, 'reservationInfo/to');
                string date = xml:get(payload, 'reservationInfo/date');
                string query = "?From=" + from + "&To=" + to + "&Date=" + date;
                response = http:HttpConnector.sendGet (xyzAirlineEP, query, m);
                reply response;
            }
        } join (all) (message[] airfareResponses) {
            xmlElement airfareAggregatedResponse = `<airfareRes></airfareRes>`;
            xml:appendChild(airfareAggregatedResponse, "/airfareRes", null, airfareResponses[0]);
            xml:appendChild(airfareAggregatedResponse, "/airfareRes", null, airfareResponses[1]);

            reply airfareAggregatedResponse;
        }
   }

}
