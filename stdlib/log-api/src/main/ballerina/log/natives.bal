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
import ballerina/time;

@final public string DEBUG_LEVEL = "debug";
@final public string ERROR_LEVEL = "error";
@final public string INFO_LEVEL = "info";
@final public string TRACE_LEVEL = "trace";
@final public string WARN_LEVEL = "warn";

stream<Log> logStream;

public type Log record {
 int time;
 string message;
 string level;
 string err;
};

public function printDebug(string msg) {
 nativePrintDebug(msg);
 Log log = { time: getTime(), message:msg, level: DEBUG_LEVEL };
 logStream.publish(log);
}

documentation {
    Logs the specified value at DEBUG level.
    P{{msg}} The message to be logged
}
native function nativePrintDebug(string msg);

public function printError(string msg, error? err = ()) {
 nativePrintError(msg, err = err);
 string errString;
 match err {
  error e => {
   json jsonError = check <json>e;
   errString = jsonError.toString();
  }
  () => errString = "";
 }
 Log log = { time: getTime(), message:msg, level: ERROR_LEVEL, err: errString };
 logStream.publish(log);
}
documentation {
    Logs the specified message at ERROR level.
    P{{msg}} The message to be logged
    P{{err}} The error struct to be logged
}
native function nativePrintError(string msg, error? err = ());

public function printInfo(string msg) {
 nativePrintInfo(msg);
 Log log = { time: getTime(), message:msg, level: INFO_LEVEL };
 logStream.publish(log);
}

documentation {
    Logs the specified message at INFO level.
    P{{msg}} The message to be logged.
}
native function nativePrintInfo(string msg);

public function printTrace(string msg) {
 nativePrintTrace(msg);
 Log log = { time: getTime(), message:msg, level: TRACE_LEVEL };
 logStream.publish(log);
}

documentation {
    Logs the specified message at TRACE level.
    P{{msg}} The message to be logged
}
native function nativePrintTrace(string msg);

public function printWarn(string msg) {
 nativePrintWarn(msg);
 Log log = { time: getTime(), message:msg, level: WARN_LEVEL };
 logStream.publish(log);
}

documentation {
    Logs the specified message at WARN level.
    P{{msg}} The message to be logged
}
native function nativePrintWarn(string msg);

public function subscribe(function(Log) logsCallBack) {
 logStream.subscribe(logsCallBack);
}

function getTime() returns int {
 time:Time currentTime = time:currentTime();
 return currentTime.milliSecond();
}
