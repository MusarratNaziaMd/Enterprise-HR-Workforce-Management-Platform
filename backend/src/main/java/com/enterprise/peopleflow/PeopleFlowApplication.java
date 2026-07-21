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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeopleFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeopleFlowApplication.class, args);
    }
}