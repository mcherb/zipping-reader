package com.mcherb.sb.zippingreader;

import lombok.SneakyThrows;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.List.copyOf;

public class InMemoryRemovalSearchReader<K, T extends Identifiable<K>> extends InMemoryReader<T> implements SearchReader<K, T> {

    private List<T> memory;

    @Override
    public List<T> find(K key) {
        List<T> toRemove = memory
                .stream()
                .filter(e -> e.id().equals(key))
                .collect(Collectors.toList());
        memory.removeAll(toRemove);
        return toRemove;
    }

    @Override
    @Nullable
    public List<T> read() {
        if (memory.isEmpty()) {
            return null;
        }

        List<T> copy = copyOf(memory);
        memory = emptyList();
        return copy;
    }

    @Override
    @SneakyThrows
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);
        if (memory == null) {
            memory = super.read();
        }
    }
}
