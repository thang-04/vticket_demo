/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vticket.vticket.utils;

/**
 *
 * @author tudd00
 */
import java.util.AbstractList;
import java.util.List;

public class Partition<T> extends AbstractList<List<T>> {

    final List<T> list;
    final int size;

    Partition(List<T> list, int size) {
        this.list = list;
        this.size = size;
    }

    @Override
    public List<T> get(int index) {
        int listSize = size();
        if (listSize < 0) {
            throw new IllegalArgumentException("negative size: " + listSize);
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "index " + index + " must not be negative");
        }
        if (index >= listSize) {
            throw new IndexOutOfBoundsException(
                    "index " + index + " must be less than size " + listSize);
        }
        int start = index * size;
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    @Override
    public int size() {
        return (list.size() + size - 1) / size;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
