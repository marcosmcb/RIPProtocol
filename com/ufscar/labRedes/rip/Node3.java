package com.ufscar.labRedes.rip;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author marcoscavalcante
 */
public class Node3 extends Thread {

    private static ReentrantLock lock = new ReentrantLock();
    private int idNode = 3;
    private final int numNodes = 4;
    private final int infinity = 999;
    private final int undefined = -1;
    private final int[][] distanceMatrix = new int[4][4]; /*This matrix will be used to store the distance amongst the nodes. */
    private final int tracing = 0; /*Variable used for debugging. */

    private static ServerSocket server;
    private final Socket node;

    public Node3(Socket client) {
        this.node = client;
        nodeInitialize();
    }

    public static void createServer() throws IOException {
        server = new ServerSocket(8004);
    }

    public static void initializeServer() {
        lock.lock();
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        Socket listener = server.accept();
                        
                        Node3 node3 = new Node3(listener);
                        node3.start();
                        
                        /*
                        listener = server.accept();
                        Node0 node0 = new Node0(listener);
                        node0.start();

                        listener = server.accept();
                        Node2 node2 = new Node2(listener);
                        node2.start();
                        */

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        lock.unlock();
    }


    public void run() {
        try {

            ObjectInputStream inputNode = new ObjectInputStream(node.getInputStream());
            Package nodePackage = (Package) inputNode.readObject();
            
            nodeUpdate(nodePackage);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Node3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /*
    This is basically the method which initializes the distanceMatrix. 
    Some distances are hardcoded because they were given. 
    I reckon that is not the best way to do that, so, feel free to change it. 
    */
    public void nodeInitialize() {

        this.distanceMatrix[3][0] = 7;
        this.distanceMatrix[3][1] = 999;
        this.distanceMatrix[3][2] = 2;
        this.distanceMatrix[3][3] = undefined;

        this.distanceMatrix[0][3] = 7;
        this.distanceMatrix[2][3] = 2;
        
        toLayer2();

    }
    
    private void toLayer2() {
        try {

            Thread.sleep(3000);
            
            Socket node0 = new Socket("localhost", 8001);
            ObjectOutputStream outNode0 = new ObjectOutputStream(node0.getOutputStream());
            
            Socket node2 = new Socket("localhost", 8003);
            ObjectOutputStream outNode2 = new ObjectOutputStream(node2.getOutputStream());
            
            outNode0.writeObject(new Package(idNode,0,distanceMatrix[idNode]));
            outNode2.writeObject(new Package(idNode,2,distanceMatrix[idNode]));

        }catch (IOException ex) {
        }catch (InterruptedException ex) {
            Logger.getLogger(Node3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void forwardPackage(Package nodePackage) throws IOException{
        
        int port = Integer.parseInt("800" +  (nodePackage.getDestinationID()+1) );
      
        try {
                Thread.sleep(3000);

                Socket forwardNode = new Socket("localhost", port);
                ObjectOutputStream outNode = new ObjectOutputStream(forwardNode.getOutputStream());

                outNode.writeObject(nodePackage);
                
        }catch (IOException ex) {
        }catch (InterruptedException ex) {
            Logger.getLogger(Node3.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    private void printDistancesNode(){
        
        //System.out.println("Matrix de distâncias do Nó 3");
       
        for(int i=0; i < numNodes; i++){
            System.out.println("Node"+i+" Distance Vector ");
            for(int j=0; j < numNodes; j++)
                System.out.print(distanceMatrix[i][j] + "\t");
            
        }
    }

    
    /*
    This is the method which calculates the distance between our "main" node, in this
    case, node 0, and the other nodes. We use the Bellman-Ford equation to do so.
    
    */

    public void nodeUpdate(Package nodePackage) throws IOException {

        int updateDistances = 0;
        int[] sourceDistances;


        if (nodePackage.getDestinationID() != idNode) {
            /* If we are not the receiver, we relay the packet */
            forwardPackage(nodePackage);
            return;
        }

        sourceDistances = nodePackage.getMinCostArr();
        
        
        /*Here is where the Bellman-Ford equation is trully applied, 
          We firstly get the distance from our node to the source node, to calculate the minimun cost,
          and if it happens to have changed, we update our own distance matrix.
        */
        for (int i = 0; i < sourceDistances.length; i++) {

            int newDistance = distanceMatrix[idNode][nodePackage.getSourceID()] + sourceDistances[i];

            if (newDistance < distanceMatrix[nodePackage.getSourceID()][i]) {
                updateDistances++;
                distanceMatrix[idNode][nodePackage.getSourceID()] = newDistance;
                distanceMatrix[nodePackage.getSourceID()][idNode] = newDistance;
            }

        }
        
        /*
          In case our variable, updateDistances, has been incremented, 
          We must send out our distanceVector to every other node.
        */
        if (updateDistances > 0) {
            toLayer2();
            printDistancesNode();
        }

    }


    public static void main(String[] args) throws IOException {
        createServer();
        initializeServer();
    }

   
}
