// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function testMethodAccessWithCasting() returns string {
    xml bookstore = xml `<bookstore>
                            <book category="cooking">
                                <title lang="en">Everyday Italian</title>
                                <author>Giada De Laurentiis</author>
                                <year>2005</year>
                                <price>30.00</price>
                            </book>
                            <book category="children">
                                <title lang="en">Harry Potter</title>
                                <author>J. K. Rowling</author>
                                <year>2005</year>
                                <price>29.99</price>
                            </book>
                            <book category="web">
                                <title lang="en">XQuery Kick Start</title>
                                <author>James McGovern</author>
                                <author>Per Bothner</author>
                                <author>Kurt Cagle</author>
                                <author>James Linn</author>
                                <author>Vaidyanathan Nagarajan</author>
                                <year>2003</year>
                                <price>49.99</price>
                            </book>
                            <book category="web" cover="paperback">
                                <title lang="en">Learning XML</title>
                                <author>Erik T. Ray</author>
                                <year>2003</year>
                                <price>39.95</price>
                            </book>
                        </bookstore>`;

    string finalOutput = from var elem in bookstore/<book>/<title>
                              select ((<xml>elem)/*).toString()+"|";

    return finalOutput;
}
