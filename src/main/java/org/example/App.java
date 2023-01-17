package org.example;

import org.example.annotations.PostConstruct;
import org.example.annotations.Service;
import org.example.configs.Configuration;
import org.example.enums.DirectoryType;
import org.example.exceptions.BeanInstantiationException;
import org.example.models.Directory;
import org.example.models.ServiceDetails;
import org.example.services.*;
import org.example.test.OtherService;

import java.util.List;
import java.util.Set;

/**
 * Hello world!
 */
public class App {

    public App() {
    }

    public static void main(String[] args) throws BeanInstantiationException {
        run(App.class);
    }

    public static void run(Class<?> startupClass) throws BeanInstantiationException {
        run(App.class, new Configuration());
    }

    public static void run(Class<?> startupClass, Configuration configuration) throws BeanInstantiationException {
        Directory directory = new DirectoryResolverImpl().resolverDirectory(startupClass);
        ServicesInstantiationService servicesInstantiationService = new ServicesInstantiationServiceImpl(
                configuration.instantiations(),
                new ObjectInstantiationServiceImpl());

        ClassLocator classLocator;
        if (directory.getDirectoryType() == DirectoryType.DIRECTORY) {
            classLocator = new ClassLocatorForDirectory();
        } else {
            classLocator = new ClassLocatorForJarFile();
        }
        Set<Class<?>> locatedClass = classLocator.locateClasses(directory.getDirectory());

        ServiceScanningService serviceScanningService = new ServiceScanningServiceImpl(configuration.annotations());
        Set<ServiceDetails<?>> serviceDetails = serviceScanningService.mapServices(locatedClass);

        List<ServiceDetails<?>> serviceDetails1 = servicesInstantiationService.instantiateServiceAndBean(serviceDetails);
    }
}
