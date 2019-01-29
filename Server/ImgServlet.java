import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImgServlet extends HttpServlet {

	final String url = "jdbc:mysql://localhost:3306/chaty?useLegacyDatetimeCode=false&serverTimezone=UTC";
	final String dbusername = "root";
	final String dbpassword = "booboo11";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		int msgID=-1,historyId=-1;
		try
		{
			msgID = Integer.parseInt(req.getParameter("msgID"));
			historyId = Integer.parseInt(req.getParameter("historyID"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		if(msgID!=-1 && historyId!=-1)
		{
			String img = req.getParameter("img");
			String[] byteValues = img.substring(1, img.length() - 1).split(",");
			byte[] imgByte = new byte[byteValues.length];
			
			for (int i=0, len=imgByte.length; i<len; i++)
				imgByte[i] = Byte.parseByte(byteValues[i].trim());     
			
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(url, dbusername, dbpassword);
				String sql = "UPDATE chaty.messages SET img = ? WHERE idMessages=?";
				PreparedStatement stat = connection.prepareStatement(sql);
				stat.setBytes(1, imgByte);
				stat.setInt(2, msgID);
				stat.executeUpdate();
				
				sql = "UPDATE chaty.msg_history SET img = ? WHERE msgID=?";
				stat = connection.prepareStatement(sql);
				stat.setBytes(1, imgByte);
				stat.setInt(2, historyId);
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
