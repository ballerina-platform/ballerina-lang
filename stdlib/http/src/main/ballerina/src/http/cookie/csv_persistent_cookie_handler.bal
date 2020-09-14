// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/file;
import ballerina/io;
import ballerina/log;
import ballerina/time;

type myCookie record {
    readonly string name;
    string value;
    readonly string domain;
    readonly string path;
    string expires;
    int maxAge;
    boolean httpOnly;
    boolean secure;
    string createdTime;
    string lastAccessedTime;
    boolean hostOnly;
};

string? cookieNameToRemove = ();
string? cookieDomainToRemove = ();
string? cookiePathToRemove = ();

# Represents a default persistent cookie handler, which stores persistent cookies in a CSV file.
#
# + fileName - Name of the CSV file to store persistent cookies
public class CsvPersistentCookieHandler {
    *PersistentCookieHandler;

    string fileName = "";
    table<myCookie> key(name, domain, path) cookiesTable = table [];

    public function init(string fileName) {
        self.fileName = checkpanic validateFileExtension(fileName);
    }

    # Adds a persistent cookie to the cookie store.
    #
    # + cookie - Cookie to be added
    # + return - An error will be returned if there is any error occurred during the storing process of the cookie or else nil is returned
    public function storeCookie(Cookie cookie) returns @tainted CookieHandlingError? {
        if (file:exists(self.fileName) && self.cookiesTable.length() == 0) {
            var tblResult = readFile(self.fileName);
            if (tblResult is table<myCookie> key(name, domain, path)) {
                self.cookiesTable = tblResult;
            } else {
                return CookieHandlingError("Error in reading the csv file", tblResult);
            }
        }
        var tableUpdateResult = addNewCookieToTable(self.cookiesTable, cookie);
        if (tableUpdateResult is table<myCookie> key(name, domain, path)) {
            self.cookiesTable = tableUpdateResult;
        } else {
            return CookieHandlingError("Error in updating the records in csv file",
            cause = tableUpdateResult);
        }
        var result = writeToFile(self.cookiesTable, <@untainted> self.fileName);
        if (result is error) {
            return CookieHandlingError("Error in writing the csv file", result);
        }
    }

