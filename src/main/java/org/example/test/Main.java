package org.example.test;

import org.example.annotations.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void getInfoClass(Class<?> cls) {
        System.out.println(cls.getName());
        System.out.println(cls.getSimpleName());

        Package pkg = cls.getPackage();
        System.out.println(pkg.getName());

        // Modifier
        int modifiers = cls.getModifiers();
        System.out.println(modifiers);
        boolean isPublic = Modifier.isPublic(modifiers);

        Class<?> superClass = cls.getSuperclass();
        System.out.println("Super Class: " + superClass.getSimpleName());

        // Interface
        Class<?>[] listInterface = cls.getInterfaces();
        for (Class<?> aClass : listInterface) {
            System.out.println("Interface: " + aClass.getSimpleName());
        }

        // Constructor
        Constructor<?>[] cst = cls.getConstructors();
        for (Constructor<?> constructor : cst) {
            System.out.println("Constructor " + constructor.getDeclaringClass().getSimpleName() + " has " + constructor.getParameterCount());
        }

        // Method
//        Method[] methods = cls.getMethods();
//        for (Method method : methods) {
//            System.out.println("method: " + method.getName());
//        }


        // Field
        Field[] fields = cls.getFields();
        for (Field field : fields) {
            System.out.println("Field: " + field.getName());
        }

        // Annotations
        Annotation[] annotations = cls.getAnnotations();
        for (Annotation annotation : annotations) {
            System.out.println(annotation.annotationType().getSimpleName());
        }

        // Get Specified Constructor
        try {
            Constructor<?> constructor = cls.getConstructor(String.class, String.class);
            System.out.println("Constructor with String Param: " + constructor.getDeclaringClass().getSimpleName());
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (Class<?> paramType : paramTypes) {
                System.out.println("Type:  " + paramType.getSimpleName());
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Object getObject(Class<?> cls) throws NoSuchMethodException {
        try {
            Constructor<?> constructor = cls.getConstructor(String.class, String.class);
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                System.out.println(parameterType.getSimpleName());
            }
            return cls.getConstructor(String.class, String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field[] getFields(Class<?> cls) {
        Field[] fields = null;
        try {
            fields = cls.getDeclaredFields();
            for (Field field : fields) {
                System.out.println(field.getName() + " : " + field.getType().getSimpleName());
            }
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
        return fields;
    }


    public static Method[] getMethods(Class<?> cls) {
        Method[] methods = null;
        try {
            methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                System.out.println(method.getName() + " : " + method.getReturnType());
            }
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
        return methods;
    }


    public static  void main(String[] args) throws Exception {
//        getInfoClass(c);
//        OtherService oherService =  OtherService.class.getDeclaredConstructor().newInstance();
//        OtherService otherService1 = OtherService.class.getConstructor(String.class, String.class).newInstance("String 1 ", "String 2");
//        Class<?> testServiceOne = TestServiceOne.class;
//        Annotation[] annotations =  testServiceOne.getAnnotations();
//        for (Annotation annotation : annotations) {
//            System.out.println(annotation.annotationType());
//        }

    }
}
