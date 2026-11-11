package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public final class Notifier {

    private Notifier() {
    }

    public static void success(String message) {
        show(message, NotificationVariant.LUMO_SUCCESS, 3000);
    }

    public static void error(String message) {
        show(message, NotificationVariant.LUMO_ERROR, 5000);
    }

    private static void show(String message, NotificationVariant variant, int duration) {
        Notification n = Notification.show(message, duration, Notification.Position.TOP_CENTER);
        n.addThemeVariants(variant);
    }
}
