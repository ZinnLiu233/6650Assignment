package A2Server;

import Entity.LifeRide;
import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class SkierServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String jsonString = new Gson().toJson("missing paramterers");
      out.write(jsonString);
      return;
    }

    String[] urlParts = urlPath.split("/");

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String jsonString = new Gson().toJson("invalid url");
      out.write(jsonString);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      String jsonString = new Gson().toJson("get successfully");
      out.write(response.getStatus() + jsonString);
    }

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
      res.setContentType("application/json");
      PrintWriter out = res.getWriter();
      String urlPath = req.getPathInfo();
      Gson gson = new Gson();
      System.out.println(urlPath);
      // check we have a URL!
      if (urlPath == null || urlPath.isEmpty()) {
          res.setStatus(HttpServletResponse.SC_NOT_FOUND);
          res.getWriter().write("missing paramterers");
          return;
      }

      String[] urlParts = urlPath.split("/");
      // and now validate url path and return the response status code
      // (and maybe also some value if input is valid)

      if (!isUrlValid(urlParts)) {
          res.setStatus(HttpServletResponse.SC_NOT_FOUND);
          res.getWriter().write(gson.toJson("data not found"));
      } else {
//          res.setStatus(HttpServletResponse.SC_OK);
//          // do any sophisticated processing with urlParts which contains all the url params
//          // TODO: process url params in `urlParts`
        try{
          LifeRide lifeRide = new LifeRide(217, 21);
          String lifeRideJson = gson.toJson(lifeRide);
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
