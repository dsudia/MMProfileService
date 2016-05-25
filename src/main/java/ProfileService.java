import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONObject;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Route;

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
        cpds.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/Users?user=postgres");
        port(8000);
        post("/create", create);
        post("/login", login);
        put("/update", update);
        put("follow", follow);
        put("/unfollow", unfollow);
        get("*", error);
        post("*", error);
        put("*", error);
        delete("*", error);
    }

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