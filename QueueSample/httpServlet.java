package com.lgcns.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/*
 * Gson gson = new Gson();
 * JsonObject creajobj = new JsonObject();
 * gson.toJson(creajobj);
 * 
 * JsonObject jObj = gson.fromJson(strBody, JsonObject.class);
 * 
 */

public class httpServlet extends HttpServlet {

	private LinkedHashMap<String, QueueMsg> queues = new LinkedHashMap<String, QueueMsg>();

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		/*
		 * 0710
		 */
		
		System.out.println();
		
		req.getRequestURI();  	// https://127.0.0.1/Project/project.jsp
		req.getRequestURL();	// /Project/project.jsp
		req.getContextPath();	// /Project
		req.getServletPath();	// /project.jsp
		req.getRemoteHost();	// / 127.0.0.1
		req.getServerName();	// /localhost
		req.getServerPort();	// 8080 
		

		System.out.println("Request GET : " + req.getRequestURL());
		if (req.getRequestURI().contains("/RECEIVE")) {

			Gson reqgson = new Gson();
			Gson resgson = new Gson();
			String resquestr;
			String queueNM = req.getRequestURI().substring(9);
			JsonObject resjobj = new JsonObject();

			// System.out.println("queueNm : " + queueNM);

			/*
			 * res.setStatus(200); res.getWriter().write(new Date().toString());
			 */

			if (queues.containsKey(queueNM)) {
				String[] receiveList = queues.get(queueNM).getQueueMsg(queueNM);

//				System.out.println("Result 1 :"+queueNM + " No Message");

				if (receiveList[0].equals("-1")) {
					resjobj.addProperty("Result", "No Message");

//					System.out.println("Result 2 :"+queueNM+ " No Message");

				} else {
					resjobj.addProperty("Result", "Ok");
					resjobj.addProperty("MessageID", receiveList[0]);
					resjobj.addProperty("Message", receiveList[1]);

					// System.out.println("MessageID : "+ receiveList[0]+" Message : "+
					// receiveList[1] +" : "+queueNM);
//					System.out.println(" Message1 : "+ resjobj.get("Message").toString() );
//					System.out.println(" Message2 : "+ receiveList[1] );
				}

			} else {
				resjobj.addProperty("Result", "No Message");

//				System.out.println("Result 3"+queueNM+ " No Message");

			}

			String que = req.getRequestURI();

			System.out.println("URL : " + que);

			resquestr = resgson.toJson(resjobj);
			res.setStatus(200);
			res.getWriter().write(resquestr);

		}

	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//		System.out.println("Request POST : " + req.getRequestURI() + " || " + req.getRequestURI().equals("/CREATE")
//				+ " || con " + req.getRequestURI().contains("/CREATE"));

