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

type CustomError distinct error;

class Iterable {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            public isolated function next() returns record {|int value;|}? {
                return {
                    value: 0
                };
            }
        };
    }
}

class IterableWithError {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|int value;|}|CustomError?;
    } {
        return object {
            public isolated function next() returns record {|int value;|}|CustomError? {
                return {
                    value: 0
                };
            }
        };
    }
}

class IterableWithoutNilReturn {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|int value;|}|error;
    } {
        return object {
            public isolated function next() returns record {|int value;|}|error {
                return {
                    value: 0
                };

            }
        };
    }
}

type Customer record {
    readonly int id;
    readonly string name;
    int noOfItems;
};

type CustomerTable table<Customer> key(id);

Iterable numGen = new Iterable();

IterableWithError numGenWithError = new IterableWithError();

IterableWithoutNilReturn numGenWithoutNilReturn = new IterableWithoutNilReturn();

function testStreamConstruction() {
    //expected 'stream<int,error>', found 'stream<int>'
    stream<int, error> _ = stream from int i in numGen
        select i;

    stream<int, error?> _ = stream from int i in numGen
        select i;

    //expected 'stream<int>', found 'stream<int, CustomError?>'
    stream<int> _ = stream from int i in numGenWithError
        select i;

    //expected 'stream<int,error>', found 'stream<int, CustomError?>'
    stream<int, error> _ = stream from int i in numGenWithError
        select i;

    stream<int, error?> _ = stream from int i in numGenWithError
        select i;

    stream<int, CustomError?> _ = stream from int i in numGenWithError
        select i;

    //expected 'stream<int>', found 'stream<int,CustomError?>'
    stream<int> _ = stream from int i in 1 ... 3
        select check getIntOrCustomError();

    //expected 'stream<int>', found 'stream<int,CustomError?>'
    stream<int> _ = stream from int i in numGenWithError
        select check getIntOrCustomError();

    //expected 'stream<int>', found 'stream<int,CustomError?>'
    stream<int> _ = stream from int i in 1 ... 3
        let int j = check getIntOrCustomError()
        select j;

    //expected 'stream<int,error>', found 'stream<int,CustomError?>'
    stream<int, error> _ = stream from int i in 1 ... 3
        where i == check getIntOrCustomError()
        select i;

    //expected 'stream<int,error>', found 'stream<int,(CustomError|error)?>'
    stream<int, error> _ = stream from int i in numGenWithError
        select check getIntOrError();

    //expected 'stream<int>', found 'stream<int,error>'
    stream<int> _ = stream from int i in numGenWithoutNilReturn
        select i;

    stream<int, error> _ = stream from int i in numGenWithoutNilReturn
        select check getIntOrError();
}

function testTableConstruction() {
    //expected 'CustomerTable', found 'CustomerTable|error)'
    CustomerTable customerTable1 = table key(id) from int i in 1 ... 3
        select check getCustomerOrError();

    CustomerTable|error customerTable2 = table key(id) from int i in 1 ... 3
        select check getCustomerOrError();

    //expected 'CustomerTable', found 'CustomerTable|error)'
    CustomerTable customerTable3 = table key(id) from int i in 1 ... 3
        let int id = check getIntOrError()
        select {id: 1, name: "Melina", noOfItems: 12};

    //expected 'CustomerTable', found 'CustomerTable|error)'
    CustomerTable customerTable4 = table key(id) from int i in check getIntArrOrError()
        select {id: 1, name: "Melina", noOfItems: 12};

    //expected '(CustomerTable|CustomError)', found '(CustomerTable|CustomError|error)'
    CustomerTable|CustomError customerTable5 = table key(id) from int i in numGenWithError
        select check getCustomerOrError();
}

function testArrayConstruction() {
    int[] _ = from int i in numGen
        select i;

    //expected 'int[]', found 'int[]|CustomError'
    int[] arr1 = from int i in numGenWithError
        select i;

    int[]|error arr2 = from int i in numGenWithError
        select i;

    int[]|CustomError arr3 = from int i in numGenWithError
        select i;

    //expected 'int[]', found 'int[]|CustomError'
    int[] _ = from int i in 1 ... 3
        select check getIntOrCustomError();

    //expected 'int[]', found 'int[]|CustomError'
    int[] _ = from int i in numGenWithError
        select check getIntOrCustomError();

    //expected 'int[]', found 'CustomError|int[]'
    int[] _ = from int i in 1 ... 3
        let int j = check getIntOrCustomError()
        select j;

    //expected 'stream<int,error>', found 'stream<int,CustomError?>'
    int[]|error arr4 = from int i in 1 ... 3
        where i == check getIntOrCustomError()
        select i;

    int[]|error arr5 = from int i in numGenWithError
        select check getIntOrError();

    //expected 'int[]', found 'int[]|error'
    int[] _ = from int i in numGenWithoutNilReturn
        select i;

    //expected 'int[]', found 'int[]|error'
    int[] _ = from int i in numGenWithoutNilReturn
        select check getIntOrError();
}

xml theXml = xml `<book>the book</book>`;
xml bitOfText = xml `bit of text\u2702\u2705`;
xml compositeXml = theXml + bitOfText;

function testXmlConstruction() {
    //expected 'xml', found 'xml|error'
    xml _ = from var elem in compositeXml
        select check getXmlOrError();

    //expected 'xml', found 'xml|error'
    xml _ = from var elem in check getXmlArrOrError()
        select theXml;

    //expected 'xml', found 'xml|CustomError'
    xml _ = from var elem in compositeXml
        let int i = check getIntOrCustomError()
        select theXml;

    //'(xml[]|error)' is not an iterable collection
    xml _ = from var elem in (from var x in check getXmlArrOrError()
            select x)
        select theXml;

    //expected 'xml', found 'xml|error'
    xml _ = from var elem in (check from var x in check getXmlArrOrError()
            select x)
        select theXml;
}

function getCustomerOrError() returns Customer|error {
    return error("Dummy Error");
}

function getIntOrCustomError() returns int|CustomError {
    return error CustomError("Custom error");
}

function getIntOrError() returns int|error {
    return error("Custom error");
}

function getIntArrOrError() returns int[]|error {
    return error("Custom error");
}

function getXmlOrError() returns xml|error {
    return error("Custom error");
}

function getXmlArrOrError() returns xml[]|error {
    return error("Custom error");
}
