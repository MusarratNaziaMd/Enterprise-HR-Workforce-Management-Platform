// package com.enterprise.peopleflow;

// import io.github.cdimascio.dotenv.Dotenv;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

// import java.nio.file.Path;

// @SpringBootApplication
// public class PeopleFlowApplication {

//     public static void main(String[] args) {
//         Dotenv dotenv = Dotenv.configure()
//                 .filename(".env")
//                 .directory(Path.of("D:", "EMS").toString())
//                 .load();
//         dotenv.entries().forEach(entry ->
//                 System.setProperty(entry.getKey(), entry.getValue()));

//         SpringApplication.run(PeopleFlowApplication.class, args);
//     }
// }


package com.enterprise.peopleflow;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeopleFlowApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        System.out.println("===== ENV TEST =====");

        System.out.println("JWT_SECRET = " + System.getProperty("JWT_SECRET"));
        System.out.println("JWT_EXPIRATION_MS = " + System.getProperty("JWT_EXPIRATION_MS"));
        System.out.println("SPRING_DATASOURCE_URL = " + System.getProperty("SPRING_DATASOURCE_URL"));

        System.out.println("====================");

        SpringApplication.run(PeopleFlowApplication.class, args);
    }
}
