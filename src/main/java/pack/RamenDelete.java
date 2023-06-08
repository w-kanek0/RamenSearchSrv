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

/**
 * Servlet implementation class RamenDelete
 */
@WebServlet("/RamenDelete")
public class RamenDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RamenDelete() {
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
		
		int shopid = Integer.parseInt(request.getParameter("id"));
		
		
		try(RamenDao dao = new RamenDao()) {
			
			// 削除対象の店舗情報のレビューに投稿された写真ファイル名一覧を取得
			List<String> deleteFilenames = dao.getAllImageList(shopid);
			
			// 取得したファイルをアップロードフォルダから削除
			if(deleteFilenames != null && deleteFilenames.size() > 0) {
				for(String filename: deleteFilenames) {
					String path = Constant.UPLOAD_REVIEW_PATH;
					File file = new File(path + filename);
					file.delete();
				}
			}
			
			// 該当の店舗に紐づいた口コミ情報にアップロード済みの画像情報を削除
			String sql = "DELETE FROM t_image WHERE review_id in ( SELECT review_id FROM t_review"
					+ " WHERE shop_id = " + shopid + " )";
			
			dao.execUpdateSql(sql);
			
			// 該当の店舗に紐づいた口コミ情報を削除
			sql = "DELETE FROM t_review WHERE shop_id =  " + shopid;
			
			dao.execUpdateSql(sql);
			
			// 該当の店舗コードのラーメン情報を削除。
			sql = "DELETE FROM t_shop WHERE shop_id = " + shopid;
			
			dao.execUpdateSql(sql);
		} catch(Exception e) {
			throw new ServletException(e);
		}
		
		// @ServletContextは共有データ
		ServletContext context = this.getServletContext();
				
		// 今登録済みのラーメン情報を一覧表示する(DspRamenSearch(→Ramenlist.jsp)にフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/DspRamenSearch"); 
		dispatcher.forward(request, response);	
	}

}
