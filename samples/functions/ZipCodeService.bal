package samples.functions;

import ballerina.lang.message;


// Importing the package with functions
import samples.functions as zipcodeUtil;


@BasePath ("/ZipCodeService")
@Source (interface = "default_http_listener")
@Service(description = "ZipCode validation service for validate zip codes by providing the country and the code.")
service PassthroughService {

    @GET
    @Path ("/*")
    resource zipCodeResource (message m, @QueryParam("country") string country, @QueryParam("zipCode") string zipCode) {
        boolean isValid;
        json successMessage;
        json failedMessage;
        // Calling a public function in the imported package.
        isValid = zipcodeUtil:validateZipCode (country, zipCode);

        if (isValid) {
            successMessage = `{"Successful" : "ValidZipCode"}`;
            message:setPayload(m, successMessage);
            message:setHeader(response, "Status", 200);
        } else {
            failedMessage = `{"Failed" : "Invalid ZipCode"}`;
            message:setPayload(m, failedMessage);
            message:setHeader(response, "Status", 500);
        }

        reply m;
    }
}