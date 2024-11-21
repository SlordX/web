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
        <param-value>com.controller</param-value>
    </context-param>
         
    <servlet>
        <servlet-name>FrontControllerServlet</servlet-name>
        <servlet-class>com.example.FrontControllerServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>FrontControllerServlet</servlet-name>
        <url-pattern>/front/*</url-pattern>
    </servlet-mapping>

    <welcome-file-list>
        <welcome-file>public/index.html</welcome-file>
    </welcome-file-list>

    
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

    et celui le controller avec la nouvelle annotation
    package com.controller;

    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import com.model.ModelView;

    @WebServlet(name = "NameController", urlPatterns = {"/NameController"})
    public class NameController extends HttpServlet {

        @GET("/submit")
        public ModelView submitName(@Param(name = "name") String name) {
            ModelView modelView = new ModelView("/WEB-INF/views/displayName.jsp");
            modelView.addData("name", name);
            return modelView;
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

3-Crée le GET, POST, Param, FormField, RequestObject, User, UserController, Mapping, Restapi, SampleController et ModelView class comme cela 
# GET
    package com.annotation;

    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD) // Cette annotation est applicable uniquement aux méthodes
    public @interface GET {
        String value(); // L'URL à laquelle la méthode doit répondre
    }

# POST
    package com.annotation;

    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface POST {
        String value();
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

### Param
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    String name();
}

#### FormField
package com.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FormField {
    String value() default "";
}

##### RequestObject
package com.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestObject {
}

###### User
package com.controller;

import com.controller.FormField;

public class User {
    @FormField("user_name")
    private String name;

    @FormField("user_age")
    private int age;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

###### UserController
package com.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import com.controller.GET;
import com.controller.RequestObject;
import com.model.ModelView;
import com.controller.User;
import com.example.MySession;
import com.example.UserDataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "UserController", urlPatterns = {"/UserController"})
public class UserController extends HttpServlet {

    // Hardcoded credentials
    private static final String HARDCODED_USERNAME_1 = "user1";
    private static final String HARDCODED_PASSWORD_1 = "password1";
    private static final String HARDCODED_USERNAME_2 = "user2";
    private static final String HARDCODED_PASSWORD_2 = "password2";

    @GET("/submitUser")
    public ModelView submitUser(@RequestObject User user) {
        ModelView mv = new ModelView("/WEB-INF/views/userResult.jsp");
        mv.addData("user", user);
        return mv;
    }

    @GET("/login")
    public ModelView showLoginForm() {
        ModelView mv = new ModelView("/WEB-INF/views/login.jsp");
        return mv;
    }

    @POST("/logins")
    public ModelView login(@Param(name = "username") String username,
                           @Param(name = "password") String password,
                           MySession session) {
        // Simulate user authentication
        if ((HARDCODED_USERNAME_1.equals(username) && HARDCODED_PASSWORD_1.equals(password)) ||
            (HARDCODED_USERNAME_2.equals(username) && HARDCODED_PASSWORD_2.equals(password))) {
            session.add("username", username);
            ModelView mv = new ModelView("/WEB-INF/views/userData.jsp");
            mv.addAttribute("username", username);  // Add username to be displayed in the view
            mv.addAttribute("dataList", UserDataStore.getUserData(username));
            return mv;
        } else {
            ModelView mv = new ModelView("/WEB-INF/views/login.jsp");
            mv.addAttribute("error", "Invalid credentials");
            return mv;
        }
    }

    @GET("/logout")
    public ModelView logout(MySession session) {
        session.delete("username");
        ModelView mv = new ModelView("/WEB-INF/views/login.jsp");
        return mv;
    }

    private List<String> getUserData(String username) {
        // Simulate fetching user-specific data
        List<String> dataList = new ArrayList<>();
        dataList.add("Data 1 for " + username);
        dataList.add("Data 2 for " + username);
        dataList.add("Data 3 for " + username);
        return dataList;
    }
}

###### Restapi
package com.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Restapi {
}

###### SampleController
package com.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.model.ModelView;

@WebServlet(name = "SampleController", urlPatterns = {"/sampleController"})
public class SampleController extends HttpServlet{

    // A REST API method returning a simple string response
    @Restapi
    @GET("/stringResponse") // URL to access this method
    public String getStringResponse(HttpServletRequest request, HttpServletResponse response) {
        return "Hello, REST API!";
    }
    
    // A REST API method returning an integer response
    @Restapi
    @GET("/intResponse") // URL to access this method
    public int getIntResponse(HttpServletRequest request, HttpServletResponse response) {
        return 42;
    }
    
    // A REST API method returning a ModelView object
    @Restapi
    @GET("/modelViewResponse") // URL to access this method
    public ModelView getModelViewResponse(HttpServletRequest request, HttpServletResponse response) {
        ModelView modelView = new ModelView("/WEB-INF/views/customView.jsp");
        modelView.addData("message", "Hello from ModelView");
        modelView.addData("status", "success");
        return modelView;
    }
}

###### ModelView
    package com.model;

import java.util.HashMap;
import java.util.Map;

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

    public void setUrl(String url) {
        this.url = url;
    }

    public void addAttribute(String key, Object value) {
        data.put(key, value);
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }
}

##### MySession
package com.example;

import jakarta.servlet.http.HttpSession;

public class MySession {
    private HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void add(String key, Object object) {
        session.setAttribute(key, object);
    }

    public void delete(String key) {
        session.removeAttribute(key);
    }
}

###### UserDataStorage
package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataStore {
    private static final Map<String, List<String>> userData = new HashMap<>();

    static {
        // Hardcoded user data
        List<String> user1Data = new ArrayList<>();
        user1Data.add("User1 carol");
        user1Data.add("User1 15");
        user1Data.add("User1 Alone");

        List<String> user2Data = new ArrayList<>();
        user2Data.add("User2 marie");
        user2Data.add("User2 30");
        user2Data.add("User2 Married");

        userData.put("user1", user1Data);
        userData.put("user2", user2Data);
    }

    public static List<String> getUserData(String username) {
        return userData.get(username);
    }
}

###### DataBaseConnection
package com.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/savefile";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC driver explicitly
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found in classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

##### Email
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Email {
    String message() default "Invalid email format";
}

##### MaxLength
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MaxLength {
    int value();
    String message() default "Field exceeds maximum length";
}

##### MinLength
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MinLength {
    int value();
    String message() default "Field length is too short";
}

##### Min
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Min {
    int value();
    String message() default "Value is below the minimum limit";
}

##### NotNull
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotNull {
    String message() default "Field must not be null";
}

##### Numeric
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Numeric {
    String message() default "Field must be numeric";
}

##### Pattern
package com.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Pattern {
    String regex();
    String message() default "Field does not match the required pattern";
}

##### Validator
package com.controller;

import com.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Validator {
    public static List<String> validate(Object obj) {
        List<String> errors = new ArrayList<>();
        
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            
            try {
                Object value = field.get(obj);
                
                // Check @NotNull
                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    NotNull notNull = field.getAnnotation(NotNull.class);
                    errors.add(notNull.message());
                }
                
                // Check @MinLength
                if (field.isAnnotationPresent(MinLength.class) && value != null) {
                    MinLength minLength = field.getAnnotation(MinLength.class);
                    if (value.toString().length() < minLength.value()) {
                        errors.add(minLength.message());
                    }
                }
                
                // Check @MaxLength
                if (field.isAnnotationPresent(MaxLength.class) && value != null) {
                    MaxLength maxLength = field.getAnnotation(MaxLength.class);
                    if (value.toString().length() > maxLength.value()) {
                        errors.add(maxLength.message());
                    }
                }
                
                // Check @Numeric
                if (field.isAnnotationPresent(Numeric.class) && value != null) {
                    if (!value.toString().matches("\\d+")) {
                        Numeric numeric = field.getAnnotation(Numeric.class);
                        errors.add(numeric.message());
                    }
                }

                // Check @Email
                if (field.isAnnotationPresent(Email.class) && value != null) {
                    String emailPattern = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
                    if (!value.toString().matches(emailPattern)) {
                        Email email = field.getAnnotation(Email.class);
                        errors.add(email.message());
                    }
                }

                // Check @Min
                if (field.isAnnotationPresent(Min.class) && value != null) {
                    Min min = field.getAnnotation(Min.class);
                    if (Integer.parseInt(value.toString()) < min.value()) {
                        errors.add(min.message());
                    }
                }

                // Check @Pattern
                if (field.isAnnotationPresent(Pattern.class) && value != null) {
                    Pattern pattern = field.getAnnotation(Pattern.class);
                    if (!value.toString().matches(pattern.regex())) {
                        errors.add(pattern.message());
                    }
                }

            } catch (IllegalAccessException e) {
                errors.add("Error accessing field: " + field.getName());
            }
        }
        
        return errors;
    }
}

##### EmpFormController
package com.controller;

import com.annotation.GET;
import com.annotation.POST;
import com.controller.RequestObject;
import com.model.EmpForm;
import com.controller.Validator;
import com.model.ModelView;

import java.util.List;

public class EmpFormController {

    @GET("/form")
    public ModelView showLoginForm() {
        // This should load the form page
        ModelView mv = new ModelView("/WEB-INF/views/form.jsp");
        return mv;
    }

    @POST("/submitEmpForm")
    public ModelView submitEmpForm(@RequestObject EmpForm form) {
        List<String> errors = Validator.validate(form);

        ModelView modelView = new ModelView();
        if (errors.isEmpty()) {
            modelView.addAttribute("success", "Employee form submitted successfully!");
            modelView.setUrl("/WEB-INF/views/empSuccess.jsp");
        } else {
            modelView.addAttribute("errors", errors);
            modelView.setUrl("/WEB-INF/views/form.jsp");
        }
        return modelView;
    }
}

##### EmpForm
package com.model;

import com.annotation.*;

public class EmpForm {
    @NotNull(message = "Employee name is required")
    private String name;
    
    @NotNull(message = "Employee ID is required")
    @MinLength(value = 5, message = "Employee ID must be at least 5 characters long")
    @MaxLength(value = 10, message = "Employee ID must not exceed 10 characters")
    @Numeric(message = "Employee ID must be numeric")
    private String employeeId;
    
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Min(value = 18, message = "Employee must be at least 18 years old")
    private int age;
    
    @NotNull(message = "Position is required")
    @Pattern(regex = "^(Manager|Developer|Designer)$", message = "Position must be 'Manager', 'Developer', or 'Designer'")
    private String position;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
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

# login.jsp
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login</title>
    </head>
    <body>
        <h2>Login</h2>
        <form method="post" action="/sprinti/listControllers/logins">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username"><br>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password"><br>
            <button type="submit">Login</button>
        </form>
        <c:if test="${not empty error}">
            <p style="color:red;">${error}</p>
        </c:if>
    </body>
    </html>

# userData.jsp    
    <!DOCTYPE html>
    <html>
    <head>
        <title>User Data</title>
    </head>
    <body>
        <h2>User Data</h2>
        <c:forEach var="data" items="${dataList}">
            <p>${dataList}</p>
        </c:forEach>
        <form method="get" action="/sprint8/listControllers/logout">
            <button type="submit">Logout</button>
        </form>
    </body>
    </html>

5-N'oublier pas de crée les views 
# index
    <!DOCTYPE html>
<html>
<head>
    <title>Soumettre un nom</title>
</head>
<body>
    <form action="/sprintg/listControllers/submit" method="post">
        <label for="name">Entrez votre nom :</label>
        <input type="text" id="name" name="name">
        <input type="submit" value="Soumettre">
    </form>
</body>
</html>

## displayName.jsp
<!DOCTYPE html>
<html>
<head>
    <title>Afficher le nom</title>
</head>
<body>
    <h1>Bonjour, ${name}!</h1>
</body>
</html>

### UserResult.jsp
<!DOCTYPE html>
<html>
<head>
    <title>User Result</title>
</head>
<body>
    <h1>User Details</h1>
    <p>Name: ${user.name}</p>
    <p>Age: ${user.age}</p>
</body>
</html>

## form.jsp
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>  <!-- Add this import statement -->
<!DOCTYPE html>
<html>
<head>
    <title>Employee Form</title>
</head>
<body>
    <h2>Employee Form</h2>

    <form action="${pageContext.request.contextPath}/listControllers/submitEmpForm" method="post">
        <label for="name">Employee Name:</label>
        <input type="text" name="name" id="name" value="${param.name}"><br>

        <label for="employeeId">Employee ID:</label>
        <input type="text" name="employeeId" id="employeeId" value="${param.employeeId}"><br>

        <label for="email">Email:</label>
        <input type="text" name="email" id="email" value="${param.email}"><br>

        <label for="age">Age:</label>
        <input type="number" name="age" id="age" value="${param.age}"><br>

        <label for="position">Position:</label>
        <input type="text" name="position" id="position" value="${param.position}"><br>

        <button type="submit">Submit</button>
    </form>
</body>
</html>


## Usage
5-Compilé le Servlet et Deployer
6-Acceder aux Servlet en utilisant le lien : http://localhost:8080/nom_de_l'App/
7-On devrait directement arrivé dans l'index avec 2 inputs pour le sprint7 et 1 input pour le sprint6 entrer n'importe quel et on devrait etre reidirigé vers un modelView pour afficher l'input entrer 
8-Acceder aux Donné du Mapping si L'URL existe en utilisant le lien : http://localhost:8080/nom_de_l'App/Custom Tout en executant la methode du controller avec l'annotation `@GET` pour renvoyer le ModelView et l'afficher via le views
9-Acceder aux Donné du Mapping si L'URL existe en utilisant le lien :  http://localhost:8080/nom_de_l'App/Customa Tout en executant la methode du controller avec l'annotation `@GET` pour avoir les donnée comme dans le sprint3
10-Pour tester on va utilisé les donnée dans SampleController, quand on appelle l'URL qui sont disponible dans celle ci, FrontControllerServlet va scanner la classe et si l'annotation Restapi est present la valeur de retour sera changé an JSON et si c'est un model View les valeurs de celle ci serant aussi transformé en JSON, pour tester on utilise le lien 
 -http://localhost:8080/nom_de_l'App/intResponse
 -http://localhost:8080/nom_de_l'App/StringResponse
 -http://localhost:8080/nom_de_l'App/modelViewResponse
11-Maintenant le FrontControllerServlet va aussi afficher les methods qui ne possédent pas d'annotation avec leurs url respectif
,mais aussi si on appelle un method avec une @POST annotation avec un GET on aurra droit a une erreur et si c'est l'inverse où on appelle un GET avec un POST on aura aussi un message d'erreur 
12-Maintenant quand il y aura des URL dupliqué le server enverra une message d'erreur
on modifié le init et get dans la classe du FrontControllerServlet
13-Apres avoir se connecter, on va voir les données de l'utilisateur et aussi un bouton pour importer un fichier et le sauvegardé dans notre base de donnée
base et table 
use savefile;
CREATE TABLE uploaded_files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_data LONGBLOB NOT NULL
);
The Form to Upload the file should look like this 
<!DOCTYPE html>
<html>
<head>
    <title>User Data</title>
</head>
<body>
    <h2>User Data</h2>
    <c:forEach var="data" items="${dataList}">
        <p>${dataList}</p>
    </c:forEach>

    <form method="post" action="/sprintl/listControllers/uploadFile" enctype="multipart/form-data">
        <label for="file">Upload File:</label>
        <input type="file" name="file" id="file" required>
        <button type="submit">Upload</button>
    </form>

    <form method="get" action="/sprintl/listControllers/logout">
        <button type="submit">Logout</button>
    </form>
</body>
</html>

14-Maintenant quand l'employer va entrer des donnée dans le formulaire la classe de validation va le verifier et que quelque chose manque ou ne correspond au donnée necessaire, le FrontControllerServlet va renvoyer une erreur