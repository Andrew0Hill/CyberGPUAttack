/*
HashCompute

Calculates random string values, then send array to GPU for hashing.
 */
import com.jogamp.opencl.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Random;

public class HashCompute extends Application {
    // Hashing constants
    static final int HASH_PRIME = 16777619;
    static final long HASH_OFFSET = 2166136261L;

    static final int NUM_STRINGS = 11000000;
    static int global_size;
    static int local_size;
    static final int STRING_LENGTH = 6;
    static final String KERNEL_NAME = "HashKernel.cl";
    static final String KERNEL_FUNC_NAME = "compute_hashes";
    static final String LOG_NAME = "strings.txt";
    static HashMap<Integer,String> computed_hashes;
    static Random gen = new Random();
    static StringBuilder rand_string = new StringBuilder();


    Controller c;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxml_loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
        c = new Controller();
        fxml_loader.setController(c);
        Parent root = fxml_loader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 960, 720));

        primaryStage.show();
        // Wait until the page is fully loaded before continuing.
        c.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if(newValue == Worker.State.SUCCEEDED){
                    postLoad();
                }
            }
        });
    }
    // Function runs after page has been loaded.
    public void postLoad(){
        // Get reference to the window.
        JSObject jso = (JSObject) c.getEngine().executeScript("window");
        /*
         * Use JSObject to call the "test" Javascript method.
         * The 'call' method expects an Object[] containing each argument.
         * To pass an array as a parameter, we create a new Object[] with the first element
         * being a Integer[].
         */
        jso.call("test",new Object[] {new Integer[]{1,2,3,4,5}});

    }

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
        launch(args);
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
        System.out.println("Compute complete.");
        IntBuffer buf = output_hashes.getBuffer();
        computed_hashes = new HashMap<>(NUM_STRINGS);
        int curr;
        try{
            File hash_output = new File("hashes.txt");
            BufferedWriter output = new BufferedWriter(new FileWriter(hash_output));
            BufferedReader input = new BufferedReader(new FileReader(new File(LOG_NAME)));
            String current;
            int collisions = 0;
            while(buf.hasRemaining() && (current = input.readLine()) != null){
                curr = buf.get();
                if (curr != 0) {
                    output.write(Integer.toHexString(curr) + "\n");
                    if(computed_hashes.get(curr) != null){
                        ++collisions;
                        //System.out.println("Collision between " + computed_hashes.get(curr) + " and " + current);
                    }
                    computed_hashes.put(curr,current);
                }else{
                    break;
                }
            }
            System.out.println("There were " + collisions + " collisions detected.");
            input.close();
            output.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        context.release();

        System.out.println("Complete. Hash table has " + computed_hashes.size() + " entries.");
        }catch(java.io.IOException e){
            System.out.println("Could not locate kernel: " + KERNEL_NAME);
        }
    }
}
