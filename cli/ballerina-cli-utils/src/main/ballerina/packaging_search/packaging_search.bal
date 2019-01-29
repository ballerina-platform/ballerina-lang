// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/time;
import ballerina/math;

# This function searches modules from ballerina central.
#
# + definedEndpoint - Endpoint defined with the proxy configurations
# + url - Endpoint url to be invoked
# + querySearched - Text searched for
# + terminalWidth - Width of the terminal
function search (http:Client definedEndpoint, string url, string querySearched, string terminalWidth) {
    http:Client httpEndpoint = definedEndpoint;
    http:Request req = new;
    var result = httpEndpoint -> get(untaint querySearched, message=req);
    http:Response httpResponse = new;
    if (result is http:Response) {
        httpResponse = result;
    } else if (result is error) {
        io:println("Connection to the remote host failed : " + result.reason());
        return;
    }
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode.hasPrefix("5")) {
        io:println("remote registry failed for url : " + url + "/" + querySearched);
    } else if (statusCode != "200") {
        var resp = httpResponse.getJsonPayload();
        if (resp is json) {
            string message = resp.msg.toString();
            io:println(message);
        } else {
            io:println("invalid response json");
        }
    } else {
        var jsonResponse = httpResponse.getJsonPayload();
        if (jsonResponse is json) {
            var artifacts = trap <json[]> jsonResponse.artifacts;
            if (artifacts is json[]) {
                if (artifacts.length() > 0) {
                    int artifactsLength = artifacts.length();
                    printTitle("Ballerina Central");

                    int rightMargin = 3;
                    int width;
                    var intTerminalWidth = int.convert(terminalWidth);
                    if (intTerminalWidth is int) {
                        width = intTerminalWidth - rightMargin;
                    } else {
                        io:println("invalid terminal width : " + terminalWidth);
                        return;
                    }

                    int dateColWidth = 15;
                    int versionColWidth = 8;
                    int authorsColWidth = 15;
                    float nameColFactor = 9.0;
                    float descColFactor = 16.0;
                    int additionalSpace = 7;
                    float remainingWidth = width - <float>(dateColWidth + versionColWidth + additionalSpace);

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
                            foreach var authorIndex in 0 ..< authorsArr.length() {
                                if (authorIndex == authorsArr.length() - 1) {
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
                    io:println("no modules found");
                }
            } else {
                io:println("invalid response json");
            }
        } else {
            io:println("invalid response json");
        }
    }
}

# This function defines an endpoint with proxy configurations.
#
# + url - URL to be invoked
# + hostname - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Endpoint defined
function defineEndpointWithProxy (string url, string hostname, int port, string username, string password) returns http:Client{
    http:Client httpEndpoint = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        proxy : getProxyConfigurations(hostname, port, username, password)
    });
    return httpEndpoint;
}

# This function defines an endpoint without proxy configurations.
#
# + url - URL to be invoked
# + return - Endpoint defined
function defineEndpointWithoutProxy (string url) returns http:Client{
    http:Client httpEndpoint = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        }
    });
    return httpEndpoint;
}

# This function prints module information.
#
# + element - Text to be printed
# + charactersAllowed - Maximum number of characters to be printed
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

# This function prints any given character the specified number of times.
#
# + element - Characters to be printed
# + charactersAllowed - Maximum number of characters to be printed
# + separator - Character to be used as the separator
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

# This function prints the title along with a horizontal separation.
#
# + title - Title to be printed
function printTitle(string title) {
    io:println("");
    io:println(title);
    io:println("=================");
    io:println("");
}

# This function gets the date the module was created in UTC.
#
# + jsonObj - Time object as a json
# + return - Date and time the module was created
function getDateCreated(json jsonObj) returns string {
    string jsonTime = jsonObj.time.toString();
    var timeInMillis = int.convert(jsonTime);
    if (timeInMillis is int) {
        time:Time timeRecord = { time: timeInMillis, zone: { id: "UTC", offset: 0 } };
        string customTimeString = time:format(timeRecord, "yyyy-MM-dd-E");
        return customTimeString;
    } else if (timeInMillis is error) {
        panic timeInMillis;
    }
    return "";
}

# This function invokes the method to search for modules.
# + args - Arguments passed
public function main (string... args) {
    http:Client httpEndpoint;
    string host = args[2];
    string strPort = args[3];
    if (host != "" && strPort != "") {
        var port = int.convert(strPort);
        if (port is int) {
            http:Client|error result = trap defineEndpointWithProxy(args[0], host, port, args[4], args[5]);
            if (result is http:Client) {
                httpEndpoint = result;
                search(httpEndpoint, args[0], args[1], args[6]);
            } else if (result is error) {
                io:println("failed to resolve host : " + host + " with port " + port);
                return;
            }
        } else {
            io:println("invalid port : " + strPort);
        }
    } else  if (host != "" || strPort != "") {
        io:println("both host and port should be provided to enable proxy");     
        return;   
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[0]);
        search(httpEndpoint, args[0], args[1], args[6]);
    }
}

# This function sets the proxy configurations for the endpoint.
#
# + hostName - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Proxy configurations for the endpoint
function getProxyConfigurations(string hostName, int port, string username, string password) returns http:ProxyConfig {
    http:ProxyConfig proxy = { host : hostName, port : port , userName: username, password : password };
    return proxy;
}
