/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java resources for the unit testing of different field bindings generated.
 *
 * @since 2.0.0
 */
public class FieldsTestResource implements InterfaceTestResource {

    // Instance primitive fields
    public byte getInstanceByte = 10;
    public char getInstanceChar = 'c';
    public short getInstanceShort = 2;
    public int getInstanceInt = 3;
    public long getInstanceLong = 5;
    public float getInstanceFloat = 5.0f;
    public double getInstanceDouble = 8.0;
    public boolean getInstanceBoolean = true;
    public String getInstanceString = "hello";

    public int[][] testUnsupportedArray = new int[10][20];

    // Instance primitive field single-dimensional arrays
    public byte[] getInstanceByteArray = "hello".getBytes();
    public char[] getInstanceCharArray = {'h', 'e', 'y'};
    public short[] getInstanceShortArray = {2, 3, 4};
    public int[] getInstanceIntArray = {5, 6, 7};
    public long[] getInstanceLongArray = {8, 9, 10};
    public float[] getInstanceFloatArray = {5.0f, 6.0f};
    public double[] getInstanceDoubleArray = {8.0, 9.0};
    public boolean[] getInstanceBooleanArray = {true, false};
    public String[] getInstanceStringArray = {"hello", "ballerina"};

    // Other instance fields
    public StringBuilder[] getInstanceObjectArray = new StringBuilder[2];
    public Integer[] getInstanceObjectMultiArray1 = new Integer[5];
    public Object[] getInstanceObjectMultiArray2 = new Object[2];
    public List getInstanceInterface = new ArrayList<>();
    public AbstractList getInstanceAbstractClass = new ArrayList<>();
    public Path getInstanceObject = Paths.get("/test.txt");
    public Set<File> getInstanceGenericObject = new HashSet<>();
    public System.Logger.Level getInstanceEnumeration = System.Logger.Level.ALL;
    public ArithmeticException getInstanceThrowable = new ArithmeticException();

    // Different access modifiers in instance fields
    private short getInstancePrivateField = 2;
    protected Path getInstanceProtectedField = Paths.get("/test.txt");
    int getInstanceDefaultField = 3;

    // Static primitive fields
    public static byte getStaticByte = 10;
    public static char getStaticChar = 'c';
    public static short getStaticShort = 2;
    public static int getStaticInt = 3;
    public static long getStaticLong = 5;
    public static float getStaticFloat = 5.0f;
    public static double getStaticDouble = 8.0;
    public static boolean getStaticBoolean = true;
    public static String getStaticString = "hello";

    // Static final primitive fields
    public static final byte GET_STATIC_FINAL_BYTE = 10;
    public static final char GET_STATIC_FINAL_CHAR = 'c';
    public static final short GET_STATIC_FINAL_SHORT = 2;
    public static final int GET_STATIC_FINAL_INT = 3;
    public static final long GET_STATIC_FINAL_LONG = 5;
    public static final float GET_STATIC_FINAL_FLOAT = 5.0f;
    public static final double GET_STATIC_FINAL_DOUBLE = 8.0;
    public static final boolean GET_STATIC_FINAL_BOOLEAN = true;
    public static final String GET_STATIC_FINAL_STRING = "hello";

    // Static primitive field single-dimensional arrays
    public static byte[] getStaticByteArray = "hello".getBytes();
    public static char[] getStaticCharArray = {'h', 'e', 'y'};
    public static short[] getStaticShortArray = {2, 3, 4};
    public static int[] getStaticIntArray = {5, 6, 7};
    public static long[] getStaticLongArray = {8, 9, 10};
    public static float[] getStaticFloatArray = {5.0f, 6.0f};
    public static double[] getStaticDoubleArray = {8.0, 9.0};
    public static boolean[] getStaticBooleanArray = {true, false};
    public static String[] getStaticStringArray = {"hello", "ballerina"};

    // Other static fields
    public StringBuilder[] getStaticObjectArray = new StringBuilder[2];
    public Integer[] getStaticObjectMultiArray1 = new Integer[2];
    public Object[] getStaticObjectMultiArray2 = new Object[2];
    public static List getStaticInterface = new ArrayList<>();
    public static AbstractList getStaticAbstractClass = new ArrayList<>();
    public static Path getStaticObject = Paths.get("/test.txt");
    public static Set<File> getStaticGenericObject = new HashSet<>();
    public static System.Logger.Level getStaticEnumeration = System.Logger.Level.ALL;
    public static ArithmeticException getStaticThrowable = new ArithmeticException();

    // Different access modifiers in static fields
    private static short getStaticPrivateField = 2;
    protected static Path getStaticProtectedField = Paths.get("/test.txt");
    static int getStaticDefaultField = 3;

    @Override
    public int testMethod(int x) {
        return 0;
    }

    @Override
    public String[] returnStringArray() {
        return new String[0];
    }
}
