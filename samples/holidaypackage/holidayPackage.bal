package samples.holidaypackage;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system.log;

@BasePath ("/holiday")
@Source (interface="x")
@Service (tags = {"Holiday_details", "Holiday_packages"} description = "Travelpedia flight, hotel and car rental services")
service HolidayPackageService;

actor http.HttpEndpoint airlineEP = new http.HttpEndpoint (..);
actor http.HttpEndpoint carRentalEP = new http.HttpEndpoint (..);
actor http.HttpEndpoint hotelEP = new http.HttpEndpoint (..);

@GET
@Path ("/flights?depart={dateDepart}&return={dateReturn}&from={from}&to={to}")
resource flights (message m) {
    var message flightRequest;
    var message response;
    try {
        var json<Request> requestJSON = message.getPayload(m);
        var json<FlightRequest> flightRequestJSON;
        flightRequestJSON = requestJSON;
        message.setPayload(flightRequest, flightRequestJSON);
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
        var json<Request> requestJSON = message.getPayload(m);
        var json<HotelRequest> hotelRequestJSON;
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
        var json<Request> requestJSON = message.getPayload(m);
        var json<CarRequest> carRequestJSON;
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

@POST
@Produces ("application/json")
@Consumes ("application/json")
@Path ("/book")
resource bookPackage (message m) {
    var message response;
    var json jsonMsg = message.getPayload(m);
    var string location = json.get(jsonMsg, "$.request.holiday.location");
    var string dateFrom = json.get(jsonMsg, "$.request.holiday.from");
    var string dateTo = json.get(jsonMsg, "$.request.holiday.to");
    var boolean booked;
    var string flightBookingRef;
    
    // FIXME
    booked, flightBookingRef = bookAirline(airlineEP, location, dateFrom, dateTo, m);

    if (booked) {
        try {
            worker hotelWorker (message m) {
                // Convert the request 
                var message nearestHotelsRequest;
                var json<Request> requestJSON = message.getPayload(m);
                var json<NearestHotelsRequest> nearestHotelsRequestJSON;
                nearestHotelsRequestJSON = requestJSON;
                message.setPayload(nearestHotelsRequest, nearestHotelsRequestJSON);

                // Get the nearest hotels
                var message nearestHotels = http.sendPost(hotelEP, nearestHotelsRequest);
                var json nearestHotelsJSON = message.getPayload(nearestHotels);

                // Query each hotel for price and availability
                var int lowestCost = 0;
                var message bestHotel;
                foreach (message hotel : json.get(nearestHotelsJSON, "$.hotels")) {
                    var message hotelRequest;
                    var json<Request> requestJSON = message.getPayload(m);
                    var json<HotelRequest> hotelRequestJSON;
                    hotelRequestJSON = requestJSON;
                    message.setPayload(hotelRequest, hotelRequestJSON);
                    var message hotelDetails = http.sendPost(hotelEP, hotelRequest);
                    var json hotelDetailsJSON = message.getPayload(hotelDetails);
                    
                    var int hotelCost = json.get(hotelDetailsJSON, "$.hotel.cost");
                    if (lowestCost == 0 || hotelCost < lowestCost) {
                        lowestCost = hotelCost;
                        bestHotel = json.get(hotelDetailsJSON, "$.hotel");
                    }
                }

                // TODO: need to merge $bestHotel and $m
                var message hotelReservationRequest;
                var json<Request> requestJSON = message.getPayload(m);
                var json<HotelReservationRequest> hotelReservationRequestJSON;
                hotelReservationRequestJSON = requestJSON;
                message.setPayload(hotelReservationRequest, hotelReservationRequestJSON);

                // Make a reservation
                return http.sendPost(hotelEP, hotelReservationRequest);
            }
        } catch(exception e) {
            log.error("Error while making hotel reservation.");
            var json responseJSON = `{ "FlightRef" : flightBookingRef }`;
            message.setPayload(response, responseJSON);
            reply response;
        }

        // Get booking reference
        message hotelResponse = wait hotelWorker;
        vae json hotelResponseJSON = message.getPayload(hotelResponse);
        string hotelBookingRef = json.get(hotelResponseJSON, "$.BookingRef");
        
        // Rent-a-car
        var string carBookingRef;
        try {
            fork (m) {
                worker hertzCars (message x) {
                    // Retrieve cars available from Hertz rent-a-car
                    var message hertzRequest;
                    var json<Request> requestJSON = message.getPayload(m);
                    var json<HertzRequest> hertzRequestJSON;
                    hertzRequestJSON = requestJSON;
                    message.setPayload(hertzRequest, hertzRequestJSON);
                    var message hertzAvailableCars = http.sendPost(carRentalEP, hertzRequest);

                    // Convert the Hertz response to a generic rent-a-car response
                    var message availableCars;
                    var json<AvailableCars> availableCarsJSON;
                    var json<HertzAvailableCars> hertzAvailableCarsJSON = message.getPayload(hertzAvailableCars);
                    availableCarsJSON = hertzAvailableCarsJSON;
                    message.setPayload(availableCars, availableCarsJSON);

                    return availableCars;
                }
                worker enterpriseCars (message y) {
                    // Retrieve cars available from Enterprise rent-a-car
                    var message enterpriseRequest;
                    var json<Request> requestJSON = message.getPayload(m);
                    var json<EnterpriseRequest> enterpriseRequestJSON;
                    enterpriseRequestJSON = requestJSON;
                    message.setPayload(enterpriseRequest, enterpriseRequestJSON);
                    var message enterpriseAvailableCars = http.sendPost(carRentalEP, enterpriseRequest);

                    // Convert the Enterprise response to a generic rent-a-car response
                    var message availableCars;
                    var json<AvailableCars> availableCarsJSON;
                    var json<enterpriseAvailableCars> enterpriseAvailableCarsJSON = message.getPayload(enterpriseAvailableCars);
                    availableCarsJSON = enterpriseAvailableCarsJSON;
                    message.setPayload(availableCars, availableCarsJSON);

                    return availableCars;
                }
            } join any (message[] cars) {
                log.debug("Retrieving available vehicles from Partners");
                var message carReservation;
                var json<CarReservation> carReservationJSON;
                var json<AvailableCars> availableCars = message.getPayload(cars[0]);
                
                // Make a reservation
                carReservationJSON = availableCars;
                message.setPayload(carReservation, carReservationJSON);
                var message carResevationResponse = http.sendPost(carRentalEP, carReservation);
                
                // Get the reservation reference
                var json carResevationResponseJSON = message.getPayload(carResevationResponse);
                carBookingRef = json.get(carResevationResponseJSON, "$.BookingRef");
            }
        } catch(exception e) {   
            log.error("Error while making car reservation.");
            
            // Reply with Flight and Hotel booking refrences
            var json responseJSON = `{ "FlightBookingRef" : flightBookingRef, "HotelBookingRef": hotelBookingRef }`;
            message.setPayload(response, responseJSON);
            message.setHeader(response, "Status", 200);
        }
        
        // Reply with Flight Booking, Hotel booking and Car booking refrences
        var json responseJSON = `{ "FlightBookingRef" : flightBookingRef, "HotelBookingRef" : hotelBookingRef, "CarBookingRef" : carBookingRef }`;
        message.setPayload(response, responseJSON);
        message.setHeader(response, "Status", 200);
    } else {
        var json errorMsg = `{"error" : "Flight reservation failed."}`;
        message.setPayload(response, errorMsg);
        message.setHeader(response, "Status", 500);
    }

    reply response;
}