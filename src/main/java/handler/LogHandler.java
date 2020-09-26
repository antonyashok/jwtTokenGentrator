package handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class LogHandler extends AbstractHandler{

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		StringBuilder log = new StringBuilder();
		log.append(request.getMethod());
		log.append(" ");
		log.append(target);
		log.append(" ");
		log.append(response.getStatus());
		System.out.println(log);
	}

}
