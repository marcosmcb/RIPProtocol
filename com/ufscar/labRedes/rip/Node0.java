package com.ufscar.labRedes.rip;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author marcoscavalcante
 */
public class Node0 extends Thread{
    
    private static ReentrantLock lock;
    private final int idNode = 0;
    private final int numNodes = 4;
    private final int infinity = 999;
    private final int undefined = -1;
    private final int[][] distanceMatrix = new int[4][4]; /*This matrix will be used to store the distance amongst the nodes. */
    private final int tracing = 0; /*Variable used for debugging. */

    private static ServerSocket server;
    private final Socket node;
    
    
    /*Constructor for the class along with a call for the initialization method */
    public Node0( Socket client ) {
        this.node = client;
        nodeInitialize();
    }
    
    /*Setting up our server*/
    public static void createServer() throws IOException {
        server = new ServerSocket(8001);
    }
    
    /*Finally, We initialize it */
    public static void initializeServer() {
        //lock.lock();
        new Thread() {
            @Override
            public void run() {
                try{
                    while(true){
                        Socket listener = server.accept();
                        Node0 node0 = new Node0(listener);
                        node0.start();
                    }
                } catch (IOException e) {
                }
            }
        }.start();
        //lock.unlock();
    }
    
    /*
    This is basically the method which initializes the distanceMatrix. 
    Some distances are hardcoded because they were given. 
    I reckon that is not the best to do that, so, feel free to change it.
    There is a call for the method toLayer2().
    */
    public void nodeInitialize (){
        
        this.distanceMatrix[0][0] = 0;
        this.distanceMatrix[0][1] = 1;
        this.distanceMatrix[0][2] = 3;
        this.distanceMatrix[0][3] = 7;
        
        this.distanceMatrix[1][0] = 1;
        this.distanceMatrix[2][0] = 3;
        this.distanceMatrix[3][0] = 7;
        
        toLayer2();
        
    }
    
    /*
    It is by using this method that we broadcast our distanceVector to ther nodes 
    which are adjacent to us.
    */
    private void toLayer2() {
        try {
            
            Socket node1 = new Socket("localhost", 8002);
            ObjectOutputStream outNode1 = new ObjectOutputStream(node1.getOutputStream());
            
            Socket node2 = new Socket("localhost", 8003);
            ObjectOutputStream outNode2 = new ObjectOutputStream(node2.getOutputStream());
            
            Socket node3 = new Socket("localhost", 8004);
            ObjectOutputStream outNode3 = new ObjectOutputStream(node3.getOutputStream());
        
            outNode1.writeObject(new Package(idNode,1,distanceMatrix[idNode]));
            outNode2.writeObject(new Package(idNode,2,distanceMatrix[idNode]));
            outNode3.writeObject(new Package(idNode,3,distanceMatrix[idNode]));

        }catch (IOException ex) {
        }
    }
    
    /*
    In case we receive a packet that is not us the receiver,
    We must forward them to the destiny node.
    */
    private void forwardPackage(Package nodePackage) throws IOException{
        /* 
        We use this little hack to make use of the port numbers, which are in sequence, so we just need to 
        concatenate the number "800" with the (destinationID + 1).
        */
        int port = Integer.parseInt("800" +  (nodePackage.getDestinationID()+1) );
      
        try {
                Socket forwardNode = new Socket("localhost", port);
                ObjectOutputStream outNode = new ObjectOutputStream(forwardNode.getOutputStream());

                outNode.writeObject(nodePackage);
                
        }catch (IOException ex) {
        }    
    }
    
    /*  This is the method by which we print out the distanceMatrix   */
    private void printDistancesNode(){        
        //System.out.println("Matrix de distâncias do Nó 0");
       
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
    public void nodeUpdate (Package nodePackage) throws IOException{

        int updateDistances = 0;
        int[] sourceDistances;
        
        /* If we are not the receiver, we relay the packet */
        if(nodePackage.getDestinationID() != idNode){
            forwardPackage(nodePackage); 
            return ;
        }
        
        sourceDistances = nodePackage.getMinCostArr();
        
        /*
          Here is where the Bellman-Ford equation is trully applied, 
          We firstly get the distance from our node to the source node, to calculate the minimun cost,
          and if it happens to have changed, we update our own distance matrix.
        */
        for(int i = 0; i < sourceDistances.length; i++){
            
            int newDistance = distanceMatrix[idNode][nodePackage.getSourceID()] + sourceDistances[i];
            
            if(newDistance < distanceMatrix[nodePackage.getSourceID()][i]){    
                updateDistances++;
                distanceMatrix[idNode][nodePackage.getSourceID()] = newDistance;
                distanceMatrix[nodePackage.getSourceID()][idNode] = newDistance;
            }
        }
        
        /*
          In case our variable, updateDistances, has been incremented, 
          We must send out our distanceVector to every other node
        */
        if(updateDistances > 0){
            toLayer2();
            printDistancesNode();
        }
    }
    
    
    public static void main(String[] args) throws IOException {
        createServer();
        initializeServer();
    }

    public void run() {
        try{
           
            ObjectInputStream inputNode = new ObjectInputStream(node.getInputStream());
            Package nodePackage = (Package) inputNode.readObject();
            
            nodeUpdate(nodePackage); /*  Call the method to update our distance Matrix   */
            
        }catch ( IOException e ) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Node0.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
}
