import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		String username = req.getParameter("username");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			String sql = "SELECT chatName,username,sender,text,date,img FROM chaty.msg_history where username=?";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, username);
			ResultSet rs = stat.executeQuery();
			while(rs.next())
			{
				byte[] img = rs.getBytes(6);
				String imgString = Arrays.toString(img);
				
				res.getOutputStream().println(rs.getString(1)+","+rs.getString(2)+","+rs.getString(3)+","+rs.getString(4)+
						","+rs.getString(5)+","+imgString);
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
}
