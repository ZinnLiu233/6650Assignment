import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    GenericObjectPool<Channel> pool;
    Gson gson = new Gson();

    public void init() throws ServletException {
        super.init();
        System.out.println("init servlet");
        try {

            RMQChannelPoolFactory factory = new RMQChannelPoolFactory();
            pool = new GenericObjectPool<>(factory);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        System.out.println("do get");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        // init
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        String urlPath = req.getPathInfo();

        System.out.println(urlPath);
        // the URL
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");
        String requestContent = req.getReader().lines().collect(Collectors.joining());
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson("data not found"));
        } else {
            // skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
            try{
                LiftRide liftRide = gson.fromJson(requestContent, LiftRide.class);
                JsonObject responseContent = new JsonObject();

                responseContent.addProperty("skierId", Integer.valueOf(urlParts[7]));
                responseContent.addProperty("time", liftRide.getTime());
                responseContent.addProperty("liftId", liftRide.getLiftID());
                responseContent.addProperty("waitTime", liftRide.getWaitTime());
                responseContent.addProperty("resortID", Integer.valueOf(urlParts[1]));
                responseContent.addProperty("seasonID", Integer.valueOf(urlParts[3]));
                responseContent.addProperty("dayID", Integer.valueOf(urlParts[5]));
                responseContent.addProperty("skierId", Integer.valueOf(urlParts[7]));
                responseContent.addProperty("vertical", liftRide.getLiftID()*10);
                Channel channel = null;
                try{
                    channel = pool.borrowObject();
                    channel.queueDeclare("rmq_queue", false, false, false, null);
                    channel.basicPublish("", "rmq_queue", null, responseContent.toString().getBytes());

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    pool.returnObject(channel);
                }
                res.setStatus(HttpServletResponse.SC_CREATED);
                res.getWriter().write(gson.toJson("post write Successfully"));

            }catch (Exception e){
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write(gson.toJson("Invalid inputs"));
            }
        }
    }
    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        // "/skiers/10/seasons/2022/days/1/skiers/45164"
        if(urlPath.length == 8) {
            return urlPath[2].equals("seasons") && urlPath[4].equals("days")
                && urlPath[6].equals("skiers");
        }
        return false;
    }
}
