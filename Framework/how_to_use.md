# ListControllersServlet

## Description
The `ListControllersServlet` scans for controllers with the `@WebServlet` annotation and lists them on a web page.

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
            <url-pattern>/front/*</url-pattern> <!-- Lien Pour Essayer le Servlet -->
        </servlet-mapping>   
    </web-app>

2-Configurer les controller a scanner on utilison les annotation comme cela:
Controller avec Annotation:
    package com.controller;

    import java.io.IOException;

    // Controller avec @WebServlet annotation
    import jakarta.servlet.*;
    import jakarta.servlet.http.*;
    import jakarta.servlet.annotation.*;

    @WebServlet(name = "AnnotherController", urlPatterns = {"/AnnotherController"}) //Annotation du Controller
    public class AnnotherController extends HttpServlet {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // Implementation here
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

## Usage
3-Compilé le Servlet et Deployer
4-Acceder aux Servlet en utilisant le lien : http://localhost:8080/nom_de_l'App/front/