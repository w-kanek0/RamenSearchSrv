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
 * Servlet implementation class RamenSearch
 */
// 検索条件に合ったラーメン情報を取得するサーブレット
@WebServlet("/RamenSearch")
public class RamenSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RamenSearch() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//受け取るデータの文字コードをエンコードする
		request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
		
		// RamenDAOのインスタンス作成
		try(RamenDao dao = new RamenDao()) {
			RamenDto dto = new RamenDto();
			
			/* リクエスト属性からパラメータを取得 */
			dto.setShopName(request.getParameter("s_name"));	// 店名(前後のスペースは取り除く)
			// エリア(前後及び文字中の空白も取り除く)		
			dto.setArea(request.getParameter("s_area"));	// エリア(前後及び文字中の空白も取り除く)
			dto.setGenreid(Integer.parseInt(request.getParameter("s_genre")));		// ジャンル
			dto.setValue(Integer.parseInt(request.getParameter("s_value")));	// 評価
			dto.setEquality(request.getParameter("equality"));	// 評価
			dto.setInputOpentime(request.getParameter("s_opentime"));	// 営業時間
			dto.setInputOpendayStart(request.getParameter("s_opendatestart"));	// オープン日(検索開始日)
			dto.setInputOpendayEnd(request.getParameter("s_opendateend"));	// オープン日(終了日)
			
			// t_shopテーブルの検索条件に合ったデータを取得して、リクエスト属性に格納する。
			List<RamenDto> list = dao.getRamenList(dto);
			request.setAttribute("ramenList", list);
			request.setAttribute("search", dto);
			
		} catch(Exception e) {
			throw new ServletException(e);
		}

		
		//@ServletContextは共有データ
		ServletContext context = this.getServletContext();		
		
		// 今登録済みのラーメン情報を一覧表示する(Ramenlist.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/Ramenlist.jsp");
		dispatcher.forward(request, response);
	}

}
