package com.minsang.study.batch;

import com.minsang.study.common.HelloMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            System.out.println("==========================================");
            System.out.println("[module-batch] 배치 작업 시작");
            System.out.println("==========================================");

            // 인자가 있으면 사용, 없으면 기본값
            String name = args.length > 0 ? args[0] : "batch-user";

            System.out.println(HelloMessage.greet(name));
            System.out.println(HelloMessage.bye(name));

            System.out.println("==========================================");
            System.out.println("[module-batch] 배치 작업 종료");
            System.out.println("==========================================");
        };
    }
}
