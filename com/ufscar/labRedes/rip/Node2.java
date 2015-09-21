package com.ufscar.labRedes.rip;

import java.io.IOException;
import java.io.ObjectInputStream;
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
public class Node2 extends Thread {

    private static ReentrantLock lock = new ReentrantLock();
    private int idNode = 2;
    private final int numNodes = 4;
    private final int infinity = 999;
    private final int undefined = -1;
    private final int[][] distanceMatrix = new int[4][4]; /*This matrix will be used to store the distance amongst the nodes. */
    private final int tracing = 0; /*Variable used for debugging. */

    private static ServerSocket server;
    private final Socket node;

    public Node2(Socket client) {
        this.node = client;
        nodeInitialize();
    }

    public static void createServer() throws IOException {
        server = new ServerSocket(8003);
    }

    public static void initializeServer() {
        lock.lock();
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        Socket listener = server.accept();
                        Node0 node0 = new Node0(listener);
                        node0.start();
                        listener = server.accept();
                        Node1 node1 = new Node1(listener);
                        node1.start();
                        listener = server.accept();
                        Node3 node3 = new Node3(listener);
                        node3.start();
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Node2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /*
    This is basically the method which initializes the distanceMatrix. 
    Some distances are hardcoded because they were given. 
    I reckon that is not the best way to do that, so, feel free to change it. 
    */
    public void nodeInitialize() {

        this.distanceMatrix[2][0] = 3;
        this.distanceMatrix[2][1] = 1;
        this.distanceMatrix[2][2] = undefined;
        this.distanceMatrix[2][3] = 2;

        this.distanceMatrix[0][2] = 3;
        this.distanceMatrix[1][2] = 1;
        this.distanceMatrix[3][2] = 2;

    }
    
    /*
    This is the method which calculates the distance between our "main" node, in this
    case, node 0, and the other nodes. We use the Bellman-Ford equation to do so.
    
    */

    public void nodeUpdate(Package nodePackage) {

        int updateDistances = 0;
        int[] sourceDistances;


        if (nodePackage.getDestinationID() != idNode) {
            /* If we are not the receiver, we relay the packet */
            toLayer2(nodePackage);
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
            }

        }
        
        
        /*
          In case our variable, updateDistances, has been incremented, 
          We must send out our distanceVector to every other node
        */

        if (updateDistances > 0) {
            //toLayer2();
            printDistancesNode();
        }

    }


    public static void main(String[] args) throws IOException {
        createServer();
        initializeServer();
    }

    private void toLayer2(Package node0Package) {
        /*TODO*/
    }

    private void printDistancesNode() {
        /*TODO*/
    }


}
