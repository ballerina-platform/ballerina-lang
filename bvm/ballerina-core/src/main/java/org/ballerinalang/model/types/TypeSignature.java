/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.types;

import java.util.List;

/**
 * @since 0.87
 */
public class TypeSignature {
    public static final String SIG_INT = "I";
    public static final String SIG_FLOAT = "F";
    public static final String SIG_STRING = "S";
    public static final String SIG_BOOLEAN = "B";
    public static final String SIG_BLOB = "L";
    public static final String SIG_REFTYPE = "R";
    public static final String SIG_JSON = "J";
    public static final String SIG_TABLE = "D";
    public static final String SIG_FUTURE = "X";
    public static final String SIG_STREAM = "H";
    public static final String SIG_MAP = "M";
    public static final String SIG_CONNECTOR = "C";
    public static final String SIG_STRUCT = "T";
    public static final String SIG_ENUM = "E";
    public static final String SIG_FUNCTION = "U";
    public static final String SIG_ARRAY = "[";
    public static final String SIG_ANY = "A";
    public static final String SIG_TYPEDESC = "Y";
    public static final String SIG_VOID = "V";
    public static final String SIG_ANNOTATION = "@";
    public static final String SIG_UNION = "O";
    public static final String SIG_NULL = "N";
    public static final String SIG_TUPLE = "P";
    public static final String SIG_SINGLETON = "K";

    private String sigChar;
    private TypeSignature elementTypeSig;
    private List<TypeSignature> memberTypeSigs;
    private String pkgPath;
    private String name;

    public TypeSignature(String sigChar) {
        this.sigChar = sigChar;
    }

    public TypeSignature(String sigChar, TypeSignature elementTypeSig) {
        this(sigChar);
        this.elementTypeSig = elementTypeSig;
    }

    public TypeSignature(String sigChar, List<TypeSignature> memberTypeSigs) {
        this(sigChar);
        this.memberTypeSigs = memberTypeSigs;
    }
    
    public TypeSignature(String sigChar, String name) {
        this(sigChar);
        this.name = name;
    }

    public TypeSignature(String sigChar, String packagePath, String name) {
        this(sigChar);
        this.pkgPath = packagePath;
        this.name = name;
    }

    public String getSigChar() {
        return sigChar;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public String getName() {
        return name;
    }

    public TypeSignature getElementTypeSig() {
        return elementTypeSig;
    }

    @Override
    public String toString() {
        if (elementTypeSig != null) {
            return sigChar + elementTypeSig.toString();
        } else if (memberTypeSigs != null) {
            StringBuilder sig = new StringBuilder(sigChar + memberTypeSigs.size() + ";");
            memberTypeSigs.forEach(memberSig -> sig.append(memberSig));
            return sig.toString();
        } else if (pkgPath != null) {
            return sigChar + pkgPath + ":" + name + ";";
        } else if (name != null) {
            return sigChar + name + ";";
        }

        return sigChar;
    }
}
