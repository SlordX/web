package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.annotation.GET;
import com.annotation.Param;
import com.controller.RequestObject;
import com.annotation.Restapi;
import com.annotation.FormField;
import com.controller.Mapping;
import com.annotation.POST;
import com.model.ModelView;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet({"/listControllers", "/listControllers/*"})
public class FrontControllerServlet extends HttpServlet {
    private String controllerPackage;
    private HashMap<String, Mapping> urlMappings;
    private static final Logger LOGGER = Logger.getLogger(FrontControllerServlet.class.getName());

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
                    String url = null;

                    // Handle GET annotation
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        url = getAnnotation.value().isEmpty() ? "/listControllers/" + method.getName() : "/listControllers" + getAnnotation.value();

                    // Handle POST annotation
                    } else if (method.isAnnotationPresent(POST.class)) {
                        POST postAnnotation = method.getAnnotation(POST.class);
                        url = postAnnotation.value().isEmpty() ? "/listControllers/" + method.getName() : "/listControllers" + postAnnotation.value();
                    
                    // Handle methods without any annotations (default to GET)
                    } else {
                        url = "/listControllers/" + controller.getSimpleName() + "/" + method.getName();  // Use method name as default URL for GET
                    }

                    // Ensure no duplicate URL mappings
                    if (urlMappings.containsKey(url)) {
                        LOGGER.log(Level.SEVERE, "Duplicate URL mapping found for URL: " + url);
                        context.setAttribute("initError", "Duplicate URL mapping found for URL: " + url);
                        return;
                    }

                    // Map the URL to the method
                    Mapping mapping = new Mapping(controller.getName(), method.getName());
                    urlMappings.put(url, mapping);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            context.setAttribute("initError", "Error scanning controllers: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = getServletContext();
        String initError = (String) context.getAttribute("initError");

        if (initError != null) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html><body>");
            out.println("<h2>Error 500</h2>");
            out.println("<h3>Error: " + initError + "</h3>");
            out.println("</body></html>");
            return;
        }

        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private boolean isJspViewRequest(String url) {
        return url.startsWith("/WEB-INF/");
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
    
        String requestUrl = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUrl = requestUrl.substring(contextPath.length());
    
        if (relativeUrl.contains("?")) {
            relativeUrl = relativeUrl.substring(0, relativeUrl.indexOf('?'));
        }

        if (urlMappings.containsKey(requestUrl) && urlMappings.get(requestUrl) == null) {
            // Handle duplicate URL mapping error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Set status code 500
            handleError(response, "Duplicate URL mapping found for URL: " + requestUrl);
            return;  // Stop further processing
        }
    
        Mapping mapping = urlMappings.get(relativeUrl);
    
        if (relativeUrl.equals("/listControllers")) {
            out.println("<html><head><style>"
                        + "body {font-family: Arial, sans-serif;}"
                        + ".controller-section {margin-bottom: 20px;}"
                        + ".controller-title {font-size: 1.2em; font-weight: bold; color: #333;}"
                        + ".annotation-methods {color: #0a7f9b;}"
                        + ".default-methods {color: #555; font-style: italic;}"
                        + ".url-pattern {margin-left: 15px; color: #666;}"
                        + "</style></head><body>");
            
            // Separate sections for controllers with annotations and default methods
            out.println("<h3 class='annotation-methods'>Controllers with Annotations and default URL:</h3>");
            
            for (Class<?> controller : getControllerClasses()) {
                boolean hasAnnotation = false;
                StringBuilder annotationMethods = new StringBuilder();
                StringBuilder defaultMethods = new StringBuilder();
                
                for (Method method : controller.getDeclaredMethods()) {
                    String urlPattern = null;
                    boolean isAnnotated = false;
                    
                    // Check for GET or POST annotations
                    if (method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class)) {
                        if (method.isAnnotationPresent(GET.class)) {
                            GET getAnnotation = method.getAnnotation(GET.class);
                            urlPattern = getAnnotation.value().isEmpty() ? "/listControllers/" + method.getName() : "/listControllers" + getAnnotation.value();
                        } else if (method.isAnnotationPresent(POST.class)) {
                            POST postAnnotation = method.getAnnotation(POST.class);
                            urlPattern = postAnnotation.value().isEmpty() ? "/listControllers/" + method.getName() : "/listControllers" + postAnnotation.value();
                        }
                        annotationMethods.append("<span class='url-pattern'>" + urlPattern + "</span><br>");
                        hasAnnotation = true;
                    }
        
                    // Also, handle default GET methods (even if there are annotations)
                    if (!method.isAnnotationPresent(GET.class) && !method.isAnnotationPresent(POST.class)) {
                        urlPattern = "/listControllers/" + controller.getSimpleName() + "/" + method.getName();
                        defaultMethods.append("<span class='url-pattern'>" + urlPattern + "</span><br>");
                    }
                }
        
                // Display controller name and methods with annotations
                if (annotationMethods.length() > 0) {
                    out.println("<div class='controller-section'>");
                    out.println("<p class='controller-title'>Controller: " + controller.getSimpleName() + "</p>");
                    out.println("<p>URL Patterns (Annotated Methods):</p>" + annotationMethods.toString());
                    out.println("</div>");
                }
                
                // Display default methods separately if there are any
                if (defaultMethods.length() > 0) {
                    out.println("<div class='controller-section'>");
                    out.println("<p class='controller-title'>Controller: " + controller.getSimpleName() + "</p>");
                    out.println("<p class='default-methods'>URL Patterns (Default GET Methods):</p>" + defaultMethods.toString());
                    out.println("</div>");
                }
            }
            
            out.println("</body></html>");
        } else if (relativeUrl.startsWith("/listControllers/")) {
            if (mapping != null) {
                String simpleClassName = mapping.getClassName().substring(mapping.getClassName().lastIndexOf('.') + 1);
                out.println("<html><body>");
                out.println("<h2>URL: " + relativeUrl + "</h2>");
                out.println("<p>Class: " + simpleClassName + "</p>");
                out.println("<p>Method: " + mapping.getMethodName() + "</p>");
                out.println("</body></html>");
    
                try {
                    Class<?> controllerClass = Class.forName(mapping.getClassName());
                    Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                    Method method = getControllerMethod(controllerClass, mapping.getMethodName());
                    method.setAccessible(true);
    
                    // Check if the HTTP verb (GET/POST) matches the method annotation
                    checkHttpVerbCompatibility(method, request.getMethod());
    
                    Object[] methodParams = prepareMethodParams(method, request, response);
                    Object result = method.invoke(controllerInstance, methodParams);
    
                    // Handle REST API responses
                    if (method.isAnnotationPresent(Restapi.class)) {
                        response.setContentType("application/json");
                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonResponse;
    
                        if (result instanceof ModelView) {
                            ModelView modelView = (ModelView) result;
                            jsonResponse = objectMapper.writeValueAsString(modelView.getData());
                        } else if (result instanceof String || result instanceof Integer || result instanceof Boolean) {
                            Map<String, Object> jsonMap = new HashMap<>();
                            jsonMap.put("value", result);
                            jsonResponse = objectMapper.writeValueAsString(jsonMap);
                        } else {
                            jsonResponse = objectMapper.writeValueAsString(result);
                        }
    
                        response.getWriter().write(jsonResponse);
                    } else {
                        // Regular HTML response handling
                        if (result instanceof ModelView) {
                            ModelView modelView = (ModelView) result;
                            for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                                request.setAttribute(entry.getKey(), entry.getValue());
                            }
                            request.getRequestDispatcher(modelView.getUrl()).forward(request, response);
                        } else if (result instanceof String) {
                            out.println(result);
                        } else if (result != null) {
                            out.println("<p>Result (Non-String): " + result.toString() + "</p>");
                        } else {
                            out.println("<p>Result: null</p>");
                        }
                    }
                } catch (Exception e) {
                    handleError(response, "Error processing request: " + e.getMessage());
                    LOGGER.log(Level.SEVERE, "Error processing request", e);
                }
            } else {
                handleError(response, "No method associated with the URL: " + relativeUrl);
            }
        } else {
            if (mapping != null) {
                if (isJspViewRequest(relativeUrl)) {
                    forwardToJspView(relativeUrl, request, response);
                    return;
                } else {
                    handleError(response, "No method associated with the URL: " + relativeUrl);
                }
            }
        }
    }
    
    

    private void checkHttpVerbCompatibility(Method method, String httpMethod) throws ServletException {
        // If the method has a @GET annotation but the HTTP request is POST
        if (method.isAnnotationPresent(GET.class) && !"GET".equalsIgnoreCase(httpMethod)) {
            throw new ServletException("Invalid HTTP method. Expected GET but received " + httpMethod);
        } 
        // If the method has a @POST annotation but the HTTP request is GET
        else if (method.isAnnotationPresent(POST.class) && !"POST".equalsIgnoreCase(httpMethod)) {
            throw new ServletException("Invalid HTTP method. Expected POST but received " + httpMethod);
        } 
        // If no annotations are present, assume it is GET by default
        else if (!method.isAnnotationPresent(GET.class) && !method.isAnnotationPresent(POST.class) && !"GET".equalsIgnoreCase(httpMethod)) {
            throw new ServletException("Invalid HTTP method. Expected GET by default but received " + httpMethod);
        }
    }
    
    
    
    private void forwardToJspView(String viewUrl, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
        dispatcher.forward(request, response);
    }    

    private Method getControllerMethod(Class<?> controllerClass, String methodName) throws NoSuchMethodException {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchMethodException(controllerClass.getName() + "." + methodName);
    }

    private Object[] prepareMethodParams(Method method, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = new Object[paramTypes.length];
    
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == MySession.class) {
                params[i] = new MySession(request.getSession());
            } else {
                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation instanceof Param) {
                        Param param = (Param) annotation;
                        String paramName = param.name();
                        String paramValue = request.getParameter(paramName);
                        params[i] = convert(paramTypes[i], paramValue);
                    } else if (annotation instanceof RequestObject) {
                        params[i] = populateRequestObject(paramTypes[i], request);
                    } else if (paramTypes[i] == HttpServletRequest.class) {
                        params[i] = request;
                    } else if (paramTypes[i] == HttpServletResponse.class) {
                        params[i] = response;
                    }
                }
            }
        }
        return params;
    }
    

    private Object populateRequestObject(Class<?> paramType, HttpServletRequest request) throws Exception {
        Object obj = paramType.getDeclaredConstructor().newInstance();

        for (Field field : paramType.getDeclaredFields()) {
            String paramName = field.getName();
            if (field.isAnnotationPresent(FormField.class)) {
                FormField formField = field.getAnnotation(FormField.class);
                if (!formField.value().isEmpty()) {
                    paramName = formField.value();
                }
            }
            String paramValue = request.getParameter(paramName);
            field.setAccessible(true);
            field.set(obj, convert(field.getType(), paramValue));
        }

        return obj;
    }

    private Object convert(Class<?> type, String value) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        // Add more conversions if needed
        return null;
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

    private void handleError(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h2>Error 404</h2>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("</body></html>");
    }
}
