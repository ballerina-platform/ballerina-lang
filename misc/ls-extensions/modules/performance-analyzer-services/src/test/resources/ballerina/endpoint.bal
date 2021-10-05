import ballerina/http;

http:Client clientEndpoint = check new ("https://api.chucknorris.io/jokes/");