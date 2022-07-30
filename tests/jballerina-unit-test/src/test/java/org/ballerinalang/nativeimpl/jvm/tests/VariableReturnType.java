/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.BmpStringValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.TableValue;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.runtime.api.TypeTags.ARRAY_TAG;
import static io.ballerina.runtime.api.TypeTags.BOOLEAN_TAG;
import static io.ballerina.runtime.api.TypeTags.BYTE_TAG;
import static io.ballerina.runtime.api.TypeTags.DECIMAL_TAG;
import static io.ballerina.runtime.api.TypeTags.FLOAT_TAG;
import static io.ballerina.runtime.api.TypeTags.INT_TAG;
import static io.ballerina.runtime.api.TypeTags.OBJECT_TYPE_TAG;
import static io.ballerina.runtime.api.TypeTags.RECORD_TYPE_TAG;
import static io.ballerina.runtime.api.TypeTags.STRING_TAG;
import static io.ballerina.runtime.api.TypeTags.XML_COMMENT_TAG;
import static io.ballerina.runtime.api.TypeTags.XML_ELEMENT_TAG;
import static io.ballerina.runtime.api.utils.TypeUtils.getReferredType;

/**
 * Native methods for testing functions with variable return types.
 *
 * @since 2.0.0-preview1
 */
public class VariableReturnType {

    private static final BString NAME = new BmpStringValue("name");
    private static final BString AGE = new BmpStringValue("age");
    private static final BString DESIGNATION = new BmpStringValue("designation");
    private static final BString CITY = new BmpStringValue("city");
    private static final BString JOHN_DOE = new BmpStringValue("John Doe");
    private static final BString JANE_DOE = new BmpStringValue("Jane Doe");
    private static final BString SOFTWARE_ENGINEER = new BmpStringValue("Software Engineer");

    public static Object echo(BValue value, BTypedesc td) {
        return value;
    }

    public static BXml getXML(BTypedesc td, BXml value) {
        return value;
    }

    public static BStream getStream(BStream value, BTypedesc td) {
        return value;
    }

    public static TableValue getTable(TableValue value, BTypedesc td) {
        return value;
    }

    public static BFunctionPointer getFunction(BFunctionPointer fp, BTypedesc param, BTypedesc ret) {
        return fp;
    }

    public static BTypedesc getTypedesc(BTypedesc td) {
        return td;
    }

    public static BFuture getFuture(BFuture value, BTypedesc td) {
        return value;
    }

    public static BError getFuture(BTypedesc reason, BTypedesc detail, BError value) {
        return value;
    }

    public static Object getValue(BTypedesc td) {
        return getValue(td.getDescribingType());
    }

    public static Object getObjectValue(ObjectValue objectValue, BTypedesc td) {
        Type describingType = td.getDescribingType();
        if (describingType.getTag() == STRING_TAG) {
            BString newFname = objectValue.getStringValue(new BmpStringValue("fname"))
                    .concat(new BmpStringValue(" ")).concat(objectValue.getStringValue(new BmpStringValue("lname")));
            objectValue.set(new BmpStringValue("fname"), newFname);
            return newFname;
        }
        return getValue(td.getDescribingType());
    }

    public static MapValue query(BString query, BTypedesc typedesc) {
        Type type = typedesc.getDescribingType();
        MapValue map;

        if (type.getTag() == INT_TAG) {
            map = new MapValueImpl(new BMapType(type));
            map.put(new BmpStringValue("one"), 10);
            map.put(new BmpStringValue("two"), 20);
        } else if (type.getTag() == STRING_TAG) {
            map = new MapValueImpl(new BMapType(type));
            map.put(NAME, new BmpStringValue("Pubudu"));
            map.put(CITY, new BmpStringValue("Panadura"));
        } else {
            map = new MapValueImpl(new BMapType(PredefinedTypes.TYPE_ANY));
        }

        return map;
    }

    public static BStream getStreamOfRecords(ObjectValue objectValue, BStream strm, BTypedesc typedesc) {
        RecordType streamConstraint = (RecordType) typedesc.getDescribingType();
        assert streamConstraint == TypeUtils.getReferredType(strm.getConstraintType());
        return strm;
    }

