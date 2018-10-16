## Module Overview
This module provides functions to encapsulate multiple body parts, such as attachments in a single message. The communication of such messages follow the MIME (Multipurpose Internet Mail Extensions) specification as specified in the RFC 2045 standard.

### MIME specific terms 
The following terms are MIME specific and are extracted from the MIME specification.
#### Entity
This refers to the header fields and the content of a message, or a part of the body in a multipart entity. 

#### Body Part
This refers to an entity that is inside a multipart entity.
#### Body
This is the body of an entity, which can be a body of a message or the body of a multipart entity.
#### Header Fields
Content-Type, Content-Transfer-Encoding, Content-ID, Content-Description, and Content-Disposition are some of the MIME header fields. These headers exist along with the other headers in the `Entity`.

```
Content-Type: image/jpeg
Content-Disposition: attachment; filename=genome.jpeg;
Content-Description: a complete map of the human genome
```
### Modify and retrieve the data in an entity
The module provides functions to set and get an entity body from different kinds of message types, such as XML, text, JSON, blob, and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`, `removeHeader()`, etc. 
## Samples
### Handle multipart request
The sample service given below handles a multipart request. It extracts the body content from each part, converts it to to a `string`, and sends a response.

``` ballerina
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

@http:ServiceConfig {basePath:"/test"}
service<http:Service> test bind {port:9090} {

   @http:ResourceConfig {
       methods:["POST"],
       path:"/multipleparts"
   }
   // The resource that handles multipart requests.
   multipartResource(endpoint client, http:Request request) {
       http:Response response = new;

       // Get the body parts from the request.
       match request.getBodyParts() {
           // If the body parts were returned, iterate through each body part and handle the content.
           mime:Entity[] bodyParts => {
               string content = "";
               foreach part in bodyParts {
                    content = content + " -- " + handleContent(part);
               }
               response.setPayload(untaint content);
           }
           // If there is an error while getting the body parts, set the response code as 500 and 
           //set the error message as the response message.
          error err => {
              response.statusCode = 500;
              response.setPayload(untaint err.message);
          }
       }
       client -> respond(response) but { error e => log:printError("Error in responding", err = e) };
   }
}

// The function that handles the content based on the body part type.
function handleContent(mime:Entity bodyPart) returns (string) {
   // Get the base type of the specific body part.
   string baseType = check mime:getMediaType(bodyPart.getContentType())!getBaseType();

   // If the base type is ‘application/xml’ or ‘text/xml’, get the XML content from body part.
   if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
       var payload = bodyPart.getXml();
       match payload {
           error err => return err.message;
           xml xmlContent => return xmlContent.getTextValue();
       }
   } else if (mime:APPLICATION_JSON == baseType) {
       // If the base type is ‘application/json’, get the JSON content from body part.
       var payload = bodyPart.getJson();
       match payload {
           error err => return err.message;
           json jsonContent => {
               return jsonContent.toString();
           }
       }
   } 
   return "";
}
```

The sample request that is sent to the above service is shown below.

```
curl -v -F "request={\"param1\": \"value1\"};type=application/json" -F "language=ballerina;type=text/plain" -F "upload=@/home/path-to-file/encode.txt;type=application/octet-stream"  http://localhost:9090/test/multipleparts -H "Expect:"
```
### Create a multipart request
The sample given below creates a multipart request. It includes two body parts with `application/json` and `application/xml` content type.

``` ballerina
// Create a JSON body part.
mime:Entity bodyPart1 = new;
// Set the JSON content.
bodyPart1.setJson({"bodyPart":"jsonPart"});

// Create another body part using an XML file.
mime:Entity bodyPart2 = new;
bodyPart2.setFileAsEntityBody("ballerina/mime/file.xml", contentType = mime:APPLICATION_XML);

//Create an array to hold all the body parts.
mime:Entity[] bodyParts = [bodyPart1, bodyPart2];

// Set the body parts to the outbound response.
http:Request outRequest = new;
// Set the content type as ‘multipart/mixed’ and set the body parts.
outRequest.setBodyParts(bodyParts, contentType = mime:MULTIPART_MIXED);
```
