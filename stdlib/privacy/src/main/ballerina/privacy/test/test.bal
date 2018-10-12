import ballerina/io;
import ballerina/test;
import ballerina/http;
import ballerina/math;

xmlns "http://services.samples/xsd" as axis;

function ex(string s) {

}

function createAccountDetails() {
    xml createReq = xml `<axis:accountHolderName>BallerinaUser</axis:accountHolderName>`;
    ex(axis:accountHolderName);
    if (1 == 1) {
       ex(axis:accountHolderName);
    }
}
