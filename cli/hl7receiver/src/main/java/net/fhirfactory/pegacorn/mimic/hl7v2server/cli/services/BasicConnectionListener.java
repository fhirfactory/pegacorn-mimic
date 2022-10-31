package net.fhirfactory.pegacorn.mimic.hl7v2server.cli.services;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;

public class BasicConnectionListener implements ConnectionListener {
    @Override
    public void connectionDiscarded(Connection connectionBeingDiscarded) {
        System.out.println("Connection discarded event fired " + connectionBeingDiscarded.getRemoteAddress());
        System.out.println("For Remote Address: " + connectionBeingDiscarded.getRemoteAddress());
        System.out.println("For Remote Port: " + connectionBeingDiscarded.getRemotePort());
    }

    @Override
    public void connectionReceived(Connection connectionBeingOpened) {
        System.out.println("Connection opened event fired " + connectionBeingOpened.getRemoteAddress());
        System.out.println("From Remote Address: " + connectionBeingOpened.getRemoteAddress());
        System.out.println("From Remote Port: " + connectionBeingOpened.getRemotePort());
    }
}
