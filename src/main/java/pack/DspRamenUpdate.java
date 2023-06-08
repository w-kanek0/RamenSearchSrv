package pack;

import java.io.IOException;

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
 * Servlet implementation class DspRamenUpdate
 */
@WebServlet("/DspRamenUpdate")
public class DspRamenUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DspRamenUpdate() {
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
		
		//リクエストパラメータから店舗コードを取得
		int shopid = Integer.parseInt(request.getParameter("id"));
		
		try(RamenDao dao = new RamenDao()) {
			// RamenDtoをインスタンス化。
			RamenDto dto = dao.getRamenDetail(shopid);
			request.setAttribute("ramen", dto);	// リクエストパラメータにRamenDtoの設定値を乗せる
		} catch(Exception e) {
			throw new ServletException();
		}
		
		// @ServletContextは共有データ
		ServletContext context = this.getServletContext();
				
		// ラーメン情報の新規登録画面を表示する(Ramenregister.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/Ramenregister.jsp"); 
		dispatcher.forward(request, response);	
	}

}
