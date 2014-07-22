package com.helpbutton;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
/**
 * Class Help implements all the methods needed to generate, communicate and control data in
 * a help situation.
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class Help{
	/**
	 * @uml.property  name="receiver_device"
	 */
	private int receiver_device;
	/**
	 * @uml.property  name="min_distance"
	 */
	private int min_distance = 999999999;
	/**
	 * @uml.property  name="name"
	 */
	private String name;
	/**
	 * @uml.property  name="phone"
	 */
	private String phone;
	/**
	 * @uml.property  name="info"
	 */
	private String info;
	/**
	 * @uml.property  name="sender_device"
	 */
	private String sender_device;
	/**
	 * @uml.property  name="picture"
	 */
	private String picture;
	/**
	 * @uml.property  name="helpId"
	 */
	private int helpId;
	/**
	 * @uml.property  name="log_devices"
	 * @uml.associationEnd  multiplicity="(0 -1)" inverse="this$0:com.helpbutton.Help$Log"
	 */
	private List<Log> log_devices = new ArrayList<Log>();
	/**
	 * @uml.property  name="longitude"
	 */
	private double longitude;
	/**
	 * @uml.property  name="latitude"
	 */
	private double latitude;
	/**
	 * @uml.property  name="con"
	 */
	private Connection con;
    /**
	 * @uml.property  name="pst"
	 */
    private PreparedStatement pst;
    /**
	 * @uml.property  name="rs"
	 */
    private ResultSet rs;
    /**
	 * @uml.property  name="props"
	 */
    private Properties props;
    /**
	 * @uml.property  name="url"
	 */
    private String url;
	/**
	 * @uml.property  name="user"
	 */
	private String user;
	/**
	 * @uml.property  name="password"
	 */
	private String password;
	/**
	 * @uml.property  name="drivers"
	 */
	private String drivers;
	/**
	 * @uml.property  name="apikey"
	 */
	private String apikey;
    /**
	 * @uml.property  name="accepted"
	 */
    private boolean accepted;

	/**
	 * The constructor Help creates a new Help object that contains all the information about a
	 * help situation.
	 * 
	 * @param sender_device This is the sender's Google device token.
	 * @param name This is the sender's name.
	 * @param phone This is the sender's phone.
	 * @param info This is the sender's health information.
	 * @param longitude This is the sender's current longitude.
	 * @param latitude This is the sender's current latitude.
	 * @param picture This is the sender's picture.
	 * @param props Theses are the properties needed to connect with the database.
	 * @param receiver_id This is the sender's receiver id, if the sender is also a receiver.
	 */
	public Help(String sender_device, String name, String phone, String info, double longitude, double latitude, String picture, Properties props, String receiver_id){
		this.sender_device = sender_device;
		this.name = name;
		this.phone = phone;
		this.info = info;
		this.longitude = longitude;
		this.latitude = latitude;
		this.props = props;
		this.helpId = java.lang.System.identityHashCode(this);
		this.picture = picture;
		this.accepted = false;
		//If there is any receiver id, this is exluced from the system
		//if(!receiver_id.equals("none"))
			//excludeSender(receiver_id);
	}
	/**
	 * This method indicates the running schedule of the system.
	 * 
	 * @return String This return a flag with information about the success or fail of the method.
	 * @exception Exception Every exception is caught in order to inform about every execution problem.
	 */
	public String startHelp(){
		try{
			//System send a message to the sender to inform about the current status.
			String userMessage = "{'help':{'helpId':'"+helpId+"','status':'Looking for a receiver.'}}";
	        notifyUser(sender_device,userMessage);
	        
			//Connect to database
			openDataBaseConnection();
			
			//Choose receiver
			receiver_device = selectReceiver();
			if(receiver_device == 0)	
				return "no_device";
			
			//Send request to receiver
			sendRequest(receiver_device);
			
			//Wait for the answer
			new Thread(new Timer()).start();
			//If answer OK wait for the end
			//If answer REJECT start again excluding the receiver (adding this to the array exclude_receivers)
			//Close database connection
			closeDataBaseConnection();
	      
			return "success";
		}
		catch(Exception ex){
			Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex); 
            return "fail";
		}
	}
	/**
	 * This method selects the receiver closer to the sender.
	 * 
	 * @exception Exception Every exception is caught in order to inform about every execution problem.
	 * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
	 */
	private int selectReceiver(){
		//Read receiver's location from database
		int receiver = 0;
        try {
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query = "SELECT * FROM locations";
            pst = con.prepareStatement(query);
            rs = pst.executeQuery();
            while (rs.next()) {
            	List<Integer> exclude = new ArrayList<Integer>();
            	for(int i=0; i<log_devices.size(); i++){
            		if(log_devices.get(i).getReason().equals("cancel") 
            			|| log_devices.get(i).getReason().equals("sender") 
            			|| log_devices.get(i).getReason().equals("reject") 
            			|| log_devices.get(i).getReason().equals("done receiver") 
            			|| log_devices.get(i).getReason().equals("timeOut")){
            			int device = 0;
            			try{
            				query = "SELECT (id) FROM receivers WHERE (device) = (?)";
            				pst = con.prepareStatement(query);
            				pst.setString(1, log_devices.get(i).getDevice()); 
            				ResultSet rs_aux = pst.executeQuery();
            				while (rs.next()) {
            					device = rs.getInt(1);
            				}
            			}
            			catch(Exception ex){
            				Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            	            lgr.log(Level.SEVERE, ex.getMessage(), ex);  
            			}
            			exclude.add(device);
            		}
            	}
            	if(!exclude.contains(Integer.parseInt(rs.getString(2)))){
	            	double receiver_longitude = Double.parseDouble(rs.getString(3));
	            	double receiver_latitude = Double.parseDouble(rs.getString(4));
	            	int user_distance = calculateDistanceByHaversineFormula(longitude, latitude, receiver_longitude, receiver_latitude);
	            	if(user_distance < min_distance){
	            		min_distance = user_distance;
	            		receiver = Integer.parseInt(rs.getString(2));
	            	}
            	}
            }  
            min_distance = 999999999;
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);     
        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}  
		return receiver;
	}
	/**
	 * This method exclude the device given as a parameter from the receives list.
	 * 
	 * @param id This is the receiver's Google device token.
	 */
	private void excludeSender(String id){
		java.util.Date today = new java.util.Date();
        Timestamp date = new java.sql.Timestamp(today.getTime());
        Log entry = new Log(id, date, "sender");
		log_devices.add(entry);
	}
	/**
	 * This method calculates the distance between two points given as longitude and latitude through
	 * Haversine's formula.
	 * 
	 * @param lon1 This is the first point's longitude.
	 * @param lat1 This is the first point's latitude.
	 * @param lon2 This is the second point's longitude.
	 * @param lat2 This is the second point's latitude
	 * @return int This return the real distance between the two points given as parameters.
	 */
	private static int calculateDistanceByHaversineFormula(double lon1, double lat1, double lon2, double lat2) {
			double earthRadius = 6371; //km
			lat1 = Math.toRadians(lat1);
			lon1 = Math.toRadians(lon1);
			lat2 = Math.toRadians(lat2);
			lon2 = Math.toRadians(lon2);
			double dlon = (lon2 - lon1);
			double dlat = (lat2 - lat1);
			double sinlat = Math.sin(dlat / 2);
			double sinlon = Math.sin(dlon / 2);
			double a = (sinlat * sinlat) + Math.cos(lat1)*Math.cos(lat2)*(sinlon*sinlon);
			double c = 2 * Math.asin (Math.min(1.0, Math.sqrt(a)));
			double distanceInMeters = earthRadius * c * 1000;
			return (int)distanceInMeters;
	}
	/** 
	 * This method sends to the receiver given as a parameter a request about a new help situation available.
	 * @param receiver This is the database receiver's id.
	 * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
	 */
	private void sendRequest(int receiver){
		String receiver_token = null;
		try {
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query_device = "SELECT (device) FROM receivers WHERE (id) = (?)";
            pst = con.prepareStatement(query_device);
            pst.setInt(1, receiver);    
            rs = pst.executeQuery();
            if (rs.next()) {
                receiver_token = rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);     
        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
		String userMessage = "{'help':{'helpId':'"+helpId+"','name':'"+name+"','longitude':'"+longitude+
				"','latitude':'"+latitude+"','info':'"+info+"'}}";
		notifyUser(receiver_token,userMessage);
	}
	/** 
	 * This method opens a connection with the database.
	 */
	private void openDataBaseConnection(){
		con = null;
        pst = null;
        rs = null;
        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");
        drivers = props.getProperty("db.drivers");
        apikey = props.getProperty("gcm.apikey");
        props.setProperty("user",user);
        props.setProperty("password",password);
        props.setProperty("drivers", drivers);
	}
	/**
	 * This method closes a connection with the database.
	 */
	private void closeDataBaseConnection(){
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
	/**
	 * This method cancels a request for one device, removing this from the available receivers list
	 * and executing again the method startHelp in order to select another receiver and continue the 
	 * help request.
	 * 
	 * @param receiver_id This is the receiver's Google device token.
	 * @return String This return the value returned by startHelp method.
	 */
	public String cancel(String receiver_id){
		java.util.Date today = new java.util.Date();
        Timestamp date = new java.sql.Timestamp(today.getTime());
        Log entry = new Log(receiver_id, date, "cancel");
        log_devices.add(entry);
        accepted = false;
		return this.startHelp();	
	}
	/**
	 * This method finishes the request from the sender side. All the information got from the help request
	 * is stored in the database.
	 * 
	 * @param sender_id This is the sender's Google device token.
	 * @return String This return a flag with information about the success or fail of the method.
	 * @exception Exception Every exception is caught in order to inform about every execution problem.
	 */
	public String doneSender(String sender_id){
		if(sender_id.equals(sender_device)){
			accepted = false;
			try{
				java.util.Date today = new java.util.Date();
		        Timestamp date = new java.sql.Timestamp(today.getTime());
		        Log entry = new Log(sender_id, date, "done sender");
		        log_devices.add(entry);
		        String userMessage = "{'help':{'helpId':'"+helpId+"','status':'Done.'}}";
		        return storeInfo();
			}
			catch(Exception ex){
				Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
	            lgr.log(Level.SEVERE, ex.getMessage(), ex);
	            return "fail";
			}	
		}
		return null;
	}
	/** 
	 * This method finishes the request from the receiver side. A message is sent to the sender asking 
	 * about this situation.
	 * 
	 * @param receiver_id This is the receiver's Google device token.
	 * @exception Exception Every exception is caught in order to inform about every execution problem.
	 */
	public String doneReceiver(String receiver_id){
		try{
			java.util.Date today = new java.util.Date();
	        Timestamp date = new java.sql.Timestamp(today.getTime());
	        Log entry = new Log(receiver_id, date, "done receiver");
	        //log_devices.add(entry);
	        String userMessage = "{'help':{'helpId':'"+helpId+"','status':'Done.'}}";
	        notifyUser(sender_device,userMessage);
	        return storeInfo();
		}
		catch(Exception ex){
			Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return "fail";
		}	
	}
	/**
	 * This method informs to sender that request is accepted by some receiver.
	 * 
	 * @param receiver_id This is the receiver's Google device token,
	 * @return String This return a flag with information about the success or fail of the method.
	 */
	public String accept(String receiver_id){
		accepted = true;
		try{
			java.util.Date today = new java.util.Date();
	        Timestamp date = new java.sql.Timestamp(today.getTime());
	        Log entry = new Log(receiver_id, date, "accept");
	        log_devices.add(entry);
	        String userMessage = "{'help':{'helpId':'"+helpId+"','status':'Accepted.'}}";
	        notifyUser(sender_device,userMessage);
			return "success";	
		}
		catch(Exception ex){
			Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return "fail";
		}
	}
	/**
	 * This method cancels a request for one device that has never accept it, removing this from the 
	 * available receivers list and executing again the method startHelp in order to select another 
	 * receiver and continue the help request.
	 * 
	 * @param receiver_id This is the receiver's Google device token.
	 * @return String This return the value returned by startHelp method.
	 */
	public String reject(String receiver_id){
		java.util.Date today = new java.util.Date();
        Timestamp date = new java.sql.Timestamp(today.getTime());
        Log entry = new Log(receiver_id, date, "reject");
		log_devices.add(entry);
		return this.startHelp();	
	}
	/**
	 * Class Log implements all the methods needed to create a log object that contains every change in a help request.
	 * @author  Alfonso Cambero Lorenzo
	 * @version  1.0
	 * @since  2014-02-01
	 */
	public class Log{
		/**
		 * @uml.property  name="device"
		 */
		private String device;
		/**
		 * @uml.property  name="date"
		 */
		private Timestamp date;
		/**
		 * @uml.property  name="reason"
		 */
		private String reason;
		/**
		 * The constructor Log creates a new Log object that contains all the information needed to create a 
		 * log about a help request.
		 * 
		 * @param device This is the Google device token that create the log object.
		 * @param date This is the date and hour when the log is created.
		 * @param reason This is the identification of the action.
		 */
		public Log(String device, Timestamp date, String reason){
			this.device = device;
			this.date = date;
			this.reason = reason;
		}
		/**
		 * This method is a getter, which returns the device that creates the log object.
		 * @return  String This returns the device that created the log.
		 * @uml.property  name="device"
		 */
		public String getDevice(){
			return this.device;
		}
		/**
		 * This method is a setter, which change the log's device for another given as a parameter.
		 * @param device  This is the new device.
		 * @uml.property  name="device"
		 */
		public void setDevice(String device){
			this.device = device;
		}
		/**
		 * This method is a getter, which returns the date and hour when the log was created.
		 * @return  Timestamp This returns the hour when the log was created.
		 * @uml.property  name="date"
		 */
		public Timestamp getDate(){
			return this.date;
		}
		/**
		 * This method is a setter, which change the log's date for another given as a parameter.
		 * @param date  This is the new hour.
		 * @uml.property  name="date"
		 */
		public void setDate(Timestamp date){
			this.date = date;
		}
		/**
		 * This method is a getter, which returns the action identification.
		 * @return  String This returns the action identification.
		 * @uml.property  name="reason"
		 */
		public String getReason(){
			return this.reason;
		}
		/**
		 * This method is a setter, which change the log's identification for another given as a parameter.
		 * @param device  This is the new action identification.
		 * @uml.property  name="reason"
		 */
		public void setReason(String reason){
			this.reason = reason;
		}
	}
	/**
	 * This method inform to receiver about the time used to something is over and the request will be transfered to another user.
	 * 
	 * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
	 */
	public void timeOut(){
		String receiver_token = "";
		try {
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query_device = "SELECT (device) FROM receivers WHERE (id) = (?)";
            pst = con.prepareStatement(query_device);
            pst.setInt(1, receiver_device);    
            rs = pst.executeQuery();
            if (rs.next()) {
                receiver_token = rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);     
        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
		java.util.Date today = new java.util.Date();
        Timestamp date = new java.sql.Timestamp(today.getTime());
        Log entry = new Log(receiver_token, date, "timeOut");
		log_devices.add(entry);
		String userMessage = "timeOut";
		notifyUser(receiver_token,userMessage);
	}
	/**
	 * This method cancels a request from the sender side, sending a message to the receiver informing about this change.
	 * 
	 * @return String This return the value returned by storeInfo method
	 * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
	 */
	public String cancelSender(){
		String receiver_token = null;
		try {
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query_device = "SELECT (device) FROM receivers WHERE (id) = (?)";
            pst = con.prepareStatement(query_device);
            pst.setInt(1, receiver_device);    
            rs = pst.executeQuery();
            if (rs.next()) {
                receiver_token = rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);     
        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
		String userMessage = "cancel";
		notifyUser(receiver_token,userMessage);
		return storeInfo();
	}
	/**
	 * This method sends messages to user through Google Cloud Messaging system.
	 * 
	 * @param device This is the device that has to receive the message.
	 * @param userMessage This is the message.
	 * @exception IOException This exception is generated when some reading or writing problem happens.
	 * @exception Exception Every exception is caught in order to inform about every execution problem.
	 */
	private void notifyUser(String device, String userMessage){
		try {
			Sender sender = new Sender(apikey);
			Message message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).addData("msg", userMessage).build();
			sender.send(message, device, 1);
		} 
		catch (IOException ex) {
			Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);  
		} catch (Exception ex) {
			Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);  
		}
	}
	/**
	 * This method is a getter, which return the sender's picture as String that will be converted in a bitmap in the mobile app.
	 * 
	 * @param id This is the receiver's database id, used as a security barrier, giving only the information to the allowed device.
	 * @return String This returns the picture data.
	 * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
	 */
	public String getPicture(String id){
		String receiver_token = null;
		try {
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query_device = "SELECT (device) FROM receivers WHERE (id) = (?)";
            pst = con.prepareStatement(query_device);
            pst.setInt(1, receiver_device);    
            rs = pst.executeQuery();
            if (rs.next()) {
                receiver_token = rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);     
        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
		if(id.equals(receiver_token))
			return picture;
		else
			return null;
	}
	/**
	 * This method is used to store every log objects created during the request in the database as String.
	 * 
	 * @return String This return a flag with information about the success or fail of the method.
	 * @exception SQLException This exception is generated when some problem related with the sql code appears.
	 * @exception ClassNotFoundException This exception is generated when the program is trying to access to a
	 * class, which does not exist.
	 */
	private String storeInfo(){
		//Prepare log from List to String
		String log = "";
		for(int i=0; i<log_devices.size(); i++){
			log += "Date:"+ log_devices.get(i).getDate() + "/Device" + log_devices.get(i).getDevice() +
					"/Action:" + log_devices.get(i).getReason() + ";";
		}
		try {
			//Get receiver token
			String receiver_token = "0";
            Class.forName(drivers);
            con = DriverManager.getConnection(url, props);
            String query = "SELECT (device) FROM receivers WHERE (id) = (?)";
            pst = con.prepareStatement(query);
            pst.setInt(1, receiver_device);    
            rs = pst.executeQuery();
            if (rs.next()) {
                receiver_token = rs.getString(1);
            }
            //Store the data
            query = "INSERT INTO alerts_history (sender,sender_longitude,sender_latitude,receiver,log) VALUES (?,?,?,?,?)";     
            pst = con.prepareStatement(query);
            pst.setString(1, sender_device);    
            pst.setDouble(2, longitude);    
            pst.setDouble(3, latitude);    
            pst.setString(4, receiver_token);    
            pst.setString(5, log);  
            pst.executeUpdate();
            
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);    
            return "fail";
        } catch (ClassNotFoundException ex) {
        	Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return "fail";
		}
		return "success";
	}
	/**
	 * Class Timer implements all the methods needed to create Runnable object in order to control times during the request.
	 * 
	 * @author Alfonso Cambero Lorenzo
	 * @version 1.0
	 * @since 2014-02-01
	 */
	public class Timer implements Runnable{
		/**
		 * This method creates a thread that will be sleeping for some time. After this time is waked up. If the request is not
		 * accepted, timeOut and startHelp are executed.
		 * 
		 * @exception InterruptedException This exception is generated when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted,
		 * either before or during the activity.
		 */
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				Thread.sleep(60*1000);
				if(!accepted){
					timeOut();
					startHelp();
				}
			}
			catch(InterruptedException ex){
				Logger lgr = Logger.getLogger(GetLocationServlet.class.getName());
	            lgr.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}
}
