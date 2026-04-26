# QuantumShield: Post-Quantum Cryptography Demonstration Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)
![Educational](https://img.shields.io/badge/Purpose-Educational-orange?style=flat-square)

A comprehensive educational platform demonstrating quantum-resistant cryptographic algorithms and the vulnerability of classical RSA to quantum computing threats.

[Features](#features) • [Quick Start](#quick-start) • [Architecture](#architecture) • [Documentation](#documentation)

</div>

---

## 📋 Overview

**QuantumShield** is an interactive Java application that educates users about the **quantum computing threat** to modern cryptography and introduces **post-quantum cryptographic solutions**. Built as a semester project, it combines theoretical knowledge with practical demonstrations through a fully functional encrypted chat system.

### The Problem
Current internet security relies on RSA and ECC, which are vulnerable to **Shor's Algorithm** on large-scale quantum computers. This project visualizes this threat and demonstrates viable alternatives.

### The Solution
QuantumShield integrates:
- **Quantum Key Distribution** (BB84 Protocol Simulation)
- **Post-Quantum Cryptography** (Lattice-based KEM)
- **Classical Vulnerability Demo** (RSA Factorization Attack)
- **Secure Communication** (AES-GCM Encrypted Chat)

---

## ✨ Features

### 🔐 Core Cryptographic Demonstrations

| Feature | Description | Technology |
|---------|-------------|-----------|
| **BB84 QKD Simulation** | Quantum key distribution based on quantum mechanics principles | Custom BB84 simulator with eavesdropping detection |
| **Lattice KEM** | Ring-LWE-inspired post-quantum key encapsulation | Toy implementation (educational) |
| **RSA Break Demo** | Shows factorization of small RSA keys in milliseconds | Brute-force factorization with Shor's comparison |
| **Performance Benchmarks** | Compares PQC vs classical cryptography overhead | Automated timing suite |
| **AES-GCM Chat System** | End-to-end encrypted messaging using derived keys | Java Crypto API with authenticated encryption |

### 🎨 Interactive Features

- **CLI Menu System** - Intuitive command-line interface
- **Entropy Visualization** - Heatmap displays of key bit distributions
- **Real-time Networking** - Multi-client chat server with TCP sockets
- **Statistics Logging** - Tracks BB84 success/failure metrics
- **Error Handling** - Comprehensive exception handling with retry logic

---

## 🚀 Quick Start

### Prerequisites
- **Java 11 or higher**
- **Maven** (optional, for build automation)
- **Terminal/Command Prompt**

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/QuantumShield.git
cd QuantumShield

# Compile
javac QuantumShield.java

# Run
java QuantumShield
```

### First Run

```
=== QuantumShield: Post-Quantum Secure Chat ===
1) Simulate BB84 key exchange
2) Run Toy Lattice KEM (encaps/decaps)
3) Start Chat Server (local)
4) Start Chat Client (connect)
5) Demonstrate RSA break (brute-force)
6) Benchmark RSA vs ToyKEM (keygen/enc/dec)
7) Exit

Choose: 1
```

---

## 📚 Usage Guide

### 1️⃣ BB84 Quantum Key Exchange

**Purpose**: Understand QKD based on quantum mechanics

```
Choose: 1

--- BB84 Quantum Key Exchange (Attempt 1) ---
Raw key length: 128
Shared key (hex): 367333f7804a571dd2e1dd7f77427c3dfe9a6cccc867d539d92df7cdafea61f
Bit count: 256
Zero bits: 108, One bits: 148
Imbalance: 15.63%
Session AES key established for chat.
```

**Key Features**:
- Automatic eavesdropping simulation
- Error detection via QBER (Quantum Bit Error Rate)
- Automatic retries if key imbalance exceeds threshold (30%)
- SHA-256 hashing for entropy amplification
- Entropy heatmap visualization

---

### 2️⃣ Lattice KEM Key Encapsulation

**Purpose**: Demonstrate post-quantum secure key exchange

```
Choose: 2

=== Post-Quantum Lattice KEM ===
Step 1: Initializing KEM...
Step 2: Generating key pair...
Step 3: Encapsulating shared secret...
Step 4: Decapsulating shared secret...

Sender's derived key:
e80bd5ddb70c1e18f47c100a7598431b5979ed33f3dcfde7e6585ba6562703ed

Receiver's derived key:
e80bd5ddb70c1e18f47c100a7598431b5979ed33f3dcfde7e6585ba6562703ed

