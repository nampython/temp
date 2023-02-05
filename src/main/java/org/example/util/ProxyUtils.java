package org.example.util;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.example.container.ServiceDetails;

import java.lang.reflect.InvocationTargetException;

public class ProxyUtils {
    public static void createProxyInstance(ServiceDetails serviceDetails, Object[] constructorParams) {
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(serviceDetails.getServiceType());
        final Class<?> cls = proxyFactory.createClass();

        Object proxyInstance;
        try {
            proxyInstance = cls.getDeclaredConstructors()[0].newInstance(constructorParams);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        ((ProxyObject) proxyInstance).setHandler((self, thisMethod, proceed, args1) -> thisMethod.invoke(serviceDetails.getActualInstance(), args1));

        serviceDetails.setProxyInstance(proxyInstance);
    }
}