    # Gets all the persistent cookies.
    #
    # + return - Array of persistent cookies stored in the cookie store or else an error is returned if one occurred during the retrieval of the cookies
    public function getAllCookies() returns @tainted Cookie[]|CookieHandlingError {
        Cookie[] cookies = [];
        if (file:exists(self.fileName)) {
            var tblResult = readFile(self.fileName);
            if (tblResult is table<myCookie> key(name, domain, path)) {
                foreach var rec in tblResult {
                    Cookie cookie = new(rec.name, rec.value);
                    cookie.domain = rec.domain;
                    cookie.path = rec.path;
                    cookie.expires = rec.expires == "-" ? () : rec.expires;
                    cookie.maxAge = rec.maxAge;
                    cookie.httpOnly = rec.httpOnly;
                    cookie.secure = rec.secure;
                    time:Time|error t1 = time:parse(rec.createdTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    if (t1 is time:Time) {
                        cookie.createdTime = t1;
                    }
                    time:Time|error t2 = time:parse(rec.lastAccessedTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    if (t2 is time:Time) {
                        cookie.lastAccessedTime = t2;
                    }
                    cookie.hostOnly = rec.hostOnly;
                    cookies.push(cookie);
                }
                return cookies;
            } else {
                return CookieHandlingError("Error in reading the csv file", tblResult);
            }
        }
       return cookies;
    }

    # Removes a specific persistent cookie.
    #
    # + name - Name of the persistent cookie to be removed
    # + domain - Domain of the persistent cookie to be removed
    # + path - Path of the persistent cookie to be removed
    # + return - An error will be returned if there is any error occurred during the removal of the cookie or else nil is returned
    public function removeCookie(string name, string domain, string path) returns @tainted CookieHandlingError? {
        cookieNameToRemove = name;
        cookieDomainToRemove = domain;
        cookiePathToRemove = path;
        if (file:exists(self.fileName)) {
            if(self.cookiesTable.length() == 0) {
                var tblResult = readFile(self.fileName);
                if (tblResult is table<myCookie> key(name, domain, path)) {
                    self.cookiesTable = tblResult;
                } else {
                    return CookieHandlingError("Error in reading the csv file", tblResult);
                }
            }
            var removedCookie = self.cookiesTable.remove([name, domain, path]);
            error? removeResults = file:remove(<@untainted> self.fileName);
            if (removeResults is error) {
                return CookieHandlingError("Error in removing the csv file", removeResults);
            }
            var writeResult = writeToFile(self.cookiesTable, <@untainted> self.fileName);
            if (writeResult is error) {
                return CookieHandlingError("Error in writing the csv file", writeResult);
            }
            return;
        }
        return CookieHandlingError("Error in removing cookie: No persistent cookie store file to remove");
    }

    # Removes all persistent cookies.
    #
    # + return - An error will be returned if there is any error occurred during the removal of all the cookies or else nil is returned
    public function removeAllCookies() returns CookieHandlingError? {
        error? removeResults = file:remove(self.fileName);
        if (removeResults is error) {
            return CookieHandlingError("Error in removing the csv file", removeResults);
        }
    }
}

function validateFileExtension(string fileName) returns string|CookieHandlingError {
    if (fileName.toLowerAscii().endsWith(".csv")) {
        return fileName;
    }
    return CookieHandlingError("Invalid file format");
}

function readFile(string fileName) returns @tainted error|table<myCookie> key(name, domain, path) {
    io:ReadableCSVChannel rCsvChannel2 = check io:openReadableCsvFile(fileName);
    var tblResult = rCsvChannel2.getTable(myCookie, ["name", "domain", "path"]);
    closeReadableCSVChannel(rCsvChannel2);
    if (tblResult is table<record{| anydata...; |}>) {
        return <table<myCookie> key(name, domain, path)>tblResult;
    } else {
        return tblResult;
    }
}

function closeReadableCSVChannel(io:ReadableCSVChannel csvChannel) {
    var result = csvChannel.close();
    if (result is error) {
        log:printError("Error occurred while closing the channel: ", result);
    }
}

// Updates the table with new cookie.
function addNewCookieToTable(table<myCookie> key(name, domain, path) cookiesTable, Cookie cookieToAdd)
returns table<myCookie> key(name, domain, path)|error {
    table<myCookie> key(name, domain, path) tableToReturn = cookiesTable;
    var name = cookieToAdd.name;
    var value = cookieToAdd.value;
    var domain = cookieToAdd.domain;
    var path = cookieToAdd.path;
    var expires = cookieToAdd.expires;
    var createdTime = time:format(cookieToAdd.createdTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    var lastAccessedTime = time:format(cookieToAdd.lastAccessedTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (name is string && value is string && domain is string && path is string && createdTime is string &&
    lastAccessedTime is string) {
        myCookie c1 = { name: name, value: value, domain: domain, path: path, expires: expires is string ?
        expires : "-", maxAge: cookieToAdd.maxAge, httpOnly: cookieToAdd.httpOnly, secure: cookieToAdd.secure,
        createdTime: createdTime, lastAccessedTime: lastAccessedTime, hostOnly: cookieToAdd.hostOnly };
        var result = tableToReturn.add(c1);
        return tableToReturn;
    }
    return CookieHandlingError("Invalid data types for cookie attributes");
}

// Writes the updated table to the file.
function writeToFile(table<myCookie> key(name, domain, path) cookiesTable, string fileName) returns @tainted error? {
    io:WritableCSVChannel wCsvChannel2 = check io:openWritableCsvFile(fileName);
    foreach var entry in cookiesTable {
        string[] rec = [entry.name, entry.value, entry.domain, entry.path, entry.expires, entry.maxAge.toString(),
        entry.httpOnly.toString(), entry.secure.toString(), entry.createdTime, entry.lastAccessedTime,
        entry.hostOnly.toString()];
        var writeResult = writeDataToCSVChannel(wCsvChannel2, rec);
        if (writeResult is error) {
            return writeResult;
        }
    }
    closeWritableCSVChannel(wCsvChannel2);
}

function writeDataToCSVChannel(io:WritableCSVChannel csvChannel, string[]... data) returns error? {
    foreach var rec in data {
        var returnedVal = csvChannel.write(rec);
        if (returnedVal is error) {
            return returnedVal;
        }
    }
}

function closeWritableCSVChannel(io:WritableCSVChannel csvChannel) {
    var result = csvChannel.close();
    if (result is error) {
        log:printError("Error occurred while closing the channel: ", result);
    }
}
