package Server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import Net.Server.*;
import Model.*;

public class ServerMain {
	private DBControl database;
	private NetServer networkServer;
	private Map<String, Account> sessionIDs;

	public ServerMain(int listenerPort, String dbURL, String dbUsername, String dbPassword) throws IOException, SQLException {
		database = new DBControl(dbURL, dbUsername, dbPassword);
		networkServer = new NetServer(listenerPort);
		sessionIDs = new HashMap<String, Account>();
		
		//No authentication:
		networkServer.addServerMethod(Method.RegisterAccount.getValue(), a -> handleRegisterAccount(a));
		networkServer.addServerMethod(Method.SignIn.getValue(), a -> handleSignIn(a));
		//networkServer.addServerMethod(Method.GetAccount.getValue(), a -> handleLoginAccount(a));
		networkServer.addServerMethod(Method.GetListings.getValue(), a -> handleGetListings(a));
		
		//Requires authentication:
		networkServer.addServerMethod(Method.CreateListing.getValue(), a -> handleCreateListing(Arrays.copyOfRange(a, 1, a.length), getAuthenticatedAccount(a)));
		networkServer.addServerMethod(Method.BuyItem.getValue(), a -> handleBuyItem(Arrays.copyOfRange(a, 1, a.length), getAuthenticatedAccount(a)));
		networkServer.addServerMethod(Method.MakeOffer.getValue(), a -> handleMakeOffer(Arrays.copyOfRange(a, 1, a.length), getAuthenticatedAccount(a)));
	}
	
	private Object[] handleMakeOffer(Object[] args, Account offeror)
	{
		// input:
		// 0: Item - item offeror wants to make an offer for.
		// 1: double - the offered item price
		
		// output:
		// 0: Boolean - was the offer successfully registered?
		return null;
	}
	
	private Object[] handleBuyItem(Object[] args, Account buyer)
	{
		// input:
		// 0: Item - the item to be bought
		// 1: int - the quantity of said item to be bought
		
		// output: 
		// 0: int - actual quantity bought
		return null;
	}
	
	private Object[] handleCreateListing(Object[] args, Account owner)
	{
		return null;
	}

	private Object[] handleGetListings(Object[] args) {
		// input:
		// 0: search keyword: String
		// 1: category
		// if both the search keyword and category are null, get every listing that has been created
		return null;
	}
	
	private Object[] handleSignIn(Object[] args) {
		// input:
		// 0: String - Username
		// 1: String - PasswordHash

		// output:
		// 0: boolean - successful login?
		// 1: String - sessionID

		Object[] response = new Object[2];
		response[0] = false;
		if (args.length == 2) {
			if (args[0] instanceof String && args[1] instanceof String) {
				Account theAccount = database.checkUserCredentials((String) args[0], (String) args[1]);
				if (theAccount != null) {
					response[0] = true;
					String session = UUID.randomUUID().toString();
					response[1] = session;
					sessionIDs.put(session, theAccount);
				}
			}
		}
		return response;
	}

	private Object[] handleRegisterAccount(Object[] args)
	{
		//input:
		//0: Account - accountDetails
		//1: String - account password
		
		//output:
		//0: RegisterAccountStatus - status
		Object[] response = new Object[] { RegisterAccountStatus.UnknownError };
		if(args.length == 2)
		{
			System.out.println(args[0]);
			System.out.println(args[1]);
			if(args[0] instanceof Account && args[1] instanceof String)
			{
				if(database.registerAccount((Account)args[0], (String)args[1]))
					response[0] = RegisterAccountStatus.AccountCreated;
				else
					response[0] = RegisterAccountStatus.UsernameAlreadyExists;
			}
		}
		return response;
	}

	private Account getAuthenticatedAccount(Object[] args)
	{
		//first index in args should be my sessionID
		if(args.length >= 1 && args[0] instanceof String && sessionIDs.containsKey((String)args[0]))
			return sessionIDs.get((String)args[0]);
		
		return null;
	}
}
