// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

type XC xml:Comment;
type XE xml:Element;
type XCE XC|XE|int;

function testXmlStepExprWithUnionTypeNegative() {
    xml<xml:Element>|int x1 = xml `<a><b><c><e>foo</e></c></b><d><g>bar</g></d></a>`;
    _ = x1/*;
    _ = x1/<d>;

    xml:Comment|string x2 = xml `<!-- comment node-->`;
    _ = x2/*;
    _ = x2/*.<a>;

    xml:Element|xml:ProcessingInstruction|record {|xml x;|} x3 = xml `<?target data?>`;
    _ = x3/*;
    _ = x3/*.<b>;

    xml:Text|xml:ProcessingInstruction|boolean x4 = xml `test xml text`;
    _ = x4/*;
    _ = x4/*.<d>;

    XCE x5 = xml `<foo><bar>b</bar></foo>`;
    _ = x5/*.<bar>;
    _ = x5/**/<bar>;

    xml<xml:Element>|xml<xml:Comment>|xml<xml:ProcessingInstruction>|xml:Text|int x6 = xml `<l><m><n></n></m></l>`;
    _ = x6/*;
    _ = x6/*.<m>;
}
