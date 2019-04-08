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
			long imgdate = Long.parseLong(req.getParameter("dateImg"));
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
				String sql = "SELECT imgDate from chaty.users where username= ?";
				PreparedStatement stat = connection.prepareStatement(sql);
				stat.setString(1, username);
				ResultSet rs = stat.executeQuery();
				if(rs.next())
					if(rs.getString(1)!=null)
						if(Long.parseLong(rs.getString(1))!=imgdate)
						{
							sql = "SELECT status,imgDate,image from chaty.users where username= ?";
							stat = connection.prepareStatement(sql);
							stat.setString(1, username);
							rs = stat.executeQuery();
							if(rs.next())
							{
								res.getOutputStream().println(rs.getString(1)+","+rs.getString(2)+","+rs.getString(3));
								System.out.println("Profile Sent!");
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
					long date = Long.parseLong(req.getParameter("imgDate"));
					try {
						Class.forName("com.mysql.cj.jdbc.Driver");
						Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
						String sql = "UPDATE chaty.users SET Image = ?, imgDate= ? WHERE username=?";
						PreparedStatement stat = connection.prepareStatement(sql);
						stat.setString(1, img);
						stat.setLong(2, date);
						stat.setString(3, username);
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
