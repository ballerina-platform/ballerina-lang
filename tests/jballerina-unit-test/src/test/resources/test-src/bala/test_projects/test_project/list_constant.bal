// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
// under the License

type IntArr int[];

const int length = 3;

public const string[] l1 = ["a", "b", "c"];
public const [int, string] l2 = [1, "d"];
public const [int, [string, int]] l3 = [1, ["e", 2]];
public const int[][] l4 = [[1, 2, 3], [4, 5, 6]];
public const boolean[length] l5 = [true, false, true];
public const IntArr l6 = [1, 2, 3];
public const [int, string...] l7 = [1, "f", "g"];
public const (string|int)[][] l8 = [[1, "2", 3], [4, 5, 6]];
public const [(string[]|int[])...] l9 = [[1, 2, 3], ["4", "5", "6"]];
