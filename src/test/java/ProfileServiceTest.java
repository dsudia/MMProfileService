import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/*
 * Created by davidsudia on 5/24/16.
 */
public class ProfileServiceTest {
    @BeforeClass
    public static void beforeAll() throws SQLException {
        String host = System.getenv("PG_PORT_5432_TCP_ADDR");
        String port = System.getenv("PG_PORT_5432_TCP_PORT");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "5432";
        }
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Profiles?user=postgres");
        Statement statement = connection.createStatement();
        String query = "CREATE TABLE public.profiles\n" +
                "(\n" +
                "  email character varying NOT NULL,\n" +
                "  display_name character varying NOT NULL,\n" +
                "  last_name character varying,\n" +
                "  CONSTRAINT users_pkey PRIMARY KEY (email)\n" +
                "  description VARCHAR,\n" +
                "  state character varying,\n" +
                "  avatar_url character varying,\n" +
                "  followed_and_staff VARCHAR[]" +
                ")";
        statement.execute(query);
        statement.close();
        connection.close();
        String[] args = {};
        ProfileService.main(args);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}