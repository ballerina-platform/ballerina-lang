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

    function init(int flags) = external;

    public function visit(int versionNumber, int access, string name, string? signature, string superName,
                            string[]? interfaces) = external;

    public function visitMethod(int access, string name, string descriptor, string? signature,
                            string[]? exceptions) returns MethodVisitor = external;

    public function visitField(int access, string name, string descriptor, string? signature = (),
                            string[]? exceptions = ()) returns FieldVisitor = external;

    public function visitEnd() = external;

    public function visitSource(string fileName) = external;
    
    public function toByteArray() returns byte[]|error = external;
};


public type MethodVisitor object {
    public function visitInsn(int opcode) = external;

    public function visitIntInsn(int opcode, int operand) = external;

    public function visitVarInsn(int opcode, int variable) = external;

    public function visitTypeInsn(int opcode, string classType) = external;

    public function visitFieldInsn(int opcode, string owner, string name, string descriptor) = external;

    public function visitMethodInsn(int opcode, string owner, string name, string descriptor,
                            boolean isInterface) = external;

    public function visitJumpInsn(int opcode, Label label) = external;

    public function visitLabel(Label label) = external;

    public function visitLdcInsn(any value) = external;

    public function visitMaxs(int maxStack, int maxLocals) = external;

    public function visitCode() = external;

    public function visitEnd() = external;

    public function visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels) = external;

    public function visitInvokeDynamicInsn(string className, string lambdaName, int closureMapCount) = external;
    
    public function visitTryCatchBlock(Label startLabel, Label endLabel, Label handlerLabel,
                                        string? exceptionType) = external;
                                        
    public function visitLineNumber(int line, Label label) = external;

    public function visitLocalVariable(string varName, string descriptor, Label startLabel, Label endLabel, int index) = external;

    public function visitIincInsn(int variable, int amount) = external;
};


public type Label object {
    public function __init(){
        self.init();
    }

    function init() = external;
};

public type FieldVisitor object {
    public function visitEnd() = external;
};

