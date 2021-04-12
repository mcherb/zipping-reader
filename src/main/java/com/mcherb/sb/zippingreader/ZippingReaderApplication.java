package com.mcherb.sb.zippingreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ZippingReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZippingReaderApplication.class, args);
    }

}
