// Copyright (c) 2024 WSO2 Inc. (http://www.wso2.org).
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

type R readonly;
type N xml<never>;
type T xml:Text;
type RN readonly & N;
type RO_E xml:Element & readonly;
type RO_C xml:Comment & readonly;
type RO_P xml:ProcessingInstruction & readonly;

type RO_XML xml<N|RO_E|RO_C|RO_P|T>;

type Rec record {|
    int a;
|};

type T1 xml<R>;
type T2 xml<int|T>;
type T3 xml<T|Rec>;

function testXmlTypeDescriptorWithTypeParameterNegative() {
    xml<xml:Element> a = xml ``;
    RO_XML _ = a;

    xml:Element|xml:Comment b = xml `<foo/>`;
    RO_XML _ = b;

    xml<xml:Element|xml:Comment> c = xml `<!--foo--><foo/>`;
    RO_XML _ = c;

    RO_XML d = xml `<?target data?>`;
    RO_C|RO_P|T _ = d;
}
