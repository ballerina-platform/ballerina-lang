package samples.holidaypackage;

import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;
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
          messages:setPayload(response, errorMsg);
          messages:setHeader(response, "Status", 500);
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
          request = messages:getPayload(m);
          hotelRequest = request;
          messages:setPayload(m, hotelRequest);
          response = http:sendPost(hotelEP, m);
      } catch (exception e) {
          errorMsg = `{"error" : "Error while getting hotel details."}`;
          messages:setPayload(response, errorMsg);
          messages:setHeader(response, "Status", 500);
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
          requestXML = messages:getPayload(m);
          carrequestXML = requestXML;
          messages:setPayload(m, carrequestXML);
          response = http:sendPost(carRentalEP, m);
      } catch (exception e) {
          errorMsg = `{"error" : "Error while getting car rental details."}`;
          messages:setPayload(response, errorMsg);
          messages:setHeader(response, "Status", 500);
      }
      reply response;
  }

}