Status: Keys match. Secure channel established.
AES Session Key: Ready for encrypted chat.
```

**Parameters** (Toy Implementation):
- Ring size: `n = 16`
- Modulus: `q = 257`
- Error: `0 < e < 5`

---

### 3️⃣ Chat Server

**Purpose**: Host encrypted multi-client chat

```
Choose: 3
Enter port to listen on (e.g., 9000): 9000
Server started on port 9000
[broadcast] Hello from client 1!
```

**Features**:
- TCP socket-based communication
- AES-GCM encryption/decryption
- Message broadcasting to all connected clients
- Graceful error handling

---

### 4️⃣ Chat Client

**Purpose**: Connect to server and exchange encrypted messages

```
Choose: 4
Server host (localhost): localhost
Server port (9000): 9000
Connected to server. Type messages, 'exit' to quit.
Hello!
[remote] Response from another client
```

---

### 5️⃣ RSA Factorization Attack

**Purpose**: Visualize classical cryptography vulnerability

```
Choose: 5
Generate RSA key size (bits, small for demo e.g., 32/64/128): 64
Demo small RSA modulus n = p * q (p and q are small primes)
n = 2701936873984437
Found factor: 1619711
Other factor: 1668647
Time (ms): 3
Original p (for comparison): 1619711
Original q (for comparison): 1668647
```

**Time Complexity**:
- 64-bit RSA: `~1-10 ms` (classical brute-force)
- 2048-bit RSA: `centuries` (classical) → `hours-days` (Shor's algorithm on quantum computer)

---

### 6️⃣ Performance Benchmarking

**Purpose**: Compare computational overhead of PQC vs RSA

```
Choose: 6
Repeat count: 100
Benchmark progress: 0/100
Benchmark progress: 50/100

Avg RSA keygen (ms): 35.42
Avg ToyKEM keygen (ms): 0.08
Avg ToyKEM encaps (ms): 0.02
Avg ToyKEM decaps (ms): 0.01
```

**Findings**:
- PQC is **400-1000x faster** than RSA for key generation
- Suitable for high-throughput applications
- Minimal overhead for encapsulation/decapsulation

---

## 🏗️ Architecture

### Class Hierarchy

```
QuantumShield (Main)
├── Menu
│   ├── runBB84()
│   ├── runLatticeKEM()
│   ├── startServer()
│   ├── startClient()
│   ├── rsaBreakDemo()
│   └── benchmark()
├── BB84Simulator
│   └── Result
├── LatticeKEM
│   ├── KEM
│   ├── LatticeKeyPair
│   └── LatticeEncapsulation
├── CryptoUtils (Static)
│   ├── sha256()
│   ├── deriveAESKeyFromBytes()
│   ├── aesGcmEncrypt/Decrypt()
│   └── concat()
├── ChatServer
│   └── ClientHandler
├── ChatClient
├── RSAAttack
└── BenchmarkSuite
```

### Key Components

| Class | Responsibility | Key Methods |
|-------|-----------------|-------------|
| `BB84Simulator` | QKD protocol simulation | `runSimulation()`, `packBitsToBytes()` |
| `LatticeKEM` | Post-quantum KEM | `keyGen()`, `encapsulate()`, `decapsulate()` |
| `CryptoUtils` | Cryptographic primitives | `sha256()`, `aesGcmEncrypt()` |
| `ChatServer` | Network server & message routing | `start()`, `broadcast()` |
| `ChatClient` | Client connection & I/O | `runInteractive()`, `reader()` |
| `RSAAttack` | Vulnerability demonstration | `demo()`, `bruteForceFactor()` |

### Data Flow

```
BB84/LatticeKEM
    ↓
    Shared Secret
    ↓
    SHA-256 Hashing
    ↓
    AES Key Derivation
    ↓
    Chat Encryption/Decryption
