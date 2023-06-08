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
 * Servlet implementation class DspRamenInsert
 */
@WebServlet("/DspRamenInsert")
public class DspRamenInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DspRamenInsert() {
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
		
		// @ServletContextは共有データ
		ServletContext context = this.getServletContext();
		
		// RamenDtoをインスタンス化。
		RamenDto dto = new RamenDto();
		dto.setShopid(0);	// 店舗IDは0に設定する。
		
		request.setAttribute("ramen", dto);	// リクエストパラメータにRamenDtoの設定値を乗せる
				
		// ラーメン情報の新規登録画面を表示する(Ramenregister.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/Ramenregister.jsp"); 
		dispatcher.forward(request, response);	
	}

}
