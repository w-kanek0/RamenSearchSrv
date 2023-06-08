package cnt;

public class Constant {
	/******** ↓キー **********/
	/** データベース接続 **/
	// PostgreSQLの場合
	public static final String JDBC_DRIVER = "org.postgresql.Driver";
	public static final String JDBC_CONNECTION = "jdbc:postgresql://localhost:5432/postgres";
	public static final String JDBC_USER = "postgres";
	public static final String JDBC_PASS = "password";

	// ラーメン店情報登録時のサムネイル画像のアップロード先
	public static final String UPLOAD_THUMBNAIL_PATH = "C:\\workspaceEE\\RamenSearchSrv\\src\\main\\webapp\\upload\\thumbnail\\";
	
	// 口コミ投稿時の画像のアップロード先
	public static final String UPLOAD_REVIEW_PATH = "C:\\workspaceEE\\RamenSearchSrv\\src\\main\\webapp\\upload\\review\\";
	
	// ユーザー登録エラー時のメッセージ
	public static final String USER_ALREADY_REGISTERED = "このユーザーIDは登録済みです。";
	
	// ログインエラー時のメッセージ
	public static final String WRONG_USERID_PASS = "ユーザーIDまたはパスワードが違います。";
}
