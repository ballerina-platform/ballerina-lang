function emptyFunction () {
}

function funcEmptyDefaultWorker () {
    worker newWorker1 {
        int a = 5;
    }

    worker newWorker2 {
        int a = 5;
    }
}

function test1 () {
    string a = "hello";
    if (a == "a") {
        a = "b";
    }
}

function addFloat1 (float x, float y) (float z) {
    z = x + y;
    return;
}

function test2 () {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else {
        a = "c";
    }
}

function addFloat2 (float x, float y) (float z) {
    z = x + y;
    return;
}

function test3 () {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else if (a == "b") {
        a = "c";
    }
}

function test4 () {
    string a = "hello";
    try {
        a = "b";
    } catch (error e) {
        a = "c";
    }
}

function test5 () {
    string a = "hello";
    try {
        a = "b";
    } finally {
        a = "c";
    }
}

function addFloat3 (float x, float y) (float z) {
    z = x + y;
    return;
}

function addFloat4 (float x, float y) (float z) {
    z = x + y;
    return;
}

function addFloat5 (float x, float y) (float z) {
    z = x + y;
    return;
}