    public static ArrayValue getTuple(BTypedesc td1, BTypedesc td2, BTypedesc td3) {
        List<Type> memTypes = new ArrayList<>();
        memTypes.add(td1.getDescribingType());
        memTypes.add(td2.getDescribingType());
        memTypes.add(td3.getDescribingType());
        BTupleType tupleType = new BTupleType(memTypes);

        ArrayValue arr = new TupleValueImpl(tupleType);
        arr.add(0, getValue(memTypes.get(0)));
        arr.add(1, getValue(memTypes.get(1)));
        arr.add(2, getValue(memTypes.get(2)));

        return arr;
    }

    public static ArrayValue getTupleWithRestDesc(BTypedesc td1, BTypedesc td2, BTypedesc td3) {
        List<Type> memTypes = new ArrayList<>();
        Type memType1 = td1.getDescribingType();
        memTypes.add(memType1);
        Type memType2 = td2.getDescribingType();
        memTypes.add(memType2);
        Type restType = td3.getDescribingType();
        BTupleType tupleType = new BTupleType(memTypes, restType, 0, false);

        ArrayValue arr = new TupleValueImpl(tupleType);
        arr.add(0, getValue(memType1));
        arr.add(1, getValue(memType2));
        Object value = getValue(restType);
        arr.add(2, value);
        arr.add(3, value);

        return arr;
    }

    public static MapValue getRecord(BTypedesc td) {
        BRecordType recType = (BRecordType) td.getDescribingType();
        MapValueImpl person = new MapValueImpl(recType);

        if (recType.getName().equals("Person")) {
            person.put(NAME, JOHN_DOE);
            person.put(AGE, 20);
        } else if (recType.getName().equals("Employee")) {
            person.put(NAME, JANE_DOE);
            person.put(AGE, 25);
            person.put(DESIGNATION, SOFTWARE_ENGINEER);
        } else {
            throw new IllegalStateException();
        }

        return person;
    }

    public static Object getVariedUnion(long x, BTypedesc td1, BTypedesc td2) {
        Type type1 = td1.getDescribingType();
        Type type2 = td2.getDescribingType();

        if (x == 0) {
            switch (type1.getTag()) {
                case INT_TAG:
                    return 100L;
                case STRING_TAG:
                    return new BmpStringValue("Foo");
            }
        }

        MapValueImpl rec = new MapValueImpl(type2);
        if (type2.getName().equals("Person")) {
            rec.put(NAME, JOHN_DOE);
            rec.put(AGE, 20);
        } else {
            rec.put(new BmpStringValue("type"), new BmpStringValue("Unknown"));
        }

        return rec;
    }

    public static ArrayValue getArray(BTypedesc td) {
        return new ArrayValueImpl(new long[]{10, 20, 30}, false);
    }

    public static BXml getXml(BTypedesc td, BXml val) {
        Type describingType = getReferredType(td.getDescribingType());
        if (describingType.getTag() == XML_ELEMENT_TAG) {
            return val;
        }

        assert describingType.getTag() == XML_COMMENT_TAG : describingType;
        return val;
    }

    public static Object getInvalidValue(BTypedesc td1, BTypedesc td2) {
        int tag = td1.getDescribingType().getTag();

        if (tag == INT_TAG) {
            return getRecord(td2);
        }

        assert tag == RECORD_TYPE_TAG;
        return 200;
    }

    private static Object getValue(Type type) {
        switch (type.getTag()) {
            case INT_TAG:
                return 150L;
            case FLOAT_TAG:
                return 12.34D;
            case DECIMAL_TAG:
                return new DecimalValue("23.45");
            case BOOLEAN_TAG:
                return true;
            case STRING_TAG:
                return new BmpStringValue("Hello World!");
            case BYTE_TAG:
                return 32;
            case RECORD_TYPE_TAG:
                BRecordType recType = (BRecordType) type;
                MapValueImpl person = new MapValueImpl(recType);

                if (recType.getName().equals("Person")) {
                    person.put(NAME, JOHN_DOE);
                    person.put(AGE, 20);
                } else if (recType.getName().equals("Employee")) {
                    person.put(NAME, JANE_DOE);
                    person.put(AGE, 25);
                    person.put(DESIGNATION, SOFTWARE_ENGINEER);
                } else {
                    throw new IllegalStateException();
                }

                return person;
        }
        return null;
    }

