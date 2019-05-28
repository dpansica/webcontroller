package it.solutionsexmachina.webcontroller.servlet;

import it.solutionsexmachina.webcontroller.servlet.utils.AjaxCallAction;
import it.solutionsexmachina.webcontroller.servlet.utils.ProducerConsumerThread;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;

@WebServlet(urlPatterns = "/send-and-listen", asyncSupported = true)
public class AsyncServlet extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AccessControlContext context = AccessController.getContext();
        Subject subject = Subject.getSubject(context);

        final AsyncContext asyncContext = req.startAsync(req, resp);
        asyncContext.setTimeout(0);

        ProducerConsumerThread producerConsumerThread = CDI.current().select(ProducerConsumerThread.class).get();
        try {
            producerConsumerThread.sendInputOnChannel("test", new AjaxCallAction(req, resp), asyncContext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
