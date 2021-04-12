package com.mcherb.sb.zippingreader;

import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

public interface SearchReader<K, T extends Identifiable<K>> extends ItemStreamReader<List<T>> {

    List<T> find(K key);
}
