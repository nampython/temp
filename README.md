# IoC Container
Have you ever wondered how Spring core famework works. I've always been curious how it works, so I created this project. It mainly used Java Rejection and support most annotations like @Service, @Autowire, @Bean, @PostConstructor…

## Table of Contents
* [General Information](#general-information)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Getting Started](#getting-started)
* [More info](#more-info)

## General Information
//TODO


## Prerequisites
- Dealing with class: [https://www.tutorialspoint.com/java/lang/java_lang_class.htm](https://www.tutorialspoint.com/java/lang/java_lang_class.htm)
- Work with files: [https://docs.oracle.com/javase/7/docs/api/java/io/File.html](https://docs.oracle.com/javase/7/docs/api/java/io/File.html)
- Java reflection (Method, Annotation, Constructor…): [https://www.oracle.com/technical-resources/articles/java/javareflection.html](https://www.oracle.com/technical-resources/articles/java/javareflection.html)
- Basic Spring Core framework and know several annotations like @Bean, @Service, @Postconstruct,  @AfterDestroy, @Autowired…:[https://www.baeldung.com/spring-core-annotations](https://www.baeldung.com/spring-core-annotations)
- Design Pattern: Builder


## Installation
You can run this project by 2 ways:
- The simple way is to run command: **mvn clean install**. Your dependency is in m2\repository\
----
	mvn clean install
----
 In the file pom of your project and add in tag <dependency></dependency>

```xml
<dependency>
 <groupId>org.example</groupId>
 <artifactId>ioc</artifactId>
 <version>1.0</version>
</dependency>
```
- The other way is to run **mvn package** and get the generated jar file. Add this dependency in your project
----
	mvn package
----
```xml
<dependency>
 <groupId>org.example</groupId>
 <artifactId>ioc</artifactId>
 <version>1.0</version>
 <scope>system</scope>
 <systemPath>${basedir}/lib/yournamejar.jar</sysyemPath>
</dependency>
```
## Getting Started
* In your main method call InitApp.run(YourStartupClass.class)
* Annotate your startup class with @Service
* Create a void method and annotate it with @StartUp

Here is a quick teaser of a complete project:

```java
import org.example.annotations.Service;
import org.example.annotations.StartUp;
import org.example.container.DependencyContainer;
import org.example.container.DependencyContainerImpl;

@Service
public class Main
{
    private static final DependencyContainer dependencyContainer;
    static {
        dependencyContainer = new DependencyContainerImpl();
    }

    public static void main( String[] args ) {
        InitApp.run(Main.class);
    }

    @StartUp
    public void startUpMethod() {
        //coding here to get all the information about service classes from dependencyContainer variable
    }
}
```
