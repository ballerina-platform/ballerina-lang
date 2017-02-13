package samples.holidaypackage;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system.log;

@BasePath ("/holiday")
@Source (interface="x")
@Service (tags = {"Holiday_details", "Holiday_packages"}, description = "Travelpedia flight, hotel and car rental services")
service HolidayPackageService {

  http:ClientConnector airlineEP = new http:ClientConnector("http://localhost:8080/airline/", {"timeOut" : 30000});
  http:ClientConnector carRentalEP = new http:ClientConnector("http://localhost:8080/carrental/", {"timeOut" : 30000});
  http:ClientConnector hotelEP = new http:ClientConnector("http://localhost:8080/hotel/", {"timeOut" : 30000});

  @GET
  @Path ("/flights?depart={dateDepart}&return={dateReturn}&from={from}&to={to}")
  resource flights (message m) {
      message response;
      json errorMsg;
      try {
          response = http:sendPost(airlineEP, m);
      } catch (exception e) {
          errorMsg = `{"error" : "Error while getting flight details."}`;
          message:setPayload(response, errorMsg);
          message:setHeader(response, "Status", 500);
      }
      reply response;
  }

  @GET
  @Path ("/hotels?from={dateFrom}&to={dateTo}&location={location}")
  resource hotels (message m) {
      message response;
      xmlElement<{"http://example.com/xsd/HolidayPackage"}Request> request;
      xmlElement<{"http://example.com/xsd/HolidayPackage"}HotelRequest> hotelRequest;
      json errorMsg;
      try {
          request = message:getPayload(m);
          hotelRequest = request;
          message:setPayload(m, hotelRequest);
          response = http:sendPost(hotelEP, m);
      } catch (exception e) {
          errorMsg = `{"error" : "Error while getting hotel details."}`;
          message:setPayload(response, errorMsg);
          message:setHeader(response, "Status", 500);
      }
      reply response;
  }

  @GET
  @Path ("/rentals?from={dateFrom}&to={dateTo}&type={type}")
  resource cars (message m) {
      message response;
      xmlElement<{"http://example.com/xsd/HolidayPackage"}Request> requestXML;
      xmlElement<{"http://example.com/xsd/HolidayPackage"}CarRequest> carrequestXML;
      json errorMsg;
      try {
          requestXML = message:getPayload(m);
          carrequestXML = requestXML;
          message:setPayload(m, carrequestXML);
          response = http:sendPost(carRentalEP, m);
      } catch (exception e) {
          errorMsg = `{"error" : "Error while getting car rental details."}`;
          message:setPayload(response, errorMsg);
          message:setHeader(response, "Status", 500);
      }
      reply response;
  }

}
