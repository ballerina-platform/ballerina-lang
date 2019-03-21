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

    public function toByteArray() returns byte[] = external;
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
