import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONException;
import org.json.JSONObject;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.InvalidKeyException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ProfileService {

    private static ComboPooledDataSource cpds;
    public static void main(String[] args) {
        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl("jdbc:postgresql://localhost/");
        String host = System.getenv("PG_PORT_5432_TCP_ADDR");
        String port = System.getenv("PG_PORT_5432_TCP_PORT");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "5432";
        }
        cpds.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/Profiles?user=postgres");
        port(8000);
        post("/create", create);
//        post("/login", login);
//        put("/update", update);
//        put("follow", follow);
//        put("/unfollow", unfollow);
        get("*", error);
        post("*", error);
        put("*", error);
        delete("*", error);
    }

    private static Route create = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            JSONObject profileInfo = new JSONObject(request.body());
            JSONObject object = new JSONObject();
            if (!profileInfo.has("email") || !profileInfo.has("displayName")) {
                object.put("status", 400);
                object.put("message", "Missing email or display name for creation");
                response.status(400);
                response.type("application/json");
                return object.toString();
            }
            String email = profileInfo.getString("email");
            String displayName = profileInfo.getString("displayName");
            String lastName = "";
            String description = "";
            String state = "";
            String avatarUrl = "";
            if (profileInfo.has("lastName")) {
                lastName = profileInfo.getString("lastName");
            }
            if (profileInfo.has("description")) {
                description = profileInfo.getString("description");
            }
            if (profileInfo.has("state")) {
                state = profileInfo.getString("state");
            }
            if (profileInfo.has("avatarUrl")) {
                avatarUrl = profileInfo.getString("avatarUrl");
            }
            String[] newStringArray = new String[0];
            Connection connection = cpds.getConnection();
            Array followedAndStaff = connection.createArrayOf("VARCHAR", newStringArray);
            String query = "select email from profiles where email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                object.put("status", 409);
                object.put("message", "Profile already exists");
                response.status(409);
                response.type("application/json");
            } else {
                query = "insert into profiles VALUES (?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, displayName);
                preparedStatement.setString(3, lastName);
                preparedStatement.setString(4, description);
                preparedStatement.setString(5, state);
                preparedStatement.setString(6, avatarUrl);
                preparedStatement.setArray(7, followedAndStaff);
                object.put("status", 201);
                object.put("message", "Profile created for " + email);
                response.status(201);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route error = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            JSONObject res = new JSONObject();
            res.put("message", "error");
            res.put("status", 404);
            res.put("requested resource", request.pathInfo());
            res.put("requested method", request.requestMethod());
            response.status(404);
            return res.toString();
        }
    };
}