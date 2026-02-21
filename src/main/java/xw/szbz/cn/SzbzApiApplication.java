package xw.szbz.cn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SzbzApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SzbzApiApplication.class, args);
    }

}
