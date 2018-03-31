import ballerina/io;
import ballerina/config;

function testFunction () {
    string a = config:("property");
}