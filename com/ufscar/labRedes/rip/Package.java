/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufscar.labRedes.rip;

import java.io.Serializable;

/**
 * @author marcoscavalcante
 */
public class Package implements Serializable {

    private int sourceID;
    private int destinationID;
    private int[] minCostArr;


    public Package(int sourceID, int destinationID, int[] distancesM) {
        this.sourceID = sourceID;
        this.destinationID = destinationID;
        this.minCostArr = distancesM;
    }

    public int getSourceID() {
        return sourceID;
    }

    public int getDestinationID() {
        return destinationID;
    }

    public int[] getMinCostArr() {
        return minCostArr;
    }

}