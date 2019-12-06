// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# The type bound for a table row.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type RowType record{};

# Returns an iterator over the members of `tbl`.
# The iterator will iterate over the rows of the table in order.
# + tbl - table to operate on
# + return - an iterator over `tbl`
public function iterator(table<RowType> tbl) returns abstract object {
    public function next() returns record {|
        RowType value;
    |}?;
} {
    TableIterator tableIterator = new(tbl);
    return tableIterator;
}
