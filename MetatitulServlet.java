package cz.incad.nkp.inprove;

import org.json.JSONObject;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(value = "/metatitul/*")
public class MetatitulServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());
    public static final String host = System.getProperty("host", "localhost");
    public static final String port = System.getProperty("port", "8983");
    public static final String titulPath = "/solr/titul/query";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.addHeader("Access-Control-Allow-Methods", "GET");
            PrintWriter out = response.getWriter();

            String query = null;
            UriComponents uri;


            if (request.getPathInfo() != null && request.getPathInfo().equals("/test")) {
                //Title information only about TEST data
                query = "*:*&q.op=OR&indent=true&sort=meta_nazev_sort%20asc&rows=500&*=&fq=id:\"1B569F5B32DD652280C63F6DB9C324D15E510C06\"";
            }
            else {
                //All title data excluding data with id = 1B569F5B32DD652280C63F6DB9C324D15E510C06 which is test data
                query = "*:*&q.op=OR&indent=true&sort=meta_nazev_sort%20asc&rows=500&*=&fq=!id:\"1B569F5B32DD652280C63F6DB9C324D15E510C06\"";
            }

            uri = uriBuilder(query);

            LOGGER.log(Level.INFO, "requesting url {0}", uri.toString());
            Map<String, String> reqProps = new HashMap<>();
            reqProps.put("Content-Type", "application/json");
            reqProps.put("Accept", "application/json");
            try (InputStream inputStream = RESTHelper.inputStream(uri.toString(), reqProps)) {
                JSONObject result = removeUnnecessaryData(inputStream);

                String str = result.toString();
                InputStream is = new ByteArrayInputStream(str.getBytes());

                out.print(org.apache.commons.io.IOUtils.toString(is, "UTF8"));
                is.close();
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private UriComponents uriBuilder(String query) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host + ":" + port)
                .path(titulPath)
                .query("q={keyword}")
                .buildAndExpand(query);
    }

    private JSONObject removeUnnecessaryData(InputStream inputStream) {
        try (BufferedReader bR = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";

            StringBuilder responseStrBuilder = new StringBuilder();
            while((line =  bR.readLine()) != null){

                responseStrBuilder.append(line);
            }

            JSONObject result= new JSONObject(responseStrBuilder.toString());
            result.remove("responseHeader");

            return result;
        }
        catch (IOException ioException) {
            throw new RuntimeException("Could not create buffered reader.");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

//    /**
//     * Handles the HTTP <code>POST</code> method.
//     *
//     * @param request servlet request
//     * @param response servlet response
//     * @throws ServletException if a servlet-specific error occurs
//     * @throws IOException if an I/O error occurs
//     */
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        processRequest(request, response);
//    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>



}
