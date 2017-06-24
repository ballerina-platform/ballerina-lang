import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;
import ballerina.doc;

@doc:Description {value : "Service is invoke using BasePath value (/cbr)."}
@http:BasePath {value:"/cbr"}
service contentBasedRouting {
 @doc:Description {value : "http:POST{} annotation declares the HTTP method."}
 @http:POST{}
 @http:Path {value:"/route"}
 resource cbrResource (message m) {
   //Create service endpoint using HTTP client-connector.
   http:ClientConnector locationEP = create
              http:ClientConnector("http://www.mocky.io");
   //Declare a string within inverted commas
   string sanFranString = "sanFrancisco";
   //Native function to get JSON payload from message.
   //Try more native functions
   //Eg: For XML payloads, getXmlPayload() is used.
   json jsonMsg = messages:getJsonPayload(m);
   //getString() Native function retrieve the string value relevant to the key(name).
   //Some other natives: getFloat(), getInt(), getBoolean(), getJson().
   string nameString = jsons:getString(jsonMsg, "$.name");
   //Additionally HTTP HEAD request can be execute to verify the accessibility.
   http:ClientConnector.head(locationEP,"/v2/594e018c1100002811d6d39a",m);

   message response = {};
   //If-Else statement will direct message to one end point.
   if (nameString == sanFranString) {
       //"post" represent the POST action of HTTP connector. Route payload to relevant service as the server accept the entity enclosed
       response = http:ClientConnector
                    .post(locationEP,"/v2/594e018c1100002811d6d39a",m);
       //Additionally If the payload needed to be stored under requested resource path, PUT action will help as follows
       http:ClientConnector.put(locationEP,"/v2/594e018c1100002811d6d39a",m);

   } else {
       response = http:ClientConnector
                    .post(locationEP, "/v2/594e026c1100004011d6d39c", m);
       //Requested resources can be deleted using DELETE
       http:ClientConnector
                    .delete(locationEP, "/v2/594e026c1100004011d6d39c", m);
   }





   //Keyword "reply" sends the response back to the client.
   reply response;
 }

}

