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


documentation {
    Logs the specified value at DEBUG level.
    P{{msg}} The message to be logged
}
public extern function printDebug(string msg);

documentation {
    Logs the specified message at ERROR level.
    P{{msg}} The message to be logged
    P{{err}} The error struct to be logged
}
public extern function printError(string msg, error? err = ());

documentation {
    Logs the specified message at INFO level.
    P{{msg}} The message to be logged.
}
public extern function printInfo(string msg);

documentation {
    Logs the specified message at TRACE level.
    P{{msg}} The message to be logged
}
public extern function printTrace(string msg);

documentation {
    Logs the specified message at WARN level.
    P{{msg}} The message to be logged
}
public extern function printWarn(string msg);