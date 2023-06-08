package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cnt.Constant;
import dto.LoginDto;

public class LoginDao implements AutoCloseable {

    public Connection conn = null;
    public Statement stmt = null;
    public ResultSet rs = null;
	
	public LoginDao() {}	
	
	// DB接続(全DAOで共通)
	public void connectDB() {
		// JDBCドライバの読み込み
		try {
			// postgresSQLのJDBCドライバを読みこみ
			Class.forName(Constant.JDBC_DRIVER);
		} catch(ClassNotFoundException e) {
			// JDBCドライバが見つからない場合
			e.printStackTrace();
		}
		
		try {
	    	// postgre接続
	    	conn = DriverManager.getConnection(Constant.JDBC_CONNECTION, 											Constant.JDBC_USER, Constant.JDBC_PASS);
	    	// 自動コミット機能を無効にする
	    	conn.setAutoCommit(false);
	    	//SELECT文をターゲットにする時に必要構文
	    	stmt = conn.createStatement();
	    } catch(Exception e) {
			e.printStackTrace();
	    }
	}
	
	// ログイン時にユーザーID、パスワードが一致しているかを確認
	public LoginDto getLoginInfo(String sql) throws Exception {
		connectDB();
		
		LoginDto dto = null;
		
		try {
			// ユーザー情報テーブルからログイン時のユーザーID、パスワードに一致するレコードを検出
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				// レコードを検出した場合のみインスタンス化
				dto = new LoginDto(rs.getString("user_id"), rs.getString("password"),
												rs.getString("username"), rs.getString("email"));
			}
			
			rs.close();
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();

			//ログイン処理で異常終了
			return null;
		} finally {
			try{
	    		if (stmt != null){
	    			//Statementをクローズする
	    			stmt.close();
	    		}
	    		if (conn != null){
	    			//コネクションをクローズする
	    			conn.close();
	    		}
	    	} catch(SQLException e) {
				//Statementをクローズする
				stmt.close();
				//コネクションをクローズする
    			conn.close();
	    	}	
		}
		
		return dto;
	}
	
	// ユーザーIDが登録済みか確認
	public boolean isUserResisterd(LoginDto dto) throws Exception  {
		connectDB();
		
		String sql = "SELECT count(*) as user_count FROM m_user WHERE user_id = '" + dto.getUserid() + "'";
		int count = 0;
		try {
			// ユーザー情報テーブルからログイン時のユーザーID、パスワードに一致するレコードを検出
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				count = rs.getInt("user_count");
			}
			
			rs.close();
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();

			//SQL実行で異常終了
			return false;
		} finally {
			try{
	    		if (stmt != null){
	    			//Statementをクローズする
	    			stmt.close();
	    		}
	    		if (conn != null){
	    			//コネクションをクローズする
	    			conn.close();
	    		}
	    	} catch(SQLException e) {
				//Statementをクローズする
				stmt.close();
				//コネクションをクローズする
    			conn.close();
	    	}	
		}
		
		if(count > 0) {
			return true;
		} else {
			return false;
		}
	}

	// 更新系(INSERT, UPDATE, DELETE)SQLは全てこのメソッドで実施
	// ユーザーの登録・削除、パスワードの変更などに利用
	public int execUpdateSql(String sql) throws Exception {

		connectDB();	// DB接続

		int result = 0;
		try {
			result = stmt.executeUpdate(sql);
			// 処理をコミットする
			conn.commit();
		} catch(Exception e) {
			// 処理をロールバックする
			conn.rollback();
			
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();

			return result;
		} finally {
			try{
				if (stmt != null){
					//Statementをクローズする
					stmt.close();
				}
				if (conn != null){
					//コネクションをクローズする
					conn.close();
				}
			} catch(SQLException e) {
				//Statementをクローズする
				stmt.close();
				//コネクションをクローズする
				conn.close();
			}
		}
		
		return result;
	}

	@Override
	public void close() throws Exception {

		if(conn != null) {
			conn.close();
			conn = null;
		}
	}
}
