package ballerina.lang.array;

native function length (int[] array) (int);

native function length (long[] array) (int);

native function length (float[] array) (int);

native function length (double[] array) (int);

native function length (string[] array) (int);

native function length (json[] array) (int);

native function length (xml[] array) (int);

native function length (message[] array) (int);

native function copyOf (int[] array) (int[]);

native function copyOf (long[] array) (long[]);

native function copyOf (float[] array) (float[]);

native function copyOf (double[] array) (double[]);

native function copyOf (string[] array) (string[]);

native function copyOf (json[] array) (json[]);

native function copyOf (xml[] array) (xml[]);

native function copyOf (message[] array) (message[]);

native function copyOfRange (int[] array, int from, int to) (int[]);

native function copyOfRange (long[] array, int from, int to) (long[]);

native function copyOfRange (float[] array, int from, int to) (float[]);

native function copyOfRange (double[] array, int from, int to) (double[]);

native function copyOfRange (string[] array, int from, int to) (string[]);

native function copyOfRange (json[] array, int from, int to) (json[]);

native function copyOfRange (xml[] array, int from, int to) (xml[]);

native function copyOfRange (message[] array, int from, int to) (message[]);