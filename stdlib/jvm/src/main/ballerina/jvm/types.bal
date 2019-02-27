public type ClassWriter object {
    public function __init(int flags){
        self.init(flags);
    }

    extern function init(int flags);

    public extern function visit(int versionNumber, int access, string name, string? signature, string superName,
                            string[]? interfaces);

    public extern function visitMethod(int access, string name, string descriptor, string? signature,
                            string[]? exceptions) returns MethodVisitor;

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
};


public type Label object {
    public function __init(){
        self.init();
    }

    extern function init();
};
