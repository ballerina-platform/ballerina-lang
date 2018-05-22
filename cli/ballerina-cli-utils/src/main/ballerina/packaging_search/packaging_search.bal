import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/time;
import ballerina/math;

documentation {
    This function searches packages from ballerina central.

    P{{definedEndpoint}} Endpoint defined with the proxy configurations
    P{{url}} Endpoint url to be invoked
    P{{querySearched}} Text searched for
    P{{terminalWidth}} Width of the terminal
}
function search (http:Client definedEndpoint, string url, string querySearched, string terminalWidth) {
    endpoint http:Client httpEndpoint = definedEndpoint;
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
            
            int rightMargin = 3;
            int width = (check <int> terminalWidth) - rightMargin;
        
            int dateColWidth = 15;
            int versionColWidth = 8;
            int authorsColWidth = 15;
            float nameColFactor = 9;
            float descColFactor = 16;
            int additionalSpace = 7;
            float remainingWidth = width - (dateColWidth + versionColWidth + additionalSpace);  
            
            int nameColWidth = math:round(remainingWidth * (nameColFactor / (nameColFactor + descColFactor)));
            int descColWidth = math:round(remainingWidth * (descColFactor / (nameColFactor + descColFactor)));  
            
            printInCLI("|NAME", nameColWidth);
            int minDescColWidth = 60;
            if (descColWidth >= minDescColWidth) {
                printInCLI("DESCRIPTION", descColWidth - authorsColWidth);
                printInCLI("AUTHOR", authorsColWidth);
            } else {
                printInCLI("DESCRIPTION", descColWidth);
            }
            
            printInCLI("DATE", dateColWidth);
            printInCLI("VERSION", versionColWidth);

            io:println("");

            printCharacter("|-", nameColWidth, "-");
            
            if (descColWidth >= minDescColWidth) {
                printCharacter("-", descColWidth - authorsColWidth, "-");
                printCharacter("-", authorsColWidth, "-");
            } else {
                printCharacter("-", descColWidth, "-");
            }

            printCharacter("-", dateColWidth, "-");
            printCharacter("-", versionColWidth, "-");

            io:println("");

            int i = 0;
            while (i < artifactsLength) {
                json jsonElement = artifacts[i];
                string orgName = jsonElement.orgName.toString();
                string packageName = jsonElement.packageName.toString();
                printInCLI("|"+ orgName + "/" + packageName, nameColWidth);
                
                string summary = jsonElement.summary.toString();
                                
                if (descColWidth >= minDescColWidth) {
                    printInCLI(summary, descColWidth - authorsColWidth);
                    string authors = "";
                    json authorsArr = jsonElement.authors;
                    foreach authorIndex in [0..lengthof authorsArr - 1] {
                        if (authorIndex == lengthof authorsArr - 1) {
                            authors = authors + authorsArr[authorIndex].toString();
                        } else {
                            authors = authors + " , " + authorsArr[authorIndex].toString();
                        }
                    }
                    printInCLI(authors, authorsColWidth);
                } else {
                    printInCLI(summary, descColWidth);
                }

                json createTimeJson = <json> jsonElement.createdDate;
                printInCLI(getDateCreated(createTimeJson), dateColWidth);
                
                string packageVersion = jsonElement.packageVersion.toString();
                printInCLI(packageVersion, versionColWidth);
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
    This function defines an endpoint with proxy configurations.

    P{{url}} URL to be invoked
    P{{hostname}} Host name of the proxy
    P{{port}} Port of the proxy
    P{{username}} Username of the proxy
    P{{password}} Password of the proxy
    R{{}} Endpoint defined
}
function defineEndpointWithProxy (string url, string hostname, string port, string username, string password) returns http:Client{
    endpoint http:Client httpEndpoint {
        url: url,
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        proxy : getProxyConfigurations(hostname, port, username, password)
    };
    return httpEndpoint;
}

documentation {
    This function defines an endpoint without proxy configurations.

    P{{url}} URL to be invoked
    R{{}} Endpoint defined
}
function defineEndpointWithoutProxy (string url) returns http:Client{
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
    return httpEndpoint;
}

documentation {
    This function prints package information.

    P{{element}} Text to be printed
    P{{charactersAllowed}} Maximum number of characters to be printed
}
function printInCLI(string element, int charactersAllowed) {
    int lengthOfElement = element.length();
    if (lengthOfElement > charactersAllowed || lengthOfElement == charactersAllowed) {
        int margin = 3;
        string trimmedElement = element.substring(0, charactersAllowed - margin) + "...";
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
    http:Client httpEndpoint;
    string host = args[2];
    string port = args[3];
    if (host != "" && port != "") {
        try {
            httpEndpoint = defineEndpointWithProxy(args[0], host, port, args[4], args[5]);
        } catch (error err) {
            io:println("failed to resolve host : " + host + " with port " + port);
            return;
        }
    } else  if (host != "" || port != "") {
        io:println("both host and port should be provided to enable proxy");     
        return;   
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[0]);
    }        
    search(httpEndpoint, args[0], args[1], args[6]);
}

documentation {
    This function sets the proxy configurations for the endpoint.

    P{{hostName}} Host name of the proxy
    P{{port}} Port of the proxy
    P{{username}} Username of the proxy
    P{{password}} Password of the proxy
    R{{}} Proxy configurations for the endpoint
}
function getProxyConfigurations(string hostName, string port, string username, string password) returns http:ProxyConfig {
    int portInt = check <int> port;
    http:ProxyConfig proxy = { host : hostName, port : portInt , userName: username, password : password };
    return proxy;
}
