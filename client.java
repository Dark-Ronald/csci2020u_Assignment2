/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.Group;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.net.Socket;
import java.net.*;
import java.io.*;
import javafx.scene.control.SelectionMode;

/**
 *
 * @author pwner
 */
public class client extends Application {
    File localDir;
    String[] remoteFiles;
    Socket server;
    static String[] args;
    
    @Override
    public void start(Stage primaryStage) {
        localDir = new File(args[1]);
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 500, 600);
        GridPane buttons = new GridPane();
        
        ListView<String> clientFileList = new ListView(getLocalFileList());
        clientFileList.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
        
        ListView<String> serverFileList = new ListView(getRemoteFileList());
        serverFileList.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
        
        Button downloadBtn = new Button();
        downloadBtn.setText("Download");
        downloadBtn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                for (String fileName : serverFileList.selectionModelProperty().get().getSelectedItems()) {
                    download(fileName);
                    clientFileList.getItems().add(fileName);
                }
            }
        });
        
        Button uploadBtn = new Button();
        uploadBtn.setText("Upload");
        uploadBtn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                for (String fileName : clientFileList.selectionModelProperty().get().getSelectedItems()) {
                    upload(fileName);
                    serverFileList.getItems().add(fileName);
                }
                
            }
        });
        
        //ListView<String> clientFileList = new ListView(getLocalFileList());
        
        buttons.add(downloadBtn, 0, 0);
        buttons.add(uploadBtn, 1, 0);
        root.setTop(buttons);
        root.setLeft(clientFileList);
        root.setRight(serverFileList);
        
        
        primaryStage.setTitle("File Sharer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        //connect to server
//        try {
//            server = new Socket("localhost", 8080);
//            
//        }
//        catch (UnknownHostException e) {
//            System.out.println("UknownHostException");
//        }
//        catch (IOException e) {
//            System.out.println("IOException");
//        }
        
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        client.args = args;
        launch(args);
    }
    
    ObservableList<String> getLocalFileList() {
        ObservableList<String> files = FXCollections.observableArrayList();
        for (File file : localDir.listFiles()) {
            files.add(file.getName());
        }
        return files;
    }
    
    ObservableList<String> getRemoteFileList() {
        System.out.println("getting remote file list");
        ObservableList<String> files = FXCollections.observableArrayList();
        try {
            server = new Socket(args[0], 8080);
            PrintWriter writer = new PrintWriter(server.getOutputStream());
            writer.println("DIR");
            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            String fileName = null;
            while ((fileName = reader.readLine()) != null) {
                files.add(fileName);
                System.out.println(fileName);
            }
            writer.close();
            reader.close();
            server.close();
        }
        catch (UnknownHostException e) {
            System.out.println("UknownHostException");
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        System.out.println("remote file list gotten");
        return files;
    }
    
    void download(String fileName) {
        
        File clientFile = new File(localDir.getAbsolutePath() + "\\" + fileName);
        
        try {
            server = new Socket(args[0], 8080);
            PrintWriter commandWriter = new PrintWriter(server.getOutputStream());
            commandWriter.println("DOWNLOAD " + fileName);
            commandWriter.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(clientFile));
            
            int blockLength = 1;
            char[] block = new char[blockLength];
            
            while (reader.read(block, 0, blockLength) != -1) {
                writer.write(block, 0, blockLength);
                block = new char[blockLength];
            }
            //writer.write(block, 0, blockLength);
            
//            int blockLength = 20;
//            char[] block = new char[blockLength];
//            
//            while (reader.read(block, 0, blockLength) != -1) {
//                writer.write(block, 0, blockLength);
//                block = new char[blockLength];
//            }
//            writer.write(block, 0, blockLength);
            
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                writer.write(line);
//                writer.newLine();
//                System.out.println("line read: " + line);
//            }
            reader.close();
            writer.flush();
            writer.close();
            commandWriter.close();
            server.close();
        }
        
        catch (UnknownHostException e) {
            System.out.println("UknownHostException");
        }
        catch (IOException e) {
            System.out.println("download IOException");
        }
        
        
    }
    
    void upload(String fileName) {
        
        File clientFile = new File(localDir.getAbsolutePath() + "\\" + fileName);
        
        try {
            server = new Socket(args[0], 8080);
            PrintWriter commandWriter = new PrintWriter(server.getOutputStream());
            commandWriter.println("UPLOAD " + fileName);
            commandWriter.flush();
            BufferedReader reader = new BufferedReader(new FileReader(clientFile));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            
            int blockLength = 1;
            char[] block = new char[blockLength];
            
            while (reader.read(block, 0, blockLength) != -1) {
                writer.write(block, 0, blockLength);
                block = new char[blockLength];
            }
            
            reader.close();
            writer.flush();
            writer.close();
            commandWriter.close();
            server.close();
        }
        
        catch (UnknownHostException e) {
            System.out.println("UknownHostException");
        }
        catch (IOException e) {
            System.out.println("upload IOException");
        } 
        
    }
    
    
}
