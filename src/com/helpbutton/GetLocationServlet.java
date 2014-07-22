package com.helpbutton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet implementation class RegistrationServlet.
 * 
 * This servlet is used to receive receivers' location and store it in the data base.
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class GetLocationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetLocationServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	}
    /**
     * This method receives receiver's location from a HttpPost request and store this information in the database.
     * 
     * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.println("<html><title>Help Button</title>" + "<body bgcolor=FFFFFF>");
		
    	Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        int device_id = 0;
        java.util.Date today = new java.util.Date();
        Timestamp date = new java.sql.Timestamp(today.getTime());
        out.println("<h6>"+"Date: "+date+"</h6>");
        
        String device = req.getParameter("ID");
        double longitude = Double.parseDouble(req.getParameter("longitude"));
        double latitude = Double.parseDouble(req.getParameter("latitude"));
        
        Properties props = new Properties();
        
        try {
            props.load(getServletContext().getResourceAsStream("/WEB-INF/database.properties"));
            out.println("<h6>"+"Read connection data OK"+"</h6>");
        } catch (IOException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            out.println("<h6>"+"Read connection data FAIL: "+ex+"</h6>");
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        String drivers = props.getProperty("db.drivers");
        props.setProperty("user",user);
        props.setProperty("password",password);
        props.setProperty("drivers", drivers);
        
        try {
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query_device = "SELECT (id) FROM receivers WHERE (device) = (?)";
            pst = con.prepareStatement(query_device);
            pst.setString(1, device);    
            rs = pst.executeQuery();
            if (rs.next()) {
                device_id = Integer.parseInt(rs.getString(1));
                out.println("<h6>Device id: "+device_id+"</h6>");
                String query_delete_device = "DELETE FROM locations WHERE (device) = (?)";
                pst = con.prepareStatement(query_delete_device);
                pst.setInt(1, device_id);    
                pst.executeUpdate();
                String query_location = "INSERT INTO locations (device, longitude, latitude, date) VALUES (?,?,?,?)";
                pst = con.prepareStatement(query_location);
                pst.setInt(1, device_id);    
                pst.setDouble(2, longitude);
                pst.setDouble(3, latitude);
                pst.setTimestamp(4, date);
                pst.executeUpdate();
                out.println("<h6>"+"Insert OK"+"</h6>");
            }
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            out.println("<h6>"+"Insert FAIL SQLException: " + ex + "</h6>");

        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            out.println("<h6>"+"Insert FAIL ClassNotFoundException: " + ex + "</h6>");
		} finally {

            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
		out.println("</body></html");
		out.close();
	}
    

}
