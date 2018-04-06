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
        string message = (jsonResponse.msg.toString() but {()=> "error occurred when searching for packages"});
        io:println(message);
        // io:println(jsonResponse.msg.toString()); 
    } else {
        json artifacts = jsonResponse.artifacts but {()=> {}};
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
                
                string orgName = (jsonElement.orgName.toString() but {()=> ""});
                string packageName = (jsonElement.packageName.toString() but {()=> ""});
                printInCLI(orgName + "/" + packageName, 30);
                
                string summary = (jsonElement.summary.toString() but {()=> ""});
                printInCLI(summary, 40);
                
                // array
                string authors = (jsonElement.author.toString() but {()=> ""});
                printInCLI(authors, 25);

                json createTimeJson = jsonElement.createdDate but {()=> {}};
                printInCLI(getDateCreated(createTimeJson), 20);
                
                string packageVersion = (jsonElement.packageVersion.toString() but {()=> ""});
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
    int timeInMillis = <int>(jsonObj.createdDate.time but {()=>0});
    time:Time timeStruct = new;
    timeStruct.time = timeInMillis;
    timeStruct.zone = {zoneId:"UTC",zoneOffset:0};
    string customTimeString = timeStruct.format("yyyy-MM-dd-E");
    return customTimeString;
}

function main (string[] args) {
    search(args[0], args[1]);
}