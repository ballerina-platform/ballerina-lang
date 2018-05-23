/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * {@code TypeSignatureReader} reads the type signature and creates type.
 * 
 * @param <T> Type of the type to be created from the signature.
 * 
 * @since 0.975.0
 */
public class TypeSignatureReader<T> {

    private TypeCreater<T> typeCreater;

    public TypeSignatureReader(TypeCreater<T> typeCreater) {
        this.typeCreater = typeCreater;
    }

    public int createBTypeFromSig(char[] chars, int index, Stack<T> typeStack) {
        int nameIndex;
        char typeChar = chars[index];
        switch (typeChar) {
            case 'I':
            case 'F':
            case 'S':
            case 'B':
            case 'L':
            case 'Y':
            case 'A':
            case 'N':
                typeStack.push(this.typeCreater.getBasicType(typeChar));
                return index + 1;
            case 'R':
                index++;
                nameIndex = index;
                while (chars[nameIndex] != ';') {
                    nameIndex++;
                }
                String typeName = new String(Arrays.copyOfRange(chars, index, nameIndex));
                typeStack.push(this.typeCreater.getBuiltinRefType(typeName));
                return nameIndex + 1;
            case 'C':
            case 'J':
            case 'T':
            case 'E':
            case 'D':
            case 'G':
            case 'Z':
                index++;
                nameIndex = index;
                int colonIndex = -1;
                while (chars[nameIndex] != ';') {
                    if (chars[nameIndex] == ':') {
                        colonIndex = nameIndex;
                    }
                    nameIndex++;
                }

                String pkgPath = null;
                String name;
                if (colonIndex != -1) {
                    pkgPath = new String(Arrays.copyOfRange(chars, index, colonIndex));
                    name = new String(Arrays.copyOfRange(chars, colonIndex + 1, nameIndex));
                } else {
                    name = new String(Arrays.copyOfRange(chars, index, nameIndex));
                }

                T constraintType = this.typeCreater.getRefType(typeChar, pkgPath, name);
                typeStack.push(this.typeCreater.getConstrainedType(typeChar, constraintType));

                return nameIndex + 1;
            case '[':
                index = createBTypeFromSig(chars, index + 1, typeStack);
                T elemType = typeStack.pop();
                typeStack.push(this.typeCreater.getArrayType(elemType));
                return index;
            case 'M':
            case 'H':
                index = createBTypeFromSig(chars, index + 1, typeStack);
                constraintType = typeStack.pop();
                typeStack.push(this.typeCreater.getConstrainedType(typeChar, constraintType));
                return index;
            case 'U':
                index++;
                index = createFunctionType(chars, index, typeStack);
                return index + 1;
            case 'O':
            case 'P':
                index++;
                nameIndex = index;
                while (chars[nameIndex] != ';') {
                    nameIndex++;
                }
                List<T> memberTypes = new ArrayList<>();
                int memberCount = Integer.parseInt(new String(Arrays.copyOfRange(chars, index, nameIndex)));
                index = nameIndex;
                for (int i = 0; i < memberCount; i++) {
                    index = createBTypeFromSig(chars, index + 1, typeStack) - 1;
                    memberTypes.add(typeStack.pop());
                }

                typeStack.push(this.typeCreater.getCollenctionType(typeChar, memberTypes));
                return index + 1;
            default:
                throw new IllegalArgumentException("unsupported base type char: " + typeChar);
        }
    }

    public T getBTypeFromDescriptor(String desc) {
        char ch = desc.charAt(0);
        switch (ch) {
            case 'I':
            case 'F':
            case 'S':
            case 'B':
            case 'Y':
            case 'L':
            case 'A':
            case 'N':
                return this.typeCreater.getBasicType(ch);
            case 'R':
                String typeName = desc.substring(1, desc.length() - 1);
                return this.typeCreater.getBuiltinRefType(typeName);
            case 'M':
            case 'H':
                T constraintType = getBTypeFromDescriptor(desc.substring(1));
                return this.typeCreater.getConstrainedType(ch, constraintType);
            case 'C':
            case 'X':
            case 'J':
            case 'T':
            case 'E':
            case 'Z':
            case 'G':
            case 'D':
                typeName = desc.substring(1, desc.length() - 1);
                String[] parts = typeName.split(":");

                if (parts.length == 1) {
                    if (ch == 'J' || ch == 'D') {
                        return this.typeCreater.getConstrainedType(ch, null);
                    }
                }

                String pkgPath = parts[0];
                String name = parts[1];

                constraintType = this.typeCreater.getRefType(ch, pkgPath, name);
                return this.typeCreater.getConstrainedType(ch, constraintType);
            case '[':
                T elemType = getBTypeFromDescriptor(desc.substring(1));
                return this.typeCreater.getArrayType(elemType);
            case 'U':
            case 'O':
            case 'P':
                Stack<T> typeStack = new Stack<>();
                createBTypeFromSig(desc.toCharArray(), 0, typeStack);
                return typeStack.pop();
            default:
                throw new IllegalArgumentException("unsupported base type char: " + ch);
        }
    }

    public int createFunctionType(char[] chars, int index, Stack<T> typeStack) {
        // Skip the first parenthesis
        index++;

        // Read function parameters
        Stack<T> funcParamsStack = new Stack<>();
        while (chars[index] != ')' || chars[index + 1] != '(') {
            index = createBTypeFromSig(chars, index, funcParamsStack);
        }

        // Read function return type.
        // Skip the two parenthesis ')(', which separate params and return params
        index += 2;
        T retType;
        if (chars[index] == ')') {
            retType = null;
        } else {
            Stack<T> returnParamsStack = new Stack<>();
            index = createBTypeFromSig(chars, index, returnParamsStack);
            retType = returnParamsStack.pop();
        }

        typeStack.push(this.typeCreater.getFunctionType(funcParamsStack, retType));
        return index;
    }
}
