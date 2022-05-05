import ballerina/module1;

type Message record {|
    string id;
    string text;
|};

service / on new module1:Listener(9090) {

    resource function get data() returns record {|*Message; string next;|} {

    }
}
