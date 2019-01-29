import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NameCheck extends HttpServlet {
	
	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String username = req.getParameter("username");	
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			String sql = "SELECT username from chaty.users where username= ?";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, username);
			System.out.println(stat.toString());
			ResultSet rs = stat.executeQuery();
			if(rs.next())
			{
				res.getOutputStream().println("Username in use!");
				System.out.println("Username in use!");
			}
			else
			{
				res.getOutputStream().println("Username is free!");
				System.out.println("Username is free to use!");
			}
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
