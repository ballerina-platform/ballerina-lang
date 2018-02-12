package org.ballerinalang.net.grpc;

import io.grpc.Context;
import io.grpc.Metadata;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * This class will preserve an instance the current connection context as thread local variable.
 */
public class MessageContext {
    public static final Context.Key<MessageContext> DATA_KEY = Context.key("MessageContext");
    private Metadata contextMetadata;
//    private Object freezeKey = null;

    // stores the current MessageContext local to the running thread.
//    private static ThreadLocal<MessageContext> currentContext = ThreadLocal.withInitial(MessageContext::new);

    /**
     * Attaches an empty message context to the provided gRPC {@code Context}.
     *
     * @throws IllegalStateException  if an ambient context has already been attached to the
     * provided gRPC {@code Context}.
     */
    public static Context initialize(Context context) {
        checkNotNull(context, "context");
        checkState(DATA_KEY.get(context) == null,
                "MessageContext has already been created in the scope of the current context");
        return context.withValue(DATA_KEY, new MessageContext());
    }

    /**
     * Returns the message context attached to the current gRPC {@code Context}.
     *
     * @throws  IllegalStateException  if no ambient context is attached to the current gRPC {@code Context}.
     */
    public static MessageContext current() {
        checkState(DATA_KEY.get() != null,
                "MessageContext has not yet been created in the scope of the current context");
        return DATA_KEY.get();
    }

    /**
     * @return true if an {@code MessageContext} is attached to the current gRPC context.
     */
    public static boolean isPresent() {
        return DATA_KEY.get() != null;
    }

    public MessageContext() {
        this.contextMetadata = new Metadata();
    }

    /**
     * Copy constructor.
     */
    MessageContext(MessageContext other) {
        this();
        this.contextMetadata.merge(other.contextMetadata);
    }

    /**
     * Similar to {@link #initialize(Context)}, {@code fork()} attaches a shallow clone of this {@code MessageContext}
     * to a provided gRPC {@code Context}. Use {@code fork()} when you want create a temporary context scope.
     *
     * @param context
     * @return
     */
    public Context fork(Context context) {
        return context.withValue(DATA_KEY, new MessageContext(this));
    }

    //    public boolean isFrozen() {
//        return freezeKey != null;
//    }

//    private void checkFreeze() {
//        checkState(freezeKey == null, "MessageContext cannot be modified while frozen");
//    }

    /**
     * Returns true if a value is defined for the given key.
     *
     * <p>This is done by linear search, so if it is followed by {@link #get} or {@link #getAll},
     * prefer calling them directly and checking the return value against {@code null}.
     */
    public boolean containsKey(Metadata.Key<?> key) {
        return contextMetadata.containsKey(key);
    }

    /**
     * Remove all values for the given key without returning them. This is a minor performance
     * optimization if you do not need the previous values.
     *
     * @throws IllegalStateException  if the AmbientContext is frozen
     */
    public <T> void discardAll(Metadata.Key<T> key) {
//        checkFreeze();
        contextMetadata.discardAll(key);
    }

    /**
     * Returns the last message context entry added with the name 'name' parsed as T.
     *
     * @return the parsed metadata entry or null if there are none.
     */
    public <T> T get(Metadata.Key<T> key) {
        return contextMetadata.get(key);
    }

    /**
     * Returns all the ambient context entries named 'name', in the order they were received, parsed as T, or
     * null if there are none. The iterator is not guaranteed to be "live." It may or may not be
     * accurate if the ambient context is mutated.
     */
    public <T> Iterable<T> getAll(final Metadata.Key<T> key) {
        return contextMetadata.getAll(key);
    }

    /**
     * Returns set of all keys in store.
     *
     * @return unmodifiable Set of keys
     */
    public Set<String> keys() {
        return contextMetadata.keys();
    }

    /**
     * Adds the {@code key, value} pair. If {@code key} already has values, {@code value} is added to
     * the end. Duplicate values for the same key are permitted.
     *
     * @throws NullPointerException if key or value is null
     * @throws IllegalStateException  if the AmbientContext is frozen
     */
    public <T> void put(Metadata.Key<T> key, T value) {
//        checkFreeze();
        contextMetadata.put(key, value);
    }

    /**
     * Removes the first occurrence of {@code value} for {@code key}.
     *
     * @param key key for value
     * @param value value
     * @return {@code true} if {@code value} removed; {@code false} if {@code value} was not present
     *
     * @throws NullPointerException if {@code key} or {@code value} is null
     * @throws IllegalStateException  if the AmbientContext is frozen
     */
    public <T> boolean remove(Metadata.Key<T> key, T value) {
//        checkFreeze();
        return contextMetadata.remove(key, value);
    }

    /**
     * Remove all values for the given key. If there were no values, {@code null} is returned.
     *
     * @throws IllegalStateException  if the AmbientContext is frozen
     */
    public <T> Iterable<T> removeAll(Metadata.Key<T> key) {
//        checkFreeze();
        return contextMetadata.removeAll(key);
    }

//    @Override
//    public String toString() {
//        return (isFrozen() ? "[FROZEN] " : "[THAWED] ") + contextMetadata.toString();
//    }

/*    public static MessageContext getThreadLocalMessageContext() {
        return currentContext.get();
    }

    private Map<String, Context.Key> contextKeyMap = new HashMap<>();

    Object getHeaderValue(String headerName) {
        Context.Key contextKey = contextKeyMap.get(headerName);
        return contextKey != null ? contextKey.get() : null;
    }

    public void setHeaderContext(String key, Context.Key contextKey) {;
        contextKeyMap.put(key, contextKey);
    }

    Set<String> getHeaderKeys() {
        return contextKeyMap.keySet();
    }

    void clearHeaderContext() {
        contextKeyMap.clear();
    }*/
}
