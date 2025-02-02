import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Transfer {
    /**
     * Transfers Ether from a sender's account to a recipient's account.
     *
     * @param senderPrivateKey The private key of the sender's Ethereum account (keep this secure).
     * @param recipientAddress The recipient's Ethereum address.
     * @param amountInEther    The amount to transfer in Ether.
     * @return The transaction hash if the transfer is successful; otherwise, returns null.
     * @throws IOException If there is an issue communicating with the Ethereum node.
     */
    public static String transfer(String senderPrivateKey, String recipientAddress, BigDecimal amountInEther) throws IOException {
        /* Network Configuration. In practice, you should config this once and reuse it. */
        String rpcUrl = "YOUR_RPC_NODE_URL";
        // Connect to the Ethereum network.
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        // Retrieve the chain ID of the connected network
        EthChainId chainIdResponse = web3j.ethChainId().send();
        long chainId = chainIdResponse.getChainId().longValue();

        /* Sender Account Configuration */
        // Create credentials from the sender's private key
        Credentials credentials = Credentials.create(senderPrivateKey);
        String senderAddress = credentials.getAddress();
        // Get the current nonce for the sender's account
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                senderAddress, DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        /* Transaction Amount and Fee Configuration */
        // Convert the amount from Ether to Wei (1 ETH = 10^18 Wei)
        BigInteger value = Convert.toWei(amountInEther, Convert.Unit.ETHER).toBigInteger();
        // Get the current Gas price
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        BigInteger gasPrice = ethGasPrice.getGasPrice();
        // Gas limit for a standard Ether transfer (typically 21,000)
        BigInteger gasLimit = BigInteger.valueOf(21000);

        /* Transfer */
        // Create the transaction object
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, recipientAddress, value);
        // Sign the transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        // Send the transaction
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
        // Return the transaction hash if successful, or null if the transaction failed
        return ethSendTransaction.getTransactionHash();
    }

    public static void main(String[] args) throws Exception {
        // The private key of the sender's Ethereum account (keep this private and secure)
        String privateKey = "YOUR_PRIVATE_KEY";
        // The recipient's Ethereum address
        String recipientAddress = "YOUR_RECIPIENT_ADDRESS";
        // The transfer amount in Ether
        BigDecimal amountInEther = new BigDecimal("0.001");
        // Perform the transfer
        String transactionHash = Transfer.transfer(privateKey, recipientAddress, amountInEther);
        System.out.println("Transaction Hash: " + transactionHash);
    }
}