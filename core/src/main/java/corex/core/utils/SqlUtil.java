package corex.core.utils;

import java.sql.*;

/**
 * Who are we?
 * Just a speck of dusk within the galaxy.
 */
public final class SqlUtil {

    public static Connection newConn(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Connection conn = newConn("jdbc:mysql://localhost:3306/test?useSSL=false&allowPublicKeyRetrieval=true", "root", "123456");
        PreparedStatement pstmt = conn.prepareStatement("select * from t_game_node");
        ResultSet rs = pstmt.executeQuery();
        int col = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= col; i++) {
                System.out.print(rs.getString(i) + "\t");
                if ((i == 2) && (rs.getString(i).length() < 8)) {
                    System.out.print("\t");
                }
            }
            System.out.println("");
        }
    }
}
