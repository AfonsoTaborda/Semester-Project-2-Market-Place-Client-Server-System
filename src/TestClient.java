import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDate;

import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;
import com.sun.org.apache.xerces.internal.util.Status;
import com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser;

import Client.*;
import Model.Account;
import Model.Method;
import Model.Person;

public class TestClient {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		
		ClientControl clientControl = ClientControl.getInstance();
		clientControl.setServerConnectionDetails("localhost", 9999);
		
		
		clientControl.registerAccount(new Account("myusername", "myemail@email.com", 
				new Person("firstname", "lastName", "address", 88888888, true, LocalDate.now()))
				, "mypass", (Status , derp) ->
		{
			 System.out.println("MStatus: " + Status);
			 System.out.println("RegStatus: " + derp);
		});
		
		Thread.sleep(5000);
		
		clientControl.disconnect();
		/*clientControl.signIn("myuser", "pwd", (status, blah) ->
		{
			System.out.println(status);
			System.out.println("args:");
			if(blah != null)
				for(int i = 0; i < blah.length; i++)
					System.out.println(blah[i]);
			
		});*/
		/*
		clientControl.runServerMethod(Method.RegisterAccount, (status, blah) ->
		{
			System.out.println(status);
			System.out.println("args:");
			if(blah != null)
				for(int i = 0; i < blah.length; i++)
					System.out.println(blah[i]);
			
		}, new Account("MyUserName", "fdlgdfgk", "email", new Person(0, "firstName", "lastName", "address", 234, false)));
		
		clientControl.runServerMethod(Method.CreateListing, (status, blah) ->
		{
			System.out.println(status);
			System.out.println("args:");
			if(blah != null)
				for(int i = 0; i < blah.length; i++)
					System.out.println(blah[i]);
		}, null);
		*/
		/*
		Object[] serverResponseVars = response.awaitResponse();
		Object[] serverResponseVars2 = response2.awaitResponse();
		System.out.println("Server returned: ");
		if(serverResponseVars != null)
			for(int i = 0; i < serverResponseVars.length; i++)
				System.out.println(serverResponseVars[i].toString());

		System.out.println("Server returned: ");
		if(serverResponseVars2 != null)
			for(int i = 0; i < serverResponseVars2.length; i++)
				System.out.println(serverResponseVars2[i].toString());*/
	}

}
