import ballerina.net.http;
import ballerina.doc;

@doc:Description{value : "A service can be invoke by the name itself when basePath is not stated specifically. In this sample service basepath is "/passthrough" "}
service passthrough {
@doc:Description{value : "GET{} annotation adds the HTTP method"}
@http:GET{}
@http:Path {value:"/"}
resource passthrough (message m) {
	//While creating an End point, the host should be given as a parameter.
  http:ClientConnector endPoint = create 
    	http:ClientConnector("http://samples.openweathermap.org");
    //Action get() returns the response from backend service. It includes endPoint, resource path and message as parameters.
  message response = http:ClientConnector
            .get(endPoint, "/data/2.5/weather?lat=35&lon=139&appid=b1b1", m);
  reply response;

 }
    
}

