package pack;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.RamenDao;
import dto.RamenDto;

/**
 * Servlet implementation class DspRamenReviewUpdate
 */
@WebServlet("/DspRamenReviewUpdate")
public class DspRamenReviewUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DspRamenReviewUpdate() {
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

		
		try(RamenDao dao = new RamenDao()) {
			// リクエストパラメータから取得したレビューIDに該当するレビュー情報を取得
			RamenDto dto = dao.getRamenReview(reviewid);
			
			// リクエストパラメータから取得したレビューIDに該当する画像ファイル名を一覧取得
			List<String> filenames = dao.getImageList(reviewid);
			dto.setFilenames(filenames);
			// リクエストパラメータから店舗コード、店舗名を取得
			dto.setShopid(Integer.parseInt(request.getParameter("shopid")));	// 店舗コード
			dto.setShopName(request.getParameter("name"));		// 店舗名
			dto.setReviewid(reviewid);	// レビューID
			
			request.setAttribute("review", dto);	// リクエストパラメータにRamenDtoの設定値を乗せる
			
		} catch(Exception e) {
			throw new ServletException(e);
		}
				
		// @ServletContextは共有データ
		ServletContext context = this.getServletContext();
		// ラーメン情報の新規登録画面を表示する(Ramenregister.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/RamenReviewregister.jsp"); 
		dispatcher.forward(request, response);
	}

}
