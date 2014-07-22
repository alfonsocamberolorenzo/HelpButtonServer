package com.helpbutton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet implementation class RegistrationServlet.
 * 
 * This servlet is used to register a receiver in data base as a new user of the receiver app. 
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class RegisterReceiversServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterReceiversServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    }
    /**
	 * This method receives receiver's Google device token from a HttpPost request and store this information in the database.
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

        String device = req.getParameter("ID");
        
        Properties props = new Properties();
        
        try {
            props.load(getServletContext().getResourceAsStream("/WEB-INF/database.properties"));
            out.println("<h6>"+"Read connection data OK"+"</h6>");
        } catch (IOException ex) {
            Logger lgr = Logger.getLogger(RegisterReceiversServlet.class.getName());
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
            String stm = "INSERT INTO receivers (device) VALUES (?)";
            pst = con.prepareStatement(stm);
            pst.setString(1, device);                    
            pst.executeUpdate();
            out.println("<h6>"+"Insert OK"+"</h6>");
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(RegisterReceiversServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            out.println("<h6>"+"Insert FAIL SQLException: " + ex + "</h6>");

        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(RegisterReceiversServlet.class.getName());
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
                Logger lgr = Logger.getLogger(RegisterReceiversServlet.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
		out.println("</body></html");
		out.close();
	}

}
