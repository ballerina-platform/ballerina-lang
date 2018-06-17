/*
 * Copyright 2015, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc;

/**
 * {@link Status} in Exception form, for propagating Status information via exceptions. This is
 * semantically equivalent to {@link StatusRuntimeException}, except for usage in APIs that promote
 * checked exceptions. gRPC's stubs favor {@code StatusRuntimeException}.
 */
public class StatusException extends Exception {
  private static final long serialVersionUID = -660954903976144640L;
  private final Status status;

  /**
   * Constructs an exception with both a status and trailers.
   */
  public StatusException(Status status) {
    super(Status.formatThrowableMessage(status), status.getCause());
    this.status = status;
  }

  /**
   * Returns the status code as a {@link Status} object.
   */
  public final Status getStatus() {
    return status;
  }
}
