/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.selector.attribute.aggergator;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;

public class StddevAttributeAggregator extends AttributeAggregator {
    private StddevAttributeAggregator stddevOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Stddev aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }

        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();

        switch (type) {
            case INT:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorInt();
                break;
            case LONG:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorLong();
                break;
            case FLOAT:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorFloat();
                break;
            case DOUBLE:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Stddev not supported for " + type);
        }
    }

    @Override
    public Attribute.Type getReturnType() { return stddevOutputAttributeAggregator.getReturnType(); }

    @Override
    public Object processAdd(Object data) { return stddevOutputAttributeAggregator.processAdd(data); }

    @Override
    public Object processAdd(Object[] data) {
        return new IllegalStateException("Stddev cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) { return stddevOutputAttributeAggregator.processRemove(data); }

    @Override
    public Object processRemove(Object[] data) {
        return new IllegalStateException("Stddev cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() { return stddevOutputAttributeAggregator.reset(); }

    @Override
    public Object[] currentState() { return stddevOutputAttributeAggregator.currentState(); }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void restoreState(Object[] state) { stddevOutputAttributeAggregator.restoreState(state); }

    private class StddevAttributeAggregatorDouble extends StddevAttributeAggregator {
        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double M, oldM, S;
        private int n = 0;

        @Override
        public Attribute.Type getReturnType() { return type; }

        @Override
        public Object processAdd(Object data) {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            n++;
            double value = (Double) data;

            if (n == 1) {
                M = oldM = value;
                S = 0.0;
            } else {
                oldM = M;
                M = oldM + (value - oldM)/n;
                S += (value - oldM)*(value - M);
            }

            if (n < 2) {
                return 0.0;
            }
            return Math.sqrt(S/n);
        }

        @Override
        public Object processRemove(Object data) {
            n--;
            double value = (Double) data;

            if (n == 0) {
                M = 0;
                S = 0;
            } else {
                oldM = M;
                M = (oldM*(n + 1) - value)/n;
                S -= (value - oldM)*(value - M);
            }

            if (n < 2) {
                return 0.0;
            }
            return Math.sqrt(S/n);
        }

        @Override
        public Object reset() {
            M = oldM = 0.0;
            S = 0.0;
            return 0;
        }

        @Override
        public Object[] currentState() { return new Object[] {M, oldM, S, n}; }

        @Override
        public void restoreState(Object[] state) {
            M = (Double) state[0];
            oldM = (Double) state[1];
            S = (Double) state[2];
            n = (Integer) state[3];
        }
    }

    private class StddevAttributeAggregatorFloat extends StddevAttributeAggregator {
	    private final Attribute.Type type = Attribute.Type.FLOAT;
	    private double M, oldM, S;
	    private int n = 0;

	    @Override
	    public Attribute.Type getReturnType() { return type; }

	    @Override
	    public Object processAdd(Object data) {
		    // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
		    n++;
		    double value = (Float) data;

		    if (n == 1) {
			    M = oldM = value;
			    S = 0.0;
		    } else {
			    oldM = M;
			    M = oldM + (value - oldM)/n;
			    S += (value - oldM)*(value - M);
		    }

		    if (n < 2) {
			    return 0.0;
		    }
		    return Math.sqrt(S/n);
	    }

	    @Override
	    public Object processRemove(Object data) {
		    n--;
		    double value = (Float) data;

		    if (n == 0) {
			    M = 0;
			    S = 0;
		    } else {
			    oldM = M;
			    M = (oldM*(n + 1) - value)/n;
			    S -= (value - oldM)*(value - M);
		    }

		    if (n < 2) {
			    return 0.0;
		    }
		    return Math.sqrt(S/n);
	    }

	    @Override
	    public Object reset() {
		    M = oldM = 0.0;
		    S = 0.0;
		    return 0;
	    }

	    @Override
	    public Object[] currentState() { return new Object[] {M, oldM, S, n}; }

	    @Override
	    public void restoreState(Object[] state) {
		    M = (Double) state[0];
		    oldM = (Double) state[1];
		    S = (Double) state[2];
		    n = (Integer) state[3];
	    }
    }

	private class StddevAttributeAggregatorLong extends StddevAttributeAggregator {
		private final Attribute.Type type = Attribute.Type.LONG;
		private double M, oldM, S;
		private int n = 0;

		@Override
		public Attribute.Type getReturnType() { return type; }

		@Override
		public Object processAdd(Object data) {
			// See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
			n++;
			double value = (Long) data;

			if (n == 1) {
				M = oldM = value;
				S = 0.0;
			} else {
				oldM = M;
				M = oldM + (value - oldM)/n;
				S += (value - oldM)*(value - M);
			}

			if (n < 2) {
				return 0.0;
			}
			return Math.sqrt(S/n);
		}

		@Override
		public Object processRemove(Object data) {
			n--;
			double value = (Long) data;

			if (n == 0) {
				M = 0;
				S = 0;
			} else {
				oldM = M;
				M = (oldM*(n + 1) - value)/n;
				S -= (value - oldM)*(value - M);
			}

			if (n < 2) {
				return 0.0;
			}
			return Math.sqrt(S/n);
		}

		@Override
		public Object reset() {
			M = oldM = 0.0;
			S = 0.0;
			return 0;
		}

		@Override
		public Object[] currentState() { return new Object[] {M, oldM, S, n}; }

		@Override
		public void restoreState(Object[] state) {
			M = (Double) state[0];
			oldM = (Double) state[1];
			S = (Double) state[2];
			n = (Integer) state[3];
		}
	}

    private class StddevAttributeAggregatorInt extends StddevAttributeAggregator {
	    private final Attribute.Type type = Attribute.Type.INT;
	    private double M, oldM, S;
	    private int n = 0;

	    @Override
	    public Attribute.Type getReturnType() { return type; }

	    @Override
	    public Object processAdd(Object data) {
		    // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
		    n++;
		    double value = (Integer) data;

		    if (n == 1) {
			    M = oldM = value;
			    S = 0.0;
		    } else {
			    oldM = M;
			    M = oldM + (value - oldM)/n;
			    S += (value - oldM)*(value - M);
		    }

		    if (n < 2) {
			    return 0.0;
		    }
		    return Math.sqrt(S/n);
	    }

	    @Override
	    public Object processRemove(Object data) {
		    n--;
		    double value = (Integer) data;

		    if (n == 0) {
			    M = 0;
			    S = 0;
		    } else {
			    oldM = M;
			    M = (oldM*(n + 1) - value)/n;
			    S -= (value - oldM)*(value - M);
		    }

		    if (n < 2) {
			    return 0.0;
		    }
		    return Math.sqrt(S/n);
	    }

	    @Override
	    public Object reset() {
		    M = oldM = 0.0;
		    S = 0.0;
		    return 0;
	    }

	    @Override
	    public Object[] currentState() { return new Object[] {M, oldM, S, n}; }

	    @Override
	    public void restoreState(Object[] state) {
		    M = (Double) state[0];
		    oldM = (Double) state[1];
		    S = (Double) state[2];
		    n = (Integer) state[3];
	    }
    }
}
