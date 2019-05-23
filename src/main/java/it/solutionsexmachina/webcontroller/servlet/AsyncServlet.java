package it.solutionsexmachina.webcontroller.servlet;

import it.solutionsexmachina.webcontroller.servlet.utils.AjaxCallAction;

import javax.security.auth.Subject;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

@WebServlet(urlPatterns = "/send-and-listen", asyncSupported = true)
public class AsyncServlet extends HttpServlet implements AsyncListener {

    private List<AsyncContext> connections = new CopyOnWriteArrayList<AsyncContext>();

    private BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();

    private Thread broadcaster = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {
                String result = blockingQueue.poll();
                if (result!=null) {
                    for (Iterator iterator = connections.iterator(); iterator.hasNext(); ) {
                        AsyncContext context = (AsyncContext) iterator.next();

                        try {
                            PrintWriter writer = context.getResponse().getWriter();

                            writer.print(result);
                            writer.flush();
                            writer.close();

                            context.complete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    });

    public void init() {
        broadcaster.start();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AccessControlContext context = AccessController.getContext();
        Subject subject = Subject.getSubject(context);

        final AsyncContext asyncContext = req.startAsync(req, resp);
        asyncContext.setTimeout(0);

        connections.add(asyncContext);

        asyncContext.addListener(this);

        try {
            blockingQueue.put(AjaxCallAction.parseCall(req, resp));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onStartAsync(AsyncEvent event) throws IOException {
    }

    public void onComplete(AsyncEvent event) throws IOException {
        connections.remove(event.getAsyncContext());
    }

    public void onTimeout(AsyncEvent event) throws IOException {
    }

    public void onError(AsyncEvent event) throws IOException {
    }
}
