package org.hulop.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.hulop.data.Directory;
import org.hulop.search.SearchBean;

/**
 * Servlet implementation class SearchServlet
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final SearchBean bean = SearchBean.getInstance();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String query = request.getParameter("q");
		String user = request.getParameter("user");
		
		Directory result = bean.search(user, query);
		if (result == null) {
			response.sendError(404);
			return;
		}
		try {
			sendJSON(result.toJSON(), request, response);
		} catch (JSONException e) {
			e.printStackTrace();
			response.sendError(500);
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
