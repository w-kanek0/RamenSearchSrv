package pack;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cnt.Constant;
import dao.LoginDao;
import dto.LoginDto;

/**
 * Servlet implementation class ExecRegisterUser
 */
@WebServlet("/RegisterUser")
public class RegisterUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterUser() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF8");
		response.setCharacterEncoding("UTF-8");
		
		LoginDto dto = new LoginDto();
		
		// リクエストパラメータから取得したユーザー情報をDtoにセット
		dto.setUserid(request.getParameter("userid"));		// ユーザーID
		dto.setPassword(request.getParameter("password"));	// パスワード
		if(request.getParameter("username") != null) {		
			dto.setUsername(request.getParameter("username"));	// ユーザー名
		}
		dto.setEmail(request.getParameter("email"));		// メールアドレス
		
		boolean existUser = false;
		try(LoginDao dao = new LoginDao()) {
			// ユーザーIDが登録済みか確認
			existUser = dao.isUserResisterd(dto);
			// ユーザーID未登録の場合、登録処理を実行
			if(!existUser) {
				// リクエストパラメータから受け取ったユーザー情報をm_userテーブルに登録。
				String sql = "INSERT INTO m_user VALUES ('" + dto.getUserid() + "', '" + dto.getPassword() + "', '"
						+ dto.getUsername() + "', '" + dto.getEmail() + "')";
				dao.execUpdateSql(sql);
			} else {
				// ユーザーID重複時、重複エラーメッセージを格納
				request.setAttribute("errorMsg", Constant.USER_ALREADY_REGISTERED);
			}
		} catch(Exception e) {
			throw new ServletException();
		}

		ServletContext context = this.getServletContext();
		RequestDispatcher dispatcher;
		if(existUser) {
			// ユーザー重複して登録できなかった場合、ユーザー登録画面遷移
			dispatcher = context.getRequestDispatcher("/jsp/signup.jsp");
		} else {
			// ユーザー重複していない場合、ユーザー登録完了画面遷移
			dispatcher = context.getRequestDispatcher("/jsp/confirmsignup.jsp");
		}
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
