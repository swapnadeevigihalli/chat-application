package assgn1;


import java.net.*;
import java.awt.*;
import java.util.*;
import java.io.*;
// code to create client socket

class Jclientsocket
    extends Thread
{
    protected Socket mysocket;
    protected static int clientnumber = 0;
    protected Jserver myparent;
    protected BufferedReader istream;
    protected PrintWriter ostream;

    public String username;
    public String location;
    public String additional;
    protected int count;


    public Jclientsocket(Jserver parent, Socket s, 
			       ThreadGroup threadgroup)
    {
	super("OTF client " + clientnumber++);

	myparent = parent;
	mysocket = s;

	username = new String ("New user");
	location = new String ("Location");
	additional = new String ("None");

	

	try {
	    istream = new BufferedReader(new 
		InputStreamReader(mysocket.getInputStream()));
	    ostream = new PrintWriter(mysocket.getOutputStream(), true);
	} 
	catch (IOException g) {
	    myparent.Serveroutput("Error setting up client streams\n");
	    try {
		mysocket.close();
	    } 
	    catch (IOException h) {
		myparent.Serveroutput("Couldn't close socket\n");
	    }
	    System.exit(1);
	}

	
	this.myoutput("Welcome to OTF chat.");
	if (myparent.connections.size() == 0) 
	    myoutput("You are the first user on line.");
	else if (myparent.connections.size() > 1)
	    myoutput("Currently there are " + myparent.connections.size() 
		     + " other users connected\n");
	else
	    myoutput("Currently there is one other user connected\n");
    }

    public void myoutput(String s)
    {
        String outstring = s;
        this.ostream.println(outstring);
    }

    public void run()
    {
	String line;

	while (true)
	    {
		
		yield();

		line = null;

		try {
		    line = this.istream.readLine();
		    if (line == null)
			continue;
		    if (line.equals(""))
			continue;
		} 
		catch (IOException g) {
		    myparent.disconnect(this, false);
		    return;
		}


	
		if (line.startsWith("/FROM "))
		    {
			String tmpLine = new String(line);
			// Discard the FROM
			tmpLine = tmpLine.substring(6);

			
			for (count = 0; tmpLine.charAt(count) != ' ';
			     count ++);
			tmpLine = tmpLine.substring(count + 1);

			
			if (tmpLine.startsWith("/TO "))
			    {
				
				tmpLine = tmpLine.substring(4);

				
				if (tmpLine.startsWith("ALL"))
				    {
					
					myparent.thewriter.clientoutput(line);
					continue;
				    }

				

				for (count = 0;
				     count < myparent.connections.size();
				     count ++)
				    {
					Jclientsocket temp = 
					    ((Jclientsocket) 
					     myparent.connections.elementAt(count));
				
					if (temp == null)
					 
					    continue;

					else if (tmpLine.startsWith(temp.username))
					    {
						
						temp.myoutput(line);
					    }
				    }
			    }
			else
			    {
				myparent.thewriter.clientoutput(line);
				continue;
			    }
		    }   

		if (line.startsWith("/NEWUSER ") || 
		    line.startsWith("/ADDUSER "))
		    {
			
			myparent.thewriter.clientoutput(line);
			continue;
		    }

		if (line.startsWith("/REMOVEUSER "))
		    {
			myparent.disconnect(this, false);
		    }


		


		if (line.startsWith("/WHOIS "))
		    {
			String whothis = new String();
			line = line.substring(7);

			for (count = 0; count < line.length(); count ++)
			    whothis += line.charAt(count);

			for (count = 0; 
			     count < myparent.connections.size();
			     count ++)
			    {
				Jclientsocket temp = 
				    ((Jclientsocket) 
				     myparent.connections.elementAt(count));
				
				if (temp.username.equals(whothis))
				    {
					this.myoutput("<<User: " 
						      + temp.username);
					this.myoutput("  Location: " 
						      + temp.location);
					this.myoutput("  Additional: "
						      + temp.additional 
						      + ">>\n");
					break;
				    }
			    }
			continue;
		    }

		if (line.startsWith("/SAVEDMESSAGES"))
		    {
			Jmessage m;

			for (count = 0; count < myparent.messages.size(); 
			     count ++)
			    {
				m = (Jmessage) 
				    myparent.messages.elementAt(count);

				if (m.isfor.equals(this.username))
				    {
					this.myoutput("/SAVEDMESSAGES " 
						      + m.from + " /IS " 
						      + m.message);
                    
					myparent.messages.removeElement(m);
					count -= 1;
				    }
			    }

			this.myoutput("/SERVERMESSAGE End of messages");
			continue;
		    }


		if (line.startsWith("/SETMESSAGE "))
		    {
			String messagefor = new String();
			String messagefrom = new String();
			String themessage = new String();

			line = line.substring(12);
            
			for (count = 0; count < line.length(); count++)
			    {
				if (line.charAt(count) != ' ')
				    messagefor += line.charAt(count);

				else
				    {
					line = line.substring(count + 1);
					break;
				    }
			    }

			for (count = 0; count < line.length(); count++)
			    {
				if (line.charAt(count) != ' ')
				    messagefrom += line.charAt(count);

				else
				    {
					line = line.substring(count);
					break;
				    }
			    }

			for (count = 0; count < line.length(); count++)
			    {
				themessage += line.charAt(count);
			    }

            
			myparent.messages.addElement(
			     new Jmessage(messagefor, messagefrom, 
						themessage));

			myparent.SaveMessages();
			continue;
		    }


		if (line.startsWith("/NAME"))
		    {
			int numberof = 0;
			Jmessage tempmessage;
			line = line.substring(6);

			
			for (int count = 0;
			     count < myparent.connections.size();
			     count ++)
			    {
				Jclientsocket tempsocket = 
				    ((Jclientsocket) 
				     myparent.connections.elementAt(count));
				
				if (tempsocket.username.equals(line))
				    {
					this.myoutput("<<There is already a user called \""
						      + line +
						      "\" logged in\n");
					this.myoutput("Please pick another name and reconnect>>\n");
					myparent.disconnect(this, true);
				    }
			    }

			this.username = line;

			if (myparent.graphics)
			    {
				myparent.mywindow.userlist.addItem(
							   this.username);

				myparent.mywindow.disconnect.setEnabled(true);

				if (myparent.connections.size() > 1)
				    myparent.mywindow.disconnectall.setEnabled(true);
			    }

			for (count = 0; count < 
				 myparent.messages.size();
			     count ++)
			    {
				tempmessage = (Jmessage)
				    myparent.messages.elementAt(count);
				if (tempmessage.isfor.equals(
							     this.username))
				    numberof += 1;
			    }

			myparent.Serveroutput("New user " + this.username +
					      " logging on\n");
			myparent.Serveroutput("There are " 
					      + myparent.connections.size()
					      + " users connected\n");

			if (numberof == 1) 
			    this.myoutput(
			  "/SERVERMESSAGE You have 1 message waiting");

			else if (numberof > 1) 
			    this.myoutput("/SERVERMESSAGE You have "
					  + numberof + " messages waiting");

			continue;
		    }

		if (line.startsWith("/LOCATION "))
		    {
			line = line.substring(10);
			this.location = line;
			continue;
		    }

		if (line.startsWith("/ADDITIONAL "))
		    {
			line = line.substring(12);
			this.additional = line;
			continue;
		    }

	
		continue;
	    }
    }
}
