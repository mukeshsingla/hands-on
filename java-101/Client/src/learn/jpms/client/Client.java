package learn.jpms.client;

//import learn.jpms.service.impl.MyServiceImpl;
import learn.jpms.service.spi.ServiceSpecification;

import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ServiceLoader;

public class Client {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, SQLException {
//        DriverManager.getConnection("jdbc:blahblah");

//        MyService service = new MyServiceImpl();
//        MyService service = ServiceFactory.getService();
        ServiceLoader<ServiceSpecification> sl = ServiceLoader.load(ServiceSpecification.class);
        for (ServiceSpecification service : sl) {
            Class<?> classz = service.getClass();
            System.out.println("Type: " + classz.getName());
            try {
                Field message = classz.getDeclaredField("message");
                System.out.println("message field found, altering...");
                message.setAccessible(true);
                message.set(null, "007 says hello");
            } catch (NoSuchFieldException e) {
                System.out.println("No message field in this service...");
            }

            System.out.println(service.getMessage());
        }
    }
}
