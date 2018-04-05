package packaging.search;
import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/time;

function search (string url, string querySearched) {
    endpoint http:ClientEndpoint httpEndpoint {
        targets: [
        {
            url: url,
            secureSocket: {
                trustStore: {
                    filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                },
                hostNameVerification:false,
                sessionCreation: true
            }
        }
        ]
    };

    http:Request req = new;
    // http:Response res = new;
    http:Response httpResponse = check httpEndpoint -> get(querySearched, req);
    // match httpResponse {
    //  http:HttpConnectorError errRes => {
    //      var errorResp = <error> errRes;
    //      match errorResp {
    //          error err =>  throw err;
    //      }
    //  }
    //  http:Response response => res = response;
    // }
    json jsonResponse = check httpResponse.getJsonPayload();
    // json jsonObj;
    // match jsonResponse {
    //         mime:EntityError errRes => {
    //             var errorResp = <error> errRes;
    //             match errorResp {
    //                 error err =>  throw err;
    //             }
    //         }  
    //         json j => jsonObj = j;            
    // }
    if (httpResponse.statusCode != 200) {
        io:println(jsonResponse.msg.toString()); 
    } else {
        json artifacts = jsonResponse.artifacts;
        int artifactsLength = lengthof artifacts;
        if (artifactsLength > 0) {
            io:println("Ballerina Central");
            printInCLI("NAME", 30);
            printInCLI("DESCRIPTION", 40);
            printInCLI("AUTHOR", 25);
            printInCLI("DATE", 20);
            printInCLI("VERSION", 15);
            io:println("");

            int i = 0;
            while (i < artifactsLength) {
                json jsonElement = artifacts[i];
                printInCLI(jsonElement.orgName.toString() + "/" + jsonElement.packageName.toString(), 30);
                printInCLI(jsonElement.description.toString(), 40);
                printInCLI(jsonElement.author.toString(), 25);

                json createTimeJson = jsonElement.createdDate;
                printInCLI(getDateCreated(createTimeJson), 20);

                printInCLI(jsonElement.packageVersion.toString(), 15);               
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

function getDateCreated(json createdDate) returns string {
    int timeInMillis = check <int> createdDate.time.toString();
    // int timeInMillis;
    // match timeConversion {
    //     error errRes => throw errRes;
    //     int time => timeInMillis = time;
    // }

    time:Time timeStruct = {time : timeInMillis, zone:{zoneId:"UTC",zoneOffset:0}};
    string customTimeString = timeStruct.format("yyyy-MM-dd-E");
    return customTimeString;
}

function main (string[] args) {
    search(args[0], args[1]);
}