import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.wrappers.RSInterfaceChild;
import org.rsbot.script.wrappers.RSTile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Forge extends Script implements PaintListener, ServerMessageListener {
	private Bars bars = new Bars();
	private Locations locations = new Locations();
	private String barXMLLocation = "http://scripts.allometry.com/app/webroot/xml/bars.xml";
	private String locationXMLLocation = "http://scripts.allometry.com/app/webroot/xml/locations.xml";
	private XMLReader barReader, locationReader;
	
	@Override
	public boolean onStart(Map<String,String> args) {
		try {
			barReader = XMLReaderFactory.createXMLReader();
			barReader.setContentHandler(bars);
			barReader.parse(new InputSource(new InputStreamReader(new URL(barXMLLocation).openStream())));
			
			locationReader = XMLReaderFactory.createXMLReader();
			locationReader.setContentHandler(locations);
			locationReader.parse(new InputSource(new InputStreamReader(new URL(locationXMLLocation).openStream())));
		} catch (SAXException e) {
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		
		ForgeGUI forgeGUI = new ForgeGUI(bars.bars.toArray(), locations.locations.toArray());
		forgeGUI.setVisible(true);
		
		while(forgeGUI.isVisible()) {}
		
		return false;
	}
	
	@Override
	public int loop() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void serverMessageRecieved(ServerMessageEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onRepaint(Graphics g2) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Bar plain ol' Java object.
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Bar {
		private RSInterfaceChild barInterface;
		private String name;
		
		public RSInterfaceChild getBarInterface() {
			return barInterface;
		}
		
		public void setBarInterface(RSInterfaceChild barInterface) {
			this.barInterface = barInterface;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Bars SAX2 XML Reader Handler.
	 * 
	 * Expects the following XML from an available InputStream:
	 * 
	 * <?xml version="1.0" encoding="utf-8"?>
	 * <bars>
	 *   <bar>
	 *     <childInterfaceID>1</childInterfaceID>
	 *     <parentInterfaceID>1</parentInterfaceID>
	 *     <name>Bronze Bar</name>
	 *   </bar>
	 * </bars>
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Bars extends DefaultHandler {
		private int childInterfaceID, parentInterfaceID = 0;
		private String currentElement = "";
		private Bar bar = null;
		private ArrayList<Bar> bars = new ArrayList<Bar>();
		
		public void startElement(String uri, String name, String qName, Attributes atts) {
			if(!name.equalsIgnoreCase("bar"))
				currentElement = name;
			else
				bar = new Bar();
		}

		public void characters(char chars[], int start, int length) {
			String elementValue = new String(chars, start, length).trim();
			if(elementValue.trim().equals("")) return ;

			if(currentElement.equalsIgnoreCase("childInterfaceID"))
				childInterfaceID = Integer.parseInt(elementValue);
			else if(currentElement.equalsIgnoreCase("parentInterfaceID"))
				parentInterfaceID = Integer.parseInt(elementValue);
			else if(currentElement.equalsIgnoreCase("name"))
				bar.setName(elementValue);
		}

		public void endElement(String uri, String name, String qName) {
			if(name.equalsIgnoreCase("bar")) {
				currentElement = "";
				parentInterfaceID = 0;
				childInterfaceID = 0;
				
				bar.setBarInterface(getChildInterface(parentInterfaceID, childInterfaceID));
				bars.add(bar);
				bar = null;
			}
		}
	}

	private class ForgeGUI extends javax.swing.JFrame {
		private javax.swing.JLabel furnaceLabel;
	    private javax.swing.JComboBox furnacesComboBox;
	    private javax.swing.JLabel barLabel;
	    private javax.swing.JComboBox locationsComboBox;
	    private javax.swing.JButton startButton;
	    
	    public ForgeGUI(Object[] bars, Object[] locations) {
	        initComponents(bars, locations);
	    }

	    private void initComponents(Object[] bars, Object[] locations) {
	        furnaceLabel = new javax.swing.JLabel();
	        furnacesComboBox = new javax.swing.JComboBox();
	        barLabel = new javax.swing.JLabel();
	        locationsComboBox = new javax.swing.JComboBox();
	        startButton = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	        setTitle("Forge");
	        setPreferredSize(new java.awt.Dimension(400, 200));
	        setResizable(false);
	        setSize(new java.awt.Dimension(400, 200));
	        getContentPane().setLayout(null);

	        furnaceLabel.setText("Select a Furnace");
	        getContentPane().add(furnaceLabel);
	        furnaceLabel.setBounds(20, 20, 360, 16);
	        furnaceLabel.getAccessibleContext().setAccessibleDescription("Furnace Label");

	        furnacesComboBox.setModel(new javax.swing.DefaultComboBoxModel(locations));
	        furnacesComboBox.setToolTipText("List of available furnaces...");
	        getContentPane().add(furnacesComboBox);
	        furnacesComboBox.setBounds(20, 40, 360, 27);

	        barLabel.setText("Select a Bar");
	        getContentPane().add(barLabel);
	        barLabel.setBounds(20, 80, 360, 16);
	        barLabel.getAccessibleContext().setAccessibleDescription("Bar Label");

	        locationsComboBox.setModel(new javax.swing.DefaultComboBoxModel(bars));
	        locationsComboBox.setToolTipText("List of available bars to smelt...");
	        getContentPane().add(locationsComboBox);
	        locationsComboBox.setBounds(20, 100, 360, 27);

	        startButton.setToolTipText("Start Forging...");
	        startButton.setText("Start");
	        startButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent event) {
	                
	            }
	        });
	        getContentPane().add(startButton);
	        startButton.setBounds(280, 140, 100, 29);

	        pack();
	    }                         
	}
	
	/**
	 * Location plain ol' Java object.
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Location {
		private int furnaceID;
		private RSTile bankLocation;
		private RSTile furnaceLocation;
		private String name;

		public int getFurnaceID() {
			return furnaceID;
		}
		
		public void setFurnaceID(int furnaceID) {
			this.furnaceID = furnaceID;
		}
		
		public RSTile getBankLocation() {
			return bankLocation;
		}

		public void setBankLocation(RSTile bankLocation) {
			this.bankLocation = bankLocation;
		}
		
		public RSTile getFurnaceLocation() {
			return furnaceLocation;
		}

		public void setFurnaceLocation(RSTile location) {
			this.furnaceLocation = location;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Locations SAX2 XML Reader Handler.
	 * 
	 * Expects the following XML from an available InputStream:
	 * 
	 * <?xml version="1.0" encoding="utf-8"?>
	 * <locations>
	 *   <location>
	 *     <name>Edgeville</name>
	 *     <furnace tileX="1234" tileY="5678" id="1" />
	 *     <bank tileX="9123" tileY="4567" /> <!--Nearest Bank Tile-->
	 *   </location>
	 * </locations>
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Locations extends DefaultHandler {
		private String currentElement = "";
		private Location location = null;
		private ArrayList<Location> locations = new ArrayList<Location>();
		
		public void startElement(String uri, String name, String qName, Attributes atts) {
			if(name.equalsIgnoreCase("location")) {
				location = new Location();
			} else if(name.equalsIgnoreCase("furnace")) {
				location.setFurnaceLocation(new RSTile(Integer.parseInt(atts.getValue("tileX")), Integer.parseInt(atts.getValue("tileY"))));
				location.setFurnaceID(Integer.parseInt(atts.getValue("id")));
			} else if(name.equalsIgnoreCase("bank")) {
				location.setBankLocation(new RSTile(Integer.parseInt(atts.getValue("tileX")), Integer.parseInt(atts.getValue("tileY"))));
			} else {
				currentElement = name;
			}
		}

		public void characters(char chars[], int start, int length) {
			String elementValue = new String(chars, start, length).trim();
			if(elementValue.trim().equals("")) return ;

			if(currentElement.equalsIgnoreCase("name"))
				location.setName(elementValue);
		}

		public void endElement(String uri, String name, String qName) {
			if(name.equalsIgnoreCase("location")) {
				currentElement = "";
				locations.add(location);
			}
		}
	}
}