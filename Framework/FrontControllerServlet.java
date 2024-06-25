package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.controller.GET;
import com.controller.Mapping;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/", "/*"})
public class FrontControllerServlet extends HttpServlet {
    private String controllerPackage;
    private HashMap<String, Mapping> urlMappings;

    @Override
    public void init() throws ServletException {
        super.init();
        urlMappings = new HashMap<>();
        ServletContext context = getServletContext();
        controllerPackage = context.getInitParameter("controllerPackage");

        try {
            List<Class<?>> controllers = getClasses(controllerPackage);
            for (Class<?> controller : controllers) {
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        String url = getAnnotation.value();
                        Mapping mapping = new Mapping(controller.getName(), method.getName());
                        urlMappings.put(url, mapping);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ServletException("Error scanning controllers", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String requestUrl = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUrl = requestUrl.substring(contextPath.length());

        Mapping mapping = urlMappings.get(relativeUrl);

        out.println("<html><body>");
        if (relativeUrl.equals("/") || relativeUrl.equals("/listControllers")) {
            out.println("<h2>Controller Classes with Annotations:</h2>");
            for (Class<?> controller : getControllerClasses()) {
                boolean foundGetMethod = false;
                StringBuilder urlPatterns = new StringBuilder();
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        if (!foundGetMethod) {
                            out.println("<p>" + controller.getSimpleName() + "</p>");
                            foundGetMethod = true;
                        }
                        urlPatterns.append(getAnnotation.value()).append("<br>");
                    }
                }
                if (foundGetMethod) {
                    out.println("<p>URL Patterns: " + urlPatterns.toString() + "</p>");
                }
            } 
        } else {
            if (mapping != null) {
                String simpleClassName = mapping.getClassName().substring(mapping.getClassName().lastIndexOf('.') + 1);
                out.println("<h2>URL: " + relativeUrl + "</h2>");
                out.println("<p>Class: " + simpleClassName + "</p>");
                out.println("<p>Method: " + mapping.getMethodName() + "</p>");

                try {
                    // Retrieve the class and method using reflection
                    Class<?> clazz = Class.forName(mapping.getClassName());
                    Method method = clazz.getDeclaredMethod(mapping.getMethodName(), HttpServletRequest.class, HttpServletResponse.class);

                    // Make the method accessible
                    method.setAccessible(true);

                    // Create an instance of the class and invoke the method
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    String result = (String) method.invoke(instance, request, response);

                    // Display the value returned by the method
                    // Check the return type and display appropriately
                    if (result instanceof String) {
                        out.println("<p>Result: " + (String) result + "</p>");
                    } else if (result != null) {
                        out.println("<p>Result (Non-String): " + result.toString() + "</p>");
                    } else {
                        out.println("<p>Result: null</p>");
                    }
                } catch (Exception e) {
                    e.printStackTrace(out);
                }
            } else {
                out.println("<h2>No method associated with the URL: " + relativeUrl + "</h2>");
            }
        }
        out.println("</body></html>");
    }

    private List<Class<?>> getControllerClasses() {
        ServletContext context = getServletContext();
        @SuppressWarnings("unchecked")
        List<Class<?>> controllers = (List<Class<?>>) context.getAttribute("cachedControllers");
        if (controllers == null) {
            try {
                controllers = getClasses(controllerPackage);
                context.setAttribute("cachedControllers", controllers);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        return controllers;
    }

    private List<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
