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

public type ClassWriter object {
    public function __init(int flags){
        self.init(flags);
    }

    extern function init(int flags);

    public extern function visit(int versionNumber, int access, string name, string? signature, string superName,
                            string[]? interfaces);

    public extern function visitMethod(int access, string name, string descriptor, string? signature,
                            string[]? exceptions) returns MethodVisitor;

    public extern function visitField(int access, string name, string descriptor, string? signature = (),
                            string[]? exceptions = ()) returns FieldVisitor;

    public extern function visitEnd();

    public extern function toByteArray() returns byte[];
};


public type MethodVisitor object {
    public extern function visitInsn(int opcode);

    public extern function visitIntInsn(int opcode, int operand);

    public extern function visitVarInsn(int opcode, int variable);

    public extern function visitTypeInsn(int opcode, string classType);

    public extern function visitFieldInsn(int opcode, string owner, string name, string descriptor);

    public extern function visitMethodInsn(int opcode, string owner, string name, string descriptor,
                            boolean isInterface);

    public extern function visitJumpInsn(int opcode, Label label);

    public extern function visitLabel(Label label);

    public extern function visitLdcInsn(any value);

    public extern function visitMaxs(int maxStack, int maxLocals);

    public extern function visitCode();

    public extern function visitEnd();

    public extern function visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels);
};


public type Label object {
    public function __init(){
        self.init();
    }

    extern function init();
};

public type FieldVisitor object {
    public extern function visitEnd();
};
