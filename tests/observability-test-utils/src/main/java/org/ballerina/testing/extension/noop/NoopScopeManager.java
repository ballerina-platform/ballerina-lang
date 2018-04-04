/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerina.testing.extension.noop;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * A no-op implementation of a ScopeManager.
 */
public interface NoopScopeManager extends ScopeManager {
    NoopScopeManager INSTANCE = new NoopScopeManagerImpl();

    /**
     * A no-op implementation of a Scope.
     */
    interface NoopScope extends Scope {
        NoopScope INSTANCE = new NoopScopeManagerImpl.NoopScopeImpl();
    }
}

class NoopScopeManagerImpl implements NoopScopeManager {
    @Override
    public Scope activate(Span span, boolean finishOnClose) {
        return NoopScope.INSTANCE;
    }

    @Override
    public Scope active() {
        return NoopScope.INSTANCE;
    }

    static class NoopScopeImpl implements NoopScopeManager.NoopScope {
        @Override
        public void close() {
        }

        @Override
        public Span span() {
            return NoopSpan.INSTANCE;
        }
    }
}
