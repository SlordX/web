# ListControllersServlet

## Description
The `ListControllersServlet` scans for controllers with the `@WebServlet` and `@GET` annotation and lists them on a web page.
If there is a `@GET` annotation display the URL with the name of the controller

## Deployment et dossier necessaire
- Compile and deploy the servlet on a Java web server.
- Configure the `web.xml` with the correct package name.
- Start the server and navigate to the servlet's URL.

1-Configurer le Web.xml tels que celle ci pour mettre le chemin du dossier a scanner:
    <?xml version="1.0" encoding="UTF-8"?>
    <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
            version="4.0">

        <!-- Specify the package to scan for controllers -->
        <context-param>
            <param-name>controllerPackage</param-name>
            <param-value>com.controller</param-value> <!-- Path to the Controller Package -->
        </context-param>
            
        <servlet>
            <servlet-name>FrontControllerServlet</servlet-name>
            <servlet-class>com.example.FrontControllerServlet</servlet-class> <!-- Path to the Servlet Package -->
        </servlet>

        <servlet-mapping>
            <servlet-name>FrontControllerServlet</servlet-name>
            <url-pattern>/</url-pattern> <!-- Lien Pour Essayer le Servlet -->
        </servlet-mapping>   
    </web-app>

2-Configurer les controller a scanner on utilison les annotation comme cela:
Controller avec Annotation:

    package com.controller;

    import java.io.IOException;
    import jakarta.servlet.*;
    import jakarta.servlet.http.*;
    import jakarta.servlet.annotation.*;

    @WebServlet(name = "AnnotatedController", urlPatterns = {"/annotatedController"})
    public class AnnotatedController extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // Default implementation for doGet
            response.getWriter().println("GET request handled by AnnotatedController");
        }

        @GET("/custom")
        protected void customGetMethod(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.getWriter().println("Custom GET request handled by customGetMethod");
        }
    }


Controller sans Annotation:
    package com.controller;

    import java.io.IOException;

    // Controller with @WebServlet annotation
    import jakarta.servlet.*;
    import jakarta.servlet.http.*;
    import jakarta.servlet.annotation.*;

    // Controller sans @WebServlet annotation
    public class NonAnnotatedController extends HttpServlet {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // Implementation here
        }
    }

3-Crée le GET et Mapping class comme cela 
# GET
    package com.controller;

    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD) // Cette annotation est applicable uniquement aux méthodes
    public @interface GET {
        String value(); // L'URL à laquelle la méthode doit répondre
    }

# Mapping
    package com.controller;

public class Mapping {
    private String className;
    private String methodName;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    // Getters and setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
}


    

## Usage
4-Compilé le Servlet et Deployer
5-Acceder aux Servlet en utilisant le lien : http://localhost:8080/nom_de_l'App/
6-Acceder aux Donné du Mapping si L'URL existe en utilisant le lien : http://localhost:8080/nom_de_l'App/Custom