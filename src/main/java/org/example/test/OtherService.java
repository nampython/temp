package org.example.test;

import org.example.annotations.Bean;
import org.example.annotations.PostConstruct;
import org.example.annotations.Service;

@Service
public class OtherService extends ServiceT implements ServiceI, ServiceA{

    private String A;
    private String B;

    public String C;

    public OtherService() {
    }

    public OtherService(String a, String b) {
        this.A = a;
        this.B = b;
    }

    public String getA() {
        return A;
    }

    public String getB() {
        return B;
    }

    public String getC() {
        return C;
    }

    @PostConstruct
    public void println() {
        System.out.println("hello");
    }

    @Override
    public void say() {
        super.say();
        System.out.println("Hello child");
    }

    @Override
    public void A() {

    }

    @Bean
    public void B() {

    }
}
