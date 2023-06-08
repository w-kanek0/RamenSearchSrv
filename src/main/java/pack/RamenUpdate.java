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
 * Servlet implementation class RamenUpdate
 */
@WebServlet("/RamenUpdate")
@MultipartConfig
public class RamenUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RamenUpdate() {
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
				
		dto.setShopid(Integer.parseInt(request.getParameter("id")));		// 店舗ID
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
			
			boolean emptyOpendayFlag = false;
			boolean emptyFileFlag = false;
			
			if(dto.getInputOpenday() == null || dto.getInputOpenday().isEmpty()) {
				emptyOpendayFlag = true;
			}
			
			
			if(dto.getFilename() == null || dto.getFilename().isEmpty()) {
				emptyFileFlag = true;
			}
			
			// オープン日及びファイル名が入力済みか否かでSQLの組み立て方法を変える
			// オープン日及びファイル名両方が空の場合、オープン日はNULLに変更、ファイル名は変更しない
			if(emptyOpendayFlag == true && emptyFileFlag == true) {
			
				sql = "UPDATE t_shop SET shop_name = '" + dto.getShopName() + "', genre_id = " + dto.getGenreid() + ",  area = '"
					 + dto.getArea() + "', open_day = NULL, open_time = '" + dto.getInputOpentime() + "', close_time = '"
					 + dto.getInputClosetime() + "', value_id = " + dto.getValue() + ", date_update = now()"
					 + " WHERE shop_id = " + dto.getShopid();
			} else if(emptyOpendayFlag == false && emptyFileFlag == true) {	
				// オープン日入力済み及びファイル名が空の場合、オープン日は入力済みのものに変更しファイル名は変更しない
				sql = "UPDATE t_shop SET shop_name = '" + dto.getShopName() + "', genre_id = " + dto.getGenreid() + ",  area = '"
						 + dto.getArea() + "', open_day = '" + dto.getInputOpenday() + "', open_time = '" + dto.getInputOpentime() + "', close_time = '"
						 + dto.getInputClosetime() + "', value_id = " + dto.getValue() + ", date_update = now()"
						 + " WHERE shop_id = " + dto.getShopid();
			} else if(emptyOpendayFlag == true && emptyFileFlag == false) { 
				// オープン日が空で、ファイル名が入力済みの場合、オープン日はNULLに、ファイル名は入力済み
				sql = "UPDATE t_shop SET shop_name = '" + dto.getShopName() + "', genre_id = " + dto.getGenreid() + ",  area = '"
						 + dto.getArea() + "', open_day = NULL, open_time = '" + dto.getInputOpentime() + "', close_time = '"
						 + dto.getInputClosetime() + "', value_id = " + dto.getValue() + ", date_update = now(), pict = '"
						 + dto.getFilename() + "' WHERE shop_id = " + dto.getShopid();
			} else {
				// オープン日及びファイル名両方とも入力済みの場合、オープン日、ファイル名共に入力済みのものに変更する
				sql = "UPDATE t_shop SET shop_name = '" + dto.getShopName() + "', genre_id = " + dto.getGenreid() + ",  area = '"
						 + dto.getArea() + "', open_day = '" + dto.getInputOpenday() + "', open_time = '" + dto.getInputOpentime() + "', close_time = '"
						 + dto.getInputClosetime() + "', value_id = " + dto.getValue() + ", date_update = now(), pict = '"
						 + dto.getFilename() + "' WHERE shop_id = " + dto.getShopid();
			}
			
			// dao.updateRamenInfo(dto);
			dao.execUpdateSql(sql);
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		//@ServletContextは共有データ
		ServletContext context = this.getServletContext();		
		
		// ラーメン情報登録画面に戻る(DspRamenUpdate → Ramenlist.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/DspRamenUpdate");
		dispatcher.forward(request, response);		
	}

}
