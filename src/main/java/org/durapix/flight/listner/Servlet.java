package org.durapix.flight.listner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends HttpServlet{

    public final static Logger LOGGER = Logger.getLogger(Servlet.class.getName());

    private JSONArray contacts = null;
    private static JSONObject jObj = null;
    private static String FLEX_APPID = "FLEX_APPID";
    private static String FLEX_APPKEY = "FLEX_APPKEY";

    private ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();

    private static final String TAG_Array = "flightStatuses";
    private static final String TAG_ID = "flightId";
    private static final String TAG_DepAIrportCode = "departureAirportFsCode";
    private static final String TAG_ArrAIrportCode = "arrivalAirportFsCode";
    private static final String TAG_FlightNum = "flightNumber";

    private static String url = "https://api.flightstats.com/flex/flightstatus/rest/v2/json/airport/status/CMB/dep/2014/04/10/0?appId="+FLEX_APPID+"&appKey="+FLEX_APPKEY+"&utc=false&numHours=1&maxFlights=5";

//        ----------- References ----------
//        https://developer.flightstats.com/api-docs/how_to
//        https://developer.flightstats.com/api-docs/flightstatus/v2/airport
//        http://stackoverflow.com/questions/15174725/how-to-use-flightstatus-api-it-returns-null-on-android
//        https://github.com/rcvrgs/Mobile-LabIV/blob/master/LabIV/src/com/computomovil/labIV/remote/RestClient.java
//        ---------------------------------

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException{

        try{
            JSONObject jSon12 = queryRESTurl(url);

            try {
                contacts = jSon12.getJSONArray(TAG_Array);
                for(int i = 0; i < contacts.length(); i++){
                    JSONObject c = contacts.getJSONObject(i);

                    Integer id = c.getInt(TAG_ID);
                    String name = c.getString(TAG_DepAIrportCode);
                    String email = c.getString(TAG_ArrAIrportCode);
                    String address = c.getString(TAG_FlightNum);

                    LOGGER.info("^^^^^^^^^^ #" + i + " > ID: " + id );

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TAG_ID, String.valueOf(id));
                    map.put(TAG_DepAIrportCode, name);
                    map.put(TAG_ArrAIrportCode, email);
                    contactList.add(map);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            PrintWriter out = response.getWriter();
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Json : "  + jSon12.toString() + "</h1>");
            out.println("</body>");
            out.println("</html>");

        }     catch (Exception e){
            e.printStackTrace();
        }
    }

    public static JSONObject queryRESTurl(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;

        try {
            response = httpclient.execute(httpget);
            LOGGER.info("Status:[" + response.getStatusLine().toString() + "]");
            HttpEntity entity = null;
            try{
                entity = response.getEntity();
            }catch(Exception e){
                LOGGER.info("Error in http connection " + e.toString());
            }
            if (entity != null) {

                InputStream instream = entity.getContent();

                String result = convertStreamToString(instream);
                LOGGER.info("TAG" + "Result of converstion: [" + result + "]");

                instream.close();
                //return result;

                try {
                    jObj = new JSONObject(result);
                } catch (JSONException e) {
                    LOGGER.info("Error parsing data " + e.toString());
                }
                return jObj;

            }
        } catch (ClientProtocolException e) {
            LOGGER.info("There was a protocol based error" + e);
        } catch (IOException e) {
            LOGGER.info("There was an IO Stream related error" + e);
        }

        return null;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException ioe) {
            LOGGER.info("RESTClient.convertStreamToString: "+ ioe.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                LOGGER.info("RESTClient.convertStreamToString finally: "+ ioe.getMessage());
            }
        }
        return sb.toString();
    }
}

