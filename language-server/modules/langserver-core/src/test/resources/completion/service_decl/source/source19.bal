import ballerina/http;
import ballerina/websub;

websub:Listener websubLi = check new websub:Listener(9090);
listener http:Listener httpListener = check new http:Listener(9091);

service on  {
    
}
