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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

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
    private Object[] mainArgs;
    private final String[] args;

    public CliSpec(Option option, Operand[] operands, String[] args) {
        //Todo: refactor code and write tests
        this.option = option;
        this.operands = operands;
        this.args = args;
    }

    public Object[] getMainArgs() {
        if (option != null) {
            BMap<BString, Object> record = option.parseRecord(args);
            int size = (operands.length + 1) * 2 + 1;
            mainArgs = new Object[size];
            processOperands(option.getOperandArgs());
            mainArgs[size - 2] = record;
            mainArgs[size - 1] = true;
        } else {
            mainArgs = new Object[operands.length * 2 + 1];
            // Todo: create an empty option and call option.getOperandArgs()
            Module module = new Module("$anon", ".", "0.0.0");
            RecordType type = TypeCreator.createRecordType("dummy", module, 1, new HashMap<>(), null, true, 6);
            Option dummyOption = new Option(type, ValueCreator.createMapValue(type));
            dummyOption.parseRecord(args);
            processOperands(dummyOption.getOperandArgs());
        }
        return mainArgs;
    }

    private Object[] processOperands(List<String> operandArgs) {
        int opIndex = 0;
        int mainIndex = 0;
        Object bValue;
        int argIndex = 0;
        while (argIndex < operandArgs.size() && opIndex < operands.length) {
            Operand curOperand = operands[opIndex++];
            Type typeOp = curOperand.type;
            if (typeOp.getTag() == TypeTags.ARRAY_TAG) {
                BArray bArray = ValueCreator.createArrayValue((ArrayType) typeOp, -1);
                Type elementType = ((ArrayType) typeOp).getElementType();
                int elementCount = getElementCount(elementType, operands, opIndex);
                while (argIndex < operandArgs.size() - elementCount) {
                    // todo: catch error when int[] followed string
                    bArray.append(CliUtil.getBValue(elementType, operandArgs.get(argIndex++)));
                }
                bValue = bArray;
            } else {
                bValue = CliUtil.getBValueWithUnionValue(curOperand.type, operandArgs.get(argIndex++));
            }
            mainArgs[++mainIndex] = bValue;
            mainArgs[++mainIndex] = true;
        }
        if (argIndex < operandArgs.size()) {
            throw ErrorCreator.createError(StringUtils.fromString("all operand arguments are not matched"));
        }
        while (opIndex < operands.length) {
            Operand operand = operands[opIndex++];
            Type opType = operand.type;
            if (operand.hasDefaultable) {
                mainArgs[++mainIndex] = getDefaultBValue(opType);
                mainArgs[++mainIndex] = false;
                continue;
            }
            if (opType.getTag() != TypeTags.UNION_TAG) {
                throw ErrorCreator.createError(
                        StringUtils.fromString("missing operand arguments for type '" + opType + "'"));
            }
            List<Type> unionMemberTypes = ((UnionType) opType).getMemberTypes();
            if (unionMemberTypes.size() != 2 || !unionMemberTypes.contains(PredefinedTypes.TYPE_NULL)) {
                throw ErrorCreator.createError(
                        StringUtils.fromString("missing operand arguments for type '" + opType + "'"));
            }
        }
        return mainArgs;
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

    private int getElementCount(Type elementType, Operand[] operands, int opIndex) {
        int count = 0;
        // todo: handle for defaultable int c = 2
        while (opIndex < operands.length && elementType == operands[opIndex++].type) {
            count++;
        }
        return count;
    }
}
