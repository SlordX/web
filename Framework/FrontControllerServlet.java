package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.controller.GET;
import com.controller.Mapping;
import com.model.ModelView;
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
import java.util.Map;

@WebServlet({"/listControllers", "/listControllers/*"})
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
                        String url = "/listControllers" + getAnnotation.value();  // Prepend '/controller' to the annotation value
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

    private boolean isJspViewRequest(String url) {
        return url.startsWith("/WEB-INF/");
    }
    
    private void forwardToJspView(String viewUrl, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
        dispatcher.forward(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String requestUrl = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUrl = requestUrl.substring(contextPath.length());

        Mapping mapping = urlMappings.get(relativeUrl);

        out.println("<html><body>");
        if (relativeUrl.equals("/listControllers")) {
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
        } else if (relativeUrl.startsWith("/listControllers/")) {
            if (mapping != null) {
                String simpleClassName = mapping.getClassName().substring(mapping.getClassName().lastIndexOf('.') + 1);
                out.println("<h2>URL: " + relativeUrl + "</h2>");
                out.println("<p>Class: " + simpleClassName + "</p>");
                out.println("<p>Method: " + mapping.getMethodName() + "</p>");

                try {
                    // Get the class
                Class<?> controllerClass = Class.forName(mapping.getClassName());
                // Create an instance of the class
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                // Get the method
                Method method = controllerClass.getDeclaredMethod(mapping.getMethodName(), HttpServletRequest.class, HttpServletResponse.class);
                // Make the method accessible if it's not public
                method.setAccessible(true);
                // Invoke the method
                Object result = method.invoke(controllerInstance, request, response);

                // Check the return type
                if (result instanceof String) {
                    // If return type is String, directly send it as response
                    out.println(result);
                } else if (result instanceof ModelView) {
                    // If return type is ModelView, set attributes and dispatch to the specified URL
                    ModelView modelView = (ModelView) result;
                    // Set attributes
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    request.getRequestDispatcher(modelView.getUrl()).forward(request, response);
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
        } else {
            if (mapping != null) {
                // Check if it's a JSP view request
                if (isJspViewRequest(relativeUrl)) {
                    // Forward directly to the JSP view
                    forwardToJspView(relativeUrl, request, response);
                    return;  // Stop further processing               
                } else {
                    out.println("<h2>Aucune méthode ou vue associée à l'URL: " + relativeUrl + "</h2>");
                }
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