    public static Object get(ObjectValue objectValue, BTypedesc td) {
        Type describingType = td.getDescribingType();

        switch (describingType.getTag()) {
            case INT_TAG:
                return objectValue.getIntValue(StringUtils.fromString("a"));
            case STRING_TAG:
                return objectValue.getStringValue(StringUtils.fromString("b"));
        }
        return objectValue.get(StringUtils.fromString("c"));
    }

    public static Object getIntFieldOrDefault(ObjectValue objectValue, BTypedesc td) {
        Type describingType = td.getDescribingType();

        if (describingType.getTag() == INT_TAG) {
            return objectValue.getIntValue(StringUtils.fromString("i"));
        }

        return getValue(describingType);
    }

    public static Object getValueForParamOne(ObjectValue objectValue, BTypedesc td1, BTypedesc td2) {
        return getIntFieldOrDefault(objectValue, td1);
    }

    public static Object getWithDefaultableParams(Object x, Object y, BTypedesc z) {
        Type xType = TypeUtils.getType(x);
        Type yType = TypeUtils.getType(y);

        if (z.getDescribingType().getTag() == INT_TAG) {
            long xAsInt = xType.getTag() == INT_TAG ? (long) x : Long.valueOf(((BString) x).getValue());
            long yAsInt = yType.getTag() == INT_TAG ? (long) y : Long.valueOf(((BString) y).getValue());
            return xAsInt + yAsInt;
        }

        BString xAsString = xType.getTag() == INT_TAG ? StringUtils.fromString(Long.toString((long) x)) : (BString) x;
        BString yAsString = yType.getTag() == INT_TAG ? StringUtils.fromString(Long.toString((long) y)) : (BString) y;
        return xAsString.concat(yAsString);
    }

    public static Object getWithUnion(Object x, BTypedesc y) {
        int tag = y.getDescribingType().getTag();
        if (tag == INT_TAG) {
            return TypeUtils.getType(x).getTag() == INT_TAG ? (long) x + 1 : (long) ((BString) x).length();
        }

        if (tag == STRING_TAG) {
            return TypeUtils.getType(x).getTag() == INT_TAG ? StringUtils.fromString(Long.toString((long) x)) :
                    (BString) x;
        }

        return ErrorCreator.createError(StringUtils.fromString("Error!"), StringUtils.fromString("Union typedesc"));
    }

    public static Object clientGetWithUnion(BObject client, Object x, BTypedesc y) {
        return getWithUnion(x, y);
    }

    public static Object clientRemoteGetWithUnion(BObject client, Object x, BTypedesc y) {
        return getWithUnion(x, y);
    }

    public static Object getWithRestParam(long i, BTypedesc j, long... k) {
        int tag = j.getDescribingType().getTag();

        long total = i;
        for (long val : k) {
            total += val;
        }

        if (tag == STRING_TAG) {
            return total == 0 ? true : (total == 1 ? false : StringUtils.fromString(Long.toString(total)));
        }

        return total;
    }

    public static Object getWithMultipleTypedescs(long i, BTypedesc j, BTypedesc k, BTypedesc... l) {
        return true;
    }

    public static Object clientPost(BObject client, BTypedesc targetType, MapValue options) {
        BString mediaType =
                Optional.ofNullable(options.getStringValue(StringUtils.fromString("mediaType")))
                        .orElse(StringUtils.fromString(""));
        BString header =
                Optional.ofNullable(options.getStringValue(StringUtils.fromString("header")))
                        .orElse(StringUtils.fromString(""));

        if (targetType.getDescribingType().getTag() == STRING_TAG) {
            return mediaType.concat(StringUtils.fromString(" ")).concat(header);
        }

        return mediaType.length() + header.length();
    }

