import ballerina.io;
import ballerina.config;

connector testConnector () {
    action testAction() {
        string a = config:("property");
    }
}