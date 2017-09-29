package edu.sdsu.its.Routes;

import org.jtwig.web.servlet.JtwigRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author Tom Paulus
 *         Created on 6/30/17.
 */
public class FollowUpUnSub extends HttpServlet {
    private final JtwigRenderer renderer = JtwigRenderer.defaultRenderer();
    final String TEMPLATE_PATH = "/WEB-INF/templates/followup/index.twig";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.addHeader("content-type", MediaType.TEXT_HTML);
        renderer.dispatcherFor(TEMPLATE_PATH)
                .render(request, response);
    }
}
