package net.icircuit.bucketdb.models;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
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
    public static <T>
    Collector<T, ?, Set<T>> toTreeSet() {
        return Collector.of(CustomeTreeSet::new,
                Set::add,
                (left, right) -> { left.addAll(right); return left; });
    }
}
class CustomeTreeSet extends TreeSet{
    @Override
    public boolean add(Object o) {
        if(!super.add(o)) {
            super.remove(o);
            return super.add(o);
        }
        return true;
    }
}