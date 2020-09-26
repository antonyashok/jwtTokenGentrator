package handler;

import java.io.BufferedReader;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class PageHandler extends AbstractHandler {

	private Map<String,String> ext2MIME = null;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(!request.getMethod().equals("GET"))	//ignore non-GET requests
			return;
		//look for target; if it cannot be found, stop handling request
				try(BufferedReader file = new BufferedReader(new FileReader("web"+target))){	//target automatically starts with a /
					//initialize Map of extensions to MIME types once only
					if(ext2MIME==null){
						ext2MIME=new HashMap<>();
						ext2MIME.put(".html", "text/html");
						ext2MIME.put(".css", "text/css");
						ext2MIME.put(".js", "application/javascript");
						ext2MIME.put(".json", "application/json");
					}
					
					//get file extension
					String fileExt=null;
					try{fileExt=target.substring(target.lastIndexOf('.'));}
					finally{}
					
					//convert file extension to mime type
					String mimetype=ext2MIME.get(fileExt);
					if(mimetype!=null)
						response.setContentType(mimetype+"; charset=utf-8");
					
					response.setStatus(HttpServletResponse.SC_OK);
					
					//copy every line of file to response body
					PrintWriter respBody = response.getWriter();
					file.lines().forEach(line -> {respBody.println(line);});
					
					baseRequest.setHandled(true);
				}
				catch(FileNotFoundException e){System.err.println("PageHandler FileNotFoundException: " + e.getMessage());}
				catch(IOException e){}
				finally{}
				
	}

}
