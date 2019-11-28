package net.icircuit.bucketdb.models;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CustomeCollectors {
    public static <T>
    Collector<T, ?, Set<T>> toLinkedHashSet() {
        return Collector.of(LinkedHashSet::new,
                Set::add,
                (left, right) -> { left.addAll(right); return left; });
    }
}
