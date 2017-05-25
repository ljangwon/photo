<%
	final String[] REGISTER_IP = {
		"0.0.0.0",
	};

	String IP_ADDRESS = request.getRemoteAddr();

	boolean isTrustedIP = false;

	for(int i=0; i<REGISTER_IP.length; i++)
	{
		if(IP_ADDRESS.equals(REGISTER_IP[i]))
			isTrustedIP = true;
	}

	if(isTrustedIP == false)
	{
		String str = "This server could not verify that you are authorized to access the document requested." +
		"<br>Either you supplied the wrong credentials (e.g., bad password), <br>or your browser doesn\'t understand how to supply" +
		"the credentials required.";

		out.println("This User is not allowed.<p>Your IP : ["+IP_ADDRESS + "]");

		out.println("<h1>Authorization Required</h1>");
		out.println("<p>" + str + "</p><hr>");
		out.println("<address>" + application.getServerInfo() + " Server at " + request.getServerName() + " Port " + request.getServerPort() 
			+ "</address>");
		return;
	}
%>