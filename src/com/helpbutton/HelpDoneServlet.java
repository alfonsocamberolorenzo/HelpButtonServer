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
 * This servlet is used to finish a help request from sender side, selecting the correct Help object 
 * and executing the method doneSender from this object if the sender selected to finish it from the 
 * mobile device; if he/she selected to don't finnish, method startHelp is executed again.
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class HelpDoneServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelpDoneServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	}
    /**
	 * This method receives sender's finish information from a HttpPost request and executes the method doneSender from the
	 * Help object linked with the help request given as the parameter helpId included in the parameter req or executes the
	 * method startHelp, depending to the parameter flag included in req. If the request is done, it is removed from the
	 * HelpObjects object.
     * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	res.setContentType("text/html");
		PrintWriter out = res.getWriter();
   
        String device = req.getParameter("ID");
        String helpId = req.getParameter("helpId");
        String flag = req.getParameter("flag");
        String state = "";
        if(flag.equals("true")){
	        state = HelpObjects.helpObjects.get(Integer.valueOf(helpId)).doneSender(device);
	        HelpObjects.helpObjects.remove(Integer.valueOf(helpId));
        }
        else{
        	state = HelpObjects.helpObjects.get(Integer.valueOf(helpId)).startHelp();
        }
		out.print(state);
		out.close();
	}
    

}
