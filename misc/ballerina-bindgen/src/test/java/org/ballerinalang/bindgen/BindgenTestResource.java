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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java resources for the unit testing of binding components generated.
 *
 * @since 2.0.0
 */
public class BindgenTestResource {

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

    // Instance primitive field multi-dimensional arrays
    public byte[][] getInstanceByteMultiArray1 = new byte[5][8];
    public char[][] getInstanceCharMultiArray1 = new char[5][8];
    public short[][] getInstanceShortMultiArray1 = new short[5][8];
    public int[][] getInstanceIntMultiArray1 = new int[5][8];
    public long[][] getInstanceLongMultiArray1 = new long[5][8];
    public float[][] getInstanceFloatMultiArray1 = new float[5][8];
    public double[][] getInstanceDoubleMultiArray1 = new double[5][8];
    public boolean[][] getInstanceBooleanMultiArray1 = new boolean[5][8];
    public String[][] getInstanceStringMultiArray1 = new String[5][8];

    public byte[][][] getInstanceByteMultiArray2 = new byte[10][2][1];
    public char[][][] getInstanceCharMultiArray2 = new char[5][8][1];
    public short[][][] getInstanceShortMultiArray2 = new short[5][8][1];
    public int[][][] getInstanceIntMultiArray2 = new int[5][8][1];
    public long[][][] getInstanceLongMultiArray2 = new long[5][8][1];
    public float[][][] getInstanceFloatMultiArray2 = new float[5][8][1];
    public double[][][] getInstanceDoubleMultiArray2 = new double[5][8][1];
    public boolean[][][] getInstanceBooleanMultiArray2 = new boolean[5][8][1];
    public String[][][] getInstanceStringMultiArray2 = new String[5][8][1];

    // Other instance fields
    public StringBuilder[] getInstanceObjectArray = new StringBuilder[2];
    public Integer[][] getInstanceObjectMultiArray1 = new Integer[2][5];
    public Object[][][] getInstanceObjectMultiArray2 = new Object[2][5][1];
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

    // Static primitive field multi-dimensional arrays
    public static byte[][] getStaticByteMultiArray1 = new byte[5][8];
    public static char[][] getStaticCharMultiArray1 = new char[5][8];
    public static short[][] getStaticShortMultiArray1 = {{8, 9}, {8, 9}};
    public static int[][] getStaticIntMultiArray1 = new int[5][8];
    public static long[][] getStaticLongMultiArray1 = {{8, 9}, {8, 9}};
    public static float[][] getStaticFloatMultiArray1 = new float[5][8];
    public static double[][] getStaticDoubleMultiArray1 = new double[5][8];
    public static boolean[][] getStaticBooleanMultiArray1 = new boolean[5][8];
    public static String[][] getStaticStringMultiArray1 = {{"hi", "how"}, {"are", "you"}};

    public static byte[][][] getStaticByteMultiArray2 = new byte[10][2][1];
    public static char[][][] getStaticCharMultiArray2 = new char[5][8][1];
    public static short[][][] getStaticShortMultiArray2 = new short[5][8][1];
    public static int[][][] getStaticIntMultiArray2 = new int[5][8][1];
    public static long[][][] getStaticLongMultiArray2 = new long[5][8][1];
    public static float[][][] getStaticFloatMultiArray2 = new float[5][8][1];
    public static double[][][] getStaticDoubleMultiArray2 = new double[5][8][1];
    public static boolean[][][] getStaticBooleanMultiArray2 = new boolean[5][8][1];
    public static String[][][] getStaticStringMultiArray2 = new String[5][8][1];

    // Other static fields
    public StringBuilder[] getStaticObjectArray = new StringBuilder[2];
    public Integer[][] getStaticObjectMultiArray1 = new Integer[2][5];
    public Object[][][] getStaticObjectMultiArray2 = new Object[2][5][1];
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

    // Different constructor combinations

    private BindgenTestResource(String obj) {
        getInstanceInt = 5;
    }

    BindgenTestResource(String obj1, String obj2) {
        getInstanceInt = 5;
    }

    protected BindgenTestResource(String obj1, String obj2, String obj3) {
        getInstanceInt = 5;
    }

    public BindgenTestResource() {
        getInstanceInt = 5;
    }

    public BindgenTestResource(int x) throws ArithmeticException {
        getInstanceInt = 5 / x;
    }

    public BindgenTestResource(int[] x, int[][] y) throws ArithmeticException, ArrayIndexOutOfBoundsException {
        getInstanceInt = 5 / x[0];
    }

    public BindgenTestResource(boolean[] x, boolean[][] y) {
        getInstanceBooleanArray = x;
    }

    public BindgenTestResource(byte x, Object obj, byte[] y, byte[][] z) {
        getInstanceByte = x;
    }

    public BindgenTestResource(char x, StringBuffer[] obj, char[] y, char[][] z) {
        getInstanceChar = x;
    }

