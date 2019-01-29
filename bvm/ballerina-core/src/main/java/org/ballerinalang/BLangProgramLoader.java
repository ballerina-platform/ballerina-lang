/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang;

import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ProgramFileFormatException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * This class contains utilities to load a {@code ProgramFile} from Ballerina program file (.balx file).
 *
 * @since 0.90
 */
public class BLangProgramLoader {

    public static ProgramFile read(Path balxFilePath) {

        ProgramFileReader programFileReader = new ProgramFileReader();
        try {
            return programFileReader.readProgram(balxFilePath);
        } catch (FileNotFoundException | NoSuchFileException e) {
            throw new BLangRuntimeException("ballerina: cannot find program file '" + balxFilePath.toString() + "'",
                    e);
        } catch (IOException e) {
            throw new BLangRuntimeException("ballerina: error reading program file: '" + e.getMessage() + "'", e);
        } catch (BLangRuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new ProgramFileFormatException("ballerina: invalid program file format", e);
        }
    }
}
