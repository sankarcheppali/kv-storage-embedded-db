package net.icircuit.bucketdb.models;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;

public class LinkedHashSetCollector {
    public static Collector<Object, LinkedHashSet<Object>, LinkedHashSet<Object>> collector(){
        return Collector.of(LinkedHashSet::new,
                Set::add,
                (left, right) -> { left.addAll(right); return left; });
    }
}
