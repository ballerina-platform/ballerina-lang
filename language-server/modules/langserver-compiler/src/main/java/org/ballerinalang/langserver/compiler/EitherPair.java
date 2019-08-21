/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler;

/**
 * Represents either type maps union types.
 *
 * Gracefully borrowed from eclipse/lsp4j project(https://github.com/eclipse/lsp4j).
 *
 * @param <L> left side value
 * @param <R> right side value
 */
public class EitherPair<L, R> {
    private final L left;
    private final R right;

    public static <L, R> EitherPair<L, R> forLeft(L left) {
        return new EitherPair(left, (Object) null);
    }

    public static <L, R> EitherPair<L, R> forRight(R right) {
        return new EitherPair((Object) null, right);
    }

    protected EitherPair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns left value.
     *
     * @return left value
     */
    public L getLeft() {
        return this.left;
    }

    /**
     * Returns right value.
     *
     * @return right value
     */
    public R getRight() {
        return this.right;
    }

    /**
     * Returns occupied value.
     *
     * @return {@link Object}  value
     */
    public Object get() {
        if (this.left != null) {
            return this.left;
        } else {
            return this.right;
        }
    }

    /**
     * Returns True when Left is occupied.
     *
     * @return True when Left is occupied.
     */
    public boolean isLeft() {
        return this.left != null;
    }

    /**
     * Returns True when Right is occupied.
     *
     * @return True when Right is occupied.
     */
    public boolean isRight() {
        return this.right != null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof EitherPair)) {
            return false;
        } else {
            EitherPair<?, ?> other = (EitherPair) obj;
            return this.left == other.left && this.right == other.right ||
                    this.left != null && other.left != null && this.left.equals(other.left) ||
                    this.right != null && other.right != null && this.right.equals(other.right);
        }
    }

    public int hashCode() {
        if (this.left != null) {
            return this.left.hashCode();
        } else {
            return this.right != null ? this.right.hashCode() : 0;
        }
    }

    public String toString() {
        StringBuilder builder = (new StringBuilder("EitherPair [")).append(System.lineSeparator());
        builder.append("  left = ").append(this.left).append(System.lineSeparator());
        builder.append("  right = ").append(this.right).append(System.lineSeparator());
        return builder.append("]").toString();
    }
}
