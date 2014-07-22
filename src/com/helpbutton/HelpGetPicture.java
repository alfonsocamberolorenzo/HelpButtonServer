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
 * This servlet is used to return a picture linked with a help request, selecting the correct Help object and 
 * executing the method getPicture from this object. 
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class HelpGetPicture extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
	 * @uml.property  name="help"
	 * @uml.associationEnd  readOnly="true"
	 */
    public Help help;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelpGetPicture() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	}
    /**
   	 * This method returns sender's picture, as String, linked to a helpId.
     * 
   	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   	 */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	res.setContentType("text/html");
		PrintWriter out = res.getWriter();
   
        String device = req.getParameter("ID");
        String helpId = req.getParameter("helpId");
        
        String state = HelpObjects.helpObjects.get(Integer.valueOf(helpId)).getPicture(device);
		out.print(state);
		out.close();
	}
    

}
