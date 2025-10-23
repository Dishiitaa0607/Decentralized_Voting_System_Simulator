import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Block class representing each vote block in the chain
class Block {
    public String previousHash;
    public String data; // vote or voter info
    public String hash;
    public long timestamp;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        try {
            String input = previousHash + Long.toString(timestamp) + data;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

// Blockchain class holding the chain of vote blocks
class Blockchain {
    public List<Block> chain;

    public Blockchain() {
        chain = new ArrayList<>();
        // Add genesis block (first block with no previous)
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block("Genesis Block", "0");
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        newBlock.previousHash = getLatestBlock().hash;
        newBlock.hash = newBlock.calculateHash();
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for(int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);
            if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
                return false;
            }
            if(!currentBlock.previousHash.equals(previousBlock.hash)) {
                return false;
            }
        }
        return true;
    }
}

// Simple Voting System incorporating voter registration and vote casting
public class VotingSystemSimulator {
    private Blockchain votingBlockchain;
    private List<String> registeredVoters;
    private List<String> votersWhoVoted;

    public VotingSystemSimulator() {
        votingBlockchain = new Blockchain();
        registeredVoters = new ArrayList<>();
        votersWhoVoted = new ArrayList<>();
    }

    public boolean registerVoter(String voterId) {
        if(registeredVoters.contains(voterId)) {
            System.out.println("Voter " + voterId + " already registered.");
            return false;
        }
        registeredVoters.add(voterId);
        System.out.println("Voter " + voterId + " registered successfully.");
        return true;
    }

    public boolean castVote(String voterId, String candidate) {
        if(!registeredVoters.contains(voterId)) {
            System.out.println("Voter " + voterId + " is not registered.");
            return false;
        }
        if(votersWhoVoted.contains(voterId)) {
            System.out.println("Voter " + voterId + " has already voted.");
            return false;
        }
        // Create vote record as block data
        String voteData = "Voter: " + voterId + ", Vote: " + candidate;
        Block newVoteBlock = new Block(voteData, votingBlockchain.getLatestBlock().hash);
        votingBlockchain.addBlock(newVoteBlock);
        votersWhoVoted.add(voterId);
        System.out.println("Vote cast successfully by " + voterId);
        return true;
    }

    public void verifyChain() {
        System.out.println("Is blockchain valid? " + votingBlockchain.isChainValid());
    }

    public void printBlockchain() {
        System.out.println("Voting Blockchain:");
        for(Block block : votingBlockchain.chain) {
            System.out.println("---------------");
            System.out.println("Data: " + block.data);
            System.out.println("Hash: " + block.hash);
            System.out.println("Previous Hash: " + block.previousHash);
            System.out.println("Timestamp: " + block.timestamp);
        }
    }

    public static void main(String[] args) {
        VotingSystemSimulator votingSystem = new VotingSystemSimulator();

        votingSystem.registerVoter("VOTER001");
        votingSystem.registerVoter("VOTER002");

        votingSystem.castVote("VOTER001", "CandidateA");
        votingSystem.castVote("VOTER002", "CandidateB");

        votingSystem.printBlockchain();
        votingSystem.verifyChain();

        // Trying to vote again with same voter (should be rejected)
        votingSystem.castVote("VOTER001", "CandidateB");
    }
}
