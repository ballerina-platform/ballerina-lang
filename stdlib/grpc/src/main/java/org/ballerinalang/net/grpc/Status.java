/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc;

import org.ballerinalang.net.grpc.exception.StatusException;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Status of an operation by providing a standard {@link Code} in conjunction with an optional descriptive message.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 *
 * @since 0.980.0
 */
public final class Status implements Serializable {

    private static final long serialVersionUID = -1L;
    public static final StatusMessageMarshaller MESSAGE_MARSHALLER = new StatusMessageMarshaller();
    public static final StatusCodeMarshaller CODE_MARSHALLER = new StatusCodeMarshaller();

    private final Code code;
    private final String description;
    private final Throwable cause;

    /**
     * The set of canonical status codes.
     */
    public enum Code {
        /**
         * The operation completed successfully.
         */
        OK(0),

        /**
         * The operation was cancelled (typically by the caller).
         */
        CANCELLED(1),

        /**
         * Unknown error.
         */
        UNKNOWN(2),

        /**
         * Client specified an invalid argument.
         */
        INVALID_ARGUMENT(3),

        /**
         * Deadline expired before operation could complete.
         */
        DEADLINE_EXCEEDED(4),

        /**
         * Some requested entity (e.g., file or directory) was not found.
         */
        NOT_FOUND(5),

        /**
         * Some entity that we attempted to create (e.g., file or directory) already exists.
         */
        ALREADY_EXISTS(6),

        /**
         * The caller does not have permission to execute the specified operation.
         */
        PERMISSION_DENIED(7),

        /**
         * Some resource has been exhausted.
         */
        RESOURCE_EXHAUSTED(8),

        /**
         * Operation was rejected because the system is not in a state required for the operation's execution.
         */
        FAILED_PRECONDITION(9),

        /**
         * The operation was aborted.
         */
        ABORTED(10),

        /**
         * Operation was attempted past the valid range.
         */
        OUT_OF_RANGE(11),

        /**
         * Operation is not implemented or not supported/enabled in this service.
         */
        UNIMPLEMENTED(12),

        /**
         * Internal errors.
         */
        INTERNAL(13),

        /**
         * The service is currently unavailable.
         */
        UNAVAILABLE(14),

        /**
         * Unrecoverable  loss or corruption.
         */
        DATA_LOSS(15),

        /**
         * The request does not have valid authentication credentials for the operation.
         */
        UNAUTHENTICATED(16);

        private final int value;
        private final byte[] valueAscii;

        Code(int value) {
            this.value = value;
            this.valueAscii = Integer.toString(value).getBytes(Charset.forName("US-ASCII"));
        }

        /**
         * The numerical value of the code.
         */
        public int value() {
            return value;
        }

        /**
         * Returns a {@link Status} object corresponding to this status code.
         */
        public Status toStatus() {
            return STATUS_LIST.get(value);
        }

        private byte[] valueAscii() {
            return valueAscii;
        }
    }

    // Create the canonical list of Status instances indexed by their code values.
    private static final List<Status> STATUS_LIST = buildStatusList();

    private static List<Status> buildStatusList() {
        TreeMap<Integer, Status> canonicalizer = new TreeMap<>();
        for (Code code : Code.values()) {
            Status replaced = canonicalizer.put(code.value(), new Status(code));
            if (replaced != null) {
                throw new IllegalStateException("Code value duplication between "
                        + replaced.getCode().name() + " & " + code.name());
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(canonicalizer.values()));
    }

    /**
     * Return a {@link Status} given a canonical error {@link Code} value.
     */
    public static Status fromCodeValue(int codeValue) {
        if (codeValue < 0 || codeValue > STATUS_LIST.size()) {
            return Code.UNKNOWN.toStatus().withDescription("Unknown code " + codeValue);
        } else {
            return STATUS_LIST.get(codeValue);
        }
    }

    private static Status fromCodeValue(byte[] asciiCodeValue) {
        if (asciiCodeValue.length == 1 && asciiCodeValue[0] == '0') {
            return Code.OK.toStatus();
        }
        return fromCodeValueSlow(asciiCodeValue);
    }

    private static Status fromCodeValueSlow(byte[] asciiCodeValue) {
        int index = 0;
        int codeValue = 0;
        switch (asciiCodeValue.length) {
            case 2:
                if (asciiCodeValue[index] < '0' || asciiCodeValue[index] > '9') {
                    break;
                }
                codeValue += (asciiCodeValue[index++] - '0') * 10;
                codeValue += asciiCodeValue[index] - '0';
                if (codeValue < STATUS_LIST.size()) {
                    return STATUS_LIST.get(codeValue);
                }
                break;
            case 1:
                if (asciiCodeValue[index] < '0' || asciiCodeValue[index] > '9') {
                    break;
                }
                codeValue += asciiCodeValue[index] - '0';
                if (codeValue < STATUS_LIST.size()) {
                    return STATUS_LIST.get(codeValue);
                }
                break;
            default:
                break;
        }
        return Code.UNKNOWN.toStatus().withDescription("Unknown code " + new String(asciiCodeValue, Charset
                .forName("US-ASCII")));
    }

    /**
     * Return a {@link Status} given a canonical error {@link Code} object.
     *
     * @param code Status code.
     * @return Status instance.
     */
    public static Status fromCode(Code code) {
        return code.toStatus();
    }

    /**
     * Extract an error from the causal chain of a {@link Throwable}.
     *
     * @param t {@link Throwable} instance.
     * @return status
     */
    public static Status fromThrowable(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException("Cause is null");
        }
        Throwable cause = t;
        while (cause != null) {
            if (cause instanceof StatusException) {
                return ((StatusException) cause).getStatus();
            } else if (cause instanceof StatusRuntimeException) {
                return ((StatusRuntimeException) cause).getStatus();
            }
            cause = cause.getCause();
        }
        return Code.UNKNOWN.toStatus().withCause(t);
    }

