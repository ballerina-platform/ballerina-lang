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

documentation{
     Represents a reader which will wrap string content as a channel.
}
public type StringReader object {
    private CharacterChannel? characterChannel;

    documentation{
      Constructs a channel to read string.

      P{{content}} content which should be written
      P{{encoding}} encoding of the characters of the content
    }
    public new(string content, string encoding = "UTF-8") {
        byte[] contentBytes = content.toByteArray(encoding);
        ByteChannel byteChannel = createMemoryChannel(contentBytes);
        characterChannel = new CharacterChannel(byteChannel, encoding);
    }

    documentation{
        Reads string as json from reader.

        R{{}} json or an error
    }
    public function readJson() returns json|error {
        return characterChannel.readJson();
    }

    documentation{
        Reads string as XML from reader

        R{{}}
    }
    public function readXml() returns xml|error? {
        return characterChannel.readXml();
    }

    documentation{
        Reads characters from the given string.

        P{{nCharacters}} read specifc number of characters
        R{{}} string or an error
    }
    public function readChar(int nCharacters) returns string|error? {
        return characterChannel.read(nCharacters);
    }

    documentation{
        Closes reader.
    }
    public function close() returns error? {
        return characterChannel.close();
    }
};
