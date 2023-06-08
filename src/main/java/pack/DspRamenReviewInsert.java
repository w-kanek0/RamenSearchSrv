package pack;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.RamenDto;

/**
 * Servlet implementation class DspRamenReviewInsert
 */
@WebServlet("/DspRamenReviewInsert")
public class DspRamenReviewInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DspRamenReviewInsert() {
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
		

		// RamenDtoをインスタンス化。
		RamenDto dto = new RamenDto();
		
		dto.setShopid(Integer.parseInt(request.getParameter("shopid")));	// 店舗ID
		dto.setShopName(request.getParameter("name"));		// 店舗名
		dto.setReviewid(0);	// レビューIDは0に設定する。
		
		request.setAttribute("review", dto);	// リクエストパラメータにRamenDtoの設定値を乗せる
				
		// @ServletContextは共有データ
		ServletContext context = this.getServletContext();
		// ラーメン情報の新規登録画面を表示する(Ramenregister.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/RamenReviewregister.jsp"); 
		dispatcher.forward(request, response);
	}

}
