package com.zjw.system;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author 朱俊伟
 * @since 2024/06/23 17:42
 */
@Component
@Data
public class ProcesInfo {

    public static long startTime;
    public static int totalVideoNum;

    /**
     * 获取系统运行时间
     *
     * @return 运行时间，格式为"00:00"
     */
    public static String getUptime() {
        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - startTime;

        Duration duration = Duration.ofMillis(uptimeMillis);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

}
