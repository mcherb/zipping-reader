package com.mcherb.sb.zippingreader;

import org.springframework.batch.item.*;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InMemoryReader<T> implements ItemStreamReader<List<T>> {

    private ItemReader<T> delegate;
    private boolean exhausted;

    public void setDelegate(ItemReader<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public List<T> read() throws Exception {
        if (exhausted) {
            return null;
        }

        List<T> memory = new ArrayList<>();
        T item;
        while ((item = delegate.read()) != null) {
            memory.add(item);
        }
        exhausted = true;
        return memory;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).open(executionContext);
        }
        exhausted = false;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).close();
        }
    }
}
