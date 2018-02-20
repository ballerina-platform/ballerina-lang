import ballerina.file;

function testCopy(string source, string dest) (file:FileNotFoundError err1, file:AccessDeniedError err2, file:IOError err3){
    file:File sourceFile = {path:source};
    file:File destFile = {path:dest};

    err1, err2, err3 = file:copy(sourceFile, destFile, false);
    return;
}

function testMove(string target, string dest) (file:FileNotFoundError err1, file:AccessDeniedError err2, file:IOError err3){
    file:File targetFile = {path:target};
    file:File destFile = {path:dest};

    err1, err2, err3 = file:move(targetFile, destFile, false);
    return;
}
