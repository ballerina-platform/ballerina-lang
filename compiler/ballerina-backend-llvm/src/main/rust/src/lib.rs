// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

extern crate libc;

//use std::io;
//use std::io::prelude::*;
//use std::mem;
use std::os::raw::c_longlong;

#[no_mangle]
pub extern "C" fn new_ref_array(size: c_longlong) -> *mut Vec<*mut c_longlong> {
    let size_val = size as usize;
    let foo: Box<Vec<*mut c_longlong>> = Box::new(Vec::with_capacity(size_val));
    let vec_pointer = Box::into_raw(foo);
    return vec_pointer as *mut Vec<*mut c_longlong>;
}
