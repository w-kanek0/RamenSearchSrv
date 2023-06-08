package pack;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cnt.Constant;
import dao.LoginDao;
import dto.LoginDto;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF8");
		response.setCharacterEncoding("UTF-8");
		
		// フォーム入力データ取得
		String userid = request.getParameter("userid").toLowerCase();
		String password = request.getParameter("password");
		
		LoginDto dto = null;
		try(LoginDao dao = new LoginDao()) {
			
			// ユーザーID、pass確認用SQL文作成
			String sql = "SELECT user_id, password, username, email FROM m_user WHERE user_id = '"
						+ userid + "' AND password='" + password +"'";
			
			// ID,パスワードで認証
			dto = dao.getLoginInfo(sql);

		} catch (Exception e) {
			throw new ServletException();
		}

		// ID, パスワードが確認できたとき
		if(dto != null) {
			
			HttpSession session = request.getSession();
			
			// セッションにユーザーデータを格納
			session.setAttribute("userId", dto.getUserid());		// ユーザーID
			session.setAttribute("password", dto.getPassword());	// パスワード
			session.setAttribute("userName", dto.getUsername());	// ユーザー名
			session.setAttribute("email", dto.getEmail());			// メールアドレス

			// ユーザー画面遷移
			ServletContext context = this.getServletContext();
			RequestDispatcher dispatcher = context.getRequestDispatcher("/DspRamenSearch");
			dispatcher.forward(request, response);
		} else {	// 認証できなかったとき(ユーザーIDまたはパスワードが間違っているとき)
			request.setAttribute("errorMsg", Constant.WRONG_USERID_PASS);
			ServletContext context = this.getServletContext();
			// ID、PASS誤入力時ログイン画面に戻る
			RequestDispatcher dispatcher = context.getRequestDispatcher("/jsp/login.jsp");
			dispatcher.forward(request, response);		
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
