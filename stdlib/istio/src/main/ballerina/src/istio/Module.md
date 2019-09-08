## Module Overview
This module offers an annotation based Istio extension implementation for ballerina. 

### Annotation Usage Sample:

```ballerina
import ballerina/http;
import ballerina/internal;
import ballerina/log;
import ballerina/kubernetes;
import ballerina/istio;

@kubernetes:Service {}
@istio:Gateway {}
@istio:VirtualService {}
listener http:Listener travelAgencyEP = new(9090);

http:Client airlineReservationEP = new("http://airline-reservation:8080/airline");
http:Client hotelReservationEP = new("http://hotel-reservation:7070/hotel");
http:Client carRentalEP = new("http://car-rental:6060/car");

@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/travel"
}
service travelAgencyService on travelAgencyEP {
    @http:ResourceConfig {
        methods: ["POST"],
        consumes: ["application/json"],
        produces: ["application/json"],
        path: "/arrange"
    }
    resource function arrangeTour(http:Caller caller, http:Request inRequest) returns error? {
        http:Response outResponse = new;
        json inReqPayload = {};

        // Try parsing the JSON payload from the user request
        var payload = inRequest.getJsonPayload();
        if (payload is json) {
            inReqPayload = payload;
        } else {
            outResponse.statusCode = 400;
            outResponse.setJsonPayload({
                Message: "Invalid payload - Not a valid JSON payload"
            });
            var result = caller->respond(outResponse);
            handleError(result);
            return;
        }

        json|error inReqPayloadNameJson = inReqPayload.Name;
        json|error inReqPayloadArrivalDateJson = inReqPayload.ArrivalDate;
        json|error inReqPayloadDepartureDateJson = inReqPayload.DepartureDate;
        json|error airlinePreference = inReqPayload.Preference.Airline;
        json|error hotelPreference = inReqPayload.Preference.Accommodation;
        json|error carPreference = inReqPayload.Preference.Car;

        if (inReqPayloadNameJson is error || inReqPayloadArrivalDateJson is error ||
            inReqPayloadDepartureDateJson is error || airlinePreference is error || hotelPreference is error ||
            carPreference is error) {
            outResponse.statusCode = 400;
            outResponse.setJsonPayload({
                Message: "Bad Request - Invalid Payload"
            });
            var result = caller->respond(outResponse);
            handleError(result);
            return;
        }

        json outReqPayloadAirline = {
            Name: checkpanic inReqPayloadNameJson,
            ArrivalDate: checkpanic inReqPayloadArrivalDateJson,
            DepartureDate: checkpanic inReqPayloadDepartureDateJson,
            Preference: checkpanic airlinePreference
        };

        http:Response inResAirline = check airlineReservationEP->post("/reserve", <@untainted> outReqPayloadAirline);

        var airlineResPayload = check inResAirline.getJsonPayload();
        string airlineStatus = airlineResPayload.Status.toString();
        if (internal:equalsIgnoreCase(airlineStatus, "Failed")) {
            outResponse.setJsonPayload({
                Message: "Failed to reserve airline! Provide a valid 'Preference' for 'Airline' and try again"
            });
            var result = caller->respond(outResponse);
            handleError(result);
            return;
        }

        json outReqPayloadHotel = {
            Name: checkpanic inReqPayloadNameJson,
            ArrivalDate: checkpanic inReqPayloadArrivalDateJson,
            DepartureDate: checkpanic inReqPayloadDepartureDateJson,
            Preference: checkpanic hotelPreference
        };

        http:Response inResHotel = check hotelReservationEP->post("/reserve", <@untainted> outReqPayloadHotel);

        var hotelResPayload = check inResHotel.getJsonPayload();
        string hotelStatus = hotelResPayload.Status.toString();
        if (internal:equalsIgnoreCase(hotelStatus, "Failed")) {
            outResponse.setJsonPayload({
                Message: "Failed to reserve hotel! Provide a valid 'Preference' for 'Accommodation' and try again"
            });
            var result = caller->respond(outResponse);
            handleError(result);
            return;
        }

        json outReqPayloadCar = {
            Name: checkpanic inReqPayloadNameJson,
            ArrivalDate: checkpanic inReqPayloadArrivalDateJson,
            DepartureDate: checkpanic inReqPayloadDepartureDateJson,
            Preference: checkpanic carPreference
        };

        http:Response inResCar = check carRentalEP->post("/rent", <@untainted> outReqPayloadCar);

        var carResPayload = check inResCar.getJsonPayload();
        string carRentalStatus = carResPayload.Status.toString();
        if (internal:equalsIgnoreCase(carRentalStatus, "Failed")) {
            outResponse.setJsonPayload({
                "Message": "Failed to rent car! Provide a valid 'Preference' for 'Car' and try again"
            });
            var result = caller->respond(outResponse);
            handleError(result);
            return;
        }

        outResponse.setJsonPayload({
            Message: "Congratulations! Your journey is ready!!"
        });
        var result = caller->respond(outResponse);
        handleError(result);
        return ();
    }
}

function handleError(error? result) {
    if (result is error) {
        log:printError(result.reason(), result);
    }
}
```
