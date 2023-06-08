/**
 * <P>タイトル : ラーメン検索システム　ログインDAO</P>
 * <P>説明 : ログインDAO</P>
 * @author  W_Kaneko
 * @version 1.0 2023.06.06
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cnt.Constant;
import dto.RamenDto;

public class RamenDao implements AutoCloseable {
	
    public Connection conn = null;
    public Statement stmt = null;
    public ResultSet rs = null;
	
	public RamenDao() {}	
	
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
	
	public List<RamenDto> getRamenList(RamenDto dto) throws Exception{
		
		connectDB();	// DB接続
		
		String sql = "SELECT s.shop_id , s.shop_name , g.genre_name , s.area , s.open_day ,"
				+ " s.open_time , s.close_time , v.value , s.date_create , s.date_update,"
				+ "  s.pict  FROM t_shop AS s LEFT OUTER JOIN m_genre AS g ON s.genre_id = g.genre_id"
				+ " LEFT OUTER JOIN m_value AS v ON s.value_id = v.value_id";

		List<RamenDto> resultList = new ArrayList<RamenDto>();
		
		
		// 検索条件が指定されている場合
		if(dto != null) {
			boolean andFlag = false;
			/* 検索条件に店舗名、エリア、ジャンル、評価、営業時間、オープン日のいずれかが指定されていれば
			 * " WHERE"	をセット
			*/ 
			if(	
				(dto.getShopName() != null && !dto.getShopName().isEmpty() ) ||
				(dto.getArea() != null && !dto.getArea().isEmpty() ) ||
				dto.getGenreid() != 0 || dto.getValue() != 0  ||
				(dto.getInputOpentime() != null && !dto.getInputOpentime().isEmpty() ) ||
				(dto.getInputOpendayStart() != null && !dto.getInputOpendayStart().isEmpty() ) ||
				(dto.getInputOpendayEnd() != null && !dto.getInputOpendayEnd().isEmpty() )
				) {
				sql += " WHERE";
			}
			
			// 店舗名が指定されていればLIKE '%店名%'をセット
			if(dto.getShopName() != null && !dto.getShopName().isEmpty()) {
				sql += " s.shop_name LIKE '%" + dto.getShopName() + "%'";
				andFlag = true;
			}
			
			// エリアが指定されていればLIKE '%エリア%'をセット
			if(dto.getArea() != null && !dto.getArea().isEmpty()) {
				if(andFlag == true) {
					sql += " AND";
				}
				sql += " s.area LIKE '%" + dto.getArea() + "%'";
				andFlag = true;
			}
			
			// ジャンル(ID)が指定されていれば" = ジャンルID"をセット
			if(dto.getGenreid() != 0) {
				if(andFlag == true) {
					sql += " AND";
				}
				sql += " s.genre_id = " + dto.getGenreid();
				andFlag = true;
			}
			
			// 評価(1～5)が指定されていれば" =(<=, >=) 評価"をセット
			if(dto.getValue() != 0) {
				if(andFlag == true) {
					sql += " AND";
				}
				sql += " s.value_id " + dto.getEquality() + " " + dto.getValue();
				andFlag = true;
			}
			
			// 営業時間が指定されていれば営業時間をセット
			// 営業時間が日付をまたぐか否かで検索条件を変更
			if(dto.getInputOpentime() != null && !dto.getInputOpentime().isEmpty()) {
				if(andFlag == true) {
					sql += " AND";
				}
				sql += " CASE WHEN (s.open_time <= s.close_time)"
					+ " THEN (s.open_time <= '" + dto.getInputOpentime() + "' AND '" + dto.getInputOpentime() + "' < s.close_time)"
					+ " ELSE ('" + dto.getInputOpentime() + "' < s.close_time OR s.open_time <= '" + dto.getInputOpentime() + "') END";
				andFlag = true;
			}
			
			// オープン日(検索開始、終了日いずれか)が指定されている場合
			if((dto.getInputOpendayStart() != null && !dto.getInputOpendayStart().isEmpty() ) ||
					(dto.getInputOpendayEnd() != null && !dto.getInputOpendayEnd().isEmpty() )) {
				if(andFlag == true) {
					sql += " AND";
				}
				// オープン日の検索開始日と終了日両方が指定されている場合
				if((dto.getInputOpendayStart() != null && !dto.getInputOpendayStart().isEmpty() ) &&
						(dto.getInputOpendayEnd() != null && !dto.getInputOpendayEnd().isEmpty() )) {
					sql += " s.open_day BETWEEN '" + dto.getInputOpendayStart() + "' AND '" + dto.getInputOpendayEnd() + "'";
				}
				
				// オープン日の検索開始日のみ指定されている場合
				if((dto.getInputOpendayStart() != null && !dto.getInputOpendayStart().isEmpty() ) &&
						(dto.getInputOpendayEnd() == null || dto.getInputOpendayEnd().isEmpty() )) {
					sql += " s.open_day >= '" + dto.getInputOpendayStart() + "'";
				}
				
				// オープン日の検索終了日のみ指定されている場合
				if((dto.getInputOpendayStart() == null || dto.getInputOpendayStart().isEmpty() ) &&
						(dto.getInputOpendayEnd() != null && !dto.getInputOpendayEnd().isEmpty() )) {
					sql += " s.open_day <= '" + dto.getInputOpendayEnd() + "'";
				}
			}
		}
		
		sql += " ORDER BY s.shop_id";
		
		try {
			// SELECT文実行
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				RamenDto dto2 = new RamenDto();
				
				dto2.setShopid(rs.getInt("shop_id"));
				dto2.setShopName(rs.getString("shop_name"));
				dto2.setGenreName(rs.getString("genre_name"));
				dto2.setArea(rs.getString("area"));
				dto2.setOpenday(rs.getTimestamp("open_day"));
				dto2.setOpentime(rs.getTime("open_time"));
				dto2.setClosetime(rs.getTime("close_time"));
				dto2.setValueLabel(rs.getString("value"));
				dto2.setRegisterdate(rs.getTimestamp("date_create"));
				dto2.setLastupdate(rs.getTimestamp("date_update"));
				dto2.setFilename(rs.getString("pict"));
				
				resultList.add(dto2);
			}
			rs.close();
			
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();

			// ラーメン店情報一覧取得で異常終了
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
		
		return resultList;
	}
	
	// 指定した店舗コードのラーメン情報を取得
	public RamenDto getRamenDetail(int shopid) throws Exception {
		
		connectDB();	// DB接続
		
		String sql = "SELECT s.shop_id , s.shop_name , g.genre_name , s.area , s.open_day ,"
				+ " s.open_time , s.close_time , v.value , s.date_create , s.date_update,"
				+ "  s.pict  FROM t_shop AS s LEFT OUTER JOIN m_genre AS g ON s.genre_id = g.genre_id"
				+ " LEFT OUTER JOIN m_value AS v ON s.value_id = v.value_id"
				+ " WHERE s.shop_id = " + shopid;
		
		RamenDto dto = new RamenDto();
		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				
				dto.setShopid(rs.getInt("shop_id"));
				dto.setShopName(rs.getString("shop_name"));
				dto.setGenreName(rs.getString("genre_name"));
				dto.setArea(rs.getString("area"));
				dto.setOpenday(rs.getTimestamp("open_day"));
				dto.setOpentime(rs.getTime("open_time"));
				dto.setClosetime(rs.getTime("close_time"));
				dto.setValueLabel(rs.getString("value"));
				dto.setRegisterdate(rs.getTimestamp("date_create"));
				dto.setLastupdate(rs.getTimestamp("date_update"));
				dto.setFilename(rs.getString("pict"));
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
	

	
	// ラーメン口コミ一覧を取得
	public List<RamenDto> getRamenReviewList(RamenDto dto) throws Exception {
		
		connectDB();	// DB接続
		
		String sql = "SELECT r.review_id, r.user_id, v.value, r.review_title, r.review, r.date_create, r.date_update, r.date_visit"
				+ " FROM t_review AS r LEFT OUTER JOIN m_value AS v ON r.value_id = v.value_id"
				+ " WHERE r.shop_id = " + dto.getShopid();

		sql += " ORDER BY r.date_update desc";

		List<RamenDto> resultList = new ArrayList<RamenDto>();
		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				RamenDto dto2 = new RamenDto();
				
				dto2.setReviewid(rs.getInt("review_id"));				// レビューID
				dto2.setUserid(rs.getString("user_id"));				// ユーザーID
				dto2.setValueLabel(rs.getString("value"));				// 評価
				dto2.setReviewTitle(rs.getString("review_title"));		// 口コミタイトル
				
				dto2.setReview(rs.getString("review"));					// 口コミ本文。
				dto2.setReviewBr(nl2br(dto2.getReview()));				//	口コミ本文。改行コードを<br>タグに変換。
				dto2.setRegisterdate(rs.getTimestamp("date_create"));	// 口コミ投稿日
				dto2.setLastupdate(rs.getTimestamp("date_update"));		// 口コミ更新日
				dto2.setVisitday(rs.getTimestamp("date_visit"));		// 来訪日		
				
				resultList.add(dto2);
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
		
		return resultList;
	}
	
	// 特定のレビューIDの口コミ情報を取得
	public RamenDto getRamenReview(int reviewid) throws Exception {
		
		connectDB();	// DB接続
		
		String sql = "SELECT r.review_id, r.user_id, v.value, r.review_title, r.review, r.date_create, r.date_update, r.date_visit"
				+ " FROM t_review AS r LEFT OUTER JOIN m_value AS v ON r.value_id = v.value_id"
				+ " WHERE r.review_id = " + reviewid;
		
		RamenDto dto = new RamenDto();
		
		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				
				dto.setReviewid(rs.getInt("review_id"));				// レビューID
				dto.setUserid(rs.getString("user_id"));				// ユーザーID
				dto.setValueLabel(rs.getString("value"));				// 評価
				dto.setReviewTitle(rs.getString("review_title"));		// レビュータイトル
				
				dto.setReview(rs.getString("review"));					// レビュー本文。
				
				dto.setRegisterdate(rs.getTimestamp("date_create"));	// レビュー投稿日
				dto.setLastupdate(rs.getTimestamp("date_update"));		// レビュー更新日
				dto.setVisitday(rs.getTimestamp("date_visit"));		// 来訪日

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

	// 該当の店舗及びユーザーでレビューが投稿済みかを確認
	public int getReviewCount(RamenDto dto) throws Exception {

		connectDB();	// DB接続
		
		String sql = "SELECT count(*) as review_count FROM t_review WHERE shop_id = "
					+ dto.getShopid() + " AND user_id = '" + dto.getUserid() + "'";
		
		int count = 0;

		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				count = rs.getInt("review_count");	// レビュー件数

			}
			rs.close();
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();

			//ログイン処理で異常終了
			return 0;
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
		
		return count;
	}
	
	// レビューIDを取得。店舗コードとユーザーIDが一致したレコードが対象
	// 1ユーザーにつき各店舗1件まで口コミ投稿が可能な仕様とする。
	public void getReviewid(RamenDto dto) throws Exception {
		
		connectDB();	// DB接続
		
		String sql = "SELECT review_id FROM t_review WHERE shop_id = " + dto.getShopid() 
					+ " AND user_id = '" + dto.getUserid() + "'";
		
		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				dto.setReviewid(rs.getInt("review_id"));				// レビューID
			}
			rs.close();
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();
			
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
	}
	

	// 特定の口コミ投稿時にアップロードした画像ファイル名一覧を取得
	public List<String> getImageList(int reviewid) throws Exception{
			
		connectDB();	// DB接続
			
		String sql = "SELECT filename from t_image WHERE review_id = " + reviewid;

		List<String> list = new ArrayList<String>();
			
		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				String filename = rs.getString("filename");				// レビューID
				list.add(filename);
			}
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();
			
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
		
		return list;
	}

	// 特定の店舗の全ての口コミに紐づいた画像ファイル名一覧を取得
	public List<String> getAllImageList(int shopid) throws Exception{
		
		connectDB();
			
		String sql = "SELECT i.filename FROM t_image AS i LEFT OUTER JOIN t_review AS r"
					+ " ON i.review_id = r.review_id WHERE r.shop_id = " + shopid;

		List<String> list = new ArrayList<String>();
			
		try {
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				String filename = rs.getString("filename");				// ファイル名
				list.add(filename);
			}
		} catch(Exception e) {
			//Statementをクローズする
			stmt.close();
			//コネクションをクローズする
			conn.close();
			
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
		
		return list;
	}

	// 更新系(INSERT, UPDATE, DELETE)SQLは全てこのメソッドで実施
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

	/**
	 * 改行コードを<br />タグに変換した情報を返却する。<br>
	 * @param s 入力文字列
	 * @return 変換後の文字列を返却します。
	 */
	public String nl2br(String s) {
	    return nl2br(s, true);
	}

	/**
	 * 改行コードを<br />、または、<br>タグに変換した情報を返却する。<br>
	 * @param s 入力文字列
	 * @param is_xhtml XHTML準拠の改行タグの使用する場合はtrueを指定します。
	 * @return 変換後の文字列を返却します。
	 */
	public String nl2br(String s, boolean is_xhtml) {
	    if (s == null || "".equals(s)) {
	        return "";
	    }
	    String tag = is_xhtml ? "<br />" : "<br>";
	    return s.replaceAll("\\r\\n|\\n\\r|\\n|\r", tag);
	}
}
