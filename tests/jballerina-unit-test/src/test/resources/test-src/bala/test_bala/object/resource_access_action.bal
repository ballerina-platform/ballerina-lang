// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import testorg/client_classes as cli;

function testBasicClientResourceAccess() {
    cli:MyClient myClient = new;

    int a = myClient->/;
    assertEquality(a, 3);
    
    int b = myClient->/.get;
    assertEquality(b, 3);
    
    int b1 = myClient->/.post;
    assertEquality(b1, 2);
    
    int c = myClient->/.get();
    assertEquality(c, 3);
    
    string[2] d = myClient->/books/names;
    assertEquality(d, <string[2]>["book1", "book2"]);
    
    string[2] e = myClient->/books/names.get;
    assertEquality(e, <string[2]>["book1", "book2"]);
    
    string[2] f = myClient->/books/names.get();
    assertEquality(f, <string[2]>["book1", "book2"]);
}

function testClientResourceAccessContainingComputedResourceAccessSegments() {
    cli:MyClient2 myClient = new;
    "books" books = "books";

    string[2] a1 = myClient->/[books]/["names"];
    assertEquality(a1, <string[2]>["book1", "book2"]);
    
    string a2 = myClient->/books/[1];
    assertEquality(a2, "book1");

    string a3 = myClient->/books/[1].get;
    assertEquality(a3, "book1");

    int bookId = 2;
    string a4 = myClient->/books/[bookId];
    assertEquality(a4, "book2");

    string b1 = myClient->/books/book1/[1.5];
    assertEquality(b1, "book1.1.5");

    string b2 = myClient->/books/book1/[5];
    assertEquality(b2, "book1.5.0");

    string b3 = myClient->/books/book1/[1.5].get;
    assertEquality(b3, "book1.1.5");

    float volume = 2;
    string b4 = myClient->/books/book1/[volume];
    assertEquality(b4, "book1.2.0");

    string book1 = "book1";
    string c1 = myClient->/books/[book1]/[1.5].post;
    assertEquality(c1, "book1.1.5");

    string c2 = myClient->/books/book1/[5].post;
    assertEquality(c2, "book1.5");

    string c3 = myClient->/books/book1/[1.5].post;
    assertEquality(c3, "book1.1.5");

    decimal volume2 = 2;
    string c4 = myClient->/books/[book1]/[volume2].post;
    assertEquality(c4, "book1.2");

    boolean d1 = myClient->/[true];
    assertEquality(d1, true);

    boolean flag = false;
    true boolVal = true;
    boolean d2 = myClient->/[flag];
    assertEquality(d2, false);

    boolean d3 = myClient->/[boolVal];
    assertEquality(d3, true);
}

function testConstantAndUserDefinedTypesInResourcePathWithoutVarName() {
    cli:MyClient3 myClient = new;
    "someLongPathSegment" path = "someLongPathSegment";

    int a1 = myClient->/books/someLongPathSegment;
    assertEquality(a1, 1);

    int a11 = myClient->/books/someLongPathSegment.put(3);
    assertEquality(a11, 3);

    int a12 = myClient->/books/someLongPathSegment.someOtherMethod(2);
    assertEquality(a12, 2);

    int a2 = myClient->/books/[cli:PATH];
    assertEquality(a2, 1);

    int a3 = myClient->/books/[path];
    assertEquality(a3, 1);

    int b1 = myClient->/books/x.post;
    assertEquality(b1, 2);

    int b2 = myClient->/books/["x"].post;
    assertEquality(b2, 2);

    "x" x = "x";
    int b3 = myClient->/books/[x].post;
    assertEquality(b3, 2);
}