    public BindgenTestResource(short x, Integer[][] obj, Byte[][][] in, short[] y) {
        getInstanceShort = x;
    }

    public BindgenTestResource(long x, Set<String> obj, short[][] y, long[] z, long[][] a) {
        getInstanceLong = x;
    }

    public BindgenTestResource(float x, System.Logger.Level obj, float[] y, float[][] z) {
        getInstanceFloat = x;
    }

    public BindgenTestResource(double x, ArithmeticException obj, double[] y, double[][] z) {
        getInstanceDouble = x;
    }

    public BindgenTestResource(boolean x, AbstractList obj, boolean[] y, boolean[][] z) {
        getInstanceBoolean = x;
    }

    public BindgenTestResource(String x, Map obj, String[] y, String[][] z) {
        getInstanceString = x;
    }

    public BindgenTestResource(float x, double y, boolean z, String[] a, short[][] b, Object[][][] c)
            throws ArithmeticException, ArrayIndexOutOfBoundsException {
        getInstanceFloat = x;
    }

    // Different instance method combinations

    private void privateMethod() {
    }

    protected void protectedMethod() {
    }

    void defaultMethod() {
    }

    public void returnVoid() {
    }

    public void returnOptionalError() throws Exception {
    }

    public IOException returnError() {
        return new IOException();
    }

    public Map returnInterface() {
        return new HashMap();
    }

    public Set<String> returnGenericObject() {
        return new HashSet<>();
    }

    public AbstractSet returnAbstractObject() {
        return new HashSet<>();
    }

    public System.Logger.Level returnEnum() {
        return System.Logger.Level.INFO;
    }

    public void errorParam(IOException x, String[][] y) {
    }

    public void interfaceParam(Map x, Object[] y, Object[][] z) {
    }

    public Set<String> genericObjectParam(Set<String> x, int y, String[] z) {
        return x;
    }

    public void abstractObjectParam(AbstractSet x, Object y, String z) {
    }

    public System.Logger.Level enumParam(System.Logger.Level x) {
        return x;
    }

    public File returnObject() {
        return new File("test.txt");
    }

    public File returnObjectThrowable() throws FileNotFoundException {
        return new File("test.txt");
    }

    public File[] returnObjectThrowableError1() throws FileNotFoundException {
        return new File[2];
    }

    public File returnObjectThrowableError2(int[] x) throws FileNotFoundException {
        return new File("test.txt");
    }

    public File[] returnObjectError1() {
        return new File[2];
    }

    public File returnObjectError2(int[] x) {
        return new File("test.txt");
    }

    public byte returnByte(byte x) throws ArithmeticException {
        return x;
    }

    public char returnChar(char x, int[] y) {
        return x;
    }

    public short returnShort(short x, int y) throws ArithmeticException {
        return x;
    }

    public int returnInt(long x) {
        return 5;
    }

    public long returnLong(long x) {
        return x;
    }

    public float returnFloat(float x) {
        return x;
    }

    public float returnFloat(float x, String y) {
        return x;
    }

    public double returnDouble(double x) {
        return x;
    }

    public double returnDouble(double x, Object y) {
        return x;
    }

    public boolean returnBoolean(boolean x) {
        return x;
    }

    public boolean returnBoolean(boolean x, Object[] y) throws ArithmeticException {
        return x;
    }

    public String returnString(String x) {
        return x;
    }

    public byte[] returnByteArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public char[] returnCharArray(char[] x, int[] y, short[] z) {
        return x;
    }

    public short[] returnShortArray(short x, int y) throws ArithmeticException {
        return new short[2];
    }

    public int[] returnIntArray(int[] x, boolean y, boolean[] z) {
        return x;
    }

    public long[] returnLongArray(long[] x, Object y) throws ArithmeticException {
        return x;
    }

    public float[] returnFloatArray(float[] x, String y) {
        return x;
    }

    public double[] returnDoubleArray(double[] x, double[] y) {
        return x;
    }

    public boolean[] returnBooleanArray() {
        return new boolean[2];
    }

    public String[] returnStringArray(String[] x, StringBuffer y, int z) throws ArrayIndexOutOfBoundsException {
        return x;
    }

    public byte[][] returnByteMultiArray(byte[][] x) throws ArithmeticException {
        return x;
    }

    public char[][] returnCharMultiArray(char[][] x, int[] y, Object[][] z) {
        return x;
    }

    public short[][] returnShortMultiArray(short x, int y, short[][] z) {
        return new short[2][1];
    }

    public int[][] returnIntMultiArray(int[][] x, boolean y) throws ArithmeticException {
        return x;
    }

    public long[][] returnLongMultiArray(long[][] x, Object y, boolean[][] z) {
        return x;
    }

    public float[][] returnFloatMultiArray(float[][] x, String y) throws ArithmeticException {
        return x;
    }

    public double[][] returnDoubleMultiArray(double[][] x, double[] y) {
        return x;
    }

