import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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
                "  CONSTRAINT users_pkey PRIMARY KEY (email)\n" +
                "  display_name character varying NOT NULL,\n" +
                "  last_name character varying,\n" +
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

    /**
     * Wipes out the user database (start clean for each test).
     * @throws SQLException if we have trouble hitting database.
     */
    @Before
    public void beforeEach() throws SQLException {
        String host = System.getenv("PG_PORT_5432_TCP_ADDR");
        String port = System.getenv("PG_PORT_5432_TCP_PORT");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "5432";
        }
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Users?user=postgres");
        Statement statement = connection.createStatement();
        String query = "delete from users";
        statement.execute(query);
        statement.close();
        connection.close();
    }

    @Test
    public void test404Get() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .get("http://localhost:8000/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "GET");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void test404Post() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "POST");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void test404Put() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .put("http://localhost:8000/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "PUT");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void test404Delete() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .delete("http://localhost:8000/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "DELETE");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void createUserWithAllInfoCreatesUser() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        obj.put("display_name", "Testy");
        obj.put("last_name", "McTestface");
        obj.put("description", "Mercedem aut nummos unde unde extricat, amaras. Petierunt uti sibi concilium totius Galliae in diem certam indicere. Curabitur est gravida et libero vitae dictum.");
        obj.put("state", "CO");
        obj.put("avatar_url", "http://someurl.com/myimg");
        String payload = obj.toString();
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(payload);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "Profile created for testy@test.com");
        expected.put("status", 201);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void createUserWithMinimumInfoCreatesUser() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        obj.put("display_name", "Testy");
        String payload = obj.toString();
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(payload);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "Profile created for testy@test.com");
        expected.put("status", 201);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void createUserWithoutEmailSendsError() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("display_name", "Testy");
        String payload = obj.toString();
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(payload);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "Profile created for testy@test.com");
        expected.put("status", 201);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void createUserWithoutDisplayNameSendsError() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        String payload = obj.toString();
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(payload);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "Profile created for testy@test.com");
        expected.put("status", 201);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void getUserByEmailReturnsAllUserInfo() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        String payload = obj.toString();
        Webb webb = Webb.create();
        Request request = webb
                .get("http://localhost:8000/get")
                .body(payload);
    }

    @Test
    public void getUserByWrongEmailReturnsError() throws Exception {

    }

    @Test
    public void newUserHasBlankArrayInFollowedAndStaff() throws Exception {

    }

    @Test
    public void updateUserCorrectlyChangesInformation() throws Exception() {

    }

    @Test
    public void followUserAppendsUserToArray() throws Exception {

    }

    @Test
    public void unfollowUserRemovesEmailFromArray() throws Exception {

    }

    @Test
    public void followUserOnlyAcceptsString() throws Exception {

    }

    @Test
    public void unfollowUserOnlyAcceptsString() throws Exception {

    }




}