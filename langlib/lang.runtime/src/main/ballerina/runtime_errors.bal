// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Type for value that can be cloned.
# This is the same as in lang.value, but is copied here to avoid a dependency.
type Cloneable readonly|xml|Cloneable[]|map<Cloneable>|table<map<Cloneable>>;

# Error Detail Record
public type Detail record {|
   Cloneable...;
|};

// TypeCastError
public type TypeCastError distinct error;

// IndexOutOfRangeError
public type IndexOutOfRange distinct error;
public type IndexNumberTooLarge distinct IndexOutOfRange;
public type ArrayIndexOutOfRange distinct IndexOutOfRange;
public type TupleIndexOutOfRange distinct IndexOutOfRange;
public type StringIndexOutOfRange distinct IndexOutOfRange;

// InherentTypeViolationError
public type InherentTypeViolation distinct error;
public type IncompatibleType distinct InherentTypeViolation;
public type InherentTableTypeViolation distinct InherentTypeViolation;
public type IncompatibleTypeForJsonCasting distinct InherentTypeViolation;

// StringOperationError
public type StringOperationError distinct error;
public type InvalidSubstringRange distinct StringOperationError;
public type NotEnoughFormatArgumentsError distinct StringOperationError;
public type InvalidFormatSpecifierError distinct StringOperationError;
public type IllegalFormatConversionError distinct StringOperationError;

public type ArithmeticOperationError distinct error;

// ConversionError
public type ConversionError distinct error;
public type NumberConversionError distinct ConversionError;
public type CannotConvertNil distinct ConversionError;

// KeyConstraintViolationError
public type KeyConstraintViolation distinct error;

// IllegalListInsertionError
public type IllegalListInsertion distinct error;

// KeyNotFoundError
public type KeyNotFound distinct error;

// InvalidUpdate
public type InvalidUpdate distinct error;
public type InvalidReadonlyValueUpdate distinct InvalidUpdate;
public type InvalidFinalFieldUpdate distinct InvalidUpdate;

// JSONOperationError
public type JSONOperationError distinct error;
