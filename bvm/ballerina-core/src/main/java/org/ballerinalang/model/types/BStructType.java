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

import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.StructInfo;

/**
 * {@code BStructType} represents a user defined {@code StructDef} in Ballerina.
 *
 * @since 1.0.0
 */
public class BStructType extends BType {

    public StructInfo structInfo;
    private StructField[] structFields;
    private AttachedFunction[] attachedFunctions;
    public AttachedFunction initializer;
    public AttachedFunction defaultsValuesInitFunc;
    private int[] fieldTypeCount;
    public int flags;

    /**
     * Create a {@code BStructType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkgPath  package of the struct
     */
    public BStructType(StructInfo structInfo, String typeName, String pkgPath, int flags) {
        super(typeName, pkgPath, BStruct.class);
        this.structInfo = structInfo;
        this.flags = flags;
    }

    public StructField[] getStructFields() {
        return structFields;
    }

    public void setStructFields(StructField[] structFields) {
        this.structFields = structFields;
    }

    public AttachedFunction[] getAttachedFunctions() {
        return attachedFunctions;
    }

    public void setAttachedFunctions(AttachedFunction[] attachedFunctions) {
        this.attachedFunctions = attachedFunctions;
    }

    public int[] getFieldTypeCount() {
        return fieldTypeCount;
    }

    public void setFieldTypeCount(int[] fieldCount) {
        this.fieldTypeCount = fieldCount;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BStruct(this);
    }

    @Override
    public TypeSignature getSig() {
        String packagePath = (pkgPath == null) ? "." : pkgPath;
        return new TypeSignature(TypeSignature.SIG_STRUCT, packagePath, typeName);
    }

    @Override
    public int getTag() {
        return TypeTags.STRUCT_TAG;
    }


    /**
     * This class represents struct field.
     *
     * @since 0.88
     */
    public static class StructField {
        public BType fieldType;
        public String fieldName;
        public int flags;

        public StructField(BType fieldType, String fieldName, int flags) {
            this.fieldType = fieldType;
            this.fieldName = fieldName;
            this.flags = flags;
        }

        public BType getFieldType() {
            return fieldType;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    /**
     * This class represents an attached function of a struct.
     *
     * @since 0.96
     */
    public static class AttachedFunction {
        public String funcName;
        public BFunctionType type;
        public int flags;

        public AttachedFunction(String funcName, BFunctionType type, int flags) {
            this.funcName = funcName;
            this.type = type;
            this.flags = flags;
        }
    }
}

