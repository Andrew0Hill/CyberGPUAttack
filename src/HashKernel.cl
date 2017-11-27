kernel void compute_hashes(global const char* strings, global int* hashes, int string_size, int num_strings, int prime, long offset) {
    // Global ID
    int id = get_global_id(0);
    // Array offset for this global ID
    int arr_pos = id * string_size;

    if (id >= num_strings){
        return;
    }
    // Constant hash offset from FNV-1a specification.
    unsigned int hash = offset;
    // Counter for byte within the byte string
    int count = 0;
    while(count < string_size){
        hash = hash ^ strings[arr_pos+count];
        hash = hash * prime;
        ++count;
    }

    hashes[id] = hash;
}