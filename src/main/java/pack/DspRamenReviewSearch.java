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
import javax.servlet.http.HttpSession;

import dao.RamenDao;
import dto.RamenDto;

/**
 * Servlet implementation class DspRamenReviewSearch
 */
@WebServlet("/DspRamenReviewSearch")
public class DspRamenReviewSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DspRamenReviewSearch() {
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
        
        HttpSession session = request.getSession();
		
        int shopid = Integer.parseInt(request.getParameter("id"));
        
        // セッションからユーザーIDを取得
        
        
		// RamenDAOのインスタンス作成
		try(RamenDao dao = new RamenDao()) {
			
			// 設定した店舗コードに該当する店舗名を取得
			// RamenDtoをインスタンス化。
			RamenDto dto = dao.getRamenDetail(shopid);
			
			// セッションからユーザーIDを取得
			// ログイン中のユーザーIDを設定
			String userid = (String)session.getAttribute("userId");
			dto.setUserid(userid);
			
			// 該当の店舗及びユーザーでレビューが投稿済みかを確認する
			int count = dao.getReviewCount(dto);
			dto.setReviewCount(count);
			
			// 設定した店舗コードに該当する情報(店舗名など)をリクエスト属性格納する。
			request.setAttribute("ramen", dto);
			
			// 設定した店舗コードに該当するレビュー情報テーブルの全データを取得して、リクエスト属性に格納する。
			List<RamenDto> list = dao.getRamenReviewList(dto);

			// 設定した店舗コードに該当する全レビュー情報に格納されている画像情報を取得
			if(list != null) {
				for(int i = 0; i < list.size(); i ++) {
					RamenDto dto2 = list.get(i);
					List<String> filenames = dao.getImageList(dto2.getReviewid());
					if(filenames != null) {
						dto2.setFilenames(filenames);
						list.set(i, dto2);
					}
				}
			}
			
			request.setAttribute("reviewList", list);
			
		} catch(Exception e) {
			throw new ServletException(e);
		}

		
		//@ServletContextは共有データ
		ServletContext context = this.getServletContext();		
		
		// 今登録済みのラーメン情報を一覧表示する(Ramenlist.jspにフォワード)
		RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/RamenReviewlist.jsp");
		dispatcher.forward(request, response);
	}

}
