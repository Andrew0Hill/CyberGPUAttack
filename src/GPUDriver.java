import com.jogamp.opencl.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GPUDriver {

    // Constant values used for implementation of FNV-1a
    static final int HASH_PRIME = 16777619;
    static final long HASH_OFFSET = 2166136261L;
    static final int NUM_CELLS = 256;
    static final long ADDRESS_SPACE = 4294967296L;
    static final int NUM_STRINGS = 1100000;

    // Global and Local work sizes for OpenCL.
    static int global_size;
    static int local_size;

    CLContext context;
    CLPlatform[] platforms;
    CLPlatform platform;
    CLProgram program;
    CLDevice device;
    CLCommandQueue commandQueue;
    CLBuffer<ByteBuffer> input_strings;


    // Length of the strings to generate.
    static final int STRING_LENGTH = 12;

    // Name of OpenCL Kernel file.
    static final String KERNEL_NAME = "HashKernel.cl";

    // Name of OpenCL kernel function within file.
    static final String KERNEL_FUNC_NAME = "compute_hashes";

    // Name of file to log output.
    static final String LOG_NAME = "strings.txt";

    // HashMap to hold the hashes that we've computed so far.
    HashMap<Integer, String> computed_hashes;

    // Random generator to help generate random input strings.
    static Random gen;

    // StringBuilder class for building random strings.
    StringBuilder rand_string;

    // ArrayList to hold the number of collisions per cell in the grid.
    ArrayList<Integer> collisions_per_cell;

    GPUDriver() {
        computed_hashes = new HashMap<>(NUM_STRINGS);
        gen = new Random();
        rand_string = new StringBuilder();

        collisions_per_cell = new ArrayList<>(NUM_CELLS);
        for (int i = 0; i < NUM_CELLS; ++i) {
            collisions_per_cell.add(i, 0);
        }

        // OpenCL setup
        platforms = CLPlatform.listCLPlatforms();
        platform = platforms[1];
        context = CLContext.create(platform);
        device = context.getDevices()[0];
        try {
            program = context.createProgram(HashGUI.class.getResourceAsStream(KERNEL_NAME)).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        calcGlobalLocalSize();
        commandQueue = device.createCommandQueue();
        input_strings = context.createByteBuffer(NUM_STRINGS * STRING_LENGTH, CLMemory.Mem.READ_ONLY);
        computed_hashes = new HashMap<>(NUM_STRINGS);

    }


    // Generate a random string of length 'length' using a StringBuilder and Random.
    public String generateString(int length) {
        rand_string.setLength(0);
        for (int i = 0; i < length; ++i) {
            char c = (char) (gen.nextInt(57) + 65);
            rand_string.append(c);
        }
        return rand_string.toString();
    }


    // Generate and add num_of_strings random String objects to a ByteBuffer.
    public void addStringsToBuffer(ByteBuffer buf, int num_of_strings) {
        try {
            File output;
            BufferedWriter output_file;
            output = new File(LOG_NAME);
            output_file = new BufferedWriter(new FileWriter(output));

            for (int i = 0; i < NUM_STRINGS; ++i) {
                String s = generateString(STRING_LENGTH);
                buf.put(s.getBytes());
                output_file.write(s + "\n");
            }

            output_file.close();
            buf.rewind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPlatform(CLPlatform p) {
        platform = p;
    }

    public void setDevice(CLDevice d) {
        device = d;
    }

    public void calcGlobalLocalSize() {
        // Set the local_size
        local_size = Math.min(device.getMaxWorkGroupSize(), 256);

        // Round global_size up to the nearest multiple of local_size
        // that is larger than NUM_STRINGS to improve performance.
        int rmd = NUM_STRINGS % local_size;
        global_size = rmd == 0 ? NUM_STRINGS : NUM_STRINGS + local_size - rmd;
    }

    public void releaseContext(){
        System.out.println("Context released.");
        context.release();
    }

    public void insertIntoCell(int hashAddress) {
        int index = (int) Math.abs(Integer.toUnsignedLong(hashAddress) / (ADDRESS_SPACE / NUM_CELLS));
        collisions_per_cell.set(index, collisions_per_cell.get(index) + 1);
    }

    public ArrayList<Integer> calculateHashes() {

        try {
            addStringsToBuffer(input_strings.getBuffer(), NUM_STRINGS);
            // Output buffer holds the computed hashes for each input string.
            CLBuffer<IntBuffer> output_hashes = context.createIntBuffer(global_size, CLMemory.Mem.WRITE_ONLY);

            CLKernel kernel = program.createCLKernel(KERNEL_FUNC_NAME);

            // Add the input string buffer and the output hash buffer to the kernel.
            kernel.putArgs(input_strings, output_hashes);
            // Add the value for string length to the kernel.
            kernel.putArg(STRING_LENGTH);
            kernel.putArg(NUM_STRINGS);
            kernel.putArg(HASH_PRIME);
            kernel.putArg(HASH_OFFSET);

            commandQueue.putWriteBuffer(input_strings, false);
            commandQueue.put1DRangeKernel(kernel, 0, global_size, local_size);
            commandQueue.putReadBuffer(output_hashes, true);
            System.out.println("Compute complete.");
            IntBuffer buf = output_hashes.getBuffer();
            int curr;
            File hash_output = new File("hashes.txt");
            BufferedWriter output = new BufferedWriter(new FileWriter(hash_output));
            BufferedReader input = new BufferedReader(new FileReader(new File(LOG_NAME)));
            String current;
            int collisions = 0;
            while (buf.hasRemaining() && (current = input.readLine()) != null) {
                curr = buf.get();
                if (curr != 0) {
                    output.write(Integer.toHexString(curr) + "\n");
                    if (computed_hashes.get(curr) != null) {
                        ++collisions;
                        insertIntoCell(curr);
                        //System.out.println("Collision between " + computed_hashes.get(curr) + " and " + current);
                    }
                    computed_hashes.put(curr, current);
                } else {
                    break;
                }
            }
            System.out.println("There were " + collisions + " collisions detected.");
            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println("Complete. Hash table has " + computed_hashes.size() + " entries.");
        return collisions_per_cell;


    }
}
