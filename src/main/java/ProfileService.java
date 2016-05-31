import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.InvalidKeyException;
import java.sql.*;
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
        port(8001);
        post("/create", create);
        get("/get", get);
        put("/update", update);
        put("follow", follow);
        put("/unfollow", unfollow);
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
                object.put("status", 400);
                object.put("message", "Profile already exists");
                response.status(400);
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
                preparedStatement.execute();
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

    private static Route get = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            Connection connection = cpds.getConnection();
            String query = "select * from profiles where email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, request.queryParams("profile"));
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            JSONObject returner = new JSONObject();
            if (resultSet.next()) {
                JSONObject profile = new JSONObject();
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    if (resultSetMetaData.getColumnType(i) == Types.ARRAY) {
                        profile.put(snakeToCamel(resultSetMetaData.getColumnName(i)), new JSONArray(resultSet.getArray(i).getArray()));

                    } else {
                        profile.put(snakeToCamel(resultSetMetaData.getColumnName(i)), resultSet.getString(i));
                    }
                }
                returner.put("message", "Returning profile");
                returner.put("profile", profile);
                returner.put("status", 200);
                response.status(200);
            } else {
                returner.put("message", "Profile does not exist in database");
                returner.put("status", 400);
                response.status(400);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();;
            response.type("application/json");
            return returner;
        }
    };

    private static Route update = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            JSONObject params = new JSONObject(request.body());
            String query = "UPDATE profiles SET ";
            Iterator<?> keys = params.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                query += camelToSnake(key) + "=?,";
            }
            query = query.substring(0, query.length() - 1);
            query += " WHERE email=?;";
            Connection connection = cpds.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            int count = 1;
            keys = params.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                preparedStatement.setString(count, params.getString(key));
                count++;
            }
            preparedStatement.setString(count, params.getString("email"));
            preparedStatement.execute();
            JSONObject returner = new JSONObject();
            returner.put("status", 200);
            returner.put("message", "Profile updated");
            return returner;
        };
    };

    private static  Route follow = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            JSONObject params = new JSONObject(request.body());
            JSONObject returner = new JSONObject();
            Connection connection = cpds.getConnection();
            String query = "select email from profiles where email=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, params.getString("follow"));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                query = "select followed_and_staff from profiles where email=?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, params.getString("email"));
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                String[] followed = (String[]) resultSet.getArray(1).getArray();
                boolean alreadyFollowed = false;
                for (String email : followed) {
                    if (email.equals(params.getString("follow"))) {
                        alreadyFollowed = true;
                        break;
                    }
                }
                if (!alreadyFollowed) {
                    query = "update profiles " +
                            "set followed_and_staff = array_append(followed_and_staff, ?)" +
                            "where email=?";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, params.getString("follow"));
                    preparedStatement.setString(2, params.getString("email"));
                    try {
                        preparedStatement.execute();
                        response.status(200);
                        returner.put("status", 200);
                        returner.put("message", params.getString("follow") + " followed");
                    } catch (Exception SQLException) {
                        response.status(500);
                        returner.put("status", 500);
                        returner.put("message", "Something went wrong on the server");
                    }
                } else {
                    response.status(400);
                    returner.put("status", 400);
                    returner.put("message", "Already following " + params.get("follow"));
                }

            } else {
                returner.put("status", 400);
                returner.put("message", params.getString("follow") + " is not a valid user");
                response.status(400);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return returner;
        }
    };

    private static  Route unfollow = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            JSONObject params = new JSONObject(request.body());
            JSONObject returner = new JSONObject();
            Connection connection = cpds.getConnection();
            String query = "select email from profiles where email=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, params.getString("unfollow"));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                query = "select followed_and_staff from profiles where email=?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, params.getString("email"));
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                String[] followed = (String[]) resultSet.getArray(1).getArray();
                boolean alreadyFollowed = false;
                for (String email : followed) {
                    if (email.equals(params.getString("unfollow"))) {
                        alreadyFollowed = true;
                        System.out.println(email);
                        break;
                    }
                }
                if (alreadyFollowed) {
                    query = "update profiles " +
                            "set followed_and_staff = array_remove(followed_and_staff, ?)" +
                            "where email=?";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, params.getString("unfollow"));
                    preparedStatement.setString(2, params.getString("email"));
                    try {
                        preparedStatement.execute();
                        response.status(200);
                        returner.put("status", 200);
                        returner.put("message", "You have unfollowed " + params.getString("unfollow"));
                    } catch (Exception SQLException) {
                        response.status(500);
                        returner.put("status", 500);
                        returner.put("message", "Something went wrong on the server");
                    }
                } else {
                    response.status(400);
                    returner.put("status", 400);
                    returner.put("message", "You are not following " + params.get("unfollow"));
                }

            } else {
                returner.put("status", 400);
                returner.put("message", params.getString("unfollow") + " is not a valid user");
                response.status(400);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return returner;
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

    private static String snakeToCamel(String snake) {
        String[] words = snake.split("_");
        String returner = words[0];
        for (int i = 1; i < words.length; i++) {
            returner += words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return returner;
    }
    private static String camelToSnake(String camel) {
        String returner = "";
        for (int i = 0; i < camel.length(); i++) {
            if (!Character.isUpperCase(camel.charAt(i))) {
                returner += camel.charAt(i);
            } else {
                returner += "_" + Character.toLowerCase(camel.charAt(i));
            }
        }
        return returner;
    }
}