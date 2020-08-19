import ballerina/io;
import pramodya/hello;
import pramodya/bye;
import wso2/sfdc46;

public function main() {
    io:println(hello:sayHello("John"));
    io:println(bye:sayBye("Anne"));

// Create Salesforce client configuration by reading from config file.
sfdc46:SalesforceConfiguration sfConfig = {
    baseUrl: "EP_URL",
    clientConfig: {
        accessToken: "ACCESS_TOKEN",
        refreshConfig: {
            clientId: "CLIENT_ID",
            clientSecret: "CLIENT_SECRET",
            refreshToken: "REFRESH_TOKEN",
            refreshUrl: "REFRESH_URL"
        }
    }
};
}
