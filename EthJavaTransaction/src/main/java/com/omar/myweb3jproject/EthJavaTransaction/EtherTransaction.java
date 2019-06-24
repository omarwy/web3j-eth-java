package com.omar.myweb3jproject.EthJavaTransaction;

import org.web3j.protocol.Web3j;
//import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.*;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.utils.Convert;
import java.math.BigInteger;
import java.util.Optional;

public class EtherTransaction {
	
	public static void main(String[] args) throws Exception {
		transferEther("http://localhost:8545", "my-account-on-geth", "0x46d562de02b3bebaedbd883248f33c3682a21a1b",
				"0xccccfcba07f64a87c864812e9b27227590e93360", "10");
	}

	/**
	 * @param ethereumClient            depending which client you are using,
	 *                                  geth/partity/infura - put in the address
	 * @param accountOnClientBlockchain              
	 * @param senderAddress             the account address of the sender who
	 *                                  wants to transfer ether
	 * @param recipientAddress          the account address of the recipient who
	 *                                  receive the ether sent
	 * @param etherAmount               the sender specifies who much ether he/she
	 *                                  is sending
	 * @throws Exception
	 */
	public static void transferEther(String ethereumClient, String accountOnClientBlockchain, String senderAddress,
			String recipientAddress, String etherAmount) throws Exception {

		/*
		 * unlock account by creating an instance of web3j that supports Geth/Parity
		 * admin commands: for this example geth is used
		 */

		Admin admin = Admin.build(new HttpService(ethereumClient));

		Web3j web3j = Web3j.build(new HttpService(ethereumClient));

		// unlock the account, and providing this was successful, send a transaction
		PersonalUnlockAccount personalUnlockAccount = admin
				.personalUnlockAccount(senderAddress, accountOnClientBlockchain).send();
		if (!personalUnlockAccount.accountUnlocked()) {
			System.out.println("Account unavailable");
			return;
		}

		/*
		 * The nonce is an increasing numeric value which is used to uniquely identify
		 * transactions. A nonce can only be used once and until a transaction is mined,
		 * it is possible to send multiple versions of a transaction with the same
		 * nonce, however, once mined, any subsequent submissions will be rejected. You
		 * can obtain the next available nonce via the:
		 */
		EthGetTransactionCount txCount = web3j.ethGetTransactionCount(senderAddress, DefaultBlockParameterName.LATEST)
				.sendAsync().get();
		BigInteger nonce = txCount.getTransactionCount();
		BigInteger amount = Convert.toWei(etherAmount, Convert.Unit.ETHER).toBigInteger();

		// Transactions for sending in this manner should be created via
		Transaction transaction = Transaction.createEtherTransaction(senderAddress, nonce, ManagedTransaction.GAS_PRICE,
				Contract.GAS_LIMIT, recipientAddress, amount);

		// The transaction is then sent using
		EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).sendAsync().get();

		String transactionHash = ethSendTransaction.getTransactionHash();
		System.out.print("\nTxHash: " + transactionHash + "\nMining... ");

		System.out
				.println("\nThe transaction could be processed within the given time limit . Is mining still active?");
	}
}