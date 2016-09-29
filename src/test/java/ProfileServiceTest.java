import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import org.json.JSONArray;
import org.json.JSONException;
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
        String dropQuery = "DROP TABLE IF EXISTS public.profiles";
        String query = "CREATE TABLE public.profiles\n" +
                "(\n" +
                "  email character varying NOT NULL,\n" +
                "  CONSTRAINT users_pkey PRIMARY KEY (email),\n" +
                "  display_name character varying NOT NULL,\n" +
                "  last_name character varying,\n" +
                "  description TEXT,\n" +
                "  state character varying,\n" +
                "  avatar_url character varying,\n" +
                "  followed_and_staff VARCHAR[]" +
                ")";
        statement.execute(dropQuery);
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
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Profiles?user=postgres");
        Statement statement = connection.createStatement();
        String query = "delete from profiles";
        statement.execute(query);
        statement.close();
        connection.close();
    }

    @Test
    public void test404Get() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .get("http://localhost:8001/doesnotexist");
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
                .post("http://localhost:8001/doesnotexist");
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
                .put("http://localhost:8001/doesnotexist");
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
                .delete("http://localhost:8001/doesnotexist");
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
        obj.put("displayName", "Testy");
        obj.put("lastName", "McTestface");
        obj.put("description", "Mercedem aut nummos unde unde extricat, amaras. Petierunt uti sibi concilium totius Galliae in diem certam indicere. Curabitur est gravida et libero vitae dictum.");
        obj.put("state", "CO");
        obj.put("avatarUrl", "http://someurl.com/myimg");
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8001/create")
                .body(obj);
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
        obj.put("displayName", "Testy");
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8001/create")
                .body(obj);
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
        JSONObject profile = seedData();
        if (profile != null) {
            JSONObject email = new JSONObject();
            email.put("email", "testy@test.com");
            Webb webb = Webb.create();
            Request request = webb
                    .get("http://localhost:8001/get?profile=testy@test.com");
            Response<JSONObject>    response = request
                    .asJsonObject();
            JSONObject result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            profile.put("followedAndStaff", new JSONArray());
            JSONObject expected = new JSONObject();
            expected.put("message", "Returning profile");
            expected.put("status", 200);
            expected.put("profile", profile);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(200, response.getStatusCode());
        } else {
            fail("Failed to create user");
        }
    }

    @Test
    public void getUserByWrongEmailReturnsError() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .get("http://localhost:8001/get?email=test@test.com");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "Profile does not exist in database");
        expected.put("status", 400);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void updateUserCorrectlyChangesInformation() throws Exception {
        JSONObject profile = seedData();
        profile.remove("lastName");
        profile.put("lastName", "Differentlastname");
        Webb webb = Webb.create();
        Request updateRequest = webb
                .put("http://localhost:8001/update")
                .body(profile);
        Response<JSONObject> response = updateRequest
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expectedResponse = new JSONObject();
        expectedResponse.put("status", 200);
        expectedResponse.put("message", "Profile updated");
        JSONAssert.assertEquals(expectedResponse, result, true);
        JSONObject getObj = new JSONObject();
        getObj.put("email", "testy@test.com");
        Request getRequest = webb
                .get("http://localhost:8001/get?profile=testy@test.com");
        response = getRequest.asJsonObject();
        result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONArray jsonArray = new JSONArray();
        profile.put("followedAndStaff", jsonArray);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void followUserAppendsEmailToArray() throws Exception {
        JSONObject profile = seedData();
        JSONObject obj = new JSONObject();
        obj.put("email", "another@email.com");
        obj.put("displayName", "another");
        obj.put("lastName", "profile");
        obj.put("description", "Mercedem aut nummos unde unde extricat, amaras. Petierunt uti sibi concilium totius Galliae in diem certam indicere. Curabitur est gravida et libero vitae dictum.");
        obj.put("state", "TX");
        obj.put("avatarUrl", "http://someurl.com/myimg");
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8001/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        if (response.getStatusCode() == 201) {
            JSONObject payload = new JSONObject();
            payload.put("email", profile.getString("email"));
            payload.put("follow", "another@email.com");
            Request followRequest = webb
                    .put("http://localhost:8001/follow")
                    .body(payload);
            response = followRequest.asJsonObject();
            JSONObject result = response.getBody();
            JSONObject expected = new JSONObject();
            expected.put("status", 200);
            expected.put("message", "another@email.com followed");
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(200, response.getStatusCode());
            // Now testing the database
            webb = Webb.create();
            request = webb
                    .get("http://localhost:8001/get?profile=testy@test.com");
            response = request.asJsonObject();
            result = response.getBody();
            JSONArray followedAndStaff = result.getJSONObject("profile").getJSONArray("followedAndStaff");
            JSONArray expected2 = new JSONArray();
            expected2.put("another@email.com");
            JSONAssert.assertEquals(expected2, followedAndStaff, true);
        }
    }

    @Test
    public void followUserWithWrongEmailReturnsError() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8001/create")
                .body(obj);
        obj.remove("displayName");
        obj.put("follow", "another@email.com");
        Request followRequest = webb
                .put("http://localhost:8001/follow")
                .body(obj);
        Response<JSONObject> response = followRequest
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "another@email.com is not a valid user");
        expected.put("status", 400);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void followUserAlreadyFollowed() throws Exception {
        JSONObject profile = seedData();
        JSONObject obj = new JSONObject();
        obj.put("email", "another@email.com");
        obj.put("displayName", "another");
        obj.put("lastName", "profile");
        obj.put("description", "Mercedem aut nummos unde unde extricat, amaras. Petierunt uti sibi concilium totius Galliae in diem certam indicere. Curabitur est gravida et libero vitae dictum.");
        obj.put("state", "TX");
        obj.put("avatarUrl", "http://someurl.com/myimg");
        Webb webb = Webb.create();
        webb.post("http://localhost:8001/create").body(obj).asVoid();
        JSONObject payload = new JSONObject();
        payload.put("email", profile.getString("email"));
        payload.put("follow", "another@email.com");
        webb.put("http://localhost:8001/follow").body(payload).asVoid();
        Request request = webb.put("http://localhost:8001/follow").body(payload);
        Response<JSONObject> response = request.asJsonObject();
        JSONObject expected = new JSONObject();
        expected.put("status", 400);
        expected.put("message", "Already following another@email.com");
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(400, response.getStatusCode());
        // Now testing the database
        webb = Webb.create();
        request = webb
                .get("http://localhost:8001/get?profile=testy@test.com");
        response = request.asJsonObject();
        result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody());
        }
        System.out.println(result);
        JSONArray followedAndStaff = result.getJSONObject("profile").getJSONArray("followedAndStaff");
        JSONArray expected2 = new JSONArray();
        expected2.put("another@email.com");
        JSONAssert.assertEquals(expected2, followedAndStaff, true);
    }

