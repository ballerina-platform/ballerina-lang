## Package Overview
This package provides functions to transmit and receive messages that have multiple parts, such as an attachment. The communication of such messages follow the MIME (Multipurpose Internet Mail Extensions) specification as specified in the RFC 2045 standard.
### MIME Specific terms 
The following terms are MIME specific and are extracted from the MIME specification.
#### Entity
This refers to the header fields and the content of a message, or a part of the body in a multipart entity. 

#### Body Part
This refers to an entity that is inside a multipart entity.
#### Body
This is the body of an entity, which can be a body of a message or the body of a multipart entity.
#### Header Fields
Content-Type, Content-Transfer-Encoding, Content-ID, Content-Description, and Content-Disposition are some MIME header fields. These headers exist along with the other headers in the `Entity`.

```
Content-Type: image/jpeg
Content-Disposition: attachment; filename=genome.jpeg;
Content-Description: a complete map of the human genome
```
### Modify and retrieve the data in an `Entity`
The package provides functions to set and get an entity body from different kinds of message types.  For example, XML, text, JSON, blob, and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`, `removeHeader(), etc.

### Encode and decode the body of an `Entity`
The content of the messages needs to be encoded and decoded when the `Content-Transfer-Encoding` header is used. Ballerina supports Base64 encoding.

The `base64EncodeString()` and `base64DecodeString` encodes and decodes `string` variables. 
## Samples
### Handle multipart request
The sample service given below handles a multipart request. It gets the message from each part of the body, converts the messages to a `string`, and sends a response.

``` ballerina
import ballerina/http;
import ballerina/io;
import ballerina/mime;

endpoint http:Listener mockEP {
   port:9090
};

@http:ServiceConfig {basePath:"/test"}
service<http:Service> test bind mockEP {

   @http:ResourceConfig {
       methods:["POST"],
       path:"/multipleparts"
   }
   // The resource that handles multipart requests.
   multipartResource(endpoint client, http:Request request) {
       http:Response response = new;

       // Get the body parts from the request.
       match request.getBodyParts() {
           // If there is an error while getting the body parts, set the response code as 500 and set the error message as the response message.
           mime:EntityError err => {
               response.statusCode = 500;
               response.setStringPayload(err.message);
           }
           // If the body parts were returned, iterate through each body part and handle the content.
           mime:Entity[] bodyParts => {
               string content = "";
               int i = 0;
               while (i < lengthof bodyParts) {
                   mime:Entity part = bodyParts[i];
                   content = content + " -- " + handleContent(part);
                   i = i + 1;
               }
               response.setStringPayload(content);
           }
       }
       _ = client -> respond(response);
   }

}

// The function that handles the content based on the body part type.
function handleContent(mime:Entity bodyPart) returns (string) {
   // Getting the base type of the specific body part.
   string baseType = bodyPart.contentType.getBaseType();

   // If the base type is ‘application/xml’ or ‘text/xml’, get the XML content from body part.
   if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
       var payload = bodyPart.getXml();
       match payload {
           mime:EntityError err => return "Error in getting xml payload";
           xml xmlContent => return xmlContent.getTextValue();
       }
   } else if (mime:APPLICATION_JSON == baseType) {
       // If the base type is ‘application/json’, get the JSON content from body part.
       var payload = bodyPart.getJson();
       match payload {
           mime:EntityError err => return "Error in getting json payload";
           json jsonContent => {
               return jsonContent.toString();
           }
       }
   } 
   return "";
}
```

The sample request that is sent to the above service and the response that is printed on the console are shown below.

```
curl -v -F "request={\"param1\": \"value1\"};type=application/json" -F "language=ballerina;type=text/plain" -F "upload=@/home/megala/encode.txt;type=application/octet-stream"  http://localhost:9090/test/multipleparts -H "Expect:"
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> POST /test/multipleparts HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
> Content-Length: 521
> Content-Type: multipart/form-data; boundary=------------------------bf3f6dafe84260f2
> 
< HTTP/1.1 200 OK
< content-type: text/plain
< content-length: 77
< server: ballerina/0.970.0-beta1-SNAPSHOT
< date: Wed, 18 Apr 2018 22:20:48 +0530
< 
 -- {"param1":"value1"} -- ballerina -- VGhpcyBpcyBhIGJhbGxlcmluYSBzYW1wbGU=
```
### Create a multipart response
The sample given below creates a multipart response. It includes ‘application/json’ and ‘text/xml’ type content.

``` ballerina
// Create a new body part.
mime:Entity bodyPart1 = new;
// Set the content type as ‘application/json’.
bodyPart1.contentType = mime:getMediaType(mime:APPLICATION_JSON);
// Finally, set the JSON content.
bodyPart1.setJson({"bodyPart":"jsonPart"});

// Create body part using an XML file.
mime:Entity bodyPart2 = new;
mime:MediaType textXml = mime:getMediaType(mime:TEXT_XML);
bodyPart2.contentType = textXml;
file:Path fileHandler = new("ballerina/mime/file.xml");
bodyPart2.setFileAsEntityBody(fileHandler);

//Create an array to hold all the body parts.
mime:Entity[] bodyParts = [bodyPart1, bodyPart2];

// Set the body parts to the outbound response.
http:Response outResponse = new;
// Set the content type as ‘multipart/mixed’ and set the body parts.
outResponse.setBodyParts(bodyParts, mime:MULTIPART_MIXED);
```
