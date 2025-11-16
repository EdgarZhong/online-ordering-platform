package com.platform.ordering.controller;

import com.platform.ordering.util.KitchenEventBus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "KitchenEventsServlet", urlPatterns = { "/admin/kitchen/events" })
public class KitchenEventsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object ridObj = req.getAttribute("restaurantId");
        if (ridObj == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int restaurantId = (Integer) ridObj;
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        PrintWriter w = resp.getWriter();
        KitchenEventBus.Subscriber s = new KitchenEventBus.Subscriber(restaurantId, w);
        KitchenEventBus.get().subscribe(s);
        try {
            w.println("event: hello");
            w.println("data: ok");
            w.println();
            w.flush();
            while (true) {
                try { Thread.sleep(15000); } catch (InterruptedException ignored) {}
                w.println("event: ping");
                w.println("data: 1");
                w.println();
                w.flush();
            }
        } catch (Exception ignored) {
        } finally {
            KitchenEventBus.get().unsubscribe(s);
            try { w.close(); } catch (Exception ignored) {}
        }
    }
}