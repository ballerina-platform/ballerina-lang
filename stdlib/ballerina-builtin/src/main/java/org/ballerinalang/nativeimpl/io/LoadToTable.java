/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Native function ballerina.io#loadToTable.
 *
 * @since 0.964.1
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "loadToTable",
        args = {@Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "recordSeparator", type = TypeKind.STRING),
                @Argument(name = "fieldSeparator", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.TABLE),
                      @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class LoadToTable extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(LoadToTable.class);

    @Override
    public void execute(Context context) {
        final String filePath = context.getStringArgument(0);
        Path path;
        try {
            path = Paths.get(filePath);
        } catch (InvalidPathException e) {
            String msg = "Unable to resolve the file path: " + filePath;
            log.error(msg, e);
            context.setReturnValues(new BTable(), IOUtils.createError(context, msg));
            return;
        }
        if (Files.notExists(path)) {
            String msg = "Unable to find a file in given path: " + filePath;
            context.setReturnValues(new BTable(), IOUtils.createError(context, msg));
            return;
        }
        try {
            BTable table = getbTable(context, path);
            context.setReturnValues(table, null);
        } catch (IOException e) {
            String msg = "Failed to process the delimited file: " + e.getMessage();
            log.error(msg, e);
            context.setReturnValues(new BTable(), IOUtils.createError(context, msg));
        }
    }

    private BTable getbTable(Context context, Path path) throws IOException, BallerinaIOException {
        BTypeValue type = (BTypeValue) context.getRefArgument(0);
        if (type == null) {
            String msg = "Unable to find the given Struct type";
            throw new BallerinaIOException(msg);
        }
        BTable table = new BTable(new BTableType(type.value()));
        try (FileChannel sourceChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            DelimitedRecordChannel recordChannel = getDelimitedRecordChannel(context, sourceChannel);
            BStructType structType = (BStructType) type.value();
            while (recordChannel.hasNext()) {
                BStruct struct = getbStruct(recordChannel, structType);
                if (struct != null) {
                    table.addData(struct);
                }
            }
        }
        return table;
    }

    private BStruct getbStruct(DelimitedRecordChannel recordChannel, final BStructType structType) throws IOException {
        BStructType.StructField[] internalStructFields = structType.getStructFields();
        int fieldLength = internalStructFields.length;
        BStruct struct = null;
        final String[] fields = recordChannel.read();
        if (fields.length > 0) {
            if (internalStructFields.length != fields.length) {
                String msg = "Record row fields count and the give struct's fields count are mismatch";
                throw new BallerinaException(msg);
            }
            struct = new BStruct(structType);
            int longRegIndex = -1;
            int doubleRegIndex = -1;
            int stringRegIndex = -1;
            int booleanRegIndex = -1;
            for (int i = 0; i < fieldLength; i++) {
                String value = fields[i];
                final BStructType.StructField internalStructField = internalStructFields[i];
                final int type = internalStructField.getFieldType().getTag();
                switch (type) {
                case TypeTags.INT_TAG:
                    struct.setIntField(++longRegIndex, Long.parseLong(value));
                    break;
                case TypeTags.FLOAT_TAG:
                    struct.setFloatField(++doubleRegIndex, Double.parseDouble(value));
                    break;
                case TypeTags.STRING_TAG:
                    struct.setStringField(++stringRegIndex, value);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    struct.setBooleanField(++booleanRegIndex, (Boolean.parseBoolean(value)) ? 1 : 0);
                    break;
                default:
                    throw new BallerinaException("Type casting support only for int, float, boolean and string. "
                            + "Invalid value for the struct field: " + value);
                }
            }
        }
        return struct;
    }

    private DelimitedRecordChannel getDelimitedRecordChannel(Context context, FileChannel sourceChannel) {
        final String recordSeparator = context.getStringArgument(1);
        final String fieldSeparator = context.getStringArgument(2);
        FileIOChannel fileIOChannel = new FileIOChannel(sourceChannel);
        CharacterChannel characterChannel = new CharacterChannel(fileIOChannel, Charset.defaultCharset().name());
        return new DelimitedRecordChannel(characterChannel, recordSeparator, fieldSeparator);
    }
}
