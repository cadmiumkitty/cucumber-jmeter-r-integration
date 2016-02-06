package com.emorozov.datasciencelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@EnableAutoConfiguration
public class DataScienceLabDemo {

    public static final Random random = new Random();

    @RequestMapping("/users")
    public String users() throws Exception {
        Thread.sleep((long) (random.nextGaussian() * 20) + 100);
        // Comment out additional sleep statement below to see the tests passing
        if (random.nextInt(100) > 80) {
            Thread.sleep(200);
        }
        return "Return some users.";
    }

    @RequestMapping("/trades")
    public String trades() throws Exception {
        Thread.sleep((long) (random.nextGaussian() * 20) + 100);
        return "Return some trades.";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DataScienceLabDemo.class, args);
    }

}
