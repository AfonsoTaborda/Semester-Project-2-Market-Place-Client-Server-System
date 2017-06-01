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
		networkServer.addServerMethod(Method.GetItems.getValue(), a -> handleGetItems(a));
		networkServer.addServerMethod(Method.GetCategories.getValue(), a -> handleGetCategories(a));
		
		//Requires authentication:
		networkServer.addServerMethod(Method.SellItem.getValue(), a -> handleSellItem(Arrays.copyOfRange(a, 1, a.length), getAuthenticatedAccount(a)));
		networkServer.addServerMethod(Method.BuyItem.getValue(), a -> handleBuyItem(Arrays.copyOfRange(a, 1, a.length), getAuthenticatedAccount(a)));
		networkServer.addServerMethod(Method.MakeOffer.getValue(), a -> handleMakeOffer(Arrays.copyOfRange(a, 1, a.length), getAuthenticatedAccount(a)));
	}
	
	private Object[] handleGetCategories(Object[] args)
	{
		//input:
		//0: Category - the category which is the parent to the returned categories, if null all parent-less categories are returned.
		
		//output: 
		//0: List<Category> - the categories found
		System.out.println("client wants to get categories");
		Category category = null;
		if(args.length == 1)
			if(args[0] instanceof Category)
				category = (Category)args[0];
		
		System.out.println(System.nanoTime());
		Object[] ret =  new Object[] { database.getCategories(category) };
		System.out.println(System.nanoTime());
		return ret;
	}
	
	private Object[] handleGetItems(Object[] args)
	{
		//input: 
		//0: Category - the category returned items have to be in (can be null)
		//1: String - the search predicate used to search for items (can be null)
		
		//output:
		//0: List<Item> - all items matching the search parameters
		
		Object[] result = new Object[1];
		
		if(args.length == 2)
		{
			if((args[0] == null || args[0] instanceof Category) && (args[1] == null || args[1] instanceof String))
			{
				Category arg1 = args[0] == null ? null : (Category)args[0];
				String arg2 = args[1] == null ? null : (String)args[1];
			
				result[0] = database.searchItems(arg1, arg2);
			}
		}
		
		return result;
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
		System.out.println("recv:");
		for(int i = 0; i < args.length; i++) System.out.println(args[i]);
		System.out.println("end");
		if (args.length == 2) {
			if (args[0] instanceof String && args[1] instanceof String) {
				Account theAccount = database.checkUserCredentials((String) args[0], (String) args[1]);
				System.out.println(theAccount);
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

	private Object[] handleMakeOffer(Object[] args, Account offeror)
	{
		// input:
		// 0: Item - item offeror wants to make an offer for.
		// 1: double - the offered item price
		
		// output:
		// 0: Boolean - was the offer successfully registered?
		System.out.println(offeror.getUserName() + " wants to make an offer");
		return new Object[] { false };
	}
	
	private Object[] handleBuyItem(Object[] args, Account buyer)
	{
		// input:
		// 0: Item - the item to be bought
		// 1: int - the quantity of said item to be bought
		Object[] response = new Object[]{BuyItemStatus.AllowedQuantityExceeded};
		if (args.length == 2) {
			System.out.println(args[0]);
			System.out.println(args[1]);
			if (args[0] instanceof Item && args[1] instanceof Integer) {
				if(database.itemExists((Item) args[0]))
				{
					if(database.buyItem(buyer, (Item) args[0], (int) args[1]))
					{
						response[0] = BuyItemStatus.Success;
						System.out.println(buyer.getUserName() + " successfully bought an item");
						return response;
					} 
					else 
					{
						System.out.println(buyer.getUserName() + " unsuccessfully bought an item: AMOUNT ALLOWED AXCEEDED");
					}
				}
				else
					System.out.println("Item doesn't exist");
			}
		}
		// output: 
		// 0: int - actual quantity bought
		return response;
	}
	
	private Object[] handleSellItem(Object[] args, Account owner)
	{
		//input:
		//0: Item - the item the user wants to sell
		
		//output:
		//0: boolean - successful or not
		
		
		return null;
	}
	

	private Account getAuthenticatedAccount(Object[] args)
	{
		//first index in args should be my sessionID
		if(args.length >= 1 && args[0] instanceof String && sessionIDs.containsKey((String)args[0]))
			return sessionIDs.get((String)args[0]);
		
		return null;
	}
}
