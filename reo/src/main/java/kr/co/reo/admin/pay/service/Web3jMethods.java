package kr.co.reo.admin.pay.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

import kr.co.reo.common.dto.ContractDTO;

public class Web3jMethods {

	private Web3j web3j = Web3j.build(new HttpService()); // 디폴트 "http://localhost:8545"
	private String defaultAddress;
	private String contractAddress;
	private EthAccounts ethAccounts;
	private EthBlockNumber ethBlockNumber;
	private EthGetTransactionCount ethGetTransactionCount;
	private EthGetBalance ethGetBalance;
	private REOContract reoContract;
	private List<Credentials> credentialsList;
	private final String[] privateKeyList = { "44bdf03454b0beb884d5cf6d8fbf30f531e7a90ce7f29b8c5a89a54b78e86d64", // 0
			"766d1bd9b138c09b24bb5569b6cd757231048cc3bd1185e9734174ebc32b6699", // 1
			"1e257d465990b008ea77da44c85dddb2f56e87727a688dc287574c9162f24c3b", // 2
			"c4a0dfcff3f20aa48d05c493d76bdde36a1daa90a02982a18d55dadaf40305ac", // 3
			"7a40fbfba6cb47ea026ae7fb37ab76df2601a86c93fcebbbec0aee3ed0b2f3ac", // 4
			"daccf56cca23662f7fd1c23d5991a033576d52b8f3a4b1371e01812a96667746", // 5
			"6949681c2fb932abc55547d8f86a2ffea73acd4e231aa1ae4ea505daa2783e0a", // 6
			"8c23a200bb5695d2ac3f9c85563b4dc7c11b98d92a47ed0d74c803a1aa4d70f8", // 7
			"cd03d05f01224ad39ed220800456a7a8abc7d9bc5bb64557e3c72c56c19a6bfd", // 8
			"7c4e587db78b13798f1647aed9a8c0ce0bf1c47d7e3da090278fbc7f644dcde6" // 9
	};

	public Web3jMethods() {
		ethAccounts = new EthAccounts();
		ethBlockNumber = new EthBlockNumber();
		ethGetTransactionCount = new EthGetTransactionCount();
		ethGetBalance = new EthGetBalance();
		defaultAddress = getEthAccounts().getAccounts().get(0);
		createCredentialsList();
		int blockNum = Integer.parseInt(getBlockNumber().getBlockNumber().toString());
		if (blockNum <= 1) {
			sendEtherToDefaultAddress();
		}
		blockNum = Integer.parseInt(getBlockNumber().getBlockNumber().toString());
		if (blockNum <= 10) {
			distContract();
		} else {
			contractAddress = "0xB00B864239e774Fc08b077e763e2720caa7c8e46";
			loadContract();
		}
	}

