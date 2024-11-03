package com.mycompany.gui_proj_inz.utils;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MaxListSizeStringListModel extends AbstractListModel<String> {
    private List<String> strings;
    private int maxListLen;
    private Supplier<Void> callback;

    public MaxListSizeStringListModel(int maxListLen) {
        this.strings = new ArrayList<>();
        this.maxListLen = maxListLen;
    }
    @Override
    public int getSize() {
        return strings.size();
    }

    @Override
    public String getElementAt(int index) {
        if (index >= 0 && index < strings.size()) {
            return strings.get(index);
        } else {
            return null;
        }
    }
    public void addElement(String str) {
        if (strings.size() == maxListLen) {
            // Remove the oldest element (index 0) if list is full
            removeElement(0);
        }
        int index = strings.size();
        strings.add(str);
        System.out.println("Added element: " + str);
        fireIntervalAdded(this, index, index);  // Notifies listeners of the change
        callback.get();
    }

    public String removeElement(int index) {
        if (index >= 0 && index < strings.size()) {
            String removed = strings.remove(index);
            fireIntervalRemoved(this, index, index);  // Notifies listeners of the removal
            return removed;
        } else {
            System.out.println("Invalid index.");
            return null;
        }
    }
    public void clear() {
        int size = strings.size();
        if (size > 0) {
            strings.clear();
            fireIntervalRemoved(this, 0, size - 1);  // Notify listeners of the removal
        }
    }

    public int getMaxListLen() {
        return maxListLen;
    }

    public void setCallback(Supplier<Void> callback) {
        this.callback = callback;
    }

    public void setMaxListLen(int maxListLen) {
        this.maxListLen = maxListLen;
    }
}