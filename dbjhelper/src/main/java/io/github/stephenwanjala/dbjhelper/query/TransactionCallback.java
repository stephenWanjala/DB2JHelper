package io.github.stephenwanjala.dbjhelper.query;

/**
 * Callback interface for transaction operations.
 */
@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(QueryExecutor executor) throws Exception;
}
