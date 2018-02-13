/*
 * Copyright (C) 2006 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.guava.util.concurrent;

import org.wso2.siddhi.core.guava.annotations.GwtCompatible;
import org.wso2.siddhi.core.guava.base.Optional;
import org.wso2.siddhi.core.guava.collect.ImmutableCollection;
import org.wso2.siddhi.core.guava.collect.ImmutableList;
import org.wso2.siddhi.core.guava.collect.Lists;
import com.google.j2objc.annotations.WeakOuter;

import java.util.List;
import javax.annotation.Nullable;

import static org.wso2.siddhi.core.guava.base.Preconditions.checkState;

/**
 * Aggregate future that collects (stores) results of each future.
 */
@GwtCompatible
abstract class CollectionFuture<V, C> extends AggregateFuture<V, C> {

  @WeakOuter
  abstract class CollectionFutureRunningState extends RunningState {
    private List<Optional<V>> values;

    CollectionFutureRunningState(
        ImmutableCollection<? extends ListenableFuture<? extends V>> futures,
        boolean allMustSucceed) {
      super(futures, allMustSucceed, true);

      this.values = futures.isEmpty() ? ImmutableList.<Optional<V>>of()
          : Lists.<Optional<V>>newArrayListWithCapacity(futures.size());

      // Populate the results list with null initially.
      for (int i = 0; i < futures.size(); ++i) {
        values.add(null);
      }
    }

    @Override
    final void collectOneValue(boolean allMustSucceed, int index, @Nullable V returnValue) {
      List<Optional<V>> localValues = values;
  
      if (localValues != null) {
        localValues.set(index, Optional.fromNullable(returnValue));
      } else {
        // Some other future failed or has been cancelled, causing this one to
        // also be cancelled or have an exception set. This should only happen
        // if allMustSucceed is true or if the output itself has been
        // cancelled.
        checkState(allMustSucceed || isCancelled(),
            "Future was done before all dependencies completed");
      }
    }
  
    @Override
    final void handleAllCompleted() {
      List<Optional<V>> localValues = values;
      if (localValues != null) {
        set(combine(localValues));
      } else {
        checkState(isDone());
      }
    }

    @Override
    void releaseResourcesAfterFailure() {
      super.releaseResourcesAfterFailure();
      this.values = null;
    }

    abstract C combine(List<Optional<V>> values);
  }
}