    public static String formatThrowableMessage(Status status) {
        if (status.description == null) {
            return status.code.toString();
        } else {
            return status.code + ": " + status.description;
        }
    }

    private Status(Code code) {
        this(code, null, null);
    }

    private Status(Code code, String description, Throwable cause) {
        this.code = code;
        this.description = description;
        this.cause = cause;
    }

    /**
     * Create a derived instance of {@link Status} with the given cause.
     *
     * @param cause {@link Throwable} instance.
     * @return status instance.
     */
    public Status withCause(Throwable cause) {
        if (java.util.Objects.equals(this.cause, cause)) {
            return this;
        }
        return new Status(this.code, this.description, cause);
    }

    /**
     * Create a derived instance of {@link Status} with the given description.
     *
     * @param description error description.
     * @return status instance.
     */
    public Status withDescription(String description) {
        if (java.util.Objects.equals(this.description, description)) {
            return this;
        }
        return new Status(this.code, description, this.cause);
    }

    /**
     * Create a derived instance of {@link Status} augmenting the current description with additional detail.
     *
     * @param additionalDetail error description.
     * @return status instance.
     */
    public Status augmentDescription(String additionalDetail) {
        if (additionalDetail == null) {
            return this;
        } else if (this.description == null) {
            return new Status(this.code, additionalDetail, this.cause);
        } else {
            return new Status(this.code, this.description + "\n" + additionalDetail, this.cause);
        }
    }

    /**
     * The canonical status code.
     *
     * @return status code.
     */
    public Code getCode() {
        return code;
    }

    /**
     * A description of the status.
     *
     * @return status description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The underlying cause of the error.
     *
     * @return cause of the error.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Returns whether status is OK.
     *
     * @return true, if status is ok. false otherwise.
     */
    public boolean isOk() {
        return Code.OK == code;
    }

    /**
     * Convert this {@link Status} to a {@link RuntimeException}.
     *
     * @return StatusRuntimeException instance.
     */
    public StatusRuntimeException asRuntimeException() {
        return new StatusRuntimeException(this);
    }

    @Override
    public String toString() {
        return ("Status{ code " + code.name() + ", ") +
                "description " + description + ", " +
                "cause " + (cause != null ? cause.getMessage() : null) + "}";
    }

    /**
     * Status Code Marshaller.
     */
    protected static final class StatusCodeMarshaller {

        public byte[] toAsciiString(Status status) {
            return status.getCode().valueAscii();
        }

        public Status parseAsciiString(byte[] serialized) {
            return fromCodeValue(serialized);
        }
    }

    /**
     * Status Message Marshaller.
     */
    protected static final class StatusMessageMarshaller {

        private static final byte[] HEX =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        public byte[] toAsciiString(String value) {
            byte[] valueBytes = value.getBytes(Charset.forName("UTF-8"));
            for (int i = 0; i < valueBytes.length; i++) {
                byte b = valueBytes[i];
                // If there are only non escaping characters, skip the slow path.
                if (isEscapingChar(b)) {
                    return toAsciiStringSlow(valueBytes, i);
                }
            }
            return valueBytes;
        }

        private static boolean isEscapingChar(byte b) {
            return b < ' ' || b >= '~' || b == '%';
        }

        /**
         * @param valueBytes the UTF-8 bytes
         * @param ri The reader index, pointed at the first byte that needs escaping.
         */
        private static byte[] toAsciiStringSlow(byte[] valueBytes, int ri) {
            byte[] escapedBytes = new byte[ri + (valueBytes.length - ri) * 3];
            // copy over the good bytes
            if (ri != 0) {
                System.arraycopy(valueBytes, 0, escapedBytes, 0, ri);
            }
            int wi = ri;
            for (; ri < valueBytes.length; ri++) {
                byte b = valueBytes[ri];
                // Manually implement URL encoding, per the gRPC spec.
                if (isEscapingChar(b)) {
                    escapedBytes[wi] = '%';
                    escapedBytes[wi + 1] = HEX[(b >> 4) & 0xF];
                    escapedBytes[wi + 2] = HEX[b & 0xF];
                    wi += 3;
                    continue;
                }
                escapedBytes[wi++] = b;
            }
            byte[] dest = new byte[wi];
            System.arraycopy(escapedBytes, 0, dest, 0, wi);
            return dest;
        }
    }
}
