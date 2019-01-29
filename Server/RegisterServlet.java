import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RegisterServlet extends HttpServlet 
{
	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
	{
		
		
		String username = req.getParameter("username");
		int nameCheck = Integer.parseInt(req.getParameter("nameCheck"));
		if(nameCheck==0)
		{
			RequestDispatcher rd = req.getRequestDispatcher("NameCheck");
			rd.forward(req, res);
		}
		else
		{
			String password = req.getParameter("password");
			String eMail = req.getParameter("Email");
			
			
			System.out.println("Connecting database...");
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
				System.out.println("Database connected!");
				
				String sql =("INSERT INTO chaty.users (userName, password, eMail,loggedIn) "+
				"SELECT * FROM (SELECT ?, ?, ?,?) AS tmp ");
				
				PreparedStatement stmt= connection.prepareStatement(sql);
				stmt.setString(1, username);
				stmt.setString(2, password);
				stmt.setString(3, eMail);
				stmt.setInt(4, 0);
				stmt.executeUpdate();
				
				
				System.out.println("User Created!");
				connection.close();
				System.out.println("Database disconnected!");
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
