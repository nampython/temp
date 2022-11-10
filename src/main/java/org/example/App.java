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
 *
 */
@Service
public class App {
    private final OtherService otherService;

    public App(OtherService otherService) {
        this.otherService = otherService;
    }

    @PostConstruct
    public void init() {
        System.out.println("hello" + this.otherService.getClass().getSimpleName());
    }

    public static void main(String[] args ) throws BeanInstantiationException {
        run(App.class);
    }

    public static void run(Class<?> startupClass) throws BeanInstantiationException {
        run(App.class, new Configuration());
    }
    public static void run(Class<?> startupClass, Configuration configuration) throws BeanInstantiationException {
        Directory directory = new DirectoryResolverImpl().resolverDirectory(startupClass);
        ClassLocator classLocator;
        if (directory.getDirectoryType() == DirectoryType.DIRECTORY) {
            classLocator = new ClassLocatorForDirectory();
        } else {
            classLocator = new ClassLocatorForJarFile();
        }

        Set<Class<?>> locatedClass = classLocator.locateClasses(directory.getDirectory());
        ServiceScanningService serviceScanningService = new ServiceScanningServiceImpl(configuration.annotations());
        ServicesInstantiationService instantiationService = new ServicesInstantiationServiceImpl(
                configuration.instantiations(),
                new ObjectInstantiationServiceImpl()
        );

        Set<ServiceDetails<?>> mappedServices = serviceScanningService.mapServices(locatedClass);
        List<ServiceDetails<?>> serviceDetails = instantiationService.instantiateServiceAndBean(mappedServices);


    }
}
