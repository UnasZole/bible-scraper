package com.github.unaszole.bible.monitor;

import java.util.*;
import java.util.function.Consumer;

public class ExecutionMonitor {
    public static final ExecutionMonitor INSTANCE = new ExecutionMonitor();

    public static class Item {
        private final String name;

        private Item(String name) {
            this.name = name;
        }

        public void start() {
            INSTANCE.start(this);
        }

        public void complete() {
            INSTANCE.complete(this);
        }
    }

    public static class Status implements Cloneable {
        public int registeredItems = 0;
        public int startedItems = 0;
        public int completedItems = 0;
        public String lastStartedItem = null;
        public Map<String, Set<String>> messages = new HashMap<>();

        @Override
        public Status clone() {
            try {
                Status clone = (Status) super.clone();
                clone.registeredItems = registeredItems;
                clone.startedItems = startedItems;
                clone.completedItems = completedItems;
                clone.lastStartedItem = lastStartedItem;
                clone.messages = Collections.unmodifiableMap(messages);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    private final Status currentStatus = new Status();
    private final Collection<Consumer<Status>> callbacks = new ArrayList<>();

    public void registerUpdateCallback(Consumer<Status> callback) {
        this.callbacks.add(callback);
    }
    private void notifyUpdates() {
        this.callbacks.forEach(c -> c.accept(currentStatus.clone()));
    }
    public Item register(String name) {
        Item item = new Item(name);
        currentStatus.registeredItems++;
        notifyUpdates();
        return item;
    }
    private void start(Item item) {
        currentStatus.startedItems++;
        currentStatus.lastStartedItem = item.name;
        notifyUpdates();
    }
    private void complete(Item item) {
        currentStatus.completedItems++;
        notifyUpdates();
    }
    public void message(String str) {
        currentStatus.messages
                .computeIfAbsent(currentStatus.lastStartedItem, k -> new HashSet<>())
                .add(str);
    }
    public void printMessages() {
        if(!currentStatus.messages.isEmpty()) {
            System.out.println("== WARNINGS ==");
        }

        currentStatus.messages.forEach((item, messages) -> {
            System.out.println("Within " + item);
            messages.forEach(m -> System.out.println("-> " + m));
        });
    }
}
