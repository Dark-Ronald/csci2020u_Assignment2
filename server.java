import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;


public class server {
    static File fileDir;
    
    public static void main(String[] args) throws Exception {
        fileDir = new File(args[0]);
        ServerSocket serverSocket = new ServerSocket(8080);
        boolean end = false;
        while (!end) {
            Socket clientSocket = serverSocket.accept();
            ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket);
            new Thread(handler).start();
        }
    }
}

class ClientConnectionHandler implements Runnable {
    Socket clientSocket;
    
    ClientConnectionHandler(Socket socket) {
        clientSocket = socket;
    }
    
    public void run() {
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            String command = reader.readLine();
            
            if (command.equals("DIR")) {
                dir();
            }
            else if (command.split(" ")[0].equals("UPLOAD")) {
                upload(command.split(" ", 2)[1], reader);
            }
            else if (command.split(" ")[0].equals("DOWNLOAD")) {
                
                download(command.split(" ", 2)[1]);
            }
            
            reader.close();
        }
        catch (IOException e) {
            System.err.println("IOException");
            
        }
        finally {
            try {
                clientSocket.close();
            }
            catch (IOException ioe) {
                System.err.println("finally IOException");
            }
        }
    }
    
    void dir() throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
        for (File file : server.fileDir.listFiles()) {
            writer.println(file.getName());
        }
        writer.flush();
        writer.close();
    }
    
    void upload(String fileName, BufferedReader reader) throws IOException {
        File clientFile = new File(server.fileDir.getAbsolutePath() + "\\" + fileName);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(clientFile));
        
        int blockLength = 1;
        char[] block = new char[blockLength];

        while (reader.read(block, 0, blockLength) != -1) {
            writer.write(block, 0, blockLength);
            block = new char[blockLength];
        }
        
        writer.close();
        
    }
    
    void download(String fileName) throws IOException {
        File clientFile = new File(server.fileDir.getAbsolutePath() + "\\" + fileName);
        
        BufferedReader reader = new BufferedReader(new FileReader(clientFile));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        
        int blockLength = 1;
        char[] block = new char[blockLength];

        while (reader.read(block, 0, blockLength) != -1) {
            writer.write(block, 0, blockLength);
            block = new char[blockLength];
        }
        
        writer.flush();
        reader.close();
        writer.close();
    }
}