/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.cli;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that has the cli argument definitions.
 *
 * @since 2.0.0
 */
public class CliSpec {
    private final Option option;
    private final Operand[] operands;
    private final List<Object> mainArgs;
    private final String[] args;

    public CliSpec(Option option, Operand[] operands, String... args) {
        this.option = option;
        this.operands = operands;
        this.args = args;
        mainArgs = new ArrayList<>();
    }

    public Object[] getMainArgs() {
        // First argument is for the strand
        mainArgs.add(null);
        if (option != null) {
            BMap<BString, Object> recordVal = option.parseRecord(args);
            processOperands(option.getOperandArgs());
            int optionLocation = option.getLocation() * 2 + 1;
            mainArgs.add(optionLocation, recordVal);
            mainArgs.add(optionLocation + 1, true);
        } else {
            RecordType type = TypeCreator.createRecordType("dummy", null, 1, new HashMap<>(), null, true, 6);
            Option dummyOption = new Option(type, ValueCreator.createRecordValue(type));
            dummyOption.parseRecord(args);
            processOperands(dummyOption.getOperandArgs());
        }
        return mainArgs.toArray();
    }

    private void processOperands(List<String> operandArgs) {
        int opIndex = 0;
        Object bValue;
        int argIndex = 0;
        while (argIndex < operandArgs.size() && opIndex < operands.length) {
            Operand curOperand = operands[opIndex++];
            Type typeOp = curOperand.type;
            if (typeOp.getTag() == TypeTags.ARRAY_TAG) {
                ArrayType arrayType = (ArrayType) typeOp;
                BArray bArray = ValueCreator.createArrayValue(arrayType, -1);
                Type elementType = arrayType.getElementType();
                int elementCount = getElementCount(operands, opIndex);
                while (argIndex < operandArgs.size() - elementCount) {
                    try {
                        bArray.append(CliUtil.getBValue(elementType, operandArgs.get(argIndex++), curOperand.name));
                    } catch (BError error) {
                        argIndex--;
                        break;
                    }
                }
                bValue = bArray;
            } else {
                bValue = CliUtil.getBValueWithUnionValue(curOperand.type, operandArgs.get(argIndex++), curOperand.name);
            }
            mainArgs.add(bValue);
            mainArgs.add(true);
        }
        if (argIndex < operandArgs.size()) {
            throw ErrorCreator.createError(StringUtils.fromString("all operand arguments are not matched"));
        }
        handleMainParametersAtTheEnd(opIndex);
    }

    private void handleMainParametersAtTheEnd(int opIndex) {
        while (opIndex < operands.length) {
            Operand operand = operands[opIndex++];
            Type opType = operand.type;
            if (operand.hasDefaultable) {
                mainArgs.add(getDefaultBValue(opType));
                mainArgs.add(false);
            } else if (isSupportedArrayType(opType)) {
                mainArgs.add(ValueCreator.createArrayValue((ArrayType) opType, -1));
                mainArgs.add(true);
            } else if ((CliUtil.isUnionWithNil(opType))) {
                mainArgs.add(null);
                mainArgs.add(true);
            } else {
                throw ErrorCreator.createError(StringUtils.fromString(
                        "missing operand arguments for parameter '" + operand.name + "' of type '" + opType + "'"));
            }
        }
    }

    private boolean isSupportedArrayType(Type opType) {
        if (opType.getTag() == TypeTags.ARRAY_TAG) {
            Type elementType = ((ArrayType) opType).getElementType();
            return CliUtil.isSupportedType(elementType.getTag());
        }
        return false;
    }

    private static Object getDefaultBValue(Type type) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BYTE_TAG:
                return 0;
            case TypeTags.BOOLEAN_TAG:
                return false;
            default:
                return null;
        }
    }

    private int getElementCount(Operand[] operands, int opIndex) {
        int count = 0;
        while (opIndex < operands.length && operands[opIndex++].type.getTag() != TypeTags.RECORD_TYPE_TAG) {
            count++;
        }
        return count;
    }
}
