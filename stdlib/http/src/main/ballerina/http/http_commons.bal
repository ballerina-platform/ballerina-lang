// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

documentation {
    Defines the possible values for the chunking configuration in HTTP services and clients.

    `AUTO`: If the payload is less than 8KB, content-length header is set in the outbound request/response,
            otherwise chunking header is set in the outbound request/response
    `ALWAYS`: Always set chunking header in the response
    `NEVER`: Never set the chunking header even if the payload is larger than 8KB in the outbound request/response
}
public type Chunking "AUTO" | "ALWAYS" | "NEVER";

documentation {If the payload is less than 8KB, content-length header is set in the outbound request/response,
              otherwise chunking header is set in the outbound request/response.}
@final public Chunking CHUNKING_AUTO = "AUTO";

documentation {Always set chunking header in the response.}
@final public Chunking CHUNKING_ALWAYS = "ALWAYS";

documentation {Never set the chunking header even if the payload is larger than 8KB in the outbound request/response.}
@final public Chunking CHUNKING_NEVER = "NEVER";

documentation {
    Options to compress using gzip or deflate.

    `AUTO`: When service behaves as a HTTP gateway inbound request/response accept-encoding option is set as the
            outbound request/response accept-encoding option
    `ALWAYS`: Always set accept-encoding in outbound request/response
    `NEVER`: Never set accept-encoding header in outbound request/response
}
public type Compression "AUTO" | "ALWAYS" | "NEVER";

documentation {When service behaves as a HTTP gateway inbound request/response accept-encoding option is set as the
               outbound request/response accept-encoding option.}
@final public Compression COMPRESSION_AUTO = "AUTO";

documentation {Always set accept-encoding in outbound request/response.}
@final public Compression COMPRESSION_ALWAYS = "ALWAYS";

documentation {Never set accept-encoding header in outbound request/response.}
@final public Compression COMPRESSION_NEVER = "NEVER";

documentation {
    A record for providing trust store related configurations.

    F{{path}} Path to the trust store file
    F{{password}} Trust store password
}
public type TrustStore record {
    string path,
    string password,
};

documentation {
    A record for providing key store related configurations.

    F{{path}} Path to the key store file
    F{{password}} Key store password
}
public type KeyStore record {
    string path,
    string password,
};

documentation {
    A record for configuring SSL/TLS protocol and version to be used.

    F{{name}} SSL Protocol to be used (e.g.: TLS1.2)
    F{{versions}} SSL/TLS protocols to be enabled (e.g.: TLSv1,TLSv1.1,TLSv1.2)
}
public type Protocols record {
    string name,
    string[] versions,
};

documentation {
    A record for providing configurations for certificate revocation status checks.

    F{{enable}} The status of `validateCertEnabled`
    F{{cacheSize}} Maximum size of the cache
    F{{cacheValidityPeriod}} The time period for which a cache entry is valid
}
public type ValidateCert record {
    boolean enable,
    int cacheSize,
    int cacheValidityPeriod,
};

documentation {
    A record for providing configurations for certificate revocation status checks.

    F{{enable}} The status of OCSP stapling
    F{{cacheSize}} Maximum size of the cache
    F{{cacheValidityPeriod}} The time period for which a cache entry is valid
}
public type ServiceOcspStapling record {
    boolean enable,
    int cacheSize,
    int cacheValidityPeriod,
};

//////////////////////////////
/// Native implementations ///
//////////////////////////////

documentation {
    Parses the given header value to extract its value and parameter map.

    P{{headerValue}} The header value
    R{{}} Returns a tuple containing the value and its parameter map
}
//TODO: Make the error nillable
public extern function parseHeader (string headerValue) returns (string, map)|error;

function buildRequest(Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns Request {
    Request request = new;
    match message {
        () => {}
        Request req => {request = req;}
        string textContent => {request.setTextPayload(textContent);}
        xml xmlContent => {request.setXmlPayload(xmlContent);}
        json jsonContent => {request.setJsonPayload(jsonContent);}
        byte[] blobContent => {request.setBinaryPayload(blobContent);}
        io:ByteChannel byteChannelContent => {request.setByteChannel(byteChannelContent);}
        mime:Entity[] bodyParts => {request.setBodyParts(bodyParts);}
    }
    return request;
}

function buildResponse(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns Response {
    Response response = new;
    match message {
        () => {}
        Response res => {response = res;}
        string textContent => {response.setTextPayload(textContent);}
        xml xmlContent => {response.setXmlPayload(xmlContent);}
        json jsonContent => {response.setJsonPayload(jsonContent);}
        byte[] blobContent => {response.setBinaryPayload(blobContent);}
        io:ByteChannel byteChannelContent => {response.setByteChannel(byteChannelContent);}
        mime:Entity[] bodyParts => {response.setBodyParts(bodyParts);}
    }
    return response;
}
