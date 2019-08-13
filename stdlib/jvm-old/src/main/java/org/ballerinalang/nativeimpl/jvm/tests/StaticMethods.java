/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.tests;

import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * This class contains a set of utility static methods required for interoperability testing.
 *
 * @since 1.0.0
 */
public class StaticMethods {
    private StaticMethods() {
    }

    public static void acceptNothingAndReturnNothing() {
    }

    public static Date acceptNothingButReturnDate() {
        return new Date();
    }

    public static Date acceptSomethingAndReturnSomething(Date date) {
        return date;
    }

    public static String acceptTwoParamsAndReturnSomething(String s1, String s2) {
        return s1 + s2;
    }

    public static Integer acceptThreeParamsAndReturnSomething(Integer s1, Integer s2, Integer s3) {
        return s1 + s2 + s3;
    }

    // This scenario is for map value to be passed to interop and return array value.
    public static ArrayValue getArrayValueFromMap(String key, MapValue mapValue) {
        ArrayValue arrayValue = new ArrayValue(BTypes.typeInt);
        arrayValue.add(0, 1);
        long fromMap = mapValue.getIntValue(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static MapValue acceptRefTypesAndReturnMap(ObjectValue a, ArrayValue b, Object c,
                                                      ErrorValue d, Object e, Object f, MapValue g) {
        MapValue<String, Object> mapValue = new MapValueImpl();
        mapValue.put("a", a);
        mapValue.put("b", b);
        mapValue.put("c", c);
//        mapValue.put("d", d);
        mapValue.put("e", e);
        mapValue.put("f", f);
        mapValue.put("g", g);
        return mapValue;
    }

    public static ErrorValue acceptStringErrorReturn(String msg) {
        return new ErrorValue(msg, null);
    }

    public static Object acceptIntUnionReturn(int flag) {
        switch (flag) {
            case 1:
                return 25;
            case 2:
                return "sample value return";
            case 3:
                return 54.88;
            default:
                return true;
        }
    }

    public static ObjectValue acceptObjectAndObjectReturn(ObjectValue p, int newVal) {
        p.set("age", newVal);
        return p;
    }

    public static MapValue acceptRecordAndRecordReturn(MapValue e, String newVal) {
        e.put("name", newVal);
        return e;
    }

    public static String getString(UUID uuid) {
        return uuid.toString() + ": Sameera";
    }

    public static void acceptNothingReturnNothingAndThrowsCheckedException() throws JavaInteropTestCheckedException {

    }

    public static void acceptNothingReturnNothingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {

    }

    public static void acceptNothingReturnNothingAndThrowsUncheckedException() throws UnsupportedOperationException {

    }

    public static void acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {

    }


    public static Date acceptNothingReturnSomethingAndThrowsCheckedException() throws JavaInteropTestCheckedException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsUncheckedException() throws UnsupportedOperationException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(Date date)
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedException(Date date)
            throws JavaInteropTestCheckedException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(Date date)
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsUncheckedException(Date date)
            throws UnsupportedOperationException {
        return date;
    }

    public static int acceptIntReturnIntThrowsCheckedException(long a) throws JavaInteropTestCheckedException {
        return (int) (a + 5);
    }

    public static ArrayValue getArrayValueFromMapWhichThrowsCheckedException(String key, MapValue mapValue)
            throws JavaInteropTestCheckedException {
        ArrayValue arrayValue = new ArrayValue(BTypes.typeInt);
        arrayValue.add(0, 1);
        long fromMap = mapValue.getIntValue(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static MapValue acceptRefTypesAndReturnMapWhichThrowsCheckedException(ObjectValue a, ArrayValue b, Object c,
                                                      ErrorValue d, Object e, Object f, MapValue g)
            throws JavaInteropTestCheckedException {
        MapValue<String, Object> mapValue = new MapValueImpl<>();
        mapValue.put("a", a);
        mapValue.put("b", b);
        mapValue.put("c", c);
        mapValue.put("e", e);
        mapValue.put("f", f);
        mapValue.put("g", g);
        return mapValue;
    }

    public static ErrorValue acceptStringErrorReturnWhichThrowsCheckedException(String msg)
            throws JavaInteropTestCheckedException {
        return new ErrorValue(msg, null);
    }

    public static Object acceptIntUnionReturnWhichThrowsCheckedException(int flag)
            throws JavaInteropTestCheckedException {
        switch (flag) {
            case 1:
                return 25;
            case 2:
                return "sample value return";
            case 3:
                return 54.88;
            default:
                return true;
        }
    }

    public static ObjectValue acceptObjectAndObjectReturnWhichThrowsCheckedException(ObjectValue p, int newVal)
            throws JavaInteropTestCheckedException {
        p.set("age", newVal);
        return p;
    }

    public static MapValue acceptRecordAndRecordReturnWhichThrowsCheckedException(MapValue e, String newVal)
            throws JavaInteropTestCheckedException {
        e.put("name", newVal);
        return e;
    }
}
