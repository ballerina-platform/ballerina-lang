// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Format which will be used to represent the CSV.
#
# DEFAULT - Would default to the format specified by CSVChannel. Precedence will be given to field
#           separator and record separator.
#
# CSV - Field separator would be "," and the record separator would be new line.
#
# TDF - Field separator will be tab and record separator will be new line.
public type Format DEFAULT|CSV|TDF;

#  Would default to the format specified by CSVChannel. Precedence will be given to field separator and record separator.
public const DEFAULT = "default";

# Field separator would be "," and the record separator would be new line.
public const CSV = "csv";

# Field separator will be tab and record separator will be new line.
public const TDF = "tdf";

# Field separators which are supported by DelimitedTextRecordChannel.
#
# COMMA - Delimited text records would be separated using a comma.
#
# TAB - Delimited text records would be separated using a tab.
#
# COLON - Delimited text records would be separated using a colon(:).
public type Separator COMMA|TAB|COLON|string;

# Comma (,) will be used as the field separator.
public const COMMA = ",";

# Tab (/t) will be use as the field separator.
public const TAB = "\t";

# Colon (:) will be use as the field separator.
public const COLON = ":";
