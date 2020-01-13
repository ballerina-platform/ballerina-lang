import ballerina/io;
import ballerina/log;

// Convert a `map<json>` value to an array of Strings.
// Return the result as a tuple, which contains the headers and fields.
function getFields(map<json> rec) returns [string[], string[]] {
    string [] fields = [];
    foreach var field in rec {
        fields[fields.length()] = field.toString();
    }
    return [rec.keys(), fields];
}

// Writes `json` content to CSV.
function writeCsv(json[] content, string path) returns @tainted error? {
    io:WritableCSVChannel csvch = check io:openWritableCsvFile(path);
    int recIndex = 0;
    int recLen = content.length();
    while (recIndex < recLen) {
        [string [], string []] result = getFields(<map<json>> content[recIndex]);
        var [headers, fields] = result;
        if (recIndex == 0) {
            //We ignore the result as this would mean a `nil` return
            check csvch.write(headers);
        }
        check csvch.write(fields);
        recIndex = recIndex + 1;
    }
}

public function main() {
    // The sample `json`, which will be written.
    json sample = {
            "employees": {
                "employee": [
                    {
                        "id": "1",
                        "firstName": "Tom",
                        "lastName": "Cruise",
                        "photo": "https://ballerina-team/profile/3737.jpg"
                    },
                    {
                        "id": "2",
                        "firstName": "Maria",
                        "lastName": "Sharapova",
                        "photo": "https://ballerina-team/profile/5676.jpg"
                    },
                    {
                        "id": "3",
                        "firstName": "James",
                        "lastName": "Bond",
                        "photo": "https://ballerina-team/profile/6776.jpg"
                    }
                ]
            }};
    // Writes JSON into a CSV.
    string path = "./files/sample.csv";
    // Specifies the JSON array, which should be transformed into CSV.
    // Also, provides the location the CSV should be written.
    var result = writeCsv(<json[]> sample.employees.employee, path);
    if (result is error) {
        log:printError("Error occurred while writing csv record :",
                        err = result);
    } else {
        io:println("json record successfully transformed to a csv, file could" +
                    " be found in " + path);
    }
}
