package pack;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import cnt.Constant;
import dao.RamenDao;
import dto.RamenDto;

/**
 * Servlet implementation class RamenInsert
 */
@WebServlet("/RamenInsert")
@MultipartConfig
public class RamenInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RamenInsert() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//取得文字のエンコード設定(文字化け防止)
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");		
		
		// リクエストパラメータを受け取り、DTOに格納する。
		RamenDto dto = new RamenDto();
		
		dto.setShopName(request.getParameter("name"));		// 店舗名
		dto.setGenreid(Integer.parseInt(request.getParameter("genre")));	// ジャンル
		dto.setArea(request.getParameter("area"));							// エリア
		dto.setValue(Integer.parseInt(request.getParameter("value")));		// 評価
		// 開店時間、閉店時間はtime型のため、String型のinputOpentime, inputClosetimeに格納する。
		dto.setInputOpentime(request.getParameter("opentime"));				// 開店時間
		dto.setInputClosetime(request.getParameter("closetime"));			// 閉店時間
		// オープン日はTimestamp型のため、String型のinputOpendayに格納する。
		dto.setInputOpenday(request.getParameter("openday"));				// オープン日
		String filename = request.getParameter("uploadfile");				// ファイル名
		if(filename != null) {
			dto.setFilename(filename);	// ファイル名をDTOへ格納する
		}
		
		// ファイルアップロード処理を行う
		Part part = request.getPart("uploadfile");	// 送信元ファイル名(フルパス)を取得する。
		
		/* レスポンスヘッダのContent-dispositionは、content-disposition: form-data;
		 * name="uploadfile"; filename="C\\Users\\user01\\Desktop\\photo01.jpg" 
		 * のようになっている。
		 * */
		
		for(String cd: part.getHeader("content-disposition").split(";")) {	// 区切りを分離して取り出す
			cd = cd.trim(); // 余分な空白を取り除く
			if(cd.startsWith("filename")) {	// filenameで始まっていた場合
				// filename=以後の部分について空白を除去し、"を除去し、\\を/に置き換える。
				filename = cd.substring(cd.indexOf("=") + 1).trim().replace("\"", "").replace("\\", "/");
				int pos = filename.lastIndexOf("/");	// 最後の"/"の位置を取得する。
				if(pos >= 0) {
					filename = filename.substring(pos + 1);	// (パスなしの)ファイル名を取得する。
				}
				
				if(filename.length() != 0) {
					String path = Constant.UPLOAD_THUMBNAIL_PATH;
					part.write(path + filename);	// アップロードされたデータの書き込みを実行
					dto.setFilename(filename);	// ファイル名をDTOへ格納する
					break;
				}
			}
		}
	
		try(RamenDao dao = new RamenDao()) {

			String sql;
			
			// オープン日が入力済みか否かでSQLの組み立て方法を変える
			if(dto.getInputOpenday() == null || dto.getInputOpenday().isEmpty()) {
			
				sql = "INSERT INTO t_shop (shop_name, genre_id, area, open_time,"
					 + " close_time, value_id, date_create, date_update, pict) VALUES ('"
					 + dto.getShopName() + "', " + dto.getGenreid() + ", '" + dto.getArea() + "', '" + dto.getInputOpentime() + "', '" + dto.getInputClosetime() + "', " + dto.getValue()
					 + ", now(), now() ,'" + dto.getFilename() + "')";

			} else {
				sql = "INSERT INTO t_shop (shop_name, genre_id, area, open_day, open_time,"
					+ " close_time, value_id, date_create, date_update, pict) VALUES ('"
					+ dto.getShopName() + "', " + dto.getGenreid() + ", '" + dto.getArea() + "', '" + dto.getInputOpenday()
					+ "', '" + dto.getInputOpentime() + "', '" + dto.getInputClosetime() + "', " + dto.getValue()
					+ ", now(), now() ,'" + dto.getFilename() + "')";
			}
			
			dao.execUpdateSql(sql);	// t_shopへラーメン店情報の挿入処理を実行
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		//@ServletContextは共有データ
		ServletContext context = this.getServletContext();		
		
		// ラーメン情報登録画面に戻る(DspRamenInsert → Ramenlist.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/DspRamenInsert");
		dispatcher.forward(request, response);
	}

}
