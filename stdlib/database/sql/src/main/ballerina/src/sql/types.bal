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

import ballerina/io;
import ballerina/time;

# Represents a parameter for the SQL Client remote functions when a variable needs to be passed
# to the remote function.
#
# + value - Value of paramter passed into the SQL statement
public type TypedValue abstract object {
    public anydata|object{}|record{} value;
};

# Possible type of parameters that can be passed into the SQL query.
public type Value string|int|boolean|float|decimal|byte[]|xml|TypedValue?;

# Represents Varchar SQL field.
#
public type VarcharValue object {
   *TypedValue;

   public function init(string? value = ()) {
       self.value = value;
   }
};

# Represents NVarchar SQL field.
#
public type NVarcharValue object {
   *TypedValue;

   public function init(string? value = ()) {
      self.value = value;
   }
};

# Represents Char SQL field.
#
public type CharValue object {
   *TypedValue;

   public function init(string? value = ()) {
       self.value = value;
   }
};

# Represents NChar SQL field.
#
public type NCharValue object {
   *TypedValue;

   public function init(string? value = ()) {
       self.value = value;
   }
};

# Represents Text SQL field.
#
public type TextValue object {
   *TypedValue;

   public function init(io:ReadableCharacterChannel|string? value = ()) {
       self.value = value;
   }
};

# Represents Clob SQL field.
#
public type ClobValue object {
   *TypedValue;

   public function init(io:ReadableCharacterChannel|string? value = ()) {
       self.value = value;
   }
};

# Represents NClob SQL field.
#
public type NClobValue object {
   *TypedValue;

   public function init(io:ReadableCharacterChannel|string? value = ()) {
       self.value = value;
   }
};

# Represents SmallInt SQL field.
#
public type SmallIntValue object {
   *TypedValue;

   public function init(int? value = ()) {
       self.value = value;
   }
};

# Represents Integer SQL field.
#
public type IntegerValue object {
   *TypedValue;

   public function init(int? value = ()) {
       self.value = value;
   }
};

# Represents BigInt SQL field.
#
public type BigIntValue object {
   *TypedValue;

   public function init(int? value = ()) {
       self.value = value;
   }
};

# Represents Numeric SQL field.
#
public type NumericValue object {
   *TypedValue;

   public function init(int|float|decimal? value = ()) {
       self.value = value;
   }
};

# Represents Decimal SQL field.
#
public type DecimalValue object {
   *TypedValue;

   public function init(int|decimal? value = ()) {
       self.value = value;
   }
};

# Represents Real SQL field.
#
public type RealValue object {
   *TypedValue;

   public function init(int|float|decimal? value = ()) {
       self.value = value;
   }
};

# Represents Float SQL field.
#
public type FloatValue object {
   *TypedValue;

   public function init(int|float? value = ()) {
       self.value = value;
   }
};

# Represents Double SQL field.
#
public type DoubleValue object {
   *TypedValue;

   public function init(int|float|decimal? value = ()) {
       self.value = value;
   }
};

# Represents Bit SQL field.
#
public type BitValue object {
   *TypedValue;

   public function init(boolean|int? value = ()) {
       self.value = value;
   }
};

# Represents Boolean SQL field.
#
public type BooleanValue object {
   *TypedValue;

   public function init(boolean? value = ()) {
       self.value = value;
   }
};

# Represents Binary SQL field.
#
public type BinaryValue object {
   *TypedValue;

   public function init(byte[]|io:ReadableByteChannel? value = ()) {
       self.value = value;
   }
};

# Represents VarBinary SQL field.
#
public type VarBinaryValue object {
   *TypedValue;

   public function init(byte[]|io:ReadableByteChannel? value = ()) {
       self.value = value;
   }
};

# Represents Blob SQL field.
#
public type BlobValue object {
   *TypedValue;

   public function init(byte[]|io:ReadableByteChannel? value = ()) {
       self.value = value;
   }
};

# Represents Date SQL field.
#
public type DateValue object {
   *TypedValue;

   public function init(string|int|time:Time? value = ()) {
       self.value = value;
   }
};

# Represents Time SQL field.
#
public type TimeValue object {
   *TypedValue;

   public function init(string|int|time:Time? value = ()) {
       self.value = value;
   }
};

# Represents DateTime SQL field.
#
public type DateTimeValue object {
   *TypedValue;

   public function init(string|int|time:Time? value = ()) {
       self.value = value;
   }
};

# Represents Timestamp SQL field.
#
public type TimestampValue object {
   *TypedValue;

   public function init(string|int|time:Time? value = ()) {
       self.value = value;
   }
};

# Represents ArrayValue SQL field.
#
public type ArrayValue object {
   *TypedValue;

   public function init(string[]|int[]|boolean[]|float[]|decimal[]|byte[][]? value = ()) {
       self.value = value;
   }
};

# Represents Ref SQL field.
#
public type RefValue object {
   *TypedValue;

   public function init(record{}? value = ()) {
       self.value = value;
   }
};

# Represents Struct SQL field.
#
public type StructValue object {
   *TypedValue;

   public function init(record{}? value = ()) {
       self.value = value;
   }
};

# Represents Row SQL field.
#
public type RowValue object {
   *TypedValue;

   public function init(byte[]? value = ()) {
       self.value = value;
   }
};

# Temporay solution util the language supports `Backtick string` natively as mentioned in
# https://github.com/ballerina-platform/ballerina-spec/issues/442.
#
# + parts - The seperated parts of the sql query
# + insertions - The values that should be filled in between the parts
public type ParameterizedString record {|
   string[] parts;
   Value[] insertions;
|};

# The result of the query without returning the rows.
#
# + affectedRowCount - Number of rows affected by the execution of the query
# + lastInsertId - The integer or string generated by the database in response to a query execution.
#                  Typically this will be from an "auto increment" column when inserting a new row. Not all databases
#                  support this feature, and hence it can be also nil
public type ExecuteResult record {
    int? affectedRowCount;
    string|int? lastInsertId;
};

# The result iterator object that is used to iterate through the results in the event stream.
#
type ResultIterator object {
    private boolean isClosed = false;
    private Error? err;

    public function init(public Error? err = ()) {
        self.err = err;
    }

    public function next() returns record {|record {} value;|}|Error? {
        if (self.isClosed) {
            return closedStreamInvocationError();
        }
        error? closeErrorIgnored = ();
        if (self.err is Error) {
            return self.err;
        } else {
            record {}|Error? result = nextResult(self);
            if (result is record {}) {
                record {|
                    record {} value;
                |} streamRecord = {value: result};
                return streamRecord;
            } else if (result is Error) {
                self.err = result;
                closeErrorIgnored = self.close();
                return self.err;
            } else {
                closeErrorIgnored = self.close();
                return result;
            }
        }
    }

    public function close() returns Error? {
        if (!self.isClosed) {
            if (self.err is ()) {
                Error? e = closeResult(self);
                if (e is ()) {
                    self.isClosed = true;
                }
                return e;
            }
        }
    }
};
