package learn.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"learn.spring"})
public class Spring101Application {

    public static void main(String[] args) {
        SpringApplication.run(Spring101Application.class, args);
    }

}
