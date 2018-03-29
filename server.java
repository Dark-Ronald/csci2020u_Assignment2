import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;


public class server {
    static File fileDir = new File(".\\src\\shared folder");
    
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        boolean end = false;
        while (!end) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("new connection");
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
            //String line = null;
            System.out.println("reading");
            String command = reader.readLine();
            System.out.println("command: " + command);
            
            if (command.equals("DIR")) {
                dir();
            }
            else if (command.split(" ")[0].equals("UPLOAD")) {
                upload(command.split(" ", 2)[1], reader);
            }
            else if (command.split(" ")[0].equals("DOWNLOAD")) {
                
                download(command.split(" ", 2)[1]);
            }
            else {
                //not valid command
            }
            
            reader.close();
        }
        catch (IOException e) {
            System.out.println("IOException");
            
        }
        finally {
            try {
                clientSocket.close();
            }
            catch (IOException ioe) {
                System.out.println("finally IOException");
            }
        }
    }
    
    void dir() throws IOException {
        System.out.println("getting file names");
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
        for (File file : server.fileDir.listFiles()) {
            writer.println(file.getName());
            System.out.println("file name: " + file.getName());
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
        String line = null;
        
        int blockLength = 1;
        char[] block = new char[blockLength];

        while (reader.read(block, 0, blockLength) != -1) {
            writer.write(block, 0, blockLength);
            block = new char[blockLength];
        }
        //writer.write(block, 0, blockLength);
        
//        int blockLength = 20;
//        char[] block = new char[blockLength];
//        
//        while (reader.read(block, 0, blockLength) != -1) {
//            writer.write(block, 0, blockLength);
//            block = new char[blockLength];
//        }
//        writer.write(block, 0, blockLength);

//        while ((line = reader.readLine()) != null) {
//            writer.write(line);
//            writer.newLine();
//            System.out.println("line written: " + line);
//        }
        writer.flush();
        reader.close();
        writer.close();
    }
}