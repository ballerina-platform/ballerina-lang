// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


@Description { value:"Removes each element that matches the given key."}
@Param { value:"j: A JSON object" }
@Param { value:"key: Key of the field to remove" }
public native function <json j> remove (string key);

@Description { value:"Converts a JSON object to a string representation"}
@Param { value:"j: A JSON object" }
@Return { value:"String value of the converted JSON" }
public native function <json j> toString () returns (string);

@Description { value:"Returns an array of keys contained in the specified JSON."}
@Param { value:"j: A JSON object" }
@Return { value:"A string array of keys contained in the specified JSON" }
public native function <json j> getKeys() returns (string[]);

@Description { value:"Converts a JSON object to a XML representation"}
@Param { value:"j: A JSON object" }
@Param { value:"options: jsonOptions struct for JSON to XML conversion properties" }
@Return { value:"The XML representation of the JSON" }
public native function <json j> toXML ({ string attributePrefix = "@";
                                           string arrayEntryTag = "item";
                                       } options) returns (xml| error);