//    @Test
//    public void unfollowUserRemovesEmailFromArray() throws Exception {
//        JSONObject profile = seedData();
//        JSONObject obj = new JSONObject();
//        obj.put("email", "another@email.com");
//        obj.put("displayName", "another");
//        obj.put("lastName", "profile");
//        obj.put("description", "Mercedem aut nummos unde unde extricat, amaras. Petierunt uti sibi concilium totius Galliae in diem certam indicere. Curabitur est gravida et libero vitae dictum.");
//        obj.put("state", "TX");
//        obj.put("avatarUrl", "http://someurl.com/myimg");
//        Webb webb = Webb.create();
//        webb.post("http://localhost:8001/create").body(obj).asVoid();
//        JSONObject payload = new JSONObject();
//        payload.put("email", profile.getString("email"));
//        payload.put("follow", "another@email.com");
//        webb.put("http://localhost:8001/follow").body(payload);
//        // Now unfollowing.
//        payload.remove("follow");
//        payload.put("unfollow", "another@email.com");
//        Request request = webb.put("http://localhost:8001/unfollow").body(payload);
//        Response<JSONObject> response = request.asJsonObject();
//        JSONObject expected = new JSONObject();
//        System.out.println(response.getErrorBody());
//        expected.put("status", 200);
//        expected.put("message", "another@email.com unfollowed");
//        JSONAssert.assertEquals(expected, response.getBody(), true);
//        assertEquals(200, response.getStatusCode());
//        // Now checking the database
//        webb = Webb.create();
//        request = webb.get("http://localhost:8001/get?profile=testy@test.com");
//        response = request.asJsonObject();
//        JSONArray followedAndStaff = response.getBody().getJSONObject("profile").getJSONArray("followedAndStaff");
//        JSONArray expected2 = new JSONArray();
//        JSONAssert.assertEquals(expected2, followedAndStaff, true);
//    }


    @Test
    public void unfollowInvalidUserReturnsError() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        obj.put("unfollow", "another@email.com");
        Webb webb = Webb.create();
        Request unfollowRequest = webb
                .put("http://localhost:8001/unfollow")
                .body(obj);
        Response<JSONObject> response = unfollowRequest
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "another@email.com is not a valid user");
        expected.put("status", 400);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(400, response.getStatusCode());
    }

    private JSONObject seedData() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("email", "testy@test.com");
        obj.put("displayName", "Testy");
        obj.put("lastName", "McTestface");
        obj.put("description", "Mercedem aut nummos unde unde extricat, amaras. Petierunt uti sibi concilium totius Galliae in diem certam indicere. Curabitur est gravida et libero vitae dictum.");
        obj.put("state", "CO");
        obj.put("avatarUrl", "http://someurl.com/myimg");
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8001/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        if (201 == response.getStatusCode()) {
            return obj;
        }
        return null;
    }
}
