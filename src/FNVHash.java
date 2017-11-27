/*
Simple implementation of the FNV-1a hash for a CPU. Used for the interactive hash value checker.
 */
public class FNVHash {
    // Constant values used for implementation of FNV-1a
    static final int HASH_PRIME = 16777619;
    static final long HASH_OFFSET = 2166136261L;

    static String hash(String input){
        char[] chars = input.toCharArray();
        long hash = HASH_OFFSET;
        for(char ch : chars){
            hash = hash ^ ch;
            hash *= HASH_PRIME;
        }

        return Integer.toHexString((int) hash).toUpperCase();
    }
}
