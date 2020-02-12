package org.wso2.ballerinalang.compiler.bir.codegen;

import com.sun.codemodel.internal.JPrimitiveType;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

public class JType extends BType {
    public String type;

    public JType(int tag, BTypeSymbol tsymbol) {
        super(JTypeTags.JTYPE, tsymbol);
    }

    public static String JBYTE_KIND = "byte";
    public static String JCHAR_KIND = "char";
    public static String JSHORT_KIND = "short";
    public static String JINT_KIND = "int";
    public static String JLONG_KIND = "long";
    public static String JFLOAT_KIND = "float";
    public static String JDOUBLE_KIND = "double";
    public static String JBOOLEAN_KIND = "boolean";
    public static String JVOID_KIND = "void";
    public static String JARRAY_KIND = "array";
    public static String JREF_KIND = "ref";
    public static String JNO_TYPE_KIND = "no";


    public static class JByte {
}

    public static class JChar extends JType {
       public JChar(){
           super();
        }
        JCHAR_KIND jTypeKind = JCHAR_KIND;
}

    public static class JShort {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JSHORT_KIND jTypeKind = JSHORT_KIND;
}

    public static class JInt {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JINT_KIND jTypeKind = JINT_KIND;
}

    public static class JLong {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JLONG_KIND jTypeKind = JLONG_KIND;
}

    public static class JFloat {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JFLOAT_KIND jTypeKind = JFLOAT_KIND;
}

    public static class JDouble {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JDOUBLE_KIND jTypeKind = JDOUBLE_KIND;
}

    public static class JBoolean {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JBOOLEAN_KIND jTypeKind = JBOOLEAN_KIND;
}

    public static class JVoid {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JVOID_KIND jTypeKind = JVOID_KIND;
}

    public type JPrimitiveType JByte | JChar | JShort | JInt | JLong | JFloat | JDouble | JBoolean | JVoid;

    public static class JArrayType {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JARRAY_KIND jTypeKind = JARRAY_KIND;
        JType elementType;
}

    public static class JRefType {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JREF_KIND jTypeKind = JREF_KIND;
        String type;
        boolean isInterface = false;
        boolean isArray = false;
    };

    public static class JNoType {
        BIRPLATFORM_TYPE_NAME typeName = BIRPLATFORM_TYPE_NAME;
        JNO_TYPE_KIND jTypeKind = JNO_TYPE_KIND;
}

//    public type JType com.sun.codemodel.internal.JPrimitiveType | JRefType | JNoType | JArrayType;

    public static JPrimitiveType|JRefType getJTypeFromTypeName(String typeName) {
        if (typeName == JBYTE_KIND) {
            return (JByte) {};
        } else if (typeName == JCHAR_KIND) {
            return (JChar) {};
        } else if (typeName == JSHORT_KIND) {
            return (JShort) {};
        } else if (typeName == JINT_KIND) {
            return (JInt) {};
        } else if (typeName == JLONG_KIND) {
            return (JLong) {};
        } else if (typeName == JFLOAT_KIND) {
            return (JFloat) {};
        } else if (typeName == JDOUBLE_KIND) {
            return (JDouble) {};
        } else if (typeName == JBOOLEAN_KIND) {
            return (JBoolean) {};
        } else {
            return (JRefType) {
                    type: typeName
        };
        }
    }

    public static JArrayType getJArrayTypeFromTypeName(String typeName, byte dimensions) {
        JArrayType arrayType = new JArrayType( elementType : getJTypeFromTypeName(typeName));
        int i = 1;
        while(i < ((int)dimensions)) {
            JArrayType temp = new JArrayType( elementType : arrayType);
            arrayType = temp;
            i += 1;
        }

        return arrayType;
    }
}
