package org.example.test;

import org.example.annotations.*;

@Service
public class OtherService {
    private final ServiceTestA serviceTestA;

    @Autowired
    public OtherService(ServiceTestA serviceTestA) {
        this.serviceTestA = serviceTestA;
    }


    @PostConstruct
    public void println() {
        System.out.println("hello everyone");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Destroy...");
    }

    @Bean
    public TestBean B() {
        return new TestBean();
    }
}