function testWithResourceAccessRestSegment() {
    cli:MyClient4 myClient = new;

    string[] a1 = myClient->/books/someLongPathSegment;
    assertEquality(a1, <string[]>["someLongPathSegment"]);

    string[] a2 = myClient->/books;
    assertEquality(a2, <string[]>[]);

    string[] a3 = myClient->/books/path/morePathSegments/morePathSegments;
    assertEquality(a3, <string[]>["path", "morePathSegments", "morePathSegments"]);

    string[] a4 = myClient->/books/path/["morePathSegments"]/morePathSegments;
    assertEquality(a4, <string[]>["path", "morePathSegments", "morePathSegments"]);

    string[] pathSegArray = ["path1", "path2"];
    [string] pathSegTuple = ["path1"];
    string[] b1 = myClient->/books/path/[...pathSegArray];
    assertEquality(b1, <string[]>["path", "path1", "path2"]);

    string[] b11 = myClient->/books/path/[...pathSegTuple];
    assertEquality(b11, <string[]>["path", "path1"]);

    string[] b2 = myClient->/books/path/[...["path1", "path2"]];
    assertEquality(b2, <string[]>["path", "path1", "path2"]);

    int[3] intArray = [1, 2, 3];
    [int, int] intTuple = [1, 2];

    int[] b3 = myClient->/[...intArray];
    assertEquality(b3, <int[]>[2, 3]);

    int[] b4 = myClient->/[...[1, 2, 3]];
    assertEquality(b4, <int[]>[2, 3]);

    int[] b5 = myClient->/[...intTuple];
    assertEquality(b5, <int[]>[2]);

    int[] b51 = myClient->/[1];
    assertEquality(b51, <int[]>[]);

    int[] b52 = myClient->/[1]/[2]/[3]/[4]/[5];
    assertEquality(b52, <int[]>[2, 3, 4, 5]);

    int b6 = myClient->/[...intTuple].post;
    assertEquality(b6, 2);

    "books"[2] booksArray = ["books", "books"];
    string[] b7 = myClient->/[...booksArray].post;
    assertEquality(b7, <"books"[]>["books"]);

    string b8 = myClient->/[...booksArray].put;
    assertEquality(b8, "books");

    string[] b9 = myClient->/[...booksArray].someOtherMethod;
    assertEquality(b9, <"books"[]>[]);

    "books"[4] booksArray2 = ["books", "books", "books", "books"];
    string[] b10 = myClient->/[...booksArray2].someOtherMethod;
    assertEquality(b10, <"books"[]>["books", "books"]);

    string[] b12 = myClient->/[...booksArray2].someOtherMethod2;
    assertEquality(b12, <"books"[]>["books"]);

    string[] b13 = myClient->/[...booksArray2].someOtherMethod3;
    assertEquality(b13, <"books"[]>["books", "books"]);
}

function testResourceAccessWithArguments() {
    cli:MyClient5 myClinet = new;

    string[][] a1 = myClinet->/;
    assertEquality(a1, <string[][]>[[], []]);

    string[][] a2 = myClinet->/books/books2;
    assertEquality(a2, <string[][]>[["books", "books2"], []]);

    string[][] a3 = myClinet->/("books", "pen");
    assertEquality(a3, <string[][]>[[], ["books", "pen"]]);

    string[][] a4 = myClinet->/a/b/c("d", "e");
    assertEquality(a4, <string[][]>[["a", "b", "c"], ["d", "e"]]);

    string[][] a41 = myClinet->/["a"]/[...["b", "c"]](...["d", "e"]);
    assertEquality(a41, <string[][]>[["a", "b", "c"], ["d", "e"]]);

    int a5 = myClinet->/a.get1(5, 6);
    assertEquality(a5, 5);

    string a = "a";
    int a6 = myClinet->/[a].get1(4, 6);
    assertEquality(a6, 4);

    int a7 = myClinet->/[...["car"]].get1(3, 6);
    assertEquality(a7, 3);

    [int, string, int[]] a8 = myClinet->/path/[1]/path2/path3("book", 6);
    assertEquality(a8, <[int, string, int[]]>[1, "book", [6]]);

    "path2"[2] pathArray = ["path2", "path2"];
    [int, string, int[]] a9 = myClinet->/path/[1]/[...pathArray]("book", 6);
    assertEquality(a9, <[int, string, int[]]>[1, "book", [6]]);

    int[2] a10 = myClinet->/[5].put(1, 6);
    assertEquality(a10, <int[2]>[1, 6]);

    int[2] a11 = myClinet->/[5].put(1);
    assertEquality(a11, <int[2]>[1, 5]);
}

