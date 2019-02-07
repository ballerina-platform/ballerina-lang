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

public extern function classWriterInit();

public extern function classWriterVisit(string className);

public extern function classWriterEnd();

public extern function visitMethodInit(int access, string name, string descriptor);

public extern function visitMethodCode();

public extern function visitMethodEnd();

public extern function visitMethodInstruction(int opcode, string className, string methodName,
                                                string methodDescriptor, boolean isInterface);

public extern function visitNoOperandInstruction(int opcode);

public extern function visitSingleOperandInstruction(int opcode, int operand);

public extern function visitVariableInstruction(int opcode, int varIndex);

public extern function visitLoadConstantInstruction(any value);

public extern function visitTypeInstruction(int opcode, string className);

public extern function createLabel(string labelId);

public extern function visitLabel(string labelId);

public extern function visitJumpInstruction(int jumpType, string labelId);

public extern function visitMaxStackValues(int maxStack, int maxLocal);

public extern function getClassFileContent() returns byte[];

public extern function visitFieldInstruction(int opcode, string className, string fieldName, string fieldDescriptor);
