import object_mocking2.mod1;

final mod1:HttpClient cl = check initializeHttpClient();

function initializeHttpClient() returns mod1:HttpClient|error => new ();

function fn() returns string|error {
    return cl->/testPath;
}
