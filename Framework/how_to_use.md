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
    import com.model.ModelView;

    @WebServlet(name = "AnnotatedController", urlPatterns = {"/annotatedController"})
    public class AnnotatedController extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.getWriter().println("GET request handled by AnnotatedController");
        }

        @GET("/custom")
        protected ModelView customGetMethod(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            ModelView mv = new ModelView("/WEB-INF/views/customView.jsp");
            mv.addObject("message", "Custom GET request handled by customGetMethod");
            return mv;
        }

        @GET("/customa")
        protected String customGetMethods(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            return "Custom GET request handled by customGetMethods";
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

3-Crée le GET, Mapping et ModelView class comme cela 
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

## Mapping
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

### ModelView
    package com.model;

    import java.util.HashMap;

    public class ModelView {
        private String url;
        private HashMap<String, Object> data;

        public ModelView(String url) {
            this.url = url;
            this.data = new HashMap<>();
        }

        public String getUrl() {
            return url;
        }

        public HashMap<String, Object> getData() {
            return data;
        }

        public void addObject(String key, Object value) {
            this.data.put(key, value);
        }
    }

4-Crée le view dans WEB-INF/views
# customView.jsp
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <html>
    <head>
        <title>Custom View</title>
    </head>
    <body>
        <h2>${message}</h2>
    </body>
    </html>


## Usage
5-Compilé le Servlet et Deployer
6-Acceder aux Servlet en utilisant le lien : http://localhost:8080/nom_de_l'App/
7-Acceder aux Donné du Mapping si L'URL existe en utilisant le lien : http://localhost:8080/nom_de_l'App/Custom Tout en executant la methode du controller avec l'annotation `@GET` pour renvoyer le ModelView et l'afficher via le views
8-Acceder aux Donné du Mapping si L'URL existe en utilisant le lien :  http://localhost:8080/nom_de_l'App/Customa Tout en executant la methode du controller avec l'annotation `@GET` pour avoir les donnée comme dans le sprint3