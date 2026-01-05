package io.poc.paymentservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("NOTIFICATIONSERVICE")
public interface NotificationProxy {

    @PostMapping(path = "api/v1/notification-service/notify")
//    void notifyUser(@RequestParam String email, @RequestParam Long orderId);
    void notifyUser(@RequestParam String email);
}
