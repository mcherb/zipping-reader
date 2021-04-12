package com.mcherb.sb.zippingreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.*;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ZippingReader<K, T extends Identifiable<K>, U extends Identifiable<K>, R> implements ItemStreamReader<R> {
    private ItemReader<T> delegate;
    private SearchReader<K, U> searchReader;
    private final BiFunction<T, List<U>, R> zipper;

    public void setDelegate(ItemReader<T> delegate) {
        this.delegate = delegate;
    }

    public void setSearchReader(SearchReader<K, U> searchReader) {
        this.searchReader = searchReader;
    }

    @Override
    @Nullable
    public R read() throws Exception {

        T item = delegate.read();
        if (item == null) {
            return null;
        }

        List<U> ts = searchReader.find(item.id());

        return zipper.apply(item, ts);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).open(executionContext);
        }
        searchReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).update(executionContext);
        }
        searchReader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).close();
        }
        searchReader.close();
    }
}
