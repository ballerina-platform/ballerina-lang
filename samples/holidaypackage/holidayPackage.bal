package samples.holidaypackage;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system.log;

@BasePath ("/holiday")
@Source (interface="x")
@Service (tags = {"Holiday_details", "Holiday_packages"} description = "Travelpedia flight, hotel and car rental services")
service HolidayPackageService;

actor HttpEndpoint airlineEP = new HttpEndpoint (..);
actor HttpEndpoint carRentalEP = new HttpEndpoint (..);
actor HttpEndpoint hotelEP = new HttpEndpoint (..);

@GET
@Path ("/flights?depart={dateDepart}&return={dateReturn}&from={from}&to={to}")
resource flights (message m) {
    //TODO: message c = datamap(messageRef = m, mappingConfig = "getFlightsMapping.js");
    var message flightRequest = m;
    var message response;
    try {
        response = http.sendPost(airlineEP, flightRequest);
    } catch (exception e) {
        var json errorMsg = `{"error" : "Error while getting flight details."}`;
        message.setPayload(response, errorMsg);
        message.setHeader(response, "Status", 500);
    }
    reply response;
}

@GET
@Path ("/hotels?from={dateFrom}&to={dateTo}&location={location}")
resource hotels (message m) {
    var message response;
    var message hotelRequest;
    try {
        xmlElement<{"http://example.com/xsd/HolidayPackage"}Request> requestJSON = message.getPayload(m);
        xmlElement<{"http://example.com/xsd/HolidayPackage"}HotelRequest> hotelRequestJSON;
        hotelRequestJSON = requestJSON;
        message.setPayload(hotelRequest, hotelRequestJSON);
        response = http.sendPost(hotelEP, hotelRequest);
    } catch (exception e) {
        var json errorMsg = `{"error" : "Error while getting hotel details."}`;
        message.setPayload(response, errorMsg);
        message.setHeader(response, "Status", 500);
    }
    reply response;
}

@GET
@Path ("/rentals?from={dateFrom}&to={dateTo}&type={type}")
resource cars (message m) {
    var message response;
    var message carRequest;
    try {
        xmlElement<{"http://example.com/xsd/HolidayPackage"}Request> requestJSON = message.getPayload(m);
        xmlElement<{"http://example.com/xsd/HolidayPackage"}CarRequest> carRequestJSON;
        carRequestJSON = requestJSON;
        message.setPayload(carRequest, carRequestJSON);
        response = http.sendPost(carRentalEP, carRequest);
    } catch (exception e) {
        var json errorMsg = `{"error" : "Error while getting car rental details."}`;
        message.setPayload(response, errorMsg);
        message.setHeader(response, "Status", 500);
    }
    reply response;
}