package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.controller.GET;
import com.controller.Param;
import com.controller.RequestObject;
import com.controller.FormField;
import com.controller.Mapping;
import com.controller.POST;
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
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        String url = "/listControllers" + getAnnotation.value();
                        if (urlMappings.containsKey(url)) {
                            LOGGER.log(Level.SEVERE, "Duplicate URL mapping found: " + url);
                            throw new ServletException("Duplicate URL mapping found for URL: " + url);
                        }
                        Mapping mapping = new Mapping(controller.getName(), method.getName());
                        urlMappings.put(url, mapping);
                    } else if (method.isAnnotationPresent(POST.class)) {
                        POST postAnnotation = method.getAnnotation(POST.class);
                        String url = "/listControllers" + postAnnotation.value();
                        if (urlMappings.containsKey(url)) {
                            LOGGER.log(Level.SEVERE, "Duplicate URL mapping found: " + url);
                            throw new ServletException("Duplicate URL mapping found for URL: " + url);
                        }
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

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
    
        String requestUrl = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUrl = requestUrl.substring(contextPath.length());
    
        if (relativeUrl.contains("?")) {
           relativeUrl = relativeUrl.substring(0, relativeUrl.indexOf('?'));
        }
    
        Mapping mapping = urlMappings.get(relativeUrl);
    
        out.println("<html><body>");
        if (relativeUrl.equals("/listControllers")) {
            out.println("<h2>Controller Classes with Annotations:</h2>");
            for (Class<?> controller : getControllerClasses()) {
                boolean foundMethod = false;
                StringBuilder urlPatterns = new StringBuilder();
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class)) {
                        String urlPattern = method.isAnnotationPresent(GET.class) ? method.getAnnotation(GET.class).value() : method.getAnnotation(POST.class).value();
                        if (!foundMethod) {
                            out.println("<p>" + controller.getSimpleName() + "</p>");
                            foundMethod = true;
                        }
                        urlPatterns.append(urlPattern).append("<br>");
                    }
                }
                if (foundMethod) {
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
                    Class<?> controllerClass = Class.forName(mapping.getClassName());
                    Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                    Method method = getControllerMethod(controllerClass, mapping.getMethodName());
                    method.setAccessible(true);
    
                    Object[] methodParams = prepareMethodParams(method, request, response);
    
                    Object result = method.invoke(controllerInstance, methodParams);
    
                    if (result instanceof String) {
                        out.println(result);
                    } else if (result instanceof ModelView) {
                        ModelView modelView = (ModelView) result;
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
        out.println("</body></html>");
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
        out.println("<h2>Error</h2>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("</body></html>");
    }
}
