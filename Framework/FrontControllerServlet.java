package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/front/*")
public class FrontControllerServlet extends HttpServlet {
    private String controllerPackage;

    @Override
    public void init() throws ServletException {
        // Retrieve the controller package name from the context parameter
        ServletContext context = getServletContext();
        controllerPackage = context.getInitParameter("controllerPackage");
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
    
        ServletContext context = getServletContext();
        @SuppressWarnings("unchecked")
        List<Class<?>> controllers = (List<Class<?>>) context.getAttribute("cachedControllers");
    
        if (controllers == null) {
            try {
                controllers = getClasses(controllerPackage);
                context.setAttribute("cachedControllers", controllers);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    
        out.println("<html><body>");
        out.println("<h2>Controller Classes with Annotations:</h2>");
        for (Class<?> controller : controllers) {
            if (controller.isAnnotationPresent(WebServlet.class)) {
                // Extracts the simple name of the class without the package name
                String simpleName = controller.getSimpleName();
                out.println("<p>" + simpleName + "</p>");
            }
        }
        out.println("</body></html>");
    }
     

    private List<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
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
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}