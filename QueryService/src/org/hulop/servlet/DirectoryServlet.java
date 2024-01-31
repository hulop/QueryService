package org.hulop.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.hulop.data.Directory;
import org.hulop.search.SearchBean;

/**
 * Servlet implementation class DirectoryServlet
 */
@WebServlet("/directory")
public class DirectoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final SearchBean bean = SearchBean.getInstance();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DirectoryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String lat = request.getParameter("lat");
		String lng = request.getParameter("lng");
		String user = request.getParameter("user");
		String dist = request.getParameter("dist");
		dist = (dist != null) ? dist : "500";
		String lang = request.getParameter("lang");
		lang = (lang != null) ? lang : "en";
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("action", "start");
		params.put("lat", lat);
		params.put("lng",  lng);
		params.put("user",  user);
		params.put("dist", dist);
		params.put("lang", lang);
			
		String use_http = System.getenv("HULOP_MAP_SERVICE_USE_HTTP");
		String protocol = ("true".equals(use_http)) ? "http" : "https";
		String mapService = System.getenv("HULOP_MAP_SERVICE");
		String urlString = String.format("%s://%s/routesearch", protocol, mapService);
		Boolean first = true;
		for(String key:params.keySet()) {
			urlString += first ? "?" : "&";
			urlString += key+"="+params.get(key);
			first = false;
		}
		try {
			URL url = new URL(urlString);
			Directory cdd = new Directory(url, new Locale(lang));
			bean.addSearchable(user, cdd);
			sendJSON(cdd.toJSON(), request, response);
		}catch (Exception e) {
			response.sendError(500);
			e.printStackTrace();
		}
	}
	
	public static void sendJSON(Object obj, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		boolean gzip = false;
		if (request != null) {
			String acceptedEncodings = request.getHeader("accept-encoding");
			gzip = acceptedEncodings != null && acceptedEncodings.indexOf("gzip") != -1;
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		OutputStream os = null;
		try {
			String s;
			if (obj instanceof JSONObject) {
				s = ((JSONObject) obj).toString();
			} else if (obj instanceof JSONArray) {
				s = ((JSONArray) obj).toString();
			} else {
				s = obj.toString();
			}
			byte[] data = s.getBytes("UTF-8");
			os = response.getOutputStream();
			if (gzip && data.length >= 860) {
				response.setHeader("Content-Encoding", "gzip");
				GZIPOutputStream gzos = new GZIPOutputStream(os);
				gzos.write(data);
				gzos.finish();
				gzos.close();
			} else {
				os.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
