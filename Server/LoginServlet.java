import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet 
{
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
	{	
		
		String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
		String dbUsername = "root";
		String dbPassword = "booboo11";
		String username = req.getParameter("username");
		String password = req.getParameter("password");

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
			
			String sql = "select username,password,loggedIn from users where username= ? and password= ?";
			PreparedStatement stmt= connection.prepareStatement(sql);  
			stmt.setString(1, username);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next())
			{
				if(Integer.parseInt(rs.getString(3))==0)
				{
					System.out.println(username+" Connected!");
					res.getOutputStream().println(rs.getString(1)+","+rs.getString(2));
					sql = "UPDATE chaty.users SET loggedIn = 1 WHERE userName=?";
					stmt = connection.prepareStatement(sql);
					stmt.setString(1, username);
					stmt.executeUpdate();
				}
				else
				{
					res.getOutputStream().println("User is already Connected!");
					System.out.println("User is already Connected!");
				}
			}
			else
			{
				System.out.println("User Don't exsist!");
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
}
