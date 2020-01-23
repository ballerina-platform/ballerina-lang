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

import ballerina/io;
import ballerina/log;
import ballerina/file;
import ballerina/time;

type myCookie record {
    string name;
    string value;
    string domain;
    string path;
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

# Represents a default persistent cookie handler which stores persistent cookies in a CSV file.
#
# + fileName - Name of the CSV file to store persistent cookies
public type CsvPersistentCookieHandler object {
    *PersistentCookieHandler;

    string fileName = "";

    public function __init(string fileName) {
        self.fileName = checkpanic validateFileExtension(fileName);
    }

    # Adds a persistent cookie to the cookie store.
    #
    # + cookie - Cookie to be added
    # + return - An error will be returned if there is any error occurred during storing the cookie or else nil is returned
    public function storeCookie(Cookie cookie) returns @tainted error? {
        table<myCookie> cookiesTable = table{};
        CookieHandlingError err;
        if (file:exists(self.fileName)) {
            var tblResult = readFile(self.fileName);
            if (tblResult is table<myCookie>) {
                cookiesTable = tblResult;
            } else {
                err = error(COOKIE_HANDLING_ERROR, message = "Error in reading the csv file", cause = tblResult);
                return err;
            }
        }
        var tableUpdateResult = addNewCookieToTable(cookiesTable, cookie);
        if (tableUpdateResult is table<myCookie>) {
            cookiesTable = tableUpdateResult;
        } else {
            err = error(COOKIE_HANDLING_ERROR, message = "Error in updating the records in csv file", cause = tableUpdateResult);
            return err;
        }
        var result = writeToFile(cookiesTable, self.fileName);
        if (result is error) {
            err = error(COOKIE_HANDLING_ERROR, message = "Error in writing the csv file", cause = result);
            return err;
        }
    }

    # Gets all persistent cookies.
    #
    # + return - Array of persistent cookies stored in the cookie store or else error is returned if occurred during getting the cookies
    public function getAllCookies() returns @tainted Cookie[] | error {
        Cookie[] cookies = [];
        if (file:exists(self.fileName)) {
            var tblResult = readFile(self.fileName);
            if (tblResult is table<myCookie>) {
                foreach var rec in tblResult {
                    Cookie cookie = new(rec.name, rec.value);
                    cookie.domain = rec.domain;
                    cookie.path = rec.path;
                    cookie.expires = rec.expires == "-" ? () : rec.expires;
                    cookie.maxAge = rec.maxAge;
                    cookie.httpOnly = rec.httpOnly;
                    cookie.secure = rec.secure;
                    time:Time | error t1 = time:parse(rec.createdTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    if (t1 is time:Time) {
                        cookie.createdTime = t1;
                    }
                    time:Time | error t2 = time:parse(rec.lastAccessedTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    if (t2 is time:Time) {
                        cookie.lastAccessedTime = t2;
                    }
                    cookie.hostOnly = rec.hostOnly;
                    cookies.push(cookie);
                }
                return cookies;
            } else {
                CookieHandlingError err = error(COOKIE_HANDLING_ERROR, message = "Error in reading the csv file", cause = tblResult);
                return err;
            }
        }
       return cookies;
   }

    # Removes a specific persistent cookie.
    #
    # + name - Name of the persistent cookie to be removed
    # + domain - Domain of the persistent cookie to be removed
    # + path - Path of the persistent cookie to be removed
    # + return - An error will be returned if there is any error occurred during removing the cookie or else nil is returned
    public function removeCookie(string name, string domain, string path) returns @tainted error? {
        cookieNameToRemove = name;
        cookieDomainToRemove = domain;
        cookiePathToRemove = path;
        CookieHandlingError err;
        if (file:exists(self.fileName)) {
            table<myCookie> cookiesTable = table{};
            var tblResult = readFile(self.fileName);
            if (tblResult is table<myCookie>) {
                cookiesTable = tblResult;
            } else {
                err = error(COOKIE_HANDLING_ERROR, message = "Error in reading the csv file", cause = tblResult);
                return err;
            }
            int | error count = cookiesTable.remove(checkRemoveCriteria);
            if (count is error || count <= 0) {
                err = error(COOKIE_HANDLING_ERROR, message = "Error in removing cookie: No such cookie to remove");
                return err;
            }
            error? removeResults = file:remove(self.fileName);
            if (removeResults is error) {
                err = error(COOKIE_HANDLING_ERROR, message = "Error in removing the csv file", cause = removeResults);
                return err;
            }
            var writeResult = writeToFile(cookiesTable, self.fileName);
            if (writeResult is error) {
                err = error(COOKIE_HANDLING_ERROR, message = "Error in writing the csv file", cause = writeResult);
                return err;
            }
            return;
        }
        err = error(COOKIE_HANDLING_ERROR, message = "Error in removing cookie: No persistent cookie store file to remove");
        return err;
    }

    # Removes all persistent cookies.
    #
    # + return - An error will be returned if there is any error occurred during removing all the cookies or else nil is returned
    public function removeAllCookies() returns error? {
        error? removeResults = file:remove(self.fileName);
        if (removeResults is error) {
            CookieHandlingError err = error(COOKIE_HANDLING_ERROR, message = "Error in removing the csv file", cause = removeResults);
            return err;
        }
    }
};

function validateFileExtension(string fileName) returns string|error {
    if (fileName.toLowerAscii().endsWith(".csv")) {
        return fileName;
    }
    CookieHandlingError err = error(COOKIE_HANDLING_ERROR, message = "Invalid file format");
    return err;
}

function readFile(string fileName) returns @tainted error | table<myCookie> {
    io:ReadableCSVChannel rCsvChannel2 = check io:openReadableCsvFile(fileName);
    var tblResult = rCsvChannel2.getTable(myCookie);
    closeReadableCSVChannel(rCsvChannel2);
    return  tblResult;
}

function closeReadableCSVChannel(io:ReadableCSVChannel csvChannel) {
    var result = csvChannel.close();
    if (result is error) {
        log:printError("Error occurred while closing the channel: ", err = result);
    }
}

// Updates the table with new cookie.
function addNewCookieToTable(table<myCookie> cookiesTable, Cookie cookieToAdd) returns table<myCookie> | error {
    table<myCookie> tableToReturn = cookiesTable;
    var name = cookieToAdd.name;
    var value = cookieToAdd.value;
    var domain = cookieToAdd.domain;
    var path = cookieToAdd.path;
    var expires = cookieToAdd.expires;
    var createdTime = time:format(cookieToAdd.createdTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    var lastAccessedTime = time:format(cookieToAdd.lastAccessedTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (name is string && value is string && domain is string && path is string && createdTime is string && lastAccessedTime is string) {
        myCookie c1 = { name: name, value: value, domain: domain, path: path, expires: expires is string ? expires : "-", maxAge: cookieToAdd.maxAge, httpOnly: cookieToAdd.httpOnly, secure: cookieToAdd.secure, createdTime: createdTime, lastAccessedTime: lastAccessedTime, hostOnly: cookieToAdd.hostOnly };
        var result = tableToReturn.add(c1);
        if (result is error) {
            return result;
        }
        return tableToReturn;
    }
    CookieHandlingError err = error(COOKIE_HANDLING_ERROR, message = "Invalid data types for cookie attributes");
    return err;
}

// Writes the updated table to file.
function writeToFile(table<myCookie> cookiesTable, string fileName) returns @tainted error? {
    io:WritableCSVChannel wCsvChannel2 = check io:openWritableCsvFile(fileName);
    foreach var entry in cookiesTable {
        string[] rec = [entry.name, entry.value, entry.domain, entry.path, entry.expires, entry.maxAge.toString(), entry.httpOnly.toString(), entry.secure.toString(), entry.createdTime, entry.lastAccessedTime, entry.hostOnly.toString()];
        var writeResult = writeDataToCSVChannel(wCsvChannel2, rec);
        if (writeResult is error) {
            return writeResult;
        }
    }
    closeWritableCSVChannel(wCsvChannel2);
}

function writeDataToCSVChannel(io:WritableCSVChannel csvChannel, string[]... data) returns error?{
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
        log:printError("Error occurred while closing the channel: ", err = result);
    }
}

function checkRemoveCriteria(myCookie rec) returns boolean {
    if (rec.name == cookieNameToRemove && rec.domain == cookieDomainToRemove && rec.path == cookiePathToRemove) {
        return true;
    }
    return false;
}
