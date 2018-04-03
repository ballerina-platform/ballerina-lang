import ballerina/collections;

function testAdd (string[] values) returns (collections:Vector) {
    collections:Vector vector = {vec:[]};
    int i = 0;

    while (i < lengthof values) {
        vector.add(values[i]);
        i = i + 1;
    }

    return vector;
}

function testClear (int size) returns (collections:Vector, collections:Vector) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    collections:Vector vectorRef = vector;
    vector.clear();

    return (vector, vectorRef);
}

function testGet (int size) returns (any, any, any, any) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    any x1 = vector.get(0);
    any x2 = vector.get(2);
    any x3 = vector.get(4);
    any x4 = vector.get(6);

    return (x1, x2, x3, x4);
}

function testInsert (int[] values, int[] indices, int size) returns (collections:Vector) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    int i = 0;
    while (i < lengthof values) {
        vector.insert(values[i], indices[i]);
        i = i + 1;
    }

    return vector;
}

function testIsEmpty (int size) returns (boolean[]) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    boolean[] isEmpty = [];
    int i = 0;

    isEmpty[i] = vector.isEmpty();
    i = i + 1;

    vector.clear();
    isEmpty[i] = vector.isEmpty();
    i = i + 1;

    populateVector(vector, size);
    int j = 0;
    while (j < size) {
        _ = vector.remove(0);
        j = j + 1;
    }
    isEmpty[i] = vector.isEmpty();

    return isEmpty;
}

function testSize (int[] addElems, int[] insertElems, int[] replaceElems, int nRemoveElems, int size) returns (int[]) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    int[] vecSizes = [];
    int sizesIndex = 0;

    int i = 0;
    while (i < lengthof addElems) {
        vector.add(addElems[i]);
        i = i + 1;
    }
    vecSizes[sizesIndex] = vector.size();
    sizesIndex = sizesIndex + 1;

    i = 0;
    while (i < lengthof insertElems) {
        vector.insert(insertElems[i], 8);
        i = i + 1;
    }
    vecSizes[sizesIndex] = vector.size();
    sizesIndex = sizesIndex + 1;

    i = 0;
    while (i < lengthof replaceElems) {
        _ = vector.replace(replaceElems[i], i + 2);
        i = i + 1;
    }
    vecSizes[sizesIndex] = vector.size();
    sizesIndex = sizesIndex + 1;

    i = 0;
    while (i < nRemoveElems) {
        _ = vector.remove(3);
        i = i + 1;
    }
    vecSizes[sizesIndex] = vector.size();
    sizesIndex = sizesIndex + 1;

    return vecSizes;
}

function testRemove (int size) returns (collections:Vector, any, any, any) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    any x1 = vector.remove(0);
    any x2 = vector.remove(2);
    any x3 = vector.remove(size - 3);

    return (vector, x1, x2, x3);
}

function testReplace (int[] values, int[] indices, int size) returns (collections:Vector, any[]) {
    collections:Vector vector = {vec:[]};
    any[] replacedVals = [];
    populateVector(vector, size);

    int i = 0;
    while (i < lengthof values) {
        replacedVals[i] = vector.replace(values[i], indices[i]);
        i = i + 1;
    }

    return (vector, replacedVals);
}

function testGetIndexOutOfRange (int[] indices, int size) returns (any[], error[]) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    any[] vals = [];
    error[] errs = [];
    int i = 0;
    int errIndex = 0;

    while (i < lengthof indices) {
        try {
            vals[i] = vector.get(indices[i]);
        } catch (error e) {
            errs[errIndex] = e;
            errIndex = errIndex + 1;
        }
        i = i + 1;
    }
    return (vals, errs);
}

function testInsertIndexOutOfRange (int[] values, int[] indices, int size) returns (collections:Vector, collections:IndexOutOfRangeError[]) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    collections:IndexOutOfRangeError[] errs = [];
    int i = 0;
    int errIndex = 0;

    while (i < lengthof values) {
        try {
            vector.insert(values[i], indices[i]);
        } catch (collections:IndexOutOfRangeError e) {
            errs[i] = e;
            errIndex = errIndex + 1;
        }
        i = i + 1;
    }

    return (vector, errs);
}

function testRemoveIndexOutOfRange (int[] indices, int size) returns (collections:Vector, any[], collections:IndexOutOfRangeError[]) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    any[] removedVals = [];
    collections:IndexOutOfRangeError[] errs = [];
    int i = 0;
    int errIndex = 0;

    while (i < lengthof indices) {
        try {
            removedVals[i] = vector.remove(indices[i]);
        } catch (collections:IndexOutOfRangeError e) {
            errs[errIndex] = e;
            errIndex = errIndex + 1;
        }
        i = i + 1;
    }

    return (vector, removedVals, errs);
}

function testReplaceIndexOutOfRange (int[] values, int[] indices, int size) returns (collections:Vector, collections:IndexOutOfRangeError[]) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    collections:IndexOutOfRangeError[] errs = [];
    int i = 0;
    int errIndex = 0;

    while (i < lengthof values) {
        try {
            _ = vector.replace(values[i], indices[i]);
        } catch (collections:IndexOutOfRangeError e) {
            errs[i] = e;
            errIndex = errIndex + 1;
        }
        i = i + 1;
    }

    return (vector, errs);
}

function populateVector (collections:Vector vector, int size) {
    int i = 1;
    while (i <= size) {
        vector.add(i * 10);
        i = i + 1;
    }
}
