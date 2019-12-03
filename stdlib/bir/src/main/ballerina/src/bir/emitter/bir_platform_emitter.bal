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

import ballerina/io;

function (BPlatformType, int) returns string emitBPlatformType = function (BPlatformType bType, int tabs) returns string {
    error e = error(io:sprintf("platform type emitter not set, cannot emit type: %s", bType));
    panic e;
};

function (Instruction, int) returns string emitPlatformInstruction = function (Instruction ins, int tabs) returns string {
    error e = error(io:sprintf("platform instruction emitter not set, cannot emit instruction: %s", ins));
    panic e;
};

function (Terminator, int) returns string emitPlatformTerminator = function (Terminator term, int tabs) returns string {
    error e = error(io:sprintf("platform terminator emitter not set, cannot emit terminator: %s", term));
    panic e;
};


public function setPlatformTypeEmitter(function (BPlatformType, int) returns string typeEmitter) {
    emitBPlatformType = typeEmitter;
}

public function setPlatformInstructionEmitter(function (Instruction, int) returns string insEmitter) {
    emitPlatformInstruction = insEmitter;
}

public function setPlatformTerminatorEmitter(function (Terminator, int) returns string termEmitter) {
    emitPlatformTerminator = termEmitter;
}

