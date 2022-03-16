package com.danil.chartographer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChartographerApplication {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No path argument is provided");
            return;
        }
        SpringApplication.run(ChartographerApplication.class, args);
    }

}
