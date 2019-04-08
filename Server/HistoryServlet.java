import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HistoryServlet extends HttpServlet
{
	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String getMsgNum = req.getParameter("msgNum");
		String username = req.getParameter("username");
		if(getMsgNum.equals("1"))
		{
			try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			String sql = "SELECT count(username) FROM chaty.msg_history where username=?";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, username);
			ResultSet rs = stat.executeQuery();
			if(rs.next())
				res.getOutputStream().println(rs.getString(1));
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
		else if(getMsgNum.equals("0"))
		{
			String num = "";
			String idList = req.getParameter("idList");
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for(int i=0;i<idList.length();i++)
			{
				if(idList.charAt(i)==',')
				{
					ids.add(Integer.parseInt(num));
					num = "";
				}
				else
				{
					num += idList.charAt(i);
				}
			}
			
			ids.add(Integer.parseInt(num));
			
			StringBuilder builder = new StringBuilder();

			for( int i = 0 ; i < ids.size(); i++ ) {
			    builder.append(",?");
			}

			
			//Integer[] id = ids.toArray(new Integer[ids.size()]);
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
				String sql = "SELECT msgID,chatName,username,sender,text,date,img FROM chaty.msg_history where msgID in("+builder.deleteCharAt(0).toString()+")";
				PreparedStatement stat = connection.prepareStatement(sql);
				
				int index = 1;
				for( Object o : ids ) {
				   stat.setObject(  index++, o ); // or whatever it applies 
				}
				
				ResultSet rs = stat.executeQuery();
				while(rs.next())
				{
					byte[] img = rs.getBytes(7);
					String imgString = Arrays.toString(img);
					
					res.getOutputStream().println(rs.getString(1)+","+rs.getString(2)+","+rs.getString(3)+","+rs.getString(4)+
							","+rs.getString(5)+","+rs.getString(6)+","+imgString);
				}
				System.out.println("History to "+username+" was sent!");
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
		else
		{
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
				String sql = "SELECT msgID FROM chaty.msg_history where username=?";
				PreparedStatement stat = connection.prepareStatement(sql);
				stat.setString(1, username);
				ResultSet rs = stat.executeQuery();
				while(rs.next())
					res.getOutputStream().println(rs.getString(1));
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
}
