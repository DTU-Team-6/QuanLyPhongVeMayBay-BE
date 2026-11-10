package com.team6.quanlyvemaybay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Cấu hình Clock chuẩn theo múi giờ Việt Nam (Asia/Ho_Chi_Minh) phục vụ truy vấn thời gian và kiểm thử.
 */
@Configuration
public class TimeConfig {

    public static final ZoneId DEFAULT_VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Bean
    public Clock systemClock() {
        return Clock.system(DEFAULT_VN_ZONE);
    }
}
