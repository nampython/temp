package org.example;

import org.example.annotations.Service;
import org.example.annotations.StartUp;
import org.example.configs.Configuration;
import org.example.container.*;
import org.example.directory.Directory;
import org.example.directory.DirectoryResolver;
import org.example.directory.DirectoryResolverImpl;
import org.example.directory.DirectoryType;
import org.example.instantiations.ServicesInstantiationService;
import org.example.instantiations.ServicesInstantiationServiceImpl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Application starting point.
 * <p>
 * Contains multiple starting point methods.
 * Holds an instance of Dependency Container.
 */
@Service
public class InitApp {

    public static void main(String[] args) {
        DependencyContainerV2 run = run(InitApp.class);
        Collection<ServiceDetails> allServiceDetails = run.getAllServices();
        allServiceDetails.forEach(System.out::println);
    }
    public static DependencyContainerV2 run(Class<?> startupClass) {
        return run(startupClass, new Configuration());
    }

    /**
     * This method calls executes when all services are loaded.
     * <p>
     * Looks for instantiated service from the given type.
     * <p>
     * If instance is found, looks for void method with 0 params
     * and with with @StartUp annotation and executes it.
     *
     * @param startupClass any class from the client side.
     */
    private static DependencyContainerV2 run(Class<?> startupClass, Configuration configuration) {
        final DependencyContainerV2 dependencyContainer = run(new File[]{
                new File(new DirectoryResolverImpl().resolveDirectory(startupClass).getDirectory()),
        }, configuration);

        runStartUpMethod(startupClass, dependencyContainer);

        return dependencyContainer;
    }

    public static DependencyContainerV2 run(File[] startupDirectories, Configuration configuration) {
        final ServicesScanningService scanningService = new ServicesScanningServiceImpl(configuration.scanning());
        final ServicesInstantiationService instantiationService = new ServicesInstantiationServiceImpl(
                new DependencyResolveServiceImpl(configuration.getInstantiationConfiguration())
        );

        final Set<Class<?>> locatedClasses = new HashSet<>();
        final List<ServiceDetails> serviceDetails = new ArrayList<>();

        final Runnable runnable = () -> {
            locatedClasses.addAll(locateClasses(startupDirectories));
            final Set<ServiceDetails> mappedServices = new HashSet<>(scanningService.mappingClass(locatedClasses));
            serviceDetails.addAll(new ArrayList<>(instantiationService.instantiateServicesAndBeans(mappedServices)));
        };

        if (configuration.general().isRunInNewThread()) {
            final Thread runner = new Thread(runnable);

            runner.setContextClassLoader(configuration.scanning().getClassLoader());
            runner.start();
            try {
                runner.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(configuration.scanning().getClassLoader());
                runnable.run();
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        return new DependencyContainerCached(locatedClasses, serviceDetails);

    }
//        final Set<Class<?>> locatedClass = getLocatedClass(startupClass);
//        final ServicesScanningService servicesScanningService = new ServicesScanningServiceImpl(
//                configuration.scanning()
//        );
//        final InstantiationService instantiationService = new InstantiationServiceImpl();
//
//        final Set<ServiceDetails> allServiceDetails = servicesScanningService.mappingClass(locatedClass);
//        final ServicesInstantiationService servicesInstantiationService = new ServicesInstantiationServiceImpl(
//                instantiationService,
//                configuration.getInstantiationConfiguration()
//        );
//        List<ServiceDetails> allServiceDetailsInstance = servicesInstantiationService.instantiateServicesAndBeans(allServiceDetails);
//        dependencyContainer.init(locatedClass, allServiceDetailsInstance, instantiationService);
//        runStartUpMethod(startupClass);
//    }

//    private static Set<Class<?>> locateClasses(File[] startupDirectories, Configuration configuration) {
//        final Set<Class<?>> locatedClasses = new HashSet<>();
//        final DirectoryResolver directoryResolver = new DirectoryResolverImpl();
//
//        for (File startupDirectory : startupDirectories) {
//            final Directory directory = directoryResolver.resolveDirectory(startupDirectory);
//
//            ClassLocator classLocator = new ClassLocatorForDirectoryImpl(configuration);
//            if (directory.getDirectoryType() == DirectoryType.JAR_FILE) {
//                classLocator = new ClassLocatorForJarFile();
//            }
//
//            locatedClasses.addAll(classLocator.locatedClass(directory.getDirectory()));
//        }
//
//        return locatedClasses;
//    }

    private static Set<Class<?>> locateClasses(File[] startupDirectories) {
        final Set<Class<?>> locatedClasses = new HashSet<>();
        final DirectoryResolver directoryResolver = new DirectoryResolverImpl();

        for (File startupDirectory : startupDirectories) {
            final Directory directory = directoryResolver.resolveDirectory(startupDirectory);

            ClassLocator classLocator = new ClassLocatorForDirectoryImpl();
            if (directory.getDirectoryType() == DirectoryType.JAR_FILE) {
                classLocator = new ClassLocatorForJarFile();
            }

            locatedClasses.addAll(classLocator.locatedClass(directory.getDirectory()));
        }

        return locatedClasses;
    }
    /**
     *
     */
    @StartUp
    public void mainStartup() {

    }

//    private static void runStartUpMethod(Class<?> startupClass, DependencyContainer dependencyContainer) {
//        ServiceDetails serviceDetails = dependencyContainer.getSingleService(startupClass);
//        if (serviceDetails == null) {
//            return;
//        }
//        Method[] declaredMethods = serviceDetails.getServiceType().getDeclaredMethods();
//        for (Method startUpMethod : declaredMethods) {
//            if (!isStartupMethod(startUpMethod)) {
//                continue;
//            }
//            invokeStartupMethod(startUpMethod, serviceDetails.getActualInstance());
//        }
//    }

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



//    private static Set<Class<?>> getLocatedClass(Class<?> startupClass) {
//        Directory directory = new DirectoryResolverImpl().resolveDirectory(startupClass);
//        ClassLocator classLocator = getClassLocator(directory);
//        return classLocator.locatedClass(directory.getDirectory());
//    }
//
//    private static ClassLocator getClassLocator(Directory directory) {
//        ClassLocator classLocator = null;
//        if (directory.getDirectoryType() == DirectoryType.DIRECTORY) {
//            classLocator = new ClassLocatorForDirectoryImpl();
//        } else {
//            classLocator = new ClassLocatorForJarFile();
//        }
//        return classLocator;
//    }
    /**
     * This method calls executes when all services are loaded.
     * <p>
     * Looks for instantiated service from the given type.
     * <p>
     * If instance is found, looks for void method with 0 params
     * and with with @StartUp annotation and executes it.
     *
     * @param startupClass any class from the client side.
     */
    private static void runStartUpMethod(Class<?> startupClass, DependencyContainerV2 dependencyContainer) {
        final ServiceDetails serviceDetails = dependencyContainer.getServiceDetails(startupClass, null);

        if (serviceDetails == null) {
            return;
        }

        for (Method declaredMethod : serviceDetails.getServiceType().getDeclaredMethods()) {
            if ((declaredMethod.getReturnType() != void.class &&
                    declaredMethod.getReturnType() != Void.class)
                    || !declaredMethod.isAnnotationPresent(StartUp.class)) {
                continue;
            }

            declaredMethod.setAccessible(true);
            final Object[] params = Arrays.stream(declaredMethod.getParameterTypes())
                    .map(dependencyContainer::getService)
                    .toArray(Object[]::new);

            try {
                declaredMethod.invoke(serviceDetails.getActualInstance(), params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            return;
        }
    }
}
