import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProfileServlet extends HttpServlet 
{
	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String username = req.getParameter("username");	
		int changeStatus = Integer.parseInt(req.getParameter("changeStatus"));
		int changeImage = Integer.parseInt(req.getParameter("changeImage"));
		
		if(changeImage==0 && changeStatus==0)
		{
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
				String sql = "SELECT status,image from chaty.users where username= ?";
				PreparedStatement stat = connection.prepareStatement(sql);
				stat.setString(1, username);
				ResultSet rs = stat.executeQuery();
				if(rs.next())
				{
					res.getOutputStream().println(rs.getString(1)+","+rs.getString(2));
					System.out.println("Profile Sent!");
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
		else
			if(changeStatus==1)
			{
				String status = req.getParameter("status");
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
					String sql = "UPDATE chaty.users SET Status = ? WHERE username=?";
					PreparedStatement stat = connection.prepareStatement(sql);
					stat.setString(1, status);
					stat.setString(2, username);
					stat.executeUpdate();
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
				if(changeImage==1)
				{
					String img = req.getParameter("img");
					try {
						Class.forName("com.mysql.cj.jdbc.Driver");
						Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
						String sql = "UPDATE chaty.users SET Image = ? WHERE username=?";
						PreparedStatement stat = connection.prepareStatement(sql);
						stat.setString(1, img);
						stat.setString(2, username);
						stat.executeUpdate();
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
