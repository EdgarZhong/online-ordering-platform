package com.platform.ordering.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitchenEventBus {
    public static class Subscriber {
        public final int restaurantId;
        public final PrintWriter writer;
        public Subscriber(int restaurantId, PrintWriter writer) { this.restaurantId = restaurantId; this.writer = writer; }
    }

    private static final KitchenEventBus INSTANCE = new KitchenEventBus();
    public static KitchenEventBus get() { return INSTANCE; }

    private final Map<Integer, List<Subscriber>> subs = Collections.synchronizedMap(new HashMap<>());

    public void subscribe(Subscriber s) {
        synchronized (subs) {
            List<Subscriber> list = subs.get(s.restaurantId);
            if (list == null) { list = new ArrayList<>(); subs.put(s.restaurantId, list); }
            list.add(s);
        }
    }

    public void unsubscribe(Subscriber s) {
        synchronized (subs) {
            List<Subscriber> list = subs.get(s.restaurantId);
            if (list != null) {
                list.remove(s);
                if (list.isEmpty()) subs.remove(s.restaurantId);
            }
        }
    }

    public void publishNewOrder(int restaurantId, int orderId) {
        List<Subscriber> list;
        synchronized (subs) { list = subs.get(restaurantId) == null ? null : new ArrayList<>(subs.get(restaurantId)); }
        if (list == null || list.isEmpty()) return;
        for (Subscriber s : list) {
            try {
                s.writer.println("event: new_order");
                s.writer.println("data: {\"orderId\":" + orderId + "}");
                s.writer.println();
                s.writer.flush();
            } catch (Exception ignored) {}
        }
    }

    public void publishOrderUpdated(int restaurantId, int orderId, String status) {
        List<Subscriber> list;
        synchronized (subs) { list = subs.get(restaurantId) == null ? null : new ArrayList<>(subs.get(restaurantId)); }
        if (list == null || list.isEmpty()) return;
        for (Subscriber s : list) {
            try {
                s.writer.println("event: order_updated");
                s.writer.println("data: {\"orderId\":" + orderId + ",\"status\":\"" + (status == null ? "" : status) + "\"}");
                s.writer.println();
                s.writer.flush();
            } catch (Exception ignored) {}
        }
    }
}