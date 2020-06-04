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

// Ballerina MIME Error Types
# Represents an `EncodeError` with the message and the cause.
public type EncodeError error;

# Represents a `DecodeError` with the message and the cause.
public type DecodeError error;

# Represents a `GenericMimeError` with the message and the cause.
public type GenericMimeError error;

# Represents a `SetHeaderError` with the message and the cause.
public type SetHeaderError error;

# Represents a `HeaderReadError` error with the message and the cause.
public type HeaderReadError error;

# Represents a `InvalidHeaderValueError` error with the message and the cause.
public type InvalidHeaderValueError error;

# Represents a `InvalidHeaderParamError` error with the message and the cause.
public type InvalidHeaderParamError error;

# Represents a `InvalidContentLengthError` error with the message and the cause.
public type InvalidContentLengthError error;

# Represents a `HeaderNotFoundError` error with the message and the cause.
public type HeaderNotFoundError error;

# Represents a `InvalidHeaderOperationError` error with the message and the cause.
public type InvalidHeaderOperationError error;

# Represents a `SerializationError` error with the message and the cause.
public type SerializationError error;

# Represents a `ParserError` with the message and the cause.
public type ParserError error;

# Represents an `InvalidContentTypeError` with the message and the cause.
public type InvalidContentTypeError error;

# Represents a `HeaderUnavailableError` with the message and the cause.
public type HeaderUnavailableError error;

# Represents an `IdleTimeoutTriggeredError` with the message and the cause.
public type IdleTimeoutTriggeredError error;

# Represents a `NoContentError` with the message and the cause.
public type NoContentError error;

# Represents MIME related errors.
public type Error ParserError|EncodeError|DecodeError|GenericMimeError|SetHeaderError|InvalidContentTypeError
                |HeaderReadError|HeaderUnavailableError|IdleTimeoutTriggeredError|NoContentError
                |InvalidHeaderValueError|InvalidHeaderParamError|InvalidContentLengthError
                |HeaderNotFoundError|InvalidHeaderOperationError|SerializationError;

# Constructs an `EncodeError` with the given details.
#
# + detail - Error details
# + return - An `EncodeError` with the given details set to the message
public function prepareEncodingErrorWithDetail(string detail) returns EncodeError {
    return EncodeError(detail);
}

# Constructs a `DecodeError` with the given details.
#
# + detail - Error details
# + return - `DecodeError` with the given details set to the message
public function prepareDecodingErrorWithDetail(string detail) returns DecodeError {
    return DecodeError(detail);
}
