import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Calculations;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSInterfaceChild;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

@ScriptManifest(authors = { "Allometry" }, category = "Smithing", name = "Forge", version = 0.1,
		description = "" +
				"<html>" +
				"<head>" +
				"<style type=\"text/css\">" +
				"body {background: #000 url(http://scripts.allometry.com/app/webroot/img/gui/window.jpg);" +
				"font-family: Georgia, 'Times New Roman', Times, serif;" +
				"font-size: 12px;font-weight: normal;" +
				"padding: 50px 10px 45px 10px;}" +
				"</style>" +
				"</head>" +
				"<body>" +
				"<p style=\"text-align: center;\"><strong>Forge</strong><br /><small>Bar Smelting by Allometry</small></p>" +
				"</body>" +
				"</html>")
public class Forge extends Script implements PaintListener, ServerMessageListener {
	private boolean isVerbose = true;
	private Bar bar;
	private Bars bars = new Bars();
	private Location location;
	private Locations locations = new Locations();
	private String barXMLLocation = "http://github.com/allometry/Forge/raw/master/bars.xml";
	private String locationXMLLocation = "http://github.com/allometry/Forge/raw/master/locations.xml";
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
		
		while(forgeGUI.isVisible()) { wait(1); }
		
		if(forgeGUI.didStart) {
			bar = (Bar)forgeGUI.barsComboBox.getSelectedItem();
			location = (Location)forgeGUI.locationsComboBox.getSelectedItem();
			
			return true;
		} else {
			return false;
		}
	}
	
	private RSObject furnace = null;
	private RSTile[] currentPath = null;
	private long timeoutMillis;
	private int enterAmountParentID = 752;
	
	private long setTimeout(int seconds) {
		return System.currentTimeMillis() + (seconds * 1000);
	}
	
	private boolean isTimedOut() {
		return (System.currentTimeMillis() > timeoutMillis);
	}
	
	@Override
	public int loop() {
		if(isInventoryFull() && !inventoryContains(bar.getID())) {
			verbose("Inventory is full and doesn't contain any bars...");
			if(!bar.getBarInterface().isValid()) {
				verbose("Smelt interface isn't yet valid...");
				if(!Calculations.onScreen(Calculations.tileToScreen(location.getFurnaceLocation()))) {
					verbose("Furnace isn't yet on screen...");
					if(currentPath == null) {
						verbose("Generating path to furnace...");
						walkTo(location.getFurnaceLocation());
					}
						
						verbose("Walking current path...");
						walkPathMM(currentPath);
						if(!Calculations.onScreen(Calculations.tileToScreen(location.getFurnaceLocation())))
							return random(2700, 3500);
						else
							return 1;
				} else {
					verbose("Furnace is on screen...");
					currentPath = null;
					furnace = getObjectAt(location.getFurnaceLocation());
					
					if(furnace != null) {
						verbose("Notice! [command: smelt][object: furnace]");
						atObject(furnace, "Smelt");
						setTimeout(5);
						
						do
							wait(1);
						while(!bar.getBarInterface().isValid() || isTimedOut());
						
						return 1;
					}
				}
			} else {
				/**
				 * TODO bug 
				 * @errorLevel show-stopper
				 * @description this section of the script isn't working. I believe it is because the interface
				 * that is generated within the XML needs to be reinitialized.
				 * @date 14/10/2010
				 * @foundBy allometry
				 */
				verbose("Smelt interface is valid...");
				setTimeout(10);
				do {
					verbose("Notice! [command: smelt x][object: bar child interface]");
					atInterface(bar.getBarInterface(), "Smelt X");
					if(!getInterface(enterAmountParentID).isValid()) wait(random(2000, 2500));
				} while(!getInterface(enterAmountParentID).isValid() || isTimedOut());
				
				if(isTimedOut()) return 1;
				
				verbose("Sending random number to smelt as x...");
				sendText("" + random(28,random(45, 65)), true);
				
				setTimeout(5);
				do
					wait(1);
				while(getMyPlayer().getAnimation() < 0 || isTimedOut());
				
				if(isTimedOut()) return 1;
				
				setTimeout(5);
				do {
					if(getMyPlayer().getAnimation() > 0) setTimeout(5);
				} while(isTimedOut());
			}
		} else {
			//TODO goto bank and bank...
		}
		
		if(!isInventoryFull()) {
			//TODO goto bank
		}
		
		return 1;
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
	 * Verbose method is a log wrapper that successfully executes if the ifVerbose variable is true.
	 *
	 * @since 0.1
	 */
	private void verbose(String message) {
		if(isVerbose) log.info(message);
	}
	
	/**
	 * Bar plain ol' Java object.
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Bar {
		private int id;
		private ArrayList<Resource> resources;
		private RSInterfaceChild barInterface;
		private String name;
		
		public int getID() {
			return id;
		}
		
		public void setID(int id) {
			this.id = id;
		}
		
		public ArrayList<Resource> getResources() {
			return resources;
		}

		public void setResources(ArrayList<Resource> resources) {
			this.resources = resources;
		}
		
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
			else if(name.equalsIgnoreCase("name"))
				bar.setID(Integer.parseInt(atts.getValue("id")));
			else if(name.equalsIgnoreCase("resource"))
				bar.getResources().add(new Resource(Integer.parseInt(atts.getValue("id")), Integer.parseInt(atts.getValue("quantity"))));
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

	/**
	 * Forge Graphical User Interface
	 * 
	 * Displays options for the Forge script.
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	public class ForgeGUI extends JFrame {
		private static final long serialVersionUID = -419949261022901083L;
		private JLabel barsLabel;
		public JComboBox barsComboBox;
		private JLabel locationsLabel;
		public JComboBox locationsComboBox;
		private JButton startButton;
		public boolean didStart = false;
		
		public ForgeGUI(Object[] bars, Object[] locations) {
			initComponents(bars, locations);
		}
		
		private void initComponents(Object[] bars, Object[] locations) {
			barsLabel = new JLabel();
			barsComboBox = new JComboBox(bars);
			locationsLabel = new JLabel();
			locationsComboBox = new JComboBox(locations);
			startButton = new JButton();

			setTitle("Forge");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			barsLabel.setText("Select a Bar to Smelt...");
			contentPane.add(barsLabel);
			barsLabel.setBounds(15, 15, 350, barsLabel.getPreferredSize().height);
			contentPane.add(barsComboBox);
			barsComboBox.setBounds(15, 35, 175, barsComboBox.getPreferredSize().height);

			locationsLabel.setText("Select a Furnace Location...");
			contentPane.add(locationsLabel);
			locationsLabel.setBounds(15, 70, 350, locationsLabel.getPreferredSize().height);
			contentPane.add(locationsComboBox);
			locationsComboBox.setBounds(15, 90, 175, locationsComboBox.getPreferredSize().height);

			startButton.setText("Start");
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					didStart = true;
					setVisible(false);
				}
			});
			contentPane.add(startButton);
			startButton.setBounds(new Rectangle(new Point(280, 125), startButton.getPreferredSize()));

			contentPane.setPreferredSize(new Dimension(380, 190));
			setSize(380, 190);
			setLocationRelativeTo(getOwner());
		}
	}
	
	/**
	 * Location plain ol' Java object.
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Location {
		private RSTile bankLocation;
		private RSTile furnaceLocation;
		private String name;
		
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
			if(name.equalsIgnoreCase("location"))
				location = new Location();
			else if(name.equalsIgnoreCase("furnace"))
				location.setFurnaceLocation(new RSTile(Integer.parseInt(atts.getValue("tileX")), Integer.parseInt(atts.getValue("tileY"))));
			else if(name.equalsIgnoreCase("bank"))
				location.setBankLocation(new RSTile(Integer.parseInt(atts.getValue("tileX")), Integer.parseInt(atts.getValue("tileY"))));
			else
				currentElement = name;
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
	
	/**
	 * Resource Class
	 * 
	 * Defines a resource.
	 * 
	 * @author allometry
	 * @version 1.0
	 */
	private class Resource {
		private int id, quantity;

		public Resource() {
			
		}
		
		public Resource(int id, int quantity) {
			this.id = id;
			this.quantity = quantity;
		}
		
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}
}