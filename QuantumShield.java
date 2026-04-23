import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.net.SocketException;


public class QuantumShield {
    static final Scanner scanner = new Scanner(System.in);
    static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.run();
    }

    static class Menu {
        ExecutorService executor = Executors.newCachedThreadPool();
        ChatServer server;
        SecretKey sessionKey;

        void run() {
            while (true) {
                System.out.println("=== QuantumShield: Post-Quantum Secure Chat ===");
                System.out.println("1) Simulate BB84 key exchange");
                System.out.println("2) Run Toy Lattice KEM (encaps/decaps)");
                System.out.println("3) Start Chat Server (local)");
                System.out.println("4) Start Chat Client (connect)");
                System.out.println("5) Demonstrate RSA break (brute-force)");
                System.out.println("6) Benchmark RSA vs ToyKEM (keygen/enc/dec)");
                System.out.println("7) Exit");
                System.out.print("Choose: ");
                String choice = scanner.nextLine().trim();
                try {
                    switch (choice) {
                        case "1": runBB84(); break;
                        case "2": runLatticeKEM(); break;
                        case "3": startServer(); break;
                        case "4": startClient(); break;
                        case "5": rsaBreakDemo(); break;
                        case "6": benchmark(); break;
                        case "7": shutdown(); return;
                        default: System.out.println("Invalid option");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace(System.out);
                }
            }
        }

        void runBB84() {
            final int MAX_RETRIES = 3;
            int attempts = 0;

            while (attempts < MAX_RETRIES) {
                attempts++;

                try {
                    System.out.println("\n--- BB84 Quantum Key Exchange (Attempt " + attempts + ") ---");

                    // 1) Run BB84 simulation
                    BB84Simulator sim = new BB84Simulator(128);
                    BB84Simulator.Result r = sim.runSimulation(0.15);

                    if (r.sharedKey == null || r.sharedKey.length == 0) {
                        throw new IllegalStateException("Shared key empty or null");
                    }

                    // 2) BIT-LEVEL analysis
                    List<Integer> bits = new ArrayList<>();
                    for (byte b : r.sharedKey) {
                        for (int i = 7; i >= 0; i--) {
                            bits.add((b >> i) & 1);
                        }
                    }

                    long zeros = bits.stream().filter(x -> x == 0).count();
                    long ones = bits.size() - zeros;

                    double balance = Math.abs(zeros - ones) / (double) bits.size();

                    if (balance > 0.30) {
                        throw new ArithmeticException("Key too unbalanced (" +
                                String.format("%.2f", balance * 100) + "% imbalance)");
                    }

                    // 3) Print key stats
                    System.out.println("Raw key length: " + r.rawKeyLength);
                    System.out.println("Shared key (hex): " + bytesToHex(r.sharedKey));
                    System.out.println("Bit count: " + bits.size());
                    System.out.println("Zero bits: " + zeros + ", One bits: " + ones);
                    System.out.println("Imbalance: " + String.format("%.2f%%", balance * 100));

                    // 4) Set AES session key
                    showEntropyHeatmap("BB84 Key Entropy Heatmap", r.sharedKey);

                    sessionKey = CryptoUtils.deriveAESKeyFromBytes(r.sharedKey);
                    System.out.println("Session AES key established for chat.");

                    // 5) Log success
                    try (FileWriter fw = new FileWriter("bb84_stats.log", true)) {
                        fw.write("SUCCESS | bits=" + bits.size() +
                                " | zeros=" + zeros + " | ones=" + ones +
                                " | imbalance=" + balance + "\n");
                    }

                    return; // success, exit

                } catch (IllegalStateException | ArithmeticException e) {
                    System.out.println("BB84 Warning: " + e.getMessage());

                } catch (Exception e) {
                    System.out.println("BB84 Error: " + e.getMessage());
                    e.printStackTrace();
                }

                // Log failure
                try (FileWriter fw = new FileWriter("bb84_stats.log", true)) {
                    fw.write("FAILED attempt " + attempts + "\n");
                } catch (IOException ignore) {}

                System.out.println("Retrying...\n");
            }

            System.out.println("BB84 failed after " + MAX_RETRIES + " attempts.");
        }

        void runLatticeKEM() {
            System.out.println("=== Post-Quantum Lattice KEM ===");
            System.out.println();

            try {
                System.out.println("Step 1: Initializing KEM...");
                LatticeKEM.KEM kem = new LatticeKEM.KEM(16, 257);

                System.out.println("Step 2: Generating key pair...");
                LatticeKEM.LatticeKeyPair kp = kem.keyGen();

                System.out.println("Step 3: Encapsulating shared secret...");
                LatticeKEM.LatticeEncapsulation enc = kem.encapsulate(kp.pk);

                System.out.println("Step 4: Decapsulating shared secret...");
                byte[] dec = kem.decapsulate(kp.sk, enc.ciphertext);

                System.out.println();
                System.out.println("--------------- RESULT ---------------");
                System.out.println();

                System.out.println("Ciphertext size: " + enc.ciphertext.length + " bytes");
                System.out.println();

                String senderKey = bytesToHex(enc.shared);
                String receiverKey = bytesToHex(dec);

                System.out.println("Sender's derived key:");
                System.out.println(senderKey);
                System.out.println();

                System.out.println("Receiver's derived key:");
                System.out.println(receiverKey);
                System.out.println();

                if (senderKey.equals(receiverKey)) {
                    System.out.println("Status: Keys match. Secure channel established.");
                } else {
                    System.out.println("Status: Keys do not match.");
                    System.out.println("Explanation: This indicates a simulation failure or possible tampering.");
                }
            
                showEntropyHeatmap("Lattice KEM Key Entropy Heatmap", dec);

                sessionKey = CryptoUtils.deriveAESKeyFromBytes(dec);
                System.out.println();
                System.out.println("AES Session Key: Ready for encrypted chat.");
                System.out.println("-------------------------------------------");

            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid KEM parameters. Details: " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Security Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error occurred during Lattice KEM execution.");
                System.out.println("Details: " + e.getMessage());
            }
        }

        private void startServer() {
            System.out.print("Enter port to listen on (e.g., 9000): ");
            String input = scanner.nextLine().trim();

            int port;
            try {
                port = Integer.parseInt(input);
                if (port < 1 || port > 65535) {
                    System.out.println("Error: Port must be between 1 and 65535.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid port. Please enter a valid integer.");
                return;
            }

            // Prepare server with sessionKey
            ChatServer server;
            try {
                server = new ChatServer(port, sessionKey);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                return;
            } catch (Exception e) {
                System.out.println("Unexpected error while creating server:");
                e.printStackTrace(System.out);
                return;
            }

            // Launch server inside executor
        
            executor.submit(() -> {
                try {
                    server.start();
                } 
                // Connection-related failures
                catch (java.net.BindException be) {
                    System.out.println("Error: Port " + port + " is already in use.");
                }
                catch (java.net.SocketException se) {
                    System.out.println("Network error: " + se.getMessage());
                } 
                // Any other failure
                catch (Exception e) {
                    System.out.println("Server crashed: " + e.getMessage());
                    e.printStackTrace(System.out);
                }
            });

            System.out.println("Server started on port " + port + "\n");
        }



        void startClient() throws Exception {
    	    System.out.print("Server host (localhost): ");
    	    String hostIn = scanner.nextLine().trim();
	    System.out.print("Server port (9000): ");
            String portIn = scanner.nextLine().trim();

            // Handle case user typed port into host field (common mistake)
    	    String host = hostIn;
    	    String portStr = portIn;
    	    if (hostIn.length() > 0 && hostIn.chars().allMatch(Character::isDigit) && (portIn.isEmpty())) {
            // user entered only a number in host prompt and left port blank -> treat that as port
            portStr = hostIn;
            host = "localhost";
   	    }

    	    if (host.isEmpty()) host = "localhost";
            int port;
            try {
        	port = portStr.isEmpty() ? 9000 : Integer.parseInt(portStr);
        	if (port < 1 || port > 65535) throw new NumberFormatException("port out of range");
    	    } catch (NumberFormatException e) {
        	System.out.println("Invalid port. Use a number 1-65535.");
        	return;
    	    }

    	ChatClient client = new ChatClient(host, port, sessionKey);
    	client.runInteractive();
	}


        void rsaBreakDemo() throws Exception {
            System.out.print("Generate RSA key size (bits, small for demo e.g., 32/64/128): ");
            int bits = Integer.parseInt(scanner.nextLine().trim());
            RSAAttack.demo(bits);
        }

        void benchmark() throws Exception {
            System.out.println("Benchmarking small RSA vs ToyLattice KEM");
            System.out.print("Repeat count: ");
            int n = Integer.parseInt(scanner.nextLine().trim());
            BenchmarkSuite.run(n);
        }

        void shutdown() {
            System.out.println("Shutting down.");
            try { if (server != null) server.stop(); } catch (Exception ignored) {}
            executor.shutdownNow();
        }
    }

    static class BB84Simulator {
        static class Result {
            byte[] sharedKey;
            int rawKeyLength;
        }

        int keyLen;
        SecureRandom rnd = new SecureRandom();

        BB84Simulator(int keyLen) { this.keyLen = keyLen; }

        Result runSimulation(double eavesdropProb) {
            int n = keyLen * 2;
            int[] aliceBits = new int[n];
            int[] aliceBases = new int[n];
            int[] bobBases = new int[n];
            int[] transmitted = new int[n];

            for (int i = 0; i < n; i++) {
                aliceBits[i] = rnd.nextBoolean() ? 1 : 0;
                aliceBases[i] = rnd.nextBoolean() ? 1 : 0;
                bobBases[i] = rnd.nextBoolean() ? 1 : 0;
                transmitted[i] = aliceBits[i];
            }

            boolean eavesdrop = rnd.nextDouble() < eavesdropProb;
            if (eavesdrop) {
                for (int i = 0; i < n; i++) {
                    if (rnd.nextBoolean()) transmitted[i] = rnd.nextBoolean() ? 1 : 0;
                }
            }

            List<Integer> kept = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (aliceBases[i] == bobBases[i]) kept.add(i);
            }

            List<Byte> keyBits = new ArrayList<>();
            int sampleSize = Math.max(1, kept.size() / 8);
            List<Integer> sampleIndices = kept.stream().limit(sampleSize).collect(Collectors.toList());
            int errors = 0;
            for (int idx : sampleIndices) if (transmitted[idx] != aliceBits[idx]) errors++;

            double errorRate = sampleSize == 0 ? 0 : (double) errors / sampleSize;
            if (errorRate > 0.15) {
                Result r = new Result();
                r.rawKeyLength = 0;
                r.sharedKey = new byte[16];
                return r;
            }

            for (int idx : kept) {
                keyBits.add((byte) aliceBits[idx]);
                if (keyBits.size() == keyLen) break;
            }

            byte[] keyBytes = packBitsToBytes(keyBits);
            Result r = new Result();
            r.rawKeyLength = keyBits.size();
            r.sharedKey = CryptoUtils.sha256(keyBytes);
            return r;
        }

        static byte[] packBitsToBytes(List<Byte> bits) {
            int len = bits.size();
            ByteBuffer buf = ByteBuffer.allocate((len + 7) / 8);
            int acc = 0, count = 0;
            for (byte b : bits) {
                acc = (acc << 1) | (b & 1);
                count++;
                if (count == 8) {
                    buf.put((byte) acc);
                    acc = 0; count = 0;
                }
            }
            if (count > 0) {
                acc <<= (8 - count);
                buf.put((byte) acc);
            }
            return Arrays.copyOf(buf.array(), buf.position());
        }
    }

    static class LatticeKEM {
        static class KEM {
            int n;
            int q;
            SecureRandom rnd = new SecureRandom();
            KEM(int n, int q) { this.n = n; this.q = q; }

            LatticeKeyPair keyGen() {
                LatticeKeyPair kp = new LatticeKeyPair();
                kp.sk = new byte[n];
                kp.pk = new byte[n];
                for (int i = 0; i < n; i++) kp.sk[i] = (byte) rnd.nextInt(q);
                for (int i = 0; i < n; i++) kp.pk[i] = (byte) ((kp.sk[(i + 1) % n] * 3 + rnd.nextInt(5)) % q);
                return kp;
            }

            LatticeEncapsulation encapsulate(byte[] pk) {
                byte[] r = new byte[n];
                for (int i = 0; i < n; i++) r[i] = (byte) rnd.nextInt(q);
                byte[] c = new byte[n];
                for (int i = 0; i < n; i++) c[i] = (byte) ((pk[i] * r[i] + rnd.nextInt(3)) % q);
                LatticeEncapsulation enc = new LatticeEncapsulation();
                enc.ciphertext = c;
                enc.shared = CryptoUtils.sha256(CryptoUtils.concat(r, c));
                return enc;
            }

            byte[] decapsulate(byte[] sk, byte[] c) {
                byte[] rhat = new byte[n];
                for (int i = 0; i < n; i++) rhat[i] = (byte) ((c[i] - sk[i] + q) % q);
                return CryptoUtils.sha256(CryptoUtils.concat(rhat, c));
            }
        }

        static class LatticeKeyPair { byte[] pk; byte[] sk; }
        static class LatticeEncapsulation { byte[] ciphertext; byte[] shared; }

        int n;
        int q;
        LatticeKEM(int n, int q) { this.n = n; this.q = q; }

        LatticeKeyPair keyGen() {
            LatticeKeyPair kp = new LatticeKeyPair();
            kp.sk = new byte[n];
            kp.pk = new byte[n];
            for (int i = 0; i < n; i++) kp.sk[i] = (byte) random.nextInt(q);
            for (int i = 0; i < n; i++) kp.pk[i] = (byte) ((kp.sk[(i + 1) % n] * 3 + random.nextInt(5)) % q);
            return kp;
        }

        LatticeEncapsulation encapsulate(byte[] pk) {
            byte[] r = new byte[n];
            for (int i = 0; i < n; i++) r[i] = (byte) random.nextInt(q);
            byte[] c = new byte[n];
            for (int i = 0; i < n; i++) c[i] = (byte) ((pk[i] * r[i] + random.nextInt(3)) % q);
            LatticeEncapsulation enc = new LatticeEncapsulation();
            enc.ciphertext = c;
            enc.shared = CryptoUtils.sha256(CryptoUtils.concat(r, c));
            return enc;
        }

        byte[] decapsulate(byte[] sk, byte[] ciphertext) {
            byte[] rhat = new byte[n];
            for (int i = 0; i < n; i++) rhat[i] = (byte) ((ciphertext[i] - sk[i] + q) % q);
            return CryptoUtils.sha256(CryptoUtils.concat(rhat, ciphertext));
        }
    }

    static class CryptoUtils {
        static byte[] sha256(byte[] in) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                return md.digest(in);
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        static SecretKey deriveAESKeyFromBytes(byte[] keyMaterial) {
            byte[] k = Arrays.copyOf(keyMaterial, 16);
            return new SecretKeySpec(k, "AES");
        }

        static byte[] aesGcmEncrypt(SecretKey key, byte[] plaintext) throws Exception {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ct = cipher.doFinal(plaintext);
            ByteBuffer b = ByteBuffer.allocate(iv.length + ct.length);
            b.put(iv);
            b.put(ct);
            return b.array();
        }

        static byte[] aesGcmDecrypt(SecretKey key, byte[] ciphertext) throws Exception {
            ByteBuffer b = ByteBuffer.wrap(ciphertext);
            byte[] iv = new byte[12];
            b.get(iv);
            byte[] ct = new byte[b.remaining()];
            b.get(ct);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(ct);
        }

        static byte[] randomBytes(int n) {
            byte[] b = new byte[n];
            random.nextBytes(b);
            return b;
        }

        static byte[] concat(byte[] a, byte[] b) {
            byte[] r = new byte[a.length + b.length];
            System.arraycopy(a, 0, r, 0, a.length);
            System.arraycopy(b, 0, r, a.length, b.length);
            return r;
        }
    }

    static class ChatServer {
        int port;
        ServerSocket serverSocket;
        volatile boolean running = false;
        SecretKey sessionKey;
        ExecutorService pool = Executors.newCachedThreadPool();
        List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

        ChatServer(int port, SecretKey sessionKey) {
            this.port = port;
            this.sessionKey = sessionKey;
        }

        boolean isRunning() { return running; }

        void start() throws Exception {
            serverSocket = new ServerSocket(port);
            running = true;
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    ClientHandler h = new ClientHandler(s, sessionKey);
                    clients.add(h);
                    pool.submit(h);
                } catch (SocketException se) {
                    // socket closed during shutdown
                    break;
                }
            }
        }

        void stop() throws Exception {
            running = false;
            if (serverSocket != null) serverSocket.close();
            pool.shutdownNow();
        }

        class ClientHandler implements Runnable {
            Socket socket;
            SecretKey aesKey;
            DataInputStream in;
            DataOutputStream out;

            ClientHandler(Socket s, SecretKey sessionKey) throws IOException {
                this.socket = s;
                this.aesKey = sessionKey != null ? sessionKey : CryptoUtils.deriveAESKeyFromBytes(CryptoUtils.sha256("default".getBytes()));
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());
            }

            public void run() {
                try {
                    while (!socket.isClosed()) {
                        int len = in.readInt();
                        byte[] data = new byte[len];
                        in.readFully(data);
                        byte[] plain = CryptoUtils.aesGcmDecrypt(aesKey, data);
                        String msg = new String(plain, StandardCharsets.UTF_8);
                        broadcast(msg);
                    }
                } catch (EOFException eof) {
                    // client closed connection
                } catch (Exception e) {
                    // other errors
                } finally {
                    try { socket.close(); } catch (Exception ignored) {}
                    clients.remove(this);
                }
            }

            void sendEncrypted(String msg) {
                try {
                    byte[] ct = CryptoUtils.aesGcmEncrypt(aesKey, msg.getBytes(StandardCharsets.UTF_8));
                    out.writeInt(ct.length);
                    out.write(ct);
                    out.flush();
                } catch (Exception e) {
                    // ignore send errors per-client
                }
            }

            void broadcast(String msg) {
                synchronized (clients) {
                    for (ClientHandler ch : new ArrayList<>(clients)) {
                        if (ch != this) ch.sendEncrypted(msg);
                    }
                    System.out.println("[broadcast] " + msg);
                }
            }
        }
    }

    static class ChatClient {
        String host;
        int port;
        SecretKey sessionKey;
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        ExecutorService ex = Executors.newSingleThreadExecutor();

        ChatClient(String host, int port, SecretKey sessionKey) {
            this.host = host;
            this.port = port;
            this.sessionKey = sessionKey != null ? sessionKey : CryptoUtils.deriveAESKeyFromBytes(CryptoUtils.sha256("default".getBytes()));
        }

        void runInteractive() throws Exception {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            ex.submit(this::reader);
            System.out.println("Connected to server. Type messages, 'exit' to quit.");
            while (true) {
                String line = scanner.nextLine();
                if ("exit".equalsIgnoreCase(line)) break;
                byte[] ct = CryptoUtils.aesGcmEncrypt(sessionKey, line.getBytes(StandardCharsets.UTF_8));
                out.writeInt(ct.length);
                out.write(ct);
                out.flush();
            }
            socket.close();
            ex.shutdownNow();
        }

        void reader() {
            try {
                while (!socket.isClosed()) {
                    int len = in.readInt();
                    byte[] data = new byte[len];
                    in.readFully(data);
                    byte[] plain = CryptoUtils.aesGcmDecrypt(sessionKey, data);
                    System.out.println("[remote] " + new String(plain, StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                // reader ended
            }
        }
    }

static class RSAAttack {
    static void demo(int bits) throws Exception {
        if (bits < 512) {
            // Small demo mode: create a tiny semiprime and factor it (educational)
            smallSemiprimeDemo(bits);
            return;
        }

        // Real RSA generation path (requires >= 512)
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(bits, random);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.interfaces.RSAPublicKey pub = (java.security.interfaces.RSAPublicKey) kp.getPublic();
        BigInteger n = pub.getModulus();
        System.out.println("Generated RSA modulus n = " + n);
        long start = System.currentTimeMillis();
        BigInteger factor = bruteForceFactor(n);
        long dur = System.currentTimeMillis() - start;
        if (factor == null) System.out.println("Failed to factor n (within simple bounds).");
        else {
            System.out.println("Found factor: " + factor);
            System.out.println("Other factor: " + n.divide(factor));
            System.out.println("Time (ms): " + dur);
        }
    }

    // educational small-semiprime generator + factorer
    static void smallSemiprimeDemo(int bits) {
        if (bits < 8) bits = 8; // minimal for demo
        int pbits = Math.max(2, bits / 2);
        int qbits = Math.max(2, bits - pbits);
        BigInteger p = BigInteger.probablePrime(pbits, random);
        BigInteger q = BigInteger.probablePrime(qbits, random);
        BigInteger n = p.multiply(q);
        System.out.println("Demo small RSA modulus n = p * q (p and q are small primes)");
        System.out.println("n = " + n);
        long start = System.currentTimeMillis();
        BigInteger factor = bruteForceFactor(n);
        long dur = System.currentTimeMillis() - start;
        if (factor == null) {
            System.out.println("Brute-force factorization failed within bounds.");
        } else {
            System.out.println("Found factor: " + factor);
            System.out.println("Other factor: " + n.divide(factor));
            System.out.println("Time (ms): " + dur);
            System.out.println("Original p (for comparison): " + p);
            System.out.println("Original q (for comparison): " + q);
        }
    }

    static BigInteger bruteForceFactor(BigInteger n) {
        BigInteger two = BigInteger.valueOf(2);
        if (n.mod(two).equals(BigInteger.ZERO)) return two;
        BigInteger limit = BigInteger.valueOf(1_000_000); // keep brute force bounded
        for (BigInteger i = BigInteger.valueOf(3); i.compareTo(limit) <= 0; i = i.add(two)) {
            if (n.mod(i).equals(BigInteger.ZERO)) return i;
        }
        return null;
    }
}

static class BenchmarkSuite {
    static void run(int repeats) throws Exception {
        int maxRepeats = 1000;
        if (repeats < 1) {
            System.out.println("Repeat count must be >= 1.");
            return;
        }
        if (repeats > maxRepeats) {
            System.out.println("Repeat count too large; capping to " + maxRepeats);
            repeats = maxRepeats;
        }

        long rsaKeyGen = 0;
        long kemKeyGen = 0, kemEnc = 0, kemDec = 0;
        for (int i = 0; i < repeats; i++) {
            if ((i % 50) == 0) System.out.println("Benchmark progress: " + i + "/" + repeats);

            long t0 = System.nanoTime();
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024, random);
            kpg.generateKeyPair();
            rsaKeyGen += System.nanoTime() - t0;

            LatticeKEM kem = new LatticeKEM(32, 4093);
            long k0 = System.nanoTime();
            LatticeKEM.LatticeKeyPair kp = kem.keyGen();
            kemKeyGen += System.nanoTime() - k0;

            long e0 = System.nanoTime();
            LatticeKEM.LatticeEncapsulation enc = kem.encapsulate(kp.pk);
            kemEnc += System.nanoTime() - e0;

            long d0 = System.nanoTime();
            byte[] shared = kem.decapsulate(kp.sk, enc.ciphertext);
            kemDec += System.nanoTime() - d0;
        }
        System.out.println("Avg RSA keygen (ms): " + (rsaKeyGen / repeats) / 1_000_000.0);
        System.out.println("Avg ToyKEM keygen (ms): " + (kemKeyGen / repeats) / 1_000_000.0);
        System.out.println("Avg ToyKEM encaps (ms): " + (kemEnc / repeats) / 1_000_000.0);
        System.out.println("Avg ToyKEM decaps (ms): " + (kemDec / repeats) / 1_000_000.0);
    }
}
	
	private static void showEntropyHeatmap(String title, byte[] keyBytes) {
    System.out.println();
    System.out.println(title);

    int bits = keyBytes.length * 8;
    char[] map = new char[bits];

    int idx = 0;
    for (byte b : keyBytes) {
        for (int i = 7; i >= 0; i--) {
            boolean bit = ((b >> i) & 1) == 1;
            map[idx++] = bit ? '█' : '░';
        }
    }

    final int WIDTH = 40;
    final int rows = (int) Math.ceil(bits / (double) WIDTH);

    System.out.println("┌" + "─".repeat(WIDTH * 2 - 1) + "┐");

    int pointer = 0;
    for (int r = 0; r < rows; r++) {
        StringBuilder row = new StringBuilder("│ ");
        for (int c = 0; c < WIDTH; c++) {
            if (pointer < bits) {
                row.append(map[pointer]).append(' ');
                pointer++;
            } else {
                row.append(' ').append(' ');
            }
        }
        row.append("│");
        System.out.println(row);
    }

    System.out.println("└" + "─".repeat(WIDTH * 2 - 1) + "┘");
    System.out.println("Legend: ░ = 0-bit   █ = 1-bit");
}


    static String bytesToHex(byte[] in) {
        if (in == null) return "(null)";
        StringBuilder sb = new StringBuilder();
        for (byte b : in) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
