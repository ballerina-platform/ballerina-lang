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
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.BmpStringValue;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.ObjectValue;
import io.ballerina.runtime.values.TableValue;
import io.ballerina.runtime.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.TypeTags.BOOLEAN_TAG;
import static io.ballerina.runtime.api.TypeTags.BYTE_TAG;
import static io.ballerina.runtime.api.TypeTags.DECIMAL_TAG;
import static io.ballerina.runtime.api.TypeTags.FLOAT_TAG;
import static io.ballerina.runtime.api.TypeTags.INT_TAG;
import static io.ballerina.runtime.api.TypeTags.RECORD_TYPE_TAG;
import static io.ballerina.runtime.api.TypeTags.STRING_TAG;

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

    public static Object echo(BTypedesc td, BValue value) {
        return value;
    }

    public static BXML getXML(BTypedesc td, BXML value) {
        return value;
    }

    public static BStream getStream(BTypedesc td, BStream value) {
        return value;
    }

    public static TableValue getTable(BTypedesc td, TableValue value) {
        return value;
    }

    public static BFunctionPointer getFunction(BTypedesc param, BTypedesc ret, BFunctionPointer fp) {
        return fp;
    }

    public static BTypedesc getTypedesc(BTypedesc td) {
        return td;
    }

    public static BFuture getFuture(BTypedesc td, BFuture value) {
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
        return new ArrayValueImpl(new long[]{10, 20, 30});
    }

    public static Object getInvalidValue(BTypedesc td1, BTypedesc td2) {
        switch (td1.getDescribingType().getTag()) {
            case INT_TAG:
                return getRecord(td2);
            case RECORD_TYPE_TAG:
                return 200;
        }
        return null;
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
}
