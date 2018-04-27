import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/time;

documentation {
    This function searches packages from ballerina central.

    P{{url}} Endpoint url to be invoked
    P{{querySearched}} Text searched for
}
function search (string url, string querySearched) {
    endpoint http:Client httpEndpoint {
        url: url,
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        }
    };
    http:Request req = new;
    var result = httpEndpoint -> get(untaint querySearched, request=req);
    http:Response httpResponse = new;
    match result {
        http:Response response => httpResponse = response;
        error e => {
            io:println("Connection to the remote host failed : " + e.message);
            return;
        }
    }
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
        json[] artifacts = check <json[]> jsonResponse.artifacts;
        if (artifacts == null || lengthof artifacts > 0) {
            int artifactsLength = lengthof artifacts;
            
            printTitle("Ballerina Central");

            printInCLI("|NAME", 18);
            printInCLI("DESCRIPTION", 32);
            printInCLI("DATE", 15);
            printInCLI("VERSION", 8);

            io:println("");

            printCharacter("|-", 18, "-");
            printCharacter("-", 32, "-");
            printCharacter("-", 15, "-");
            printCharacter("-", 8, "-");

            io:println("");

            int i = 0;
            while (i < artifactsLength) {
                json jsonElement = artifacts[i];
                string orgName = jsonElement.orgName.toString();
                string packageName = jsonElement.packageName.toString();
                printInCLI("|"+ orgName + "/" + packageName, 18);
                
                string summary = jsonElement.summary.toString();
                printInCLI(summary, 32);

                json createTimeJson = <json> jsonElement.createdDate;
                printInCLI(getDateCreated(createTimeJson), 15);
                
                string packageVersion = jsonElement.packageVersion.toString();
                printInCLI(packageVersion, 8);
                i = i + 1;
                io:println("");
            }
            io:println("");
        } else {
            io:println("no packages found");
        }
    }
}

documentation {
    This function prints package information.

    P{{element}} Text to be printed
    P{{charactersAllowed}} Maximum number of characters to be printed
}
function printInCLI(string element, int charactersAllowed) {
    int lengthOfElement = element.length();
    if (lengthOfElement > charactersAllowed || lengthOfElement == charactersAllowed) {
        string trimmedElement = element.substring(0, charactersAllowed - 3) + "...";
        io:print(trimmedElement + "| ");
    } else {
        printCharacter(element, charactersAllowed, " ");
    }
}

documentation {
    This function prints any given character the specified number of times.

    P{{element}} Characters to be printed
    P{{charactersAllowed}} Maximum number of characters to be printed
    P{{separator}} Character to be used as the separator
}
function printCharacter(string element, int charactersAllowed, string separator) {
    int lengthOfElement = element.length();
    string print = element;
    int i = 0;
    while(i < charactersAllowed - lengthOfElement) {
        print = print + separator;
        i = i + 1;
    }
    io:print(print + "| ");
}

documentation {
    This function prints the title along with a horizontal separation.

    P{{title}} Title to be printed
}
function printTitle(string title) {
    io:println("");
    io:println(title);
    io:println("=================");
    io:println("");
}

documentation {
    This function gets the date the package was created in UTC.

    P{{jsonObj}} Time object as a json
    R{{}} Date and time the package was created
}
function getDateCreated(json jsonObj) returns string {
    string jsonTime = jsonObj.time.toString();
    int timeInMillis = check <int> jsonTime;
    time:Time timeStruct = new(timeInMillis, { zoneId: "UTC", zoneOffset: 0 });
    string customTimeString = timeStruct.format("yyyy-MM-dd-E");
    return customTimeString;
}

documentation {
    This function invokes the method to search for packages.
}
function main (string... args) {
    search(args[0], args[1]);
}
