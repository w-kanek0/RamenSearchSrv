package pack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import cnt.Constant;
import dao.RamenDao;
import dto.RamenDto;

/**
 * Servlet implementation class RamenReviewInsert
 */
@WebServlet("/RamenReviewInsert")
@MultipartConfig
public class RamenReviewInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RamenReviewInsert() {
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
		
		HttpSession session = request.getSession();
		
		// リクエストパラメータを受け取り、DTOに格納する。
		RamenDto dto = new RamenDto();
		
		dto.setShopid(Integer.parseInt(request.getParameter("shopid")));		// 店舗コード
		dto.setShopName(request.getParameter("name"));		// 店舗名(画面の表示で使用)
		dto.setUserid(request.getParameter("userid"));							// ユーザーID
		dto.setInputVisitday(request.getParameter("visitday"));							// 来訪日
		dto.setValue(Integer.parseInt(request.getParameter("value")));	// 評価
		dto.setReviewTitle(request.getParameter("reviewtitle"));							// レビュータイトル
		dto.setReview(request.getParameter("review").replace("'", "''"));							// レビュー本文
		
		// ファイルアップロード処理を行う
		List<String> filenames = new ArrayList<String>();
		
		/* レスポンスヘッダのContent-dispositionは、content-disposition: form-data;
		 * name="uploadfile"; filename="C\\Users\\user01\\Desktop\\photo01.jpg" 
		 * のようになっている。
		 * */
		for(Part part: request.getParts()) {
			if (part.getName().equals("uploadfile")) { 
				for(String cd: part.getHeader("content-disposition").split(";")) {	// 区切りを分離して取り出す
					cd = cd.trim(); // 余分な空白を取り除く
					if(cd.startsWith("filename")) {	// filenameで始まっていた場合
						// filename=以後の部分について空白を除去し、"を除去し、\\を/に置き換える。
						String filename = cd.substring(cd.indexOf("=") + 1).trim().replace("\"", "").replace("\\", "/");
						int pos = filename.lastIndexOf("/");	// 最後の"/"の位置を取得する。
						if(pos >= 0) {
							filename = filename.substring(pos + 1);	// (パスなしの)ファイル名を取得する。
						}
				
						if(filename.length() != 0) {
							String path = Constant.UPLOAD_REVIEW_PATH;
							part.write(path + filename);	// アップロードされたデータの書き込みを実行
							filenames.add(filename);	// ファイル名をリストへ格納する
							break;
						}
					}
				}
			}
		}
		dto.setFilenames(filenames);
		
		try(RamenDao dao = new RamenDao()) {
			String sql;
			// 口コミ情報の2重登録防止のため。
			// 口コミ情報を登録した後一覧に遷移するがそこでF5やブラウザの更新ボタンを押したときなど
			if(dao.getReviewCount(dto) == 0) {
				sql = "INSERT INTO t_review (shop_id, user_id, value_id, review_title, review, date_create, date_update, date_visit)"
					+ " VALUES (" + dto.getShopid() + ", '" + dto.getUserid() + "', " + dto.getValue() + ", '" 
					+ dto.getReviewTitle() + "', '" + dto.getReview() + "', now(), now(), '" + dto.getInputVisitday() + "')";
				
				// dao.insertReview(dto);	// 口コミ情報テーブルに口コミ内容を挿入
				dao.execUpdateSql(sql);	// 口コミ情報テーブルに口コミ内容を挿入
			}
			
			// 挿入した口コミ情報のレビューIDを取得。
			dao.getReviewid(dto);
			
			// 画像情報テーブルにアップロードした画像ファイル分を挿入
			for(String filename: dto.getFilenames()) {
				dto.setFilename(filename);
				sql = "INSERT INTO t_image (review_id, filename) VALUES (" + dto.getReviewid() + ", '" + dto.getFilename() + "')";
				dao.execUpdateSql(sql);
			}
			
			request.setAttribute("review", dto);
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		// データ挿入後に再度該当店舗のレビュー一覧を取得。
		// RamenDAOのインスタンス作成
		try(RamenDao dao = new RamenDao()) {
					
			// 設定した店舗コードに該当する店舗名を取得
			// RamenDtoをインスタンス化。
			RamenDto dto2 = dao.getRamenDetail(dto.getShopid());
			// ログイン中のユーザーIDを設定
			String userid = (String)session.getAttribute("userId");
			dto2.setUserid(userid);
			
			// 該当の店舗及びユーザーでレビューが投稿済みかを確認する
			int count = dao.getReviewCount(dto2);
			dto2.setReviewCount(count);
			// 設定した店舗コードに該当する情報(店舗名など)をリクエスト属性格納する。
			request.setAttribute("ramen", dto2);
					
			// 設定した店舗コードに該当するレビュー情報テーブルの全データを取得して、リクエスト属性に格納する。
			List<RamenDto> list = dao.getRamenReviewList(dto2);

			// 設定した店舗コードに該当する全レビュー情報に格納されている画像情報を取得
			if(list != null) {
				for(int i = 0; i < list.size(); i ++) {
					RamenDto dto3 = list.get(i);
					List<String> getFilenames = dao.getImageList(dto3.getReviewid());
					if(getFilenames != null) {
						dto3.setFilenames(getFilenames);
						list.set(i, dto3);
					}
				}
			}
			request.setAttribute("reviewList", list);
					
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		//@ServletContextは共有データ
		ServletContext context = this.getServletContext();	
		
		// ラーメン屋口コミ一覧画面に戻る(RamenReviewlist.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/RamenReviewlist.jsp");
		dispatcher.forward(request, response);
	}

}
