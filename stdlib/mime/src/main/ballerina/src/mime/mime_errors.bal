// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Holds the details of the entity header and body-related errors.
#
# + message - Error message
# + cause - Error cause
public type Detail record {
    string message;
    error cause?;
};

// Ballerina MIME Error Types

# Identifies encoding errors.
public const ENCODE_ERROR = "{ballerina/mime}EncodingFailed";

# Represents an `EncodeError` with a detailed message.
public type EncodeError error<ENCODE_ERROR, Detail>;

# Identifies decoding errors.
public const DECODE_ERROR = "{ballerina/mime}DecodingFailed";

# Represents a `DecodeError` with a detailed message.
public type DecodeError error<DECODE_ERROR, Detail>;

# Identifies generic errors related to MIME.
public const GENERIC_MIME_ERROR = "{ballerina/mime}GenericMimeError";

# Represents a `GenericMimeError` with a detailed message.
public type GenericMimeError error<GENERIC_MIME_ERROR, Detail>;

# Identifies the set header errors.
public const SET_HEADER_FAILED = "{ballerina/mime}SetHeaderFailed";

# Represents a `SetHeaderError` with a detailed message.
public type SetHeaderError error<SET_HEADER_FAILED, Detail>;

// TODO: Give a proper name for this.
# Identifies header parsing errors.
public const READING_HEADER_FAILED = "{ballerina/mime}ReadingHeaderFailed";

# Represents a `ReadingHeaderFailed` error with a detailed message.
public type ReadingHeaderFailed error<READING_HEADER_FAILED, Detail>;

# Identifies entity body parsing errors.
public const PARSER_ERROR = "{ballerina/mime}ParsingEntityBodyFailed";

# Represents a `ParserError` with a detailed message.
public type ParserError error<PARSER_ERROR, Detail>;

# Identifies errors related to content-type header.
public const INVALID_CONTENT_TYPE = "{ballerina/mime}InvalidContentType";

# Represents an `InvalidContentTypeError` with a detailed message.
public type InvalidContentTypeError error<INVALID_CONTENT_TYPE, Detail>;

# Identifies errors related to header unavailability.
public const HEADER_UNAVAILABLE = "{ballerina/mime}HeaderUnavailable";

# Represents a `HeaderUnavailableError` with a detailed message.
public type HeaderUnavailableError error<HEADER_UNAVAILABLE, Detail>;

# Identifies errors related to read/write timeouts.
public const IDLE_TIMEOUT_TRIGGERED = "{ballerina/mime}IdleTimeoutTriggeredError";

# Represents an `IdleTimeoutTriggeredError` with a detailed message.
public type IdleTimeoutTriggeredError error<IDLE_TIMEOUT_TRIGGERED, Detail>;

# Identifies the errors occurred due to payloads with no content.
public const NO_CONTENT_ERROR_CODE = "{ballerina/mime}NoContentError";

# Represents a `NoContentError` with a detailed message.
public type NoContentError error<NO_CONTENT_ERROR_CODE, Detail>;

# Represents MIME related errors.
public type Error ParserError|EncodeError|DecodeError|GenericMimeError|SetHeaderError|InvalidContentTypeError
                |ReadingHeaderFailed|InvalidContentTypeError|HeaderUnavailableError|IdleTimeoutTriggeredError
                |NoContentError;

# Constructs an `EncodeError` with the given details.
# + detail - Error details
# + return - An `EncodeError` with the given details set to the message
public function prepareEncodingErrorWithDetail(string detail) returns EncodeError {
    return error(ENCODE_ERROR, message = detail);
}

# Constructs a `DecodeError` with the given details.
# + detail - Error details
# + return - `DecodeError` with the given details set to the message
public function prepareDecodingErrorWithDetail(string detail) returns DecodeError {
    return error(DECODE_ERROR, message = detail);
}
