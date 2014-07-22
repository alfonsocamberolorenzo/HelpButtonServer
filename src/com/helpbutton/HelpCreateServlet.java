package com.helpbutton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;

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
 * This servlet is used to receive a help request from the sender, creating a Help object and executing
 * the method startHelp from this object.
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class HelpCreateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
	 * @uml.property  name="help"
	 * @uml.associationEnd  
	 */
    public Help help;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelpCreateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	}
    /**
   	 * This method receives sender's information needed to create a Help object, and creates and runs it.
   	 * 
   	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   	 */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	res.setContentType("text/html");
		PrintWriter out = res.getWriter();

        String device = req.getParameter("ID");
        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String info = req.getParameter("info");    
        String picture = req.getParameter("picture");
        double longitude = Double.parseDouble(req.getParameter("longitude"));
        double latitude = Double.parseDouble(req.getParameter("latitude"));
        String receiver_id = req.getParameter("receiver_id");
        
        Properties props = new Properties();
        
        try {
            props.load(getServletContext().getResourceAsStream("/WEB-INF/database.properties"));
        } catch (IOException ex) {
            Logger lgr = Logger.getLogger(HelpCreateServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
		help = new Help (device,name,phone,info,longitude,latitude,picture,props,receiver_id);
		
		String state = help.startHelp();
		if(state.equals("success"))
			HelpObjects.helpObjects.put(java.lang.System.identityHashCode(help), help);
		out.print(state);
		out.close();
	}
    

}
