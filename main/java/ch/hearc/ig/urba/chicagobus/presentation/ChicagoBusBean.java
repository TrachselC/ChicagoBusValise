/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.hearc.ig.urba.chicagobus.presentation;

import ch.hearc.ch.ig.urba.chicagobus.business.Bus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author chloe.trachsel
 */
@Named(value = "chicagoBusBean")
@RequestScoped
public class ChicagoBusBean {

    private ArrayList<Bus> busList = new ArrayList<>();
    private ArrayList<Bus> busProbablyList = new ArrayList<>();
    private Document xmlDocument;
    String idBusSelected;
    boolean init = true;
    private Double distanceBusStop;
    private Boolean tracked = false;

    /**
     * Creates a new instance of ChicagoBusBean
     */
    public ChicagoBusBean() {

    }

    public void init() throws ParserConfigurationException, SAXException, IOException, Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder parser = factory.newDocumentBuilder();

        xmlDocument = (Document) parser.parse(new InputSource(new StringReader(getBusInformation())));

        busList = readXmlDocument();

        if (init) {
            getProbablyBus();
            init = false;
        }
    }

    public ArrayList<Bus> readXmlDocument() {
        ArrayList<Bus> buses = new ArrayList<>();

        NodeList listNode = xmlDocument.getElementsByTagName("bus");

        for (int i = 0; i < listNode.getLength(); i++) {
            Bus bus = new Bus();
            Element busE = (Element) listNode.item(i);

            Element busEId = (Element) busE.getElementsByTagName("id").item(0);
            bus.setID(busEId.getTextContent());

            Element busEDir = (Element) busE.getElementsByTagName("pd").item(0);
            bus.setDirection(busEDir.getTextContent());

            Element busELat = (Element) busE.getElementsByTagName("lat").item(0);
            bus.setLat(Double.parseDouble(busELat.getTextContent()));

            Element busELng = (Element) busE.getElementsByTagName("lon").item(0);
            bus.setLng(Double.parseDouble(busELng.getTextContent()));
            bus.setState("ALL");
            buses.add(bus);
        }
        return buses;
    }

    public String getBusInformation() throws MalformedURLException, IOException {

        HttpURLConnection connection = null;

        URL busInfos = new URL("http://ctabustracker.com/bustime/map/getBusesForRoute.jsp?route=22");
        URLConnection st = busInfos.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(st.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            //System.out.println(inputLine);
        }

        return response.toString();
    }

    public void getProbablyBus() throws Exception {
        // Place dans la liste busProbablyList, les bus qui vont vers le nord, après avoir pasé l'arret.
        for (Bus bus : busList) {
            if ((bus.getDirection().equals("Northbound")) && (bus.getLat() > 41.984982)) {
                
                Double distanceLat = bus.getLat()-41.984982;
                Double distanceMile = distanceLat*69/1;
                
                bus.setDistance(distanceMile);
                bus.setState("probably");
                busProbablyList.add(bus);
            }
        }

        // Ajouter les latitude des ces bus pour les trier et obtenir le plus proche.
        ArrayList<Double> listSorted = new ArrayList();
        for (Bus bus : busProbablyList) {
            listSorted.add(bus.getLat());
        }
        Collections.sort(listSorted);

        // Récupère le bon bus de la liste busProbablyList avec la liste de latitudes triées.
        for (Bus bus : busProbablyList) {
            if (bus.getLat().toString().equals(listSorted.get(0).toString())) {
                if(idBusSelected != null){
                    if(bus.getID() == idBusSelected){
                        
                    }else{
                        throw new Exception("Le bus est au dépôt !");
                    }
                }
                idBusSelected = bus.getID(); 
                
                Double distanceLat = bus.getLat()-41.980262;
                Double distanceMile = distanceLat*69/1;
                
                distanceBusStop = distanceMile;
            }
        }
        
        for (Bus bus : busList) {
            if (bus.getID() == idBusSelected) {
                bus.setState("TRACKED");
            }
            
        }
    }
    
    public String busInformationString(){
        for (Bus bus : busList) {
            if(bus.getState()=="TRACKED"){
                if(bus.getDirection() == "SOUTHBOUND"){
                    return "Le bus redescend ...";
                }else{
                    return "Le bus va vers le nord. Il reviendra ...";
                }
                
            }
            
        }
        return "";
    }

    public List<Bus> getBusList() {
        return busList;
    }

    public String getIdBusSelected() {
        return idBusSelected;
    }

    public ArrayList<Bus> getBusProbablyList() {
        return busProbablyList;
    }

    public Double getDistanceBusStop() {
        return distanceBusStop;
    }

    public Boolean getTracked() {
        return tracked;
    }

}
