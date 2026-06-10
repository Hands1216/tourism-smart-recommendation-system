package com.tourism;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 旅游智慧推荐系统启动类
 */
@SpringBootApplication(scanBasePackages = "com.tourism")
@MapperScan("com.tourism.service.mapper")
@EnableScheduling
public class TourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourismApplication.class, args);
        System.out.println("""

                ========================================
                   旅游智慧推荐系统启动成功！
                ========================================
                """);
    }
}
