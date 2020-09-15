// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.__internal as internal;

class FilterSupport {
    public stream<Type, ErrorType> strm;
    public any func;

    public function init(stream<Type, ErrorType> strm, function(Type val) returns boolean func) {
      self.strm = strm;
      self.func = func;
    }

    public isolated function next() returns record {|Type value;|}|ErrorType? {
        // while loop is required to continue filtering until we find a value which matches the filter or ().
        while(true) {
            var nextVal = next(self.strm);
            if (nextVal is ()) {
                return ();
            } else if (nextVal is error) {
                return nextVal;
            } else {
                var value = nextVal?.value;
                function(any|error) returns boolean func = internal:getFilterFunc(self.func);
                if (func(value)) {
                    return nextVal;
                }
            }
        }
        return ();
    }
}

class MapSupport {
    public stream<Type, ErrorType> strm;
    public any func;

    public function init(stream<Type, ErrorType> strm, function(Type val) returns Type1 func) {
        self.strm = strm;
        self.func = func;
    }

    public isolated function next() returns record {|Type value;|}|ErrorType? {
        var nextVal = next(self.strm);
        if (nextVal is ()) {
            return ();
        } else {
            function(any | error) returns any | error mappingFunc = internal:getMapFunc(self.func);
            if (nextVal is error) {
                 return nextVal;
            } else {
                 var value = mappingFunc(nextVal.value);
                 return internal:setNarrowType(typeof value, {value : value});
            }
        }
    }
}