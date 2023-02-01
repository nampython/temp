package org.example;

import org.example.annotations.Service;
import org.example.annotations.StartUp;
import org.example.configs.Configuration;
import org.example.container.*;
import org.example.directory.Directory;
import org.example.directory.DirectoryResolverImpl;
import org.example.directory.DirectoryType;
import org.example.instantiations.InstantiationService;
import org.example.instantiations.InstantiationServiceImpl;
import org.example.instantiations.ServicesInstantiationService;
import org.example.instantiations.ServicesInstantiationServiceImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;


@Service
public class App {

    private static final DependencyContainer dependencyContainer;

    static {
        dependencyContainer = new DependencyContainerImpl();
    }

    public static void run(Class<?> startupClass) {
        run(startupClass, new Configuration());
    }

    private static void run(Class<?> startupClass, Configuration configuration) {
        Set<Class<?>> locatedClass = getLocatedClass(startupClass);
        ServicesScanningService serviceScanningService = new ServicesScanningServiceImpl(
                configuration.getAnnotations()
        );
        ServicesScanningService servicesScanningService = new ServicesScanningServiceImpl(
                configuration.getAnnotations()
        );
        InstantiationService instantiationService = new InstantiationServiceImpl();

        Set<ServiceDetails<?>> allServiceDetails = servicesScanningService.mappingClass(locatedClass);
        ServicesInstantiationService servicesInstantiationService = new ServicesInstantiationServiceImpl(
                instantiationService,
                configuration.getInstantiationConfiguration()
        );
        List<ServiceDetails<?>> allServiceDetailsInstance = servicesInstantiationService.instantiateServicesAndBeans(allServiceDetails);
        dependencyContainer.init(allServiceDetailsInstance, instantiationService);
        runStartUpMethod(startupClass);
    }

    /**
     *
     */
    @StartUp
    public void mainStartup() {
        // coding here to get all the information about service
    }

    private static void runStartUpMethod(Class<?> startupClass) {
        ServiceDetails<?> serviceDetails = dependencyContainer.getSingleService(startupClass);
        if (serviceDetails == null) {
            throw new RuntimeException("");
        }
        Method[] declaredMethods = serviceDetails.getServiceType().getDeclaredMethods();
        for (Method startUpMethod : declaredMethods) {
            if (!isStartupMethod(startUpMethod)) {
                continue;
            }
            invokeStartupMethod(startUpMethod, serviceDetails.getInstance());
        }
    }

    private static void invokeStartupMethod(Method startUpMethod, Object instance) {
        try {
            startUpMethod.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isStartupMethod(Method declaredMethod) {
        boolean isZeroStartupMethod = declaredMethod.getParameterCount() != 0;
        boolean isVoidStartupMethod = declaredMethod.getReturnType() != void.class && declaredMethod.getReturnType() != Void.class;
        boolean isAnnotationStartupMethod = !declaredMethod.isAnnotationPresent(StartUp.class);
        return !isZeroStartupMethod && !isVoidStartupMethod && !isAnnotationStartupMethod;
    }

    private static Set<Class<?>> getLocatedClass(Class<?> startupClass) {
        Directory directory = new DirectoryResolverImpl().resolveDirectory(startupClass);
        ClassLocator classLocator = getClassLocator(directory);
        return classLocator.locatedClass(directory.getDirectory());
    }

    private static ClassLocator getClassLocator(Directory directory) {
        ClassLocator classLocator = null;
        if (directory.getDirectoryType() == DirectoryType.DIRECTORY) {
            classLocator = new ClassLocatorForDirectoryImpl();
        } else {
            classLocator = new ClassLocatorForJarFile();
        }
        return classLocator;
    }
}