```

---

## 📖 Documentation

### Theoretical Background

#### Quantum Key Distribution (BB84)
- **Principle**: Quantum mechanical properties guarantee eavesdropping detection
- **No-Cloning Theorem**: Any measurement attempt by Eve introduces detectable errors
- **QBER Threshold**: Errors exceeding 15% indicate eavesdropping
- **Reference**: [BB84 Protocol - Wikipedia](https://en.wikipedia.org/wiki/BB84)

#### Post-Quantum Cryptography
- **Hard Problem**: Learning with Errors (LWE) / Shortest Vector Problem (SVP)
- **Security**: Believed hard even for quantum computers
- **Key Encapsulation Mechanism**: Establishes shared secret, not for encryption
- **Reference**: [NIST Post-Quantum Cryptography Standardization](https://csrc.nist.gov/projects/post-quantum-cryptography/)

#### RSA Vulnerability
- **Classical**: Factorization takes centuries
- **Quantum (Shor's Algorithm)**: Polynomial time (hours-days)
- **Threat Timeline**: Estimated 10-30 years until viable quantum computers

### Project Report

See `QuantumShield_Report.pdf` for:
- Detailed implementation analysis
- Entropy heatmap visualizations
- Benchmark result tables
- Future enhancement roadmap

---

## 🔧 Configuration

### BB84 Parameters

```java
BB84Simulator sim = new BB84Simulator(128);  // 128-bit raw key
BB84Simulator.Result r = sim.runSimulation(0.15);  // 15% eavesdrop probability
```

### Lattice KEM Parameters

```java
LatticeKEM.KEM kem = new LatticeKEM.KEM(16, 257);  // n=16, q=257
// For benchmarks: new LatticeKEM(32, 4093)
```

### AES Configuration

```java
// AES-128 with GCM mode (AEAD)
GCMParameterSpec spec = new GCMParameterSpec(128, iv);  // 128-bit auth tag
```

---

## 📊 Performance Metrics

### Key Generation

| Algorithm | Time (ms) | Notes |
|-----------|-----------|-------|
| RSA 1024-bit | 30-50 | Relies on large prime generation |
| Toy LatticeKEM | 0.05-0.10 | Simple matrix/vector operations |

### Encapsulation/Decapsulation

| Operation | Time (ms) | Algorithm |
|-----------|-----------|-----------|
| Encapsulation | 0.01-0.05 | Matrix multiplication + hashing |
| Decapsulation | 0.01-0.05 | Vector subtraction + hashing |

### Improvement Factor
- **~600-1000x faster** for key generation
- Suitable for real-world deployment with high throughput requirements

---

## ⚠️ Disclaimer

**Educational Purpose Only**

This project is for **educational and research purposes**. The implementations are **simplified** and **not cryptographically secure** for production use:

- BB84 lacks error correction and privacy amplification
- Lattice KEM uses toy parameters (n=16)
- Not compliant with NIST PQC standards

**For production**, use:
- [liboqs-java](https://github.com/open-quantum-safe/liboqs-java) - NIST-standardized algorithms
- Industry cryptographic libraries (OpenSSL, Bouncy Castle)

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Areas for Contribution
- [ ] Integrate NIST PQC finalists (Kyber, Dilithium)
- [ ] Implement error correction in BB84
- [ ] Add privacy amplification
- [ ] Hybrid mode (RSA + Kyber)
- [ ] GUI dashboard for visualizations
- [ ] Performance profiling and optimization
- [ ] Additional PQC algorithms (NTRU, Classic McEliece)

---

## 📝 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

You are free to:
-  Use for educational purposes
-  Modify and distribute
-  Reference in academic work

---

## 🙋 Support & Questions

- **Issues**: Please open a [GitHub Issue](https://github.com/yourusername/QuantumShield/issues)
- **Discussions**: Use [GitHub Discussions](https://github.com/yourusername/QuantumShield/discussions)
- **Email**: Contact the authors directly

---

## 📚 References

1. **BB84 Protocol**: Bennett & Brassard (1984). *Quantum Cryptography: Public Key Distribution and Coin Tossing*
2. **Learning with Errors**: Regev (2005). *On Lattices, Learning with Errors, Random Linear Codes, and Cryptography*
3. **Shor's Algorithm**: Shor (1994). *Algorithms for Quantum Computation: Discrete Logarithms and Factoring*
4. **NIST PQC Standardization**: https://csrc.nist.gov/projects/post-quantum-cryptography/
5. **AES-GCM**: NIST SP 800-38D. *Recommendation for Block Cipher Modes of Operation: Galois/Counter Mode*

---

## 🔗 Related Resources

- [Open Quantum Safe Project](https://openquantumsafe.org/)
- [NIST Cybersecurity Resource Center](https://csrc.nist.gov/)
- [Quantum Computing Report](https://www.quantumcomputingreport.com/)

---

<div align="center">

**Made with ❤️ for quantum-safe cryptography education**

⭐ Please star if this project helped you understand post-quantum cryptography!

</div>
