// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

client class BookService {
    resource function get .() returns Book[] {
        return [
            {id: 1, title: "AtoZ", author: "Jeo"},
            {id: 2, title: "BtoY", author: "Frank"}
        ];
    }

    resource function get authors/[string... names]() returns string[] {
        return names;
    }

    resource function post authors(string author) returns string {
        return author;
    }

    resource function get registered/[float|decimal isbn]/[int... ex]() returns Book? {
        return {id: 1, title: "AtoZ", author: "Jeo"};
    }

    resource function get ["is-author"]/[string author]/[int bookId]() returns boolean {
        return true;
    }

    resource function post [float... fl]() {
    }

    resource function get [string...]() {
    }

    resource function get [string]() {
    }

    resource function get books/[int year]/["AD"|"BC" era]/[string... authors]() {
    }

    resource function put books/[int]/["AD"|"BC" era]/[string... authors]() {
    }

    resource function post books/[int year]/["AD"|"BC" era]/[string...]() {
    }

    resource function patch books/[int]/["AD"|"BC"]/[string...]() {
    }
}

client class MemberService {
    @Pipe {
        meta: "ZO"
    }
    resource function get members() {
    }

    resource function get members/[@Pipe {meta: "XO"} string id](boolean isRegistered) {
    }

    resource function get members/filter/[@Pipe {meta: "XO"} string... ids]() {
    }
}

type Book record {|
    readonly int id;
    string title;
    string author;
|};

type PipeType record {|
    string meta;
|};

annotation PipeType Pipe;