		if (req.getRequestURI().contains("/CREATE")) {

			Gson reqgson = new Gson();
			Gson resgson = new Gson();
			String resquestr;
			String queueNM = req.getRequestURI().substring(8);
			int quesize = 0;

			System.out.println("queue Name : " + queueNM);
			JsonObject reqjobj = new JsonObject();
			JsonObject resjobj = new JsonObject();
			StringBuffer sb = new StringBuffer();

			BufferedReader br = req.getReader();
			String reqline = null;

			while ((reqline = br.readLine()) != null) {
				// System.out.println("sb1 : "+ sb.toString() + "line : "+ line);
				sb.append(reqline);
				// System.out.println("sb2 : "+ sb.toString()+ "line : "+ line);
			}

			reqjobj = reqgson.fromJson(sb.toString(), JsonObject.class);

			quesize = Integer.parseInt(reqjobj.get("QueueSize").toString());

			// System.out.println("reqjobj : " + reqjobj.get("QueueSize"));

			// queue

			QueueMsg qm = new QueueMsg();

			if (queues.containsKey(queueNM)) {

				// System.out.println("Queue Exist");
				resjobj.addProperty("Result", "Queue Exist");

			} else {
				List<String> listMsg = new ArrayList<String>();
				qm.createQueue(quesize, listMsg);
				queues.put(queueNM, qm);
				resjobj.addProperty("Result", "Ok");
			}

			/*
			 * ±»ÀÌ ÇÊ¿ä ¾ø´Â µí res.setContentType("application/json");
			 */
			resquestr = resgson.toJson(resjobj);
			res.setStatus(200);
			res.getWriter().write(resquestr);

			System.out.println("URL CREATE : " + resquestr);

		} else if (req.getRequestURI().contains("/SEND")) {
			String resquestr;
			String queueNM = req.getRequestURI().substring(6);
			String queMsg = "";

			Gson reqgson = new Gson();
			Gson resgson = new Gson();

			JsonObject reqjobj = new JsonObject();
			JsonObject resjobj = new JsonObject();
			StringBuffer sb = new StringBuffer();

			BufferedReader br = req.getReader();
			String reqline = null;

			while ((reqline = br.readLine()) != null) {
				// System.out.println("sb1 : "+ sb.toString() + "line : "+ line);
				sb.append(reqline);
				// System.out.println("sb2 : "+ sb.toString()+ "line : "+ line);
			}
			reqjobj = reqgson.fromJson(sb.toString(), JsonObject.class);

			queMsg = reqjobj.get("Message").toString();
			queMsg = queMsg.substring(1, queMsg.length() - 1);

			if (!queues.get(queueNM).inputQueueMsg(queMsg)) {
//				System.out.println("Queue Full");
				resjobj.addProperty("Result", "Queue Full");
			} else {
//				System.out.println("Result OK");
				resjobj.addProperty("Result", "Ok");
			}

			resquestr = resgson.toJson(resjobj);
			res.setStatus(200);
			res.getWriter().write(resquestr);

			// System.out.println("quName : "+ queueNM);

		} else if (req.getRequestURI().contains("/ACK")) {
			String resquestr;
			String queueNM = "";
			int queMsgID = 0;

			Gson reqgson = new Gson();
			Gson resgson = new Gson();

			JsonObject reqjobj = new JsonObject();
			JsonObject resjobj = new JsonObject();

			String[] strlist = req.getRequestURI().split("/");

			System.out.println("ack :" + strlist[2] + "msgID :" + strlist[3]);

			/*
			 * StringBuffer sb = new StringBuffer();
			 * 
			 * BufferedReader br = req.getReader(); String reqline = null;
			 * 
			 * while ((reqline = br.readLine()) != null) { // System.out.println("sb1 : "+
			 * sb.toString() + "line : "+ line); sb.append(reqline); //
			 * System.out.println("sb2 : "+ sb.toString()+ "line : "+ line); } reqjobj =
			 * reqgson.fromJson(sb.toString(), JsonObject.class);
			 */

			queueNM = strlist[2];
			queMsgID = Integer.parseInt(strlist[3]);
			String remStr ="";

			if (queues.containsKey(queueNM)) {

				remStr = queues.get(queueNM).outputQueueMsg(queMsgID);
				resjobj.addProperty("Result", "Ok");
				
				System.out.println("remove message :"+ remStr);

//				String[] receiveList = queues.get(queueNM).getQueueMsg(queueNM);

			} else {
				resjobj.addProperty("Result", "No Message");

			}

			resquestr = resgson.toJson(resjobj);
			res.setStatus(200);
			res.getWriter().write(resquestr);

		} else if (req.getRequestURI().contains("/FAIL")) {

			String resquestr;
			String queueNM = "";
			String queMsg = "";
			int queMsgID = 0;

			Gson reqgson = new Gson();
			Gson resgson = new Gson();

			JsonObject reqjobj = new JsonObject();
			JsonObject resjobj = new JsonObject();

			String[] strlist = req.getRequestURI().split("/");

//			System.out.println("FAIL :" + strlist[0] + "msgID :" + strlist[1] + ":" + strlist[2] + ":" + strlist[3]					+ " URI :" + req.getRequestURI() + "size : " + strlist.length);

			
			queueNM = strlist[2];
			queMsgID = Integer.parseInt(strlist[3]);

			if (queues.containsKey(queueNM)) {

				queues.get(queueNM).outputFailMsg(queMsgID);
				resjobj.addProperty("Result", "Ok");
			}
			
			resquestr = resgson.toJson(resjobj);
			res.setStatus(200);
			res.getWriter().write(resquestr);


		}

	}

}
