package it.solutionsexmachina.webcontroller.servlet;

import it.solutionsexmachina.webcontroller.servlet.utils.AjaxCall;
import it.solutionsexmachina.webcontroller.servlet.utils.AjaxCallAction;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlContext;
import java.security.AccessController;

@WebServlet(urlPatterns = "/ajax/*")
public class GenericAjaxServlet extends HttpServlet {

    private static final String actionBeansPath = "it.solutionexmachina.crudwebapp.actionbeans";
    private static final long serialVersionUID = -1L;

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        super.doOptions(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        String page = request.getParameter("page");
        if (page != null) {
            request.getRequestDispatcher(page).forward(request, response);
        } else {
            request.getRequestDispatcher("/").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AccessControlContext context = AccessController.getContext();
        Subject subject = Subject.getSubject(context);

        String result = "{}";

        AjaxCallAction ajaxCallAction = new AjaxCallAction(request, response, actionBeansPath);
        AjaxCall ajaxCall = ajaxCallAction.getAjaxCallParameters(request);

        result = ajaxCallAction.callBeanMethod(ajaxCall);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Length", String.valueOf(result.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"response.json\"");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        out.print(result);

        out.flush();
        out.close();


    }

}
