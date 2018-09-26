/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.auth.ldap.util;

import org.apache.commons.lang3.ArrayUtils;
import org.ballerinalang.auth.ldap.UserStoreException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This class wraps a character array to be used to handle sensitive data like passwords.
 */
public class Secret {

    private char[] chars;
    private byte[] bytes;
    private int accessCount;

    private Secret(char[] chars) {
        this.chars = chars;
        this.accessCount = 0;
    }

    /**
     * Returns the secret as a character array.
     *
     * @return char[]
     */
    public char[] getChars() {
        if (chars == null) {
            this.chars = ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        return Arrays.copyOf(chars, chars.length);
    }

    /**
     * Returns the secret as a byte array in UTF-8 format.
     *
     * @return byte[]
     */
    public byte[] getBytes() {
        return getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns the secret as a byte array for the given encoding format.
     *
     * @param charset Character encoding format
     * @return byte[]
     */
    public byte[] getBytes(Charset charset) {
        clearBytes(bytes);
        CharBuffer charBuffer = CharBuffer.wrap(getChars());
        ByteBuffer byteBuffer = charset.encode(charBuffer);
        bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Check if the character array of the secret is null or empty.
     *
     * @return true if character array is null or empty, else return false
     */
    public boolean isEmpty() {
        return chars == null || chars.length < 1;
    }

    /**
     * Sets the given character array as the secret and clears the previous character array.
     *
     * @param chars character array of the secret
     */
    public void setChars(char[] chars) {
        clearChars(this.chars);
        this.chars = Arrays.copyOf(chars, chars.length);
    }

    /**
     * Adds the given character array to the existing character array of the secret.
     *
     * @param chars character array to be added
     */
    public void addChars(char[] chars) {
        char[] previous = getChars();
        setChars(ArrayUtils.addAll(previous, chars));
        clearChars(previous);
    }

    /**
     * Clears byte and character arrays of the secret.
     * For each invocation of this method the internal counter which tracks the access count of the instance is
     * decremented. Byte and character arrays are cleared if and only if the internal counter value is less than 0.
     * <p>
     * For proper operation, this method should be invoked once, per each invocation of getSecret factory method.
     */
    public void clear() {
        accessCount--;
        if (accessCount < 0) {
            clearChars(this.chars);
            clearBytes(this.bytes);
        }
    }

    /**
     * Returns an instance of a Secret for the given secret type.
     * Given secret type should be an instance of a Secret, char[] or a String.
     * If the given secret is of Secret type the internal counter which tracks the access count of the instance is
     * incremented. Thus, for proper operation once the instance of Secret is retrieved from this method and used,
     * clear() method should be invoked to clear internal char and byte arrays.
     *
     * @param secret given secret. Supports only Secret, char[] or a String types
     * @return an instance of Secret
     * @throws UserStoreException thrown if the given secret is not either
     *                            a Secret, char[] or String
     */
    public static Secret getSecret(Object secret) throws UserStoreException {
        if (secret != null) {
            if (secret instanceof Secret) {
                Secret secretObj = (Secret) secret;
                secretObj.accessCount++;
                return secretObj;
            } else if (secret instanceof char[]) {
                char[] secretChars = (char[]) secret;
                return new Secret(Arrays.copyOf(secretChars, secretChars.length));
            } else if (secret instanceof String) {
                return new Secret(((String) secret).trim().toCharArray());
            } else {
                throw new UserStoreException(
                        "Unsupported Secret Type. Can handle only string type or character array type secrets");
            }
        }

        return new Secret(ArrayUtils.EMPTY_CHAR_ARRAY);
    }

    private void clearChars(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, '\u0000');
        }
    }

    private void clearBytes(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }
}