	public void createWallet() {
		try {
			String walletFileName;
			walletFileName = WalletUtils.generateFullNewWalletFile("12REO345", new File("/"));
			System.out.println(walletFileName);
			Credentials cre = WalletUtils.loadCredentials("asdfg", "/" + walletFileName + ".json");
			System.out.println(cre.getAddress());
		} catch (IOException | CipherException | NoSuchAlgorithmException | NoSuchProviderException
				| InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}

	// 지갑 생성
	private void createCredentialsList() {
		credentialsList = new ArrayList<>();
		for (String pk : privateKeyList) {
			credentialsList.add(Credentials.create(pk));
		}
	}

	// 디폴트에 이더 전송
	private void sendEtherToDefaultAddress() {
		for (Credentials account : credentialsList) {
			try {
				Transfer.sendFunds(web3j, account, defaultAddress, BigDecimal.valueOf(99.999), Convert.Unit.ETHER)
						.sendAsync().get();
			} catch (InterruptedException | ExecutionException | IOException | TransactionException e) {
				e.printStackTrace();
			}
		}
	}

	// 계약서 최초 배포
	@SuppressWarnings("deprecation")
	private void distContract() {
		try {
			// I am willing to pay 1Gwei (1,000,000,000 wei or 0.000000001 ether) for each
			// unit of gas consumed by the transaction.
			BigInteger gasPrice = Convert.toWei("0", Unit.GWEI).toBigInteger();
			BigInteger gasLimit = BigInteger.valueOf(3000000);
			reoContract = REOContract.deploy(web3j, credentialsList.get(0), gasPrice, gasLimit).sendAsync().get();
			contractAddress = reoContract.getContractAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 계약서 최초 로드
	@SuppressWarnings("deprecation")
	private void loadContract() {
		try {
			BigInteger gasPrice = Convert.toWei("0", Unit.GWEI).toBigInteger();
			BigInteger gasLimit = BigInteger.valueOf(3000000);
			reoContract = REOContract.load(contractAddress, web3j, credentialsList.get(0), gasPrice, gasLimit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 현재 블록 번호
	public EthBlockNumber getBlockNumber() {
		try {
			ethBlockNumber = web3j.ethBlockNumber().sendAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return ethBlockNumber;
	}

	// 계좌
	public EthAccounts getEthAccounts() {
		try {
			ethAccounts = web3j.ethAccounts().sendAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return ethAccounts;
	}

	// 계좌 거래 수
	public EthGetTransactionCount getTransactionCount(String address) {
		try {
			ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameter.valueOf("latest"))
					.sendAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return ethGetTransactionCount;
	}

	// 계좌 잔액
	public EthGetBalance getEthBalance(String address) {
		if (address.equals("")) {
			address = defaultAddress;
		}
		try {
			ethGetBalance = this.web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).sendAsync()
					.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return ethGetBalance;
	}

	// 이더 전송
	public boolean sendEther(String toAddress, String value) {
//		BigInteger nonce = getTransactionCount(account).getTransactionCount();
		try {
			String balance = web3j
					.ethGetBalance(credentialsList.get(0).getAddress(), DefaultBlockParameter.valueOf("latest"))
					.sendAsync().get().getBalance().toString();
			if (Long.valueOf(balance) > Long.valueOf(value)) {
				Transfer.sendFunds(web3j, credentialsList.get(0), toAddress, BigDecimal.valueOf(Long.valueOf(value)),
						Convert.Unit.ETHER).sendAsync().get();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// 계약서 블록 생성
	public boolean mint(ContractDTO contractDTO) {
		BigInteger _price = BigInteger.valueOf(contractDTO.getPrice());
		BigInteger _payday = BigInteger.valueOf(contractDTO.getPayday());
		BigInteger _startdate = BigInteger.valueOf(contractDTO.getStartdate());
		BigInteger _enddate = BigInteger.valueOf(contractDTO.getEnddate());
		BigInteger _tokenId = BigInteger.valueOf(contractDTO.getTokenId());
		BigInteger weiValue = BigInteger.valueOf(0);
		RemoteFunctionCall<TransactionReceipt> mintCall = reoContract._mint(contractDTO.getName(), contractDTO.getId(),
				contractDTO.getLocation(), _price, _payday, _startdate, _enddate, _tokenId, weiValue);
		try {
			mintCall.sendAsync().get();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// 계약서 블록 가져오기
	public ContractDTO reoContract(long tokenId) {
		BigInteger _tokenId = BigInteger.valueOf(tokenId);
		ContractDTO contractDTO = null;
		try {
			Tuple7<String, String, String, BigInteger, BigInteger, BigInteger, BigInteger> result = reoContract
					.reocontract(_tokenId).sendAsync().get();
			if (!result.component2().equals("")) {
				contractDTO = new ContractDTO();
				contractDTO.setTokenId(tokenId);
				contractDTO.setName(result.component1());
				contractDTO.setId(result.component2());
				contractDTO.setLocation(result.component3());
				contractDTO.setPrice(result.component4().longValue());
				contractDTO.setPayday(result.component5().longValue());
				contractDTO.setStartdate(result.component6().longValue());
				contractDTO.setEnddate(result.component7().longValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return contractDTO;
	}

	// 계약서 개수
	public long getCount() {
		long count = -1L;
		RemoteFunctionCall<BigInteger> getCountCall = reoContract.getCount();
		try {
			BigInteger _count = getCountCall.sendAsync().get();
			count = _count.longValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	// 계약서 ID
	public long getTokenId(long index) {
		long indexL = -1L;
		BigInteger _index = BigInteger.valueOf(index);
		RemoteFunctionCall<BigInteger> getTokenId = reoContract.getTokenId(_index);
		try {
			indexL = getTokenId.sendAsync().get().longValue();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return indexL;
	}

	// 계약서 ID 리스트
	public List<Long> getTokenIdList(int limit, int offset, int count) {
		long limitL = (long) limit;
		long offsetL = (long) offset;
		long start = count - offsetL;
		long end = start - limitL;
		if (start < 0L) {
			start = count % limitL;
		}
		if (end < 0L) {
			end = 0L;
		}
		List<Long> tokenList = new ArrayList<>();
		try {
			for (long i = start; i >= end; i--) {
				RemoteFunctionCall<BigInteger> getTokenId = reoContract.getTokenId(BigInteger.valueOf(i));
				tokenList.add(getTokenId.sendAsync().get().longValue());
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return tokenList;
	}

	// 계약서 리스트
	public List<ContractDTO> reoContractList(int limit, int offset, int count) {
		List<Long> tokenList = getTokenIdList(limit, offset, count);
		List<ContractDTO> reoContractList = new ArrayList<>();
		try {
			for (long l : tokenList) {
				reoContractList.add(reoContract(l));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reoContractList;
	}

	public String getDefaultAddress() {
		return defaultAddress;
	}
}