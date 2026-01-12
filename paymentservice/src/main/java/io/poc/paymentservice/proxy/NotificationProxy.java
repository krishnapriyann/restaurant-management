package io.poc.paymentservice.proxy;

import io.poc.paymentservice.model.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("NOTIFICATIONSERVICE")
public interface NotificationProxy {

    @PostMapping(path = "api/v1/notification-service/notify")
    void notifyUser(@RequestBody OrderDto order);
}
