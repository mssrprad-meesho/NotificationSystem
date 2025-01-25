//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.sleuth.Span;
//import org.springframework.cloud.sleuth.Tracer;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class SleuthController {
//
//    private final Logger logger = LoggerFactory.getLogger(SleuthController.class);
//
//    @Autowired
//    private Tracer tracer;
//
//    @GetMapping("/hello")
//    public String hello() {
//        Span currentSpan = tracer.currentSpan();
//        String correlationId = currentSpan.context().traceId();
//        logger.info("Handling hello request with trace ID: {}", correlationId);
//        return "Hello from Sleuth Demo!";
//    }
//}