import ballerina.collections;

function testAdd (string[] values) (collections:Vector) {
    collections:Vector vector = {vec:[]};
    int i = 0;

    while (i < lengthof values) {
        vector.add(values[i]);
        i = i + 1;
    }

    return vector;
}

function testClear (int size) (collections:Vector, collections:Vector) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    collections:Vector vectorRef = vector;
    vector.clear();

    return vector, vectorRef;
}

function testGet (int size) (any x1, any x2, any x3, any x4) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    x1 = vector.get(0);
    x2 = vector.get(2);
    x3 = vector.get(4);
    x4 = vector.get(6);

    return;
}

function testInsert (int[] values, int[] indices, int size) (collections:Vector) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    int i = 0;
    while (i < lengthof values) {
        vector.insert(values[i], indices[i]);
        i = i + 1;
    }

    return vector;
}

function testIsEmpty (int size) (boolean[] isEmpty) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    isEmpty = [];
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

    return;
}

function testSize (int[] addElems, int[] insertElems, int[] replaceElems, int nRemoveElems, int size) (int[] vecSizes) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    vecSizes = [];
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

    return;
}

function testRemove (int size) (collections:Vector vector, any x1, any x2, any x3) {
    vector = {vec:[]};
    populateVector(vector, size);

    x1 = vector.remove(0);
    x2 = vector.remove(2);
    x3 = vector.remove(size - 3);

    return;
}

function testReplace (int[] values, int[] indices, int size) (collections:Vector, any[]) {
    collections:Vector vector = {vec:[]};
    any[] replacedVals = [];
    populateVector(vector, size);

    int i = 0;
    while (i < lengthof values) {
        replacedVals[i] = vector.replace(values[i], indices[i]);
        i = i + 1;
    }

    return vector, replacedVals;
}

function testGetIndexOutOfRange (int[] indices, int size) (any[] vals, error[] errs) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);
    vals = [];
    errs = [];
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
    return;
}

function testInsertIndexOutOfRange (int[] values, int[] indices, int size) (collections:Vector vector, collections:IndexOutOfRangeError[] errs) {
    vector = {vec:[]};
    populateVector(vector, size);
    errs = [];
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

    return vector, errs;
}

function testRemoveIndexOutOfRange (int[] indices, int size) (collections:Vector vector, any[] removedVals, collections:IndexOutOfRangeError[] errs) {
    vector = {vec:[]};
    populateVector(vector, size);
    removedVals = [];
    errs = [];
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

    return;
}

function testReplaceIndexOutOfRange (int[] values, int[] indices, int size) (collections:Vector vector, collections:IndexOutOfRangeError[] errs) {
    vector = {vec:[]};
    populateVector(vector, size);
    errs = [];
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

    return vector, errs;
}

function populateVector (collections:Vector vector, int size) {
    int i = 1;
    while (i <= size) {
        vector.add(i * 10);
        i = i + 1;
    }
}