function testStaticTypeOfClientResourceAccessAction() {
    cli:MyClient6 myClinet = new;

    var a1 = myClinet->/;
    string[][] a01 = a1;
    assertEquality(a01, <string[][]>[[], []]);

    var a2 = myClinet->/books/books2;
    string[][] a02 = a2;
    assertEquality(a02, <string[][]>[["books", "books2"], []]);

    var a3 = myClinet->/("books", "pen");
    string[][] a03 = a3;
    assertEquality(a03, <string[][]>[[], ["books", "pen"]]);

    var a5 = myClinet->/a.get1(5, 6);
    int? a05 = a5;
    assertEquality(a05, 5);

    var a7 = myClinet->/[...["car"]].get1(3, 6);
    int? a07 = a7;
    assertEquality(a07, 3);

    var a8 = myClinet->/path/[1]/path2/path3("book", 6);
    [int, string, int[]] a08 = a8;
    assertEquality(a08, <[int, string, int[]]>[1, "book", [6]]);

    var a10 = myClinet->/[5].put(1, 6);
    int[2] a010 = a10;
    assertEquality(a010, <int[2]>[1, 6]);

    var a11 = myClinet->/[5].put(1);
    int[2] a011 = a11;
    assertEquality(a011, <int[2]>[1, 5]);

    var a12 = myClinet->/path/[true]("car");
    boolean|xml a012 = a12;
    assertEquality(a012, true);

    var a13 = myClinet->/[1.45]("car");
    float|map<int> a013 = a13;
    assertEquality(a013, 1.45);

    var a14 = myClinet->/[1.0003]/path("car");
    decimal a014 = a14;
    assertEquality(a014, 1.0003d);
}

function testResourceAccessOfAnObjectConstructedViaObjectCons() {
    string[][] a1 = cli:myClinet->/;
    assertEquality(a1, <string[][]>[[], []]);

    string[][] a2 = cli:myClinet->/books/books2;
    assertEquality(a2, <string[][]>[["books", "books2"], []]);

    string[][] a3 = cli:myClinet->/("books", "pen");
    assertEquality(a3, <string[][]>[[], ["books", "pen"]]);

    string[][] a41 = cli:myClinet->/["a"]/[...["b", "c"]](...["d", "e"]);
    assertEquality(a41, <string[][]>[["a", "b", "c"], ["d", "e"]]);

    int a5 = cli:myClinet->/a.get1(5, 6);
    assertEquality(a5, 5);

    int a7 = cli:myClinet->/[...["car"]].get1(3, 6);
    assertEquality(a7, 3);

    [int, string, int[]] a8 = cli:myClinet->/path/[1]/path2/path3("book", 6);
    assertEquality(a8, <[int, string, int[]]>[1, "book", [6]]);

    "path2"[2] pathArray = ["path2", "path2"];
    [int, string, int[]] a9 = cli:myClinet->/path/[1]/[...pathArray]("book", 6);
    assertEquality(a9, <[int, string, int[]]>[1, "book", [6]]);

    int[2] a10 = cli:myClinet->/[5].put(1, 6);
    assertEquality(a10, <int[2]>[1, 6]);

    int[2] a11 = cli:myClinet->/[5].put(1);
    assertEquality(a11, <int[2]>[1, 5]);

    int a12 = cli:myClinet->/[cli:PATH]/path.put(4);
    assertEquality(a12, 4);
}

function testResourceAccessContainingSpecialChars() {
    cli:MyClient7 myClient = new;

    string a = myClient->/customers\-json;
    assertEquality(a, "response1");

    string b = myClient->/'customers\-json;
    assertEquality(b, "response1");

    string c = myClient->/'955;
    assertEquality(c, "response2");

    string d = myClient->/A\u{0042};
    assertEquality(d, "response3");

    string e = myClient->/AB;
    assertEquality(e, "response3");

    string f = myClient->/A\u{0042}.post;
    assertEquality(f, "response4");
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
