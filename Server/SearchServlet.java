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

public class SearchServlet extends HttpServlet
{
	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String para = req.getParameter("para");	
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
			String sql = "SELECT username,email,status,image from chaty.users where username= ?";
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setString(1, para);
			ResultSet rs = stat.executeQuery();
			if(rs.next())
			{	
				res.getOutputStream().println(rs.getString(1)+","+rs.getString(2)+","+rs.getString(3)+","+rs.getString(4));
				System.out.println("Username Found!");
			}
			else
			{
				System.out.println("Username not Found!");
				sql = "SELECT username,email,status,image from chaty.users where email= ?";
				stat.setString(1, para);
				rs = stat.executeQuery();
				if(rs.next())
				{
					res.getOutputStream().println(rs.getString(1)+","+rs.getString(2)+","+rs.getString(3)+","+rs.getString(4));
					System.out.println("Username Found!");
				}
				else
				{
					System.out.println("Email not Found!");
				}
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
