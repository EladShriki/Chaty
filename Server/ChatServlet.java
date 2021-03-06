import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChatServlet extends HttpServlet
{
	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		int img = Integer.parseInt(req.getParameter("Img"));
		if(img==1)
			sendImg(req,res);
		else
		{
			int send = Integer.parseInt(req.getParameter("Send"));
			if(send==1)//Send Message to DB
				sendMsg(req,res);
			else
				getMsg(req,res);
		}
	}
	
	public void getMsg(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String sender = req.getParameter("username");
		//String reciver = req.getParameter("reciver");
		String id;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			//String sql = "SELECT Sender,text,idMessages,date,Img FROM chaty.messages Where reciver=? And sender=?";
			String sql = "SELECT Sender,text,idMessages,date,Img FROM chaty.messages Where reciver=?";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, sender);
			//stat.setString(2, reciver);
			System.out.println(stat.toString());
			ResultSet rs = stat.executeQuery();
			while(rs.next())
			{
				byte[] img = rs.getBytes(5);
				String imgString = Arrays.toString(img);
				id = rs.getString(3);
				String msg = rs.getString(2);
				msg = msg.replace("\n", "\\n");
				String senderReturn = rs.getString(1);
				String date = rs.getString(4);
				
				
				
				sql = "Insert into chaty.msg_history (sender,text,chatName,username,date,img) values (?,?,?,?,?,?)";
				stat = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				stat.setString(1, rs.getString(1));
				stat.setString(2, msg);
				stat.setString(3, rs.getString(1));
				//stat.setString(3, reciver);
				stat.setString(4, sender);
				stat.setString(5, rs.getString(4));
				stat.setBytes(6, img);
				stat.executeUpdate();
				
				ResultSet GeneratedKeys = stat.getGeneratedKeys();
				if(GeneratedKeys.next())
				{
					res.getOutputStream().println(GeneratedKeys.getLong(1)+","+senderReturn+","+msg+","+date+","+imgString);
					System.out.println("Message Found!");
				}
				
				sql = "DELETE FROM chaty.messages WHERE idMessages=?";
				stat = connection.prepareStatement(sql);
				stat.setString(1, id);
				stat.executeUpdate();
			}
			connection.close();
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (SQLException e) 
		{
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
	}
	
	public void sendMsg(HttpServletRequest req,HttpServletResponse res)
	{
		String sender = req.getParameter("sender");
		String reciver = req.getParameter("reciver");
		String chatName = req.getParameter("chatName");
		String date = req.getParameter("date");
		String msg = req.getParameter("msg");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			String sql = "Insert into chaty.messages (sender,reciver,text,date) values (?,?,?,?)";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, sender);
			stat.setString(2, reciver);
			stat.setString(3, msg);
			stat.setString(4, date);
			stat.executeUpdate();
			
			System.out.println("Msg sent to ->"+reciver+" From ->"+sender);
			sql = "Insert into chaty.msg_history (chatName,username,sender,text,date) values (?,?,?,?,?)";
			stat = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			stat.setString(1, chatName);
			stat.setString(2, sender);
			stat.setString(3, sender);
			stat.setString(4, msg);
			stat.setString(5, date);
			stat.executeUpdate();
			
			ResultSet GeneratedKeys = stat.getGeneratedKeys();
			if(GeneratedKeys.next())
				res.getOutputStream().println(GeneratedKeys.getLong(1));
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (SQLException e) 
		{
		    throw new IllegalStateException("Cannot connect the database!", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendImg(HttpServletRequest req,HttpServletResponse res) throws IOException
	{
		String sender = req.getParameter("sender");
		String reciver = req.getParameter("reciver");
		String chatName = req.getParameter("chatName");
		String date = req.getParameter("date");
		String msg = req.getParameter("msg");
		
		String img = req.getParameter("imgByte");
		String[] byteValues = img.substring(1, img.length() - 1).split(",");
		byte[] imgByte = new byte[byteValues.length];
		
		for (int i=0, len=imgByte.length; i<len; i++)
			imgByte[i] = Byte.parseByte(byteValues[i].trim());
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			String sql = "Insert into chaty.messages (sender,reciver,text,date,Img) values (?,?,?,?,?)";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, sender);
			stat.setString(2, reciver);
			stat.setString(3, msg);
			stat.setString(4, date);
			stat.setBytes(5, imgByte);
			stat.executeUpdate();
			
			System.out.println("Msg sent to ->"+reciver+" From ->"+sender);
			sql = "INSERT INTO chaty.msg_history (chatName,username,sender,text,date,Img) VALUES (?,?,?,?,?,?); ";
			stat = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			stat.setString(1, chatName);
			stat.setString(2, sender);
			stat.setString(3, sender);
			stat.setString(4, msg);
			stat.setString(5, date);
			stat.setBytes(6, imgByte);
			stat.executeUpdate();
			
			ResultSet GeneratedKeys = stat.getGeneratedKeys();
			if(GeneratedKeys.next())
				res.getOutputStream().println(GeneratedKeys.getLong(1));
			
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (SQLException e) 
		{
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
		
	}
}
