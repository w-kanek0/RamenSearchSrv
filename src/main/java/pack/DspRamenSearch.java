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
 * Servlet implementation class DspRamenSearch
 */
// ラーメン検索システム初期表示用サーブレット
@WebServlet("/DspRamenSearch")
public class DspRamenSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DspRamenSearch() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 *
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		// RamenDAOのインスタンス作成
		try(RamenDao dao = new RamenDao()) {
			
			// t_shopテーブルの全データを取得して、リクエスト属性に格納する。
			List<RamenDto> list = dao.getRamenList(null);
			request.setAttribute("ramenList", list);
			
		} catch(Exception e) {
			throw new ServletException(e);
		}

		
		//@ServletContextは共有データ
		ServletContext context = this.getServletContext();		
		
		// 今登録済みのラーメン情報を一覧表示する(Ramenlist.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/Ramenlist.jsp");
		dispatcher.forward(request, response);
		
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
