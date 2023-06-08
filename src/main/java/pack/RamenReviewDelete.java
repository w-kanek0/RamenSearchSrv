package pack;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cnt.Constant;
import dao.RamenDao;
import dto.RamenDto;

/**
 * Servlet implementation class RamenReviewDelete
 */
@WebServlet("/RamenReviewDelete")
public class RamenReviewDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RamenReviewDelete() {
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
		
		int reviewid = Integer.parseInt(request.getParameter("reviewid"));
		int shopid = Integer.parseInt(request.getParameter("shopid"));
		
		
		try(RamenDao dao = new RamenDao()) {
			
			// 該当レビューIDの画像ファイル情報一覧をを画像テーブルから取得。
			List<String> filenames = dao.getImageList(reviewid);
			if(filenames != null && filenames.size() > 0) {
				for(String filename: filenames) {
					String path = Constant.UPLOAD_REVIEW_PATH;
				
					File file = new File(path + filename);
					// 取得したファイル名をアップロードフォルダから削除
					file.delete();
				}
			}
			
			String sql = "DELETE FROM t_image WHERE review_id =  " + reviewid;
			dao.execUpdateSql(sql); // 該当レビューIDの画像ファイル情報を画像テーブルから削除
			
			sql = "DELETE FROM t_review WHERE review_id =  " + reviewid;
			dao.execUpdateSql(sql); // 該当レビューIDのレビュー情報をテーブルから削除
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		// 削除後に再度該当店舗のレビュー一覧を取得。
		// RamenDAOのインスタンス作成
		try(RamenDao dao = new RamenDao()) {
			
			// 設定した店舗コードに該当する店舗名を取得
			// RamenDtoをインスタンス化。
			RamenDto dto = dao.getRamenDetail(shopid);
			// 設定した店舗コードに該当する情報(店舗名など)をリクエスト属性格納する。
			request.setAttribute("ramen", dto);
			
			// 設定した店舗コードに該当するレビュー情報テーブルの全データを取得して、リクエスト属性に格納する。
			List<RamenDto> list = dao.getRamenReviewList(dto);
			
			// 設定した店舗コードに該当する全レビュー情報に格納されている画像情報を取得
			for(int i = 0; i < list.size(); i ++) {
				RamenDto dto2 = list.get(i);
				List<String> filenames = dao.getImageList(dto2.getReviewid());
				if(filenames != null) {
					dto2.setFilenames(filenames);
					list.set(i, dto2);
				}
			}
			
			request.setAttribute("reviewList", list);
			
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		// @ServletContextは共有データ
		ServletContext context = this.getServletContext();
				
		// 今登録済みのラーメン屋口コミ情報を一覧表示する(DspRamenReviewSearch→Ramenreviewlist.jsp)にフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/RamenReviewlist.jsp"); 
		dispatcher.forward(request, response);
	}

}
