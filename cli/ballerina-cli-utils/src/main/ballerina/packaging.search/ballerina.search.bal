package packaging.search;
import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/time;

function search (string url, string querySearched) {
    endpoint http:Client httpEndpoint {
        url:url,
        secureSocket:{
            trustStore:{
                filePath:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password:"ballerina"
            },
            verifyHostname:false,
            shareSession:true
        }
    };
    http:Request req = new;
    var result = httpEndpoint -> get(untaint querySearched, req);
    http:Response httpResponse = check result;
    json jsonResponse = null;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        io:println("remote registry failed for url : " + url + "/" + querySearched);
    } else if (statusCode != "200") {
        jsonResponse = check (httpResponse.getJsonPayload());
        string message = jsonResponse.msg.toString();
        io:println(message);
    } else {
        jsonResponse = check (httpResponse.getJsonPayload());
        json[] artifacts = check <json[]>jsonResponse.artifacts;
        if (artifacts == null || lengthof artifacts > 0) {
            int artifactsLength = lengthof artifacts;
            io:println("Ballerina Central");
            printInCLI("NAME", 30);
            printInCLI("DESCRIPTION", 40);
            printInCLI("AUTHOR", 40);
            printInCLI("DATE", 20);
            printInCLI("VERSION", 15);
            io:println("");

            int i = 0;
            while (i < artifactsLength) {
                json jsonElement = artifacts[i];
                string orgName = jsonElement.orgName.toString();
                string packageName = jsonElement.packageName.toString();
                printInCLI(orgName + "/" + packageName, 30);
                
                string summary = jsonElement.summary.toString();
                printInCLI(summary, 40);
                
                string authors;
                json authorsArr = jsonElement.authors;
                foreach authorIndex in [0..lengthof authorsArr - 1] {
                    if (authorIndex == lengthof authorsArr - 1) {
                        authors = authors + authorsArr[authorIndex].toString();
                    } else {
                        authors = authors + " , " + authorsArr[authorIndex].toString();
                    }
                }
                printInCLI(authors, 40);

                json createTimeJson = <json>jsonElement.createdDate;
                printInCLI(getDateCreated(createTimeJson), 20);
                
                string packageVersion = jsonElement.packageVersion.toString();
                printInCLI(packageVersion, 15);               
                i = i + 1;
                io:println("");
            }
        } else {
            io:println("no packages found");
        }
    }
}

function printInCLI(string element, int charactersAllowed) {
    int lengthOfElement = element.length();
    if (lengthOfElement > charactersAllowed || lengthOfElement == charactersAllowed) {
        string trimmedElement = element.subString(0, charactersAllowed - 3) + "...";
        io:print(trimmedElement + "| ");
    } else {
        io:print(element);
        int i = 0;
        while(i < charactersAllowed - lengthOfElement) {
            io:print(" ");
            i = i + 1;
        }
        io:print("| ");
    }
}

function getDateCreated(json jsonObj) returns string {
    string jsonTime = jsonObj.time.toString();
    int timeInMillis = check <int> jsonTime;
    time:Time timeStruct = new(timeInMillis, {zoneId:"UTC",zoneOffset:0});
    string customTimeString = timeStruct.format("yyyy-MM-dd-E");
    return customTimeString;
}

function main (string... args) {
    search(args[0], args[1]);
}