    public boolean[][] returnBooleanMultiArray() {
        return new boolean[2][1];
    }

    public String[][] returnStringMultiArray(String[][] x, StringBuffer y, int z)
            throws ArrayIndexOutOfBoundsException {
        return x;
    }

    // Different static method combinations

    private static void privateStaticMethod() {
    }

    protected static void protectedStaticMethod() {
    }

    static void defaultStaticMethod() {
    }

    public static void returnStaticVoid() {
    }

    public static void returnStaticOptionalError() throws Exception {
    }

    public static IOException returnStaticError() {
        return new IOException();
    }

    public static Map returnStaticInterface() {
        return new HashMap();
    }

    public static Set<String> returnStaticGenericObject() {
        return new HashSet<>();
    }

    public static AbstractSet returnStaticAbstractObject() {
        return new HashSet<>();
    }

    public static System.Logger.Level returnStaticEnum() {
        return System.Logger.Level.INFO;
    }

    public static void errorStaticParam(IOException x, String[][] z) {
    }

    public static void interfaceStaticParam(Map x, boolean[] y) {
    }

    public static Set<String> genericObjectStaticParam(Set<String> x) {
        return x;
    }

    public static void abstractObjectStaticParam(AbstractSet x, int y, Object z) {
    }

    public static System.Logger.Level enumStaticParam(System.Logger.Level x, Object[] y) {
        return x;
    }

    public static File returnStaticObject() {
        return new File("test.txt");
    }

    public static File returnStaticObjectThrowable() throws FileNotFoundException {
        return new File("test.txt");
    }

    public static File[] returnStaticObjectThrowableError1() throws FileNotFoundException {
        return new File[2];
    }

    public static File returnStaticObjectThrowableError2(int[] x) throws FileNotFoundException {
        return new File("test.txt");
    }

    public static File[] returnStaticObjectError1() {
        return new File[2];
    }

    public static File returnStaticObjectError2(int[] x) {
        return new File("test.txt");
    }

    public static byte returnStaticByte(byte x) throws ArithmeticException {
        return x;
    }

    public static char returnStaticChar(char x, int[] y) {
        return x;
    }

    public static short returnStaticShort(short x, int y) throws ArithmeticException {
        return x;
    }

    public static int returnStaticInt(long x) {
        return 5;
    }

    public static long returnStaticLong(long x) {
        return x;
    }

    public static float returnStaticFloat(float x, String y) {
        return x;
    }

    public static float returnStaticFloat(float x) {
        return x;
    }

    public static double returnStaticDouble(double x) {
        return x;
    }

    public static boolean returnStaticBoolean(boolean x) {
        return x;
    }

    public static double returnStaticDouble(double x, Object y, String[] z) {
        return x;
    }

    public static boolean returnStaticBoolean(boolean x, Object[] y) throws ArithmeticException {
        return x;
    }

    public static String returnStaticString(String x) {
        return x;
    }

    public static byte[] returnStaticByteArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public static char[] returnStaticCharArray(char[] x, int[] y) {
        return x;
    }

    public static short[] returnStaticShortArray(short x, int y) throws ArithmeticException {
        return new short[2];
    }

    public static int[] returnStaticIntArray(int[] x, boolean y, boolean[] z) {
        return x;
    }

    public static long[] returnStaticLongArray(long[] x, Object y) throws ArithmeticException {
        return x;
    }

    public static float[] returnStaticFloatArray(float[] x, String y, Object[][] z) {
        return x;
    }

    public static double[] returnStaticDoubleArray(double[] x, double[] y) {
        return x;
    }

    public static boolean[] returnStaticBooleanArray() {
        return new boolean[2];
    }

    public static String[] returnStaticStringArray(String[] x, StringBuffer y, int z)
            throws ArrayIndexOutOfBoundsException {
        return x;
    }

    public static byte[][] returnStaticByteMultiArray(byte[][] x) throws ArithmeticException {
        return x;
    }

    public static char[][] returnStaticCharMultiArray(char[][] x, int[] y, String[][] z) {
        return x;
    }

    public static short[][] returnStaticShortMultiArray(short x, int y, short[][] z) {
        return new short[2][1];
    }

    public static int[][] returnStaticIntMultiArray(int[][] x, boolean y) throws ArithmeticException {
        return x;
    }

    public static long[][] returnStaticLongMultiArray(long[][] x, Object y, boolean[][] z) {
        return x;
    }

    public static float[][] returnStaticFloatMultiArray(float[][] x, String y) throws ArithmeticException {
        return x;
    }

    public static double[][] returnStaticDoubleMultiArray(double[][] x, double[] y) {
        return x;
    }

    public static boolean[][] returnStaticBooleanMultiArray() {
        return new boolean[2][1];
    }

    public static String[][] returnStaticStringMultiArray(String[][] x, StringBuffer y, int z)
            throws ArrayIndexOutOfBoundsException {
        return x;
    }
}
