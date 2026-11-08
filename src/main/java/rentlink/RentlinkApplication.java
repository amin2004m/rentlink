package rentlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RentlinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentlinkApplication.class, args);
    }

}
