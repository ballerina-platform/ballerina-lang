import ballerina/io;

public function main() {
    string instance = "";
    string job = "";

    json payload = {};

    //io:println("payload: ", payload.toJsonString());

    [string, string]? data = getData(payload);
    if (data is ()) {
        io:println("Data not received");
    } else {
        io:println("Data received successfully: ", data);
    }

}

function getData(json payload) returns [string, string]? {
    map<anydata>|error data = payload.cloneWithType(map<anydata>);
    if (!(data is map<anydata>)) {
        return ();
    } else {
        var alerts = data["alerts"];
        if (!(alerts is anydata[])) {

        } else {
            foreach var alert in alerts {
                //io:println("alert: ", alert);

                var alertMap = alert.cloneWithType(map<anydata>);

                if (alertMap is map<anydata>) {
                    var status = alertMap["status"];

                    if (status == "firing") {
                        io:println("+++++ firing");

                        var labels = alertMap["labels"];
                        var labelsMap = labels.cloneWithType(map<anydata>);

                        if (labelsMap is map<anydata>) {

                            string instance;
                            string job;
                            var temp = labelsMap["instance"];
                            io:println("instance: ", temp);

                            if (temp is string) {
                                instance = temp;
                            }

                            temp = labelsMap["job"];
                            io:println("job: ", job);

                            if (temp is string) {
                                job = temp;
                            }
                            return [instance, jobl];
                        }
                    } else {
                        io:println("++++++other state");
                    }
                }
            }
        } else
{}
        io:println("not a map");
    }
}
