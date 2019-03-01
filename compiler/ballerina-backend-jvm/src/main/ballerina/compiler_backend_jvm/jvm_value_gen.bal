public type ObjectGenerator object {

    // hold the mapping between the object name and the class name
    map<string> valueClass = {};

    public function generateClasses(bir:TypeDef[] typeDefs, map<byte[]> jarEntries) {
        foreach var typeDef in typeDefs {
            bir:BType bType = typeDef.typeValue;
            if (bType is bir:BObjectType) {
                string className = typeDef.name.value;
                byte[] bytes = self.createObjectValueClass(bType, className);
                jarEntries[className + ".class"] = bytes;
            }
        }
    }

    // Private methods

    function createObjectValueClass(bir:BObjectType objectType, string className) returns byte[] {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, OBJECT, [OBJECT_VALUE]);

        bir:BObjectField[] fields = objectType.fields;
        self.createFields(cw, fields);
        // self.createInit(cw);
        self.createCallMethod(cw);
        self.createGetMethod(cw, fields, className);
        self.createSetMethod(cw, fields);
        cw.visitEnd();
        return cw.toByteArray();
    }

    function createFields(jvm:ClassWriter cw, bir:BObjectField[] fields) {
        foreach var field in fields {
            jvm:FieldVisitor fv = cw.visitField(0, field.name.value, getTypeDesc(field.typeValue));
            fv.visitEnd();
        }
    }

    function createInit(jvm:ClassWriter cw) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    function createCallMethod(jvm:ClassWriter cw) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call",
                io:sprintf("(L%s;[L%s;)L%s;", STRING_VALUE, OBJECT, OBJECT),
                null, null);
        mv.visitCode();
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    function createGetMethod(jvm:ClassWriter cw, bir:BObjectField[] fields, string className) {
        int size = fields.length();
        jvm:Label[] labels = [];
        int[] hashCodes = [];

        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, OBJECT), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

        // Create labels for the cases
        int i = 0;
        foreach var field in fields {
            labels[i] = new jvm:Label();
            hashCodes[i] = field.name.value.hashCode();
            i += 1;
        }
        jvm:Label defaultCaseLabel = new jvm:Label();
        mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels);

        jvm:Label[] targetLabels = [];
        i = 0;
        while (i < size) {
            mv.visitLabel(labels[i]);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(fields[i].name.value);
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    io:sprintf("(L%s;)Z", OBJECT), false);
            jvm:Label targetLabel = new jvm:Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels[i] = targetLabel;
            i += 1;
        }

        i = 0;
        foreach var field in fields {
            jvm:Label targetLabel = targetLabels[i];
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, field.name.value, getTypeDesc(field.typeValue));
            addBoxInsn(mv, field.typeValue);
            mv.visitInsn(ARETURN);
            i += 1;
        }

        mv.visitLabel(defaultCaseLabel);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("error!");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>",
                io:sprintf("(L%s;)V", STRING_VALUE), false);
        mv.visitInsn(ATHROW);

        mv.visitMaxs(size + 10, size + 10);
        mv.visitEnd();
    }

    function createSetMethod(jvm:ClassWriter cw, bir:BObjectField[] fields) {
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set",
                io:sprintf("(L%s;[L%s;)V", STRING_VALUE, OBJECT),
                null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }
};

function addBoxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    if (bType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else {
        return;
    }
}