    public static Object calculate(BObject client, long i, BTypedesc targetType, MapValue options) {
        BString mediaType =
                Optional.ofNullable(options.getStringValue(StringUtils.fromString("mediaType")))
                        .orElse(StringUtils.fromString(""));
        BString header =
                Optional.ofNullable(options.getStringValue(StringUtils.fromString("header")))
                        .orElse(StringUtils.fromString(""));

        if (targetType.getDescribingType().getTag() == STRING_TAG) {
            return mediaType.concat(StringUtils.fromString(" ")).concat(header)
                    .concat(StringUtils.fromString(Long.toString(i)));
        }

        return mediaType.length() + header.length() + i;
    }

    public static Object getResource(BObject client, BArray path, BTypedesc targetType) {
        int targetTypeTag = targetType.getDescribingType().getTag();
        if (targetTypeTag == STRING_TAG) {
            return StringUtils.fromString(path.toString());
        }
        
        assert targetTypeTag == INT_TAG;
        return 0;
    }

    public static Object getSimpleUnion(Object val, BTypedesc td) {
        if (TypeUtils.getType(val).getTag() == INT_TAG) {
            if (td.getDescribingType().getTag() == INT_TAG) {
                return val;
            }

            return false;
        }

        if (td.getDescribingType().getTag() == INT_TAG) {
            return null;
        }

        return val;
    }

    public static Object getComplexUnion(BTypedesc td) {
        if (td.getDescribingType().getTag() == INT_TAG) {
            return ValueCreator.createArrayValue(new long[]{1, 2});
        }

        BTupleType tupleType = new BTupleType(List.of(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));
        BArray tupleValue = ValueCreator.createTupleValue(tupleType);
        tupleValue.add(0, 100L);
        tupleValue.add(1, StringUtils.fromString("Hello World"));

        BArrayType arrayType = new BArrayType(tupleType);
        BArray arrayValue = ValueCreator.createArrayValue(new Object[]{tupleValue}, arrayType);

        BMapInitialValueEntry[] initialValues = {
                ValueCreator.createKeyFieldEntry(StringUtils.fromString("entry"), arrayValue)};
        return ValueCreator.createMapValue(new BMapType(arrayType), initialValues);
    }

    public static long untaintedParamFunc(BTypedesc typedesc) {
        return 0;
    }

    public static BArray funcWithMultipleArgs(long i, BTypedesc td, BArray arr) {
        if (td.getDescribingType().getTag() == STRING_TAG) {
            arr.append(StringUtils.fromString(Long.toString(i)));
            return arr;
        }

        assert td.getDescribingType().getTag() == INT_TAG;
        return ValueCreator.createArrayValue(new long[]{arr.getLength(), i});
    }

    public static Object funcReturningUnionWithBuiltInRefType(Object strm, BTypedesc td) {
        int tag = ((BStreamType) getReferredType(td.getDescribingType())).getConstrainedType().getTag();

        if (tag == INT_TAG) {
            return strm;
        }

        assert tag == BYTE_TAG;
        if (strm == null) {
            return 100L;
        }

        return "hello world";
    }

    public static Object getValueWithUnionReturnType(Object val, BTypedesc td) {
        int tag = TypeUtils.getType(val).getTag();

        Type describingType = td.getDescribingType();
        if (tag == RECORD_TYPE_TAG) {
            assert describingType.getTag() == INT_TAG;
            return 101L;
        }

        if (tag == OBJECT_TYPE_TAG) {
            assert describingType.getTag() == ARRAY_TAG &&
                    ((BArrayType) describingType).getElementType().getTag() == STRING_TAG;
            return val;
        }

        assert describingType.getTag() == BOOLEAN_TAG;
        return !((boolean) val);
    }

    public static BFunctionPointer getFunctionWithAnyFunctionParamType(BFunctionPointer x, BTypedesc td) {
        assert td.getDescribingType().getTag() == INT_TAG;
        return x;
    }

    public static Object functionWithInferredArgForParamOfTypeReferenceType(BTypedesc td) {
        Type describingType = td.getDescribingType();

        int tag = describingType.getTag();

        if (tag == INT_TAG) {
            return 9876L;
        }

        assert tag == STRING_TAG;
        return StringUtils.fromString("hello!");
    }
}
