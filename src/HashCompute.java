/*
HashCompute

Calculates random string values, then send array to GPU for hashing.
 */
import com.jogamp.opencl.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

public class HashCompute {
    // Hashing constants
    static final int HASH_PRIME = 16777619;
    static final long HASH_OFFSET = 2166136261L;

    static final int NUM_STRINGS = 1100000;
    static int global_size;
    static int local_size;
    static final int STRING_LENGTH = 6;
    static final String KERNEL_NAME = "HashKernel.cl";
    static final String KERNEL_FUNC_NAME = "compute_hashes";
    static final String LOG_NAME = "strings.txt";
    static Random gen = new Random();
    static StringBuilder rand_string = new StringBuilder();

    public static String generateString(int length){
        rand_string.setLength(0);
        for(int i = 0; i < length; ++i){
            char c = (char)(gen.nextInt(57) + 65);
            rand_string.append(c);
        }
        return rand_string.toString();
    }
    public static void addStringsToBuffer(ByteBuffer buf, int num_of_strings, boolean read_from_file){
        try {
            File output;
            BufferedReader input_file;
            BufferedWriter output_file;
            if (read_from_file) {
                input_file = new BufferedReader(new FileReader("input_strings.txt"));
                String current_line;
                int read_count = 0;
                while ((current_line = input_file.readLine()) != null && read_count < num_of_strings) {
                    buf.put(current_line.getBytes());
                }
                return;
            }
            output = new File(LOG_NAME);
            output_file = new BufferedWriter(new FileWriter(output));

            for (int i = 0; i < NUM_STRINGS; ++i) {
                String s = generateString(STRING_LENGTH);
                buf.put(s.getBytes());
                output_file.write(s+"\n");
            }

            output_file.close();
            buf.rewind();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){

        // Array of OpenCL Platforms
        // Should be made editable through GUI
        CLPlatform platforms[] = CLPlatform.listCLPlatforms();

        // Create OpenCL context with GPU device.
        // Should be made editable through GUI
        CLContext context = CLContext.create(platforms[1]);

        // Create a CLProgram to run the process.
        CLProgram program;
        try {
            program = context.createProgram(HashCompute.class.getResourceAsStream(KERNEL_NAME)).build();

        // Get a list of all devices in this context
        CLDevice devices[] = context.getDevices();

        // Select device.
        // Should be made editable through GUI.
        CLDevice device = devices[0];

        local_size = Math.min(device.getMaxWorkGroupSize(),256);
        int remain = NUM_STRINGS %local_size;
        global_size = NUM_STRINGS + local_size - remain;



        // Create the command queue on the selected device.
        CLCommandQueue queue = device.createCommandQueue();

        // Input buffer holds randomly generated input strings.
        CLBuffer<ByteBuffer> input_strings = context.createByteBuffer(global_size* STRING_LENGTH, CLMemory.Mem.READ_ONLY);

        addStringsToBuffer(input_strings.getBuffer(),NUM_STRINGS,false);
        // Output buffer holds the computed hashes for each input string.
        CLBuffer<IntBuffer> output_hashes = context.createIntBuffer(global_size, CLMemory.Mem.WRITE_ONLY);

        CLKernel kernel = program.createCLKernel(KERNEL_FUNC_NAME);

        // Add the input string buffer and the output hash buffer to the kernel.
        kernel.putArgs(input_strings,output_hashes);
        // Add the value for string length to the kernel.
        kernel.putArg(STRING_LENGTH);
        kernel.putArg(NUM_STRINGS);
        kernel.putArg(HASH_PRIME);
        kernel.putArg(HASH_OFFSET);

        queue.putWriteBuffer(input_strings,false);
        queue.put1DRangeKernel(kernel,0,global_size,local_size);
        queue.putReadBuffer(output_hashes,true);

        IntBuffer buf = output_hashes.getBuffer();
        try{
            File hash_output = new File("hashes.txt");
            BufferedWriter output = new BufferedWriter(new FileWriter(hash_output));
            while(buf.hasRemaining()){
                output.write(Integer.toHexString(buf.get()) + "\n");
            }
            output.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        context.release();

        }catch(java.io.IOException e){
            System.out.println("Could not locate kernel: " + KERNEL_NAME);
        }
    }
}
